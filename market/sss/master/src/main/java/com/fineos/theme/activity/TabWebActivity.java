package com.fineos.theme.activity;

import java.util.Observable;
import java.util.Observer;
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
import android.view.MenuItem;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.fineos.theme.R;
import com.fineos.theme.utils.ThemeLog;
public class TabWebActivity extends Activity {
    public static WebView webview;
    private String url;
    private int urlId;
    
	private Handler mHandler;
	private static final String TAG = "TabWebActivity";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        getWindow().setFeatureInt( Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
        setTheme(com.fineos.internal.R.style.FineOSTheme_Material_Light_WhiteActionBar);
		setContentView(R.layout.tab_web_view);
        Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		url = bundle.getString("bUrl");
		ThemeLog.v(TAG, "TabWebActivity url="+url);
		String title = bundle.getString("title");
		ThemeLog.v(TAG, "TabWebActivity title="+title);
		getActionBar().setTitle(title);
		getActionBar().setDisplayUseLogoEnabled(false);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setDisplayShowHomeEnabled(false);
		
        webview = (WebView)findViewById(R.id.main_web);
        webview.getSettings().setJavaScriptEnabled(true); 

        webview.setWebChromeClient(new WebChromeClient() {  
        	@Override
            public void onProgressChanged(WebView view, int progress) {
        		ThemeLog.v(TAG, "onProgressChanged" + "progress ="+progress);
            	setProgress(progress * 100);
            }  
        }); 
        webview.loadUrl(url); 
        webview.setWebViewClient(new webViewClient()); 
        
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
    
    @Override
	public void finish() {
		super.finish();
		overridePendingTransition(com.fineos.R.anim.slide_in_left, com.fineos.R.anim.slide_out_right);
	}
	
    private class webViewClient extends WebViewClient { 
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) { 
//        	view.setWebChromeClient(new WebChromeClient() {  
//            	@Override
//                public void onProgressChanged(WebView view, int progress) {
//                	ThemeLog.v("shouldOverrideUrlLoading", "progress ="+progress);
//                	setProgress(progress * 100);
//                }  
//            }); 
            view.loadUrl(url); 
            return true; 
        } 
    } 
}