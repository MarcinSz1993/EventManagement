package com.marcinsz.eventmanagementsystem.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "spring.kafka.config")
public class KafkaConfig {
    private String allEventsTopic;
    private String CancelledEventsTopic;
    private String groupId;
}
