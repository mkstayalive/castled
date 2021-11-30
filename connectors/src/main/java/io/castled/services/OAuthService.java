package io.castled.services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.castled.cache.OAuthCache;
import io.castled.oauth.OAuthDAO;
import io.castled.oauth.OAuthDetails;
import io.castled.oauth.OAuthState;
import io.castled.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Optional;

@Singleton
@Slf4j
public class OAuthService {


    private static final String OAUTH_NS = "oauth:";

    private final OAuthDAO oAuthDAO;
    private final OAuthCache oAuthCache;
    private final JedisPool jedisPool;

    @Inject
    public OAuthService(Jdbi jdbi, JedisPool jedisPool, OAuthCache oAuthCache) {
        this.oAuthDAO = jdbi.onDemand(OAuthDAO.class);
        this.jedisPool = jedisPool;
        this.oAuthCache = oAuthCache;
    }


    private OAuthState getOAuthState(String stateId) {
        try (Jedis jedis = jedisPool.getResource()) {
            String stateJson = jedis.get(OAUTH_NS + stateId);
            return Optional.ofNullable(stateJson)
                    .map(jsonRef -> JsonUtils.jsonStringToObject(jsonRef, OAuthState.class)).orElse(null);
        }
    }

    public OAuthDetails getOAuthDetails(Long oAuthId, boolean cached) {
        if (cached) {
            return oAuthCache.getValue(oAuthId);
        }
        return oAuthDAO.getOAuthDetails(oAuthId);
    }

    public OAuthDetails getOAuthDetails(Long oAuthId) {
        return getOAuthDetails(oAuthId, false);
    }

}
