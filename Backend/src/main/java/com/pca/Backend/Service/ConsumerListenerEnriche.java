package com.pca.Backend.Service;

import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pca.Backend.DTO.NotifEnrechi;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConsumerListenerEnriche {
    private final NotificationService notifService;
    private ObjectMapper mapper = new ObjectMapper();
    @RetryableTopic(attempts = "2", backOff = @BackOff(delay = 1000L, multiplier = 2), 
    autoCreateTopics = "true", topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
)
    @KafkaListener(topics = "transaction-enrechissement", groupId = "enri-grp", containerFactory = "kafkaListenerContainerFactory")
    public void consume(String message){
        try{
        NotifEnrechi notifEnrechi = mapper.readValue(message, NotifEnrechi.class);
         notifService.sendNotification(
                    notifEnrechi.userId(),
                    notifEnrechi.message(),
                    notifEnrechi.amount(),
                    notifEnrechi.isCritical()
                ); 
                notifService.ArchivageNotif(notifEnrechi);
    }
    catch(Exception e){
        System.out.printf("le message %s", e.getMessage());
    }
}
  @DltHandler
  public void handleError(String message){
  System.err.printf("le message est %s", message);
  }
}