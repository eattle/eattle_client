package com.eattle.phoket.Card.view;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dexafree.materialList.model.CardItemView;
import com.eattle.phoket.CONSTANT;
import com.eattle.phoket.Card.BigStoryCard;
import com.eattle.phoket.MainActivity;
import com.eattle.phoket.R;
import com.eattle.phoket.helper.DatabaseHelper;

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

        itemNum.setText("" + card.getItemNum());
        //storyImage.setImageURI(Uri.parse(card.getTitleImage()));
        DatabaseHelper db = DatabaseHelper.getInstance(getContext());
        if(db != null){
            if(db.getGuide() == 1){//가이드가 끝났을 때
                Glide.with(getContext())
                        .load(card.getTitleImage())
                        .override(CONSTANT.screenWidth,CONSTANT.screenHeight)
                        .into(storyImage);
            }
            else if(db.getGuide() == 0){//가이드 중일때
                Glide.with(getContext())
                        .load(R.mipmap.phoket1)
                        .override(CONSTANT.screenWidth,CONSTANT.screenHeight)
                        .into(storyImage);
            }
        }

        date.setText(card.getDate());
        storyName.setText(card.getStoryName());

        if(card.isSelecting())  setSelect();
        else                    setNoSelect();

        StaggeredGridLayoutManager.LayoutParams sglp = (StaggeredGridLayoutManager.LayoutParams) this.getLayoutParams();
        sglp.setFullSpan(true);
        this.setLayoutParams(sglp);
    }

    public void setSelect(){
        ImageView pressed = (ImageView)findViewById(R.id.interactionEffect);
        pressed.setBackgroundResource(R.drawable.pressed_button);
        ImageView check = (ImageView)findViewById(R.id.check);
        check.setVisibility(VISIBLE);
    }

    public void setNoSelect(){
        ImageView pressed = (ImageView)findViewById(R.id.interactionEffect);
        pressed.setBackgroundResource(R.drawable.ripple_button);
        ImageView check = (ImageView)findViewById(R.id.check);
        check.setVisibility(INVISIBLE);
    }

}
