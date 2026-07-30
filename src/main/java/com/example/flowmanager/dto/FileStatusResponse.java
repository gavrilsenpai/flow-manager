package com.example.flowmanager.dto;

import com.example.flowmanager.entity.FileStatus;

import java.time.Instant;

public record FileStatusResponse(
        String id,
        String originalFileName,
        FileStatus status,
        String errorMessage,
        Instant createdAt,
        Instant updatedAt
) {}
