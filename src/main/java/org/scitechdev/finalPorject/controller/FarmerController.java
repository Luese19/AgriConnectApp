package org.scitechdev.finalPorject.controller;

import org.scitechdev.finalPorject.Entity.InventoryItem;
import org.scitechdev.finalPorject.Entity.Order;
import org.scitechdev.finalPorject.Entity.Farmer;
import org.scitechdev.finalPorject.service.FarmerOrderService;
import org.scitechdev.finalPorject.service.FarmerService;
import org.scitechdev.finalPorject.service.InventoryItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/farmer")
public class FarmerController {
    
    private static final Logger logger = LoggerFactory.getLogger(FarmerController.class);
    
    @Autowired
    private InventoryItemService inventoryItemService;

    @Autowired
    private FarmerOrderService farmerOrderService;

    @Autowired
    private FarmerService farmerService;

    // Add UserService if you don't have it
    @Autowired
    private org.scitechdev.finalPorject.service.UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String farmerId = auth.getName();
        var items = inventoryItemService.getItemsByFarmerId(farmerId);
        model.addAttribute("inventoryItems", items);
        model.addAttribute("totalInventory", items.size());
        model.addAttribute("userRole", "FARMER"); // Add user role for header conditional logic
          // Get orders by status for this farmer
        var pendingOrders = farmerOrderService.getOrdersByFarmerIdAndStatus(farmerId, "Pending");
        var acceptedOrders = farmerOrderService.getOrdersByFarmerIdAndStatus(farmerId, "Processing"); // Processing means accepted
        var deliveredOrders = farmerOrderService.getOrdersByFarmerIdAndStatus(farmerId, "Delivered");
        
        // Get order statistics
        int pendingCount = pendingOrders.size();
        int processingCount = acceptedOrders.size();
        int deliveredCount = deliveredOrders.size();
        
        model.addAttribute("pendingOrdersCount", pendingCount);
        model.addAttribute("processingOrdersCount", processingCount);
        model.addAttribute("deliveredOrdersCount", deliveredCount);
        
        // DEBUG: Log orders fetched for this farmer
        System.out.println("Farmer ID: " + farmerId);
        System.out.println("Pending orders: " + pendingCount + ", Accepted orders: " + processingCount + ", Delivered orders: " + deliveredCount);
        
        // Populate itemNames and quantities for pending orders
        populateOrderItemNames(pendingOrders);
        // Populate itemNames and quantities for accepted orders
        populateOrderItemNames(acceptedOrders);
        // Populate itemNames and quantities for delivered orders
        populateOrderItemNames(deliveredOrders);
        
        // Add to model
        model.addAttribute("pendingOrderRequests", pendingOrders);
        model.addAttribute("acceptedOrderRequests", acceptedOrders);
        model.addAttribute("deliveredOrderRequests", deliveredOrders);
          // Legacy support - add all orders combined for existing functionality
        var allOrders = new ArrayList<Order>();
        allOrders.addAll(pendingOrders);
        allOrders.addAll(acceptedOrders);
        allOrders.addAll(deliveredOrders);
        model.addAttribute("orderRequests", allOrders);
        model.addAttribute("pendingOrders", allOrders.size());
        
        // Add new item for the form
        model.addAttribute("newItem", new InventoryItem());
        
        return "Farmer_Dashboard";
    }

    @PostMapping("/add-crop")
    public String addCrop(@ModelAttribute("newItem") InventoryItem item,
                         @RequestParam(value = "cropImage", required = false) MultipartFile cropImage,
                         Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String farmerId = auth.getName();
            item.setFarmerId(farmerId);
            // Location is set by form binding: item.setLocation(item.getLocation());            // Handle image upload if a file is provided
            if (cropImage != null && !cropImage.isEmpty()) {
                // Save the file to a local folder (e.g., src/main/resources/static/img/)
                String uploadDir = "src/main/resources/static/img/";
                File uploadFolder = new File(uploadDir);
                if (!uploadFolder.exists()) {
                    uploadFolder.mkdirs();
                }
                String fileName = System.currentTimeMillis() + "_" + cropImage.getOriginalFilename();
                File dest = new File(uploadDir + fileName);
                cropImage.transferTo(dest);
                // Set the image path relative to static for web access
                item.setItemImage("/img/" + fileName);
            }
            inventoryItemService.saveItem(item);
        } catch (IOException e) {
            model.addAttribute("error", "Failed to upload image: " + e.getMessage());
            model.addAttribute("inventoryItems", inventoryItemService.getAllItems());
            return "Farmer_Dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to add crop: " + e.getMessage());
            model.addAttribute("inventoryItems", inventoryItemService.getAllItems());
            return "Farmer_Dashboard";
        }
        return "redirect:/farmer/dashboard";
    }    @PostMapping("/delete-crop")
    public String deleteCrop(@RequestParam("itemId") String itemId) {
        inventoryItemService.deleteItem(itemId);
        return "redirect:/farmer/dashboard";
    }

    @GetMapping("/edit-crop/{itemId}")
    public String editCrop(@PathVariable String itemId, Model model) {
        InventoryItem item = inventoryItemService.getItemById(itemId);
        model.addAttribute("editItem", item);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String farmerId = auth.getName();
        var items = inventoryItemService.getItemsByFarmerId(farmerId);
        model.addAttribute("inventoryItems", items);
        return "inventory";
    }

    @PostMapping("/update-crop")
    public String updateCrop(@ModelAttribute("editItem") InventoryItem item, Model model) {
        System.out.println("[DEBUG] update-crop called for itemId: " + item.getItemId());
        System.out.println("[DEBUG] Name: " + item.getItemName() + ", Price: " + item.getItemPrice() + ", Quantity: " + item.getItemQuantity());
        inventoryItemService.saveItem(item);
        // Set the editItem in the model so the modal will open after redirect
        model.addAttribute("editItem", item);
        return "redirect:/farmer/inventory?editItemId=" + item.getItemId();
    }    @PostMapping("/order/accept")
    public String acceptOrder(@RequestParam("orderId") String orderId,
                             @RequestParam(value = "estimatedDeliveryTime", required = false) String estimatedDeliveryTime,
                             @RequestParam(value = "farmerNotes", required = false) String farmerNotes,
                             HttpServletRequest request) {
        try {
            // Debug logging
            System.out.println("=== ACCEPT ORDER DEBUG ===");
            System.out.println("Received orderId parameter: '" + orderId + "'");
            System.out.println("EstimatedDeliveryTime: '" + estimatedDeliveryTime + "'");
            System.out.println("FarmerNotes: '" + farmerNotes + "'");
            
            // Log all request parameters
            System.out.println("All request parameters:");
            request.getParameterMap().forEach((key, values) -> {
                System.out.println("  " + key + " = " + Arrays.toString(values));
            });
            System.out.println("=========================");
            
            // Validate orderId
            if (orderId == null || orderId.trim().isEmpty()) {
                System.err.println("Accept order failed: Order ID is null or empty");
                System.err.println("OrderId value: '" + orderId + "'");
                return "redirect:/farmer/dashboard?error=invalid_order_id";
            }
            
            System.out.println("Calling farmerOrderService.acceptOrder with orderId: " + orderId);
            farmerOrderService.acceptOrder(orderId, estimatedDeliveryTime, farmerNotes);
            System.out.println("Order accepted successfully: " + orderId);
            return "redirect:/farmer/dashboard?success=order_accepted";
        } catch (Exception e) {
            System.err.println("Error accepting order: " + e.getMessage());
            System.err.println("OrderId was: '" + orderId + "'");
            e.printStackTrace();
            return "redirect:/farmer/dashboard?error=accept_failed";
        }
    }@PostMapping("/order/reject")
    public String rejectOrder(@RequestParam("orderId") String orderId,
                             @RequestParam(value = "rejectionReason", required = false) String rejectionReason) {
        try {
            // Validate orderId
            if (orderId == null || orderId.trim().isEmpty()) {
                System.err.println("Reject order failed: Order ID is null or empty");
                return "redirect:/farmer/dashboard?error=invalid_order_id";
            }
            
            farmerOrderService.rejectOrder(orderId, rejectionReason);
            return "redirect:/farmer/dashboard?success=order_rejected";
        } catch (Exception e) {
            System.err.println("Error rejecting order: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/farmer/dashboard?error=reject_failed";
        }
    }    @PostMapping("/order/deliver")
    public String markOrderAsDelivered(@RequestParam("orderId") String orderId,
        @RequestParam(value = "deliveryNotes", required = false) String deliveryNotes) {
        try {
            // Validate orderId
            if (orderId == null || orderId.trim().isEmpty()) {
                System.err.println("Mark delivered failed: Order ID is null or empty");
                return "redirect:/farmer/dashboard?error=invalid_order_id";
            }
            
            farmerOrderService.markAsDelivered(orderId, deliveryNotes);
            return "redirect:/farmer/dashboard?success=order_delivered";
        } catch (Exception e) {
            System.err.println("Error marking order as delivered: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/farmer/dashboard?error=deliver_failed";
        }
    }    // REST endpoint to fetch order details for the modal    
    @GetMapping("/api/order/{orderId}")
    @ResponseBody
    public ResponseEntity<?> getOrderDetails(@PathVariable String orderId, HttpSession session) {
        try {
            // Try session-based authentication first
            String farmerId = (String) session.getAttribute("userId");
            
            // If session auth fails, try Spring Security authentication as fallback
            if (farmerId == null) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.isAuthenticated()) {
                    farmerId = auth.getName();
                }
            }
            
            if (farmerId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not authenticated"));
            }

            // Find the order by ID
            Order order = farmerOrderService.getOrderById(orderId);
            
            if (order == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Order not found"));
            }

            // Verify this order belongs to the current farmer by checking if any of the products belong to this farmer
            boolean belongsToFarmer = false;
            if (order.getProductIds() != null && !order.getProductIds().isEmpty()) {
                for (String productId : order.getProductIds()) {
                    InventoryItem item = inventoryItemService.getItemById(productId);
                    if (item != null && farmerId.equals(item.getFarmerId())) {
                        belongsToFarmer = true;
                        break;
                    }
                }
            }
            
            if (!belongsToFarmer) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied"));
            }

            // Populate item names if not already done
            if (order.getItemNames() == null || order.getItemNames().isEmpty()) {
                List<String> itemNames = new ArrayList<>();
                if (order.getProductIds() != null) {
                    for (String productId : order.getProductIds()) {
                        InventoryItem item = inventoryItemService.getItemById(productId);
                        itemNames.add(item != null ? item.getItemName() : "Unknown Product");
                    }
                }
                order.setItemNames(itemNames);
            }

            // Get customer information
            String customerName = order.getBuyerId();
            String customerContact = "Contact not available";
            String customerEmail = "Email not available";
            
            try {
                // Try to get customer details from UserService
                if (userService != null && order.getBuyerId() != null) {
                    Map<String, Object> customerData = userService.getUserDetails(order.getBuyerId());
                    if (customerData != null) {
                        customerName = (String) customerData.getOrDefault("username", order.getBuyerId());
                        customerEmail = (String) customerData.getOrDefault("email", "Email not available");
                        customerContact = (String) customerData.getOrDefault("phone", "Contact not available");
                    }
                }
            } catch (Exception e) {
                logger.warn("Could not fetch customer details for buyer: {}", order.getBuyerId(), e);
            }

            // Ensure delivery address is available
            String deliveryAddress = order.getDeliveryAddress();
            if (deliveryAddress == null || deliveryAddress.trim().isEmpty()) {
                deliveryAddress = "Delivery address not provided";
            }

            // Create detailed response with all order information
            Map<String, Object> orderDetails = new HashMap<>();
            orderDetails.put("orderId", order.getOrderId());
            orderDetails.put("buyerId", order.getBuyerId());
            orderDetails.put("customerName", customerName);
            orderDetails.put("customerContact", customerContact);
            orderDetails.put("customerEmail", customerEmail);
            orderDetails.put("deliveryAddress", deliveryAddress);
            orderDetails.put("orderDate", order.getOrderDate());
            orderDetails.put("status", order.getStatus());
            orderDetails.put("totalAmount", order.getTotalAmount());
            orderDetails.put("itemNames", order.getItemNames());
            orderDetails.put("quantities", order.getQuantities());
            orderDetails.put("deliveryOption", "Standard Delivery"); // Default or fetch from order
            orderDetails.put("paymentMethod", "Cash on Delivery"); // Default or fetch from order
            orderDetails.put("orderPriority", order.getOrderPriority() != null ? order.getOrderPriority() : "normal");
            orderDetails.put("farmerNotes", order.getFarmerNotes());
            orderDetails.put("buyerNotes", order.getBuyerNotes());
            orderDetails.put("estimatedDeliveryTime", order.getEstimatedDeliveryTime());
            
            // Add timeline dates if available
            if ("accepted".equalsIgnoreCase(order.getStatus()) || "processing".equalsIgnoreCase(order.getStatus())) {
                orderDetails.put("acceptedDate", order.getOrderDate()); // You might have a separate accepted date
            }
            if ("delivered".equalsIgnoreCase(order.getStatus())) {
                orderDetails.put("deliveredDate", order.getOrderDate()); // You might have a separate delivered date
            }

            return ResponseEntity.ok(orderDetails);
            
        } catch (Exception e) {
            logger.error("Error fetching order details for orderId: {}", orderId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch order details: " + e.getMessage()));
        }
    }

    @GetMapping("/inventory")
    public String inventoryPage(@RequestParam(value = "editItemId", required = false) String editItemId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String farmerId = auth.getName();
        var items = inventoryItemService.getItemsByFarmerId(farmerId);
        model.addAttribute("inventoryItems", items);
        model.addAttribute("userRole", "FARMER"); // Add user role for header conditional logic
        // If editItemId is present, fetch and set editItem
        if (editItemId != null && !editItemId.isBlank()) {
            InventoryItem editItem = inventoryItemService.getItemById(editItemId);
            model.addAttribute("editItem", editItem);
        } else if (!model.containsAttribute("editItem")) {
            model.addAttribute("editItem", new InventoryItem());
        }
        model.addAttribute("newItem", new InventoryItem());
        return "/inventory";
    }

    @ModelAttribute("newItem")
    public InventoryItem newItem() {
        return new InventoryItem();
    }

    @GetMapping("/profile")
    public String farmerProfile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String farmerId = auth.getName();
        
        // Get farmer profile data
        Farmer farmer = farmerService.getFarmerById(farmerId);
        model.addAttribute("farmer", farmer);
        model.addAttribute("farmerId", farmerId);
        model.addAttribute("userRole", "FARMER");
        model.addAttribute("pageTitle", "Farmer Profile");
        
        // Add contact number separately if it exists in phone field
        if (farmer != null && farmer.getFarmerPhone() != null) {
            model.addAttribute("contactNumber", farmer.getFarmerPhone());
        }
        
        // Add custom styling attributes for smaller header
        model.addAttribute("headerSize", "compact");
        model.addAttribute("customCSS", 
            "header { height: 60px !important; padding: 10px 0 !important; } " +
            ".navbar { min-height: 50px !important; } " +
            ".navbar-brand { font-size: 18px !important; padding: 10px 15px !important; } " +
            ".navbar-nav > li > a { padding: 15px 10px !important; font-size: 14px !important; } " +
            ".navbar-header { height: 50px !important; } " +
            ".container-fluid { padding: 0 15px !important; } " +
            "body { padding-top: 70px !important; }"
        );
        
        return "farmer/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam("farmName") String farmName,
            @RequestParam("ownerName") String ownerName,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam("address") String address,
            @RequestParam("city") String city,
            @RequestParam("state") String state,
            @RequestParam("zipCode") String zipCode,
            @RequestParam("farmType") String farmType,
            @RequestParam("description") String description,
            @RequestParam("farmSize") double farmSize,
            @RequestParam("establishedYear") String establishedYear,
            @RequestParam("certifications") String certifications,
            @RequestParam(value = "contactNumber", required = false) String contactNumber,
            RedirectAttributes redirectAttributes) {
        
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String farmerId = auth.getName();
            
            // Validate contact number format if provided
            if (contactNumber != null && !contactNumber.trim().isEmpty()) {
                if (!isValidContactNumber(contactNumber)) {
                    redirectAttributes.addFlashAttribute("error", "Invalid contact number format. Please enter a valid phone number.");
                    return "redirect:/farmer/profile";
                }
            }
            
            // Validate email format
            if (email != null && !email.trim().isEmpty()) {
                if (!isValidEmail(email)) {
                    redirectAttributes.addFlashAttribute("error", "Invalid email format. Please enter a valid email address.");
                    return "redirect:/farmer/profile";
                }
            }
            
            // Use contactNumber if provided, otherwise fall back to phone
            String finalPhone = (contactNumber != null && !contactNumber.trim().isEmpty()) ? contactNumber : phone;
            
            // Update basic info with validated contact number
            farmerService.updateBasicInfo(farmerId, farmName, ownerName, email, finalPhone, address, city, state, zipCode);
            
            // Update farm details
            farmerService.updateFarmDetails(farmerId, farmType, description, farmSize, establishedYear, certifications);
            
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
        } catch (Exception e) {
            System.err.println("Error updating farmer profile: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to update profile. Please try again.");
        }
        
        return "redirect:/farmer/profile";
    }

    // Add contact number validation method
    private boolean isValidContactNumber(String contactNumber) {
        if (contactNumber == null || contactNumber.trim().isEmpty()) {
            return false;
        }
        
        // Remove all non-digit characters for validation
        String cleanNumber = contactNumber.replaceAll("[^0-9]", "");
        
        // Check if it's a valid length (10-15 digits)
        if (cleanNumber.length() < 10 || cleanNumber.length() > 15) {
            return false;
        }
        
        // Basic regex pattern for phone numbers (supports various formats)
        String phonePattern = "^[+]?[0-9\\s\\-\\(\\)]{10,20}$";
        return contactNumber.matches(phonePattern);
    }
    
    // Add email validation method
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return true; // Allow empty email
        }
        
        String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailPattern);
    }

    // Add REST endpoint for real-time contact validation
    @GetMapping("/api/validate-contact")
    @ResponseBody
    public ResponseEntity<?> validateContactNumber(@RequestParam("contactNumber") String contactNumber) {
        try {
            boolean isValid = isValidContactNumber(contactNumber);
            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            
            if (!isValid) {
                response.put("message", "Please enter a valid contact number (10-15 digits)");
            } else {
                response.put("message", "Contact number is valid");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Validation failed", "valid", false));
        }
    }


    // Add endpoint to update just contact number
    @PostMapping("/profile/update-contact")
    @ResponseBody
    public ResponseEntity<?> updateContactNumber(
            @RequestParam("contactNumber") String contactNumber) {
        
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String farmerId = auth.getName();
            
            // Validate contact number
            if (!isValidContactNumber(contactNumber)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Invalid contact number format"));
            }
            
            // Get current farmer data
            Farmer farmer = farmerService.getFarmerById(farmerId);
            if (farmer == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Update only the contact number
            farmerService.updateBasicInfo(
                farmerId, 
                farmer.getFarmName(), 
                farmer.getOwnerName(), 
                farmer.getFarmerEmail(), 
                contactNumber, 
                farmer.getFarmerAddress(), 
                farmer.getCity(), 
                farmer.getState(), 
                farmer.getZipCode()
            );
            
            return ResponseEntity.ok(Map.of("success", true, "message", "Contact number updated successfully"));
            
        } catch (Exception e) {
            System.err.println("Error updating contact number: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Failed to update contact number"));
        }
    }
    
    // REST endpoint for dashboard statistics
    @GetMapping("/statistics")
    @ResponseBody
    public ResponseEntity<?> getDashboardStatistics() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String farmerId = auth.getName();
            
            if (farmerId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not authenticated"));
            }
            
            // Get order statistics
            List<Order> allOrders = farmerOrderService.getOrdersByFarmerId(farmerId);
            int pendingOrders = farmerOrderService.getOrdersByFarmerIdAndStatus(farmerId, "Pending").size();
            int processingOrders = farmerOrderService.getOrdersByFarmerIdAndStatus(farmerId, "Processing").size();
            int deliveredOrders = farmerOrderService.getOrdersByFarmerIdAndStatus(farmerId, "Delivered").size();
            int totalOrders = allOrders.size();
            
            // Get inventory statistics
            List<InventoryItem> items = inventoryItemService.getItemsByFarmerId(farmerId);
            int totalItems = items.size();
            int lowStockItems = (int) items.stream()
                .filter(item -> {
                    try {
                        return Integer.parseInt(item.getItemQuantity()) < 5;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                })
                .count();
              // Calculate revenue statistics
            double totalRevenue = allOrders.stream()
                .filter(order -> "Delivered".equals(order.getStatus()))
                .mapToDouble(order -> order.getTotalAmount())
                .sum();
            
            // Calculate monthly revenue (current month)
            double monthlyRevenue = allOrders.stream()
                .filter(order -> "Delivered".equals(order.getStatus()))
                .filter(order -> {
                    try {
                        String orderDate = order.getOrderDate();
                        if (orderDate != null && orderDate.length() >= 7) {
                            String orderMonth = orderDate.substring(0, 7); // YYYY-MM format
                            String currentMonth = java.time.YearMonth.now().toString();
                            return orderMonth.equals(currentMonth);
                        }
                    } catch (Exception e) {
                        // Ignore date parsing errors
                    }
                    return false;
                })
                .mapToDouble(order -> order.getTotalAmount())
                .sum();
            
            // Create response map
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("pendingOrders", pendingOrders);
            statistics.put("processingOrders", processingOrders);
            statistics.put("deliveredOrders", deliveredOrders);
            statistics.put("totalOrders", totalOrders);
            statistics.put("totalItems", totalItems);
            statistics.put("lowStockItems", lowStockItems);
            statistics.put("totalRevenue", totalRevenue);
            statistics.put("monthlyRevenue", monthlyRevenue);
            
            return ResponseEntity.ok(statistics);
            
        } catch (Exception e) {
            logger.error("Error fetching dashboard statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch statistics: " + e.getMessage()));
        }
    }

    // Helper method to populate item names and quantities for orders
    private void populateOrderItemNames(List<Order> orders) {
        for (var order : orders) {
            List<String> itemNames = new ArrayList<>();
            List<String> quantities = new ArrayList<>();
            if (order.getProductIds() != null) {
                for (int i = 0; i < order.getProductIds().size(); i++) {
                    String productId = order.getProductIds().get(i);
                    InventoryItem item = inventoryItemService.getItemById(productId);
                    itemNames.add(item != null ? item.getItemName() : "Unknown Product");
                    if (order.getQuantities() != null && i < order.getQuantities().size()) {
                        Object qtyObj = order.getQuantities().get(i);
                        String qtyStr;
                        if (qtyObj instanceof Number) {
                            qtyStr = String.valueOf(((Number) qtyObj).intValue());
                        } else {
                            try {
                                qtyStr = String.valueOf(Double.valueOf(qtyObj.toString()).intValue());
                            } catch (Exception e) {
                                qtyStr = qtyObj.toString();
                            }
                        }
                        quantities.add(qtyStr);
                    } else {
                        quantities.add("1");
                    }
                }
            }
            order.setItemNames(itemNames);
            // Don't set order.setQuantities(quantities) since setter expects List<Object> and we only need to display quantities as strings in the view
        }
    }

}
