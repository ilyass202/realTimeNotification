package com.pca.Backend.Config;

import org.springframework.boot.micrometer.metrics.autoconfigure.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

@Configuration
public class MetricsConfig {

    /** Concurrence configurée dans {@link ConsumerConfiguration} (transaction + fraude). */
    private static final double KAFKA_LISTENER_THREADS = 26.0;

    @Bean
    MeterRegistryCustomizer<MeterRegistry> commonTags() {
        return registry -> registry.config()
            .commonTags("application", "pfe-backend");
    }

    @Bean
    Gauge kafkaListenerConcurrencyGauge(MeterRegistry registry) {
        return Gauge.builder("pfe.kafka.listener.concurrency", () -> KAFKA_LISTENER_THREADS)
            .description("Threads consumers Kafka (ingress+intermediate+enrichment) — capacité parallèle")
            .register(registry);
    }
}
