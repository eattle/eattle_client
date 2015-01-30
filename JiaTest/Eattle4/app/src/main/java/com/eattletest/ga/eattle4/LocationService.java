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