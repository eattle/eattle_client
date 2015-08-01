package com.eattle.phoket;

import android.app.Fragment;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.eattle.phoket.helper.DatabaseHelper;

/**
 * Created by dh_st_000 on 2015-05-21.
 */
public class StoryStartFragment extends Fragment {//'스토리시작'을 눌렀을 때 맨처음 화면
    private String TAG = "StoryStartFragment";

    ImageView blurImage;
    ImageView backImage;
    ImageView filterImage;
    private int position;
    private static Context context;
    public static StoryStartFragment newInstance(String titleImagePath, String titleName, int kind, int position) {

        StoryStartFragment fragment = new StoryStartFragment();
        Bundle args = new Bundle();
        args.putString("titleImagePath", titleImagePath);
        args.putString("titleName", titleName);
        args.putInt("kind", kind);
        args.putInt("position", position);
        fragment.setArguments(args);
        return fragment;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView() 호출");
        View root = inflater.inflate(R.layout.story_start, container, false);
        Bundle args = getArguments();

        String titleImagePath = args.getString("titleImagePath");
        String titleName = args.getString("titleName");
        int kind = args.getInt("kind");
        position = args.getInt("position");
        context = getActivity();
        //대표 이미지
        try {
            ImageView storyStartImage = (ImageView) root.findViewById(R.id.storyStartImage);

            //화면 크기, 사진 크기에 따라 사진을 최적화 한다
            //Bitmap changedBitmap = CONSTANT.decodeSampledBitmapFromPath(titleImagePath, CONSTANT.screenWidth, CONSTANT.screenHeight);
            //storyStartImage.setImageBitmap(changedBitmap);
            DatabaseHelper db = DatabaseHelper.getInstance(context);
            if(db.getGuide() == 0) {//가이드 사진일 경우
                Glide.with(context)
                        .load(R.mipmap.phoket1)
                        .into(storyStartImage);
            }
            else {
                Glide.with(context)
                        .load(titleImagePath)
                        .thumbnail(0.5f)
                        .into(storyStartImage);
            }
        } catch (OutOfMemoryError e) {
            Log.e("warning", "이미지가 너무 큽니다");
        }


        //날짜
        TextView storyStartDate = (TextView) root.findViewById(R.id.storyStartDate);
        //제목
        TextView storyStartTitle = (TextView) root.findViewById(R.id.storyStartTitle);

        if (kind == CONSTANT.FOLDER) {
            storyStartDate.setText(CONSTANT.convertFolderNameToDate(titleName));
            storyStartTitle.setText(CONSTANT.convertFolderNameToStoryName(titleName));
        } else if (kind == CONSTANT.DEFAULT_TAG || kind == CONSTANT.TAG) {
            storyStartDate.setText("");
            storyStartTitle.setText(titleName);
        }


        blurImage = (ImageView)root.findViewById(R.id.blurImage);
        setGrayScale(blurImage);
        backImage = (ImageView)root.findViewById(R.id.storyStartImage);
        applyBlur();
        filterImage = (ImageView)root.findViewById(R.id.filterImage);


        if(position != -1){
            storyStartDate.setVisibility(View.INVISIBLE);
            storyStartTitle.setVisibility(View.INVISIBLE);
        }

        return root;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated() 호출");
    }
    private void applyBlur() {
        backImage.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                backImage.getViewTreeObserver().removeOnPreDrawListener(this);
                backImage.buildDrawingCache();

                Bitmap bmp = backImage.getDrawingCache();
                blur(bmp, blurImage);
                return true;
            }
        });
    }

    private void blur(Bitmap bkg, ImageView view) {
        long startMs = System.currentTimeMillis();
        float scaleFactor = 8;
        float radius = 6;

//        Bitmap overlay = Bitmap.createBitmap((int) (view.getMeasuredWidth() / scaleFactor),
//                (int) (view.getMeasuredHeight() / scaleFactor), Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(overlay);
//        canvas.translate(-view.getLeft() / scaleFactor, -view.getTop() / scaleFactor);
//        canvas.scale(1 / scaleFactor, 1 / scaleFactor);
//        Paint paint = new Paint();
//        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
//        canvas.drawBitmap(bkg, 0, 0, paint);

//        overlay = FastBlur.doBlur(overlay, (int)radius, true);
//        Log.d("asdfasdf","블러에 사용되는 바이트 : "+overlay.getByteCount());
//        view.setImageDrawable(new BitmapDrawable(getResources(), overlay));
        view.setAlpha(0.0f);

        /*
        Glide.with(this)
                .
                .placeHolder(new BitmapDrawable(getResources(), overlay))
                .into(view);
        */


        if(position != -1){
            showBlur(1.0f);
        }



        Log.d("Blur", System.currentTimeMillis() - startMs + "ms");
    }

    public void setGrayScale(ImageView v){
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);                        //0이면 grayscale
        ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);
        v.setColorFilter(cf);
    }


    public void showBlur(float positionOffset){

        blurImage.setAlpha(1.0f*positionOffset);
        filterImage.setAlpha(0.5f*positionOffset + 0.2f);
    }
    public void showBlur(){
        blurImage.setAlpha(1.0f);
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop() 호출");
        Glide.get(context).clearMemory();
        Glide.get(context).trimMemory(ComponentCallbacks2.TRIM_MEMORY_COMPLETE);

//        if (backImage != null) {
//            for(int i=0;i<3;i++) {
//                Drawable d = null;
//                if(i==0)
//                    d = backImage.getDrawable();
//                else if(i==1)
//                    d = blurImage.getDrawable();
//                else if(i==2)
//                    d = filterImage.getDrawable();
//
//                if (d instanceof BitmapDrawable) {
//                    Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
//                    if (bitmap != null && !bitmap.isRecycled()) {
//                        Log.d("StoryMainFragment", "[onStop]에서 " + bitmap.getByteCount() + "만큼 recycle() & gc() 호출");
//                        backImage.setImageBitmap(null);
//                        bitmap.recycle();
//                        bitmap = null;
//                        d.setCallback(null);
//                    }
//                }
//            }
//        }
//
//        System.gc();//garbage collector
//        Runtime.getRuntime().gc();//garbage collector
        super.onStop();
    }
}
