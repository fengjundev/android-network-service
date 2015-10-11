package com.feng.android.network;

/**
 * @description  网络请求监听器
 * @author       fengjun
 * @version      1.0
 * @created      2015-3-1
 */
public interface NetRequestListener<T> {
	void onSuccess(T response);
	void onFailed(String message);
}
