package org.scitechdev.finalPorject.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.scitechdev.finalPorject.Entity.Order;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class OrderService {
    private static final String COLLECTION_NAME = "orders";    public void saveOrder(Order order) {
        Firestore db = FirestoreClient.getFirestore();
        if (order.getOrderId() == null || order.getOrderId().isEmpty()) {
            order.setOrderId(UUID.randomUUID().toString());
        }
        try {
            db.collection(COLLECTION_NAME).document(order.getOrderId()).set(order);
        } catch (Exception e) {
            System.err.println("Error saving order: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to save order: " + e.getMessage(), e);
        }
    }

    public List<Order> getOrdersByBuyerId(String buyerId) {
        Firestore db = FirestoreClient.getFirestore();
        List<Order> orders = new ArrayList<>();
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).whereEqualTo("buyerId", buyerId).get();
        try {
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (QueryDocumentSnapshot doc : documents) {
                Order order = doc.toObject(Order.class);
                orders.add(order);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return orders;
    }    public void deleteOrder(String orderId) {
        // Validate orderId
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID cannot be null or empty");
        }
        
        Firestore db = FirestoreClient.getFirestore();
        try {
            db.collection(COLLECTION_NAME).document(orderId.trim()).delete();
        } catch (Exception e) {
            System.err.println("Error deleting order with ID: " + orderId);
            e.printStackTrace();
            throw new RuntimeException("Failed to delete order: " + e.getMessage(), e);
        }
    }
}
