package com.eattle.phoket;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.eattle.phoket.helper.DatabaseHelper;
import com.eattle.phoket.view.CustomViewPager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_TAG = "MAIN_ACTIVITY";

    private final static int STATE_RUNNING = 0;
    private final static int STATE_SELECT = 1;
    private final static int STATE_OPTION = 2;
    private boolean mIsClassifying = false;


    //UI 관련 변수
    private static CustomViewPager mViewPager;
    private static Adapter mAdapter;
    private static FloatingActionButton mFAB;


    //파일 시스템 관련 변수
//    FileSystem fileSystem;
//    private UsbDeviceHost usbDeviceHost;
//    private CachedBlockDevice blockDevice;


    //service에 메시지를 보내기 위해 데이터를 담는 intent
    private Intent mService;
    //service로 부터 메시지를 받아서 UI작업 하는 broadcastreceiver
    private ClassificationReceiver mClassificationReceiver;

    ActionMode mActionMode;

    int state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //스마트폰 화면 크기를 구한다(이미지 최적화-out of memory 방지를 위해)
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        CONSTANT.screenWidth = metrics.widthPixels;
        CONSTANT.screenHeight = metrics.heightPixels;

        final Button toUSB = (Button) findViewById(R.id.toUSB);
        toUSB.setVisibility(View.GONE);//하단에 USB 버튼을 일단 없앤다

        mViewPager = (CustomViewPager) findViewById(R.id.pager);
        if (mViewPager != null) {
            setupViewPager(mViewPager);
        }


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mViewPager = (CustomViewPager) findViewById(R.id.pager);
        if (mViewPager != null) {
            setupViewPager(mViewPager);
        }

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        state = STATE_RUNNING;

        //우측 하단 FAB
        //롱클릭 했을 때만 보여짐
        mFAB = (FloatingActionButton)findViewById(R.id.fab);

        mFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                state = STATE_OPTION;
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                Fragment fr = SelectOptionFragment.newInstance(((Section1) (mAdapter.getItem(0))).selected);
                fragmentTransaction.add(R.id.fragment, fr, "Option");
                fragmentTransaction.commit();

                Animation anim = android.view.animation.AnimationUtils.loadAnimation(mFAB.getContext(), R.anim.fab_out);
                anim.setInterpolator(new FastOutSlowInInterpolator());
                anim.setDuration(200L);
                anim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mFAB.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                mFAB.startAnimation(anim);

            }
        });


        //데이터베이스 OPEN
        DatabaseHelper db = DatabaseHelper.getInstance(getApplicationContext());

        if (db.getGuide() == 0 && GUIDE.GUIDE_STEP==0) {//앱 최초 실행시
            //더미데이터 삭제(가이드 도중에 앱이 종료된 경우를 위해)
            db.deleteAllFolder();
            db.deleteAllMedia();
            db.deleteAllMediaTag();
            db.deleteAllTag();

            GUIDE.guide_initiate(MainActivity.this);
            //GUIDE.GUIDE_STEP = 0;
            //GUIDE.GUIDE_STEP++;
        }

        //서비스로부터 오는 브로드케스트를 캐치하기위해
        //메인에 리시버를 등록
        IntentFilter statusIntentFilter = new IntentFilter(CONSTANT.BROADCAST_ACTION);
        statusIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        mClassificationReceiver = new ClassificationReceiver();
        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(
                mClassificationReceiver,
                statusIntentFilter);


    }

    /***************** Menu 부분 **********************/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            state = STATE_SELECT;

            mode.setTitle("사진을 선택해주세요");
            mode.setSubtitle("1개 선택됨");
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_action_select, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            mViewPager.setPagingDisabled();
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
//                case R.id.cancel_action:
//                    shareCurrentItem();
//                    mode.finish(); // Action picked, so close the CAB
//                    return true;
//                default:
//                    return false;
            }
            return false;
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {

            mViewPager.setPagingEnabled();
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fm.beginTransaction();
            Fragment fr = fm.findFragmentByTag("Option");
            if(fr != null) {
                fragmentTransaction.remove(fr);
                fragmentTransaction.commit();
            }
            mActionMode = null;

            if(state != STATE_RUNNING){
                state = STATE_RUNNING;
                mFAB.setVisibility(View.INVISIBLE);
                ((Section1)(mAdapter.getItem(0))).initialize();
            }

            //데이터베이스 OPEN
            DatabaseHelper db = DatabaseHelper.getInstance(MainActivity.this);
            if(db.getGuide() == 0 && GUIDE.GUIDE_STEP>=7){
                GUIDE.guide_eight(MainActivity.this);

                db.createGuide(1);//가이드 종료했다는 표시
                GUIDE.GUIDE_STEP = -1;
                //더미데이터 삭제
                db.deleteAllFolder();
                db.deleteAllMedia();
                db.deleteAllMediaTag();
                db.deleteAllTag();
                deleteAllCard();
            }


        }
    };

    /***************** 기본 activity 관련 함수 부분 **********************/


    @Override
    public void onStart() {
        super.onStart();
        //서비스 등록
        mService = new Intent(this, ServiceOfPictureClassification.class);
        startService(mService);

    }

    @Override
    protected void onResume() {
        super.onResume();

        DatabaseHelper db = DatabaseHelper.getInstance(MainActivity.this);
        if(db.getGuide() == 0 && GUIDE.GUIDE_STEP==6){
            GUIDE.guide_six(MainActivity.this);
            //GUIDE.GUIDE_STEP++;
        }/*
            if(db.getGuide() == 0 && GUIDE.GUIDE_STEP==8){//마지막 가이드
                GUIDE.guide_eight(MainActivity.this);
                db.createGuide(1);//가이드 종료
                db.deleteAllFolder();
                db.deleteAllMedia();
                db.deleteAllMediaTag();
                db.deleteAllTag();
            }*/

    }

    public void deleteAllCard(){
        ((Section1)(mAdapter.getItem(0))).initialize();
        ((Section2)(mAdapter.getItem(1))).initialize();
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        if(mIsClassifying || state != STATE_RUNNING)  return;

        if(CONSTANT.FLAG_REFRESH) {
            Log.d(EXTRA_TAG,"FLAG_REFRESH");
            ((Section1) (mAdapter.getItem(0))).initialize();
            ((Section2) (mAdapter.getItem(1))).initialize();

            CONSTANT.FLAG_REFRESH = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Glide.get(this).clearMemory();
        Glide.get(this).trimMemory(ComponentCallbacks2.TRIM_MEMORY_MODERATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        //stopService(mService);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (mClassificationReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mClassificationReceiver);
            mClassificationReceiver = null;
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){//백버튼을 통제(비밀번호 유지를 위해)
            DatabaseHelper db = DatabaseHelper.getInstance(MainActivity.this);
            if (db.getGuide() == 0)//가이드 도중에
                return true;//백버튼을 막는다

            //선택된 것을 두번째로 끔
            if(state != STATE_RUNNING){
                return true;
//                state = STATE_RUNNING;
//                mFAB.setVisibility(View.INVISIBLE);
//                ((Section1)(mAdapter.getItem(0))).initialize();
            }else{
                wantCapicUSB();
            }

            return false;

        }
        return true;
    }

    /***************** ViewPager 부분 **********************/
    private void setupViewPager(CustomViewPager viewPager) {
        viewPager.setPagingEnabled();
        mAdapter = new Adapter(getSupportFragmentManager());
        mAdapter.addFragment(new Section1(), "모아보기");
        mAdapter.addFragment(new Section2(), "스토리");
        mAdapter.addFragment(new Section3(), "포켓");
        viewPager.setAdapter(mAdapter);

        viewPager.setOffscreenPageLimit(2);

    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }

    //fragment에서 select할 수 있는 카드를 길게 눌렀을 경우 호출하는 함수
    //actionmode를 selectmode로 바꾸고
    //fab를 노출한다
    public void setSelectMode(){
        Animation anim;

        switch (state){
            case STATE_RUNNING:
                anim = android.view.animation.AnimationUtils.loadAnimation(mFAB.getContext(), R.anim.fab_in);
                anim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        state = STATE_SELECT;
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mFAB.setVisibility(View.VISIBLE);
                        mActionMode = startSupportActionMode(mActionModeCallback);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                anim.setInterpolator(new FastOutSlowInInterpolator());
                anim.setDuration(200L);
                mFAB.startAnimation(anim);

                break;
            case STATE_OPTION:
                anim = android.view.animation.AnimationUtils.loadAnimation(mFAB.getContext(), R.anim.fab_in);
                anim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        state = STATE_SELECT;
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mFAB.setVisibility(View.VISIBLE);
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                anim.setInterpolator(new FastOutSlowInInterpolator());
                anim.setDuration(200L);
                mFAB.startAnimation(anim);

                break;
            case STATE_SELECT:
                anim = android.view.animation.AnimationUtils.loadAnimation(mFAB.getContext(), R.anim.fab_out);
                anim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        state = STATE_RUNNING;
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mFAB.setVisibility(View.INVISIBLE);
                        mActionMode.finish();
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                anim.setInterpolator(new FastOutSlowInInterpolator());
                anim.setDuration(200L);
                mFAB.startAnimation(anim);

                break;
        }

    }

    /***************** Classification Service 부분 **********************/
    //서비스로 메세지를 보낸다(MainActivity -> ServiceOfPictureClassification)
    //bind-> start로 바뀌면서 intent를 보내는 것으로 바뀜
    public void sendMessageToService(int typeOfMessage) {
        mService = new Intent(this, ServiceOfPictureClassification.class);
        mService.putExtra("what", typeOfMessage);
        startService(mService);
    }



    //서비스로부터 오는 메세지를 처리한다(ServiceOfPictureClassification -> MainActivity)
    private class ClassificationReceiver extends BroadcastReceiver {

        private ClassificationReceiver() {

        }

        @Override
        public void onReceive(Context context,final Intent intent) {
            int data;
            DatabaseHelper db = DatabaseHelper.getInstance(MainActivity.this);
            switch (intent.getIntExtra(CONSTANT.EXTENDED_DATA_STATUS,
                    CONSTANT.END_OF_PICTURE_CLASSIFICATION)) {

                // Logs "started" state
                case CONSTANT.END_OF_PICTURE_CLASSIFICATION:
                    //pictureDialog.dismiss();
                    ((Section1)(mAdapter.getItem(0))).setRunning();
                    ((Section2)(mAdapter.getItem(1))).setRunning();
                    ((Section3)(mAdapter.getItem(2))).initialize();


                    Snackbar s = Snackbar.make(findViewById(R.id.main_content), "사진 정리가 완료되었습니다!", Snackbar.LENGTH_LONG);
                    s.getView().setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    s.setAction("Action", null).show();

                    mIsClassifying = false;

                    //wantBackUp();
                    //exportDB();//Sqlite DB 추출(USB와의 동기화를 위해)
                    break;
                case CONSTANT.END_OF_SINGLE_STORY://하나의 스토리가 정리 되었을 때
                    data = intent.getIntExtra(CONSTANT.EXTENDED_DATA, -1);
                    if(data == -1)    return;

                    ((Section1)(mAdapter.getItem(0))).addSingleCard(db.getFolder(data));
                    ((Section2)(mAdapter.getItem(1))).addSingleCard(db.getFolder(data));


                    break;

                case CONSTANT.RECEIPT_OF_PICTURE_CLASSIFICATION://서비스가 사진 정리를 시작했다는 메세지
                    //사진정리중이면 1, 아니면 0이 들어있음
                    data = intent.getIntExtra(CONSTANT.EXTENDED_DATA, 0);
                    if(data == 1){
                        ((Section1)(mAdapter.getItem(0))).setLoading();
                        ((Section2)(mAdapter.getItem(1))).setLoading();
                        ((Section3)(mAdapter.getItem(2))).setLoading();


                        Snackbar.make(findViewById(R.id.main_content), "사진을 정리 중입니다", Snackbar.LENGTH_SHORT)
                                .setAction("Action", null).show();

                        mIsClassifying = true;
                        //((Section2)(mAdapter.getItem(1))).addSingleCard(db.getFolder(id));
                    }

                    break;

                case CONSTANT.END_OF_SINGLE_STORY_GUIDE:
                    data = intent.getIntExtra(CONSTANT.EXTENDED_DATA, -1);
                    if(data == -1)    return;

                    ((Section1)(mAdapter.getItem(0))).addSingleCard(db.getFolder(data));
                    ((Section2)(mAdapter.getItem(1))).addSingleCard(db.getFolder(data));

                    ((Section1)(mAdapter.getItem(0))).setRunning();
                    ((Section2)(mAdapter.getItem(1))).setRunning();
                    ((Section3)(mAdapter.getItem(2))).initialize();


                    Snackbar s_ = Snackbar.make(findViewById(R.id.main_content), "사진 정리가 완료되었습니다!", Snackbar.LENGTH_LONG);
                    s_.getView().setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    s_.setAction("Action", null).show();

                    mIsClassifying = false;

                    GUIDE.guide2(MainActivity.this);
                    //GUIDE.GUIDE_STEP++;

                    break;
                default:
                    break;
            }
        }
    }



    /***************** Dialog 부분 **********************/
    public void wantCapicUSB() {//앱을 종료하려 할때, USB 구매의사를 묻는다.
        new MaterialDialog.Builder(this)
                .title(R.string.exitComment)
                .positiveText(R.string.negativeAnswer)
                .negativeText(R.string.positiveAnswer)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        moveTaskToBack(true);
                        finish();

                    }

                })
                .show();
    }

    public void wantBackUp() {//사진 정리가 완료되고 USB에 백업된 후에, 스마트폰에서 사진을 지울 것인지 물어본다

        AlertDialog.Builder d = new AlertDialog.Builder(this);
        d.setTitle("사진이 정리되었습니다!");

        DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        //스마트폰에서 사진들을 삭제한다
                        break;
                }
            }
        };
        d.setPositiveButton("확인", l);
        d.show();

    }

}
