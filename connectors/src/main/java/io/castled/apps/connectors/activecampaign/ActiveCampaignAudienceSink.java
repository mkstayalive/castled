package io.castled.apps.connectors.activecampaign;

import com.google.common.collect.Iterables;
import com.google.inject.Singleton;
import io.castled.ObjectRegistry;
import io.castled.apps.BufferedObjectSink;
import io.castled.apps.connectors.activecampaign.client.ActiveCampaignRestClient;
import io.castled.apps.connectors.activecampaign.constant.ActiveCampaignConstants;
import io.castled.apps.connectors.activecampaign.dto.Contact;
import io.castled.apps.connectors.activecampaign.dto.FieldValue;
import io.castled.apps.connectors.activecampaign.models.ContactAndError;
import io.castled.apps.models.GenericSyncObject;
import io.castled.commons.models.MessageSyncStats;
import io.castled.commons.streams.ErrorOutputStream;
import io.castled.schema.models.Field;
import io.castled.schema.models.Message;
import io.castled.schema.models.Tuple;
import io.castled.utils.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;


@Singleton
public class ActiveCampaignAudienceSink extends BufferedObjectSink<Message> {

    private final ActiveCampaignRestClient activeCampaignRestClient;
    private final ActiveCampaignErrorParser activeCampaignErrorParser;
    private final ErrorOutputStream errorOutputStream;
    private final GenericSyncObject audienceSyncObject;
    private final AtomicLong processedRecords = new AtomicLong(0);
    private long lastProcessedOffset = 0;

    public ActiveCampaignAudienceSink(ActiveCampaignAppConfig activeCampaignAppConfig,
                                      ErrorOutputStream errorOutputStream,
                                      GenericSyncObject audienceSyncObject) {
        this.activeCampaignRestClient = new ActiveCampaignRestClient(activeCampaignAppConfig.getApiURL(),activeCampaignAppConfig.getApiKey());
        this.errorOutputStream = errorOutputStream;
        this.activeCampaignErrorParser = ObjectRegistry.getInstance(ActiveCampaignErrorParser.class);
        this.audienceSyncObject = audienceSyncObject;
    }

    @Override
    protected void writeRecords(List<Message> messages) {

        //Messages with missing emails.
        List<Message> messagesWithMissingEmails= messages.stream().filter(message -> StringUtils.nullIfEmpty(getEmail(message.getRecord()))==null).collect(Collectors.toList());

        //Write to error streams message entries with missing email.
        messagesWithMissingEmails.forEach(message -> this.errorOutputStream.writeFailedRecord(message,activeCampaignErrorParser.getMissingRequiredFieldError("email")));

        messages.removeAll(messagesWithMissingEmails);

        List<ContactAndError> failedRecords = this.activeCampaignRestClient.upsertContacts(
                messages.stream().map(Message::getRecord).map(this::constructContact).collect(Collectors.toList()));

        Map<String, Message> emailRecordMapper = messages.stream().filter(message -> getEmail(message.getRecord()) != null)
                .collect(Collectors.toMap(message -> getEmail(message.getRecord()), Function.identity()));

        failedRecords.forEach(failedRecord ->
                failedRecord.getFailureReasons().forEach(failureReason -> this.errorOutputStream.writeFailedRecord(emailRecordMapper.get(failedRecord.getContact().getEmail()),
                activeCampaignErrorParser.getPipelineError(failureReason))));

        /*failedRecords.stream().forEach(failedRecord -> {

            failedRecord.getContactList().stream().forEach(contact -> {
                this.errorOutputStream.writeFailedRecord(emailRecordMapper.get(contact.getEmail()),
                        activeCampaignErrorParser.getPipelineError(failedRecord.getOperationError()));
            });

            this.errorOutputStream.writeFailedRecord(emailRecordMapper.get(failedRecord.getContact().getEmail()),
                    activeCampaignErrorParser.getPipelineError(failedRecord.getOperationError()));
        });*/

        this.processedRecords.addAndGet(messages.size());
        this.lastProcessedOffset = Math.max(lastProcessedOffset, Iterables.getLast(messages).getOffset());
    }

    private String getEmail(Tuple record) {
        return (String) record.getValue(ActiveCampaignObjectFields.CONTACTS_FIELDS.EMAIL.getFieldName());
    }

    private Contact constructContact(Tuple record) {

        //Primary fields
        String email = getEmail(record);
        String firstName = (String) record.getValue(ActiveCampaignObjectFields.CONTACTS_FIELDS.FIRST_NAME.getFieldName());
        String lastName = (String) record.getValue(ActiveCampaignObjectFields.CONTACTS_FIELDS.LAST_NAME.getFieldName());
        String phoneNumber = (String) record.getValue(ActiveCampaignObjectFields.CONTACTS_FIELDS.PHONE_NUMBER.getFieldName());

        //Custom fields
        List<FieldValue> fieldValues = record.getFields().stream().filter(fieldRef -> (boolean)fieldRef.getParams().get(ActiveCampaignConstants.CUSTOM_FIELD_INDICATOR)).
                map(field -> FieldValue.builder().id(transformFieldId(field)).value(transformFieldValue(field)).build()).collect(Collectors.toList());

        return Contact.builder().email(email).first_name(firstName).last_name(lastName).phone(phoneNumber).fields(fieldValues).build();
    }

    private Integer transformFieldId(Field field)
    {
        return (Integer) Optional.ofNullable(field.getParams().get(ActiveCampaignConstants.CUSTOM_FIELD_ID))
                .filter(objectRef -> objectRef instanceof Integer).orElse(null);
    }

    private String transformFieldValue(Field field)
    {
        Object object = field.getValue();
        if(object instanceof Integer) {
            return String.valueOf(object);
        }
        else if(object instanceof String) {
            return (String) object;
        }
        else if (object instanceof LocalDate) {
            return ((LocalDate) object).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        else if (object instanceof LocalDateTime) {
            return ((LocalDateTime) object).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ"));
        }
        return null;
    }


    public MessageSyncStats getSyncStats() {
        return new MessageSyncStats(processedRecords.get(), lastProcessedOffset);
    }

    @Override
    public long getMaxBufferedObjects() {
        return 250;
    }
}
