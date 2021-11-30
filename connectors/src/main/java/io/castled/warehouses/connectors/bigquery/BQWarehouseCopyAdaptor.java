package io.castled.warehouses.connectors.bigquery;

import com.google.inject.Singleton;
import io.castled.schema.SchemaUtils;
import io.castled.schema.models.Schema;
import io.castled.warehouses.WarehouseCopyAdaptor;


import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Singleton
public class BQWarehouseCopyAdaptor implements WarehouseCopyAdaptor {

    @Override
    public Object constructSyncableRecord(Object value, Schema schema) {
        if (SchemaUtils.isDateSchema(schema)) {
            Date date = (Date) value;
            return date.toInstant().atZone(ZoneId.of("UTC")).toLocalDate().format(DateTimeFormatter.ISO_DATE);
        }
        if (SchemaUtils.isTimestampSchema(schema)) {
            Date date = (Date) value;
            return date.toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime().format(DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSS"));
        }
        if (SchemaUtils.isTimeSchema(schema)) {
            Date date = (Date) value;
            return date.toInstant().atZone(ZoneId.of("UTC")).toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        }
        return value;
    }
}
