package io.castled.apps.connectors.activecampaign;

import io.castled.apps.AppConfig;
import io.castled.forms.FormField;
import io.castled.forms.FormFieldSchema;
import io.castled.forms.FormFieldType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActiveCampaignAppConfig extends AppConfig {

    @FormField(description = "API KEY", title = "API KEY", schema = FormFieldSchema.STRING, type = FormFieldType.TEXT_BOX)
    private String apiKey;

    @FormField(description = "Active Campaign Access Token", title = "API URL", schema = FormFieldSchema.STRING, type = FormFieldType.TEXT_BOX)
    private String apiURL;
}
