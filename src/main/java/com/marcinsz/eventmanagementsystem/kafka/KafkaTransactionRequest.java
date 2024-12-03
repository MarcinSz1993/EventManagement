package com.marcinsz.eventmanagementsystem.kafka;

import com.marcinsz.eventmanagementsystem.request.TransactionKafkaRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor

public class KafkaTransactionRequest implements KafkaMessageSender<TransactionKafkaRequest> {
    private final KafkaTemplate<String, TransactionKafkaRequest> kafkaTemplate;

    @Override
    public void sendMessage(String topic, TransactionKafkaRequest message) {
        try {
            CompletableFuture<SendResult<String, TransactionKafkaRequest>> expectingPayments = kafkaTemplate.send(topic, message);
            expectingPayments.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Sent message: {}with offset: {}", message, result.getRecordMetadata().offset());
                } else {
                    log.error("Unable to send message: {}", message);
                }
            });
        } catch (Exception ex) {
            log.error("Error: {}", ex.getMessage());
        }
    }
}
