package com.feng.android.network;

/**
 * @description  网络请求异常类
 * @author       fengjun
 * @version      1.0
 * @created      2015-4-17
 */
public class NetWorkException extends Exception{

	private static final long serialVersionUID = 1L;

	public NetWorkException(){
		super();
	}
	
    public NetWorkException(String msg){
    	super(msg);
    }
    
    public NetWorkException(String msg, Throwable cause){
    	super(msg, cause);
    }
    
    public NetWorkException(Throwable cause){
    	super(cause);
    }
}
