package com.eattle.phoket.Card;

import android.content.Context;

import com.dexafree.materialList.cards.OnButtonPressListener;
import com.dexafree.materialList.cards.SimpleCard;
import com.eattle.phoket.CONSTANT;
import com.eattle.phoket.R;

/**
 * Created by GA on 2015. 5. 14..
 */
public class DailyCard extends SimpleCard {
    private int[] dailyId;
    private String[] dailyImage;
    private int count;
    private OnButtonPressListener mListener;


    public DailyCard(Context context) {
        super(context);
        dailyImage = new String[CONSTANT.BOUNDARY];
        dailyId = new int[CONSTANT.BOUNDARY];

        count = 0;
    }

    public int getDailyId(int n) {
        return dailyId[n];
    }
    public String getDailyImage(int n) {
        return dailyImage[n];
    }

    public void setDailyImage(int n, int dailyId, String dailyImage) {
        count++;
        this.dailyId[n] = dailyId;
        this.dailyImage[n] = dailyImage;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public OnButtonPressListener getOnButtonPressedListener() {
        return mListener;
    }

    public void setOnButtonPressedListener(OnButtonPressListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public int getLayout(){
        return R.layout.material_daily_card;
    }
}
