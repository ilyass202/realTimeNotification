package com.pca.Backend.Repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pca.Backend.Entity.HistoriqueNotifEntity;

@Repository
public interface HistoriqueNotif extends JpaRepository<HistoriqueNotifEntity, Long>{

}
