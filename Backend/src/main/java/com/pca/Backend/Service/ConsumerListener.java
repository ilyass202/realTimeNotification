package com.pca.Backend.Service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pca.Backend.Entity.ReferentielAlerte;
import com.pca.Backend.Repo.ReferentielAlerteRepo;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsumerListener {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
     private final ObjectMapper objectMapper = new ObjectMapper();
     @Autowired
     private ReferentielAlerteRepo referentielAlerteRepo;
    @KafkaListener(topics = "notifications.public.transactions", groupId = "notifs", containerFactory = "kafkaListenerContainerFactory")
    public void consumer(String message){
            try{
                JsonNode json = objectMapper.readTree(message);
                Long userId = json.path("user_id").asLong();
                ReferentielAlerte referentielAlerte = referentielAlerteRepo.findByClientId(userId).orElse(null);
                if(referentielAlerte != null && referentielAlerte.isAlerteTransaction()){
                    kafkaTemplate.send("transaction-intermediare", message);
                } else {
                    return;
                }
            }
            catch(JsonProcessingException e){
                log.error("Invalid transaction message payload={}", message, e);
                throw new IllegalStateException("Invalid transaction message", e);
            }
        }
    }
    
    