# AndroidSocket #
A simple Android socket communication, you're gonna love it！<br><br>
![Alt text](https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1497958504351&di=4b57a7e68c56540f95beb62a9bb92cc3&imgtype=0&src=http%3A%2F%2Fe.hiphotos.baidu.com%2Fbaike%2Fw%253D268%2Fsign%3D5b952a087e3e6709be0042f903c79fb8%2F34fae6cd7b899e51f3a0ec3b43a7d933c995d143ad4b2dcf.jpg)<br><br><br>

Add AndroidSocket to your project
-----

#### Gradle <br>
     compile 'com.virtue.androidsocket:AndroidSocket:1.0.6'

#### Maven <br>
     <dependency>
       <groupId>com.virtue.androidsocket</groupId>
       <artifactId>AndroidSocket</artifactId>
       <version>1.0.6</version>
       <type>pom</type>
     </dependency>



How do I use AndroidSocket?（使用说明）
-----

#### First you need to build a service for socket communication and heartbeat tasks before you use it.(在使用之前建一个服务，用于socket通讯, 在服务中可以建立自己的心跳任务)<br>
    startService(new Intent(this, MySocketService.class));



#### In the service, configure the relevant parameters.(服务中配置连接参数)<br>
    Socketer.getInstance(getApplicationContext()).bindServerConnect("123.57.56.201", 20083) //配置socket地址和端口
                .setTimeout(15).setEncode("UTF_8") //Configure Timeout and encoding,Timeout unit is seconds配置超时时间与编码
                .setReceiveType(ReceiveType.SEPARATION_SIGN) //Configuring the Receive Type配置接收形式以分隔符接收
                .setEndCharSequence("\r\n") //"\r\n" is End for split 配置结束符
                .setSendMaxByteLength(1500).start(); //Send Max bytes配置一次性最多发送的消息字节数
    或者or：
    Socketer.getInstance(getApplicationContext()).bindServerConnect("123.57.56.201", 20083)
                .setTimeout(15).setEncode("UTF_8")
                .setReceiveType(ReceiveType.FIXED_LENGTH) //Configuring the Receive Type配置接收形式以分隔符接收
                .setMsgLength(2048) //Fixed length receive 配置固定长度大小接收
                .setSendMaxByteLength(1500).start(); //配置一次性最多发送的消息字节数


<br>

Case1. Auto Parse ! （自动解析包含服务器主推通知和请求响应两种数据）
-----

#### If the service has unsolicited information to you, you need to register a broadcast, like this:(如果服务有主推通知消息，你需要注册以下广播)<br>

      //Set parse to Auto
      Socketer.getInstance(MainActivity.this).setParseMode(ParseMode.AUTO_PARSE);

      IntentFilter intentFilter = new IntentFilter();
      intentFilter.addAction(BroadCastType.SERVER_NOTICE);
      MessageReceiver dataReceiver = new MessageReceiver();
      registerReceiver(dataReceiver, intentFilter);



#### Broadcast reception is as follows：(广播接收如下)<br>
    @Override
             public void onReceive(Context context, Intent intent) {
                  if (intent.getAction() == BroadCastType.SERVER_NOTICE) {
                      String dataStr = intent.getStringExtra(BroadCastType.SERVER_NOTICE_DATA);
                      Log.i(TAG, "Data given to me by the server:" + dataStr);
                  }
             }



#### Send a request to the server（发送请求到服务器）<br>
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
<br>
<p>其中参数1代表是请求的数据，参数2代表是返回数据中的唯一标识，可以是请求ID、token值或者能标识唯一性的字符串</p>
<p>Where parameter 1 represents the requested data, parameter 2 represents a unique identity in the returned data, either a request ID, a token value, or a string that uniquely identifies the uniqueness.</p>
<br>
<br>

Case2. Manually Parse!suggest.（手动解析没有服务主推通知和请求响应之分，完全由自己自定义解析）
------

#### If you want to parse the response data yourself（如果想自己解析响应数据）<br>
     //Set parse to Manual
     Socketer.getInstance(MainActivity.this).setParseMode(ParseMode.MANUALLY_PARSE);

#### Set Listener for response（设置监听响应）<br>
       Socketer.getInstance(MainActivity.this).setOnReceiveListener(new OnReceiveListener() {
                   @Override
                   public void onConnected(Socketer socketer) {

                   }

                   @Override
                   public void onDisconnected(Socketer socketer) {

                   }

                   @Override
                   public void onResponse(final String data) {
                       runOnUiThread(new Runnable() {
                           @Override
                           public void run() {
                               //response data
                               tvResponse.setTextColor(getResources().getColor(R.color.blue));
                               tvResponse.setText(data);
                               //... your parse ...
                           }
                       });
                   }
        });

#### Send data（发送数据）<br>
      Socketer.getInstance(MainActivity.this).sendStrData(reDataStr); //request



<br><br>

Other
-----

#### If you want to reconnect to other servers(如过你想重新连接另一个服务器)<br>
     //If the configuration of another server is different, first configure the information
     //如果另一服务器配置不一样请先配置信息
     //Socketer.getInstance(getApplicationContext()).setEncode("UTF_8");
     //Socketer.getInstance(getApplicationContext()).setReceiveType(ReceiveType.SEPARATION_SIGN);
     //Socketer.getInstance(getApplicationContext()).setTimeout(15); ...
      Socketer.getInstance(getApplicationContext()).reConnectSever(ip, port);

<br><br>

Bugs and Feedback
-----

<p>For bugs, feature requests, and discussion please use <a href="https://github.com/Zvirtuey/AndroidSocket/issues" title="GitHub Issues">GitHub Issues</a></p>


