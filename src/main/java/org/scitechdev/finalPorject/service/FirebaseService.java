package org.scitechdev.finalPorject.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.scitechdev.finalPorject.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;

@Service
public class FirebaseService {
    
    @Autowired
    private PasswordEncoder passwordEncoder;    public String createUser(User user) throws Exception {
        // Validate password length (at least 6 characters for security)
        if (user.getPassword() == null || user.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }
        
        // Skip Firebase Authentication and store directly in Firestore
        System.out.println("Getting Firestore instance...");
        Firestore firestore = FirestoreClient.getFirestore();
        
        // Check if user with this email already exists
        System.out.println("Checking if user already exists with email: " + user.getEmail());
        ApiFuture<QuerySnapshot> future = firestore.collection("users")
                .whereEqualTo("email", user.getEmail())
                .get();
        
        try {
            // Wait for the query to complete
            QuerySnapshot snapshot = future.get();
            if (!snapshot.isEmpty()) {
                throw new IllegalArgumentException("A user with this email already exists");
            }
            
            // Generate a custom userId
            String userId = java.util.UUID.randomUUID().toString();
            
            // Hash the password for security before storing
            String hashedPassword = passwordEncoder.encode(user.getPassword());
            
            Map<String, Object> userData = new HashMap<>();
            userData.put("userId", userId);
            userData.put("username", user.getUsername());
            userData.put("email", user.getEmail());
            userData.put("password", hashedPassword); // Store hashed password
            userData.put("role", user.getRole());
            userData.put("createdAt", java.time.Instant.now().toString());
            
            // Store in "users" collection using the userId as document ID
            System.out.println("Writing user data to Firestore in 'users' collection with document ID: " + userId);
            firestore.collection("users").document(userId).set(userData).get(); // Use .get() to wait for completion
            System.out.println("Successfully wrote user data to Firestore");
            
            return userId;
        } catch (ExecutionException | InterruptedException e) {
            System.err.println("Error creating user in Firestore: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
      public User getUserByEmail(String email) throws ExecutionException, InterruptedException {
        Firestore firestore = FirestoreClient.getFirestore();
        
        // Query Firestore to find user by email
        ApiFuture<QuerySnapshot> future = firestore.collection("users")
                .whereEqualTo("email", email)
                .limit(1)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        if (!querySnapshot.isEmpty()) {
            DocumentSnapshot document = querySnapshot.getDocuments().get(0);
            User user = new User();
            user.setUsername(document.getString("username"));
            user.setEmail(document.getString("email"));
            user.setRole(document.getString("role"));
            // Don't set the password field
            return user;
        } else {
            return null;
        }
    }public boolean authenticateUser(String email, String password) {
        try {
            // Find user in Firestore by email
            System.out.println("Authenticating user with email: " + email);
            Firestore firestore = FirestoreClient.getFirestore();
            ApiFuture<QuerySnapshot> future = firestore.collection("users")
                    .whereEqualTo("email", email)
                    .get();
            
            QuerySnapshot querySnapshot = future.get();
            if (!querySnapshot.isEmpty()) {
                DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                String storedPassword = document.getString("password");
                
                // Check if password matches
                boolean matches = passwordEncoder.matches(password, storedPassword);
                System.out.println("Password verification result: " + matches);
                return matches;
            } else {
                System.out.println("No user found with email: " + email);
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error authenticating user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Test method to verify Firestore connection
     */
    public boolean testFirestoreConnection() {
        try {
            System.out.println("Testing Firestore connection...");
            Firestore firestore = FirestoreClient.getFirestore();
            
            // Try to access a collection
            firestore.collection("test").document("test").get().get();
            System.out.println("Firestore connection test successful");
            return true;
        } catch (Exception e) {
            System.err.println("Firestore connection test failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
