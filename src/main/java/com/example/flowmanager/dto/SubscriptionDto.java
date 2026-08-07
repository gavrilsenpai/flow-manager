package com.example.flowmanager.dto;

import java.io.Serializable;
import java.time.Instant;

public record SubscriptionDto(String username, String planType, Instant expiresAt) implements Serializable {
    private static final long serialVersionUID = 1L;
}
