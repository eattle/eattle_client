package com.eattle.phoket;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.eattle.phoket.helper.DatabaseHelper;
import com.eattle.phoket.model.Folder;
import com.eattle.phoket.model.Manager;
import com.eattle.phoket.model.Media;
import com.eattle.phoket.model.NotificationM;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

//사진 분류, 백업 등을 담당하는 서비스
public class ServiceOfPictureClassification extends Service {
    public static final String EXTRA_TAG = "EATTLE_SERVICE";

    public static boolean isClassifying = false;//사진 분류중이면 1, 평상시엔 0

    //사진 정리 관련된 정보를 보내는 브로드 캐스트 알림
    //이 변수를 이용해 이전에 메시지를 보내는 것과 같은 방식으로 메인에 의사 전달을 할 수 있음
    private BroadcastNotifier mBroadcaster;
    //main으로부터 오는 메시지를 받는 메신저
    //final Messenger mMessenger = new Messenger(new IncomingHandler(this));


    //사진 정리와 관련된 변수들
    int totalPictureNum = 0;//사진들의 총 개수
    long totalInterval;//사진 간격의 총합
    long standardDerivation = 0;//사진 간격의 표준편차
    Cursor mCursor;
    ContentResolver mCr;

    //앨범의 Image Setting(미디어 DB 연결)
//    static AlbumImageSetter ImageSetter;

    //데이터베이스 관련 변수들
    DatabaseHelper db;

    //장소 관련(역지오코딩)
//    LocationManager mLocMan;
    Geocoder mCoder;
    //IncomingHandler incomingHandler = new IncomingHandler(this);

    //파일시스템
//    FileSystem fileSystem;


    BroadcastListener broadcastListener = null;
    public ServiceOfPictureClassification() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(EXTRA_TAG, "서비스 onCreate() 호출");
        unregisterRestartService();

        //쓰레드를 생성하여 사진 관련 서비스 시작
        PictureThread serviceOfEattle = new PictureThread();
        serviceOfEattle.start();
        mBroadcaster = new BroadcastNotifier(this);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(EXTRA_TAG, "Service onStartCommand");
        if(intent!=null) {
            switch (intent.getIntExtra("what", -1)) {
                case CONSTANT.START_OF_PICTURE_CLASSIFICATION:
                    Log.d("IncomingHandler", "[ServiceOfPictureClassification]message 수신! handleMessage() - START_OF_PICTURE_CLASSIFICATION || 'MainActivity가 사진 정리를 요청하였습니다' ");
                    if (!isClassifying) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {//사진 정리를 시작한다
                                    isClassifying = true;
                                    mBroadcaster.broadcastIntentWithState(CONSTANT.RECEIPT_OF_PICTURE_CLASSIFICATION, 1);
                                    pictureClassification();
                                } catch (IOException e) {
                                    Log.d("PictureClassification", e.getMessage());
                                } catch (Exception e) {
                                    Log.d("PictureClassification", e.getMessage());
                                } finally {
                                    isClassifying = false;
                                    mBroadcaster.broadcastIntentWithState(CONSTANT.END_OF_PICTURE_CLASSIFICATION);
                                }
                            }
                        }).start();
                    }

                    break;
                case CONSTANT.START_OF_GUIDE:
                    Log.d("IncomingHandler", "[ServiceOfPictureClassification]message 수신! handleMessage() - START_OF_GUIDE || 'MainActivity가 가이드 시작을 요청하였습니다' ");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {//가이드를 시작한다
                                isClassifying = true;
                                pictureClassification_guide();
                            } catch (IOException e) {
                                Log.d("PictureClassification", e.getMessage());
                            } catch (Exception e) {
                                Log.d("PictureClassification", e.getMessage());
                            } finally {
                                isClassifying = false;
                            }
                        }
                    }).start();
                    break;

                default:
                    break;
            }
        }
        return Service.START_STICKY;
    }


    @Override
    public IBinder onBind(Intent arg0) {
        Log.i(EXTRA_TAG, "Service onBind");
        return null;
    }

    @Override
    public void onDestroy() {
        Log.i(EXTRA_TAG, "Service onDestroy");
        unregisterReceiver(broadcastListener);
        registerRestartService();//서비스가 죽으면 다시 살리기 위해서
    }


    class PictureThread extends Thread {

        public void run() {
            Log.d(EXTRA_TAG, "ServiceOfPictureClassification Run() 호출");
            db = DatabaseHelper.getInstance(getApplicationContext());

            NotificationM n = db.getNotification();
            //86400000
            //마지막 푸시를 넣은지 24시간을 넘지 않았으면
            if(n != null) {
                if (System.currentTimeMillis() - n.getNotificationTime() < 86400000L)
                    BroadcastListener.setHowOftenCheck(CONSTANT.ONEDAY);//24시간후에 다시 체크
                else
                    BroadcastListener.setHowOftenCheck(CONSTANT.CHECK);//10분마다 체크
            }


            //Notification을 위한 브로드캐스트 리시버
            if(broadcastListener == null)
                broadcastListener = new BroadcastListener(getApplicationContext());
            registerReceiver(broadcastListener, new IntentFilter(Intent.ACTION_TIME_TICK));

            Looper.prepare();
        }
    }

    /*
    //MainActivity로 부터 온 메세지를 받는 부분
    private static class IncomingHandler extends Handler {
        private final WeakReference<ServiceOfPictureClassification> mReference;

        IncomingHandler(ServiceOfPictureClassification service) {
            mReference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            final ServiceOfPictureClassification service = mReference.get();
            switch (msg.what) {
                case CONSTANT.START_OF_PICTURE_CLASSIFICATION:
                    Log.d("IncomingHandler", "[ServiceOfPictureClassification]message 수신! handleMessage() - START_OF_PICTURE_CLASSIFICATION || 'MainActivity가 사진 정리를 요청하였습니다' ");
                    if (service != null && !isClassifying) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {//사진 정리를 시작한다
                                    isClassifying = true;
                                    service.mBroadcaster.broadcastIntentWithState(CONSTANT.RECEIPT_OF_PICTURE_CLASSIFICATION, 1);
                                    service.pictureClassification();
                                } catch (IOException e) {
                                    Log.d("PictureClassification", e.getMessage());
                                } catch (Exception e) {
                                    Log.d("PictureClassification", e.getMessage());
                                }finally {
                                    isClassifying = false;
                                    service.mBroadcaster.broadcastIntentWithState(CONSTANT.END_OF_PICTURE_CLASSIFICATION);
                                }
                            }
                        }).start();
                    }

                    break;
                case CONSTANT.START_OF_GUIDE:
                    Log.d("IncomingHandler", "[ServiceOfPictureClassification]message 수신! handleMessage() - START_OF_GUIDE || 'MainActivity가 가이드 시작을 요청하였습니다' ");
                    if (service != null) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {//가이드를 시작한다
                                    isClassifying = true;
                                    service.pictureClassification_guide();
                                } catch (IOException e) {
                                    Log.d("PictureClassification", e.getMessage());
                                } catch (Exception e) {
                                    Log.d("PictureClassification", e.getMessage());
                                }finally {
                                    isClassifying = false;
                                }
                            }
                        }).start();
                    }
                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    }*/


    //사진 정리와 관련된 함수들
    private void calculatePictureInterval() {//사진간 시간 간격을 계산하는 함수
        totalInterval = 0;
        totalPictureNum = 0;
        mCursor.moveToFirst();

//        ImageSetter.setCursor(0, 0);//커서의 위치를 처음으로 이동시킨다.
        long pictureTakenTime = 0;
        while (mCursor.moveToNext()) {
            String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));

            //Log.d("MainActivity", "!!" + path);
            //썸네일 사진들은 계산대상에서 제외한다
            if (path.contains("thumbnail") || path.contains("Screenshot") || path.contains("screenshot")) {
                //Log.d("pictureClassification", "썸네일 및 기존 스토리는 계산대상에서 제외");
                continue;
            }
            //사진이 촬영된 날짜
            long _pictureTakenTime = mCursor.getLong(mCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN));
            //_pictureTakenTime *= 1000; //second->millisecond
            if (pictureTakenTime == 0)
                pictureTakenTime = _pictureTakenTime;

            totalInterval += _pictureTakenTime - pictureTakenTime;
            pictureTakenTime = _pictureTakenTime;
            totalPictureNum++;
        }
    }

    private void pictureClassification() throws Exception {//시간간격을 바탕으로 사진들을 분류하는 함수
        String TAG = "classification";
        //MainActivity에게 사진 정리를 시작했다는 메세지를 보낸다.

//        sendMessageToUI(CONSTANT.RECEIPT_OF_PICTURE_CLASSIFICATION,isClassifying);


        //pictureClassification()의 속도 개선 방안
        //1. 처음에 mediaDB를 전부 삭제하지 않는다
        // -> folderIDForDB만 업데이트 한다
        // -> 위치 정보가 있으면 새로 받지 않는다
        // -> 사진의 경로가 변경되었는지 확인한다
        //2. folderDB는 기존대로 전부 삭제한다
        //3. 특정 사진에 있던 태그들은 삭제하지 않는다


        mCoder = new Geocoder(this);
        mCr = this.getContentResolver();
        mCursor = mCr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Images.ImageColumns.DATE_TAKEN + " ASC");
        calculatePictureInterval();//사진의 시간간격의 총합을 구한다.

        Log.d("pictureClassification","사진 정리 시작");
        long averageInterval = totalInterval;
        if (totalPictureNum != 0)
            averageInterval /= totalPictureNum;
        CONSTANT.TIMEINTERVAL = averageInterval;
        //DB를 참조한다.
        Manager _m = new Manager(totalPictureNum, averageInterval, standardDerivation);
        db.createManager(_m);//Manager DB에 값들을 집어넣음

        db.deleteAllFolder();//DB에 있는 폴더 데이터를 초기화 한다(isFixed == 1<- 고정 스토리)은 제외되어 있음

        String startFolderID = "";
        String endFolderID = "!";

        int folderIDForDB = 0;//Folder DB에 들어가는 아이디
        long _pictureTakenTime = 0;//현재 읽고 있는 사진 이전의 찍힌 시간, 초기값은 현재 시간
        String representativeImage = "";//폴더에 들어가는 대표이미지의 경로, 일단 폴더에 들어가는 첫번째 사진으로 한다.
        String thumbNailID = "";//폴더에 들어가는 썸네일 사진의 이름, 일단 폴더에 들어가는 첫번째 사진으로 한다.
        String thumbnail_path = "";//사진의 썸네일 경로
        String representativeThumbnail_path = "";//폴더의 대표 사진의 썸네일 경로
        int pictureID = -1;//사진의 고유 ID(안드로이드가 지정한 ID)
        int pictureNumInStory = 0;//특정 스토리에 들어가는 사진의 개수를 센다
        String previousStoryName = "";//중복 날짜 스토리를 처리하기 위한 변수
        int overlappedNum = 1;//해당 스토리가 몇번째 중복 스토리인지
        int tempFixedFolder=0;//고정 스토리 판별을 위한 flag
        List<Folder> fixedFolders = db.getFixedFolder();//고정 스토리 목록
        for(int i=0;i<fixedFolders.size();i++)
            Log.d(TAG,"고정 스토리 목록 : "+fixedFolders.get(i).getId());
        mCursor.moveToLast();//마지막 사진부터 정리 == 현재에서 가장 가까운 스토리부터
        do {
            final String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));

            Log.d("사진 분류", path);
            //썸네일 사진들은 분류대상에서 제외한다
            if (path.contains("thumbnail") || path.contains("Screenshot") || path.contains("screenshot")) {
                //Log.d("pictureClassification", "썸네일 및 기존 스토리는 분류 대상에서 제외");
                continue;
            }

            //picture = new File(path);
            //사진 ID
            pictureID = mCursor.getInt(mCursor.getColumnIndex(MediaStore.MediaColumns._ID));
            thumbnail_path = CONSTANT.getThumbnailPath(mCr,pictureID);


            Media ExistedMedia = db.getMediaById(pictureID);//pictureID에 해당하는 사진이 이미 DB에 등록되어 있는지 확인한다


            //사진이 촬영된 날짜
            long pictureTakenTime = mCursor.getLong(mCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN));


            //pictureTakenTime *= 1000; //second->millisecond
            //millisecond -> Calendar
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(pictureTakenTime);
            String folderID = "" + cal.get(Calendar.YEAR) + "_" + (cal.get(Calendar.MONTH) + 1) + "_" + cal.get(Calendar.DATE);


            Log.d("MainActivity", "[pictureID] : " + String.valueOf(pictureID) + " [pictureTakenTime] : " + Long.toString(pictureTakenTime));

            //이전에 읽었던 사진과 시간 차이가 CONSTANT.TIMEINTERVAL보다 크면 새로 폴더를 만든다.
            Log.d("MainActivity", "CONSTANT.TIMEINTERVAL " + CONSTANT.TIMEINTERVAL);
            Log.d("MainActivity", "_pictureTakenTime-pictureTakenTime = " + Math.abs(_pictureTakenTime - pictureTakenTime));
            if ((Math.abs(_pictureTakenTime - pictureTakenTime) > CONSTANT.TIMEINTERVAL)
                    && tempFixedFolder == 0) {//고정 스토리에 속한 사진이 아니어야 함
                //이전에 만들어진 폴더의 이름을 바꾼다(endFolderID ~ startFolderID)
                Log.d("MainActivity", "startFolderID  " + startFolderID + " endFolderID : " + endFolderID);
                if (!startFolderID.equals("")) {
                    String new_name;
                    if (!startFolderID.equals(endFolderID))
                        new_name = endFolderID + "~" + startFolderID + "의 스토리";
                    else
                        new_name = startFolderID + "의 스토리";

                    if (pictureNumInStory > CONSTANT.BOUNDARY) {//'일상'이 아닌 '스토리'에 대해
                        if (previousStoryName.equals(new_name)) {//중복 날짜 스토리
                            Log.d("MainActivity", "중복 날짜 스토리 : " + new_name);
                            overlappedNum++;
                            new_name += (" - " + overlappedNum);//스토리 이름 뒤에 숫자를 붙여준다
                        } else {//중복 날짜 스토리가 아니면
                            Log.d("MainActivity", "중복 날짜가 아닌 스토리 : " + new_name);
                            overlappedNum = 1;
                            previousStoryName = new_name;
                        }
                    }

                    Folder f = new Folder(folderIDForDB, new_name, representativeImage, representativeThumbnail_path , pictureNumInStory, Integer.parseInt(thumbNailID), 0);
                    //db.createFolder(f);
                    //메인 액티비티에게 하나의 스토리가 정리되었음을 알린다
                    mBroadcaster.broadcastIntentWithState(CONSTANT.END_OF_SINGLE_STORY, db.createFolder(f));
//                    sendMessageToUI(CONSTANT.END_OF_SINGLE_STORY,db.createFolder(f));
                    pictureNumInStory = 0;
                    representativeImage = "";
                    Log.d("MainActivity", "Folder DB 입력 완료");
                }

                //방금 읽은 사진의 folderID가 시작날짜가 된다.
                startFolderID = folderID;

                //다음 스토리의 ID(folderIDForDB)를 결정하는 부분.(고정 스토리와 중복되지 않도록 한다)
                int j;
                while(true) {
                    folderIDForDB++;
                    for(j=0;j<fixedFolders.size();j++){
                        if(folderIDForDB == fixedFolders.get(j).getId())
                            break;
                    }
                    if(j == fixedFolders.size())
                        break;
                }

            }

            if((ExistedMedia != null) &&
                    ((ExistedMedia.getFolder_id() == -1) || ExistedMedia.getIsFixed() == 1)) {//휴지통에 들어있는 사진 || 고정 스토리에 속한 사진
                //썸네일 경로가 바뀌었을 수도 있으므로 업데이트한다
                ExistedMedia.setThumbnail_path(thumbnail_path);
                db.updateMedia(ExistedMedia);
                Log.d(TAG,"휴지통에 들어있는 사진 || 고정 스토리에 속한 사진");
                //TODO 고정 스토리에 속하는 사진들이 외부의 조작으로 인하여 사진의 경로가 달라졌는지 확인해야한다
                //TODO File 객체 생성하여 사진 파일 존재여부 체크.
                //TODO 사진의 경로가 달라졌다면 '고정 스토리를 해제하는 절차'를 거쳐야 한다.

                if(tempFixedFolder == ExistedMedia.getFolder_id())
                    continue;

                if(ExistedMedia.getIsFixed() == 1) {
                    int folderID_ = ExistedMedia.getFolder_id();
                    Log.d(TAG,"고정 스토리 메인 액티비티로 전송");
                    mBroadcaster.broadcastIntentWithState(CONSTANT.END_OF_SINGLE_STORY, folderID_);

//                    sendMessageToUI(CONSTANT.END_OF_SINGLE_STORY,folderID_);
                    tempFixedFolder = folderID_;
                }
                continue; //정리에 포함시키지 않는다.
            }
            //TODO 사진의 경로가 바뀌어도 아이디가 그대로 유지되는지 확인해볼것 -- 유지됨
            tempFixedFolder = 0;//고정 스토리 판별을 위한 flag

            if (representativeImage.equals("")) {
                //representativeImage = String.valueOf(pictureID);
                representativeImage = path;//폴더에 들어갈 첫번째 사진의 경로
                representativeThumbnail_path = thumbnail_path;
                thumbNailID = String.valueOf(pictureID);
            }
            //사진에 위치 정보가 있으면 얻어온다
            //mediaDB에 pictureID를 가진 사진이 있는지 확인한다
            String placeName_ = "";
            double longitude = 0.0;
            double latitude = 0.0;

            if (ExistedMedia != null) {//해당 사진이 기존에 있었을 경우
                Log.d(EXTRA_TAG, "기존에 존재하는 사진에 대해서 위치 조회 안함");
                placeName_ = ExistedMedia.getPlaceName();
            } else {//새로운 사진

            }


            //DB에 사진 데이터를 넣는다.
            if (ExistedMedia == null) {//새로운 사진
                String[] pathArr = path.split("/");

                Media m = new Media(pictureID, folderIDForDB, pathArr[pathArr.length - 1] , pictureTakenTime, cal.get(Calendar.YEAR), (cal.get(Calendar.MONTH) + 1), cal.get(Calendar.DATE), latitude, longitude, placeName_, path, thumbnail_path,0);
                db.createMedia(m);
                String folderNameForPicture = pathArr[pathArr.length - 2];
                if(!folderNameForPicture.contains("스토리"))//사진이 속하는 폴더 이름에 '스토리'가 없을 때에만
                    db.createTag(folderNameForPicture, pictureID);//사진이 속했던 폴더 이름으로 태그 만들기(디폴트 태그)
                Log.d(TAG,"미디어 id "+pictureID+" 에 대해 createMedia() 호출 (folderIDForDB : "+folderIDForDB+")");
            } else {//기존 사진
                //업데이트만 한다
                ExistedMedia.setFolder_id(folderIDForDB);
                ExistedMedia.setPath(path);
                ExistedMedia.setThumbnail_path(thumbnail_path);//처음에 정리할때는 내장 썸네일이 없었다가 나중에 생겼을 수도 있음
                //Log.d(Tag,"기존에 존재하는 사진 : "+ExistedMedia.getId()+" "+ExistedMedia.getFolder_id()+" "+ExistedMedia.getName()+" "+ExistedMedia.getYear()+" "+ExistedMedia.getMonth()+" "+ExistedMedia.getDay()+" "+ExistedMedia.getLatitude()+" "+ExistedMedia.getLongitude()+" "+ExistedMedia.getPlaceName()+" "+ExistedMedia.getPath());
                db.updateMedia(ExistedMedia);
                Log.d(TAG,"미디어 id "+pictureID+" 에 대해 updateMedia() 호출 (folderIDForDB : "+folderIDForDB+")");
            }
            //USB에 사진을 백업
            //1. 이미 USB에 있는 사진일 경우
            //2. 새로운 사진
/*            if (CONSTANT.ISUSBCONNECTED == 1) {
                if (fileSystem.stringSearch(pictureID + ".jpg")[0] == -1) //USB에 없는 사진이면
                    fileSystem.addElementPush(pictureID + ".jpg", CONSTANT.BLOCKDEVICE, path);//USB로 백업

                Log.d("service", pictureID + ".jpg 백업 완료");
            }*/


            pictureNumInStory++;

            _pictureTakenTime = pictureTakenTime;
            endFolderID = folderID;
            Log.d("classification","pictureNumInStory : "+pictureNumInStory);
            Log.d("classification","------------------------------------------------------------");
        } while (mCursor.moveToPrevious());

        //마지막 남은 폴더를 처리한다.
        //이전에 만들어진 폴더의 이름을 바꾼다(startFolderID ~ endFolderID)
        String new_name = null;
        if (!startFolderID.equals("")) {
            if (!startFolderID.equals(endFolderID)) {
                new_name = endFolderID + "~" + startFolderID + "의 스토리";
            } else {
                new_name = startFolderID + "의 스토리";
            }

            if (previousStoryName.equals(new_name) && pictureNumInStory > CONSTANT.BOUNDARY) {//'일상'이 아닌 '스토리'에 대해 중복 날짜 스토리
                Log.d("MainActivity", "중복 날짜 스토리 : " + new_name);
                overlappedNum++;
                new_name += (" - " + overlappedNum);//스토리 이름 뒤에 숫자를 붙여준다
            }
        }

        Folder f = new Folder(folderIDForDB, new_name, representativeImage, representativeThumbnail_path , pictureNumInStory, Integer.parseInt(thumbNailID), 0);

        mBroadcaster.broadcastIntentWithState(CONSTANT.END_OF_SINGLE_STORY, db.createFolder(f));

//        sendMessageToUI(CONSTANT.END_OF_SINGLE_STORY, db.createFolder(f));
//        sendMessageToUI(CONSTANT.END_OF_PICTURE_CLASSIFICATION, 1);
//        isClassifying = 0;
        mCursor.close();
    }

    private void pictureClassification_guide() throws Exception {//시간간격을 바탕으로 사진들을 분류하는 함수
        String TAG = "classification";
        //isClassifying = 1;
        //MainActivity에게 사진 정리를 시작했다는 메세지를 보낸다.
        //sendMessageToUI(CONSTANT.RECEIPT_OF_PICTURE_CLASSIFICATION,isClassifying);

        Log.d("guide","가이드 시작");
        db.deleteAllFolder();//가이드 도중에 앱을 종료하고 다시 시작할 경우를 대비
        db.deleteAllMedia();//가이드 도중에 앱을 종료하고 다시 시작할 경우를 대비
        long currentTime = System.currentTimeMillis();//현재 시간
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(currentTime);
        String folderName = "" + cal.get(Calendar.YEAR) + "_" + (cal.get(Calendar.MONTH) + 1) + "_" + cal.get(Calendar.DATE)+"의 스토리";
        Folder f = new Folder(1, folderName, "" , null , 8, 0, 0);
        Media m1 = new Media(1, 1, "phoket1" , currentTime, cal.get(Calendar.YEAR), (cal.get(Calendar.MONTH) + 1), cal.get(Calendar.DATE), 0, 0, null, null, null,0);
        Media m2 = new Media(2, 1, "phoket2" , currentTime, cal.get(Calendar.YEAR), (cal.get(Calendar.MONTH) + 1), cal.get(Calendar.DATE), 0, 0, null, null, null,0);
        Media m3 = new Media(3, 1, "phoket3" , currentTime, cal.get(Calendar.YEAR), (cal.get(Calendar.MONTH) + 1), cal.get(Calendar.DATE), 0, 0, null, null, null,0);
        Media m4 = new Media(4, 1, "phoket4" , currentTime, cal.get(Calendar.YEAR), (cal.get(Calendar.MONTH) + 1), cal.get(Calendar.DATE), 0, 0, null, null, null,0);
        Media m5 = new Media(5, 1, "phoket5" , currentTime, cal.get(Calendar.YEAR), (cal.get(Calendar.MONTH) + 1), cal.get(Calendar.DATE), 0, 0, null, null, null,0);
        Media m6 = new Media(6, 1, "phoket6" , currentTime, cal.get(Calendar.YEAR), (cal.get(Calendar.MONTH) + 1), cal.get(Calendar.DATE), 0, 0, null, null, null,0);
        Media m7 = new Media(7, 1, "phoket7" , currentTime, cal.get(Calendar.YEAR), (cal.get(Calendar.MONTH) + 1), cal.get(Calendar.DATE), 0, 0, null, null, null,0);
        Media m8 = new Media(8, 1, "phoket8" , currentTime, cal.get(Calendar.YEAR), (cal.get(Calendar.MONTH) + 1), cal.get(Calendar.DATE), 0, 0, null, null, null,0);
        db.createMedia(m1);
        db.createMedia(m2);
        db.createMedia(m3);
        db.createMedia(m4);
        db.createMedia(m5);
        db.createMedia(m6);
        db.createMedia(m7);
        db.createMedia(m8);

        //db.createFolder(f);
        //메인 액티비티에게 하나의 스토리가 정리되었음을 알린다
        mBroadcaster.broadcastIntentWithState(CONSTANT.END_OF_SINGLE_STORY_GUIDE, db.createFolder(f));
    }
    //서비스가 죽었을 때 다시 살리기 위한 함수
    public void registerRestartService() {
        Log.d(EXTRA_TAG, "registerRestartService() 호출");
        Intent intent = new Intent(ServiceOfPictureClassification.this,BroadcastListener.class);
        intent.setAction(BroadcastListener.ACTION_RESTART_PERSISTENTSERVICE);
        intent.putExtra("countForTick", BroadcastListener.getCountForTick());
        intent.putExtra("HOWOFTENCHECK",BroadcastListener.getHOWOFTENCHECK());
        Log.d(EXTRA_TAG, "countForTick "+BroadcastListener.getCountForTick());
        Log.d(EXTRA_TAG, "HOWOFTENCHECK " + BroadcastListener.getHOWOFTENCHECK());
        PendingIntent sender = PendingIntent.getBroadcast(
                ServiceOfPictureClassification.this,0,intent,0);
        long currentTime = SystemClock.elapsedRealtime();//현재 시간
        currentTime += 1*1000;
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME, currentTime, 60 * 1000, sender);
    }
    //서비스가 죽었을 때 다시 살리기 위한 함수
    public void unregisterRestartService(){
        Log.d(EXTRA_TAG,"unregisterRestartService() 호출");
        Intent intent = new Intent(ServiceOfPictureClassification.this,BroadcastListener.class);
        intent.setAction(BroadcastListener.ACTION_RESTART_PERSISTENTSERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(
                ServiceOfPictureClassification.this,0,intent,0);
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        am.cancel(sender);
    }

}

