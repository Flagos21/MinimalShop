package com.plf.minimalshop.model;

public class Productos {
    String productName, productBrand, productCategory, productDescription, productStock, productPrice, productImageUrl;
    public Productos() {}

    public Productos(String productName, String productBrand, String productCategory, String productDescription, String productStock, String productPrice, String productImageUrl) {
        this.productName = productName;
        this.productBrand = productBrand;
        this.productCategory = productCategory;
        this.productDescription = productDescription;
        this.productStock = productStock;
        this.productPrice = productPrice;
        this.productImageUrl = productImageUrl;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductBrand() {
        return productBrand;
    }

    public void setProductBrand(String productBrand) {
        this.productBrand = productBrand;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public String getProductStock() {
        return productStock;
    }

    public void setProductStock(String productStock) {
        this.productStock = productStock;
    }

    public String getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(String productPrice) {
        this.productPrice = productPrice;
    }

    public String getProductImageUrl() {
        return productImageUrl;
    }

    public void setProductImageUrl(String productImageUrl) {
        this.productImageUrl = productImageUrl;
    }
}
