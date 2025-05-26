package org.scitechdev.finalPorject.Entity;

import org.checkerframework.checker.signature.qual.Identifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@EntityScan
public class Farmer {
    @Identifier
    private String farmerId;
    private String farmerName;
    private String farmerAddress;
    private String farmerPhone;
    private String farmerEmail;
    private String farmerPassword;
    private String farmerType;
    private String farmerStatus;
    private String farmerImage;
    private String farmerLocation;


    public Farmer(String farmerId, String farmerName, String farmerAddress, String farmerPhone, String farmerEmail,
            String farmerPassword, String farmerType, String farmerStatus, String farmerImage, String farmerLocation) {
        this.farmerId = farmerId;
        this.farmerName = farmerName;
        this.farmerAddress = farmerAddress;
        this.farmerPhone = farmerPhone;
        this.farmerEmail = farmerEmail;
        this.farmerPassword = farmerPassword;
        this.farmerType = farmerType;
        this.farmerStatus = farmerStatus;
        this.farmerImage = farmerImage;
        this.farmerLocation = farmerLocation;
    }


    public String getFarmerId() {
        return farmerId;
    }


    public void setFarmerId(String farmerId) {
        this.farmerId = farmerId;
    }


    public String getFarmerName() {
        return farmerName;
    }


    public void setFarmerName(String farmerName) {
        this.farmerName = farmerName;
    }


    public String getFarmerAddress() {
        return farmerAddress;
    }


    public void setFarmerAddress(String farmerAddress) {
        this.farmerAddress = farmerAddress;
    }


    public String getFarmerPhone() {
        return farmerPhone;
    }


    public void setFarmerPhone(String farmerPhone) {
        this.farmerPhone = farmerPhone;
    }


    public String getFarmerEmail() {
        return farmerEmail;
    }


    public void setFarmerEmail(String farmerEmail) {
        this.farmerEmail = farmerEmail;
    }


    public String getFarmerPassword() {
        return farmerPassword;
    }


    public void setFarmerPassword(String farmerPassword) {
        this.farmerPassword = farmerPassword;
    }


    public String getFarmerType() {
        return farmerType;
    }


    public void setFarmerType(String farmerType) {
        this.farmerType = farmerType;
    }


    public String getFarmerStatus() {
        return farmerStatus;
    }


    public void setFarmerStatus(String farmerStatus) {
        this.farmerStatus = farmerStatus;
    }


    public String getFarmerImage() {
        return farmerImage;
    }


    public void setFarmerImage(String farmerImage) {
        this.farmerImage = farmerImage;
    }


    public String getFarmerLocation() {
        return farmerLocation;
    }


    public void setFarmerLocation(String farmerLocation) {
        this.farmerLocation = farmerLocation;
    }

    
    
}
