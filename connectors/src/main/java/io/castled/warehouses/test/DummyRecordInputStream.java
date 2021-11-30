package io.castled.warehouses.test;

import com.google.common.collect.Lists;
import io.castled.commons.streams.RecordInputStream;
import io.castled.schema.models.*;
import lombok.Getter;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class DummyRecordInputStream implements RecordInputStream {

    private List<String> emails = Lists.newArrayList("arun@gmail.com", "frank@gmail.com");
    private List<String> firstNames = Lists.newArrayList("Arun", "Frank");
    private List<String> lastNames = Lists.newArrayList("TG", "GV");
    private List<String> lifeCycleStages = Lists.newArrayList("lead", "subscriber");

    private int readTill = -1;
    @Getter
    private final RecordSchema schema;

    public DummyRecordInputStream() {

        Schema stringSchema = StringSchema.builder().optional(true).maxLength(64).build();
        this.schema = RecordSchema.builder().name("Ticket")
                .put("email", stringSchema)
                .put("first_name", stringSchema)
                .put("last_name", stringSchema)
                .put("lifecycle_stage", stringSchema)
                .build();
    }

    @Override
    public Tuple readRecord() {
        if (readTill + 1 >= emails.size()) {
            return null;
        }
        Date date = Date.from(LocalDate.of(2017, Month.FEBRUARY, 12).atStartOfDay().atZone(ZoneId.of("UTC")).toInstant());
        Tuple.Builder recordBuilder = Tuple.builder();
        for (FieldSchema fieldSchema : schema.getFieldSchemas()) {
            recordBuilder.put(fieldSchema, emails.get(readTill + 1));
            readTill++;
        }
        return recordBuilder.build();
    }

}
