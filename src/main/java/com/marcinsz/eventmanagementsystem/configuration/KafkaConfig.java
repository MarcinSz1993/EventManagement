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
    private String cancelledEventsTopic;
    private String paymentTopic;
    private String completedPaymentsTopic;
    private String failedPaymentsTopic;
    private String eventManagementGroupId;
    private String bankServiceGroupId;
}