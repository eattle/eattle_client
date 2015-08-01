package com.eattle.phoket;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.ImageView;

import com.eattle.phoket.device.CachedBlockDevice;
import com.eattle.phoket.helper.DatabaseHelper;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by GA on 2015. 5. 14..
 */
public class CONSTANT {

    public static long TIMEINTERVAL = 15000L;//사진 분류시 간격(millisecond)

    public static ArrayList<Activity> actList = new ArrayList<Activity>();

    public static final int ONEDAY = 1440;
    public static final int CHECK = 10;


    public static final int NOTHING = -1;
    public static final int FOLDER = 0;
    public static final int TAG = 1;
    public static final int DEFAULT_TAG = 2;
    public static final int TOPHOKET = 3;
    public static final int DAILY = 4;


    public static int BOUNDARY = 5; //스토리에 있는 사진의 개수가 BOUNDARY 이하일 경우, 다른 형식으로 보여지게 된다. 즉, 스토리와 일상의 경계가 되는 사진 수
    public static int ISUSBCONNECTED = 0; //USB가 연결되어 있으면 1, 아니면 0
    public static int PASSWORD = 0;//비밀번호 해제 안됬으면 0, 해제 됬으면 1
    public static int PASSWORD_TRIAL = 5; //비밀번호가 PASSWORD_TRIAL보다 많이 틀릴 경우 앱 종료
    public static CachedBlockDevice BLOCKDEVICE;

    public static final String PACKAGENAME = "com.example.cds.eattle_prototype_2";
    public static final String appDBPath = "/data/" + CONSTANT.PACKAGENAME + "/databases/" + DatabaseHelper.DATABASE_NAME;//스마트폰 앱단의 DB 경로

    public static final int BIGSTORYCARD = 0;
    public static final int DAILYCARD = 1;
    public static final int TAGSCARD = 2;
    public static final int TOPHOKETCARD = 3;
    public static final int NOTIFICARD = 4;

    // Defines a custom Intent action
    public static final String BROADCAST_ACTION = "com.eattle.phoket.BROADCAST";
    public static final String EXTENDED_DATA_STATUS = "com.eattle.phoket.STATUS";
    public static final String EXTENDED_DATA = "com.eattle.phoket.DATA";


    public static final int MSG_REGISTER_CLIENT = 1;//MainActivity와 Service가 bind 되었을 때
    public static final int MSG_UNREGISTER_CLIENT = 2;//MainActivity와 Service가 bind를 중단하라는 메세지
    public static final int START_OF_PICTURE_CLASSIFICATION = 3;//MainActivity가 Service에게 사진 정리를 요청하는 메세지
    public static final int RECEIPT_OF_PICTURE_CLASSIFICATION = 7;//Service가 MainActivity의 사진 요청을 수락했다는 메세지
    public static final int END_OF_PICTURE_CLASSIFICATION = 4;//Service가 MainActivity에게 사진 정리를 완료 했다고 보내는 메세지
    public static final int END_OF_SINGLE_STORY = 5;//스토리가 정리되는대로 바로바로 보여주기 위하여 정의한 메세지,하나의 스토리가 정리될때마다 보낸다
    public static final int END_OF_DECODING_THUMBNAIL = 6;//loadBitmap에서 썸네일 생성을 완료했을 때
    public static final int START_OF_GUIDE = 8;//MainActivity가 Service에게 가이드 시작을 알릴 때
    public static final int END_OF_SINGLE_STORY_GUIDE = 9;//Service가 MainActivity에게 가이드 사진정리를 완료했음을 알림

    public static int screenWidth;//스마트폰 화면 너비
    public static int screenHeight;//스마트폰 화면 높이

    public static int COUNTIMAGE = 0;

    public static Snackbar snackbar;

    public static String convertFolderNameToStoryName(String folderName) {
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

    public static String convertFolderNameToDate(String folderName) {
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
    public static String convertFolderNameToStoryFolderName(String folderName) {
        String name = "";
        if (folderName.contains("~")) {//여러 날짜를 포함하는 스토리일 경우
            String[] bigSplit = folderName.split("~");
            String[] tempName = bigSplit[0].split("_");
            name += tempName[0] + "년" + tempName[1] + "월" + tempName[2] + "일~";
            tempName = bigSplit[1].split("_");
            name += tempName[1] + "월" + tempName[2].replace("의", "일의");
        } else {//단일 날짜의 스토리일 경우
            String[] tempName = folderName.split("_");
            name = tempName[0] + "년" + tempName[1] + "월" + tempName[2].replace("의", "일의");
        }

        return name;
    }
    /**
     * 사진 최적화를 위한 함수들 -----------------------------------------------------------------
     */
    //안드로이드 내장 썸네일을 얻는 함수
    public static Bitmap getThumbnail(ContentResolver cr, String originalPath, String thumbnailPath, int mediaId) throws Exception {
        Bitmap beforeBitmap = MediaStore.Images.Thumbnails.getThumbnail(cr, mediaId, MediaStore.Images.Thumbnails.MINI_KIND, null);
        //사진 회전
        int degree = GetExifOrientation(originalPath);//사진 방향은 originalPath로만 알 수 있다
        return GetRotatedBitmap(beforeBitmap, degree);
    }

    //안드로이드 내장 썸네일 경로를 얻는 함수
    public static String getThumbnailPath(ContentResolver cr, String path) throws Exception {
        //path를 통해 미디어 DB에 쿼리를 날리고 cursor를 얻어온다.
        Cursor ca = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.MediaColumns._ID, MediaStore.Images.Thumbnails.DATA}, MediaStore.MediaColumns.DATA + "=?", new String[]{path}, null);
        if (ca != null && ca.moveToFirst()) {

            int id = ca.getInt(ca.getColumnIndex(MediaStore.MediaColumns._ID));
            Cursor caa = MediaStore.Images.Thumbnails.queryMiniThumbnail(cr, id, MediaStore.Images.Thumbnails.MINI_KIND, null);

            String thumbnailPath = null;
            if (caa != null && caa.getCount() > 0) {
                caa.moveToFirst();
                thumbnailPath = caa.getString(caa.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.DATA));
                caa.close();
            }
            Log.d("CONSTANT", "썸네일 경로 : " + thumbnailPath);
            ca.close();

            return thumbnailPath;
        }

        ca.close();
        return null;
    }

    //안드로이드 내장 썸네일 경로를 얻는 함수
    public static String getThumbnailPath(ContentResolver cr, int id) throws Exception {
        Cursor caa = MediaStore.Images.Thumbnails.queryMiniThumbnail(cr, id, MediaStore.Images.Thumbnails.MINI_KIND, null);

        String thumbnailPath = null;
        if (caa != null && caa.getCount() > 0) {
            caa.moveToFirst();
            thumbnailPath = caa.getString(caa.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.DATA));

        }
        Log.d("CONSTANT", "썸네일 경로 : " + thumbnailPath);
        caa.close();
        return thumbnailPath;
    }


    //화면 크기,사진 크기에 따라 Options.inSampleSize 값을 어떻게 해야하는지 알려주는 함수
    public static int calculateInSampleSize(int width, int height, int reqWidth, int reqHeight) {
        Log.d("CONSTANT", "reqWidth & reqHeight & rawWidth & rawHeight :: " + reqWidth + " " + reqHeight + " " + width + " " + height);
        //모든 사진에 대해서 width가 height보다 크다. 따라서 스마트폰 가로모드에서는 width, height 값을 바꿀 필요가 없다!
        if (reqWidth < reqHeight && width > height) {//스마트폰 세로모드에서, 가로 사진 로드시
            Log.d("CONSTANT", "스마트폰 세로모드에서, 가로 사진 로드시");
            int temp = width;
            width = height;
            height = temp;
        }
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
            //Log.d("CONSTANT","최종 width & height  "+halfWidth/inSampleSize+" "+halfHeight/inSampleSize);
        }


        return inSampleSize;
    }

    //상황에 따른 적절한 사진을 얻는다
    public static Bitmap decodeSampledBitmapFromPath(String path, int reqWidth, int reqHeight) {

        //사진 크기를 알기 위해 inJustDecodeBounds=true 를 설정한다
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // inSampleSize를 계산한다
        options.inSampleSize = calculateInSampleSize(options.outWidth, options.outHeight, reqWidth, reqHeight);
        Log.d("CONSTANT", "[decodeSampledBitmapFromPath] inSampleSize : " + options.inSampleSize);
        // 비트맵 생성 후 반환
        options.inJustDecodeBounds = false;
        final Bitmap beforeRotate = BitmapFactory.decodeFile(path, options);

        //사진 방향 파악
        int degree = GetExifOrientation(path);
        //회전된 비트맵 반환
        return GetRotatedBitmap(beforeRotate, degree);
    }

    //sampleSize를 지정
    public static Bitmap decodeSampledBitmapFromPath(String path, int sampleSize) {

        //사진 크기를 알기 위해 inJustDecodeBounds=true 를 설정한다
        final BitmapFactory.Options options = new BitmapFactory.Options();
        // inSampleSize
        options.inSampleSize = sampleSize;
        return BitmapFactory.decodeFile(path, options);
    }

    //사진의 촬영 방향을 알아내는 함수
    public synchronized static int GetExifOrientation(String filepath) {
        int degree = 0;
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(filepath);
        } catch (IOException e) {
            Log.e("CONSTANT", "exif를 읽을수 없음");
            e.printStackTrace();
        }
        if (exif != null) {
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            if (orientation != -1) {
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                }
            }
        }
        return degree;
    }

    //사진을 회전시키는 함수
    public synchronized static Bitmap GetRotatedBitmap(Bitmap bitmap, int degrees) {
        if (degrees != 0 && bitmap != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);
            try {
                Bitmap b2 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
                if (bitmap != b2) {
                    bitmap.recycle();
                    bitmap = b2;
                }
            } catch (OutOfMemoryError ex) {
                // We have no memory to rotate. Return the original bitmap.
            }
        }
        return bitmap;
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

    public static int dpFromPx(final Context context, final float px) {
        return Math.round(px / context.getResources().getDisplayMetrics().density);
    }
    public static int pxFromDp(final Context context, final float dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }
}







