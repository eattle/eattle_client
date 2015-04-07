package com.example.cds.eattle_prototype_2;


import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
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

//            img.setImageURI();
//            img.setImageResource(mMediaList.get(mediaPosition+position));

            container.addView(img, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

/*            FragmentManager fm = getFragmentManager();
            Fragment fragment = fm.findFragmentById(R.id.tagLayout);
            //프래그먼트가 있으면, 프래그먼를 새로 넣고 값을 바꿔준다.
            if(fragment != null) {
                FragmentTransaction tr = fm.beginTransaction();
                TabToTag ttt = TabToTag.newInstance(m.getId());
                tr.replace(R.id.tagLayout, ttt);
                tr.commit();
                fm.executePendingTransactions();

            }*/

            //태그를 불러오기 위한 클릭 리스너
            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager fm = getFragmentManager();
                    //태그가 들어갈 레이아웃을 불러온다
                    Fragment fragment = fm.findFragmentById(R.id.tagLayout);
                    if(fragment == null) {
                        FragmentTransaction tr = fm.beginTransaction();
                        TabToTag ttt = TabToTag.newInstance(m.getId());
                        tr.add(R.id.tagLayout, ttt, "TabToTag");
                        tr.setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                        tr.commit();
                    }
                    else{
                        FragmentTransaction tr = fm.beginTransaction();

                        //tagArrayList에 있는 모든 태그들을 삭제한다
                        tr.remove(fragment);
                        tr.setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                        tr.commit();
                        fm.executePendingTransactions();
                    }
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
