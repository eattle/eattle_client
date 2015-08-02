package com.eattle.phoket;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.eattle.phoket.helper.DatabaseHelper;
import com.eattle.phoket.model.Folder;
import com.eattle.phoket.model.Media;
import com.eattle.phoket.model.NotificationM;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * Created by dh_st_000 on 2015-07-11.
 */
public class BroadcastListener extends BroadcastReceiver {
    private String TAG = "BroadcastListener";
    private static int countForTick = 0;
    private static int howOftenCheck = CONSTANT.CHECK;//60이면 60분마다 체크,10이면 10분마다 체크
    //일정 시간마다 새로운 사진이 생성되었는지 여부를 체크한다.
    private static final String TIME_TICK = "android.intent.action.TIME_TICK";
    public static String ACTION_RESTART_PERSISTENTSERVICE = "ACTION.Restart.PhoketService";//서비스 재시작을 위해

    private Cursor mCursor;
    private ContentResolver mCr;
    private static Context mContext;

    public BroadcastListener() {
    }

    public BroadcastListener(Context context) {
        mContext = context;
        mCr = context.getContentResolver();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        mCr = context.getContentResolver();

        String action = intent.getAction();
        Log.d(TAG, "[브로드캐스트 리시버] " + action);
        //서비스 재시작을 위해 ---------------------------------------
        //앱이 종료되었거나 스마트폰이 재부팅 되었을 때
        if (action.equals(ACTION_RESTART_PERSISTENTSERVICE) ||
                action.equals(Intent.ACTION_BOOT_COMPLETED)) {

            Log.d(TAG, "ACTION_RESTART_PERSISTENTSERVICE || ACTION_BOOT_COMPLETED");
            int countForTick = intent.getIntExtra("countForTick", 0);
            int HOWOFTENCHECK = intent.getIntExtra("HOWOFTENCHECK", 10);

            //서비스를 시작하기 위한 새로운 인텐트
            Intent i = new Intent(context, ServiceOfPictureClassification.class);
            intent.putExtra("countForTick", countForTick);
            intent.putExtra("HOWOFTENCHECK", HOWOFTENCHECK);

            context.startService(i);//죽었던 서비스를 다시 시작한다
        } else if (action.equals(TIME_TICK)) {
            countForTick++;
            if (countForTick % howOftenCheck == 0) {//일정시간마다 체크
                /*TODO : ERROR*/
                Log.d(TAG, "ServiceOfPictureClassification.isClassifying ? " + ServiceOfPictureClassification.isClassifying);
                if (ServiceOfPictureClassification.isClassifying)//사진을 정리하고 있으면
                    return;//아무것도 하지 않음
                if (howOftenCheck == CONSTANT.TWODAY)//마지막 푸시를 한지 24시간이 지났으면
                    howOftenCheck = CONSTANT.CHECK;//다시 10분마다 체크를 한다.
                String[] projection = {MediaStore.MediaColumns._ID};
                String selection = MediaStore.Images.ImageColumns.DATE_TAKEN + " > ?";
                String before = ((new Date().getTime() - (CONSTANT.CHECK * 60 * 1000)) / 1000) + "";//최근 10분동안 찍은 사진만 가져온다!!
                String[] selectionArgs = {before};
                String sortOrder = MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC";
                mCursor = mCr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, sortOrder);

                if (mCursor.moveToFirst()) {//제일 최신사진을 확인
                    int pictureID = mCursor.getInt(mCursor.getColumnIndex(MediaStore.MediaColumns._ID));

                    DatabaseHelper db = DatabaseHelper.getInstance(mContext);
                    Media m = db.getMediaById(pictureID);
                    NotificationM notificationM = db.getNotification();
                    if ((m == null) &&
                            (db.getNotification() == null || (notificationM != null && notificationM.getLastPictureID() != pictureID))) {//새로운 사진이 들어온 경우

                        //정리 제외 대상인지 확인한다
                        /** ------------------정리 제외 대상------------------ **/
                        String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));//사진이 존재하는 경로

                        if (path.contains("thumbnail") || path.contains("Screenshot") || path.contains("screenshot"))
                            return;
                        //해당 경로에 존재하지 않는 사진은 건너띈다
                        if (!(new File(path)).exists())
                            return;


                        makeNotification(mContext);
                        //푸시한 시간을 DB에 입력한다(푸시간에 최소한의 시간간격을 유지하기 위함 & 중간에 서비스 재시작되는경우)
                        long currentTime = System.currentTimeMillis();
                        NotificationM notification = new NotificationM(currentTime, pictureID);
                        db.createNotification(notification);

                        countForTick = 0;
                        //이틀에 한번 이상 보내지 않는다
                        howOftenCheck = CONSTANT.TWODAY; //다음 확인은 최소 이틀이 지난후에
                    }
                }
                mCursor.close();
            }
            Log.d(TAG, "[TIME_TICK 도착] countForTick : " + countForTick + " howOftenCheck : " + howOftenCheck);
        }

    }

    private void makeNotification(Context context) {
        //notification
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, SplashActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mCompatBuilder = new NotificationCompat.Builder(context);
        mCompatBuilder.setSmallIcon(R.mipmap.icon);
        mCompatBuilder.setTicker("새로운 사진으로 편리하게 스토리를 만들어 보세요");
        mCompatBuilder.setWhen(System.currentTimeMillis());
        //mCompatBuilder.setNumber(1);
        mCompatBuilder.setContentTitle("Phoket");
        mCompatBuilder.setContentText("새로운 사진으로 편리하게 스토리를 만들어 보세요");
        mCompatBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        mCompatBuilder.setContentIntent(pendingIntent);
        mCompatBuilder.setAutoCancel(true);

        nm.notify(222, mCompatBuilder.build());
    }

    public static int getCountForTick() {
        return countForTick;
    }

    public static int getHOWOFTENCHECK() {
        return howOftenCheck;
    }

    public static void setHowOftenCheck(int howOftenCheck1) {
        howOftenCheck = howOftenCheck1;
    }

}