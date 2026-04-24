package com.pca.Backend.Config;

import java.io.FileInputStream;
import java.io.IOException;

import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import jakarta.annotation.PostConstruct;

@Configuration
public class InitializeFirebase {
    @PostConstruct
    public void initializeFirebase() throws IOException{
        FileInputStream serviceAccount =
          new FileInputStream("src/main/resources/test-4b623-firebase-adminsdk-fbsvc-87089ff908.json");
        FirebaseOptions options = new FirebaseOptions.Builder()
        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
        .build();
     if(FirebaseApp.getApps().isEmpty()){
     FirebaseApp.initializeApp(options);
     }
    }
}
