package com.example.choi.eattle_prototype;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.choi.eattle_prototype.model1.Path;
import com.example.choi.eattle_prototype.model1.Spot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class AlbumMainActivity extends ActionBarActivity {


    public static final int TITLE = 0;
    public static final int LAYOUT1 = 1;
    public static final int LAYOUT2 = 2;

    ArrayList<Fragment> mLayouts = new ArrayList<Fragment>();
    ArrayList<Integer>  mPageList = new ArrayList<Integer>();
    ArrayList<Integer>  mPagePicNumList = new ArrayList<Integer>();
    int mPageIndex;
    int mPathIndex;

    List<Path> paths;

    //앨범의 Image Setting(미디어 DB 연결)
    static AlbumImageSetter ImageSetter;

    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_main);

        //DB로부터 경로 찾아서 넣어줌
        this.db = NearSpotService.dbHelper;
        paths = db.getAllPaths();

        mLayouts.add(new AlbumTitleFragment());
        mLayouts.add(new AlbumLayoutFragment1());
        mLayouts.add(new AlbumLayoutFragment2());

        mPageList.add(0);
        mPageIndex = 0;

        changeFragment(mPageList.get(mPageIndex));

        mPathIndex = 0;

        //마지막 path 이면 끝나는 시간을 현재시간으으로 넣어줌
//        ImageSetter = new AlbumImageSetter(this,paths.get(mPathIndex).getTime(), mPathIndex == paths.size()-1 ? System.currentTimeMillis()/1000 : paths.get(mPathIndex+1).getTime());
        ImageSetter = new AlbumImageSetter(this,paths.get(mPathIndex).getTime(), paths.get(mPathIndex+1).getTime());

    }

    public void mOnClick(View v){
        switch(v.getId())
        {
            case R.id.prev:
                if(mPageIndex == 0) break;
                int subNum = ((AlbumLayout)(mLayouts.get(mPageList.get(mPageIndex)))).getImageNum();
                mPageIndex--;
                Log.e("index1", "" + subNum);
                subNum +=   mPagePicNumList.get(mPageIndex);


                if(mPageList.get(mPageIndex+1)==TITLE){
                    mPathIndex--;
                    ImageSetter.changeTime(paths.get(mPathIndex).getTime(), paths.get(mPathIndex+1).getTime());
                    ImageSetter.changeCursorToLast();
                    subNum--;
                }
                Log.e("index", subNum + " ");

                ImageSetter.setCursorPrev(subNum);

                getSupportFragmentManager().popBackStack();

                break;
            case R.id.next:
                if(paths.get(mPathIndex).getSpotId() == -1) break;


                //하나의 스토리가 끝난 경우 - 타이틀을 넣어줌
                //하나의 스토리가 끝나지 않은 경우 - 다음 장으
                //다음장이 정해져 있는 경우 - 그거대로 다음장으로로
                //다음장이 정해져 있지 않는 경우 - 새로만들어서 다음장으로

                if(ImageSetter.isOver()){
                    // 하나의 스토리가 끝난 경우
                    //하나의 스토리가 끝났다면, 타이틀을 넣고 그걸로 페이지 바꾸고, 시간 바꾸고, 다음 패스로 이동
                    // 다음 것이 -1 이면 return
                    if(paths.get(mPathIndex + 1).getSpotId() == -1) break;
                    mPagePicNumList.add(((AlbumLayout)(mLayouts.get(mPageList.get(mPageIndex)))).getImageNum());

                    mPathIndex++;
                    mPageList.add(TITLE);
                    mPageIndex++;
                    ImageSetter.changeTime(paths.get(mPathIndex).getTime(), paths.get(mPathIndex+1).getTime());
                    changeFragment(TITLE);
                } else {
                    //하나의 스토리가 끝나지 않은 경우
                    if(mPageIndex == mPageList.size()-1){
                        //다음장이 정해져 있지 않는 경우
                        mPagePicNumList.add(((AlbumLayout)(mLayouts.get(mPageList.get(mPageIndex)))).getImageNum());

                        Random random = new Random();
                        int num;
                        do {
                            num = random.nextInt(mLayouts.size()-1)+1;
                        } while (num == mPageList.get(mPageIndex));
                        Log.e("asda", ""+num);
                        mPageList.add(num);
                        mPageIndex++;
                        changeFragment(num);

                    } else {
                        // 다음장이 정해져 있는 경우
                        mPageIndex++;
                        changeFragment(mPageList.get(mPageIndex));

                    }
                }
/*
                //다음장이 정해지지 않았을 경우
                if(mPageIndex == mPageList.size()){
                    if(ImageSetter.isOver()){
                        //하나의 스토리가 끝난 경우
                        //하나의 스토리가 끝났다면, 타이틀을 넣고 그걸로 페이지 바꾸고, 시간 바꾸고, 다음 패스로 이동
                        mPathIndex++;
                        if(paths.get(mPathIndex).getSpotId() == -1){
                            mPageIndex--;
                            break;
                        }
                        mPageList.add(0);
                        changeFragment(mPageList.get(mPageIndex));
                        ImageSetter.changeTime(paths.get(mPathIndex).getTime(), paths.get(mPathIndex+1).getTime());
                    } else {
                        //하나의 스토리가 끝지 않은 경우
                        Random random = new Random();
                        int num;
                        do {
                            num = random.nextInt(mLayouts.size()-1)+1;
                            Log.e("asd", mLayouts.size()+" " +num);
                        } while (num == mPageList.get(mPageIndex - 1));
                        mPageList.add(num);
                        changeFragment(num);
                    }
                }else {
                    //다음장이 정해져 있을 경우()
                    if (ImageSetter.isOver()) {
                        //하나의 스토리가 끝난 경우
                        //하나의 스토리가 끝났다면, 타이틀을 넣고 그걸로 페이지 바꾸고, 시간 바꾸고, 다음 패스로 이동
                        mPathIndex++;
                        if (paths.get(mPathIndex).getSpotId() == -1) {
                            mPageIndex--;
                            break;
                        }
                        mPageList.add(0);
                        changeFragment(mPageList.get(mPageIndex));
                        ImageSetter.changeTime(paths.get(mPathIndex).getTime(), paths.get(mPathIndex + 1).getTime());
                    } else {
                        changeFragment(mPageList.get(mPageIndex));
                    }
                }*/
                break;

        }
    }

    //add replace
    void changeFragment(int layoutnum){
        //어떤 프래그 먼트를 할지는 인자로 받음

        Fragment layout;

        //레이아웃은 list로 부터 만들고, Title은 새로 new 해줌(backstack을 위해)
        switch (layoutnum){
            case TITLE:
                int a = paths.get(mPathIndex).getSpotId();
                Spot spotFromPath = db.getSpot(a);
                int picNum = getResources().getIdentifier(spotFromPath.getPicName(), "drawable", CONSTANT.PACKAGE_NAME);
                layout = AlbumTitleFragment.newInstance(spotFromPath, picNum);
                break;
            default:
                layout = mLayouts.get(layoutnum);
                break;
        }

        //프래그먼트를 먼저 찾고 비어있으면 add, 비어있지 않으면 replace를 호출

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.frame);

        FragmentTransaction tr = fm.beginTransaction();

        if(fragment == null){
            tr.add(R.id.frame, layout, "" + layoutnum);
            tr.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        }else {
            tr.replace(R.id.frame, layout, ""+layoutnum);
            tr.addToBackStack(null);
            tr.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        }

        //백스택에 저장해야됨
        //if(layoutnum == TITLE)
        //    tr.addToBackStack(null);
        tr.commit();
    }

    public void onBackPressed()
    {
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        super.onBackPressed();
    }

/*

    public void addFragment(){
//        aIndex = 0;
//        Random random = new Random();
//        int numFrag = random.nextInt(aLayoutList.size());
        // 0 번째 레이아웃이 뭔지 추가 (인트형)
//        aPageList.add(numFrag);
        aPageList.add(0);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        //aLayoutList.get(aPageList.get(aIndex)) == numFrag
        transaction.replace(R.id.layout, aLayoutList.get(aPageList.get(aIndex)), "AlbumLayout" + 0);
        transaction.commit();

//        ((AlbumLayout)(aLayoutList.get(0))).setSpot(""+paths.get(0).getSpotName());
    }

    public void addTitle(){
        aIndex ++;
        aPageList.add(0);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        //aLayoutList.get(aPageList.get(aIndex)) == numFrag
        transaction.replace(R.id.layout, aLayoutList.get(0), "AlbumLayout" + 0);
        transaction.commit();

//        ((AlbumLayout)(aLayoutList.get(0))).setSpot(""+paths.get(0).getSpotName());
    }

    public void replacePrev(){
        if(aIndex  == 1)
            return;

        aIndex --;
        int subNum = ((AlbumLayout_)(aLayoutList.get(aPageList.get(aIndex+1)))).getMaxImageNum();
        Log.e("index1", "" + subNum);
        subNum += ((AlbumLayout_)(aLayoutList.get(aPageList.get(aIndex)))).getMaxImageNum();

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

*/
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

