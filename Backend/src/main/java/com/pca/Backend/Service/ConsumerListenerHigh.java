package com.pca.Backend.Service;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.pca.Backend.Entity.ReferentielAlerte;
import com.pca.Backend.Repo.ReferentielAlerteRepo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsumerListenerHigh {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private ObjectMapper objectMapper = new ObjectMapper();
    private final ReferentielAlerteRepo repo;
    @KafkaListener(topics="notification.public.fraude", groupId = "highPriority", containerFactory = "fraudeListenerContainerFactory")
    public void consumeHighPrio(String message){
       try{
          JsonNode json = objectMapper.readTree(message);
                Long userId = json.path("user_id").asLong();
               ReferentielAlerte alerte = repo.findByClientId(userId).orElse(null);
               if(alerte != null && alerte.isAlerteFraude()){
                    kafkaTemplate.send("fraude-intermediare", message);
               }    
               else {
                return;
               }
       }
       catch (Exception e){
             throw new IllegalStateException("Echec traitement fraude", e);
       }
    }
     
}
