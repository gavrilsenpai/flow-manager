package com.example.flowmanager.service;

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
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileManagementService {

    private final FileMetadataRepository fileMetadataRepository;
    private final MinioStorageService minioStorageService;
    private final KafkaProducerService kafkaProducerService;
    private final MinioProperties minioProperties;

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
