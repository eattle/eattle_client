package com.example.choi.eattle_prototype;

import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;


/**
 * 2 depth
 */
public class DetailedInfoActivity extends ActionBarActivity {

    private int NUMOFSPOTDETAILED;
    private int currentSpot; // 어떤 관광지를 터치해서 들어왔는지

    /*
    DetailedInfoActivity(){}
    DetailedInfoActivity(int currentSpot){
        this.currentSpot = currentSpot;
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("DetailedInfoActivity", "DetailedInfoActivity onCreate함수 호출");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_info);

        // 뷰페이저 객체를 참조하고 어댑터를 설정합니다.
        ViewPager pager = (ViewPager) findViewById(R.id.pager);

        ArrayList<TouristSpotInfo> spot;
        Intent i = getIntent();
        spot = i.getParcelableArrayListExtra("spots");

        ViewPagerAdapter adapter = new ViewPagerAdapter(this,spot);
        pager.setAdapter(adapter);
        // 뷰페이저 페이지 개수
        NUMOFSPOTDETAILED = spot.size();//상세 정보의 개수만큼
        pager.setOffscreenPageLimit(NUMOFSPOTDETAILED);
    }

    public void onNewIntent(Intent newIntent){
        Intent receivedIntent = getIntent();

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
