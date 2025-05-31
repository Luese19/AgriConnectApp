package org.scitechdev.finalPorject.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class UserService {
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    public Map<String, Object> getUserDetails(String userId) {
        try {
            Firestore db = FirestoreClient.getFirestore();
            
            // Try to find in users collection first
            DocumentSnapshot userDoc = db.collection("users").document(userId).get().get();
            if (userDoc.exists()) {
                Map<String, Object> userData = userDoc.getData();
                if (userData != null) {
                    return userData;
                }
            }

            // If not found in users, try farmers collection
            ApiFuture<QuerySnapshot> farmerFuture = db.collection("farmers")
                    .whereEqualTo("farmerId", userId)
                    .get();
            
            QuerySnapshot farmerSnapshot = farmerFuture.get();
            if (!farmerSnapshot.isEmpty()) {
                Map<String, Object> farmerData = farmerSnapshot.getDocuments().get(0).getData();
                if (farmerData != null) {
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("userId", userId);
                    userData.put("username", farmerData.get("farmerName"));
                    userData.put("email", farmerData.get("farmerEmail"));
                    userData.put("role", "FARMER");
                    userData.put("image", farmerData.get("farmerImage"));
                    userData.put("location", farmerData.get("farmerLocation"));
                    return userData;
                }
            }

            // If not found in farmers, try buyers/suppliers collection
            ApiFuture<QuerySnapshot> buyerFuture = db.collection("suppliers")
                    .whereEqualTo("supplierId", userId)
                    .get();
            
            QuerySnapshot buyerSnapshot = buyerFuture.get();
            if (!buyerSnapshot.isEmpty()) {
                Map<String, Object> buyerData = buyerSnapshot.getDocuments().get(0).getData();
                if (buyerData != null) {
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("userId", userId);
                    userData.put("username", buyerData.get("supplierName"));
                    userData.put("email", buyerData.get("supplierEmail"));
                    userData.put("role", "BUYER");
                    userData.put("image", buyerData.get("supplierImage"));
                    userData.put("location", buyerData.get("supplierLocation"));
                    return userData;
                }
            }

            return null;

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Map<String, Object>> searchUsers(String searchTerm, String userRole, String currentUserId) {
        try {
            Firestore db = FirestoreClient.getFirestore();
            List<Map<String, Object>> users = new ArrayList<>();

            if (userRole.equals("FARMER")) {
                // Search for buyers/suppliers
                ApiFuture<QuerySnapshot> future = db.collection("suppliers").get();
                QuerySnapshot snapshot = future.get();
                
                for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
                    Map<String, Object> data = doc.getData();
                    String supplierName = (String) data.get("supplierName");
                    String supplierId = (String) data.get("supplierId");
                    
                    if (supplierName != null && !supplierId.equals(currentUserId) &&
                        supplierName.toLowerCase().contains(searchTerm.toLowerCase())) {
                        
                        Map<String, Object> user = new HashMap<>();
                        user.put("userId", supplierId);
                        user.put("username", supplierName);
                        user.put("email", data.get("supplierEmail"));
                        user.put("role", "BUYER");
                        user.put("image", data.get("supplierImage"));
                        user.put("location", data.get("supplierLocation"));
                        users.add(user);
                    }
                }
            } else {
                // Search for farmers
                ApiFuture<QuerySnapshot> future = db.collection("farmers").get();
                QuerySnapshot snapshot = future.get();
                
                for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
                    Map<String, Object> data = doc.getData();
                    String farmerName = (String) data.get("farmerName");
                    String farmerId = (String) data.get("farmerId");
                    
                    if (farmerName != null && !farmerId.equals(currentUserId) &&
                        farmerName.toLowerCase().contains(searchTerm.toLowerCase())) {
                        
                        Map<String, Object> user = new HashMap<>();
                        user.put("userId", farmerId);
                        user.put("username", farmerName);
                        user.put("email", data.get("farmerEmail"));
                        user.put("role", "FARMER");
                        user.put("image", data.get("farmerImage"));
                        user.put("location", data.get("farmerLocation"));
                        users.add(user);
                    }
                }
            }

            return users;

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public String getUserRole(String userId) {
        Map<String, Object> userData = getUserDetails(userId);
        if (userData != null) {
            return (String) userData.get("role");
        }
        return null;
    }

    public String getUserName(String userId) {
        Map<String, Object> userData = getUserDetails(userId);
        if (userData != null) {
            return (String) userData.get("username");
        }
        return "Unknown User";
    }

    public boolean validatePassword(String userId, String currentPassword) {
        try {
            Firestore db = FirestoreClient.getFirestore();
            
            // Get user's stored password hash
            DocumentSnapshot userDoc = db.collection("users").document(userId).get().get();
            if (userDoc.exists()) {
                Map<String, Object> userData = userDoc.getData();
                if (userData != null) {
                    String storedPasswordHash = (String) userData.get("password");
                    return passwordEncoder.matches(currentPassword, storedPasswordHash);
                }
            }
            
            // Try farmers collection
            ApiFuture<QuerySnapshot> farmerFuture = db.collection("farmers")
                    .whereEqualTo("farmerId", userId)
                    .get();
            
            QuerySnapshot farmerSnapshot = farmerFuture.get();
            if (!farmerSnapshot.isEmpty()) {
                Map<String, Object> farmerData = farmerSnapshot.getDocuments().get(0).getData();
                if (farmerData != null) {
                    String storedPasswordHash = (String) farmerData.get("farmerPassword");
                    return passwordEncoder.matches(currentPassword, storedPasswordHash);
                }
            }
            
            // Try suppliers collection
            ApiFuture<QuerySnapshot> buyerFuture = db.collection("suppliers")
                    .whereEqualTo("supplierId", userId)
                    .get();
            
            QuerySnapshot buyerSnapshot = buyerFuture.get();
            if (!buyerSnapshot.isEmpty()) {
                Map<String, Object> buyerData = buyerSnapshot.getDocuments().get(0).getData();
                if (buyerData != null) {
                    String storedPasswordHash = (String) buyerData.get("supplierPassword");
                    return passwordEncoder.matches(currentPassword, storedPasswordHash);
                }
            }
            
            return false;
            
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean updatePassword(String userId, String newPassword) {
        try {
            Firestore db = FirestoreClient.getFirestore();
            String hashedPassword = passwordEncoder.encode(newPassword);
            
            // Try users collection first
            DocumentSnapshot userDoc = db.collection("users").document(userId).get().get();
            if (userDoc.exists()) {
                Map<String, Object> updateData = new HashMap<>();
                updateData.put("password", hashedPassword);
                db.collection("users").document(userId).update(updateData).get();
                return true;
            }
            
            // Try farmers collection
            ApiFuture<QuerySnapshot> farmerFuture = db.collection("farmers")
                    .whereEqualTo("farmerId", userId)
                    .get();
            
            QuerySnapshot farmerSnapshot = farmerFuture.get();
            if (!farmerSnapshot.isEmpty()) {
                String docId = farmerSnapshot.getDocuments().get(0).getId();
                Map<String, Object> updateData = new HashMap<>();
                updateData.put("farmerPassword", hashedPassword);
                db.collection("farmers").document(docId).update(updateData).get();
                return true;
            }
            
            // Try suppliers collection
            ApiFuture<QuerySnapshot> buyerFuture = db.collection("suppliers")
                    .whereEqualTo("supplierId", userId)
                    .get();
            
            QuerySnapshot buyerSnapshot = buyerFuture.get();
            if (!buyerSnapshot.isEmpty()) {
                String docId = buyerSnapshot.getDocuments().get(0).getId();
                Map<String, Object> updateData = new HashMap<>();
                updateData.put("supplierPassword", hashedPassword);
                db.collection("suppliers").document(docId).update(updateData).get();
                return true;
            }
            
            return false;
            
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }
}
