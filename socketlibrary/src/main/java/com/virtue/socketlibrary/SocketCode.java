package com.virtue.socketlibrary;

public interface SocketCode {
	int SUCCESS = 0; //发送成功（send succeed）
	int SEND_FAIL = 1; //发送失败 （send fail）
	int TIME_OUT = 2; //发送超时 （overtime）
	int SEND_TO_LONG = 3; //发送数据过长 （send so long）
	int DISCONNECT = -1; //连接断开 （disconnected）

}
