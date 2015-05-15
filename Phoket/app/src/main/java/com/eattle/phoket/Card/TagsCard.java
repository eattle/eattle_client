package com.eattle.phoket.Card;

import android.content.Context;

import com.dexafree.materialList.cards.SimpleCard;
import com.eattle.phoket.R;

/**
 * Created by GA on 2015. 5. 14..
 */
public class TagsCard extends SimpleCard {


    public TagsCard(Context context) {
        super(context);
    }

    @Override
    public int getLayout() {
        return R.layout.material_tags_card;
    }
}
