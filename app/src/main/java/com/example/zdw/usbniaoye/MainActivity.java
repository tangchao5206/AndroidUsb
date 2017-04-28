package com.example.zdw.usbniaoye;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.TextView;

import com.example.zdw.usbniaoye.callback.UsbCallback;
import com.example.zdw.usbniaoye.service.UsbService;



public class MainActivity extends Activity {

    private UsbService mService;
    private TextView   tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv= (TextView) findViewById(R.id.tv);
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
                    public void OnReceive(String data) {

                    }

                    @Override
                    public void OnMessage(String Action) {
                        //收到usb设备连接的状态
                        tv.setText(Action);
                    }
                });
                byte[] bytes = mService.init();//初始化并返回接收到的数据

            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);
    }
}
