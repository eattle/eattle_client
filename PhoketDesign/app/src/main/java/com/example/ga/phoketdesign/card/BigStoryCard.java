package com.example.ga.phoketdesign.card;

import android.content.Context;

import com.dexafree.materialList.cards.SimpleCard;
import com.example.ga.phoketdesign.R;

/**
 * Created by GA on 2015. 5. 14..
 */
public class BigStoryCard extends SimpleCard {


    public BigStoryCard(Context context) {
        super(context);
        this.setSelectable(true);
    }

    @Override
    public int getLayout(){
        return R.layout.material_big_story_card;
    }
}
