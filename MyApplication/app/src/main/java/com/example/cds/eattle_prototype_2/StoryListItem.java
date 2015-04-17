package com.example.cds.eattle_prototype_2;

import com.example.cds.eattle_prototype_2.device.BlockDevice;

/**
 * Created by CDS on 15. 4. 1..
 */
public class StoryListItem {
    private String imgID;//이미지 이름
    private String name;//스토리 이름
    private int folderID;//특정 스토리에 해당하는 폴더 아이디
    private int pictureNumInStory;//특정 스토리에 있는 사진들의 개수
    private BlockDevice blockDevice;//USB에서 사진을 읽어오기 위해

    public StoryListItem(){

    }

    public StoryListItem(String imgID,String name,int folderID,int pictureNumInStory){
        this.imgID = imgID;
        this.name = name;
        this.folderID = folderID;
        this.pictureNumInStory = pictureNumInStory;
    }

    public StoryListItem(String imgID,String name,int folderID,int pictureNumInStory,BlockDevice blockDevice){
        this.imgID = imgID;
        this.name = name;
        this.folderID = folderID;
        this.pictureNumInStory = pictureNumInStory;
        this.blockDevice = blockDevice;
    }

    public BlockDevice getBlockDevice(){
        return blockDevice;
    }

    public void setBlockDevice(BlockDevice blockDevice){
        this.blockDevice = blockDevice;
    }

    public int getPictureNumInStory(){
        return pictureNumInStory;
    }

    public void setPictureNumInStory(int pictureNumInStory){
        this.pictureNumInStory = pictureNumInStory;
    }

    public void setFolderID(int folderID) {
        this.folderID = folderID;
    }

    public int getFolderID() {

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
