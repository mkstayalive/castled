package io.castled.cache;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CacheWrapper<V> {
    private V value;
}
