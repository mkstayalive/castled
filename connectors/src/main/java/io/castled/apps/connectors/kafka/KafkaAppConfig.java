package io.castled.apps.connectors.kafka;

import io.castled.apps.AppConfig;
import io.castled.forms.FormField;
import io.castled.forms.FormFieldSchema;
import io.castled.forms.FormFieldType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KafkaAppConfig extends AppConfig {

    @FormField(title = "Bootstrap Servers", placeholder = "eg: host1:9092, host2:9092", schema = FormFieldSchema.STRING, type = FormFieldType.TEXT_BOX)
    private String bootstrapServers;
}
