package com.feng.android.network.toolbox;

import android.util.Log;

/**
 * Log Util
 * 
 * @author fengjun
 */
public class L {
	
	private static String DEFAULT_TAG = "fengjun";
	
	/**
	 * You can change its value to "false" on the release version
	 */
	private static final boolean LOGGER = true;

	private L() {
		throw new AssertionError();
	}

	public static void v(String tag, String msg) {
		if (LOGGER) {
			Log.v(tag, msg);
		}
	}

	public static void d(String tag, String msg) {
		if (LOGGER) {
			Log.d(tag, msg);
		}
	}

	public static void i(String tag, String msg) {
		if (LOGGER) {
			Log.i(tag, msg);
		}
	}

	public static void w(String tag, String msg) {
		if (LOGGER) {
			Log.w(tag, msg);
		}
	}

	public static void e(String tag, String msg) {
		if (LOGGER) {
			Log.e(tag, msg);
		}
	}

	public static void e(String tag, String msg, Throwable tr) {
		if (LOGGER) {
			Log.e(tag, msg);
		}
	}
	
	public static void v(String msg) {
		if (LOGGER) {
			Log.v(DEFAULT_TAG, msg);
		}
	}

	public static void d(String msg) {
		if (LOGGER) {
			Log.d(DEFAULT_TAG, msg);
		}
	}

	public static void i(String msg) {
		if (LOGGER) {
			Log.i(DEFAULT_TAG, msg);
		}
	}

	public static void w(String msg) {
		if (LOGGER) {
			Log.w(DEFAULT_TAG, msg);
		}
	}

	public static void e(String msg) {
		if (LOGGER) {
			Log.e(DEFAULT_TAG, msg);
		}
	}

	public static void e(String msg, Throwable tr) {
		if (LOGGER) {
			Log.e(DEFAULT_TAG, msg);
		}
	}
}
