package io.castled.apps.connectors.sendgrid;

import io.castled.schema.models.Schema;
import io.castled.schema.models.SchemaType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

// Performs any formatting before sending a request
// TODO: Create a RequestFormatterUtils interface
public class SendgridRequestFormatterUtils {

    public static Object formatValue(Object value, Schema schema) {
        if (schema.getType() == SchemaType.DATE) {
            return DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.ENGLISH).format((LocalDate)value);
        }
        return value;
    }
}
