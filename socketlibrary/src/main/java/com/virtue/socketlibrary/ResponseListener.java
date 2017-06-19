package com.virtue.socketlibrary;

/**
 * Created by virtue on 2017/6/10.
 */

public interface ResponseListener {
    void onSuccess(String data);
    void onFail(int failCode);
}
