package com.pca.Backend.Config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

@Configuration
@EnableKafka
public class ConsumerConfiguration {

    private final KafkaProperties kafkaProperties;

    @Value("${pfe.kafka.ingress.concurrency:6}")
    private int ingressConcurrency;

    @Value("${pfe.kafka.intermediate.concurrency:6}")
    private int intermediateConcurrency;

    @Value("${pfe.kafka.enrichment.concurrency:8}")
    private int enrichmentConcurrency;

    @Value("${pfe.kafka.consumer.max-poll-records-fast:500}")
    private int maxPollRecordsFast;

    @Value("${pfe.kafka.consumer.max-poll-records-enrich:25}")
    private int maxPollRecordsEnrich;

    public ConsumerConfiguration(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactoryFast() {
        return buildConsumerFactory(maxPollRecordsFast, 300_000);
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactoryEnrich() {
        return buildConsumerFactory(maxPollRecordsEnrich, 600_000);
    }

    private DefaultKafkaConsumerFactory<String, String> buildConsumerFactory(int maxPollRecords, int maxPollIntervalMs) {
        Map<String, Object> configs = new HashMap<>(kafkaProperties.buildConsumerProperties());
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configs.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        configs.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, maxPollIntervalMs);
        configs.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 16_384);
        configs.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 100);
        return new DefaultKafkaConsumerFactory<>(configs);
    }

    @Bean(name = "ingressListenerContainerFactory")
    ConcurrentKafkaListenerContainerFactory<String, String> ingressListenerContainerFactory() {
        return buildFactory(consumerFactoryFast(), ingressConcurrency, 2_000L);
    }


    @Bean(name = "kafkaListenerContainerFactory")
    ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        return buildFactory(consumerFactoryFast(), intermediateConcurrency, 2_000L);
    }

    /** Enrichissement final : Firebase + JPA (plus lent). */
    @Bean(name = "enrichmentListenerContainerFactory")
    ConcurrentKafkaListenerContainerFactory<String, String> enrichmentListenerContainerFactory() {
        var factory = buildFactory(consumerFactoryEnrich(), enrichmentConcurrency, 4_000L);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        return factory;
    }

    @Bean(name = "fraudeListenerContainerFactory")
    ConcurrentKafkaListenerContainerFactory<String, String> fraudeListenerContainerFactory() {
        return buildFactory(consumerFactoryFast(), intermediateConcurrency, 2_000L);
    }

    private ConcurrentKafkaListenerContainerFactory<String, String> buildFactory(
        ConsumerFactory<String, String> consumerFactory,
        int concurrency,
        long pollTimeoutMs
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(concurrency);
        factory.getContainerProperties().setPollTimeout(pollTimeoutMs);
        return factory;
    }
}
