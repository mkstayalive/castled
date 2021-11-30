package io.castled.apps.connectors.activecampaign.models;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.castled.apps.connectors.activecampaign.dto.Contact;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@AllArgsConstructor
@NoArgsConstructor
public class ContactAndError {
    private Contact contact;
    //private List<Contact> contactList;
    //private ActiveCampaignOperationError operationError;
    private List<String> failureReasons;
}
