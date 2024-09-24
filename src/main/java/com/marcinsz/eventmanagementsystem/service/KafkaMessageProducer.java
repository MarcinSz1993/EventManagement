package com.marcinsz.eventmanagementsystem.service;

import com.marcinsz.eventmanagementsystem.configuration.KafkaConfig;
import com.marcinsz.eventmanagementsystem.dto.EventDto;
import com.marcinsz.eventmanagementsystem.kafka.KafkaEventDto;
import com.marcinsz.eventmanagementsystem.kafka.KafkaTransactionRequest;
import com.marcinsz.eventmanagementsystem.request.TransactionKafkaRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaMessageProducer {

    private final KafkaConfig kafkaConfig;
    private final KafkaTransactionRequest kafkaTransactionRequest;
    private final KafkaEventDto kafkaEventDto;

    public void sendTransactionRequestMessageToExpectingPaymentsTopic(TransactionKafkaRequest transactionKafkaRequest){
        kafkaTransactionRequest.sendMessage(kafkaConfig.getPaymentTopic(), transactionKafkaRequest);
    }

    public void sendCreatedEventMessageToAllEventsTopic(EventDto eventDto){
        kafkaEventDto.sendMessage(kafkaConfig.getAllEventsTopic(),eventDto);
    }

    public void sendCancelledEventMessageToCancellationTopic(EventDto eventDto){
        kafkaEventDto.sendMessage(kafkaConfig.getCancelledEventsTopic(),eventDto);
    }
}
