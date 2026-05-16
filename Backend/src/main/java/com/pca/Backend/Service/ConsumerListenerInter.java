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
public class ConsumerListenerInter{ 
    private final KafkaTemplate<String, String> kafkaTemplate;
    private ObjectMapper mapper = new ObjectMapper();
    @KafkaListener(topics = "transaction-intermediare", groupId = "inter_grp", containerFactory="kafkaListenerContainerFactory")
    public void consume(String message){
        try{
        var json = mapper.readTree(message);
        Long userId =  json.path("user_id").asLong(); 
        Long destinataireId = json.has("destinataire_id")
            ? json.path("destinataire_id").asLong()
            : json.path("destinataire").asLong();
        Long amount = json.path("amount").asLong();
        if (userId == null || userId <= 0) {
            return;
        }
        String eventId = UUID.randomUUID().toString().substring(0, 8);
        NotifEnrechi notifEnrechi = new NotifEnrechi(
            userId,
            destinataireId,
            amount,
            eventId,
            false,
            false,
            "Transaction traitée avec succès"
        );

        kafkaTemplate.send("transaction-enrechissement", mapper.writeValueAsString(notifEnrechi));
    }
    catch(Exception e){
        throw new IllegalStateException("Echec consumer inter", e);
    }
}

}
