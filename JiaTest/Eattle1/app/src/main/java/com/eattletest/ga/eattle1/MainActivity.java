package com.eattletest.ga.eattle1;

import android.content.*;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore.*;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import static android.view.GestureDetector.*;


public class MainActivity extends ActionBarActivity {
    final static int ACT_VIEW = 0;

    final static int MAX_IMAGE_PER_PAGE = 3;
    final static int MIN_VELOCITY = 300;
    final static int MIN_DISTANCE = 200;
    ImageView [] mImages = new ImageView[MAX_IMAGE_PER_PAGE];
    String [] mImagesPath = new String[MAX_IMAGE_PER_PAGE];
    Cursor mCursor;
    ContentResolver mCr;
    GestureDetector mDetector;

    int mNowImageNum;
    int mMaxImageNum;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Up to MAX_IMAGE_PER_PAGE
        mImages[0] = (ImageView)findViewById(R.id.imageView1);
        mImages[1] = (ImageView)findViewById(R.id.imageView2);
        mImages[2] = (ImageView)findViewById(R.id.imageView3);

        mCr = getContentResolver();
        mDetector = new GestureDetector(this, mGestureListener);
        mDetector.setIsLongpressEnabled(false);
        //Initialize Cursor from ContentResolver
        setCursor();
        Toast.makeText(MainActivity.this, mCursor.getPosition() + " oncreate " +mNowImageNum, Toast.LENGTH_SHORT).show();

        //Set Images from Cursor
        setImages();
/*
        //register BR
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
        filter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        filter.addAction(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        filter.addDataScheme("file");
        registerReceiver(mScanReceiver, filter);*/
    }
/*
    BroadcastReceiver mScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setCursor();
            setImages();
        }
    };

    public void onDestroy(){
        super.onDestroy();
        //unregister BR
        unregisterReceiver(mScanReceiver);
    }

*/
    public void mOnClickImage(View v){
        Intent intent = new Intent(this, ViewPhotoActivity.class);
        switch (v.getId()) {
            case R.id.imageView1:
                intent.putExtra("ImagePath", mImagesPath[0]);
                break;
            case R.id.imageView2:
                intent.putExtra("ImagePath", mImagesPath[1]);
                break;
            case R.id.imageView3:
                intent.putExtra("ImagePath", mImagesPath[2]);
                break;
        }
        startActivity(intent);
    }

    public boolean onTouchEvent(MotionEvent event){
        return mDetector.onTouchEvent(event);
    }

    OnGestureListener mGestureListener = new OnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if(Math.abs(velocityX) > MIN_VELOCITY){
                if(e1.getX() - e2.getX() > MIN_DISTANCE){
                    //다음장
                    setImages();
//                    Toast.makeText(MainActivity.this, "왼쪽 스크롤  " + velocityX, Toast.LENGTH_SHORT).show();
                }
                if(e2.getX() - e1.getX() > MIN_DISTANCE){
                    //이전장
                    mNowImageNum -= MAX_IMAGE_PER_PAGE*2;
                    setImages();
//                    Toast.makeText(MainActivity.this, "오른쪽 스크롤   " + velocityX, Toast.LENGTH_SHORT).show();
                }
            }
            return true;
        }
    };


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

    void setCursor(){
        //mCursor position : -1
        //Toast.makeText(MainActivity.this, mCursor.getPosition() + "", Toast.LENGTH_SHORT).show();
        //Initialize Position to zero
        mCursor = mCr.query(Images.Media.EXTERNAL_CONTENT_URI, null, null, null, Images.ImageColumns.DATE_ADDED+" DESC");
        mNowImageNum = 0;
        mMaxImageNum = mCursor.getCount();
        mCursor.moveToPosition(mNowImageNum);
    }

    void setImages(){
        if(mNowImageNum <= 0)   mNowImageNum = 0;

        for(int i = 0; i < MAX_IMAGE_PER_PAGE; i++, mNowImageNum++){
            mCursor.moveToPosition(mNowImageNum);

            mImagesPath[i] = mCursor.getString(mCursor.getColumnIndex(Images.ImageColumns.DATA));
            try {
                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inSampleSize = 4;
                Bitmap bm = BitmapFactory.decodeFile(mImagesPath[i], opt);
                mImages[i].setImageBitmap(bm);
            } catch (OutOfMemoryError e) {
                Toast.makeText(MainActivity.this, "이미지가 너무 큽니다", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
