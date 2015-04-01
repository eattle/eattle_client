package com.example.cds.eattle_prototype_2.model;

/**
 * Created by GA on 2015. 3. 31..
 */
public class Media_Tag {
    long id;
    long tag_id;
    long media_id;

    public Media_Tag() {
    }

    public Media_Tag(long tag_id, long media_id) {
        tag_id = tag_id;
        media_id = media_id;
    }

    public Media_Tag(long id, long tag_id, long media_id) {
        this.id = id;
        tag_id = tag_id;
        media_id = media_id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTag_id() {
        return tag_id;
    }

    public void setTag_id(long tag_id) {
        tag_id = tag_id;
    }

    public long getMedia_id() {
        return media_id;
    }

    public void setMedia_id(long media_id) {
        media_id = media_id;
    }
}
