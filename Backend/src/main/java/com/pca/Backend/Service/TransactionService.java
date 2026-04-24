
package com.pca.Backend.Service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.pca.Backend.DTO.VirementDTO;
import com.pca.Backend.DTO.VirementResponse;
import com.pca.Backend.Entity.Fraude;
import com.pca.Backend.Entity.Transaction;
import com.pca.Backend.Repo.FraudeRepo;
import com.pca.Backend.Repo.TransactionRepo;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepo transactionRepo;
    private final FraudeMockService fraudeMockService;
    private final FraudeRepo fraudeRepo;
    @Transactional
    public VirementResponse createVirement(VirementDTO virement){
        if(virement.amount() <= 0 ){
            throw new IllegalArgumentException("le montant doit étre positive");
        }
        if(virement.userId().equals(virement.destinataireId())){
            throw new IllegalArgumentException("le transfert ne peut pas se faire au meme compte");
        }
        boolean isFraud = fraudeMockService.isFraudVirement(virement);
        if(isFraud){
            var fraude = Fraude.builder().userId(virement.userId()).amount(virement.amount()).alertType("fraude").alertMessage("fraude detectée").build();
            fraudeRepo.save(fraude);
            return new VirementResponse("fraude detectée", null);
        }
        else{
        Transaction transaction = new Transaction();
        transaction.setUserId(virement.userId());
        transaction.setAmount(virement.amount());
        transaction.setDestinataireId(virement.destinataireId());
        transaction.setCreatedAt(LocalDateTime.now());
        transactionRepo.save(transaction);
        return new VirementResponse("transaction avec succes", transaction.getId());
    }
}    
}
