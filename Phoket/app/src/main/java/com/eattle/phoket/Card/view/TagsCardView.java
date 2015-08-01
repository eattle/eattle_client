package com.eattle.phoket.Card.view;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dexafree.materialList.controller.RecyclerItemClickListener;
import com.dexafree.materialList.controller.StickyHeaderDecoration;
import com.dexafree.materialList.model.CardItemView;
import com.dexafree.materialList.view.MaterialListView;
import com.eattle.phoket.AlbumFullActivity;
import com.eattle.phoket.AlbumGridActivity;
import com.eattle.phoket.CONSTANT;
import com.eattle.phoket.Card.TagButtonCard;
import com.eattle.phoket.Card.TagsCard;
import com.eattle.phoket.Card.manager.CardData;
import com.eattle.phoket.Card.manager.CardManager;
import com.eattle.phoket.GUIDE;
import com.eattle.phoket.MainActivity;
import com.eattle.phoket.PopupPictureActivity;
import com.eattle.phoket.R;
import com.eattle.phoket.helper.DatabaseHelper;
import com.eattle.phoket.model.Media;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.animators.FadeInUpAnimator;

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

//
//        GridLayout gridList = (GridLayout)findViewById(R.id.tagList);
//
//        gridList.removeAllViews();
//        Log.d("adsad", "adsads");
//
//        FrameLayout tagCard = (FrameLayout) inflate(getContext(), R.layout.view_tag_card, this.find);
//
//        Log.d("adsad", "adsads");
////        TagButtonCardView tagCard = (TagButtonCardView) inflate(getContext(), R.layout.material_tag_button_card, null);
//
////        GridLayout.LayoutParams param =new GridLayout.LayoutParams();
////        param.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1);
////        tagCard.setLayoutParams(param);
//
//        int size = card.getTags().size();
//        for(int i = 0; i < size; i++) {
//            ImageView tagImage = (ImageView) tagCard.findViewById(R.id.tagImage);
//            TextView tagName = (TextView) tagCard.findViewById(R.id.tagName);
//            tagImage.setBackgroundColor(card.getTags().get(0).getColor());
//            tagName.setText(card.getTags().get(0).getName());
//
//            gridList.addView(tagCard);
//        }
//
//
        findViewById(R.id.tag5).setVisibility(INVISIBLE);
        findViewById(R.id.tag4).setVisibility(INVISIBLE);
        findViewById(R.id.tag3).setVisibility(INVISIBLE);
        findViewById(R.id.tag2).setVisibility(INVISIBLE);
        findViewById(R.id.tag1).setVisibility(INVISIBLE);

        switch (card.getCount()) {
            case 5:
                findViewById(R.id.tag5).setVisibility(VISIBLE);
                TextView tagText5 = (TextView)findViewById(R.id.tagtext5);
                tagText5.setText(card.getTagText(4));
                final FrameLayout tagButton5 = (FrameLayout)findViewById(R.id.tag5);
                tagButton5.setBackgroundColor(card.getTagColor(4));
                tagButton5.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (card.getOnButtonPressedListener() != null) {
                            card.getOnButtonPressedListener().onButtonPressedListener(tagButton5, card);
                        }
                    }
                });

            case 4:
                findViewById(R.id.tag4).setVisibility(VISIBLE);

                TextView tagText4 = (TextView)findViewById(R.id.tagtext4);
                tagText4.setText(card.getTagText(3));
                final FrameLayout tagButton4 = (FrameLayout)findViewById(R.id.tag4);
                tagButton4.setBackgroundColor(card.getTagColor(3));

                tagButton4.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (card.getOnButtonPressedListener() != null) {
                            card.getOnButtonPressedListener().onButtonPressedListener(tagButton4, card);
                        }
                    }
                });

            case 3:
                findViewById(R.id.tag3).setVisibility(VISIBLE);

                TextView tagText3 = (TextView)findViewById(R.id.tagtext3);
                tagText3.setText(card.getTagText(2));
                final FrameLayout tagButton3 = (FrameLayout)findViewById(R.id.tag3);
                tagButton3.setBackgroundColor(card.getTagColor(2));

                tagButton3.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (card.getOnButtonPressedListener() != null) {
                            card.getOnButtonPressedListener().onButtonPressedListener(tagButton3, card);
                        }
                    }
                });

            case 2:
                findViewById(R.id.tag2).setVisibility(VISIBLE);

                TextView tagText2 = (TextView)findViewById(R.id.tagtext2);
                tagText2.setText(card.getTagText(1));
                final FrameLayout tagButton2 = (FrameLayout)findViewById(R.id.tag2);
                tagButton2.setBackgroundColor(card.getTagColor(1));


                tagButton2.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (card.getOnButtonPressedListener() != null) {
                            card.getOnButtonPressedListener().onButtonPressedListener(tagButton2, card);
                        }
                    }
                });

            case 1:
                findViewById(R.id.tag1).setVisibility(VISIBLE);
                TextView tagText1 = (TextView)findViewById(R.id.tagtext1);
                tagText1.setText(card.getTagText(0));
                final FrameLayout tagButton1 = (FrameLayout)findViewById(R.id.tag1);
                tagButton1.setBackgroundColor(card.getTagColor(0));

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

        StaggeredGridLayoutManager.LayoutParams sglp = (StaggeredGridLayoutManager.LayoutParams) this.getLayoutParams();
        sglp.setFullSpan(true);
        this.setLayoutParams(sglp);
    }

}
