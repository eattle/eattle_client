package com.eattle.phoket.Card;

import android.content.Context;

import com.dexafree.materialList.cards.SimpleCard;
import com.eattle.phoket.R;

/**
 * Created by GA on 2015. 5. 14..
 */
public class ToPhoketCard extends SimpleCard {
    public ToPhoketCard(Context context) {
        super(context);
    }

    @Override
    public int getLayout() {
        return R.layout.material_to_phoket_card;
    }
}
