package io.castled.apps.connectors.googleads;

import io.castled.apps.OAuthAppConfig;
import io.castled.forms.FormField;
import io.castled.forms.FormFieldSchema;
import io.castled.forms.FormFieldType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleAdsAppConfig extends OAuthAppConfig {

    @FormField(description = "Developer token of the registered oauth app", title = "Google Ads Developer token", schema = FormFieldSchema.STRING, type = FormFieldType.TEXT_BOX)
    private String developerToken;
}
