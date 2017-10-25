package com.virtue.socketlibrary;

/**
 * Created by virtue on 2017/6/10.
 */

public interface BroadCastType {
    String IS_CONNECTED="is_connected"; //网络连接携带参数标记
    String NETWORK_CONNECT_STATE="network_connect_state"; //发送网络连接的广播Action
    String SERVER_NOTICE_DATA ="server_data"; //服务器主推数据参数标记
    String SERVER_NOTICE="server_notice"; //发送服务器主推的广播Action
    String RESPONSE_RESULT_DATA ="response_result_data"; //请求响应的数据参数标记
//    String RESPONSE_RESULT_LISTENER ="response_result_listener"; //请求结果数据对应的监听器参数标记
//    String RESPONSE_RESULT="server_result"; //发送请求获得响应的广播Action
}
