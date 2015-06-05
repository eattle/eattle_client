package com.eattle.phoket;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.eattle.phoket.helper.DatabaseHelper;
import com.eattle.phoket.model.Folder;
import com.eattle.phoket.model.Media;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by dh_st_000 on 2015-05-23.
 */
public class StoryRecommendFragment extends Fragment {
    DatabaseHelper db;
    int folderID;//현재 보고 있는 스토리의 ID
    static FileSystem fileSystem;
    int randomFolder[];//추천 스토리의 폴더 ID가 들어갈 배열
    int recommendNum = 4;//추천할 스토리의 개수(개수 추가할 경우 story_recommend에 추가해야 함)
    LinearLayout storyRecommend;
    public static StoryRecommendFragment newInstance(int folderID) {

        fileSystem = FileSystem.getInstance();

        StoryRecommendFragment storyRecommendFragment = new StoryRecommendFragment();

        Bundle args = new Bundle();
        args.putInt("folderID", folderID);
        storyRecommendFragment.setArguments(args);

        return storyRecommendFragment;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.story_recommend, container, false);
        storyRecommend = (LinearLayout)root;
        Bundle args = getArguments();
        if (args != null)
            folderID = args.getInt("folderID");
        db = DatabaseHelper.getInstance(getActivity());

        //리스트뷰에 들어갈 스토리를 추가한다
        //랜덤하게 4개의 스토리를 얻어온다(3개는 임의로 정한 것)
        List<Folder> folders = db.getAllFolders();
        int totalFolderNum = folders.size();
        Log.d("storyRecommendFragment", "[totalFolderNum]   " + totalFolderNum);


        if (totalFolderNum < recommendNum)//총 스토리의 개수가 4개가 안될떄
            recommendNum = totalFolderNum - 1;//현재 보고있는 스토리 제외
        Log.d("storyRecommendFragment", "[recommendNum]   " + recommendNum);

        TextView noRecommend = (TextView) root.findViewById(R.id.noRecommend);
        //if (recommendNum == 0) {//추천할 스토리가 없을 때
        if (recommendNum < 4) {//추천 스토리가 4개가 안되면 (추후변경)
            Log.d("storyRecommendFragment", "추천할 스토리가 없음");
            noRecommend.setVisibility(View.VISIBLE);
            return root;
        } else
            noRecommend.setVisibility(View.GONE);

        randomFolder = new int[recommendNum];
        Random random = new Random();
        for (int count = 0; count < recommendNum; ) {
            int select = random.nextInt(totalFolderNum);
            int isOverlapped = 0;
            for (int i = 0; i < count; i++) {//추천 스토리가 중복되지 않도록 한다
                if (select == randomFolder[i] || select == folderID) {//기존에 뽑은것과 중복되거나, 현재 스토리와 동일하면
                    isOverlapped = 1;//마크
                    break;
                }
            }
            if (isOverlapped == 0) {//중복되지 않으면
                //TODO 실제로는 없는 스토리인데 folderDB에 등록된 것이 있는듯!!
                Folder temp = db.getFolder(select);
                if (temp.getName() != null && temp.getPicture_num() > 3) {////오류가 있는 folderID || 일상이 아니면
                    randomFolder[count] = select;
                    count++;
                }
            }
        }
        for (int i = 0; i < recommendNum; i++) {
            Folder folder = db.getFolder(randomFolder[i]);
            ImageView storyRecommendImage = null;
            TextView storyRecommendTitle = null;
            switch (i) {
                case 0:
                    storyRecommendImage = (ImageView) root.findViewById(R.id.firstImage);
                    storyRecommendTitle = (TextView) root.findViewById(R.id.firstText);
                    break;
                case 1:
                    storyRecommendImage = (ImageView) root.findViewById(R.id.secondImage);
                    storyRecommendTitle = (TextView) root.findViewById(R.id.secondText);
                    break;
                case 2:
                    storyRecommendImage = (ImageView) root.findViewById(R.id.thirdImage);
                    storyRecommendTitle = (TextView) root.findViewById(R.id.thirdText);
                    break;
                case 3:
                    storyRecommendImage = (ImageView) root.findViewById(R.id.fourthImage);
                    storyRecommendTitle = (TextView) root.findViewById(R.id.fourthText);
                    break;
            }
            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/" + "thumbnail" + "/" + folder.getThumbNail_name() + ".jpg";
            Bitmap bitmap = CONSTANT.decodeSampledBitmapFromPath(path, CONSTANT.screenWidth,300);
            storyRecommendImage.setImageBitmap(bitmap);

            storyRecommendTitle.setText(CONSTANT.convertFolderNameToStoryName(folder.getName()));

            final int i_ = i;
            storyRecommendTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onRecommendClick(i_);
                }
            });

        }

        return root;
    }
    //백버튼을 눌렀을 때, 메모리 정리를 한다

    public void onRecommendClick(int num) {
        //불필요한 메모리 정리---------------------------------------------------------------
        AlbumFullActivity.mViewPager = null;
        AlbumFullActivity.touchImageAdapter = null;

        CONSTANT.releaseImageMemory((ImageView) getActivity().findViewById(R.id.storyStartImage));
        CONSTANT.releaseImageMemory((ImageView) getActivity().findViewById(R.id.blurImage));
        //추천 이미지 삭제
        CONSTANT.releaseImageMemory((ImageView) getActivity().findViewById(R.id.firstImage));
        CONSTANT.releaseImageMemory((ImageView) getActivity().findViewById(R.id.secondImage));
        CONSTANT.releaseImageMemory((ImageView) getActivity().findViewById(R.id.thirdImage));
        CONSTANT.releaseImageMemory((ImageView) getActivity().findViewById(R.id.fourthImage));
        //아직 스토리에 남아있는 사진 삭제
        while(AlbumFullActivity.viewPagerImage.size() > 0){
            Log.d("TagsOverAlbum","아직 남아있는 사진의 개수 : "+AlbumFullActivity.viewPagerImage.size());
            ImageView temp = AlbumFullActivity.viewPagerImage.get(0);
            AlbumFullActivity.viewPagerImage.remove(0);
            CONSTANT.releaseImageMemory(temp);

            if(AlbumFullActivity.viewPagerImage.size() == 0) {
                Log.d("TagsOverAlbum","break!");

                break;
            }
        }
        System.gc();//garbage collector
        Runtime.getRuntime().gc();//garbage collector
        getActivity().finish();//현재 띄워져 있던 albumFullActivity 종료(메모리 확보를 위해)
        //-----------------------------------------------------------------------------------
        Intent intent = new Intent(getActivity(), AlbumGridActivity.class);
        intent.putExtra("kind", CONSTANT.FOLDER);
        switch (num) {
            case 0:
                intent.putExtra("id", randomFolder[0]);
                break;
            case 1:
                intent.putExtra("id", randomFolder[1]);
                break;
            case 2:
                intent.putExtra("id", randomFolder[2]);
                break;
            case 3:
                intent.putExtra("id", randomFolder[3]);
                break;
        }
        getActivity().startActivity(intent);
    }
    public void showBlur(float positionOffset){
        if(storyRecommend.getAlpha() >= 1.0f)
            return;

        storyRecommend.setAlpha(1.0f*positionOffset);
        storyRecommend.setAlpha(0.5f*positionOffset + 0.2f);
    }
}
