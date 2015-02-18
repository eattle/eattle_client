package com.example.choi.eattle_prototype.model;

/**
 * Created by GA on 2015. 2. 18..
 */
public class Path {
    long time;
    String spotName;

    public Path(){

    }

    public Path(long time, String spotName){
        this.time = time;
        this.spotName = spotName;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getSpotName() {
        return spotName;
    }

    public void setSpotName(String spotName) {
        this.spotName = spotName;
    }
}
