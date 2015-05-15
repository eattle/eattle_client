package com.example.cds.eattle_prototype_2;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cds.eattle_prototype_2.device.BlockDevice;
import com.example.cds.eattle_prototype_2.device.CachedBlockDevice;
import com.example.cds.eattle_prototype_2.device.CachedUsbMassStorageBlockDevice;
import com.example.cds.eattle_prototype_2.helper.DatabaseHelper;
import com.example.cds.eattle_prototype_2.host.BlockDeviceApp;
import com.example.cds.eattle_prototype_2.host.UsbDeviceHost;
import com.example.cds.eattle_prototype_2.model.Folder;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {
    private String Tag = "MainActivity";
    //데이터베이스 관련 변수들
    DatabaseHelper db;
    FileSystem fileSystem;
    static final String DATABASE_NAME = "";

    ImageView mImage;

    private ListView storyList;//메인화면의 스토리 목록들이 들어가는 리스트뷰
    private StoryListAdapter storyListAdapter;//리스트뷰를 위한 어댑터
    Button classification;


    Messenger mService = null;
    boolean mIsBound;
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    private UsbDeviceHost usbDeviceHost;
    private CachedBlockDevice blockDevice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(Tag, "onCreate 호출");
        final Button toUSB = (Button)findViewById(R.id.toUSB);
        toUSB.setVisibility(View.GONE);//하단에 USB 버튼을 일단 없앤다


        classification = (Button) findViewById(R.id.classification);
        if (CONSTANT.PASSWORD == 0) {//비밀 번호 해제 안됬으면
            //password 창을 띄운다
            Intent intent = new Intent(this, Password.class);
            startActivity(intent);
        }

        fileSystem = FileSystem.getInstance();

        usbDeviceHost = new UsbDeviceHost();
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
        });

        //데이터베이스 OPEN
        db = DatabaseHelper.getInstance(this);

        //activity_main.xml에 있는 storyList 리스트뷰에 연결
        storyList = (ListView) findViewById(R.id.storyList);
        //커스텀 어댑터 생성
        storyListAdapter = new StoryListAdapter(this);
        //ListView에 어댑터 연결
        storyList.setAdapter(storyListAdapter);
        doBindService();
    }

    @Override
    protected void onResume() {
        super.onResume();

        drawMainView();
        if (CONSTANT.PASSWORD == 0) {//비밀 번호 해제 안됬으면
            //password 창을 띄운다
            Intent intent = new Intent(this, Password.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
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


    //서비스로부터 메세지를 받는 부분
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ServiceOfPictureClassification.END_OF_PICTURE_CLASSIFICATION://사진 정리가 완료 되었을 때 받게되는 메세지
                    Log.d("IncomingHandler", "[MainActivity]message 수신! handleMessage() - END_OF_PICTURE_CLASSIFICATION || 'Service가 사진 정리를 완료했다는 메세지가 도착했습니다' ");
                    //pictureDialog.dismiss();

                    wantBackUp();
                    exportDB();//Sqlite DB 추출(USB와의 동기화를 위해)
                    classification.setEnabled(true); // 클릭 유효화
                    break;
                case ServiceOfPictureClassification.END_OF_SINGLE_STORY://하나의 스토리가 정리 되었을 때
                    String thumbNailID = msg.getData().getString("thumbNailID");
                    String new_name = msg.getData().getString("new_name");
                    int folderIDForDB = msg.getData().getInt("folderIDForDB");
                    int pictureNumInStory = msg.getData().getInt("picture_num");
                    StoryListItem tempItem = new StoryListItem(thumbNailID, new_name, folderIDForDB, pictureNumInStory);
                    storyListAdapter.add(tempItem);
                    storyListAdapter.notifyDataSetChanged();//메인화면에게 리스트뷰가 업데이트 되었음을 알린다

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
                Message msg = Message.obtain(null, ServiceOfPictureClassification.MSG_REGISTER_CLIENT);
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
    private void sendMessageToService(int typeOfMessage, int intValueToSend) {
        if (mIsBound) {
            if (mService != null) {
                try {
                    //START_OF_PICTURE_CLASSIFICATION은 '메세지의 유형'을 정의하는 것
                    Message msg = Message.obtain(null, typeOfMessage, intValueToSend, 0);
                    msg.replyTo = mMessenger;//'답장은 mMessenger로 받겠습니다'라는 의미
                    mService.send(msg);//서비스로 메세지를 보낸다
                } catch (RemoteException e) {

                }
            }
        }
    }

    void doBindService() {
        Log.d(Tag, "doBindService() 호출");
        bindService(new Intent(this, ServiceOfPictureClassification.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with it, then now is the time to unregister.
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, ServiceOfPictureClassification.MSG_UNREGISTER_CLIENT);
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

    public void drawMainView() {//폴더를 기반으로 스토리의 목록을 보여준다.
        /*
        storyListAdapter.clear();
        for (int i = 0; i < folderList.size(); i++) {
            StoryListItem tempItem = new StoryListItem(folderList.get(i).getThumbNail_name(),folderList.get(i).getName(),folderList.get(i).getId());
            storyListAdapter.add(tempItem);
        }

        ArrayList<StoryListItem> listItems = storyListAdapter.getAllItems();
        storyListAdapter.clear();
        for(int i=0;i<listItems.size();i++){
            storyListAdapter.add(listItems.get(i));
            storyListAdapter.notifyDataSetChanged() ;

        }*/
        /*
        //리스트뷰에 아이템 추가---------------------------
        //모든 폴더 목록들을 불러온다
        List<Folder> folderList = db.getAllFolders();
        if(folderList.isEmpty()) {//폴더가 정리되어 있지 않으면
            TextView tempLayout = new TextView(this);
            tempLayout.setText("앨범이 비어있어요!");
            tempLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300));
            tempLayout.setTextSize(20);
            tempLayout.setGravity(Gravity.CENTER);
//            storyList.addView(tempLayout);
        }
        else {
            storyListAdapter.clear();
            for (int i = 0; i < folderList.size(); i++) {
                StoryListItem tempItem = new StoryListItem(folderList.get(i).getThumbNail_name(),folderList.get(i).getName(),folderList.get(i).getId());
                storyListAdapter.add(tempItem);
            }
            storyListAdapter.notifyDataSetChanged() ;

        }*/
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.classification:
                Toast.makeText(this, "사진 정리 중입니다", Toast.LENGTH_SHORT).show();
                classification.setEnabled(false); // 클릭 무효화
                storyListAdapter.clear();//메인 화면을 일단 전부 지운다
                storyListAdapter.notifyDataSetChanged();//메인화면에게 리스트뷰가 업데이트 되었음을 알린다

                importDB(); //USB에 있는 Sqlite DB를 import한다(기존의 앱 DB에서 대체함 - USB가 꽂혀있을 때만 실행함)


                //서비스에게 사진 정리를 요청한다
                sendMessageToService(ServiceOfPictureClassification.START_OF_PICTURE_CLASSIFICATION, 1);//1은 더미데이터(추후에 용도 지정, 예를 들면 0이면 전체 사진 새로 정리, 1이면 일부 사진 새로 정리 등)
                //pictureDialog = ProgressDialog.show(MainActivity.this,"","사진을 정리하는 중입니다",true);

                break;

            case R.id.toUSB:
                importDB();//USB가 꽂혀있을 때만 실행함
                Intent intent = new Intent(MainActivity.this, USBMainActivity.class);
                intent.putExtra("check", 1);
                startActivity(intent);
                break;
        }
    }


    public void wantCapicUSB() {//앱을 종료하려 할때, USB 구매의사를 묻는다.
        AlertDialog.Builder d = new AlertDialog.Builder(this);
        d.setTitle("종료하시겠습니까?");
        final LinearLayout r = (LinearLayout) View.inflate(this, R.layout.capic_usb_dialog, null);
        d.setView(r);
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
        d.setTitle("백업이 완료되었습니다!");
        final LinearLayout r = (LinearLayout) View.inflate(this, R.layout.complete_classify_picture_dialog, null);
        d.setView(r);
        DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        //스마트폰에서 사진들을 삭제한다
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        //스마트폰에서 사진들을 삭제하지 않는다
                        break;

                }
            }
        };
        d.setPositiveButton("Yes", l);
        d.setNegativeButton("No", l);
        d.show();
    }

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
                    Log.d(Tag,"[exportDB]"+backupDB.toString());

                }
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), "[exportDB]"+e.toString(), Toast.LENGTH_LONG).show();
            }
            //일단은 2단계로 구성. 추후에 한번에 USB로 가도록
            fileSystem.delete(DatabaseHelper.DATABASE_NAME, CONSTANT.BLOCKDEVICE);
            fileSystem.addElementPush(DatabaseHelper.DATABASE_NAME, CONSTANT.BLOCKDEVICE, sd + middlePoint);
            Log.d(Tag,"[exportDB] APP->USB 성공");
            Toast.makeText(getBaseContext(), "[exportDB] export후 APP DB 존재여부 "+currentDB.exists(), Toast.LENGTH_LONG).show();

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
            Log.d(Tag,"[importDB]middlePointFile 값? " + middlePointFile);

            byte tempDBArray[] = getDBFromUSB(DatabaseHelper.DATABASE_NAME, CONSTANT.BLOCKDEVICE);
            if (tempDBArray == null) {
                Log.d(Tag, "[importDB]tempDBArray == null 에러! importDB 중단");
                return;
            }
            else
                Log.d(Tag, "[importDB]tempDBArray != null import 성공!, tempDBArray Length " + tempDBArray.length);

            try{
                if(middlePointFile != null && tempDBArray != null) {
                    FileOutputStream fos = new FileOutputStream(middlePointFile);
                    fos.write(tempDBArray);
                    fos.close();
                    Log.d(Tag, "[importDB]FileOutputStream Success ! ");
                }
            } catch(IOException e){
                Log.d(Tag,"[importDB]FileOutputStream Error ! " + e.toString());
            }

            // /CaPic/에서 앱 DB로
            try {
                if (sd.canWrite()) {
                    File backupDB = new File(data, CONSTANT.appDBPath);
                    if(!backupDB.exists())
                        FolderManage.makeFile(backupDB,data+CONSTANT.appDBPath);
                    Toast.makeText(getBaseContext(), "[importDB] backupDB 존재여부 "+backupDB.exists(), Toast.LENGTH_LONG).show();
                    File currentDB = new File(sd, "/CaPic/" + DatabaseHelper.DATABASE_NAME);
                    Toast.makeText(getBaseContext(), "[importDB] currentDB 존재여부 "+currentDB.exists(), Toast.LENGTH_LONG).show();

                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    Log.d(Tag,"[importDB] USB->APP 성공");
                    Toast.makeText(getBaseContext(), "[importDB] USB->APP 성공", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), "[importDB]"+e.toString(), Toast.LENGTH_LONG).show();
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
            Log.d(Tag,"[getDBFromUSB]값이 잘못들어왔습니다");
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
private void pictureClassification() throws IOException {//시간간격을 바탕으로 사진들을 분류하는 함수
        //DCIM 폴더의 Eattle이 만든 폴더를 다 삭제한다(추후 변경)
        String[] folderList = FolderManage.getList(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)+"/"));
        for(int i=0;i<folderList.length;i++){
            if(!folderList[i].equals("Camera") && !folderList[i].equals("thumbnail"))
                FolderManage.deleteFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)+"/"+folderList[i]+"/"));
        }
        //---------------------------------------------
        ImageSetter = new AlbumImageSetter(this, 0, 0);
        calculatePictureInterval();//사진의 시간간격의 총합을 구한다.
        long averageInterval = totalInterval;
        if (totalPictureNum != 0)
            averageInterval /= totalPictureNum;
        CONSTANT.TIMEINTERVAL=averageInterval;
        //DB를 참조한다.
        Manager _m = new Manager(totalPictureNum, averageInterval, standardDerivation);
        db.createManager(_m);//Manager DB에 값들을 집어넣음

        db.deleteAllFolder();
        db.deleteAllMedia();
        db.deleteAllTag();
        db.deleteAllMediaTag();
        ImageSetter.setCursor(0,0);//커서의 위치를 처음으로 이동시킨다.
        File picture=null;
        File dir=null;
        String startFolderID="";
        String endFolderID="";
        int folderIDForDB=0;//Folder DB에 들어가는 아이디
        long _pictureTakenTime=0;//현재 읽고 있는 사진 이전의 찍힌 시간
        String representativeImage="";//폴더에 들어가는 대표이미지의 이름(경로제외), 일단 폴더에 들어가는 첫번째 사진으로 한다.
        String folderName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)+"/tempEattle/";
        String folderThumbnailName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)+"/thumbnail/";
        FolderManage.makeDirectory(folderThumbnailName);


        while(ImageSetter.mCursor.moveToNext()){
            String path = ImageSetter.mCursor.getString(ImageSetter.mCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));

            Log.d("사진 분류",path);
            //썸네일 사진들은 분류대상에서 제외한다
            if(path.contains("thumbnail") || path.contains("스토리")) {
                Log.d("pictureClassification","썸네일 및 기존 스토리는 분류 대상에서 제외");
                continue;
            }

            picture = new File(path);
            //사진 ID
            int pictureID = ImageSetter.mCursor.getInt(ImageSetter.mCursor.getColumnIndex(MediaStore.MediaColumns._ID));
            //사진이 촬영된 날짜
            long pictureTakenTime = ImageSetter.mCursor.getLong(ImageSetter.mCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_ADDED));
            pictureTakenTime *= 1000; //second->millisecond
            //millisecond -> Calendar
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(pictureTakenTime);
            String folderID=""+cal.get(Calendar.YEAR)+"_"+(cal.get(Calendar.MONTH)+1)+"_"+cal.get(Calendar.DATE);
            if(representativeImage.equals(""))
                representativeImage = String.valueOf(pictureID);


            //썸네일 이미지를 생성한다
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inSampleSize = 16;//기존 해상도의 1/16로 줄인다
            Bitmap bitmap = BitmapFactory.decodeFile(path,opt);
            createThumbnail(bitmap, folderThumbnailName, String.valueOf(pictureID)+".jpg");

            Log.d("MainActivity", "[pictureID] : " + String.valueOf(pictureID) + " [pictureTakenTime] : " + Long.toString(pictureTakenTime));

            //이전에 읽었던 사진과 시간 차이가 CONSTANT.TIMEINTERVAL보다 크면 새로 폴더를 만든다.
            Log.d("MainActivity","pictureTakenTime-_pictureTakenTime = "+(pictureTakenTime-_pictureTakenTime));
            if(pictureTakenTime-_pictureTakenTime > CONSTANT.TIMEINTERVAL){
                //이전에 만들어진 폴더의 이름을 바꾼다(startFolderID ~ endFolderID)
                if(!startFolderID.equals("")) {
                    File new_name = null;
                    if (!startFolderID.equals(endFolderID))
                        new_name = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/" + startFolderID + "~" + endFolderID + "의 스토리");
                    else
                        new_name = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/" + startFolderID + "의 스토리");

                    String _new_name=FolderManage.reNameFile(dir, new_name);
                    //Folder DB에 넣는다.
                    Folder f = new Folder(folderIDForDB,_new_name,representativeImage);
                    db.createFolder(f);
                    representativeImage="";
                    Log.d("MainActivity","tempEattle 폴더 이름 변경");
                }

                //방금 읽은 사진의 folderID가 시작날짜가 된다.
                startFolderID = folderID;
                //tempEattle이라는 이름으로 임시 폴더를 만든다.
                dir = FolderManage.makeDirectory(folderName);
                folderIDForDB++;
            }
            //사진에 위치 정보가 있으면 얻어온다
            //위치 정보가 없으면 longitude = 0.0, latitude = 0.0이 들어감
            double longitude = ImageSetter.mCursor.getDouble(ImageSetter.mCursor.getColumnIndex(MediaStore.Images.ImageColumns.LONGITUDE));
            double latitude = ImageSetter.mCursor.getDouble(ImageSetter.mCursor.getColumnIndex(MediaStore.Images.ImageColumns.LATITUDE));
            //위치 정보를 토대로 지명이름을 가져온다
            String placeName_ = "";
            List<Address> placeName = null;
            if(longitude != 0 && latitude != 0) {
                try {
                    placeName = mCoder.getFromLocation(latitude, longitude, 1);
                } catch (IOException e) {
                    Log.e("PictureClassification", e.getMessage());
                }
                //위치 정보가 없으면
                if (placeName == null) {
                    Log.e("PictureClassification", "위치 정보가 없습니다");
                } else {
                    //placeName_ = placeName.get(0).getLocality();//ex)강남구
                    placeName_ = placeName.get(0).getThoroughfare();//ex)선릉로93길, 역삼동
                    Log.e("PictureClassification", "~"+placeName.get(0).getAdminArea()+"~"+placeName.get(0).getCountryCode()+"~"+placeName.get(0).getFeatureName()+"~"+placeName.get(0).getLocality()+"~"+placeName.get(0).getSubAdminArea()+"~"+placeName.get(0).getSubLocality()+"~"+placeName.get(0).getSubThoroughfare()+"~"+placeName.get(0).getThoroughfare()+"~"+placeName.get(0).getMaxAddressLineIndex());
                    //ex)~서울특별시~KR~54~강남구~null~null~54~선릉로93길~0
                    //~서울특별시~KR~619-26~강남구~null~null~619-26~역삼동~0
                }

            }

            //사진을 새로운 폴더로 복사한다.
            FolderManage.copyFile(picture , folderName+String.valueOf(pictureID)+".jpg");

            //DB에 사진 데이터를 넣는다.
            Media m = new Media(pictureID,folderIDForDB,""+pictureID,cal.get(Calendar.YEAR),(cal.get(Calendar.MONTH)+1),cal.get(Calendar.DATE),latitude,longitude,placeName_);

            db.createMedia(m);
            _pictureTakenTime = pictureTakenTime;
            endFolderID = folderID;
        }

        //마지막 남은 폴더를 처리한다.
        //이전에 만들어진 폴더의 이름을 바꾼다(startFolderID ~ endFolderID)
        if(!startFolderID.equals("")) {
            File new_name = null;
            if (!startFolderID.equals(endFolderID)) {
                new_name = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/" + startFolderID + "~" + endFolderID + "의 스토리");
            } else
                new_name = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/" + startFolderID + "의 스토리");
            String _new_name = FolderManage.reNameFile(dir, new_name);
            //Folder DB에 넣는다.
            Folder f = new Folder(folderIDForDB,_new_name,representativeImage);
            db.createFolder(f);
            representativeImage="";
            Log.d("MainActivity","tempEattle 폴더 이름 변경");
        }
        //메인화면의 스토리 목록을 갱신한다.
        drawMainView();
        Toast.makeText(getBaseContext(),"사진 정리가 완료되었습니다",Toast.LENGTH_LONG).show();
        ImageSetter.mCursor.close();
    }


 */

