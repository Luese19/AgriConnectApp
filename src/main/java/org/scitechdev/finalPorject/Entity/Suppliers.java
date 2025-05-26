package org.scitechdev.finalPorject.Entity;

import org.checkerframework.checker.signature.qual.Identifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@EntityScan
public class Suppliers {

    @Identifier
    private String supplierId;
    private String supplierName;
    private String supplierAddress;
    private String supplierPhone;
    private String supplierEmail;
    private String supplierPassword;
    private String supplierType;
    private String supplierStatus;
    private String supplierImage;
    private String supplierLocation;
    private String supplierDescription;


    public Suppliers(String supplierId, String supplierName, String supplierAddress, String supplierPhone,
            String supplierEmail, String supplierPassword, String supplierType, String supplierStatus,
            String supplierImage, String supplierLocation, String supplierDescription) {
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.supplierAddress = supplierAddress;
        this.supplierPhone = supplierPhone;
        this.supplierEmail = supplierEmail;
        this.supplierPassword = supplierPassword;
        this.supplierType = supplierType;
        this.supplierStatus = supplierStatus;
        this.supplierImage = supplierImage;
        this.supplierLocation = supplierLocation;
        this.supplierDescription = supplierDescription;
    }


    public String getSupplierId() {
        return supplierId;
    }


    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }


    public String getSupplierName() {
        return supplierName;
    }


    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }


    public String getSupplierAddress() {
        return supplierAddress;
    }


    public void setSupplierAddress(String supplierAddress) {
        this.supplierAddress = supplierAddress;
    }


    public String getSupplierPhone() {
        return supplierPhone;
    }


    public void setSupplierPhone(String supplierPhone) {
        this.supplierPhone = supplierPhone;
    }


    public String getSupplierEmail() {
        return supplierEmail;
    }


    public void setSupplierEmail(String supplierEmail) {
        this.supplierEmail = supplierEmail;
    }


    public String getSupplierPassword() {
        return supplierPassword;
    }


    public void setSupplierPassword(String supplierPassword) {
        this.supplierPassword = supplierPassword;
    }


    public String getSupplierType() {
        return supplierType;
    }


    public void setSupplierType(String supplierType) {
        this.supplierType = supplierType;
    }


    public String getSupplierStatus() {
        return supplierStatus;
    }


    public void setSupplierStatus(String supplierStatus) {
        this.supplierStatus = supplierStatus;
    }


    public String getSupplierImage() {
        return supplierImage;
    }


    public void setSupplierImage(String supplierImage) {
        this.supplierImage = supplierImage;
    }


    public String getSupplierLocation() {
        return supplierLocation;
    }


    public void setSupplierLocation(String supplierLocation) {
        this.supplierLocation = supplierLocation;
    }


    public String getSupplierDescription() {
        return supplierDescription;
    }


    public void setSupplierDescription(String supplierDescription) {
        this.supplierDescription = supplierDescription;
    }
    
}
