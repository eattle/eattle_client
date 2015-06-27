package com.eattle.phoket.Card;

import android.content.Context;

import com.dexafree.materialList.cards.WelcomeCard;
import com.eattle.phoket.R;

/**
 * Created by GA_SOMA on 15. 6. 18..
 */
public class NotifyCard extends WelcomeCard{

    public NotifyCard(Context context) {
        super(context);
    }

    @Override
    public int getLayout(){
        return R.layout.material_notify_card;
    }

}
