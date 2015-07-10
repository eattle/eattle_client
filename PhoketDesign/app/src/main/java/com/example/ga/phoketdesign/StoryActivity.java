package com.example.ga.phoketdesign;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.dexafree.materialList.controller.RecyclerItemClickListener;
import com.dexafree.materialList.model.CardItemView;
import com.dexafree.materialList.view.MaterialListView;
import com.example.ga.phoketdesign.card.DailyCard;

import jp.wasabeef.recyclerview.animators.FadeInUpAnimator;


public class StoryActivity extends AppCompatActivity {

    public static final String EXTRA_NAME = "story_name";
    MaterialListView materialListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);

        Intent intent = getIntent();
        final String cheeseName = intent.getStringExtra(EXTRA_NAME);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(cheeseName);

        loadBackdrop();

        materialListView = (MaterialListView)findViewById(R.id.list);
        setupMaterialListView();

        materialListView.addOnItemTouchListener(new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(CardItemView view, int position) {
                switch (((CardTag) (view.getTag())).getCardType()) {
                    case CardTag.CARDTYPE_DAILY:
                        break;
                }

            }

            @Override
            public void onItemLongClick(CardItemView view, int position) {
                switch (((CardTag) (view.getTag())).getCardType()) {
                    case CardTag.CARDTYPE_DAILY:
                        break;
                }

            }
        });


        for(int i = 0; i < 10; i++) {
            setupDailyItem();
        }
//        new InitializeApplicationsTask().execute();



    }

    private void loadBackdrop() {
        final ImageView imageView = (ImageView) findViewById(R.id.backdrop);
        Glide.with(this).load(R.drawable.cheese_3).centerCrop().into(imageView);
    }

    public void setupMaterialListView() {
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        materialListView.setLayoutManager(layoutManager);

    }

    public void setupDailyItem() {
        DailyCard card = new DailyCard(getApplicationContext());
        CardTag cardTag = new CardTag();
        cardTag.setCardType(CardTag.CARDTYPE_DAILY);

        card.setTag(cardTag);

        card.setTag(cardTag);
        materialListView.add(card);
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

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            materialListView.clear();

            for(int i = 0; i < 10; i++) {
                setupDailyItem();
            }

            super.onPostExecute(result);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_story, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
