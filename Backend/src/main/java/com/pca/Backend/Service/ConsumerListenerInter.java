package com.pca.Backend.Service;

import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pca.Backend.DTO.NotifEnrechi;
import com.pca.Backend.metrics.EventTimestampParser;
import com.pca.Backend.metrics.PipelineMetrics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsumerListenerInter {

    private static final String PIPELINE = "transaction";
    private static final String INTER = "transaction-intermediare";
    private static final String ENRICH = "transaction-enrechissement";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper mapper = new ObjectMapper();
    private final PipelineMetrics metrics;

    @KafkaListener(topics = INTER, groupId = "inter_grp", containerFactory = "kafkaListenerContainerFactory")
    public void consume(ConsumerRecord<String, String> record) {
        metrics.track(PIPELINE, "intermediate", INTER, () -> {
            try {
                String message = record.value();
                var json = mapper.readTree(message);
                metrics.recordEventLagFromPayload(PIPELINE, "intermediate", INTER, json);
                if (EventTimestampParser.lagMillis(json) < 0) {
                    metrics.recordEventLagFromKafkaRecord(PIPELINE, "intermediate", INTER, record.timestamp());
                }
                long userId = json.path("user_id").asLong(0);
                Long id = json.path("id").asLong();
                Long destinataireId = json.has("destinataire_id")
                    ? json.path("destinataire_id").asLong()
                    : json.path("destinataire").asLong();
                Long amount = json.path("amount").asLong();
                Long createdAt = json.has("created_at")
                    ? json.path("created_at").asLong()
                    : json.path("createdAt").asLong(0);
                if (userId <= 0) {
                    metrics.skipped(PIPELINE, INTER, "invalid_user_id");
                    return;
                }
                String eventId = UUID.randomUUID().toString().substring(0, 8);
                NotifEnrechi notifEnrechi = new NotifEnrechi(
                    userId,
                    destinataireId,
                    amount,
                    eventId,
                    false,
                    false,
                    "Transaction traitée avec succès",
                    createdAt
                );
                ProducerRecord<String, String> recordProduced =
                    new ProducerRecord<>(ENRICH, String.valueOf(id), mapper.writeValueAsString(notifEnrechi));
                kafkaTemplate.send(recordProduced);
                metrics.forwarded(PIPELINE, INTER, ENRICH);
            } catch (Exception e) {
                throw new IllegalStateException("Echec consumer inter", e);
            }
        });
    }
}
