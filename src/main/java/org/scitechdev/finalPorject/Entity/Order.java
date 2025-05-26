package org.scitechdev.finalPorject.Entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private String orderId;
    private String buyerId;
    private List<String> productIds;
    private List<Object> quantities;
    private List<String> itemNames;
    private double totalAmount;
    private String status;    private String deliveryOption;
    private String paymentMethod;
    private String deliveryAddress;
    private String orderDate; // Stored as ISO string for Firestore compatibility

    public Order() {}

    public Order(String orderId, String buyerId, List<String> productIds, List<Object> quantities, double totalAmount, String status) {
        this.orderId = orderId;
        this.buyerId = buyerId;
        this.productIds = productIds;
        this.quantities = quantities;
        this.totalAmount = totalAmount;
        this.status = status;
        this.orderDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
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
        this.deliveryAddress = deliveryAddress;
    }    public LocalDateTime getOrderDate() {
        return orderDate != null ? LocalDateTime.parse(orderDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate != null ? orderDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
    }
    
    // Additional convenience methods for direct string access
    public String getOrderDateString() {
        return orderDate;
    }
    
    public void setOrderDateString(String orderDate) {
        this.orderDate = orderDate;
    }
}
