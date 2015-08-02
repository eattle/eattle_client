package com.eattle.phoket;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.afollestad.materialdialogs.MaterialDialog;
import com.eattle.phoket.helper.DatabaseHelper;

import java.util.ArrayList;

/**
 * Created by dh_st_000 on 2015-07-20.
 */
public class GUIDE {
    public static int GUIDE_STEP = 0;//몇번째 가이드까지 진행했는지
    public static ArrayList<PopupWindow> CURRENT_POPUP = new ArrayList<PopupWindow>();

    public static int guide_grid(String name){
        switch(name){
            case "phoket1":
                return R.mipmap.phoket1;
            case "phoket2":
                return R.mipmap.phoket2;
            case "phoket3":
                return R.mipmap.phoket3;
            case "phoket4":
                return R.mipmap.phoket4;
            case "phoket5":
                return R.mipmap.phoket5;
            case "phoket6":
                return R.mipmap.phoket6;
            case "phoket7":
                return R.mipmap.phoket7;
            case "phoket8":
                return R.mipmap.phoket8;

        }
        return 0;
    }


    public static void guide_initiate(final Context context) {//앱을 최초 실행했을 때 사진정리를 누르도록 한다.
        GUIDE_STEP = 0;
        new MaterialDialog.Builder(context)
                .title(R.string.guide1Title)
                .content(R.string.guide1Content)
                .negativeText(R.string.guide1Button2)
                .positiveText(R.string.guide1Button1)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        guide1(context);
                    }
                    @Override
                    public void onNegative(MaterialDialog dialog){

                        guide_eight(context);
                        //튜토리얼을 생략한다
                        DatabaseHelper db = DatabaseHelper.getInstance(context);
                        db.createGuide(1);
                    }
                })
                .cancelable(false)
                .show();
    }


    public static void guide1(Context context) {//사진 정리하는 방법
        GUIDE_STEP = 1;

        //동그라미 당기는 애니메이션 보여주고
        showPullAnimation(context);

        //첫번째 가이드 보여주고
        final LinearLayout r = (LinearLayout) View.inflate(context, R.layout.popup_guide_one, null);
        final PopupWindow mPopupWindow = new PopupWindow(r, LinearLayout.LayoutParams.MATCH_PARENT, CONSTANT.screenHeight - CONSTANT.pxFromDp(context, 356));
        mPopupWindow.setAnimationStyle(-1); // 애니메이션 설정
        mPopupWindow.showAtLocation(r, Gravity.BOTTOM, 0, 0);
        CURRENT_POPUP.add(mPopupWindow);

        //상위에 터치 막는 popup 설정
        showFakePopupAtTop(context);
    }


    public static void guide2(Context context) {//스토리 클릭
        GUIDE_STEP = 2;

        for(int i=0;i<CURRENT_POPUP.size();i++)
            CURRENT_POPUP.get(i).dismiss();//이전의 팝업을 지운다

        showTouchAnimation(context, 100, 270);

        final LinearLayout r = (LinearLayout) View.inflate(context, R.layout.popup_guide_two, null);
        PopupWindow mPopupWindow = new PopupWindow(r, LinearLayout.LayoutParams.MATCH_PARENT, CONSTANT.screenHeight - CONSTANT.pxFromDp(context, 356));
        mPopupWindow.setAnimationStyle(-1); // 애니메이션 설정
        mPopupWindow.showAtLocation(r, Gravity.BOTTOM, 0, 0);
        CURRENT_POPUP.add(mPopupWindow);

        showFakePopupAtTop(context);
        /*
        long currentTime = System.currentTimeMillis();//현재 시간
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(currentTime);
        String folderName = "" + cal.get(Calendar.YEAR) + "_" + (cal.get(Calendar.MONTH) + 1) + "_" + cal.get(Calendar.DATE)+"의 스토리";
        folderName = CONSTANT.convertFolderNameToStoryName(folderName);
        TextView textView = (TextView)r.findViewById(R.id.guide_two_text);
        textView.setText("스토리가 생성되었습니다 ! \n\n"+folderName+"\n를 눌러보세요");
        */
    }

/*    public static void guide_three(final Context context) {//앨범 그리드 뷰
        for(int i=0;i<CURRENT_POPUP.size();i++)
            CURRENT_POPUP.get(i).dismiss();//이전의 팝업을 지운다

        AlertDialog.Builder d = new AlertDialog.Builder(context);
        final LinearLayout r = (LinearLayout) View.inflate(context, R.layout.popup_guide_three, null);
        d.setView(r);
        DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        guide_three_one(context);
                        break;
                }
            }
        };
        d.setCancelable(false);
        d.setPositiveButton("다음", l);
        d.show();
    }*/

    public static void guide3(final Context context) {//앨범 그리드 뷰
        GUIDE_STEP = 3;

        for(int i=0;i<CURRENT_POPUP.size();i++)
            CURRENT_POPUP.get(i).dismiss();//이전의 팝업을 지운다

        final LinearLayout r = (LinearLayout) View.inflate(context, R.layout.popup_guide_three_one, null);
        final PopupWindow mPopupWindow = new PopupWindow(r, LinearLayout.LayoutParams.MATCH_PARENT, CONSTANT.pxFromDp(context, 304));
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                showTouchAnimation(context, 20, 360);
                mPopupWindow.showAtLocation(r, Gravity.TOP, 0, 0);
            }
        }, 500);

        CURRENT_POPUP.add(mPopupWindow);
    }

    public static void guide_four(final Context context) {//앨범 full activity
        GUIDE_STEP = 4;

        for(int i=0; i<CURRENT_POPUP.size(); i++)
            CURRENT_POPUP.get(i).dismiss();//이전의 팝업을 지운다
        final LinearLayout r = (LinearLayout) View.inflate(context, R.layout.popup_guide_four, null);

        Animation move = new TranslateAnimation(0, CONSTANT.pxFromDp(context,140),0, 0);
        move.setDuration(1500L);
        move.setRepeatCount(Animation.INFINITE);
        move.setRepeatMode(Animation.REVERSE);
        r.findViewById(R.id.touch).startAnimation(move);

        final PopupWindow mPopupWindow = new PopupWindow(r, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        mPopupWindow.setTouchable(false);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                mPopupWindow.showAtLocation(r, Gravity.NO_GRAVITY, 0, 0);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        guide_four_one(context);
                    }
                }, 3500);
            }
        }, 1000);

        CURRENT_POPUP.add(mPopupWindow);

/*

        AlertDialog.Builder d = new AlertDialog.Builder(context);
        final LinearLayout r = (LinearLayout) View.inflate(context, R.layout.popup_guide_four, null);
        d.setView(r);
        DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        guide_four_one(context);
                        break;
                }
            }
        };
        d.setCancelable(false);
        d.setPositiveButton("다음", l);
        d.show();*/
    }
    public static void guide_four_one(Context context) {//앨범 full activity
        GUIDE_STEP = 5;

        for(int i=0; i<CURRENT_POPUP.size(); i++)
            CURRENT_POPUP.get(i).dismiss();//이전의 팝업을 지운다
        final LinearLayout r = (LinearLayout) View.inflate(context, R.layout.popup_guide_four_one, null);

        final Animation in = new AlphaAnimation(1.0f, 0.5f);
        in.setDuration(500L);
        in.setInterpolator(new FastOutSlowInInterpolator());
        in.setRepeatMode(Animation.REVERSE);
        in.setRepeatCount(Animation.INFINITE);
        r.findViewById(R.id.touch).startAnimation(in);

        final PopupWindow mPopupWindow = new PopupWindow(r, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        mPopupWindow.setTouchable(false);
        mPopupWindow.showAtLocation(r, Gravity.NO_GRAVITY, 0, 0);

        CURRENT_POPUP.add(mPopupWindow);

    }

    public static void guide_five(final Context context) {//tags over album
        GUIDE_STEP = 6;

        for(int i=0;i<CURRENT_POPUP.size();i++)
            CURRENT_POPUP.get(i).dismiss();//이전의 팝업을 지운다

        final LinearLayout r = (LinearLayout) View.inflate(context, R.layout.popup_guide_five, null);
        r.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                for (int i = 0; i < CURRENT_POPUP.size(); i++)
                    CURRENT_POPUP.get(i).dismiss();//이전의 팝업을 지운다

                for (int i = 0; i < CONSTANT.actList.size(); i++)
                    CONSTANT.actList.get(i).finish();
                return false;
            }
        });
        PopupWindow mPopupWindow = new PopupWindow(r, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        mPopupWindow.showAtLocation(r, Gravity.NO_GRAVITY, 0, 0);

        CURRENT_POPUP.add(mPopupWindow);
    }

    public static void guide_six(Context context) {//스토리 길게 클릭
        GUIDE_STEP = 7;

        for(int i=0;i<CURRENT_POPUP.size();i++)
            CURRENT_POPUP.get(i).dismiss();//이전의 팝업을 지운다
        showLongTouchAnimation(context, 100, 270);

        final LinearLayout r = (LinearLayout) View.inflate(context, R.layout.popup_guide_six, null);
        PopupWindow mPopupWindow = new PopupWindow(r, LinearLayout.LayoutParams.MATCH_PARENT,CONSTANT.screenHeight - CONSTANT.pxFromDp(context, 356));
        mPopupWindow.setAnimationStyle(-1); // 애니메이션 설정
        mPopupWindow.showAtLocation(r, Gravity.BOTTOM, 0, 0);
        CURRENT_POPUP.add(mPopupWindow);

        showFakePopupAtTop(context);
    }

    public static void guide_seven(final Context context) {//로컬 내보내기, 공유 설명
        GUIDE_STEP = 8;

        for(int i=0;i<CURRENT_POPUP.size();i++)
            CURRENT_POPUP.get(i).dismiss();//이전의 팝업을 지운다

        final LinearLayout r = (LinearLayout) View.inflate(context, R.layout.popup_guide_seven, null);
        r.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        PopupWindow mPopupWindow = new PopupWindow(r, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        mPopupWindow.showAtLocation(r, Gravity.NO_GRAVITY, 0, 0);
        CURRENT_POPUP.add(mPopupWindow);
    }

    public static void guide_eight(final Context context) {
        GUIDE_STEP = 9;

        for(int i=0;i<CURRENT_POPUP.size();i++)
            CURRENT_POPUP.get(i).dismiss();//이전의 팝업을 지운다


        final LinearLayout r = (LinearLayout) View.inflate(context, R.layout.popup_guide_eight, null);

        final PopupWindow mPopupWindow = new PopupWindow(r, LinearLayout.LayoutParams.MATCH_PARENT, CONSTANT.screenHeight - CONSTANT.pxFromDp(context, 356));
        mPopupWindow.setAnimationStyle(-1); // 애니메이션 설정
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                showPullAnimation(context);
                mPopupWindow.showAtLocation(r, Gravity.BOTTOM, 0, 0);
                showFakePopupAtTop(context);

            }
        }, 500);
        CURRENT_POPUP.add(mPopupWindow);

    }

    public static void showPullAnimation(Context context){
        final LinearLayout a = (LinearLayout) View.inflate(context, R.layout.popup_guide_animation, null);
        ImageView i = (ImageView)a.findViewById(R.id.touch);

        ViewGroup.MarginLayoutParams lpimg = (ViewGroup.MarginLayoutParams) i.getLayoutParams();
        lpimg.leftMargin = CONSTANT.pxFromDp(context,50);
        lpimg.topMargin = CONSTANT.pxFromDp(context,250);
        i.setLayoutParams(lpimg);

        Animation move = new TranslateAnimation(0, 0, -CONSTANT.pxFromDp(context,130), 0);
        move.setDuration(1500L);
        move.setInterpolator(new FastOutSlowInInterpolator());
        move.setRepeatCount(Animation.INFINITE);

        i.startAnimation(move);

        PopupWindow animationPopup = new PopupWindow(a, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        animationPopup.setAnimationStyle(-1); // 애니메이션 설정
        animationPopup.setTouchable(false);
        animationPopup.showAtLocation(a, Gravity.NO_GRAVITY, 0, 0);
        CURRENT_POPUP.add(animationPopup);
    }


    public static void showTouchAnimation(Context context, int left, int top){
        final LinearLayout a = (LinearLayout) View.inflate(context, R.layout.popup_guide_animation, null);
        ImageView i = (ImageView)a.findViewById(R.id.touch);

        ViewGroup.MarginLayoutParams lpimg = (ViewGroup.MarginLayoutParams) i.getLayoutParams();
        lpimg.leftMargin = CONSTANT.pxFromDp(context,left);
        lpimg.topMargin = CONSTANT.pxFromDp(context,top);
        i.setLayoutParams(lpimg);

        final Animation in = new AlphaAnimation(1.0f, 0.5f);
        in.setDuration(500L);
        in.setInterpolator(new FastOutSlowInInterpolator());
        in.setRepeatMode(Animation.REVERSE);
        in.setRepeatCount(Animation.INFINITE);

        i.startAnimation(in);

        PopupWindow animationPopup = new PopupWindow(a, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        animationPopup.setAnimationStyle(-1); // 애니메이션 설정
        animationPopup.setTouchable(false);
        animationPopup.showAtLocation(a, Gravity.NO_GRAVITY, 0, 0);
        CURRENT_POPUP.add(animationPopup);
    }

    public static void showLongTouchAnimation(Context context, int left, int top){
        final LinearLayout a = (LinearLayout) View.inflate(context, R.layout.popup_guide_animation, null);
        ImageView i = (ImageView)a.findViewById(R.id.touch);

        ViewGroup.MarginLayoutParams lpimg = (ViewGroup.MarginLayoutParams) i.getLayoutParams();
        lpimg.leftMargin = CONSTANT.pxFromDp(context,left);
        lpimg.topMargin = CONSTANT.pxFromDp(context,top);
        i.setLayoutParams(lpimg);

        final Animation in = new ScaleAnimation(0.6f,1.0f,0.6f,1.0f, 50,50);
        in.setDuration(1000L);
        in.setInterpolator(new FastOutSlowInInterpolator());
        in.setRepeatCount(Animation.INFINITE);

        i.startAnimation(in);

        PopupWindow animationPopup = new PopupWindow(a, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        animationPopup.setAnimationStyle(-1); // 애니메이션 설정
        animationPopup.setTouchable(false);
        animationPopup.showAtLocation(a, Gravity.NO_GRAVITY, 0, 0);
        CURRENT_POPUP.add(animationPopup);
    }


    public static void showFakePopupAtTop(Context context){
        //터치를 막기위한 fake 팝업
        final LinearLayout blank = (LinearLayout) View.inflate(context,R.layout.popup_guide_blank, null);
        PopupWindow blankPopup = new PopupWindow(blank, LinearLayout.LayoutParams.MATCH_PARENT, CONSTANT.pxFromDp(context,104));
        blankPopup.showAtLocation(blank, Gravity.NO_GRAVITY, 0, 0);
        CURRENT_POPUP.add(blankPopup);
    }

}
