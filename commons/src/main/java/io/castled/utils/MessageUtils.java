package io.castled.utils;

import io.castled.schema.models.Field;
import io.castled.schema.models.Message;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class MessageUtils {

    public static byte[] messageToBytes(Message message) {
        Map<String, Object> messageMap = message.getRecord().getFields()
                .stream().collect(Collectors.toMap(Field::getName, MessageUtils::getJsonValue));
        return JsonUtils.objectToByteArray(messageMap);
    }

    private static Object getJsonValue(Field field) {
        switch (field.getSchema().getType()) {
            case DATE:
                LocalDate localDate = (LocalDate) field.getValue();
                return localDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
            case TIMESTAMP:
                LocalDateTime localDateTime = (LocalDateTime) field.getValue();
                return localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            case ZONED_TIMESTAMP:
                ZonedDateTime zonedDateTime = (ZonedDateTime) field.getValue();
                return zonedDateTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
            case TIME:
                LocalTime localTime = (LocalTime) field.getValue();
                return localTime.format(DateTimeFormatter.ISO_TIME);
            default:
                return field.getValue();
        }

    }
}
