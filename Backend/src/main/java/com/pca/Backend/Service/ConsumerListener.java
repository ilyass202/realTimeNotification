package com.pca.Backend.Service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pca.Backend.Entity.ReferentielAlerte;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.pca.Backend.metrics.EventTimestampParser;
import com.pca.Backend.metrics.PipelineMetrics;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsumerListener {
    private static final String PIPELINE = "transaction";
    private static final String INGRESS = "notifications.public.transactions";
    private static final String INTER = "transaction-intermediare";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ReferentielAlerteCache referentielAlerteCache;
    private final PipelineMetrics metrics;

    @KafkaListener(topics = INGRESS, groupId = "notifs", containerFactory = "ingressListenerContainerFactory")
    public void consumer(ConsumerRecord<String, String> consumerRecord) {
        String message = consumerRecord.value();
        metrics.track(PIPELINE, "ingress", INGRESS, () -> {
            try {
                JsonNode json = objectMapper.readTree(message);
                metrics.recordEventLagFromPayload(PIPELINE, "ingress", INGRESS, json);
                if (EventTimestampParser.lagMillis(json) < 0) {
                    metrics.recordEventLagFromKafkaRecord(PIPELINE, "ingress", INGRESS, consumerRecord.timestamp());
                }
                Long userId = json.path("user_id").asLong();
                Long id = json.path("id").asLong();
                ReferentielAlerte referentielAlerte = referentielAlerteCache.getForClientId(userId).orElse(null);
                if (referentielAlerte != null
                    && referentielAlerte.isActive()
                    && !referentielAlerte.isBlackList()
                    && referentielAlerte.isAlerteTransaction()) {
                    ProducerRecord<String, String> producerRecord =
                        new ProducerRecord<>(INTER, String.valueOf(id), message);
                    kafkaTemplate.send(producerRecord);
                    metrics.forwarded(PIPELINE, INGRESS, INTER);
                } else {
                    metrics.skipped(PIPELINE, INGRESS, "alerte_transaction_disabled");
                }
            } catch (JsonProcessingException e) {
                log.error("Invalid transaction message payload={}", message, e);
                throw new IllegalStateException("Invalid transaction message", e);
            }
        });
    }
}
