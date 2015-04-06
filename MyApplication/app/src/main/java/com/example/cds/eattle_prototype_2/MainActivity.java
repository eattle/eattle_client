package com.example.cds.eattle_prototype_2;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.location.*;
import android.os.*;

import com.example.cds.eattle_prototype_2.helper.DatabaseHelper;
import com.example.cds.eattle_prototype_2.model.Folder;
import com.example.cds.eattle_prototype_2.model.Manager;
import com.example.cds.eattle_prototype_2.model.Media;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class MainActivity extends ActionBarActivity {
    private String Tag="MainActivity";
    //데이터베이스 관련 변수들
    DatabaseHelper db;

    ImageView mImage;

    private ListView storyList;//메인화면의 스토리 목록들이 들어가는 리스트뷰
    private StoryListAdapter storyListAdapter;//리스트뷰를 위한 어댑터



    Messenger mService = null;
    boolean mIsBound;
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    ProgressDialog pictureDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //데이터베이스 OPEN
        db = DatabaseHelper.getInstance(getApplicationContext());

        //Folder DB를 기반으로 메인화면을 구성한다.
        drawMainView();

        //서비스 시작
        //startService(new Intent(MainActivity.this, ServiceOfPictureClassification.class));
        doBindService();

        //test
        //sendMessageToService(ServiceOfPictureClassification.START_OF_PICTURE_CLASSIFICATION,1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        drawMainView();
    }

    //서비스로부터 메세지를 받는 부분
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ServiceOfPictureClassification.END_OF_PICTURE_CLASSIFICATION://사진 정리가 완료 되었을 때 받게되는 메세지
                    Log.d("IncomingHandler", "[MainActivity]message 수신! handleMessage() - END_OF_PICTURE_CLASSIFICATION || 'Service가 사진 정리를 완료했다는 메세지가 도착했습니다' ");
                    pictureDialog.dismiss();
                    drawMainView();
                    Toast.makeText(MainActivity.this,"사진 정리가 완료되었습니다",Toast.LENGTH_LONG).show();
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
            Log.d("ServiceConnection()","onServiceConnected() 함수 호출");
            mService = new Messenger(service);
            try{
                Message msg = Message.obtain(null, ServiceOfPictureClassification.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;//'답장은 mMessenger로 받겠습니다'라는 의미
                mService.send(msg);//메세지를 보낸다
            } catch(RemoteException e){

            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("ServiceConnection()","onServiceDisconnected() 함수 호출");
            mService = null;
        }
    };

    //서비스로 메세지를 보낸다(MainActivity -> ServiceOfPictureClassification)
    //메세지의 형태는 ServiceOfPictureClassification에 정의된 상수를 통해서
    private void sendMessageToService(int typeOfMessage, int intValueToSend){
        if(mIsBound){
            if(mService != null){
                try{
                    //START_OF_PICTURE_CLASSIFICATION은 '메세지의 유형'을 정의하는 것
                    Message msg = Message.obtain(null, typeOfMessage, intValueToSend,0);
                    msg.replyTo = mMessenger;//'답장은 mMessenger로 받겠습니다'라는 의미
                    mService.send(msg);//서비스로 메세지를 보낸다
                } catch(RemoteException e){

                }
            }
        }
    }

    void doBindService() {
        Log.d(Tag,"doBindService() 호출");
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
                }
                catch (RemoteException e) {
                    // There is nothing special we need to do if the service has crashed.
                }
            }
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    public void drawMainView(){//폴더를 기반으로 스토리의 목록을 보여준다.
        //커스텀 어댑터 생성
        storyListAdapter = new StoryListAdapter(this);
        //activity_main.xml에 있는 storyList 리스트뷰에 연결
        storyList = (ListView)findViewById(R.id.storyList);
        //ListView에 어댑터 연결
        storyList.setAdapter(storyListAdapter);

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
            for (int i = 0; i < folderList.size(); i++) {
                StoryListItem tempItem = new StoryListItem(folderList.get(i).getThumbNail_name(),folderList.get(i).getName(),folderList.get(i).getId());
                storyListAdapter.add(tempItem);
            }
            storyListAdapter.notifyDataSetChanged() ;

        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.classification:
                //Toast.makeText(this,"사진 정리 중",Toast.LENGTH_LONG).show();
                Button classification = (Button)findViewById(R.id.classification);
                classification.setEnabled(false); // 클릭 무효화

                //서비스에게 사진 정리를 요청한다
                sendMessageToService(ServiceOfPictureClassification.START_OF_PICTURE_CLASSIFICATION,1);//1은 더미데이터(추후에 용도 지정, 예를 들면 0이면 전체 사진 새로 정리, 1이면 일부 사진 새로 정리 등)
                pictureDialog = ProgressDialog.show(MainActivity.this,"","사진을 정리하는 중입니다",true);
                classification.setEnabled(true); // 클릭 유효화
                break;

            /*
            case R.id.intervalOk:
                EditText editText = (EditText)findViewById(R.id.intervalText);
                String text = editText.getText().toString();
                if(text!=null) {//시간간격을 입력했으면
                    CONSTANT.TIMEINTERVAL = Long.parseLong(text);
                    pictureClassification();
                }
                else
                    Toast.makeText(getBaseContext(),"시간 간격을 입력하세요",Toast.LENGTH_SHORT).show();
                editText.clearFocus();*/
        }
    }




    // 외장 메모리 DCIM 전체 MediaScanning
    // 킷캣 이후 버전 이후로 앱단에서 스캐닝 불가
    public static void startExtMediaScan(Context mContext){
        mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM))));
    }


    /*
    public void CheckIfServiceIsRunning(){
        if(ServiceOfPictureClassification.isRunning())
            doBindService();
    }*/

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

