package com.eattle.phoket;


import android.app.Activity;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dexafree.materialList.cards.SimpleCard;
import com.dexafree.materialList.controller.RecyclerItemClickListener;
import com.dexafree.materialList.model.CardItemView;
import com.dexafree.materialList.view.MaterialListView;
import com.eattle.phoket.Card.SmallStoryCard;
import com.eattle.phoket.helper.DatabaseHelper;
import com.eattle.phoket.Card.manager.CardData;
import com.eattle.phoket.model.Folder;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class Section2 extends Fragment {

    MaterialListView mListView;
    Context mContext;
    DatabaseHelper db;
    public MaterialListView getmListView(){
        return this.mListView;
    }
    public static Section2 newInstance() {

//    public static Section1 newInstance(String param1, String param2) {
        Section2 fragment = new Section2();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
        return fragment;
    }

    public Section2() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_section2, container, false);
        mContext = getActivity();

        mListView = (MaterialListView) root.findViewById(R.id.section_listview2);
        db = DatabaseHelper.getInstance(mContext);

        List<Folder> stories = db.getAllFolders();

        int storiesNum = stories.size();
        for(int i = 0; i < storiesNum; i++){
            selectCard(stories.get(i).getImage(), stories.get(i).getThumbNail_path(), stories.get(i).getName(), stories.get(i).getId(), stories.get(i).getPicture_num());
        }

        //mListView.add(card);

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

    void selectCard(String path, String thumbNailPath, String storyName, int folderID, int pictureNum){
        //TODO:update날짜 비교해서 추가할지 말지 결정 or list안에서 비교해서 추가할지 말지 결정

        SimpleCard card;
        CardData data;
        if(pictureNum > CONSTANT.BOUNDARY){
            card = new SmallStoryCard(mContext);
            data = new CardData(CONSTANT.FOLDER, folderID);
            card.setTag(data);
            ((SmallStoryCard)card).setStoryName(CONSTANT.convertFolderNameToStoryName(storyName));

            if(thumbNailPath == null)
                ((SmallStoryCard)card).setTitleImage(path);
            else
                ((SmallStoryCard)card).setTitleImage(thumbNailPath);

            ((SmallStoryCard)card).setDate(CONSTANT.convertFolderNameToDate(storyName));
            ((SmallStoryCard)card).setItemNum(pictureNum);
            mListView.add(card);
            //TODO:folder id로 db검색해서 폴더에 걸린 tag 찾아오기

        }
    }


}
