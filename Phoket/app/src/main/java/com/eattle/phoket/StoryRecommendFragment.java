package com.eattle.phoket;

import android.app.Fragment;
import android.content.ComponentCallbacks2;
import android.content.ContentResolver;
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

import com.bumptech.glide.Glide;
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
    String TAG = "storyRecommendFragment";

    int folderID;//현재 보고 있는 스토리의 ID
    static FileSystem fileSystem;
    int randomFolder[];//추천 스토리의 폴더 ID가 들어갈 배열
    int recommendNum = 4;//추천할 스토리의 개수(개수 추가할 경우 story_recommend에 추가해야 함)
    LinearLayout storyRecommend;
    ContentResolver cr;
    private static Context context;
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
        context = getActivity();



        //리스트뷰에 들어갈 스토리를 추가한다
        //랜덤하게 4개의 스토리를 얻어온다(3개는 임의로 정한 것)
        DatabaseHelper db = DatabaseHelper.getInstance(context);
        List<Folder> folders = db.getAllFolders();
        int totalFolderNum=0;
        List<Integer> candidateStory = new ArrayList<Integer>();
        //총 '스토리'의 개수를 구해야 한다.
        for(int i=0;i<folders.size();i++){
            if(folders.get(i).getPicture_num() > CONSTANT.BOUNDARY) {//'일상'이 아니고 '스토리' 일 경우
                totalFolderNum++;
                candidateStory.add(folders.get(i).getId());//'스토리'에 해당하는 folder id를 candidateStory배열에 넣는다.
            }
        }


        if (totalFolderNum <= recommendNum)//총 스토리의 개수가 4개 이하일 때
            recommendNum = totalFolderNum - 1;//현재 보고있는 스토리 제외
        Log.d(TAG, "'일상'이 아닌 '스토리'의 개수 [totalFolderNum]   " + totalFolderNum);
        Log.d(TAG, "추천할 수 있는 사진의 개수 [recommendNum](최대 4개)  : " + recommendNum);

        TextView noRecommend = (TextView) root.findViewById(R.id.noRecommend);
        //if (recommendNum == 0) {//추천할 스토리가 없을 때
        if (recommendNum < 4) {//추천 스토리가 4개가 안되면 (추후변경)
            Log.d(TAG, "추천할 스토리가 없음");
            noRecommend.setVisibility(View.VISIBLE);
            return root;
        } else
            noRecommend.setVisibility(View.GONE);

        randomFolder = new int[recommendNum];
        Random random = new Random();
        for (int count = 0; count < recommendNum; ) {
            int select = candidateStory.get(random.nextInt(totalFolderNum));
            int isOverlapped = 0;
            for (int i = 0; i < count; i++) {//추천 스토리가 중복되지 않도록 한다
                if (select == randomFolder[i] || select == folderID) {//기존에 뽑은것과 중복되거나, 현재 스토리와 동일하면
                    isOverlapped = 1;//마크
                    break;
                }
            }
            Log.d(TAG,"random.nextInt(totalFolderNum) : "+random.nextInt(totalFolderNum)+" select : "+select);
            if (isOverlapped == 0) {//중복되지 않으면
                Folder temp = db.getFolder(select);
                Log.d(TAG,"추천 후보 스토리의 이름 : "+temp.getName()+" 추천 후보 스토리의 사진 개수 : "+temp.getPicture_num());
                if (temp.getName() != null) {////오류가 있는 folderID가 아니면
                    randomFolder[count] = select;
                    count++;
                }
            }
        }

        cr = getActivity().getContentResolver();
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
            //String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/" + "thumbnail" + "/" + folder.getThumbNail_name() + ".jpg";
            //Bitmap bitmap = CONSTANT.decodeSampledBitmapFromPath(path, CONSTANT.screenWidth,300);
            //storyRecommendImage.setImageBitmap(bitmap);

            Glide.with(getActivity())
                    .load(folder.getImage())
//                    .override(CONSTANT.screenWidth, 300)
                    .centerCrop()
                    .into(storyRecommendImage);
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
        getActivity().finish();//현재 띄워져 있던 albumFullActivity 종료
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

    @Override
    public void onStop() {
        Log.d(TAG, "onStop() 호출");
        Glide.get(getActivity()).clearMemory();
        Glide.get(getActivity()).trimMemory(ComponentCallbacks2.TRIM_MEMORY_MODERATE);

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

        System.gc();//garbage collector
        Runtime.getRuntime().gc();//garbage collector
        super.onStop();
    }
}
