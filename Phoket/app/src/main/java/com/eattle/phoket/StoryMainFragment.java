package com.eattle.phoket;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.eattle.phoket.device.CachedBlockDevice;
import com.eattle.phoket.model.Media;

import java.io.File;

/**
 * Created by dh_st_000 on 2015-05-29.
 */

public class StoryMainFragment extends android.support.v4.app.Fragment {
    //private TouchImageView img;
    private TouchImageView img;
    private int position;
    private String path = "";
    private Media m;
    private int isBitmapTaskExecuted = 0;//1이면 bitmapWorkerTask.execute()가 실행된것
    private int smallOrLarge = 0;//0이면 작은 이미지가 로드된 상태, 1이면 큰 이미지가 로드된 상태
    AlbumFullActivity.BitmapWorkerTask bitmapWorkerTask;
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

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("StoryMainFragment", "onCreateView() 호출(현재 position : " + position + ")");
        final View root = inflater.inflate(R.layout.story_main, container, false);
        //if (isRecycled == 1) {//이미 한번 recycle()이 호출되었다면
            //img = null;//새로 만들어준다.
        //}
        Bundle args = getArguments();
        m = args.getParcelable("m");
        position = args.getInt("position");
        final int mediaListSize = args.getInt("mediaListSize");

        if (position == -1 || position == mediaListSize)//제목화면 또는 추천스토리 부분은 아무것도 안함(onPageSelected에서 해줌)
            return root;//아무것도 설정하지 않은 fragment를 반환(//배경사진 fragment만 보이게 한다 또는 추천스토리 fragment만 보이게 한다)
        FrameLayout frameLayout = (FrameLayout) root.findViewById(R.id.storyMain);
        img = (TouchImageView) root.findViewById(R.id.pagerImage);
        //RecyclingImageView img_ = (RecyclingImageView)root.findViewById(R.id.pagerImage);
        path = m.getPath();//사진의 경로를 가져온다

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
                    Toast.makeText(getActivity(), "사진이 존재하지 않습니다. USB를 연결하세요", Toast.LENGTH_SHORT).show();
                    //return null;
                } else {
                    //화면 크기, 사진 크기에 따라 사진을 최적화 한다


                    //일단 작은 사진을 부른다
                    bitmapWorkerTask = ((AlbumFullActivity) getActivity()).loadBitmap(path, img,m.getId());



                    //한페이지에 오래 머물러 있으면 큰사진 로딩
                    //CONSTANT.ImagePathAndImageView imageInfoForLoading = new CONSTANT.ImagePathAndImageView(path,img);
                    //CONSTANT.currentImageInfo.add(imageInfoForLoading);//큰사진은 바로 로딩하지 않는다
                }
            }

        } catch (OutOfMemoryError e) {
            Log.e("warning", "이미지가 너무 큽니다");
        }
        //frameLayout.addView(img, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

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
            }
        });
        AlbumFullActivity.viewPagerImage.add(img);

        return root;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d("setUserVisibleHint", "현재 position 값 : " + position + " path : " + path + " img : " + img);

        Log.d("setUserVisibleHint", "CONSTANT.currentImageView 값 : " + img);


        if (isVisibleToUser) {
            CONSTANT.currentImageView = img;//CONSTANT.currentImageView는 현재 보고 있는 이미지를 가리킨다(페이지를 넘길때마다 실시간으로 계속 바뀜)
            if(bitmapWorkerTask == null){
                Log.d("setUserVisibleHint", "BitmapWorkerTask값이 null입니다!!!!!!!!!!!!!이럼 안되는데!!!" );
                bitmapWorkerTask = AlbumFullActivity.getBitmapWorkerTask(img);
            }



            //일단 0.5초동안 대기한다
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
/*
                    ((AlbumFullActivity) getActivity()).loadBitmap(path, img);

 */
                    if (img != CONSTANT.currentImageView) {//현재 보고 있는 뷰와 로드하려는 이미지가 다를 경우
                        //큰 이미지를 로드하지 않는다.(아무것도 하지 않음)
                    }
                    //여기에 딜레이 후 시작할 작업들을 입력
                    else if (img != null && path != null) {//0.5초후엔 img에 1/10 샘플이 채워져 있을 것
                        //CONSTANT.previousBigImageView = img;//큰 이미지로 로드되는 뷰를 저장해둔다.
                        if (bitmapWorkerTask != null && isBitmapTaskExecuted == 0) {

                        //if (bitmapWorkerTask != null){

                            //if(isBitmapTaskExecuted == 1)
                                Log.d("StoryMainFragment", bitmapWorkerTask.getStatus()  + " 다시 execute() !!!!! path !!!!! : " + path + " AlbumFullActivity.BitmapWorkerTask : " + bitmapWorkerTask);


                            isBitmapTaskExecuted = 1;//execute는 한번만 실행될 수 있다
                            smallOrLarge = 1;

                            bitmapWorkerTask.execute(path);//큰 이미지 로드 시작
                        }
                    }
                }
            }, 300);// 0.5초 정도 딜레이를 준 후 시작

        } else {
// fragment is no longer visible

            if (smallOrLarge == 1 && img != null) {//큰 이미지가 로드되어 있는 상태
                //if(img!= null){
                Log.d("StoryMainFragment", "큰 이미지를 지우자~~~");
                //큰 이미지를 recycle()한다
                //일단 AlbumFullActivity.viewpagerImage에서 삭제해준다
                AlbumFullActivity.viewPagerImage.remove(img);

                Drawable d = img.getDrawable();

                if (d instanceof BitmapDrawable) {
                    Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
                    if (bitmap != null && !bitmap.isRecycled()) {
                        Log.d("storyMainFragment","bitmap.isRecycled() : "+bitmap.isRecycled());
                        Log.d("StoryMainFragment", bitmap.getByteCount() + " recycle() & gc() 호출");
                        //recycle()을 하기 전에 imageView에 null을 할당한다(이전의 비트맵에 참조하는 것을 방지)
                        bitmapWorkerTask = null;//참조될 가능성이 있는 모든 객체를 해제한다
                        //img.setImageBitmap(null);

                        //작은 이미지로 대체한다
                        bitmapWorkerTask = ((AlbumFullActivity) getActivity()).loadBitmap(path, img,m.getId());
                        //작은 이미지로 대체한 다음에 recycle()을 한다
                        bitmap.recycle();
                        bitmap = null;
                        Runtime.getRuntime().gc();
                        System.gc();
                        isBitmapTaskExecuted = 0;
                        d.setCallback(null);
                        //img.setImageBitmap(CONSTANT.decodeSampledBitmapFromPath(path, CONSTANT.screenWidth/6, CONSTANT.screenHeight/6));
                    }
                }
                smallOrLarge = 0;
            }
            else if (smallOrLarge == 0) {//작은 이미지가 로드되어 있는 상태
                Log.d("StoryMainFragment", "이미 로드되어 있습니다");

            }
            //다시 작은 이미지로 바꿔준다(백그라운드에서)
            //img.setImageBitmap(CONSTANT.decodeSampledBitmapFromPath(path, CONSTANT.screenWidth/6, CONSTANT.screenHeight/6));

        }
    }

    @Override
    public void onPause(){
        super.onPause();

    }
    /*
    @Override
    public void onPause(){
        super.onPause();
    }*/
    @Override
    public void onDestroy() {
        //CONSTANT.currentImageInfo에서 해당 뷰에 관련된 아이템을 삭제한다
        //CONSTANT.ImagePathAndImageView tempInfo = new CONSTANT.ImagePathAndImageView(path,img);
        //CONSTANT.currentImageInfo.remove(tempInfo);//이미지 로딩을 위한 배열에서 삭제한다

        int imgIndex = AlbumFullActivity.viewPagerImage.indexOf(img);
        Log.d("StoryMainFragment", "onDestroy 호출!!!! " + AlbumFullActivity.viewPagerImage.size());
        if (imgIndex != -1) {
            ImageView tempImage = AlbumFullActivity.viewPagerImage.get(imgIndex);
            tempImage = null;
            AlbumFullActivity.viewPagerImage.remove(imgIndex);
        }
        if (img != null) {
            Drawable d = img.getDrawable();
            if (d instanceof BitmapDrawable) {
                Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
                if (bitmap != null && !bitmap.isRecycled()) {
                    Log.d("StoryMainFragment", bitmap.getByteCount() + " recycle() & gc() 호출");
                    bitmapWorkerTask = null;
                    img.setImageBitmap(null);
                    bitmap.recycle();
                    bitmap = null;
                    d.setCallback(null);
                    //isRecycled = 1;
                }
            }
            //d.setCallback(null);
            //img.setImageBitmap(null);
        }

        System.gc();//garbage collector
        Runtime.getRuntime().gc();//garbage collector
        super.onDestroy();
    }

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