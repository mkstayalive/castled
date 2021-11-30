package io.castled.utils;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Slf4j
@Singleton
public class GenericKeyValueStore {

    private final JedisPool jedisPool;

    @Inject
    public GenericKeyValueStore(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    public void putKey(String nameSpace, String key, int ttlSeconds, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setex(nameSpace + key, ttlSeconds, value);
        }
    }

    public void putKey(String nameSpace, String key, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.set(nameSpace + key, value);
        }
    }

    public String getKey(String nameSpace, String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(nameSpace + key);
        }
    }
}
