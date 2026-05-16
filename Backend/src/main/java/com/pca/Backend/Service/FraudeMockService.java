package com.pca.Backend.Service;

import org.springframework.stereotype.Service;

import com.pca.Backend.DTO.VirementDTO;

@Service
public class FraudeMockService {
    public boolean isFraudVirement(VirementDTO virementDTO){
        if(virementDTO.amount() > 100000){
            return true;
        }
         if(Math.random() < 0.2){
            return true;
         }
        return false;
        }
    }
