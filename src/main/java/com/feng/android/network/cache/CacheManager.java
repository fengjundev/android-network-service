package com.feng.android.network.cache;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.feng.android.network.toolbox.GetRequestParams;

import java.util.Map;

/**
 * @description  Cache manager for net work request
 * @author       fengjun
 * @version      1.0
 * @created      2015-4-27
 */
public class CacheManager {
	
	public static final int  		TIME_SECOND 		= 1;
	public static final int  		TIME_MINUTE 		= 60 * TIME_SECOND;
	public static final int  		TIME_HOUR 			= 60 * TIME_MINUTE;
	public static final int  		TIME_DAY 			= TIME_HOUR * 24;
	public static final int  		TIME_WEEK 			= TIME_DAY * 7;

	
	/**  The default validity of the cache is one WEEK */
	public  static final int 		CACHE_VALID_TIME 	= 6 * ACache.TIME_HOUR;
	private static CacheManager 	manager;
	private static Object    		objLock      		= new Object();
	private ACache 					cache;
	
	private CacheManager(Context ctx){
		initCache(ctx);
	}
	
	public static CacheManager getInstance(Context ctx){
		if(manager == null){
			synchronized(objLock){
				if(manager == null){
					manager = new CacheManager(ctx.getApplicationContext());
					manager.initCache(ctx.getApplicationContext());
				}
			}
		}
		return manager;
	}
	
	public void resetCache(Context ctx){
		if(null != cache){
			cache.resetCache();
			cache = null;
		}
		manager.initCache(ctx.getApplicationContext());
	}
	
	private void initCache(Context ctx){
		if(cache == null){
			cache = ACache.get(ctx.getApplicationContext());
		}
	}
	
	public void saveString(String key, String value){
		cache.put(key, value, CACHE_VALID_TIME);
	}
	
	public void saveString(String key, String value, int time){
		cache.put(key, value, time);
	}
	
	public CacheInfo<String> loadString(String key){
		CacheInfo<String> cacheInfo = cache.getAsStringConsiderDue(key);
		return cacheInfo;
	}
	
	public boolean isCacheExist(String url, final Map<String, String> params){
		GetRequestParams getRequestParams = new GetRequestParams(url, params);
		final String urlWithParams = getRequestParams.toString();
		CacheInfo<String> jsonCache= loadString(urlWithParams);
		
		return (jsonCache == null) ? false : true;
	}
	
	public void saveObject(String key, Object value){
		String data = JSON.toJSONString(value);
		cache.put(key, data, CACHE_VALID_TIME);
	}
	
	public <T> T getObject(String key, Class<T> type){
		CacheInfo<String> cacheInfo = loadString(key);
		if ( null == cacheInfo ) {
			return null;
		}
		if(cacheInfo.getCache() == null) return null;
		
		T object = JSON.parseObject(cacheInfo.getCache(), type);
		return object;
	}
	
	public void removeCache( String key ) {
		cache.remove( key );
	}
	
    public boolean removeCache( String url, final Map<String, String> params ) {
		GetRequestParams getRequestParams = new GetRequestParams(url, params);
		final String urlWithParams = getRequestParams.toString();
		return cache.remove( urlWithParams );
	}
}
