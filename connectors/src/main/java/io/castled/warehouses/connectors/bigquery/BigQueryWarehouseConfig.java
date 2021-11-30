package io.castled.warehouses.connectors.bigquery;

import io.castled.OptionsReferences;
import io.castled.forms.*;
import io.castled.warehouses.WarehouseConfig;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@CodeBlock(title = "Run the following commands on the google cloud console",
        dependencies = {"projectId", "bucketName", "serviceAccount"}
        , snippets = {@CodeSnippet(title = "BQ Data Viewer Role", ref = "bq_data_viewer_access"),
        @CodeSnippet(title = "BQ User Role", ref = "bq_data_user_access"),
@CodeSnippet(title = "GCP Storage Admin Role", ref = "gcp_storage_admin_access")})
public class BigQueryWarehouseConfig extends WarehouseConfig {

    @FormField(description = "Project Id", title = "Project Id", placeholder = "", schema = FormFieldSchema.STRING, type = FormFieldType.TEXT_BOX)
    private String projectId;

    @FormField(description = "GCS Bucket Name", title = "GCS Bucket Name", placeholder = "", schema = FormFieldSchema.STRING, type = FormFieldType.TEXT_BOX)
    private String bucketName;

    @FormField(description = "Dataset location", title = "Dataset location", schema = FormFieldSchema.STRING, type = FormFieldType.DROP_DOWN,
            optionsRef = @OptionsRef(value = OptionsReferences.BQ_LOCATIONS, type = OptionsRefType.STATIC))
    private String location;

    @FormField(type = FormFieldType.TEXT_BOX, description = "Service Account", title = "Service Account (Auto Generated)",
            optionsRef = @OptionsRef(value = OptionsReferences.BQ_SERVICE_ACCOUNT, type = OptionsRefType.DYNAMIC))
    private String serviceAccount;
}
