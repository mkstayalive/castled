package io.castled.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.castled.utils.TimeUtils;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class CastledCache<K, V> {

    private final Cache<K, CacheWrapper<V>> cache;
    private final Function<K, V> cacheLoader;
    private final boolean wrapNulls;

    public CastledCache(long ttlMs, long capacity, Function<K, V> cacheLoader, boolean wrapNulls) {
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(adjustTTL(ttlMs), TimeUnit.MILLISECONDS)
                .maximumSize(capacity).build();
        this.cacheLoader = cacheLoader;
        this.wrapNulls = wrapNulls;
    }

    public void write(K key, V value) {
        this.cache.put(key, new CacheWrapper<>(value));
    }

    public V getValue(K key) {
        CacheWrapper<V> cacheWrapper = this.cache.get(key,
                keyRef -> {
                    V value = cacheLoader.apply(keyRef);
                    if (value != null || wrapNulls) {
                        return new CacheWrapper<>(value);
                    }
                    return null;
                });
        return Optional.ofNullable(cacheWrapper).map(CacheWrapper::getValue).orElse(null);
    }

    private long adjustTTL(long ttlMs) {
        //generate number between -10 and +10
        long ttlDelta = new Random().nextInt(20) - 10;
        return ttlMs + TimeUtils.secondsToMillis(ttlDelta);
    }

    public V getValueIfPresent(K key) {
        CacheWrapper<V> cacheWrapper = this.cache.getIfPresent(key);
        return Optional.ofNullable(cacheWrapper).map(CacheWrapper::getValue).orElse(null);
    }

    public void invalidate(K key) {
        this.cache.invalidate(key);
    }
}
