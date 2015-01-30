package com.eattletest.ga.eattle4;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


public class LocationController extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_controller);
    }

    public void mOnClick(View v){
        Intent intent;
        switch (v.getId()){
            case R.id.start:
                intent = new Intent(this, LocationService.class);
                startService(intent);
                break;
            case R.id.finish:
                intent = new Intent(this, LocationService.class);
                stopService(intent);
                break;
        }
    }

    public void setProvider(String provider){
        ((TextView)findViewById(R.id.provider)).setText(provider);
    }
    public void setStatus(String status){
        ((TextView)findViewById(R.id.status)).setText(status);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_location_controller, menu);
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
