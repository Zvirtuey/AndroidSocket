package com.virtue.socketlibrary;

/**
 * Created by virtue on 2017/6/10.
 */

public interface BroadCastType {
    String IS_CONNECTED="is_connected"; //网络连接携带参数标记
    String NETWORK_CONNECT_STATE="network_connect_state"; //发送网络连接广播的Action
    String SERVER_NOTICE_DATA ="server_data"; //服务器主推数据参数标记
    String SERVER_NOTICE="server_notice"; //发送服务器主推广播的Action
}
