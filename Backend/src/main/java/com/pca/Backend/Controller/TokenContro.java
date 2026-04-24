package com.pca.Backend.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.pca.Backend.DTO.RegisterToken;
import com.pca.Backend.DTO.TokenRequest;
import com.pca.Backend.Service.TokenManager;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;



@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TokenContro {
    private final TokenManager tokenManager;
    @PostMapping("/saveToken")
    public ResponseEntity<?> saveToken(@RequestBody RegisterToken tokenDto) {
        System.out.println("REQUEST RECEIVED = " + tokenDto);
        System.out.println("TOKEN = " + tokenDto.fcmToken());
        System.out.println("USER = " + tokenDto.userId());
        var response = tokenManager.saveToken(tokenDto);
        return ResponseEntity.ok(response);   
    }
    @GetMapping("/getToken/{userId}")
    public ResponseEntity<?> getToken(@PathVariable("userId") Long userId) {
        var tokenRequest = new TokenRequest(userId);
         var response = tokenManager.getToken(tokenRequest);
        return ResponseEntity.ok(response);
    }
    
}
