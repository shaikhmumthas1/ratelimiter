package com.vajro.ratelimitter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

@Configuration
public class RedisConfig {

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    /**
     * Provides a RedisScript<Long> for the INCR + EXPIRE pattern used by the rate limiter.
     */
    @Bean
    public DefaultRedisScript<Long> incrScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        // Script returns the value of the counter after increment
        String lua = "local current = redis.call('incr', KEYS[1]); " +
                "if tonumber(current) == 1 then redis.call('expire', KEYS[1], ARGV[1]) end; " +
                "return current;";
        script.setScriptText(lua);
        script.setResultType(Long.class);
        return script;
    }
}
