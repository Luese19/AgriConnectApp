package org.scitechdev.finalPorject.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.scitechdev.finalPorject.Entity.Order;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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
    }

    public void updateOrderStatus(String orderId, String status) {
        Firestore db = FirestoreClient.getFirestore();
        db.collection(COLLECTION_NAME).document(orderId).update("status", status);
    }
}
