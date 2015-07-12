package com.eattle.phoket;


import android.app.Activity;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import com.eattle.phoket.Card.SmallStoryCard;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class Section2 extends Fragment {

    private final static String EXTRA_TAG = "MAIN_SECTION2";

    private final static int STATE_LOADING = 0;
    private final static int STATE_RUNNING = 1;

    private Context mContext;
    private DatabaseHelper db;

    private MaterialListView mListView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressBar mProgressBar;

    private int state;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_section2, container, false);
        mContext = getActivity();
        db = DatabaseHelper.getInstance(mContext);

        state = STATE_LOADING;

        mListView = (MaterialListView) root.findViewById(R.id.section_listview2);
        mProgressBar = (ProgressBar) root.findViewById(R.id.progressBar);

        setupMaterialListView();

        mListView.addOnItemTouchListener(new RecyclerItemClickListener.OnItemClickListener() {

            @Override
            public void onItemClick(CardItemView view, int position) {
//                if(state != STATE_RUNNING)  return;

                CardData data = (CardData)view.getTag();
                Intent intent = new Intent(mContext, AlbumGridActivity.class);
                intent.putExtra("kind", data.getType());
                intent.putExtra("id", data.getData());
                mContext.startActivity(intent);

            }

            @Override
            public void onItemLongClick(CardItemView view, int position) {
                if(state != STATE_RUNNING)  return;

//                Log.d("LONG_CLICK", view.getTag().toString());
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
        if(f.getPicture_num() > CONSTANT.BOUNDARY){
            CardManager.setSmallStoryItem(mListView, mContext,
                    f.getId(),
                    CONSTANT.convertFolderNameToStoryName(f.getName()),
                    f.getImage(),
                    CONSTANT.convertFolderNameToDate(f.getName()),
                    f.getPicture_num());
        }
    }

    public void setLoading(){
        state = STATE_LOADING;
        mListView.clear();
    }

    public void setRunning(){
        state = STATE_RUNNING;
        mSwipeRefreshLayout.setRefreshing(false);
    }

    public void addSingleCard(Folder f){
        if(mListView == null)   return;
        if(state == STATE_RUNNING)
            setLoading();
        addCard(f);
    }


}
