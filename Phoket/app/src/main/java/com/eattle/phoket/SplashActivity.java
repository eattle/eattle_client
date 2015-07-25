package com.eattle.phoket;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.android.gms.analytics.HitBuilders;


public class SplashActivity extends Activity {

    static final int SPLASH_DISPLAY_LENGTH = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        // Builder parameters can overwrite the screen name set on the tracker.


        new Handler().postDelayed(new Runnable() {
            public void run() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        }, SPLASH_DISPLAY_LENGTH * 1000);

    }
}
