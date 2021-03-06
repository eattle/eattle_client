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
import com.eattle.phoket.Card.SmallStoryCard;
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
//        TextView date = (TextView)findViewById(R.id.smallStoryDate);
        TextView storyName = (TextView) findViewById(R.id.smallStoryName);
        TextView itemNum = (TextView)findViewById(R.id.smallStoryItemNum);

        itemNum.setText(""+card.getItemNum());
        //storyImage.setImageURI(Uri.parse(card.getTitleImage()));

        Glide.with(getContext())
                .load(card.getTitleImage())
                .override(CONSTANT.screenWidth, CONSTANT.screenHeight/2)
                .into(storyImage);


//        date.setText(card.getDate());
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
    }

    public void setNoSelect(){
        ImageView pressed = (ImageView)findViewById(R.id.interactionEffect);
        pressed.setBackgroundResource(R.drawable.ripple_button);
    }

}
