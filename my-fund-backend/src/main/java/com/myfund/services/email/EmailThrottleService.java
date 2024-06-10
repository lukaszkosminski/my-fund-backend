package com.myfund.services.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class EmailThrottleService {

    private final CacheManager cacheManager;

    public EmailThrottleService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Cacheable(value = "emailThrottleCache", key = "#email")
    public AtomicInteger getEmailCount(String email) {
        AtomicInteger cachedCount = cacheManager.getCache("emailThrottleCache").get(email, AtomicInteger.class);
        return cachedCount != null ? cachedCount : new AtomicInteger(0);
    }

    @CachePut(value = "emailThrottleCache", key = "#email")
    public AtomicInteger incrementEmailCount(String email) {
        AtomicInteger emailCount = getEmailCount(email);
        emailCount.incrementAndGet();
        log.info("Incremented email count for {}: {}", email, emailCount.get());
        return emailCount;
    }

    public boolean canSendEmail(String email) {
        AtomicInteger emailCount = getEmailCount(email);
        log.info("Email count for {}: {}", email, emailCount.get());
        return emailCount.get() < 3;
    }
}
