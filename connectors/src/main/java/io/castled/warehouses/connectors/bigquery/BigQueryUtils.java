package io.castled.warehouses.connectors.bigquery;

import com.google.api.gax.paging.Page;
import com.google.cloud.RetryOption;
import com.google.cloud.bigquery.*;
import com.google.common.collect.Lists;
import io.castled.exceptions.CastledException;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.schema.SchemaConstants;
import io.castled.schema.models.DecimalSchema;
import io.castled.schema.models.RecordSchema;
import org.threeten.bp.Duration;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class BigQueryUtils {

    public static final int MAX_NUMERIC_SCALE = 9;
    public static final int MAX_NUMERIC_PRECISION = 38;

    public static final int MAX_BIG_NUMERIC_SCALE = 38;
    public static final int MAX_BIG_NUMERIC_PRECISION = 76;


    public static RecordSchema bqSchemaToConnectSchema(com.google.cloud.bigquery.Schema bqSchema) {
        RecordSchema.Builder schemaBuilder = RecordSchema.builder();
        for (com.google.cloud.bigquery.Field field : bqSchema.getFields()) {
            schemaBuilder.put(field.getName(), bqFieldToConnectSchema(field));
        }
        return schemaBuilder.build();
    }

    public static List<String> fieldNames(com.google.cloud.bigquery.Schema schema) {
        return schema.getFields().stream().map(com.google.cloud.bigquery.Field::getName).collect(Collectors.toList());
    }

    public static String parseFieldValue(Object fieldValue, LegacySQLTypeName sqlTypeName) {
        if (sqlTypeName.equals(LegacySQLTypeName.TIMESTAMP)) {
            return parseTimestampFieldValue((String) fieldValue);
        }
        return fieldValue.toString();
    }

    public static Dataset getOrCreateDataset(String datasetName, BigQuery bigQuery, String location) {
        Dataset dataset = bigQuery.getDataset(DatasetId.of(datasetName));
        if (dataset == null) {
            return bigQuery.create(DatasetInfo.newBuilder(datasetName).setLocation(location).build());
        }
        return dataset;
    }

    public static void runJob(Job job) throws Exception {
        Job processedJob = job.waitFor(RetryOption.initialRetryDelay(Duration.ofSeconds(1)),
                RetryOption.totalTimeout(Duration.ofMinutes(40)));

        if (processedJob.getStatus().getExecutionErrors() != null) {
            throw new CastledException(String.format("Job %s failed to run with error %s",
                    processedJob.getJobId().getJob(), processedJob.getStatus().getExecutionErrors().toString()));
        }
    }

    public static List<String> listTables(String datasetName, BigQuery bigQuery) {
        List<String> allTables = Lists.newArrayList();
        try {
            Page<Table> tablePage = bigQuery.listTables(DatasetId.of(datasetName));
            do {
                tablePage.iterateAll().forEach(table -> allTables.add(table.getTableId().getTable()));

            } while ((tablePage = tablePage.getNextPage()) != null);
            return allTables;
        } catch (BigQueryException e) {
            //Data set not found
            if (e.getCode() == 404) {
                return allTables;
            }
            throw e;
        }
    }

    public static String parseTimestampFieldValue(String fieldValue) {
        String[] valueTokens = fieldValue.split("\\.");
        if (valueTokens.length != 2) {
            throw new CastledRuntimeException(String.format("Invalid timestamp field value string %s", fieldValue));
        }
        long epochSeconds = Long.parseLong(valueTokens[0]);
        long nanos = ((Double) (Integer.parseInt(valueTokens[1]) * Math.pow(10, 9 - valueTokens[1].length()))).longValue();
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds, nanos), ZoneId.of("UTC"));
        return zonedDateTime.format(DateTimeFormatter.ISO_DATE_TIME);
    }

    public static io.castled.schema.models.Schema bqFieldToConnectSchema(com.google.cloud.bigquery.Field field) {

        io.castled.schema.models.Schema schema = buildSchema(field);
        if (field.getMode() == com.google.cloud.bigquery.Field.Mode.NULLABLE) {
            schema.setOptional(true);
        }
        return schema;
    }

    private static io.castled.schema.models.Schema buildSchema(com.google.cloud.bigquery.Field field) {
        if (field.getType().name().equals("BIGNUMERIC")) {
            return DecimalSchema.builder().scale(MAX_BIG_NUMERIC_SCALE).precision(MAX_BIG_NUMERIC_PRECISION).build();
        }
        switch (field.getType().getStandardType()) {
            case STRING:
                return SchemaConstants.STRING_SCHEMA;
            case TIME:
                return SchemaConstants.TIME_SCHEMA;
            case BOOL:
                return SchemaConstants.BOOL_SCHEMA;
            case BYTES:
                return SchemaConstants.BYTES_SCHEMA;
            case INT64:
                return SchemaConstants.LONG_SCHEMA;
            case FLOAT64:
                return SchemaConstants.DOUBLE_SCHEMA;
            case NUMERIC:
                return DecimalSchema.builder().scale(MAX_NUMERIC_SCALE).precision(MAX_NUMERIC_PRECISION).build();
            case DATE:
                return SchemaConstants.DATE_SCHEMA;
            case DATETIME:
                return SchemaConstants.TIMESTAMP_SCHEMA;
            case TIMESTAMP:
                return SchemaConstants.ZONED_TIMESTAMP_SCHEMA;
            default:
                throw new CastledRuntimeException(String.format("Unsupported field type %s", field.getType().getStandardType()));
        }
    }
}
