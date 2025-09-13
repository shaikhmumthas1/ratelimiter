# Rate-Limited API Gateway Middleware (Spring Boot)

## Overview
This project provides a **rate-limiting middleware** for Spring Boot APIs using:
- **Spring Security** for JWT-based authentication.
- **Redis** to store request counters.
- **Custom filter** to enforce **100 requests per minute per authenticated user**.

If the limit is exceeded, the middleware returns:
- **HTTP 429 Too Many Requests**
- A `Retry-After` header with the remaining seconds until reset.

---

## Features
- ✅ Per-user request limiting (100 req/minute).
- ✅ Atomic counters in Redis using a Lua script (`INCR + EXPIRE`).
- ✅ Works under **high concurrency** (Redis is single-threaded, INCR is atomic).
- ✅ Integrates with **Spring Security 6** (JWT-based).
- ✅ Returns useful headers: `X-RateLimit-Limit`, `X-RateLimit-Remaining`.
- ✅ Unit tests with **Mockito** + concurrency safety test.

---

## Architecture
1. **Client Request → Spring Security**
    - JWT is validated by `JwtAuthenticationFilter`.
    - User ID is stored in `SecurityContext`.
2. **RateLimitingFilter**
    - Extracts user ID.
    - Calls `RateLimiterService.isAllowed(userId)`.
    - Queries Redis counter and TTL.
    - If limit exceeded → returns **429** immediately.
    - Otherwise, forwards to next filter/endpoint.
3. **Redis**
    - Stores per-user keys (`rl:{userId}`).
    - Each key has TTL = 60s (fixed window).
    - Atomic increments ensure correct counting.

---

## Project Structure
