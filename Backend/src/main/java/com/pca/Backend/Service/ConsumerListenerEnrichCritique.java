package com.pca.Backend.Service;

import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pca.Backend.DTO.NotifEnrechi;
import com.pca.Backend.metrics.PipelineMetrics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsumerListenerEnrichCritique {

    private static final String PIPELINE = "fraude";
    private static final String TOPIC = "fraude-enrechissement";

    private final NotificationService notifService;
    private final ObjectMapper mapper = new ObjectMapper();
    private final PipelineMetrics metrics;

    @RetryableTopic(
        attempts = "3",
        backOff = @BackOff(delay = 1000L, multiplier = 1.5),
        topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
        autoCreateTopics = "true"
    )
    @KafkaListener(topics = TOPIC, groupId = "enri-critique-grp", containerFactory = "enrichmentListenerContainerFactory")
    public void consume(String message) {
        metrics.track(PIPELINE, "enrichment", TOPIC, () -> {
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
                throw new IllegalStateException("Echec traitement enrichissement critique", e);
            }
        });
    }

    @DltHandler
    public void handleMessage(String message) {
        metrics.dlt(PIPELINE, TOPIC);
        log.error("DLT fraude-enrechissement: {}", message);
    }
}
