package com.marcinsz.eventmanagementsystem.kafka;

import com.marcinsz.eventmanagementsystem.dto.EventDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class KafkaEventDto implements KafkaMessageSender<EventDto>{
    private final KafkaTemplate<String, EventDto> kafkaTemplate;

    @Override
    public void sendMessage(String topic, EventDto message) {
        try {
            CompletableFuture<SendResult<String, EventDto>> allEvents = kafkaTemplate.send(topic, message);
            allEvents.whenComplete((result, ex) -> {
                if (ex == null) {
                    System.out.println("Sent message: " + message.toString() +
                            "with offset: " + result.getRecordMetadata().offset());
                } else {
                    System.out.println("Unable to send message: " + message.toString());
                }
            });
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
