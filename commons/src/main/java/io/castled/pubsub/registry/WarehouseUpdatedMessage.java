package io.castled.pubsub.registry;

import io.castled.pubsub.MessageType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class WarehouseUpdatedMessage extends Message {

    private Long warehouseId;

    public WarehouseUpdatedMessage(Long warehouseId) {
        super(MessageType.WAREHOUSE_UPDATED);
        this.warehouseId = warehouseId;
    }

}
