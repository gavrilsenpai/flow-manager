package com.example.flowmanager.messaging.event;

public record FileConversionRequestedEvent(
        String messageId,
        String bucket,
        String objectKey
) {}
