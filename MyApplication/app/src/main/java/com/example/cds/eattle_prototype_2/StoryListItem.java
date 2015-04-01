package com.example.cds.eattle_prototype_2;

/**
 * Created by CDS on 15. 4. 1..
 */
public class StoryListItem {
    private String imgID;//이미지 이름
    private String name;//스토리 이름
    private long folderID;//특정 스토리에 해당하는 폴더 아이디

    public StoryListItem(){

    }

    public StoryListItem(String imgID,String name,long folderID){
        this.imgID = imgID;
        this.name = name;
        this.folderID = folderID;
    }
    public void setFolderID(long folderID) {
        this.folderID = folderID;
    }

    public long getFolderID() {

        return folderID;
    }

    public void setImgID(String imgPath) {
        this.imgID = imgPath;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImgID() {

        return imgID;
    }

    public String getName() {
        return name;
    }
}
