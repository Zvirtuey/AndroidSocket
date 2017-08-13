# AndroidSocket #
A simple Android socket communication, you're gonna love it！<br><br>
![Alt text](https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1497958504351&di=4b57a7e68c56540f95beb62a9bb92cc3&imgtype=0&src=http%3A%2F%2Fe.hiphotos.baidu.com%2Fbaike%2Fw%253D268%2Fsign%3D5b952a087e3e6709be0042f903c79fb8%2F34fae6cd7b899e51f3a0ec3b43a7d933c995d143ad4b2dcf.jpg)<br><br><br>

Add EventBus to your project
-----

#### Gradle <br>
     compile 'com.virtue.androidsocket:AndroidSocket:1.0.2' <br>
     Maven<br>
     <dependency>
       <groupId>com.virtue.androidsocket</groupId>
       <artifactId>AndroidSocket</artifactId>
       <version>1.0.2</version>
       <type>pom</type>
     </dependency>
     <br>

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



#### 5.Send a request to the server（发送请求到服务器）<br>
    Socketer.getInstance(MainActivity.this).sendStrData(reDataStr, "\"seq\":100", new ResponseListener() {
                        @Override
                        public void onSuccess(final String data) {
                            Log.i("Test server data", "callback data：" + data);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tvResponse.setText(data);
                                }
                            });
                        }

                        @Override
                        public void onFail(int failCode) {
                            Log.e("Test server data", "callback error：" + failCode);
                        }
                    });
<p>其中参数1代表是请求的数据，参数2代表是返回数据中的唯一标识，可以是请求ID、token值或者能标识唯一性的字符串</p>
<p>Where parameter 1 represents the requested data, parameter 2 represents a unique identity in the returned data, either a request ID, a token value, or a string that uniquely identifies the uniqueness.</p>



Bugs and Feedback
-----
<p>For bugs, feature requests, and discussion please use <a href="https://github.com/Zvirtuey/AndroidSocket/issues" title="GitHub Issues">GitHub Issues</a></p>


