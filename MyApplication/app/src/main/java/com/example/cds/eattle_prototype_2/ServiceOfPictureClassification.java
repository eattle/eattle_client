package com.example.cds.eattle_prototype_2;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;

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

//사진 분류, 백업 등을 담당하는 서비스
public class ServiceOfPictureClassification extends Service {
    String Tag = "Eattle_Service";

    private static boolean isRunning = false;

    static final int MSG_REGISTER_CLIENT = 1;//MainActivity와 Service가 bind 되었을 때
    static final int MSG_UNREGISTER_CLIENT = 2;//MainActivity와 Service가 bind를 중단하라는 메세지
    static final int START_OF_PICTURE_CLASSIFICATION = 3;//MainActivity가 Service에게 사진 정리를 요청하는 메세지
    static final int END_OF_PICTURE_CLASSIFICATION = 4;//Service가 MainActivity에게 사진 정리를 완료 했다고 보내는 메세지
    static final int END_OF_SINGLE_STORY = 5;//스토리가 정리되는대로 바로바로 보여주기 위하여 정의한 메세지,하나의 스토리가 정리될때마다 보낸다

    ArrayList<Messenger> mClients = new ArrayList<Messenger>();
    ;
    final Messenger mMessenger = new Messenger(new IncomingHandler());


    //사진 정리와 관련된 변수들
    int totalPictureNum = 0;//사진들의 총 개수
    long totalInterval;//사진 간격의 총합
    long standardDerivation = 0;//사진 간격의 표준편차

    int folderID = 0;//시간에 따라 할당될 폴더 아이디 (0부터 시작)
    //앨범의 Image Setting(미디어 DB 연결)
    static AlbumImageSetter ImageSetter;
    //데이터베이스 관련 변수들
    DatabaseHelper db;
    //장소 관련(역지오코딩)
    LocationManager mLocMan;
    Geocoder mCoder;
    IncomingHandler incomingHandler = new IncomingHandler();

    //파일시스템
    FileSystem fileSystem;

    public ServiceOfPictureClassification() {
    }

    public void onCreate() {
        super.onCreate();

        Log.d(Tag, "서비스 onCreate() 호출");
        //쓰레드를 생성하여 사진 관련 서비스 시작
        //Thread serviceOfEattle = new Thread(incomingHandler);
        //serviceOfEattle.start();
        PictureThread serviceOfEattle = new PictureThread(incomingHandler);
        serviceOfEattle.start();

        fileSystem = FileSystem.getInstance();

    }

    class PictureThread extends Thread {
        android.os.Handler mHandler;

        public PictureThread(android.os.Handler handler) {
            mHandler = handler;
        }

        public void run() {
            Log.d(Tag, "ServiceOfPictureClassification Run() 호출!!!");
            db = DatabaseHelper.getInstance(getApplicationContext());
            isRunning = true;
            Looper.prepare();
        }
    }
    /*
    public void run(){
        Log.d(Tag,"ServiceOfPictureClassification Run() 호출");
        //데이터베이스 OPEN
        db = DatabaseHelper.getInstance(getApplicationContext());
        //isRunning = true;
    }*/

    public static boolean isRunning() {
        return isRunning;
    }

    //MainActivity로 부터 온 메세지를 받는 부분
    class IncomingHandler extends android.os.Handler {
        boolean isNew = true;
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    Log.d("IncomingHandler", "[ServiceOfPictureClassification]message 수신! handleMessage() - MSG_REGISTER_CLIENT || 'MainActivity가 연결을 요청하였습니다' ");
                    //mClients에 이미 있다면 등록하지(add) 않는다
                    for(int i=0;i<mClients.size();i++){
                        if(mClients.get(i) == msg.replyTo) {
                            isNew = false;
                            break;
                        }
                    }
                    if(isNew)
                        mClients.add(msg.replyTo);
                    break;

                case MSG_UNREGISTER_CLIENT:
                    Log.d("IncomingHandler", "[ServiceOfPictureClassification]message 수신! handleMessage() - MSG_UNREGISTER_CLIENT || 'MainActivity가 연결 취소를 원합니다' ");
                    mClients.remove(msg.replyTo);

                case START_OF_PICTURE_CLASSIFICATION:
                    Log.d("IncomingHandler", "[ServiceOfPictureClassification]message 수신! handleMessage() - START_OF_PICTURE_CLASSIFICATION || 'MainActivity가 사진 정리를 요청하였습니다' ");
                    for(int i=0;i<mClients.size();i++){
                        if(mClients.get(i) == msg.replyTo) {
                            isNew = false;
                            break;
                        }
                    }
                    if(isNew)
                        mClients.add(msg.replyTo);


                    Thread pictureThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {//사진 정리를 시작한다
                                pictureClassification();
                            } catch (IOException e) {
                                Log.d("PictureClassification", e.getMessage());
                            }
                        }
                    });
                    pictureThread.start();


                    break;

                default:
                    Log.d("IncomingHandler", "[ServiceOfPictureClassification]message 수신! handleMessage() - Default");
                    super.handleMessage(msg);
            }
        }
    }

    //MainActivity에게 메세지를 보내는 함수(int값만 보낼 때)
    private void sendMessageToUI(int typeOfMessage, int intValueToSend) {
        for (int i = mClients.size() - 1; i >= 0; i--) {
            try {
                // Send data as an Integer
                mClients.get(i).send(Message.obtain(null, typeOfMessage, intValueToSend, 0));


            } catch (RemoteException e) {
                // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
                mClients.remove(i);
            }
        }
    }

    //MainActivity에게 메세지를 보내는 함수(메인 화면 구성을 위한 여러 데이터를 보낼 때)
    private void sendMessageToUI(int typeOfMessage, String thumbNailID, String new_name, int folderIDForDB, int picture_num) {
        for (int i = mClients.size() - 1; i >= 0; i--) {
            try {
                Bundle bundle = new Bundle();
                bundle.putString("thumbNailID",thumbNailID);
                bundle.putString("new_name",new_name);
                bundle.putInt("folderIDForDB",folderIDForDB);
                bundle.putInt("picture_num",picture_num);

                Message msg = Message.obtain(null, typeOfMessage);
                msg.setData(bundle);
                mClients.get(i).send(msg);

            } catch (RemoteException e) {
                // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
                mClients.remove(i);
            }
        }
    }


    //사진 정리와 관련된 함수들

    private void calculatePictureInterval() {//사진간 시간 간격을 계산하는 함수
        totalInterval = 0;
        totalPictureNum = 0;
        ImageSetter.setCursor(0, 0);//커서의 위치를 처음으로 이동시킨다.
        long pictureTakenTime = 0;
        while (ImageSetter.mCursor.moveToNext()) {
            String path = ImageSetter.mCursor.getString(ImageSetter.mCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
            Log.d("MainActivity", "!!" + path);
            //썸네일 사진들은 계산대상에서 제외한다
            if (path.contains("thumbnail") || path.contains("스토리")) {
                Log.d("pictureClassification", "썸네일 및 기존 스토리는 계산대상에서 제외");
                continue;
            }
            //사진이 촬영된 날짜
            long _pictureTakenTime = ImageSetter.mCursor.getLong(ImageSetter.mCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN));
            //_pictureTakenTime *= 1000; //second->millisecond
            if (pictureTakenTime == 0)
                pictureTakenTime = _pictureTakenTime;

            totalInterval += _pictureTakenTime - pictureTakenTime;
            pictureTakenTime = _pictureTakenTime;
            totalPictureNum++;
        }
    }

    private void pictureClassification() throws IOException {//시간간격을 바탕으로 사진들을 분류하는 함수

        //pictureClassification()의 속도 개선 방안
        //1. 처음에 mediaDB를 전부 삭제하지 않는다
        // -> folderIDForDB만 업데이트 한다
        // -> 위치 정보가 있으면 새로 받지 않는다
        // -> 사진의 경로가 변경되었는지 확인한다
        //2. folderDB는 기존대로 전부 삭제한다
        //3. 특정 사진에 있던 태그들은 삭제하지 않는다


        mCoder = new Geocoder(this);
        ImageSetter = new AlbumImageSetter(this, 0, 0);
        calculatePictureInterval();//사진의 시간간격의 총합을 구한다.
        long averageInterval = totalInterval;
        if (totalPictureNum != 0)
            averageInterval /= totalPictureNum;
        CONSTANT.TIMEINTERVAL = averageInterval;
        //DB를 참조한다.
        Manager _m = new Manager(totalPictureNum, averageInterval, standardDerivation);
        db.createManager(_m);//Manager DB에 값들을 집어넣음
        //DB에 있는 데이터들을 초기화한다
        db.deleteAllFolder();
        //db.deleteAllMedia();
        //db.deleteAllTag();
        //db.deleteAllMediaTag();
        //커서의 위치를 처음으로 이동시킨다.
        ImageSetter.setCursor(0, 0);
        //File picture=null;
        //File dir=null;

        ArrayList<Media> medias = new ArrayList<Media>();//DB에 추가될 Media들의 목록(DB에 한꺼번에 넣기 위하여)
        String startFolderID = "";
        String endFolderID = "";
        int folderIDForDB = 0;//Folder DB에 들어가는 아이디
        long _pictureTakenTime = 0;//현재 읽고 있는 사진 이전의 찍힌 시간
        String representativeImage = "";//폴더에 들어가는 대표이미지의 경로, 일단 폴더에 들어가는 첫번째 사진으로 한다.
        String thumbNailID = "";//폴더에 들어가는 썸네일 사진의 이름, 일단 폴더에 들어가는 첫번째 사진으로 한다.
        int pictureNumInStory = 0;//특정 스토리에 들어가는 사진의 개수를 센다
        String folderThumbnailName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/thumbnail/";
        FolderManage.makeDirectory(folderThumbnailName);


        while (ImageSetter.mCursor.moveToNext()) {
            final String path = ImageSetter.mCursor.getString(ImageSetter.mCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));

            Log.d("사진 분류", path);
            //썸네일 사진들은 분류대상에서 제외한다
            if (path.contains("thumbnail") || path.contains("스토리")) {
                Log.d("pictureClassification", "썸네일 및 기존 스토리는 분류 대상에서 제외");
                continue;
            }

            //picture = new File(path);
            //사진 ID
            final int pictureID = ImageSetter.mCursor.getInt(ImageSetter.mCursor.getColumnIndex(MediaStore.MediaColumns._ID));
            Media ExistedMedia = db.getMediaById(pictureID);//pictureID에 해당하는 사진이 이미 DB에 등록되어 있는지 확인한다
            Log.d("Media","ExistedMedia == null : "+(ExistedMedia==null));
            //TODO 사진의 경로가 바뀌어도 아이디가 그대로 유지되는지 확인해볼것
            //사진이 촬영된 날짜
            long pictureTakenTime = ImageSetter.mCursor.getLong(ImageSetter.mCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN));
            //pictureTakenTime *= 1000; //second->millisecond
            //millisecond -> Calendar
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(pictureTakenTime);
            String folderID = "" + cal.get(Calendar.YEAR) + "_" + (cal.get(Calendar.MONTH) + 1) + "_" + cal.get(Calendar.DATE);
            if (representativeImage.equals("")) {
                //representativeImage = String.valueOf(pictureID);
                representativeImage = path;//폴더에 들어갈 첫번째 사진의 경로
                thumbNailID = String.valueOf(pictureID);
            }

            //썸네일 이미지를 생성한다
            File ExisTedThumbNail = new File(folderThumbnailName + String.valueOf(pictureID) + ".jpg");
            if (!ExisTedThumbNail.exists()) {//썸네일이 없는 경우
                Bitmap bitmap;
                if((bitmap = ImageSetter.getThumbnail(pictureID)) == null){//안드로이드 자체의 썸네일도 없는 경우
                    BitmapFactory.Options opt = new BitmapFactory.Options();
                    opt.inSampleSize = 16;//기존 해상도의 1/16로 줄인다
                    bitmap = BitmapFactory.decodeFile(path, opt);
                    Log.e("asdad", "is here");
                }
                createThumbnail(bitmap, folderThumbnailName, String.valueOf(pictureID) + ".jpg");
            }


            Log.d("MainActivity", "[pictureID] : " + String.valueOf(pictureID) + " [pictureTakenTime] : " + Long.toString(pictureTakenTime));

            //이전에 읽었던 사진과 시간 차이가 CONSTANT.TIMEINTERVAL보다 크면 새로 폴더를 만든다.
            Log.d("MainActivity", "pictureTakenTime-_pictureTakenTime = " + (pictureTakenTime - _pictureTakenTime));
            if (pictureTakenTime - _pictureTakenTime > CONSTANT.TIMEINTERVAL) {
                //이전에 만들어진 폴더의 이름을 바꾼다(startFolderID ~ endFolderID)
                if (!startFolderID.equals("")) {
                    //File new_name = null;
                    String new_name;
                    if (!startFolderID.equals(endFolderID))
                        new_name = startFolderID + "~" + endFolderID + "의 스토리";
                        //new_name = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/" + startFolderID + "~" + endFolderID + "의 스토리");
                    else
                        new_name = startFolderID + "의 스토리";
                    //new_name = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/" + startFolderID + "의 스토리");

                    //String _new_name=FolderManage.reNameFile(dir, new_name);
                    //Folder DB에 넣는다.
                    Folder f = new Folder(folderIDForDB, new_name, representativeImage, thumbNailID,pictureNumInStory);
                    db.createFolder(f);
                    //메인 액티비티에게 하나의 스토리가 정리되었음을 알린다
                    sendMessageToUI(ServiceOfPictureClassification.END_OF_SINGLE_STORY,thumbNailID,new_name,folderIDForDB,pictureNumInStory);
                    pictureNumInStory = 0;
                    representativeImage = "";
                    //Log.d("MainActivity","tempEattle 폴더 이름 변경");
                    Log.d("MainActivity", "Folder DB 입력 완료");
                }

                //방금 읽은 사진의 folderID가 시작날짜가 된다.
                startFolderID = folderID;
                //tempEattle이라는 이름으로 임시 폴더를 만든다.
                //dir = FolderManage.makeDirectory(folderName);

                folderIDForDB++;
            }
            //사진에 위치 정보가 있으면 얻어온다
            //mediaDB에 pictureID를 가진 사진이 있는지 확인한다
            String placeName_ = "";
            double longitude = 0.0;
            double latitude = 0.0;

            if (ExistedMedia != null) {//해당 사진이 기존에 있었을 경우
                Log.d(Tag, "기존에 존재하는 사진에 대해서 위치 조회 안함");
                placeName_ = ExistedMedia.getPlaceName();
            }
            else {//새로운 사진
        /*
                //위치 정보가 없으면 longitude = 0.0, latitude = 0.0이 들어감
                longitude = ImageSetter.mCursor.getDouble(ImageSetter.mCursor.getColumnIndex(MediaStore.Images.ImageColumns.LONGITUDE));
                latitude = ImageSetter.mCursor.getDouble(ImageSetter.mCursor.getColumnIndex(MediaStore.Images.ImageColumns.LATITUDE));
                //위치 정보를 토대로 지명이름을 가져온다
                List<Address> placeName = null;
                if (longitude != 0 && latitude != 0) {
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
                        Log.e("PictureClassification", "~" + placeName.get(0).getAdminArea() + "~" + placeName.get(0).getCountryCode() + "~" + placeName.get(0).getFeatureName() + "~" + placeName.get(0).getLocality() + "~" + placeName.get(0).getSubAdminArea() + "~" + placeName.get(0).getSubLocality() + "~" + placeName.get(0).getSubThoroughfare() + "~" + placeName.get(0).getThoroughfare() + "~" + placeName.get(0).getMaxAddressLineIndex());
                        //ex)~서울특별시~KR~54~강남구~null~null~54~선릉로93길~0
                        //~서울특별시~KR~619-26~강남구~null~null~619-26~역삼동~0
                    }

                }
        */
            }


            //DB에 사진 데이터를 넣는다.
            if(ExistedMedia == null) {//새로운 사진
                Media m = new Media(pictureID, folderIDForDB, "" + pictureID, pictureTakenTime, cal.get(Calendar.YEAR), (cal.get(Calendar.MONTH) + 1), cal.get(Calendar.DATE), latitude, longitude, placeName_, path);
                //medias.add(m);
                db.createMedia(m);

            }
            else {//기존 사진
                //업데이트만 한다
                ExistedMedia.setFolder_id(folderIDForDB);
                ExistedMedia.setPath(path);
                //Log.d(Tag,"기존에 존재하는 사진 : "+ExistedMedia.getId()+" "+ExistedMedia.getFolder_id()+" "+ExistedMedia.getName()+" "+ExistedMedia.getYear()+" "+ExistedMedia.getMonth()+" "+ExistedMedia.getDay()+" "+ExistedMedia.getLatitude()+" "+ExistedMedia.getLongitude()+" "+ExistedMedia.getPlaceName()+" "+ExistedMedia.getPath());
                db.updateMedia(ExistedMedia);
            }
            //USB에 사진을 백업
            //1. 이미 USB에 있는 사진일 경우
            //2. 새로운 사진
            if(CONSTANT.ISUSBCONNECTED == 1) {
                //fileSystem.addElementPush(pictureID + ".jpg", CONSTANT.BLOCKDEVICE, path);
                Log.d("service",pictureID+".jpg 백업 완료");

            }


            pictureNumInStory++;

            _pictureTakenTime = pictureTakenTime;
            endFolderID = folderID;
        }

        //마지막 남은 폴더를 처리한다.
        //이전에 만들어진 폴더의 이름을 바꾼다(startFolderID ~ endFolderID)
        String new_name = null;
        if (!startFolderID.equals("")) {
            //File new_name = null;
            if (!startFolderID.equals(endFolderID)) {
                new_name = startFolderID + "~" + endFolderID + "의 스토리";
                //new_name = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/" + startFolderID + "~" + endFolderID + "의 스토리");
            } else
                new_name = startFolderID + "의 스토리";
            //new_name = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/" + startFolderID + "의 스토리");
            //String _new_name = FolderManage.reNameFile(dir, new_name);
            //Folder DB에 넣는다.
            Folder f = new Folder(folderIDForDB, new_name, representativeImage, thumbNailID,pictureNumInStory);
            db.createFolder(f);
            Log.d("MainActivity", "tempEattle 폴더 이름 변경");
        }
        //db.createSeveralMedia(medias);//사진 목록들을 한꺼번에 DB에 넣는다

        //메인화면의 스토리 목록을 갱신한다.
        //drawMainView();
        //MainActivity에 메세지를 보낸다

        sendMessageToUI(ServiceOfPictureClassification.END_OF_SINGLE_STORY,thumbNailID,new_name,folderIDForDB,pictureNumInStory);
        sendMessageToUI(ServiceOfPictureClassification.END_OF_PICTURE_CLASSIFICATION, 1);

        ImageSetter.mCursor.close();
    }

    //썸네일 생성 함수
    public static void createThumbnail(Bitmap bitmap, String strFilePath, String filename) {

        File file = new File(strFilePath);

        if (!file.exists()) {
            file.mkdirs();
            // Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
        }
        File fileCacheItem = new File(strFilePath + filename);
        //strFilePath+filename이 이미 존재한다면, 썸네일을 만들 필요가 없다
        if (fileCacheItem.exists()) {
            Log.d("createThumbnail", "썸네일이 이미 존재합니다");
            return;
        }

        OutputStream out = null;


        try {
            int height = bitmap.getHeight();
            int width = bitmap.getWidth();

            fileCacheItem.createNewFile();
            out = new FileOutputStream(fileCacheItem);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(Tag, "서비스 onDestroy() 호출");
        isRunning = false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //return null;
        return mMessenger.getBinder();
    }
}
