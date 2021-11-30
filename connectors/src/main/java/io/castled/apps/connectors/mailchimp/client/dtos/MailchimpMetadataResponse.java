package io.castled.apps.connectors.mailchimp.client.dtos;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.Getter;

import java.util.Optional;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class MailchimpMetadataResponse {
    private String dc;
    private Login login;
    private String apiEndpoint;

    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    @Data
    static class Login {

        @Getter
        private String loginEmail;
    }

    public String getLoginEmail() {
        return Optional.ofNullable(login).map(Login::getLoginEmail).orElse(null);
    }
}
