package com.pca.Backend.Service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pca.Backend.Entity.ReferentielAlerte;
import com.pca.Backend.metrics.PipelineMetrics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsumerListenerHigh {

    private static final String PIPELINE = "fraude";
    private static final String INGRESS = "notification.public.fraude";
    private static final String INTER = "fraude-intermediare";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ReferentielAlerteCache referentielAlerteCache;
    private final PipelineMetrics metrics;

    @KafkaListener(topics = INGRESS, groupId = "highPriority", containerFactory = "ingressListenerContainerFactory")
    public void consumeHighPrio(String message) {
        metrics.track(PIPELINE, "ingress", INGRESS, () -> {
            try {
                JsonNode json = objectMapper.readTree(message);
                metrics.recordEventLagFromPayload(PIPELINE, "ingress", INGRESS, json);
                Long userId = json.path("user_id").asLong();
                ReferentielAlerte alerte = referentielAlerteCache.getForClientId(userId).orElse(null);
                if (alerte != null
                    && alerte.isActive()
                    && !alerte.isBlackList()
                    && alerte.isAlerteFraude()) {
                    kafkaTemplate.send(INTER, message);
                    metrics.forwarded(PIPELINE, INGRESS, INTER);
                } else {
                    metrics.skipped(PIPELINE, INGRESS, "alerte_fraude_disabled");
                }
            } catch (Exception e) {
                throw new IllegalStateException("Echec traitement fraude", e);
            }
        });
    }
}
