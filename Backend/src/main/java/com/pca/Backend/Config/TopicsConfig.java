package com.pca.Backend.Config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class TopicsConfig {
    @Autowired
    private KafkaProperties kafkaProperties;
    @Bean 
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>(kafkaProperties.buildAdminProperties());
        return new KafkaAdmin(configs);
    }
    @Bean
    public NewTopic TopicEnrechissementCritique(){
        return new NewTopic("fraude-enrechissement", 3, (short) 1);
    }
    @Bean
    public NewTopic TopicEnrechissement(){
        return new NewTopic("transaction-enrechissement", 6, (short) 1);
    }
    @Bean 
    public NewTopic topicIntermediaire(){
        return new NewTopic("transaction-intermediare", 6, (short) 1);
    }
    @Bean 
    public NewTopic topicIntermediaireCritique(){
       return new NewTopic("fraude-intermediare", 3, (short)1);
    }
    @Bean
    public NewTopic topicCardIntermediare(){
         return new NewTopic("card-intermediare", 4, (short) 1);
    }
    @Bean
    public NewTopic topicCardEnrichissement() {
        return new NewTopic("card-enrichissement", 4, (short) 1);
    }
}
