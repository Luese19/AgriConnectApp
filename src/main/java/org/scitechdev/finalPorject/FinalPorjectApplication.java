package org.scitechdev.finalPorject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.google.firebase.FirebaseApp;

@SpringBootApplication
public class FinalPorjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinalPorjectApplication.class, args);

		FirebaseApp firebaseApp = FirebaseApp.getInstance();
		if (firebaseApp == null) {
			System.out.println("Firebase App is not initialized.");
		} else {
			System.out.println("Firebase App is initialized.");
		}
		
	}

}
