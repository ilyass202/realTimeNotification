package com.pca.Backend.metrics;

import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.annotation.PostConstruct;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Métriques métier pour Prometheus / Grafana : débit, latence, erreurs, skips, DLT, notifications.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PipelineMetrics {

    private final MeterRegistry registry;

    /** Expose les séries `pfe_*` dans /actuator/prometheus avant le premier message Kafka. */
    @PostConstruct
    void registerMetricCatalog() {
        messageCounter("transaction", "bootstrap", "success");
        messageCounter("fraude", "bootstrap", "success");
        messageCounter("carte", "bootstrap", "success");
        Counter.builder("pfe.kafka.forwarded")
            .tag("pipeline", "transaction")
            .tag("source", "bootstrap")
            .tag("target", "bootstrap")
            .register(registry);
        Counter.builder("pfe.kafka.skipped")
            .tag("pipeline", "transaction")
            .tag("topic", "bootstrap")
            .tag("reason", "bootstrap")
            .register(registry);
        Counter.builder("pfe.kafka.dlt")
            .tag("pipeline", "transaction")
            .tag("topic", "bootstrap")
            .register(registry);
        Counter.builder("pfe.notification.sent")
            .tag("outcome", "bootstrap")
            .tag("critical", "false")
            .register(registry);
        processingTimer("transaction", "bootstrap", "bootstrap", "success");
        messageCounter("transaction", "bootstrap", "error");
        recordEventLagMillis("transaction", "bootstrap", "bootstrap", 0L);
    }

    public void track(String pipeline, String stage, String topic, Runnable action) {
        Timer.Sample sample = Timer.start(registry);
        try {
            action.run();
            sample.stop(processingTimer(pipeline, stage, topic, "success"));
            messageCounter(pipeline, topic, "success").increment();
        } catch (RuntimeException e) {
            sample.stop(processingTimer(pipeline, stage, topic, "error"));
            messageCounter(pipeline, topic, "error").increment();
            throw e;
        }
    }

    public <T> T track(String pipeline, String stage, String topic, Supplier<T> action) {
        Timer.Sample sample = Timer.start(registry);
        try {
            T result = action.get();
            sample.stop(processingTimer(pipeline, stage, topic, "success"));
            messageCounter(pipeline, topic, "success").increment();
            return result;
        } catch (RuntimeException e) {
            sample.stop(processingTimer(pipeline, stage, topic, "error"));
            messageCounter(pipeline, topic, "error").increment();
            throw e;
        }
    }

    public void skipped(String pipeline, String topic, String reason) {
        messageCounter(pipeline, topic, "skipped").increment();
        Counter.builder("pfe.kafka.skipped")
            .description("Messages ignorés (règle métier, user invalide, etc.)")
            .tag("pipeline", pipeline)
            .tag("topic", topic)
            .tag("reason", reason)
            .register(registry)
            .increment();
    }

    public void forwarded(String pipeline, String sourceTopic, String targetTopic) {
        Counter.builder("pfe.kafka.forwarded")
            .description("Messages routés vers un topic intermédiaire ou enrichissement")
            .tag("pipeline", pipeline)
            .tag("source", sourceTopic)
            .tag("target", targetTopic)
            .register(registry)
            .increment();
    }

    public void dlt(String pipeline, String topic) {
        Counter.builder("pfe.kafka.dlt")
            .description("Messages en Dead Letter Topic (échec après retries)")
            .tag("pipeline", pipeline)
            .tag("topic", topic)
            .register(registry)
            .increment();
    }

    public void notification(String outcome, boolean critical) {
        Counter.builder("pfe.notification.sent")
            .description("Tentatives d'envoi Firebase")
            .tag("outcome", outcome)
            .tag("critical", String.valueOf(critical))
            .register(registry)
            .increment();
    }

    public Timer.Sample startNotificationTimer() {
        return Timer.start(registry);
    }

    public void stopNotificationTimer(Timer.Sample sample, String outcome, boolean critical) {
        sample.stop(Timer.builder("pfe.notification.duration")
            .description("Latence envoi Firebase")
            .tag("outcome", outcome)
            .tag("critical", String.valueOf(critical))
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(registry));
    }

    /**
     * Délai entre {@code created_at} dans le message (CDC) et le moment du traitement — indicateur temps réel.
     */
    public void recordEventLagFromPayload(String pipeline, String stage, String topic, JsonNode json) {
        long lagMs = EventTimestampParser.lagMillis(json);
        if (lagMs < 0) {
            log.debug("event_lag ignoré (pas de created_at valide), pipeline={}, topic={}", pipeline, topic);
            return;
        }
        recordEventLagMillis(pipeline, stage, topic, lagMs);
    }

    public void recordEventLagFromTimestamp(String pipeline, String stage, String topic, Long rawTimestamp) {
        long lagMs = EventTimestampParser.lagMillisFromRaw(rawTimestamp);
        if (lagMs < 0) {
            return;
        }
        recordEventLagMillis(pipeline, stage, topic, lagMs);
    }

    public void recordEventLagFromKafkaRecord(String pipeline, String stage, String topic, long kafkaTimestampMs) {
        long lagMs = EventTimestampParser.lagMillisFromKafkaRecord(kafkaTimestampMs);
        if (lagMs < 0) {
            return;
        }
        recordEventLagMillis(pipeline, stage, topic, lagMs);
    }

    private void recordEventLagMillis(String pipeline, String stage, String topic, long lagMs) {
        DistributionSummary.builder("pfe.kafka.event_lag")
            .description("Délai événement source → traitement consumer (millisecondes)")
            .tag("pipeline", pipeline)
            .tag("stage", stage)
            .tag("topic", topic)
            .publishPercentileHistogram()
            .register(registry)
            .record(lagMs);
    }

    private Counter messageCounter(String pipeline, String topic, String outcome) {
        return Counter.builder("pfe.kafka.messages")
            .description("Messages traités par les listeners Kafka")
            .tag("pipeline", pipeline)
            .tag("topic", topic)
            .tag("outcome", outcome)
            .register(registry);
    }

    private Timer processingTimer(String pipeline, String stage, String topic, String outcome) {
        return Timer.builder("pfe.kafka.processing")
            .description("Durée de traitement d'un message (par étape du pipeline)")
            .tag("pipeline", pipeline)
            .tag("stage", stage)
            .tag("topic", topic)
            .tag("outcome", outcome)
            .publishPercentileHistogram()
            .register(registry);
    }
}
