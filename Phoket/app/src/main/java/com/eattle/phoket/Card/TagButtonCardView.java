package com.eattle.phoket.Card;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import com.dexafree.materialList.model.CardItemView;
import com.eattle.phoket.R;

/**
 * Created by GA on 2015. 5. 22..
 */
public class TagButtonCardView extends CardItemView<TagButtonCard> {

    // Default constructors
    public TagButtonCardView(Context context) {
        super(context);
    }

    public TagButtonCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TagButtonCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void build(TagButtonCard card) {
        super.build(card);
        ImageView tagImage = (ImageView)findViewById(R.id.tagImage);
        TextView tagName = (TextView) findViewById(R.id.tagName);

//        tagImage.setImageURI(Uri.parse(card.getTagImage()));
        tagName.setText(card.getTagName());
    }

}
