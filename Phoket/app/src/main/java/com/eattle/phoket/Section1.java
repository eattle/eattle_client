package com.eattle.phoket;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dexafree.materialList.cards.OnButtonPressListener;
import com.dexafree.materialList.cards.SimpleCard;
import com.dexafree.materialList.controller.RecyclerItemClickListener;
import com.dexafree.materialList.model.Card;
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

import java.util.ArrayList;
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
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        mListView.setLayoutManager(layoutManager);

        db = DatabaseHelper.getInstance(mContext);

        List<Folder> stories = db.getAllFolders();

        int storiesNum = stories.size();
        for(int i = 0; i < storiesNum; i++){
            selectCard(stories.get(i).getThumbNail_path(), stories.get(i).getName(), stories.get(i).getId(), stories.get(i).getPicture_num());
        }

        //mListView.add(card);

        mListView.addOnItemTouchListener(new RecyclerItemClickListener.OnItemClickListener() {

            @Override
            public void onItemClick(CardItemView view, int position) {
                CardData data = (CardData)view.getTag();
                Intent intent;
                switch (data.getType()){
                    case CONSTANT.NOTHING:
                        break;
                    case CONSTANT.TOPHOKET:
                        intent = new Intent(mContext, PopupPictureActivity.class);
                        intent.putExtra("id", data.getData());
                        mContext.startActivity(intent);
                        break;
                    case CONSTANT.DAILY:
                        List<Media> dailyMedia = db.getAllMediaByFolder(data.getData());
                        intent = new Intent(mContext, AlbumFullActivity.class);
                        intent.putParcelableArrayListExtra("mediaList", new ArrayList<Parcelable>(dailyMedia));
                        intent.putExtra("IDForStoryOrTag", data.getData());// 스토리를 위한 folderID, 또는 사용자 태그를 위한 tagID
                        intent.putExtra("kind", CONSTANT.FOLDER);// 그리드 종류(스토리,디폴트태그,태그)
                        intent.putExtra("position", data.getId());//어디에서 시작할지
                        mContext.startActivity(intent);

                        break;
                    case CONSTANT.FOLDER:
                    case CONSTANT.TAG:
                        intent = new Intent(mContext, AlbumGridActivity.class);
                        intent.putExtra("kind", data.getType());
                        intent.putExtra("id", data.getData());
                        mContext.startActivity(intent);
                        break;
                }
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



    void selectCard(String thumbNailPath, String storyName, int folderID, int pictureNum){
        //TODO:update날짜 비교해서 추가할지 말지 결정 or list안에서 비교해서 추가할지 말지 결정

        SimpleCard card;
        CardData data;
        if(pictureNum <= CONSTANT.BOUNDARY){
            //daily card 추가
            List<Media> dailyMedia = db.getAllMediaByFolder(folderID);
            for(int i = 0; i < pictureNum; i++){
                card = new DailyCard(mContext);
                data = new CardData(CONSTANT.DAILY, folderID,i);
                card.setTag(data);
                ((DailyCard)card).setDailyImage(dailyMedia.get(i).getThumbnail_path());

                mListView.add(card);
            }
/*            card = new DailyCard(mContext);
            data = new CardData(CONSTANT.NOTHING, -1);
            card.setTag(data);
            ((DailyCard)card).setDailyId(dailyMedia.get(i).getId());
//            for(int i = 0; i < pictureNum; i++){
                ((DailyCard)card).setDailyImage(dailyMedia.get(i).getId(), Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/" + "thumbnail" + "/" + dailyMedia.get(i).getId() + ".jpg");
//            }
            ((DailyCard) card).setOnButtonPressedListener(new OnButtonPressListener() {
                @Override
                public void onButtonPressedListener(View view, Card card) {
                    Intent intent = new Intent(mContext, AlbumFullActivity.class);
                    intent.putParcelableArrayListExtra("mediaList", new ArrayList<Parcelable>(dailyMedia));
                    intent.putExtra("IDForStoryOrTag", folderId_);// 스토리를 위한 folderID, 또는 사용자 태그를 위한 tagID
                    intent.putExtra("kind", CONSTANT.FOLDER);// 그리드 종류(스토리,디폴트태그,태그)


                    switch (view.getId()) {
                        case R.id.dailyImage3:
                            intent.putExtra("position", 2);//어디에서 시작할지


                            mContext.startActivity(intent);
                            break;
                        case R.id.dailyImage2:
                            intent.putExtra("position", 1);//어디에서 시작할지

                            mContext.startActivity(intent);

                            break;
                        case R.id.dailyImage1:
                            intent.putExtra("position", 0);//어디에서 시작할지

                            mContext.startActivity(intent);

                            break;
                    }
                }
            });

            mListView.add(card);*/

        }else {
            card = new BigStoryCard(mContext);
            data = new CardData(CONSTANT.FOLDER, folderID);
            card.setTag(data);
            ((BigStoryCard)card).setStoryName(CONSTANT.convertFolderNameToStoryName(storyName));
            ((BigStoryCard)card).setTitleImage(thumbNailPath);
            ((BigStoryCard)card).setDate(CONSTANT.convertFolderNameToDate(storyName));
            ((BigStoryCard)card).setItemNum(pictureNum);
            mListView.add(card);

            List<Tag> storyTags = db.getAllTagsByFolderId(folderID);
            int storyTagsSize = storyTags.size() < 5 ? storyTags.size() : 5;
            if(storyTagsSize > 0) {
                card = new TagsCard(mContext);
                data = new CardData(CONSTANT.NOTHING, -1);
                card.setTag(data);
                for (int i = 0; i < storyTagsSize; i++) {
                    ((TagsCard) card).setTag(i, storyTags.get(i).getId(), storyTags.get(i).getName(), storyTags.get(i).getColor());
                }
                ((TagsCard) card).setOnButtonPressedListener(new OnButtonPressListener() {
                    @Override
                    public void onButtonPressedListener(View view, Card card) {
                        Intent intent = new Intent(mContext, AlbumGridActivity.class);

                        switch (view.getId()) {
                            case R.id.tag1:
                                intent.putExtra("kind", CONSTANT.TAG);
                                intent.putExtra("id", ((TagsCard) card).getTagId(0));
                                mContext.startActivity(intent);
                                break;
                            case R.id.tag2:
                                intent.putExtra("kind", CONSTANT.TAG);
                                intent.putExtra("id", ((TagsCard) card).getTagId(1));
                                mContext.startActivity(intent);

                                break;
                            case R.id.tag3:
                                intent.putExtra("kind", CONSTANT.TAG);
                                intent.putExtra("id", ((TagsCard) card).getTagId(2));
                                mContext.startActivity(intent);

                                break;
                            case R.id.tag4:
                                intent.putExtra("kind", CONSTANT.TAG);
                                intent.putExtra("id", ((TagsCard) card).getTagId(3));
                                mContext.startActivity(intent);

                                break;
                            case R.id.tag5:
                                intent.putExtra("kind", CONSTANT.TAG);
                                intent.putExtra("id", ((TagsCard) card).getTagId(4));
                                mContext.startActivity(intent);

                                break;
                        }
                    }
                });

                mListView.add(card);
            }

            int randomMediaId = db.getMediaByFolderRandomly(folderID).getId();
            String randomMediaThumbnailPath = db.getMediaByFolderRandomly(folderID).getThumbnail_path();
            card = new ToPhoketCard(mContext);
            data = new CardData(CONSTANT.TOPHOKET, randomMediaId);
            card.setTag(data);
            ((ToPhoketCard)card).setImage(randomMediaThumbnailPath);
            mListView.add(card);
            //TODO: 포켓에 넣어달라고 추천할만한 사진 걸러내기

        }
    }



}