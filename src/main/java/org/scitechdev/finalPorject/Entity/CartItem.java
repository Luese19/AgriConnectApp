package org.scitechdev.finalPorject.Entity;

public class CartItem {
    private String cartItemId;
    private String buyerId;
    private String productId;
    private int quantity;

    public CartItem() {}

    public CartItem(String cartItemId, String buyerId, String productId, int quantity) {
        this.cartItemId = cartItemId;
        this.buyerId = buyerId;
        this.productId = productId;
        this.quantity = quantity;
    }

    public String getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(String cartItemId) {
        this.cartItemId = cartItemId;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
