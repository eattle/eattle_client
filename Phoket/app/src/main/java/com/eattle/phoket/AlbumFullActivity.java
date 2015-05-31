package com.eattle.phoket;


import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;

import android.os.Bundle;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.LruCache;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.eattle.phoket.device.CachedBlockDevice;
import com.eattle.phoket.helper.DatabaseHelper;
import com.eattle.phoket.model.Folder;
import com.eattle.phoket.model.Media;
import com.eattle.phoket.model.Tag;

import java.io.File;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

//스토리 그리드뷰에서 특정 사진을 클릭했을 때, 뷰페이저를 만들어주는 부분
public class AlbumFullActivity extends ActionBarActivity {

    DatabaseHelper db;
    static List<Media> mMediaList;
    int initialMediaPosition;
    //USB에서 사진을 불러오기 위한 변수
    static FileSystem fileSystem;
    static int totalPictureNum;

    //'스토리 시작'을 통해 들어왔을 경우
    static String titleName;
    static String titleImagePath;
    static String titleImageId;
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


    private LruCache<String, Bitmap> mMemoryCache;
    private Bitmap mPlaceHolderBitmap = null;//이미지를 처리하는 동안 보여줄 이미지
    //Set<SoftReference<Bitmap>> mReusableBitmaps;//이미지 재활용
    //private LruCache<String, BitmapDrawable> mMemoryCache;//캐시
    ImageView blurImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        CONSTANT.actList.add(this);
/*
        //이미지 캐싱을 위한 과정
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        //MaxMemory에서 1/8을 사용한다
        final int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than number of items.
                return bitmap.getByteCount() / 1024;
            }
        };*/

        //mPlaceHolderBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        db = DatabaseHelper.getInstance(getApplicationContext());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_full);


        Intent intent = getIntent();
        mMediaList = intent.getParcelableArrayListExtra("mediaList");
        Id = intent.getIntExtra("IDForStoryOrTag", 0);
        mediaId = intent.getIntExtra("mediaId", -1);
        kind = intent.getIntExtra("kind", 0);
        tagName = intent.getStringExtra("tagName");
        initialMediaPosition = intent.getIntExtra("position", 0);
        Log.d("AlbumFullPicture", Id + " " + mediaId + " " + kind + " " + initialMediaPosition + "!!!!");
        if (kind == CONSTANT.FOLDER) {//스토리를 타고 들어왔을 경우
            Folder folder = db.getFolder(Id);
            totalPictureNum = folder.getPicture_num();
            titleName = folder.getName();
            titleImagePath = folder.getImage();
            titleImageId = folder.getThumbNail_name();
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
            titleImageId = String.valueOf(mMediaList.get(0).getId());
            totalPictureNum = mMediaList.size();
        } else if (kind == CONSTANT.TAG) {//사용자 태그 그리드뷰에서 들어왔을 경우
            Tag t = db.getTagByTagId(Id);
            //Media mediaByTag = db.getMediaById(intent.getIntExtra("mediaId", -1));
            mMediaList = db.getAllMediaByTagId(Id);

            titleName = t.getName();
            titleImagePath = mMediaList.get(0).getPath();
            titleImageId = String.valueOf(mMediaList.get(0).getId());
            totalPictureNum = mMediaList.size();
        }
        /*
        totalPictureNum = intent.getIntExtra("totalPictureNum", 0);
        if (initialMediaPosition == -1) {//'스토리 시작'버튼으로 들어왔을 경우
            titleName = intent.getStringExtra("titleName");
            titleImagePath = intent.getStringExtra("titleImagePath");
        }
        titleImageId = intent.getStringExtra("titleImageId");
        */

        fileSystem = FileSystem.getInstance();

        //뷰페이저 생성
        mViewPager = (ViewPager) findViewById(R.id.albumFullViewPager);
        touchImageAdapter = new TouchImageAdapter(this, mMediaList,getSupportFragmentManager());
        mViewPager.setAdapter(touchImageAdapter);//뷰페이저 어댑터 설정
        if (initialMediaPosition != -1)//-1이면 스토리 처음부터 시작(제목화면부터)
            mViewPager.setCurrentItem(initialMediaPosition);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                Log.d("asdf","onpageselected호출 "+position);
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

                if(position != -1){// ~년~월~일의 스토리 -> 없앰
                    storyStartFragment.getView().findViewById(R.id.storyStartDate).setVisibility(View.INVISIBLE);
                    storyStartFragment.getView().findViewById(R.id.storyStartTitle).setVisibility(View.INVISIBLE);
                }
                //추천스토리를 관리하는 부분-----------------------------------------------
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                if(position == mMediaList.size()){
                    if (isTagAppeared == 1){
                        pushTabToTag(mMediaList.get(position-1), position-1);
                        setPlacePopup(mMediaList.get(position-1));
                    }
                    storyRecommendFragment = StoryRecommendFragment.newInstance(mMediaList.get(0).getFolder_id());
                    fragmentTransaction.add(R.id.frameForStoryRecommend, storyRecommendFragment, "storyRecommendFragment");
                    fragmentTransaction.setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    fragmentTransaction.commit();
                    return;
                }
                else if(storyRecommendFragment != null){//마지막 페이지가 아니면서 기존에 추천 스토리를 만들어 놓은게 있으면
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
                if(position < 0 && getFragmentManager().findFragmentById(R.id.storyStart) != null){
                    ((StoryStartFragment) (getFragmentManager().findFragmentById(R.id.storyStart))).showBlur(positionOffset);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });


    }

    //뷰페이저
    class TouchImageAdapter extends FragmentStatePagerAdapter {

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
            Log.d("TouchImageAdapter", "getItemPosition 호출 : " + object);
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
        public android.support.v4.app.Fragment getItem(int position){
            Log.d("asdf","getItem 호출");
            if (initialMediaPosition == -1) {  //스토리 제목부터 시작해야 하는 경우
                Log.d("asdf","여기!!!");
                position--;//첫화면에 제목화면을 넣기 위해.
            }
            if (storyStartFragment == null) {//항상 뒤에 배경사진을 깔아둔다

                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                storyStartFragment = StoryStartFragment.newInstance(titleImagePath, titleName, kind,position);
                fragmentTransaction.add(R.id.storyStart, storyStartFragment, "StoryStartFragment");
                fragmentTransaction.commit();

            }
            if (position == -1 || position == mediaList.size())//스토리 시작 화면 또는 추천스토리 부분
                return StoryMainFragment.newInstance(null,position,mediaList.size());//결과적으로 아무것도 반환되지 않도록 한다
            return StoryMainFragment.newInstance(mediaList.get(position),position,mediaList.size());
        }
        public void removeView(int position) {//뷰페이저 업데이트를 위해 선언
            Log.d("touchImageAdapter", "지우려는 viewpager 포지션 : " + position);
            mediaList.remove(position);
            this.notifyDataSetChanged();
        }

/*
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

            if(initialMediaPosition == -1)
                position--;
            BitmapDrawable d = (BitmapDrawable)((ImageView)findViewById(R.id.pagerImage)).getDrawable();
            if(d != null) {
                Bitmap b = d.getBitmap();
                b.recycle();
                System.gc();
            }
            super.destroyItem(container,position,object);

            if(initialMediaPosition == -1)
                position--;
            Log.d("AlbumFullActivity",position+" 에 대한 destroyItem 호출");
            if(position != -1) {
                android.support.v4.app.FragmentManager manager = ((StoryMainFragment)object).getFragmentManager();
                android.support.v4.app.FragmentTransaction trans = manager.beginTransaction();
                trans.remove((StoryMainFragment) object);
                trans.commit();
            }
        }*/
    }

    Fragment isThereTabToTagHere() {
        return getFragmentManager().findFragmentById(R.id.tagLayout);
    }

    /*
    void setTabToTag(Media m) {
        if (isThereTabToTagHere() != null) {
            FragmentTransaction tr = getFragmentManager().beginTransaction();
            TagsOverAlbum ttt = TagsOverAlbum.newInstance(m);
            tr.replace(R.id.tagLayout, ttt, "TabToTag");
            tr.setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            tr.commit();
        }
    }*/

    //백버튼을 눌렀을 때, 메모리 정리를 한다
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK://백버튼을 통제(비밀번호 유지를 위해)
                //불필요한 메모리 정리---------------------------------------------------------------
                AlbumFullActivity.mViewPager = null;
                AlbumFullActivity.touchImageAdapter = null;
                System.gc();
                Runtime.getRuntime().gc();
                ImageView storyStartImage = (ImageView)findViewById(R.id.storyStartImage);
                ImageView blurImage = (ImageView)findViewById(R.id.blurImage);
                if(storyStartImage != null) {
                    Drawable d = storyStartImage.getDrawable();
                    if (d instanceof BitmapDrawable) {
                        Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
                        if(bitmap != null) {
                            Log.d("StoryRecommendFragment", bitmap.getByteCount() + " recycle() & gc() 호출");
                            bitmap.recycle();
                            bitmap = null;
                        }
                    }
                    d.setCallback(null);
                    storyStartImage.setImageBitmap(null);
                }
                if(blurImage != null) {
                    Drawable d = blurImage.getDrawable();
                    if (d instanceof BitmapDrawable) {
                        Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
                        if(bitmap != null) {
                            Log.d("StoryRecommendFragment", bitmap.getByteCount() + " recycle() & gc() 호출");
                            bitmap.recycle();
                            bitmap = null;
                        }
                    }
                    d.setCallback(null);
                    blurImage.setImageBitmap(null);
                }
                System.gc();//garbage collector
                Runtime.getRuntime().gc();//garbage collector
                finish();//현재 띄워져 있던 albumFullActivity 종료(메모리 확보를 위해)
                return false;
        }
        return true;
    }

    void setTabToTag(Media m, int position) {
        if (isThereTabToTagHere() != null) {
            FragmentTransaction tr = getFragmentManager().beginTransaction();
            TagsOverAlbum ttt = TagsOverAlbum.newInstance(m, position, totalPictureNum);
            tr.replace(R.id.tagLayout, ttt, "TabToTag");//기존의 fragment의 내용을 변경한다(position ,totalPictureNum)
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
            TagsOverAlbum ttt = TagsOverAlbum.newInstance(m, position, totalPictureNum);
            tr.add(R.id.tagLayout, ttt, "TabToTag");
            tr.add(R.id.exit, new StoryExitFragment(), "StoryExitFragment");
            tr.setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            tr.commit();

        }
    }

    /*
    void pushTabToTag(Media m,int position) {
        Fragment f;
        if ((f = isThereTabToTagHere()) != null) {
            FragmentTransaction tr = getFragmentManager().beginTransaction();
            //tagArrayList에 있는 모든 태그들을 삭제한다
            tr.remove(f);
            tr.setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            tr.commit();
        } else {
            FragmentTransaction tr = getFragmentManager().beginTransaction();
            TagsOverAlbum ttt = TagsOverAlbum.newInstance(m,position,totalPictureNum);
            tr.add(R.id.tagLayout, ttt, "TabToTag");
            tr.setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            tr.commit();
        }
    }*/

    void setPlacePopup(Media m) {
        //장소명이 존재하면 태그로 추가할지 묻는다
        if (!m.getPlaceName().equals("")) {
            //일단 m.getPlaceName()이 태그 목록에 있는지 확인한다
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


    //이미지 최적화 작업(ex. inSampleSize)등을 백그라운드에서 하도록 하는 클래스
    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private String path = "";
        public BitmapWorkerTask(ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(String... params) {
            final Bitmap bitmap = CONSTANT.decodeSampledBitmapFromPath(params[0], CONSTANT.screenWidth, CONSTANT.screenHeight);
            return bitmap;
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }

            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
                if (this == bitmapWorkerTask && imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
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

    public void loadBitmap(String path, ImageView imageView) {
        if (cancelPotentialWork(path, imageView)) {
            final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(getResources(), mPlaceHolderBitmap, task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(path);
        }
    }
    /*
    public void loadBitmap(String path, ImageView imageView) {
        Log.d("AlbumFullActivity","loadBitmap 호출");
        final String imageKey = String.valueOf(path);

        final Bitmap bitmap = getBitmapFromMemCache(imageKey);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            if (cancelPotentialWork(path, imageView)) {
                final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
                final AsyncDrawable asyncDrawable = new AsyncDrawable(getResources(), mPlaceHolderBitmap, task);
                imageView.setImageDrawable(asyncDrawable);
                task.execute(path);
            }
        }
    }*/
    //동일한 imageview를 가리키고 있는 여러 작업들이 있다면, 더 오래된 작업을 중단한다
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
    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }
    /**
     이미지 캐싱을 통한 부드러운 스와이핑 제공
     */
    /*
    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }
    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }
*/




    private Bitmap fileoutimage(String outString, CachedBlockDevice blockDevice) {//USB -> 스마트폰
        //D  S   X
        //1220879 1870864 2133464

        int result[] = AlbumFullActivity.fileSystem.stringSearch(outString);
        byte[] dummyBuffer = new byte[(int) AlbumFullActivity.fileSystem.CLUSTERSPACESIZE];
        //1866136
        //result[0] = 4096;
        //result[0] = 6505;
        Log.d("xxxxxx", "result[0] " + result[0]);
        if (result[0] == -1) {
            Toast.makeText(this, "값이 잘못들어왔습니다", Toast.LENGTH_SHORT).show();
            return null;
        } else {

            byte resultbyte[] = new byte[result[4]];
            //int resultstringaddress = 6085;
            int resultstringaddress = result[0];
            //int resultaddress = readIntToBinary(result[0],result[1]+80,LOCATIONSIZE);

            int limit = 0;
            int bytecnt = 0;


            blockDevice.readBlock(resultstringaddress, dummyBuffer);

            while (resultstringaddress != 0) {

                int originalbyteAddress = AlbumFullActivity.fileSystem.readIntToBinary(resultstringaddress, limit, AlbumFullActivity.fileSystem.LOCATIONSIZE, dummyBuffer, blockDevice);

                blockDevice.readBlock(originalbyteAddress, AlbumFullActivity.fileSystem.buffer);
                for (int i = 0; i < AlbumFullActivity.fileSystem.CLUSTERSPACESIZE; i++) {
                    if (bytecnt < result[4]) {
                        resultbyte[bytecnt++] = AlbumFullActivity.fileSystem.buffer[i];
                    } else
                        break;
                }
                if (bytecnt >= result[4])
                    break;

                limit += AlbumFullActivity.fileSystem.LOCATIONSIZE;

                if (limit >= AlbumFullActivity.fileSystem.SPACELOCATION) {
                    resultstringaddress = AlbumFullActivity.fileSystem.readIntToBinary(resultstringaddress, AlbumFullActivity.fileSystem.NEXTLOCATION, AlbumFullActivity.fileSystem.LOCATIONSIZE, dummyBuffer, blockDevice);
                    blockDevice.readBlock(resultstringaddress, dummyBuffer);
                    limit = 0;
                }

            }


            Log.d("xxxxxx", "xxxxxxxxxxxx " + resultbyte);
            Log.d("xxxxxx", "xxxxxxxxxxxxxxxxxxx " + resultbyte.length);

            Toast.makeText(this, "1 " + resultbyte, Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "1 " + resultbyte.length, Toast.LENGTH_SHORT).show();

            Bitmap byteimage = BitmapFactory.decodeByteArray(resultbyte, 0, resultbyte.length);
            //imageView.setImageBitmap(byteimage);

            //imageView.setImageBitmap(resizeBitmapImageFn(byteimage,540));

            //Bitmap bitmap1 = BitmapFactory.decodeFile("/storage/emulated/0/DCIM/Camera/1.jpg");
            //imageView.setImageBitmap(resizeBitmapImageFn(bitmap1,540));
            return byteimage;
        }
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


}
/*

    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private int data = 0;

        public BitmapWorkerTask(ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        // Decode image in background.

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(String... params) {
            final Bitmap bitmap = CONSTANT.decodeSampledBitmapFromPath(params[0], CONSTANT.screenWidth, CONSTANT.screenHeight);
            return bitmap;
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }

            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                final BitmapWorkerTask bitmapWorkerTask =
                        getBitmapWorkerTask(imageView);
                if (this == bitmapWorkerTask && imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }
    public void loadBitmap(String path, ImageView imageView) {
        if (cancelPotentialWork(path, imageView)) {
            final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
            final AsyncDrawable asyncDrawable =
                    new AsyncDrawable(getResources(), mPlaceHolderBitmap, task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(path);
        }
    }

    //동일한 imageview를 가리키고 있는 여러 작업들이 있다면, 더 오래된 작업을 중단한다
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
    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap,
                             BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference =
                    new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }
 */