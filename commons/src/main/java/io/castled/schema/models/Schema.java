package io.castled.schema.models;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.castled.schema.exceptions.IncompatibleValueException;
import io.castled.schema.exceptions.NullValueException;
import io.castled.schema.exceptions.SchemaValidationException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        visible = true,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ShortSchema.class, name = "SHORT"),
        @JsonSubTypes.Type(value = LongSchema.class, name = "LONG"),
        @JsonSubTypes.Type(value = IntegerSchema.class, name = "INT"),
})
@AllArgsConstructor
@Data
@NoArgsConstructor
public abstract class Schema {

    private SchemaType type;
    private List<Class<?>> allowedTypes;
    private boolean optional;

    public void validateValue(Object value) throws SchemaValidationException {

        if (value == null) {
            if (!optional) {
                throw new NullValueException(type);
            }
            return;
        }
        if (allowedTypes.stream().noneMatch(allowedType -> allowedType.isInstance(value))) {
            throw new IncompatibleValueException(type, value);
        }
    }
}
