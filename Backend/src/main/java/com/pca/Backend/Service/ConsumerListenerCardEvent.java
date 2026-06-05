package com.pca.Backend.Service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pca.Backend.Entity.ReferentielAlerte;
import com.pca.Backend.metrics.PipelineMetrics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsumerListenerCardEvent {

    private static final String PIPELINE = "carte";
    private static final String INGRESS = "notifications.public.carte";
    private static final String INTER = "card-intermediare";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ReferentielAlerteCache referentielAlerteCache;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PipelineMetrics metrics;

    @KafkaListener(
        topics = INGRESS,
        groupId = "card-group",
        containerFactory = "ingressListenerContainerFactory"
    )
    public void consumer(String message) {
        metrics.track(PIPELINE, "ingress", INGRESS, () -> {
            try {
                JsonNode json = objectMapper.readTree(message);
                metrics.recordEventLagFromPayload(PIPELINE, "ingress", INGRESS, json);
                Long userId = json.path("user_id").asLong();
                ReferentielAlerte alerte = referentielAlerteCache.getForClientId(userId).orElse(null);
                if (alerte != null
                    && alerte.isActive()
                    && !alerte.isBlackList()
                    && alerte.isAlerteCarte()) {
                    kafkaTemplate.send(INTER, message);
                    metrics.forwarded(PIPELINE, INGRESS, INTER);
                } else {
                    metrics.skipped(PIPELINE, INGRESS, "alerte_carte_disabled");
                }
            } catch (JsonProcessingException e) {
                log.error("Invalid card message payload={}", message, e);
                throw new IllegalStateException("Invalid card message", e);
            }
        });
    }
}
