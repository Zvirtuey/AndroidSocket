package com.virtue.socketlibrary;

public interface SocketCode {
	int SEND_SUCCESS = 0; //发送成功（send succeed）
	int SEND_FAIL = 1; //发送失败 （send fail）
	int SEND_TIME_OUT = 2; //发送超时 （overtime）
	int SEND_DISCONNECT = -1; //连接断开 （disconnected）

	int RECEIVE_SUCCESS = 0; // 接收成功（receive succeed）
	int RECEIVE_FAIL = 1; // 接收数据错误 （receive error）
	int RECEIVE_TIME_OUT = 2; // 没有可用连接 （unusable）
	int RECEIVE_ANALYZE_ERROR = 3; // 解析数据失败 (analyze error)
	int RECEIVE_DISCONNECT = -1; // 连接断开 （disconnected）

	String DATA_RESULT ="MessageResult"; //服务器返回数据广播Action (broadcast action)

}
