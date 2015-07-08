package com.example.ga.phoketdesign;

/**
 * Created by GA on 2015. 7. 8..
 */
public class CardTag {
    public final static int CARDTYPE_HEADER = 0;
    public final static int CARDTYPE_BIGSTORY = 1;
    public final static int CARDTYPE_DAILY = 2;

    int cardType;

    public int getCardType() {
        return cardType;
    }

    public void setCardType(int cardType) {
        this.cardType = cardType;
    }
}
