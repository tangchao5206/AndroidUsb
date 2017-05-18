package com.example.zdw.usbniaoye;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;

import com.example.zdw.usbniaoye.callback.UsbCallback;
import com.example.zdw.usbniaoye.service.UsbService;



public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    private UsbService mService;
    private TextView   tv;
    private TextView   tvState;//连接状态
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv= (TextView) findViewById(R.id.tv);
        tvState= (TextView) findViewById(R.id.tv_state);
        //开始绑定服务
        Intent bindIntent = new Intent(MainActivity.this, UsbService.class);
        bindService(bindIntent, conn, Context.BIND_AUTO_CREATE);


    }


    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ((UsbService.MyBinder) service).getService();
            if (mService != null) {
                mService.setCallback(new UsbCallback() {
                    @Override
                    public void OnReceive(final String data) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv.setText(data);
                            }
                        });
                        Log.d(TAG,data);
                    }

                    @Override
                    public void OnMessage(String Action) {
                        //收到usb设备连接的状态
                        if ("no insert".equals(Action)) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tvState.setText("连接失败");
                                }
                            });

                        } else if ("connect".equals(Action)) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tvState.setText("连接成功");
                                }
                            });
                        }
                    }
                });
                 mService.init();//初始化

            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };
    //usb写入数据
    private boolean SendData(byte[] data){
        if (mService!=null){
           return mService.sendMessageToPoint(data);
        }
        return false;
    }

    //usb读取数据
    private byte[] ReceiveData(){
        if (mService!=null){
            return mService.receiveMessageFromPoint();
        }
        return null;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);
    }
}
