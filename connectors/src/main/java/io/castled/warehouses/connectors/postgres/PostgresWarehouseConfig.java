package io.castled.warehouses.connectors.postgres;

import io.castled.forms.FormField;
import io.castled.forms.FormFieldSchema;
import io.castled.forms.FormFieldType;
import io.castled.warehouses.TunneledWarehouseConfig;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostgresWarehouseConfig extends TunneledWarehouseConfig {

    @FormField(description = "Database Server Host", title = "Database Server Host", placeholder = "", schema = FormFieldSchema.STRING, type = FormFieldType.TEXT_BOX)
    private String serverHost;

    @FormField(description = "Database Server Port", title = "Database Server Port", placeholder = "", schema = FormFieldSchema.NUMBER, type = FormFieldType.TEXT_BOX)
    private int serverPort;

    @FormField(description = "Database Name", title = "Database Name", placeholder = "", schema = FormFieldSchema.STRING, type = FormFieldType.TEXT_BOX)
    private String dbName;

    @FormField(description = "Database User", title = "Database User", placeholder = "", schema = FormFieldSchema.STRING, type = FormFieldType.TEXT_BOX)
    private String dbUser;

    @FormField(description = "Database Password", title = "Database Password", placeholder = "", schema = FormFieldSchema.STRING, type = FormFieldType.TEXT_BOX)
    private String dbPassword;
}
