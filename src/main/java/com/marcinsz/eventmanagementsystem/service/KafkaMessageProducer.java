package com.marcinsz.eventmanagementsystem.service;

import com.marcinsz.eventmanagementsystem.configuration.KafkaConfig;
import com.marcinsz.eventmanagementsystem.dto.EventDto;
import com.marcinsz.eventmanagementsystem.kafka.KafkaEventDto;
import com.marcinsz.eventmanagementsystem.kafka.KafkaTransactionRequest;
import com.marcinsz.eventmanagementsystem.request.TransactionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaMessageProducer {

    private final KafkaConfig kafkaConfig;
    private final KafkaTransactionRequest kafkaTransactionRequest;
    private final KafkaEventDto kafkaEventDto;

    public void sendTransactionRequestMessageToExpectingPaymentsTopic(TransactionRequest transactionRequest){
        kafkaTransactionRequest.sendMessage(kafkaConfig.getPaymentTopic(),transactionRequest);
    }

    public void sendCreatedEventMessageToAllEventsTopic(EventDto eventDto){
        kafkaEventDto.sendMessage(kafkaConfig.getAllEventsTopic(),eventDto);
    }

    public void sendCancelledEventMessageToCancellationTopic(EventDto eventDto){
        kafkaEventDto.sendMessage(kafkaConfig.getCancelledEventsTopic(),eventDto);
    }
}
