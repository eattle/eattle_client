package com.example.choi.eattle_prototype;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class NearSpotService extends Service implements Runnable{
    //관광지 푸시를 위한 변수들
    private LocationManager mLocationManager;//시스템 서비스
    private SpotIntentReceiver mIntentReceiver;
    ArrayList mPendingIntentList;

    String intentKey = "spotProximity";
    //데이터베이스 관련 변수
    private DatabaseHelper dbHelper;
    public static SQLiteDatabase db;
    public NearSpotService() {
    }

    public void onCreate(){
        super.onCreate();
        Log.d("NearSpotService", "위치 관련 서비스 시작");
        //DB에서 관광지를 불러온다.
        //데이터베이스 OPEN
        dbHelper = new DatabaseHelper(this);
        Log.d("NearSpotService", "DEBUG!!!!!!!!!!!!!!!!!!!");
        db = dbHelper.getWritableDatabase();
        dbHelper.onCreate(db);//onCreate함수를 강제로 호출해준다(안그러면 폰에서 안됨)

        String SQL = "SELECT name,picName,latitude,longitutde,spotInfoID FROM spot";
        Cursor c = db.rawQuery(SQL, null);
        GLOBAL.recordCount = c.getCount();
        GLOBAL.spot  = new TouristSpotInfo[GLOBAL.recordCount];
        for (int i = 0; i < GLOBAL.recordCount; i++) {
            c.moveToNext();
            String name = c.getString(0);
            String _picName = c.getString(1);
            //R.drawable을 동적으로 가져온다.
            int picName = getResources().getIdentifier(_picName, "drawable", CONSTANT.PACKAGE_NAME);
            double latitude = c.getDouble(2);
            double longitude = c.getDouble(3);
            String spotInfoID = c.getString(4);
            GLOBAL.spot[i] = new TouristSpotInfo(name, picName, latitude, longitude, spotInfoID);
        }
        //-------------특정 관광지에 근접했는지 체크한다-------------
        // 위치 관리자 객체 참조
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mPendingIntentList = new ArrayList();

        // 수신자 객체 생성하여 등록
        //SpotIntentReceiver는 브로드캐스트 수신자를 상속함.
        mIntentReceiver = new SpotIntentReceiver(intentKey);
        //브로드캐스트 수신자는 처음부터 메니페스트 파일에 등록될 수도 있지만
        //자바 코드에서 registerReceiver를 통해 등록될 수도 있다.
        registerReceiver(mIntentReceiver, mIntentReceiver.getFilter());
        Toast.makeText(getApplicationContext(), GLOBAL.recordCount + "개 관광지에 대한 근접 리스너 등록", Toast.LENGTH_LONG).show();

        //쓰레드를 생성하여 위치 관련 서비스 시작
        Thread nearSpotService = new Thread(this);
        nearSpotService.start();
    }
    /**
     * 현재 위치 확인을 위해 정의한 메소드
     */
    private void startLocationService() {
        Log.d("NearSpotService","startLocationService 호출");
        // 위치 관리자 객체 참조
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // 리스너 객체 생성
        GPSListener gpsListener = new GPSListener();
        long minTime = 10000;//몇 ms 마다 위치를 확인할 것인지
        float minDistance = 0;

        // GPS 기반 위치 요청
        manager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                minTime,
                minDistance,
                gpsListener,
                Looper.getMainLooper());

        // 네트워크 기반 위치 요청
        manager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                minTime,
                minDistance,
                gpsListener,
                Looper.getMainLooper());

        for(int i=0;i<GLOBAL.recordCount;i++){
            //근접 관광지 체크를 위해 등록
            register(1001+i,GLOBAL.spot[i].getName(),GLOBAL.spot[i].getLatitude(),GLOBAL.spot[i].getLongitutde(),200,-1);
        }
//        Toast.makeText(getApplicationContext(), "위치 확인 시작", Toast.LENGTH_SHORT).show();
    }

    public void run(){
//        Looper.prepare();
        startLocationService();
    }
    /**
     * 리스너 정의
     */
    private class GPSListener implements LocationListener {
        /**
         * 위치 정보가 확인되었을 때 호출되는 메소드
         */
        public void onLocationChanged(Location location) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            TourMainActivity.setHavelatlonInfo(true); // 위치 정보를 받았다는 표시
            TourMainActivity.setLastLatitude(latitude); // 최신으로 받은 위도
            TourMainActivity.setLastLongitutde(longitude); // 최신으로 받은 경도

            String msg = "위도 : "+ latitude + "/ 경도:"+ longitude;
            Log.i("GPSLocationService", msg);

            /*
            //구글 지도에 그리기 위한 위도, 경도를 추가한다.
            TourMapActivity.rectOptions.add(new LatLng(latitude, longitude));
            TourMapActivity.rectOptions.color(Color.BLUE);
            */

            // 현재 위치의 지도를 보여주기 위해 정의한 메소드 호출
            //최상위 액티비티가 TourMapActivity(지도) 일때만 지도에 그려준다.

            ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> Info = am.getRunningTasks(1);
            ComponentName topActivity = Info.get(0).topActivity;
            String topactivityname = topActivity.getClassName();
            Log.d("GPS", topactivityname);
            if(topactivityname.equals("com.example.choi.eattle_prototype.TourMapActivity")) {
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
    private void register(int id, String name,double latitude, double longitude, float radius, long expiration) {
        Log.d("GPS", "근접 관광지 푸시 관련, register 함수 호출");
        Intent proximityIntent = new Intent(intentKey);
        proximityIntent.putExtra("id", id);
        proximityIntent.putExtra("name",name);
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
                String name = intent.getStringExtra("name");
                double latitude = intent.getDoubleExtra("latitude", 0.0D);
                double longitude = intent.getDoubleExtra("longitude", 0.0D);

                Toast.makeText(context, "근접한 관광지 : " + id + ", 이름 : " +name + " " + latitude + ", " + longitude, Toast.LENGTH_LONG).show();
                //notification
                NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, TourMapActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationCompat.Builder mCompatBuilder = new NotificationCompat.Builder(context);
                mCompatBuilder.setSmallIcon(R.drawable.ic_launcher);
                mCompatBuilder.setTicker("주변에 관광 명소가 있어요!");
                mCompatBuilder.setWhen(System.currentTimeMillis());
                mCompatBuilder.setNumber(1);
                mCompatBuilder.setContentTitle("Eattle");
                mCompatBuilder.setContentText(name+"가 가까이 있어요!\n확인하러 갈까요?");
                mCompatBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                mCompatBuilder.setContentIntent(pendingIntent);
                mCompatBuilder.setAutoCancel(true);

                nm.notify(222, mCompatBuilder.build());

                //일단 해당 관광지를 방문한 것으로 판단하고 표시한다.
                for(int i=0;i<GLOBAL.recordCount;i++){
                    if(name.equals(GLOBAL.spot[i].getName())){
                        GLOBAL.spot[i].setVisit(1);
                        break;
                    }
                }
                //현재 최상위 액티비티가 지도이면 바로 표시한다.
                ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningTaskInfo> Info = am.getRunningTasks(1);
                ComponentName topActivity = Info.get(0).topActivity;
                String topactivityname = topActivity.getClassName();
                Log.d("GPS", topactivityname);
                if(topactivityname.equals("com.example.choi.eattle_prototype.TourMapActivity")) {
                    Log.d("GPSLocationService", "최상위 액티비티 : TourMapActivity");
                    //관광지를 방문했을 경우 업데이트 해야 하므로 다시 호출
                    for(int i=0;i<GLOBAL.recordCount;i++){
                        // 특정 위치에 관광지를 표시하기 위해 정의한 메소드(여기에 관광지들 등록하면 됨)
                        TourMapActivity.showSpotPosition(GLOBAL.spot[i].getLatitude(),GLOBAL.spot[i].getLongitutde(),GLOBAL.spot[i].getName(),GLOBAL.spot[i].getName(),GLOBAL.spot[i].getVisit());
                    }
                }
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
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
