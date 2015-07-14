package com.eattle.phoket;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.dexafree.materialList.cards.OnButtonPressListener;
import com.dexafree.materialList.cards.SimpleCard;
import com.dexafree.materialList.controller.RecyclerItemClickListener;
import com.dexafree.materialList.controller.StickyHeaderDecoration;
import com.dexafree.materialList.model.Card;
import com.dexafree.materialList.model.CardItemView;
import com.dexafree.materialList.view.MaterialListView;
import com.eattle.phoket.Card.TagsCard;
import com.eattle.phoket.Card.manager.CardManager;
import com.eattle.phoket.helper.DatabaseHelper;
import com.eattle.phoket.Card.manager.CardData;
import com.eattle.phoket.model.Folder;
import com.eattle.phoket.model.Media;
import com.eattle.phoket.model.Tag;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.animators.FadeInUpAnimator;



public class Section1 extends Fragment {
    private final static String EXTRA_TAG = "MAIN_SECTION1";

    private final static int STATE_LOADING = 0;
    private final static int STATE_RUNNING = 1;

    private Context mContext;
    private DatabaseHelper db;

    private MaterialListView mListView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressBar mProgressBar;

    private int state;
    private boolean isDaily;

    boolean isSelectMode = false;
    ArrayList<CardData> selected = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_section1, container, false);
        mContext = getActivity();
        db = DatabaseHelper.getInstance(mContext);

        state = STATE_LOADING;
        isDaily = false;

        mListView = (MaterialListView) root.findViewById(R.id.section_listview1);
        mProgressBar = (ProgressBar) root.findViewById(R.id.progressBar);

        setupMaterialListView();

        mListView.addOnItemTouchListener(new RecyclerItemClickListener.OnItemClickListener() {

            @Override
            public void onItemClick(CardItemView view, int position) {

                if(isSelectMode){
                    if(mListView.isSelectable(position)){
                        if(mListView.setSelect(position)){
                            selected.add((CardData)view.getTag());
                        }else{
                            selected.remove((CardData)view.getTag());
                            if(selected.size() <= 0) {
                                ((MainActivity) getActivity()).setSelectMode();
                                isSelectMode = false;
                            }
                        }
                    }
                    return;
                }
//                if(state != STATE_RUNNING)  return;
                CardData data = (CardData) view.getTag();
                Intent intent;
                switch (data.getType()) {
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
                        intent.putExtra("kind", CardManager.FOLDER);// 그리드 종류(스토리,디폴트태그,태그)
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
                if(state != STATE_RUNNING)  return;
                if(isSelectMode)    return;
                if(!mListView.isSelectable(position))   return;
                isSelectMode = true;
                if(mListView.setSelect(position)){
                    selected.add((CardData)view.getTag());
                }else{
                    selected.remove((CardData)view.getTag());
                }
                ((MainActivity)getActivity()).setSelectMode();
            }
        });

        mSwipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));
        mSwipeRefreshLayout.setRefreshing(true);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                setLoading();
                ((MainActivity) getActivity()).sendMessageToService(CONSTANT.START_OF_PICTURE_CLASSIFICATION, 1);
                Snackbar.make(mSwipeRefreshLayout, "사진을 정리 중입니다", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();

            }
        });

        new InitializeApplicationsTask().execute();

        //show progress
        mListView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);


        return root;

    }


    private void setupMaterialListView() {
        mListView.setItemAnimator(new FadeInUpAnimator());
//        mListView.getItemAnimator().setAddDuration(300);
//        mListView.getItemAnimator().setRemoveDuration(300);

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        mListView.setLayoutManager(layoutManager);

        mListView.addItemDecoration(new StickyHeaderDecoration(mListView.getAdapter()));
    }

    /**
     * A simple AsyncTask to load the list of applications and display them
     */
    private class InitializeApplicationsTask extends AsyncTask<Void, Void, List<Folder>> {

        @Override
        protected void onPreExecute() {
            state = STATE_LOADING;
            super.onPreExecute();
        }

        @Override
        protected List<Folder> doInBackground(Void... params) {
            //Query the applications
            List<Folder> stories = db.getAllFolders();

            return stories;
        }

        @Override
        protected void onPostExecute(List<Folder> result) {


            mProgressBar.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);

            //set data for list
            mListView.clear();

            int storiesNum = result.size();
            for(int i = 0; i < storiesNum; i++){
                addCard(result.get(i));
            }

            setRunning();

            super.onPostExecute(result);
        }
    }



    private void addCard(Folder f){
        SimpleCard card;
        CardData tag;
        //일상
        if(f.getPicture_num() <= CONSTANT.BOUNDARY){
            if(!isDaily){
                isDaily = true;
                CardManager.setHeaderItem(mListView, mContext, CONSTANT.convertFolderNameToStoryName(f.getName()));
            }
            //daily card 추가
            List<Media> dailyMedia = db.getAllMediaByFolder(f.getId());
            for(int i = 0; i < f.getPicture_num(); i++){
                if(dailyMedia.get(i).getThumbnail_path() == null)//썸네일이 없으면
                    //원본에서 로드
                    CardManager.setDailyItem(mListView, mContext, f.getId(), i, dailyMedia.get(i).getPath());
                else
                    //썸네일에서 로드
                    CardManager.setDailyItem(mListView, mContext, f.getId(), i, dailyMedia.get(i).getThumbnail_path());
            }
        }else {
            isDaily = false;

            CardManager.setBigStoryItem(mListView, mContext,
                    f.getId(),
                    CONSTANT.convertFolderNameToStoryName(f.getName()),
                    f.getImage(),
                    CONSTANT.convertFolderNameToDate(f.getName()),
                    f.getPicture_num());

            List<Tag> storyTags = db.getAllTagsByFolderId(f.getId());
            int storyTagsSize = storyTags.size() < 5 ? storyTags.size() : 5;
            if(storyTagsSize > 0) {
                CardManager.setRelationTagsItem(mListView, mContext,
                        storyTagsSize,
                        storyTags,
                        new OnButtonPressListener() {
                            @Override
                            public void onButtonPressedListener(View view, Card card) {
                                Intent intent = new Intent(mContext, AlbumGridActivity.class);

                                switch (view.getId()) {
                                    case R.id.tag1:
                                        intent.putExtra("kind", CardManager.TAG);
                                        intent.putExtra("id", ((TagsCard) card).getTagId(0));
                                        mContext.startActivity(intent);
                                        break;
                                    case R.id.tag2:
                                        intent.putExtra("kind", CardManager.TAG);
                                        intent.putExtra("id", ((TagsCard) card).getTagId(1));
                                        mContext.startActivity(intent);

                                        break;
                                    case R.id.tag3:
                                        intent.putExtra("kind", CardManager.TAG);
                                        intent.putExtra("id", ((TagsCard) card).getTagId(2));
                                        mContext.startActivity(intent);

                                        break;
                                    case R.id.tag4:
                                        intent.putExtra("kind", CardManager.TAG);
                                        intent.putExtra("id", ((TagsCard) card).getTagId(3));
                                        mContext.startActivity(intent);

                                        break;
                                    case R.id.tag5:
                                        intent.putExtra("kind", CardManager.TAG);
                                        intent.putExtra("id", ((TagsCard) card).getTagId(4));
                                        mContext.startActivity(intent);

                                        break;
                                }
                            }
                        });
            }
            Media randomMedia = db.getMediaByFolderRandomly(f.getId());
            if(randomMedia.getThumbnail_path() == null)//썸네일이 없으면
                CardManager.setRecommendItem(mListView, mContext,
                        randomMedia.getId(),
                        randomMedia.getPath());
            else
                CardManager.setRecommendItem(mListView, mContext,
                        randomMedia.getId(),
                        randomMedia.getThumbnail_path());

        }
    }


    public void setLoading(){
        state = STATE_LOADING;
        mListView.clear();
    }

    public void setRunning(){
        state = STATE_RUNNING;
        mSwipeRefreshLayout.setRefreshing(false);
        isDaily = false;

    }

    public void addSingleCard(Folder f){
        if(mListView == null)   return;
        if(state == STATE_RUNNING)
            setLoading();
        addCard(f);
    }

/*    private void setOptionButton(){
        if(isSelectMode){
            mFab.setVisibility(View.VISIBLE);
        }else{
            mFab.setVisibility(View.GONE);
        }
    }*/
}