package com.example.choi.eattle_prototype;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;


public class AlbumMainActivity extends ActionBarActivity {
    static final int NUM_FRAGMENT = 2;
    ArrayList<Fragment> mAf = new ArrayList<Fragment>();
    ArrayList<Integer> mAlbumLayouts = new ArrayList<Integer>();
    Integer mAlbumIndex = 0;
    static AlbumImageSetter ImageSetter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_main);

        mAf.add(new AlbumLayout1());
        mAf.add(new AlbumLayout2());

        addFragment();

        ImageSetter = new AlbumImageSetter(this);

    }

    public void mOnClickButton(View v){
        switch (v.getId()) {
            case R.id.prev:
                break;
            case R.id.next:
                replaceFragment();
                break;
        }
    }


    public void addFragment(){
        Random random = new Random();
        int numFrag = random.nextInt(NUM_FRAGMENT);
        mAlbumLayouts.add(numFrag);
        Log.e("asdsfs", "" + numFrag);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.add(R.id.layout, mAf.get(numFrag), "AlbumLayout" + numFrag+1);
        transaction.commit();
    }

    public void replaceFragment(){
        Random random = new Random();
        int numFrag;
        do{
            numFrag = random.nextInt(NUM_FRAGMENT);
        }while(numFrag == mAlbumLayouts.get(mAlbumIndex));
        mAlbumIndex ++;
        mAlbumLayouts.add(numFrag);

        Log.e("asdsfs", "" + numFrag);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.layout, mAf.get(numFrag), "AlbumLayout" + numFrag+1);
        transaction.addToBackStack(null);
        transaction.commit();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_album_main, menu);
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

