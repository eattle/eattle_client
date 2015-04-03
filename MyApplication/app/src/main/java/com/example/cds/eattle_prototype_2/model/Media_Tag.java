package com.example.cds.eattle_prototype_2.model;

/**
 * Created by GA on 2015. 3. 31..
 */
public class Media_Tag {
    int id;
    int tag_id;
    int media_id;

    public Media_Tag() {
    }

    public Media_Tag(int tag_id, int media_id) {
        tag_id = tag_id;
        media_id = media_id;
    }

    public Media_Tag(int id, int tag_id, int media_id) {
        this.id = id;
        tag_id = tag_id;
        media_id = media_id;
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
        tag_id = tag_id;
    }

    public int getMedia_id() {
        return media_id;
    }

    public void setMedia_id(int media_id) {
        media_id = media_id;
    }
}
