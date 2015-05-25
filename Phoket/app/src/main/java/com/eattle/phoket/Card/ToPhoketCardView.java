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
public class ToPhoketCardView extends CardItemView<ToPhoketCard> {

    public ToPhoketCardView(Context context) {
        super(context);
    }

    public ToPhoketCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ToPhoketCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void build(ToPhoketCard card) {
        super.build(card);
        ImageView toPhoketImage = (ImageView)findViewById(R.id.toPhoketImage);
        toPhoketImage.setImageURI(Uri.parse(card.getImage()));
    }

}
