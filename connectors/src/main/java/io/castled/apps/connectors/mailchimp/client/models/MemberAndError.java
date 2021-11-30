package io.castled.apps.connectors.mailchimp.client.models;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.castled.apps.connectors.mailchimp.client.dtos.MailchimpMember;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@AllArgsConstructor
@NoArgsConstructor
public class MemberAndError {
    private MailchimpMember member;
    private MailchimpOperationError operationError;
}
