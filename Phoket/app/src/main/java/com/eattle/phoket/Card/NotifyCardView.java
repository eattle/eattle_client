package com.eattle.phoket.Card;

import android.content.Context;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;

import com.dexafree.materialList.cards.internal.BaseTextCardItemView;

/**
 * Created by GA_SOMA on 15. 6. 18..
 */
public class NotifyCardView extends BaseTextCardItemView<NotifyCard> {
    public NotifyCardView(Context context) {
        super(context);
    }

    public NotifyCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NotifyCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void build(NotifyCard card) {
        super.build(card);

        StaggeredGridLayoutManager.LayoutParams sglp = (StaggeredGridLayoutManager.LayoutParams) this.getLayoutParams();
        sglp.setFullSpan(true);
        this.setLayoutParams(sglp);
    }

}
