package org.scitechdev.finalPorject.Entity;

public class InventoryItem {
    private String itemId;
    private String itemName;
    private String itemDescription;
    private String itemCategory;
    private String itemPrice;
    private String itemQuantity;
    private String itemSupplierId;
    private String itemImage;
    private String itemStatus;
    private String farmerId;
    private String location;

    public InventoryItem() {}

    public InventoryItem(String itemId, String itemName, String itemDescription, String itemCategory, String itemPrice,
            String itemQuantity, String itemSupplierId, String itemImage, String itemStatus, String farmerId, String location) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.itemCategory = itemCategory;
        this.itemPrice = itemPrice;
        this.itemQuantity = itemQuantity;
        this.itemSupplierId = itemSupplierId;
        this.itemImage = itemImage;
        this.itemStatus = itemStatus;
        this.farmerId = farmerId;
        this.location = location;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public String getItemCategory() {
        return itemCategory;
    }

    public void setItemCategory(String itemCategory) {
        this.itemCategory = itemCategory;
    }

    public String getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(String itemPrice) {
        this.itemPrice = itemPrice;
    }

    public String getItemQuantity() {
        return itemQuantity;
    }

    public void setItemQuantity(String itemQuantity) {
        this.itemQuantity = itemQuantity;
    }

    public String getItemSupplierId() {
        return itemSupplierId;
    }

    public void setItemSupplierId(String itemSupplierId) {
        this.itemSupplierId = itemSupplierId;
    }

    public String getItemImage() {
        return itemImage;
    }

    public void setItemImage(String itemImage) {
        this.itemImage = itemImage;
    }

    public String getItemStatus() {
        return itemStatus;
    }

    public void setItemStatus(String itemStatus) {
        this.itemStatus = itemStatus;
    }

    public String getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(String farmerId) {
        this.farmerId = farmerId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
    
}