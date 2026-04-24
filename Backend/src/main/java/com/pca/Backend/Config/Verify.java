package com.pca.Backend.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;

import jakarta.annotation.PostConstruct;

public class Verify {
    @Autowired 
    private KafkaProperties kafkaProperties;
    @PostConstruct
    public void printProperties(){
        System.out.println("l'adress de serveur" + kafkaProperties.getBootstrapServers());
        System.out.println("l'adress SSL" + kafkaProperties.getSsl());
    }
}
