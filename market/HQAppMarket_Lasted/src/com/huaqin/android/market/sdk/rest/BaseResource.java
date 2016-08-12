package com.huaqin.android.market.sdk.rest;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParamBean;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import com.huaqin.android.market.sdk.ClientInfo;
import com.repack.google.gson.Gson;
import com.repack.google.gson.GsonBuilder;

/**
 * REST资源服务基类
 * 
 * @author duanweiming
 * 
 */
public class BaseResource {

	private static DefaultHttpClient httpClient;
	
	public static Map<String, String> customHeaders=new HashMap<String, String>();

	private static Gson GSON = new GsonBuilder().registerTypeAdapter(Date.class, NumericDateTypeAdapter.FACTORY).create();
	
	private static final ReadWriteLock lock=new ReentrantReadWriteLock();
    
    private static final Lock read=lock.readLock();
    
    private static final Lock write=lock.writeLock();

	static {
	    initialize();
	}
	
	private static void initialize(){
        HttpParams httpParams=new BasicHttpParams(); 
        HttpProtocolParamBean paramsBean=new HttpProtocolParamBean(httpParams);
        paramsBean.setVersion(HttpVersion.HTTP_1_1);
        paramsBean.setContentCharset("UTF-8");
//      paramsBean.setUseExpectContinue(true);
        
        ConnManagerParams.setMaxTotalConnections(httpParams, 10);
        
        SchemeRegistry schemeRegistry=new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(),80));
        schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(),443));
        
//      httpParams.setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
        
        ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(httpParams,schemeRegistry);
        
        httpClient = new DefaultHttpClient(cm,httpParams);
        
        httpClient.setKeepAliveStrategy(new ConnectionKeepAliveStrategy() {
            
            public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                
                HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
                while (it.hasNext()) {
                    HeaderElement he = it.nextElement();
                    String param = he.getName(); 
                    String value = he.getValue();
                    if (value != null && param.equalsIgnoreCase("timeout")) {
                        try {
                            return Long.parseLong(value) * 1000;
                        } catch(NumberFormatException ignore) {
                        }
                    }
                }
                return 30 * 1000;
            }
            
        });
        
        httpClient.addRequestInterceptor(new HttpRequestInterceptor() {
            public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
                for(Entry<String, String> header:customHeaders.entrySet()){
                    request.setHeader(header.getKey(), header.getValue());
                }
                if(ClientInfo.GZIP_ENCODING)
                    request.setHeader("Accept-Encoding", "gzip");
            }
            
        });
        
        httpClient.addResponseInterceptor(new HttpResponseInterceptor() {
            public void process(final HttpResponse response, final HttpContext context) throws HttpException, IOException {
                HttpEntity entity = response.getEntity();
                if(entity!=null){
                Header header = entity.getContentEncoding();
	                if (header != null) {
	                    HeaderElement[] codecs = header.getElements();
	                    for (int i = 0; i < codecs.length; i++) {
	                        if (codecs[i].getName().equalsIgnoreCase("gzip")) {
	                            response.setEntity(new GzipDecompressingEntity(response.getEntity()));
	                            return;
	                        }
	                    }
	                }
                }
            }

        });
    }
	
	public static void rebuildContext(){
        write.lock();
        try{
            if(httpClient!=null){
                httpClient.getConnectionManager().shutdown();
                httpClient=null;
            }
            initialize();
        }finally{
            write.unlock();
        }
    }

	protected static <T> T get(String uri, Class<T> resultClass) throws IOException, HttpException {
		HttpGet httpGet = new HttpGet(uri);
		httpGet.setHeader("Accept", "application/json");
		ResponseHandler<String> handler = new ResponseHandler<String>() {

			public String handleResponse(HttpResponse response)
					throws ClientProtocolException, IOException {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					return EntityUtils.toString(entity, "UTF-8");
				} else {
					return null;
				}
			}
		};

		read.lock();
        try{
    		String response = httpClient.execute(httpGet, handler);
    		return GSON.fromJson(response, resultClass);
        }finally{
            read.unlock();
        }
	}

	protected static <T> T get(String uri, Type type) throws IOException,HttpException {
		HttpGet httpGet = new HttpGet(uri);
		httpGet.setHeader("Accept", "application/json");
		ResponseHandler<String> handler = new ResponseHandler<String>() {

			public String handleResponse(HttpResponse response)
					throws ClientProtocolException, IOException {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					return EntityUtils.toString(entity, "UTF-8");
				} else {
					return null;
				}
			}
		};

		read.lock();
        try{
    		String response = httpClient.execute(httpGet, handler);
    		return GSON.fromJson(response, type);
        }finally{
            read.unlock();
        }
	}

	protected static <T> T post(String uri, List<NameValuePair> formParams,Class<T> resultClass) throws IOException, HttpException {
		HttpPost httpPost = new HttpPost(uri);
		httpPost.setHeader("Accept", "application/json");
		httpPost.setEntity(new UrlEncodedFormEntity(formParams, "UTF-8"));
		ResponseHandler<String> handler = new ResponseHandler<String>() {

			public String handleResponse(HttpResponse response)
					throws ClientProtocolException, IOException {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					return EntityUtils.toString(entity, "UTF-8");
				} else {
					return null;
				}
			}
		};
		read.lock();
        try{
    		String response = httpClient.execute(httpPost, handler);
    		return GSON.fromJson(response, resultClass);
        }finally{
            read.unlock();
        }
	}

	protected static String post(String uri, List<NameValuePair> formParams) throws IOException, HttpException {
		HttpPost httpPost = new HttpPost(uri);
		httpPost.setHeader("Accept", "application/json");
		httpPost.setEntity(new UrlEncodedFormEntity(formParams, "UTF-8"));
		ResponseHandler<String> handler = new ResponseHandler<String>() {

			public String handleResponse(HttpResponse response)
					throws ClientProtocolException, IOException {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					return EntityUtils.toString(entity, "UTF-8");
				} else {
					return null;
				}
			}
		};
		read.lock();
        try{
    		String response = httpClient.execute(httpPost, handler);
    		return response;
        }finally{
            read.unlock();
        }
	}

	protected static <T> T post(String uri, Object entity, Class<T> resultClass) throws IOException, HttpException {
		String json = GSON.toJson(entity);
		HttpPost httpPost = new HttpPost(uri);
		httpPost.setHeader("Accept", "application/json");
		StringEntity jsonEntity = new StringEntity(json, "UTF-8");
		jsonEntity.setContentType("application/json");
		httpPost.setEntity(jsonEntity);
		ResponseHandler<String> handler = new ResponseHandler<String>() {

			public String handleResponse(HttpResponse response)
					throws ClientProtocolException, IOException {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					return EntityUtils.toString(entity, "UTF-8");
				} else {
					return null;
				}
			}
		};

		read.lock();
        try{
    		String response = httpClient.execute(httpPost, handler);
    		return GSON.fromJson(response, resultClass);
        }finally{
            read.unlock();
        }
	}

	protected static byte[] getRawData(String uri, final String mediaType)throws IOException, HttpException {
		HttpGet httpGet = new HttpGet(uri);
		httpGet.setHeader("Accept", mediaType);
		ResponseHandler<byte[]> handler = new ResponseHandler<byte[]>() {
			public byte[] handleResponse(HttpResponse response)
					throws ClientProtocolException, IOException {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					return EntityUtils.toByteArray(entity);
				} else {
					return null;
				}
			}
		};
		
		read.lock();
        try{
            return httpClient.execute(httpGet, handler);
        }finally{
            read.unlock();
        }
	}

	protected static <T> T put(String uri, Object entity, Class<T> resultClass) throws IOException, HttpException {
		String json = GSON.toJson(entity);
		HttpPut httpPut = new HttpPut(uri);
		httpPut.setHeader("Accept", "application/json");
		StringEntity jsonEntity = new StringEntity(json, "UTF-8");
		jsonEntity.setContentType("application/json");
		httpPut.setEntity(jsonEntity);
		ResponseHandler<String> handler = new ResponseHandler<String>() {

			public String handleResponse(HttpResponse response)
					throws ClientProtocolException, IOException {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					return EntityUtils.toString(entity, "UTF-8");
				} else {
					return null;
				}
			}
		};

		read.lock();
        try{
    		String response = httpClient.execute(httpPut, handler);
    		return GSON.fromJson(response, resultClass);
        }finally{
            read.unlock();
        }
	}
}
