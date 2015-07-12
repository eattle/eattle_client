package com.eattle.phoket.Card.manager;

/**
 * Created by GA on 2015. 5. 15..
 */
public class CardData {
    // 카드 타입
    private int type;
    // 스토리 id
    private int data;
    //몇번째인지
    private int id;

    public CardData(int type, int data) {
        this.type = type;
        this.data = data;
    }

    public CardData(int type, int data, int id) {
        this.type = type;
        this.data = data;
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getData() {
        return data;
    }

    public void setData(int data) {
        this.data = data;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
