package com.example.cds.eattle_prototype_2;


import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.cds.eattle_prototype_2.helper.DatabaseHelper;
import com.example.cds.eattle_prototype_2.model.Media;

import java.util.ArrayList;
import java.util.List;

//스토리 그리드뷰에서 특정 사진을 클릭했을 때, 뷰페이저를 만들어주는 부분
public class FullPicture extends ActionBarActivity {

    DatabaseHelper db;
    List<Media> mMediaList;
    int initialMediaPosition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        db = DatabaseHelper.getInstance(getApplicationContext());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_picture);

        Intent intent = getIntent();
        mMediaList = intent.getParcelableArrayListExtra("mediaList");
//        int folderId = intent.getIntExtra("folderId", 0);
        initialMediaPosition = intent.getIntExtra("position", 0);

        //뷰페이저 생성
        ExtendedViewPager mViewPager = (ExtendedViewPager) findViewById(R.id.view_pager);
        mViewPager.setAdapter(new TouchImageAdapter());//뷰페이저 어댑터 설정
        mViewPager.setCurrentItem(initialMediaPosition);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                setTabToTag(mMediaList.get(position));
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
    }

    class TouchImageAdapter extends PagerAdapter {

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
            //Toast.makeText(getApplicationContext(),"장소명 : "+m.getPlaceName(),Toast.LENGTH_LONG).show();


            //String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)+"/"+ db.getFolder(m.getFolder_id()).getName()+"/"+m.getName()+".jpg";
            String path = m.getPath();//사진의 경로를 가져온다
            try {
                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inSampleSize = 4;
                Bitmap bm = BitmapFactory.decodeFile(path, opt);
                img.setImageBitmap(bm);
            } catch (OutOfMemoryError e) {
                Log.e("warning", "이미지가 너무 큽니다");
            }

            container.addView(img, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);


            //태그를 불러오기 위한 클릭 리스너
            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pushTabToTag(m);
                    setPlacePopup(m);
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

    }

    Fragment isThereTabToTagHere(){
        return getFragmentManager().findFragmentById(R.id.tagLayout);
    }

    void setTabToTag(Media m){
        if(isThereTabToTagHere() != null){
            FragmentTransaction tr = getFragmentManager().beginTransaction();
            TabToTag ttt = TabToTag.newInstance(m.getId());
            tr.replace(R.id.tagLayout, ttt, "TabToTag");
            tr.setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            tr.commit();
        }
    }

    void pushTabToTag(Media m){
        Fragment f;
        if((f = isThereTabToTagHere()) != null) {
            FragmentTransaction tr = getFragmentManager().beginTransaction();
            //tagArrayList에 있는 모든 태그들을 삭제한다
            tr.remove(f);
            tr.setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            tr.commit();
        }
        else{
            FragmentTransaction tr = getFragmentManager().beginTransaction();
            TabToTag ttt = TabToTag.newInstance(m.getId());
            tr.add(R.id.tagLayout, ttt, "TabToTag");
            tr.setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            tr.commit();
        }
    }

    void setPlacePopup(Media m){
        //장소명이 존재하면 태그로 추가할지 묻는다
        if(!m.getPlaceName().equals("")){
            //일단 m.getPlaceName()이 태그 목록에 있는지 확인한다
            int tagId=db.getTagIdByTagName(m.getPlaceName());
            //1. 해당 장소명으로 태그가 아예 존재하지 않을 때 -> 묻는다
            if(tagId == 0) {
                Intent intent = new Intent(getApplicationContext(), PopupForTagAddition.class);
                DataForTagAddition tempData = new DataForTagAddition(tagId,m.getId(),m.getFolder_id(),m.getPlaceName());
                intent.putExtra("dataForTagAddition",tempData);
                startActivity(intent);
            }
            //2. 해당 장소명으로 태그가 존재하는데, 해당 폴더에 등록되어 있지 않을 때 -> 묻는다
            else if(db.getMediaTagByIds(tagId,m.getId()) == 0) {
                Intent intent = new Intent(getApplicationContext(), PopupForTagAddition.class);
                DataForTagAddition tempData = new DataForTagAddition(tagId,m.getId(),m.getFolder_id(),m.getPlaceName());
                intent.putExtra("dataForTagAddition",tempData);
                startActivity(intent);
            }
            //(한번 물어봤는데 아니요로 대답할 시에 다음부터 묻지 않는다 -> 추후구현(아니오라고 대답했을 때 태그-미디어 DB에 등록하고 추가적인 flag를 다는 방식으로)
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
