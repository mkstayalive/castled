package io.castled.apps.connectors.customerio;

import io.castled.apps.DataSink;
import io.castled.apps.models.DataSinkRequest;
import io.castled.commons.models.AppSyncStats;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.schema.models.Message;

import java.util.List;
import java.util.Optional;

public class CustomerIODataSink implements DataSink {


    private volatile CustomerIOObjectSink<String> customerIOObjectSink;

    private long skippedRecords = 0;

    @Override
    public void syncRecords(DataSinkRequest dataSinkRequest) throws Exception {

        this.customerIOObjectSink = getObjectSink(dataSinkRequest);
        Message message;
        while ((message = dataSinkRequest.getMessageInputStream().readMessage()) != null) {
            if (!this.writeRecord(message,dataSinkRequest.getPrimaryKeys())) {
                skippedRecords++;
            }
        }
        this.customerIOObjectSink.flushRecords();
    }

    private CustomerIOObjectSink<String> getObjectSink(DataSinkRequest dataSinkRequest) {
        CustomerIOObjectSink<String> customerIOObjectSink = null;
        CustomerIOObject customerIOObject = CustomerIOObject
                .getObjectByName(dataSinkRequest.getAppSyncConfig().getObject().getObjectName());
        switch (customerIOObject) {
            case EVENT:
                customerIOObjectSink = new CustomerIOEventSink(dataSinkRequest);
                break;
            case PERSON:
                customerIOObjectSink = new CustomerIOPersonSink(dataSinkRequest);
                break;
            default:
                throw new CastledRuntimeException(String.format("Invalid object type %s!", customerIOObject.getName()));
        }
        return customerIOObjectSink;
    }

    @Override
    public AppSyncStats getSyncStats() {
        return Optional.ofNullable(customerIOObjectSink).map(CustomerIOObjectSink::getSyncStats).
                map(statsRef -> new AppSyncStats(statsRef.getRecordsProcessed(),
                        statsRef.getOffset(), skippedRecords)).orElse(new AppSyncStats(0, 0, 0));
    }

    private boolean writeRecord(Message message,List<String> primaryKeys) {
        this.customerIOObjectSink.createOrUpdateObject(message);
        return true;
    }
}
