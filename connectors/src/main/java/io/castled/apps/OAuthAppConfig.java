package io.castled.apps;

import io.castled.forms.FormField;
import io.castled.forms.FormFieldSchema;
import io.castled.forms.FormFieldType;
import io.castled.oauth.OAuthClientConfig;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OAuthAppConfig extends AppConfig {

    @FormField(description = "Client id of the registered oauth app", title = "OAuth Client id", schema = FormFieldSchema.STRING, type = FormFieldType.TEXT_BOX)
    private String clientId;

    @FormField(description = "Client Secret of the registered oauth app", title = "OAuth Client secret", schema = FormFieldSchema.STRING, type = FormFieldType.TEXT_BOX)
    private String clientSecret;

    private Long oAuthToken;

    public OAuthClientConfig getClientConfig() {
        return new OAuthClientConfig(clientId, clientSecret);
    }
}
