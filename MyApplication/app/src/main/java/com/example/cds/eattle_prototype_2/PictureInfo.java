package com.example.cds.eattle_prototype_2;

import java.io.File;

/**
 * Created by CDS on 15. 3. 17..
 */
public class PictureInfo {
    private long _ID;
    private String folderID;//사진이 속하게 될 폴더 ID
    private File picture;//사진의 File 객체

    PictureInfo(){}
    PictureInfo(long _ID,String folderID,String picturePath){
        this._ID = _ID;
        this.folderID = folderID;
        this.picture = new File(picturePath);
    }
    //get,set
    public long get_ID(){
        return this._ID;
    }
    public void set_ID(long _ID){
        this._ID=_ID;
    }
    public String getFolderID(){
        return this.folderID;
    }
    public void setFolderID(String folderID){
        this.folderID=folderID;
    }
    public File getPicture(){
        return this.picture;
    }
    public void setPicture(File picture){
        this.picture=picture;
    }
}

