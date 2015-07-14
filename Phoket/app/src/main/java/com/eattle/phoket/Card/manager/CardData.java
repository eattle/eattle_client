package com.eattle.phoket.Card.manager;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by GA on 2015. 5. 15..
 */
public class CardData  implements Parcelable {
    // 카드 타입
    private int type;
    // 스토리 id
    private int data;
    //몇번째인지
    private int id;


    public CardData(Parcel in){
        type = in.readInt();
        data = in.readInt();
        id = in.readInt();
    }


    public CardData(CardData m){
        this.type = m.getType();
        this.data = m.getData();
        this.id = m.getId();
    }

    public CardData(int type, int data) {
        this.type = type;
        this.data = data;
    }

    public CardData(int type, int data, int id) {
        this.type = type;
        this.data = data;
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getData() {
        return data;
    }

    public void setData(int data) {
        this.data = data;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeInt(data);
        dest.writeInt(id);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public CardData createFromParcel(Parcel in) {
            return new CardData(in);
        }

        public CardData[] newArray(int size) {
            return new CardData[size];
        }
    };
}
