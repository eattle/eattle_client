package com.eattle.phoket.Card;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import com.dexafree.materialList.model.CardItemView;
import com.eattle.phoket.R;

/**
 * Created by GA on 2015. 5. 21..
 */
public class SmallStoryCardView extends CardItemView<SmallStoryCard> {

    // Default constructors
    public SmallStoryCardView(Context context) {
        super(context);
    }

    public SmallStoryCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SmallStoryCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void build(SmallStoryCard card) {
        super.build(card);
        ImageView storyImage = (ImageView)findViewById(R.id.smallStoryImage);
        TextView date = (TextView)findViewById(R.id.smallStoryDate);
        TextView storyName = (TextView) findViewById(R.id.smallStoryName);
        TextView itemNum = (TextView)findViewById(R.id.smallStoryItemNum);

        itemNum.setText(""+card.getItemNum());
        storyImage.setImageURI(Uri.parse(card.getTitleImage()));
        date.setText(card.getDate());
        storyName.setText(card.getStoryName());

    }

}
