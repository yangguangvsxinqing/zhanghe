package com.huaqin.market;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import org.json.JSONObject;
import org.json.me.JSONException;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import com.huaqin.android.market.sdk.ClientInfo;
import com.huaqin.android.market.sdk.bean.Partner;
import com.huaqin.android.market.sdk.bean.UpdateStates;
import com.huaqin.market.download.DownloadManager;
import com.huaqin.market.download.Helpers;
import com.huaqin.market.list.SearchAppListActivity;
import com.huaqin.market.ui.TabWebActivity;
import com.huaqin.market.ui.VersionActivity;
import com.huaqin.market.utils.Constant;
import com.huaqin.market.utils.DeviceUtil;
import com.huaqin.market.utils.GlobalUtil;
import com.huaqin.market.utils.OptionsMenu;
import com.huaqin.market.utils.PackageUtil;
import com.huaqin.market.webservice.IMarketService;
import com.huaqin.market.webservice.MarketService;
import com.huaqin.market.webservice.Request;

public class MarketBrowser extends TabActivity
	implements TabHost.OnTabChangeListener {

	public static final String TAB_NEW = "new";
	public static final String TAB_RANK = "rank";
	public static final String TAB_CATEGORY = "category";
	public static final String TAB_SEARCH = "search";
	public static final String TAB_MANAGE = "manage";
	public static final String TAB_CATEAPP = "categoryapp";
	public static final String TAB_ULE = "ule";
	public static final String TAB_VER = "version";
	public static final String TAB_WEB = "web";
	public static final String NEW_TAB_GAME = "game";
	public static final String NEW_TAB_APP = "app";
	
	public static final int TAB_ID_NEW = 0;
	public static final int TAB_ID_RANK = 3;
	public static final int TAB_ID_CATEGORY = 2;
	public static final int TAB_ID_SEARCH = 3;
	public static final int TAB_ID_MANAGE = 4;
	public static final int TAB_ID_ULE = 5;
	public static final int TAB_ID_VER = 6;
	public static final int NEW_TAB_ID_GAME = 2;
	public static final int NEW_TAB_ID_APP = 1;
	
	private static final int ACTION_CHECK_SELF_UPDATE = 0;
	
	private static final int DIALOG_CONFIRM_EXIT = 100;
	private static final int DIALOG_UPDATE_AVALIABLE = 200;
	private static final int DIALOG_NOTIFI_AVALIABLE = 500;
	
	private static final int DIALOG_SDCARD_NOT_AMOUNT = 300;
	private static final int DIALOG_SDCARD_NOT_ENOUGH = 301;
	
	public static DisplayMetrics displayMetrics;
	public static float density = 0.0f;
	public static int originalOrientation = 0;
	public static int screenWidth = 0;
	public static int screenHeight = 0;
	public static int longerSide = 0;
	public static int shorterSide = 0;
	public static int processMyPid;
    
	private Context mContext;
	
	private TabHost mTabHost;
	private ImageView mUpdateView;
	private TabWidget mTabWidget;
	private ArrayList<View> mTabViews;
	private Display mDisplay;
	private static WindowManager mWindowManager;
	private UpdateStates mSelfUpdateInfo;
	private IMarketService mMarketService;
	private Handler mHandler;
	private Intent manageIntent;
	
	private final BroadcastReceiver mApplicationsReceiver;
	private final BroadcastReceiver mUpdateAppsReceiver;
	
	private boolean bDownload;
	private boolean bManage;
	private boolean bUpdated;
	private boolean bUpdate;
	private String releaseURL;
	private int fileSize;
	
	private String notice = null;
	
	private static final int ACTION_NETWORK_ERROR = 0;
	private static final int ACTION_USER_REGISTER = 2;
	private static final int ACTION_USER_PARTNER = 3;
	/*************Added-s by JimmyJin for Ule Project**************/
//	//HQ app appkey
//	String Appkey ="dd87d22032c1e646";
//	//HQ app appSecret
//	String AppSpec = "9530b66dae864f98a2df9bfc200b10cf";
//	private Action act = null;
//	private boolean mUserisHavePush;
	/*************Added-e by JimmyJin for Ule Project**************/
	
	/*************Added-s by JimmyJin**************/
	private static final int ACTION_USER_INFO = 1;
	private static final int ACTION_USER_NOTIFY = 10;
	
	public static String userId = null;
	public static String mUleId = null;
    private final static String TAG = "MarketBrowser";
	/*************Added-e by JimmyJin**************/
    private String mUserVersion = null;
    private String mUserPartnerName = null;
    private String mUserPattern = null;
    private String mUserUrls = null;
    private int mUserUrlId = 0;
    
	public static View img ;
	 private AlertDialog myDialog = null; 
    public MarketBrowser() {
		mApplicationsReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				final String action = intent.getAction();
				final String pkgName = intent.getDataString().replace("package:", "");
				
				if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
					Log.v(TAG,"JimmyJin Intent.ACTION_PACKAGE_ADDED:pkgName="+pkgName);
//					/*************Added-s by JimmyJin**************/
//            		//安装时间接口
//            		try {
//            			String userId = MarketBrowser.userId;
//            			Log.v(TAG,"JimmyJin DownloadAPK_userId="+userId);
//            			if(userId != null){
//            				Log.v(TAG,"JimmyJin Enter Into !Null");
//            			}   ReportProvider.postInstallInfo(userId,"99999999",pkgName);
//            		} catch (IOException e) {
//            			// TODO Auto-generated catch block
//            			e.printStackTrace();
//            		} catch (HttpException e) {
//            			// TODO Auto-generated catch block
//            			e.printStackTrace();
//            		}
//            		/*************Added-e by JimmyJin**************/           		
					//PackageUtil.refreshUpdateApps(mContext, mMarketService);
				} else {
					PackageUtil.removeUpdateApp(pkgName);
				} 
			}
		};
		final LinearLayout.LayoutParams params = 
			new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT );
		
		mUpdateAppsReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				final String action = intent.getAction();
				if(PackageUtil.ACTION_PACKAGE_UPDATED.equals(action)) {
					if(mUpdateView.getParent() == null) {
						mUpdateView.setLayoutParams(params);
//						((ViewGroup)(((ViewGroup)mTabHost.getChildAt(1)).getChildAt(4))).addView(mUpdateView);
					}
					
					if(manageIntent != null) {
						bUpdated = true;
						manageIntent.putExtra("bUpdated", bUpdated);
					}
					
				} else if(PackageUtil.ACTION_PACKAGE_NOT_UPDATED.equals(action)) {
//					if(mUpdateView != null)
//						((ViewGroup)(((ViewGroup)mTabHost.getChildAt(1)).getChildAt(4))).removeView(mUpdateView);
				} 
			}
		};
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// TODO Auto-generated method stub
		if (event.getAction() == KeyEvent.ACTION_UP) {
			switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_BACK:
				/*************Modified-s by JimmyJin for Ule Project**************/
				if(TabWebActivity.webview!=null){
					Log.v(TAG,"TabWebActivity.webview.canGoBack()="+TabWebActivity.webview.canGoBack());
				}
				
				String mTabTag = mTabHost.getCurrentTabTag();
				
				if(mTabTag.equals(TAB_WEB)&&TabWebActivity.webview.canGoBack()){
					TabWebActivity.webview.goBack();
					return false;
				}
//				Log.v(TAG,"mTabTag="+mTabTag);
//				if(mTabTag.equals("ule")&&sdkcontext.ULE_LAST_VIEW)
//				{
//					showDialog(DIALOG_CONFIRM_EXIT);
//					return true;
//				}else if(mTabTag.equals("ule")&&!sdkcontext.ULE_LAST_VIEW){
//					return super.dispatchKeyEvent(event);
//				}else 
				if(!mTabTag.equals("ule")&&!mTabTag.equals("search")){
					showDialog(DIALOG_CONFIRM_EXIT);
				}
//				else if(mTabTag.equals("search")&&SearchAppListActivity.sPageFlag){
//					showDialog(DIALOG_CONFIRM_EXIT);
//				}else if(mTabTag.equals("search")&&!SearchAppListActivity.sPageFlag){
//					Intent intent = new Intent();
//					intent.setAction(SearchAppBrowser.SEARCH_MAIN);
//					sendBroadcast(intent);
////					SearchAppListActivity.sPageFlag = true;
//				}
//				SearchAppListActivity.sPageFlag = true;
				return true;
				/*************Modified-e by JimmyJin for Ule Project**************/
			default:
				break;
			}
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		initTabWidgetLayout();
		super.onConfigurationChanged(newConfig);
	}
	
    @Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.market_main);
		img = findViewById(R.id.img);		
		img.setVisibility(View.INVISIBLE);
				
		/*************Added-s by JimmyJin for Ule Project**************/
//		mContext = this;
//		SharedPreferences sharedULePreferences = getSharedPreferences("ULeId", 0);
//		mUleId = sharedULePreferences.getString("ULeId", null);
//		Log.v(TAG,"JimmyJin mUleId6688="+mUleId);
//		
//		SharedPreferences userisHavaPushSharedPreferences = getSharedPreferences("Report", 0);
//		mUserisHavePush = userisHavaPushSharedPreferences.getBoolean("partnerisHavePush", false);
//		Log.v(TAG,"JimmyJin mUserisHavePush="+mUserisHavePush);
//		if(mUleId != null){
//			sdkcontext.init(this, Appkey, AppSpec, mUleId);
//			
//			if(mUserisHavePush){
//				sdkcontext.startPushMsgService(this);
//			}else{
//				sdkcontext.stopPushMsgService(this);
//			}
//		}
//			
//		try {
//			act =  createActionFromPushMsgParams(getIntent());
//		} catch (JSONException e2) {
//			// TODO Auto-generated catch block
//			e2.printStackTrace();
//		}
//		Log.v("asd", "mUleId123");
		/*************Added-e by JimmyJin for Ule Project**************/
		
		/*************Added-s by JimmyJin**************/
		SharedPreferences sharedPreferences = getSharedPreferences("Report", 0);
		String mUserId = sharedPreferences.getString("userId", null);
		Log.v(TAG,"JimmyJin mUserId="+mUserId);
		/*************Added-e by JimmyJin**************/

		SharedPreferences registerSharedPreferences = getSharedPreferences("Report", 0);
		mUserVersion = registerSharedPreferences.getString("version", null);
		
		mContext = this;
		mUpdateView = new ImageView(this);
		mUpdateView.setImageResource(R.drawable.ic_new_flag);
		
		mDisplay = getWindowManager().getDefaultDisplay();
		screenWidth = mDisplay.getWidth();
		screenHeight = mDisplay.getHeight();  
		
		if (screenWidth > screenHeight) {
			longerSide = screenWidth;
			shorterSide = screenHeight;
		} else {
			longerSide = screenHeight;
			shorterSide = screenWidth;
		}
		originalOrientation = getResources().getConfiguration().orientation;
		displayMetrics = new DisplayMetrics();
		mDisplay.getMetrics(displayMetrics);
		density = displayMetrics.density;
		
		mMarketService = MarketService.getServiceInstance(mContext);
		
		try {
			getNotice();
		} catch (Throwable t) {
			t.printStackTrace();
			}
		
		/*************Added-s by JimmyJin**************/
		if(mUserId == null)
			try {
				getUserId();
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		else 
			userId = mUserId;
			/*************Added-s by JimmyJin**************/		
		mTabHost = getTabHost();
		mTabWidget = mTabHost.getTabWidget();
//		Log.v("getLayoutParams", "mTabWidget.getLayoutParams()1.height ="+mTabWidget.getLayoutParams().height);
////		mTabWidget.getLayoutParams().height = 72;
//		Log.v("getLayoutParams", "mTabWidget.getLayoutParams().height ="+mTabWidget.getLayoutParams().height);
		try {
			getPartner();
		} catch (NameNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		initHandler();
		initTabs();

//        for (int i = 0; i < mTabWidget.getChildCount(); i++) {
//          final ImageView iv = (ImageView) mTabWidget.getChildAt(i).findViewById(android.R.id.icon);
//          final TextView tv = (TextView) mTabWidget.getChildAt(i).findViewById(android.R.id.title);
//          
//          RelativeLayout.LayoutParams paramsiv = (RelativeLayout.LayoutParams) iv.getLayoutParams(); 
//
//          paramsiv.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
//          
//          RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tv.getLayoutParams(); 
//
//          params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
//          mWindowManager = getWindowManager();
//          int width = mWindowManager.getDefaultDisplay().getWidth();
//          int height = mWindowManager.getDefaultDisplay().getHeight();
//          DisplayMetrics metric = new DisplayMetrics();
//          getWindowManager().getDefaultDisplay().getMetrics(metric);
//          int densityDpi = metric.densityDpi; 
//          int paddingBottom = 0;
//          if(height == 480){
//        	  paddingBottom=3;
//          }
//          if(height == 800){
//        	  paddingBottom=5;
//          }
//          if(height == 854){
//        	  paddingBottom=4;
//          }
//          if(height == 960){
//        	  paddingBottom=6;
//          }
//          if(height == 1280){
//        	  paddingBottom=8;
//          }
//          Log.v("getLayoutParams", "height ="+height);
//          Log.v("getLayoutParams", "paddingBottom ="+paddingBottom);
//          iv.getLayoutParams().height = 38*densityDpi/240;
//         
//          tv.getLayoutParams().height = 22*densityDpi/240;
//          tv.setTextSize(12);
//          params.setMargins(0, 35, 0, 0);
//        }
		try {
			Log.v("asd", "refreshUpdateApps");
			PackageUtil.refreshUpdateApps(mContext, mMarketService);
			
//			if (!GlobalUtil.allowInstallNonMarketApps(mContext)) {
//				GlobalUtil.showNonMarketAppDialog(mContext);
//			}
			
			if (GlobalUtil.bAutoCheckUpdate) {
				checkSelfUpdateAvaliable();
			}
			
			registerIntentReceivers();
		} catch (Throwable t) {
			t.printStackTrace();
			}
		processMyPid=Process.myPid();
		Log.v("asd","processMyPid="+processMyPid);
		if(userId!=null){
			try {
				addSlideViewRegisterRequest();
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		mContext.startService(new Intent(mContext, MarketBackgroundService.class));
		/*************Added-s by JimmyJin for Ule Project**************/
//		if(act != null){
//			//for ULE statistic
//			sdkcontext.startShopping();
//			
//			//pass action parameter to ULE
//			sdkcontext.setAction(act);
//			
//			mTabHost.setCurrentTab(TAB_ID_MANAGE);
//		}
		/*************Added-e by JimmyJin for Ule Project**************/
		
	}
   
	@Override
	protected Dialog onCreateDialog(int id) {
		if(mSelfUpdateInfo != null) {
			releaseURL = mSelfUpdateInfo.getReleaseURL();
			java.net.URLConnection conn;
			try {
				URL url = new URL(releaseURL);
				conn = url.openConnection();
				fileSize = conn.getContentLength();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		Log.v("asd", "Builder="+Process.myPid());
		switch (id) {
		case DIALOG_CONFIRM_EXIT:
			builder.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.dlg_exit_title)
				.setMessage(R.string.dlg_exit_msg)
				.setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						unregisterIntentReceivers();
						/*
						NotificationManager notifyMgr =
							(NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
						
						if (notifyMgr != null) {
							notifyMgr.cancelAll();
						}
						*/
						Log.v("asd", "DIALOG_CONFIRM_EXIT="+Process.myPid());
						Process.killProcess(Process.myPid());
					}
				})
				.setNegativeButton(R.string.btn_no, null);
			
			return builder.create();
    		
    	case DIALOG_UPDATE_AVALIABLE:
    		Log.v("asd", "DIALOG_UPDATE_AVALIABLE");
    		if (mSelfUpdateInfo != null) {
				View view = LayoutInflater.from(this).inflate(R.layout.update_content_view, null);
				TextView textView = (TextView) view.findViewById(R.id.asset_desc);
				String text = mSelfUpdateInfo.getReleaseNote();
				int index = text.indexOf("&nbsp;&nbsp;");
				if (index != -1) {
					text = text.substring(0, index);
				}
				textView.setText(text);
				
				builder.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle(R.string.dlg_update_or_not_title)
					.setView(view)
					.setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							long sdCardAvailSize = Helpers.getAvailaleSize();
							if (sdCardAvailSize == -1) {
								showDialog(DIALOG_SDCARD_NOT_AMOUNT);
							} else if(sdCardAvailSize < fileSize) {
								showDialog(DIALOG_SDCARD_NOT_ENOUGH);
							} else {
								DownloadManager.startDownloadSelf(mContext, releaseURL, fileSize);
							}
						}
					})
					.setNegativeButton(R.string.btn_no, null);
				
				return builder.create();
    		}
    		break;
    	case DIALOG_NOTIFI_AVALIABLE:
			return builder.create();
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
    	default:
    		break;
		}
		return null;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		unregisterIntentReceivers();
		super.onDestroy();
	}
	@Override  
	protected void onStart() {  

 
	Log.v("asd", "onStart");
	Log.v("asd", "MarketBrowser onResume");
	int flag = 0;
	Intent getIntent = getIntent();
	flag = getIntent.getIntExtra("flag", 0);  
	Log.v("asd", "onNewIntent,"+flag);
	if(flag == OptionsMenu.EXIT_APPLICATION){  
		finish();
		Process.killProcess(Process.myPid()); 
	}    
	super.onStart();  
	} 
	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		
		int flag = 0;
		flag = getIntent().getIntExtra("flag", 0);  
		Log.v("asd", "onNewIntent,"+flag);
		if(flag == OptionsMenu.EXIT_APPLICATION){  
			Process.killProcess(Process.myPid());  }  
		super.onNewIntent(intent);  

		setIntent(intent);
		/*************Added-s by JimmyJin for Ule Project**************/
//		String result = intent.getStringExtra("xml");
//		if (result != null){
//			sdkcontext.setUnionPayResult(result);
//		}
//
//		try {
//			act =  createActionFromPushMsgParams(intent);
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		Log.v(TAG, "JimmyJin act888="+act);
//		if(act != null){
//			sdkcontext.setAction(act);
//			mTabHost.setCurrentTabByTag(TAB_ULE);
//		}
		/*************Added-e by JimmyJin for Ule Project**************/
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
//		PackageUtil.refreshUpdateApps(mContext, mMarketService);
		Intent intent = getIntent();
		if (intent.getIntExtra("tabId", -1) == TAB_ID_MANAGE) {
			onTabChanged(TAB_MANAGE);
			mTabWidget.setCurrentTab(TAB_ID_MANAGE);
			mTabWidget.getChildAt(TAB_ID_MANAGE).performClick();
			intent.putExtra("tabId", -1);
		}
		int flag = 0;
		flag = getIntent().getIntExtra("flag", 0);  
		Log.v("asd", "onNewIntent,"+flag);
		if(flag == OptionsMenu.EXIT_APPLICATION){  
			finish();
			Process.killProcess(Process.myPid());  }  
		super.onNewIntent(intent);  
	}

	@Override
	public void onTabChanged(String tabId) {
		// TODO Auto-generated method stub
		int newTabId = -1;
		/*************Added-s by JimmyJin for Ule Project**************/	
//		if (tabId.equals(TAB_ULE)){
//			sdkcontext.startShopping();
//		}
		/*************Added-e by JimmyJin for Ule Project**************/
		
		if (tabId.equals(TAB_NEW)) {
			newTabId = TAB_ID_NEW;
//			try {
//				if(SlideViewPager.svCurrIndex == 0){
////					postPV("编辑推荐");
//				}
//				if(SlideViewPager.svCurrIndex == 1){
////					postPV("专题推荐");
//				}
//				if(SlideViewPager.svCurrIndex == 2){
////					postPV("最新上架");
//				}
//			} catch (NameNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		} else if (tabId.equals(TAB_RANK)) {
			newTabId = TAB_ID_RANK;
//			try {
//				if(SlideViewPager.svCurrIndex == 0){
////					postPV("周排名");
//				}
//				if(SlideViewPager.svCurrIndex == 1){
//					postPV("月排名");
//				}
//				if(SlideViewPager.svCurrIndex == 2){
//					postPV("总排名");
//				}
//			} catch (NameNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		} else if (tabId.equals(TAB_CATEGORY)) {
			newTabId = TAB_ID_CATEGORY;
//			try {
//				if(SlideViewPager.svCurrIndex == 0){
//					postPV("应用");
//				}
//				if(SlideViewPager.svCurrIndex == 1){
//					postPV("游戏");
//				}
//			} catch (NameNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		} else if (tabId.equals(TAB_SEARCH)) {
			newTabId = TAB_ID_SEARCH;
//			try {
//				if(SlideViewPager.svCurrIndex == 0){
//					postPV("搜索");
//				}
//				if(SlideViewPager.svCurrIndex == 1){
//					postPV("搜索详情");
//				}
//			} catch (NameNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		} else if (tabId.equals(TAB_MANAGE)) {
			newTabId = TAB_ID_MANAGE;
		} else if (tabId.equals(TAB_WEB)) {
			newTabId = TAB_ID_MANAGE;
		}else if (tabId.equals(TAB_VER)) {
			newTabId = TAB_ID_MANAGE;
		}else if (tabId.equals(TAB_ULE))  {
			newTabId = TAB_ID_MANAGE;
		}else if (tabId.equals(NEW_TAB_GAME))  {
			newTabId = NEW_TAB_ID_GAME;
		}else if (tabId.equals(NEW_TAB_APP))  {
			newTabId = NEW_TAB_ID_APP;
		}
		Log.v("onTabChanged", "newTabId ="+newTabId);
		setCurrentTab(newTabId);
	}

	private void initHandler() {
		// TODO Auto-generated method stub
		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				Log.v("MarketBrowser", "msg.what ="+msg.what);
				switch (msg.what) {	    	    	
				case ACTION_CHECK_SELF_UPDATE:
					Log.v("MarketBrowser", "ACTION_CHECK_SELF_UPDATE");
					mSelfUpdateInfo = (UpdateStates) msg.obj;
					Log.v("MarketBrowser", "mSelfUpdateInfo="+mSelfUpdateInfo);
					if (mSelfUpdateInfo != null&&mSelfUpdateInfo.isHasNewRelease()) {
						showDialog(DIALOG_UPDATE_AVALIABLE);
					}
					break;
					/*************Added-s by JimmyJin**************/
				case ACTION_USER_INFO:
					userId = (String) msg.obj;
					Log.v(TAG,"JimmyJin userId="+userId);
				      SharedPreferences.Editor editor = 
					    	  mContext.getSharedPreferences("Report", MODE_PRIVATE).edit();
					      editor.putString("userId", userId);
					      editor.commit();
					      
					      ClientInfo.setUserId(userId);//for PV
					try {
						addSlideViewRegisterRequest();
					} catch (NameNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				case ACTION_USER_NOTIFY:
					notice = (String) msg.obj;
					Log.v("ACTION_USER_NOTIFY", "notice ="+notice);
					if(notice != null){
		                myDialog = new AlertDialog.Builder(MarketBrowser.this).create();  
		                myDialog.show();  
		                myDialog.getWindow().setContentView(R.layout.notice_content_view);  

		        		TextView message = (TextView) myDialog.getWindow()  
		                        .findViewById(R.id.asset_desc);
		        		message.setText(notice);
		                myDialog.getWindow()  
		                    .findViewById(R.id.notify_exit)  
		                    .setOnClickListener(new View.OnClickListener() {  
		                    @Override  
		                    public void onClick(View v) {  
		                        myDialog.dismiss();  
		                    }  
		                }); 
					}
					break;
				case ACTION_USER_PARTNER:
					Partner partner = (Partner) msg.obj;
					SharedPreferences.Editor partnerEditor = 
				    		mContext.getSharedPreferences("Report", MODE_PRIVATE).edit();
					Log.v("ACTION_USER_PARTNER", "partnerisHavePush ="+partner.isHavePush());
					partnerEditor.putString("partnerName", partner.getName());
					partnerEditor.putString("partnerPattern", partner.getPattern());
					partnerEditor.putInt("partnerWebId", partner.getId());
					partnerEditor.putBoolean("partnerisHavePush", partner.isHavePush());
					partnerEditor.putString("partnerUrls", partner.getUrls());
					partnerEditor.commit();
					break;
					/*************Added-e by JimmyJin**************/
				case ACTION_USER_REGISTER:
					String version = (String) msg.obj;
						SharedPreferences.Editor registerEditor = 
					    		mContext.getSharedPreferences("Report", MODE_PRIVATE).edit();
						registerEditor.putString("version", version);
						registerEditor.commit();
					break;
				default:
					break;
				}
			}
		};
	}

	private void initTabs() {
		Intent intent = getIntent();
		bDownload = intent.getBooleanExtra("bDownload", false);
		bManage = intent.getBooleanExtra("bManage", false);
		bUpdate = intent.getBooleanExtra("bUpdate", false);
		if(bManage || bDownload || bUpdate) {
			Intent manageIntent = new Intent(MarketBrowser.this,SlideViewPager.class);
    		manageIntent.putExtra("intentpage", MarketBrowser.TAB_MANAGE);
    		startActivity(manageIntent);
		}
		SharedPreferences registerSharedPreferences = getSharedPreferences("Report", 0);
		mUserPartnerName = registerSharedPreferences.getString("partnerName", null);
		mUserPattern = registerSharedPreferences.getString("partnerPattern", null);
//		registerSharedPreferences.getBoolean("partnerisHavePush", false);
		mUserUrls = registerSharedPreferences.getString("partnerUrls", null);
		mUserUrlId = registerSharedPreferences.getInt("partnerWebId", 0);
		
		initNewtab();
		initNewAppTab();
		initNewGameTab();
		initRatingTab();
		Log.v("initTabs", "mUserPartnerName ="+mUserPartnerName);
		Log.v("initTabs", "mUserPattern ="+mUserPattern);
		if(mUserPartnerName != null){
//			if(mUserPattern.equals("0")){
//				initUleTab();
//			}
			if(mUserPattern.equals("1")){
				initWebTab();
			}else{
				initVerTab();
			}
		}else{
//			initUleTab();
			initVerTab();
		}
		
//		initManageTab();
		// initialize tab widget layout
		initTabWidgetLayout();
		
		// initialize tab widget bottom strip
		initTabWidgetStrip();
		
		mTabHost.setOnTabChangedListener(this);
		setCurrentTab(TAB_ID_NEW);

	}

	private void initTabWidgetStrip() {
		// TODO Auto-generated method stub
		if (DeviceUtil.getSDKVersionInt() >= 8) {
    		try {
				Field leftStrip = mTabWidget.getClass().getDeclaredField("mLeftStrip");
				Field rightStrip = mTabWidget.getClass().getDeclaredField("mRightStrip");
				
				if (!leftStrip.isAccessible()) {
					leftStrip.setAccessible(true);
				}
				if (!rightStrip.isAccessible()) {
					rightStrip.setAccessible(true);
				}
				
				Drawable strip = getResources().getDrawable(R.drawable.bg_tab_bottom_strip);
				leftStrip.set(mTabWidget, strip); 
				rightStrip.set(mTabWidget, strip);
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void initTabWidgetLayout() {
		// TODO Auto-generated method stub
		int width = mDisplay.getWidth() / 5;
		LinearLayout.LayoutParams lp = 
			new LinearLayout.LayoutParams(width, LayoutParams.FILL_PARENT);
		mTabWidget.getChildAt(0).setLayoutParams(lp);
		mTabWidget.getChildAt(1).setLayoutParams(lp);
		mTabWidget.getChildAt(2).setLayoutParams(lp);
		mTabWidget.getChildAt(3).setLayoutParams(lp);
		mTabWidget.getChildAt(4).setLayoutParams(lp);
//		mTabWidget.getChildAt(5).setLayoutParams(lp);
	}
//	private void addTab(String label, int drawableId) {
//		Intent intent = new Intent(this, MockActivity.class);
//		TabHost.TabSpec spec = tabHost.newTabSpec(label);
//		<span style="color: #ff0000;">View tabIndicator = LayoutInflater.from(this).inflate(R.layout.tab_indicator, getTabWidget(), false);</span>
//		TextView title = (TextView) tabIndicator.findViewById(R.id.title);
//		title.setText(label);
//		ImageView icon = (ImageView) tabIndicator.findViewById(R.id.icon);
//		icon.setImageResource(drawableId);
//		<span style="color: #ff0000;">spec.setIndicator(tabIndicator);</span>
//		spec.setContent(intent);
//		tabHost.addTab(spec);
//		}
	private void initNewtab() {
		// TODO Auto-generated method stub
//		Intent intent = new Intent(this, NewAppBrowser.class);
		Log.v("asd", "initNewtab");
		Intent intent = new Intent(this, SlideViewPager.class);
		intent.putExtra("intentpage", TAB_NEW);
		TabHost.TabSpec tabSpec = mTabHost.newTabSpec(TAB_NEW);
		View tabIndicator = LayoutInflater.from(this).inflate(R.layout.tab_indicator, getTabWidget(), false);
		String str = getResources().getString(R.string.tab_title_new); 
		TextView title = (TextView) tabIndicator.findViewById(R.id.title);
		title.setText(str);
		ImageView icon = (ImageView) tabIndicator.findViewById(R.id.icon);
		icon.setBackgroundResource(R.drawable.ic_tab_recommend);
		tabSpec.setIndicator(tabIndicator);
		tabSpec.setContent(intent);
		mTabHost.addTab(tabSpec);
	}

	private void initRatingTab() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, SlideViewPager.class);
		intent.putExtra("intentpage", TAB_RANK);
		TabHost.TabSpec tabSpec = mTabHost.newTabSpec(TAB_RANK);
		View tabIndicator = LayoutInflater.from(this).inflate(R.layout.tab_indicator, getTabWidget(), false);
		String str = getResources().getString(R.string.tab_title_rank); 
		TextView title = (TextView) tabIndicator.findViewById(R.id.title);
		title.setText(str);
		ImageView icon = (ImageView) tabIndicator.findViewById(R.id.icon);
		icon.setBackgroundResource(R.drawable.ic_tab_rank);
		tabSpec.setIndicator(tabIndicator);
		tabSpec.setContent(intent);
		mTabHost.addTab(tabSpec);
	}

	private void initCategoryTab() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, CategoryAppBrowser.class);
		intent.putExtra("intentpage", TAB_CATEGORY);
		TabHost.TabSpec tabSpec = mTabHost.newTabSpec(TAB_CATEGORY);
		View tabIndicator = LayoutInflater.from(this).inflate(R.layout.tab_indicator, getTabWidget(), false);
		String str = getResources().getString(R.string.tab_title_category); 
		TextView title = (TextView) tabIndicator.findViewById(R.id.title);
		title.setText(str);
		ImageView icon = (ImageView) tabIndicator.findViewById(R.id.icon);
		icon.setBackgroundResource(R.drawable.ic_tab_category);
		tabSpec.setIndicator(tabIndicator);
		tabSpec.setContent(intent);
		mTabHost.addTab(tabSpec);
	}

	private void initSearchTab() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, SearchAppBrowser.class);
		intent.putExtra("intentpage", TAB_SEARCH);
		TabHost.TabSpec tabSpec = mTabHost.newTabSpec(TAB_SEARCH);
		View tabIndicator = LayoutInflater.from(this).inflate(R.layout.tab_indicator, getTabWidget(), false);
		String str = getResources().getString(R.string.tab_title_search); 
		TextView title = (TextView) tabIndicator.findViewById(R.id.title);
		title.setText(str);
		ImageView icon = (ImageView) tabIndicator.findViewById(R.id.icon);
		icon.setBackgroundResource(R.drawable.ic_tab_search);
		tabSpec.setIndicator(tabIndicator);
		tabSpec.setContent(intent);
		mTabHost.addTab(tabSpec);
		
	}

	private void initManageTab() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, VersionActivity.class);
		intent.putExtra("bDownload", bDownload);
		intent.putExtra("bUpdate", bUpdate);
//		manageIntent = new Intent(this, SlideViewPager.class);
		manageIntent.putExtra("intentpage", TAB_MANAGE);
		TabHost.TabSpec tabSpec = mTabHost.newTabSpec(TAB_VER);
		View tabIndicator = LayoutInflater.from(this).inflate(R.layout.tab_indicator, getTabWidget(), false);
		String str = getResources().getString(R.string.menu_about); 
		TextView title = (TextView) tabIndicator.findViewById(R.id.title);
		title.setText(str);
		ImageView icon = (ImageView) tabIndicator.findViewById(R.id.icon);
		icon.setBackgroundResource(R.drawable.ic_tab_manage);
		tabSpec.setIndicator(tabIndicator);
		tabSpec.setContent(intent);
		mTabHost.addTab(tabSpec);
		
	}
	
	private void initVerTab() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, VersionActivity.class);
		TabHost.TabSpec tabSpec = mTabHost.newTabSpec(TAB_VER);
		View tabIndicator = LayoutInflater.from(this).inflate(R.layout.tab_indicator, getTabWidget(), false);
		String str = getResources().getString(R.string.menu_about); 
		TextView title = (TextView) tabIndicator.findViewById(R.id.title);
		title.setText(str);
		ImageView icon = (ImageView) tabIndicator.findViewById(R.id.icon);
		icon.setBackgroundResource(R.drawable.ic_tab_about);
		tabSpec.setIndicator(tabIndicator);
		tabSpec.setContent(intent);
		mTabHost.addTab(tabSpec);
		
	}
	private void initWebTab() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, TabWebActivity.class);
		TabHost.TabSpec tabSpec = mTabHost.newTabSpec(TAB_WEB);
		intent.putExtra("bUrl", mUserUrls);
		intent.putExtra("bUserUrlId", mUserUrlId);
		String str = getResources().getString(R.string.tab_title_ule); 
		View tabIndicator = LayoutInflater.from(this).inflate(R.layout.tab_indicator, getTabWidget(), false);
		TextView title = (TextView) tabIndicator.findViewById(R.id.title);
		if(mUserPartnerName == null){
			mUserPartnerName = str;
		}
		title.setText(mUserPartnerName);
		ImageView icon = (ImageView) tabIndicator.findViewById(R.id.icon);
		icon.setBackgroundResource(R.drawable.ic_tab_manage);
		Log.v("initWebTab", "mUserUrls ="+mUserUrls);
		Log.v("initWebTab", "mUserUrlId ="+mUserUrlId);
		Log.v("initWebTab", "mUserPartnerName ="+mUserPartnerName);
		Log.v("initWebTab", "intent ="+intent);
		tabSpec.setIndicator(tabIndicator);
		tabSpec.setContent(intent);
		mTabHost.addTab(tabSpec);
		
	}
//	private void initUleTab() {
//		// TODO Auto-generated method stub		
//		manageIntent = new Intent(this, Main.class);
//		TabHost.TabSpec tabSpec = mTabHost.newTabSpec(TAB_ULE);
//		manageIntent.putExtra(Action.ULE_ACTION, act);
//		View tabIndicator = LayoutInflater.from(this).inflate(R.layout.tab_indicator, getTabWidget(), false);
//		String str = getResources().getString(R.string.tab_title_ule); 
//		TextView title = (TextView) tabIndicator.findViewById(R.id.title);
//		if(mUserPartnerName == null){
//			mUserPartnerName = str;
//		}
//		title.setText(mUserPartnerName);
//		ImageView icon = (ImageView) tabIndicator.findViewById(R.id.icon);
//		icon.setBackgroundResource(R.drawable.ic_tab_market);
//		
//		tabSpec.setIndicator(tabIndicator);
//		tabSpec.setContent(manageIntent);
//		mTabHost.addTab(tabSpec);
////		sdkcontext.startShopping();
//		
////		TabHost.TabSpec tabSpec = null;
////		String str = null;
////		Drawable drawable = null;
//////		if(uleFlag){
////			manageIntent = new Intent(this, Main.class);
////			manageIntent.putExtra(Action.ULE_ACTION, act);
////
////			tabSpec = mTabHost.newTabSpec(TAB_ULE);
////			str = getResources().getString(R.string.tab_title_ule);
////			drawable = getResources().getDrawable(R.drawable.ic_tab_market);
//////		}else{
//////			manageIntent = new Intent(this, VersionActivity.class);
////////			manageIntent.putExtra(Action.ULE_ACTION, act);
//////
//////			tabSpec = mTabHost.newTabSpec(TAB_VER);
//////			str = getResources().getString(R.string.menu_about);
//////			drawable = getResources().getDrawable(R.drawable.ic_tab_manage);
//////		}
////		if(mUserPartnerName == null){
////			mUserPartnerName = str;
////		}
////		tabSpec.setIndicator(mUserPartnerName, drawable);
////		tabSpec.setContent(manageIntent);
////		mTabHost.addTab(tabSpec);
//	}
	
	private void initNewGameTab() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, CategoryAppBrowser.class);
		TabHost.TabSpec tabSpec = mTabHost.newTabSpec(NEW_TAB_GAME);
		intent.putExtra("intentpage", NEW_TAB_GAME);
		intent.putExtra("bUrl", mUserUrls);
		View tabIndicator = LayoutInflater.from(this).inflate(R.layout.tab_indicator, getTabWidget(), false);
		String str = getResources().getString(R.string.tab_category_game); 
		TextView title = (TextView) tabIndicator.findViewById(R.id.title);
		title.setText(str);
		ImageView icon = (ImageView) tabIndicator.findViewById(R.id.icon);
		icon.setBackgroundResource(R.drawable.ic_tab_game);
		tabSpec.setIndicator(tabIndicator);
		tabSpec.setContent(intent);
		mTabHost.addTab(tabSpec);
		
//		TabHost.TabSpec tabSpec = null;
//		Drawable drawable = null;
//		Intent intent = new Intent(this, CategoryAppBrowser.class);
//		intent.putExtra("intentpage", NEW_TAB_GAME);
//		intent.putExtra("bUrl", mUserUrls);
//		tabSpec = mTabHost.newTabSpec(NEW_TAB_GAME);
//		drawable = getResources().getDrawable(R.drawable.ic_tab_game);
//		String str = getResources().getString(R.string.tab_category_game); 
//		
//		tabSpec.setIndicator(str, drawable);
//		tabSpec.setContent(intent);
//		mTabHost.addTab(tabSpec);
	}
	private void initNewAppTab() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, CategoryAppBrowser.class);
		TabHost.TabSpec tabSpec = mTabHost.newTabSpec(NEW_TAB_APP);
		intent.putExtra("intentpage", NEW_TAB_APP);
		intent.putExtra("bUrl", mUserUrls);
		View tabIndicator = LayoutInflater.from(this).inflate(R.layout.tab_indicator, getTabWidget(), false);
		String str = getResources().getString(R.string.tab_category_app); 
		TextView title = (TextView) tabIndicator.findViewById(R.id.title);
		title.setText(str);
		ImageView icon = (ImageView) tabIndicator.findViewById(R.id.icon);
		icon.setBackgroundResource(R.drawable.ic_tab_app);
		tabSpec.setIndicator(tabIndicator);
		tabSpec.setContent(intent);
		mTabHost.addTab(tabSpec);
		
//		TabHost.TabSpec tabSpec = null;
//		Drawable drawable = null;
//		Intent intent = new Intent(this, CategoryAppBrowser.class);
//		intent.putExtra("intentpage", NEW_TAB_APP);
//		intent.putExtra("bUrl", mUserUrls);
//		tabSpec = mTabHost.newTabSpec(NEW_TAB_APP);
//		drawable = getResources().getDrawable(R.drawable.ic_tab_app);
//		String str = getResources().getString(R.string.tab_category_app); 
//		
//		tabSpec.setIndicator(str, drawable);
//		tabSpec.setContent(intent);
//		mTabHost.addTab(tabSpec);
	}
	
	private void setCurrentTab(int tabIdNew) {
		// TODO Auto-generated method stub
		View view;
		TextView textView;
		Log.v("setCurrentTab", "mTabViews ="+mTabViews);
		if (mTabViews == null) 
		{
			mTabViews = new ArrayList<View>();			
			Log.v("setCurrentTab", "mTabWidget.getChildCount() ="+mTabWidget.getChildCount());
			for (int i = 0; i < mTabWidget.getChildCount(); i++) 
			{
				/****************begin,modified by daniel_whj,2012-02-06,for bug HQ00079989&HQ00079704&HQ00079109******************/		
				/*
				 these bugs were caused by function getChildAt(getView),when after deleting or updating app,
				 the category activity "need update app" not update list view in time,so calling  getview
				 is failure .
				 we must update list view and use try catch statement to avoid these cases.
				*/
				try
				{
					mTabViews.add(mTabWidget.getChildAt(i));					
				}
				catch(NullPointerException e)
				{
					e.printStackTrace();					
					break;
				}	
				/****************end,modified by daniel_whj,2012-02-06,for bug HQ00079989&HQ00079704&HQ00079109******************/
			}				
				
		}
		for (int i = 0; i < mTabViews.size(); i++) {
			Log.v("asd", "mTabViews.size ="+mTabViews.size());
			view = mTabViews.get(i);
			textView = (TextView) view.findViewById(R.id.title);
			view.setBackgroundColor(0x1f000000);
			if (i == tabIdNew) {
				textView.setTextColor(0xff2084dd);
//				Drawable drawable = mContext.getResources().getDrawable(R.drawable.tab_hl);
//				view.setBackgroundResource(R.drawable.tab_hl);
			} else {
				textView.setTextColor(0xffffffff);
//				textView.setBackgroundDrawable(null);
			}
		}
	}

	private void registerIntentReceivers() {
		// TODO Auto-generated method stub
		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		intentFilter.addDataScheme("package");
		
		registerReceiver(mApplicationsReceiver, intentFilter);
		
		intentFilter = new IntentFilter();
		intentFilter.addAction(PackageUtil.ACTION_PACKAGE_UPDATED);
		intentFilter.addAction(PackageUtil.ACTION_PACKAGE_NOT_UPDATED);
		registerReceiver(mUpdateAppsReceiver, intentFilter);
	}

	private void unregisterIntentReceivers() {
		// TODO Auto-generated method stub
		unregisterReceiver(mApplicationsReceiver);
		unregisterReceiver(mUpdateAppsReceiver);
	}

	private void checkSelfUpdateAvaliable() {
		// TODO Auto-generated method stub
		Request request = new Request(0, Constant.TYPE_CHECK_SELF_UPDATE);
		int version = PackageUtil.getPackageVersionCode(
				this.getPackageManager(), this.getPackageName());
		request.setData(Integer.valueOf(version));
		request.addObserver(new Observer() {

			@Override
			public void update(Observable observable, Object data) {
				// TODO Auto-generated method stub
				if (data != null) {
		    		Message msg = Message.obtain(mHandler, ACTION_CHECK_SELF_UPDATE, data);
		    		mHandler.sendMessage(msg);
		    	}
			}
		});
		
		mMarketService.checkSelfUpdate(request);
	}
	
	private void getNotice() {
		// TODO Auto-generated method stub
		Request request = new Request(0, Constant.TYPE_CHECK_USER_NOTIFY);
		request.addObserver(new Observer() {

			@Override
			public void update(Observable observable, Object data) {
				// TODO Auto-generated method stub
				if (data != null) {
		    		Message msg = Message.obtain(mHandler, ACTION_USER_NOTIFY, data);
		    		mHandler.sendMessage(msg);
		    	}
			}
		});
		
		mMarketService.checkSelfUpdate(request);
	}
	
	private void getPartner() throws NameNotFoundException{
		Request request = new Request(0, Constant.TYPE_POST_PARTNER);
		String mDeviceModelId = DeviceUtil.getDeviceModel();
		
		PackageManager manager = mContext.getPackageManager();
		PackageInfo pkgInfo = manager.getPackageInfo(this.getPackageName(), 0);

		Object[] params = new Object[2];
		
		params[0] = mDeviceModelId;
		params[1] = pkgInfo.versionName;
		Log.v("getPartner", "mDeviceModelId ="+mDeviceModelId);
		Log.v("getPartner", "pkgInfo.versionName ="+pkgInfo.versionName);
		request.setData(params);

		request.addObserver(new Observer() {

			@Override
			public void update(Observable observable, Object data) {
				// TODO Auto-generated method stub
				Log.v("getPartner", "data ="+data);
				if (data != null) {
					Log.v("getPartner", "update");
		    		Message msg = Message.obtain(mHandler, ACTION_USER_PARTNER, data);
		    		mHandler.sendMessage(msg);
		    	}
			}
		});
		
		mMarketService.getUserId(request);
	}
	/*************Added-s by JimmyJin
	 * @throws NameNotFoundException **************/
	private void getUserId() throws NameNotFoundException {
		// TODO Auto-generated method stub
		Request request = new Request(0, Constant.TYPE_USER_INFO);
		String mDeviceId = DeviceUtil.getIMEI(mContext);
		String mSubsId = DeviceUtil.getIMSI(mContext);
		String mSoftId = PackageUtil.getSystemVersionName(mContext);
//		String mMessageId = PackageUtil.getMessageCenterNumber(mContext);
		String mDeviceModelId = DeviceUtil.getDeviceModel();
		
		String BuildVersionRelease = DeviceUtil.getBuildVersionRelease();
		
		Log.v(TAG,"JimmyJin BuildVersionRelease="+BuildVersionRelease);
		PackageManager manager = mContext.getPackageManager();
		PackageInfo pkgInfo = manager.getPackageInfo(this.getPackageName(), 0);

		Object[] params = new Object[5];
		
		params[0] = mDeviceId;
		params[1] = mSubsId;
		params[2] = mSoftId;
		params[3] = pkgInfo.versionName;
		params[4] = mDeviceModelId;
		
		Log.v("asd","pkgInfo.versionName="+pkgInfo.versionName);
		Log.v(TAG,"JimmyJin mDeviceId="+mDeviceId);
		Log.v(TAG,"JimmyJin mSubsId="+mSubsId);
		Log.v(TAG,"JimmyJin mSoftId="+mSoftId);
		Log.v(TAG,"JimmyJin mDeviceModelId="+mDeviceModelId);

		
		request.setData(params);

		request.addObserver(new Observer() {

			@Override
			public void update(Observable observable, Object data) {
				// TODO Auto-generated method stub
				if (data != null) {
		    		Message msg = Message.obtain(mHandler, ACTION_USER_INFO, data);
		    		mHandler.sendMessage(msg);
		    	}
			}
		});
		
		mMarketService.getUserId(request);
	}
	/*************Added-e by JimmyJin
	 * @throws NameNotFoundException **************/
	private void addSlideViewRegisterRequest() throws NameNotFoundException {
		
		PackageManager manager = mContext.getPackageManager();
		PackageInfo pkgInfo = manager.getPackageInfo(this.getPackageName(), 0);
		Log.v("addSlideViewRegisterRequest", "userId ="+userId);
		Log.v("addSlideViewRegisterRequest", "mUserVersion ="+mUserVersion);
		Log.v("addSlideViewRegisterRequest", "pkgInfo.versionName ="+pkgInfo.versionName);
		if(mUserVersion!=null&&mUserVersion.equals(pkgInfo.versionName)){
			return;
		}
		Log.v("addSlideViewRegisterRequest", "TYPE_POST_VERSION_REGISTER");
		Request request = new Request(0, Constant.TYPE_POST_VERSION_REGISTER);
		Object[] params = new Object[2];

		params[0] = userId;
		params[1] = pkgInfo.versionName;
//		params[1] = contact;
		Log.v("addSlideViewRegisterRequest","params="+params);
		Log.v("addSlideViewRegisterRequest","params0="+params[0]);
		Log.v("addSlideViewRegisterRequest","params1="+params[1]);
		request.setData(params);
		request.addObserver(new Observer() {

			@Override
			public void update(Observable observable, Object data) {
				Log.v("addSlideViewRegisterRequest","data="+data);
				// TODO Auto-generated method stub
				if (data != null) {
					Message msg = Message.obtain(mHandler, ACTION_USER_REGISTER, data);
					//	msg.obj = Integer.valueOf(appId);
					mHandler.sendMessage(msg);
				} else {
					Request request = (Request) observable;
					if (request.getStatus() == Constant.STATUS_ERROR) {
						mHandler.sendEmptyMessage(ACTION_NETWORK_ERROR);
					}
				}
			}
		});
	mMarketService.addAppComment(request);
	}
//	public Action createActionFromPushMsgParams(Intent i) throws JSONException {
//		Action myAction = null;
//		String PushMsg = X2X(i.getStringExtra("tompushmsgpageparam"), "8859_1",
//				"GBK");
//		if (i != null && PushMsg != null) {
//
//			Log.i(this.toString(), PushMsg.replace("-", "_"));
//
//			try {
//				myAction = new Action(PushMsg.replace("-", "_"));
//				
//				//add start
//				sdkcontext.msgid = "";
//				if (myAction.msgid != null && !"".equals(myAction.msgid))
//					sdkcontext.msgid = sdkcontext.MSGID + myAction.msgid;
//				//add end
//				
//			} catch (org.json.me.JSONException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				
//				//add start
//				myAction = null;
//				sdkcontext.msgid = "";
//				//add end
//			}
//		}
//		if (myAction == null) {
//			
//			//add start
//			sdkcontext.msgid = "";
//			//add end
//			
//			sdkcontext.PUSH_TYPE = "0";
//		} else {
//			String id = Action.class.getName().substring(
//					(Action.class.getName().indexOf("ui.") > 0) ? Action.class
//							.getName().indexOf("ui.") : 0);
//			if (myAction.listid != null && !myAction.listid.equals("")) {
//
//				sdkcontext.PARAMA = id + "#" + myAction.listid;
//			} else if (myAction.params != null && !myAction.params.equals("")) {
//				try {
//					JSONObject object = new JSONObject(myAction.params);
//					String titleStr = object.getString("title");
//					if (titleStr != null && !titleStr.equals("")) {
//						sdkcontext.PARAMA = id + "#" + titleStr;
//					}
//				} catch (org.json.JSONException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//			sdkcontext.SRC = "push";
//			sdkcontext.PUSH_TYPE = "1";
//		}
//		return myAction;
//	}
//
//	public static String X2X(String text, String x1, String x2) {
//		if (text == null)
//			return "";
//		String result = "";
//		try {
//			result = new String(text.getBytes(x1), x2);
//		} catch (java.io.UnsupportedEncodingException ex) {
//			result = text;
//			ex.printStackTrace(System.err);
//		}
//		return result;
//	}

}