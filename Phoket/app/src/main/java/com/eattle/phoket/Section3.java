package com.eattle.phoket;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dexafree.materialList.cards.SimpleCard;
import com.dexafree.materialList.controller.RecyclerItemClickListener;
import com.dexafree.materialList.model.CardItemView;
import com.dexafree.materialList.view.MaterialListView;
import com.eattle.phoket.Card.TagButtonCard;
import com.eattle.phoket.helper.DatabaseHelper;
import com.eattle.phoket.Card.manager.CardData;
import com.eattle.phoket.model.Tag;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class Section3 extends Fragment {

    MaterialListView mListView;
    Context mContext;
    DatabaseHelper db;
    public MaterialListView getmListView(){
        return this.mListView;
    }
    public static Section3 newInstance() {

//    public static Section1 newInstance(String param1, String param2) {
        Section3 fragment = new Section3();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
        return fragment;
    }


    public Section3() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_section3, container, false);
        mContext = getActivity();

        mListView = (MaterialListView) root.findViewById(R.id.section_listview3);
        db = DatabaseHelper.getInstance(mContext);

        List<Tag> tags = db.getAllTags();

        int storiesNum = tags.size();
        for(int i = 0; i < storiesNum; i++){
            selectCard(tags.get(i).getId(), tags.get(i).getName(), tags.get(i).getColor());
        }


        mListView.addOnItemTouchListener(new RecyclerItemClickListener.OnItemClickListener() {

            @Override
            public void onItemClick(CardItemView view, int position) {

                CardData data = (CardData)view.getTag();
                Intent intent = new Intent(mContext, AlbumGridActivity.class);
                intent.putExtra("kind", data.getType());
                intent.putExtra("id", data.getData());
                mContext.startActivity(intent);

//                Log.d("CARD_TYPE", view.getTag().toString());
            }

            @Override
            public void onItemLongClick(CardItemView view, int position) {
                Log.d("LONG_CLICK", view.getTag().toString());
            }
        });


        return root;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
/*        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        mListener = null;
    }

    void selectCard(int tagId, String tagName, int color){
        //TODO:update날짜 비교해서 추가할지 말지 결정 or list안에서 비교해서 추가할지 말지 결정

        SimpleCard card;
        CardData data;
        card = new TagButtonCard(mContext);
        data = new CardData(CONSTANT.TAG, tagId);
        card.setTag(data);
        ((TagButtonCard)card).setTagName(tagName);
        ((TagButtonCard)card).setTagColor(color);

        mListView.add(card);

    }



}
