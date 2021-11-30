package io.castled.warehouses.connectors.redshift;

import com.amazonaws.regions.Regions;
import io.castled.forms.FormField;
import io.castled.forms.FormFieldSchema;
import io.castled.forms.FormFieldType;
import io.castled.utils.JsonUtils;
import io.castled.warehouses.TunneledWarehouseConfig;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RedshiftWarehouseConfig extends TunneledWarehouseConfig {

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

    @FormField(description = "S3 Bucket to be used as the staging area", title = "S3 Bucket", placeholder = "", schema = FormFieldSchema.STRING, type = FormFieldType.TEXT_BOX)
    private String s3Bucket;

    @FormField(description = "S3 Access Key Id", title = "S3 Access Key Id", placeholder = "", schema = FormFieldSchema.STRING, type = FormFieldType.TEXT_BOX)
    private String accessKeyId;

    @FormField(description = "S3 Access Key Secret", title = "S3 Access Key Secret", placeholder = "", schema = FormFieldSchema.STRING, type = FormFieldType.TEXT_BOX)
    private String accessKeySecret;

    @FormField(description = "S3 Bucket Location", title = "S3 Bucket Location", placeholder = "", schema = FormFieldSchema.STRING, type = FormFieldType.TEXT_BOX)
    private Regions region;

}
