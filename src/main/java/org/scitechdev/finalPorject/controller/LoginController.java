package org.scitechdev.finalPorject.controller;

import org.scitechdev.finalPorject.model.User;
import org.scitechdev.finalPorject.service.FirebaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.firebase.FirebaseApp;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class LoginController {

    @Autowired
    private FirebaseService firebaseService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String login() {
        return "login"; // This refers to a login.html template in your templates directory
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }
    
    @GetMapping("/home")
    public String home(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isFarmer = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_FARMER"));
        boolean isBuyer = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_BUYER"));
        if (isBuyer) {
            return "redirect:/buyer/";
        } else if (isFarmer) {
            model.addAttribute("newItem", new org.scitechdev.finalPorject.Entity.InventoryItem());
            model.addAttribute("inventoryItems", new java.util.ArrayList<>()); // or fetch actual items if possible
            return "Farmer_Dashboard"; // This refers to Farmer_Dashboard.html template
        } else {
            return "redirect:/login";
        }
    }
    
    @PostMapping("/signup")
    public String signup(@RequestParam String username, 
                         @RequestParam String email,
                         @RequestParam String password,
                         @RequestParam String role,
                         RedirectAttributes redirectAttributes) {
        // Validate password length (Firebase requires at least 6 characters)
        if (password == null || password.length() < 6) {
            redirectAttributes.addFlashAttribute("signupError", "Password must be at least 6 characters long.");
            return "redirect:/login";
        }
        try {
            System.out.println("Processing signup request for user: " + username + ", email: " + email + ", role: " + role);
            // Use the role from the form
            User newUser = new User(username, email, password);
            newUser.setRole(role);
            String userId = firebaseService.createUser(newUser);
            System.out.println("User created with ID: " + userId);
            redirectAttributes.addFlashAttribute("signupSuccess", "Account created successfully! Please login with your credentials.");
            System.out.println("Signup process completed successfully");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            System.err.println("Validation error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("signupError", e.getMessage());
            return "redirect:/login";
        } catch (Exception e) {
            System.err.println("Unexpected error during signup: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("signupError", "An unexpected error occurred: " + e.getMessage());
            return "redirect:/login";
        }    }    @PostMapping("/custom-login")
    public String processLogin(@RequestParam String email, 
                               @RequestParam String password,
                               RedirectAttributes redirectAttributes,
                               HttpServletRequest request) {
        System.out.println("Login attempt received for email: " + email);
        try {
            boolean isAuthenticated = firebaseService.authenticateUser(email, password);
            System.out.println("Authentication result: " + isAuthenticated);
            
            if (isAuthenticated) {
                // Fetch user from Firestore to get the actual role
                org.scitechdev.finalPorject.model.User user = firebaseService.getUserByEmail(email);
                if (user == null) {
                    redirectAttributes.addFlashAttribute("loginError", "User not found.");
                    return "redirect:/login";
                }
                String role = user.getRole();
                String springRole = "ROLE_" + (role != null ? role.toUpperCase() : "FARMER");
                System.out.println("Setting role to: " + springRole);
                
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        email, null, AuthorityUtils.createAuthorityList(springRole));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                // Explicitly set authentication in session
                request.getSession().setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
                
                System.out.println("User authenticated successfully with role: " + springRole);
                System.out.println("Security context authentication: " + SecurityContextHolder.getContext().getAuthentication());
                
                // Redirect based on role
                if (springRole.equals("ROLE_BUYER")) {
                    System.out.println("Redirecting to buyer dashboard");
                    return "redirect:/buyer/";
                } else if (springRole.equals("ROLE_FARMER")) {
                    System.out.println("Redirecting to farmer dashboard");
                    return "redirect:/farmer/dashboard";
                } else {
                    return "redirect:/login";
                }
            } else {
                System.err.println("Authentication failed: Invalid credentials");
                redirectAttributes.addFlashAttribute("loginError", "Invalid email or password");
                return "redirect:/login";
            }
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("loginError", "An error occurred during login: " + e.getMessage());
            return "redirect:/login";
        }
    }
    
    @GetMapping("/test-firebase")
    public String testFirebase(Model model) {
        System.out.println("Testing Firebase connectivity...");
        
        try {
            FirebaseApp firebaseApp = FirebaseApp.getInstance();
            boolean firestoreConnection = firebaseService.testFirestoreConnection();
            
            model.addAttribute("firebaseInitialized", firebaseApp != null);
            model.addAttribute("firestoreConnected", firestoreConnection);
            
            return "firebase-test";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            e.printStackTrace();
            return "firebase-test";
        }
    }

    // Debug endpoint to check authentication status
    @GetMapping("/auth-debug")
    public String authDebug(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Current authentication: " + auth);
        System.out.println("Name: " + auth.getName());
        System.out.println("Authorities: " + auth.getAuthorities());
        System.out.println("Principal: " + auth.getPrincipal());
        System.out.println("Details: " + auth.getDetails());
        
        model.addAttribute("username", auth.getName());
        model.addAttribute("authorities", auth.getAuthorities());
        model.addAttribute("isAuthenticated", auth.isAuthenticated());
        
        return "auth-debug";
    }
}
