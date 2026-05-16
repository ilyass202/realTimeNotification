package com.pca.Backend.Entity;

import jakarta.persistence.Column;
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
@Table(name = "ReferentielAlerte")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReferentielAlerte {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name="client_id")
    private Long clientId;
    @Column(name="alerte_fraude")
    private boolean alerteFraude = true;
    @Column(name="alerte_transaction")
    private boolean alerteTransaction = true;
    @Column(name = "alerte_carte")
    private boolean alerteCarte = true;
    @Column(name="is_active")
    private boolean isActive = true;
    @Column(name="is_BlackList")
    private boolean isBlackList = false;
    @Column(name="telephone")
    private String telephone;
}
