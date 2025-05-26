package org.scitechdev.finalPorject.config;

import java.io.FileInputStream;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

@Configuration
public class FirebaseConfig {
    
    @Value("${firebase.credentials.path}")
    private String firebaseCredentialsPath;
      @Value("${firebase.database.url:https://agriconnect-lite-wbo1k.firebaseio.com}")
    private String firebaseDatabaseUrl;
    
    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        System.out.println("Initializing Firebase with credentials file: " + firebaseCredentialsPath);
        
        try {
            FileInputStream serviceAccount = new FileInputStream(firebaseCredentialsPath);
            
            FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl(firebaseDatabaseUrl)
                .build();
    
            System.out.println("Firebase options created successfully");
            
            if (FirebaseApp.getApps().isEmpty()) {
                System.out.println("Initializing new Firebase app");
                return FirebaseApp.initializeApp(options);
            } else {
                System.out.println("Firebase app already initialized, returning existing instance");
                return FirebaseApp.getInstance();
            }
        } catch (Exception e) {
            System.err.println("Error initializing Firebase: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}