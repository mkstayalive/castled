package io.castled.apps.connectors.mailchimp;

import com.google.inject.Singleton;
import io.castled.ObjectRegistry;
import io.castled.apps.BufferedObjectSink;
import io.castled.apps.OAuthAppConfig;
import io.castled.apps.connectors.mailchimp.client.MailchimpRestClient;
import io.castled.apps.connectors.mailchimp.client.dtos.MailchimpMember;
import io.castled.apps.connectors.mailchimp.client.dtos.MemberAddress;
import io.castled.apps.connectors.mailchimp.client.dtos.MemberMergeFields;
import io.castled.apps.connectors.mailchimp.client.models.MemberAndError;
import io.castled.commons.models.MessageSyncStats;
import io.castled.commons.streams.ErrorOutputStream;
import io.castled.schema.models.Message;
import io.castled.schema.models.Tuple;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;


@Singleton
public class MailchimpAudienceSink extends BufferedObjectSink<Message> {

    private final MailchimpRestClient mailchimpRestClient;
    private final MailchimpErrorParser mailchimpErrorParser;
    private final ErrorOutputStream errorOutputStream;
    private final MailchimpAudienceSyncObject audienceSyncObject;
    private final AtomicLong processedRecords = new AtomicLong(0);
    private long lastProcessedOffset = 0;

    public MailchimpAudienceSink(OAuthAppConfig mailchimpAppConfig,
                                 ErrorOutputStream errorOutputStream,
                                 MailchimpAudienceSyncObject audienceSyncObject) {
        this.mailchimpRestClient = new MailchimpRestClient(mailchimpAppConfig.getOAuthToken());
        this.errorOutputStream = errorOutputStream;
        this.mailchimpErrorParser = ObjectRegistry.getInstance(MailchimpErrorParser.class);
        this.audienceSyncObject = audienceSyncObject;
    }

    @Override
    protected void writeRecords(List<Message> messages) {

        Map<String, Message> emailRecordMapper = messages.stream().filter(message -> getEmail(message.getRecord()) != null)
                .collect(Collectors.toMap(message -> getEmail(message.getRecord()), Function.identity()));
        List<MemberAndError> failedRecords = this.mailchimpRestClient.upsertMembers(
                audienceSyncObject.getAudienceId(), messages.stream().map(Message::getRecord).map(this::getMailchimpMember).collect(Collectors.toList()));
        for (MemberAndError recordAndError : failedRecords) {
            this.errorOutputStream.writeFailedRecord(emailRecordMapper.get(recordAndError.getMember().getEmailAddress()),
                    mailchimpErrorParser.getPipelineError(recordAndError.getOperationError()));
        }
        this.processedRecords.addAndGet(messages.size());
        this.lastProcessedOffset = Math.max(lastProcessedOffset, messages.get(messages.size() - 1).getOffset());
    }

    private String getEmail(Tuple record) {
        return (String) record.getValue(MailchimpObjectFields.AUDIENCE_FIELDS.EMAIL.getFieldName());
    }

    private MailchimpMember getMailchimpMember(Tuple record) {
        String email = getEmail(record);
        String firstName = (String) record.getValue(MailchimpObjectFields.AUDIENCE_FIELDS.FIRST_NAME.getFieldName());
        String lastName = (String) record.getValue(MailchimpObjectFields.AUDIENCE_FIELDS.LAST_NAME.getFieldName());
        String phoneNumber = (String) record.getValue(MailchimpObjectFields.AUDIENCE_FIELDS.PHONE_NUMBER.getFieldName());

        MemberMergeFields memberMergeFields = MemberMergeFields.builder().ADDRESS(getMemberAddress(record)).FNAME(firstName)
                .LNAME(lastName).PHONE(phoneNumber).build();

        return MailchimpMember.builder().emailAddress(email).status("subscribed").mergeFields(memberMergeFields).build();
    }

    private MemberAddress getMemberAddress(Tuple record) {
        String addressLine1 = (String) record.getValue(MailchimpObjectFields.AUDIENCE_FIELDS.ADDRESS_LINE_1.getFieldName());
        String addressLine2 = (String) record.getValue(MailchimpObjectFields.AUDIENCE_FIELDS.ADDRESS_LINE_2.getFieldName());
        String city = (String) record.getValue(MailchimpObjectFields.AUDIENCE_FIELDS.CITY.getFieldName());
        String state = (String) record.getValue(MailchimpObjectFields.AUDIENCE_FIELDS.STATE.getFieldName());
        String zipcode = (String) record.getValue(MailchimpObjectFields.AUDIENCE_FIELDS.ZIPCODE.getFieldName());
        String country = (String) record.getValue(MailchimpObjectFields.AUDIENCE_FIELDS.COUNTRY.getFieldName());

        if (addressLine1 == null && addressLine2 == null && city == null && state == null && zipcode == null && country == null) {
            return null;
        }
        return MemberAddress.builder().addr1(addressLine1).addr2(addressLine2)
                .city(city).state(state).zip(zipcode).country(country).build();
    }

    public MessageSyncStats getSyncStats() {
        return new MessageSyncStats(processedRecords.get(), lastProcessedOffset);
    }

    @Override
    public long getMaxBufferedObjects() {
        return 10000;
    }
}
