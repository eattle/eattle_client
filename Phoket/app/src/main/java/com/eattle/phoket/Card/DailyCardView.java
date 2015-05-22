package com.eattle.phoket.Card;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.dexafree.materialList.model.CardItemView;
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
    public void build(DailyCard card) {
        super.build(card);
        ImageView dailyImage1 = (ImageView)findViewById(R.id.dailyImage1);
        ImageView dailyImage2 = (ImageView)findViewById(R.id.dailyImage2);
        ImageView dailyImage3 = (ImageView)findViewById(R.id.dailyImage3);

        dailyImage1.setImageURI(Uri.parse(card.getDailyImage1()));
        dailyImage2.setImageURI(Uri.parse(card.getDailyImage2()));
        dailyImage3.setImageURI(Uri.parse(card.getDailyImage3()));
    }

}

