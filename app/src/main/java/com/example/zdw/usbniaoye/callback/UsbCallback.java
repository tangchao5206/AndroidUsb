package com.example.zdw.usbniaoye.callback;

/**
 * 创 建 人: tangchao
 * 创建日期: 2016/7/11 14:27
 * 修改时间：
 * 修改备注：
 */
public interface UsbCallback {
    public void OnReceive(String data);
    public void OnMessage(String Action);
}
