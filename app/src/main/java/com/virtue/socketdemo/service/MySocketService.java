package com.virtue.socketdemo.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.virtue.socketlibrary.Socketer;

/**
 * Created by virtue on 2017/2/16.
 */

public class MySocketService extends Service {

    public static Socketer socketer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        socketer = Socketer.getInstance(getApplicationContext());
        socketer.setSocketInfo("192.168.2.152", 20000, 15 * 1000);
        socketer.setSocketEnCode("UTF-8");
        socketer.setReciveMsgSlitChar("8002");
        socketer.startSocket();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
       //socketer.stopSocket();
    }
}
