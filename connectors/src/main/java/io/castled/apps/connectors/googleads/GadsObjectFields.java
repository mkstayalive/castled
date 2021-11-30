package io.castled.apps.connectors.googleads;

import io.castled.schema.SchemaConstants;
import io.castled.schema.models.Schema;
import lombok.Getter;

public class GadsObjectFields {

    public enum CUSTOMER_MATCH_CONTACT_INFO_FIELDS {
        EMAIL("email"),
        FIRST_NAME("first name"),
        LAST_NAME("last name"),
        PHONE_NUMBER("phone number"),
        COUNTRY_CODE("country code"),
        POSTAL_CODE("postal code"),
        CITY("city");

        @Getter
        private final String fieldName;

        CUSTOMER_MATCH_CONTACT_INFO_FIELDS(String fieldName) {
            this.fieldName = fieldName;

        }
    }

    public static final String CUSTOMER_MATCH_USER_ID_FIELD = "User Id";

    public static final String CUSTOMER_MATCH_MOBILE_DEVICE_ID_FIELD = "Mobile Device Id";

    public enum CLICK_CONVERSION_STANDARD_FIELDS {
        CONVERSION_VALUE("Conversion value", SchemaConstants.OPTIONAL_DOUBLE_SCHEMA),
        CONVERSION_TIME("Conversion time", SchemaConstants.OPTIONAL_TIMESTAMP_SCHEMA),
        CURRENCY_CODE("Currency Code", SchemaConstants.OPTIONAL_STRING_SCHEMA),
        ORDER_ID("Order Id", SchemaConstants.OPTIONAL_STRING_SCHEMA),
        GCLID("Google Click Id", SchemaConstants.STRING_SCHEMA);


        CLICK_CONVERSION_STANDARD_FIELDS(String fieldName, Schema schema) {
            this.fieldName = fieldName;
            this.schema = schema;
        }

        @Getter
        private final String fieldName;

        @Getter
        private final Schema schema;


    }

    public enum CALL_CONVERSION_STANDARD_FIELDS {
        CONVERSION_VALUE("Conversion value", SchemaConstants.OPTIONAL_DOUBLE_SCHEMA),
        CONVERSION_TIME("Conversion time", SchemaConstants.TIMESTAMP_SCHEMA),
        CURRENCY_CODE("Currency Code", SchemaConstants.OPTIONAL_STRING_SCHEMA),
        CALL_START_TIME("Call Start Time", SchemaConstants.OPTIONAL_TIMESTAMP_SCHEMA),
        CALLER_ID("Caller Id", SchemaConstants.STRING_SCHEMA);


        CALL_CONVERSION_STANDARD_FIELDS(String fieldName, Schema schema) {
            this.fieldName = fieldName;
            this.schema = schema;
        }

        @Getter
        private final String fieldName;

        @Getter
        private final Schema schema;

    }
}
