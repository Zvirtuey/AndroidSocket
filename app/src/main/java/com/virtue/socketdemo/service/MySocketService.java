package com.virtue.socketdemo.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.virtue.socketlibrary.BroadCastType;
import com.virtue.socketlibrary.ReceiveType;
import com.virtue.socketlibrary.SocketCode;
import com.virtue.socketlibrary.Socketer;

/**
 * Created by virtue on 2017/2/16.
 */

public class MySocketService extends Service {

    public static Socketer socketer;
    private static final String TAG = "MySocketService";
    private MessageReceiver dataReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Socketer.getInstance(getApplicationContext()).bindServerContect("192.168.1.108", 20180)
                .setTimeout(15).setEncode("UTF_8")
                .setReceiveType(ReceiveType.SEPARATION_SIGN)
                .setEndCharSequence("\r\n")
                .setMsgLength(1500).start();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadCastType.SERVER_NOTICE);
        dataReceiver = new MessageReceiver();
        registerReceiver(dataReceiver, intentFilter);
        super.onCreate();
    }

    class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == BroadCastType.SERVER_NOTICE) {
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
        }
    }
}
