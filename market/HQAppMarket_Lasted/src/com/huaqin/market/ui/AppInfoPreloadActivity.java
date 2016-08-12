package com.huaqin.market.ui;

import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.huaqin.android.market.sdk.ClientInfo;
import com.huaqin.android.market.sdk.bean.Application;
import com.huaqin.market.R;
import com.huaqin.market.model.Application2;
import com.huaqin.market.utils.Constant;
import com.huaqin.market.utils.DeviceUtil;
import com.huaqin.market.utils.PackageUtil;
import com.huaqin.market.webservice.IMarketService;
import com.huaqin.market.webservice.MarketService;
import com.huaqin.market.webservice.Request;

public class AppInfoPreloadActivity extends Activity {

	private static final int ACTION_APP_DETAIL = 1;
	private static final int ACTION_NETWORK_ERROR = 2;
	public static final String TAB_APPINFO = "appinfo";
	private ImageView mLoadingAnimation;
	private int nAppId;
	
	private IMarketService mMarketService;
	private Handler mHandler;
	private Context mContext;
	
	private AnimationDrawable loadingAnimation;
	
	/*************Added-s by JimmyJin for Pudding Project**************/

    private final static String TAG = "AppInfoPreloadActivity";
	private int type = 0;
	private static final int ACTION_USER_INFO = 3;
	public static String userId = null;
//	private int fromSplash = 0;
	private int fromPudding = 1;
	/*************Added-e by JimmyJin for Pudding Project**************/

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		mMarketService = MarketService.getServiceInstance(this);
		nAppId = getIntent().getIntExtra("appId", 0);
		/*************Added-s by JimmyJin for Pudding Project**************/
		SharedPreferences sharedPreferences = getSharedPreferences("Report", 0);
		String mUserId = sharedPreferences.getString("userId", null);
		Log.v(TAG,"JimmyJin mUserId="+mUserId);
		mContext = this;
		type = getIntent().getIntExtra("type", 0);
		Log.v(TAG,"JimmyJin type="+type);
		
		if(mUserId == null && type == fromPudding)
			try {
				getUserId();
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		else if(mUserId != null && type == fromPudding)
			userId = mUserId;
		/*************Added-e by JimmyJin for Pudding Project**************/
		
		Log.v(TAG,"JimmyJin nAppId="+nAppId);
		
		setContentView(R.layout.fullscreen_loading_indicator);
		
		initHandler();
		initViews();
	}

	private void initHandler() {
		// TODO Auto-generated method stub
		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch (msg.what) {
				case ACTION_APP_DETAIL:
					Log.v(TAG,"ACTION_APP_DETAIL");
					try {
						Application2 appInfo =
							new Application2((Application) msg.obj);
						Intent intent =
							new Intent(AppInfoPreloadActivity.this, AppInfoActivity.class);
						Log.v(TAG,"appInfo="+appInfo);
						intent.putExtra("appInfo", appInfo);
						/*************Added-s by JimmyJin for Pudding Project**************/
						intent.putExtra("type", type);
						/*************Added-e by JimmyJin for Pudding Project**************/
//						intent.putExtra("intentpage", TAB_APPINFO);
						intent.putExtra("appId", appInfo.getAppId());
						AppInfoPreloadActivity.this.startActivity(intent);
						Log.v(TAG,"startActivity AppInfoActivity");
						AppInfoPreloadActivity.this.finish();
					} catch (Exception ex) {
						showToastAndExist(AppInfoPreloadActivity.this);
					}
					break;
					/*************Added-s by JimmyJin for Pudding Project**************/
				case ACTION_USER_INFO:
					userId = (String) msg.obj;
					Log.v(TAG,"JimmyJin userId="+userId);
				      SharedPreferences.Editor editor = 
				    		  AppInfoPreloadActivity.this.getSharedPreferences("Report", MODE_PRIVATE).edit();
					      editor.putString("userId", userId);
					      editor.commit();
					      
					      ClientInfo.setUserId(userId);//for PV
					break;
					/*************Added-e by JimmyJin for Pudding Project**************/
					
				case ACTION_NETWORK_ERROR:
					showToastAndExist(AppInfoPreloadActivity.this);
					break;
					
				default:
					break;
				}
			}
		};
	}

	private void initViews() {
		// TODO Auto-generated method stub
		mLoadingAnimation = 
			(ImageView) findViewById(R.id.fullscreen_loading);
//		LoadingAnimation animation = new LoadingAnimation(
//				this,
//				LoadingAnimation.SIZE_SMALL,
//				0, 0, LoadingAnimation.DEFAULT_DURATION);
		mLoadingAnimation.setBackgroundResource(R.anim.loading_anim);
		loadingAnimation = (AnimationDrawable) mLoadingAnimation.getBackground();
		mLoadingAnimation.post(new Runnable(){
			@Override     
			public void run() {
				loadingAnimation.start();     
			}                     
		});  
		addAppDetailRequest();
	}

	private void addAppDetailRequest() {
		// TODO Auto-generated method stub
		Request request = new Request(0, Constant.TYPE_APP_DETAIL);
		
		request.setData(Integer.valueOf(nAppId));
		request.addObserver(new Observer() {

			@Override
			public void update(Observable observable, Object data) {
				// TODO Auto-generated method stub
				if (data != null) {
					Message msg = Message.obtain(mHandler, ACTION_APP_DETAIL, data);
					mHandler.sendMessage(msg);
				} else {
					mHandler.sendEmptyMessage(ACTION_NETWORK_ERROR);
				}
			}
		});
		
		mMarketService.getAppDetail(request);
	}

	private void showToastAndExist(Context context) {
		// TODO Auto-generated method stub
		Toast.makeText(context, R.string.error_no_app_detail, Toast.LENGTH_LONG)
			.show();
		finish();
	}
	/*************Added-s by JimmyJin for Pudding Project
	 * @throws NameNotFoundException **************/	
	private void getUserId() throws NameNotFoundException {
		// TODO Auto-generated method stub
		Request request = new Request(0, Constant.TYPE_USER_INFO);
		String mDeviceId = DeviceUtil.getIMEI(AppInfoPreloadActivity.this);
		String mSubsId = DeviceUtil.getIMSI(AppInfoPreloadActivity.this);
		String mSoftId = PackageUtil.getSystemVersionName(AppInfoPreloadActivity.this);
//		String mMessageId = PackageUtil.getMessageCenterNumber(AppInfoPreloadActivity.this);
		String mDeviceModelId = DeviceUtil.getDeviceModel();
		Object[] params = new Object[5];
		PackageManager manager = mContext.getPackageManager();
		PackageInfo pkgInfo = manager.getPackageInfo(this.getPackageName(), 0);
		params[0] = mDeviceId;
		params[1] = mSubsId;
		params[2] = mSoftId;
		params[3] = pkgInfo.versionName;
		params[4] = mDeviceModelId;

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
	/*************Added-s by JimmyJin for Pudding Project**************/
}