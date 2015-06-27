package com.eattle.phoket.Card;

import android.content.Context;

import com.dexafree.materialList.cards.OnButtonPressListener;
import com.dexafree.materialList.cards.SimpleCard;
import com.eattle.phoket.R;

/**
 * Created by GA on 2015. 5. 14..
 */
public class TagsCard extends SimpleCard {
    private String[] tagText;
    private int[] tagId;
    private int[] tagColor;
    private int count;
    private OnButtonPressListener mListener;


    public TagsCard(Context context) {
        super(context);
        tagText = new String[5];
        tagId = new int[5];
        tagColor = new int[5];
        count = 0;

    }

    public String getTagText(int n) {
        return tagText[n];
    }
    public int getTagId(int n) {
        return tagId[n];
    }
    public int getTagColor(int n) {
        return tagColor[n];
    }


    public void setTag(int n, int tagId, String tagText, int tagColor) {
        count++;
        this.tagId[n] = tagId;
        this.tagText[n] = tagText;
        this.tagColor[n] = tagColor;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public OnButtonPressListener getOnButtonPressedListener() {
        return mListener;
    }

    public void setOnButtonPressedListener(OnButtonPressListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public int getLayout() {
        return R.layout.material_tags_card;
    }
}
