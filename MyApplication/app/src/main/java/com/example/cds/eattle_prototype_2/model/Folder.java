package com.example.cds.eattle_prototype_2.model;

/**
 * Created by GA on 2015. 3. 20..
 */
public class Folder {
    int id;
    String name;
    String image;

    public Folder() {
    }

    public Folder(String name) {
        this.name = name;
    }

    public Folder(int id, String name, String image) {
        this.id = id;
        this.name = name;
        this.image = image;
    }

    public Folder(String image, String name) {
        this.image = image;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage(){
        return image;
    }

    public void setImage(String image){
        this.image = image;
    }
}
