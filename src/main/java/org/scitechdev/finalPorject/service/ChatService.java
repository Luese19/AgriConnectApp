package org.scitechdev.finalPorject.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.scitechdev.finalPorject.Entity.ChatConversation;
import org.scitechdev.finalPorject.Entity.ChatMessageEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class ChatService {
    private static final String CONVERSATIONS_COLLECTION = "chat_conversations";
    private static final String MESSAGES_COLLECTION = "chat_messages";

    // Conversation Management
    public String createOrGetConversation(String farmerId, String buyerId, String farmerName, String buyerName) {
        try {
            Firestore db = FirestoreClient.getFirestore();
            
            // Check if conversation already exists
            ApiFuture<QuerySnapshot> future = db.collection(CONVERSATIONS_COLLECTION)
                    .whereEqualTo("farmerId", farmerId)
                    .whereEqualTo("buyerId", buyerId)
                    .get();

            QuerySnapshot querySnapshot = future.get();
            
            if (!querySnapshot.isEmpty()) {
                // Return existing conversation
                return querySnapshot.getDocuments().get(0).getId();
            }

            // Create new conversation
            ChatConversation conversation = new ChatConversation(farmerId, buyerId, farmerName, buyerName);
            String conversationId = UUID.randomUUID().toString();
            conversation.setConversationId(conversationId);

            db.collection(CONVERSATIONS_COLLECTION).document(conversationId).set(conversation).get();
            return conversationId;

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new RuntimeException("Error creating/getting conversation: " + e.getMessage());
        }
    }

    public List<ChatConversation> getConversationsByUserId(String userId, String userRole) {
        try {
            Firestore db = FirestoreClient.getFirestore();
            List<ChatConversation> conversations = new ArrayList<>();

            String fieldName = userRole.equals("FARMER") ? "farmerId" : "buyerId";
            
            ApiFuture<QuerySnapshot> future = db.collection(CONVERSATIONS_COLLECTION)
                    .whereEqualTo(fieldName, userId)
                    .whereEqualTo("status", "ACTIVE")
                    .orderBy("lastMessageTime", Query.Direction.DESCENDING)
                    .get();

            QuerySnapshot querySnapshot = future.get();
            for (QueryDocumentSnapshot doc : querySnapshot.getDocuments()) {
                ChatConversation conversation = doc.toObject(ChatConversation.class);
                conversations.add(conversation);
            }

            return conversations;

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public ChatConversation getConversationById(String conversationId) {
        try {
            Firestore db = FirestoreClient.getFirestore();
            DocumentSnapshot doc = db.collection(CONVERSATIONS_COLLECTION).document(conversationId).get().get();
            
            if (doc.exists()) {
                return doc.toObject(ChatConversation.class);
            }
            return null;

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Message Management
    public String saveMessage(ChatMessageEntity message) {
        try {
            Firestore db = FirestoreClient.getFirestore();
            
            String messageId = UUID.randomUUID().toString();
            message.setMessageId(messageId);

            // Save message
            db.collection(MESSAGES_COLLECTION).document(messageId).set(message).get();

            // Update conversation with last message info
            updateConversationLastMessage(message);

            return messageId;

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new RuntimeException("Error saving message: " + e.getMessage());
        }
    }

    public List<ChatMessageEntity> getMessagesByConversationId(String conversationId, int limit) {
        try {
            Firestore db = FirestoreClient.getFirestore();
            List<ChatMessageEntity> messages = new ArrayList<>();

            Query query = db.collection(MESSAGES_COLLECTION)
                    .whereEqualTo("conversationId", conversationId)
                    .orderBy("timestamp", Query.Direction.ASCENDING);

            if (limit > 0) {
                query = query.limit(limit);
            }

            ApiFuture<QuerySnapshot> future = query.get();
            QuerySnapshot querySnapshot = future.get();

            for (QueryDocumentSnapshot doc : querySnapshot.getDocuments()) {
                ChatMessageEntity message = doc.toObject(ChatMessageEntity.class);
                messages.add(message);
            }

            return messages;

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<ChatMessageEntity> getMessagesByConversationId(String conversationId) {
        return getMessagesByConversationId(conversationId, 0); // 0 means no limit
    }

    public void markMessageAsRead(String messageId) {
        try {
            Firestore db = FirestoreClient.getFirestore();
            db.collection(MESSAGES_COLLECTION).document(messageId).update("read", true, "status", "READ").get();

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void markConversationAsRead(String conversationId, String userId, String userRole) {
        try {
            Firestore db = FirestoreClient.getFirestore();
            
            Map<String, Object> updates = new HashMap<>();
            if (userRole.equals("FARMER")) {
                updates.put("farmerRead", true);
                updates.put("unreadCountFarmer", 0);
            } else {
                updates.put("buyerRead", true);
                updates.put("unreadCountBuyer", 0);
            }

            db.collection(CONVERSATIONS_COLLECTION).document(conversationId).update(updates).get();

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void updateConversationLastMessage(ChatMessageEntity message) {
        try {
            Firestore db = FirestoreClient.getFirestore();
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("lastMessage", message.getContent());
            updates.put("lastMessageSender", message.getSenderName());
            updates.put("lastMessageTime", message.getTimestamp());
            updates.put("updatedAt", LocalDateTime.now());

            // Update unread counts
            ChatConversation conversation = getConversationById(message.getConversationId());
            if (conversation != null) {
                if (message.getSenderRole().equals("FARMER")) {
                    updates.put("buyerRead", false);
                    updates.put("unreadCountBuyer", conversation.getUnreadCountBuyer() + 1);
                } else {
                    updates.put("farmerRead", false);
                    updates.put("unreadCountFarmer", conversation.getUnreadCountFarmer() + 1);
                }
            }

            db.collection(CONVERSATIONS_COLLECTION).document(message.getConversationId()).update(updates).get();

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    // Search and utility methods
    public List<ChatConversation> searchConversations(String userId, String userRole, String searchTerm) {
        try {            Firestore db = FirestoreClient.getFirestore();
            List<ChatConversation> conversations = new ArrayList<>();

            String fieldName = userRole.equals("FARMER") ? "farmerId" : "buyerId";
            
            ApiFuture<QuerySnapshot> future = db.collection(CONVERSATIONS_COLLECTION)
                    .whereEqualTo(fieldName, userId)
                    .whereEqualTo("status", "ACTIVE")
                    .get();

            QuerySnapshot querySnapshot = future.get();
            for (QueryDocumentSnapshot doc : querySnapshot.getDocuments()) {
                ChatConversation conversation = doc.toObject(ChatConversation.class);
                
                // Simple search filter
                String searchTarget = userRole.equals("FARMER") ? conversation.getBuyerName() : conversation.getFarmerName();
                if (searchTarget != null && searchTarget.toLowerCase().contains(searchTerm.toLowerCase())) {
                    conversations.add(conversation);
                }
            }

            return conversations;

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public int getUnreadMessageCount(String userId, String userRole) {
        try {
            List<ChatConversation> conversations = getConversationsByUserId(userId, userRole);
            return conversations.stream()
                    .mapToInt(conv -> userRole.equals("FARMER") ? conv.getUnreadCountFarmer() : conv.getUnreadCountBuyer())
                    .sum();

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void deleteConversation(String conversationId) {
        try {
            Firestore db = FirestoreClient.getFirestore();
            
            // Mark conversation as archived instead of deleting
            db.collection(CONVERSATIONS_COLLECTION).document(conversationId).update("status", "ARCHIVED").get();

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
