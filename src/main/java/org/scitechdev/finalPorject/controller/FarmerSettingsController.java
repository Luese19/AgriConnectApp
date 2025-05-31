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

import java.util.Map;
import java.util.HashMap;

@Controller
@RequestMapping("/farmer")
public class FarmerSettingsController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private FirebaseService firebaseService;

    @GetMapping("/settings")
    public String showFarmerSettings(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();
        
        // Get user details from Firestore
        Map<String, Object> userDetails = userService.getUserDetails(userId);
        
        if (userDetails != null) {
            User user = mapToUser(userDetails, userId);
            model.addAttribute("user", user);
            
            // Get user settings
            Map<String, Object> settings = getUserSettings(userId);
            model.addAttribute("settings", settings);
        } else {
            User user = new User();
            user.setUsername(userId);
            model.addAttribute("user", user);
            model.addAttribute("settings", getDefaultFarmerSettings());
        }
        
        model.addAttribute("pageTitle", "Farmer Settings");
        return "farmer/settings";
    }

    @PostMapping("/settings/notifications")
    @ResponseBody
    public Map<String, Object> updateNotificationSettings(@RequestBody Map<String, Object> notificationSettings) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean success = updateUserSettings(userId, "notifications", notificationSettings);
            response.put("success", success);
            response.put("message", success ? "Notification settings updated successfully!" : "Failed to update settings");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "An error occurred while updating settings");
        }
        
        return response;
    }

    @PostMapping("/settings/privacy")
    @ResponseBody
    public Map<String, Object> updatePrivacySettings(@RequestBody Map<String, Object> privacySettings) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean success = updateUserSettings(userId, "privacy", privacySettings);
            response.put("success", success);
            response.put("message", success ? "Privacy settings updated successfully!" : "Failed to update settings");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "An error occurred while updating settings");
        }
        
        return response;
    }

    @PostMapping("/settings/business")
    @ResponseBody
    public Map<String, Object> updateBusinessSettings(@RequestBody Map<String, Object> businessSettings) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean success = updateUserSettings(userId, "business", businessSettings);
            response.put("success", success);
            response.put("message", success ? "Business settings updated successfully!" : "Failed to update settings");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "An error occurred while updating settings");
        }
        
        return response;
    }

    @PostMapping("/settings/change-password")
    @ResponseBody
    public Map<String, Object> changePassword(@RequestBody Map<String, String> passwordData) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String currentPassword = passwordData.get("currentPassword");
            String newPassword = passwordData.get("newPassword");
            
            // Validate current password
            boolean isCurrentPasswordValid = userService.validatePassword(userId, currentPassword);
            
            if (!isCurrentPasswordValid) {
                response.put("success", false);
                response.put("message", "Current password is incorrect");
                return response;
            }
            
            // Update password
            boolean success = userService.updatePassword(userId, newPassword);
            response.put("success", success);
            response.put("message", success ? "Password changed successfully!" : "Failed to change password");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "An error occurred while changing password");
        }
        
        return response;
    }

    private User mapToUser(Map<String, Object> userDetails, String userId) {
        User user = new User();
        user.setUsername((String) userDetails.get("username"));
        user.setEmail((String) userDetails.get("email"));
        user.setRole((String) userDetails.get("role"));
        user.setFirstName((String) userDetails.getOrDefault("firstName", ""));
        user.setLastName((String) userDetails.getOrDefault("lastName", ""));
        user.setPhone((String) userDetails.getOrDefault("phone", ""));
        user.setAddress((String) userDetails.getOrDefault("address", ""));
        user.setProfileImage((String) userDetails.getOrDefault("profileImage", ""));
        user.setLocation((String) userDetails.getOrDefault("location", ""));
        
        return user;
    }    private Map<String, Object> getUserSettings(String userId) {
        try {
            Map<String, Object> userDetails = userService.getUserDetails(userId);
            if (userDetails != null && userDetails.containsKey("settings")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> settings = (Map<String, Object>) userDetails.get("settings");
                return settings;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getDefaultFarmerSettings();
    }

    private Map<String, Object> getDefaultFarmerSettings() {
        Map<String, Object> settings = new HashMap<>();
        
        // Notification settings
        Map<String, Object> notifications = new HashMap<>();
        notifications.put("emailNewOrders", true);
        notifications.put("emailMessages", true);
        notifications.put("emailInventoryAlerts", true);
        notifications.put("pushNotifications", true);
        notifications.put("notificationFrequency", "immediate");
        settings.put("notifications", notifications);
        
        // Privacy settings
        Map<String, Object> privacy = new HashMap<>();
        privacy.put("profileVisibility", "public");
        privacy.put("showContactInfo", true);
        privacy.put("allowMessages", true);
        privacy.put("twoFactorAuth", false);
        settings.put("privacy", privacy);
        
        // Business settings specific to farmers
        Map<String, Object> business = new HashMap<>();
        business.put("autoAcceptOrders", false);
        business.put("deliveryRadius", "50");
        business.put("inventoryAlertThreshold", "10");
        business.put("businessHoursStart", "08:00");
        business.put("businessHoursEnd", "18:00");
        business.put("weekendAvailable", false);
        business.put("seasonalProducts", true);
        business.put("organicCertified", false);
        settings.put("business", business);
        
        return settings;
    }    private boolean updateUserSettings(String userId, String settingType, Map<String, Object> settingData) {
        try {
            Map<String, Object> userDetails = userService.getUserDetails(userId);
            if (userDetails == null) {
                return false;
            }
            
            String collection = "farmers";
            
            // Get current settings
            Map<String, Object> currentSettings = getUserSettings(userId);
            currentSettings.put(settingType, settingData);
            
            // Update settings in Firestore
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("settings", currentSettings);
            
            return firebaseService.updateDocument(collection, userId, updateData);
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
