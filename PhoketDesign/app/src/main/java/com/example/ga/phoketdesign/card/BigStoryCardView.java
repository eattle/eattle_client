package com.example.ga.phoketdesign.card;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dexafree.materialList.model.CardItemView;
import com.example.ga.phoketdesign.R;

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

        Glide.with(storyImage.getContext())
                .load(R.drawable.cheese_1)
                .fitCenter()
                .into(storyImage);


        StaggeredGridLayoutManager.LayoutParams sglp = (StaggeredGridLayoutManager.LayoutParams) this.getLayoutParams();
        sglp.setFullSpan(true);
        this.setLayoutParams(sglp);
    }

}
