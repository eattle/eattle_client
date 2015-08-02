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
        Log.d(TAG, "onCreate() 호출");
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
        Log.d("AlbumFullPicture", mMediaList.size()+" " + Id + " " + mediaId + " " + kind + " "+tagName+" " + initialMediaPosition + "!!!!");
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

                Log.d(TAG, "onpageselected호출(현재 position : " + position + ")");
                Log.d(TAG, "StoryStartFragment : " + (storyStartFragment == null));

                makeStoryStartFragment(position);
                if (position >= 0)
                    ((StoryStartFragment) (getFragmentManager().findFragmentById(R.id.storyStart))).showBlur(1);


                //Log.d(TAG, "StoryStartFragment : " + (storyStartFragment == null) + " " + storyStartFragment.getView());
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
                Log.d(TAG, "onPageSelected에서의 setTabToTag");
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
            Log.d(TAG, "getItemPosition 호출 : " + object);
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
            Log.d("albumFullActivity", "getItem 호출(현재 position : " + position + ")");

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
            Log.d(TAG, "지우려는 viewpager 포지션 : " + position);
            if (initialMediaPosition == -1) {  //스토리 제목부터 시작한 경우
                position--;//인덱스 -1이 스토리시작화면!!
            }

            if (position >= 0 && position < mediaList.size())
                mediaList.remove(position);
            else
                Log.d(TAG, "지우려는 viewpager 포지션 : " + position + "에 에러가 있음");

            this.notifyDataSetChanged();

            //태그 상태 업데이트
            if (mediaList.size() != 0) {//아직 사진이 남아 있을 때
                Log.d(TAG, "removeView에서의 setTabToTag");

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
        Log.d(TAG, "makeStoryStartFragment(position) 호출");
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
            Log.d(TAG, "makeStoryStartFragment(position) -- storyStartFragment == null? " + (storyStartFragment == null));
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

/*    //이미지 최적화 작업(ex. inSampleSize)등을 백그라운드에서 하도록 하는 클래스
    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private String TAG = "BitmapWorkerTask";
        public final WeakReference<ImageView> imageViewReference;

        public String path = "";
        int imageIdForTaskExecute;

        public BitmapWorkerTask(ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        public BitmapWorkerTask(ImageView imageView, int imageIdForTaskExecute) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
            this.imageIdForTaskExecute = imageIdForTaskExecute;
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(String... params) {
            Log.d(TAG, "doInBackground() 호출");
            //final String imageKey = params[0];//params[0]에서 이름만 추출하기

            // Check disk cache in background thread
            // Bitmap bitmapFromDiskCache = getBitmapFromDiskCache(imageKey);

            if (imageViewReference != null) {
                Log.d(TAG, "[imageViewReference != null] imageView가 존재하므로 큰(원본) 이미지로 대체");
                final Bitmap bitmap = CONSTANT.decodeSampledBitmapFromPath(params[0], CONSTANT.screenWidth, CONSTANT.screenHeight);
                // Add final bitmap to caches
                //addBitmapToCache(imageKey, bitmap);
                return bitmap;//큰 이미지를 로딩한다
            } else {
                Log.d(TAG, "[imageViewReference == null] imageView가 존재하지 않음");
                // Add final bitmap to caches
                //addBitmapToCache(imageKey, bitmap);
                return null;
            }

        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {//bitmap은 새로 붙이려는 큰 이미지
            Log.d(TAG, "onPostExecute() 호출");
            //if (isCancelled()) {
            if (imageViewReference == null) {
                Log.d(TAG, "onPostExecute() isCancelled");
                bitmap.recycle();
                bitmap = null;
            }

            if (imageViewReference != null && bitmap != null) {

                final ImageView imageView = imageViewReference.get();
                //작은 이미지 삭제
                Bitmap bitmap_ = null;
                Drawable d = null;
                if (imageView != null) {
                    //(imageViewReference.get()).setImageBitmap(null); 해야하는데 오류뜸.

                    d = imageView.getDrawable();
                    if (d instanceof BitmapDrawable) {
                        bitmap_ = ((BitmapDrawable) d).getBitmap();//bitmap_은 지우려는 작은 이미지
                    }
                }
                final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
                if (this == bitmapWorkerTask && imageView != null) {
                    if (bitmap != null && !bitmap.isRecycled()) {//이미 recycle된 사진은 쓰지 않는다!
                        imageView.setImageBitmap(bitmap);//큰 사진을 로드한다
                    }
                    //!!!!여기 !!!!
                }
                if (bitmap_ != null && !bitmap_.isRecycled()) {
                    Log.d(TAG, "기존의 작은 이미지 삭제]" + bitmap_.getByteCount() + " recycle() & gc() 호출");
                    bitmap_.recycle();
                    System.gc();
                    Runtime.getRuntime().gc();//비트맵을 확실하게 지운다
                    bitmap_ = null;
                    d.setCallback(null);
                }
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    //Concurrency 문제를 다루기 위한 클래스
    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap,
                             BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    //이미지 로딩을 위한 callback interface
    public interface CallbackForImageLoading {
        public void callbackMethod(ImageView imageView, AsyncDrawable asyncDrawable, BitmapWorkerTask task, String path);
    }

    public class EventRegistration {
        private CallbackForImageLoading callbackEvent;

        //생성자
        public EventRegistration(CallbackForImageLoading event) {
            callbackEvent = event;
        }

        //콜백함수 실행을 위한 함수
        public void doWork(ImageView imageView, AsyncDrawable asyncDrawable, BitmapWorkerTask task, String path) {
            callbackEvent.callbackMethod(imageView, asyncDrawable, task, path);
        }
    }

    CallbackForImageLoading callbackForImageLoading = new CallbackForImageLoading() {
        @Override
        public void callbackMethod(ImageView imageView, AsyncDrawable asyncDrawable, BitmapWorkerTask task, String path) {
            imageView.setImageDrawable(asyncDrawable);
            task.execute(path);
        }
    };


//    public void loadBitmap(final String path, final ImageView imageView, int mediaId, int imageIdForTaskExecute) {
//
//
//             /*
//            //original-------------------------------------------------------------------
//            try {
//                mPlaceHolderBitmap = CONSTANT.getThumbnail(cr, path);//안드로이드 내장 썸네일을 가져온다
//            }catch(Exception e){
//                e.printStackTrace();
//            }
//            if(mPlaceHolderBitmap == null){//내장 썸네일이 혹시 존재하지 않을 경우에만
//                Log.d(TAG, "썸네일이 존재하지 않아 직접 만들었습니다");
//                mPlaceHolderBitmap = CONSTANT.decodeSampledBitmapFromPath(path, 10);//직접 만든다
//            }
//            final AsyncDrawable asyncDrawable = new AsyncDrawable(getResources(), mPlaceHolderBitmap, task);
//            imageView.setImageDrawable(asyncDrawable);
//            EventRegistration eventRegistration = new EventRegistration(callbackForImageLoading);
//            //eventRegistration.doWork(task,path_);
//            task.execute(path);
//            */
//
//            /* 2차 수정(glide 대체 전)
//            //작은 이미지(썸네일)처리가 완료되면,
//            //imageView에 비트맵을 붙이고 task.execute()을 해주는 부분
//            final Handler handler = new Handler(){
//
//                public void handleMessage(Message msg){
//                    switch(msg.what){
//                        case CONSTANT.END_OF_DECODING_THUMBNAIL:
//                            Log.d(TAG,"썸네일 Decoding 완료");
//                            final AsyncDrawable asyncDrawable = new AsyncDrawable(getResources(), mPlaceHolderBitmap, task);
//                            imageView.setImageDrawable(asyncDrawable);
//                            task.execute(path);
//                            break;
//                    }
//                }
//
//            };
//
//            //썸네일 불러오기는 쓰레드를 생성해서 처리한다.
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        mPlaceHolderBitmap = CONSTANT.getThumbnail(cr, path);//안드로이드 내장 썸네일을 가져온다
//                    }catch(Exception e){
//                        Log.d(TAG, "CONSTANT.getThumbnail() 오류");
//                        e.printStackTrace();
//                    }
//                    if(mPlaceHolderBitmap == null){//내장 썸네일이 혹시 존재하지 않을 경우에만
//                        Log.d(TAG, "썸네일이 존재하지 않아 직접 만들었습니다");
//                        mPlaceHolderBitmap = CONSTANT.decodeSampledBitmapFromPath(path, 10);//직접 만든다
//                    }
//
//                    Message m = Message.obtain(null, CONSTANT.END_OF_DECODING_THUMBNAIL);
//                    handler.sendMessage(m);//썸네일 생성이 완료되었다는 메세지
//                }
//            }).start();
//            */
//
//
//        //작은 이미지(썸네일)처리가 완료되면,
//        //imageView에 비트맵을 붙이고 task.execute()을 해주는 부분
//        final Handler handler = new Handler() {
//
//            public void handleMessage(Message msg) {
//                switch (msg.what) {
//                    case CONSTANT.END_OF_DECODING_THUMBNAIL:
//                        Log.d(TAG, "썸네일 Decoding 완료");
//                        Drawable mPlaceHolder = new BitmapDrawable(getResources(), mPlaceHolderBitmap);
//                        Glide.with(getApplicationContext())
//                                .load(path)
//                                .placeholder(mPlaceHolder)
//                                .into(imageView);
//                        //.placeholder(mPlaceHolder)
//                        break;
//                }
//            }
//        };
//
//        //썸네일 불러오기는 쓰레드를 생성해서 처리한다.
//        DatabaseHelper db = DatabaseHelper.getInstance(AlbumFullActivity.this);
//        final Media media = db.getMediaById(mediaId);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    mPlaceHolderBitmap = CONSTANT.getThumbnail(cr, path, media.getThumbnail_path(), media.getId());//안드로이드 내장 썸네일을 가져온다
//                } catch (Exception e) {
//                    Log.d(TAG, "CONSTANT.getThumbnail() 오류");
//                    e.printStackTrace();
//                }
//                if (mPlaceHolderBitmap == null) {//내장 썸네일이 혹시 존재하지 않을 경우에만
//                    Log.d(TAG, "썸네일이 존재하지 않아 직접 생성");
//                    mPlaceHolderBitmap = CONSTANT.decodeSampledBitmapFromPath(path, 10);//직접 만든다
//                    int degree = CONSTANT.GetExifOrientation(media.getPath());//사진 방향은 originalPath로만 알 수 있다
//                    mPlaceHolderBitmap = CONSTANT.GetRotatedBitmap(mPlaceHolderBitmap, degree);
//                }
//
//                Message m = Message.obtain(null, CONSTANT.END_OF_DECODING_THUMBNAIL);
//                handler.sendMessage(m);//썸네일 생성이 완료되었다는 메세지
//
//            }
//        }).start();
//
//
//    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop() 호출");

        Glide.get(this).clearMemory();
        Glide.get(this).trimMemory(ComponentCallbacks2.TRIM_MEMORY_COMPLETE);
        super.onStop();
    }

/*    //동일한 imageview를 가리키고 있는 여러 작업들이 있다면, 더 오래된 작업을 중단한다
    public static boolean cancelPotentialWork(String path, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final String bitmapData = bitmapWorkerTask.path;
            // If bitmapData is not yet set or it differs from the new data
            if (!bitmapData.equals("") || bitmapData.equals(path)) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    //특정 imageview에 관련된 task들을 검색한다
    public static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }
*/
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

    /*
    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSavedInstanceState() 호출");
        super.onSaveInstanceState(outState);


        //현재 보고있는 사진 + 좌우 사진의 비트맵 저장

        //storyStartFragment 저장
        finish();

    }
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "onRestoreInstanceState() 호출");
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);

        // Restore state members from saved instance

    }
*/
    @Override
    protected void onUserLeaveHint() {
        Log.d(TAG, "onUserLeaveHint() 호출");

        DatabaseHelper db = DatabaseHelper.getInstance(AlbumFullActivity.this);
        //AlbumFullActivity에서 '가이드 도중'에 홈버튼을 누를 경우, MainActivity로 이동해둔다
        if (db.getGuide() == 0) {
            Log.d(TAG, "MainActivity로 이동");
            GUIDE.GUIDE_STEP = 5;
            for (int i = 0; i < CONSTANT.actList.size(); i++)
                CONSTANT.actList.get(i).finish();
        }
    }
}
