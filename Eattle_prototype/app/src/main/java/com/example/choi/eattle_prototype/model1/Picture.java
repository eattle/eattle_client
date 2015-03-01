package com.example.choi.eattle_prototype.model1;

/**
 * Created by GA on 2015. 2. 18..
 */
public class Picture {

    int pictureID;
    String picPath;
    String memo;
    long time;

    public Picture() {
    }

    public Picture(int pictureID, String picPath, String memo, long time) {
        this.pictureID = pictureID;
        this.picPath = picPath;
        this.memo = memo;
        this.time = time;
    }

    public int getPictureID() {
        return pictureID;
    }

    public void setPictureID(int pictureID) {
        this.pictureID = pictureID;
    }

    public String getPicPath() {
        return picPath;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
