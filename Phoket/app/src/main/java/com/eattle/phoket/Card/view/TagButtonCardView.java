package com.eattle.phoket.Card.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import com.dexafree.materialList.model.CardItemView;
import com.eattle.phoket.Card.TagButtonCard;
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
        tagImage.setBackgroundColor(card.getTagColor());
        tagName.setText(card.getTagName());

        if(card.isSelecting())  setSelect();
        else                    setNoSelect();

    }


    public void setSelect(){
        ImageView pressed = (ImageView)findViewById(R.id.interactionEffect);
        pressed.setBackgroundResource(R.drawable.pressed_button);
    }

    public void setNoSelect(){
        ImageView pressed = (ImageView)findViewById(R.id.interactionEffect);
        pressed.setBackgroundResource(R.drawable.ripple_button);
    }


}
