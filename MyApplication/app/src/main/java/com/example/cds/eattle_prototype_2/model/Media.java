package com.example.cds.eattle_prototype_2.model;

/**
 * Created by GA on 2015. 3. 19..
 */
public class Media {
    long id;             //전체에서의 사진 id **primary key**
    int folder_id;   //폴더 id (속한 스토리의 id)
    String name;        //사진 경로
    int year;           //년
    int month;          //월
    int day;            //일
    double latitude;       //위도
    double longitude;      //경도
    String tag;         //추가 태그

    public Media() {
    }

    public Media(long id, int folder_id, String name, int year, int month, int day, double latitude, double longitude, String tag) {
        this.id = id;
        this.folder_id = folder_id;
        this.name = name;
        this.year = year;
        this.month = month;
        this.day = day;
        this.latitude = latitude;
        this.longitude = longitude;
        this.tag = tag;
    }

    // 위치 정보가 존재할 경우
    public Media(int folder_id, String name, int year, int month, int day, double latitude, double longitude, String tag) {
        this.folder_id = folder_id;
        this.name = name;
        this.year = year;
        this.month = month;
        this.day = day;
        this.latitude = latitude;
        this.longitude = longitude;
        this.tag = tag;
    }

    // 위치 정보가 존재하지 않을 경우
    public Media(int folder_id, String name, int year, int month, int day) {
        this.folder_id = folder_id;
        this.name = name;
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getFolder_id() {
        return folder_id;
    }

    public void setFolder_id(int folder_id) {
        this.folder_id = folder_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
