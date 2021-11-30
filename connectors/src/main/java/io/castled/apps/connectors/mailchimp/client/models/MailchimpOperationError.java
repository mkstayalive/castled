package io.castled.apps.connectors.mailchimp.client.models;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class MailchimpOperationError {
    private int statusCode;
    private String operationId;
    private String response;
}
