package io.castled.apps.connectors.mixpanel;

import io.castled.schema.SchemaConstants;
import io.castled.schema.models.Schema;
import lombok.Getter;

public class MixpanelObjectFields {

    public enum USER_PROFILE_FIELDS {
        DISTINCT_ID("distinct_id","Distinct ID",SchemaConstants.STRING_SCHEMA),
        LAST_NAME("last_name","Last Name", SchemaConstants.STRING_SCHEMA),
        FIRST_NAME("first_name","First Name", SchemaConstants.STRING_SCHEMA),
        EMAIL("email","Email", SchemaConstants.STRING_SCHEMA);

        USER_PROFILE_FIELDS(String fieldName, String fieldTitle , Schema schema) {
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

    public enum GROUP_PROFILE_FIELDS {
        GROUP_KEY("group_key","Group Key",SchemaConstants.STRING_SCHEMA),
        GROUP_ID("group_id","Group ID", SchemaConstants.STRING_SCHEMA);

        GROUP_PROFILE_FIELDS(String fieldName, String fieldTitle , Schema schema) {
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
        EVENT_NAME("event","Email",SchemaConstants.STRING_SCHEMA),
        INSERT_ID("insert_id","Insert ID", SchemaConstants.STRING_SCHEMA),
        DISTINCT_ID("distinct_id","Distinct ID", SchemaConstants.STRING_SCHEMA),
        GEO_IP("ip","GEO IP", SchemaConstants.STRING_SCHEMA),
        EVENT_TIMESTAMP("time","Event Timestamp", SchemaConstants.STRING_SCHEMA);

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
