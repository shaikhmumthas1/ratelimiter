package com.vajro.ratelimitter.config;

import com.vajro.ratelimitter.filter.RateLimitingFilter;
import com.vajro.ratelimitter.security.JwtAuthenticationFilter;
import com.vajro.ratelimitter.service.RateLimiterService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   RateLimiterService rateLimiterService) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                // Register JWT filter first (authn)
                .addFilterBefore(new JwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                // Then rate limiting filter
                .addFilterAfter(new RateLimitingFilter(rateLimiterService), JwtAuthenticationFilter.class);

        return http.build();
    }
}
