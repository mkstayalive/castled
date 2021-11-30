package io.castled.apps.connectors.mailchimp;

import io.castled.schema.SchemaConstants;
import io.castled.schema.models.Schema;
import lombok.Getter;

public class MailchimpObjectFields {

    public enum AUDIENCE_FIELDS {
        EMAIL("email", SchemaConstants.STRING_SCHEMA),
        BIRTHDAY("Birth Day", SchemaConstants.DATE_SCHEMA),
        FIRST_NAME("First Name", SchemaConstants.OPTIONAL_STRING_SCHEMA),
        LAST_NAME("Last Name", SchemaConstants.OPTIONAL_STRING_SCHEMA),
        PHONE_NUMBER("Phone Number", SchemaConstants.OPTIONAL_STRING_SCHEMA),
        ADDRESS_LINE_1("Address Line 1", SchemaConstants.OPTIONAL_STRING_SCHEMA),
        ADDRESS_LINE_2("Address Line 2", SchemaConstants.OPTIONAL_STRING_SCHEMA),
        CITY("City", SchemaConstants.OPTIONAL_STRING_SCHEMA),
        STATE("State", SchemaConstants.OPTIONAL_STRING_SCHEMA),
        ZIPCODE("Zip Code", SchemaConstants.OPTIONAL_STRING_SCHEMA),
        COUNTRY("Country", SchemaConstants.OPTIONAL_STRING_SCHEMA);

        AUDIENCE_FIELDS(String fieldName, Schema schema) {
            this.fieldName = fieldName;
            this.schema = schema;
        }

        @Getter
        private final String fieldName;

        @Getter
        private final Schema schema;
    }

}
