package io.castled.utils;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.UUID;

@Slf4j
@Singleton
public class OAuthStateStore {

    private static final String OAUTH_NS = "oauth:";

    private final GenericKeyValueStore kvStore;

    @Inject
    public OAuthStateStore(GenericKeyValueStore kvStore) {
        this.kvStore = kvStore;
    }

    public String persistOAuthState(String state) {
        String uuid = UUID.randomUUID().toString();
        kvStore.putKey(OAUTH_NS, uuid, (int) TimeUtils.minutesToMillis(30), state);
        return uuid;
    }

    public String getOAuthState(String stateId) {
        return kvStore.getKey(OAUTH_NS, stateId);
    }
}
