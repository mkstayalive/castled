package io.castled.apps.connectors.hubspot;

import io.castled.apps.connectors.hubspot.client.dtos.BatchObjectError;

import io.castled.commons.errors.CastledError;
import io.castled.commons.errors.CastledErrorCode;
import io.castled.commons.errors.errorclassifications.InvalidFieldValueError;
import io.castled.utils.JsonUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestHubspotErrorParser {

    @Test
    public void parseError() {
        HubspotErrorParser hubspotErrorParser = new HubspotErrorParser();
        String batchErrorMessage = "{\n" +
                "    \"status\": \"error\",\n" +
                "    \"message\": \"Property values were not valid: [{\\\"isValid\\\":false,\\\"message\\\":\\\"PROSPECT1 was not one of the allowed options: [label: \\\\\\\"Prospect\\\\\\\"\\\\nvalue: \\\\\\\"PROSPECT\\\\\\\"\\\\ndisplay_order: 0\\\\ndouble_data: 0.0\\\\nhidden: false\\\\ndescription: \\\\\\\"\\\\\\\"\\\\nread_only: false\\\\n, label: \\\\\\\"Partner\\\\\\\"\\\\nvalue: \\\\\\\"PARTNER\\\\\\\"\\\\ndisplay_order: 1\\\\ndouble_data: 0.0\\\\nhidden: false\\\\ndescription: \\\\\\\"\\\\\\\"\\\\nread_only: false\\\\n, label: \\\\\\\"Reseller\\\\\\\"\\\\nvalue: \\\\\\\"RESELLER\\\\\\\"\\\\ndisplay_order: 2\\\\ndouble_data: 0.0\\\\nhidden: false\\\\ndescription: \\\\\\\"\\\\\\\"\\\\nread_only: false\\\\n, label: \\\\\\\"Vendor\\\\\\\"\\\\nvalue: \\\\\\\"VENDOR\\\\\\\"\\\\ndisplay_order: 3\\\\ndouble_data: 0.0\\\\nhidden: false\\\\ndescription: \\\\\\\"\\\\\\\"\\\\nread_only: false\\\\n, label: \\\\\\\"Other\\\\\\\"\\\\nvalue: \\\\\\\"OTHER\\\\\\\"\\\\ndisplay_order: 4\\\\ndouble_data: 0.0\\\\nhidden: false\\\\ndescription: \\\\\\\"\\\\\\\"\\\\nread_only: false\\\\n]\\\",\\\"error\\\":\\\"INVALID_OPTION\\\",\\\"name\\\":\\\"type\\\"}]\",\n" +
                "    \"correlationId\": \"b4da824b-7b8d-46d7-9ef8-5a951cbf63d7\",\n" +
                "    \"category\": \"VALIDATION_ERROR\"\n" +
                "}";

        BatchObjectError batchObjectError = JsonUtils.jsonStringToObject(batchErrorMessage, BatchObjectError.class);
        CastledError pipelineError = hubspotErrorParser.parseError(batchObjectError);
        assertEquals(pipelineError.getErrorCode(), CastledErrorCode.INVALID_FIELD_VALUE);
        InvalidFieldValueError invalidFieldValueError = (InvalidFieldValueError) pipelineError;
        assertEquals("INVALID_OPTION", invalidFieldValueError.getErrorType());
        assertEquals("type", invalidFieldValueError.getFieldName());

    }
}