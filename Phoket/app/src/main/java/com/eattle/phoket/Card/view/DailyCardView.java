package com.eattle.phoket.Card.view;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.dexafree.materialList.model.CardItemView;
import com.eattle.phoket.Card.DailyCard;
import com.eattle.phoket.GUIDE;
import com.eattle.phoket.R;

/**
 * Created by GA on 2015. 5. 14..
 */
public class DailyCardView extends CardItemView<DailyCard> {


    public DailyCardView(Context context) {
        super(context);
    }

    public DailyCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DailyCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void build(final DailyCard card) {
        super.build(card);
        ImageView dailyImage = (ImageView) findViewById(R.id.dailyImage);
        if(!card.getDailyImage().contains("phoket")) {
            Glide.with(getContext())
                    .load(card.getDailyImage())
                    .into(dailyImage);
        }
        else{//가이드 중일때
            String asdf = "phoket1";
            Glide.with(getContext())
                    .load(GUIDE.guide_grid(card.getDailyImage()))
                    .into(dailyImage);
        }
        if(card.isSelecting())  setSelect();
        else                    setNoSelect();

    }

    public void setSelect(){
        ImageView pressed = (ImageView)findViewById(R.id.interactionEffect);
        pressed.setBackgroundResource(R.drawable.pressed_button);
        ImageView check = (ImageView)findViewById(R.id.check);
        check.setVisibility(VISIBLE);

    }

    public void setNoSelect(){
        ImageView pressed = (ImageView)findViewById(R.id.interactionEffect);
        pressed.setBackgroundResource(R.drawable.ripple_button);
        ImageView check = (ImageView)findViewById(R.id.check);
        check.setVisibility(INVISIBLE);

    }

}

