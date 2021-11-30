package io.castled.apps.connectors.customerio;

import io.castled.apps.AppConfig;
import io.castled.forms.FormField;
import io.castled.forms.FormFieldSchema;
import io.castled.forms.FormFieldType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerIOAppConfig extends AppConfig {

    @FormField(description = "Site Id", title = "Site Id", schema = FormFieldSchema.STRING, type = FormFieldType.TEXT_BOX)
    private String siteId;

    @FormField(description = "Tracking API Key", title = "API Key", schema = FormFieldSchema.STRING, type = FormFieldType.TEXT_BOX)
    private String apiKey;
}
