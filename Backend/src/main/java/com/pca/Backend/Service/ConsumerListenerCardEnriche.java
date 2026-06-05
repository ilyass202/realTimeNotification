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
public class ConsumerListenerCardEnriche {

    private static final String PIPELINE = "carte";
    private static final String TOPIC = "card-enrichissement";

    private final NotificationService notifService;
    private final ObjectMapper mapper = new ObjectMapper();
    private final PipelineMetrics metrics;

    @RetryableTopic(
        attempts = "2",
        backOff = @BackOff(delay = 1000L, multiplier = 2),
        autoCreateTopics = "true",
        topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
    )
    @KafkaListener(
        topics = TOPIC,
        groupId = "card-enri-grp",
        containerFactory = "enrichmentListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, String> record) {
        metrics.track(PIPELINE, "enrichment", TOPIC, () -> {
            try {
                String message = record.value();
                NotifEnrechi notif = mapper.readValue(message, NotifEnrechi.class);
                metrics.recordEventLagFromTimestamp(PIPELINE, "enrichment", TOPIC, notif.createdAt());
                if (notif.createdAt() == null || notif.createdAt() <= 0) {
                    metrics.recordEventLagFromKafkaRecord(PIPELINE, "enrichment", TOPIC, record.timestamp());
                }
                notifService.sendNotification(
                    notif.userId(),
                    notif.message(),
                    notif.amount(),
                    notif.isCritical()
                );
                notifService.ArchivageNotif(notif);
            } catch (Exception e) {
                throw new IllegalStateException("Echec consumer card-enrichissement", e);
            }
        });
    }

    @DltHandler
    public void handleError(String message) {
        metrics.dlt(PIPELINE, TOPIC);
        log.error("DLT card-enrichissement: {}", message);
    }
}
