package com.pca.Backend.Entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "fraude")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fraude {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private Long amount;
    private String alertType;
    private String alertMessage;
    private LocalDateTime createdAt = LocalDateTime.now();
    private Long destinataireId;

}
