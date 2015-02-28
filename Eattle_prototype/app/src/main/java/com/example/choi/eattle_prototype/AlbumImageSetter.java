package com.example.choi.eattle_prototype;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

/**
 * Created by GA on 2015. 2. 15..
 */
public class AlbumImageSetter {
    Cursor mCursor;
    ContentResolver mCr;
    Context context;

    public AlbumImageSetter(Context context, long startTime, long endTime){
        this.context = context;
        mCr = context.getContentResolver();
        setCursor(startTime, endTime);
    }

    public void changeTime(long startTime, long endTime){
        setCursor(startTime, endTime);
    }
    public void changeCursorToLast(){mCursor.moveToLast();}

    void setCursor(long startTime, long endTime){
        //mCursor position : -1
        //Toast.makeText(MainActivity.this, mCursor.getPosition() + "", Toast.LENGTH_SHORT).show();
        //Initialize Position to zero
        mCursor = mCr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Images.ImageColumns.DATA}, MediaStore.Images.ImageColumns.DATE_ADDED + " BETWEEN " + startTime + " AND " + endTime, null, MediaStore.Images.ImageColumns.DATE_ADDED+" ASC");
//        mCursor = mCr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Images.ImageColumns.DATE_ADDED+" DESC");
        mCursor.moveToFirst();
    }

    boolean setImage(ImageView image){
        if(mCursor.isAfterLast())   return false;
        if(mCursor.isBeforeFirst()) return false;
        String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
        try {
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inSampleSize = 4;
            Bitmap bm = BitmapFactory.decodeFile(path, opt);
            image.setImageBitmap(bm);
        } catch (OutOfMemoryError e) {
            Log.e("warning", "이미지가 너무 큽니다");
        }

        mCursor.moveToNext();
        return true;
    }

    boolean isOver(){
        if(mCursor.isAfterLast())   return true;
        else                        return false;
    }
    boolean isStart(){
        if(mCursor.isBeforeFirst())   return true;
        else                        return false;
    }


    void setCursorPrev(int num){
        int position = mCursor.getPosition() - num;
        mCursor.moveToPosition(position);
    }

}

