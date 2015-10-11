package com.feng.android.network;

import com.alibaba.fastjson.JSON;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;


public class FastJsonRequest<T> extends Request<T> {
	
	private static final String DEBUG_TAG = "NetWorkRequest";
	/** Default charset for JSON request. */
    protected static final String PROTOCOL_CHARSET = "utf-8";
    
    private Listener<T> mListener;
    private final Map<String, String> mParams;
    private Map<String,String> mHeaders;
    private Request.Priority mPriority;
    private Class<T> mClass;
    private String url;
    private String mRequestBody;
    
    public FastJsonRequest(int method, String url, Map<String, String> params, 
    						Map<String, String> headers, Class<T> pclass, 
    						Listener<T> listener, ErrorListener errorListener,
    						Priority priority) {
    	
        super(method, url, errorListener);
        this.url = url;
        mListener = listener;
        mParams = params;
        mHeaders = headers;
        mPriority = priority;
        mClass = pclass;
    }
    
	public FastJsonRequest(int method, String url, Map<String, String> params, String body,
			Map<String, String> headers, Class<T> pclass, Listener<T> listener,
			ErrorListener errorListener, Priority priority) {

		super(method, url, errorListener);
		this.url = url;
		mListener = listener;
		mParams = params;
		mHeaders = headers;
		mPriority = priority;
		mClass = pclass;
		mRequestBody = body;
	}
 
    public FastJsonRequest(String url, Map<String, String> params,
    						Map<String, String> headers, Class<T> pclass, 
    						Listener<T> listener, ErrorListener errorListener,
    						Priority priority) {
        this(null == params ? Method.GET : Method.POST, url, 
        params, headers,
        pclass, listener, 
        errorListener, priority);
    }
    
    @Override
    public byte[] getBody() throws AuthFailureError {
    	try {
            return mRequestBody == null ? null : mRequestBody.getBytes(PROTOCOL_CHARSET);
        } catch (UnsupportedEncodingException uee) {
            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s",
                    mRequestBody, PROTOCOL_CHARSET);
            return null;
        }
    }
 
    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return mParams;
    }
 
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        if (null == mHeaders) {
            mHeaders = new HashMap<String, String>();
        }
        
        mHeaders.put("Content-Type", "application/json");  
        
        return mHeaders;
    }
    
    @Override
    public Request.Priority getPriority() {
    	return (mPriority == null) ? Priority.NORMAL : mPriority;
    };
    
    @Override
    protected void deliverResponse(T response) {
    	if(mListener != null)
    		mListener.onResponse(response);
    }
    
    @Override
    public void cancel() {
    	super.cancel();
    }

	@Override
	protected Response<T> parseNetworkResponse(NetworkResponse response) {
		try {
			String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
			return Response.success(JSON.parseObject(jsonString, mClass), HttpHeaderParser.parseCacheHeaders(response));
		} catch (UnsupportedEncodingException e) {
			return Response.error(new ParseError(e));
		} catch (NullPointerException e){
			return Response.error(new ParseError(e));
		} catch (Exception e){
			return Response.error(new ParseError(e));
		}
	}
}