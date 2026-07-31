package com.example.flowmanager.messaging.event;

public record FileConversionResultEvent(
        String messageId,
        String status,
        String bucket,
        String objectKey,
        String errorMessage
) {
    public boolean isSuccess() {
        return "SUCCESS".equalsIgnoreCase(status);
    }
}
