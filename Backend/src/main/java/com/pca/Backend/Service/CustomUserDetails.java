package com.pca.Backend.Service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.User;
import java.util.ArrayList;

import com.pca.Backend.DTO.SignUp;
import com.pca.Backend.Entity.ReferentielUser;
import com.pca.Backend.Repo.UserRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetailsService{
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        ReferentielUser user = userRepo.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
       return new User(user.getEmail(), user.getPassword(), new ArrayList<>());
    }
    public ReferentielUser signUp(SignUp signUp) {
        userRepo.findByEmail(signUp.email()).ifPresent(existing -> {
            throw new RuntimeException("User already exists");
        });

        ReferentielUser newUser = new ReferentielUser();
        newUser.setName(signUp.name());
        newUser.setEmail(signUp.email());
        newUser.setPassword(passwordEncoder.encode(signUp.password()));
        return userRepo.save(newUser);
    }
}
