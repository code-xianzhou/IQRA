package com.iqra.mcp;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * MCP: Model Control Plane - rate limiting and monitoring via AOP
 */
@Slf4j
@Aspect
@Component
public class McpAspect {

    // Rate limiting: per user QPS
    private static final int MAX_QPS_PER_USER = 5;
    private final Map<String, AtomicInteger> userQpsCounter = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> userQpsTimestamp = new ConcurrentHashMap<>();

    // Statistics
    private final AtomicInteger totalCalls = new AtomicInteger(0);
    private final AtomicLong totalResponseTime = new AtomicLong(0);
    private final Map<String, AtomicInteger> modelCallCount = new ConcurrentHashMap<>();

    @Around("execution(* com.iqra.controller.ChatController.ask(..))")
    public Object rateLimitAndMonitor(ProceedingJoinPoint joinPoint) throws Throwable {
        String userId = getCurrentUserId(joinPoint);
        long startTime = System.currentTimeMillis();

        // Rate limit check
        if (!checkRateLimit(userId)) {
            throw new com.iqra.common.BusinessException("请求过于频繁，请稍后再试");
        }

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            // Record statistics
            totalCalls.incrementAndGet();
            totalResponseTime.addAndGet(duration);

            log.info("MCP: userId={}, duration={}ms, totalCalls={}", userId, duration, totalCalls.get());
            return result;
        } catch (Exception e) {
            log.error("MCP: request failed for userId={}", userId, e);
            throw e;
        } finally {
            decrementQps(userId);
        }
    }

    private boolean checkRateLimit(String userId) {
        long now = System.currentTimeMillis();
        AtomicLong lastRequest = userQpsTimestamp.computeIfAbsent(userId, k -> new AtomicLong(now));

        if (now - lastRequest.get() > 1000) {
            // Reset counter for new second
            userQpsCounter.put(userId, new AtomicInteger(1));
            lastRequest.set(now);
            return true;
        }

        int currentQps = userQpsCounter.getOrDefault(userId, new AtomicInteger(0)).incrementAndGet();
        return currentQps <= MAX_QPS_PER_USER;
    }

    private void decrementQps(String userId) {
        // Counter auto-expires after 1 second
    }

    private String getCurrentUserId(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof com.iqra.model.dto.AskRequest) {
                return ((com.iqra.model.dto.AskRequest) arg).getUserId();
            }
        }
        return "unknown";
    }

    public int getTotalCalls() {
        return totalCalls.get();
    }

    public long getAvgResponseTime() {
        int calls = totalCalls.get();
        return calls > 0 ? totalResponseTime.get() / calls : 0;
    }

    public Map<String, Integer> getModelCallCount() {
        Map<String, Integer> result = new ConcurrentHashMap<>();
        modelCallCount.forEach((k, v) -> result.put(k, v.get()));
        return result;
    }
}
