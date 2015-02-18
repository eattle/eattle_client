package com.example.choi.eattle_prototype.model;

/**
 * Created by GA on 2015. 2. 18..
 */
public class Spot {

    int id;
    String name;
    float latitude;
    float longitude;
    String spotInfoID;
    int spotgroupID;
    int productID;
    String picName;

    public Spot() {
    }

    public Spot(String name, float latitude, float longitude, String spotInfoID, int spotgroupID, int productID, String picName) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.spotInfoID = spotInfoID;
        this.spotgroupID = spotgroupID;
        this.productID = productID;
        this.picName = picName;
    }

    public Spot(int id, String name, float latitude, float longitude, String spotInfoID, int spotgroupID, int productID, String picName) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.spotInfoID = spotInfoID;
        this.spotgroupID = spotgroupID;
        this.productID = productID;
        this.picName = picName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public String getSpotInfoID() {
        return spotInfoID;
    }

    public void setSpotInfoID(String spotInfoID) {
        this.spotInfoID = spotInfoID;
    }

    public int getSpotgroupID() {
        return spotgroupID;
    }

    public void setSpotgroupID(int spotgroupID) {
        this.spotgroupID = spotgroupID;
    }

    public int getProductID() {
        return productID;
    }

    public void setProductID(int productID) {
        this.productID = productID;
    }

    public String getPicName() {
        return picName;
    }

    public void setPicName(String picName) {
        this.picName = picName;
    }
}
