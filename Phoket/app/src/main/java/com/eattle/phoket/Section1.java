package com.eattle.phoket;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.animators.FadeInUpAnimator;



public class Section1 extends Fragment {
    private final static String EXTRA_TAG = "MAIN_SECTION1";

    private final static int STATE_LOADING = 0;
    private final static int STATE_RUNNING = 1;
    private final static int STATE_SELECT = 2;




    private Context mContext;
    private DatabaseHelper db;

    private MaterialListView mListView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressBar mProgressBar;

    private int state;
    private boolean isDaily = false;


//    boolean isSelectMode = false;
    ArrayList<CardData> selected = new ArrayList<>();
    ArrayList<Integer> selectedp = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_section1, container, false);
        mContext = getActivity();
        db = DatabaseHelper.getInstance(mContext);

        mListView = (MaterialListView) root.findViewById(R.id.section_listview1);
        mProgressBar = (ProgressBar) root.findViewById(R.id.progressBar);

        setupMaterialListView();


        mListView.addOnItemTouchListener(new RecyclerItemClickListener.OnItemClickListener() {

            @Override
            public void onItemClick(CardItemView view, int position) {
                CardData data = (CardData) view.getTag();

                switch (state) {
                    case STATE_SELECT:
                        if (!mListView.isSelectable(position))
                            break;
                        if (mListView.setSelect(position)) {
                            selected.add(data);
                            selectedp.add(position);
                        } else {
                            selected.remove(data);
                            selectedp.remove((Integer)position);
                            if (selected.size() <= 0) {
                                ((MainActivity) getActivity()).setSelectMode();
                                state = STATE_RUNNING;
                            }
                        }
                        ((MainActivity) getActivity()).mActionMode.setSubtitle(selected.size() + "개 선택됨");
                        break;
                    case STATE_LOADING:
                    case STATE_RUNNING:
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
                        break;
                }
            }

            @Override
            public void onItemLongClick(CardItemView view, int position) {
                if(state == STATE_RUNNING && mListView.isSelectable(position)){
                    state = STATE_SELECT;
                    mListView.setSelect(position);
                    selected.add((CardData) view.getTag());
                    selectedp.add(position);
                    ((MainActivity)getActivity()).setSelectMode();
                }else if(state == STATE_LOADING){
                    Snackbar.make(mSwipeRefreshLayout, "정리가 완료된 후 다시 시도해주세요", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                }

                if(db == null)
                    db = DatabaseHelper.getInstance(getActivity());

                if(db != null && db.getGuide() == 0) {//가이드 중
                    GUIDE.guide_seven(getActivity());
                    GUIDE.GUIDE_STEP++;
                }
            }
        });

        mSwipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));
        mSwipeRefreshLayout.setRefreshing(true);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(state == STATE_RUNNING) {
                    if (db.getGuide() == 0)
                        //가이드를 완료하지 않았으면
                        //서비스에게 가이드 시작을 요청한다
                        ((MainActivity) getActivity()).sendMessageToService(CONSTANT.START_OF_GUIDE);
                    else{
                        for(int i=0;i<GUIDE.CURRENT_POPUP.size();i++)
                            GUIDE.CURRENT_POPUP.get(i).dismiss();//가이드 팝업을 지운다
                        
                        ((MainActivity) getActivity()).sendMessageToService(CONSTANT.START_OF_PICTURE_CLASSIFICATION);
                    }
                }else{
                    mSwipeRefreshLayout.setRefreshing(false);
                    Snackbar.make(mSwipeRefreshLayout, "선택을 취소한 후 다시 시도해주세요", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                }
            }
        });


        //show progress
        mListView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);

        initialize();

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

            Log.d(EXTRA_TAG, "" + storiesNum);

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
                CardManager.setHeaderItem(mListView, mContext, CONSTANT.convertFolderNameToDate(f.getName()));
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
            else {
                CardManager.setRecommendItem(mListView, mContext,
                        randomMedia.getId(),
                        randomMedia.getThumbnail_path());
            }
        }
    }

    //초기화
    //단순히 db에서 값을 가져와서 보여줌
    public void initialize(){
        state = STATE_LOADING;
        isDaily = false;
        int s = selected.size();
        for(int i = 0; i < s; i++) {
            mListView.setSelect(selectedp.get(i));
        }
        selected.clear();
        selectedp.clear();
        if(s <= 0) {
            new InitializeApplicationsTask().execute();
        }else{
            state = STATE_RUNNING;

        }
    }

    // 상태를 loading으로
    // db자체를 바꾸는 service를 실행
    public void setLoading(){
        state = STATE_LOADING;
        mListView.clear();
        mSwipeRefreshLayout.setRefreshing(true);
    }

    public void setRunning(){
        state = STATE_RUNNING;
        isDaily = false;
        mSwipeRefreshLayout.setRefreshing(false);
    }

    public void addSingleCard(Folder f){
        if(mListView == null)   return;
        if(state == STATE_RUNNING){
            state = STATE_LOADING;
            mSwipeRefreshLayout.setRefreshing(true);
        }
        addCard(f);
    }


}