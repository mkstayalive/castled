package io.castled.warehouses.connectors.bigquery;

import org.junit.Test;


public class TestBQWarehouseCopyAdaptor {

    BQWarehouseCopyAdaptor bqWarehouseCopyAdaptor = new BQWarehouseCopyAdaptor();

    @Test
    public void transformValue() throws Exception{
//        Date date = Date.from(LocalDateTime.of(LocalDate.of(2017, 1, 13), LocalTime.of(0, 34, 35, 899988798))
//                .atZone(ZoneId.of("UTC")).toInstant());
//
//        Object value = bqWarehouseCopyAdaptor.constructSyncableRecord(date, org.apache.kafka.connect.data.Date.builder().build());
//        assertTrue(value instanceof String);
//        assertEquals(value,"2017-01-13");
//
//        value = bqWarehouseCopyAdaptor.constructSyncableRecord(date, org.apache.kafka.connect.data.Timestamp.builder().build());
//        assertEquals(value,"2017-01-13 00:34:35.899");
//
//        value = bqWarehouseCopyAdaptor.constructSyncableRecord(date, org.apache.kafka.connect.data.Time.builder().build());
//        assertEquals(value,"00:34:35.899");


    }
}