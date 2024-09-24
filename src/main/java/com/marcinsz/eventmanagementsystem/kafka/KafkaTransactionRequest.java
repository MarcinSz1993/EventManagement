package com.marcinsz.eventmanagementsystem.kafka;

import com.marcinsz.eventmanagementsystem.request.TransactionKafkaRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

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
                    System.out.println("Sent message: " + message
                            + "with offset: " + result.getRecordMetadata().offset());
                } else {
                    System.out.println("Unable to send message: " + message);
                }
            });
        } catch (Exception ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }
}
