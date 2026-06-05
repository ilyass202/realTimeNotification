package com.pca.Backend.Service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
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
public class ConsumerListenerEnriche {

    private static final String PIPELINE = "transaction";
    private static final String TOPIC = "transaction-enrechissement";

    private final NotificationService notifService;
    private final ObjectMapper mapper = new ObjectMapper();
    private final PipelineMetrics metrics;

    @RetryableTopic(
        attempts = "2",
        backOff = @BackOff(delay = 1000L, multiplier = 2),
        autoCreateTopics = "true",
        topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
    )
    @KafkaListener(topics = TOPIC, groupId = "enri-grp", containerFactory = "enrichmentListenerContainerFactory")
    public void consume(ConsumerRecord<String, String> record) {
        metrics.track(PIPELINE, "enrichment", TOPIC, () -> {
            try {
                String message = record.value();
                NotifEnrechi notifEnrechi = mapper.readValue(message, NotifEnrechi.class);
                metrics.recordEventLagFromTimestamp(PIPELINE, "enrichment", TOPIC, notifEnrechi.createdAt());
                if (notifEnrechi.createdAt() == null || notifEnrechi.createdAt() <= 0) {
                    metrics.recordEventLagFromKafkaRecord(PIPELINE, "enrichment", TOPIC, record.timestamp());
                }
                notifService.sendNotification(
                    notifEnrechi.userId(),
                    notifEnrechi.message(),
                    notifEnrechi.amount(),
                    notifEnrechi.isCritical()
                );
                notifService.ArchivageNotif(notifEnrechi);
            } catch (Exception e) {
                log.error("Echec enrichissement transaction: {}", e.getMessage());
                throw new IllegalStateException("Echec enrichissement transaction", e);
            }
        });
    }

    @DltHandler
    public void handleError(String message) {
        metrics.dlt(PIPELINE, TOPIC);
        log.error("DLT transaction-enrechissement: {}", message);
    }
}
