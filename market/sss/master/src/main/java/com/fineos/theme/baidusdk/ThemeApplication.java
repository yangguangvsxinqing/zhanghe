package com.fineos.theme.baidusdk;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.cmcm.adsdk.CMAdManager;
import com.fineos.theme.ThemeConfig;
import com.fineos.theme.ThemeDataCache;
import com.fineos.theme.utils.Constant;
import com.fineos.theme.utils.ImageCache;

import android.Manifest;
import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

public class ThemeApplication extends Application {
	public static final String subADKey = "1125";
	public static final String subADPosId = "1125100";
	private RequestQueue mRequestQueue;
	private static ThemeApplication sInstance;
	public static final String[] permissions = new String[]{
		Manifest.permission.READ_PHONE_STATE,
		Manifest.permission.READ_EXTERNAL_STORAGE,
		Manifest.permission.GET_ACCOUNTS,
		Manifest.permission.ACCESS_COARSE_LOCATION
	};
	@Override
	public void onCreate() {
		super.onCreate();
		
		//设置是否是猎豹移动公司内部产品，外部产品不调此接口。
		//CMAdManager.setIsInner();
		//初始化聚合sdk
		//第一个参数：Context
		//第二个参数：AppId
		//第三个参数：产品渠道号Id
		sInstance = this;
		ThemeConfig.setContext(getApplicationContext());
		SharedPreferences sharedPreferences = getSharedPreferences("theme", 0);
		String adPosId = sharedPreferences.getString("adPosId", subADPosId);
		if(adPosId!=null&&adPosId.length()>0){
			CMAdManager.applicationInit(this, subADKey, adPosId);
		    
			//开启Debug模式，默认不开启不会打印log
			CMAdManager.setDebug();	
		}
	    
		
		ImageCache.initCacheThumbnail();
		float cacheSize = ThemeDataCache.getIconCacheFile();
		if (cacheSize > Constant.THEME_CACHE_MAX_SIZE) {
			Thread thread = new Thread() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					ThemeDataCache.deleteFileCaches();
					super.run();
				}
			};
			thread.start();
		}
	}
	public static synchronized ThemeApplication getInstance() {
        return sInstance;
    }

	
	public RequestQueue getRequestQueue() {
		if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return mRequestQueue;
    }

	public void addToRequestQueue(Request req, String tag) {

        // set the default tag if tag is empty

        req.setTag(tag);
        getRequestQueue().add(req);
    }

	public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

}
