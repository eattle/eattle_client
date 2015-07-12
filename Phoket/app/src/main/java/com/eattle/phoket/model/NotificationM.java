package com.eattle.phoket.model;

/**
 * Created by dh_st_000 on 2015-07-11.
 */
public class NotificationM {
    Long notificationTime;
    int lastPictureID;


    public NotificationM() {
    }
    public NotificationM(Long notificationTime,int lastPictureID){
        this.notificationTime = notificationTime;
        this.lastPictureID = lastPictureID;
    }

    public Long getNotificationTime(){
        return this.notificationTime;
    }
    public void setNotificationTime(Long notificationTime){
        this.notificationTime = notificationTime;
    }
    public int getLastPictureID(){
        return this.lastPictureID;
    }
    public void setLastPictureID(int lastPictureID){
        this.lastPictureID = lastPictureID;
    }
}
