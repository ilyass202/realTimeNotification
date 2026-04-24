package com.pca.Backend.Service;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.pca.Backend.DTO.LoginDto;
import com.pca.Backend.DTO.LoginResponse;
import com.pca.Backend.DTO.SignUp;
import com.pca.Backend.Entity.UserEntity;
import com.pca.Backend.Repo.UserRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceAuth {
    private final CustomUserDetails customUserDetails;
    private final AuthenticationManager authMana;
    private final UserRepo userRepo;
    public UserEntity signUp(SignUp signUp) {
        return customUserDetails.signUp(signUp);
    }
    public LoginResponse login(LoginDto loginDto) {
      Authentication auth =authMana.authenticate(new UsernamePasswordAuthenticationToken(loginDto.email(), loginDto.password()));
      SecurityContextHolder.getContext().setAuthentication(auth);
      String email = auth.getName();
      UserEntity user = userRepo.findByEmail(email).orElseGet(null);
      return new LoginResponse(user.getId(), user.getFcmToken());
}
}
