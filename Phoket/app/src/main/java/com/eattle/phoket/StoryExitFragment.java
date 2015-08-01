package com.eattle.phoket;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by dh_st_000 on 2015-05-22.
 */
public class StoryExitFragment extends Fragment {
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_exit_story, container, false);
        ImageView exitStory = (ImageView) root.findViewById(R.id.exitStory);
        exitStory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                clearMemory();
                getActivity().finish();
            }
        });
        return root;
    }

//    //불필요한 메모리 정리---------------------------------------------------------------
//    private void clearMemory() {
//        AlbumFullActivity.mViewPager = null;
//        AlbumFullActivity.touchImageAdapter = null;
//
//        CONSTANT.releaseImageMemory((ImageView) getActivity().findViewById(R.id.storyStartImage));
//        CONSTANT.releaseImageMemory((ImageView)getActivity().findViewById(R.id.blurImage));
//        //아직 스토리에 남아있는 사진 삭제
//
//        //아직 스토리에 남아있는 사진 삭제
//        while(AlbumFullActivity.viewPagerImage.size() > 0){
//            Log.d("TagsOverAlbum","아직 남아있는 사진의 개수 : "+AlbumFullActivity.viewPagerImage.size());
//            ImageView temp = AlbumFullActivity.viewPagerImage.get(0);
//            AlbumFullActivity.viewPagerImage.remove(0);
//            CONSTANT.releaseImageMemory(temp);
//
//            if(AlbumFullActivity.viewPagerImage.size() == 0) {
//                Log.d("TagsOverAlbum","break!");
//
//                break;
//            }
//        }
//
//        System.gc();//garbage collector
//        Runtime.getRuntime().gc();//garbage collector
//        getActivity().finish();//현재 띄워져 있던 albumFullActivity 종료(메모리 확보를 위해)
//        //-----------------------------------------------------------------------------------
//    }
}
