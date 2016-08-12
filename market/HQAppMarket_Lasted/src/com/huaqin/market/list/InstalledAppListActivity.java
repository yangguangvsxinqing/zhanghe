package com.huaqin.market.list;

import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.huaqin.market.R;
import com.huaqin.market.utils.OptionsMenu;
import com.huaqin.market.utils.PackageUtil;

public class InstalledAppListActivity extends Activity {
	
	private static final int ACTION_PACKAGE_LIST = 1;
	
	private Context mContext;
	
	private Handler mHandler;
	
	private View mLoadingIndicator;
	private ImageView mLoadingAnimation;
	private AnimationDrawable loadingAnimation;
	private ListView mListView;
	private InstalledAppListAdapter mAppListAdapter;
	private ArrayList<PackageInfo> mInstalledPkgs;
	
	private final BroadcastReceiver mApplicationsReceiver;
	
	public InstalledAppListActivity() {
		
		mApplicationsReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				// refresh installed packages
				getInstalledpackages();
			}
		};
	}
	
	private void initHandler() {
		// TODO Auto-generated method stub
		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				if (msg.what == ACTION_PACKAGE_LIST) {
					refreshListView();
				} 
			}
		};
	}

	private void initListView() {
		// TODO Auto-generated method stub
		mLoadingIndicator = findViewById(R.id.fullscreen_loading_indicator);
		mLoadingAnimation = 
			(ImageView) mLoadingIndicator.findViewById(R.id.fullscreen_loading);
		mLoadingAnimation.setBackgroundResource(R.anim.loading_anim);
		loadingAnimation = (AnimationDrawable) mLoadingAnimation.getBackground();
		mLoadingAnimation.post(new Runnable(){
			@Override     
			public void run() {
				loadingAnimation.start();     
			}                     
		});  
		
		mListView = (ListView) findViewById(android.R.id.list);
		mListView.setScrollbarFadingEnabled(true);
		
		View emptyView = findViewById(R.id.list_empty);
		mListView.setEmptyView(emptyView);
		
		getInstalledpackages();
	}
	
	private void getInstalledpackages() {
		// TODO Auto-generated method stub
		if (mInstalledPkgs != null) {
			mInstalledPkgs.clear();
			mInstalledPkgs = null;
		}
		new QueryPackagesTask().execute();
	}
	
	private void refreshListView() {
		// TODO Auto-generated method stub
		if (mAppListAdapter != null) {
			mAppListAdapter.clear();
			mAppListAdapter = null;
		}
		Log.v("asd", "refreshListView");
		mAppListAdapter =
			new InstalledAppListAdapter(mContext, mInstalledPkgs);
		mListView.setAdapter(mAppListAdapter);
		
		mLoadingIndicator.setVisibility(View.GONE);
		mListView.setVisibility(View.VISIBLE);
	}
	
	private void registerIntentReceivers() {
		// TODO Auto-generated method stub
		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		intentFilter.addDataScheme("package");
		registerReceiver(mApplicationsReceiver, intentFilter);
	}
	
	private void unregisterIntentReceivers() {
		// TODO Auto-generated method stub
		unregisterReceiver(mApplicationsReceiver);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		mContext = this;
		
		setContentView(R.layout.app_list);
		
		initHandler();
		initListView();
		registerIntentReceivers();
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// TODO Auto-generated method stub
//		boolean bRet = super.onCreateOptionsMenu(menu);
//		if (bRet) {
//			bRet = OptionsMenu.onCreateOptionsMenu(menu);
//		}
//		return bRet;
//	}
//    @Override
//    public boolean onKeyUp(int keyCode, KeyEvent event) {
//    	if (keyCode == KeyEvent.KEYCODE_MENU) {
//    		startActivity(new Intent(InstalledAppListActivity.this, OptionsMenu.class));
//    		overridePendingTransition(R.anim.fade, R.anim.hold);
//    	}
//    	return super.onKeyUp(keyCode, event);
//    }

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		if (mAppListAdapter != null) {
			mAppListAdapter.clear();
		}
		if (mListView != null) {
			mListView.setAdapter(null);
		}
		unregisterIntentReceivers();
	}

//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		// TODO Auto-generated method stub
//		return OptionsMenu.onOptionsItemSelected(this, item);
//	}

	/*
	 * AsyncTask to get all installed, not system packages' info
	 */
	private class QueryPackagesTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO Auto-generated method stub
			mInstalledPkgs = PackageUtil.getInstalledPackages(mContext);
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			if (result) {
				mHandler.sendEmptyMessage(ACTION_PACKAGE_LIST);
			}
		}
	}
}