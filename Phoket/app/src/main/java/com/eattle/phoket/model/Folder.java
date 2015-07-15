package com.eattle.phoket.model;

/**
 * Created by GA on 2015. 3. 20..
 */
public class Folder {
    int id;//폴더의 ID
    String name;
    String image;//폴더 대표 이미지의 경로
    String thumbNail_path;//폴더 대표 이미지의 썸네일 경로
    int picture_num;//특정 folder에 들어있는 사진의 개수
    int titleImageID;//폴더 대표 이미지의 아이디
    int isFixed;//고정 스토리인지, 아닌지

    public Folder() {
    }

    public Folder(String name) {
        this.name = name;
    }

    public Folder(int id, String name, String image) {
        this.id = id;
        this.name = name;
        this.image = image;
    }

    public Folder(String image, String name) {
        this.image = image;
        this.name = name;
    }

    public Folder(int id, String name, String image, String thumbNail_path,int picture_num, int titleImageID, int isFixed) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.thumbNail_path = thumbNail_path;
        this.picture_num = picture_num;
        this.titleImageID = titleImageID;
        this.isFixed = isFixed;
    }
    public int getPicture_num(){
        return picture_num;
    }

    public void setPicture_num(int picture_num){
        this.picture_num = picture_num;
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

    public String getImage(){
        return image;
    }

    public void setImage(String image){
        this.image = image;
    }


    public void setThumbNail_path(String thumbNail_name) {
        this.thumbNail_path = thumbNail_name;
    }

    public String getThumbNail_path() {
        return this.thumbNail_path;
    }

    public void setTitleImageID(int titleImageID){
        this.titleImageID = titleImageID;
    }
    public int getTitleImageID(){
        return this.titleImageID;
    }
    public void setIsFixed(int isFixed){
        this.isFixed = isFixed;
    }
    public int getIsFixed(){
        return this.isFixed;
    }
}
