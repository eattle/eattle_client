package com.eattle.phoket.model;

/**
 * Created by GA on 2015. 7. 29..
 */
public class Folder_Tag {
    int id;
    int tag_id;
    int folder_id;
    int count;

    public Folder_Tag() {
    }

    public Folder_Tag(int tag_id, int folder_id) {
        this.tag_id = tag_id;
        this.folder_id = folder_id;
    }

    public Folder_Tag(int id, int tag_id, int folder_id) {
        this.id = id;
        this.tag_id = tag_id;
        this.folder_id = folder_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTag_id() {
        return tag_id;
    }

    public void setTag_id(int tag_id) {
        this.tag_id = tag_id;
    }

    public int getFolder_id() {
        return folder_id;
    }

    public void setFolder_id(int folder_id) {
        this.folder_id = folder_id;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
