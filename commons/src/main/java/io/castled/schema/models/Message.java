package io.castled.schema.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class Message implements MessageOffsetSupplier {
    private long offset;
    private Tuple record;
}
