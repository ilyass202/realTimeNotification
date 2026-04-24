package com.pca.Backend.Service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConsumerListener {
    private final NotificationService notifService;
     private final ObjectMapper objectMapper = new ObjectMapper();
     
    /* @RetryableTopic(attempts = "2", backOff = @BackOff(delay = 2000, multiplier=1.5), 
    autoCreateTopics = "true",
     topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
)*/ // j'ai pas utilisé un mécanisme de gestion d'erreur car j'ai choisi de traiter les messages de transaction en Batch 
//en effet cela permet de minimiser appel a Firebase et la charge sur le réseau et les messages de transaction n'est pas nécessaire de faire un mécanisme de gestion des erreurs 
    @KafkaListener(topics = "notifications.public.transactions", groupId = "notifs", containerFactory = "kafkaListenerContainerFactory")
    public void consumeLowPrio(List<String> messages){
         for(String message : messages){
            try{
                JsonNode json = objectMapper.readTree(message);
                Long userId = json.path("user_id").asLong();
                Long destinataireId = json.path("destinataire").asLong();
                Long amount = json.path("amount").asLong();
                 /* notifService.sendNotification(
                    userId,
                    "Votre virement a été effectué avec succès de valeur : " + amount ,
                    amount,
                    false
                ); */ 
                notifService.sendNotification(
                    destinataireId,
                    "Vous avez reçu un virement de : " + amount + "DH",
                    amount,
                    false
                );
            }
            catch(JsonProcessingException e){
                System.err.print(e.getMessage());
            }
           System.out.printf("le message %s", message);

        }
        }
       /* @DltHandler
        public void handleMessage(String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic){
            System.err.printf( "%s est envoyé au topic %s",message, topic);
             
        }*/
    }
    