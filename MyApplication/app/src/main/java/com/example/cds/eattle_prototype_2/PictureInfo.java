package com.example.cds.eattle_prototype_2;

/**
 * Created by CDS on 15. 3. 17..
 */
public class PictureInfo {
    private long _ID;
    private String folderID;//사진이 속하게 될 폴더 ID

    PictureInfo(){}
    PictureInfo(long _ID,String folderID){
        this._ID = _ID;
        this.folderID = folderID;
    }
    //get,set
    public long get_ID(){
        return this._ID;
    }
    void set_ID(long _ID){
        this._ID=_ID;
    }
    public String getFolderID(){
        return this.folderID;
    }
    void setFolderID(String folderID){
        this.folderID=folderID;
    }

}

