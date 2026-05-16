package com.pca.Backend.Repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pca.Backend.Entity.Card;

@Repository
public interface CardRepo extends JpaRepository<Card, Long>{
}
