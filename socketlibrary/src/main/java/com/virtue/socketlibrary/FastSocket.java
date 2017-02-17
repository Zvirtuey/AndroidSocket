package com.virtue.socketlibrary;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Arrays;

public class FastSocket extends Thread {

    public Socket socket;
    private BufferedWriter outputStream;
    private DataInputStream inputStream;
    private static final String TAG = "FastSocket";
    private String ip = ""; //服务器地址
    private int port = 0; //服务器端口
    private int timeout = 15000; //连接服务器超时时长
    private boolean isRuning = true; //是否接受服务器数据
    private String encode = "UTF-8"; //编码
    private String endCharSequence = " "; //分割结束标识符
    private int msgLength; //固定长度分割
    private int receiveType = 0; //接收形式
    private Context context;

    private int responseCode;
    private int requestCode;

    private FastSocket() {
        // 私有的构造函数
    }

    public static final FastSocket getInstance() {
        return SingletonHolder.INSTANCE;
    }

    // 定义的静态内部类
    private static class SingletonHolder {
        private static final FastSocket INSTANCE = new FastSocket(); // 创建实例的地方
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public boolean isRuning() {
        return isRuning;
    }

    public void setRuning(boolean runing) {
        isRuning = runing;
    }

    public String getEncode() {
        return encode;
    }

    public void setEncode(String encode) {
        this.encode = encode;
    }

    public String getEndCharSequence() {
        return endCharSequence;
    }

    public void setEndCharSequence(String endCharSequence) {
        this.endCharSequence = endCharSequence;
    }

    public int getMsgLength() {
        return msgLength;
    }

    public void setMsgLength(int msgLength) {
        this.msgLength = msgLength;
    }

    public int getReceiveType() {
        return receiveType;
    }

    public void setReceiveType(int receiveType) {
        this.receiveType = receiveType;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void run() {

        while (isRuning) {
            Log.i(TAG, "连接服务器等待接受消息");
            while (socket == null || isServerClose(socket)) {
                try {
                    connectSocket();
                    Log.i(TAG, "连接服务器");
                } catch (IOException e) {
                    e.printStackTrace();
                    responseCode = SocketCode.RECEIVE_DISCONNECT;// 连接服务器失败
                    Log.e(TAG, "run连接服务器失败,请检测网络和服务器");
                    try {
                        sleep(3000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }

            try {
                if (socket.isConnected()) {
                    int len = 0;
                    byte[] temp = new byte[2048];
                    while ((len = inputStream.read(temp)) != -1) {
                        switch (receiveType) {
                            case 0: //按包尾字符分割信息
                                String data = new String(temp, encode);
                                if (data.contains(endCharSequence)) {
                                    byte[] cont = Arrays.copyOf(temp, len - endCharSequence.length());
                                    data = data.substring(0, len - endCharSequence.length());
                                    Intent intent = new Intent();
                                    intent.putExtra("string", data);
                                    intent.putExtra("byte", cont);
                                    intent.setAction(SocketCode.DATA_RESULT);
                                    context.sendBroadcast(intent);
                                    temp = null;
                                    responseCode = SocketCode.RECEIVE_SUCCESS;
                                }

                                break;

                            case 1: //按固定长度分割信息
                                if (msgLength > 0 && msgLength == len) {
                                    String msg = new String(temp, encode);
                                    Intent intent = new Intent();
                                    intent.putExtra("string", msg);
                                    intent.putExtra("byte", temp);
                                    intent.setAction(SocketCode.DATA_RESULT);
                                    context.sendBroadcast(intent);
                                    temp = null;
                                    responseCode = SocketCode.RECEIVE_SUCCESS;
                                }

                                break;

                            default:
                                break;
                        }
                    }
                } else {
                    connectSocket();
                    responseCode = SocketCode.RECEIVE_TIME_OUT;// 没有可用连接
                    Log.i(TAG, "没有可用的连接");
                }
            } catch (IOException e) {
                Log.e(TAG, "没有可接收的数据");
                e.printStackTrace();
                responseCode = SocketCode.RECEIVE_FAIL;// 没有可接收的数据
            }
        }

    }

    public void connectSocket() throws IOException {
        socket = new Socket(ip, port);
        socket.setSoTimeout(timeout);// 设置阻塞时间
        socket.setTcpNoDelay(true);// 关闭 Nagle 算法
        outputStream = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), encode));
        inputStream = new DataInputStream(socket.getInputStream());

    }

    public Boolean isServerClose(Socket socket) {
        try {
            socket.sendUrgentData(0xFF);// 发送1个字节的紧急数据，默认情况下，服务器端没有开启紧急数据处理，不影响正常通信
            return false;
        } catch (Exception se) {
            return true;
        }
    }

    public synchronized int sendByteData(final byte[] bytes) {
        requestCode = SocketCode.SEND_SUCCESS;
        new Thread(new Runnable() {

            @Override
            public void run() {

                if (socket == null) {
                    try {
                        connectSocket();
                    } catch (IOException e) {
                        e.printStackTrace();
                        requestCode = SocketCode.SEND_DISCONNECT;// 连接服务器失败
                        Log.e(TAG, "连接服务器失败,请检测网络");
                        return;
                    }
                } else {
                    if (outputStream == null) {
                        try {
                            outputStream = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), encode));
                        } catch (IOException e) {
                            e.printStackTrace();
                            requestCode = SocketCode.SEND_TIME_OUT;// 获取输出流失败
                        }
                    } else {
                        try {
                            outputStream.write(new String(bytes, encode));
                            outputStream.flush();
                            Log.i(TAG, "消息已发送");
                            requestCode = SocketCode.SEND_SUCCESS;
                            return;
                        } catch (IOException e) {
                            e.printStackTrace();
                            requestCode = SocketCode.SEND_FAIL;// 发送请求失败
                            return;
                        }
                    }
                }
            }
        }).start();
        return requestCode;

    }

    public synchronized int sendByteData(final String data) {
        requestCode = SocketCode.SEND_SUCCESS;
        new Thread(new Runnable() {

            @Override
            public void run() {
                BufferedWriter bf = null;
                if (socket == null) {
                    try {
                        connectSocket();
                    } catch (IOException e) {
                        e.printStackTrace();
                        requestCode = SocketCode.SEND_DISCONNECT;// 连接服务器失败
                        Log.e(TAG, "连接服务器失败,请检测网络");
                        return;
                    }
                } else {
                    if (outputStream == null) {
                        try {
                            outputStream = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), encode));
                        } catch (IOException e) {
                            e.printStackTrace();
                            requestCode = SocketCode.SEND_TIME_OUT;// 获取输出流失败
                        }
                    } else {
                        try {
                            outputStream.write(data);
                            outputStream.flush();
                            Log.i(TAG, "消息已发送");
                            requestCode = SocketCode.SEND_SUCCESS;
                            return;
                        } catch (IOException e) {
                            e.printStackTrace();
                            requestCode = SocketCode.SEND_FAIL;// 发送请求失败
                            return;
                        }
                    }
                }
            }
        }).start();
        return requestCode;

    }


    /**
     * 关闭连接
     */
    public void closeConnect() {
        try {
            if (socket != null) {
                inputStream.close();
                outputStream.close();
                socket.close();
                isRuning = false;
            }
        } catch (Exception e) {
            e.printStackTrace();// 关闭连接失败
        }

    }

    /**
     * 将12位数组的后四位转为int
     *
     * @param b
     * @param ioffset
     * @return
     */
    public static int byteArrayToInt(byte[] b, int ioffset) {
        return b[ioffset + 3] & 0xFF | (b[ioffset + 2] & 0xFF) << 8 | (b[ioffset + 1] & 0xFF) << 16
                | (b[ioffset] & 0xFF) << 24;
    }

}
