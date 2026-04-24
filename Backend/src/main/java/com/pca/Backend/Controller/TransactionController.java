package com.pca.Backend.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pca.Backend.DTO.VirementDTO;
import com.pca.Backend.Service.TransactionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("transaction")
@RequiredArgsConstructor
public class TransactionController {
       private final TransactionService transactionService;
       @PostMapping("/virement")
       public ResponseEntity<?> virement(@RequestBody VirementDTO virementDTO){
          try{
               var response = transactionService.createVirement(virementDTO);
               return ResponseEntity.ok(response);
          }
          catch(IllegalArgumentException e){
            return ResponseEntity.ok(e.getMessage());

          }
       }    
}
