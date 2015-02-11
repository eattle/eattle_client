package com.example.choi.eattle_prototype;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.Arrays;

public class TourMainActivity extends ActionBarActivity {

    ImageView[] tourSpotPicture = new ImageView[CONSTANT.NUMOFSPOT];
    private static TouristSpotInfo[] spot = new TouristSpotInfo[CONSTANT.NUMOFSPOT];//관광지정보
    private static boolean havelatlonInfo = false; //메인 액티비티를 띄울 때 위도,경도 정보가 있으면 true, 없으면 false
    private static double lastLatitude;//가장 마지막으로 받은 위도
    private static double lastLongitutde;//가장 마지막으로 받은 경도



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour_main);

        //관광지 목록들을 DB에서 읽어온다-------------------------------------------------------
        /*
        spot[0] = new TouristSpotInfo("기숙사 150동",R.drawable.spot1,40.418776, -86.925172);
        spot[1] = new TouristSpotInfo("Burton Morgan",R.drawable.spot2,40.423646, -86.922908);
        spot[2] = new TouristSpotInfo("DLR",R.drawable.spot3,40.421226, -86.922258);
        spot[3] = new TouristSpotInfo("PMU",R.drawable.spot4,40.425588, -86.910810);
        spot[4] = new TouristSpotInfo("Knoy Hall",R.drawable.spot5,40.427661, -86.9111284);
        */
        String SQL = "SELECT name,picName,latitude,longitutde,spotInfoID FROM spot";
        Cursor c = MainActivity.db.rawQuery(SQL,null);
        int recordCount = c.getCount();

        for(int i=0;i<recordCount;i++){
            c.moveToNext();
            String name = c.getString(0);
            String _picName = c.getString(1);
            //R.drawable을 동적으로 가져온다.
            //int picName = getResources().getIdentifier(_picName,"drawable",getPackageName());
            int picName = getResources().getIdentifier(_picName,"drawable",CONSTANT.PACKAGE_NAME);

            float latitude = c.getFloat(2);
            float longitude = c.getFloat(3);
            String spotInfoID = c.getString(4);
            spot[i] = new TouristSpotInfo(name,picName,latitude,longitude,spotInfoID);
        }
        //------------------------------------------------------------------------------------

        //메인 액티비티를 띄울 때 위도, 경도 정보가 있으면
        //거리가 가까운 순으로 관광지들을 정리한다.
        if(havelatlonInfo == true){
            //현재 위치로부터의 거리를 계산한다.
            for(int i=0;i<CONSTANT.NUMOFSPOT;i++) {
                double temp = calcDistance(lastLatitude, lastLongitutde, spot[i].getLatitude(), spot[i].getLongitutde());
                spot[i].setSpotDistanceFromMe(temp);
                Log.d("MainActivity", Double.toString(temp));
            }//가까운 순으로 정렬한다.
            Arrays.sort(spot);
        }

        //"지도보기" 버튼
        Button toMap = (Button)findViewById(R.id.toMap);
        toMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //지도를 띄운다.
                Intent intent = new Intent(getBaseContext(),TourMapActivity.class);
                startActivity(intent);
            }
        });

        // 뷰페이저 객체를 참조하고 어댑터를 설정합니다.
        ViewPager pager = (ViewPager) findViewById(R.id.pager);

        //지정된 텍스트와 이미지로 뷰페이지를 생성한다.
        ViewPagerAdapter adapter = new ViewPagerAdapter(this,spot);

        pager.setAdapter(adapter);
        // 뷰페이저 페이지 개수 설정
        pager.setOffscreenPageLimit(CONSTANT.NUMOFSPOT);


        // 페이지가 변경될 때
        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrollStateChanged(int state) {}

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            //페이지가 변경될때
            public void onPageSelected(int position) {}
        });


    }
    public void onResume(){
        super.onResume();
        if(havelatlonInfo == true){
            //현재 위치로부터의 거리를 계산한다.
            for(int i=0;i<CONSTANT.NUMOFSPOT;i++){
                double temp = calcDistance(lastLatitude, lastLongitutde, spot[i].getLatitude(), spot[i].getLongitutde());
                spot[i].setSpotDistanceFromMe(temp);
                Log.d("MainActivity", Double.toString(temp));//가까운 순으로 정렬한다.
            }
            Arrays.sort(spot);
        }
        // 뷰페이저 객체를 참조하고 어댑터를 설정합니다.
        ViewPager pager = (ViewPager) findViewById(R.id.pager);

        //지정된 텍스트와 이미지로 뷰페이지를 생성한다.
        ViewPagerAdapter adapter = new ViewPagerAdapter(this,spot);

        pager.setAdapter(adapter);
        // 뷰페이저 페이지 개수 설정
        pager.setOffscreenPageLimit(CONSTANT.NUMOFSPOT);

    }

    //현재 위치에서 관광지까지의 거리 계산을 위한 함수
    public static double calcDistance(double lat1, double lon1, double lat2, double lon2){
        double EARTH_R, Rad, radLat1, radLat2, radDist;
        double distance, ret;

        EARTH_R = 6371000.0;
        Rad = Math.PI/180;
        radLat1 = Rad * lat1;
        radLat2 = Rad * lat2;
        radDist = Rad * (lon1 - lon2);

        distance = Math.sin(radLat1) * Math.sin(radLat2);
        distance = distance + Math.cos(radLat1) * Math.cos(radLat2) * Math.cos(radDist);
        ret = EARTH_R * Math.acos(distance);

        double result = Math.round(Math.round(ret) / 1000);
        if(result == 0) result = Math.round(ret);

        return result;
    }

    // get, set
    public static boolean getHavelatlonInfo(){
        return havelatlonInfo;
    }
    public static void setHavelatlonInfo(boolean havelatlonInfo){
        TourMainActivity.havelatlonInfo = havelatlonInfo;
    }
    public static double getLastLatitude(){
        return lastLatitude;
    }
    public static void setLastLatitude(double lastLatitude){
        TourMainActivity.lastLatitude = lastLatitude;
    }
    public static double getLastLongitutde(){
        return lastLongitutde;
    }
    public static void setLastLongitutde(double lastLongitutde){
        TourMainActivity.lastLongitutde = lastLongitutde;
    }
    public static TouristSpotInfo getTouristSpotInfo(int index){
        return spot[index];
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