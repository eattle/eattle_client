package com.eattle.phoket.Card;

import android.content.Context;

import com.dexafree.materialList.cards.SimpleCard;
import com.eattle.phoket.R;

/**
 * Created by GA on 2015. 7. 10..
 */
public class TransparentDividerCard extends SimpleCard {
    public TransparentDividerCard(Context context) {
        super(context);
    }

    @Override
    public int getLayout() {
        return R.layout.material_transparent_divider_card;
    }
}
