package io.castled.schema;

import org.apache.commons.validator.routines.EmailValidator;
import com.google.inject.Singleton;
import io.castled.schema.models.Schema;
import io.castled.schema.models.SchemaType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeParseException;

@Singleton
public class SchemaMapper {

    public Object transformValue(Object value, Schema schema) throws IncompatibleValueException {
        if (value == null) {
            return null;
        }
        Object transformedValue = doTransformValue(value, schema);
        if (transformedValue == null) {
            throw new IncompatibleValueException(value, schema);
        }
        return transformedValue;
    }

    private Object doTransformValue(Object value, Schema schema) throws IncompatibleValueException {
        if (schema.getType() == SchemaType.DECIMAL) {
            return transformValueToDecimal(value, schema);
        }
        if (schema.getType() == SchemaType.TIMESTAMP) {
            return transformValueToTimestamp(value, schema);
        }
        if (schema.getType() == SchemaType.ZONED_TIMESTAMP) {
            return transformValueToZonedDateTime(value, schema);
        }
        if (schema.getType() == SchemaType.DATE) {
            return transformValueToDate(value, schema);
        }
        if (schema.getType() == SchemaType.TIME) {
            return transformValueToTime(value, schema);
        }
        if (schema.getType() == SchemaType.EMAIL) {
            return transformValueToEmail(value, schema);
        }
        switch (schema.getType()) {
            case SHORT:
                return transformValueToInt16(value, schema);
            case INT:
                return transformValueToInt32(value, schema);
            case LONG:
                return transformValueToInt64(value, schema);
            case FLOAT:
                return transformValueToFloat(value, schema);
            case DOUBLE:
                return transformValueToDouble(value, schema);
            case BOOLEAN:
                return transformValueToBool(value, schema);
            case STRING:
                return transformValueToString(value, schema);
            case BYTES:
                return transformValueToBytes(value, schema);
            default:
                return value;
        }
    }

    public ZonedDateTime transformValueToZonedDateTime(Object value, Schema schema) throws IncompatibleValueException {
        if (value instanceof ZonedDateTime) {
            return (ZonedDateTime) value;
        }
        if (value instanceof LocalDateTime) {
            LocalDateTime localDateTime = (LocalDateTime) value;
            return localDateTime.atZone(ZoneId.of("UTC"));
        }
        if (value instanceof LocalDate) {
            return ZonedDateTime.of((LocalDate) value, LocalTime.of(0, 0, 0), ZoneId.of("UTC"));
        }
        throw new IncompatibleValueException(value, schema);
    }

    public LocalTime transformValueToTime(Object value, Schema schema) throws IncompatibleValueException {
        if (value instanceof ZonedDateTime) {
            ZonedDateTime zonedDateTime = (ZonedDateTime) value;
            return zonedDateTime.toLocalTime();
        }
        if (value instanceof LocalDateTime) {
            LocalDateTime localDateTime = (LocalDateTime) value;
            return localDateTime.toLocalTime();
        }
        if (value instanceof LocalTime) {
            return (LocalTime) value;
        }
        throw new IncompatibleValueException(value, schema);

    }

    public LocalDate transformValueToDate(Object value, Schema schema) throws IncompatibleValueException {
        if (value instanceof ZonedDateTime) {
            ZonedDateTime zonedDateTime = (ZonedDateTime) value;
            return zonedDateTime.toLocalDate();
        }
        if (value instanceof LocalDateTime) {
            LocalDateTime localDateTime = (LocalDateTime) value;
            return localDateTime.toLocalDate();
        }
        if (value instanceof LocalDate) {
            return (LocalDate) value;
        }
        if (value instanceof String) {
            try {
                return LocalDate.parse((String) value);
            } catch (DateTimeParseException e) {
                //ignore
            }
        }
        throw new IncompatibleValueException(value, schema);

    }

    public LocalDateTime transformValueToTimestamp(Object value, Schema schema) throws IncompatibleValueException {
        if (value instanceof ZonedDateTime) {
            ZonedDateTime zonedDateTime = (ZonedDateTime) value;
            return zonedDateTime.toLocalDateTime();
        }
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }
        throw new IncompatibleValueException(value, schema);
    }

    public BigDecimal transformValueToDecimal(Object value, Schema schema) throws IncompatibleValueException {
        int schemaScale = SchemaUtils.getDecimalScale(schema);
        if (value instanceof BigInteger) {
            return new BigDecimal((BigInteger) value).setScale(schemaScale, RoundingMode.HALF_UP);
        }
        if (value instanceof Float) {
            return BigDecimal.valueOf((float) value).setScale(schemaScale, RoundingMode.HALF_UP);
        }
        if (value instanceof Double) {
            return BigDecimal.valueOf((double) value).setScale(schemaScale, RoundingMode.HALF_UP);
        }
        if (isIntegral(value)) {
            Number number = (Number) value;
            return BigDecimal.valueOf(number.longValue()).setScale(schemaScale, RoundingMode.HALF_UP);
        }
        if (value instanceof String) {
            try {
                return new BigDecimal((String) value).setScale(schemaScale, RoundingMode.HALF_UP);
            } catch (NumberFormatException e) {
                throw new IncompatibleValueException(value, schema);
            }
        }
        if (value instanceof BigDecimal) {
            BigDecimal bigDecimal = (BigDecimal) value;
            if (bigDecimal.scale() <= schemaScale) {
                return bigDecimal.setScale(schemaScale, RoundingMode.HALF_UP);
            }
            throw new IncompatibleValueException(value, schema);
        }
        throw new IncompatibleValueException(value, schema);
    }


    private byte[] transformValueToBytes(Object value, Schema schema) throws IncompatibleValueException {
        if (value instanceof byte[]) {
            return (byte[]) value;
        } else if (value instanceof String) {
            return ((String) value).getBytes();
        }
        throw new IncompatibleValueException(value, schema);
    }

    private String transformValueToString(Object value, Schema schema) throws IncompatibleValueException {
        if (value instanceof byte[]) {
            return new String((byte[]) value);
        }
        return String.valueOf(value);
    }


    private Boolean transformValueToBool(Object value, Schema schema) throws IncompatibleValueException {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (isIntegral(value)) {
            Number number = (Number) value;
            if (number.longValue() == 1) {
                return Boolean.TRUE;
            }
            if (number.longValue() == 0) {
                return Boolean.FALSE;
            }
            throw new IncompatibleValueException(value, schema);
        }
        if (value instanceof String) {
            try {
                return Boolean.valueOf((String) value);
            } catch (NumberFormatException ne) {
                throw new IncompatibleValueException(value, schema);
            }
        }
        throw new IncompatibleValueException(value, schema);
    }

    private Double transformValueToDouble(Object value, Schema schema) throws IncompatibleValueException {
        if (value instanceof Double) {
            return (Double) value;
        }
        if (value instanceof Integer || value instanceof Long) {
            Number number = (Number) value;
            return (double) number.longValue();
        }
        if (value instanceof Float) {
            float afloat = (Float) value;
            return (double) afloat;
        }
        if (value instanceof String) {
            try {
                return Double.valueOf((String) value);
            } catch (NumberFormatException e) {
                throw new IncompatibleValueException(value, schema);
            }
        }
        if (value instanceof BigDecimal) {
            BigDecimal bigDecimal = (BigDecimal) value;
            BigDecimal doubleDecimal = BigDecimal.valueOf(bigDecimal.doubleValue());
            if (doubleDecimal.stripTrailingZeros().equals(bigDecimal.stripTrailingZeros())) {
                return bigDecimal.doubleValue();
            }
        }
        throw new IncompatibleValueException(value, schema);

    }

    private Float transformValueToFloat(Object value, Schema schema) throws IncompatibleValueException {
        if (value instanceof Float) {
            return (Float) value;
        }
        if (value instanceof Integer || value instanceof Long) {
            Number number = (Number) value;
            return (float) number.longValue();
        }
        if (value instanceof Double) {
            double doubleValue = (double) value;
            if (doubleValue < Float.MAX_VALUE && doubleValue > Float.MIN_VALUE) {
                return (float) doubleValue;
            }
            throw new IncompatibleValueException(value, schema);
        }

        if (value instanceof String) {
            try {
                return Float.valueOf((String) value);
            } catch (NumberFormatException ne) {
                throw new IncompatibleValueException(value, schema);
            }
        }
        if (value instanceof BigDecimal) {
            BigDecimal bigDecimal = (BigDecimal) value;
            BigDecimal floatDecimal = BigDecimal.valueOf(bigDecimal.floatValue());
            if (floatDecimal.stripTrailingZeros().equals(bigDecimal.stripTrailingZeros())) {
                return bigDecimal.floatValue();
            }
        }
        throw new IncompatibleValueException(value, schema);
    }

    private Long transformValueToInt64(Object value, Schema schema) throws IncompatibleValueException {
        if (isIntegral(value)) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.valueOf((String) value);
            } catch (NumberFormatException e) {
                throw new IncompatibleValueException(value, schema);
            }
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).longValue();
        }
        return null;
    }

    private Integer transformValueToInt32(Object value, Schema schema) throws IncompatibleValueException {
        if (isIntegral(value)) {
            Number number = (Number) value;
            if (number.longValue() <= Integer.MAX_VALUE && number.longValue() >= Integer.MIN_VALUE) {
                return number.intValue();
            }
            throw new IncompatibleValueException(value, schema);
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.valueOf((String) value);
            } catch (NumberFormatException e) {
                throw new IncompatibleValueException(value, schema);
            }
        }
        throw new IncompatibleValueException(value, schema);
    }


    private Short transformValueToInt16(Object value, Schema schema) throws IncompatibleValueException {
        if (isIntegral(value)) {
            Number number = (Number) value;
            if (number.longValue() <= Short.MAX_VALUE && number.longValue() >= Short.MIN_VALUE) {
                return number.shortValue();
            }
            throw new IncompatibleValueException(value, schema);
        }
        if (value instanceof String) {
            try {
                return Short.valueOf((String) value);
            } catch (NumberFormatException e) {
                throw new IncompatibleValueException(value, schema);
            }
        }
        throw new IncompatibleValueException(value, schema);
    }

    private Byte transformValueToInt8(Object value, Schema schema) throws IncompatibleValueException {

        if (isIntegral(value)) {
            Number number = (Number) value;
            if (number.longValue() <= Byte.MAX_VALUE && number.longValue() >= Byte.MIN_VALUE) {
                return number.byteValue();
            }
            throw new IncompatibleValueException(value, schema);
        }
        if (value instanceof String) {
            try {
                return Byte.valueOf((String) value);
            } catch (NumberFormatException e) {
                throw new IncompatibleValueException(value, schema);
            }
        }
        throw new IncompatibleValueException(value, schema);
    }

    public String transformValueToEmail(Object value, Schema schema) throws IncompatibleValueException {
        if (!EmailValidator.getInstance().isValid(String.valueOf(value))) {
            throw new IncompatibleValueException(value, schema);
        }
        return transformValueToString(value, schema);
    }

    private boolean isIntegral(Object value) {
        return value instanceof Long || value instanceof Integer ||
                value instanceof Short || value instanceof Byte;
    }
}
