package com.feng.android.network.toolbox;

import android.util.Log;

import java.util.Locale;

/**
 * @description  Logging Helper
 * @author       fengjun
 * @version      1.0
 * @created      2015-3-1
 */
public class NetWorkServiceLog {
	
    public static String TAG = "NetWorkRequest";
    public static boolean DEBUG = true;

    public static void setTag(String tag) {
        TAG = tag;
    }

    public static void v(String format, Object... args) {
        if (DEBUG) {
            Log.v(TAG, buildMessage(format, args));
        }
    }

    public static void d(String format, Object... args) {
        Log.d(TAG, buildMessage(format, args));
    }

    public static void e(String format, Object... args) {
        Log.e(TAG, buildMessage(format, args));
    }

    public static void e(Throwable tr, String format, Object... args) {
        Log.e(TAG, buildMessage(format, args), tr);
    }

    /**
     * Formats the caller's provided message and prepends useful info like
     * calling thread ID and method name.
     */
    private static String buildMessage(String format, Object... args) {
        String msg = (args == null) ? format : String.format(Locale.US, format, args);
        return msg;
    }
}
