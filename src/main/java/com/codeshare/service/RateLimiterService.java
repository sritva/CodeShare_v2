package com.codeshare.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private final Map<Long, List<Long>> requestTimes = new ConcurrentHashMap<>();

    private static final long TIME_WINDOW_MS = 60000; // 1 minute
    private static final int MAX_REQUESTS = 5; // Max 5 requests per minute

    public boolean allowRequest(Long userId) {
        if (userId == null) {
            return false;
        }

        long now = System.currentTimeMillis();
        List<Long> timestamps = requestTimes.computeIfAbsent(userId, k -> new ArrayList<>());

        synchronized (timestamps) {
            timestamps.removeIf(time -> (now - time) > TIME_WINDOW_MS);

            if (timestamps.size() >= MAX_REQUESTS) {
                return false;
            }

            timestamps.add(now);
            return true;
        }
    }

    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    public void cleanupStaleEntries() {
        long now = System.currentTimeMillis();
        requestTimes.entrySet().removeIf(entry -> {
            List<Long> timestamps = entry.getValue();
            synchronized (timestamps) {
                timestamps.removeIf(time -> (now - time) > TIME_WINDOW_MS);
                return timestamps.isEmpty();
            }
        });
    }
}
