package com.pca.Backend.Service;

import java.util.UUID;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pca.Backend.DTO.NotifEnrechi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsumerListenerCardInter {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    @KafkaListener(
        topics = "card-intermediare",
        groupId = "card-inter-grp",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(String message) {
        try {
            var json = mapper.readTree(message);
            Long userId = json.path("user_id").asLong();
            Long amount = json.path("min_amount").asLong();
            String eventId = UUID.randomUUID().toString().substring(0, 8);
            NotifEnrechi notifEnrechi = new NotifEnrechi(
                userId,
                null,
                amount,
                eventId,
                false,
                false,
                getMessage(amount) 
            );
        
            kafkaTemplate.send("card-enrichissement", mapper.writeValueAsString(notifEnrechi));
        } catch (Exception e) {
            throw new IllegalStateException("Echec consumer card-intermediare", e);
        }
    }
    private String getMessage(Long amount){
        if(amount == 0L){
            return "carte crée avec succès, il faut disposer un montant";
        }
        else {
            return "carte crée avec succès";
        }
    }
}
