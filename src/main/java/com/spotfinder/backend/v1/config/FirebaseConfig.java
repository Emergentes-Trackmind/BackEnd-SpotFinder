package com.spotfinder.backend.v1.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.util.Base64;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initializeFirebase() {
        try {
            String firebaseConfigBase64 = System.getenv("FIREBASE_ADMIN_CONFIG");

            if (firebaseConfigBase64 == null || firebaseConfigBase64.isEmpty()) {
                throw new IllegalStateException("Variable de entorno FIREBASE_ADMIN_CONFIG no está definida.");
            }

            byte[] decodedBytes = Base64.getDecoder().decode(firebaseConfigBase64);
            ByteArrayInputStream serviceAccount = new ByteArrayInputStream(decodedBytes);

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase inicializado correctamente desde variable de entorno.");
            }

        } catch (Exception e) {
            System.err.println("Error al inicializar Firebase desde variable de entorno:");
            e.printStackTrace();
        }
    }
}
