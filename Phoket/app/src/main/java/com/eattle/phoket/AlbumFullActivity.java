package com.eattle.phoket;


import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.bumptech.glide.Glide;
import com.eattle.phoket.helper.DatabaseHelper;
import com.eattle.phoket.model.Folder;
import com.eattle.phoket.model.Media;
import com.eattle.phoket.model.Tag;

import java.util.List;

//스토리 그리드뷰에서 특정 사진을 클릭했을 때, 뷰페이저를 만들어주는 부분
public class AlbumFullActivity extends ActionBarActivity {

    private String TAG = "AlbumFullActivity";

    static List<Media> mMediaList;
    int initialMediaPosition;

    static int totalPictureNum;

    //'스토리 시작'을 통해 들어왔을 경우
    static String titleName;
    static String titleImagePath;
    static int titleImageId;
    static int kind;
    static int Id;
    static int mediaId;
    String tagName = "";

    //static ExtendedViewPager mViewPager;
    static ViewPager mViewPager;
    static TouchImageAdapter touchImageAdapter;
    static int isTagAppeared = 0;//태그가 띄워져 있으면 1, 아니면 0
    StoryStartFragment storyStartFragment;
    StoryRecommendFragment storyRecommendFragment;

    //static ArrayList<ImageView> viewPagerImage;

    //ContentResolver cr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //if (viewPagerImage == null)
        //    viewPagerImage = new ArrayList<ImageView>();//나중에 비트맵 메모리 해제를 위한 리스트
        CONSTANT.actList.add(this);

        DatabaseHelper db = DatabaseHelper.getInstance(AlbumFullActivity.this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_full);


        Intent intent = getIntent();
        mMediaList = intent.getParcelableArrayListExtra("mediaList");
        Id = intent.getIntExtra("IDForStoryOrTag", 0);
        mediaId = intent.getIntExtra("mediaId", -1);
        kind = intent.getIntExtra("kind", 0);
        tagName = intent.getStringExtra("tagName");
        initialMediaPosition = intent.getIntExtra("position", 0);
        if (kind == CONSTANT.FOLDER) {//스토리를 타고 들어왔을 경우
            Folder folder = db.getFolder(Id);
            totalPictureNum = folder.getPicture_num();
            titleName = folder.getName();
            titleImagePath = folder.getImage();
            titleImageId = folder.getTitleImageID();
        } else if (kind == CONSTANT.DEFAULT_TAG) {//디폴트 태그 그리드뷰에서 들어왔을 경우
            Media mediaByTag = db.getMediaById(mediaId);
            //기본태그를 타고 들어왔을 때는 tag의 id가 -1이다. -> tagName을 통해서 검색해야 함

            if (tagName.contains("년")) {
                mMediaList = db.getAllMediaByYear(mediaByTag.getYear());
            } else if (tagName.contains("월")) {
                mMediaList = db.getAllMediaByMonth(mediaByTag.getMonth());
            } else if (tagName.contains("일")) {
                mMediaList = db.getAllMediaByDay(mediaByTag.getDay());
            }
            titleName = tagName + "의 추억";
            titleImagePath = mMediaList.get(0).getPath();
            titleImageId = mMediaList.get(0).getId();
            totalPictureNum = mMediaList.size();
        } else if (kind == CONSTANT.TAG) {//사용자 태그 그리드뷰에서 들어왔을 경우
            Tag t = db.getTagByTagId(Id);
            //Media mediaByTag = db.getMediaById(intent.getIntExtra("mediaId", -1));
            mMediaList = db.getAllMediaByTagId(Id);

            titleName = t.getName();
            titleImagePath = mMediaList.get(0).getPath();
            titleImageId = mMediaList.get(0).getId();
            totalPictureNum = mMediaList.size();
        }


        //cr = this.getContentResolver();

        //뷰페이저 생성
        mViewPager = (ViewPager) findViewById(R.id.albumFullViewPager);
        //mViewPager.setOffscreenPageLimit(0);
        touchImageAdapter = new TouchImageAdapter(this, mMediaList, getSupportFragmentManager());
        mViewPager.setAdapter(touchImageAdapter);//뷰페이저 어댑터 설정
        if (initialMediaPosition != -1) {//-1이면 스토리 처음부터 시작(제목화면부터)
            mViewPager.setCurrentItem(initialMediaPosition);
        }


        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                makeStoryStartFragment(position);
                if (position >= 0)
                    ((StoryStartFragment) (getFragmentManager().findFragmentById(R.id.storyStart))).showBlur(1);


                if (initialMediaPosition == -1) {  //스토리 제목(타이틀 화면)부터 시작해야 하는 경우
                    position--;//첫화면에 제목화면을 넣기 위해.
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction tr = fragmentManager.beginTransaction();
                    if (position == -1) {
                        // ~년~월~일의 스토리 -> 보임
                        storyStartFragment.getView().findViewById(R.id.storyStartDate).setVisibility(View.VISIBLE);
                        storyStartFragment.getView().findViewById(R.id.storyStartTitle).setVisibility(View.VISIBLE);

                        Fragment f;
                        if ((f = isThereTabToTagHere()) != null) {//만약 태그들이 띄워져 있었으면 삭제한다
                            tr.remove(f);//tagArrayList에 있는 모든 태그들을 삭제한다
                            tr.remove(fragmentManager.findFragmentById(R.id.exit));//종료 버튼 삭제
                            tr.setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                            tr.commit();
                        }
                        return;
                    }
                }

                if (position != -1) {// ~년~월~일의 스토리 -> 없앰
                    if (storyStartFragment.getView() != null) {
                        storyStartFragment.getView().findViewById(R.id.storyStartDate).setVisibility(View.INVISIBLE);
                        storyStartFragment.getView().findViewById(R.id.storyStartTitle).setVisibility(View.INVISIBLE);
                    }
                }
                //추천스토리를 관리하는 부분-----------------------------------------------
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                if (position == mMediaList.size()) {
                    if (isTagAppeared == 1) {
                        pushTabToTag(mMediaList.get(position - 1), position - 1);
                        setPlacePopup(mMediaList.get(position - 1));
                    }
                    storyRecommendFragment = StoryRecommendFragment.newInstance(mMediaList.get(0).getFolder_id());
                    fragmentTransaction.add(R.id.frameForStoryRecommend, storyRecommendFragment, "storyRecommendFragment");
                    fragmentTransaction.setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);

                    fragmentTransaction.commit();
                    return;
                } else if (storyRecommendFragment != null) {//마지막 페이지가 아니면서 기존에 추천 스토리를 만들어 놓은게 있으면
                    fragmentTransaction.remove(storyRecommendFragment);
                    fragmentTransaction.commit();
                }


                //------------------------------------------------------------------------------
                if (isTagAppeared == 1) {//태그들이 띄워져 있어야 하는데
                    Fragment f;
                    if ((f = isThereTabToTagHere()) == null) {//스토리 제목(타이틀 화면)에 도달해서 잠시 안보였던 경우
                        pushTabToTag(mMediaList.get(position), position);//다시 보이게 한다
                    }
                }
                setTabToTag(mMediaList.get(position), position);
            }


            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (initialMediaPosition == -1)   //스토리 제목부터 시작해야 하는 경우
                    position--;//첫화면에 제목화면을 넣기 위해
                if (position < 0 && getFragmentManager().findFragmentById(R.id.storyStart) != null) {
                    ((StoryStartFragment) (getFragmentManager().findFragmentById(R.id.storyStart))).showBlur(positionOffset);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });


        if (db == null) {
            db = DatabaseHelper.getInstance(AlbumFullActivity.this);
        }
        if (db.getGuide() == 0) {//가이드 중
            GUIDE.guide_four(AlbumFullActivity.this);
            //GUIDE.GUIDE_STEP++;
        }
    }

    //뷰페이저
    class TouchImageAdapter extends FragmentStatePagerAdapter {
        private String TAG = "touchImageAdapter";
        Context context;
        List<Media> mediaList;

        public TouchImageAdapter(Context context, List<Media> mediaList, android.support.v4.app.FragmentManager fm) {
            super(fm);
            this.context = context;
            this.mediaList = mediaList;
        }

        @Override
        //상관없음
        public int getItemPosition(Object object) {//뷰페이저 업데이트를 위해 반드시 있어야 함
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            if (initialMediaPosition != -1) //default
                return (mediaList.size() + 1);

            else //-1이면 스토리 처음부터 시작(제목화면부터)
                return (mediaList.size() + 2);//하나가 더 추가됨
        }


        @Override
        public android.support.v4.app.Fragment getItem(int position) {

            if (initialMediaPosition == -1) {  //스토리 제목부터 시작해야 하는 경우
                position--;//첫화면에 제목화면을 넣기 위해.
            }

            if (storyStartFragment == null)//간략 체크
                makeStoryStartFragment(position);

            if (position == -1 || position == mediaList.size())//스토리 시작 화면 또는 추천스토리 부분
                return StoryMainFragment.newInstance(null, position, mediaList.size());//결과적으로 아무것도 반환되지 않도록 한다
            StoryMainFragment storyMainFragment = StoryMainFragment.newInstance(mediaList.get(position), position, mediaList.size());


            return storyMainFragment;
        }

        public void removeView(int position) {//뷰페이저 업데이트를 위해 선언

            if (position >= 0 && position < mediaList.size())
                mediaList.remove(position);

            this.notifyDataSetChanged();

            //태그 상태 업데이트
            if (mediaList.size() != 0) {//아직 사진이 남아 있을 때

                if (position == mediaList.size()) {//마지막 사진
                    setTabToTag(mMediaList.get(position - 1), position - 1);
                    mViewPager.setCurrentItem(position - 1);//뷰페이저 초점 이동
                } else
                    setTabToTag(mMediaList.get(position), position);
            }
        }
    }

    Fragment isThereTabToTagHere() {
        return getFragmentManager().findFragmentById(R.id.tagLayout);
    }

    public void makeStoryStartFragment(int position) {
        //화면이 회전될 때, 안드로이드자체가 fragment를 다시 생성해 주지만 storyStartFragment 변수에 할당이 안될 수 있음
        //즉, 일단 storyStartFragment가 있는지 확인
        Fragment tempForStoryStartFragment = getFragmentManager().findFragmentById(R.id.storyStart);
        if (tempForStoryStartFragment != null)//기존에 만들어진 storyStartFragment가 있으면
            storyStartFragment = (StoryStartFragment) tempForStoryStartFragment;
        else {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            storyStartFragment = StoryStartFragment.newInstance(titleImagePath, titleName, kind, position);
            fragmentTransaction.add(R.id.storyStart, storyStartFragment, "StoryStartFragment");
            fragmentTransaction.commit();
        }
    }

    //백버튼을 눌렀을 때, 메모리 정리를 한다
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK://백버튼을 통제(비밀번호 유지를 위해)
                DatabaseHelper db = DatabaseHelper.getInstance(AlbumFullActivity.this);
                if (db.getGuide() == 0)//가이드 도중에
                    return true;//백버튼을 막는다

                finish();//현재 띄워져 있던 albumFullActivity 종료(메모리 확보를 위해)
                return false;
        }
        return true;
    }

    void setTabToTag(Media m, int position) {
        if (isThereTabToTagHere() != null) {
            FragmentTransaction tr = getFragmentManager().beginTransaction();
            TagsOverAlbum ttt = TagsOverAlbum.newInstance(m, position, mMediaList.size());
            tr.replace(R.id.tagLayout, ttt, "TabToTag");//기존의 fragment의 내용을 변경한다(position , mMediaList.size())
            tr.setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            tr.commit();
        }
    }

    void pushTabToTag(Media m, int position) {
        Fragment f;
        if ((f = isThereTabToTagHere()) != null) {
            FragmentTransaction tr = getFragmentManager().beginTransaction();
            //tagArrayList에 있는 모든 태그들을 삭제한다
            tr.remove(f);
            tr.remove(getFragmentManager().findFragmentById(R.id.exit));
            tr.setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            tr.commit();
        } else {
            FragmentTransaction tr = getFragmentManager().beginTransaction();
            TagsOverAlbum ttt = TagsOverAlbum.newInstance(m, position, mMediaList.size());
            tr.add(R.id.tagLayout, ttt, "TabToTag");
            tr.add(R.id.exit, new StoryExitFragment(), "StoryExitFragment");
            tr.setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            tr.commit();

        }
    }

    void setPlacePopup(Media m) {
        //장소명이 존재하면 태그로 추가할지 묻는다
        if ((m.getPlaceName() != null) && !m.getPlaceName().equals("")) {
            //일단 m.getPlaceName()이 태그 목록에 있는지 확인한다
            DatabaseHelper db = DatabaseHelper.getInstance(AlbumFullActivity.this);
            int tagId = db.getTagIdByTagName(m.getPlaceName());
            //1. 해당 장소명으로 태그가 아예 존재하지 않을 때 -> 묻는다
            if (tagId == 0) {
                Intent intent = new Intent(getApplicationContext(), PopupForTagAddition.class);
                DataForTagAddition tempData = new DataForTagAddition(tagId, m.getId(), m.getFolder_id(), m.getPlaceName());
                intent.putExtra("dataForTagAddition", tempData);
                startActivity(intent);
            }
            //2. 해당 장소명으로 태그가 존재하는데, 해당 폴더에 등록되어 있지 않을 때 -> 묻는다
            else if (db.getMediaTagByIds(tagId, m.getId()) == 0) {
                Intent intent = new Intent(getApplicationContext(), PopupForTagAddition.class);
                DataForTagAddition tempData = new DataForTagAddition(tagId, m.getId(), m.getFolder_id(), m.getPlaceName());
                intent.putExtra("dataForTagAddition", tempData);
                startActivity(intent);
            }
            //(한번 물어봤는데 아니요로 대답할 시에 다음부터 묻지 않는다 -> 추후구현(아니오라고 대답했을 때 태그-미디어 DB에 등록하고 추가적인 flag를 다는 방식으로)
        }

    }

    @Override
    public void onStop() {
        Glide.get(this).clearMemory();
        Glide.get(this).trimMemory(ComponentCallbacks2.TRIM_MEMORY_COMPLETE);
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_album_full, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onUserLeaveHint() {
        DatabaseHelper db = DatabaseHelper.getInstance(AlbumFullActivity.this);
        if (db.getGuide() == 0) {
            GUIDE.GUIDE_STEP = 5;
            for (int i = 0; i < CONSTANT.actList.size(); i++)
                CONSTANT.actList.get(i).finish();
        }
    }
}
