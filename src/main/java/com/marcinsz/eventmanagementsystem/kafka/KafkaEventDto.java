package com.marcinsz.eventmanagementsystem.kafka;

import com.marcinsz.eventmanagementsystem.dto.EventDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Data
@Slf4j
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
                    log.info("Sent message: {}", message + " with offset: " + result.getRecordMetadata().offset());
                } else {
                    log.error("Unable to send message: {}", message);
                }
            });
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
