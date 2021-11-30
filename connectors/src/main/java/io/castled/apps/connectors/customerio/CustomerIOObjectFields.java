package io.castled.apps.connectors.customerio;

import io.castled.schema.SchemaConstants;
import io.castled.schema.models.Schema;
import lombok.Getter;

public class CustomerIOObjectFields {

    public enum CONTACTS_FIELDS {
        EMAIL("email","Email",SchemaConstants.STRING_SCHEMA),
        ID("id","ID", SchemaConstants.STRING_SCHEMA);

        CONTACTS_FIELDS(String fieldName, String fieldTitle , Schema schema) {
            this.fieldName = fieldName;
            this.fieldTitle = fieldTitle;
            this.schema = schema;
        }

        @Getter
        private final String fieldName;

        @Getter
        private final String fieldTitle;

        @Getter
        private final Schema schema;
    }

    public enum EVENT_FIELDS {
        EMAIL("email","Email",SchemaConstants.STRING_SCHEMA),
        CUSTOMER_ID("customer_id","ID", SchemaConstants.STRING_SCHEMA),
        EVENT_NAME("name","Event Name", SchemaConstants.STRING_SCHEMA),
        EVENT_ID("event_id","Event Id", SchemaConstants.STRING_SCHEMA),
        PAGE_URL("page_URL","Page URL", SchemaConstants.STRING_SCHEMA),
        EVENT_TIMESTAMP("timestamp","Event Timestamp", SchemaConstants.STRING_SCHEMA);

        EVENT_FIELDS(String fieldName, String fieldTitle , Schema schema) {
            this.fieldName = fieldName;
            this.fieldTitle = fieldTitle;
            this.schema = schema;
        }

        @Getter
        private final String fieldName;

        @Getter
        private final String fieldTitle;

        @Getter
        private final Schema schema;
    }

}
