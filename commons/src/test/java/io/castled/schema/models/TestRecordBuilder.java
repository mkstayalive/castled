package io.castled.schema.models;


import io.castled.schema.SchemaConstants;
import org.junit.Assert;
import org.junit.Test;

public class TestRecordBuilder {

    @Test
    public void testCreateRecord() throws Exception {

        RecordSchema recordSchema = RecordSchema.builder().put("field1", SchemaConstants.STRING_SCHEMA)
                .put("field2", SchemaConstants.OPTIONAL_STRING_SCHEMA).build();

        Schema field1Schema = recordSchema.getSchema("field1");
        Assert.assertSame(field1Schema.getType(), SchemaType.STRING);
        Assert.assertEquals(field1Schema.isOptional(), Boolean.FALSE);

        Schema field2Schema = recordSchema.getSchema("field2");
        Assert.assertSame(field2Schema.getType(), SchemaType.STRING);
        Assert.assertEquals(field2Schema.isOptional(), Boolean.TRUE);

        Tuple.Builder recordBuilder = Tuple.builder();
        for (FieldSchema fieldSchema : recordSchema.getFieldSchemas()) {
            recordBuilder.put(fieldSchema, "abcd");
        }
        Tuple record = recordBuilder.build();

        Assert.assertEquals(record.getValue("field1"),"abcd");
        Assert.assertEquals(record.getValue("field2"),"abcd");
    }
}