package com.eattle.phoket.Card;

import android.content.Context;
import android.util.AttributeSet;
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
    public void build(TagsCard card) {
        super.build(card);
        switch (card.getCount()) {
            case 5:
                TextView tagText5 = (TextView)findViewById(R.id.tagtext5);
                tagText5.setText(card.getTagText(4));

            case 4:
                TextView tagText4 = (TextView)findViewById(R.id.tagtext4);
                tagText4.setText(card.getTagText(3));

            case 3:
                TextView tagText3 = (TextView)findViewById(R.id.tagtext3);
                tagText3.setText(card.getTagText(2));

            case 2:
                TextView tagText2 = (TextView)findViewById(R.id.tagtext2);
                tagText2.setText(card.getTagText(1));

            case 1:
                TextView tagText1 = (TextView)findViewById(R.id.tagtext1);
                tagText1.setText(card.getTagText(0));
                break;
            default:
                break;
        }
    }

}
