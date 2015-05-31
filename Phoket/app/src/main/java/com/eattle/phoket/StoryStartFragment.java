package com.eattle.phoket;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by dh_st_000 on 2015-05-21.
 */
public class StoryStartFragment extends Fragment {//'스토리시작'을 눌렀을 때 맨처음 화면

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
        View root = inflater.inflate(R.layout.story_start, container, false);
        Bundle args = getArguments();

        String titleImagePath = args.getString("titleImagePath");
        String titleName = args.getString("titleName");
        int kind = args.getInt("kind");
        int position = args.getInt("position");
        //대표 이미지
        try {
            ImageView storyStartImage = (ImageView) root.findViewById(R.id.storyStartImage);

            //화면 크기, 사진 크기에 따라 사진을 최적화 한다
            Bitmap changedBitmap = CONSTANT.decodeSampledBitmapFromPath(titleImagePath, CONSTANT.screenWidth, CONSTANT.screenHeight);
            storyStartImage.setImageBitmap(changedBitmap);

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

        if(position != -1){
            storyStartDate.setVisibility(View.INVISIBLE);
            storyStartTitle.setVisibility(View.INVISIBLE);
        }

        return root;
    }

}
