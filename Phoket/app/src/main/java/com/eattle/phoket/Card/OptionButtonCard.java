package com.eattle.phoket.Card;

import android.content.Context;

import com.dexafree.materialList.cards.OnButtonPressListener;
import com.dexafree.materialList.cards.SimpleCard;
import com.eattle.phoket.R;

/**
 * Created by GA on 2015. 7. 3..
 */
public class OptionButtonCard extends SimpleCard{

    int option = 0;
    private OnButtonPressListener mListener;



    public OptionButtonCard(Context context) {
        super(context);
    }

    public int getOption() {
        return option;
    }

    public void setOption(int option) {
        this.option = option;
    }

    public OnButtonPressListener getOnButtonPressedListener() {
        return mListener;
    }

    public void setOnButtonPressedListener(OnButtonPressListener mListener) {
        this.mListener = mListener;
    }


    @Override
    public int getLayout() {
        return R.layout.material_option_button_card;
    }
}
