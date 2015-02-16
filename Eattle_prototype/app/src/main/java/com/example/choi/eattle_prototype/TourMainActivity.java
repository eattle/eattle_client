package com.example.choi.eattle_prototype;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
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
import android.widget.Toast;

import java.util.ArrayList;

public class TourMainActivity extends ActionBarActivity {

    private int viewMode = 0;//0이면 뷰페이저, 1이면 리스트 형식으로
    private android.support.v4.view.ViewPager pager;
    private LinearLayout list;
    private ScrollView scroll;

    //스와이프를 인식하기 위한 변수
    private float x1,x2;
    //컨텍스트 메뉴를 위한 변수
    private int selectedSpot;
    FrameLayout tempFrame;//즐겨찾기 별표 보여주기를 위한 임시 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour_main);
        pager = (android.support.v4.view.ViewPager) findViewById(R.id.pager);
        list = (LinearLayout) findViewById(R.id.list);
        scroll = (ScrollView) findViewById(R.id.scroll);

        //뷰페이저 그리기
        drawViewPagerMode();
        //스크롤뷰(리스트) 그리기
        for (int i = 0; i < GLOBAL.recordCount; i++) {
            //리스트(스크롤뷰)에 관광지를 추가한다.
            addTouristSpotToList(GLOBAL.spot[i].getResId(),GLOBAL.spot[i].getName(),i);
        }
        //------------------------------------------------------------------------------------

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

        // 스크롤뷰 다시 그리기-------------------------------------------
        list.removeAllViews();
        for(int i=0;i<GLOBAL.recordCount;i++){
            addTouristSpotToList(GLOBAL.spot[i].getResId(),GLOBAL.spot[i].getName(),i);
        }
        // 뷰페이저 다시 그리기-------------------------------------------
        drawViewPagerMode();
    }



    //스크롤뷰(리스트)에 관광지들을 동적으로 추가하는 함수
    public void addTouristSpotToList(int picName,String name, final int i){
        //리스트 형식의 뷰에도 마찬가지로 추가한다.
        FrameLayout listLayout = new FrameLayout(this);
        ImageView listImage = new ImageView(this);
        TextView listText = new TextView(this);
        ImageView favorite = new ImageView(this);
        //listLayout.setOrientation(LinearLayout.HORIZONTAL);
        listLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,200));

        // 해당 레이아웃의 파라미터 값을 호출
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) listLayout.getLayoutParams();
        // 해당 margin값 변경
        lp.setMargins(15,15,15,15);
        // 변경된 값 적용
        listLayout.setLayoutParams(lp);
        listLayout.setId(i);

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

        //즐겨찾기 별표 추가.
        favorite.setImageResource(R.drawable.favorite);
        favorite.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        favorite.setScaleType(ImageView.ScaleType.FIT_START);
        if(GLOBAL.spot[i].getFavorite() == 0)
            favorite.setVisibility(View.INVISIBLE);
        else if(GLOBAL.spot[i].getFavorite() == 1)
            favorite.setVisibility(View.VISIBLE);
        //한줄 추가
        listLayout.addView(listImage);
        listLayout.addView(listText);
        listLayout.addView(favorite);
        //전체 추가
        list.addView(listLayout);
        //컨텍스트 메뉴를 위해 등록한다.
        registerForContextMenu(listLayout);

        //롱클릭 리스너를 등록한다. - 등록할 필요 없음. 컨텍스트메뉴가 알아서 처리해줌.
        /*
        listLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // TODO Auto-generated method stub

                Toast.makeText(getApplicationContext(), "롱클릭리스너", Toast.LENGTH_SHORT).show ();
                GLOBAL.isLongClick = 1;
                return true;
            }
        });*/
        //클릭 이벤트를 등록한다.
        listLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

            }
        });
        //터치 이벤트(스와이프)를 등록한다.
        listLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        x1 = event.getX();
                        GLOBAL.isLongClick = 0;
                        //Toast.makeText(getApplicationContext(), "action_down", Toast.LENGTH_SHORT).show ();
                        return false;
                    //    break;
                    case MotionEvent.ACTION_UP:
                        if(GLOBAL.isLongClick == 1)
                            break;
                        x2 = event.getX();

                        //Toast.makeText(getApplicationContext(), "action_up"+Float.toString(x1)+" "+Float.toString(x2), Toast.LENGTH_SHORT).show();

                        float deltaX = x2 - x1;

                        //스와이프로 인식
                        if (Math.abs(deltaX) > CONSTANT.MIN_DISTANCE)
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

    //컨텍스트 메뉴 관련 함수
    public boolean onContextItemSelected(MenuItem item){
        switch(item.getItemId()){
            case 1://즐겨찾기 등록
                Toast.makeText(getApplicationContext(), "즐겨찾기 등록", Toast.LENGTH_SHORT).show ();
                //아이디가 selectedSpot인 관광지를 즐겨찾기로 등록한다.
                GLOBAL.spot[selectedSpot].setFavorite(1);
                tempFrame.getChildAt(2).setVisibility(View.VISIBLE);

                // 뷰페이저 다시 그리기-------------------------------------------
                drawViewPagerMode();

                break;
            case 2://즐겨찾기 해제
                Toast.makeText(getApplicationContext(), "즐겨찾기 해제", Toast.LENGTH_SHORT).show ();
                GLOBAL.spot[selectedSpot].setFavorite(0);
                tempFrame.getChildAt(2).setVisibility(View.INVISIBLE);

                // 뷰페이저 다시 그리기-------------------------------------------
                drawViewPagerMode();

                break;
        }

        return super.onContextItemSelected(item);
    }
    public void onCreateContextMenu(ContextMenu menu,View v,ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, v, menuInfo);
        tempFrame = (FrameLayout)v;

        selectedSpot = v.getId();//선택된 관광지의 아이디를 저장한다.
        if(GLOBAL.spot[selectedSpot].getFavorite() == 0)//즐겨찾기로 등록되어 있지 않은 경우
            menu.add(0,1,0,"즐겨찾기 등록");
        if(GLOBAL.spot[selectedSpot].getFavorite() == 1)//이미 즐겨찾기로 등록되어 있는 경우
            menu.add(0,2,0,"즐겨찾기 해제");
    }
    public void drawViewPagerMode(){
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        //지정된 텍스트와 이미지로 뷰페이지를 생성
        ViewPagerAdapter adapter = new ViewPagerAdapter(this, GLOBAL.spot);
        pager.setAdapter(adapter);
        // 뷰페이저 페이지 개수 설정
        pager.setOffscreenPageLimit(GLOBAL.recordCount);
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
