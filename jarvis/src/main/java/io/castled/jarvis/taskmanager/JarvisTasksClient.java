package io.castled.jarvis.taskmanager;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.castled.exceptions.CastledException;
import io.castled.jarvis.taskmanager.daos.JarvisTasksDAO;
import io.castled.jarvis.taskmanager.exceptions.JarvisException;
import io.castled.jarvis.taskmanager.models.JarvisTaskClientConfig;
import io.castled.jarvis.taskmanager.models.JarvisKafkaConfig;
import io.castled.jarvis.taskmanager.models.Task;
import io.castled.jarvis.taskmanager.models.TaskGroup;
import io.castled.jarvis.taskmanager.models.requests.TaskCreateRequest;
import io.castled.kafka.producer.KafkaProducerConfiguration;
import io.castled.kafka.producer.CastledKafkaProducer;
import io.castled.utils.JsonUtils;
import io.castled.utils.ThreadUtils;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Singleton
public class JarvisTasksClient implements AutoCloseable {

    private final JarvisTasksService jarvisTasksService;
    private final ExecutorService requestsConsumerService;
    private final CastledKafkaProducer kafkaProducer;

    @Inject
    public JarvisTasksClient(JarvisTaskClientConfig jarvisTaskClientConfig) {
        JarvisTasksDAO jarvisTasksDAO = jarvisTaskClientConfig.getJdbi().onDemand(JarvisTasksDAO.class);
        JarvisKafkaConfig jarvisKafkaConfig = jarvisTaskClientConfig.getJarvisKafkaConfig();
        this.kafkaProducer = Optional.ofNullable(jarvisKafkaConfig.getCastledKafkaProducer())
                .orElse(initializeKafkaProducer(jarvisKafkaConfig));
        this.requestsConsumerService = Executors.newFixedThreadPool(jarvisKafkaConfig.getConsumerCount());

        Map<String, JesqueTasksClient> jesqueClientPool = Maps.newHashMap();
        for (TaskGroup taskGroup : jarvisTaskClientConfig.getTaskGroups()) {
            jesqueClientPool.put(taskGroup.getGroup(),
                    new JesqueTasksClient(jarvisTaskClientConfig.getJedisPool(), jarvisTasksDAO, taskGroup,
                            kafkaProducer));
        }
        this.jarvisTasksService = new JarvisTasksService(jarvisTasksDAO, kafkaProducer, jesqueClientPool);
        for (int i = 0; i < jarvisKafkaConfig.getConsumerCount(); i++) {
            requestsConsumerService.submit(new JarvisRequestsConsumer(jarvisTaskClientConfig.getJarvisKafkaConfig(), jarvisTasksService));
        }
    }

    public void createTaskSync(TaskCreateRequest taskCreateRequest) throws JarvisException {
        this.jarvisTasksService.createTask(taskCreateRequest);
    }

    public List<Task> getTasksBySearchId(String searchId, String taskType) throws JarvisException {
        return this.jarvisTasksService.getTasksBySearchId(searchId, taskType);

    }

    public void createTask(TaskCreateRequest taskCreateRequest) throws CastledException {
        String partitionKey = taskCreateRequest.getType() + "_" + Optional.ofNullable(taskCreateRequest.getUniqueId()).orElse("");
        ProducerRecord<byte[], byte[]> producerRecord = new ProducerRecord<>(JarvisConstants.JARVIS_EVENTS_TOPIC,
                partitionKey.getBytes(), JsonUtils.objectToByteArray(taskCreateRequest));
        this.kafkaProducer.publishSync(producerRecord);
    }

    private CastledKafkaProducer initializeKafkaProducer(JarvisKafkaConfig jarvisKafkaConfig) {
        KafkaProducerConfiguration producerConfiguration = KafkaProducerConfiguration
                .builder().bootstrapServers(jarvisKafkaConfig.getBootstrapServers()).build();
        return new CastledKafkaProducer(producerConfiguration);
    }

    @Override
    public void close() {
        this.kafkaProducer.close();
        ThreadUtils.terminateGracefully(requestsConsumerService, 5);
        jarvisTasksService.close();
    }

    public JarvisTasksService getJarvisTasksService() {
        return jarvisTasksService;
    }
}
