package com.eattle.phoket;

import android.content.ComponentCallbacks2;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.eattle.phoket.helper.DatabaseHelper;
import com.eattle.phoket.model.Media;
import com.eattle.phoket.view.TouchImageView;

import java.io.File;

/**
 * Created by dh_st_000 on 2015-05-29.
 */

public class StoryMainFragment extends android.support.v4.app.Fragment {
    private String TAG = "StroyMainFragment";

    //private TouchImageView img;
    private int position;
    private String path = "";
    private Media m;
    public int imageIdForTaskExecute = CONSTANT.COUNTIMAGE++;//imageview객체마다 고유의 아이디를 부여한다(task 중복 실행을 방지하기 위해)
    private static Context context;

    public static StoryMainFragment newInstance(Media m, int position, int mediaListSize) {
        final StoryMainFragment fragment = new StoryMainFragment();
        Bundle args = new Bundle();
        args.putParcelable("m", m);
        args.putInt("position", position);
        args.putInt("mediaListSize", mediaListSize);
        fragment.setArguments(args);

        return fragment;
    }

    public StoryMainFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getActivity();//꼭 여기서 해줘야함
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View root = inflater.inflate(R.layout.story_main, container, false);

        Bundle args = getArguments();
        m = args.getParcelable("m");
        position = args.getInt("position");
        final int mediaListSize = args.getInt("mediaListSize");



        if (position == -1 || position == mediaListSize)//제목화면 또는 추천스토리 부분은 아무것도 안함(onPageSelected에서 해줌)
            return root;//아무것도 설정하지 않은 fragment를 반환(//배경사진 fragment만 보이게 한다 또는 추천스토리 fragment만 보이게 한다)
        FrameLayout frameLayout = (FrameLayout) root.findViewById(R.id.storyMain);
        final TouchImageView img = (TouchImageView) root.findViewById(R.id.pagerImage);
        path = m.getPath();//사진의 경로를 가져온다


        final DatabaseHelper db = DatabaseHelper.getInstance(context);


        if (db.getGuide() == 0 && path == null) {//가이드 사진일 경우
            Glide.with(context)
                    .load(GUIDE.guide_grid(m.getName()))
                    .placeholder(R.mipmap.loading)
                    .into(img);
        } else {

            //TODO 사진 경로에 사진이 없을 경우를 체크한다
            //사진은 USB에서 읽어오는 것을 표준으로 한다
            try {
                File isExist = new File(path);

                if (!isExist.exists()) {
                    //사진 파일이 로컬에 존재하지 않고 USB에만 있다고 판단될 때
                    Toast.makeText(context, "사진이 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
                    //return null;
                } else {

                    //일단 썸네일을 부르면서 사진 로딩 시작
                    //((AlbumFullActivity) getActivity()).loadBitmap(path, img, m.getId(), imageIdForTaskExecute);
                    Glide.with(context)
                            .load(path)
                            .placeholder(R.mipmap.loading)
                            .into(img);
                }
            } catch (OutOfMemoryError e) {
                Log.e("warning", e.getMessage());
            }
        }


        final int _position = position;

        //태그를 불러오기 위한 클릭 리스너
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (db != null && db.getGuide() == 0) {
                    if (GUIDE.GUIDE_STEP != 5) {
                        return;
                    }
                    GUIDE.guide_five(context);
                }

                ((AlbumFullActivity) context).pushTabToTag(m, _position);
                ((AlbumFullActivity) context).setPlacePopup(m);
                if (((AlbumFullActivity) context).isTagAppeared == 1)
                    ((AlbumFullActivity) context).isTagAppeared = 0;
                else if (((AlbumFullActivity) context).isTagAppeared == 0)
                    ((AlbumFullActivity) context).isTagAppeared = 1;


            }
        });

        return root;
    }


    @Override
    public void onStop() {
        Glide.get(context).clearMemory();
        Glide.get(context).trimMemory(ComponentCallbacks2.TRIM_MEMORY_COMPLETE);

        super.onStop();
    }
}