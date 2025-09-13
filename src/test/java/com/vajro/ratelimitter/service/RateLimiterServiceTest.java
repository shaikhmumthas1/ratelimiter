package com.vajro.ratelimitter.service;


import com.vajro.ratelimitter.config.RateLimitProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RateLimiterServiceTest {

    private StringRedisTemplate redisTemplate;
    private DefaultRedisScript<Long> redisScript;
    private RateLimitProperties properties;
    private RateLimiterService service;

    @BeforeEach
    public void setup() {
        redisTemplate = mock(StringRedisTemplate.class);
        redisScript = mock(DefaultRedisScript.class);
        properties = new RateLimitProperties();
        properties.setLimit(3); // smaller limit for unit tests
        properties.setWindowSeconds(60);
        service = new RateLimiterService(redisTemplate, redisScript, properties);
    }

    @Test
    public void testAllowedUnderLimit() {
        // simulate first increment returns 1
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), anyString())).thenReturn(1L);
        when(redisTemplate.getExpire(anyString())).thenReturn(59L);

        RateLimiterService.RateLimitResponse res = service.isAllowed("user1");
        assertTrue(res.allowed);
        assertEquals(1, res.current);
        assertEquals(2, res.remaining);
        verify(redisTemplate).execute(any(DefaultRedisScript.class), anyList(), eq(String.valueOf(60)));
    }

    @Test
    public void testBlockedWhenExceeds() {
        // simulate counter returns 4 while limit is 3
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), anyString())).thenReturn(4L);
        when(redisTemplate.getExpire(anyString())).thenReturn(30L);

        RateLimiterService.RateLimitResponse res = service.isAllowed("user2");
        assertFalse(res.allowed);
        assertEquals(4, res.current);
        assertEquals(0, res.remaining); // negative remaining cleaned to zero
    }

    @Test
    public void testFailOpenOnRedisError() {
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), anyString()))
                .thenThrow(new RuntimeException("redis down"));
        RateLimiterService.RateLimitResponse res = service.isAllowed("user3");
        assertTrue(res.allowed); // fail-open default
    }
}
