# AndroidSocket #
A simple Android, socket communication, you're gonna love it！<br><br><br><br><br>


How do I use AndroidSocket?（使用说明）
-----

#### 1.First you need to build a service for socket communication before you use it.(在使用之前建一个服务，用于socket通讯)<br>
    startService(new Intent(this, MySocketService.class));


#### 2.In the service, configure the relevant parameters.(服务中配置连接参数)<br>
    Socketer.getInstance(getApplicationContext()).bindServerContect("123.57.56.201", 20083) //配置socket地址和端口
                .setTimeout(15).setEncode("UTF_8") //Configure Timeout and encoding,Timeout unit is seconds配置超时时间与编码
                .setReceiveType(ReceiveType.SEPARATION_SIGN) //Configuring the Receive Type配置接收形式以分隔符接收
                .setEndCharSequence("\r\n") //"\r\n" is End for split 配置结束符
                .setMsgLength(1500).start(); //Send Max bytes配置一次性最多发送的消息字节数
    或者or：
    Socketer.getInstance(getApplicationContext()).bindServerContect("123.57.56.201", 20083)
                .setTimeout(15).setEncode("UTF_8")
                .setReceiveType(ReceiveType.FIXED_LENGTH) //Configuring the Receive Type配置接收形式以分隔符接收
                .setMsgLength(2048) //Fixed length receive 配置固定长度大小接收
                .setMsgLength(1500).start();


#### 3.If the service has unsolicited information to you, you need to register a broadcast, like this:(如果服务有主推消息，你需要注册以下广播)<br>
    IntentFilter intentFilter = new IntentFilter();
           intentFilter.addAction(BroadCastType.SERVER_NOTICE);
           dataReceiver = new MessageReceiver();
           registerReceiver(dataReceiver, intentFilter);


#### 4.Broadcast reception is as follows：(广播接收如下)<br>
    @Override
              public void onReceive(Context context, Intent intent) {
                  if (intent.getAction() == BroadCastType.SERVER_NOTICE) {
                      String dataStr = intent.getStringExtra(BroadCastType.SERVER_NOTICE_DATA);
                      Log.i(TAG, "Data given to me by the server:" + dataStr);
                  }
              }
