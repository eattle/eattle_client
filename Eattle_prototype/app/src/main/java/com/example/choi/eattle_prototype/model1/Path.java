package com.example.choi.eattle_prototype.model1;

/**
 * Created by GA on 2015. 2. 18..
 */
public class Path {
    long time;
    int spotId;

    public Path(){

    }

    public Path(long time, int spotId){
        this.time = time;
        this.spotId = spotId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getSpotId() {
        return spotId;
    }

    public void setSpotId(int spotId) {
        this.spotId = spotId;
    }
}
