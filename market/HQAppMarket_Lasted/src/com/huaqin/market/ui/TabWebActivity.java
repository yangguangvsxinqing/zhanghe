package com.huaqin.market.ui;

import java.util.Observable;
import java.util.Observer;

import com.huaqin.market.R;
import com.huaqin.market.utils.Constant;
import com.huaqin.market.utils.DeviceUtil;
import com.huaqin.market.utils.OptionsMenu;
import com.huaqin.market.webservice.IMarketService;
import com.huaqin.market.webservice.MarketService;
import com.huaqin.market.webservice.Request;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class TabWebActivity extends Activity {
    public static WebView webview;
    private String url;
    private int appIndex;
    private int urlId;
    
	private IMarketService mMarketService;
	private Handler mHandler;
	private static final int ACTION_USER_PV = 1;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        getWindow().setFeatureInt( Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
        setContentView(R.layout.tab_web_view);
        Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		url = bundle.getString("bUrl");
		Log.v("TabWebActivity", "bUrl ="+url);
		
		appIndex = bundle.getInt("appIndex");
		urlId = bundle.getInt("bUserUrlId");
        webview = (WebView)findViewById(R.id.main_web);
        webview.getSettings().setJavaScriptEnabled(true); 

        webview.setWebChromeClient(new WebChromeClient() {  
        	@Override
            public void onProgressChanged(WebView view, int progress) {
            	Log.v("onProgressChanged", "progress ="+progress);
            	setProgress(progress * 100);
            }  
        }); 
        webview.loadUrl(url); 
        webview.setWebViewClient(new webViewClient()); 
        initHandler();
        mMarketService = MarketService.getServiceInstance(this);
        try {
			postPV(this.getString(R.string.webview_name)+urlId);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	private void postPV(String pageName) throws NameNotFoundException{
		Request request = new Request(0, Constant.TYPE_POST_PV);
		String mDeviceModelId = DeviceUtil.getDeviceModel();
		
		PackageManager manager = this.getPackageManager();
		PackageInfo pkgInfo = manager.getPackageInfo(this.getPackageName(), 0);

		Object[] params = new Object[3];
		
		params[0] = mDeviceModelId;
		params[1] = pkgInfo.versionName;
		params[2] = pageName;
		Log.v("postPV", "mDeviceModelId ="+mDeviceModelId);
		Log.v("postPV", "pkgInfo.versionName ="+pkgInfo.versionName);
		Log.v("postPV", "pageName ="+pageName);
		request.setData(params);
		Log.v("postPV", "request ="+request);
		request.addObserver(new Observer() {

			@Override
			public void update(Observable observable, Object data) {
				// TODO Auto-generated method stub
				Log.v("getPartner", "data ="+data);
				if (data != null) {
					Log.v("getPartner", "update");
		    		Message msg = Message.obtain(mHandler, ACTION_USER_PV, data);
		    		mHandler.sendMessage(msg);
		    	}
			}
		});
//		mCurrentRequest = request;
		Log.v("postPV", "mMarketService ="+mMarketService);
		mMarketService.PostPV(request);
	}    
	private void initHandler() {
		// TODO Auto-generated method stub
		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				Log.v("TabWebActivity", "msg.what ="+msg.what);
				switch (msg.what) {	    	    	
				case ACTION_USER_PV:
					break;
				default:
					break;
				}
			}
		};
	}	
    private class webViewClient extends WebViewClient { 
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) { 
//        	view.setWebChromeClient(new WebChromeClient() {  
//            	@Override
//                public void onProgressChanged(WebView view, int progress) {
//                	Log.v("shouldOverrideUrlLoading", "progress ="+progress);
//                	setProgress(progress * 100);
//                }  
//            }); 
            view.loadUrl(url); 
            return true; 
        } 
    } 

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_MENU) {
    		startActivity(new Intent(this, OptionsMenu.class));
    		overridePendingTransition(R.anim.fade, R.anim.hold);
    	}
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) { 
            webview.goBack(); //goBack()表示返回WebView的上一页面 
        }
        return super.onKeyUp(keyCode, event);
    } 
}