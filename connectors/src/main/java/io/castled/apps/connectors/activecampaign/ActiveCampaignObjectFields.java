package io.castled.apps.connectors.activecampaign;

import io.castled.schema.SchemaConstants;
import io.castled.schema.models.Schema;
import lombok.Getter;

public class ActiveCampaignObjectFields {

    public enum CONTACTS_FIELDS {
        EMAIL("email","Email",SchemaConstants.STRING_SCHEMA),
        FIRST_NAME("firstName","First Name", SchemaConstants.OPTIONAL_STRING_SCHEMA),
        LAST_NAME("lastName","Last Name", SchemaConstants.OPTIONAL_STRING_SCHEMA),
        PHONE_NUMBER("phone", "Phone",SchemaConstants.OPTIONAL_STRING_SCHEMA),;

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

}
