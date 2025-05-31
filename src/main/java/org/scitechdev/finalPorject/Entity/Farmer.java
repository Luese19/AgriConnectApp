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
    
    // Additional fields for enhanced profile management
    private String farmName;
    private String ownerName;
    private String city;
    private String state;
    private String zipCode;
    private String description;
    private double farmSize;
    private String establishedYear;
    private String certifications;
    
    // Settings fields
    private boolean emailNotifications = true;
    private boolean smsNotifications = false;
    private String preferredLanguage = "English";
    private String timezone = "UTC";

    // Default constructor
    public Farmer() {
    }


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

    // Getters and setters for new fields
    public String getFarmName() {
        return farmName;
    }

    public void setFarmName(String farmName) {
        this.farmName = farmName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getFarmSize() {
        return farmSize;
    }

    public void setFarmSize(double farmSize) {
        this.farmSize = farmSize;
    }

    public String getEstablishedYear() {
        return establishedYear;
    }

    public void setEstablishedYear(String establishedYear) {
        this.establishedYear = establishedYear;
    }

    public String getCertifications() {
        return certifications;
    }

    public void setCertifications(String certifications) {
        this.certifications = certifications;
    }

    public boolean isEmailNotifications() {
        return emailNotifications;
    }

    public void setEmailNotifications(boolean emailNotifications) {
        this.emailNotifications = emailNotifications;
    }

    public boolean isSmsNotifications() {
        return smsNotifications;
    }

    public void setSmsNotifications(boolean smsNotifications) {
        this.smsNotifications = smsNotifications;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
    
}
