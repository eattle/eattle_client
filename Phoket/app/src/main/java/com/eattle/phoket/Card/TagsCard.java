package com.eattle.phoket.Card;

import android.content.Context;

import com.dexafree.materialList.cards.SimpleCard;
import com.eattle.phoket.R;

/**
 * Created by GA on 2015. 5. 14..
 */
public class TagsCard extends SimpleCard {
    String[] tagText;
    int count;

    public TagsCard(Context context) {
        super(context);
        tagText = new String[5];
        count = 0;

    }

    public String getTagText(int n) {
        return tagText[n];
    }

    public void setTagText(String tagText, int n) {
        count++;
        this.tagText[n] = tagText;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }


    @Override
    public int getLayout() {
        return R.layout.material_tags_card;
    }
}
