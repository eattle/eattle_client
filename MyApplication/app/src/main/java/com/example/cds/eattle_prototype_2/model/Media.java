package com.example.cds.eattle_prototype_2.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by GA on 2015. 3. 19..
 */
public class Media implements Parcelable {
    int id;             //전체에서의 사진 id **primary key**(media DB의 사진 ID와 관련 없음)
    int folder_id;   //폴더 id (속한 스토리의 id)
    String name;        //사진ID(media DB의 ID).jpg
    int year;           //년
    int month;          //월
    int day;            //일
    double latitude;       //위도
    double longitude;      //경도
    String placeName;       //위도,경도에 따른 장소명(없으면 "")
    String path;        //사진 경로



    public Media() {
    }

    public Media(Parcel in){
        id = in.readInt();
        folder_id = in.readInt();
        name = in.readString();
        year = in.readInt();
        month = in.readInt();
        day = in.readInt();
        latitude = in.readDouble();
        longitude = in.readDouble();
        placeName = in.readString();
        path = in.readString();
    }

    public Media(int id, int folder_id, String name, int year, int month, int day, double latitude, double longitude, String placeName, String path) {
        this.id = id;
        this.folder_id = folder_id;
        this.name = name;
        this.year = year;
        this.month = month;
        this.day = day;
        this.latitude = latitude;
        this.longitude = longitude;
        this.placeName = placeName;
        this.path = path;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getPlaceName() {

        return placeName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {

        return path;
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(folder_id);
        dest.writeString(name);
        dest.writeInt(year);
        dest.writeInt(month);
        dest.writeInt(day);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(placeName);
        dest.writeString(path);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Media createFromParcel(Parcel in) {
            return new Media(in);
        }

        public Media[] newArray(int size) {
            return new Media[size];
        }
    };
}
