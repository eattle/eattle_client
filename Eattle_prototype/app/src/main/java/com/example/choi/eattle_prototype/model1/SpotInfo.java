package com.example.choi.eattle_prototype.model1;

/**
 * Created by GA on 2015. 2. 18..
 */
public class SpotInfo {
    int id;
    String spotInfo;
    String picName;

    public SpotInfo() {

    }

    public SpotInfo(String spotInfo, String picName) {
        this.spotInfo = spotInfo;
        this.picName = picName;
    }

    public SpotInfo(int id, String spotInfo, String picName) {
        this.id = id;
        this.spotInfo = spotInfo;
        this.picName = picName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSpotInfo() {
        return spotInfo;
    }

    public void setSpotInfo(String spotInfo) {
        this.spotInfo = spotInfo;
    }

    public String getPicName() {
        return picName;
    }

    public void setPicName(String picName) {
        this.picName = picName;
    }
}
