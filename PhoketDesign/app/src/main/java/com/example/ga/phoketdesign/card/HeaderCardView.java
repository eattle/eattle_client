package com.example.ga.phoketdesign.card;

import android.content.Context;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;

import com.dexafree.materialList.model.CardItemView;

/**
 * Created by GA on 2015. 7. 7..
 */
public class HeaderCardView extends CardItemView<HeaderCard> {

    public HeaderCardView(Context context) {
        super(context);
    }

    public HeaderCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HeaderCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    public void build(HeaderCard card) {
        super.build(card);
        StaggeredGridLayoutManager.LayoutParams sglp = (StaggeredGridLayoutManager.LayoutParams) this.getLayoutParams();
        sglp.setFullSpan(true);
        this.setLayoutParams(sglp);
    }

}
