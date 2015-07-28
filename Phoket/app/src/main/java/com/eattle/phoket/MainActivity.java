package com.eattle.phoket;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks2;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
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
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.eattle.phoket.Card.manager.CardData;
import com.eattle.phoket.device.CachedBlockDevice;
import com.eattle.phoket.helper.DatabaseHelper;
import com.eattle.phoket.host.UsbDeviceHost;
import com.eattle.phoket.view.CustomViewPager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_TAG = "MAIN_ACTIVITY";

    private final static int STATE_RUNNING = 0;
    private final static int STATE_SELECT = 1;
    private final static int STATE_OPTION = 2;
    private boolean mIsClassifying = false;


    //UI 관련 변수
    private CustomViewPager mViewPager;
    private Adapter mAdapter;
    private FloatingActionButton mFAB;


    //파일 시스템 관련 변수
    FileSystem fileSystem;
    private UsbDeviceHost usbDeviceHost;
    private CachedBlockDevice blockDevice;

    //DB관련 변수
    DatabaseHelper db;

    //service에 메시지를 보내기 위해 데이터를 담는 intent
    private Intent mService;
    //service로 부터 메시지를 받아서 UI작업 하는 broadcastreceiver
    private ClassificationReceiver mClassificationReceiver;

    ActionMode mActionMode;

    int state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        /*StrictMode*/
        //StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
        //        .detectLeakedSqlLiteObjects()
        //        .detectLeakedClosableObjects()
        //        .penaltyLog()
        //        .penaltyDeath()
        //        .build());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //스마트폰 화면 크기를 구한다(이미지 최적화-out of memory 방지를 위해)
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        CONSTANT.screenWidth = metrics.widthPixels;
        CONSTANT.screenHeight = metrics.heightPixels;

        final Button toUSB = (Button) findViewById(R.id.toUSB);
        toUSB.setVisibility(View.GONE);//하단에 USB 버튼을 일단 없앤다

        /*FileSystem*/
        //fileSystem = FileSystem.getInstance();

        //usbDeviceHost = new UsbDeviceHost();
        //usbDeviceHost.start(this, new BlockDeviceApp() {
        //    @Override
        //    public void onConnected(BlockDevice originalBlockDevice) {
        //        CachedBlockDevice blockDevice = new CachedUsbMassStorageBlockDevice(originalBlockDevice);
//
        //        fileSystem.incaseSearchTable(blockDevice);
//
        //        CONSTANT.BLOCKDEVICE = blockDevice;//temp
        //        setBlockDevice(blockDevice);
        //        //USB가 스마트폰에 연결되었을 떄
        //        CONSTANT.ISUSBCONNECTED = 1;
        //        toUSB.setVisibility(View.VISIBLE);//USB가 연결되었으면 하단에 USB 버튼을 보여준다
        //        //fileSystem.delete(DatabaseHelper.DATABASE_NAME,blockDevice);
        //        //fileSystem.delete(DatabaseHelper.DATABASE_NAME+"tt",blockDevice);
//
        //    }
        //});

        //데이터베이스 OPEN

        db = DatabaseHelper.getInstance(getApplicationContext());

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

        if (db.getGuide() == 0 && GUIDE.GUIDE_STEP==0) {//앱 최초 실행시
            //더미데이터 삭제(가이드 도중에 앱이 종료된 경우를 위해)
            db.deleteAllFolder();
            db.deleteAllMedia();
            db.deleteAllMediaTag();
            db.deleteAllTag();

            GUIDE.guide_initiate(this);
            //GUIDE.GUIDE_STEP = 0;
            //GUIDE.GUIDE_STEP++;
        }


        //서비스로부터 오는 브로드케스트를 캐치하기위해
        //메인에 리시버를 등록
        IntentFilter statusIntentFilter = new IntentFilter(CONSTANT.BROADCAST_ACTION);
        statusIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        mClassificationReceiver = new ClassificationReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(
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

            if(db == null)
                db = DatabaseHelper.getInstance(MainActivity.this);
            if(db.getGuide() == 0 && GUIDE.GUIDE_STEP>=7){
                GUIDE.guide_eight(MainActivity.this);

                db.createGuide(1);//가이드 종료했다는 표시

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

        Log.d(EXTRA_TAG, "onStart");
    }

    @Override
    protected void onResume() {
        Log.d(EXTRA_TAG, "onResume() 호출");
        super.onResume();

        if(db == null)
            db = DatabaseHelper.getInstance(MainActivity.this);

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
        Log.d(EXTRA_TAG, "onRestart() 호출");
        if(mIsClassifying || state != STATE_RUNNING)  return;
        ((Section1)(mAdapter.getItem(0))).initialize();
        ((Section2)(mAdapter.getItem(1))).initialize();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(EXTRA_TAG,"onPause() 호출");
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
        Log.d(EXTRA_TAG, "onDestroy() 호출");
        if (mClassificationReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mClassificationReceiver);
            mClassificationReceiver = null;
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){//백버튼을 통제(비밀번호 유지를 위해)
            if(db == null)
                db = DatabaseHelper.getInstance(MainActivity.this);
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
        public void onReceive(Context context, Intent intent) {
            int data;
            switch (intent.getIntExtra(CONSTANT.EXTENDED_DATA_STATUS,
                    CONSTANT.END_OF_PICTURE_CLASSIFICATION)) {

                // Logs "started" state
                case CONSTANT.END_OF_PICTURE_CLASSIFICATION:
                    Log.d("IncomingHandler", "[MainActivity]message 수신! handleMessage() - END_OF_PICTURE_CLASSIFICATION || 'Service가 사진 정리를 완료했다는 메세지가 도착했습니다' ");
                    //pictureDialog.dismiss();
                    //mSectionsPagerAdapter.notifyDataSetChanged();

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
                    Log.d(EXTRA_TAG, "ADD CARDS");

                    break;

                case CONSTANT.RECEIPT_OF_PICTURE_CLASSIFICATION://서비스가 사진 정리를 시작했다는 메세지
                    //사진정리중이면 1, 아니면 0이 들어있음
                    Log.d("IncomingHandler", "[MainActivity]message 수신! handleMessage() - RECEIPT_OF_PICTURE_CLASSIFICATION || 'Service가 사진 정리를 시작했다는 메세지가 도착했습니다' ");

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
        AlertDialog.Builder d = new AlertDialog.Builder(this);
        d.setTitle("종료하시겠습니까?");
//        final LinearLayout r = (LinearLayout) View.inflate(this, R.layout.popup_capic_usb_dialog, null);
//        d.setView(r);
        DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        moveTaskToBack(true);
                        finish();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;

                }
            }
        };
        d.setPositiveButton("Yes", l);
        d.setNegativeButton("No", l);
        d.show();
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



    /***************** File System 부분 **********************/

    public FileSystem getFileSystem() {
        return this.fileSystem;
    }

    public void setFileSystem(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    public CachedBlockDevice getBlockDevice() {
        return blockDevice;
    }

    public void setBlockDevice(CachedBlockDevice blockDevice) {
        this.blockDevice = blockDevice;
    }

    /***************** File System DB 호환 부분 **********************/

    //[DB] 앱 -> USB
    File sd = Environment.getExternalStorageDirectory();
    File data = Environment.getDataDirectory();

    private void exportDB() {
        // TODO Auto-generated method stub
        if (CONSTANT.ISUSBCONNECTED == 1) {//USB가 연결되어 있을 때만 export
            String middlePoint = "/CaPic/" + DatabaseHelper.DATABASE_NAME;

            FileChannel src = null;
            FileChannel dst = null;
            File currentDB = null;
            try {

                if (sd.canWrite()) {

                    FolderManage.makeDirectory(sd + "/CaPic/");//스마트폰 최상단 폴더에 CaPic 폴더를 만든다-DB 저장을 위해(기존에 있으면 안만듬)
                    currentDB = new File(data, CONSTANT.appDBPath);
                    File backupDB = new File(sd, middlePoint);

                    src = new FileInputStream(currentDB).getChannel();
                    dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    //Toast.makeText(getBaseContext(), backupDB.toString(), Toast.LENGTH_SHORT).show();
                    Log.d(EXTRA_TAG, "[exportDB]" + backupDB.toString());

                }
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), "[exportDB]" + e.toString(), Toast.LENGTH_LONG).show();
            }
            //일단은 2단계로 구성. 추후에 한번에 USB로 가도록
            fileSystem.delete(DatabaseHelper.DATABASE_NAME, CONSTANT.BLOCKDEVICE);
            fileSystem.addElementPush(DatabaseHelper.DATABASE_NAME, CONSTANT.BLOCKDEVICE, sd + middlePoint);
            Log.d(EXTRA_TAG, "[exportDB] APP->USB 성공");
            Toast.makeText(getBaseContext(), "[exportDB] export후 APP DB 존재여부 " + currentDB.exists(), Toast.LENGTH_LONG).show();

            Toast.makeText(this, "[exportDB] APP->USB 성공", Toast.LENGTH_SHORT).show();
        }
    }

    //[DB] USB -> 앱
    private void importDB() {
        // TODO Auto-generated method stub
        if (CONSTANT.ISUSBCONNECTED == 1) {//USB가 연결되어 있을 때만 import
            //기존의 APP DB를 삭제한다
            getBaseContext().deleteDatabase(DatabaseHelper.DATABASE_NAME);

            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();
            //일단 USB -> 스마트폰 /CaPic 폴더
            File middlePointFile = new File(sd, "/CaPic/" + DatabaseHelper.DATABASE_NAME);
            Log.d(EXTRA_TAG, "[importDB]middlePointFile 값? " + middlePointFile);

            byte tempDBArray[] = getDBFromUSB(DatabaseHelper.DATABASE_NAME, CONSTANT.BLOCKDEVICE);
            if (tempDBArray == null) {
                Log.d(EXTRA_TAG, "[importDB]tempDBArray == null 에러! importDB 중단");
                return;
            } else
                Log.d(EXTRA_TAG, "[importDB]tempDBArray != null import 성공!, tempDBArray Length " + tempDBArray.length);

            try {
                if (middlePointFile != null && tempDBArray != null) {
                    FileOutputStream fos = new FileOutputStream(middlePointFile);
                    fos.write(tempDBArray);
                    fos.close();
                    Log.d(EXTRA_TAG, "[importDB]FileOutputStream Success ! ");
                }
            } catch (IOException e) {
                Log.d(EXTRA_TAG, "[importDB]FileOutputStream Error ! " + e.toString());
            }

            // /CaPic/에서 앱 DB로
            try {
                if (sd.canWrite()) {
                    File backupDB = new File(data, CONSTANT.appDBPath);
                    if (!backupDB.exists())
                        FolderManage.makeFile(backupDB, data + CONSTANT.appDBPath);
                    Toast.makeText(getBaseContext(), "[importDB] backupDB 존재여부 " + backupDB.exists(), Toast.LENGTH_LONG).show();
                    File currentDB = new File(sd, "/CaPic/" + DatabaseHelper.DATABASE_NAME);
                    Toast.makeText(getBaseContext(), "[importDB] currentDB 존재여부 " + currentDB.exists(), Toast.LENGTH_LONG).show();

                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    Log.d(EXTRA_TAG, "[importDB] USB->APP 성공");
                    Toast.makeText(getBaseContext(), "[importDB] USB->APP 성공", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), "[importDB]" + e.toString(), Toast.LENGTH_LONG).show();
            }

        }
    }

    private byte[] getDBFromUSB(String outString, CachedBlockDevice blockDevice) {//내보내기
        //D  S   X
        //1220879 1870864 2133464

        int result[] = fileSystem.stringSearch(outString);
        byte[] dummyBuffer = new byte[(int) fileSystem.CLUSTERSPACESIZE];
        //1866136
        //result[0] = 4096;
        //result[0] = 6505;
        Log.d("xxxxxx", "result[0] " + result[0]);
        if (result[0] == -1) {
            Log.d(EXTRA_TAG, "[getDBFromUSB]값이 잘못들어왔습니다");
            //Toast.makeText(this, "값이 잘못들어왔습니다", Toast.LENGTH_SHORT).show();
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

                int originalbyteAddress = fileSystem.readIntToBinary(resultstringaddress, limit, fileSystem.LOCATIONSIZE, dummyBuffer, blockDevice);

                blockDevice.readBlock(originalbyteAddress, fileSystem.buffer);
                for (int i = 0; i < fileSystem.CLUSTERSPACESIZE; i++) {
                    if (bytecnt < result[4]) {
                        resultbyte[bytecnt++] = fileSystem.buffer[i];
                    } else
                        break;
                }
                if (bytecnt >= result[4])
                    break;

                limit += fileSystem.LOCATIONSIZE;

                if (limit >= fileSystem.SPACELOCATION) {
                    resultstringaddress = fileSystem.readIntToBinary(resultstringaddress, fileSystem.NEXTLOCATION, fileSystem.LOCATIONSIZE, dummyBuffer, blockDevice);
                    blockDevice.readBlock(resultstringaddress, dummyBuffer);
                    limit = 0;
                }

            }


            Log.d("xxxxxx", "xxxxxxxxxxxx " + resultbyte);
            Log.d("xxxxxx", "xxxxxxxxxxxxxxxxxxx " + resultbyte.length);

            //Toast.makeText(this, "1 " + resultbyte, Toast.LENGTH_SHORT).show();
            //Toast.makeText(this, "1 " + resultbyte.length, Toast.LENGTH_SHORT).show();

            return resultbyte;
        }
    }
}
