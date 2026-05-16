package com.pca.Backend.Repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pca.Backend.Entity.ReferentielUser;

@Repository
public interface UserRepo extends JpaRepository<ReferentielUser, Long> {
    Optional<ReferentielUser> findByEmail(String email);
    Optional<ReferentielUser> findById(Long id);
}
