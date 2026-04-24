package com.pca.Backend.Service;

import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class ConsumerListenerHigh {
    private final NotificationService notificationService;
    private ObjectMapper objectMapper = new ObjectMapper();
    @RetryableTopic(attempts = "3", autoCreateTopics = "true", 
    backOff = @BackOff(delay = 1000, multiplier = 2), topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
    )
    @KafkaListener(topics="notification.public.fraude", groupId = "highPriority", containerFactory = "fraudeListenerContainerFactory")
    public void consumeHighPrio(String message){
       try{
          JsonNode json = objectMapper.readTree(message);
          Long userId = json.get("userId").asLong();
          Long amount = json.get("amount").asLong()
          String alertType = json.get("alertType").asText();
          String alertMessage = json.get("alertMessage").asText();
          notificationService.sendNotification(userId, alertMessage, amount, true);
       }
       catch (JsonProcessingException e){
             System.err.print(e.getMessage());
       }
      System.out.printf("le message envoyé est: %d", message);
    }
    @DltHandler
    public void handleMessage(String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic){
        System.err.printf("le %s message est envoye au topic %s", message, topic);
    }
     
}
