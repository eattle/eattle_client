package com.eattle.phoket.Card;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.dexafree.materialList.model.CardItemView;
import com.eattle.phoket.R;

/**
 * Created by GA on 2015. 5. 14..
 */
public class TagsCardView extends CardItemView<TagsCard> {
    public TagsCardView(Context context) {
        super(context);
    }

    public TagsCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TagsCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void build(final TagsCard card) {
        super.build(card);
        switch (card.getCount()) {
            case 5:
                TextView tagText5 = (TextView)findViewById(R.id.tagtext5);
                tagText5.setText(card.getTagText(4));
                final FrameLayout tagButton5 = (FrameLayout)findViewById(R.id.tag5);
                tagButton5.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (card.getOnButtonPressedListener() != null) {
                            card.getOnButtonPressedListener().onButtonPressedListener(tagButton5, card);
                        }
                    }
                });

            case 4:
                TextView tagText4 = (TextView)findViewById(R.id.tagtext4);
                tagText4.setText(card.getTagText(3));
                final FrameLayout tagButton4 = (FrameLayout)findViewById(R.id.tag4);
                tagButton4.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (card.getOnButtonPressedListener() != null) {
                            card.getOnButtonPressedListener().onButtonPressedListener(tagButton4, card);
                        }
                    }
                });

            case 3:
                TextView tagText3 = (TextView)findViewById(R.id.tagtext3);
                tagText3.setText(card.getTagText(2));
                final FrameLayout tagButton3 = (FrameLayout)findViewById(R.id.tag3);
                tagButton3.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (card.getOnButtonPressedListener() != null) {
                            card.getOnButtonPressedListener().onButtonPressedListener(tagButton3, card);
                        }
                    }
                });

            case 2:
                TextView tagText2 = (TextView)findViewById(R.id.tagtext2);
                tagText2.setText(card.getTagText(1));
                final FrameLayout tagButton2 = (FrameLayout)findViewById(R.id.tag2);
                tagButton2.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (card.getOnButtonPressedListener() != null) {
                            card.getOnButtonPressedListener().onButtonPressedListener(tagButton2, card);
                        }
                    }
                });

            case 1:
                TextView tagText1 = (TextView)findViewById(R.id.tagtext1);
                tagText1.setText(card.getTagText(0));
                final FrameLayout tagButton1 = (FrameLayout)findViewById(R.id.tag1);
                tagButton1.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (card.getOnButtonPressedListener() != null) {
                            card.getOnButtonPressedListener().onButtonPressedListener(tagButton1, card);
                        }
                    }
                });
                break;
            default:
                break;
        }
    }

}
