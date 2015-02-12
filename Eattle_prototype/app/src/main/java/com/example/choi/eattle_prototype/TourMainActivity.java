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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Arrays;

public class TourMainActivity extends ActionBarActivity {

    ImageView[] tourSpotPicture = new ImageView[CONSTANT.NUMOFSPOT];
    private static TouristSpotInfo[] spot = new TouristSpotInfo[CONSTANT.NUMOFSPOT];//관광지정보
    private static boolean havelatlonInfo = false; //메인 액티비티를 띄울 때 위도,경도 정보가 있으면 true, 없으면 false
    private static double lastLatitude;//가장 마지막으로 받은 위도
    private static double lastLongitutde;//가장 마지막으로 받은 경도
    private int mode = 0;//0이면 뷰페이저, 1이면 리스트 형식으로
    private android.support.v4.view.ViewPager pager;
    private LinearLayout list;
    private ScrollView scroll;
    /*
    //제스처를 인식하기 위한 변수들-----------------
    // 드래그시 좌표 저장
    int posX1=0, posX2=0, posY1=0, posY2=0;
    // 핀치시 두좌표간의 거리 저장
    float oldDist = 1f;
    float newDist = 1f;
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;
*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour_main);
        pager = (android.support.v4.view.ViewPager) findViewById(R.id.pager);
        list = (LinearLayout) findViewById(R.id.list);
        scroll = (ScrollView) findViewById(R.id.scroll);
        //관광지 목록들을 DB에서 읽어온다-------------------------------------------------------
        /*
        spot[0] = new TouristSpotInfo("기숙사 150동",R.drawable.spot1,40.418776, -86.925172);
        spot[1] = new TouristSpotInfo("Burton Morgan",R.drawable.spot2,40.423646, -86.922908);
        spot[2] = new TouristSpotInfo("DLR",R.drawable.spot3,40.421226, -86.922258);
        spot[3] = new TouristSpotInfo("PMU",R.drawable.spot4,40.425588, -86.910810);
        spot[4] = new TouristSpotInfo("Knoy Hall",R.drawable.spot5,40.427661, -86.9111284);
        */
        String SQL = "SELECT name,picName,latitude,longitutde,spotInfoID FROM spot";
        Cursor c = MainActivity.db.rawQuery(SQL, null);
        int recordCount = c.getCount();

        for (int i = 0; i < recordCount; i++) {
            c.moveToNext();
            String name = c.getString(0);
            String _picName = c.getString(1);
            //R.drawable을 동적으로 가져온다.
            //int picName = getResources().getIdentifier(_picName,"drawable",getPackageName());
            int picName = getResources().getIdentifier(_picName, "drawable", CONSTANT.PACKAGE_NAME);

            float latitude = c.getFloat(2);
            float longitude = c.getFloat(3);
            String spotInfoID = c.getString(4);
            spot[i] = new TouristSpotInfo(name, picName, latitude, longitude, spotInfoID);

            //리스트(스크롤뷰)에 관광지를 추가한다.
            addTouristSpotToList(picName,name);
        }
        //------------------------------------------------------------------------------------

        //메인 액티비티를 띄울 때 위도, 경도 정보가 있으면
        //거리가 가까운 순으로 관광지들을 정리한다.
        if (havelatlonInfo == true) {
            //현재 위치로부터의 거리를 계산한다.
            for (int i = 0; i < CONSTANT.NUMOFSPOT; i++) {
                double temp = calcDistance(lastLatitude, lastLongitutde, spot[i].getLatitude(), spot[i].getLongitutde());
                spot[i].setSpotDistanceFromMe(temp);
                Log.d("MainActivity", Double.toString(temp));
            }//가까운 순으로 정렬한다.
            Arrays.sort(spot);
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
                if (mode == 0) {//뷰페이저->리스트뷰
                    //뷰페이저를 안보이도록 한다.
                    pager.setVisibility(View.INVISIBLE);
                    //리스트를 보이도록 한다.
                    scroll.setVisibility(View.VISIBLE);
                    mode=1;
                } else if (mode == 1) {//리스트뷰->뷰페이저
                    //리스트를 안보이도록 한다.
                    scroll.setVisibility(View.INVISIBLE);
                    //뷰페이저를 보이도록 한다.
                    pager.setVisibility(View.VISIBLE);
                    mode=0;
                }
            }
        });


        // 뷰페이저 객체를 참조하고 어댑터를 설정합니다.
        ViewPager pager = (ViewPager) findViewById(R.id.pager);

        //지정된 텍스트와 이미지로 뷰페이지를 생성한다.
        ViewPagerAdapter adapter = new ViewPagerAdapter(this, spot);

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

        if (havelatlonInfo == true) {
            //현재 위치로부터의 거리를 계산한다.
            for (int i = 0; i < CONSTANT.NUMOFSPOT; i++) {
                double temp = calcDistance(lastLatitude, lastLongitutde, spot[i].getLatitude(), spot[i].getLongitutde());
                spot[i].setSpotDistanceFromMe(temp);
                Log.d("MainActivity", Double.toString(temp));//가까운 순으로 정렬한다.
            }
            Arrays.sort(spot);
        }
        // 스크롤뷰 다시 그리기-------------------------------------------
        list.removeAllViews();
        for(int i=0;i<CONSTANT.NUMOFSPOT;i++){
            addTouristSpotToList(spot[i].getResId(),spot[i].getName());
        }
        // 뷰페이저 다시 그리기-------------------------------------------
        // 뷰페이저 객체를 참조하고 어댑터를 설정
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        //지정된 텍스트와 이미지로 뷰페이지를 생성
        ViewPagerAdapter adapter = new ViewPagerAdapter(this, spot);
        pager.setAdapter(adapter);
        // 뷰페이저 페이지 개수 설정
        pager.setOffscreenPageLimit(CONSTANT.NUMOFSPOT);
    }

    //현재 위치에서 관광지까지의 거리 계산을 위한 함수
    public static double calcDistance(double lat1, double lon1, double lat2, double lon2) {
        double EARTH_R, Rad, radLat1, radLat2, radDist;
        double distance, ret;

        EARTH_R = 6371000.0;
        Rad = Math.PI / 180;
        radLat1 = Rad * lat1;
        radLat2 = Rad * lat2;
        radDist = Rad * (lon1 - lon2);

        distance = Math.sin(radLat1) * Math.sin(radLat2);
        distance = distance + Math.cos(radLat1) * Math.cos(radLat2) * Math.cos(radDist);
        ret = EARTH_R * Math.acos(distance);

        double result = Math.round(Math.round(ret) / 1000);
        if (result == 0) result = Math.round(ret);

        return result;
    }
    public void addTouristSpotToList(int picName,String name){
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
    }
    /*
    //제스처(줌인,줌아웃)을 인식하기 위한 함수
    public boolean onTouchEvent(MotionEvent event) {
        int act = event.getAction();
        String strMsg = "";

        switch(act & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:    //첫번째 손가락 터치(드래그 용도)
                posX1 = (int) event.getX();
                posY1 = (int) event.getY();

                Log.d("zoom", "mode=DRAG");
                mode = DRAG;
                break;
            case MotionEvent.ACTION_MOVE:
                if(mode == DRAG) {  // 드래그 중
                    posX2 = (int) event.getX();
                    posY2 = (int) event.getY();

                    if(Math.abs(posX2-posX1)>20 || Math.abs(posY2-posY1)>20) {
                        posX1 = posX2;
                        posY1 = posY2;
                        strMsg = "drag";
                        Toast toast = Toast.makeText(this, strMsg, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                } else if (mode == ZOOM) {    // 핀치 중
                    newDist = spacing(event);

                    Log.d("zoom", "newDist=" + newDist);
                    Log.d("zoom", "oldDist=" + oldDist);

                    if (newDist - oldDist > 40) { // zoom in
                        oldDist = newDist;

                        strMsg = "zoom in";//확대
                        Toast toast = Toast.makeText(this, strMsg, Toast.LENGTH_SHORT);
                        toast.show();
                    } else if(oldDist - newDist > 40) { // zoom out
                        oldDist = newDist;

                        strMsg = "zoom out";//축소
                        Toast toast = Toast.makeText(this, strMsg, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:    // 첫번째 손가락을 떼었을 경우
            case MotionEvent.ACTION_POINTER_UP:  // 두번째 손가락을 떼었을 경우
                mode = NONE;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                //두번째 손가락 터치(손가락 2개를 인식하였기 때문에 핀치 줌으로 판별)
                mode = ZOOM;

                newDist = spacing(event);
                oldDist = spacing(event);

                Log.d("zoom", "newDist=" + newDist);
                Log.d("zoom", "oldDist=" + oldDist);
                Log.d("zoom", "mode=ZOOM");
                break;
            case MotionEvent.ACTION_CANCEL:
            default :
                break;
        }

        return super.onTouchEvent(event);
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }*/
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

    public static TouristSpotInfo getTouristSpotInfo(int index) {
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
