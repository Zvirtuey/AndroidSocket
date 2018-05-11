package com.virtue.socketlibrary.utils;

import com.virtue.socketlibrary.manager.Socketer;

/**
 * Created by Virtue on 2017/10/24.
 */

public interface OnReceiveListener {
    void onConnected(Socketer socketer);
    void onDisconnected(Socketer socketer);
    void onResponse(String data);

}
