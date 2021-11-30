package io.castled.apps.connectors.hubspot.client.dtos;

import lombok.Data;

@Data
public class RecordError {

    @Data
    private static class SourceData {
        private long lineNumber;
    }

    private String errorType;
    private String objectType;
    private String invalidValue;
    private String extraContext;
    private String objectTypeId;
    private long knownColumnNumber;
    private String id;
    private SourceData sourceData;

    public long getLineNumber() {
        return sourceData.getLineNumber();
    }

}
