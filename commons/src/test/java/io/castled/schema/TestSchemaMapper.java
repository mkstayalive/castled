package io.castled.schema;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class TestSchemaMapper {

    @Test
    public void transformValue() throws Exception {

//        //test int8
//        SchemaMapper schemaMapper = new SchemaMapper();
//        boolean valueIncompatible = false;
//        Object transformedValue = null;
//        try {
//            transformedValue = schemaMapper.transformValue(10000L, Schema.INT8_SCHEMA);
//        } catch (IncompatibleValueException e) {
//            valueIncompatible = true;
//        }
//        Assert.assertTrue(valueIncompatible);
//        transformedValue = schemaMapper.transformValue(126L, Schema.INT8_SCHEMA);
//        Assert.assertTrue(transformedValue instanceof Byte);
//        assertEquals(transformedValue, (byte) 126);
//
//        //test int16
//        valueIncompatible = false;
//        try {
//            transformedValue = schemaMapper.transformValue((long) Short.MAX_VALUE + 12, Schema.INT16_SCHEMA);
//        } catch (IncompatibleValueException e) {
//            valueIncompatible = true;
//        }
//        Assert.assertTrue(valueIncompatible);
//
//        transformedValue = schemaMapper.transformValue(32766L, Schema.INT16_SCHEMA);
//        Assert.assertTrue(transformedValue instanceof Short);
//        assertEquals(transformedValue, (short) 32766);
//
//        //test int32
//        valueIncompatible = false;
//        try {
//            transformedValue = schemaMapper.transformValue((long) Integer.MAX_VALUE + 12, Schema.INT32_SCHEMA);
//        } catch (IncompatibleValueException e) {
//            valueIncompatible = true;
//        }
//        Assert.assertTrue(valueIncompatible);
//
//        transformedValue = schemaMapper.transformValue(2147483647L, Schema.INT32_SCHEMA);
//        Assert.assertTrue(transformedValue instanceof Integer);
//        assertEquals(transformedValue, (int) 2147483647);
//
//        //test float32
//        valueIncompatible = false;
//        try {
//            transformedValue = schemaMapper.transformValue((double) Float.MAX_VALUE + 12, Schema.FLOAT32_SCHEMA);
//        } catch (IncompatibleValueException e) {
//            valueIncompatible = true;
//        }
//        Assert.assertTrue(valueIncompatible);
//        transformedValue = schemaMapper.transformValue(12.44777d, Schema.FLOAT32_SCHEMA);
//        assertTrue((float) transformedValue < 12.45f && (float) transformedValue > 12.43f);
//
//        //test float64
//        transformedValue = schemaMapper.transformValue(12.44777d, Schema.FLOAT64_SCHEMA);
//        assertTrue((double) transformedValue < 12.45f && (double) transformedValue > 12.43f);
//
//        //test boolean
//        valueIncompatible = false;
//        try {
//            transformedValue = schemaMapper.transformValue(14L, Schema.BOOLEAN_SCHEMA);
//        } catch (IncompatibleValueException e) {
//            valueIncompatible = true;
//        }
//        Assert.assertTrue(valueIncompatible);
//        transformedValue = schemaMapper.transformValue(1, Schema.BOOLEAN_SCHEMA);
//        Assert.assertTrue((Boolean) transformedValue);
//        transformedValue = schemaMapper.transformValue(0, Schema.BOOLEAN_SCHEMA);
//        Assert.assertFalse((Boolean) transformedValue);
//
//        //test String
//        transformedValue = schemaMapper.transformValue("abcd".getBytes(), Schema.STRING_SCHEMA);
//        assertEquals("abcd", transformedValue);
//
//        transformedValue = schemaMapper.transformValue("abcd", Schema.STRING_SCHEMA);
//        assertEquals("abcd", transformedValue);
//
//        //test decimal schema
//        transformedValue = schemaMapper.transformValue(12.34f, Decimal.schema(10));
//        assertTrue(transformedValue instanceof BigDecimal);
//        assertEquals(((BigDecimal) transformedValue).scale(), 10);
//
//        transformedValue = schemaMapper.transformValue(12.34d, Decimal.schema(10));
//        assertTrue(transformedValue instanceof BigDecimal);
//        assertEquals(((BigDecimal) transformedValue).scale(), 10);
//
//        transformedValue = schemaMapper.transformValue("12.34", Decimal.schema(10));
//        assertTrue(transformedValue instanceof BigDecimal);
//        assertEquals(((BigDecimal) transformedValue).scale(), 10);
//
//        transformedValue = schemaMapper.transformValue(new BigDecimal("12.34"), Decimal.schema(10));
//        assertTrue(transformedValue instanceof BigDecimal);
//        assertEquals(((BigDecimal) transformedValue).scale(), 10);

    }
}