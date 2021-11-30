package io.castled.apps;

import com.google.common.collect.Lists;
import io.castled.commons.models.AppSyncStats;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

@Slf4j
public abstract class BufferedObjectSink<T> {

    protected final List<T> objects = Lists.newArrayList();

    public void writeRecord(T object) {
        objects.add(object);
        if (objects.size() >= getMaxBufferedObjects()) {
            flushBufferedRecords(objects);
            objects.clear();
        }
    }

    public void flushBufferedRecords(List<T> records) {
        if (CollectionUtils.isNotEmpty(records)) {
            writeRecords(records);
        }

    }

    protected abstract void writeRecords(List<T> records);

    public void flushRecords() throws Exception {
        if (objects.size() > 0) {
            flushBufferedRecords(objects);
            objects.clear();
        }
        afterRecordsFlush();
    }

    public void afterRecordsFlush() {
    }

    public abstract long getMaxBufferedObjects();

}
