package com.eattle.phoket.Card.manager;

import android.content.Context;

import com.dexafree.materialList.cards.OnButtonPressListener;
import com.dexafree.materialList.cards.SimpleCard;
import com.dexafree.materialList.model.Card;
import com.dexafree.materialList.view.MaterialListView;
import com.eattle.phoket.CONSTANT;
import com.eattle.phoket.Card.BigStoryCard;
import com.eattle.phoket.Card.DailyCard;
import com.eattle.phoket.Card.HeaderCard;
import com.eattle.phoket.Card.SmallStoryCard;
import com.eattle.phoket.Card.TagButtonCard;
import com.eattle.phoket.Card.TagsCard;
import com.eattle.phoket.Card.ToPhoketCard;
import com.eattle.phoket.Card.TransparentDividerCard;
import com.eattle.phoket.model.Tag;

import java.util.List;

/**
 * Created by GA on 2015. 7. 11..
 */
public class CardManager {

    public static final int NOTHING = -1;
    public static final int FOLDER = 0;
    public static final int TAG = 1;
    public static final int DEFAULT_TAG = 2;
    public static final int TOPHOKET = 3;
    public static final int DAILY = 4;



    //빈 card를 add로 넣어서 다음 카드들이 fullspan되도록 함
    private static Card setupFullSpan(final Context context){
        TransparentDividerCard card = new TransparentDividerCard(context);
        CardData tag = new CardData(NOTHING, -1);
        card.setTag(tag);

        return card;
    }

    //빈 card를 addHeader로 넣어서 header가 없는 것 처럼 보이게 만듬
    private static Card setupNoHeader(final Context context){
        TransparentDividerCard card = new TransparentDividerCard(context);
        CardData tag = new CardData(NOTHING, -1);
        card.setTag(tag);

        return card;
    }

    public static void setHeaderList(MaterialListView list, final Context context){
        list.add(setupFullSpan(context));
    }


    //basic text header를 추가
    //다음에 그리드 item이 나올지도 모르므로 추가한 후 fullspan 해야 됨(header뒤에 추가)
    public static void setHeaderItem(MaterialListView list, final Context context, String title) {
        //list.add(setupFullSpan(context));
        HeaderCard card = new HeaderCard(context);
        CardData tag = new CardData(NOTHING, -1);
        card.setTag(tag);
        card.setTitle(title);

        list.addHeader(card);
        list.add(setupFullSpan(context));
    }

    //무조건 header가 없음
    //추가하기 전에 noheader로 만들어야함
    public static void setBigStoryItem(MaterialListView list, final Context context, int fId, String fName, String path, String date, int itemNum){

        list.addHeader(setupNoHeader(context));

        BigStoryCard card = new BigStoryCard(context);
        CardData tag = new CardData(FOLDER, fId);
        card.setTag(tag);
        card.setStoryName(fName);
        card.setTitleImage(path);
        card.setDate(date);
        card.setItemNum(itemNum);

        list.add(card);
    }

    public static void setDailyItem(MaterialListView list, final Context context, int fId, int order, String path){
        DailyCard card = new DailyCard(context);
        CardData tag = new CardData(DAILY, fId, order);
        card.setTag(tag);
        card.setDailyImage(path);

        list.add(card);
    }

    public static void setRelationTagsItem(MaterialListView list, final Context context, int tagNum, List<Tag> tagList, OnButtonPressListener listener){
        TagsCard card = new TagsCard(context);
        CardData tag = new CardData(NOTHING, -1);
        card.setTag(tag);
        for (int i = 0; i < tagNum; i++) {
            card.setTag(i, tagList.get(i).getId(), tagList.get(i).getName(), tagList.get(i).getColor());
        }
        card.setOnButtonPressedListener(listener);

        list.add(card);
    }

    public static void setRecommendItem(MaterialListView list, final Context context, int mId, String path){
        ToPhoketCard card = new ToPhoketCard(context);
        CardData tag = new CardData(TOPHOKET, mId);
        card.setTag(tag);
        card.setImage(path);

        list.add(card);
    }


    public static void setSmallStoryItem(MaterialListView list, final Context context, int fId, String fName, String path, String date, int itemNum){
        SmallStoryCard card = new SmallStoryCard(context);
        CardData tag = new CardData(FOLDER, fId);
        card.setTag(tag);
        card.setStoryName(fName);
        card.setTitleImage(path);
        card.setDate(date);
        card.setItemNum(itemNum);

        list.add(card);
    }


    public static void setTagItem(MaterialListView list, final Context context, int tId, String tName, int color){
        TagButtonCard card = new TagButtonCard(context);
        CardData tag = new CardData(TAG, tId);
        card.setTag(tag);
        ((TagButtonCard)card).setTagName(tName);
        ((TagButtonCard)card).setTagColor(color);

        list.add(card);
    }

}

