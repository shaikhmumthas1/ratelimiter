package com.vajro.ratelimitter.filter;


import com.vajro.ratelimitter.service.RateLimiterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;

import jakarta.servlet.FilterChain;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class RateLimitingFilterTest {

    private RateLimiterService rateLimiterService;
    private RateLimitingFilter filter;

    @BeforeEach
    public void setup() {
        rateLimiterService = mock(RateLimiterService.class);
        filter = new RateLimitingFilter(rateLimiterService);
    }

    @Test
    public void test429WhenNotAllowed() throws Exception {
        when(rateLimiterService.isAllowed(anyString()))
                .thenReturn(new RateLimiterService.RateLimitResponse(false, 101, 0, 30L));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("1.2.3.4");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);
        assertEquals(429, response.getStatus());
        assertTrue(response.getContentAsString().contains("Too Many Requests"));
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    public void testContinueWhenAllowed() throws Exception {
        when(rateLimiterService.isAllowed(anyString()))
                .thenReturn(new RateLimiterService.RateLimitResponse(true, 10, 90, 50L));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("1.2.3.4");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);
        verify(chain, times(1)).doFilter(any(), any());
        assertEquals(200, response.getStatus()); // default MockHttpServletResponse status is 200
    }
}
