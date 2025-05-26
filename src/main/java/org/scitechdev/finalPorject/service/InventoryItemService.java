package org.scitechdev.finalPorject.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.scitechdev.finalPorject.Entity.InventoryItem;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class InventoryItemService {
    private static final String COLLECTION_NAME = "inventoryItems";

    public void saveItem(InventoryItem item) {
        Firestore db = FirestoreClient.getFirestore();
        if (item.getItemId() == null || item.getItemId().isEmpty()) {
            item.setItemId(UUID.randomUUID().toString());
        }
        db.collection(COLLECTION_NAME).document(item.getItemId()).set(item);
    }

    public List<InventoryItem> getAllItems() {
        Firestore db = FirestoreClient.getFirestore();
        List<InventoryItem> items = new ArrayList<>();
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).get();
        try {
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (QueryDocumentSnapshot doc : documents) {
                InventoryItem item = doc.toObject(InventoryItem.class);
                items.add(item);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return items;
    }

    public void deleteItem(String itemId) {
        Firestore db = FirestoreClient.getFirestore();
        db.collection(COLLECTION_NAME).document(itemId).delete();
    }

    public InventoryItem getItemById(String itemId) {
        Firestore db = FirestoreClient.getFirestore();
        try {
            DocumentSnapshot doc = db.collection(COLLECTION_NAME).document(itemId).get().get();
            if (doc.exists()) {
                return doc.toObject(InventoryItem.class);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<InventoryItem> getItemsByFarmerId(String farmerId) {
        Firestore db = FirestoreClient.getFirestore();
        List<InventoryItem> items = new ArrayList<>();
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).whereEqualTo("farmerId", farmerId).get();
        try {
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (QueryDocumentSnapshot doc : documents) {
                InventoryItem item = doc.toObject(InventoryItem.class);
                items.add(item);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return items;
    }
}
