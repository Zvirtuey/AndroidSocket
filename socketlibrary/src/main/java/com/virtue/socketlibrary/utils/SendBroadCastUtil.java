package com.virtue.socketlibrary.utils;

import android.content.Context;
import android.content.Intent;

import com.virtue.socketlibrary.type.BroadCastType;

public class SendBroadCastUtil {

    /**
     * 发送网络状态广播 (send network Network status)
     *
     * @param mContext
     * @param isConnected
     */
    public static void sendNetworkStateBroadcast(Context mContext, boolean isConnected) {
        Intent intent = new Intent();
        intent.putExtra(BroadCastType.IS_CONNECTED, isConnected);
        intent.setAction(BroadCastType.NETWORK_CONNECT_STATE);
        mContext.sendBroadcast(intent);
    }

    /**
     * 发送服务器主推通知信息 (send message from server)
     *
     * @param mContext
     * @param mData
     */
    public static void sendServerData(Context mContext, String mData) {
        Intent intent = new Intent();
        intent.putExtra(BroadCastType.SERVER_NOTICE_DATA, mData);
        intent.setAction(BroadCastType.SERVER_NOTICE);
        mContext.sendBroadcast(intent);
    }


}
