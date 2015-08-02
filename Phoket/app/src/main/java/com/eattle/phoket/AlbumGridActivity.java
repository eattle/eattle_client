package com.eattle.phoket;

import android.content.ComponentCallbacks2;
import android.os.AsyncTask;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.dexafree.materialList.controller.RecyclerItemClickListener;
import com.dexafree.materialList.controller.StickyHeaderDecoration;
import com.dexafree.materialList.model.CardItemView;
import com.dexafree.materialList.view.MaterialListView;
import com.eattle.phoket.Card.manager.CardData;
import com.eattle.phoket.Card.manager.CardManager;
import com.eattle.phoket.helper.DatabaseHelper;
import com.eattle.phoket.model.Folder;
import com.eattle.phoket.model.Media;
import com.eattle.phoket.model.Tag;

import java.util.ArrayList;
import java.util.List;

public class AlbumGridActivity extends AppCompatActivity {
    private final static String EXTRA_TAG = "ALBUM_GRID";

    private static MaterialListView mGridView;
    private static ProgressBar mProgressBar;

    //folderId or tagId
    private int id;
    //스토리로 들어왔는지, 태그로 들어왔는지, 디폴트 태그로 들어왔는지
    private int kind;

    private List<Media> mMediaList;

    private int day = 0;
    private int month = 0;
    private int year = 0;

    String titleName;
    String titleImagePath;
    Media mediaByTag;//태그가 눌려진 사진
    int mediaId;//태그가 눌려진 사진의 아이디
    String tagName;//태그

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Bundle bundle = new Bundle();
        bundle.putInt("id",id);
        bundle.putInt("kind", kind);
        bundle.putInt("mediaId", mediaId);
        bundle.putString("tagName", tagName);

        outState.putBundle("save_data", bundle);
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DatabaseHelper db = DatabaseHelper.getInstance(AlbumGridActivity.this);
        setContentView(R.layout.activity_album_grid);
        CONSTANT.actList.add(this);

        if ( savedInstanceState != null )
        {
            Bundle bundle = savedInstanceState.getBundle("save_data");
            id = bundle.getInt("id",-1);
            kind = bundle.getInt("kind", -1);
            mediaId = bundle.getInt("mediaId", -1);
            mediaByTag = db.getMediaById(mediaId);
            tagName = bundle.getString("tagName", "");

        }
        else {
            Intent intent = new Intent(this.getIntent());

            id = intent.getIntExtra("id", -1);//folderId가 될수도 있고 TagId가 될 수도 있다
            kind = intent.getIntExtra("kind", -1);
            if (kind == CONSTANT.DEFAULT_TAG) {
                mediaId = intent.getIntExtra("mediaId", -1);
                mediaByTag = db.getMediaById(mediaId);
                tagName = intent.getStringExtra("tagName");
            }
        }

        refreshGrid();

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //폴더(스토리)의 제목 등록
        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(titleName);
        collapsingToolbar.setExpandedTitleTextAppearance(R.style.TextAppearance_AppCompat_Subhead);
        collapsingToolbar.setCollapsedTitleTextAppearance(R.style.TextAppearance_AppCompat_Small);


        //폴더(스토리)의 대표사진 등록
        loadBackdrop();

        mGridView = (MaterialListView) findViewById(R.id.GridList);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        setupMaterialListView();


        mGridView.addOnItemTouchListener(new RecyclerItemClickListener.OnItemClickListener() {

            @Override
            public void onItemClick(CardItemView view, int position) {
                CardData tag = (CardData) view.getTag();

                Intent intent = new Intent(getApplicationContext(), AlbumFullActivity.class);
                intent.putParcelableArrayListExtra("mediaList", new ArrayList<Parcelable>(mMediaList));
                intent.putExtra("position", tag.getId());//어디에서 시작할지
                intent.putExtra("IDForStoryOrTag", id);// 스토리를 위한 folderID, 또는 사용자 태그를 위한 tagID
                intent.putExtra("tagName", tagName);// 디폴트 태그를 위한 tagName
                intent.putExtra("mediaId", mediaId);// 태그클릭->그리드뷰->풀픽쳐 인 경우에 필요
                intent.putExtra("kind", kind);// 그리드 종류(스토리,디폴트태그,태그)

                startActivity(intent);

            }

            @Override
            public void onItemLongClick(CardItemView view, int position) {
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.storyStart);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AlbumFullActivity.class);
                intent.putParcelableArrayListExtra("mediaList", new ArrayList<Parcelable>(mMediaList));
                intent.putExtra("position", -1);//-1을 넘겨주면 스토리 '맨 처음'부터 시작(제목화면부터)
                intent.putExtra("IDForStoryOrTag", id);// folder ID
                intent.putExtra("tagName", tagName);//디폴트 태그는 tag이름을 통해서 작동
                intent.putExtra("mediaId", mediaId);// 태그클릭->그리드뷰->풀픽쳐 인 경우에 필요
                intent.putExtra("kind", kind);// 그리드 종류(스토리,디폴트태그,태그)
                startActivity(intent);
            }
        });

        new InitializeApplicationsTask().execute();

        //show progress
        mGridView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);

        if (db.getGuide() == 0) {//가이드 중
            GUIDE.guide3(AlbumGridActivity.this);
            //GUIDE.GUIDE_STEP++;
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_album_grid, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                int actSize = CONSTANT.actList.size();
                for (int i = 0; i < actSize; i++) {
                    CONSTANT.actList.get(i).finish();
                    finish();
                }
                return true;
            case R.id.search:
                Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void loadBackdrop() {
        final ImageView imageView = (ImageView) findViewById(R.id.backdrop);
        DatabaseHelper db = DatabaseHelper.getInstance(AlbumGridActivity.this);
        if (db.getGuide() == 1) {//가이드 아닐때
            Glide.with(AlbumGridActivity.this)
                    .load(titleImagePath)
                    .thumbnail(0.1f)
                    .into(imageView);
        } else if (db.getGuide() == 0) {//가이드 중
            Glide.with(AlbumGridActivity.this)
                    .load(R.mipmap.phoket1)
                    .thumbnail(0.1f)
                    .into(imageView);
        }
    }

    public void setupMaterialListView() {

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        mGridView.setLayoutManager(layoutManager);

        mGridView.addItemDecoration(new StickyHeaderDecoration(mGridView.getAdapter()));

    }


    @Override
    protected void onRestart() {
        refreshGrid();
        //변경사항 적용
        new InitializeApplicationsTask().execute();
        super.onRestart();
    }

    @Override
    public void onStop() {
        Glide.get(AlbumGridActivity.this).clearMemory();
        Glide.get(AlbumGridActivity.this).trimMemory(ComponentCallbacks2.TRIM_MEMORY_MODERATE);
        super.onStop();
    }

    public void refreshGrid() {
        day = 0;
        month = 0;
        year = 0;
        //grid view를 업데이트 한다(백버튼 또는 x버튼으로 들어왔을 때)
        //1. CONSTANT.FOLDER 2. CONSTANT.DEFAULT_TAG 3.CONSTANT.TAG
        DatabaseHelper db = DatabaseHelper.getInstance(AlbumGridActivity.this);
        if (kind == CONSTANT.FOLDER) {//스토리(폴더)로 보고있을 때
            Folder f = db.getFolder(id);
            mMediaList = db.getAllMediaByFolder(id);

            titleName = CONSTANT.convertFolderNameToStoryName(f.getName());
            titleImagePath = f.getImage();//대표 이미지의 경로를 얻는다
            //titleThumbnailPath = f.getThumbNail_path();//대표 사진의 썸네일 경로를 얻는다.

        } else if (kind == CONSTANT.DEFAULT_TAG) {//기본 태그(날짜, 장소)를 타고 들어왔을 경우
            if (tagName.contains("년")) {
                mMediaList = db.getAllMediaByYear(mediaByTag.getYear());
            } else if (tagName.contains("월")) {
                mMediaList = db.getAllMediaByMonth(mediaByTag.getMonth());
            } else if (tagName.contains("일")) {
                mMediaList = db.getAllMediaByDay(mediaByTag.getDay());
            }
            titleName = tagName + "의 추억";
            titleImagePath = mMediaList.get(0).getPath();

        } else if (kind == CONSTANT.TAG) {
            Tag t = db.getTagByTagId(id);
            //Media mediaByTag = db.getMediaById(intent.getIntExtra("mediaId", -1));
            mMediaList = db.getAllMediaByTagId(id);

            titleName = t.getName();
            titleImagePath = mMediaList.get(0).getPath();

        }

        if (mMediaList.size() == 0)//사진이 하나도 남지 않으면
            this.finish();//그리드뷰 종료
    }

    //백버튼을 눌렀을 때, 메모리 정리를 한다
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK://백버튼을 통제(비밀번호 유지를 위해)
                DatabaseHelper db = DatabaseHelper.getInstance(AlbumGridActivity.this);
                if (db.getGuide() == 0)//가이드 도중에
                    return true;//백버튼을 막는다

                CONSTANT.actList.remove(this);

                finish();//현재 띄워져 있던 albumGridActivity 종료(메모리 확보를 위해)
                return false;
        }
        return true;
    }

    /**
     * A simple AsyncTask to load the list of applications and display them
     */
    private class InitializeApplicationsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            //Query the applications

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {


            mProgressBar.setVisibility(View.GONE);
            mGridView.setVisibility(View.VISIBLE);

            //set data for list
            mGridView.clear();

            int num = mMediaList.size();
            for (int i = 0; i < num; i++) {
                addCard(mMediaList.get(i), i);
            }

            super.onPostExecute(result);
        }
    }

    private void addCard(Media m, int order) {

        if (day != m.getDay() || month != m.getMonth() || year != m.getYear()) {
            day = m.getDay();
            month = m.getMonth();
            year = m.getYear();
            CardManager.setHeaderItem(mGridView, getBaseContext(), CONSTANT.convertFolderNameToDate("" + year + "_" + month + "_" + day));
        }

        DatabaseHelper db = DatabaseHelper.getInstance(AlbumGridActivity.this);
        if (db.getGuide() == 1) {//가이드가 아닐 때(평상시)

            if (m.getThumbnail_path() == null)//내장 썸네일이 혹시 존재하지 않을 경우에만
                CardManager.setMediaItem(mGridView, AlbumGridActivity.this, m.getId(), order, m.getPath());
            else
                CardManager.setMediaItem(mGridView, AlbumGridActivity.this, m.getId(), order, m.getThumbnail_path());

        } else if (db.getGuide() == 0) {//가이드 중일 때
            CardManager.setMediaItem(mGridView, AlbumGridActivity.this, m.getId(), order, "phoket" + m.getId());
        }
    }
}
