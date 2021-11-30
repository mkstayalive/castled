package io.castled.apps.connectors.salesforce.client.dtos;

import lombok.Data;

@Data
public class Job {

    private String id;
    private String object;
    private Double apiVersion;

    private String contentType;
    private String contentUrl;
    private String lineEnding;

    private String createdById;
    private String createdDate;

    private String externalIdFieldName;
    private String operation;

    private JobState state;
    private String systemModstamp;
}
