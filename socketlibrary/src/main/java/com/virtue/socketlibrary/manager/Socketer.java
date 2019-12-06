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

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
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
    //private BufferedWriter outputStream;
    private BufferedOutputStream outputStream;
    private DataInputStream inputStream;
    private static final String TAG = "Socketer";
    private String ip = "192.168.1.108"; //服务器地址(server address)
    private int port = 80; //服务器端口(server port)
    private int timeout = 15; //请求超时时长 单位秒(request time-out time unit sec)
    private int sendMaxByteLength = 1500; //发送字节数限制 单位字节(send byte limit unit byte)
    private boolean isRunning = true; //是否接受服务器数据(whether to accept server data)
    public boolean isConnected = false; //是否连接服务器(whether to connect to the server)
    private String encode = "UTF-8"; //编码(encode)
    private String endCharSequence = "\r\n"; //分割结束标识符(split end identifier)
    private String endData = ""; //尾部剩余数据(tail remaining data)
    private int msgLength = 2048; //固定长度分割(fixed-length segmentation)
    private ReceiveType receiveType = SEPARATION_SIGN; //接收方式(how to receive)
    private ParseMode parseMode = ParseMode.AUTO_PARSE; //解析方式(how to resolve)
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
     * Get timeouts
     *
     * @return
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * 设置超时时间
     * Set a timeout
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
     * Get the maximum number of bytes to send
     *
     * @return
     */
    public int getSendMaxByteLength() {
        return sendMaxByteLength;
    }

    /**
     * 设置发送最大字节数
     * Set the maximum number of bytes to send
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
     * Set up the code
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
     * Get the form of reception
     *
     * @return
     */
    public ReceiveType getReceiveType() {
        return receiveType;
    }

    /**
     * 设置接收形式
     * Set up the form of reception
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
     * Get a fixed-length value received
     *
     * @return
     */
    public int getMsgLength() {
        return msgLength;
    }

    /**
     * 设置接收固定长度值
     * Set a receive fixed length value
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
     * Get the receive split
     *
     * @return
     */
    public String getEndCharSequence() {
        return endCharSequence;
    }

    /**
     * 设置接收分隔符
     * Set the receive separator
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
     * Set how the response is resolved (default is automatic resolution)
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
     * How to get the resolution of the response (default is automatic resolution)
     *
     * @return
     */
    public ParseMode getParseMode() {
        return parseMode;
    }

    @Override
    public void run() {
        //开启超时任务
        scheduledExecutorService.scheduleWithFixedDelay(runnable, timeout, 1, TimeUnit.SECONDS);
        while (isRunning) {
            while (socket == null || !socket.isConnected() || isClosedServer(socket)) {
                try {
                    connectSocket();
                    Log.i(TAG, "Connecting the server");
                    if (mReceiveListener != null) {
                        mReceiveListener.onConnected(this);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Connection server failed, please detect the network and server");
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
                    Log.i(TAG, "Connection server waiting to accept message");
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
                                        Log.e(TAG, "Set the number of bytes to receive a fixed size");
                                    }

                                    break;

                                default:
                                    break;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.i(TAG, "No data to receive");
                    }
                } else {
                    inputStream = new DataInputStream(socket.getInputStream());
                    Log.i(TAG, "Service not connected, reconnected...");
                }
            } catch (IOException e) {
                Log.e(TAG, "Server connection failed!");
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
            }
        }
    }

    /**
     * 请求服务器byte[]数据
     * Request server byte[] data
     *
     * @param bytes            请求byte数据
     * @param confirmId        服务器返回的唯一标识
     * @param responseListener 监听器
     */
    public synchronized void sendByteData(byte[] bytes, String confirmId, ResponseListener responseListener) {
        if (responseListener == null) {
            throw new IllegalArgumentException("responseListener must not be null");
        }
        if (isConnected) {
            boolean sendCode = executeSendTask(bytes, confirmId, responseListener);
            if (!sendCode) {
                responseListener.onFail(SocketCode.SEND_FAIL);
            }
        } else {
            responseListener.onFail(SocketCode.DISCONNECT);
        }
    }

    /**
     * 请求服务器String数据
     * Request server String data
     *
     * @param requestData      请求数据 (request data)
     * @param confirmId        服务器返回的唯一标识 (unique identity returned)
     * @param responseListener 监听器 (Listener)
     */
    public void sendStrData(String requestData, String confirmId, ResponseListener responseListener) {
        if (responseListener == null) {
            throw new IllegalArgumentException("responseListener must not be null");
        }
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
     * Request server String data
     *
     * @param requestData 请求数据 (request data)
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
        outputStream = new BufferedOutputStream(socket.getOutputStream());
        //outputStream = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), encode));
        inputStream = new DataInputStream(socket.getInputStream());
        isRunning = true;
    }

    /**
     * 重新连接服务器，可以换服务器地址、端口
     * 如果重新连接的服务器配置信息不一样请先设置各个配置（如编码、解析方式、超时时间等），然后再调用此方法
     * Reconnect the server, you can change the server address and port, If the reconnected server
     * configuration information is different, set up individual configurations (e.g. encoding, resolution, timeout, etc.)
     * before calling this method.
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
     * 关闭连接
     * close socket
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
            }
        } catch (Exception e) {
            e.printStackTrace(); // 关闭连接失败 close fail
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                    socket = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
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
        } catch (Exception e) {
            return true;
        }
    }

    public synchronized boolean executeSendTask(byte[] data, String requestId, ResponseListener mListener) {
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
        bundle.putByteArray("byte", data);
        bundle.putString("msgId", requestId);
        msg.setData(bundle);
        msg.obj = mListener;
        return sendMsgThread.mHandler.sendMessage(msg);
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
                    Bundle mArgs = msg.getData();
                    byte[] mBytes = mArgs.getByteArray("byte");
                    String mData = mArgs.getString("content");
                    String mReqId = mArgs.getString("msgId");
                    ResponseListener responseListener = (ResponseListener) msg.obj;
                    if (mData != null && !mData.equals("") && mData.length() > 0) {
                        mBytes = mData.getBytes();
                    }
                    if (socket == null || !socket.isConnected()) {
                        try {
                            connectSocket();
                        } catch (IOException e) {
                            e.printStackTrace();
                            if (parseMode == ParseMode.AUTO_PARSE) {
                                responseListener.onFail(SocketCode.DISCONNECT);
                            }
                            Log.e(TAG, "Failed to connect to the server, please detect the network -->" + e);
                            return;
                        }
                    }
                    if (outputStream == null) {
                        try {
                            outputStream = new BufferedOutputStream(socket.getOutputStream());
                            //outputStream = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), encode));
                        } catch (IOException e) {
                            e.printStackTrace();
                            if (parseMode == ParseMode.AUTO_PARSE) {
                                responseListener.onFail(SocketCode.SEND_FAIL);
                            }
                            Log.e(TAG, "Failed to send a fetch stream -->" + e);
                            return;
                        }
                    }
                    Log.i(TAG, "The length of the requested data ：" + mData.length() + "\nRequest data ：" + mData);
                    if (mBytes.length > sendMaxByteLength
                            && parseMode == ParseMode.AUTO_PARSE) {
                        responseListener.onFail(SocketCode.SEND_TO_LONG);
                        return;
                    }

                    try {
                        if (mReqId != null && !mReqId.equals("")
                                && parseMode == ParseMode.AUTO_PARSE) {
                            mListenerMap.put(mReqId, responseListener);
                            reqIdList.add(mReqId);
                            long sendTime = System.currentTimeMillis();
                            mTimeOutMap.put(mReqId, sendTime);
                        }
                        outputStream.write(mBytes);
                        outputStream.flush();
                        Log.i(TAG, "message send successful");
                    } catch (IOException e) {
                        e.printStackTrace();
                        if (parseMode == ParseMode.AUTO_PARSE) {
                            responseListener.onFail(SocketCode.SEND_FAIL);
                        }
                        Log.e(TAG, "send failed -->" + e);
                        try {
                            socket.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                            Log.e(TAG, "close stream failed -->" + e1);
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
                    Log.v(TAG, "reqIdList size: " + reqIdList.size() + "\nData returned by the server：" + "\n" + mData);
                    switch (parseMode) {
                        case AUTO_PARSE: //自动解析 Auto Parse
                            autoParseData(mData);

                            break;
                        case MANUALLY_PARSE: //手动解析 Manually Parse
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
                Log.v(TAG, mStr);
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

    public void setOnReceiveListener(OnReceiveListener receiveListener) {
        if (receiveListener == null) {
            throw new IllegalArgumentException("receiveListener must not be null");
        }
        mReceiveListener = receiveListener;

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
