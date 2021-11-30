package io.castled.schema.models;

import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DecimalSchema extends Schema {

    private int scale;
    private int precision;

    @Builder
    public DecimalSchema(int scale, int precision, boolean optional) {
        super(SchemaType.DECIMAL, Lists.newArrayList(BigDecimal.class), optional);
        this.scale = scale;
        this.precision = precision;
    }
}
