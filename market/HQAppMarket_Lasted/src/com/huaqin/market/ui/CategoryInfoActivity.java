package com.huaqin.market.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import com.huaqin.android.market.sdk.ClientInfo;
import com.huaqin.market.R;
import com.huaqin.market.list.CategoryAppListActivity;
import com.huaqin.market.utils.Constant;
import com.huaqin.market.utils.DeviceUtil;
import com.huaqin.market.utils.PackageUtil;
import com.huaqin.market.webservice.IMarketService;
import com.huaqin.market.webservice.MarketService;
import com.huaqin.market.webservice.Request;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.TabHost;
import android.widget.TableLayout;
import android.widget.TextView;

public class CategoryInfoActivity extends TabActivity
	implements TabHost.OnTabChangeListener {

	private static final int TAB_ID_BY_DOWNLOAD = 0;
	private static final int TAB_ID_BY_RATING = 1;
	private static final int TAB_ID_BY_DATE = 2;
	private static final String TAB_BY_DOWNLOAD = "by_download";
	private static final String TAB_BY_RATING = "by_rating";
	private static final String TAB_BY_DATE = "by_date";
	
	private int nCategoryId;
	
	/*************Added-s by JimmyJin for Pudding Project**************/
    private final static String TAG = "CategoryInfoActivity";

	private static final int ACTION_USER_INFO = 3;
	private static final int ACTION_NETWORK_ERROR = 4;
	public static String userId = null;
	private int type = 0;
	private int fromPudding = 1;
	private Handler mHandler;
	private IMarketService mMarketService;
	private Context mContext;
	private static final String APP_DIR_NAME = "/hqappmarket";
	private static final String APP_DIR_PATH = Environment.getExternalStorageDirectory() + APP_DIR_NAME;
	private static final String APK_DIR_PATH = APP_DIR_PATH + "/apks";
	private static final String ICON_DIR_PATH = APP_DIR_PATH + "/icons";
	/*************Added-e by JimmyJin for Pudding Project**************/
	
	private TabHost mTabHost;
	private ArrayList<View> mTabViews;

	public CategoryInfoActivity() {
		// TODO Auto-generated constructor stub
		nCategoryId = 0;
		/*************Added-s by JimmyJin for Pudding Project**************/
		type = 0;
		/*************Added-e by JimmyJin for Pudding Project**************/
	}

	private void initViews() {
		initTabs();
	}

	private void initTabs() {
		// TODO Auto-generated method stub
		LayoutInflater layoutInflater = 
			(LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		TableLayout.LayoutParams layoutParams = 
			new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT, 1.0f);
		TabHost.TabSpec tabSpec;
		TextView textView;
		Intent intent;
		
		mTabViews = new ArrayList<View>();
		
		tabSpec = mTabHost.newTabSpec(TAB_BY_DOWNLOAD);
		textView = (TextView) layoutInflater.inflate(R.layout.tab_header_indicator, null);
		textView.setText(R.string.tab_by_download);
		textView.setTextColor(Color.BLACK);
		textView.setLayoutParams(layoutParams);
		tabSpec.setIndicator(textView);
		mTabViews.add(textView);
		intent = new Intent();
		intent.setClass(this, CategoryAppListActivity.class);
		intent.putExtra("cateId", nCategoryId);
		/*************Added-s by JimmyJin for Pudding Project**************/
		intent.putExtra("type", type);
		/*************Added-e by JimmyJin for Pudding Project**************/
		intent.putExtra("sortType", Constant.LIST_SORT_BY_DOWNLOAD);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		tabSpec.setContent(intent);
		mTabHost.addTab(tabSpec);
		
		tabSpec = mTabHost.newTabSpec(TAB_BY_RATING);
		textView = (TextView) layoutInflater.inflate(R.layout.tab_header_indicator, null);
		textView.setText(R.string.tab_by_rating);
		textView.setTextColor(Color.BLACK);
		textView.setLayoutParams(layoutParams);
		tabSpec.setIndicator(textView);
		mTabViews.add(textView);
		intent = new Intent();
		intent.setClass(this, CategoryAppListActivity.class);
		intent.putExtra("cateId", nCategoryId);
		/*************Added-s by JimmyJin for Pudding Project**************/
		intent.putExtra("type", type);
		/*************Added-e by JimmyJin for Pudding Project**************/
		intent.putExtra("sortType", Constant.LIST_SORT_BY_RATING);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		tabSpec.setContent(intent);
		mTabHost.addTab(tabSpec);
		
		tabSpec = mTabHost.newTabSpec(TAB_BY_DATE);
		textView = (TextView) layoutInflater.inflate(R.layout.tab_header_indicator, null);
		textView.setText(R.string.tab_by_date);
		textView.setTextColor(Color.BLACK);
		textView.setLayoutParams(layoutParams);
		tabSpec.setIndicator(textView);
		mTabViews.add(textView);
		intent = new Intent();
		intent.setClass(this, CategoryAppListActivity.class);
		intent.putExtra("cateId", nCategoryId);
		/*************Added-s by JimmyJin for Pudding Project**************/
		intent.putExtra("type", type);
		/*************Added-e by JimmyJin for Pudding Project**************/
		intent.putExtra("sortType", Constant.LIST_SORT_BY_DATE);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		tabSpec.setContent(intent);
		mTabHost.addTab(tabSpec);
		
		mTabHost.setOnTabChangedListener(this);
		setCurrentTab(TAB_ID_BY_DOWNLOAD);
	}

	private void setCurrentTab(int newTabId) {
		// TODO Auto-generated method stub
		TextView textView = null;
		for (int i = 0; i < mTabViews.size(); i++) {
			textView = (TextView) mTabViews.get(i);
			if (i == newTabId) {
				textView.setBackgroundResource(R.drawable.bg_tab_header_focus);
				textView.setTextColor(Color.BLACK);
			} else {
				textView.setBackgroundDrawable(null);
				textView.setTextColor(Color.WHITE);
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mContext = this;
		if (savedInstanceState == null) {
			Intent intent = getIntent();
			nCategoryId = intent.getIntExtra("cateId", 0);
			/*************Added-s by JimmyJin for Pudding Project**************/
			File dir = new File(APK_DIR_PATH);
			Log.v(TAG,"JimmyJin dir1="+dir);
			Log.v(TAG,"JimmyJin dir1.exists()="+dir.exists());
			if(!dir.exists()){
				dir.mkdirs();
			}
			
			dir = null;		
			dir = new File(ICON_DIR_PATH);
			Log.v(TAG,"JimmyJin dir2="+dir);
			Log.v(TAG,"JimmyJin dir2.exists()="+dir.exists());
			if(!dir.exists()){
				dir.mkdirs();
			}
			
			type = intent.getIntExtra("type", 0);
			Log.v(TAG,"JimmyJin type88="+type);
			/*************Added-e by JimmyJin for Pudding Project**************/
			Log.v(TAG,"JimmyJin nCategoryId666="+nCategoryId);
		} else {
			nCategoryId = savedInstanceState.getInt("cateId", 0);
			/*************Added-s by JimmyJin for Pudding Project**************/
			type = savedInstanceState.getInt("type", 0);
			Log.v(TAG,"JimmyJin type_else="+type);
			/*************Added-e by JimmyJin for Pudding Project**************/
		}
		
		/*************Added-s by JimmyJin for Pudding Project**************/
		mMarketService = MarketService.getServiceInstance(this);
		SharedPreferences sharedPreferences = getSharedPreferences("Report", 0);
		String mUserId = sharedPreferences.getString("userId", null);
		Log.v(TAG,"JimmyJin mUserId="+mUserId);
		
		if(mUserId == null && type == fromPudding){
			initHandler();
			try {
				getUserId();
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}

		else if(mUserId != null && type == fromPudding){
			userId = mUserId;
		}
		/*************Added-e by JimmyJin for Pudding Project**************/
		setContentView(R.layout.cateinfo_main);
		
		mTabHost = getTabHost();
		initViews();
	}

	@Override
	public void onTabChanged(String tabId) {
		// TODO Auto-generated method stub
		int newTabId = -1;
		if (tabId.equals(TAB_BY_DOWNLOAD)) {
			newTabId = TAB_ID_BY_DOWNLOAD;
		} else if (tabId.equals(TAB_BY_RATING)) {
			newTabId = TAB_ID_BY_RATING;
		} else {
			newTabId = TAB_ID_BY_DATE;
		}
		setCurrentTab(newTabId);
	}
	/*************Added-s by JimmyJin for Pudding Project**************/	
	private void initHandler() {
		// TODO Auto-generated method stub
		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch (msg.what) {
				case ACTION_USER_INFO:
					userId = (String) msg.obj;
					Log.v(TAG,"JimmyJin userId="+userId);
				      SharedPreferences.Editor editor = 
				    		  CategoryInfoActivity.this.getSharedPreferences("Report", MODE_PRIVATE).edit();
					      editor.putString("userId", userId);
					      editor.commit();
					      
					      ClientInfo.setUserId(userId);//for PV
					break;
					
				case ACTION_NETWORK_ERROR:
					break;
					
				default:
					break;
				}
			}
		};
	}
	
	
	private void getUserId() throws NameNotFoundException {
		// TODO Auto-generated method stub
		Request request = new Request(0, Constant.TYPE_USER_INFO);
		String mDeviceId = DeviceUtil.getIMEI(CategoryInfoActivity.this);
		String mSubsId = DeviceUtil.getIMSI(CategoryInfoActivity.this);
		String mSoftId = PackageUtil.getSystemVersionName(CategoryInfoActivity.this);
//		String mMessageId = PackageUtil.getMessageCenterNumber(CategoryInfoActivity.this);
		String mDeviceModelId = DeviceUtil.getDeviceModel();
		Object[] params = new Object[5];
		PackageManager manager = mContext.getPackageManager();
		PackageInfo pkgInfo = manager.getPackageInfo(this.getPackageName(), 0);
		params[0] = mDeviceId;
		params[1] = mSubsId;
		params[2] = mSoftId;
		params[3] = pkgInfo.versionName;
		params[4] = mDeviceModelId;
		
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
	/*************Added-s by JimmyJin for Pudding Project**************/
}