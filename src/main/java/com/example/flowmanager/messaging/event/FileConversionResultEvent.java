package com.example.flowmanager.messaging.event;

public record FileConversionResultEvent(
        String messageId,
        boolean success,
        String bucket,
        String objectKey,
        String errorMessage
) {}
