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
public class ConsumerListenerCardInter {

    private static final String PIPELINE = "carte";
    private static final String INTER = "card-intermediare";
    private static final String ENRICH = "card-enrichissement";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper mapper = new ObjectMapper();
    private final PipelineMetrics metrics;

    @KafkaListener(
        topics = INTER,
        groupId = "card-inter-grp",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(String message) {
        metrics.track(PIPELINE, "intermediate", INTER, () -> {
            try {
                var json = mapper.readTree(message);
                metrics.recordEventLagFromPayload(PIPELINE, "intermediate", INTER, json);
                long userId = json.path("user_id").asLong(0);
                Long amount = json.path("min_amount").asLong();
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
                    null,
                    amount,
                    eventId,
                    false,
                    false,
                    getMessage(amount),
                    createdAt
                );
                kafkaTemplate.send(ENRICH, mapper.writeValueAsString(notifEnrechi));
                metrics.forwarded(PIPELINE, INTER, ENRICH);
            } catch (Exception e) {
                throw new IllegalStateException("Echec consumer card-intermediare", e);
            }
        });
    }

    private String getMessage(Long amount) {
        if (amount == null || amount == 0L) {
            return "Carte créée avec succès, il faut disposer un montant";
        }
        return "Carte créée avec succès";
    }
}
