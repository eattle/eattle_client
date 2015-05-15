package com.eattle.phoket.Card;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import com.dexafree.materialList.model.CardItemView;
import com.eattle.phoket.R;

/**
 * Created by GA on 2015. 5. 14..
 */
public class BigStoryCardView extends CardItemView<BigStoryCard> {

    // Default constructors
    public BigStoryCardView(Context context) {
        super(context);
    }

    public BigStoryCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BigStoryCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void build(BigStoryCard card) {
        super.build(card);
        ImageView storyImage = (ImageView)findViewById(R.id.bigStoryImage);
        TextView date = (TextView)findViewById(R.id.bigStoryDate);
        TextView storyName = (TextView) findViewById(R.id.bigStoryName);
        TextView itemNum = (TextView)findViewById(R.id.bigStoryItemNum);

        itemNum.setText(""+card.getItemNum());
        storyImage.setImageURI(Uri.parse(card.getTitleImage()));
        date.setText(card.getDate());
        storyName.setText(card.getStoryName());
    }

}
