package com.example.choi.eattle_prototype.model;

/**
 * Created by GA on 2015. 2. 18..
 */
public class Location {
    long time;
    long latitude;
    long longitude;

    public Location() {

    }

    public Location(long time, long latitude, long longitutde){
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitutde;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getLatitude() {
        return latitude;
    }

    public void setLatitude(long latitude) {
        this.latitude = latitude;
    }

    public long getLongitude() {
        return longitude;
    }

    public void setLongitude(long longitude) {
        this.longitude = longitude;
    }
}

