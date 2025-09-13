package com.vajro.ratelimitter.filter;


import com.vajro.ratelimitter.service.RateLimiterService;
import com.vajro.ratelimitter.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter which calls RateLimiterService per request and denies with 429 if limit exceeded.
 */
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);
    private final RateLimiterService rateLimiterService;

    public RateLimitingFilter(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null || userId.isEmpty()) {
            // fallback: use remote IP (best-effort)
            userId = request.getRemoteAddr();
        }

        RateLimiterService.RateLimitResponse rl = rateLimiterService.isAllowed(userId);

        if (!rl.allowed) {
            // over limit
            log.debug("Rate limit exceeded for {} : count={} ttl={}", userId, rl.current, rl.ttlSeconds);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader(HttpHeaders.RETRY_AFTER, rl.ttlSeconds > 0 ? String.valueOf(rl.ttlSeconds) : "60");
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            String body = "{\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded.\"}";
            response.getWriter().write(body);
            return;
        }

        // Optionally expose rate-limit headers
        response.setHeader("X-RateLimit-Limit", String.valueOf( /* limit */ 100 ));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, rl.remaining)));

        filterChain.doFilter(request, response);
    }
}
