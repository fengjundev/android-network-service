package com.feng.android.network;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Request.Priority;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.feng.android.network.cache.CacheInfo;
import com.feng.android.network.cache.CacheManager;
import com.feng.android.network.toolbox.GetRequestParams;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Global net work request tools
 * 
 * @author fengjun
 */
public class NetWorkRequest {
	
	private static final String 	DEBUG_TAG 						= "NetWorkRequest";
	
	/** log info */
	private static final String 	LOG_RESPONSE_ERROR_NULL  		= "REQUEST FAILED, NO ERROR LOG";
	private static final String 	LOG_URL_IS_NULL 	      		= "CREATE REQUEST FAILED, URL CAN NOT BE NULL";
	private static final String 	LOG_RESPONSE_NULL 	     		= "REQUEST FAILED, RESPONSE IS NULL";
	private static final String 	LOG_PARAMS_IS_NULL 	  	  		= "CREATE REQUEST FAILED, PARAMS CAN NOT BE NULL";
	private static final String 	LOG_TAG_IS_NULL 		  		= "CREATE REQUEST FAILED, TAG CAN NOT BE NULL";
	private static final int    	NET_WORK_DEFAUL_TIME_OUT 		= 60000;
	
	/** priority of request , default is PRIORITY_NORMAL */
	public  static final int 		PRIORITY_HIGHEST 				= 0;
	public  static final int 		PRIORITY_HIGH 					= 1;
	public  static final int 		PRIORITY_NORMAL 				= 2; 
	public  static final int 		PRIORITY_LOW 					= 3;
	
	/** net work manager request instance */
	private static NetWorkRequest 	mNetWorkRequest;
	private static Object    		objLock      					= new Object();
	private static Object    		mapLock      					= new Object();
	private RequestQueue 			mQueue;
	private Context 				ctx;
	
	/**
	 * Current requests those are still waiting for responding <br>
	 * <ul>
	 * <li> String is the cache key </li>
	 * <li> Request<?> is the request object </li>
	 * </ul>
	 */
	private Map<String, Request<?>> mCurrentRequests = new HashMap<String, Request<?>>();
	
	private NetWorkRequest(Context ctx){
		mQueue = Volley.newRequestQueue(ctx.getApplicationContext());
		this.ctx = ctx.getApplicationContext();
	}
	
	public static NetWorkRequest getInstance(Context ctx){
		if(mNetWorkRequest == null){
			synchronized(objLock){
				if(mNetWorkRequest == null){
					mNetWorkRequest = new NetWorkRequest(ctx.getApplicationContext());
				}
			}
		}
		return mNetWorkRequest;
	}
	
	/**
	 * 取消队列中全部带tag标识的任务,一般与Activity生命周期中onDestroy联动
	 * @param tag 标签,建议使用activity的context.getClass().getName()
	 */
	public void cancleRequest(final String tag) {
		try{
		    mQueue.cancelAll(tag);
		    removeAllRequestInMapWithTag(tag);
		}catch(IllegalArgumentException e){}
	}
	
	public Request<?> getRequestFromMap(String key){
		synchronized (mapLock) {
			if(mCurrentRequests.containsKey(key)){
				return mCurrentRequests.get(key);
			}else{
				return null;
			}
        }
	}
	
	private boolean addToCurrentRequestMap(String key, Request<?> mRequest){
		synchronized (mapLock) {
			if(mCurrentRequests.containsKey(key)){
				return false;
			}else{
				mCurrentRequests.put(key, mRequest);
				return true;
			}
        }
	}
	
	private void removeRequestFromMap(String key){
		synchronized (mapLock) {
			if(mCurrentRequests.containsKey(key)){
				mCurrentRequests.remove(key);
			}
        }
	}
	
	private void removeAllRequestInMapWithTag(String tag){
		synchronized (mapLock) {
			Iterator<Entry<String, Request<?>>> iter = mCurrentRequests.entrySet().iterator(); 
			while (iter.hasNext()) { 
			    Map.Entry<String, Request<?>> entry = (Map.Entry<String, Request<?>>) iter.next(); 
			    if(entry.getValue() == null) continue;
			    String contentTag = (String)entry.getValue().getTag();
			    Request<?> request = entry.getValue();
			    if(tag.equals(contentTag)){
			    	iter.remove();
			    	if(request != null){
			    		request.cancel();
			    		request = null;
			    	}
			    }
			} 
        }
	}
	
	private <T> void saveDataToCache(T response, String urlWithParams, boolean needCache){
		if(response instanceof IDataEmpty){
			IDataEmpty iDataEmpty = (IDataEmpty) response;
			if(iDataEmpty.isResultDataEmpty()){
				// do nothing
			}else{
				saveCache(needCache, urlWithParams, response);
			}
		}else{
			saveCache(needCache, urlWithParams, response);
		}
	}
	
	private <T> void saveDataToCache(T response, String urlWithParams, boolean needCache, int time){
		if(response instanceof IDataEmpty){
			IDataEmpty iDataEmpty = (IDataEmpty) response;
			if(iDataEmpty.isResultDataEmpty()){
				// do nothing
			}else{
				saveCache(needCache, urlWithParams, response, time);
			}
		}else{
			saveCache(needCache, urlWithParams, response, time);
		}
	}
	
	/**
	 * GET方式请求JSON数据
	 * @param url 请求链接
	 * @param needCache 是否从缓存读取
	 * @param params 参数表
	 * @param javaBeanClass 自动解析出来的json对应的Java Bean Class,需自定义
	 * @param tag 任务标签,建议使用activity的context.getClass().getName()
	 * @param listener NetRequestListener结果监听
	 */
	public <T> void getJson(final String url, final boolean needCache, final Map<String, String> params,
			final Class<T> javaBeanClass, final String tag, final NetRequestListener<T> listener){
		
		getJson(url, needCache, params, javaBeanClass, PRIORITY_NORMAL, tag, listener);
	}
	
	/**
	 * GET方式请求JSON数据
	 * @param url 请求链接
	 * @param needCache 是否从缓存读取
	 * @param cacheDeleteTime 缓存有效时间,请使用CacheManager.TIME_MINUTE等基本单位
	 * @param params 参数表
	 * @param javaBeanClass 自动解析出来的json对应的Java Bean Class,需自定义
	 * @param tag 任务标签,建议使用activity的context.getClass().getName()
	 * @param listener NetRequestListener结果监听
	 */
	public <T> void getJson(final String url, final boolean needCache, final int cacheDeleteTime, final Map<String, String> params,
			final Class<T> javaBeanClass, final String tag, final NetRequestListener<T> listener){
		
		getJson(url, needCache, cacheDeleteTime, params, javaBeanClass, PRIORITY_NORMAL, tag, listener);
	}
	
	/**
	 * POST方式请求JSON数据
	 * @param url 请求链接
	 * @param needCache 是否从缓存读取
	 * @param params 参数表
	 * @param javaBeanClass 自动解析出来的json对应的Java Bean Class,需自定义
	 * @param tag 任务标签,建议使用activity的context.getClass().getName()
	 * @param listener NetRequestListener结果监听
	 */
	public <T> void postJson(final String url,final boolean needCache, final Map<String, String> params, 
			final Class<T> javaBeanClass, final String tag,
			final NetRequestListener<T> listener){
		
		postJson(url, needCache, params, javaBeanClass, PRIORITY_NORMAL, tag, listener);
	}
	
	public <T> void postJson(final String url,final boolean needCache, final Map<String, String> params, final String body, 
			final Class<T> javaBeanClass, final String tag,
			final NetRequestListener<T> listener){
		
		postJson(url, needCache, params, body, javaBeanClass, PRIORITY_NORMAL, tag, listener);
	}
	
	/**
	 * 普通post请求数据
	 * @param url 请求链接
	 * @param needCache 是否从缓存读取
	 * @param params 参数表
	 * @param tag 任务标签,建议使用activity的context.getClass().getName()
	 * @param listener NetRequestListener结果监听
	 */
	public void postNormal(final String url,final boolean needCache, final Map<String, String> params, 
			 final String tag, final NetRequestListener<String> listener){
		
		postNormal(url, needCache, params, PRIORITY_NORMAL, tag, listener);
	}
	
	/**
	 * 普通get请求数据
	 * @param url 请求链接
	 * @param needCache 是否从缓存读取
	 * @param params 参数表
	 * @param tag 任务标签,建议使用activity的context.getClass().getName()
	 * @param listener NetRequestListener结果监听
	 */
	public void getNormal(final String url,final boolean needCache, final Map<String, String> params,
			final String tag,
			final NetRequestListener<String> listener){
		getNormal(url, needCache, params, PRIORITY_NORMAL, tag, listener);
	}
	
	/**
	 * GET方式请求JSON数据
	 * @param url 请求链接
	 * @param needCache 是否从缓存读取
	 * @param params 参数表
	 * @param javaBeanClass 自动解析出来的json对应的Java Bean Class,需自定义
	 * @param priority 任务优先级 (NetWorkRequest.PRIORITY_NORMAL等)
	 * @param tag 任务标签,建议使用activity的context.getClass().getName()
	 * @param listener 结果监听
	 */
	public <T> void getJson(final String url, final boolean needCache, 
			final Map<String, String> params, final Class<T> javaBeanClass, 
			final int priority, final String tag, final NetRequestListener<T> listener){
		
		if(url == null){
			listener.onFailed(LOG_URL_IS_NULL);
			return;
		}
		
		if(tag == null || "".equals(tag)){
			listener.onFailed(LOG_TAG_IS_NULL);
			return;
		}
		
		GetRequestParams getRequestParams = new GetRequestParams(url, params);
		final String urlWithParams = getRequestParams.toString();
		
		
		FastJsonRequest<T> request1 = (FastJsonRequest<T>) getRequestFromMap(urlWithParams);
		if(request1 != null){
			return;
		}
		
		final CacheInfo<T> cacheResponse = loadCache(needCache, urlWithParams, javaBeanClass);
		if(cacheResponse != null && !cacheResponse.isDue()){
			listener.onSuccess(cacheResponse.getCache());
			return;
		}
		
		FastJsonRequest<T> request = new FastJsonRequest<T>(Request.Method.GET, urlWithParams, null, null, javaBeanClass,
			    new Response.Listener<T>() 
			    {
			        @Override
			        public void onResponse(T response) { 
			        	removeRequestFromMap(urlWithParams);
			        	if(response == null)
			        		listener.onFailed(LOG_RESPONSE_NULL);
			        	else{
			        		listener.onSuccess(response);
			        		
			        		saveDataToCache(response, urlWithParams, needCache);
			        	}
			        }
			    }, 
			    new Response.ErrorListener() 
			    {
			        @Override
					public void onErrorResponse(VolleyError error) {
			        	removeRequestFromMap(urlWithParams);
						if (needCache && cacheResponse != null && cacheResponse.isDue()) {
							listener.onSuccess(cacheResponse.getCache());
						} else if (error == null || error.getMessage() == null){
							listener.onFailed(LOG_RESPONSE_ERROR_NULL);
						}
						else{
							listener.onFailed(error.getMessage());
						}
					}
			    },
			    getTaskPriority(priority)
			);
		
		request.setTag(tag);
		//request.setShouldCache(false);
		request.setRetryPolicy(new DefaultRetryPolicy(
				NET_WORK_DEFAUL_TIME_OUT, 
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, 
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));   
		addToCurrentRequestMap(urlWithParams, request);
		mQueue.add(request);
	}
	
	/**
	 * GET方式请求JSON数据
	 * @param url 请求链接
	 * @param needCache 是否从缓存读取
	 * @param cacheDeleteTime 缓存有效时间,请使用CacheManager.TIME_MINUTE等基本单位
	 * @param params 参数表
	 * @param javaBeanClass 自动解析出来的json对应的Java Bean Class,需自定义
	 * @param priority 任务优先级 (NetWorkRequest.PRIORITY_NORMAL等)
	 * @param tag 任务标签,建议使用activity的context.getClass().getName()
	 * @param listener 结果监听
	 */
	public <T> void getJson(final String url, final boolean needCache, final int cacheDeleteTime, 
			final Map<String, String> params, final Class<T> javaBeanClass, 
			final int priority, final String tag, final NetRequestListener<T> listener){
		
		if(url == null){
			listener.onFailed(LOG_URL_IS_NULL);
			return;
		}
		
		if(tag == null || "".equals(tag)){
			listener.onFailed(LOG_TAG_IS_NULL);
			return;
		}
		
		GetRequestParams getRequestParams = new GetRequestParams(url, params);
		final String urlWithParams = getRequestParams.toString();
		
		
		FastJsonRequest<T> request1 = (FastJsonRequest<T>) getRequestFromMap(urlWithParams);
		if(request1 != null){
			return;
		}
		
		final CacheInfo<T> cacheResponse = loadCache(needCache, urlWithParams, javaBeanClass);
		if(cacheResponse != null && !cacheResponse.isDue()){
			listener.onSuccess(cacheResponse.getCache());
			return;
		}
		
		FastJsonRequest<T> request = new FastJsonRequest<T>(Request.Method.GET, urlWithParams, null, null, javaBeanClass,
			    new Response.Listener<T>() 
			    {
			        @Override
			        public void onResponse(T response) { 
			        	removeRequestFromMap(urlWithParams);
			        	if(response == null)
			        		listener.onFailed(LOG_RESPONSE_NULL);
			        	else{
			        		listener.onSuccess(response);
			        		
			        		saveDataToCache(response, urlWithParams, needCache, cacheDeleteTime);
			        	}
			        }
			    }, 
			    new Response.ErrorListener() 
			    {
			        @Override
					public void onErrorResponse(VolleyError error) {
			        	removeRequestFromMap(urlWithParams);
						if (needCache && cacheResponse != null && cacheResponse.isDue()) {
							listener.onSuccess(cacheResponse.getCache());
						} else if (error == null || error.getMessage() == null){
							listener.onFailed(LOG_RESPONSE_ERROR_NULL);
						}
						else{
							listener.onFailed(error.getMessage());
						}
					}
			    },
			    getTaskPriority(priority)
			);
		
		request.setTag(tag);
		//request.setShouldCache(false);
		request.setRetryPolicy(new DefaultRetryPolicy(
				NET_WORK_DEFAUL_TIME_OUT, 
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, 
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));   
		addToCurrentRequestMap(urlWithParams, request);
		mQueue.add(request);
	}
	
	/**
	 * POST方式请求JSON数据
	 * @param url 请求链接
	 * @param needCache 是否从缓存读取
	 * @param params 参数表
	 * @param javaBeanClass 自动解析出来的json对应的Java Bean Class,需自定义
	 * @param priority 任务优先级 (NetWorkRequest.PRIORITY_NORMAL等)
	 * @param tag 任务标签,建议使用activity的context.getClass().getName()
	 * @param listener 结果监听
	 */
	public <T> void postJson(final String url, final boolean needCache,final Map<String, String> params, 
			final Class<T> javaBeanClass, final int priority, final String tag,
			final NetRequestListener<T> listener){
		
		if(url == null){
			listener.onFailed(LOG_URL_IS_NULL);
			return;
		}
		if(tag == null || "".equals(tag)){
			listener.onFailed(LOG_TAG_IS_NULL);
			return;
		}
		
		if(params == null){
			listener.onFailed(LOG_PARAMS_IS_NULL);
			return;
		}
		
		
		final String cacheKey = url + params.toString();
		
		
		FastJsonRequest<T> request1 = (FastJsonRequest<T>) getRequestFromMap(cacheKey);
		if(request1 != null){
			return;
		}
		
		final CacheInfo<T> cacheResponse = loadCache(needCache, cacheKey, javaBeanClass);
		if(cacheResponse != null && !cacheResponse.isDue()){
			listener.onSuccess(cacheResponse.getCache());
			return;
		}
		
		FastJsonRequest<T> request = new FastJsonRequest<T>(Request.Method.POST, url, params, null, javaBeanClass,
			    new Response.Listener<T>() 
			    {
			        @Override
			        public void onResponse(T response) {   
			        	removeRequestFromMap(cacheKey);
			        	if(response == null)
			        		listener.onFailed(LOG_RESPONSE_NULL);
			        	else{
			        		listener.onSuccess(response);
			        		saveDataToCache(response, cacheKey, needCache);
			        	}
			        }
			    }, 
			    new Response.ErrorListener() 
			    {
			         @Override
			         public void onErrorResponse(VolleyError error) {  
			        	 removeRequestFromMap(cacheKey);
			        	if(needCache && cacheResponse != null && cacheResponse.isDue()){
				     		 listener.onSuccess(cacheResponse.getCache());
				     	}else if(error == null){
			        		 listener.onFailed(LOG_RESPONSE_ERROR_NULL);
				     	}else{
			        		 listener.onFailed(error.getMessage());
			        	}
			       }
			    },
			    getTaskPriority(priority)
			);
		
		request.setTag(tag);
		request.setShouldCache(false);
		request.setRetryPolicy(new DefaultRetryPolicy(
				NET_WORK_DEFAUL_TIME_OUT, 
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, 
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));     
		addToCurrentRequestMap(cacheKey, request);
		mQueue.add(request);
	}
	
	
	public <T> void postJson(final String url, final boolean needCache,final Map<String, String> params, final String body,
			final Class<T> javaBeanClass, final int priority, final String tag,
			final NetRequestListener<T> listener){
		
		if(url == null){
			listener.onFailed(LOG_URL_IS_NULL);
			return;
		}
		if(tag == null || "".equals(tag)){
			listener.onFailed(LOG_TAG_IS_NULL);
			return;
		}
		
		if(params == null){
			listener.onFailed(LOG_PARAMS_IS_NULL);
			return;
		}
		
		
		final String cacheKey = url + params.toString();
		
		
		FastJsonRequest<T> request1 = (FastJsonRequest<T>) getRequestFromMap(cacheKey);
		if(request1 != null){
			return;
		}
		
		final CacheInfo<T> cacheResponse = loadCache(needCache, cacheKey, javaBeanClass);
		if(cacheResponse != null && !cacheResponse.isDue()){
			listener.onSuccess(cacheResponse.getCache());
			return;
		}
		
		FastJsonRequest<T> request = new FastJsonRequest<T>(Request.Method.POST, url, params,body, null, javaBeanClass,
			    new Response.Listener<T>() 
			    {
			        @Override
			        public void onResponse(T response) {   
			        	removeRequestFromMap(cacheKey);
			        	if(response == null)
			        		listener.onFailed(LOG_RESPONSE_NULL);
			        	else{
			        		listener.onSuccess(response);
			        		saveDataToCache(response, cacheKey, needCache);
			        	}
			        }
			    }, 
			    new Response.ErrorListener() 
			    {
			         @Override
			         public void onErrorResponse(VolleyError error) {  
			        	 removeRequestFromMap(cacheKey);
			        	if(needCache && cacheResponse != null && cacheResponse.isDue()){
				     		 listener.onSuccess(cacheResponse.getCache());
				     	}else if(error == null){
			        		 listener.onFailed(LOG_RESPONSE_ERROR_NULL);
				     	}else{
			        		 listener.onFailed(error.getMessage());
			        	}
			       }
			    },
			    getTaskPriority(priority)
			);
		
		request.setTag(tag);
		request.setShouldCache(false);
		request.setRetryPolicy(new DefaultRetryPolicy(
				NET_WORK_DEFAUL_TIME_OUT, 
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, 
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));     
		addToCurrentRequestMap(cacheKey, request);
		mQueue.add(request);
	}
	
	/**
	 * 普通post方式请求数据
	 * @param url 请求链接
	 * @param needCache 是否从缓存读取
	 * @param params 参数表
	 * @param priority 优先级
	 * @param tag 任务标签,建议使用activity的context.getClass().getName()
	 * @param listener 结果监听
	 */
	public void postNormal(final String url, final boolean needCache,final Map<String, String> params, 
			final int priority,final String tag,
			final NetRequestListener<String> listener){
		
		if(url == null){
			listener.onFailed(LOG_URL_IS_NULL);
			return;
		}
		if(tag == null || "".equals(tag)){
			listener.onFailed(LOG_TAG_IS_NULL);
			return;
		}
		
		if(params == null){
			listener.onFailed(LOG_PARAMS_IS_NULL);
			return;
		}
		
		
		final String cacheKey = url + params.toString();
		
		StringRequest request1 = (StringRequest) getRequestFromMap(cacheKey);
		if(request1 != null){
			return;
		}
		
		final CacheInfo<String> cacheResponse = loadCache(needCache, cacheKey, String.class);
		if(cacheResponse != null && !cacheResponse.isDue()){
			listener.onSuccess(cacheResponse.getCache());
			return;
		}
		
		StringRequest request = new StringRequest(Request.Method.POST, url,
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						removeRequestFromMap(cacheKey);
						if(response == null)
			        		listener.onFailed(LOG_RESPONSE_NULL);
			        	else{
			        		listener.onSuccess(response);
			        		saveDataToCache(response, cacheKey, needCache);
			        	}
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						removeRequestFromMap(cacheKey);
						if (needCache && cacheResponse != null && cacheResponse.isDue()) {
							listener.onSuccess(cacheResponse.getCache());
						} else if (error == null)
							listener.onFailed(LOG_RESPONSE_ERROR_NULL);
						else{
							listener.onFailed(error.getMessage());
						}
					}
				}) {
			    @Override
			    protected Map<String, String> getParams() {
				   	return params;
			    }
			    @Override
				public Priority getPriority() {
				    return getTaskPriority(priority);
			    }
		};
		
		request.setTag(tag);
		request.setShouldCache(false);
		request.setRetryPolicy(new DefaultRetryPolicy(
				NET_WORK_DEFAUL_TIME_OUT, 
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, 
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));    
		addToCurrentRequestMap(cacheKey, request);
		mQueue.add(request);
	}
	
	/**
	 * 普通get请求数据
	 * @param url 请求链接
	 * @param needCache 是否从缓存读取
	 * @param priority 优先级
	 * @param tag 任务标签,建议使用activity的context.getClass().getName()
	 * @param listener 结果监听
	 */
	public void getNormal(final String url, final boolean needCache,
			final Map<String, String> params, final int priority, 
			final String tag, final NetRequestListener<String> listener){
		
		if(url == null){
			listener.onFailed(LOG_URL_IS_NULL);
			return;
		}
		if(tag == null || "".equals(tag)){
			listener.onFailed(LOG_TAG_IS_NULL);
			return;
		}
		
		GetRequestParams getRequestParams = new GetRequestParams(url, params);
		final String urlWithParams = getRequestParams.toString();
		
		StringRequest request1 = (StringRequest) getRequestFromMap(urlWithParams);
		if(request1 != null){
			return;
		}
		
		final CacheInfo<String> cacheResponse = loadCache(needCache, urlWithParams, String.class);
		if(cacheResponse != null && !cacheResponse.isDue()){
			listener.onSuccess(cacheResponse.getCache());
			return;
		}
		
		StringRequest request = new StringRequest(Request.Method.GET, urlWithParams,
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						removeRequestFromMap(urlWithParams);
						if(response == null)
			        		listener.onFailed(LOG_RESPONSE_NULL);
			        	else{
			        		listener.onSuccess(response);
			        		saveDataToCache(response, urlWithParams, needCache);
			        	}
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						removeRequestFromMap(urlWithParams);
						if(cacheResponse != null && cacheResponse.isDue()){
				     		 listener.onSuccess(cacheResponse.getCache());
				     	}else if(error == null)
			        		 listener.onFailed(LOG_RESPONSE_ERROR_NULL);
			        	 else{
			        		 listener.onFailed(error.getMessage());
			        	 }
					}
				}){
				@Override
				public Priority getPriority() {
					return getTaskPriority(priority);
				}
		};
		
		request.setTag(tag);
		request.setShouldCache(false);
		request.setRetryPolicy(new DefaultRetryPolicy(
				NET_WORK_DEFAUL_TIME_OUT, 
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, 
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));    
		addToCurrentRequestMap(urlWithParams, request);
		mQueue.add(request);
	}

	private <T> void saveCache(boolean needCache, String key, T value){
		String jsonString= JSON.toJSONString(value);
		CacheManager.getInstance(ctx).saveString(key, jsonString);
	}
	
	private <T> void saveCache(boolean needCache, String key, T value, int time){
		String jsonString= JSON.toJSONString(value);
		CacheManager.getInstance(ctx).saveString(key, jsonString, time);
	}
	
	private <T> CacheInfo<T> loadCache(boolean needCache, String key, Class<T> javaBeanClass){
		if(!needCache) return null;
		
		CacheInfo<String> jsonCache= CacheManager.getInstance(ctx).loadString(key);
		
		if(jsonCache == null){
			return null;
		}
		
		try {
			T cacheResponse = JSON.parseObject(jsonCache.getCache(), javaBeanClass);
			CacheInfo<T> cacheInfo = new CacheInfo<T>();
			cacheInfo.setCache(cacheResponse);
			cacheInfo.setDue(jsonCache.isDue());
			
			return cacheInfo;
		} catch (Exception e){
			return null;
		}
	}
	
	private Priority getTaskPriority(final int priority){
		if(priority == PRIORITY_HIGHEST){
			return Priority.IMMEDIATE;
		}else if(priority == PRIORITY_HIGH){
			return Priority.HIGH;
		}else if(priority == PRIORITY_NORMAL){
			return Priority.NORMAL;
		}else if(priority == PRIORITY_LOW){
			return Priority.LOW;
		}else{
			return Priority.NORMAL;
		}
	}
}
