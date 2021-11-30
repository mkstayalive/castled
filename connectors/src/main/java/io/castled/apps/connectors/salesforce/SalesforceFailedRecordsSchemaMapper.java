package io.castled.apps.connectors.salesforce;

import com.google.inject.Singleton;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.schema.IncompatibleValueException;
import io.castled.schema.SchemaMapper;
import io.castled.schema.SchemaUtils;
import io.castled.schema.models.Schema;
import io.castled.schema.models.SchemaType;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Singleton
public class SalesforceFailedRecordsSchemaMapper extends SchemaMapper {

    private static final DateFormat SF_TS_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public Object transformValue(Object value, Schema schema) throws IncompatibleValueException {
        if (!(value instanceof String)) {
            throw new CastledRuntimeException("csv value should be an instance of String");
        }
        String valueStr = (String) value;
        if (SchemaUtils.isDateSchema(schema)) {
            LocalDate localDate = LocalDate.parse(valueStr);
            return Date.from(localDate.atStartOfDay(ZoneId.of("UTC")).toInstant());
        }

        if (SchemaUtils.isTimestampSchema(schema)) {
            try {
                return SF_TS_FORMAT.parse(valueStr);
            } catch (ParseException e) {
                //ignore
            }
        }
        if (schema.getType() == SchemaType.LONG || schema.getType() == SchemaType.INT || schema.getType() == SchemaType.SHORT) {
            return ((Double) Double.parseDouble(valueStr)).longValue();
        }
        return super.transformValue(value, schema);
    }


}
