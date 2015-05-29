package com.eattle.phoket;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.v7.internal.widget.ViewUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by dh_st_000 on 2015-05-21.
 */
public class StoryStartFragment extends Fragment {//'스토리시작'을 눌렀을 때 맨처음 화면

    ImageView blurImage;
    ImageView backImage;

    public static StoryStartFragment newInstance(String titleImagePath, String titleName, int kind) {
        StoryStartFragment fragment = new StoryStartFragment();
        Bundle args = new Bundle();
        args.putString("titleImagePath", titleImagePath);
        args.putString("titleName", titleName);
        args.putInt("kind", kind);
        fragment.setArguments(args);
        return fragment;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.story_start, container, false);
        Bundle args = getArguments();

        String titleImagePath = args.getString("titleImagePath");
        String titleName = args.getString("titleName");
        int kind = args.getInt("kind");
        //대표 이미지
        try {
            ImageView storyStartImage = (ImageView) root.findViewById(R.id.storyStartImage);
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inSampleSize = 1;
            Bitmap bm = BitmapFactory.decodeFile(titleImagePath, opt);
            //bm = CONSTANT.blur(getActivity(), bm, 25.0f);//blur효과
            storyStartImage.setImageBitmap(bm);

        } catch (OutOfMemoryError e) {
            Log.e("warning", "이미지가 너무 큽니다");
        }

        //날짜
        TextView storyStartDate = (TextView) root.findViewById(R.id.storyStartDate);


        //제목
        TextView storyStartTitle = (TextView) root.findViewById(R.id.storyStartTitle);

        if(kind == CONSTANT.FOLDER){
            storyStartDate.setText(CONSTANT.convertFolderNameToDate(titleName));
            storyStartTitle.setText(CONSTANT.convertFolderNameToStoryName(titleName));
        }
        else if(kind == CONSTANT.DEFAULT_TAG || kind == CONSTANT.TAG) {
            storyStartDate.setText("");
            storyStartTitle.setText(titleName);
        }

        blurImage = (ImageView)root.findViewById(R.id.blurImage);
        backImage = (ImageView)root.findViewById(R.id.storyStartImage);
        applyBlur();

        return root;
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

    private void blur(Bitmap bkg, View view) {
        long startMs = System.currentTimeMillis();
        float scaleFactor = 8;
        float radius = 2;

        Bitmap overlay = Bitmap.createBitmap((int) (view.getMeasuredWidth() / scaleFactor),
                (int) (view.getMeasuredHeight() / scaleFactor), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(overlay);
        canvas.translate(-view.getLeft()/scaleFactor, -view.getTop()/scaleFactor);
        canvas.scale(1 / scaleFactor, 1 / scaleFactor);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(bkg, 0, 0, paint);

        overlay = FastBlur.doBlur(overlay, (int)radius, true);
        view.setBackground(new BitmapDrawable(getResources(), overlay));
        //view.setBackgroundColor(0x00000000);
        Log.d("Blur", System.currentTimeMillis() - startMs + "ms");
    }

}
