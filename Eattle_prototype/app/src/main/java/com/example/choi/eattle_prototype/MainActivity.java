package com.example.choi.eattle_prototype;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class MainActivity extends ActionBarActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //위치 관련 서비스 시작
        Intent locationIntent = new Intent(this,NearSpotService.class);
        startService(locationIntent);

    }

    //사진 뷰어, 관광 뷰어로 나누어지는 부분
    public void mOnClickButton(View v){
        switch(v.getId()){
            case R.id.picture:
                Intent toPicture = new Intent(this,AlbumMainActivity.class);
                startActivity(toPicture);
                break;
            case R.id.tour:
                Intent toTour = new Intent(this,TourMainActivity.class);
                startActivity(toTour);
                break;
        }
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
