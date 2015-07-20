package com.eattle.phoket;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by dh_st_000 on 2015-07-20.
 */
public class GUIDE {
    public static int GUIDE_STEP = 0;//몇번째 가이드까지 진행했는지


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

    public static void guide_one(Context context) {//앱을 최초 실행했을 때 사진정리를 누르도록 한다.
        AlertDialog.Builder d = new AlertDialog.Builder(context);
        final LinearLayout r = (LinearLayout) View.inflate(context, R.layout.popup_guide_one, null);
        d.setView(r);
        DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:


                        //서비스에게 사진 정리를 요청한다
                        //sendMessageToService(CONSTANT.START_OF_PICTURE_CLASSIFICATION, 1);//1은 더미데이터(추후에 용도 지정, 예를 들면 0이면 전체 사진 새로 정리, 1이면 일부 사진 새로 정리 등)


                        break;
                }
            }
        };
        d.setPositiveButton("다음", l);
        d.show();
    }

    public static void guide_two(Context context) {//앱을 최초 실행했을 때 사진정리를 누르도록 한다.
        AlertDialog.Builder d = new AlertDialog.Builder(context);
        final LinearLayout r = (LinearLayout) View.inflate(context, R.layout.popup_guide_two, null);
        d.setView(r);
        DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        break;
                }
            }
        };
        d.setPositiveButton("다음", l);
        d.show();
    }

    public static void guide_three(Context context) {//앱을 최초 실행했을 때 사진정리를 누르도록 한다.
        AlertDialog.Builder d = new AlertDialog.Builder(context);
        final LinearLayout r = (LinearLayout) View.inflate(context, R.layout.popup_guide_three, null);
        d.setView(r);
        DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        break;
                }
            }
        };
        d.setPositiveButton("다음", l);
        d.show();
    }

    public static void guide_four(Context context) {//앱을 최초 실행했을 때 사진정리를 누르도록 한다.
        AlertDialog.Builder d = new AlertDialog.Builder(context);
        final LinearLayout r = (LinearLayout) View.inflate(context, R.layout.popup_guide_four, null);
        d.setView(r);
        DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        break;
                }
            }
        };
        d.setPositiveButton("다음", l);
        d.show();
    }

    public static void guide_five(Context context) {//앱을 최초 실행했을 때 사진정리를 누르도록 한다.
        AlertDialog.Builder d = new AlertDialog.Builder(context);
        final LinearLayout r = (LinearLayout) View.inflate(context, R.layout.popup_guide_five, null);
        d.setView(r);
        DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        break;
                }
            }
        };
        d.setPositiveButton("다음", l);
        d.show();
    }

    public static void guide_six(Context context) {//앱을 최초 실행했을 때 사진정리를 누르도록 한다.
        AlertDialog.Builder d = new AlertDialog.Builder(context);
        final LinearLayout r = (LinearLayout) View.inflate(context, R.layout.popup_guide_six, null);
        d.setView(r);
        DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        break;
                }
            }
        };
        d.setPositiveButton("다음", l);
        d.show();
    }

    public static void guide_seven(Context context) {//앱을 최초 실행했을 때 사진정리를 누르도록 한다.
        AlertDialog.Builder d = new AlertDialog.Builder(context);
        final LinearLayout r = (LinearLayout) View.inflate(context, R.layout.popup_guide_seven, null);
        d.setView(r);
        DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        break;
                }
            }
        };
        d.setPositiveButton("다음", l);
        d.show();
    }

    public static void guide_eight(Context context) {//앱을 최초 실행했을 때 사진정리를 누르도록 한다.
        AlertDialog.Builder d = new AlertDialog.Builder(context);
        final LinearLayout r = (LinearLayout) View.inflate(context, R.layout.popup_guide_eight, null);
        d.setView(r);
        DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        break;
                }
            }
        };
        d.setPositiveButton("다음", l);
        d.show();
    }
}
