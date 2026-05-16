package com.pca.Backend.Service;

import org.springframework.stereotype.Service;

import com.google.firebase.FirebaseException;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.pca.Backend.DTO.NotifEnrechi;
import com.pca.Backend.Entity.HistoriqueNotifEntity;
import com.pca.Backend.Entity.ReferentielUser;
import com.pca.Backend.Entity.Status;
import com.pca.Backend.Repo.HistoriqueNotif;
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
    private final HistoriqueNotif repoHistorique;
    public void sendNotification(Long userId, String body, Long amount, boolean isCritical, boolean isCard) {
        ReferentielUser user = userRepo.findById(userId).orElseThrow(()-> new EntityNotFoundException("userId nest pas trouvé"));
        if(StringUtils.isBlank(user.getFcmToken())){
            log.warn("Aucun fcmToken pour userId={}", userId);
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
        log.info("Notification envoyee Firebase, messageId={}, userId={}", response, userId);
    }
    catch(FirebaseException e){
        log.error("Echec envoi notification Firebase userId={}", userId, e);
        throw new IllegalStateException("Echec envoi notification Firebase", e);
    }   
    }
    public void ArchivageNotif(NotifEnrechi notifEnrichi){
        HistoriqueNotifEntity historiqueNotif = HistoriqueNotifEntity.builder()
        .userId(notifEnrichi.userId())
                     .destinataireId(notifEnrichi.destinationId())
                     .montant(notifEnrichi.amount())
                     .eventId(notifEnrichi.eventId())
                     .isCritical(notifEnrichi.isCritical())
                     .isSuspected(notifEnrichi.isSuspected())
                     .status(Status.DELIVRE)
                     .build();
        repoHistorique.save(historiqueNotif);

}
}
