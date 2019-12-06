package com.virtue.socketdemo.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.virtue.socketlibrary.manager.Socketer;
import com.virtue.socketlibrary.type.BroadCastType;
import com.virtue.socketlibrary.type.ReceiveType;

/**
 * Created by virtue on 2017/2/16.
 */

public class MySocketService extends Service {

    private static final String TAG = "MySocketService";
    private MessageReceiver dataReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //Demo in the LAN network, if you want to demo you need to modify your own server!
        Socketer.getInstance(getApplicationContext()).bindServerConnect("192.168.2.171", 20083)
                .setTimeout(10).setEncode("UTF_8")
                .setReceiveType(ReceiveType.SEPARATION_SIGN)
                .setEndCharSequence("\r\n") //服务器分割消息符
                .setMsgLength(1500).start();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadCastType.SERVER_NOTICE);
        dataReceiver = new MessageReceiver();
        registerReceiver(dataReceiver, intentFilter);
    }

    class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BroadCastType.SERVER_NOTICE)) {
                String dataStr = intent.getStringExtra(BroadCastType.SERVER_NOTICE_DATA);
                Log.i(TAG, "Data given to me by the server:" + dataStr);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dataReceiver != null) {
            unregisterReceiver(dataReceiver);
            Socketer.getInstance(getApplicationContext()).closeConnect();
            Socketer.getInstance(getApplicationContext()).closeSocketer();
        }
    }
}
