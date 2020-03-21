package com.hfad.a8tadah.App;

import android.app.Application;

import com.hfad.a8tadah.Preferences.MyPreferences;

public class MyApplication extends Application {

    static private MyApplication mInstance;
    private MyPreferences pref;

    @Override
    public void onCreate() {
        super.onCreate();
        this.mInstance = this;
    }

    public synchronized static MyApplication getInstance(){
        if (mInstance == null)
                mInstance = new MyApplication();
        return mInstance;
    }

    public MyPreferences getPreferences(){
        if (pref == null)
            pref = new MyPreferences(this);

        return pref;
    }
}