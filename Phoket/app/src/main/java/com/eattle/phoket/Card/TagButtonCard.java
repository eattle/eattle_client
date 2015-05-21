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

    public TagButtonCard(Context context) {
        super(context);
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

    @Override
    public int getLayout() {
        return R.layout.material_tag_button_card;
    }
}
