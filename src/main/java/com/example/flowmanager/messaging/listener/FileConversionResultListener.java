package com.example.flowmanager.messaging.listener;

import com.example.flowmanager.entity.FileMetadata;
import com.example.flowmanager.entity.FileStatus;
import com.example.flowmanager.messaging.event.FileConversionResultEvent;
import com.example.flowmanager.repository.FileMetadataRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileConversionResultListener {

    private final FileMetadataRepository fileMetadataRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${app.kafka.topics.converted}", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void handleResult(String payload) {
        try {
            FileConversionResultEvent event = objectMapper.readValue(payload, FileConversionResultEvent.class);
            log.info("Получен результат конвертации: messageId={}, success={}", event.messageId(), event.success());

            FileMetadata fileMetadata = fileMetadataRepository.findById(event.messageId())
                    .orElseThrow(() -> new IllegalArgumentException("Метаданные файла не найдены по id: " + event.messageId()));

            if (event.success()) {
                fileMetadata.setStatus(FileStatus.SUCCESS);
                fileMetadata.setTargetBucket(event.bucket());
                fileMetadata.setTargetObjectKey(event.objectKey());
            } else {
                fileMetadata.setStatus(FileStatus.FAILED);
                fileMetadata.setErrorMessage(event.errorMessage());
            }

            fileMetadataRepository.save(fileMetadata);
        } catch (Exception e) {
            log.error("Ошибка обработки результата из Kafka: payload={}", payload, e);
        }
    }
}
