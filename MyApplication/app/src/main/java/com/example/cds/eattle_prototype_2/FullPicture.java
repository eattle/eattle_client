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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.cds.eattle_prototype_2.helper.DatabaseHelper;
import com.example.cds.eattle_prototype_2.model.Media;

import java.util.List;


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

        ExtendedViewPager mViewPager = (ExtendedViewPager) findViewById(R.id.view_pager);
        mViewPager.setAdapter(new TouchImageAdapter());
        mViewPager.setCurrentItem(mediaPosition);

    }

    class TouchImageAdapter extends PagerAdapter {

//        private static int[] images = { R.drawable.nature_1, R.drawable.nature_2, R.drawable.nature_3, R.drawable.nature_4, R.drawable.nature_5 };

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

            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager fm = getFragmentManager();
                    Fragment fragment = fm.findFragmentById(R.id.frame);
                    if(fragment == null) {

                        FragmentTransaction tr = fm.beginTransaction();
                        TabToTag ttt = TabToTag.newInstance(m.getId());
                        tr.add(R.id.frame, ttt, "TabToTag");
                        tr.setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                        tr.commit();
                    }
                    else{
                        FragmentTransaction tr = fm.beginTransaction();
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
            Fragment fragment = fm.findFragmentById(R.id.frame);
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
