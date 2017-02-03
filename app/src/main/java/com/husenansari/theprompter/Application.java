package com.husenansari.theprompter;

import android.preference.PreferenceManager;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;

import com.husenansari.theprompter.data.PrompterContract;


public class Application extends android.app.Application {
    Tracker tracker;

    @Override
    public void onCreate() {
        boolean hasRun = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("APP_RUN", false);

        if (!hasRun) {
            PrompterContract.addDummyContent(this);
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("APP_RUN", true).commit();
        }
        super.onCreate();
    }

    public Tracker startTracking()
    {
        if (tracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            tracker = analytics.newTracker(R.xml.analytics);
            tracker.enableAutoActivityTracking(true);
            analytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
            analytics.setLocalDispatchPeriod(10);
        }

        return tracker;
    }
}
