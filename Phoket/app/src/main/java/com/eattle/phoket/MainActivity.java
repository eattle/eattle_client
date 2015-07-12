package com.eattle.phoket;

import android.app.AlertDialog;
import android.content.ComponentCallbacks2;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.eattle.phoket.device.CachedBlockDevice;
import com.eattle.phoket.device.CachedUsbMassStorageBlockDevice;
import com.eattle.phoket.helper.DatabaseHelper;
import com.eattle.phoket.host.BlockDeviceApp;
import com.eattle.phoket.host.UsbDeviceHost;
import com.eattle.phoket.model.Folder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_TAG = "MAIN_ACTIVITY";

    //UI 관련 변수
    private ViewPager mViewPager;
    private Adapter mAdapter;

    //파일 시스템 관련 변수
    FileSystem fileSystem;
    private UsbDeviceHost usbDeviceHost;
    private CachedBlockDevice blockDevice;

    //DB관련 변수
    DatabaseHelper db;


    Messenger mService = null;
    boolean mIsBound;
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    //TextView alarm;


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

        fileSystem = FileSystem.getInstance();

/*        usbDeviceHost = new UsbDeviceHost();
        usbDeviceHost.start(this, new BlockDeviceApp() {
            @Override
            public void onConnected(BlockDevice originalBlockDevice) {
                CachedBlockDevice blockDevice = new CachedUsbMassStorageBlockDevice(originalBlockDevice);

                fileSystem.incaseSearchTable(blockDevice);

                CONSTANT.BLOCKDEVICE = blockDevice;//temp
                setBlockDevice(blockDevice);
                //USB가 스마트폰에 연결되었을 떄
                CONSTANT.ISUSBCONNECTED = 1;
                toUSB.setVisibility(View.VISIBLE);//USB가 연결되었으면 하단에 USB 버튼을 보여준다
                //fileSystem.delete(DatabaseHelper.DATABASE_NAME,blockDevice);
                //fileSystem.delete(DatabaseHelper.DATABASE_NAME+"tt",blockDevice);

            }
        });*/

        //데이터베이스 OPEN
        db = DatabaseHelper.getInstance(getApplicationContext());

        doBindService();
/*
        if (db.getAllFolders().size() == 0) {//앱 최초 실행시, 또는 사진 정리가 되어 있지 않을 때
            guide();
        }*/

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        mViewPager = (ViewPager) findViewById(R.id.pager);
        if (mViewPager != null) {
            setupViewPager(mViewPager);
        }

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

/*
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);//탭간 스와이프를 부드럽게 하기 위해 양옆에 2개씩 가지고 있음
        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        LinearLayout actionBarLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.actionbar_main, null);
        ActionBar.LayoutParams params = new ActionBar.LayoutParams(
                ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT,
                Gravity.LEFT);

        //TextView alarm = (TextView)actionBarLayout.findViewById(R.id.alarmIcon);
        alarm = (TextView) actionBarLayout.findViewById(R.id.alarmIcon);

        alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if (CONSTANT.ISUSBCONNECTED == 1) //USB가 연결되었을 떄
                //    importDB(); //USB에 있는 Sqlite DB를 import한다(기존의 앱 DB에서 대체함)
                Toast.makeText(getBaseContext(), "사진 정리 중입니다", Toast.LENGTH_SHORT).show();

                alarm.setEnabled(false); // 클릭 무효화
                //서비스에게 사진 정리를 요청한다
                sendMessageToService(CONSTANT.START_OF_PICTURE_CLASSIFICATION, 1);//1은 더미데이터(추후에 용도 지정, 예를 들면 0이면 전체 사진 새로 정리, 1이면 일부 사진 새로 정리 등)

                flagForList = 1;
            }
        });

        ImageView search = (ImageView) actionBarLayout.findViewById(R.id.searchIcon);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                startActivity(intent);
            }
        });

        actionBar.setCustomView(actionBarLayout, params);
        actionBar.setDisplayHomeAsUpEnabled(false);

*/
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(!mIsBound)//서비스와 연결 안되어 있으면
            doBindService();//연결
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(EXTRA_TAG,"onPause() 호출");
        Glide.get(this).clearMemory();
        Glide.get(this).trimMemory(ComponentCallbacks2.TRIM_MEMORY_MODERATE);
    }

    @Override
    protected void onDestroy(){
        super.onStop();
        Log.d(EXTRA_TAG,"onDestroy() 호출");
        if(mIsBound)//서비스와 연결되어 있으면 해제
            doUnbindService();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK://백버튼을 통제(비밀번호 유지를 위해)
                wantCapicUSB();
                return false;
        }
        return true;
    }

    /***************** ViewPager 부분 **********************/
    private void setupViewPager(ViewPager viewPager) {
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


    /***************** Classification Service 부분 **********************/
    void doBindService() {
        Log.d(EXTRA_TAG, "doBindService() 호출");
        if(!mIsBound) {
            bindService(new Intent(this, ServiceOfPictureClassification.class), mConnection, Context.BIND_AUTO_CREATE);
            mIsBound = true;
        }
    }


    void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with it, then now is the time to unregister.
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, CONSTANT.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service has crashed.
                }
            }
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }
    //서비스로부터 메세지를 받는 부분
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CONSTANT.END_OF_PICTURE_CLASSIFICATION://사진 정리가 완료 되었을 때 받게되는 메세지
                    Log.d("IncomingHandler", "[MainActivity]message 수신! handleMessage() - END_OF_PICTURE_CLASSIFICATION || 'Service가 사진 정리를 완료했다는 메세지가 도착했습니다' ");
                    //pictureDialog.dismiss();
                    //mSectionsPagerAdapter.notifyDataSetChanged();

                    ((Section1)(mAdapter.getItem(0))).setRunning();


                    wantBackUp();
                    //exportDB();//Sqlite DB 추출(USB와의 동기화를 위해)
//                    classification.setEnabled(true); // 클릭 유효화
//                    alarm.setEnabled(true); // 클릭 무효화
                    break;
                case CONSTANT.END_OF_SINGLE_STORY://하나의 스토리가 정리 되었을 때
                    int id = msg.arg1;
/*                    String path = msg.getData().getString("path");
                    String thumbNailPath = msg.getData().getString("thumbNailPath");
                    String new_name = msg.getData().getString("new_name");
                    int folderIDForDB = msg.getData().getInt("folderIDForDB");
                    int pictureNumInStory = msg.getData().getInt("picture_num");*/

                    ((Section1)(mAdapter.getItem(0))).addSingleCard(db.getFolder(id));
//                    ((Section2)(mAdapter.getItem(1)))(db.getFolder(id));


//                    Fragment page = mSectionsPagerAdapter.getRegisteredFragment(0);//모아보기
//                    ((Section1) page).selectCard(path, thumbNailPath, new_name, folderIDForDB, pictureNumInStory);
//                    page = mSectionsPagerAdapter.getRegisteredFragment(1);//스토리
//                    ((Section2) page).selectCard(path, thumbNailPath, new_name, folderIDForDB, pictureNumInStory);

//                    if(flagForList == 1) {//기존에 있던 리스트들을 다 지우고 시작하는 부분
//                        mSectionsPagerAdapter.notifyDataSetChanged();
//                        flagForList = 0;
//                    }
                    /*
                    if (mViewPager.getCurrentItem() == 0) {
                        Fragment page = mSectionsPagerAdapter.getRegisteredFragment(mViewPager.getCurrentItem());
                        ((Section1) page).selectCard(thumbNailPath, new_name, folderIDForDB, pictureNumInStory);
                    }*/
                    break;
                default:
                    Log.d("IncomingHandler", "[MainActivity]message 수신! handleMessage() - Default");
                    super.handleMessage(msg);
            }
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("ServiceConnection()", "onServiceConnected() 함수 호출");
            mService = new Messenger(service);
            try {
                Message msg = Message.obtain(null, CONSTANT.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;//'답장은 mMessenger로 받겠습니다'라는 의미
                mService.send(msg);//메세지를 보낸다
            } catch (RemoteException e) {

            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("ServiceConnection()", "onServiceDisconnected() 함수 호출");
            mService = null;
        }
    };

    //서비스로 메세지를 보낸다(MainActivity -> ServiceOfPictureClassification)
    //메세지의 형태는 ServiceOfPictureClassification에 정의된 상수를 통해서
    public void sendMessageToService(int typeOfMessage, int intValueToSend) {
        if (mIsBound) {
            if (mService != null) {
                try {
                    //START_OF_PICTURE_CLASSIFICATION은 '메세지의 유형'을 정의하는 것
                    Message msg = Message.obtain(null, typeOfMessage, intValueToSend, 0);
                    msg.replyTo = mMessenger;//'답장은 mMessenger로 받겠습니다'라는 의미
                    mService.send(msg);//서비스로 메세지를 보낸다
                } catch (RemoteException e) {
                    Log.d(EXTRA_TAG, e.toString());
                }
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

    public void guide() {//앱을 최초 실행했을 때 사진정리를 누르도록 한다.
        AlertDialog.Builder d = new AlertDialog.Builder(this);
        final LinearLayout r = (LinearLayout) View.inflate(this, R.layout.popup_first_classification, null);
        d.setView(r);
        DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        Toast.makeText(getBaseContext(), "사진 정리 중입니다", Toast.LENGTH_SHORT).show();

                        Snackbar.make(findViewById(R.id.main_content), "사진을 정리 중입니다", Snackbar.LENGTH_SHORT)
                                .setAction("Action", null).show();


                        //alarm.setEnabled(false); // 정리 버튼 클릭 무효화
                        //서비스에게 사진 정리를 요청한다
                        sendMessageToService(CONSTANT.START_OF_PICTURE_CLASSIFICATION, 1);//1은 더미데이터(추후에 용도 지정, 예를 들면 0이면 전체 사진 새로 정리, 1이면 일부 사진 새로 정리 등)

                        break;
                }
            }
        };
        d.setPositiveButton("정리 시작", l);
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
