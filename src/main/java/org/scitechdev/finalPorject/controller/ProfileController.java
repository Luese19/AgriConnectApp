package org.scitechdev.finalPorject.controller;

import org.scitechdev.finalPorject.model.User;
import org.scitechdev.finalPorject.service.UserService;
import org.scitechdev.finalPorject.service.FirebaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/buyer")
public class ProfileController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private FirebaseService firebaseService;

    @GetMapping("/profile")
    public String showProfile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();
        
        // Get user details from Firestore
        Map<String, Object> userDetails = userService.getUserDetails(userId);
        
        if (userDetails != null) {
            // Create User object from Firestore data
            User user = mapToUser(userDetails, userId);
            model.addAttribute("user", user);
        } else {
            // Create empty user if not found
            User user = new User();
            user.setUsername(userId);
            model.addAttribute("user", user);
        }
        
        model.addAttribute("pageTitle", "Profile");
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();
        
        try {
            // Update user profile in Firestore
            boolean success = updateUserProfile(userId, user);
            
            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to update profile. Please try again.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while updating profile.");
        }
        
        return "redirect:/buyer/profile";
    }

    private User mapToUser(Map<String, Object> userDetails, String userId) {
        User user = new User();
        user.setUsername((String) userDetails.get("username"));
        user.setEmail((String) userDetails.get("email"));
        user.setRole((String) userDetails.get("role"));
        
        // Handle additional fields with null checks
        user.setFirstName((String) userDetails.getOrDefault("firstName", ""));
        user.setLastName((String) userDetails.getOrDefault("lastName", ""));
        user.setPhone((String) userDetails.getOrDefault("phone", ""));
        user.setAddress((String) userDetails.getOrDefault("address", ""));
        user.setProfileImage((String) userDetails.getOrDefault("profileImage", ""));
        user.setLocation((String) userDetails.getOrDefault("location", ""));
        
        return user;
    }

    private boolean updateUserProfile(String userId, User user) {
        try {
            // First, get current user details to determine which collection to update
            Map<String, Object> currentDetails = userService.getUserDetails(userId);
            
            if (currentDetails == null) {
                return false;
            }
            
            String role = (String) currentDetails.get("role");
            String collection = determineCollection(role);
            String documentId = determineDocumentId(userId, role, currentDetails);
            
            // Prepare update data
            Map<String, Object> updateData = prepareUpdateData(user, role);
            
            // Update in Firestore
            return firebaseService.updateDocument(collection, documentId, updateData);
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String determineCollection(String role) {
        switch (role.toUpperCase()) {
            case "FARMER":
                return "farmers";
            case "BUYER":
                return "suppliers";
            default:
                return "users";
        }
    }    private String determineDocumentId(String userId, String role, Map<String, Object> currentDetails) {
        switch (role.toUpperCase()) {
            case "FARMER":
                // For farmers, we need to find the actual document ID from the farmers collection
                return findDocumentIdInCollection("farmers", "farmerId", userId);
            case "BUYER":
                // For buyers, we need to find the actual document ID from the suppliers collection
                return findDocumentIdInCollection("suppliers", "supplierId", userId);
            default:
                return userId;
        }
    }

    private String findDocumentIdInCollection(String collection, String field, String value) {
        try {
            Map<String, Object> userDetails = userService.getUserDetails(value);
            if (userDetails != null) {
                // For now, use the userId as the document ID
                // This might need adjustment based on your Firestore structure
                return value;
            }
            return value;
        } catch (Exception e) {
            e.printStackTrace();
            return value;
        }
    }

    private Map<String, Object> prepareUpdateData(User user, String role) {
        Map<String, Object> updateData = new java.util.HashMap<>();
        
        // Common fields
        updateData.put("firstName", user.getFirstName());
        updateData.put("lastName", user.getLastName());
        updateData.put("phone", user.getPhone());
        updateData.put("address", user.getAddress());
        updateData.put("profileImage", user.getProfileImage());
        updateData.put("location", user.getLocation());
        
        // Role-specific field mapping
        switch (role.toUpperCase()) {
            case "FARMER":
                updateData.put("farmerName", user.getUsername());
                updateData.put("farmerEmail", user.getEmail());
                updateData.put("farmerLocation", user.getLocation());
                updateData.put("farmerImage", user.getProfileImage());
                break;
            case "BUYER":
                updateData.put("supplierName", user.getUsername());
                updateData.put("supplierEmail", user.getEmail());
                updateData.put("supplierLocation", user.getLocation());
                updateData.put("supplierImage", user.getProfileImage());
                break;
            default:
                updateData.put("username", user.getUsername());
                updateData.put("email", user.getEmail());
                break;
        }
        
        return updateData;
    }
}
