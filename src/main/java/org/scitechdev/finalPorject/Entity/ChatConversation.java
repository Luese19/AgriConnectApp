package org.scitechdev.finalPorject.Entity;

import java.time.LocalDateTime;
import java.util.List;

public class ChatConversation {
    private String conversationId;
    private String farmerId;
    private String buyerId;
    private String farmerName;
    private String buyerName;
    private String farmerImage;
    private String buyerImage;
    private String lastMessage;
    private String lastMessageSender;
    private LocalDateTime lastMessageTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean farmerRead;
    private boolean buyerRead;
    private int unreadCountFarmer;
    private int unreadCountBuyer;
    private String status; // ACTIVE, ARCHIVED, BLOCKED

    public ChatConversation() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.farmerRead = true;
        this.buyerRead = true;
        this.unreadCountFarmer = 0;
        this.unreadCountBuyer = 0;
        this.status = "ACTIVE";
    }

    public ChatConversation(String farmerId, String buyerId, String farmerName, String buyerName) {
        this();
        this.farmerId = farmerId;
        this.buyerId = buyerId;
        this.farmerName = farmerName;
        this.buyerName = buyerName;
    }

    // Getters and Setters
    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(String farmerId) {
        this.farmerId = farmerId;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public String getFarmerName() {
        return farmerName;
    }

    public void setFarmerName(String farmerName) {
        this.farmerName = farmerName;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getFarmerImage() {
        return farmerImage;
    }

    public void setFarmerImage(String farmerImage) {
        this.farmerImage = farmerImage;
    }

    public String getBuyerImage() {
        return buyerImage;
    }

    public void setBuyerImage(String buyerImage) {
        this.buyerImage = buyerImage;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastMessageSender() {
        return lastMessageSender;
    }

    public void setLastMessageSender(String lastMessageSender) {
        this.lastMessageSender = lastMessageSender;
    }

    public LocalDateTime getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(LocalDateTime lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isFarmerRead() {
        return farmerRead;
    }

    public void setFarmerRead(boolean farmerRead) {
        this.farmerRead = farmerRead;
    }

    public boolean isBuyerRead() {
        return buyerRead;
    }

    public void setBuyerRead(boolean buyerRead) {
        this.buyerRead = buyerRead;
    }

    public int getUnreadCountFarmer() {
        return unreadCountFarmer;
    }

    public void setUnreadCountFarmer(int unreadCountFarmer) {
        this.unreadCountFarmer = unreadCountFarmer;
    }

    public int getUnreadCountBuyer() {
        return unreadCountBuyer;
    }

    public void setUnreadCountBuyer(int unreadCountBuyer) {
        this.unreadCountBuyer = unreadCountBuyer;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
