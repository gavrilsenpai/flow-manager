package com.example.flowmanager.storage.service;

import com.example.flowmanager.storage.config.MinioProperties;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioStorageService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    public void uploadFile(String bucket, String objectKey, MultipartFile file) {
        try {
            ensureBucketExists(bucket);
            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucket)
                                .object(objectKey)
                                .stream(inputStream, file.getSize(), -1)
                                .contentType(file.getContentType())
                                .build()
                );
            }
        } catch (Exception e) {
            log.error("Ошибка при загрузке файла в MinIO: bucket={}, key={}", bucket, objectKey, e);
            throw new RuntimeException("Не удалось сохранить файл в хранилище", e);
        }
    }

    public InputStream downloadFile(String bucket, String objectKey) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            log.error("Ошибка при скачивании файла из MinIO: bucket={}, key={}", bucket, objectKey, e);
            throw new RuntimeException("Не удалось получить файл из хранилища", e);
        }
    }

    private void ensureBucketExists(String bucket) throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
    }
}
