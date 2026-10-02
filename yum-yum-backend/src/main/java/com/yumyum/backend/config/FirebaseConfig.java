package com.yumyum.backend.config;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;

@Configuration 
public class FirebaseConfig {
    
    @Bean (destroyMethod = "delete")
    public FirebaseApp firebaseApp() throws IOException{
        FirebaseOptions options = FirebaseOptions.builder()
                                    .setCredentials(GoogleCredentials.getApplicationDefault())
                                    .build();
        return FirebaseApp.initializeApp(options);
    }

    @Bean 
    public FirebaseAuth firebaseAuth(FirebaseApp firebaseApp){
        return FirebaseAuth.getInstance(firebaseApp);
    }
}
