package com.huaqin.market.list;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.huaqin.android.market.sdk.bean.Application;
import com.huaqin.android.market.sdk.bean.InstalledApp;
import com.huaqin.market.R;
import com.huaqin.market.download.DownloadManager;
import com.huaqin.market.download.Helpers;
import com.huaqin.market.list.AppListAdapter.AppInfo;
import com.huaqin.market.model.Application2;
import com.huaqin.market.ui.AppInfoActivity;
import com.huaqin.market.ui.AppInfoPreloadActivity;
import com.huaqin.market.utils.Constant;
import com.huaqin.market.utils.OptionsMenu;
import com.huaqin.market.utils.PackageUtil;
import com.huaqin.market.webservice.IMarketService;
import com.huaqin.market.webservice.MarketService;
import com.huaqin.market.webservice.Request;

public class UpgradeAppListActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "UpgradeAppList";
	private static final int ACTION_APPS_LIST = 0;
	private static final int ACTION_UPDATE_AVALIABLE_APPS = 1;
	private static final int ACTION_NETWORK_ERROR = 2;
	
	private static final int DIALOG_NETWORK_ERROR = 100;
	private static final int DIALOG_SDCARD_NOT_AMOUNT = 101;
	private static final int DIALOG_SDCARD_NOT_ENOUGH = 102;
	private static final int DIALOG_UPGRADE_DOWNLOADING = 103;
	
	private Context mContext;
	private Request mCurrentRequest;
	private IMarketService mMarketService;
	private Handler mHandler;
	
	private View mLoadingIndicator;
	private ImageView mLoadingAnimation;
	private AnimationDrawable loadingAnimation;
	private ListView mListView;
	public static UpgradeAppListAdapter mAppListAdapter;
	private int nAppListSize;
	
	private final BroadcastReceiver mApplicationsReceiver;
	private final BroadcastReceiver mApplicationsUpdateReceiver;
	
	public UpgradeAppListActivity() {
		
		nAppListSize = -1;
		Log.v("UpgradeAppListActivity", "UpgradeAppListActivity");
		mApplicationsReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				initListData();
			}
		};
		
		mApplicationsUpdateReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				if  (mAppListAdapter != null) {
					mAppListAdapter.notifyDataSetChanged();
				}
			}
		};
		
	}

	private void initHandler() {
		// TODO Auto-generated method stub
		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch (msg.what) {
				case ACTION_APPS_LIST:
					Log.v("UpgradeAppListActivity", "ACTION_APPS_LIST");
					requestUpdateAvaliableNums();
					break;
					
				case ACTION_UPDATE_AVALIABLE_APPS:
					@SuppressWarnings("unchecked")
					ArrayList<Application> appList =
						(ArrayList<Application>) msg.obj;
					Log.v("UpgradeAppListActivity", "appList = "+appList.size());
					if (mAppListAdapter != null) {
//						mAppListAdapter.clear();
						mAppListAdapter = null;
					}
					mAppListAdapter = new UpgradeAppListAdapter(mContext, appList);
					mListView.setAdapter(mAppListAdapter);
					Log.v("UpgradeAppListActivity", "mAppListAdapter = "+mAppListAdapter);
					mLoadingIndicator.setVisibility(View.GONE);
					mListView.setVisibility(View.VISIBLE);
					break;
					
				case ACTION_NETWORK_ERROR:
					Log.v("UpgradeAppListActivity", "ACTION_NETWORK_ERROR");
					mLoadingIndicator.setVisibility(View.GONE);
					mListView.setVisibility(View.VISIBLE);
					Toast.makeText(mContext, mContext.getString(R.string.error_network_low_speed), Toast.LENGTH_LONG).show();
//					showDialog(DIALOG_NETWORK_ERROR);
					break;
					
				default:
					break;
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
		((TextView) emptyView.findViewById(R.id.list_empty_text))
			.setText(R.string.error_no_app_update);
		mListView.setEmptyView(emptyView);
	}

	private void initListData() {
		// TODO Auto-generated method stub
		int newSize = PackageUtil.getUpdateApps().size();
		
		if (newSize != nAppListSize || mAppListAdapter == null) {
			nAppListSize = newSize;
			refreshListData();
		}		
	}
	
	private void refreshListData() {
//		if(!PackageUtil.getUpdateApps().isEmpty()){
		Log.v("UpgradeAppListActivity", "refreshListData");
			Message msg = Message.obtain(mHandler,
					ACTION_UPDATE_AVALIABLE_APPS,
					PackageUtil.getUpdateApps());
			mHandler.sendMessage(msg);
//		}
			
	}

	private void requestUpdateAvaliableNums() {
		Log.v("UpgradeAppListActivity", "requestUpdateAvaliableNums  void");
        ArrayList<PackageInfo> pkgInfos = PackageUtil.getInstalledPackages(mContext);
        Log.v("UpgradeAppListActivity", "pkgInfos ="+pkgInfos);
		if (pkgInfos.size() == 0) {
			mLoadingIndicator.setVisibility(View.GONE);
			mListView.setVisibility(View.VISIBLE);
    		return;
    	}
		
		InstalledApp[] mInstalledApps = new InstalledApp[pkgInfos.size()];
		Log.v("UpgradeAppListActivity", "pkgInfos = "+pkgInfos.size());
    	for (int i = 0; i < pkgInfos.size(); i++) {
    		PackageInfo pkgInfo = pkgInfos.get(i);
    		InstalledApp app = new InstalledApp();
    		app.setAppPackage(pkgInfo.packageName);
    		app.setVersionCode(pkgInfo.versionCode);
    		mInstalledApps[i] = app;
    	}
    	Log.v("UpgradeAppListActivity", "mInstalledApps = "+mInstalledApps.length);
    	Log.v("UpgradeAppListActivity", "mInstalledApps = "+mInstalledApps);
		Request request = new Request(0, Constant.TYPE_CHECK_APP_UPDATE);
		Log.v("UpgradeAppListActivity", "request = "+request);
		request.setData(mInstalledApps);
		request.addObserver(new Observer() {

			@SuppressWarnings("unchecked")
			@Override
			public void update(Observable observable, Object data) {
				// TODO Auto-generated method stub
				Log.v("UpgradeAppListActivity", "data = "+data);
				if (data != null) {
					ArrayList<Application> list = (ArrayList<Application>) data;
					Log.v("UpgradeAppListActivity", "list = "+list.size());
					Log.v("UpgradeAppListActivity", "list = "+list);
					if(list.size() > 0 || list.size() == 0 ) {
						Log.v("UpgradeAppListActivity", "requestUpdateAvaliableNums = "+list.size());
						Message msg = Message.obtain(mHandler, ACTION_UPDATE_AVALIABLE_APPS, list);
						mHandler.sendMessage(msg);
					} 
				}else {
					Request request = (Request) observable;
					if (request.getStatus() == Constant.STATUS_ERROR) {
    	    	    	mHandler.sendEmptyMessage(ACTION_NETWORK_ERROR);
    	    		}
				} 
			}
		});
		mMarketService.checkAppUpdate(request);
	}

	private void registerIntentReceivers() {
		// TODO Auto-generated method stub
		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		intentFilter.addDataScheme("package");
		registerReceiver(mApplicationsReceiver, intentFilter);
		
		IntentFilter intentFilter2 = new IntentFilter(PackageUtil.ACTION_PACKAGE_UPDATED);
		registerReceiver(mApplicationsUpdateReceiver, intentFilter2);
	}

	private void unregisterIntentReceivers() {
		// TODO Auto-generated method stub
		unregisterReceiver(mApplicationsReceiver);
		unregisterReceiver(mApplicationsUpdateReceiver);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		mContext = this;
	    mMarketService = MarketService.getServiceInstance(this);
	    Log.v("UpgradeAppListActivity", "onCreate");
		setContentView(R.layout.app_list);
		initHandler();
		 Log.v("UpgradeAppListActivity", "initHandler");
		initListView();
		Log.v("UpgradeAppListActivity", "initListView");
		registerIntentReceivers();
		Log.v("UpgradeAppListActivity", "registerIntentReceivers");
		requestUpdateAvaliableNums();
		Log.v("UpgradeAppListActivity", "requestUpdateAvaliableNums");
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this.getParent());
		switch (id) {
		case DIALOG_NETWORK_ERROR:
			builder.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle(R.string.dlg_network_error_title)
			.setMessage(R.string.dlg_network_error_msg)
			.setPositiveButton(R.string.btn_retry, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					if (mCurrentRequest != null) {
						mMarketService.checkAppUpdate(mCurrentRequest);
					}
				}
			})
			.setNegativeButton(R.string.btn_cancel, null);
			break;
		case DIALOG_SDCARD_NOT_AMOUNT:
			builder.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.sdcard_not_amount)
				.setMessage(R.string.sdcard_not_amount_info)
				.setPositiveButton(R.string.btn_ok, null);
			break;
		case DIALOG_SDCARD_NOT_ENOUGH:
			builder.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.dlg_sdcard_size_not_enough_title)
				.setMessage(R.string.dlg_sdcard_size_not_enough_msg)
				.setPositiveButton(R.string.btn_ok, null);
			break;
		case DIALOG_UPGRADE_DOWNLOADING:
			builder.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.dlg_apk_downloading_title)
				.setMessage(R.string.dlg_apk_downloading_msg)
				.setPositiveButton(R.string.btn_ok, null);
		break;
		default:
			break;
		}
		return builder.create();
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
//    		startActivity(new Intent(UpgradeAppListActivity.this, OptionsMenu.class));
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

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		/****************begin,modified by daniel_whj,2012-02-06,for bug HQ00079989&HQ00079704&HQ00079109******************/		
		/*
		 these bugs were caused by function getChildAt(getView),when after deleting or updating app,
		 the category activity "need update app" not update list view in time,so calling  getview
		 is failure .
		 we must update list view and use try catch statement to avoid these cases.
		*/
		initListData();
		//refreshListData();
		/****************end,modified by daniel_whj,2012-02-06,for bug HQ00079989&HQ00079704&HQ00079109******************/
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.app_listitem_operate:
			onOperateClick(v);
			break;
			
		case R.id.app_listitem_group:
			onListItemClick(v);
			break;
			
		default:
			break;
		}
	}
	
	private void onOperateClick(View v) {
		// TODO Auto-generated method stub
		int position = ((Integer) v.getTag()).intValue();
		Log.v("UpgradeAppListActivity", "position = "+position);
		Application2 appInfo = new Application2(mAppListAdapter.getItem(position));
		boolean downloading = DownloadManager.queryDownloadingURL(mContext, appInfo);
		long sdCardAvailSize = Helpers.getAvailaleSize();
		if(sdCardAvailSize == -1) {
			showDialog(DIALOG_SDCARD_NOT_AMOUNT);
		} else if(sdCardAvailSize < appInfo.getSize()) {
			showDialog(DIALOG_SDCARD_NOT_ENOUGH);
		} else {
			Log.v("UpgradeAppListActivity", "onOperateClick "+DownloadManager.queryDownloadingURL(mContext, appInfo));
			if(!DownloadManager.queryDownloadingURL(mContext, appInfo)) {
				DownloadManager.startDownloadAPK(mContext, appInfo,"0");
			} else {
				Log.v("UpgradeAppListActivity", "onOperateClick DIALOG_UPGRADE_DOWNLOADING");
				showDialog(DIALOG_UPGRADE_DOWNLOADING);
			}
		}
	}

	private void onListItemClick(View v) {
		// TODO Auto-generated method stub
		Log.v("UpgradeAppListActivity", "View ="+v);
//		int appId = ((Integer) v.getTag()).intValue();
		int appId = ((Application)v.getTag()).getAppId();
		Log.v("UpgradeAppListActivity", "appId = "+appId);
		if(appId > 0) {
//			Intent intent = new Intent(mContext, AppInfoPreloadActivity.class);
			Intent intent = new Intent(this, AppInfoActivity.class);
			intent.putExtra("appInfo",new Application2((Application)v.getTag()));
			/*************Added-s by JimmyJin for Pudding Project**************/
			intent.putExtra("type",0);
			/*************Added-s by JimmyJin for Pudding Project**************/
			intent.putExtra("appId", appId);
//			intent.putExtra("download", mAppListAdapter.getItem(appId).getTotalDownloads());
			mContext.startActivity(intent);
		}
	}
}