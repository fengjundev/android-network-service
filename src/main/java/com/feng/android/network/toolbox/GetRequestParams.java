package com.feng.android.network.toolbox;

import java.util.Map;

public class GetRequestParams {

	private Map<String, String> params;
	private String url;
	
	public GetRequestParams(String url, Map<String, String> params){
		this.url = url;
		this.params = params;
	}
	
	@Override
    public String toString() {
		if(params == null){
            return url;
        }
		
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (result.length() > 0)
                result.append("&");

            result.append(entry.getKey());
            result.append("=");
            result.append(entry.getValue());
        }

        String urlWithParams = url;
        String paramStr = result.toString();
        
        if(paramStr != null && !"".equals(paramStr))
            urlWithParams = url + "?" + result.toString();

        return urlWithParams;
    }
}
