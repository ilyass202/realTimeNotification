package com.pca.Backend.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pca.Backend.DTO.CardDtoRequest;
import com.pca.Backend.DTO.CardResponse;
import com.pca.Backend.Service.CardService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("card")
@RequiredArgsConstructor
public class CardController {
    private final CardService service;
    @PostMapping("/createCard")
    public ResponseEntity<CardResponse> createCard(@RequestBody CardDtoRequest request) {
        try{
            CardResponse response = service.createCard(request);
            return ResponseEntity.ok(response);
        }
        catch(Exception e){
            throw new IllegalArgumentException();
        }
        
    }
    
    

}
