package com.example.volley;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.protocol.HTTP;

import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.google.gson.Gson;

public class GsonGetRequest<T> extends Request<T> {private final Listener<T> mListener;  

private Gson mGson;  

private Class<T> mClass;  

public GsonGetRequest(int method, String url, Class<T> clazz, Listener<T> listener,  
        ErrorListener errorListener) {  
    super(method, url, errorListener);  
    mGson = new Gson();  
    mClass = clazz;  
    mListener = listener;  
}  

public GsonGetRequest(String url, Class<T> clazz, Listener<T> listener,  
        ErrorListener errorListener) {  
    this(Method.GET, url, clazz, listener, errorListener);  
}  

@Override  
protected Response<T> parseNetworkResponse(NetworkResponse response) {  
    try {  
        String jsonString = new String(response.data,  
                "UTF-8");  
        Log.v("", "ssss GsonGetRequest jsonString="+jsonString);
        return Response.success(mGson.fromJson(jsonString, mClass),  
                HttpHeaderParser.parseCacheHeaders(response));  
    } catch (UnsupportedEncodingException e) {  
        return Response.error(new ParseError(e));  
    }  
}  

@Override  
protected void deliverResponse(T response) {  
    mListener.onResponse(response);  
}  }  