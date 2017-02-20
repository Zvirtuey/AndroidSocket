package com.virtue.socketlibrary;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by virtue on 2017/2/15.
 */

public class Socketer {

    private static FastSocket fastSocket = null;
    private static Context mContext;

    private Socketer() {
        // 私有的构造函数
    }

    public static final Socketer getInstance(Context context) {
        fastSocket = FastSocket.getInstance();
        fastSocket.setContext(context);
        mContext = context;
        return SingleHolder.INSTANCE;
    }

    // 定义的静态内部类
    private static class SingleHolder {
        private static final Socketer INSTANCE = new Socketer(); // 创建实例的地方
    }


    /**
     * 设置服务器ip
     *
     * @param ip
     */
    public void setSocketIP(String ip) {
        fastSocket.setIp(ip);
    }

    /**
     * 设置服务器的端口
     *
     * @param port
     */
    public void setSocketPort(int port) {
        fastSocket.setPort(port);
    }

    /**
     * 设置服务器的ip，端口，连接超时时间
     *
     * @param ip
     * @param port
     * @param timeOut
     */
    public void setSocketInfo(String ip, int port, int timeOut) {
        fastSocket.setIp(ip);
        fastSocket.setPort(port);
        fastSocket.setTimeout(timeOut);
    }

    /**
     * 设置通讯的编码（针对与byte与String之间转换）
     *
     * @param enCode
     */
    public void setSocketEnCode(String enCode) {
        fastSocket.setEncode(enCode);
    }

    /**
     * 设置分割接收信息的结束标记符
     *
     * @param slit
     */
    public void setReciveMsgSlitChar(String slit) {
        fastSocket.setReceiveType(0);
        fastSocket.setEndCharSequence(slit);
    }

    /**
     * 设置分割接收消息的固定长度
     *
     * @param length
     */
    public void setReciveMsgSlitLength(int length) {
        fastSocket.setReceiveType(1);
        fastSocket.setMsgLength(length);
    }

    /**
     * 发送byte数据
     *
     * @param bytes
     * @return
     */
    public int sendData(byte[] bytes) {
        return fastSocket.sendByteData(bytes);
    }

    /**
     * 发送String数据
     *
     * @param data
     * @return
     */
    public int sendData(String data) {
        return fastSocket.sendByteData(data);
    }

    /**
     * 开启socket服务
     */
    public void startSocket() {
        if (checkSocketInfo()) {
            fastSocket.start();
        }
    }

    /**
     * 停止socket服务
     */
    public void stopSocket() {
        fastSocket.closeConnect();
    }

    /**
     * 重新连接socket服务
     */
    public void reConnectSocket() {
        if (checkSocketInfo()) {
            try {
                fastSocket.connectSocket();
                fastSocket.setRuning(true);
                fastSocket.run();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 校验socket服务设置
     */
    private boolean checkSocketInfo() {
        String ip = fastSocket.getIp();
        int port = fastSocket.getPort();
        if (ip == null || TextUtils.equals(ip, "")) {
            Toast.makeText(mContext, "请设置服务器ip", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (port == 0) {
            Toast.makeText(mContext, "请设置服务器端口", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
