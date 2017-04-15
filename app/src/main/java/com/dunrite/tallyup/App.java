package com.dunrite.tallyup;

import android.app.Application;

import shortbread.Shortbread;

/**
 * Application
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Shortbread.create(this);
    }
}
