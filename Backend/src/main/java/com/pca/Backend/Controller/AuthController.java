package com.pca.Backend.Controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import com.pca.Backend.DTO.LoginDto;
import com.pca.Backend.DTO.SignUp;
import com.pca.Backend.Entity.ReferentielUser;
import com.pca.Backend.Service.ServiceAuth;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final ServiceAuth auth;
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody SignUp signUp) {
        ReferentielUser user = auth.signUp(signUp);
        return ResponseEntity.ok(Map.of(
            "id", user.getId(),
            "name", user.getName(),
            "email", user.getEmail(),
            "message", "Compte créé. Connectez-vous avec POST /api/auth/login"
        ));
    }
   @PostMapping("/login")
   public ResponseEntity<?> login(@RequestBody LoginDto loginDto){
       var message = auth.login(loginDto);
       return ResponseEntity.ok(message);
   }
}
