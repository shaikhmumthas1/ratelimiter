package com.vajro.ratelimitter.util;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Helper to extract current authenticated principal identifier.
 * Assumes Authentication.getName() returns unique user id (or implement custom retrieval).
 */
public class SecurityUtils {
    public static String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;

        // If JWT-based, often principal name or a claim holds userId
        Object principal = auth.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            return ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        }
        // fallback to name
        return auth.getName();
    }
}

