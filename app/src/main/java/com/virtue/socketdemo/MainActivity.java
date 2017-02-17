package com.virtue.socketdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.virtue.socketdemo.service.MySocketService;
import com.virtue.socketlibrary.FastSocket;
import com.virtue.socketlibrary.SocketCode;

public class MainActivity extends AppCompatActivity {

    private TextView tvRequest;
    private TextView tvResponse;
    private Button btSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startService(new Intent(this, MySocketService.class));
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SocketCode.DATA_RESULT);
        MessageReceiver dataReceiver = new MessageReceiver();
        registerReceiver(dataReceiver, intentFilter);

        tvRequest = (TextView) findViewById(R.id.tv_request);
        tvResponse = (TextView) findViewById(R.id.tv_response);
        btSend = (Button) findViewById(R.id.bt_send);

        final StringBuilder builder = new StringBuilder();
        builder.append("acProtocolVersion:PV1.01");
        builder.append(':');
        builder.append("acDeviceID:");
        builder.append(':');
        builder.append("iParam0:0");
        builder.append(':');
        builder.append("iParam1:0");
        builder.append(':');
        builder.append("acParamList:");
        builder.append(':');
        builder.append("iReqId:1");
        builder.append(':');
        builder.append("iResult:0");
        builder.append(':');
        builder.append("iTerminalId:0");
        builder.append(':');
        builder.append("acUsrName:18002");
        builder.append(':');
        builder.append("acPwd:49b8ba8f9e5b7b260a306c25393a4181");
        builder.append(':');
        builder.append("iTerminalType:2");
        builder.append(':');
        builder.append("iRight:0");
        builder.append(':');
        builder.append("uiDataLen:0");
        builder.append(':');
        builder.append("acData:");
        System.out.println("请求的字符串：" + builder.toString());
        tvRequest.setText("请求数据：" + builder.toString());

        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = MySocketService.socketer.sendData(builder.toString().getBytes());
                Log.d("发送状态：",i+"");
            }
        });
    }

    class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == SocketCode.DATA_RESULT) {
                String datastr = intent.getStringExtra("string");
                byte[] bytes = intent.getByteArrayExtra("byte");
                tvResponse.setText(datastr);
            }
        }
    }
}
