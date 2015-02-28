package com.example.choi.eattle_prototype.model1;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by GA on 2015. 2. 18..
 */
public class Spot implements Parcelable{

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

    public Spot(Parcel in) {
        readFromParcel(in);
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


    @Override
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {

        dest.writeInt(id);
        dest.writeString(name);
        dest.writeFloat(latitude);
        dest.writeFloat(longitude);
        dest.writeString(spotInfoID);
        dest.writeInt(spotgroupID);
        dest.writeInt(productID);
        dest.writeString(picName);
    }

    private void readFromParcel(Parcel in){
        id = in.readInt();
        name = in.readString();
        latitude = in.readFloat();
        longitude = in.readFloat();
        spotInfoID = in.readString();
        spotgroupID = in.readInt();
        productID = in.readInt();
        picName = in.readString();
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Spot createFromParcel(Parcel in) {
            return new Spot(in);
        }

        public Spot[] newArray(int size) {
            return new Spot[size];
        }
    };
}
