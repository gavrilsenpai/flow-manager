package com.example.flowmanager.service;

import com.example.flowmanager.dto.PlanType;
import com.example.flowmanager.dto.SubscriptionDto;
import com.example.flowmanager.entity.FileMetadata;
import com.example.flowmanager.entity.FileStatus;
import com.example.flowmanager.messaging.event.FileConversionRequestedEvent;
import com.example.flowmanager.messaging.service.KafkaProducerService;
import com.example.flowmanager.repository.FileMetadataRepository;
import com.example.flowmanager.storage.config.MinioProperties;
import com.example.flowmanager.storage.service.MinioStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileManagementService {

    private final FileMetadataRepository fileMetadataRepository;
    private final MinioStorageService minioStorageService;
    private final KafkaProducerService kafkaProducerService;
    private final MinioProperties minioProperties;
    private final SubscriptionCacheService subscriptionCacheService;
    private static final long MAX_FREE_FILE_SIZE = 100L * 1024 * 1024;

    @Transactional
    public FileMetadata uploadAndStartConversion(MultipartFile file) {
        String fileId = UUID.randomUUID().toString();
        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unnamed";
        String objectKey = fileId + "_" + originalFilename;

        minioStorageService.uploadFile(minioProperties.getIncomingBucket(), objectKey, file);

        FileMetadata metadata = new FileMetadata(fileId, originalFilename, minioProperties.getIncomingBucket(), objectKey);
        fileMetadataRepository.save(metadata);

        kafkaProducerService.sendConversionRequest(
                new FileConversionRequestedEvent(fileId, minioProperties.getIncomingBucket(), objectKey)
        );

        return metadata;
    }

    public FileMetadata validateAndUpload(MultipartFile file, String username) {
        if (username != null && !username.isBlank()) {
            SubscriptionDto sub = subscriptionCacheService.getSubscription(username);

            boolean isExpired = sub.expiresAt() != null && sub.expiresAt().isBefore(Instant.now());
            boolean isFreePlan = PlanType.FREE.equals(sub.planType()) || isExpired;

            if (isFreePlan && file.getSize() > MAX_FREE_FILE_SIZE) {
                throw new MaxUploadSizeExceededException(100L);
            }
        }

        return uploadAndStartConversion(file);
    }

    @Transactional(readOnly = true)
    public FileMetadata getFileMetadata(String id) {
        return fileMetadataRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Файл с id " + id + " не найден"));
    }

    @Transactional(readOnly = true)
    public InputStream downloadConvertedFile(String id) {
        FileMetadata metadata = getFileMetadata(id);

        if (metadata.getStatus() != FileStatus.SUCCESS) {
            throw new IllegalStateException("Файл еще не сконвертирован или произошла ошибка. Текущий статус: " + metadata.getStatus());
        }

        return minioStorageService.downloadFile(metadata.getTargetBucket(), metadata.getTargetObjectKey());
    }
}
