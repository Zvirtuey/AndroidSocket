package com.virtue.socketlibrary.manager;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.virtue.socketlibrary.type.ParseMode;
import com.virtue.socketlibrary.type.ReceiveType;
import com.virtue.socketlibrary.utils.OnReceiveListener;
import com.virtue.socketlibrary.utils.ResponseListener;
import com.virtue.socketlibrary.utils.SendBroadCastUtil;
import com.virtue.socketlibrary.utils.SocketCode;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.virtue.socketlibrary.type.ReceiveType.SEPARATION_SIGN;

/**
 * Created by virtue on 2017/2/15.
 * <p>
 * https://github.com/Zvirtuey/AndroidSocket
 */

public class Socketer extends Thread {

    private static Context mContext;
    public Socket socket;
    private BufferedWriter outputStream;
    private DataInputStream inputStream;
    private static final String TAG = "Socketer";
    private String ip = "192.168.1.108"; //服务器地址
    private int port = 80; //服务器端口
    private int timeout = 15; //请求超时时长 单位秒（Unit sec）
    private int sendMaxByteLength = 1500; //发送字节数限制 单位字节（Unit byte）
    private boolean isRunning = true; //是否接受服务器数据
    public boolean isConnected = false; // 是否连接服务器
    private String encode = "UTF-8"; //编码
    private String endCharSequence = "\r\n"; //分割结束标识符
    private String endData = ""; //尾部剩余数据
    private int msgLength = 2048; //固定长度分割
    private ReceiveType receiveType = SEPARATION_SIGN; //接收形式
    private ParseMode parseMode = ParseMode.AUTO_PARSE; //接收形式
    private SendMsgThread sendMsgThread;
    private ReceiveMsgThread receiveMsgThread;
    private ConcurrentHashMap<String, ResponseListener> mListenerMap;
    private ConcurrentHashMap<String, Long> mTimeOutMap;
    private List<String> reqIdList;
    private OnReceiveListener mReceiveListener;

    private Socketer() {
        sendMsgThread = new SendMsgThread();
        receiveMsgThread = new ReceiveMsgThread();
        mListenerMap = new ConcurrentHashMap();
        mTimeOutMap = new ConcurrentHashMap();
        reqIdList = Collections.synchronizedList(new ArrayList<String>());
    }

    public static final Socketer getInstance(Context context) {
        mContext = context.getApplicationContext();
        return SingleHolder.INSTANCE;
    }

    private static class SingleHolder {
        private static final Socketer INSTANCE = new Socketer(); // 创建实例的地方
    }

    public Socketer bindServerConnect(String address, int port) {
        this.ip = address;
        this.port = port;
        return this;
    }

    public void onStart() {
        this.start();
    }

    /**
     * 获取超时时间
     *
     * @return
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * 设置超时时间
     *
     * @param timeout
     * @return
     */
    public Socketer setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * 获取发送最大字节数
     *
     * @return
     */
    public int getSendMaxByteLength() {
        return sendMaxByteLength;
    }

    /**
     * 设置发送最大字节数
     *
     * @param sendMaxByteLength
     * @return
     */
    public Socketer setSendMaxByteLength(int sendMaxByteLength) {
        this.sendMaxByteLength = sendMaxByteLength;
        return this;
    }

    public String getEncode() {
        return encode;
    }

    /**
     * 设置编码
     *
     * @param encode
     * @return
     */
    public Socketer setEncode(String encode) {
        this.encode = encode;
        return this;
    }

    /**
     * 获取接收形式
     *
     * @return
     */
    public ReceiveType getReceiveType() {
        return receiveType;
    }

    /**
     * 设置接收形式
     *
     * @param receiveType
     * @return
     */
    public Socketer setReceiveType(ReceiveType receiveType) {
        this.receiveType = receiveType;
        return this;
    }

    /**
     * 获取接收的固定长度值
     *
     * @return
     */
    public int getMsgLength() {
        return msgLength;
    }

    /**
     * 设置接收固定长度值
     *
     * @param msgLength
     * @return
     */
    public Socketer setMsgLength(int msgLength) {
        this.msgLength = msgLength;
        return this;
    }

    /**
     * 获取接收分割符
     *
     * @return
     */
    public String getEndCharSequence() {
        return endCharSequence;
    }

    /**
     * 设置接收分隔符
     *
     * @param endCharSequence
     * @return
     */
    public Socketer setEndCharSequence(String endCharSequence) {
        this.endCharSequence = endCharSequence;
        return this;
    }

    /**
     * 设置响应的解析方式（默认是自动解析）
     *
     * @param mMode 解析方式枚举对象（ParseMode）
     * @return
     */
    public Socketer setParseMode(ParseMode mMode) {
        this.parseMode = mMode;
        return this;
    }


    /**
     * 获取响应时的解析方式（默认是自动解析）
     *
     * @return
     */
    public ParseMode getParseMode() {
        return parseMode;
    }

    @Override
    public void run() {
        while (isRunning) {
            while (socket == null || !socket.isConnected() || isClosedServer(socket)) {
                try {
                    connectSocket();
                    Log.i(TAG, "连接服务器");
                    if (mReceiveListener != null) {
                        mReceiveListener.onConnected(this);
                    }
                    //开启超时任务
                    scheduledExecutorService.scheduleWithFixedDelay(runnable, timeout, 1, TimeUnit.SECONDS);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "run连接服务器失败,请检测网络和服务器");
                    if (isConnected == true) {
                        isConnected = false;
                        if (mReceiveListener != null) {
                            mReceiveListener.onDisconnected(this);
                        }
                        SendBroadCastUtil.sendNetworkStateBroadcast(mContext, isConnected);
                    }
                    closeConnect();
                    try {
                        sleep(3000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            try {
                if (socket.isConnected() && inputStream != null) {
                    if (isConnected == false) {
                        isConnected = true;
                        if (mReceiveListener != null) {
                            mReceiveListener.onConnected(this);
                        }
                        SendBroadCastUtil.sendNetworkStateBroadcast(mContext, isConnected);
                    }
                    Log.i(TAG, "连接服务器等待接受消息");
                    try {
                        int len = 0;
                        byte[] temp;
                        if (msgLength > 0) {
                            temp = new byte[msgLength];
                        } else {
                            temp = new byte[2048];
                        }
                        while ((len = inputStream.read(temp == null ? temp = new byte[2048] : temp)) != -1) {

                            switch (receiveType) {
                                case SEPARATION_SIGN: // 按包尾字符分割信息
                                    String tempData = new String(temp, 0, len);
                                    StringBuilder builder = new StringBuilder();
                                    builder.append(endData);
                                    builder.append(tempData);
                                    String totalStr = builder.toString();
                                    while (totalStr.contains(endCharSequence)) {
                                        String[] splitArray = totalStr.split(endCharSequence, 2);
                                        String mData = splitArray[0];
                                        endData = splitArray[1];
                                        totalStr = endData;
                                        executeReceiveTask(mData);
                                    }

                                    break;

                                case FIXED_LENGTH: // 按固定长度分割信息
                                    if (msgLength > 0) {
                                        String allData = new String(temp, 0, len);
                                        executeReceiveTask(allData);
                                    } else {
                                        Log.e(TAG, "请设置接收固定大小的字节数");
                                    }

                                    break;

                                default:
                                    break;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.i(TAG, "没有可接收的数据");
                    }
                } else {
                    inputStream = new DataInputStream(socket.getInputStream());
                    Log.i(TAG, "服务未连接，重连中...");
                }
            } catch (IOException e) {
                Log.e(TAG, "服务器连接失败！");
                e.printStackTrace();
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    socket = null;
                }
                isConnected = false;
                if (mReceiveListener != null) {
                    mReceiveListener.onDisconnected(this);
                }
                // 没有可接收的数据
            }
        }
    }

    /**
     * 请求服务器String数据
     *
     * @param requestData      请求数据
     * @param confirmId        服务器返回的唯一标识
     * @param responseListener 监听器
     */
    public void sendStrData(String requestData, String confirmId, ResponseListener responseListener) {
        if (isConnected) {
            boolean sendCode = executeSendTask(requestData, confirmId, responseListener);
            if (!sendCode) {
                responseListener.onFail(SocketCode.SEND_FAIL);
            }
        } else {
            responseListener.onFail(SocketCode.DISCONNECT);
        }
    }

    /**
     * 请求服务器String数据
     *
     * @param requestData 请求数据
     * @return
     */
    public boolean sendStrData(String requestData) {
        boolean sendResult = false;
        if (isConnected) {
            sendResult = executeSendTask(requestData, null, null);
        }
        return sendResult;
    }

    private void connectSocket() throws IOException {
        endData = "";
        socket = new Socket();
        SocketAddress socketAddress = new InetSocketAddress(ip, port);
        socket.connect(socketAddress, 5 * 1000);
        socket.setTcpNoDelay(true); // 关闭 Nagle 算法
        socket.setReceiveBufferSize(1024 * 10000);
        outputStream = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), encode));
        inputStream = new DataInputStream(socket.getInputStream());
        isRunning = true;
    }

    /**
     * 重新连接服务器，可以换服务器地址、端口
     * 如果重新连接的服务器配置信息不一样请先设置各个配置（如编码、解析方式、超时时间等），然后再调用此接口
     *
     * @param ip
     * @param port
     */
    public void reConnectSever(String ip, int port) {
        boolean isAlive = isAlive();
        if (isAlive) {
            Socketer.getInstance(mContext).closeConnect();
            Socketer.getInstance(mContext).closeSocketer();
        } else {
            bindServerConnect(ip, port).start();
        }
        Socketer.getInstance(mContext).bindServerConnect(ip, port);

    }

    /**
     * 关闭连接 close socket
     */
    public void closeConnect() {
        try {
            if (socket != null) {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace(); // 关闭连接失败 close fail
        }
    }

    public void closeSocketer() {
        reqIdList.clear();
        mTimeOutMap.clear();
        mListenerMap.clear();
        ip = "";
    }

    public Boolean isClosedServer(Socket socket) {
        try {
            socket.sendUrgentData(0xFF);// 发送1个字节的紧急数据，默认情况下，服务器端没有开启紧急数据处理，不影响正常通信
            return false;
        } catch (Exception se) {
            return true;
        }
    }

    public synchronized boolean executeSendTask(String data, String requestId, ResponseListener mListener) {
        if (sendMsgThread == null || !sendMsgThread.isAlive()) {
            sendMsgThread = new SendMsgThread();
            synchronized (sendMsgThread) {
                sendMsgThread.start();
                try {
                    sendMsgThread.wait();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (sendMsgThread.mLooper == null || sendMsgThread.mHandler == null) {
            Log.e(TAG, "workerThread mLooper mHandler ERROR！");
            return false;
        }
        Message msg = Message.obtain();
        Bundle bundle = new Bundle();
        bundle.putString("content", data);
        bundle.putString("msgId", requestId);
        msg.setData(bundle);
        msg.obj = mListener;
        return sendMsgThread.mHandler.sendMessage(msg);
    }

    public synchronized boolean executeReceiveTask(String data) {
        if (receiveMsgThread == null || !receiveMsgThread.isAlive()) {
            receiveMsgThread = new ReceiveMsgThread();
            synchronized (receiveMsgThread) {
                receiveMsgThread.start();
                try {
                    receiveMsgThread.wait();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (receiveMsgThread.mReceiveLooper == null || receiveMsgThread.mHandler == null) {
            Log.e(TAG, "receiveMsgThread mLooper mHandler ERROR！");
            return false;
        }
        Message msg = Message.obtain();
        msg.obj = data;
        return receiveMsgThread.mHandler.sendMessage(msg);
    }

    public class SendMsgThread extends Thread {
        protected final String TAG = "SendMsgThread";
        private Handler mHandler;
        private Looper mLooper;

        public void run() {
            Looper.prepare();
            mLooper = Looper.myLooper();
            mHandler = new Handler(mLooper) {
                public void handleMessage(Message msg) {
                    int sendCode = SocketCode.DISCONNECT;
                    Bundle mArgs = msg.getData();
                    String mData = mArgs.getString("content");
                    String mReqId = mArgs.getString("msgId");
                    ResponseListener responseListener = (ResponseListener) msg.obj;
                    if (socket == null || !socket.isConnected()) {
                        try {
                            connectSocket();
                        } catch (IOException e) {
                            e.printStackTrace();
                            sendCode = SocketCode.DISCONNECT;// 连接服务器失败
                            responseListener.onFail(sendCode);
                            Log.e(TAG, "连接服务器失败,请检测网络" + e);
                            return;
                        }
                    }
                    if (outputStream == null) {
                        try {
                            outputStream = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), encode));
                        } catch (IOException e) {
                            e.printStackTrace();
                            sendCode = SocketCode.SEND_FAIL;// 获取输出流失败
                            responseListener.onFail(sendCode);
                            Log.e(TAG, "发送获取流失败" + e);
                            return;
                        }
                    }
                    Log.i(TAG, "请求的数据长度：" + mData.length() + "\n请求数据：" + mData);
                    if (mData.getBytes().length > sendMaxByteLength) {
                        responseListener.onFail(SocketCode.SEND_TO_LONG);
                        return;
                    }
                    try {
                        if (mReqId != null && !mReqId.equals("") && parseMode == ParseMode.AUTO_PARSE) {
                            mListenerMap.put(mReqId, responseListener);
                            reqIdList.add(mReqId);
                            long sendTime = System.currentTimeMillis();
                            mTimeOutMap.put(mReqId, sendTime);
                        }
                        outputStream.write(mData);
                        outputStream.flush();
                        Log.i(TAG, "消息已发送");
                    } catch (IOException e) {
                        e.printStackTrace();
                        sendCode = SocketCode.SEND_FAIL; // 发送请求失败
                        responseListener.onFail(sendCode);
                        Log.e(TAG, "发送请求失败" + e);
                        try {
                            socket.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                            Log.e(TAG, "关流失败" + e1);
                        }
                        socket = null;
                        return;
                    }
                }
            };
            synchronized (this) {
                try {
                    notify();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Looper.loop();
        }
    }

    public class ReceiveMsgThread extends Thread {
        protected final String TAG = "ReceiveMsgThread";
        private Handler mHandler;
        private Looper mReceiveLooper;

        public void run() {
            Looper.prepare();
            mReceiveLooper = Looper.myLooper();
            mHandler = new Handler(mReceiveLooper) {
                public void handleMessage(Message msg) {
                    String mData = (String) msg.obj;
                    Log.w(TAG, "服务器返回的数据：" + "reqIdList: " + reqIdList.size() + "\n" + mData);
                    switch (parseMode) {
                        case AUTO_PARSE: //自动解析
                            autoParseData(mData);

                            break;
                        case MANUALLY_PARSE: //手动解析
                            if (mReceiveListener != null) {
                                mReceiveListener.onResponse(mData);
                            }

                            break;

                        default:

                            break;
                    }
                }
            };
            synchronized (this) {
                try {
                    notify();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Looper.loop();
        }

        private void autoParseData(String mData) {
            if (reqIdList == null || reqIdList.size() <= 0) {
                SendBroadCastUtil.sendServerData(mContext, mData);
                return;
            }
            for (String mStr : reqIdList) {
                Log.e(TAG, mStr);
                if (mData.contains(mStr)) {
                    if (mListenerMap.containsKey(mStr)) {
                        ResponseListener responseListener = mListenerMap.get(mStr);
                        responseListener.onSuccess(mData);
                        mListenerMap.remove(mStr);
                        reqIdList.remove(mStr);
                        if (mTimeOutMap.containsKey(mStr)) {
                            mTimeOutMap.remove(mStr);
                        }
                    }
                } else {
                    SendBroadCastUtil.sendServerData(mContext, mData);
                }
            }
        }
    }

    public void setOnReceiveListener(OnReceiveListener mReceiveListener) {
        if (mReceiveListener != null) {
            this.mReceiveListener = mReceiveListener;
        } else {
            Log.e(TAG, "mReceiveListener is null");
        }
    }

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(3);
    //超时处理（Time out）
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (mTimeOutMap != null && mTimeOutMap.size() > 0) {
                long receiveTime = System.currentTimeMillis();
                Iterator<Map.Entry<String, Long>> entries = mTimeOutMap.entrySet().iterator();
                while (entries.hasNext()) {
                    Map.Entry<String, Long> entry = entries.next();
                    String reStrId = entry.getKey();
                    long sendTime = entry.getValue();
                    if (receiveTime - sendTime > timeout * 1000) {
                        if (mListenerMap.containsKey(reStrId)) {
                            ResponseListener responseListener = mListenerMap.get(reStrId);
                            responseListener.onFail(SocketCode.TIME_OUT);
                            mListenerMap.remove(reStrId);
                            mTimeOutMap.remove(reStrId);
                            if (reqIdList.contains(reStrId)) {
                                reqIdList.remove(reStrId);
                            }
                        }
                    }
                    System.out.println("Time out Key = " + entry.getKey() + ", Time outValue = " + entry.getValue());
                }
            }
        }
    };


}
