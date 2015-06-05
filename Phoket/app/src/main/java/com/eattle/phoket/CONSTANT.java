package com.eattle.phoket;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Log;
import android.widget.ImageView;

import com.eattle.phoket.device.CachedBlockDevice;
import com.eattle.phoket.helper.DatabaseHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by GA on 2015. 5. 14..
 */
public class CONSTANT {
    public static long TIMEINTERVAL=15000L;//사진 분류시 간격(millisecond)

    public static ArrayList<Activity> actList = new ArrayList<Activity>();

    public static final int NOTHING = -1;
    public static final int FOLDER = 0;
    public static final int TAG = 1;
    public static final int DEFAULT_TAG = 2;
    public static final int TOPHOKET = 3;
    public static final int DAILY = 4;


    public static int BOUNDARY = 3; //스토리에 있는 사진의 개수가 BOUNDARY 이하일 경우, 다른 형식으로 보여지게 된다
    public static int ISUSBCONNECTED = 0; //USB가 연결되어 있으면 1, 아니면 0
    public static int PASSWORD = 0;//비밀번호 해제 안됬으면 0, 해제 됬으면 1
    public static int PASSWORD_TRIAL = 5; //비밀번호가 PASSWORD_TRIAL보다 많이 틀릴 경우 앱 종료
    public static CachedBlockDevice BLOCKDEVICE;

    public static final String PACKAGENAME="com.example.cds.eattle_prototype_2";
    public static final String appDBPath="/data/" + CONSTANT.PACKAGENAME + "/databases/" + DatabaseHelper.DATABASE_NAME;//스마트폰 앱단의 DB 경로

    public static final int BIGSTORYCARD = 0;
    public static final int DAILYCARD = 1;
    public static final int TAGSCARD = 2;
    public static final int TOPHOKETCARD = 3;
    public static final int NOTIFICARD = 4;

    public static final int MSG_REGISTER_CLIENT = 1;//MainActivity와 Service가 bind 되었을 때
    public static final int MSG_UNREGISTER_CLIENT = 2;//MainActivity와 Service가 bind를 중단하라는 메세지
    public static final int START_OF_PICTURE_CLASSIFICATION = 3;//MainActivity가 Service에게 사진 정리를 요청하는 메세지
    public static final int END_OF_PICTURE_CLASSIFICATION = 4;//Service가 MainActivity에게 사진 정리를 완료 했다고 보내는 메세지
    public static final int END_OF_SINGLE_STORY = 5;//스토리가 정리되는대로 바로바로 보여주기 위하여 정의한 메세지,하나의 스토리가 정리될때마다 보낸다

    public static int screenWidth;//스마트폰 화면 너비
    public static int screenHeight;//스마트폰 화면 높이

    public static ImageView currentImageView;
    public static ArrayList<ImagePathAndImageView> currentImageInfo = new ArrayList<ImagePathAndImageView>();//현재 보고있는 사진에 대해서만 원본을 로드하도록 함

    public static class ImagePathAndImageView{
        private String path;
        private ImageView imageView;

        public ImagePathAndImageView(String path, ImageView imageView){
            this.path = path;
            this.imageView = imageView;
        }

        public String getPath(){
            return path;
        }
        public ImageView getImageView(){
            return imageView;
        }

    }

    public static String convertFolderNameToStoryName(String folderName){
        String name = "";
        if (folderName.contains("~")) {//여러 날짜를 포함하는 스토리일 경우
            String[] bigSplit = folderName.split("~");
            String[] tempName = bigSplit[0].split("_");
            name += tempName[0] + "년 " + tempName[1] + "월 " + tempName[2] + "일 ~ ";
            tempName = bigSplit[1].split("_");
            name += tempName[1] + "월 " + tempName[2].replace("의", "일의");
        } else {//단일 날짜의 스토리일 경우
            String[] tempName = folderName.split("_");
            name = tempName[0] + "년 " + tempName[1] + "월 " + tempName[2].replace("의", "일의");
        }

        return name;
    }

    public static String convertFolderNameToDate(String folderName){
        String name = "";
        if (folderName.contains("~")) {//여러 날짜를 포함하는 스토리일 경우
            String[] bigSplit = folderName.split("~");
            String[] tempName = bigSplit[0].split("_");
            name += tempName[0] + "." + tempName[1] + "." + tempName[2] + " ~ ";
            tempName = bigSplit[1].split("_");
            name += tempName[1] + "." + tempName[2];
        } else {//단일 날짜의 스토리일 경우
            String[] tempName = folderName.split("_");
            name = tempName[0] + "." + tempName[1] + "." + tempName[2];
        }
        name = name.replace("의 스토리", "");


        return name;

    }
    /**
        사진 최적화를 위한 함수들 -----------------------------------------------------------------
     **/
    //화면 크기,사진 크기에 따라 Options.inSampleSize 값을 어떻게 해야하는지 알려주는 함수
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;//더 줄인다
    }

    //상황에 따른 적절한 사진을 얻는다
    public static Bitmap decodeSampledBitmapFromPath(String path, int reqWidth, int reqHeight) {

        //사진 크기를 알기 위해 inJustDecodeBounds=true 를 설정한다
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // inSampleSize를 계산한다
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        Log.d("CONSTANT","inSampleSize : "+options.inSampleSize);
        // 비트맵 생성 후 반환
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    //sampleSize를 지정
    public static Bitmap decodeSampledBitmapFromPath(String path,int sampleSize) {

        //사진 크기를 알기 위해 inJustDecodeBounds=true 를 설정한다
        final BitmapFactory.Options options = new BitmapFactory.Options();
        // inSampleSize
        options.inSampleSize = sampleSize;
        return BitmapFactory.decodeFile(path, options);
    }
    //Bitmap 메모리 해제를 위한 함수
    public static void releaseImageMemory(ImageView img) {

        if (img != null) {
            Drawable d = img.getDrawable();
            if (d instanceof BitmapDrawable) {
                Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
                if (bitmap != null && !bitmap.isRecycled()) {
                    Log.d("StoryRecommendFragment", bitmap.getByteCount() + " recycle() & gc() 호출");
                    img.setImageBitmap(null);
                    bitmap.recycle();
                    bitmap = null;
                    d.setCallback(null);
                }
            }

            img = null;
        }
    }


}

