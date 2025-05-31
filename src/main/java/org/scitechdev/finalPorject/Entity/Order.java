package org.scitechdev.finalPorject.Entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private String orderId;
    private String buyerId;
    private List<String> productIds;
    private List<Object> quantities;
    private List<String> itemNames;
    private double totalAmount;    private String status;
    private String deliveryOption;
    private String paymentMethod;    private String deliveryAddress;
    private String orderDate; // Store as ISO formatted string
    private String acceptedDate; // When farmer accepts the order
    private String deliveredDate; // When order is marked as delivered
    private String farmerNotes; // Notes from farmer to buyer
    private String buyerNotes; // Notes from buyer to farmer
    private String estimatedDeliveryTime; // Estimated delivery time set by farmer
    private double shippingCost; // Shipping cost
    private String orderPriority; // normal, urgent, express

    public Order() {}

    public Order(String orderId, String buyerId, List<String> productIds, List<Object> quantities, double totalAmount, String status) {
        this.orderId = orderId;
        this.buyerId = buyerId;
        this.productIds = productIds;
        this.quantities = quantities;
        this.totalAmount = totalAmount;
        this.status = status;
        this.orderDate = java.time.LocalDateTime.now().toString();
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public List<String> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<String> productIds) {
        this.productIds = productIds;
    }

    public List<Object> getQuantities() {
        // If Firestore stored a single number instead of a list, wrap it in a list
        if (quantities != null && !(quantities instanceof List)) {
            List<Object> fixed = new ArrayList<>();
            fixed.add(quantities);
            return fixed;
        }
        return quantities;
    }

    public void setQuantities(List<Object> quantities) {
        this.quantities = quantities;
    }

    public List<String> getItemNames() {
        return itemNames;
    }

    public void setItemNames(List<String> itemNames) {
        this.itemNames = itemNames;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDeliveryOption() {
        return deliveryOption;
    }

    public void setDeliveryOption(String deliveryOption) {
        this.deliveryOption = deliveryOption;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;    }    // Simple string-based date methods
    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    // Convenience method to get LocalDateTime for business logic
    private LocalDateTime getOrderDateTime() {
        return orderDate != null ? LocalDateTime.parse(orderDate) : null;
    }
    
    private void setOrderDateTime(LocalDateTime orderDate) {
        this.orderDate = orderDate != null ? orderDate.toString() : null;
    }

    public String getAcceptedDate() {
        return acceptedDate;
    }

    public void setAcceptedDate(String acceptedDate) {
        this.acceptedDate = acceptedDate;
    }

    public String getDeliveredDate() {
        return deliveredDate;
    }    public void setDeliveredDate(String deliveredDate) {
        this.deliveredDate = deliveredDate;
    }

    public String getFarmerNotes() {
        return farmerNotes;
    }

    public void setFarmerNotes(String farmerNotes) {
        this.farmerNotes = farmerNotes;
    }

    public String getBuyerNotes() {
        return buyerNotes;
    }

    public void setBuyerNotes(String buyerNotes) {
        this.buyerNotes = buyerNotes;
    }

    public String getEstimatedDeliveryTime() {
        return estimatedDeliveryTime;
    }

    public void setEstimatedDeliveryTime(String estimatedDeliveryTime) {
        this.estimatedDeliveryTime = estimatedDeliveryTime;
    }

    public double getShippingCost() {
        return shippingCost;
    }

    public void setShippingCost(double shippingCost) {
        this.shippingCost = shippingCost;
    }

    public String getOrderPriority() {
        return orderPriority;
    }

    public void setOrderPriority(String orderPriority) {
        this.orderPriority = orderPriority;    }
}
