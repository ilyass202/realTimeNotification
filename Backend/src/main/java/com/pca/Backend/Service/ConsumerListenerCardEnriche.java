package com.pca.Backend.Service;

import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pca.Backend.DTO.NotifEnrechi;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConsumerListenerCardEnriche {

    private final NotificationService notifService;
    private final ObjectMapper mapper = new ObjectMapper();

    @RetryableTopic(
        attempts = "2",
        backOff = @BackOff(delay = 1000L, multiplier = 2),
        autoCreateTopics = "true",
        topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
    )
    @KafkaListener(
        topics = "card-enrichissement",
        groupId = "card-enri-grp",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(String message) {
        try {
            NotifEnrechi notif = mapper.readValue(message, NotifEnrechi.class);
            String body = notif.message();
            notifService.sendNotification(
                notif.userId(),
                body,
                notif.amount(),
                notif.isCritical()
            );
            notifService.ArchivageNotif(notif);
        } catch (Exception e) {
            throw new IllegalStateException("Echec consumer card-enrichissement", e);
        }
    }

    @DltHandler
    public void handleError(
        String message
    ) {
        System.err.printf("le message est %s", message);
    }
}
