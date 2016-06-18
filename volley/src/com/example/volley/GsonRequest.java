package com.example.volley;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.protocol.HTTP;

import android.util.Log;
import android.util.Xml.Encoding;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.google.gson.Gson;

public class GsonRequest<T> extends JsonRequest<T> {

    String stringRequest;
    private Class<T> mClass; 
    private Gson mGson;  
    /**
     * 这里的method必须是Method.POST，也就是必须带参数。
     * 如果不想带参数，可以用JsonObjectRequest，给它构造参数传null。GET方式请求。
     * @param stringRequest 格式应该是 "key1=value1&key2=value2"
     */

    public GsonRequest(String url, String stringRequest, Class<T> mClass,
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
        headers.put(HTTP.USER_AGENT, "App-Rom/0.1b");
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


//public class GsonRequest<T> extends Request<T> {  
//      
//        private final Listener<T> mListener;  
//      
//        private Gson mGson;  
//      
//        private Class<T> mClass;  
//      
//        public GsonRequest(int method, String url, String stringRequest, Class<T> clazz, Listener<T> listener,  
//                ErrorListener errorListener) {  
//            super(method, url, stringRequest, errorListener);  
//            mGson = new Gson();  
//            mClass = clazz;  
//            mListener = listener;  
//        }  
//      
////        public GsonRequest(String url, Class<T> clazz, Listener<T> listener,  
////                ErrorListener errorListener) {  
////            this(Method.GET, url, clazz, listener, errorListener);  
////        }  
//      
//        @Override  
//        protected Response<T> parseNetworkResponse(NetworkResponse response) {  
//            try {  
//                String jsonString = new String(response.data,  
//                        HttpHeaderParser.parseCharset(response.headers));  
//                return Response.success(mGson.fromJson(jsonString, mClass),  
//                        HttpHeaderParser.parseCacheHeaders(response));  
//            } catch (UnsupportedEncodingException e) {  
//                return Response.error(new ParseError(e));  
//            }  
//        }  
//      
//        @Override  
//        protected void deliverResponse(T response) {  
//            mListener.onResponse(response);  
//        }
//      
//    }  