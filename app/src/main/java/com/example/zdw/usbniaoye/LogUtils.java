package com.example.zdw.usbniaoye;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Administrator on 2016/1/3.
 */
public class LogUtils {

    private static boolean isShow = false;

    public static void showLogI(String str) {
        if (isShow == false) {
            Log.e("TAG", "--------" + str + "-------" + str);
        }
    }

    public static void showToast(Context context, String str) {
        if (str != null) {
            Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
        }
    }
}
