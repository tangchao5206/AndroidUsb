package com.example.zdw.usbniaoye.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.example.zdw.usbniaoye.LogUtils;
import com.example.zdw.usbniaoye.callback.UsbCallback;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Yunncan on 2016/7/26.
 */
public class UsbService extends Service {

    private UsbManager myUsbManager;//usb管理类
    private int VendorID;
    private int ProductID;          //这两个都是你usb的设备id
    private UsbDevice myUsbDevice;


    private UsbInterface Interface2;
    private UsbEndpoint epBulkOut;
    private UsbEndpoint epBulkIn;
    private UsbDeviceConnection myDeviceConnection;
    private UsbEndpoint epIntEndpointIn;
    private UsbEndpoint epIntEndpointOut;
    private UsbEndpoint epControl;
    private UsbCallback callback; //

    private final IBinder binder = new MyBinder();

    @Override
    public IBinder onBind(Intent intent) {
        LogUtils.showLogI("onbind");
        return binder;
    }

    public class MyBinder extends Binder {
        public UsbService getService() {
            return UsbService.this;
        }
    }

    public void setCallback(UsbCallback back) {
        this.callback = back;

    }

    public byte[] init() {
        System.out.println("----------进入service的onStart函数");
        myUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE); // 获取UsbManager
        // 枚举设备
        enumerateDevice(myUsbManager);
        // 查找设备接口
        getDeviceInterface();
        if (myUsbDevice != null) {
            // 获取设备endpoint

            epBulkOut=Interface2.getEndpoint(0);
            epBulkIn=Interface2.getEndpoint(1);
            // 打开conn连接通道
            openDevice(Interface2);
            byte[] getNumByte = new byte[]{(byte) 0x93, (byte) 0x8e, 0x04, 0x00, 0x08, 0x04, 0x10};
            sendMessageToPoint(getNumByte);

            byte[] bytes1 = receiveMessageFromPoint();
            if (bytes1 == null) {
                return null;
            }
            for (byte b : bytes1) {
                Log.d("ss", "--------得到usb返回的数据-----" + String.valueOf(b & 0xff));
            }
            return bytes1;
        }
        return null;
    }


    // 枚举设备函数
    private void enumerateDevice(UsbManager mUsbManager) {
        System.out.println("--------------开始进行枚举设备!");
        if (mUsbManager == null) {
            System.out.println("创建UsbManager失败，请重新启动应用！");
            return;
        } else {
            HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
            if (!(deviceList.isEmpty())) {
                // deviceList不为空
                System.out.println("deviceList is not null!");
                Iterator<UsbDevice> deviceIterator = deviceList.values()
                        .iterator();
                while (deviceIterator.hasNext()) {
                    UsbDevice device = deviceIterator.next();
                    // 输出设备信息
                    Log.i("TAG", "-------------DeviceInfo: " + device.getVendorId() + " , "
                            + device.getProductId());
                    System.out.println("-----------DeviceInfo:" + device.getVendorId()
                            + " , " + device.getProductId());
                    // 保存设备VID和PID
                    VendorID = device.getVendorId();
                    ProductID = device.getProductId();
                    // 保存匹配到的设备（这里填上你usb设备的id,就可以找到该usb设备）
                    if (VendorID == 1155 && ProductID == 22336) {
                        myUsbDevice = device; // 获取USBDevice
                        System.out.println("发现待匹配设备:" + device.getVendorId()
                                + "," + device.getProductId());
                        callback.OnMessage("find usbDevice");
                    }
                }
            } else {
                //安卓设备没有发现usb设备
                callback.OnMessage("no usbDevice");
            }
        }
    }


    // 寻找设备接口
    private void getDeviceInterface() {
        if (myUsbDevice != null) {
            Log.d("TAG", "设备接口个数------ : " + myUsbDevice.getInterfaceCount());
            Log.d("TAG", "设备名称------ : " + myUsbDevice.getDeviceName());
           /* for (int i = 0; i < myUsbDevice.getInterfaceCount(); i++) {
                UsbInterface intf = myUsbDevice.getInterface(i);

                if (i == 0) {
                    Interface1 = intf; // 保存设备接口
                    System.out.println("---------成功获得设备接口:" + Interface1.getId());
                }
                if (i == 1) {
                    Interface2 = intf;
                    System.out.println("---------成功获得设备接口:" + Interface2.getId());
                }
            }*/
            //获取你能收到数据的设备接口
            Interface2=myUsbDevice.getInterface(1);

        } else {
            System.out.println("设备为空！");
        }
    }

    // 分配端点，IN | OUT，即输入输出；可以通过判断
    private UsbEndpoint assignEndpoint(UsbInterface mInterface) {

        for (int i = 0; i < mInterface.getEndpointCount(); i++) {
            UsbEndpoint ep = mInterface.getEndpoint(i);

            // look for bulk endpoint
            if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                if (ep.getDirection() == UsbConstants.USB_DIR_OUT) {
                    epBulkOut = ep;
                    System.out.println("------------Find the BulkEndpointOut," + "index:"
                            + i + "," + "使用端点号："
                            + epBulkOut.getEndpointNumber());
                } else {
                    epBulkIn = ep;
                    System.out
                            .println("----------------Find the BulkEndpointIn:" + "index:" + i
                                    + "," + "使用端点号："
                                    + epBulkIn.getEndpointNumber());
                }
            }
            // look for contorl endpoint
            if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_CONTROL) {
                epControl = ep;
                System.out.println("---------------find the ControlEndPoint:" + "index:" + i
                        + "," + epControl.getEndpointNumber());
            }
            // look for interrupte endpoint
            if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_INT) {
                if (ep.getDirection() == UsbConstants.USB_DIR_OUT) {
                    epIntEndpointOut = ep;
                    System.out.println("--------------find the InterruptEndpointOut:"
                            + "index:" + i + ","
                            + epIntEndpointOut.getEndpointNumber());
                }
                if (ep.getDirection() == UsbConstants.USB_DIR_IN) {
                    epIntEndpointIn = ep;
                    System.out.println("--------------find the InterruptEndpointIn:"
                            + "index:" + i + ","
                            + epIntEndpointIn.getEndpointNumber());
                }
            }
        }
        if (epBulkOut == null && epBulkIn == null && epControl == null
                && epIntEndpointOut == null && epIntEndpointIn == null) {
            throw new IllegalArgumentException("not endpoint is founded!");
        }
        return epIntEndpointIn;
    }

    // 打开设备
    public void openDevice(UsbInterface mInterface) {
        if (mInterface != null) {
            UsbDeviceConnection conn = null;
            // 在open前判断是否有连接权限；对于连接权限可以静态分配，也可以动态分配权限
            Log.i("TAG", "----------是否有Usb权限--------" + myUsbManager.hasPermission(myUsbDevice));
            if (myUsbManager.hasPermission(myUsbDevice)) {
                conn = myUsbManager.openDevice(myUsbDevice);
            }

            if (conn == null) {
                return;
            }

            if (conn.claimInterface(mInterface, true)) {
                myDeviceConnection = conn;
                if (myDeviceConnection != null)// 到此你的android设备已经连上zigbee设备
                    System.out.println("-------------open设备成功！");
                    callback.OnMessage("usbDevice connect");
                final String mySerial = myDeviceConnection.getSerial();
                System.out.println("------------设备serial number：" + mySerial);
            } else {
                System.out.println("无法打开连接通道。");
                conn.close();
            }
        }
    }

    // 发送数据
    private void sendMessageToPoint(byte[] buffer) {
        // bulkOut传输
        int res = myDeviceConnection
                .bulkTransfer(epBulkOut, buffer, buffer.length, 2000);
        if (res < 0)
            System.out.println("bulkOut返回输出为  负数");
        else {
            System.out.println("-----------发送数据成功！");
        }
        System.out.println("---------发送的状态------" + res);
    }


    // 从设备接收数据bulkIn
    private byte[] receiveMessageFromPoint() {
        int max = epBulkIn.getMaxPacketSize();
        System.out.println("------读的大小------" + max);
        byte[] buffer = new byte[max];
        if (myDeviceConnection.bulkTransfer(epBulkIn, buffer, buffer.length,
                2000) < 0) {
            System.out.println("------bulkIn返回输出为  负数");
            return null;
        } else {
            System.out.println("------Receive Message Succese！"
            );
        }
        return buffer;
    }

}
