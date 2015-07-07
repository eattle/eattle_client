package com.eattle.phoket.Card;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dexafree.materialList.model.CardItemView;
import com.eattle.phoket.AlbumGridActivity;
import com.eattle.phoket.CONSTANT;
import com.eattle.phoket.R;

import java.util.zip.Inflater;

/**
 * Created by GA on 2015. 7. 3..
 */
public class OptionButtonCardView extends CardItemView<OptionButtonCard> {
    public OptionButtonCardView(Context context) {
        super(context);
    }

    public OptionButtonCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OptionButtonCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    public void build(final OptionButtonCard card) {
        super.build(card);
        LinearLayout layout = (LinearLayout)findViewById(R.id.options);
        int option[] = CONSTANT.OPTIONS[card.getOption()];
        for(int i = 0; i < option.length; i++){
            final LinearLayout optionButton = (LinearLayout) this.inflate(getContext(), R.layout.view_option_button, null);
            //TextView optionName = (TextView)optionButton.findViewById(R.id.optionButtonName);
            //ImageView optionIcon = (ImageView)optionButton.findViewById(R.id.optionButtonIcon);


            optionButton.setId(option[i]);
            layout.addView(optionButton);

            optionButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (card.getOnButtonPressedListener() != null) {
                        card.getOnButtonPressedListener().onButtonPressedListener(optionButton, card);
                    }
                }
            });
        }


        StaggeredGridLayoutManager.LayoutParams sglp = (StaggeredGridLayoutManager.LayoutParams) this.getLayoutParams();
        sglp.setFullSpan(true);
        this.setLayoutParams(sglp);

    }

}
