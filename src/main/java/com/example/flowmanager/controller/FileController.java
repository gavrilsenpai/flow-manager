package com.example.flowmanager.controller;

import com.example.flowmanager.dto.FileStatusResponse;
import com.example.flowmanager.dto.SubscriptionDto;
import com.example.flowmanager.entity.FileMetadata;
import com.example.flowmanager.mapper.FileMapper;
import com.example.flowmanager.service.FileManagementService;
import com.example.flowmanager.service.SubscriptionCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileManagementService fileManagementService;
    private final FileMapper fileMapper;
    private final SubscriptionCacheService subscriptionCacheService;

    private static final long MAX_FREE_FILE_SIZE = 100L * 1024 * 1024;

    @PostMapping
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "X-User-Login", required = false) String username) {

        if (username != null && !username.isBlank()) {
            SubscriptionDto sub = subscriptionCacheService.getSubscription(username);

            boolean isExpired = sub.expiresAt() != null && sub.expiresAt().isBefore(Instant.now());
            boolean isFreePlan = "FREE".equalsIgnoreCase(sub.planType()) || isExpired;

            if (isFreePlan && file.getSize() > MAX_FREE_FILE_SIZE) {
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                        .body("Limit exceeded: Free subscription allows files up to 100MB only.");
            }
        }

        FileMetadata metadata = fileManagementService.uploadAndStartConversion(file);
        return ResponseEntity.ok(fileMapper.toResponse(metadata));
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<FileStatusResponse> getStatus(@PathVariable String id) {
        FileMetadata metadata = fileManagementService.getFileMetadata(id);
        return ResponseEntity.ok(fileMapper.toResponse(metadata));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable String id) {
        FileMetadata metadata = fileManagementService.getFileMetadata(id);
        InputStream inputStream = fileManagementService.downloadConvertedFile(id);

        String pdfFileName = metadata.getOriginalFileName() + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + pdfFileName + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(inputStream));
    }
}
