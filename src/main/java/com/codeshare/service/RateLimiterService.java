package com.codeshare.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    // Map of Session ID to list of request timestamps (in milliseconds)
    private final Map<String, List<Long>> requestTimes = new ConcurrentHashMap<>();

    private static final long TIME_WINDOW_MS = 60000; // 1 minute
    private static final int MAX_REQUESTS = 5; // Max 5 requests per minute

    /**
     * Checks if a session is allowed to make an AI request.
     * @param sessionId the HTTP session ID of the user
     * @return true if the request is allowed, false if rate limited
     */
    public boolean allowRequest(String sessionId) {
        if (sessionId == null) {
            return false;
        }

        long now = System.currentTimeMillis();
        List<Long> timestamps = requestTimes.computeIfAbsent(sessionId, k -> new ArrayList<>());

        synchronized (timestamps) {
            // Remove timestamps outside of the 1-minute window
            timestamps.removeIf(time -> (now - time) > TIME_WINDOW_MS);

            // Check if size is within limits
            if (timestamps.size() >= MAX_REQUESTS) {
                return false;
            }

            // Record current request
            timestamps.add(now);
            return true;
        }
    }
}
