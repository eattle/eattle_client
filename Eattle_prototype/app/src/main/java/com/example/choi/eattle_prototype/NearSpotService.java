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
import java.util.Arrays;
import java.util.List;

public class NearSpotService extends Service implements Runnable {
    /*
    //관광지 푸시를 위한 변수들
    private LocationManager mLocationManager;//시스템 서비스
    private SpotIntentReceiver mIntentReceiver;
    ArrayList mPendingIntentList;*/
    private static double lastLatitude;//가장 마지막으로 받은 위도
    private static double lastLongitude;//가장 마지막으로 받은 경도
    private static boolean havelatlonInfo = false; //메인 액티비티를 띄울 때 위도,경도 정보가 있으면 true, 없으면 false

    String intentKey = "spotProximity";
    //데이터베이스 관련 변수
    private DatabaseHelper dbHelper;
    public static SQLiteDatabase db;

    //관광지 방문 여부를 판단하기 위한 변수
    private int count = 0;
    private String currentVisitSpot = "";//현재 방문하고 있는 관광지의 이
    private String candidateVisitSpot = "";//방문하고 있을 것으로 판단되는 후보 관광지의 이름


    public NearSpotService() {
    }

    public void onCreate() {
        super.onCreate();
        Log.d("NearSpotService", "위치 관련 서비스 시작");
        //DB에서 관광지를 불러온다.
        //데이터베이스 OPEN
        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getWritableDatabase();
        dbHelper.onCreate(db);//onCreate함수를 강제로 호출해준다(안그러면 폰에서 안됨)

        String SQL = "SELECT _id,name,explanation,picName,latitude,longitude,radius,spotInfoID FROM spot";

        Cursor c = db.rawQuery(SQL, null);
        GLOBAL.recordCount = c.getCount();
        GLOBAL.spot = new TouristSpotInfo[GLOBAL.recordCount];
        for (int i = 0; i < GLOBAL.recordCount; i++) {
            c.moveToNext();
            int _id = c.getInt(0);
            String name = c.getString(1);
            String explanation = c.getString(2);
            String _picName = c.getString(3);
            //R.drawable을 동적으로 가져온다.
            int picName = getResources().getIdentifier(_picName, "drawable", CONSTANT.PACKAGE_NAME);
            double latitude = c.getDouble(4);
            double longitude = c.getDouble(5);
            double radius = c.getDouble(6);
            String spotInfoID = c.getString(7);
            GLOBAL.spot[i] = new TouristSpotInfo(_id, name, explanation, picName, latitude, longitude, radius, spotInfoID);
        }

        //쓰레드를 생성하여 위치 관련 서비스 시작
        Thread nearSpotService = new Thread(this);
        nearSpotService.start();
    }

    /**
     * 현재 위치 확인을 위해 정의한 메소드
     */
    private void startLocationService() {
        Log.d("NearSpotService", "startLocationService 호출");
        // 위치 관리자 객체 참조
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // 리스너 객체 생성
        GPSListener gpsListener = new GPSListener();
        long minTime = 20000;//몇 ms 마다 위치를 확인할 것인지
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

        for (int i = 0; i < GLOBAL.recordCount; i++) {
            //근접 관광지 체크를 위해 등록
            register(1001 + i, GLOBAL.spot[i].getName(), GLOBAL.spot[i].getLatitude(), GLOBAL.spot[i].getLongitude(), 200, -1);
        }
//        Toast.makeText(getApplicationContext(), "위치 확인 시작", Toast.LENGTH_SHORT).show();
    }

    public void run() {
//        Looper.prepare();
        startLocationService();
    }

    // get, set
    public boolean getHavelatlonInfo() {
        return havelatlonInfo;
    }

    public void setHavelatlonInfo(boolean havelatlonInfo) {
        this.havelatlonInfo = havelatlonInfo;
    }

    public double getLastLatitude() {
        return this.lastLatitude;
    }

    public void setLastLatitude(double lastLatitude) {
        this.lastLatitude = lastLatitude;
    }

    public double getLastLongitude() {
        return this.lastLongitude;
    }

    public void setLastLongitude(double lastLongitude) {
        this.lastLongitude = lastLongitude;
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

            setHavelatlonInfo(true); // 위치 정보를 받았다는 표시
            setLastLatitude(latitude); // 최신으로 받은 위도
            setLastLongitude(longitude); // 최신으로 받은 경도

            String msg = "위도 : " + latitude + "/ 경도:" + longitude;
            Log.i("GPSLocationService", msg);

            /*
            //구글 지도에 그리기 위한 위도, 경도를 추가한다.
            TourMapActivity.rectOptions.add(new LatLng(latitude, longitude));
            TourMapActivity.rectOptions.color(Color.BLUE);
            */

            // 현재 위치의 지도를 보여주기 위해 정의한 메소드 호출
            //최상위 액티비티가 TourMapActivity(지도) 일때만 지도에 그려준다.

            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> Info = am.getRunningTasks(1);
            ComponentName topActivity = Info.get(0).topActivity;
            String topactivityname = topActivity.getClassName();
            Log.d("GPS", topactivityname);
            if (topactivityname.equals("com.example.choi.eattle_prototype.TourMapActivity")) {
                Log.d("GPSLocationService", "최상위 액티비티 : TourMapActivity");
                TourMapActivity.showCurrentLocation(latitude, longitude);
            }

            //거리에 따라 관광지를 정렬한다
            int numOfTempFavorite = 0;
            int numOfTempNotFavorite = 0;
            //현재 위치로부터의 거리를 계산한다.
            for (int i = 0; i < GLOBAL.recordCount; i++) {
                if (GLOBAL.spot[i].getFavorite() == 0)
                    numOfTempNotFavorite++;
                else if (GLOBAL.spot[i].getFavorite() == 1)
                    numOfTempFavorite++;
                Log.d("MainActivity", "관광지의 위도, 경도" + Double.toString(GLOBAL.spot[i].getLatitude()) + " " + Double.toString(GLOBAL.spot[i].getLongitude()));
                double temp = calcDistance(lastLatitude, lastLongitude, GLOBAL.spot[i].getLatitude(), GLOBAL.spot[i].getLongitude());
                GLOBAL.spot[i].setSpotDistanceFromMe(temp);
                Log.d("MainActivity", GLOBAL.spot[i].getName() + " 가 현재 위치로 부터 떨어진 거리 : " + Double.toString(temp));
            }
            //가까운 순으로 정렬한다.
            TouristSpotInfo[] tempFavorite = new TouristSpotInfo[numOfTempFavorite];
            TouristSpotInfo[] tempNotFavorite = new TouristSpotInfo[numOfTempNotFavorite];
            numOfTempFavorite = 0;
            numOfTempNotFavorite = 0;
            for (int i = 0; i < GLOBAL.recordCount; i++) {
                if (GLOBAL.spot[i].getFavorite() == 1) {//즐겨찾기
                    tempFavorite[numOfTempFavorite++] = GLOBAL.spot[i];
                } else if (GLOBAL.spot[i].getFavorite() == 0)
                    tempNotFavorite[numOfTempNotFavorite++] = GLOBAL.spot[i];
            }
            Arrays.sort(tempFavorite);
            Arrays.sort(tempNotFavorite);
            for (int i = 0; i < GLOBAL.recordCount; i++) {
                if (i < numOfTempFavorite)
                    GLOBAL.spot[i] = tempFavorite[i];
                else
                    GLOBAL.spot[i] = tempNotFavorite[i - numOfTempFavorite];
            }

            //특정 관광지에 근접했는지 체크한다
            //가장 가까운 관광지에 대해서만 확인하면 된다.
            //관광지의 반경 + 1000에 들어왔는지 확인한다 -> 근접 관광지로 판단하여 notification
            if (GLOBAL.spot[0].getSpotDistanceFromMe() < GLOBAL.spot[0].getRadius() + 1000) {
                if (GLOBAL.spot[0].getIsNotified() == 0) {//아직 notification 을 넣지 않다면
                    //notification을 넣는다
                    NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, new Intent(getBaseContext(), TourMapActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

                    NotificationCompat.Builder mCompatBuilder = new NotificationCompat.Builder(getBaseContext());
                    mCompatBuilder.setSmallIcon(R.drawable.appicon);
                    mCompatBuilder.setTicker("주변에 관광 명소가 있어요!");
                    mCompatBuilder.setWhen(System.currentTimeMillis());
                    mCompatBuilder.setNumber(1);
                    mCompatBuilder.setContentTitle("Eattle");
                    mCompatBuilder.setContentText(GLOBAL.spot[0].getName() + " 가까이 있어요!\n확인하러 갈까요?");
                    mCompatBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                    mCompatBuilder.setContentIntent(pendingIntent);
                    mCompatBuilder.setAutoCancel(true);

                    nm.notify(222, mCompatBuilder.build());
                    GLOBAL.spot[0].setIsNotified(1);
                }
                //관광지의 반경에 들어왔는지 판단한다 -> 해당 관광지를 방문하고 있는지 판단하기 위하여 카운트 시
                if (GLOBAL.spot[0].getSpotDistanceFromMe() < GLOBAL.spot[0].getRadius()) {
                    //새로운 관광지에 들어갔다고 판단되면,
                    if (candidateVisitSpot.equals(GLOBAL.spot[0].getName()) == false) {
                        //currentVisitSpot = "";//현재 방문중인 관광지를 초기화한다.
                        candidateVisitSpot = GLOBAL.spot[0].getName();
                        count = 0;
                    }
                    count++;
                    if (count == 5) {// 같은 관광지에 동일하게 5번 카운트 되면, 방문하고 있다고 판단
                        //currentVisitSpot = candidateVisitSpot;
                        // 현재 시간을 얻는다.
                        long now = System.currentTimeMillis();
                        // 해당 관광지의 id 값을 얻어온다.
                        int _id = GLOBAL.spot[0].get_id();
                        // DB에 넣는다.
                        db.execSQL("insert into path (time,spotID) values ("+now+","+_id+");");

                        //일단 해당 관광지를 방문한 것으로 판단하고 표시한다.
                        GLOBAL.spot[0].setVisit(1);
                        //현재 최상위 액티비티가 지도이면 바로 표시한다.
                        /* // 추가해야 하는 부분이지만 좀더 최적화 하는 방법 생각해보기
                        am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                        Info = am.getRunningTasks(1);
                        topActivity = Info.get(0).topActivity;
                        topactivityname = topActivity.getClassName();*/

                        Log.d("GPS", topactivityname);
                        if (topactivityname.equals("com.example.choi.eattle_prototype.TourMapActivity")) {
                            Log.d("GPSLocationService", "최상위 액티비티 : TourMapActivity");
                            //관광지를 방문했을 경우 업데이트 해야 하므로 다시 호출
                            for (int i = 0; i < GLOBAL.recordCount; i++) {
                                // 특정 위치에 관광지를 표시하기 위해 정의한 메소드(여기에 관광지들 등록하면 됨)
                                TourMapActivity.showSpotPosition(GLOBAL.spot[i].getLatitude(), GLOBAL.spot[i].getLongitude(), GLOBAL.spot[i].getName(), GLOBAL.spot[i].getName(), GLOBAL.spot[i].getVisit());
                            }
                            //TourMapActivity.showSpotPosition(GLOBAL.spot[0].getLatitude(), GLOBAL.spot[0].getLongitude(), GLOBAL.spot[0].getName(), GLOBAL.spot[0].getName(), GLOBAL.spot[0].getVisit());
                        }

                        Log.d("NearSpotService","관광지 방문 시작");
                    }
                    Log.d("NearSpotService","현재 count : "+count);
                }
            }
            //이동중 여부를 판단한다.
            if(GLOBAL.spot[0].getSpotDistanceFromMe() > GLOBAL.spot[0].getRadius()){
                if(count != 0){//count가 있는 상태에서 해당 조건문을 만나면, '이동'을 시작했다는 의미
                    // 현재 시간을 얻는다.
                    long now = System.currentTimeMillis();
                    // 해당 관광지의 id 값을 얻어온다.
                    int _id = GLOBAL.spot[0].get_id();
                    // 이동중이라는 표시를 한다.
                    db.execSQL("insert into path (time,spotID) values ("+now+",-1);");
                    count=0;
                    Log.d("NearSpotService","이동 시작");
                }
            }

        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

    //관광지 푸시를 위해 등록하는 함수
    private void register(int id, String name, double latitude, double longitude, float radius, long expiration) {
        /*
        Log.d("GPS", "근접 관광지 푸시 관련, register 함수 호출");
        Intent proximityIntent = new Intent(intentKey);
        proximityIntent.putExtra("id", id);
        proximityIntent.putExtra("name", name);
        proximityIntent.putExtra("latitude", latitude);
        proximityIntent.putExtra("longitude", longitude);
        //pendingIntent : 인텐트를 바로 전달하지 않고, 지연시켜주는 인텐트.
        PendingIntent Pintent = PendingIntent.getBroadcast(getBaseContext(), id, proximityIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        mLocationManager.addProximityAlert(latitude, longitude, radius, expiration, Pintent);

        PendingIntentInfo pendingIntentInfo = new PendingIntentInfo(Pintent,name);
        mPendingIntentList.add(pendingIntentInfo);
        */
    }
    /*
    //근접 푸시 관련 펜딩인텐트를 관리하기 위한 클래스
    private class PendingIntentInfo{
        private PendingIntent Pintent;
        private String name;//관광지 이름

        PendingIntentInfo(){}
        PendingIntentInfo(PendingIntent Pintent,String name){
            this.Pintent = Pintent;
            this.name = name;
        }

        public PendingIntent getPintent(){
            return Pintent;
        }
        public String getName(){
            return name;
        }
    }
    */
    /*
    //근접 푸시 해제
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
    }*/


    //브로드캐스팅 메시지를 받았을 때 처리할 수신자 정의
    /*
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

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                Log.d("GPS", "근접 관광지 푸시 관련 onReceive 함수 호출");

                mLastReceivedIntent = intent;
                int id = intent.getIntExtra("id", 0);
                String name = intent.getStringExtra("name");
                double latitude = intent.getDoubleExtra("latitude", 0.0D);
                double longitude = intent.getDoubleExtra("longitude", 0.0D);

                Toast.makeText(context, "근접한 관광지 : " + id + ", 이름 : " + name + " " + latitude + ", " + longitude, Toast.LENGTH_LONG).show();
                //notification
                NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, TourMapActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationCompat.Builder mCompatBuilder = new NotificationCompat.Builder(context);
                mCompatBuilder.setSmallIcon(R.drawable.appicon);
                mCompatBuilder.setTicker("주변에 관광 명소가 있어요!");
                mCompatBuilder.setWhen(System.currentTimeMillis());
                mCompatBuilder.setNumber(1);
                mCompatBuilder.setContentTitle("Eattle");
                mCompatBuilder.setContentText(name + "가 가까이 있어요!\n확인하러 갈까요?");
                mCompatBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                mCompatBuilder.setContentIntent(pendingIntent);
                mCompatBuilder.setAutoCancel(true);

                nm.notify(222, mCompatBuilder.build());

                //일단 해당 관광지를 방문한 것으로 판단하고 표시한다.
                for (int i = 0; i < GLOBAL.recordCount; i++) {
                    if (name.equals(GLOBAL.spot[i].getName())) {
                        GLOBAL.spot[i].setVisit(1);
                        break;
                    }
                }
                //현재 최상위 액티비티가 지도이면 바로 표시한다.
                ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningTaskInfo> Info = am.getRunningTasks(1);
                ComponentName topActivity = Info.get(0).topActivity;
                String topactivityname = topActivity.getClassName();
                Log.d("GPS", topactivityname);
                if (topactivityname.equals("com.example.choi.eattle_prototype.TourMapActivity")) {
                    Log.d("GPSLocationService", "최상위 액티비티 : TourMapActivity");
                    //관광지를 방문했을 경우 업데이트 해야 하므로 다시 호출
                    for (int i = 0; i < GLOBAL.recordCount; i++) {
                        // 특정 위치에 관광지를 표시하기 위해 정의한 메소드(여기에 관광지들 등록하면 됨)
                        TourMapActivity.showSpotPosition(GLOBAL.spot[i].getLatitude(), GLOBAL.spot[i].getLongitude(), GLOBAL.spot[i].getName(), GLOBAL.spot[i].getName(), GLOBAL.spot[i].getVisit());
                    }
                }
                //일단 푸시를 받은 관광지는 펜딩인텐트 목록에서 제외한다.
                for(int i=0;i<mPendingIntentList.size();i++){
                    PendingIntentInfo tempInfo = (PendingIntentInfo)mPendingIntentList.get(i);
                    if(tempInfo.getName().equals(name)){
                        mLocationManager.removeProximityAlert(tempInfo.getPintent());//근접경보해제
                        //mPendingIntentList.remove(i);
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
    }*/


    //현재 위치에서 관광지까지의 거리 계산을 위한 함수
    public double calcDistance(double P1_latitude, double P1_longitude,
                               double P2_latitude, double P2_longitude) {
        if ((P1_latitude == P2_latitude) && (P1_longitude == P2_longitude)) {
            return 0;
        }
        double e10 = P1_latitude * Math.PI / 180;
        double e11 = P1_longitude * Math.PI / 180;
        double e12 = P2_latitude * Math.PI / 180;
        double e13 = P2_longitude * Math.PI / 180;
        /* 타원체 GRS80 */
        double c16 = 6356752.314140910;
        double c15 = 6378137.000000000;
        double c17 = 0.0033528107;
        double f15 = c17 + c17 * c17;
        double f16 = f15 / 2;
        double f17 = c17 * c17 / 2;
        double f18 = c17 * c17 / 8;
        double f19 = c17 * c17 / 16;
        double c18 = e13 - e11;
        double c20 = (1 - c17) * Math.tan(e10);
        double c21 = Math.atan(c20);
        double c22 = Math.sin(c21);
        double c23 = Math.cos(c21);
        double c24 = (1 - c17) * Math.tan(e12);
        double c25 = Math.atan(c24);
        double c26 = Math.sin(c25);
        double c27 = Math.cos(c25);
        double c29 = c18;
        double c31 = (c27 * Math.sin(c29) * c27 * Math.sin(c29))
                + (c23 * c26 - c22 * c27 * Math.cos(c29))
                * (c23 * c26 - c22 * c27 * Math.cos(c29));
        double c33 = (c22 * c26) + (c23 * c27 * Math.cos(c29));
        double c35 = Math.sqrt(c31) / c33;
        double c36 = Math.atan(c35);
        double c38 = 0;
        if (c31 == 0) {
            c38 = 0;
        } else {
            c38 = c23 * c27 * Math.sin(c29) / Math.sqrt(c31);
        }
        double c40 = 0;
        if ((Math.cos(Math.asin(c38)) * Math.cos(Math.asin(c38))) == 0) {
            c40 = 0;
        } else {
            c40 = c33 - 2 * c22 * c26
                    / (Math.cos(Math.asin(c38)) * Math.cos(Math.asin(c38)));
        }
        double c41 = Math.cos(Math.asin(c38)) * Math.cos(Math.asin(c38))
                * (c15 * c15 - c16 * c16) / (c16 * c16);
        double c43 = 1 + c41 / 16384
                * (4096 + c41 * (-768 + c41 * (320 - 175 * c41)));
        double c45 = c41 / 1024 * (256 + c41 * (-128 + c41 * (74 - 47 * c41)));
        double c47 = c45
                * Math.sqrt(c31)
                * (c40 + c45
                / 4
                * (c33 * (-1 + 2 * c40 * c40) - c45 / 6 * c40
                * (-3 + 4 * c31) * (-3 + 4 * c40 * c40)));
        double c50 = c17
                / 16
                * Math.cos(Math.asin(c38))
                * Math.cos(Math.asin(c38))
                * (4 + c17
                * (4 - 3 * Math.cos(Math.asin(c38))
                * Math.cos(Math.asin(c38))));
        double c52 = c18
                + (1 - c50)
                * c17
                * c38
                * (Math.acos(c33) + c50 * Math.sin(Math.acos(c33))
                * (c40 + c50 * c33 * (-1 + 2 * c40 * c40)));
        double c54 = c16 * c43 * (Math.atan(c35) - c47);
        // return distance in meter
        return Math.abs(c54);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
