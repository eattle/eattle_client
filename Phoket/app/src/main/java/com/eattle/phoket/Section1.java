package com.eattle.phoket;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dexafree.materialList.cards.SimpleCard;
import com.dexafree.materialList.cards.WelcomeCard;
import com.dexafree.materialList.controller.RecyclerItemClickListener;
import com.dexafree.materialList.model.CardItemView;
import com.dexafree.materialList.view.MaterialListView;
import com.eattle.phoket.Card.BigStoryCard;
import com.eattle.phoket.Card.DailyCard;
import com.eattle.phoket.Card.TagsCard;
import com.eattle.phoket.Card.ToPhoketCard;
import com.eattle.phoket.helper.DatabaseHelper;
import com.eattle.phoket.model.CardData;
import com.eattle.phoket.model.Folder;
import com.eattle.phoket.model.Media;
import com.eattle.phoket.model.Tag;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Section1.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Section1#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Section1 extends Fragment {

    MaterialListView mListView;
    Context mContext;
    DatabaseHelper db;

    //private String mParam1;
    //private String mParam2;
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Section1.
     */
    // TODO: Rename and change types and number of parameters
    public static Section1 newInstance() {

//    public static Section1 newInstance(String param1, String param2) {
        Section1 fragment = new Section1();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
        return fragment;
    }

    public Section1() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
    }

/*    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("curChoice", mCurCheckPosition);
    }*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_section1, container, false);
        mContext = getActivity();

        mListView = (MaterialListView) root.findViewById(R.id.section_listview1);
        db = DatabaseHelper.getInstance(mContext);

        List<Folder> stories = db.getAllFolders();

        int storiesNum = stories.size();
        for(int i = 0; i < storiesNum; i++){
            selectCard(stories.get(i).getThumbNail_name(), stories.get(i).getName(), stories.get(i).getId(), stories.get(i).getPicture_num());
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     *
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }
     */


    SimpleCard getCard(int cardNum){
        SimpleCard card;

        switch (cardNum){
            case CONSTANT.BIGSTORYCARD:
                card = new BigStoryCard(mContext);
                card.setTag("BIG_STORY_CARD");
                return card;
            case CONSTANT.DAILYCARD:
                card = new DailyCard(mContext);
                card.setTag("DAILY_CARD");
                return card;
            case CONSTANT.TAGSCARD:
                card = new TagsCard(mContext);
                card.setTag("TAGS_CARD");
                return card;
            case CONSTANT.TOPHOKETCARD:
                card = new ToPhoketCard(mContext);
                card.setTag("TO_PHOKET_CARD");
                return card;
            case CONSTANT.NOTIFICARD:
                card = new WelcomeCard(mContext);
                card.setTag("WELCOME_CARD");
            default:
                card = new BigStoryCard(mContext);
                card.setTag("BIG_STORY_CARD");
                return card;
        }
    }

    void selectCard(String thumbNailID, String storyName, int folderID, int pictureNum){
        //TODO:update날짜 비교해서 추가할지 말지 결정 or list안에서 비교해서 추가할지 말지 결정

        SimpleCard card;
        CardData data;
        if(pictureNum <= CONSTANT.BOUNDARY){
            List<Media> dailyMedia = db.getAllMediaByFolder(folderID);
            card = new DailyCard(mContext);
            //data = new CardData();
            card.setTag("dailyCard");
            for(int i = 0; i < pictureNum; i++){
                ((DailyCard)card).setDailyImage(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/" + "thumbnail" + "/" + dailyMedia.get(i).getId() + ".jpg", i);
            }
            mListView.add(card);

        }else {
            card = new BigStoryCard(mContext);
            data = new CardData(CONSTANT.FOLDER, folderID);
            card.setTag(data);
            ((BigStoryCard)card).setStoryName(CONSTANT.convertFolderNameToStoryName(storyName));
            ((BigStoryCard)card).setTitleImage(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/" + "thumbnail" + "/" + thumbNailID + ".jpg");
            ((BigStoryCard)card).setDate(CONSTANT.convertFolderNameToDate(storyName));
            ((BigStoryCard)card).setItemNum(pictureNum);
            mListView.add(card);

            List<Tag> storyTags = db.getAllTagsByFolderId(folderID);
            int storyTagsSize = storyTags.size() < 5 ? storyTags.size() : 5;
            if(storyTagsSize > 0) {
                card = new TagsCard(mContext);
                //data = new CardData();
                card.setTag("tagsCard");
                for (int i = 0; i < storyTagsSize; i++) {
                    ((TagsCard) card).setTagText(storyTags.get(i).getName(), i);
                }
                mListView.add(card);
            }



            //TODO:folder id로 db검색해서 폴더에 걸린 tag 찾아오기

        }
    }



}
