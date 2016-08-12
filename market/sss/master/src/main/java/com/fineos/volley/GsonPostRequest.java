package com.fineos.volley;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.google.gson.Gson;

public class GsonPostRequest<T> extends JsonRequest<T> {

    String stringRequest;
    private Class<T> mClass; 
    private Gson mGson;  
    /**
     * 这里的method必须是Method.POST，也就是必须带参数。
     * 如果不想带参数，可以用JsonObjectRequest，给它构造参数传null。GET方式请求。
     * @param stringRequest 格式应该是 "key1=value1&key2=value2"
     */

    public GsonPostRequest(String url, String stringRequest, Class<T> mClass,
                             Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(Method.POST, url, stringRequest, listener, errorListener);
        Log.v("", "ssss GsonRequest stringRequest="+stringRequest);
        this.stringRequest = stringRequest;
        this.mClass = mClass;
        mGson = new Gson();
    }
    
    @Override
    public Map getHeaders() {
        HashMap headers = new HashMap();
        headers.put("Accept", "application/json;charset=utf-8");
//        headers.put("Accept-Encoding", "gzip");
        headers.put("User-Agent", "App-Rom/0.1b");
        headers.put("API-Version", "0.1b");
        headers.put("User_ID", "123_123");
        return headers;
    }

    @Override
	public String getBodyContentType() {
	    if (getMethod() == Method.POST) {
	        return "application/x-www-form-urlencoded;charset=utf-8";
	    }
	    return super.getBodyContentType();
	}
	
	@Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data,"UTF-8");
            Log.d("TAG", "ssss parseNetworkResponse jsonString ="+jsonString);
            Log.d("TAG", "ssss parseNetworkResponse mClass ="+mClass);
            return Response.success(mGson.fromJson(jsonString, mClass),  
                  HttpHeaderParser.parseCacheHeaders(response));  
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
    }
 }