package com.virtue.socketlibrary.utils;

/**
 * Created by virtue on 2017/6/10.
 */

public interface ResponseListener {

    //response data
    void onSuccess(String data);

    //failed code
    void onFail(int failCode);
}
