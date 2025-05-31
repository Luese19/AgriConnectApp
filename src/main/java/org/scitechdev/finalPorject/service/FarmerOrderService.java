package org.scitechdev.finalPorject.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.scitechdev.finalPorject.Entity.Order;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class FarmerOrderService {
    private static final String COLLECTION_NAME = "orders";

    // Get all orders that contain at least one product belonging to this farmer
    public List<Order> getOrdersByFarmerId(String farmerId) {
        Firestore db = FirestoreClient.getFirestore();
        List<Order> orders = new ArrayList<>();
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).get();
        try {
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (QueryDocumentSnapshot doc : documents) {
                Order order = doc.toObject(Order.class);
                if (order.getProductIds() != null) {
                    for (String productId : order.getProductIds()) {
                        // Check if product belongs to this farmer
                        DocumentSnapshot productDoc = db.collection("inventoryItems").document(productId).get().get();
                        if (productDoc.exists() && farmerId.equals(productDoc.getString("farmerId"))) {
                            orders.add(order);
                            break;
                        }
                    }
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return orders;
    }    public void updateOrderStatus(String orderId, String status) {
        // Validate orderId
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID cannot be null or empty");
        }
        
        Firestore db = FirestoreClient.getFirestore();
        try {
            db.collection(COLLECTION_NAME).document(orderId.trim()).update("status", status);
        } catch (Exception e) {
            System.err.println("Error updating order status for ID: " + orderId);
            e.printStackTrace();
            throw new RuntimeException("Failed to update order status: " + e.getMessage(), e);
        }
    }// Enhanced method to accept order with additional details
    public void acceptOrder(String orderId, String estimatedDeliveryTime, String farmerNotes) {
        // Validate orderId
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID cannot be null or empty");
        }
        
        Firestore db = FirestoreClient.getFirestore();
        Map<String, Object> updates = new HashMap<>();        
        updates.put("status", "Processing");
        updates.put("acceptedDate", java.time.LocalDateTime.now().toString());
        
        if (estimatedDeliveryTime != null && !estimatedDeliveryTime.isBlank()) {
            updates.put("estimatedDeliveryTime", estimatedDeliveryTime);
        }
        if (farmerNotes != null && !farmerNotes.isBlank()) {
            updates.put("farmerNotes", farmerNotes);
        }
        
        try {
            db.collection(COLLECTION_NAME).document(orderId.trim()).update(updates);
        } catch (Exception e) {
            System.err.println("Error accepting order with ID: " + orderId);
            e.printStackTrace();
            throw new RuntimeException("Failed to accept order: " + e.getMessage(), e);
        }
    }    // Method to reject order with reason
    public void rejectOrder(String orderId, String rejectionReason) {
        // Validate orderId
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID cannot be null or empty");
        }
        
        Firestore db = FirestoreClient.getFirestore();
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "Rejected");
        updates.put("farmerNotes", rejectionReason != null ? rejectionReason : "Order rejected by farmer");
        
        try {
            db.collection(COLLECTION_NAME).document(orderId.trim()).update(updates);
        } catch (Exception e) {
            System.err.println("Error rejecting order with ID: " + orderId);
            e.printStackTrace();
            throw new RuntimeException("Failed to reject order: " + e.getMessage(), e);
        }
    }    // Method to mark order as delivered
    public void markAsDelivered(String orderId, String deliveryNotes) {
        // Validate orderId
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID cannot be null or empty");
        }
        
        Firestore db = FirestoreClient.getFirestore();
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "Delivered");
        updates.put("deliveredDate", java.time.LocalDateTime.now().toString());
        
        if (deliveryNotes != null && !deliveryNotes.isBlank()) {
            updates.put("farmerNotes", deliveryNotes);
        }
        
        try {
            db.collection(COLLECTION_NAME).document(orderId.trim()).update(updates);
        } catch (Exception e) {
            System.err.println("Error marking order as delivered for ID: " + orderId);
            e.printStackTrace();
            throw new RuntimeException("Failed to mark order as delivered: " + e.getMessage(), e);
        }
    }    // Get order by ID
    public Order getOrderById(String orderId) {
        // Validate orderId
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID cannot be null or empty");
        }
        
        Firestore db = FirestoreClient.getFirestore();
        try {
            DocumentSnapshot doc = db.collection(COLLECTION_NAME).document(orderId.trim()).get().get();
            if (doc.exists()) {
                Order order = doc.toObject(Order.class);
                // Populate additional fields that might be missing
                populateOrderDetails(order);
                return order;
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error getting order by ID: " + orderId);
            e.printStackTrace();
        }
        return null;
    }

    private void populateOrderDetails(Order order) {
        try {
            // If delivery address is missing, try to get it from buyer profile
            if (order.getDeliveryAddress() == null || order.getDeliveryAddress().isEmpty()) {
                // You might need to implement this based on your user service
                // order.setDeliveryAddress("Address not available");
            }
            
            // Populate item names if missing
            if (order.getItemNames() == null || order.getItemNames().isEmpty()) {
                List<String> itemNames = new ArrayList<>();
                if (order.getProductIds() != null) {
                    for (String productId : order.getProductIds()) {
                        // You'll need to inject InventoryItemService here or pass it as parameter
                        itemNames.add("Product " + productId); // Placeholder
                    }
                    order.setItemNames(itemNames);
                }
            }
        } catch (Exception e) {
            System.err.println("Error populating order details");
            e.printStackTrace();
        }
    }

    // Get orders by status for a specific farmer
    public List<Order> getOrdersByFarmerIdAndStatus(String farmerId, String status) {
        List<Order> allOrders = getOrdersByFarmerId(farmerId);
        List<Order> filteredOrders = new ArrayList<>();
        
        for (Order order : allOrders) {
            if (status.equalsIgnoreCase(order.getStatus())) {
                filteredOrders.add(order);
            }
        }
        
        return filteredOrders;
    }

    // Get pending orders count for a farmer
    public int getPendingOrdersCount(String farmerId) {
        return getOrdersByFarmerIdAndStatus(farmerId, "Pending").size();
    }

    // Get processing orders count for a farmer
    public int getProcessingOrdersCount(String farmerId) {
        return getOrdersByFarmerIdAndStatus(farmerId, "Processing").size();
    }

    // Get delivered orders count for a farmer
    public int getDeliveredOrdersCount(String farmerId) {
        return getOrdersByFarmerIdAndStatus(farmerId, "Delivered").size();
    }
}
