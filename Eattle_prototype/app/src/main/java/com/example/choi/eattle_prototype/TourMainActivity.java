package com.example.choi.eattle_prototype;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class TourMainActivity extends ActionBarActivity {

    ImageView[] tourSpotPicture = new ImageView[CONSTANT.NUMOFSPOT];
    //private static TouristSpotInfo[] spot = new TouristSpotInfo[CONSTANT.NUMOFSPOT];//관광지정보
    private static boolean havelatlonInfo = false; //메인 액티비티를 띄울 때 위도,경도 정보가 있으면 true, 없으면 false
    private static double lastLatitude;//가장 마지막으로 받은 위도
    private static double lastLongitutde;//가장 마지막으로 받은 경도
    private int viewMode = 0;//0이면 뷰페이저, 1이면 리스트 형식으로
    private android.support.v4.view.ViewPager pager;
    private LinearLayout list;
    private ScrollView scroll;

    //스와이프를 인식하기 위한 변수
    private float x1,x2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour_main);
        pager = (android.support.v4.view.ViewPager) findViewById(R.id.pager);
        list = (LinearLayout) findViewById(R.id.list);
        scroll = (ScrollView) findViewById(R.id.scroll);

        for (int i = 0; i < GLOBAL.recordCount; i++) {
            //리스트(스크롤뷰)에 관광지를 추가한다.
            addTouristSpotToList(GLOBAL.spot[i].getResId(),GLOBAL.spot[i].getName(),i);
        }
        //------------------------------------------------------------------------------------

        //메인 액티비티를 띄울 때 위도, 경도 정보가 있으면
        //거리가 가까운 순으로 관광지들을 정리한다.
        if (havelatlonInfo == true) {
            //현재 위치로부터의 거리를 계산한다.
            for (int i = 0; i < CONSTANT.NUMOFSPOT; i++) {
                double temp = calcDistance(lastLatitude, lastLongitutde, GLOBAL.spot[i].getLatitude(), GLOBAL.spot[i].getLongitutde());
                GLOBAL.spot[i].setSpotDistanceFromMe(temp);
                Log.d("MainActivity", "관광지의 위도, 경도"+Double.toString(GLOBAL.spot[i].getLatitude())+" "+Double.toString(GLOBAL.spot[i].getLongitutde()));
                Log.d("MainActivity", GLOBAL.spot[i].getName()+" 가 현재 위치로 부터 떨어진 거리 : "+Double.toString(temp));
            }//가까운 순으로 정렬한다.
            Arrays.sort(GLOBAL.spot);
        }

        //"지도보기" 버튼-----------------------------------------------------------
        Button toMap = (Button) findViewById(R.id.toMap);
        toMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //지도를 띄운다.
                Intent intent = new Intent(getBaseContext(), TourMapActivity.class);
                startActivity(intent);
            }
        });
        //"모드변환" 버튼-----------------------------------------------------------
        Button changeMode = (Button) findViewById(R.id.changeMode);
        changeMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //관광지를 하나씩 보여줄 것인지, 리스트 형태로 보여줄 것인지
                if (viewMode == 0) {//뷰페이저->리스트뷰
                    //뷰페이저를 안보이도록 한다.
                    pager.setVisibility(View.INVISIBLE);
                    //리스트를 보이도록 한다.
                    scroll.setVisibility(View.VISIBLE);
                    viewMode=1;
                } else if (viewMode == 1) {//리스트뷰->뷰페이저
                    //리스트를 안보이도록 한다.
                    scroll.setVisibility(View.INVISIBLE);
                    //뷰페이저를 보이도록 한다.
                    pager.setVisibility(View.VISIBLE);
                    viewMode=0;
                }
            }
        });


        // 뷰페이저 객체를 참조하고 어댑터를 설정합니다.
        ViewPager pager = (ViewPager) findViewById(R.id.pager);

        //지정된 텍스트와 이미지로 뷰페이지를 생성한다.
        ViewPagerAdapter adapter = new ViewPagerAdapter(this, GLOBAL.spot);

        pager.setAdapter(adapter);
        // 뷰페이저 페이지 개수 설정
        pager.setOffscreenPageLimit(CONSTANT.NUMOFSPOT);


        // 페이지가 변경될 때
        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener(){
            @Override
            public void onPageScrollStateChanged(int state) {
            }
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            @Override
            //페이지가 변경될때
            public void onPageSelected(int position) {
            }
        });


    }

    public void onResume() {
        super.onResume();
        //거리에 따라 관광지를 정렬한다
        if (havelatlonInfo == true) {
            //현재 위치로부터의 거리를 계산한다.
            for (int i = 0; i < GLOBAL.recordCount; i++) {
                Log.d("MainActivity", "!!!"+Double.toString(lastLatitude)+" "+Double.toString(lastLongitutde));
                Log.d("MainActivity", "관광지의 위도, 경도"+Double.toString(GLOBAL.spot[i].getLatitude())+" "+Double.toString(GLOBAL.spot[i].getLongitutde()));
                double temp = calcDistance(lastLatitude, lastLongitutde, GLOBAL.spot[i].getLatitude(), GLOBAL.spot[i].getLongitutde());
                GLOBAL.spot[i].setSpotDistanceFromMe(temp);
                Log.d("MainActivity", GLOBAL.spot[i].getName()+" 가 현재 위치로 부터 떨어진 거리 : "+Double.toString(temp));
            }//가까운 순으로 정렬한다.
            Arrays.sort(GLOBAL.spot);
        }
        // 스크롤뷰 다시 그리기-------------------------------------------
        list.removeAllViews();
        for(int i=0;i<GLOBAL.recordCount;i++){
            addTouristSpotToList(GLOBAL.spot[i].getResId(),GLOBAL.spot[i].getName(),i);
        }
        // 뷰페이저 다시 그리기-------------------------------------------
        // 뷰페이저 객체를 참조하고 어댑터를 설정
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        //지정된 텍스트와 이미지로 뷰페이지를 생성
        ViewPagerAdapter adapter = new ViewPagerAdapter(this, GLOBAL.spot);
        pager.setAdapter(adapter);
        // 뷰페이저 페이지 개수 설정
        pager.setOffscreenPageLimit(GLOBAL.recordCount);
    }

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
        return c54;
    }

    //스크롤뷰(리스트)에 관광지들을 동적으로 추가하는 함수
    public void addTouristSpotToList(int picName,String name, final int i){
        //리스트 형식의 뷰에도 마찬가지로 추가한다.
        FrameLayout listLayout = new FrameLayout(this);
        ImageView listImage = new ImageView(this);
        TextView listText = new TextView(this);
        //listLayout.setOrientation(LinearLayout.HORIZONTAL);
        listLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,200));

        // 해당 레이아웃의 파라미터 값을 호출
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) listLayout.getLayoutParams();
        // 해당 margin값 변경
        lp.setMargins(15,15,15,15);
        // 변경된 값 적용
        listLayout.setLayoutParams(lp);

        listImage.setImageResource(picName);
        listImage.setScaleType(ImageView.ScaleType.CENTER_CROP); // 레이아웃 크기에 이미지를 맞춘다
        //listImage.setLayoutParams(new ViewGroup.LayoutParams(120,120));
        listImage.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
        listImage.setAlpha(1200);

        listText.setText(name);
        listText.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        listText.setPadding(10, 10, 20, 10);
        listText.setTextColor(Color.WHITE);
        listText.setTextSize(20);
        listText.setGravity(Gravity.CENTER);
        //한줄 추가
        listLayout.addView(listImage);
        listLayout.addView(listText);
        //전체 추가
        list.addView(listLayout);

        //터치 이벤트를 등록한다.
        listLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        x1 = event.getX();
                        //Toast.makeText(getApplicationContext(), "action_down", Toast.LENGTH_SHORT).show ();
                        return true;
                    //    break;
                    case MotionEvent.ACTION_UP:
                        x2 = event.getX();

                        //Toast.makeText(getApplicationContext(), "action_up"+Float.toString(x1)+" "+Float.toString(x2), Toast.LENGTH_SHORT).show();

                        float deltaX = x2 - x1;
                        //관광지 선택으로 인식
                        if(Math.abs(deltaX) < 10){
                            Intent intent = new Intent(getApplicationContext(), DetailedInfoActivity.class);

                            //DB 쿼리로 변경될 부분, intent와 함께 넘겨줄 데이터를 정의하는 부분
                            ArrayList<TouristSpotInfo> spot = new ArrayList<TouristSpotInfo>();

                            String[] args = GLOBAL.spot[i].getDetailedInfo();//특정 관광지의 상세정보 ID를 얻어온다.

                            for (int j = 0; j < args.length; j++) {
                                String SQL = "SELECT info,picName FROM spotInfo WHERE _id = " + args[j];
                                Cursor c = NearSpotService.db.rawQuery(SQL, null);
                                c.moveToNext();
                                String spotInfo = c.getString(0);
                                String _picName = c.getString(1);
                                //R.drawable을 동적으로 가져온다.
                                int tempPicName = getResources().getIdentifier(_picName, "drawable", CONSTANT.PACKAGE_NAME);
                                spot.add(new TouristSpotInfo(spotInfo, tempPicName, 1, 1));
                            }

                            //객체배열을 ArrayList로 넘겨준다.
                            intent.putParcelableArrayListExtra("spots", spot);
                            startActivity(intent);
                            x1 = event.getX();
                        }
                        //스와이프로 인식
                        else if (Math.abs(deltaX) > CONSTANT.MIN_DISTANCE)
                        {
                            //Toast.makeText(getApplicationContext(), "swipe", Toast.LENGTH_SHORT).show ();
                            //spot에서 i인덱스를 가진 관광지를 목록에서 지운다.
                            for(int j=(i+1);j<GLOBAL.recordCount;j++) {
                                GLOBAL.spot[j-1] = GLOBAL.spot[j];
                            }
                            GLOBAL.recordCount--;
                            CONSTANT.NUMOFSPOT--;
                            //새로 그린다.
                            onResume();
                        }
                        break;
                }
                return false;
            }
        });
    }

    // get, set
    public static boolean getHavelatlonInfo() {
        return havelatlonInfo;
    }

    public static void setHavelatlonInfo(boolean havelatlonInfo) {
        TourMainActivity.havelatlonInfo = havelatlonInfo;
    }

    public static double getLastLatitude() {
        return lastLatitude;
    }

    public static void setLastLatitude(double lastLatitude) {
        TourMainActivity.lastLatitude = lastLatitude;
    }

    public static double getLastLongitutde() {
        return lastLongitutde;
    }

    public static void setLastLongitutde(double lastLongitutde) {
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
