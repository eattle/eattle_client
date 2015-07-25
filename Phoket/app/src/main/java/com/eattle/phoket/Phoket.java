package com.eattle.phoket;

import android.app.Application;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by GA on 2015. 7. 25..
 */
public class Phoket extends Application {
    public static GoogleAnalytics analytics;
    public static Tracker tracker;

    @Override
    public void onCreate() {

        Log.d("ga", "analytics connected");
        Log.d("ga", "check this 'https://www.google.com/analytics/web/?authuser=0#report/app-overview/a65606247w101864405p105840533/'");

        analytics = GoogleAnalytics.getInstance(this);
        analytics.setLocalDispatchPeriod(1800);

        tracker = analytics.newTracker("UA-65606247-1"); // Replace with actual tracker/property Id
        tracker.enableExceptionReporting(true);
        tracker.enableAdvertisingIdCollection(true);
        tracker.enableAutoActivityTracking(true);

//        tracker.setScreenName("main screen");
//
//        tracker.send(new HitBuilders.EventBuilder()
//                .setCategory("UX")
//                .setAction("click")
//                .setLabel("submit")
//                .build());
    }

}