package com.pca.Backend.Repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pca.Backend.Entity.Fraude;

@Repository
public interface FraudeRepo extends JpaRepository<Fraude, Long> {
    Optional<Fraude> findByUserId(Long userId);
    Optional<Fraude> findById(Long id);
}
