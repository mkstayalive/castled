package io.castled.apps.connectors.mixpanel;

import io.castled.apps.AppConfig;
import io.castled.forms.FormField;
import io.castled.forms.FormFieldSchema;
import io.castled.forms.FormFieldType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MixpanelAppConfig extends AppConfig {

    @FormField(description = "Project Token", title = "Project Token", schema = FormFieldSchema.STRING, type = FormFieldType.TEXT_BOX)
    private String projectToken;

    @FormField(description = "API Secret", title = "API Secret", schema = FormFieldSchema.STRING, type = FormFieldType.TEXT_BOX)
    private String apiSecret;
}
