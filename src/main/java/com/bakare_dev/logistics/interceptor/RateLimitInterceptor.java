package com.bakare_dev.logistics.interceptor;

import com.bakare_dev.logistics.annotation.RateLimit;
import com.bakare_dev.logistics.exception.RateLimitExceededException;
import com.bakare_dev.logistics.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String KEY_PREFIX = "rate_limit:";

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);
        if (rateLimit == null) {
            return true;
        }

        String identifier = resolveIdentifier(request);
        String endpoint = request.getMethod() + ":" + request.getRequestURI();
        String key = KEY_PREFIX + identifier + ":" + endpoint;

        int maxRequests = rateLimit.requests();
        int windowMinutes = rateLimit.minutes();

        Long currentCount = redisTemplate.opsForValue().increment(key);
        if (currentCount != null && currentCount == 1) {
            redisTemplate.expire(key, Duration.ofMinutes(windowMinutes));
        }

        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        long remaining = Math.max(0, maxRequests - (currentCount != null ? currentCount : 0));

        response.setHeader("X-RateLimit-Limit", String.valueOf(maxRequests));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
        response.setHeader("X-RateLimit-Reset", String.valueOf(ttl != null ? ttl : windowMinutes * 60));

        if (currentCount != null && currentCount > maxRequests) {
            throw new RateLimitExceededException("Rate limit exceeded. Try again in " + ttl + " seconds.");
        }

        return true;
    }

    private String resolveIdentifier(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return "user:" + userDetails.getUser().getId();
        }

        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return "ip:" + forwarded.split(",")[0].trim();
        }
        return "ip:" + request.getRemoteAddr();
    }
}
