package com.zmm.wifiserver;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.zmm.wifiserver.server.SocketServer;
import com.zmm.wifiserver.wifitool.WifiTools;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SocketServer.SConnectListener {

    private final static String TAG = MainActivity.class.getSimpleName();


    private Context context;
    private WifiTools wifiTools;
    private SocketServer socketServer;
    private TextView mRead;
    private EditText mEditWrite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        socketServer = new SocketServer();
        socketServer.setOnSConnectListener(MainActivity.this);

        Button startService = (Button) findViewById(R.id.btn_start_service);
        mRead = (TextView) findViewById(R.id.tv_read);
        mEditWrite = (EditText) findViewById(R.id.et_write);
        Button write = (Button) findViewById(R.id.btn_write);

        startService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                createChatRoom();
            }
        });

        write.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }


    private void createChatRoom() {
        wifiTools = WifiTools.getInstance(context);
        WifiConfiguration wifiConfiguration;
        wifiConfiguration = wifiTools.createWifiInfo("HOT-Device", "12345678", "WifiAP", wifiTools.WIFICIPHER_WPA2);
        wifiTools.createHotSpot(wifiConfiguration);

        new Thread() {
            @Override
            public void run() {
                System.out.println("create Server");
                while (wifiTools.getWifiApState() != wifiTools.WIFI_AP_STATE_ENABLED) {

                    try {
                        Thread.sleep(500);
                        Log.i(TAG, "wait_ap_enabled");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                socketServer.createServer();

            }
        }.start();
    }


    @Override
    public void onSReceiveData(final String data) {
        Log.i(TAG, "设置服务器监听收到消息" + data);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRead.setText(data);
            }
        });
    }

    @Override
    public void onSNotify(String msg) {
        Log.i(TAG, "设置服务器给出通知" + msg);
    }

    @Override
    public void onClientConnected() {
        Log.i(TAG, "客户端已连接");
    }


    private void sendMessage() {
        String data = mEditWrite.getText().toString().trim();

        if (wifiTools.getConnectedHotIP().size() >= 2) {
            Log.i(TAG, "发送消息给客户端");
            if (socketServer != null && data != null) {
                try {
                    socketServer.sendMessageToClient(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Log.i(TAG, "socketServer为null");
                Toast.makeText(getApplicationContext(),"socketServer is null",Toast.LENGTH_SHORT).show();

            }
        } else {
            Log.i(TAG, "没有连接的客户端");
        }
    }
}
