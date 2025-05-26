package org.scitechdev.finalPorject.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.scitechdev.finalPorject.Entity.CartItem;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class CartItemService {
    private static final String COLLECTION_NAME = "cartItems";

    public void saveCartItem(CartItem item) {
        Firestore db = FirestoreClient.getFirestore();
        if (item.getCartItemId() == null || item.getCartItemId().isEmpty()) {
            item.setCartItemId(UUID.randomUUID().toString());
        }
        db.collection(COLLECTION_NAME).document(item.getCartItemId()).set(item);
    }

    public List<CartItem> getCartItemsByBuyerId(String buyerId) {
        Firestore db = FirestoreClient.getFirestore();
        List<CartItem> items = new ArrayList<>();
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).whereEqualTo("buyerId", buyerId).get();
        try {
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (QueryDocumentSnapshot doc : documents) {
                CartItem item = doc.toObject(CartItem.class);
                items.add(item);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return items;
    }

    public void deleteCartItem(String cartItemId) {
        Firestore db = FirestoreClient.getFirestore();
        db.collection(COLLECTION_NAME).document(cartItemId).delete();
    }
}
