package com.pca.Backend.Entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ArchivageNotification")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoriqueNotifEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name="user_id")
    private Long userId;
    @Column(name="destinataire_id")
    private Long destinataireId;
    @Column(name="montant")
    private Long montant;
    @Column(name="date_notif")
    private LocalDateTime dateNotif = LocalDateTime.now();
    @Column(name="is_critical")
    private boolean isCritical;
    @Column(name="is_suspected")
    private boolean isSuspected;
    @Column(name="evnet_id")
    private String eventId;
    @Enumerated(EnumType.STRING)
    private Status status;

}
