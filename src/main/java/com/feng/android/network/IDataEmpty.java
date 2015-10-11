package com.feng.android.network;


/**
 * @description  用于判断服务器返回数据是否为空
 * @author       fengjun
 * @version      1.0
 * @created      2015-6-23
 */
public interface IDataEmpty {
	
	/**
	 * 自己实现数据为空的判断
	 * @return 数据是否为真的空
	 */
	boolean isResultDataEmpty();
}
