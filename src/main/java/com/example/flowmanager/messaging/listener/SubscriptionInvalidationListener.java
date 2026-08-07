package com.example.flowmanager.messaging.listener;

import com.example.flowmanager.service.SubscriptionCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionInvalidationListener {

    private final SubscriptionCacheService cacheService;

    @KafkaListener(topics = "subscription-invalidations", groupId = "flow-manager-group")
    public void handleInvalidation(String username) {
        log.info("Received cache invalidation event for user: {}", username);
        cacheService.evictSubscriptionCache(username);
    }
}
