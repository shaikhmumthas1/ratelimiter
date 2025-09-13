package com.vajro.ratelimitter.config;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "rate.limit")
public class RateLimitProperties {
    /**
     * Number of allowed requests per window
     */
    private int limit = 100;

    /**
     * Window seconds (default 60)
     */
    private int windowSeconds = 60;

    public int getLimit() { return limit; }
    public void setLimit(int limit) { this.limit = limit; }

    public int getWindowSeconds() { return windowSeconds; }
    public void setWindowSeconds(int windowSeconds) { this.windowSeconds = windowSeconds; }
}
