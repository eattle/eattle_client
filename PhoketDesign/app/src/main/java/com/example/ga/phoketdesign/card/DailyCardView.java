package com.example.ga.phoketdesign.card;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.dexafree.materialList.model.CardItemView;
import com.example.ga.phoketdesign.R;

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
        ImageView dailyImage = (ImageView)findViewById(R.id.dailyImage1);


        Glide.with(dailyImage.getContext())
                .load(R.drawable.cheese_2)
                .fitCenter()
                .into(dailyImage);

        if(card.isSelecting())  setSelect();
        else                    setNoSelect();
    }


    public void setSelect(){
        ImageView pressed = (ImageView)findViewById(R.id.imageView2);
        pressed.setImageResource(R.drawable.pressed_button);
    }

    public void setNoSelect(){
        ImageView pressed = (ImageView)findViewById(R.id.imageView2);
        pressed.setImageResource(R.drawable.ripple_button);
    }

}

