package io.castled.commons.models;

import io.castled.schema.models.Message;
import io.castled.schema.models.MessageOffsetSupplier;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ObjectIdAndMessage implements MessageOffsetSupplier {

    private String id;
    private Message message;

    @Override
    public long getOffset() {
        return message.getOffset();
    }
}
