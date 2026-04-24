package com.pca.Backend.Repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pca.Backend.Entity.Transaction;

@Repository
public interface TransactionRepo extends JpaRepository<Transaction, Long>{
    
}
