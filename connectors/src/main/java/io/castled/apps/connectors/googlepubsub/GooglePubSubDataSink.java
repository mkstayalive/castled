package io.castled.apps.connectors.googlepubsub;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.api.gax.batching.BatchingSettings;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import io.castled.apps.DataSink;
import io.castled.apps.models.DataSinkRequest;
import io.castled.commons.models.AppSyncStats;
import io.castled.schema.models.Message;
import io.castled.utils.MessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.threeten.bp.Duration;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class GooglePubSubDataSink implements DataSink {

    public static final long REQUEST_BYTES_THRESHOLD = 10485760L;
    public static final long MESSAGE_COUNT_BATCH_SIZE = 1000L;
    public static final int PUBLISH_DELAY_THRESHOLD = 1000;
    private static final long FLUSH_BATCH_SIZE = 10000L;

    private final AtomicLong recordsProcessed = new AtomicLong(0);
    private final Set<Long> pendingMessageIds = Sets.newConcurrentHashSet();
    private long lastBufferedOffset = 0;
    private volatile Exception exception;

    @Override
    public void syncRecords(DataSinkRequest dataSinkRequest) throws Exception {
        GooglePubSubAppConfig googlePubSubAppConfig = (GooglePubSubAppConfig) dataSinkRequest.getExternalApp().getConfig();
        GooglePubSubAppSyncConfig googlePubSubAppSyncConfig = (GooglePubSubAppSyncConfig) dataSinkRequest.getAppSyncConfig();

        TopicName topicName = TopicName.of(googlePubSubAppConfig.getProjectID(), googlePubSubAppSyncConfig.getObject().getTopicId());
        Publisher publisher = null;
        List<ApiFuture<String>> messageIdFutures = new ArrayList<>();

        try {
            // Batch settings control how the publisher batches messages
            BatchingSettings batchingSettings = BatchingSettings.newBuilder().setElementCountThreshold(MESSAGE_COUNT_BATCH_SIZE)
                    .setRequestByteThreshold(REQUEST_BYTES_THRESHOLD).setDelayThreshold(Duration.ofMillis(PUBLISH_DELAY_THRESHOLD)).build();

            // Create a publisher instance with default settings bound to the topic
            publisher = Publisher.newBuilder(topicName).setBatchingSettings(batchingSettings)
                    .setCredentialsProvider(new GooglePubSubCredentialsProvider(googlePubSubAppConfig.getServiceAccountDetails())).build();

            Message message;
            while ((message = dataSinkRequest.getMessageInputStream().readMessage()) != null) {
                messageIdFutures.add(publishMessage(publisher, message));
                if (messageIdFutures.size() == FLUSH_BATCH_SIZE) {
                    publishOutstanding(publisher, messageIdFutures);
                    messageIdFutures.clear();
                }
            }
            publishOutstanding(publisher, messageIdFutures);
        } finally {
            if (publisher != null) {
                publisher.shutdown();
                publisher.awaitTermination(1, TimeUnit.MINUTES);
            }
        }
    }

    private void publishOutstanding(Publisher publisher, List<ApiFuture<String>> messageIdFutures) throws Exception {
        if (CollectionUtils.isEmpty(messageIdFutures)) {
            return;
        }
        publisher.publishAllOutstanding();
        ApiFutures.allAsList(messageIdFutures).get();
        validateAndThrow();
    }

    private ApiFuture<String> publishMessage(Publisher publisher, Message message) throws Exception {
        pendingMessageIds.add(message.getOffset());
        PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(ByteString.copyFrom(MessageUtils.messageToBytes(message))).build();
        ApiFuture<String> messageIdFuture = publisher.publish(pubsubMessage);
        lastBufferedOffset = message.getOffset();
        ApiFutures.addCallback(messageIdFuture, new DataSinkCallback(message.getOffset()),
                MoreExecutors.directExecutor());

        validateAndThrow();
        return messageIdFuture;
    }

    @Override
    public AppSyncStats getSyncStats() {
        return new AppSyncStats(recordsProcessed.get(), getProcessedOffset(), 0);
    }

    public long getProcessedOffset() {
        try {
            long currentMinPendingId = Collections.min(pendingMessageIds);
            return currentMinPendingId - 1;
        } catch (NoSuchElementException e) {
            return lastBufferedOffset;
        }
    }

    private void validateAndThrow() throws Exception {
        if (exception != null) {
            throw exception;
        }
    }

    public class DataSinkCallback implements ApiFutureCallback<String> {

        private final long messageOffset;

        public DataSinkCallback(long messageOffset) {
            this.messageOffset = messageOffset;
        }

        @Override
        public void onFailure(Throwable throwable) {
            exception = (Exception) throwable;
        }

        @Override
        public void onSuccess(String messageId) {
            recordsProcessed.incrementAndGet();
            pendingMessageIds.remove(messageOffset);
        }
    }
}