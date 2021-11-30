package io.castled.caches;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.castled.cache.CastledCache;
import io.castled.pubsub.MessageListener;
import io.castled.pubsub.MessageSubscriber;
import io.castled.pubsub.MessageType;
import io.castled.pubsub.registry.Message;
import io.castled.pubsub.registry.WarehouseUpdatedMessage;
import io.castled.utils.TimeUtils;
import io.castled.models.Warehouse;
import io.castled.warehouses.WarehouseDAO;
import org.jdbi.v3.core.Jdbi;

@Singleton
public class WarehouseCache extends CastledCache<Long, Warehouse> implements MessageListener {

    @Inject
    public WarehouseCache(Jdbi jdbi, MessageSubscriber messageSubscriber) {
        super(TimeUtils.hoursToMillis(3), 1000,
                (warehouseId) -> jdbi.onDemand(WarehouseDAO.class).getWarehouse(warehouseId), false);
        messageSubscriber.subscribe(MessageType.WAREHOUSE_UPDATED, this);
    }

    @Override
    public void handleMessage(Message message) {
        if (message.getType().equals(MessageType.WAREHOUSE_UPDATED)) {
            WarehouseUpdatedMessage warehouseUpdatedMessage = (WarehouseUpdatedMessage) message;
            this.invalidate(warehouseUpdatedMessage.getWarehouseId());
        }
    }
}
