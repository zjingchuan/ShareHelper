package com.sharehelper.app;

import android.app.Application;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by jingchuan on 2015/8/26.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MobclickAgent.setDebugMode(true);
        MobclickAgent.setCatchUncaughtExceptions(true);
    }
}
