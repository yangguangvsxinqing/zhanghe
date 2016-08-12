package com.huaqin.market.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.LocalActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.BaseColumns;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.huaqin.market.R;
import com.huaqin.market.SlideViewPager;
import com.huaqin.market.download.Constants;
import com.huaqin.market.download.DownloadManager;
import com.huaqin.market.download.DownloadReceiver;
import com.huaqin.market.download.Downloads;
import com.huaqin.market.download.Helpers;
import com.huaqin.market.list.DownloadAppBrowser;
import com.huaqin.market.list.DownloadingAppListActivity;
import com.huaqin.market.model.Application2;
import com.huaqin.market.model.Image2;
import com.huaqin.market.utils.CachedThumbnails;
import com.huaqin.market.utils.Constant;
import com.huaqin.market.utils.DeviceUtil;
import com.huaqin.market.utils.OptionsMenu;
import com.huaqin.market.utils.PackageUtil;
import com.huaqin.market.webservice.IMarketService;
import com.huaqin.market.webservice.MarketService;
import com.huaqin.market.webservice.Request;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.DisplayMetrics;

import com.huaqin.market.ui.AppInfoCommentsActivity;
import com.huaqin.market.ui.AppInfoDetailActivity;
import com.huaqin.market.ui.MarketPage;


public class AppInfoActivity extends Activity{
	private static final String TAG = "AppInfoActivity";
//	private static final int TAB_ID_APP_DETAIL = 0;
//	private static final int TAB_ID_APP_COMMENT = 1;
//	private static final int TAB_ID_APP_RELATIVE = 2;
//	private static final String TAB_APP_DETAIL = "appdetail";
//	private static final String TAB_APP_COMMENT = "appcomment";
//	private static final String TAB_APP_RELATIVE = "apprelative";
	private static final int DIALOG_SDCARD_NOT_AMOUNT = 100;
	private static final int DIALOG_SDCARD_NOT_ENOUGH = 101;
	private static final int DIALOG_UPGRADE_DOWNLOADING = 102;
	private static final int PROGRESSBAR_UPDATEING = 103;
	public static final String ACTION_DETAIL_DOWNLOAD= "com.hauqin.intent.action.APP_DETAIL_DOWNLOAD";
	public static final String ACTION_DETAIL_DOWNLOAD_REZUME= "com.hauqin.intent.action.APP_DETAIL_DOWNLOAD_REZUME";
	public static final String ACTION_DETAIL_DOWNLOAD_PAUSE= "com.hauqin.intent.action.APP_DETAIL_DOWNLOAD_PAUSE";
	public static final String ACTION_DETAIL_FREE= "com.hauqin.intent.action.APP_DETAIL_FREE";
	public static final int ACTION_NETWORK_BYTES= 104;
	private boolean timerFlag = false;
	public static int downloadFlag = 0;
//	private TabHost mTabHost;
//	private ArrayList<View> mTabViews;
	private View mLoadingIndicator;
	private View mAppInfoTop;
	private ImageView mAppThumb;
//	private Button mDownloadButton;
	private RatingBar mAppRating;
	private TextView mAppName;
//	private TextView mAppRateNum;
//	private TextView mAppStatus;
	private Timer txBytesTimer = new Timer();
	private Application2 mAppInfo;
	private int nAppId;
	private boolean bUpdateAvailable;
	private boolean bInstalled;
	private boolean bDownloadNotInstalled;
	private String mPackageName;
	private ProgressBar mProgressBar;
	private TextView mProgressBytes;
	private final BroadcastReceiver mApplicationsReceiver;
	private final BroadcastReceiver mUpdateReceiver;
	private final BroadcastReceiver mApplicationDeleteReciever;
	private final BroadcastReceiver mApplicationsInstallingReceiver;
	private final BroadcastReceiver mApplicationsInstalledReceiver;
	private File downloadedAppFile;
	
	private int type = 0;
	private int fromSplash = 0;
	private int fromPudding = 1;
	Drawable drawable = null;
	private Handler mHandler;
	private static final int ACTION_APP_ICON = 3;
	private static final int ACTION_NETWORK_ERROR = 4;
	private static final int ACTION_USER_PV = 1;
	private IMarketService mMarketService;
	
	private static final String APP_DIR_NAME = "/hqappmarket";
	private static final String APP_DIR_PATH = Environment.getExternalStorageDirectory() + APP_DIR_NAME;
	private static final String APK_DIR_PATH = APP_DIR_PATH + "/apks";
	private static final String ICON_DIR_PATH = APP_DIR_PATH + "/icons";
	public static MarketPage amPager;//页卡内容
	private List<View> listViews; // Tab页面列表
	private LocalActivityManager manager = null;
	
	private ImageView cursorImage;// 动画图片
	private TextView t1, t2;// 页卡头标
	private int offset = 0;// 动画图片偏移量
	private int currIndex = 0;// 当前页卡编号
	private int bmpW;// 动画图片宽度
//	private String page = null;

	private ImageView mDownloadImage;
	private TextView mDownloadText;
	private int downFlag = 0;
//	private int appid;
//	private int progress;
	private Context mContext;
	private Cursor mCursor;
	private DownloadContentObserver mObserver;
	private DownloadThread mDownloadThread;
	private String[] mCols = new String [] {
            Downloads._ID,
            Downloads.COLUMN_TITLE,
            Downloads.COLUMN_CURRENT_BYTES,
            Downloads.COLUMN_TOTAL_BYTES,
            Downloads._DATA,
            Downloads.COLUMN_APP_ID,
            Downloads.COLUMN_CONTROL
    };
	private final int idColumn = 0;
	private final int titleColumn = 1;
	private final int currentBytesColumn = 2;
	private final int totalBytesColumn = 3;
	private final int dataColumn = 4;
	private final int appIdColumn = 5;
	private final int controlColumn = 6;
	private long forwardBytes = 0;
	private long currentBytes = 0;
	private final int currentBytesRate = 2;
	public AppInfoActivity() {

		nAppId = 0;
		bUpdateAvailable = false;
		bInstalled = false;
		bDownloadNotInstalled = false;
		mPackageName = "";
		mContext = this;

		type = 0;

		mApplicationsReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				final String pkgName = intent.getDataString().replace("package:", "");
				if (pkgName.equals(mAppInfo.getAppPackage())) {
					initAppStatus();
				}
				
			}
		};
		
		mUpdateReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent intent) {
				// TODO Auto-generated method stub
				final String action = intent.getAction();
				int rAppId = intent.getIntExtra("detailappId", 1);
				if (action.equals(ACTION_DETAIL_DOWNLOAD_PAUSE)) {
					if(rAppId == nAppId)
					{	downFlag = 2;}
				} else if (action.equals(ACTION_DETAIL_DOWNLOAD_REZUME)) {
					if(rAppId == nAppId)
					{	downFlag = 1;}
				}
				initAppStatus();
			}
		};
		
		mApplicationDeleteReciever = new BroadcastReceiver() {

			@Override
			public void onReceive(Context arg0, Intent intent) {
				// TODO Auto-generated method stub
				final String action = intent.getAction();
				if (action.equals(DownloadingAppListActivity.ACTION_APP_DELETED)) {
					mDownloadText.setText(R.string.app_download);
					mProgressBar.setProgress(0);
					downFlag = 0;
				}
			}
		};
		
		mApplicationsInstalledReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				/**************Added-s by JimmyJin 20120816*****************/
				final String action = intent.getAction();
				String pkgName = intent.getDataString().replace("package:", "");
				if(action.equals(Intent.ACTION_PACKAGE_ADDED)){
					if(pkgName.equals(mAppInfo.getAppPackage())){
						bUpdateAvailable = false;
						bInstalled = true;
						bDownloadNotInstalled = false;
						pkgName += getText(R.string.app_has_installed);
						mProgressBar.setProgress(100);
						Intent newintent = new Intent();
						newintent.setAction(AppInfoCommentsActivity.ADD_COMMENT_REFLASH);
						sendBroadcast(newintent);
					}
//					Toast.makeText(getApplicationContext(), getText(R.string.app_has_installed), Toast.LENGTH_LONG).show();	
				}
				onResume();
			}
		};
		
		mApplicationsInstallingReceiver = new BroadcastReceiver(){

			@Override
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();
				if(action.equals(DownloadReceiver.ACTION_APP_INSTALLING)){
					mProgressBar.setProgress(100);
					bUpdateAvailable = false;
					bInstalled = false;
					bDownloadNotInstalled = true;
					Log.v("asd", "ACTION_DOWNLOAD_COMPLETED initAppStatus");
					initAppStatus();
				}
			}
		};
	}

	private void initViews() {
		// TODO Auto-generated method stub
		mAppInfoTop = findViewById(R.id.appinfo_tabs);
		mLoadingIndicator = findViewById(R.id.fullscreen_loading_indicator);
		mLoadingIndicator.setVisibility(View.GONE);
		mAppInfoTop.setVisibility(View.VISIBLE);

		mProgressBar = (ProgressBar) findViewById(R.id.app_download_progressbar);
		mProgressBytes = (TextView)findViewById(R.id.air_download_bytes);
		mProgressBar.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (bUpdateAvailable) {
					Application2 appInfo =
						new Application2(PackageUtil.getUpdateApplication(mAppInfo.getAppPackage()));
					long sdCardAvailSize = Helpers.getAvailaleSize();
					if (sdCardAvailSize == -1) {
						showDialog(DIALOG_SDCARD_NOT_AMOUNT);
					} else if(sdCardAvailSize < appInfo.getSize()) {
						showDialog(DIALOG_SDCARD_NOT_ENOUGH);
					} else {
//						if(!DownloadManager.queryDownloadingURL(AppInfoActivity.this, appInfo)) {
//							DownloadManager.startDownloadAPK(AppInfoActivity.this, appInfo, String.valueOf(type));
////							Log.v("asd", "downFlag = "+downFlag);
////							if(downFlag == 0){
////								downFlag = 1;
////								DownloadManager.resumeDownload(mContext, new long[]{nAppId});
////							}
////							if(downFlag == 1){
////								downFlag = 2;
////								DownloadManager.pauseDownload(mContext, new long[]{nAppId});
////							}
//							

////							if(type == fromPudding){
////								finish();
////								android.os.Process.killProcess(android.os.Process.myPid());
////							}else if(type == fromSplash){
////								finish();
////							}
//

//							Toast.makeText(getApplicationContext(), getText(R.string.app_downloading_view_other_layout), Toast.LENGTH_LONG).show();
//							
//						} else {
//							showDialog(DIALOG_UPGRADE_DOWNLOADING);
//						}
						Log.v("asd", "downFlag = "+downFlag);
						switch(downFlag)
						{
							case 0:
								DownloadManager.startDownloadAPK(AppInfoActivity.this, mAppInfo,String.valueOf(type));
								Toast.makeText(mContext.getApplicationContext(), mContext.getText(R.string.app_downloading_view_other_layout), Toast.LENGTH_LONG).show();
								downFlag=1;
								break;
							case 1:
								DownloadManager.pauseDownload(mContext, new long[]{nAppId});
								downFlag=2;
								break;
							case 2:
								DownloadManager.resumeDownload(mContext, new long[]{nAppId});
								downFlag=1;
								break;
						}
					}
				} else if(bDownloadNotInstalled) { 
					Intent intent = new Intent("com.hauqin.intent.action.APP_DOWNLOAD_START");
	            	AppInfoActivity.this.sendBroadcast(intent);
					Intent install = new Intent(Intent.ACTION_VIEW);
	    			install.setDataAndType(Uri.fromFile(downloadedAppFile), Constants.MIMETYPE_APK);
	    			install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    			AppInfoActivity.this.startActivity(install);
				}else if(bInstalled){
					Intent intent = getPackageManager().getLaunchIntentForPackage(mAppInfo.getAppPackage());  
					if(intent != null){
					startActivity(intent); } 

				} else {
					long sdCardAvailSize = Helpers.getAvailaleSize();
					if (sdCardAvailSize == -1) {
						showDialog(DIALOG_SDCARD_NOT_AMOUNT);
					} else if(sdCardAvailSize < mAppInfo.getSize()) {
						showDialog(DIALOG_SDCARD_NOT_ENOUGH);
					} else {
						Log.v("asd", "startDownloadAPK downFlag = "+downFlag);
						switch(downFlag)
						{
							case 0:
								DownloadManager.startDownloadAPK(AppInfoActivity.this, mAppInfo,String.valueOf(type));
								Toast.makeText(mContext.getApplicationContext(), mContext.getText(R.string.app_downloading_view_other_layout), Toast.LENGTH_LONG).show();
								downFlag=1;
								break;
							case 1:
								DownloadManager.pauseDownload(mContext, new long[]{nAppId});
								downFlag=2;
								break;
							case 2:
								DownloadManager.resumeDownload(mContext, new long[]{nAppId});
								downFlag=1;
								break;
						}
//						if(type == fromPudding){
//							finish();
//							android.os.Process.killProcess(android.os.Process.myPid());
//						}else if(type == fromSplash){
//							finish();
//						}

//						Toast.makeText(getApplicationContext(), getText(R.string.app_downloading_view_other_layout), Toast.LENGTH_LONG).show();
					}
				}
				initAppStatus();
				
			}
			
		});	
		initTopBar();
		initBottomBar();
		initAppStatus();
		initTabs();
		Log.v("asd", "initAppStatus");
//		if(mCursor != null){
//			mProgressBar.setProgress(mCursor.getInt(currentBytesColumn)*100/mCursor.getInt(totalBytesColumn));
//		}
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
		request.setData(params);
		request.addObserver(new Observer() {

			@Override
			public void update(Observable observable, Object data) {
				// TODO Auto-generated method stub
				if (data != null) {
		    		Message msg = Message.obtain(mHandler, ACTION_USER_PV, data);
		    		mHandler.sendMessage(msg);
		    	}
			}
		});
//		mCurrentRequest = request;
		Log.v("postPV", "mMarketService ="+mMarketService);
		mMarketService.PostPV(request);
	}
	private void initTopBar() {
		// TODO Auto-generated method stub
		if(type == fromSplash){//来自装机精灵，直接从缓存中取图片	
			drawable = CachedThumbnails.getThumbnail(this, nAppId);	
			mAppThumb = (ImageView) mAppInfoTop.findViewById(R.id.iv_appinfo_thumb);
			if (drawable == null) {
				drawable = CachedThumbnails.getDefaultIcon(this);
			}
			mAppThumb.setImageDrawable(drawable); 
		}	
///		mDownloadButton = (Button) mAppInfoTop.findViewById(R.id.btn_appinfo_download);
//		
//		mDownloadButton.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				if (bUpdateAvailable) {
//					Application2 appInfo =
//						new Application2(PackageUtil.getUpdateApplication(mAppInfo.getAppPackage()));
//					long sdCardAvailSize = Helpers.getAvailaleSize();
//					if (sdCardAvailSize == -1) {
//						showDialog(DIALOG_SDCARD_NOT_AMOUNT);
//					} else if(sdCardAvailSize < appInfo.getSize()) {
//						showDialog(DIALOG_SDCARD_NOT_ENOUGH);
//					} else {
//						if(!DownloadManager.queryDownloadingURL(AppInfoActivity.this, appInfo)) {
//							DownloadManager.startDownloadAPK(AppInfoActivity.this, appInfo, String.valueOf(type));
////							if(type == fromPudding){
////								finish();
////								android.os.Process.killProcess(android.os.Process.myPid());
////							}else if(type == fromSplash){
////								finish();
////							}
//
//							Toast.makeText(getApplicationContext(), getText(R.string.app_downloading_view_other_layout), Toast.LENGTH_LONG).show();
//						} else {
//							showDialog(DIALOG_UPGRADE_DOWNLOADING);
//						}
//					}
//				} else if(bDownloadNotInstalled) { 
//					Intent install = new Intent(Intent.ACTION_VIEW);
//	    			install.setDataAndType(Uri.fromFile(downloadedAppFile), Constants.MIMETYPE_APK);
//	    			install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//	    			AppInfoActivity.this.startActivity(install);
//				} else {
//					long sdCardAvailSize = Helpers.getAvailaleSize();
//					if (sdCardAvailSize == -1) {
//						showDialog(DIALOG_SDCARD_NOT_AMOUNT);
//					} else if(sdCardAvailSize < mAppInfo.getSize()) {
//						showDialog(DIALOG_SDCARD_NOT_ENOUGH);
//					} else {
//						DownloadManager.startDownloadAPK(AppInfoActivity.this, mAppInfo,String.valueOf(type));
////						if(type == fromPudding){
////							finish();
////							android.os.Process.killProcess(android.os.Process.myPid());
////						}else if(type == fromSplash){
////							finish();
////						}
//
////						Toast.makeText(getApplicationContext(), getText(R.string.app_downloading_view_other_layout), Toast.LENGTH_LONG).show();
//					}
//				}
//			
//			}
//		});
//			
		mAppRating = (RatingBar) mAppInfoTop.findViewById(R.id.rb_appinfo_rating);
		mAppRating.setRating(mAppInfo.getStars());
		
		mAppName = (TextView) mAppInfoTop.findViewById(R.id.tv_appinfo_name);
		mAppName.setText(mAppInfo.getAppName());
		
//		mAppRateNum = (TextView) mAppInfoTop.findViewById(R.id.tv_appinfo_rate_num);
//		mAppRateNum.setText(mAppInfo.getDownloads() + getString(R.string.app_rate_num));
		
//		mAppStatus = (TextView) mAppInfoTop.findViewById(R.id.tv_appinfo_status);
	}
	private void initAppStatus() {
		int controlValue = 2;
		int appStatus =
			PackageUtil.getApplicationStatus(getPackageManager(), mAppInfo.getAppPackage());
		
		Log.v("initAppStatus", "appStatus = "+appStatus);
		Log.v("initAppStatus", "mAppInfo.getAppPackage() = "+mAppInfo.getAppPackage());
		boolean downloading = DownloadManager.queryDownloadingURL(this, mAppInfo);
		String downloadedPath = DownloadManager.queryDownloadedURL(this, mAppInfo);
		String downloadedORDownloading = DownloadManager.queryDownloadedORDownloadingAPKEx(mContext, mAppInfo);
		String where = Downloads.WHERE_RUNNING +" and "+Downloads.WHERE_APP_ID;

		Log.v("asd", "where = "+where);
		
		String downloadedforDownloading = DownloadManager.queryDownloadedURL(mContext, mAppInfo);

		if(null == downloadedORDownloading||null == downloadedforDownloading)
		if(downloadedORDownloading != null&&downloadedORDownloading.equals("Downloading"))
		
			Log.v("initAppStatus", "downloadedORDownloading = "+downloadedORDownloading);
		if(downloadedORDownloading != null) {
			downloadedAppFile = new File(downloadedORDownloading);
		}
		
		mCursor = mContext.getContentResolver()
				.query(Downloads.CONTENT_URI,
						mCols, where, new String[]{"" + nAppId}, null);
		if(mCursor != null){
			if(mCursor.moveToFirst()){
				controlValue = mCursor.getInt(controlColumn);
			}
		}
		if(mCursor!=null&&!mCursor.isClosed()){
			mCursor.close();
		}
		switch (appStatus) {
		case PackageUtil.PACKAGE_NOT_INSTALLED:
			bUpdateAvailable = false;
			bInstalled = false;
			Log.v("initAppStatus", "downloadedORDownloading12 = "+downloadedORDownloading);
			if(downloadedORDownloading != null&&downloadedORDownloading.equals("Downloading")){
//				mDownloadButton.setClickable(false);
//				mAppStatus.setText(R.string.app_downloading);
				
				if(controlValue == 0) {
					mDownloadImage.setBackgroundResource(R.drawable.btn_appinfo_downloaded_pause);
					downFlag = 1;
					mDownloadText.setText(R.string.app_pause);
				} else if(controlValue == 1) {
					mDownloadImage.setBackgroundResource(R.drawable.btn_appinfo_downloaded_continu);
					downFlag = 2;
					mDownloadText.setText(R.string.app_rezume);
				}
			} else if(downloadedAppFile != null && downloadedAppFile.exists()) {
//				mDownloadButton.setClickable(true);
				bDownloadNotInstalled = true;
//				mAppStatus.setText(R.string.app_downloaded);
//				mDownloadButton.setText(getText(R.string.app_install));
//				mDownloadButton.setTextColor(Color.WHITE);
//				mDownloadButton.setBackgroundResource(R.drawable.btn_appinfo_install);
				mDownloadImage.setBackgroundResource(R.drawable.btn_manage_downloaded_install);
				mDownloadText.setText(R.string.app_install);
				mProgressBar.setProgress(100);
				mProgressBytes.setVisibility(View.GONE);
			} else {
//				mDownloadButton.setClickable(true);
//				mAppStatus.setText(R.string.app_fee_free);
				mDownloadImage.setBackgroundResource(R.drawable.subinfo_app_download_down);
				mDownloadText.setText(R.string.app_download);
				mProgressBar.setProgress(0);
			}
			break;
			
		case PackageUtil.PACKAGE_INSTALLED:
			bUpdateAvailable = false;
			bInstalled = true;
//			mDownloadButton.setClickable(false);
//			mAppStatus.setText(R.string.app_installed);
			mDownloadImage.setBackgroundResource(R.drawable.btn_subinfo_open);
			mDownloadText.setText(R.string.app_open);
			break;
			
		case PackageUtil.PACKAGE_UPDATE_AVAILABLE:
			bUpdateAvailable = true;
			bInstalled = false;
//			mDownloadButton.setClickable(true);
//			mAppStatus.setText(R.string.app_update_available);
			mDownloadImage.setBackgroundResource(R.drawable.btn_subinfo_update);
			mDownloadText.setText(R.string.app_update_available);
			if(downloading){
				if(controlValue == 0) {
					mDownloadImage.setBackgroundResource(R.drawable.btn_appinfo_downloaded_pause);
					downFlag = 1;
					mDownloadText.setText(R.string.app_pause);
				} else if(controlValue == 1) {
					mDownloadImage.setBackgroundResource(R.drawable.btn_appinfo_downloaded_continu);
					downFlag = 2;
					mDownloadText.setText(R.string.app_rezume);
				}
			}

			if(downloadedAppFile != null && downloadedAppFile.exists()) {
//				mDownloadButton.setClickable(true);
				bUpdateAvailable = false;
				bDownloadNotInstalled = true;
				Log.v("asd", "mProgressBar = ");
//				mAppStatus.setText(R.string.app_downloaded);
//				mDownloadButton.setText(getText(R.string.app_install));
//				mDownloadButton.setTextColor(Color.WHITE);
//				mDownloadButton.setBackgroundResource(R.drawable.btn_appinfo_install);
				mDownloadImage.setBackgroundResource(R.drawable.btn_manage_downloaded_install);
				mDownloadText.setText(R.string.app_install);
				mProgressBar.setProgress(100);
			}
			break;
			
		default:
			break;
		}
	}

	private void initTabs() {
		// TODO Auto-generated method stub		
		
		amPager = (MarketPage) findViewById(R.id.viewpage);
		Log.v("asd", "mPager="+amPager);
		listViews = new ArrayList<View>();
		Intent intent1 = new Intent();
		intent1.setClass(this, AppInfoDetailActivity.class);
		intent1.putExtra("appId", nAppId);
		t1.setText(R.string.app_detail);
		listViews.add(getView("1", intent1));
		
		Intent intent2 = new Intent();
		intent2.setClass(this, AppInfoCommentsActivity.class);
		intent2.putExtra("appId", nAppId);
		t2.setText(R.string.app_comment);
		listViews.add(getView("2", intent2));
		
		amPager.setAdapter(new MyPagerAdapter(listViews));
		amPager.setCurrentItem(0);
		amPager.setOnPageChangeListener(new MyOnPageChangeListener());
	}

	private void initBottomBar() {
		mDownloadImage = (ImageView)findViewById(R.id.air_download);
		mDownloadText = (TextView)findViewById(R.id.air_download_text);
		ImageView imContact = null;
		imContact = (ImageView)findViewById(R.id.to_contact);
		imContact.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(AppInfoActivity.this, AddContactActivity.class);
				startActivity(intent);
			}
		});
		ImageView imManege = null;
		imManege = (ImageView)findViewById(R.id.to_download);
		imManege.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(AppInfoActivity.this, DownloadAppBrowser.class);
				startActivity(intent);
			}
		});
	}
	private void registerIntentReceivers() {
    	// TODO Auto-generated method stub
	    IntentFilter intentFilter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
	    intentFilter.addDataScheme("package");
	    registerReceiver(mApplicationsReceiver, intentFilter);
	    
	    IntentFilter updateIntentFilter = new IntentFilter(ACTION_DETAIL_DOWNLOAD);
		registerReceiver(mUpdateReceiver, updateIntentFilter);
		
		IntentFilter deleteIntentFilter = new IntentFilter(DownloadingAppListActivity.ACTION_APP_DELETED);
		deleteIntentFilter.addCategory(Downloads.ACTION_DOWNLOAD_COMPLETED);
		registerReceiver(mApplicationDeleteReciever, deleteIntentFilter);
		
		IntentFilter intentInstalledFilter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
		intentInstalledFilter.addDataScheme("package");
		registerReceiver(mApplicationsInstalledReceiver, intentInstalledFilter);
		
		
		IntentFilter intentInstallingFilter = new IntentFilter(DownloadReceiver.ACTION_APP_INSTALLING);
		registerReceiver(mApplicationsInstallingReceiver, intentInstallingFilter);
		
	}

	private void unregisterIntentReceivers() {
    	// TODO Auto-generated method stub
    	unregisterReceiver(mApplicationsReceiver);
    	unregisterReceiver(mUpdateReceiver);
    	unregisterReceiver(mApplicationDeleteReciever);
    	unregisterReceiver(mApplicationsInstalledReceiver);
    	unregisterReceiver(mApplicationsInstallingReceiver);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		mAppInfo = (Application2) getIntent().getSerializableExtra("appInfo");
		File dir = new File(APK_DIR_PATH);
		if(!dir.exists()){
			dir.mkdirs();
		}
		
		dir = null;		
		dir = new File(ICON_DIR_PATH);
		if(!dir.exists()){
			dir.mkdirs();
		}
	    manager = new LocalActivityManager(this, true);
        manager.dispatchCreate(savedInstanceState);
		type = getIntent().getIntExtra("type", 0);

		
		nAppId = mAppInfo.getAppId();
		
		setContentView(R.layout.appinfo_main);
		initHandler();
		mMarketService = MarketService.getServiceInstance(this);
//		if(type == fromPudding){
			addThumbnailRequest(nAppId);
//		}
		InitTextView();
		initViews();
		InitImageView();
//		try {
//			postPV("应用详情");
//		} catch (NameNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		mObserver = new DownloadContentObserver();
        getContentResolver().registerContentObserver(Downloads.CONTENT_URI,
                true, mObserver);
		registerIntentReceivers();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		
		super.onDestroy();
		unregisterIntentReceivers();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		if (isFinishing()) {
			
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		if (mPackageName != null) {
			
		}
		Log.v("asd", "onResume");

		initAppStatus();
		initTabs();
		InitTextView();
		super.onResume();
	}

	public Application2 getAppInfo() {
		// TODO Auto-generated method stub
		return mAppInfo;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		switch (id) {
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
	
	
	private void initHandler() {
		// TODO Auto-generated method stub
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch (msg.what) {
				case ACTION_APP_ICON:
					Image2 icInfo = (Image2) msg.obj;
					
					if (icInfo.mAppIcon != null) {
						CachedThumbnails.cacheThumbnail(
								AppInfoActivity.this,
								icInfo._id, icInfo.mAppIcon);
						
						drawable = icInfo.mAppIcon;
						mAppThumb = (ImageView) mAppInfoTop.findViewById(R.id.iv_appinfo_thumb);
						if (drawable == null) {
							drawable = CachedThumbnails.getDefaultIcon(AppInfoActivity.this);
						} 
						mAppThumb.setImageDrawable(drawable);
					}
					break;
				case PROGRESSBAR_UPDATEING:
					Object[] data = (Object[])msg.obj;
					int mAppId = ((Integer)data[0]).intValue();
					int progress = ((Integer)data[1]).intValue();
					mProgressBar = (ProgressBar) findViewById(R.id.app_download_progressbar);
					if(mProgressBar == null) {
						Log.d(TAG, "mProgressBar is null");
					}else{
						if(nAppId == mAppId ){
							mProgressBar.setProgress(progress);}
					}
					break;
					
				case ACTION_NETWORK_BYTES:
					Object bytesData = (Object)msg.obj;
					String str = bytesData+"";
					long downLoadSpeed = 0;
					if(str != null){
						downLoadSpeed = Long.valueOf(String.valueOf(str)).longValue();
					}
					if(downLoadSpeed < 1024){
						mProgressBytes.setText(downLoadSpeed+"b/s");
					}else if(downLoadSpeed > 1024*1024){
						mProgressBytes.setText((float)(Math.round((float)downLoadSpeed/(1024*1024)*100))/100+"m/s");
					}else{
						mProgressBytes.setText(downLoadSpeed/1024+"k/s");
					}
					
					break;
				case ACTION_NETWORK_ERROR:
					break;
				default:
					break;
				}
			}
		};
	}
//	@Override    
//    public boolean dispatchKeyEvent(KeyEvent event) {    
//    	if (event.getKeyCode() == KeyEvent.KEYCODE_BACK                 
//    			&& event.getAction() == KeyEvent.ACTION_UP) {      
//    		Intent intentCateFree = new Intent(ACTION_DETAIL_FREE);    
//            this.sendBroadcast(intentCateFree);
//    		finish();
//    		} 
//    	if (event.getKeyCode()  == KeyEvent.KEYCODE_MENU&& event.getAction() == KeyEvent.ACTION_UP) {
//    		startActivity(new Intent(AppInfoActivity.this, OptionsMenu.class));
//    		overridePendingTransition(R.anim.fade, R.anim.hold);
//    	}
//    	return false;
// }
	private void addThumbnailRequest(int id) {
		
		Request request = new Request(0L, Constant.TYPE_APP_ICON);
		Object[] params = new Object[2];
		String imgUrl = mAppInfo.getIconUrl();
		
		params[0] = Integer.valueOf(id);
		params[1] = imgUrl;
		request.setData(params);
		request.addObserver(new Observer() {

			@Override
			public void update(Observable observable, Object data) {
				// TODO Auto-generated method stub
				if (data != null) {
					Message msg = Message.obtain(mHandler, ACTION_APP_ICON, data);
					mHandler.sendMessage(msg);
				}
			}
		});
		if (request.getStatus() == Constant.STATUS_ERROR) {
			mHandler.sendEmptyMessage(ACTION_NETWORK_ERROR);
		}
		mMarketService.getAppIcon(request);
	}


    private View getView(String id, Intent intent) {
        return manager.startActivity(id, intent).getDecorView();
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
    	if (event.getKeyCode() == KeyEvent.KEYCODE_BACK                 
    			&& event.getAction() == KeyEvent.ACTION_UP) {      
    		Intent intentCateFree = new Intent(ACTION_DETAIL_FREE);    
            this.sendBroadcast(intentCateFree);
    		finish();
    	} 
    	if (keyCode == KeyEvent.KEYCODE_MENU) {
    		startActivity(new Intent(this, OptionsMenu.class));
    		overridePendingTransition(R.anim.fade, R.anim.hold);
    	}
    	return super.onKeyUp(keyCode, event);
    }
	/**
	 * 初始化头标
	 */
	private void InitTextView() {
		t1 = (TextView) findViewById(R.id.text_info);
		t2 = (TextView) findViewById(R.id.text_comment);

		t1.setOnClickListener(new MyOnClickListener(0));
		t2.setOnClickListener(new MyOnClickListener(1));
	}

	/**
	 * 初始化动画
	 */
	private void InitImageView() {
		cursorImage = (ImageView) findViewById(R.id.cursor);
		bmpW = BitmapFactory.decodeResource(getResources(), R.drawable.arrow_two)
				.getWidth();// 获取图片宽度
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenW = dm.widthPixels;// 获取分辨率宽度
		offset = (screenW / 2 - bmpW)/2;// 计算偏移量
		Matrix matrix = new Matrix();
		matrix.postTranslate(offset, 0);
		cursorImage.setImageMatrix(matrix);// 设置动画初始位置
	}

	/**
	 * ViewPager适配器
	 */
	public class MyPagerAdapter extends PagerAdapter {
		public List<View> mListViews;

		public MyPagerAdapter(List<View> mListViews) {
			this.mListViews = mListViews;
		}

		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
			((ViewPager) arg0).removeView(mListViews.get(arg1));
		}

		@Override
		public void finishUpdate(View arg0) {
		}

		@Override
		public int getCount() {
			return mListViews.size();
		}

		@Override
		public Object instantiateItem(View arg0, int arg1) {
			((ViewPager) arg0).addView(mListViews.get(arg1), 0);
			return mListViews.get(arg1);
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == (arg1);
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {
		}
	}

	/**
	 * 头标点击监听
	 */
	public class MyOnClickListener implements View.OnClickListener {
		private int index = 0;

		public MyOnClickListener(int i) {
			index = i;
		}

		@Override
		public void onClick(View v) {
			Log.v("asd", "MyOnClickListener index ="+index);
			amPager.setCurrentItem(index);
		}
	};


	/**
	 * 页卡切换监听
	 */
	public class MyOnPageChangeListener implements OnPageChangeListener {

		int one;// 页卡1 -> 页卡2 偏移量
		@Override
		public void onPageSelected(int arg0) {
			Animation animation = null;
			one = offset*2 + bmpW;
			switch (arg0) {
			case 0:
					animation = new TranslateAnimation(one, 0, 0, 0);
					t1.setTextColor(0xff1c79cc);
					t2.setTextColor(0xff76756e);
				break;
			case 1:
					animation = new TranslateAnimation(offset, one, 0, 0);
					t1.setTextColor(0xff76756e);
					t2.setTextColor(0xff1c79cc);
				break;
			}
			currIndex = arg0;
			Log.v("asd", "currIndex = "+currIndex);
			animation.setFillAfter(true);// True:图片停在动画结束位置
			animation.setDuration(300);
//			cursor.setAlpha(100);
			cursorImage.startAnimation(animation);
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	}
	
	private class DownloadContentObserver extends ContentObserver {

		public DownloadContentObserver() {
			super(mHandler);
		}
		
		@Override
		public void onChange(final boolean selfChange) {
			mDownloadThread = new DownloadThread();
            mDownloadThread.start();
        }
	}
	private class DownloadThread extends Thread {
		@Override
		public void run() {
			updateProgressBar();
		}

		private void updateProgressBar() {
	        
			Cursor cursor = mContext.getContentResolver().query(
	                Downloads.CONTENT_URI, mCols,
	                Downloads.WHERE_RUNNING, null, BaseColumns._ID);
	        if (cursor == null) {
	            return;
	        }
	        try{
		        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
		            int max = cursor.getInt(totalBytesColumn);
		            int progress = cursor.getInt(currentBytesColumn);
		            int appId = cursor.getInt(appIdColumn);
		            currentBytes = progress;
		            Log.v("asd", "progress="+progress);
		            Log.v("asd", "appId="+appId);
		            if(progress < max && max > 0 && (progress * 100) > max) {
		            	Object[] data = new Object[2];
		            	data[0] = new Integer(appId);
		            	data[1] = new Integer(progress * 100 / max);
		            	Log.v("asd", "data="+data[0]);
		            	Message msg = Message.obtain(mHandler, PROGRESSBAR_UPDATEING, data);
						mHandler.sendMessage(msg);
		            }
		        }
	        }finally {
	        	cursor.close();
	        }
	        if(!timerFlag){
				timerFlag = true;
				txBytesTimer.schedule(new TimerTask() {
			        @Override
			        public void run() {
			            long downloadSpeed = 0;
			            if(forwardBytes == 0){
			            	forwardBytes = currentBytes;
			            }else{
			            	downloadSpeed = currentBytes - forwardBytes;
			            	forwardBytes = currentBytes;
			            }
			        	Message msg = Message.obtain(mHandler, ACTION_NETWORK_BYTES,downloadSpeed/currentBytesRate);
						mHandler.sendMessage(msg);
			        	}
			        }, 0, currentBytesRate*1000);
			}
	        
		}
	}
}