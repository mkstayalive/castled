package io.castled.schema.models;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.castled.schema.exceptions.DuplicateFieldException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
public class RecordSchema {

    @Getter
    @Setter
    private String name;

    @Getter
    private List<FieldSchema> fieldSchemas;

    private Map<String, Schema> fieldSchemaMapping;

    public void addFieldSchema(FieldSchema fieldSchema) throws DuplicateFieldException {
        if (fieldSchemaMapping.containsKey(fieldSchema.getName())) {
            throw new DuplicateFieldException(fieldSchema.getName());
        }
        fieldSchemas.add(fieldSchema);
        fieldSchemaMapping.put(fieldSchema.getName(), fieldSchema.getSchema());
    }

    public void removeFieldSchema(List<FieldSchema> schemas) {
        fieldSchemas.removeAll(schemas);
        fieldSchemaMapping.keySet().removeAll(fieldSchemas.stream().map(fieldSchema -> fieldSchema.getName()).collect(Collectors.toList()));
    }


    public Schema getSchema(String fieldName) {
        return fieldSchemaMapping.get(fieldName);
    }

    public static class Builder {

        private final RecordSchema recordSchema = new RecordSchema(null, Lists.newArrayList(), Maps.newHashMap());

        public Builder name(String name) {
            this.recordSchema.setName(name);
            return this;
        }

        public Builder put(String name, Schema schema) {
            this.recordSchema.addFieldSchema(new FieldSchema(name, schema));
            return this;
        }

        public Builder put(String name, Schema schema, Map<String, Object> params) {
            this.recordSchema.addFieldSchema(new FieldSchema(name, schema, params));
            return this;
        }

        public RecordSchema build() {
            return recordSchema;
        }
    }

    public static RecordSchema.Builder builder() {
        return new RecordSchema.Builder();
    }
}
