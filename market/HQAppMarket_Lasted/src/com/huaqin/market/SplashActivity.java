package com.huaqin.market;

import java.util.Observable;
import java.util.Observer;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.huaqin.market.utils.CachedThumbnails;
import com.huaqin.market.utils.Constant;
import com.huaqin.market.utils.DeviceUtil;
import com.huaqin.market.utils.GlobalUtil;
import com.huaqin.market.webservice.IMarketService;
import com.huaqin.market.webservice.MarketService;
import com.huaqin.market.webservice.Request;

public class SplashActivity extends Activity {

	public static final String ACTION_ADD_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT";
	public static final String PACKAGE_NAME = "com.huaqin.market";
	public static final String PREFS_NAME = "com.huaqin.market_preferences";
	
	private static final int ACTION_INSTALL_LOG = 0;
//	private static final int ACTION_TOP_APP = 1;
//	private static final int ACTION_APP_LIST = 2;
	private static final int ACTION_NETWORK_ERROR = 3;
	private static final int ACTION_NEW_ACTIVITY = 4;
	private static final int ACTION_VFALG = 10;
	private static final int DIALOG_NETWORK_ERROR = 100; 
	
//	private static final String LAUNCHER_AUTH = "com.android.launcher.settings";
//	private static final String LAUNCHER2_AUTH = "com.android.launcher2.settings";
	
	private TextView mLoginText;
	private IMarketService mMarketService;
	private Context mContext;
	private Handler mHandler;
	private View mLoadingView;
//	private boolean bTopAppReady = false;
//	private boolean bAppListReady = false;
	private boolean bDownload;
    private boolean bUpdate;
//    private boolean mUserisHavaPush;
    private String mULeId = null;
    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getApplicationContext();
		/*************Added-s by JimmyJin for Ule Project**************/
//		// start push service
//		SharedPreferences sharedULePreferences = getSharedPreferences("ULeId", 0);
//		mULeId = sharedULePreferences.getString("ULeId", null);
//		if(mULeId == null){
//			getULeid();
//		}
////		SharedPreferences registerSharedPreferences = getSharedPreferences("Report", 0);
////		mUserisHavaPush = registerSharedPreferences.getBoolean("partnerisHavaPush", false);
////		if(mUserisHavaPush == true){
////			sdkcontext.startPushMsgService(this);
////		}else{
////			sdkcontext.stopPushMsgService(this);
////		}
//		
		/*************Added-e by JimmyJin for Ule Project**************/
		
//		getWindow().setFormat(PixelFormat.RGBA_8888);
		mMarketService = MarketService.getServiceInstance(mContext);
		setContentView(R.layout.splash_day);
		
		initHandler();
		try {
			initViews();
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		initSettings();
		
//		processShortcut();
		if (checkNetworkState()) {
//			mHandler.sendEmptyMessageDelayed(ACTION_NEW_ACTIVITY, 60);
			mHandler.sendEmptyMessage(ACTION_NEW_ACTIVITY);
		} else {
			mHandler.sendEmptyMessageDelayed(ACTION_NETWORK_ERROR, 1000);
		}
		addSlideViewPagerNewFlagRequest();
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// TODO Auto-generated method stub
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_BACK:
				Process.killProcess(Process.myPid());
				finish();
				return true;
			default:
				break;
			}
		}
		return super.dispatchKeyEvent(event);
	}
	@Override
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		switch (id) {
		case DIALOG_NETWORK_ERROR:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.dlg_network_error_title)
				.setMessage(R.string.dlg_network_error_and_exit)
				.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						try {
//							Process.killProcess(Process.myPid());
//							return;
							Intent intent = new Intent(mContext, MarketBrowser.class);
							intent.putExtra("bManage", true);
							startActivity(intent);
							finish();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			return builder.create();
			
		default:
			break;
		}
		return null;
	}

	private void initHandler() {
		// TODO Auto-generated method stub
		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				Log.v("asd", "msg.what ="+msg.what);
				switch (msg.what) {
				case ACTION_INSTALL_LOG:
				      SharedPreferences.Editor editor = 
				    	  mContext.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
				      editor.putBoolean("install_log", false);
				      editor.commit();
					break;
					
//				case ACTION_TOP_APP:
//					if (bTopAppReady) {
//						if (!bAppListReady) {
//							initAppList();
//						}
//					}
//					break;
//					
//				case ACTION_APP_LIST:
//					if (bAppListReady) {
//						if (bTopAppReady && bAppListReady) {
//							Intent intent = new Intent(mContext, MarketBrowser.class);
//							intent.putExtra("bDownload", bDownload);
//							startActivity(intent);
//							finish();
//						}
//					}
//					break;
					
				case ACTION_NEW_ACTIVITY:
					
					Intent intent = new Intent(mContext, MarketBrowser.class);
					intent.putExtra("bDownload", bDownload);
					intent.putExtra("bUpdate", bUpdate);
					Log.v("asd", "SplashActivity to MarketBrowser");
					startActivity(intent);
					finish();
					break;
				case ACTION_VFALG:
					SlideViewPager.newFlag = (String)msg.obj;
					break;	
				case ACTION_NETWORK_ERROR:
//					mLoadingView.setVisibility(View.GONE);
//					showDialog(DIALOG_NETWORK_ERROR);
					Log.v("asd", "SplashActivity to ACTION_NETWORK_ERROR");
					SlideViewPager.newFlag = "0";
					Intent intent2 = new Intent(mContext, MarketBrowser.class);
					intent2.putExtra("bManage", true);
					startActivity(intent2);
					finish();
					Toast.makeText(mContext, mContext.getString(R.string.error_network_low_speed), Toast.LENGTH_LONG).show();
					break;
					
				default:
					break;
				}
			}
		};
	}

	private void initViews() throws NameNotFoundException {
		Intent intent = getIntent();
		PackageManager manager = mContext.getPackageManager();
		PackageInfo pkgInfo = manager.getPackageInfo(this.getPackageName(), 0);
		bDownload = intent.getBooleanExtra("bDownload", false);
		bUpdate = intent.getBooleanExtra("bUpdate", false);
		mLoginText = (TextView) findViewById(R.id.splash_login);
//		mLoginTextVer = (TextView) findViewById(R.id.splash_login_version);
//		String version = "Version"+pkgInfo.versionName;
//		mLoginTextVer.setText(version);
		mLoadingView = (View) mLoginText.getParent();
	}

	private void initSettings() {
		// TODO Auto-generated method stub
		SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, 0);
		
		CachedThumbnails.bAllowBufferIcon =
			sharedPreferences.getBoolean("settings_buffer_icon", true);
		
		GlobalUtil.bAutoCheckUpdate =
			sharedPreferences.getBoolean("settings_check_update", true);
		
//		GlobalUtil.bAutoCreateShortcut =
//			sharedPreferences.getBoolean("settings_create_shortcut", true);
	}

//	private void processShortcut() {
//		// TODO Auto-generated method stub
//		if (GlobalUtil.bAutoCreateShortcut
//				&& !isShortcutExists(mContext, PACKAGE_NAME)) {
//			createShortcut(getIntent());
//		}
//	}

//	private boolean isShortcutExists(Context context, String packageName) {
//		// TODO Auto-generated method stub
//		boolean bRet = false;
//		String auth = LAUNCHER_AUTH;
//		
//		if (DeviceUtil.getSDKVersionInt() > 7) {
//			auth = LAUNCHER2_AUTH;
//		}
//		Uri uri = Uri.parse("content://" + auth + "/favorites");
//		String where = "intent like '%" + packageName + "%'";
//		
//		ContentResolver cr = context.getContentResolver();
//		Cursor c = cr.query(uri, new String[] {"intent"}, where, null, null);
//		try {
//			bRet = c.moveToFirst();
//			c.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return bRet;
//	}

//	private void createShortcut(Intent startIntent) {
//		// TODO Auto-generated method stub
//		Intent intent = new Intent(ACTION_ADD_SHORTCUT);
//		
//		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
//				Intent.ShortcutIconResource.fromContext(this, R.drawable.icon));
//		intent.putExtra(Intent.EXTRA_SHORTCUT_NAME,
//				getString(R.string.app_name));
//		intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, startIntent);
//		sendBroadcast(intent);
//	}
	private void addSlideViewPagerNewFlagRequest() {
		// TODO Auto-generated method stub
		Request request = new Request(0, Constant.LIST_VIEWPAGER_FLAG);
		request.addObserver(new Observer() {
			@Override
			public void update(Observable observable, Object data) {
				// TODO Auto-generated method stub
				Log.v("asd", "data ="+data);
				if (data != null) {
					Log.v("asd", "mHandler ="+mHandler);
					Message msg = Message.obtain(mHandler, ACTION_VFALG, data);
					Log.v("asd", "msg ="+msg);
					mHandler.sendMessage(msg);
				}
				 else {
					Request request = (Request) observable;
					if (request.getStatus() == Constant.STATUS_ERROR) {
						mHandler.sendEmptyMessageDelayed(ACTION_NETWORK_ERROR, 100);
					}
				}
			}
		});
		
		mMarketService.getSearchHotwords(request);
	}

	private boolean checkNetworkState() {
		// TODO Auto-generated method stub
		ConnectivityManager connectMgr =
			(ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		if (connectMgr == null) {
			return false;
		}
		
		NetworkInfo nwInfo = connectMgr.getActiveNetworkInfo();
		
		if (nwInfo == null || !nwInfo.isAvailable()) {
			return false;
		}
		return true;
	}
	private String getULeid() {
		String mUUID = UUID.randomUUID().toString();
		String mChannelID = DeviceUtil.getChannelID(mContext);
		String clientUserId = mUUID+"_"+mChannelID;
		
		SharedPreferences.Editor registerULeEditor = 
	    		mContext.getSharedPreferences("ULeId", MODE_PRIVATE).edit();
		registerULeEditor.putString("ULeId", clientUserId);
		registerULeEditor.commit();
		
		return clientUserId;
	}
}