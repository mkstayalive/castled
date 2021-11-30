package io.castled.schema.models;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.castled.schema.exceptions.SchemaValidationException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
@NoArgsConstructor
public class Tuple {

    @Getter
    @Setter
    private String name;

    @Getter
    private List<Field> fields;
    private Map<String, Field> fieldMap;

    public void addField(Field field) {
        fields.add(field);
        fieldMap.put(field.getName(), field);
    }

    public Object getValue(String fieldName) {
        return Optional.ofNullable(fieldMap.get(fieldName)).map(Field::getValue).orElse(null);
    }

    public Field getField(String fieldName) {
        return Optional.ofNullable(fieldMap.get(fieldName)).orElse(null);
    }

    public static class Builder {

        private final Tuple record = new Tuple(null, Lists.newArrayList(), Maps.newHashMap());


        public Builder put(FieldSchema fieldSchema, Object value) throws SchemaValidationException {
            record.addField(new Field(fieldSchema, value));
            return this;
        }

        public Builder put(Field field) {
            record.addField(field);
            return this;
        }

        public Builder name(String name) {
            record.setName(name);
            return this;
        }

        public Tuple build() {
            return record;
        }
    }

    public static Tuple.Builder builder() {
        return new Builder();
    }
}
