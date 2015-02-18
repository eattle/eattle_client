package com.example.choi.eattle_prototype;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;


public class AlbumMainActivity extends ActionBarActivity {
    //레이아웃
    ArrayList<Fragment> aLayoutList = new ArrayList<Fragment>();
    //각 페이지 별 레이아웃 넘버를 저장하는 List
    ArrayList<Integer> aPageList = new ArrayList<Integer>();
    //현재의 AlbumIndex
    Integer aIndex = 0;

    //앨범의 Image Setting(미디어 DB 연결)
    static AlbumImageSetter ImageSetter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_main);

        aLayoutList.add(new AlbumLayout1());
        aLayoutList.add(new AlbumLayout2());

        addFragment();

        ImageSetter = new AlbumImageSetter(this);

    }

    public void mOnClickButton(View v){

        switch (v.getId()) {
            case R.id.prev:
                replacePrev();
                break;
            case R.id.next:
                replaceNext();
                break;
        }
        Log.e("size1", "" + aPageList.size());
        Log.e("size2", ""+aIndex);

    }


    public void addFragment(){
        aIndex = 0;
        Random random = new Random();
        int numFrag = random.nextInt(aLayoutList.size());
        // 0 번째 레이아웃이 뭔지 추가 (인트형)
        aPageList.add(numFrag);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        //aLayoutList.get(aPageList.get(aIndex)) == numFrag
        transaction.add(R.id.layout, aLayoutList.get(aPageList.get(aIndex)), "AlbumLayout" + (numFrag + 1));
        transaction.commit();
    }

    public void replacePrev(){
        if(aIndex  == 0)
            return;

        aIndex --;
        int subNum = ((AlbumLayout)(aLayoutList.get(aPageList.get(aIndex+1)))).getMaxImageNum();
        Log.e("index1", "" + subNum);
        subNum += ((AlbumLayout)(aLayoutList.get(aPageList.get(aIndex)))).getMaxImageNum();

        Log.e("index2", "" + subNum);


        ImageSetter.setCursorPrev(subNum);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        //aPageList.get(aIndex) == numFrag
        transaction.replace(R.id.layout, aLayoutList.get(aPageList.get(aIndex)));
        transaction.commit();
    }


    public void replaceNext(){
        aIndex ++;
        Random random = new Random();
        int numFrag;

        //aIndex가 지금까지 본 마지막일 경우
        if(aPageList.size() == aIndex) {

            //이전 것과 같지 않은레이아웃으로 선별
            do {
                numFrag = random.nextInt(aLayoutList.size());
            } while (numFrag == aPageList.get(aIndex-1));
            aPageList.add(numFrag);
        }

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        //aPageList.get(aIndex) == numFrag
        transaction.replace(R.id.layout, aLayoutList.get(aPageList.get(aIndex)));
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

