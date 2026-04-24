package com.pca.Backend.Config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;


@Configuration
@EnableKafka
public class ConsumerConfiguration {
    @Autowired
    private KafkaProperties kafkaProperties;
    @Bean
    public ConsumerFactory<String, String> consumerFactory(){
        Map<String, Object> configs = new HashMap<>(kafkaProperties.buildConsumerProperties());
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configs.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 40);
        return new DefaultKafkaConsumerFactory<>(configs);
    }
    @Bean(name = "kafkaListenerContainerFactory")
    ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(){
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setPollTimeout(4000L);
        factory.setConcurrency(3);
        factory.setBatchListener(true);
        return factory;
    }
    @Bean(name="fraudeListenerContainerFactory")
    ConcurrentKafkaListenerContainerFactory<String, String> fraudeListenerContainerFactory(){
    var factory = new ConcurrentKafkaListenerContainerFactory<String, String>();
    factory.setConsumerFactory(consumerFactory());
    factory.setConcurrency(2);
    factory.getContainerProperties().setPollTimeout(2000L);
    return factory;
    }

}
