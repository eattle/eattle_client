package com.eattle.phoket;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.eattle.phoket.helper.DatabaseHelper;
import com.eattle.phoket.model.Folder;
import com.eattle.phoket.model.Media;

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
        Bundle args = getArguments();
        if (args != null)
            folderID = args.getInt("folderID");
        db = DatabaseHelper.getInstance(getActivity());

        //리스트뷰에 들어갈 스토리를 추가한다
        //랜덤하게 4개의 스토리를 얻어온다(3개는 임의로 정한 것)
        List<Folder> folders = db.getAllFolders();
        int totalFolderNum = folders.size();
        Log.d("asdfasdf", "asdfasdfasdfasdfasdf   " + totalFolderNum);

        int recommendNum = 4;//추천할 스토리의 개수(개수 추가할 경우 story_recommend에 추가해야 함)
        if (totalFolderNum < recommendNum)//총 스토리의 개수가 4개가 안될떄
            recommendNum = totalFolderNum - 1;//현재 보고있는 스토리 제외
        Log.d("asdfasdf", "asdfasdfasdfasdfasdf   " + recommendNum);

        TextView noRecommend = (TextView) root.findViewById(R.id.noRecommend);
        if (recommendNum == 0) {//추천할 스토리가 없을 때
            Log.d("asdfasdf", "asdfasdfasdfasdfasdf");
            noRecommend.setVisibility(View.VISIBLE);
            return root;
        } else
            noRecommend.setVisibility(View.GONE);

        randomFolder = new int[recommendNum];
        Random random = new Random();
        for (int count = 0; count < recommendNum; ) {
            int select = random.nextInt(totalFolderNum - 1);
            int isOverlapped = 0;
            for (int i = 0; i < count; i++) {//추천 스토리가 중복되지 않도록 한다
                if (select == randomFolder[i] || select == folderID) {//기존에 뽑은것과 중복되거나, 현재 스토리와 동일하면
                    isOverlapped = 1;//마크
                    break;
                }
            }
            if (isOverlapped == 0) {//중복되지 않으면
                //TODO 실제로는 없는 스토리인데 folderDB에 등록된 것이 있는듯!!
                Folder temp = db.getFolder(select);//오류가 있는 folderID인지 확인한다
                if (temp.getName() != null) {
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
            storyRecommendImage.setImageURI(Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/" + "thumbnail" + "/" + folder.getThumbNail_name() + ".jpg"));
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

    public void onRecommendClick(int num) {
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
}
