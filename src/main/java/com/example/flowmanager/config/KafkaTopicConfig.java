package com.example.flowmanager.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${app.kafka.topics.to-convert}")
    private String toConvertTopic;

    @Value("${app.kafka.topics.converted}")
    private String convertedTopic;

    @Bean
    public NewTopic toConvertTopic() {
        return TopicBuilder.name(toConvertTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic convertedTopic() {
        return TopicBuilder.name(convertedTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
