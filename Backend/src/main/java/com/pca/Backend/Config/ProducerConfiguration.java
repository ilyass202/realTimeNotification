package com.pca.Backend.Config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.apache.kafka.clients.producer.ProducerConfig;


@Configuration
public class ProducerConfiguration {
    @Autowired
    private KafkaProperties kafkaProperties;
    @Bean 
    public ProducerFactory<String, String> producerFactory(){
        Map<String, Object> configs = new HashMap<>(kafkaProperties.buildProducerProperties());
        configs.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, "com.pca.Backend.Service.ProducerInter");
        return new DefaultKafkaProducerFactory<>(configs);
    }
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

}
