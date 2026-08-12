package com.example.flowmanager.service;

import com.example.flowmanager.client.SubscriptionClient;
import com.example.flowmanager.dto.SubscriptionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionCacheService {

    private final SubscriptionClient subscriptionClient;

    @Cacheable(value = "subscriptions", key = "#username")
    public SubscriptionDto getSubscription(String username) {
        log.info("Fetching subscription from SUBSCRIPTION-SERVICE for user: {}", username);
        return subscriptionClient.getSubscription(username);
    }

    @CacheEvict(value = "subscriptions", key = "#username")
    public void evictSubscriptionCache(String username) {
        log.info("Evicted subscription cache for user: {}", username);
    }
}
