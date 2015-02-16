package com.example.choi.eattle_prototype;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by choi on 2015-01-21.
 */
//관광지 정보들을 담고 있는 클래스 - DB에서 읽어들인 데이터로 초기화 한다.
public class TouristSpotInfo implements Parcelable, Comparable<TouristSpotInfo> {
    private String name;
    private int resId;
    //위치 정보
    private double latitude; //위도
    private double longitutde; //경도
    //private int radius; //반경
    private double spotDistanceFromMe; // 현재 위치로 부터 얼마나 떨어져 있는지
    private String[] detailedInfo;
    private int visit;//해당 관광지를 방문하지 않았으면0, 방문했으면 1
    private int favorite;//즐겨찾기가 아니면 0, 맞으면 1
    public TouristSpotInfo() {
    }

    //depth2를 위한 생성자
    public TouristSpotInfo(String name, int resId, double latitude, double longitutde) {
        this.name = name;
        this.resId = resId;
        this.latitude = latitude;
        this.longitutde = longitutde;
        this.visit = 0;
        this.favorite = 0;
    }

    //depth1을 위한 생성자
    public TouristSpotInfo(String name, int resId, double latitude, double longitutde, String spotInfoID) {
        this.name = name;
        this.resId = resId;
        this.latitude = latitude;
        this.longitutde = longitutde;
        this.detailedInfo =  spotInfoID.split("\\.");// 마침표(.)단위로 파싱한다.
        this.visit = 0;
        this.favorite = 0;
    }

    public TouristSpotInfo(Parcel src) {
        this.name = src.readString();
        this.resId = src.readInt();
        this.latitude = src.readDouble();
        this.longitutde = src.readDouble();
    }

    @SuppressWarnings("unchecked")
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

        public TouristSpotInfo createFromParcel(Parcel in) {
            return new TouristSpotInfo(in);
        }

        public TouristSpotInfo[] newArray(int size) {
            return new TouristSpotInfo[size];
        }

    };


    public int describeContents() {
        return 0;
    }

    /**
     * 데이터를 Parcel 객체로 쓰기
     */

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeInt(this.resId);
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitutde);
    }

    //spotDistanceFromMe를 기준으로 오름차순으로 정렬하기 위한 함수
    public int compareTo(TouristSpotInfo man) {
        if (this.spotDistanceFromMe < man.spotDistanceFromMe) {
            return -1;
        } else if (this.spotDistanceFromMe == man.spotDistanceFromMe) {
            return 0;
        } else {
            return 1;
        }
    }

    // get, set
    String getName() {
        return this.name;
    }

    int getResId() {
        return this.resId;
    }

    double getLatitude() {
        return this.latitude;
    }

    double getLongitutde() {
        return this.longitutde;
    }

    double getSpotDistanceFromMe() {
        return this.spotDistanceFromMe;
    }

    String[] getDetailedInfo(){
        return this.detailedInfo;
    }

    int getVisit(){
        return this.visit;
    }
    void setVisit(int visit){
        this.visit = visit;
    }
    int getFavorite(){ return this.favorite;}
    void setFavorite(int favorite){this.favorite = favorite;}

    void setSpotDistanceFromMe(double spotDistanceFromMe) {
        this.spotDistanceFromMe = spotDistanceFromMe;
    }
}