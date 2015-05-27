package com.eattle.phoket;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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

    public static StoryStartFragment newInstance(String titleImagePath, String titleName) {
        StoryStartFragment fragment = new StoryStartFragment();
        Bundle args = new Bundle();
        args.putString("titleImagePath", titleImagePath);
        args.putString("titleName", titleName);
        fragment.setArguments(args);
        return fragment;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.story_start, container, false);
        Bundle args = getArguments();

        String titleImagePath = args.getString("titleImagePath");
        String titleName = args.getString("titleName");
        //대표 이미지
        try {
            ImageView storyStartImage = (ImageView) root.findViewById(R.id.storyStartImage);
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inSampleSize = 1;
            Bitmap bm = BitmapFactory.decodeFile(titleImagePath, opt);
            storyStartImage.setImageBitmap(bm);
        } catch (OutOfMemoryError e) {
            Log.e("warning", "이미지가 너무 큽니다");
        }

        //날짜
        TextView storyStartDate = (TextView) root.findViewById(R.id.storyStartDate);
        storyStartDate.setText(CONSTANT.convertFolderNameToDate(titleName));

        //제목
        TextView storyStartTitle = (TextView) root.findViewById(R.id.storyStartTitle);
        storyStartTitle.setText(CONSTANT.convertFolderNameToStoryName(titleName));


        return root;
    }
}
