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
public class ConsumerListenerInterHigh {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper mapper = new ObjectMapper();
    @KafkaListener(topics = "fraude-intermediare", groupId = "inter_critique_grp", containerFactory = "fraudeListenerContainerFactory")
    public void consumeHigh(String message) {
        try {
            var json = mapper.readTree(message);
            Long userId = json.path("user_id").asLong();
            Long destinataireId = json.path("destinataire_id").asLong();
            Long amount = json.path("amount").asLong();
            String eventId = UUID.randomUUID().toString().substring(0, 8);

            NotifEnrechi notifEnrechi = new NotifEnrechi(
                userId,
                destinataireId,
                amount,
                eventId,
                true,
                true,
                "Alerte fraude: transaction critique detectée"
            );
            kafkaTemplate.send("fraude-enrechissement", mapper.writeValueAsString(notifEnrechi));
        } catch (Exception e) {
            log.error("Echec consumer inter critique: {}", message, e);
            throw new IllegalStateException("Echec consumer inter critique", e);
        }
    }

}
