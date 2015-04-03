package com.example.cds.eattle_prototype_2;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by CDS on 15. 4. 4..
 */
public class DataForTagAddition implements Parcelable {
    int tagId;
    int mediaId;
    int folderId;
    String tagName;

    public DataForTagAddition(){

    }
    public DataForTagAddition(Parcel in){
        tagId = in.readInt();
        mediaId = in.readInt();
        folderId = in.readInt();
        tagName = in.readString();
    }
    public DataForTagAddition(int tagId,int mediaId,int folderId,String tagName){
        this.tagId=tagId;
        this.mediaId=mediaId;
        this.folderId=folderId;
        this.tagName=tagName;
    }
    public String getTagName(){
        return tagName;
    }
    public int getTagId(){
        return tagId;
    }
    public int getMediaId(){
        return mediaId;
    }
    public int getFolderId(){
        return folderId;
    }
    public void setTagName(String tagName){
        this.tagName=tagName;
    }
    public void setTagId(int tagId){
        this.tagId=tagId;
    }
    public void setMediaId(int mediaId){
        this.mediaId=mediaId;
    }
    public void setFolderId(int folderId){
        this.folderId=folderId;
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(tagId);
        dest.writeInt(mediaId);
        dest.writeInt(folderId);
        dest.writeString(tagName);
    }

        public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
            public DataForTagAddition createFromParcel(Parcel in) {
                return new DataForTagAddition(in);
            }

            public DataForTagAddition[] newArray(int size) {
                return new DataForTagAddition[size];
            }

        };
}
