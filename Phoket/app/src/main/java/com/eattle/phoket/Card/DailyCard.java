package com.eattle.phoket.Card;

import android.content.Context;

import com.dexafree.materialList.cards.SimpleCard;
import com.eattle.phoket.CONSTANT;
import com.eattle.phoket.R;

/**
 * Created by GA on 2015. 5. 14..
 */
public class DailyCard extends SimpleCard {
    String[] dailyImage;
    int count;


    public DailyCard(Context context) {
        super(context);
        dailyImage = new String[CONSTANT.BOUNDARY];
        count = 0;
    }

    public String getDailyImage(int n) {
        return dailyImage[n];
    }

    public void setDailyImage(String dailyImage, int n) {
        count++;
        this.dailyImage[n] = dailyImage;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public int getLayout(){
        return R.layout.material_daily_card;
    }
}
