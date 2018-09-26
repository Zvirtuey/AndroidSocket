package com.virtue.socketlibrary.utils;

import com.virtue.socketlibrary.manager.Socketer;

/**
 * Created by Virtue on 2017/10/24.
 */

public interface OnReceiveListener {
    //socket 连接成功
    void onConnected(Socketer socketer);

    //socket 连接失败
    void onDisconnected(Socketer socketer);

    //socket 数据响应
    void onResponse(String data);

}
