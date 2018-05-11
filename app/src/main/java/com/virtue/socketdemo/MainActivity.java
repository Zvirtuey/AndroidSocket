package com.virtue.socketdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.virtue.socketdemo.bean.TestBean;
import com.virtue.socketdemo.service.MySocketService;
import com.virtue.socketlibrary.manager.Socketer;
import com.virtue.socketlibrary.type.ParseMode;
import com.virtue.socketlibrary.utils.OnReceiveListener;
import com.virtue.socketlibrary.utils.ResponseListener;

import java.util.ArrayList;

import static android.R.attr.type;

public class MainActivity extends AppCompatActivity {

    private TextView tvRequest;
    private TextView tvResponse;
    private Button btSend;
    private String reDataStr;
    private Spinner spMode;
    private ParseMode parseMode = ParseMode.AUTO_PARSE;
    private ArrayList<String> mList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Open Service
        startService(new Intent(this, MySocketService.class));
        initView();
        initData();
        initListener();
        manuallyParseListener(); //手动解析有效（manually parse is valid）
    }

    private void initView() {
        tvRequest = (TextView) findViewById(R.id.tv_request);
        tvResponse = (TextView) findViewById(R.id.tv_response);
        spMode = (Spinner) findViewById(R.id.sp_mode);
        btSend = (Button) findViewById(R.id.bt_send);
    }

    private void initData() {
        mList = new ArrayList<>();
        mList.add("auto");
        mList.add("manually");
        //适配器
        ArrayAdapter<String> arr_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mList);
        //设置样式
        arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //加载适配器
        spMode.setAdapter(arr_adapter);

        Gson gson = new Gson();
        TestBean codeGetModle = new TestBean();
        codeGetModle.protver = "100";
        codeGetModle.pkgtype = 1;
        codeGetModle.command = 15;
        codeGetModle.seq = 1000;
        TestBean.Inform in = codeGetModle.body;
        in.cmd = type;
        in.Phone = "18500000000";
        final String json = gson.toJson(codeGetModle);
        // bean转字符串并发送
        reDataStr = json + "\r\n";
        tvRequest.setText(json);
    }

    private void initListener() {
        spMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String str = mList.get(position).toString().trim();
                if (str.equals("auto")) {
                    Socketer.getInstance(MainActivity.this).setParseMode(ParseMode.AUTO_PARSE);
                    parseMode = ParseMode.AUTO_PARSE;
                } else if (str.equals("manually")) {
                    Socketer.getInstance(MainActivity.this).setParseMode(ParseMode.MANUALLY_PARSE);
                    parseMode = ParseMode.MANUALLY_PARSE;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Socketer.getInstance(MainActivity.this).setParseMode(ParseMode.AUTO_PARSE);
                parseMode = ParseMode.AUTO_PARSE;
            }

        });

        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (parseMode) {
                    case AUTO_PARSE:
                        autoReceiveData();

                        break;
                    case MANUALLY_PARSE:
                        Socketer.getInstance(MainActivity.this).sendStrData(reDataStr); //request

                        break;

                    default:

                        break;
                }
            }
        });
    }


    private void manuallyParseListener() {
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
                    }
                });
            }
        });
    }

    private void autoReceiveData() {
        Socketer.getInstance(MainActivity.this).sendStrData(reDataStr, "\"seq\":1000", new ResponseListener() {
            @Override
            public void onSuccess(final String data) {
                Log.i("Test server data", "callback data：" + data);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvResponse.setTextColor(getResources().getColor(R.color.red));
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


}
