package com.example.flowmanager.messaging.service;

import com.example.flowmanager.messaging.event.FileConversionRequestedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topics.to-convert}")
    private String toConvertTopic;

    public void sendConversionRequest(FileConversionRequestedEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(toConvertTopic, event.messageId(), payload);
            log.info("Отправлен запрос на конвертацию в Kafka: topic={}, messageId={}", toConvertTopic, event.messageId());
        } catch (Exception e) {
            log.error("Ошибка при отправке события в Kafka: messageId={}", event.messageId(), e);
            throw new IllegalStateException("Не удалось отправить событие в Kafka", e);
        }
    }
}
