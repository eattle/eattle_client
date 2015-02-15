package com.example.choi.eattle_prototype;


import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * 뷰페이저를 위한 어댑터 정의
 */
public class ViewPagerAdapter extends PagerAdapter {
    //private String[] names;// 관광지 이름
    //private int[] resIds;// 관광지 사진
    private TouristSpotInfo[] tourSpot;
    private Context mContext;//Context 객체

    //생성자
    public ViewPagerAdapter(Context context) {
        mContext = context;
    }
    public ViewPagerAdapter(Context context, TouristSpotInfo[] tourSpot){//TourSpot 배열을 전달받았을 때
        this.tourSpot = new TouristSpotInfo[GLOBAL.recordCount];
        System.arraycopy(tourSpot,0,this.tourSpot,0,GLOBAL.recordCount);

        mContext = context;
    }

    public ViewPagerAdapter(Context context, ArrayList<TouristSpotInfo> spot) {//TourSpot ArrayList를 받았을 때
        this.tourSpot = spot.toArray(new TouristSpotInfo[GLOBAL.recordCount]);
        mContext = context;
    }

    /**
     * 페이지 갯수
     */
    public int getCount() {
        return GLOBAL.recordCount;
    }


    /**
     * 뷰페이저가 만들어졌을 때 호출됨(NUMOFSPOT 수만큼 호출됨)
     */
    public Object instantiateItem(ViewGroup container, int position) {
        // create a instance of the page and set data
        SpotPage page;
        if(tourSpot[position].getLatitude() == 1 && tourSpot[position].getLongitutde() == 1){
            page = new SpotPage(mContext,-1);
        }
        else {
            Log.d("ViewPagerAdapter",Integer.toString(position));
            page = new SpotPage(mContext, position);
        }
        page.setNameText(tourSpot[position].getName());
        page.setImage(tourSpot[position].getResId());

        // 컨테이너에 추가
        //container.addView(page, ((ViewPager)container).getChildCount() > position ? position : ((ViewPager)container).getChildCount());
        container.addView(page, position);

        return page;
    }

    /**
     * Called to remove the page
     */
    public void destroyItem(ViewGroup container, int position, Object view) {
        container.removeView((View) view);
    }

    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }
}
