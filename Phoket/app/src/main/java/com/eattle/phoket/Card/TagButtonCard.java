package com.eattle.phoket.Card;

import android.content.Context;

import com.dexafree.materialList.cards.SimpleCard;
import com.eattle.phoket.R;

/**
 * Created by GA on 2015. 5. 22..
 */
public class TagButtonCard extends SimpleCard {

    String tagName;
    String tagImage;
    int tagColor;

    public TagButtonCard(Context context) {
        super(context);
        this.setSelectable(true);

    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getTagImage() {
        return tagImage;
    }

    public void setTagImage(String tagImage) {
        this.tagImage = tagImage;
    }

    public int getTagColor() {
        return tagColor;
    }

    public void setTagColor(int tagColor) {
        this.tagColor = tagColor;
    }

    @Override
    public int getLayout() {
        return R.layout.material_tag_button_card;
    }
}
