package com.eattle.phoket;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;


public class SplashActivity extends Activity {

    static final int SPLASH_DISPLAY_LENGTH = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                startActivity(new Intent(SplashActivity.this, PasswordActivity.class));
                finish();
            }
        }, SPLASH_DISPLAY_LENGTH * 1000);

    }
}
