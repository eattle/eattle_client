package com.example.choi.eattle;

import android.content.Intent;
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

    private int NUMOFSPOT = 5;//관광지 개수
    ImageView[] tourSpotPicture = new ImageView[NUMOFSPOT];
    TourSpot[] spot = new TourSpot[NUMOFSPOT];//관광지정보
    static boolean havelatlonInfo = false; //메인 액티비티를 띄울 때 위도,경도 정보가 있으면 true, 없으면 false
    static double lastLatitude;//가장 마지막으로 받은 위도
    static double lastLongitutde;//가장 마지막으로 받은 경도



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour_main);


        //임의의 지점으로 설정한 더미데이터
        spot[0] = new TourSpot("성산일출봉",R.drawable.spot1,37.5042846,127.0414756);
        spot[1] = new TourSpot("제주월드컵경기장",R.drawable.spot2,37.502757,127.043621);
        spot[2] = new TourSpot("백록담",R.drawable.spot3,37.5009083,127.045714);
        spot[3] = new TourSpot("돌하르방공원",R.drawable.spot4,37.5032836,127.0446134);
        spot[4] = new TourSpot("한라수목원",R.drawable.spot5,37.5005613,127.035285);

        //메인 액티비티를 띄울 때 위도, 경도 정보가 있으면
        //거리가 가까운 순으로 관광지들을 정리한다.
        if(havelatlonInfo == true){
            //현재 위치로부터의 거리를 계산한다.
            for(int i=0;i<NUMOFSPOT;i++) {
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
        pager.setOffscreenPageLimit(NUMOFSPOT);


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
            for(int i=0;i<NUMOFSPOT;i++){
                double temp = calcDistance(lastLatitude, lastLongitutde, spot[i].getLatitude(), spot[i].getLongitutde());
                spot[i].setSpotDistanceFromMe(temp);
                Log.d("MainActivity", Double.toString(temp));            //가까운 순으로 정렬한다.
            }
            Arrays.sort(spot);
        }
        // 뷰페이저 객체를 참조하고 어댑터를 설정합니다.
        ViewPager pager = (ViewPager) findViewById(R.id.pager);

        //지정된 텍스트와 이미지로 뷰페이지를 생성한다.
        ViewPagerAdapter adapter = new ViewPagerAdapter(this,spot);

        pager.setAdapter(adapter);
        // 뷰페이저 페이지 개수 설정
        pager.setOffscreenPageLimit(NUMOFSPOT);

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
