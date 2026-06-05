package com.pca.Backend.Service;

import java.util.UUID;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pca.Backend.DTO.NotifEnrechi;
import com.pca.Backend.metrics.PipelineMetrics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsumerListenerInterHigh {

    private static final String PIPELINE = "fraude";
    private static final String INTER = "fraude-intermediare";
    private static final String ENRICH = "fraude-enrechissement";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper mapper = new ObjectMapper();
    private final PipelineMetrics metrics;

    @KafkaListener(topics = INTER, groupId = "inter_critique_grp", containerFactory = "fraudeListenerContainerFactory")
    public void consumeHigh(String message) {
        metrics.track(PIPELINE, "intermediate", INTER, () -> {
            try {
                var json = mapper.readTree(message);
                metrics.recordEventLagFromPayload(PIPELINE, "intermediate", INTER, json);
                Long userId = json.path("user_id").asLong();
                Long destinataireId = json.path("destinataire_id").asLong();
                Long amount = json.path("amount").asLong();
                Long createdAt = json.has("created_at")
                    ? json.path("created_at").asLong()
                    : json.path("createdAt").asLong(0);
                String eventId = UUID.randomUUID().toString().substring(0, 8);

                NotifEnrechi notifEnrechi = new NotifEnrechi(
                    userId,
                    destinataireId,
                    amount,
                    eventId,
                    true,
                    true,
                    "Alerte fraude: transaction critique detectée",
                    createdAt
                );
                kafkaTemplate.send(ENRICH, mapper.writeValueAsString(notifEnrechi));
                metrics.forwarded(PIPELINE, INTER, ENRICH);
            } catch (Exception e) {
                log.error("Echec consumer inter critique: {}", message, e);
                throw new IllegalStateException("Echec consumer inter critique", e);
            }
        });
    }
}
