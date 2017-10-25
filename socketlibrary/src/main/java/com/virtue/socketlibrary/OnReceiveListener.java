package com.virtue.socketlibrary;

/**
 * Created by Virtue on 2017/10/24.
 */

public interface OnReceiveListener {
    void onConnected(Socketer socketer);
    void onDisconnected(Socketer socketer);
    void onResponse(String data);

}
