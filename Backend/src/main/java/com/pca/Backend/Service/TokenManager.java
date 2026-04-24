package com.pca.Backend.Service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.pca.Backend.DTO.RegisterToken;
import com.pca.Backend.DTO.ResponseRegToken;
import com.pca.Backend.DTO.ResponseToken;
import com.pca.Backend.DTO.TokenRequest;
import com.pca.Backend.Entity.UserEntity;
import com.pca.Backend.Repo.UserRepo;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TokenManager {
    private final UserRepo userRepo;
    @Transactional
    public ResponseRegToken saveToken(RegisterToken register){
        UserEntity user = userRepo.findById(register.userId()).orElseThrow(()-> new EntityNotFoundException("le user nest pas trouvé"));
        user.setFcmToken(register.fcmToken());
        user.setUpdatedToken(LocalDateTime.now());
        userRepo.save(user);
       return new ResponseRegToken("token enregistré" , String.valueOf(register.userId()));
    }
    public ResponseToken getToken(TokenRequest token){
         UserEntity user = userRepo.findById(token.userId()).orElseThrow(()-> new EntityNotFoundException("le user nest pas trouvé"));
         return user.getFcmToken() != null ? new ResponseToken(user.getFcmToken(), !false) : new ResponseToken("", false);
        
    }
}
