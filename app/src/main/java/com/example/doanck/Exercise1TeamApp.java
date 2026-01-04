package com.example.exercise1_team;

import android.app.Application;

/**
 * Initializes global singletons for the app process.
 */
public class Exercise1TeamApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SessionManager.init(this);
    }
}

