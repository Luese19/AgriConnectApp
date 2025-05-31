package org.scitechdev.finalPorject.controller;

import org.scitechdev.finalPorject.Entity.Order;
import org.scitechdev.finalPorject.Entity.InventoryItem;
import org.scitechdev.finalPorject.service.OrderService;
import org.scitechdev.finalPorject.service.InventoryItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/buyer/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private InventoryItemService inventoryItemService;    @GetMapping
    public String getOrders(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String buyerId = auth.getName();
        List<Order> orders = orderService.getOrdersByBuyerId(buyerId);
        for (Order order : orders) {
            List<String> itemNames = new ArrayList<>();
            List<String> quantitiesStr = new ArrayList<>();
            if (order.getProductIds() != null) {
                for (int i = 0; i < order.getProductIds().size(); i++) {
                    String productId = order.getProductIds().get(i);
                    InventoryItem item = inventoryItemService.getItemById(productId);
                    itemNames.add(item != null ? item.getItemName() : "Unknown Product");
                    // Use the quantity from Firestore, default to "1" if missing
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
                        quantitiesStr.add(qtyStr);
                    } else {
                        quantitiesStr.add("1");
                    }
                }
            }
            order.setItemNames(itemNames);
            // Set quantities as strings for display but keep original quantities intact
            if (!quantitiesStr.isEmpty()) {
                List<Object> quantitiesObj = new ArrayList<>(quantitiesStr);
                order.setQuantities(quantitiesObj);
            }
        }
        model.addAttribute("orders", orders);
        return "buyer/orders";
    }

    @PostMapping("/place")
    public String placeOrder(@ModelAttribute Order order, @RequestParam(value = "quantities", required = false) List<Integer> quantities) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String buyerId = auth.getName();        order.setBuyerId(buyerId);
        // Set order date if not already set
        if (order.getOrderDate() == null || order.getOrderDate().trim().isEmpty()) {
            order.setOrderDate(java.time.LocalDateTime.now().toString());
        }
        if (order.getOrderPriority() == null || order.getOrderPriority().trim().isEmpty()) {
            order.setOrderPriority("normal");
        }
        if (order.getDeliveryOption() == null || order.getDeliveryOption().trim().isEmpty()) {
            order.setDeliveryOption("delivery");
        }
        if (order.getPaymentMethod() == null || order.getPaymentMethod().trim().isEmpty()) {
            order.setPaymentMethod("cod");
        }
        if (order.getDeliveryAddress() == null || order.getDeliveryAddress().trim().isEmpty()) {
            order.setDeliveryAddress("Address not provided");
        }
        
        // If quantities are submitted as request param, use them
        if (quantities != null && !quantities.isEmpty()) {
            List<Object> intQuantities = new ArrayList<>(quantities);
            order.setQuantities(intQuantities);
        } else if (order.getQuantities() != null) {
            // Fallback: try to parse from model attribute
            List<Object> originalQuantities = order.getQuantities();
            List<Integer> parsedQuantities = new ArrayList<>();
            for (Object qtyObj : originalQuantities) {
                try {
                    int intQty = Integer.parseInt(qtyObj.toString());
                    parsedQuantities.add(intQty);
                } catch (Exception e) {
                    parsedQuantities.add(1); // fallback if conversion fails
                }
            }
            order.setQuantities(new ArrayList<Object>(parsedQuantities));
        }
        orderService.saveOrder(order);
        return "redirect:/buyer/orders";
    }

    @PostMapping("/delete")
    public String deleteOrder(@RequestParam String orderId) {
        orderService.deleteOrder(orderId);
        return "redirect:/buyer/orders";
    }    // API endpoint to fetch order details for the modal
    @GetMapping("/api/{orderId}")
    @ResponseBody
    public ResponseEntity<?> getOrderDetails(@PathVariable String orderId) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String buyerId = auth.getName();
            
            // Validate orderId
            if (orderId == null || orderId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Order ID is required");
            }
            
            List<Order> orders = orderService.getOrdersByBuyerId(buyerId);
            Order order = orders.stream()
                .filter(o -> orderId.equals(o.getOrderId()))
                .findFirst()
                .orElse(null);
            
            if (order == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Populate item names and quantities if not already done
            if (order.getItemNames() == null || order.getItemNames().isEmpty()) {
                List<String> itemNames = new ArrayList<>();
                List<String> quantities = new ArrayList<>();
                
                if (order.getProductIds() != null && !order.getProductIds().isEmpty()) {
                    for (int i = 0; i < order.getProductIds().size(); i++) {
                        String productId = order.getProductIds().get(i);
                        InventoryItem item = inventoryItemService.getItemById(productId);
                        itemNames.add(item != null ? item.getItemName() : "Unknown Product");
                        
                        // Handle quantities
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
                } else {
                    itemNames.add("No items specified");
                    quantities.add("1");
                }
                
                order.setItemNames(itemNames);
                // Also set the quantities as List<Object> for consistency
                List<Object> quantitiesObj = new ArrayList<>(quantities);
                order.setQuantities(quantitiesObj);
            }
            
            return ResponseEntity.ok(order);
            
        } catch (Exception e) {
            System.err.println("Error fetching order details for orderId: " + orderId + " - " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error loading order details: " + e.getMessage());
        }
    }
}
