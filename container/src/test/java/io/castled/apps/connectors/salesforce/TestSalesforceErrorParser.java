package io.castled.apps.connectors.salesforce;

import io.castled.commons.errors.CastledError;
import io.castled.commons.errors.CastledErrorCode;
import org.junit.Assert;
import org.junit.Test;

public class TestSalesforceErrorParser {

    @Test
    public void parseSalesforceError() {
        SalesforceErrorParser salesforceErrorParser = new SalesforceErrorParser();
        CastledError pipelineError = salesforceErrorParser.
                parseSalesforceError("DUPLICATE_VALUE:duplicate value found: email__c duplicates value on record with id: a0N2w000001WQcq, id__c duplicates value on record with id: a0N2w000001WQcq:--");
        Assert.assertEquals(pipelineError.getErrorCode(), CastledErrorCode.DUPLICATE_UNIQUE_KEY_VALUES);
    }
}