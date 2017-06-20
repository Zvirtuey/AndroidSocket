package com.virtue.socketdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.virtue.socketdemo.bean.TestBean;
import com.virtue.socketdemo.service.MySocketService;
import com.virtue.socketlibrary.ResponseListener;
import com.virtue.socketlibrary.Socketer;

import static android.R.attr.type;

public class MainActivity extends AppCompatActivity {

    private TextView tvRequest;
    private TextView tvResponse;
    private Button btSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Open Socket Service
        startService(new Intent(this, MySocketService.class));
        tvRequest = (TextView) findViewById(R.id.tv_request);
        tvResponse = (TextView) findViewById(R.id.tv_response);
        btSend = (Button) findViewById(R.id.bt_send);
        Gson gson = new Gson();
        TestBean codeGetModle = new TestBean();
        codeGetModle.protver = "100";
        codeGetModle.pkgtype = 1;
        codeGetModle.command = 15;
        codeGetModle.seq = 100;
        TestBean.Inform in = codeGetModle.body;
        in.cmd = type;
        in.Phone = "18500000000";
        final String json = gson.toJson(codeGetModle);
        // bean转字符串并发送
        tvRequest.setText(json);
        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String reDataStr = json + "\r\n";
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
            }
        });
    }

}
