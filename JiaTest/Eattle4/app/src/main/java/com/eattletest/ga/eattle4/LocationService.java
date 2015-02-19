package com.eattletest.ga.eattle4;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.*;
import android.widget.Toast;


public class LocationService extends Service{
    LocationThread mthread;
    DBHelper mHelper;
    public  void onCreate(){
        super.onCreate();
    }

    public void onDestroy(){
        super.onDestroy();

        mthread.stopLocation();
        Toast.makeText(this, "Service End", Toast.LENGTH_SHORT).show();
    }

    public int onStartCommand(Intent intent, int flags, int startId){
        super.onStartCommand(intent, flags, startId);

        mHelper = new DBHelper(this);
        mthread = new LocationThread(this, mHandler);
        mthread.start();
        return START_STICKY;
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class LocationThread extends Thread{
        LocationManager mLocMan;
        LocationService mParent;
        Handler mHandler;
        String mProvider;

        public LocationThread(LocationService parent, Handler handler){
            mParent = parent;
            mHandler = handler;
            mLocMan = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

            Criteria crit = new Criteria();
            crit.setAccuracy(Criteria.NO_REQUIREMENT);
            crit.setPowerRequirement(Criteria.NO_REQUIREMENT);
            crit.setAltitudeRequired(false);
            crit.setCostAllowed(false);
            mProvider = mLocMan.getBestProvider(crit, true);

            Message msg = new Message();
            msg.what = 1;
            msg.obj = "공급자 : " + mProvider;
            mHandler.sendMessage(msg);
        }

        public void run(){
            mLocMan.requestLocationUpdates(mProvider, 0, 0, mListener, Looper.getMainLooper());
            //startLocationService()가 들어가면 될듯
        }

        public void stopLocation(){
            mLocMan.removeUpdates(mListener);
        }

        LocationListener mListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                SQLiteDatabase db;
                long now = System.currentTimeMillis();
                db = mHelper.getWritableDatabase();
                db.execSQL("INSERT INTO location VALUES ("+ now + ", '"+ location.getLatitude()+ "', '" + location.getLongitude()  + " ');");
                mHelper.close();

                db = mHelper.getReadableDatabase();
                Cursor cursor;
                cursor = db.rawQuery("SELECT * FROM location where time = " + now, null);

                cursor.moveToFirst();
                String sloc = String.format("위도 : %s\n경도 : %s\n시 : %s", cursor.getString(1), cursor.getString(2), cursor.getString(0));

                Message msg = new Message();
                msg.what = 0;
                msg.obj = sloc;
                mHandler.sendMessage(msg);

                cursor.close();
                mHelper.close();

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                String sStatus = "";
                switch (status){
                    case LocationProvider.OUT_OF_SERVICE:
                        sStatus = "범위 벗어남";
                        break;
                    case LocationProvider.TEMPORARILY_UNAVAILABLE:
                        sStatus = "일시적 불능";
                        break;
                    case LocationProvider.AVAILABLE:
                        sStatus = "사용 가능";
                        break;
                }
                Message msg = new Message();
                msg.what = 2;
                msg.obj = provider + "상태 변경" + sStatus;
                mHandler.sendMessage(msg);
            }

            @Override
            public void onProviderEnabled(String provider) {
                Message msg = new Message();
                msg.what = 2;
                msg.obj = "현재 상태 : 서비스 사용 가능";
                mHandler.sendMessage(msg);
            }

            @Override
            public void onProviderDisabled(String provider) {
                Message msg = new Message();
                msg.what = 2;
                msg.obj = "현재 상태 : 서비스 사용 불가";
                mHandler.sendMessage(msg);
            }
        };

    }

    Handler mHandler = new Handler() {
        public  void handleMessage(Message msg){
            if(msg.what == 0){
                String location = (String)msg.obj;
                Toast.makeText(LocationService.this, location, Toast.LENGTH_SHORT).show();
            }
            else if(msg.what == 1){
                String provider = (String)msg.obj;
                Toast.makeText(LocationService.this, provider, Toast.LENGTH_SHORT).show();


            }
            else if(msg.what == 2){
                String status = (String)msg.obj;
                Toast.makeText(LocationService.this, status, Toast.LENGTH_SHORT).show();

            }
        }
    };
}

class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context){
        super(context, "Location.db", null, 1);
    }

    public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE location ( time LONG PRIMARY KEY, " + "latitude FLOAT, longitude FLOAT);");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS location");
        onCreate(db);
    }
}


public class NearSpotService extends Service {
    //관광지 푸시를 위한 변수들
    private LocationManager mLocationManager;//시스템 서비스
    private SpotIntentReceiver mIntentReceiver;

    ArrayList mPendingIntentList;

    String intentKey = "spotProximity";

    public NearSpotService() {
    }

    public void onCreate(){
        super.onCreate();
        Log.d("NearSpotService", "위치 관련 서비스 시작");
        //-------------특정 관광지에 근접했는지 체크한다-------------
        // 위치 관리자 객체 참조
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mPendingIntentList = new ArrayList();
        //7개의 관광지를 등록
        int countTargets = 7;
        register(1001, 37.4871636, 126.979744, 2000, -1);//우리집
        register(1002, 37.5043299, 127.0447994, 2000, -1);//소프트웨어 마에스트로
        register(1003, 37.5042846, 127.0414756, 2000, -1);//성산일출봉
        register(1004, 37.502757, 127.043621, 2000, -1);//제주월드컵경기장
        register(1005, 37.5009083, 127.045714, 2000, -1);//백록담
        register(1006, 37.5032836, 127.0446134, 2000, -1);//돌하르방공원
        register(1007, 37.5005613, 127.035285, 2000, -1);//한라수목원


        // 수신자 객체 생성하여 등록
        //SpotIntentReceiver는 브로드캐스트 수신자를 상속함.
        mIntentReceiver = new SpotIntentReceiver(intentKey);
        //브로드캐스트 수신자는 처음부터 메니페스트 파일에 등록될 수도 있지만
        //자바 코드에서 registerReceiver를 통해 등록될 수도 있다.
        registerReceiver(mIntentReceiver, mIntentReceiver.getFilter());
        Toast.makeText(getApplicationContext(), countTargets + "개 관광지에 대한 근접 리스너 등록", Toast.LENGTH_LONG).show();

        startLocationService();
    }

    //현재 위치 확인을 위해 정의한 메소드

    private void startLocationService() {
        // 위치 관리자 객체 참조
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // 리스너 객체 생성
        GPSListener gpsListener = new GPSListener();
        long minTime = 60000;//몇 ms 마다 위치를 확인할 것인지
        float minDistance = 0;

        // GPS 기반 위치 요청
        manager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                minTime,
                minDistance,
                gpsListener);

        // 네트워크 기반 위치 요청
        manager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                minTime,
                minDistance,
                gpsListener);

        Toast.makeText(getApplicationContext(), "위치 확인 시작", Toast.LENGTH_SHORT).show();
    }

    /**
     * 리스너 정의
     */
    private class GPSListener implements LocationListener {
        /**
         * 위치 정보가 확인되었을 때 호출되는 메소드
         */
        public void onLocationChanged(Location location) {
            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();

            TourMainActivity.setHavelatlonInfo(true); // 위치 정보를 받았다는 표시
            TourMainActivity.setLastLatitude(latitude); // 최신으로 받은 위도
            TourMainActivity.setLastLongitutde(longitude); // 최신으로 받은 경도

            String msg = "위도 : "+ latitude + "/ 경도:"+ longitude;
            Log.i("GPSLocationService", msg);

            //구글 지도에 그리기 위한 위도, 경도를 추가한다.
            TourMapActivity.rectOptions.add(new LatLng(latitude, longitude));
            TourMapActivity.rectOptions.color(Color.BLUE);

            // 현재 위치의 지도를 보여주기 위해 정의한 메소드 호출
            //최상위 액티비티가 TourMapActivity(지도) 일때만 지도에 그려준다.
            ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> Info = am.getRunningTasks(1);
            ComponentName topActivity = Info.get(0).topActivity;
            String topactivityname = topActivity.getClassName();
            Log.d("GPS", topactivityname);
            if(topactivityname.equals("com.example.choi.eattle.TourMapActivity")) {
                Log.d("GPSLocationService", "최상위 액티비티 : TourMapActivity");
                TourMapActivity.showCurrentLocation(latitude, longitude);
            }
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

    }
    /**
     * 근접 관광지 푸시 관련
     */
    private void register(int id, double latitude, double longitude, float radius, long expiration) {
        Log.d("GPS", "근접 관광지 푸시 관련, register 함수 호출");
        Intent proximityIntent = new Intent(intentKey);
        proximityIntent.putExtra("id", id);
        proximityIntent.putExtra("latitude", latitude);
        proximityIntent.putExtra("longitude", longitude);
        //pendingIntent : 인텐트를 바로 전달하지 않고, 지연시켜주는 인텐트.
        PendingIntent Pintent = PendingIntent.getBroadcast(getBaseContext(), id, proximityIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        mLocationManager.addProximityAlert(latitude, longitude, radius, expiration, Pintent);

        mPendingIntentList.add(Pintent);

    }
    /**
     * 등록한 정보 해제
     */

    private void unregister() {
        if (mPendingIntentList != null) {
            for (int i = 0; i < mPendingIntentList.size(); i++) {
                PendingIntent curIntent = (PendingIntent) mPendingIntentList.get(i);
                mLocationManager.removeProximityAlert(curIntent);
                mPendingIntentList.remove(i);
            }
        }

        if (mIntentReceiver != null) {
            unregisterReceiver(mIntentReceiver);
            mIntentReceiver = null;
        }
    }

    /**
     * 브로드캐스팅 메시지를 받았을 때 처리할 수신자 정의
     */
    private class SpotIntentReceiver extends BroadcastReceiver {

        private String mExpectedAction;
        private Intent mLastReceivedIntent;

        public SpotIntentReceiver(String expectedAction) {
            mExpectedAction = expectedAction;
            mLastReceivedIntent = null;
        }

        public IntentFilter getFilter() {
            IntentFilter filter = new IntentFilter(mExpectedAction);
            return filter;
        }

        /**
         * 받았을 때 호출되는 메소드
         *
         * @param context
         * @param intent
         */
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                Log.d("GPS", "근접 관광지 푸시 관련 onReceive 함수 호출");
                mLastReceivedIntent = intent;

                int id = intent.getIntExtra("id", 0);
                double latitude = intent.getDoubleExtra("latitude", 0.0D);
                double longitude = intent.getDoubleExtra("longitude", 0.0D);

                //Toast.makeText(context, "근접한 관광지 : " + id + ", " + latitude + ", " + longitude, Toast.LENGTH_LONG).show();
                //notification
                NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, TourMapActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationCompat.Builder mCompatBuilder = new NotificationCompat.Builder(context);
                mCompatBuilder.setSmallIcon(R.drawable.ic_launcher);
                mCompatBuilder.setTicker("주변에 관광 명소가 있어요!");
                mCompatBuilder.setWhen(System.currentTimeMillis());
                mCompatBuilder.setNumber(10);
                mCompatBuilder.setContentTitle("Eattle");
                mCompatBuilder.setContentText("주변에 관광 명소가 있어요!\n확인하러 갈까요?");
                mCompatBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                mCompatBuilder.setContentIntent(pendingIntent);
                mCompatBuilder.setAutoCancel(true);

                nm.notify(222, mCompatBuilder.build());
            }
        }

        public Intent getLastReceivedIntent() {
            return mLastReceivedIntent;
        }

        public void clearReceivedIntents() {
            mLastReceivedIntent = null;
        }
    }
    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }
}
