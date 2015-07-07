package com.example.ga.phoketdesign;


import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.dexafree.materialList.cards.WelcomeCard;
import com.dexafree.materialList.controller.RecyclerItemClickListener;
import com.dexafree.materialList.model.Card;
import com.dexafree.materialList.model.CardItemView;
import com.dexafree.materialList.view.MaterialListView;
import com.example.ga.phoketdesign.card.BigStoryCard;
import com.example.ga.phoketdesign.card.TitleCard;


import java.util.Collections;
import java.util.List;

import jp.wasabeef.recyclerview.animators.FadeInAnimator;
import jp.wasabeef.recyclerview.animators.FadeInUpAnimator;


public class MainSection1 extends Fragment {

    private Context mContext;
    private MaterialListView materialListView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressBar mProgressBar;


    public MainSection1() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View root = inflater.inflate(R.layout.fragment_main_section1, container, false);
        mContext = getActivity();
        materialListView = (MaterialListView) root.findViewById(R.id.list);
        mProgressBar = (ProgressBar) root.findViewById(R.id.progressBar);


        setupMaterialListView();
        materialListView.addOnItemTouchListener(new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(CardItemView view, int position) {
            }

            @Override
            public void onItemLongClick(CardItemView view, int position) {
                if (view.getTag().toString() == "BIG_STORY_CARD") {
                    materialListView.add(position + 1, getRandomCard());
                }
            }
        });

        mSwipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));
        mSwipeRefreshLayout.setRefreshing(true);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new InitializeApplicationsTask().execute();
            }
        });

        new InitializeApplicationsTask().execute();

        //show progress
        materialListView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);

        return root;
    }

    public void setupMaterialListView() {
        materialListView.setItemAnimator(new FadeInUpAnimator());
        materialListView.getItemAnimator().setAddDuration(300);
        materialListView.getItemAnimator().setRemoveDuration(300);

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        materialListView.setLayoutManager(layoutManager);
    }

    public void setupMaterialListItem() {

        materialListView.addAtStart(getRandomCard());
        materialListView.add(new TitleCard(mContext));
    }

    private Card getRandomCard(){
        BigStoryCard card;

        card = new BigStoryCard(mContext);
        card.setTag("BIG_STORY_CARD");

        return card;

    }

    /**
     * A simple AsyncTask to load the list of applications and display them
     */
    private class InitializeApplicationsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
//            materialListView.clear();
//            mAdapter.clearApplications();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            //Query the applications

            try {
                Thread.sleep(2000);
            }catch(InterruptedException e){
                Log.d("sleep", e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            materialListView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);

            //set data for list
            mSwipeRefreshLayout.setRefreshing(false);
            materialListView.clear();

            for(int i = 0; i < 5; i++){
                setupMaterialListItem();
            }

            super.onPostExecute(result);
        }
    }




}
