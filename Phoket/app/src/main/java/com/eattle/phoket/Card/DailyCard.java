package com.eattle.phoket.Card;

import android.content.Context;

import com.dexafree.materialList.cards.SimpleCard;
import com.eattle.phoket.R;

/**
 * Created by GA on 2015. 5. 14..
 */
public class DailyCard extends SimpleCard {
    String dailyImage1;
    String dailyImage2;
    String dailyImage3;
    String count;


    public DailyCard(Context context) {
        super(context);
    }

    public String getDailyImage1() {
        return dailyImage1;
    }

    public void setDailyImage1(String dailyImage1) {
        this.dailyImage1 = dailyImage1;
    }

    public String getDailyImage2() {
        return dailyImage2;
    }

    public void setDailyImage2(String dailyImage2) {
        this.dailyImage2 = dailyImage2;
    }

    public String getDailyImage3() {
        return dailyImage3;
    }

    public void setDailyImage3(String dailyImage3) {
        this.dailyImage3 = dailyImage3;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    @Override
    public int getLayout(){
        return R.layout.material_daily_card;
    }
}
