package com.pca.Backend.Service;

import org.springframework.stereotype.Service;

import com.google.firebase.FirebaseException;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.pca.Backend.Entity.UserEntity;
import com.pca.Backend.Repo.UserRepo;

import io.micrometer.common.util.StringUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final UserRepo userRepo;
    public void sendNotification(Long userId, String body, Long amount, boolean isCritical) {
        UserEntity user = userRepo.findById(userId).orElseThrow(()-> new EntityNotFoundException("userId nest pas trouvé"));
        if(StringUtils.isBlank(user.getFcmToken())){
            return;
        }
    var message = Message.builder().setToken(user.getFcmToken())
    .putData("title", "virement")
    .putData("body", body)                
    .putData("montant", String.valueOf(amount))   
    .putData("screen", "Notifications")      
    .putData("critical", String.valueOf(isCritical))
    .build();
    try{
        String response = FirebaseMessaging.getInstance().send(message);
        log.info("la notification a été envoyé dans Firebase" + response);
        System.out.printf("le message avec Id %s a été envoyé", response);
    }
    catch(FirebaseException e){
        log.error(e.getMessage());
    }

}
}
