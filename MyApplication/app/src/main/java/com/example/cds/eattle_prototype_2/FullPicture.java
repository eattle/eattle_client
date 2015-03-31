package com.example.cds.eattle_prototype_2;


import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.cds.eattle_prototype_2.helper.DatabaseHelper;
import com.example.cds.eattle_prototype_2.model.Media;

import java.util.ArrayList;
import java.util.List;

//스토리 그리드뷰에서 특정 사진을 클릭했을 때, 뷰페이저를 만들어주는 부분
public class FullPicture extends ActionBarActivity {

    final static int TAGING = 11111;

    DatabaseHelper db;
    List<Media> mMediaList;
    int mediaPosition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        db = DatabaseHelper.getInstance(getApplicationContext());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_picture);

        Bundle bundle = this.getIntent().getExtras();

        int folderId = bundle.getInt("folderId");
        mediaPosition = bundle.getInt("position");
//        int folderId = this.getIntent().getExtras().get("folderId");
//        Object selectedMedia =  this.getIntent().getExtras().get("selectedMedia");

        mMediaList = db.getAllMediaByFolder(folderId);
        Log.d("media", ""+mMediaList.size());
//        mediaPosition = ((int) ((Media)selectedMedia).getId());

        //뷰페이저 생성
        ExtendedViewPager mViewPager = (ExtendedViewPager) findViewById(R.id.view_pager);
        mViewPager.setAdapter(new TouchImageAdapter());//뷰페이저 어댑터 설정
        mViewPager.setCurrentItem(mediaPosition);
    }

    class TouchImageAdapter extends PagerAdapter {

//        private static int[] images = { R.drawable.nature_1, R.drawable.nature_2, R.drawable.nature_3, R.drawable.nature_4, R.drawable.nature_5 };

        //하나의 이미지에 하나 이상의 태그가 있기 때문에 ArrayList를 선언한다
        ArrayList<TabToTag> tagArrayList = new ArrayList<TabToTag>();

        @Override
        public int getCount() {
            return mMediaList.size();
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            TouchImageView img = new TouchImageView(container.getContext());

            final Media m = mMediaList.get(position);

            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)+"/"+ db.getFolder(m.getFolder_id()).getName()+"/"+m.getName()+".jpg";
            try {
                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inSampleSize = 4;
                Bitmap bm = BitmapFactory.decodeFile(path, opt);
                img.setImageBitmap(bm);
            } catch (OutOfMemoryError e) {
                Log.e("warning", "이미지가 너무 큽니다");
            }

//            img.setImageURI();
//            img.setImageResource(mMediaList.get(mediaPosition+position));

            container.addView(img, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);


            //태그를 불러오기 위한 클릭 리스너
            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager fm = getFragmentManager();
                    //태그가 들어갈 레이아웃을 불러온다
                    Fragment fragment = fm.findFragmentById(R.id.tagLayout);
                    if(fragment == null) {

                        FragmentTransaction tr = fm.beginTransaction();


                        //Tag DB에서 해당 이미지의 아이디로 태그 목록들을 불러온다
                        long pictureID = Long.valueOf(m.getName());
                        //TODO

                        //반복문을 통해 모든 태그들을 등록한다
                        int numOfTag = 2;//TODO , 2는 더미데이터
                        for(int i=0;i<numOfTag;i++){
                            //태그의 이름을 가져온다
                            //TODO

                            //TabToTag 객체를 생성한다
                            TabToTag ttt = TabToTag.newInstance(m.getYear());//TODO , m.getYear()는 더미데이터
                            tr.add(R.id.tagLayout, ttt, "TabToTag");

                            //태그의 위치를 조정한다
                            //TODO
                        }

                        tr.setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                        tr.commit();
                    }
                    else{
                        FragmentTransaction tr = fm.beginTransaction();

                        //tagArrayList에 있는 모든 태그들을 삭제한다
                        //TODO

                        tr.remove(fragment);
                        tr.setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                        tr.commit();
                        fm.executePendingTransactions();
                    }
                }
            });

            return img;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void startUpdate(ViewGroup container){
            FragmentManager fm = getFragmentManager();
            Fragment fragment = fm.findFragmentById(R.id.tagLayout);
            if(fragment != null) {
                FragmentTransaction tr = fm.beginTransaction();
                tr.remove(fragment);
                tr.commit();
                fm.executePendingTransactions();
            }
            super.startUpdate(container);
        }

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_full_picture, menu);
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
