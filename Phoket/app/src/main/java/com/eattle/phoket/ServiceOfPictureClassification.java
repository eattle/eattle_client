package com.eattle.phoket;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.Geocoder;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;

import com.eattle.phoket.helper.DatabaseHelper;
import com.eattle.phoket.model.Folder;
import com.eattle.phoket.model.Manager;
import com.eattle.phoket.model.Media;
import com.eattle.phoket.model.NotificationM;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Iterator;
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

    //데이터베이스 관련 변수들
    DatabaseHelper db;

    //장소 관련(역지오코딩)
//    LocationManager mLocMan;
    Geocoder mCoder;
    //IncomingHandler incomingHandler = new IncomingHandler(this);



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
        if (intent != null) {
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
                                mBroadcaster.broadcastIntentWithState(CONSTANT.RECEIPT_OF_PICTURE_CLASSIFICATION, 1);
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
            if (n != null) {
                if (System.currentTimeMillis() - n.getNotificationTime() < 86400000L)
                    BroadcastListener.setHowOftenCheck(CONSTANT.ONEDAY);//24시간후에 다시 체크
                else
                    BroadcastListener.setHowOftenCheck(CONSTANT.CHECK);//10분마다 체크
            }


            //Notification을 위한 브로드캐스트 리시버
            if (broadcastListener == null)
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
        mCursor.moveToLast();

//        ImageSetter.setCursor(0, 0);//커서의 위치를 처음으로 이동시킨다.
        long pictureTakenTime = 0;

        do {
            /** ------------------사진 정보 획득------------------ **/
            String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
            int pictureID = mCursor.getInt(mCursor.getColumnIndex(MediaStore.MediaColumns._ID));//사진 고유(안드로이드 상)의 ID
            Media ExistedMedia = db.getMediaById(pictureID);//pictureID에 해당하는 사진이 이미 DB에 등록되어 있는지 확인한다

            /** ------------------정리 제외 대상------------------ **/
            if (path.contains("thumbnail") || path.contains("Screenshot") || path.contains("screenshot"))
                continue;
            //해당 경로에 존재하지 않는 사진은 건너띈다
            if (!isExisted(path) && ExistedMedia != null && ExistedMedia.getIsFixed() == 0)
                //ExistedMedia에 있는 사진인데 경로에 없으면서 고정 스토리에 속한 사진이 아닐 경우
                continue;




            //사진이 촬영된 날짜
            long _pictureTakenTime = mCursor.getLong(mCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN));
            //_pictureTakenTime *= 1000; //second->millisecond
            if (pictureTakenTime == 0)
                pictureTakenTime = _pictureTakenTime;

            totalInterval += pictureTakenTime - _pictureTakenTime;
            pictureTakenTime = _pictureTakenTime;
            totalPictureNum++;
        }while (mCursor.moveToPrevious());
    }


    int overlappedNum;
    String previousStoryName;
    List<Folder> fixedFolders;//고정 스토리 목록
    int folderIDForDB;//스토리(폴더) 아이디를 결정
    int YEAR;
    int MONTH;
    int DATE;
    private void pictureClassification() throws Exception {//시간간격을 바탕으로 사진들을 분류하는 함수
        String TAG = "classification";
        Log.d(TAG, "사진 정리 시작");

        /** ---------------------------변수--------------------------- **/
        //----> 스토리는 'end_Y_M_D~start_Y_M_D의 스토리'형태로 DB에 들어간다
        String start_Y_M_D = "";
        String end_Y_M_D = "!";
        folderIDForDB = 0;//Folder DB에 들어가는 아이디
        long _pictureTakenTime = 0;//현재 읽고 있는 사진 이전의 찍힌 시간, 초기값은 0
        String titleImagePath = "";//폴더에 들어가는 대표이미지의 경로, 일단 폴더에 들어가는 첫번째 사진으로 한다.
        int titleImageID = 0;//폴더에 들어가는 대표 사진의 ID, 일단 폴더에 들어가는 첫번째 사진으로 한다.
        String titleImageThumbnailPath = "";//폴더의 대표 사진의 썸네일 경로
        String thumbnail_path;//사진의 썸네일 경로
        int pictureID;//사진의 고유 ID(안드로이드가 지정한 ID)
        int pictureNumInStory = 0;//특정 스토리에 들어가는 사진의 개수를 센다
        previousStoryName = "";//중복 날짜 스토리를 처리하기 위한 변수
        overlappedNum = 1;//해당 스토리가 몇번째 중복 스토리인지(default = 1)
        mCoder = new Geocoder(this);
        mCr = this.getContentResolver();
        mCursor = mCr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Images.ImageColumns.DATE_TAKEN + " ASC");



        /** ---------------------------시간 간격 계산--------------------------- **/
        calculatePictureInterval();//사진의 시간간격의 총합을 구한다.
        long averageInterval = totalInterval;
        if (totalPictureNum != 0)
            averageInterval /= totalPictureNum;
        Log.d(TAG,"totalInterval : "+totalInterval+" totalPictureNum : "+totalPictureNum);
        CONSTANT.TIMEINTERVAL = averageInterval;
        Manager manager = new Manager(totalPictureNum, averageInterval, standardDerivation);
        db.createManager(manager);//Manager DB에 값들을 집어넣음



        /** ---------------------------DB 초기화--------------------------- **/
        db.deleteAllFolder();//DB에 있는 폴더 데이터를 초기화 한다(isFixed == 1<- 고정 스토리)은 제외되어 있음



        /** ------------------고정 스토리는 미리 처리해둔다(전처리)------------------ **/
        checkFixedStory();
        fixedFolders = db.getFixedFolder();//고정 스토리 목록을 다시 불러온다(없어진 고정스토리가 있을 수도 있음)
        Log.d(TAG,"fixedFolder의 개수 : "+fixedFolders.size());


        /** ------------------본격적인 사진 정리 시작------------------ **/
        mCursor.moveToLast();//마지막 사진부터 정리 == 현재에서 가장 가까운 스토리부터
        do {
            /** ------------------사진 정보를 획득하는 부분------------------ **/
            pictureID = mCursor.getInt(mCursor.getColumnIndex(MediaStore.MediaColumns._ID));//사진 고유(안드로이드 상)의 ID
            thumbnail_path = CONSTANT.getThumbnailPath(mCr, pictureID);//안드로이드 상의 썸네일 경로
            final String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));//사진이 존재하는 경로
            long pictureTakenTime = mCursor.getLong(mCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN));//사진이 촬영된 날짜
            String Y_M_D = getYMD(pictureTakenTime);//millisecond -> Y_M_D
            Log.d(TAG," [Y_M_D] : "+Y_M_D);
            Log.d(TAG, "[pictureID] : " + String.valueOf(pictureID) + " [pictureTakenTime] : " + Long.toString(pictureTakenTime) + " [path] : " + path);
            Media ExistedMedia = db.getMediaByPath(path);//path에 해당하는 사진이 이미 DB에 등록되어 있는지 확인한다
            if(ExistedMedia != null) {
                ExistedMedia.setId(pictureID);//phoketDB에 path에 해당하는 사진의 pictureID 값이 달라졌는지 확인한다(안드로이드 MediaDB상의 ID와 phoketDB상의 ID가 달라지는 경우가 발생-미디어 스캐닝이 새로 이루어 졌을 경우)
                db.updateMedia(ExistedMedia);
            }



            /** ------------------정리 제외 대상------------------ **/
            if (path.contains("thumbnail") || path.contains("Screenshot") || path.contains("screenshot"))
                continue;
            if ((ExistedMedia != null && ExistedMedia.getIsFixed() == 1)) {
                Log.d(TAG,"고정 스토리에 속한 사진은 건너뛴다");
                continue;//고정 스토리에 속하는 사진은 건너뛴다
            }
            //해당 경로에 존재하지 않는 사진은 건너띈다
            if (!isExisted(path)) {
                Log.d(TAG,"해당 경로에 존재하지 않는 사진은 건너띈다");
                if (ExistedMedia != null)//ExistedMedia에 있는 사진인데 경로에 없다면 DB에서 지우고 continue
                    db.deleteMedia(pictureID);
                continue;
            }



            /** ---------------------------스토리 구분--------------------------- **/
            //이전에 읽었던 사진과 시간 차이가 CONSTANT.TIMEINTERVAL보다 크면 새로 폴더를 만든다.
            Log.d(TAG, "두 사진의 시간 차이 = " + Math.abs(_pictureTakenTime - pictureTakenTime) + " CONSTANT.TIMEINTERVAL " + CONSTANT.TIMEINTERVAL);
            if ((Math.abs(_pictureTakenTime - pictureTakenTime) > CONSTANT.TIMEINTERVAL)) {
                //이전에 만들어진 폴더의 이름을 바꾼다
                Log.d(TAG, "start_Y_M_D  " + start_Y_M_D + " end_Y_M_D : " + end_Y_M_D);
                String new_name = changeFolderName(start_Y_M_D, end_Y_M_D, pictureNumInStory);//start_Y_M_D == ""일 경우 new_name은 null이 된다.
                if (new_name != null) {
                    Folder f = new Folder(folderIDForDB, new_name, titleImagePath, titleImageThumbnailPath, pictureNumInStory, titleImageID, 0);

                    //해당 스토리를 보내기 전에 '고정 스토리' 목록중 보낼 것이 있는지 체크한다
                    isThereFixedStoryToSend(titleImageID);

                    //메인 액티비티에게 하나의 스토리가 정리되었음을 알린다
                    mBroadcaster.broadcastIntentWithState(CONSTANT.END_OF_SINGLE_STORY, db.createFolder(f));
                    Log.d(TAG, "END_OF_SINGLE_STORY & Folder DB 입력 완료");
                }

                pictureNumInStory = 0;//데이터 초기화
                start_Y_M_D = Y_M_D;//방금 읽은 사진의 folderID가 다음 스토리의 마지막 날짜가 된다.
                titleImagePath = path;//폴더에 들어갈 첫번째 사진의 경로
                titleImageThumbnailPath = thumbnail_path;
                titleImageID = pictureID;

                decideNextFolderID();//다음 스토리의 ID(folderIDForDB)를 결정하는 부분.(고정 스토리와 중복되지 않도록 한다)
            }



            /** ---------------------------사진에 대한 위치정보 조회--------------------------- **/
            //사진에 위치 정보가 있으면 얻어온다
            String placeName_ = "";
            double longitude = 0.0;
            double latitude = 0.0;

            if (ExistedMedia != null) {//해당 사진이 기존에 있었을 경우
                Log.d(EXTRA_TAG, "기존에 존재하는 사진에 대해서 위치 조회 안함");
                placeName_ = ExistedMedia.getPlaceName();
            } else {//새로운 사진

            }


            /** ---------------------------DB에 사진 데이터를 넣는다--------------------------- **/
            if (ExistedMedia == null) {//새로운 사진
                String[] pathArr = path.split("/");

                Media m = new Media(pictureID, folderIDForDB, pathArr[pathArr.length - 1], pictureTakenTime, YEAR, MONTH, DATE, latitude, longitude, placeName_, path, thumbnail_path, 0);
                db.createMedia(m);
                String folderNameForPicture = pathArr[pathArr.length - 2];//사진이 속하는 폴더 이름
                if (!folderNameForPicture.contains("스토리"))//사진이 속하는 폴더 이름에 '스토리'가 없을 때에만
                    db.createTag(folderNameForPicture, pictureID);//사진이 속했던 폴더 이름으로 태그 만들기(디폴트 태그)
                Log.d(TAG, "미디어 id " + pictureID + " 에 대해 createMedia() 호출 (folderIDForDB : " + folderIDForDB + ")");
            } else {//기존 사진은 업데이트만 한다
                ExistedMedia.setFolder_id(folderIDForDB);
                ExistedMedia.setPath(path);
                ExistedMedia.setThumbnail_path(thumbnail_path);//처음에 정리할때는 내장 썸네일이 없었다가 나중에 생겼을 수도 있음
                db.updateMedia(ExistedMedia);
                Log.d(TAG, "미디어 id " + pictureID + " 에 대해 updateMedia() 호출 (folderIDForDB : " + folderIDForDB + ")");
            }

            pictureNumInStory++;//경로에 사진이 존재할 경우에만 카운트를 증가시킨다(경로에 없는 사진은 이부분까지 도달하지 못함)
            _pictureTakenTime = pictureTakenTime;
            end_Y_M_D = Y_M_D;
            Log.d("classification", "pictureNumInStory : " + pictureNumInStory);
            Log.d("classification", "------------------------------------------------------------");

        } while (mCursor.moveToPrevious());



        /** ---------------------------마지막 남은 폴더를 처리--------------------------- **/
        //이전에 만들어진 폴더의 이름을 바꾼다(start_Y_M_D ~ end_Y_M_D)
        String new_name = changeFolderName(start_Y_M_D, end_Y_M_D, pictureNumInStory);//start_Y_M_D == ""일 경우 new_name은 null이 된다.
        Log.d(TAG,"마지막 남은 폴더를 처리 : "+new_name);
        if (new_name != null) {
            Folder f = new Folder(folderIDForDB, new_name, titleImagePath, titleImageThumbnailPath, pictureNumInStory, titleImageID, 0);

            //해당 스토리를 보내기 전에 '고정 스토리' 목록중 보낼 것이 있는지 체크한다
            isThereFixedStoryToSend(titleImageID);

            //메인 액티비티에게 하나의 스토리가 정리되었음을 알린다
            mBroadcaster.broadcastIntentWithState(CONSTANT.END_OF_SINGLE_STORY, db.createFolder(f));
            Log.d(TAG, "END_OF_SINGLE_STORY & Folder DB 입력 완료");
        }


        //아직 남아있는 '고정 스토리'들을 모두 보낸다
        Log.d(TAG,"아직 남아있는 '고정 스토리'들을 모두 보낸다");
        for (int i = 0; i < fixedFolders.size();i++) {
            mBroadcaster.broadcastIntentWithState(CONSTANT.END_OF_SINGLE_STORY, fixedFolders.get(i).getId());
        }


        /** ---------------------------정리 끝--------------------------- **/
        mCursor.close();
    }


    /**
     * ----------pictureClassification()에서 사용하는 함수들---------- *
     */
    //millisecond -> 년월일
    private String getYMD(long pictureTakenTime) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(pictureTakenTime);
        YEAR = cal.get(Calendar.YEAR);
        MONTH = (cal.get(Calendar.MONTH) + 1);
        DATE = cal.get(Calendar.DATE);
        return ("" + YEAR + "_" + MONTH + "_" + DATE);
    }


    //Y_M_D~Y_M_D의 스토리, Y_M_D의 스토리, Y_M_D의 스토리 - 2(중복스토리)
    private String changeFolderName(String start_Y_M_D, String end_Y_M_D, int pictureNumInStory) {
        String new_name = null;
        if (!start_Y_M_D.equals("")) {
            if (!start_Y_M_D.equals(end_Y_M_D))
                new_name = end_Y_M_D + "~" + start_Y_M_D + "의 스토리";
            else
                new_name = start_Y_M_D + "의 스토리";

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
        }
        return new_name;
    }

    //다음 스토리(폴더)의 ID를 결정하는 함수
    private void decideNextFolderID() {
        int j;
        while (true) {
            folderIDForDB++;
            for (j = 0; j < fixedFolders.size(); j++) {
                if (folderIDForDB == fixedFolders.get(j).getId())
                    break;
            }
            if (j == fixedFolders.size())
                break;
        }
    }

    //사진이 해당 경로에 존재하는지 체크한다
    private boolean isExisted(String path) {
        File file = new File(path);
        if (!file.exists())
            return false;//사진이 없습니다
        else
            return true;//사진이 존재합니다
    }

    //고정 스토리에 대한 전처리. 사진에 변동이 있는지 등을 확인
    public void checkFixedStory() throws Exception {
        String TAG = "handleFixedStory";
        List<Folder> fixedFolders = db.getFixedFolder();//고정 스토리 목록
        for (int i = 0; i < fixedFolders.size(); i++) {
            Folder fixed = fixedFolders.get(i);
            Log.d(TAG, "고정 스토리 목록 - ID :" + fixed.getId() + " / NAME : " + fixed.getName());

            List<Media> mediaList = db.getAllMediaByFolder(fixed.getId());
            for (int j = 0; j < mediaList.size(); j++) {//고정 스토리에 속한 사진들을 순회한다
                Media media = mediaList.get(j);
                Log.d(TAG,"고정 스토리의 사진 ID : "+media.getId());




                //사진이 삭제되거나 경로가 달라졌으면
                if (!isExisted(media.getPath())) {
                    db.deleteMedia(media.getId());//DB에서 지워버리고
                    continue;//건너뛴다
                }

                //썸네일 경로가 업데이트되었는지 확인한다
                String thumbnail_path = CONSTANT.getThumbnailPath(mCr, media.getId());
                media.setThumbnail_path(thumbnail_path);
                db.updateMedia(media);//업데이트한다
            }

            //대표 사진 재선정 및 스토리 삭제 여부 판별
            mediaList = db.getAllMediaByFolder(fixed.getId());//다시 한번 DB에서 사진들을 가져온다(없어진 사진들이 있으므로)

            if (mediaList.size() == 0) //고정 스토리에 사진이 하나도 안남았는지 확인한다(경로 변경으로 인하여), 없으면 고정 스토리 삭제
                db.deleteFolder(fixed.getId(), true);//해당 스토리를 DB에서 지워버린다
            else {

                //첫번째 사진을 대표로 선정
                fixed.setTitleImageID(mediaList.get(0).getId());
                fixed.setThumbNail_path(mediaList.get(0).getThumbnail_path());
                fixed.setImage(mediaList.get(0).getPath());
                //스토리의 사진 개수 수정
                fixed.setPicture_num(mediaList.size());
                //DB에 업데이트
                db.updateFolder(fixed);
            }
        }
    }

    //MainActivity로 보내야할 고정 스토리가 있는지 확인한다
    public void isThereFixedStoryToSend(int titleImageID) {
        Media normalStoryTitle = db.getMediaById(titleImageID);
        long pictureTakenTime = normalStoryTitle.getPictureTaken();

        Media temp;

        Iterator<Folder> i = fixedFolders.iterator();
        while (i.hasNext()) {
            Folder folder = i.next();
            //some condition
            int mediaID = folder.getTitleImageID();
            temp = db.getMediaById(mediaID);

            if (temp != null && temp.getPictureTaken() >= pictureTakenTime) {//'고정 스토리'가 일반 스토리보다 시간상 먼저일 경우
                mBroadcaster.broadcastIntentWithState(CONSTANT.END_OF_SINGLE_STORY, folder.getId());
                i.remove();
            }
        }
    }


    /**
     * ----------가이드를 위한 함수들---------- *
     */
    private void pictureClassification_guide() throws Exception {//시간간격을 바탕으로 사진들을 분류하는 함수
        String TAG = "classification";
        //isClassifying = 1;
        //MainActivity에게 사진 정리를 시작했다는 메세지를 보낸다.
        //sendMessageToUI(CONSTANT.RECEIPT_OF_PICTURE_CLASSIFICATION,isClassifying);

        Log.d("guide", "가이드 시작");
        db.deleteAllFolder();//가이드 도중에 앱을 종료하고 다시 시작할 경우를 대비
        db.deleteAllMedia();//가이드 도중에 앱을 종료하고 다시 시작할 경우를 대비
        long currentTime = System.currentTimeMillis();//현재 시간
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(currentTime);
        String folderName = "" + cal.get(Calendar.YEAR) + "_" + (cal.get(Calendar.MONTH) + 1) + "_" + cal.get(Calendar.DATE) + "의 스토리";
        Folder f = new Folder(1, folderName, "", null, 8, 0, 0);
        Media m1 = new Media(1, 1, "phoket1", currentTime, cal.get(Calendar.YEAR), (cal.get(Calendar.MONTH) + 1), cal.get(Calendar.DATE), 0, 0, null, null, null, 0);
        Media m2 = new Media(2, 1, "phoket2", currentTime, cal.get(Calendar.YEAR), (cal.get(Calendar.MONTH) + 1), cal.get(Calendar.DATE), 0, 0, null, null, null, 0);
        Media m3 = new Media(3, 1, "phoket3", currentTime, cal.get(Calendar.YEAR), (cal.get(Calendar.MONTH) + 1), cal.get(Calendar.DATE), 0, 0, null, null, null, 0);
        Media m4 = new Media(4, 1, "phoket4", currentTime, cal.get(Calendar.YEAR), (cal.get(Calendar.MONTH) + 1), cal.get(Calendar.DATE), 0, 0, null, null, null, 0);
        Media m5 = new Media(5, 1, "phoket5", currentTime, cal.get(Calendar.YEAR), (cal.get(Calendar.MONTH) + 1), cal.get(Calendar.DATE), 0, 0, null, null, null, 0);
        Media m6 = new Media(6, 1, "phoket6", currentTime, cal.get(Calendar.YEAR), (cal.get(Calendar.MONTH) + 1), cal.get(Calendar.DATE), 0, 0, null, null, null, 0);
        Media m7 = new Media(7, 1, "phoket7", currentTime, cal.get(Calendar.YEAR), (cal.get(Calendar.MONTH) + 1), cal.get(Calendar.DATE), 0, 0, null, null, null, 0);
        Media m8 = new Media(8, 1, "phoket8", currentTime, cal.get(Calendar.YEAR), (cal.get(Calendar.MONTH) + 1), cal.get(Calendar.DATE), 0, 0, null, null, null, 0);
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

    /**
     * ----------서비스가 죽었을 때 다시 살리기 위한 함수들---------- *
     */
    public void registerRestartService() {
        Log.d(EXTRA_TAG, "registerRestartService() 호출");
        Intent intent = new Intent(ServiceOfPictureClassification.this, BroadcastListener.class);
        intent.setAction(BroadcastListener.ACTION_RESTART_PERSISTENTSERVICE);
        intent.putExtra("countForTick", BroadcastListener.getCountForTick());
        intent.putExtra("HOWOFTENCHECK", BroadcastListener.getHOWOFTENCHECK());
        Log.d(EXTRA_TAG, "countForTick " + BroadcastListener.getCountForTick());
        Log.d(EXTRA_TAG, "HOWOFTENCHECK " + BroadcastListener.getHOWOFTENCHECK());
        PendingIntent sender = PendingIntent.getBroadcast(
                ServiceOfPictureClassification.this, 0, intent, 0);
        long currentTime = SystemClock.elapsedRealtime();//현재 시간
        currentTime += 1 * 1000;
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME, currentTime, 60 * 1000, sender);
    }

    //서비스가 죽었을 때 다시 살리기 위한 함수
    public void unregisterRestartService() {
        Log.d(EXTRA_TAG, "unregisterRestartService() 호출");
        Intent intent = new Intent(ServiceOfPictureClassification.this, BroadcastListener.class);
        intent.setAction(BroadcastListener.ACTION_RESTART_PERSISTENTSERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(
                ServiceOfPictureClassification.this, 0, intent, 0);
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.cancel(sender);
    }

}
