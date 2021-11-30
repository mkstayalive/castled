package io.castled.warehouses.connectors.snowflake;

import com.amazonaws.regions.Regions;
import io.castled.forms.FormField;
import io.castled.forms.FormFieldType;
import io.castled.forms.FormFieldSchema;
import io.castled.warehouses.WarehouseConfig;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SnowflakeWarehouseConfig extends WarehouseConfig {

    @FormField(description = "Account Name", title = "Account Name", placeholder = "", schema = FormFieldSchema.STRING, type = FormFieldType.TEXT_BOX)
    private String accountName;

    @FormField(description = "Warehouse Name", title = "Warehouse Name", placeholder = "", schema = FormFieldSchema.NUMBER, type = FormFieldType.TEXT_BOX)
    private String warehouseName;

    @FormField(description = "Database name", title = "Database Name", placeholder = "", schema = FormFieldSchema.STRING, type = FormFieldType.TEXT_BOX)
    private String dbName;

    @FormField(description = "Database User", title = "Database User", placeholder = "", schema = FormFieldSchema.STRING, type = FormFieldType.TEXT_BOX)
    private String dbUser;

    @FormField(description = "Database password", title = "Database Password", placeholder = "", schema = FormFieldSchema.STRING, type = FormFieldType.TEXT_BOX)
    private String dbPassword;

    @FormField(description = "Schema", title = "Schema Name", placeholder = "", schema = FormFieldSchema.STRING, type = FormFieldType.TEXT_BOX)
    private String schemaName;

    @FormField(description = "S3 Bucket to be used as the staging area", title = "S3 Bucket", placeholder = "", schema = FormFieldSchema.STRING, type = FormFieldType.TEXT_BOX)
    private String s3Bucket;

    @FormField(description = "S3 Access Key Id", title = "S3 Access Key Id", placeholder = "", schema = FormFieldSchema.STRING, type = FormFieldType.TEXT_BOX)
    private String accessKeyId;

    @FormField(description = "S3 Access Key Secret", title = "S3 Access Key Secret", placeholder = "", schema = FormFieldSchema.STRING, type = FormFieldType.TEXT_BOX)
    private String accessKeySecret;

    @FormField(description = "S3 Bucket Location", title = "S3 Bucket Location", placeholder = "", schema = FormFieldSchema.STRING, type = FormFieldType.TEXT_BOX)
    private Regions region;

    public String getDbHost() {
        return getAccountName() + ".snowflakecomputing.com";
    }

    public int getDbPort() {
        //https port
        return 443;
    }
}
