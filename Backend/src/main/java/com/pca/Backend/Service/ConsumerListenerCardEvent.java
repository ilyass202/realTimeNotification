package com.pca.Backend.Service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pca.Backend.Entity.ReferentielAlerte;
import com.pca.Backend.Repo.ReferentielAlerteRepo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsumerListenerCardEvent {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ReferentielAlerteRepo referentielAlerteRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(
        topics = "notifications.public.carte",
        groupId = "card-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumer(String message) {
        try {
            JsonNode json = objectMapper.readTree(message);
            System.out.printf("le message %s", message);
            Long userId = json.path("user_id").asLong();
            ReferentielAlerte ref = referentielAlerteRepo.findByClientId(userId).orElse(null);
            if (ref != null && ref.isAlerteCarte()) {
                kafkaTemplate.send("card-intermediare", message);
            }
        } catch (JsonProcessingException e) {
            log.error("Invalid card message payload={}", message, e);
            throw new IllegalStateException("Invalid card message", e);
        }
    }
}
