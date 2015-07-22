package com.eattle.phoket;

import android.content.ComponentCallbacks2;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.eattle.phoket.device.CachedBlockDevice;
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
    private Context context;

    public static StoryMainFragment newInstance(Media m, int position, int mediaListSize) {
        Log.d("StoryMainFragment", "newInstance() 호출(현재 position : " + position + ")");
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
        Log.d("StoryMainFragment", "onCreateView() 호출(현재 position : " + position + ")");

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



        final DatabaseHelper db = DatabaseHelper.getInstance(getActivity());

        if(db.getGuide() == 0 && path == null) {//가이드 사진일 경우
            Glide.with(context)
                    .load(GUIDE.guide_grid(m.getName()))
                    .placeholder(R.mipmap.loading)
                    .into(img);
        }
        else {
            //TODO 사진 경로에 사진이 없을 경우를 체크한다
            //사진은 USB에서 읽어오는 것을 표준으로 한다
            try {
                if (CONSTANT.ISUSBCONNECTED == 1) {//USB가 연결되어 있을 때
                    //Bitmap bm = fileoutimage(m.getName() + ".jpg", CONSTANT.BLOCKDEVICE);
                    //img.setImageBitmap(bm);
                } else {
                    File isExist = new File(path);
                    if (!isExist.exists()) {
                        //사진 파일이 로컬에 존재하지 않고 USB에만 있다고 판단될 때
                        Toast.makeText(getActivity(), "사진이 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
                        //return null;
                    } else {
                        //일단 썸네일을 부르면서 사진 로딩 시작
                        //((AlbumFullActivity) getActivity()).loadBitmap(path, img, m.getId(), imageIdForTaskExecute);
                        Glide.with(context)
                                .load(path)
                                .placeholder(R.mipmap.loading)
                                .thumbnail(0.3f)
                                .into(img);


                    }
                }

            } catch (OutOfMemoryError e) {
                Log.e("warning", "이미지가 너무 큽니다");
            }
        }

        final int _position = position;

        //태그를 불러오기 위한 클릭 리스너
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AlbumFullActivity) getActivity()).pushTabToTag(m, _position);
                ((AlbumFullActivity) getActivity()).setPlacePopup(m);
                if (((AlbumFullActivity) getActivity()).isTagAppeared == 1)
                    ((AlbumFullActivity) getActivity()).isTagAppeared = 0;
                else if (((AlbumFullActivity) getActivity()).isTagAppeared == 0)
                    ((AlbumFullActivity) getActivity()).isTagAppeared = 1;


                if(db != null && db.getGuide() == 0 && GUIDE.GUIDE_STEP == 5){//가이드 도중
                    GUIDE.guide_five(getActivity());
                    //GUIDE.GUIDE_STEP++;
                }
            }
        });

        return root;
    }


    @Override
    public void onStop() {
        Log.d(TAG, "onStop() 호출");
        Glide.get(getActivity()).clearMemory();
        Glide.get(getActivity()).trimMemory(ComponentCallbacks2.TRIM_MEMORY_COMPLETE);

        super.onStop();
    }

    /*
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated() 호출");
        Log.d(TAG, "img == null? "+(img==null));
        Glide.with(this)
                .load(path)
                .thumbnail(0.1f)
                .into((ImageView) getView().findViewById(R.id.pagerImage));
        super.onActivityCreated(savedInstanceState);
    }*/

    private Bitmap fileoutimage(String outString, CachedBlockDevice blockDevice) {//USB -> 스마트폰
        //D  S   X
        //1220879 1870864 2133464

        int result[] = AlbumFullActivity.fileSystem.stringSearch(outString);
        byte[] dummyBuffer = new byte[(int) AlbumFullActivity.fileSystem.CLUSTERSPACESIZE];
        //1866136
        //result[0] = 4096;
        //result[0] = 6505;
        Log.d("xxxxxx", "result[0] " + result[0]);
        if (result[0] == -1) {
            Toast.makeText(getActivity(), "값이 잘못들어왔습니다", Toast.LENGTH_SHORT).show();
            return null;
        } else {

            byte resultbyte[] = new byte[result[4]];
            //int resultstringaddress = 6085;
            int resultstringaddress = result[0];
            //int resultaddress = readIntToBinary(result[0],result[1]+80,LOCATIONSIZE);

            int limit = 0;
            int bytecnt = 0;


            blockDevice.readBlock(resultstringaddress, dummyBuffer);

            while (resultstringaddress != 0) {

                int originalbyteAddress = AlbumFullActivity.fileSystem.readIntToBinary(resultstringaddress, limit, AlbumFullActivity.fileSystem.LOCATIONSIZE, dummyBuffer, blockDevice);

                blockDevice.readBlock(originalbyteAddress, AlbumFullActivity.fileSystem.buffer);
                for (int i = 0; i < AlbumFullActivity.fileSystem.CLUSTERSPACESIZE; i++) {
                    if (bytecnt < result[4]) {
                        resultbyte[bytecnt++] = AlbumFullActivity.fileSystem.buffer[i];
                    } else
                        break;
                }
                if (bytecnt >= result[4])
                    break;

                limit += AlbumFullActivity.fileSystem.LOCATIONSIZE;

                if (limit >= AlbumFullActivity.fileSystem.SPACELOCATION) {
                    resultstringaddress = AlbumFullActivity.fileSystem.readIntToBinary(resultstringaddress, AlbumFullActivity.fileSystem.NEXTLOCATION, AlbumFullActivity.fileSystem.LOCATIONSIZE, dummyBuffer, blockDevice);
                    blockDevice.readBlock(resultstringaddress, dummyBuffer);
                    limit = 0;
                }

            }


            Log.d("xxxxxx", "xxxxxxxxxxxx " + resultbyte);
            Log.d("xxxxxx", "xxxxxxxxxxxxxxxxxxx " + resultbyte.length);

            Toast.makeText(getActivity(), "1 " + resultbyte, Toast.LENGTH_SHORT).show();
            Toast.makeText(getActivity(), "1 " + resultbyte.length, Toast.LENGTH_SHORT).show();

            Bitmap byteimage = BitmapFactory.decodeByteArray(resultbyte, 0, resultbyte.length);
            //imageView.setImageBitmap(byteimage);

            //imageView.setImageBitmap(resizeBitmapImageFn(byteimage,540));

            //Bitmap bitmap1 = BitmapFactory.decodeFile("/storage/emulated/0/DCIM/Camera/1.jpg");
            //imageView.setImageBitmap(resizeBitmapImageFn(bitmap1,540));
            return byteimage;
        }
    }

}