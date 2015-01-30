package com.example.choi.eattle;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by choi on 2015-01-21.
 */
//관광지 정보들을 담고 있는 클래스
public class TourSpot implements Parcelable, Comparable<TourSpot> {
    private String name;
    private int resId;
    //위치 정보
    private double latitude; //위도
    private double longitutde; //경도
    //private int radius; //반경
    private double spotDistanceFromMe; // 현재 위치로 부터 얼마나 떨어져 있는지

    public TourSpot() {
    }

    public TourSpot(String name, int resId, double latitude, double longitutde) {
        this.name = name;
        this.resId = resId;
        this.latitude = latitude;
        this.longitutde = longitutde;
    }

    public TourSpot(Parcel src) {
        this.name = src.readString();
        this.resId = src.readInt();
        this.latitude = src.readDouble();
        this.longitutde = src.readDouble();
    }

    @SuppressWarnings("unchecked")
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

        public TourSpot createFromParcel(Parcel in) {
            return new TourSpot(in);
        }

        public TourSpot[] newArray(int size) {
            return new TourSpot[size];
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
    public int compareTo(TourSpot man) {
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

    void setSpotDistanceFromMe(double spotDistanceFromMe) {
        this.spotDistanceFromMe = spotDistanceFromMe;
    }
}