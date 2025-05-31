package org.scitechdev.finalPorject.service;

import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.scitechdev.finalPorject.Entity.Farmer;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class FarmerService {
    private static final String COLLECTION_NAME = "farmers";

    // Create or update farmer profile
    public void saveFarmer(Farmer farmer) {
        Firestore db = FirestoreClient.getFirestore();
        try {
            db.collection(COLLECTION_NAME).document(farmer.getFarmerId()).set(farmer);
            System.out.println("Farmer profile saved successfully for ID: " + farmer.getFarmerId());
        } catch (Exception e) {
            System.err.println("Error saving farmer profile: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to save farmer profile", e);
        }
    }

    // Get farmer by ID
    public Farmer getFarmerById(String farmerId) {
        if (farmerId == null || farmerId.trim().isEmpty()) {
            return null;
        }

        Firestore db = FirestoreClient.getFirestore();
        try {
            DocumentSnapshot document = db.collection(COLLECTION_NAME).document(farmerId.trim()).get().get();
            if (document.exists()) {
                return document.toObject(Farmer.class);
            } else {
                // Return a basic farmer profile if doesn't exist
                return createDefaultFarmerProfile(farmerId);
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error getting farmer by ID: " + farmerId);
            e.printStackTrace();
            return createDefaultFarmerProfile(farmerId);
        }
    }

    // Update farmer profile
    public void updateFarmerProfile(String farmerId, Map<String, Object> updates) {
        if (farmerId == null || farmerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Farmer ID cannot be null or empty");
        }

        Firestore db = FirestoreClient.getFirestore();
        try {
            db.collection(COLLECTION_NAME).document(farmerId.trim()).update(updates);
            System.out.println("Farmer profile updated successfully for ID: " + farmerId);
        } catch (Exception e) {
            System.err.println("Error updating farmer profile: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to update farmer profile", e);
        }
    }

    // Update farmer settings
    public void updateFarmerSettings(String farmerId, boolean emailNotifications, boolean smsNotifications, 
                                   String preferredLanguage, String timezone) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("emailNotifications", emailNotifications);
        updates.put("smsNotifications", smsNotifications);
        updates.put("preferredLanguage", preferredLanguage);
        updates.put("timezone", timezone);
        
        updateFarmerProfile(farmerId, updates);
    }

    // Update farmer basic info
    public void updateBasicInfo(String farmerId, String farmName, String ownerName, String email, 
                              String phone, String address, String city, String state, String zipCode) {
        Map<String, Object> updates = new HashMap<>();
        if (farmName != null && !farmName.trim().isEmpty()) updates.put("farmName", farmName);
        if (ownerName != null && !ownerName.trim().isEmpty()) updates.put("ownerName", ownerName);
        if (email != null && !email.trim().isEmpty()) updates.put("email", email);
        if (phone != null && !phone.trim().isEmpty()) updates.put("phone", phone);
        if (address != null && !address.trim().isEmpty()) updates.put("address", address);
        if (city != null && !city.trim().isEmpty()) updates.put("city", city);
        if (state != null && !state.trim().isEmpty()) updates.put("state", state);
        if (zipCode != null && !zipCode.trim().isEmpty()) updates.put("zipCode", zipCode);
        
        updateFarmerProfile(farmerId, updates);
    }

    // Update farm details
    public void updateFarmDetails(String farmerId, String farmType, String description, double farmSize, 
                                String establishedYear, String certifications) {
        Map<String, Object> updates = new HashMap<>();
        if (farmType != null && !farmType.trim().isEmpty()) updates.put("farmType", farmType);
        if (description != null && !description.trim().isEmpty()) updates.put("description", description);
        if (farmSize > 0) updates.put("farmSize", farmSize);
        if (establishedYear != null && !establishedYear.trim().isEmpty()) updates.put("establishedYear", establishedYear);
        if (certifications != null && !certifications.trim().isEmpty()) updates.put("certifications", certifications);
        
        updateFarmerProfile(farmerId, updates);
    }

    // Check if farmer exists
    public boolean farmerExists(String farmerId) {
        if (farmerId == null || farmerId.trim().isEmpty()) {
            return false;
        }

        Firestore db = FirestoreClient.getFirestore();
        try {
            DocumentSnapshot document = db.collection(COLLECTION_NAME).document(farmerId.trim()).get().get();
            return document.exists();
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error checking if farmer exists: " + farmerId);
            e.printStackTrace();
            return false;
        }
    }

    // Create default farmer profile
    private Farmer createDefaultFarmerProfile(String farmerId) {
        Farmer farmer = new Farmer();
        farmer.setFarmerId(farmerId);
        farmer.setFarmerName(farmerId + "'s Farm"); // Using old field for compatibility
        farmer.setFarmerEmail(farmerId + "@example.com"); // Default email
        farmer.setFarmerStatus("Active");
        farmer.setFarmerType("Organic");
        farmer.setFarmerLocation("Not specified");
        return farmer;
    }

    // Delete farmer profile
    public void deleteFarmer(String farmerId) {
        if (farmerId == null || farmerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Farmer ID cannot be null or empty");
        }

        Firestore db = FirestoreClient.getFirestore();
        try {
            db.collection(COLLECTION_NAME).document(farmerId.trim()).delete();
            System.out.println("Farmer profile deleted successfully for ID: " + farmerId);
        } catch (Exception e) {
            System.err.println("Error deleting farmer profile: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete farmer profile", e);
        }
    }
}
