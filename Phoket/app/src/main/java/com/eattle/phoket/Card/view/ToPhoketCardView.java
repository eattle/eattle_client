package com.eattle.phoket.Card.view;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.dexafree.materialList.model.CardItemView;
import com.eattle.phoket.CONSTANT;
import com.eattle.phoket.Card.ToPhoketCard;
import com.eattle.phoket.R;
import com.eattle.phoket.helper.DatabaseHelper;

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
        //toPhoketImage.setImageURI(Uri.parse(card.getImage()));

        DatabaseHelper db = DatabaseHelper.getInstance(getContext());
        if(db != null){
            if(db.getGuide() == 1){//가이드가 끝났을 때
                Glide.with(getContext())
                        .load(card.getImage())
                        .into(toPhoketImage);
            }
            else if(db.getGuide() == 0){//가이드 중일때
                Glide.with(getContext())
                        .load(R.mipmap.phoket2)
                        .into(toPhoketImage);
            }
        }
        StaggeredGridLayoutManager.LayoutParams sglp = (StaggeredGridLayoutManager.LayoutParams) this.getLayoutParams();
        sglp.setFullSpan(true);
        this.setLayoutParams(sglp);
    }

}
