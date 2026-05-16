package com.pca.Backend.Repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pca.Backend.Entity.ReferentielAlerte;

@Repository
public interface ReferentielAlerteRepo extends JpaRepository<ReferentielAlerte, Long> {
    Optional<ReferentielAlerte> findByClientId(Long clientId);
}
