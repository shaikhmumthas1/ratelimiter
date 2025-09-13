package com.vajro.ratelimitter.service;

import com.vajro.ratelimitter.config.RateLimitProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class RateLimiterService {
    private static final Logger log = LoggerFactory.getLogger(RateLimiterService.class);

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<Long> incrScript;
    private final RateLimitProperties properties;

    public RateLimiterService(StringRedisTemplate redisTemplate,
                              DefaultRedisScript<Long> incrScript,
                              RateLimitProperties properties) {
        this.redisTemplate = redisTemplate;
        this.incrScript = incrScript;
        this.properties = properties;
    }

    /**
     * Check if user is allowed. Returns RateLimitResponse with info.
     */
    public RateLimitResponse isAllowed(String key) {
        try {
            String redisKey = buildKey(key);
            Long count = redisTemplate.execute(incrScript, Collections.singletonList(redisKey),
                    String.valueOf(properties.getWindowSeconds()));
            if (count == null) {
                // treat null as allow (fail-open)
                return RateLimitResponse.allowed(-1, properties.getLimit());
            }
            Long ttl = redisTemplate.getExpire(redisKey);
            boolean allowed = count <= properties.getLimit();
            int remaining = Math.max(0, properties.getLimit() - count.intValue());
            return new RateLimitResponse(allowed, count.intValue(), remaining, ttl);
        } catch (DataAccessException ex) {
            log.error("Redis access error in rate limiter, failing open", ex);
            // Fail-open: allow in case of Redis outage but you can choose fail-closed
            return RateLimitResponse.allowed(-1, properties.getLimit());
        }
    }

    private String buildKey(String identifier) {
        return "rl:" + identifier;
    }

    public static class RateLimitResponse {
        public final boolean allowed;
        public final int current;
        public final int remaining;
        public final long ttlSeconds;

        public RateLimitResponse(boolean allowed, int current, int remaining, long ttlSeconds) {
            this.allowed = allowed;
            this.current = current;
            this.remaining = remaining;
            this.ttlSeconds = ttlSeconds;
        }

        public static RateLimitResponse allowed(long current, int limit) {
            return new RateLimitResponse(true, (int)current, Math.max(0, limit - (int)current), -1);
        }
    }
}

