package io.castled.apps.connectors.marketo;

import io.castled.apps.AppConfig;
import io.castled.forms.FormField;
import io.castled.forms.FormFieldSchema;
import io.castled.forms.FormFieldType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MarketoAppConfig extends AppConfig  {
    @FormField(description = "Marketo Base url", title = "Base Url", schema = FormFieldSchema.STRING, type = FormFieldType.TEXT_BOX)
    private String baseUrl;

    @FormField(description = "Client ID", title = "Client ID", schema = FormFieldSchema.STRING, type = FormFieldType.TEXT_BOX)
    private String clientId;

    @FormField(description = "Client Secret", title = "Client Secret", schema = FormFieldSchema.STRING, type = FormFieldType.TEXT_BOX)
    private String clientSecret;
}
