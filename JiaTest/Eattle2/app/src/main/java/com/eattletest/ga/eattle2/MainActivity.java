package com.eattletest.ga.eattle2;

import android.content.Context;
import android.location.*;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    LocationManager mLocMan;
    TextView mStatus;
    TextView mResult;
    String mProvider;
    int mCount;
    Geocoder mCoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLocMan = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        mStatus = (TextView)findViewById(R.id.status);
        mResult = (TextView)findViewById(R.id.result);

        List<String> arProvider = mLocMan.getProviders(false);
        String result = "";

        for(int i = 0; i < arProvider.size(); i++){
            result += ("Provider " + i + " : " + arProvider.get(i) + "\n");
        }

        Criteria crit = new Criteria();
        //정밀도 설정
        crit.setAccuracy(Criteria.NO_REQUIREMENT);
        //배터리 사용
        crit.setPowerRequirement(Criteria.NO_REQUIREMENT);
        //고도 사용
        crit.setAltitudeRequired(false);
        //비용 설정
        crit.setCostAllowed(false);
        //기준에 가장 부합되는 하나를 선택
        mProvider = mLocMan.getBestProvider(crit, true);
        result += ("\nbest provider : " + mProvider + "\n\n");

        result += LocationManager.GPS_PROVIDER + " : " + mLocMan.isProviderEnabled(LocationManager.GPS_PROVIDER) + "\n";
        result += LocationManager.NETWORK_PROVIDER + " " + mLocMan.isProviderEnabled(LocationManager.NETWORK_PROVIDER) + "\n";

        TextView EditResult = (TextView)findViewById(R.id.provider);
        EditResult.setText(result);

        mCoder = new Geocoder(this);
    }

    public void onResume(){
        super.onResume();
        mCount = 0;

        Location location = mLocMan.getLastKnownLocation(mProvider);

        if(location != null) {
            String sloc = String.format("수신 회수 : %d\n위도 : %f\n경도 : %f\n고도 : %f", mCount, location.getLatitude(), location.getLongitude(), location.getAltitude());
            mResult.setText(sloc);
        }

        mLocMan.requestLocationUpdates(mProvider, 3000, 10, mListener);
        mStatus.setText("현재 상태 : 서비스 시작");

    }
    public void onPause(){
        super.onPause();
        mLocMan.removeUpdates(mListener);
        mStatus.setText("현재 상태 : 서비스 정지");
    }


    LocationListener mListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            mCount++;
            String sloc = String.format("수신 회수 : %d\n위도 : %f\n경도 : %f\n고도 : %f", mCount, location.getLatitude(), location.getLongitude(), location.getAltitude());
            mResult.setText(sloc);
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            ((TextView)findViewById(R.id.lat1)).setText(Double.toString(latitude));
            ((TextView)findViewById(R.id.lot1)).setText(Double.toString(longitude));
            String slat = (latitude > 0 ? "북위" : "남위") + " " + Location.convert(latitude, Location.FORMAT_SECONDS);
            String slot = (longitude > 0 ? "동경" : "서경") + " " + Location.convert(longitude, Location.FORMAT_SECONDS);
            ((TextView)findViewById(R.id.lat2)).setText(slat);
            ((TextView)findViewById(R.id.lot2)).setText(slot);

            List<Address> addr;
            try {
                addr = mCoder.getFromLocation(latitude, longitude, 5);

            } catch (IOException e){
                ((TextView)findViewById(R.id.address)).setText("IO error : " + e.getMessage());
                return;
            }

            if(addr == null){
                ((TextView)findViewById(R.id.address)).setText("no result");
                return;
            }

            ((TextView)findViewById(R.id.address)).setText("개수 = " + addr.size() + "\n" + addr.get(0).toString());
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
            mStatus.setText(provider + "상태 변경" + sStatus);
        }

        @Override
        public void onProviderEnabled(String provider) {
            mStatus.setText("현재 상태 : 서비스 사용 가능");
        }

        @Override
        public void onProviderDisabled(String provider) {
            mStatus.setText("현재 상태 : 서비스 사용 불가");
        }
    };

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
