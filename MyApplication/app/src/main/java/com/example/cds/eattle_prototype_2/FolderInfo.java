package com.example.cds.eattle_prototype_2;

/**
 * Created by CDS on 15. 3. 17..
 */
public class FolderInfo {
    private String folderID;
    private String folderName;

    FolderInfo(){}
    FolderInfo(String folderID,String folderName){
        this.folderID=folderID;
        this.folderName=folderName;
    }

    //get,set
    public String getFolderID(){
        return this.folderID;
    }
    public void setFolderID(String folderID){
        this.folderID=folderID;
    }
    public String getFolderName(){
        return this.folderName;
    }
    public void setFolderName(String folderName){
        this.folderName=folderName;
    }
}
