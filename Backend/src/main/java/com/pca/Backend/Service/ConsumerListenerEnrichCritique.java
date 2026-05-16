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
import com.pca.Backend.Entity.HistoriqueNotifEntity;
import com.pca.Backend.Entity.Status;
import com.pca.Backend.Repo.HistoriqueNotif;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsumerListenerEnrichCritique {
    private final NotificationService notifService;
    private final ObjectMapper mapper = new ObjectMapper();
    @RetryableTopic(attempts = "3", backOff = @BackOff(delay = 1000L, multiplier = 1.5),
    topicSuffixingStrategy= TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
    autoCreateTopics = "true"
)
    @KafkaListener(topics = "fraude-enrechissement", groupId = "enri-critique-grp", containerFactory = "fraudeListenerContainerFactory")
    public void consume(String message) {
        try {
            NotifEnrechi notifEnrechi = mapper.readValue(message, NotifEnrechi.class);
            notifService.sendNotification(
                notifEnrechi.userId(),
                "[CRITIQUE] " + notifEnrechi.message(),
                notifEnrechi.amount(),
                true
            );
            notifService.ArchivageNotif(notifEnrechi);
            
        } catch (Exception e) {
            log.error("Echec traitement message enrichissement critique: {}", message, e);
            throw new IllegalStateException("Echec traitement enrichissement critique", e);
        }
    }
    @DltHandler
    public void handleMessage(String message){
        System.err.printf("le message envoye %s", message) ;
    }
}
