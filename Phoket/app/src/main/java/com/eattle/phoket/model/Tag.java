package com.eattle.phoket.model;

/**
 * Created by GA on 2015. 3. 31..
 */
public class Tag {
    int id;
    String name;
    int color;

    public Tag() {
    }

    public Tag(int id, String name, int color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }

    public Tag(String name) {
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

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
