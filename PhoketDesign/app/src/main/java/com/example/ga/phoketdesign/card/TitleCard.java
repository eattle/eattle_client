package com.example.ga.phoketdesign.card;

import android.content.Context;

import com.dexafree.materialList.cards.SimpleCard;
import com.example.ga.phoketdesign.R;

/**
 * Created by GA on 2015. 7. 7..
 */
public class TitleCard extends SimpleCard {
    public TitleCard(Context context) {
        super(context);
    }

    @Override
    public int getLayout() {
        return R.layout.material_title_card;
    }
}
