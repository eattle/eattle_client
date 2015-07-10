package com.example.ga.phoketdesign.card;

import android.content.Context;

import com.dexafree.materialList.cards.SimpleCard;
import com.example.ga.phoketdesign.R;

/**
 * Created by GA on 2015. 7. 7..
 */
public class HeaderCard extends SimpleCard {
    String title;

    public HeaderCard(Context context) {
        super(context);
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public int getLayout() {
        return R.layout.material_title_card;
    }
}
