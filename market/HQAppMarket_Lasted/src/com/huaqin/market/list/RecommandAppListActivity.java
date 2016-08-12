package com.huaqin.market.list;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.BaseColumns;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.huaqin.android.market.sdk.bean.AdInfoList;
import com.huaqin.android.market.sdk.bean.Application;
import com.huaqin.market.SlideViewPager;
import com.huaqin.market.R;
import com.huaqin.market.download.Downloads;
import com.huaqin.market.list.AppListAdapter.ViewHolder;
import com.huaqin.market.model.Application2;
import com.huaqin.market.model.Image2;
import com.huaqin.market.model.TopAppDetial;
import com.huaqin.market.ui.AppInfoActivity;
import com.huaqin.market.ui.AppInfoPreloadActivity;
import com.huaqin.market.ui.LoadingAnimation;
import com.huaqin.market.ui.MarketGallery;
import com.huaqin.market.ui.TabWebActivity;
import com.huaqin.market.utils.CachedThumbnails;
import com.huaqin.market.utils.Constant;
import com.huaqin.market.utils.DeviceUtil;
import com.huaqin.market.utils.OptionsMenu;
import com.huaqin.market.utils.PackageUtil;
import com.huaqin.market.webservice.AppCacheService;
import com.huaqin.market.webservice.IMarketService;
import com.huaqin.market.webservice.MarketService;
import com.huaqin.market.webservice.Request;

public class RecommandAppListActivity extends Activity
	implements AdapterView.OnItemClickListener {
	
	private static final int ACTION_LOGIN = 0;
	private static final int ACTION_APP_LIST = 1;
	private static final int ACTION_APP_ICON = 2;
	private static final int ACTION_TOP_APP_DETIAL = 3;
	private static final int ACTION_TOP_APP_ICON = 4;
	private static final int ACTION_NETWORK_ERROR = 5;
	private static final int ACTION_TOP_LAYOUT_DETIAL = 6;
	private static final int ACTION_APP_ICON_SHOW = 15;
	private static final int HEADVIEW_MIN_HEIGHT = 1;
	private static final int DIALOG_NETWORK_ERROR = 100;
	public static final int IMAGE_CHANGE = 10;
	public static final int DATE_LOAD = 11;
	public static final int ICON_SHOW = 12;
	
	private static final int PROGRESSBAR_UPDATEING = 13;
	private static final int ACTION_USER_PV = 20;
	
	private static Context mContext;
	public static WindowManager mWindowManager;
	private static View mHeaderView;
	private static View mFooterView;
	private static View mFooterViewSpace;
	
	private Display mDisplay;
	public static int screenWidth = 0;
	public static int screenHeight = 0;
	
	private Hashtable<Integer, Boolean> mIconStatusMap;
	public static Hashtable<Integer, Boolean> mIconAnimStatusMap;
	private Request mCurrentRequest;
	private Request mIconRequest;
	private Request mTopIconRequest;
	private Request mTopDetailRequest;
	private Request mTopLayoutRequest;
	private IMarketService mMarketService;
	private AppCacheService mAppCacheService;
	private Handler mHandler;
	private MarketGallery imageChange;
	Timer autoGallery = new Timer();
	private ArrayList<Drawable> mPreviews;
	private int positon = 0;
	
	Timer dateLoad = new Timer();
	private View mLoadingIndicator;
	private ImageView mLoadingAnimation;
	private ListView mListView;
	
	private AnimationDrawable loadingAnimation;
	public static AppListAdapter mAppListAdapter;
	public static DisplayMetrics dm;
	private int nStartIndex;
		
	private boolean bNewAppList = true;
	private boolean bContainsHeader = false;
	private boolean bInflatingAppList = false;
	private boolean bReachEnd = false;
	private boolean bBusy = false;
	private AbsListView.OnScrollListener mScrollListener;
	private boolean mTouch;
	private boolean bLoadFlag = true;
	private ArrayList<TopAppDetial> mTopApp;
	private int mScreenOrietation;
	private final BroadcastReceiver mApplicationsReceiver;

	private DownloadingContentObserver mObserver;
	private DownloadingThread mDownloadThread;
	private Cursor mCursor;
	private String[] mCols = new String [] {
            Downloads._ID,
            Downloads.COLUMN_TITLE,
            Downloads.COLUMN_CURRENT_BYTES,
            Downloads.COLUMN_TOTAL_BYTES,
            Downloads._DATA,
            Downloads.COLUMN_APP_ID,
            Downloads.COLUMN_CONTROL
    };
	private Context context;
	private final int idColumn = 0;
	private final int titleColumn = 1;
	private final int currentBytesColumn = 2;
	private final int totalBytesColumn = 3;
	private final int dataColumn = 4;
	private final int appIdColumn = 5;
	private final int controlColumn = 6;
	private ProgressBar mProgressBar;
	private TextView mStatus;
	private Animation icon_push_in; 
	// Constructor
	public RecommandAppListActivity() {
		
		nStartIndex = 0;
		mIconStatusMap = new Hashtable<Integer, Boolean>();
		mIconAnimStatusMap = new Hashtable<Integer, Boolean>();
		mPreviews = new ArrayList<Drawable>();
		mTouch = true;
		context = this;
		mApplicationsReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				if (mAppListAdapter != null) {
					mAppListAdapter.notifyDataSetChanged();
				}
			}
		};
		initListener();
	}
	
	private void initListView() {
		mDisplay = getWindowManager().getDefaultDisplay();
		screenWidth = mDisplay.getWidth();
		screenHeight = mDisplay.getHeight();
		mLoadingIndicator = findViewById(R.id.fullscreen_loading_indicator);
		mLoadingAnimation =
			(ImageView) mLoadingIndicator.findViewById(R.id.fullscreen_loading);
//		LoadingAnimation animation = new LoadingAnimation(
//				this,
//				LoadingAnimation.SIZE_MEDIUM,
//				0, 0, LoadingAnimation.DEFAULT_DURATION);
//		mLoadingAnimation.setIndeterminateDrawable(animation);
		mLoadingAnimation.setBackgroundResource(R.anim.loading_anim);
		loadingAnimation = (AnimationDrawable) mLoadingAnimation.getBackground();
		mLoadingAnimation.post(new Runnable(){
			@Override     
			public void run() {
				loadingAnimation.start();    
				dateLoad.schedule(new TimerTask() {
		            @Override
		            public void run() {
			                Message msg = new Message();
			                msg.what = DATE_LOAD;//消息标识
			                loadDateHandler.sendMessage(msg);
		            	}
		            }, 100, 100);
			}                     
		});  
		
		mListView = (ListView) findViewById(android.R.id.list);
		mListView.setScrollbarFadingEnabled(true);
		
		mFooterView = LayoutInflater.from(mContext).inflate(R.layout.app_list_footer, null);
		mFooterViewSpace = LayoutInflater.from(mContext).inflate(R.layout.app_list_foot_space, null);
		ProgressBar loadingSmall =
			(ProgressBar) mFooterView.findViewById(R.id.small_loading);
		LoadingAnimation aniSmall = new LoadingAnimation(
				this,
				LoadingAnimation.SIZE_SMALL,
				0, 0, LoadingAnimation.DEFAULT_DURATION);
		loadingSmall.setIndeterminateDrawable(aniSmall);
		Button btnRefresh = (Button) mFooterView.findViewById(R.id.btn_retry);
		btnRefresh.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mCurrentRequest != null) {
					mMarketService.getAppList(mCurrentRequest);
				}
			}
		});
		mListView.addFooterView(mFooterView);
		
		View emptyView = findViewById(R.id.low_speed);
		TextView tvRefresh = (TextView) emptyView.findViewById(R.id.lowspeed_refresh);
		tvRefresh.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mCurrentRequest != null && mListView != null) {
					mLoadingIndicator.setVisibility(View.VISIBLE);
					mListView.setVisibility(View.GONE);
					bContainsHeader = false;
					bReachEnd = false;
					nStartIndex = 0;

					if (mListView.getFooterViewsCount() == 0 && mFooterView != null) {
						mListView.addFooterView(mFooterView);
					}
//					mMarketService.getAppList(mCurrentRequest);
					initTopApp();
					inflateAppList();
				}
			}
		});
		mListView.setEmptyView(emptyView);
		mListView.setOnItemClickListener(this); 
		mListView.setOnScrollListener(mScrollListener);
		
		//Added-s by JimmyJin
		if(mFooterViewSpace != null){
			mListView.addFooterView(mFooterViewSpace);
		}else
			mListView.removeFooterView(mFooterViewSpace);
		//Added-e by JimmyJin
		
//	    final ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);    
//	    ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();   
//	    activityManager.getMemoryInfo(info);    
//	    Log.v("asd","系统剩余内存:"+(info.availMem >> 10)+"k");   
//	    Log.v("asd","系统是否处于低内存运行："+info.lowMemory);
//	    Log.v("asd","当系统剩余内存低于"+info.threshold+"时就看成低内存运行");
	}
	
	private void initHeaderView(){
		dm =getResources().getDisplayMetrics();
		int width = mWindowManager.getDefaultDisplay().getWidth();
		int height = HEADVIEW_MIN_HEIGHT;
		AbsListView.LayoutParams lp = 
			new AbsListView.LayoutParams(width, height);
        mHeaderView.setLayoutParams(lp);
        mListView.addHeaderView(mHeaderView, null, false);
	}
	
	private void initTopApp() {

		// get top application icon
		mTopIconRequest = new Request(0, Constant.TYPE_TOP_APP_ICON);
		mTopIconRequest.addObserver(new Observer() {

			@Override
			public void update(Observable observable, Object data) {
				// TODO Auto-generated method stub
				Request request = (Request) observable;
				
				if (request.getStatus() == Constant.STATUS_SUCCESS) {
					Message msg = Message.obtain(mHandler, ACTION_TOP_APP_ICON, data);
					mHandler.sendMessage(msg);
				}
			}
		});
		mAppCacheService.getTopAppIcon(mTopIconRequest);
	}
	
	private void resetTopApp() {
		// TODO Auto-generated method stub		
		if (mHeaderView != null) {
			Drawable drawable = mHeaderView.getBackground();
			int width = mWindowManager.getDefaultDisplay().getWidth();
			int height = 0;
			if(drawable != null && drawable.getMinimumWidth() > 0) {
				height = drawable.getMinimumHeight() * width / drawable.getMinimumWidth();
				Log.v("asd", "height = "+height);
			}
			AbsListView.LayoutParams lp1 = 
				new AbsListView.LayoutParams(width, height);
//			LinearLayout.LayoutParams lp2;
			
			mHeaderView.setLayoutParams(lp1);
			
//			lp2 = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT, 1.0f);

//			mTopButton1.setLayoutParams(lp2);
//			mTopButton2.setLayoutParams(lp2);
//			mTopButton3.setLayoutParams(lp2);
//			mTopButton4.setLayoutParams(lp2);
		}
	}
	private class DownloadingContentObserver extends ContentObserver {

		public DownloadingContentObserver() {
			super(mHandler);
		}
		
		@Override
		public void onChange(final boolean selfChange) {
			mDownloadThread = new DownloadingThread();
            mDownloadThread.start();
        }
	}
	
	private class DownloadingThread extends Thread {
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
		            String title = cursor.getString(titleColumn);
		            if (title == null || title.length() == 0) {
		                title = mContext.getResources().getString(
		                        R.string.download_unknown_title);
		            }
		            
		            if(progress < max && max > 0 && (progress * 100) > max) {
		            	Object[] data = new Object[2];
		            	data[0] = new Integer(appId);
		            	data[1] = new Integer(progress * 100 / max);
		            	Message msg = Message.obtain(mHandler, PROGRESSBAR_UPDATEING, data);
						mHandler.sendMessage(msg);
		            }
		        }
	        }finally {
	        	cursor.close();
	        }
		}
	}
	private void inflateAppList() {
		// TODO Auto-generated method stub
		if (bReachEnd || bInflatingAppList) {
			return;
		}
		bInflatingAppList = true;
		Request request = new Request(0, Constant.TYPE_APP_RECOMMAND_LIST);
		Object[] params = new Object[2];
		
		params[0] = Integer.valueOf(0);
		params[1] = Integer.valueOf(nStartIndex);
		request.setData(params);
		
		request.addObserver(new Observer() {

			@Override
			public void update(Observable observable, Object data) {
				// TODO Auto-generated method stub
				if (data != null) {
					Message msg = Message.obtain(mHandler, ACTION_APP_LIST, data);
					mHandler.sendMessage(msg);
				} else {
					Request request = (Request) observable;
					if (request.getStatus() == Constant.STATUS_ERROR) {
						mHandler.sendEmptyMessage(ACTION_NETWORK_ERROR);
					}
				}
			}
		});
		
		mCurrentRequest = request;
		if (bNewAppList || nStartIndex == 0) {
			mAppCacheService.getAppList(request);
		} else {
			mMarketService.getAppList(request);
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
	//UI优化
	private void initListener() {
		// TODO Auto-generated method stub
		mScrollListener = new AbsListView.OnScrollListener() {
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				return;
			}
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				switch (scrollState) {
				case SCROLL_STATE_IDLE:
					bBusy = false;
					int start = view.getFirstVisiblePosition();
					int counts = view.getChildCount();
					int position = 0;
					for (int i = 0; i < counts; i++) {
						View localView = view.getChildAt(i);
						if (bContainsHeader) {
							position = start + i - 1;
						} else {
							position = start + i;
						}
						ViewHolder viewHolder = (ViewHolder) localView.getTag();
					    if (viewHolder != null) {
//					    	mAppListAdapter.initBtnStatus(viewHolder,(Application2)viewHolder.mButton.getTag());
					          int id = (int) mAppListAdapter.getItemId(position);
					          Drawable drawable = getThumbnail(position, id);
//					          drawable.setCallback(null);
					          viewHolder.mThumbnail.setImageDrawable(drawable);
					    }
					}
					
					// as list scrolled to end, send request to get above data
					if ((start + counts) >= (mAppListAdapter.getCount() - 2)) {
						inflateAppList();
					}
					break;
				case SCROLL_STATE_TOUCH_SCROLL:
				case SCROLL_STATE_FLING:
					if (!bBusy) {
						clearPendingThumbRequest();
						bBusy = true;
					}
				default:
					break;
				}
			}
		};
		
		new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int nAppIndex = ((Integer)v.getTag()).intValue();
				Log.i("RecommandAppListActivity", "onclick(0");
				if (nAppIndex >= 0) {
					if(mTopApp.get(nAppIndex).getAdType() == 0){
						Intent intent = new Intent(mContext, AppInfoPreloadActivity.class);
						intent.putExtra("appId", mTopApp.get(nAppIndex).getAppId());
						/*************Added-s by JimmyJin for Pudding Project**************/
						intent.putExtra("type",0);
						/*************Added-s by JimmyJin for Pudding Project**************/
						intent.putExtra("download", mAppListAdapter.getItem(nAppIndex).getTotalDownloads());
						startActivity(intent);
					}
					if(mTopApp.get(nAppIndex).getAdType() == 2){
						Intent intent = new Intent(mContext, TabWebActivity.class);
						intent.putExtra("bUrl", mTopApp.get(nAppIndex).getwebUrl());
						intent.putExtra("type",2);
						intent.putExtra("appIndex",nAppIndex);
						startActivity(intent);
					}
				}
			}
		};
		
	}
//	private ArrayList<Application> refleshAppList(ArrayList<Application> appList){
//		if(appList != null&&appList.size() > 0){
//			ArrayList<Application> tmpAppList = new ArrayList<Application>();
//			int allInstallSize = appList.size()<10?appList.size():10;
//			for(int i=0;i<appList.size();i++){
//				int appStatus = PackageUtil.getApplicationStatus(mContext.getPackageManager(), appList.get(i).getAppPackage());
//				if(appStatus == PackageUtil.PACKAGE_INSTALLED&&appStatus != PackageUtil.PACKAGE_UPDATE_AVAILABLE){
//					tmpAppList.add(appList.get(i));
//					appList.remove(i);
//					i-=1;
//				}
//			}
//			
//			int unInstallSize = appList.size();
//			Log.v("refleshAppList", "allInstallSize ="+allInstallSize);
//			Log.v("refleshAppList", "unInstallSize ="+unInstallSize);
//			Log.v("refleshAppList", "appList.size() ="+appList.size());
//			Log.v("refleshAppList", "tmpAppList.size() ="+tmpAppList.size());
//			if(appList.size()<allInstallSize&&tmpAppList.size()>0){
//				for(int i=0;i<(allInstallSize-unInstallSize);i++){
//					appList.add(tmpAppList.get(i));
//				}
//			}else if(unInstallSize > 10){
//				for(int i=0;i<(unInstallSize-10);i++){
//					appList.remove(unInstallSize-i-1);
//				}
//			}
//		}
//		return appList;
//	}
	private ArrayList<Application> refleshAppList(ArrayList<Application> appList){
		Log.v("refleshAppList", "appList.size ="+appList.size());
		if(appList != null&&appList.size() > 0){
			for(int i=0;i<appList.size();i++){
				int appStatus = PackageUtil.getApplicationStatus(mContext.getPackageManager(), appList.get(i).getAppPackage());
				if(appStatus == PackageUtil.PACKAGE_INSTALLED&&appStatus != PackageUtil.PACKAGE_UPDATE_AVAILABLE){
					appList.remove(i);
					i-=1;
				}
			}
		}
		return appList;
	}
	private void initAd(){
		mPreviews.clear();
		
//		for(int i=0;i<5;i++){
			mPreviews.add(getResources().getDrawable(R.drawable.ad));							
//		}
		imageChange = (MarketGallery)mHeaderView.findViewById(R.id.gallery_image); 
	    ImageGalleryAdapter imageAdapter = new ImageGalleryAdapter(mContext);
	    imageChange.setAdapter(imageAdapter);

		if(mPreviews.size()>=2)
		{						
			imageChange.setSelection(mPreviews.size()*1000000);
		}
//		LinearLayout pointLinear3 = (LinearLayout) findViewById(R.id.gallery_point_linear);
//	    for (int i = 0; i < mPreviews.size(); i++) {
//	        ImageView pointView3 = new ImageView(mHeaderView.getContext());
//	        if(i==0){
//	        	pointView3.setBackgroundResource(R.drawable.feature_point_cur);
//	        }else
//	        	pointView3.setBackgroundResource(R.drawable.feature_point);
//		    pointLinear3.addView(pointView3);
//		}
	        
		int width = mWindowManager.getDefaultDisplay().getWidth();
		int height = getResources().getDrawable(R.drawable.ad).getMinimumHeight() * width / getResources().getDrawable(R.drawable.ad).getMinimumWidth();
		AbsListView.LayoutParams lp = 
			new AbsListView.LayoutParams(width, height);
	    mHeaderView.setLayoutParams(lp);				
				        
	    Message msg1 = Message.obtain(mHandler, ACTION_TOP_LAYOUT_DETIAL, null); 
	    mHandler.sendMessage(msg1);
	}
	private void initHandlers() {
		// TODO Auto-generated method stub
		mHandler = new Handler() {

			@SuppressWarnings({ "unchecked", "deprecation" })
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				Log.v("asd", "handleMessage="+msg.what);
				switch (msg.what) {
				case ACTION_LOGIN:
					inflateAppList();
					break;
				case ACTION_APP_LIST:
					ArrayList<Application> tmpAppList = 
							(ArrayList<Application>) msg.obj;
					ArrayList<Application> appList = new ArrayList<Application>();
					Log.v("asd", "handleMessage="+msg.what);
					int appListSize = 0;
					int tmpAppListSize = 0;
					int appListIndex = 0;
					tmpAppListSize = tmpAppList.size();
					appListSize = tmpAppList.size();
					nStartIndex += appListSize;
					if (tmpAppList != null&&appListSize>0) {
						appList = refleshAppList(tmpAppList);
					}
					appListSize = appList.size();
					if (mAppListAdapter == null) {
						mAppListAdapter = new AppListAdapter(
								RecommandAppListActivity.this,
								appList,
								Constant.LIST_ITEMTYPE_NORMAL,
								"0");
						mListView.setAdapter(mAppListAdapter);
					} else {
						for (; appListIndex < appListSize; appListIndex++) {
							mAppListAdapter.add(appList.get(appListIndex));
						}
						if (appListIndex >= appListSize && appListIndex != 0) {
							mAppListAdapter.notifyDataSetChanged();
						}
					}
					
					if (tmpAppListSize == 0 || tmpAppListSize < Constant.RECOMMANDLIST_COUNT_PER_TIME) {
						bReachEnd = true;
						mListView.removeFooterView(mFooterView);
//						mListView.addFooterView(mFooterViewSpace);
					}
					if(mListView.getAdapter() == null&&mAppListAdapter!=null){
						mListView.setAdapter(mAppListAdapter);
					}
					if (mListView.getAdapter().isEmpty()) {
						//mListView.removeHeaderView(mHeaderView);
					}
					
					// wait top applications and application list requests all finished
					// then display mListView, or continue display loading
					
					//Modified-s by JimmyJin
//					if (bNewAppList) {
//						initTopApp();
//					} else {
//						mLoadingIndicator.setVisibility(View.GONE);
//						mListView.setVisibility(View.VISIBLE);
//					}
					
					mLoadingIndicator.setVisibility(View.GONE);
					mListView.setVisibility(View.VISIBLE);
//					mAppListAdapter.notifyDataSetChanged();
					//Modified-e by JimmyJin
					if (bNewAppList) {
						bNewAppList = false;
					}
					String where = Downloads.WHERE_RUNNING;
					
					mCursor = mContext.getContentResolver()
								.query(Downloads.CONTENT_URI,
										mCols, where, null, null);
					
					mAppListAdapter.setCursor(mCursor);
					if(mCursor!=null&&!mCursor.isClosed()){
						mCursor.close();
					}
					bInflatingAppList = false;
					
					break;
					
				case ACTION_APP_ICON:
					Image2 icInfo = (Image2) msg.obj;
					Log.v("ACTION_APP_ICON", "ACTION_APP_ICON");
					if (icInfo.mAppIcon != null) {
						CachedThumbnails.cacheThumbnail(mContext,
								icInfo._id, icInfo.mAppIcon);
						if (mAppListAdapter != null) {
							mAppListAdapter.notifyDataSetChanged();
						}
					}
					break;
					
				case ACTION_TOP_APP_DETIAL:
					ArrayList<TopAppDetial> topAppIds = new ArrayList<TopAppDetial>(5);
					topAppIds = (ArrayList<TopAppDetial>) msg.obj;
					
					if (topAppIds != null) {
						Log.v("asd", "topAppIds.size="+topAppIds.size());
						for (int i = 0; i < topAppIds.size(); i++) {
							mTopApp.add(topAppIds.get(i));
						}
					}
					break;
					
				case ACTION_TOP_LAYOUT_DETIAL:
//					int[] layout = (int[]) msg.obj;
					
//					if (layout != null) {
//						int rowCount = layout[0];
//						int colCount = layout[1];
					int rowCount = 1;
						mTopApp = new ArrayList<TopAppDetial>(5);
						// get top application detail info
						mTopDetailRequest = new Request(0, Constant.TYPE_TOP_APP_DETAIL);	
						mTopDetailRequest.addObserver(new Observer() {

							@Override
							public void update(Observable observable, Object data) {
								// TODO Auto-generated method stub
								if (data != null) {
									Message msg = Message.obtain(mHandler, ACTION_TOP_APP_DETIAL, data);
									mHandler.sendMessage(msg);
									Log.v("asd", "data="+data);
								}
							}
						});
						mAppCacheService.getTopAppList(mTopDetailRequest);
//						LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT, 1.0f);
						LinearLayout.LayoutParams lp1 = null;
//						if(rowCount == 1) {
							lp1 = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, mHeaderView.getHeight(), 2.0f);
//						} else {
//							lp1 = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, mHeaderView.getHeight()/2, 2.0f);
//						}
							
							Log.v("asd", "rowCount="+rowCount);
//				        for(int j = 0; j < rowCount; j++) {
				        	ViewGroup linerLayout = (ViewGroup)(LayoutInflater.from(mContext).inflate(R.layout.top_app_linerlayout, null));
				        	
//				        	for(int i = 0; i < colCount; i ++) {
//				        		button = LayoutInflater.from(mContext).inflate(R.layout.top_app_button, null);
//				        		button.setTag(Integer.valueOf((rowCount - 1) * colCount + i));
//				        		button.setOnClickListener(mTopAppClickListener);
//				        		linerLayout.addView(button, lp);
//				        	}
				        	((ViewGroup) mHeaderView).addView(linerLayout, lp1);
//				        }
				       
				        mHeaderView.invalidate();
//					}
					break;
					
				case ACTION_TOP_APP_ICON:
					Drawable[] drawable = (Drawable[]) msg.obj;
					
					if (drawable == null) {
						bContainsHeader = false;
						//Added-s by JimmyJin
						mPreviews.clear();
//						for(int i=0;i<5;i++){
							mPreviews.add(getResources().getDrawable(R.drawable.ad));							
//						}
						 imageChange = (MarketGallery)mHeaderView.findViewById(R.id.gallery_image); 
					        ImageGalleryAdapter imageAdapter = new ImageGalleryAdapter(mContext);
					       imageChange.setAdapter(imageAdapter);

							if(mPreviews.size()>=2)
							{						
								imageChange.setSelection(mPreviews.size()*1000000);
							}
//							LinearLayout pointLinear1 = (LinearLayout) findViewById(R.id.gallery_point_linear);
//					        for (int i = 0; i < mPreviews.size(); i++) {
//					        	ImageView pointView1 = new ImageView(mHeaderView.getContext());
//					        	if(i==0){
//					        		pointView1.setBackgroundResource(R.drawable.feature_point_cur);
//					        	}else
//					        		pointView1.setBackgroundResource(R.drawable.feature_point);
//						        pointLinear1.addView(pointView1);
//							}
					        
							int width = mWindowManager.getDefaultDisplay().getWidth();
							int height = getResources().getDrawable(R.drawable.ad).getMinimumHeight() * width / getResources().getDrawable(R.drawable.ad).getMinimumWidth();
							AbsListView.LayoutParams lp = 
								new AbsListView.LayoutParams(width, height);
					        mHeaderView.setLayoutParams(lp);				
							
					        
					        Message msg1 = Message.obtain(mHandler, ACTION_TOP_LAYOUT_DETIAL, null);
							mHandler.sendMessage(msg1);
					        
//					        mLoadingIndicator.setVisibility(View.GONE);
//					        mListView.setVisibility(View.VISIBLE);
						//Added-e by JimmyJin
						
//					} else if (drawable != null && bContainsHeader) {
					} else if (drawable != null ) {
						Log.v("asd", "drawable.length="+drawable.length);
						bContainsHeader = true;//Added by JimmyJin
						mPreviews.clear();
						for(int i=0;i<drawable.length;i++){
							if(drawable[i]!=null){
							mPreviews.add(drawable[i]);
							}
						}
						 imageChange = (MarketGallery)mHeaderView.findViewById(R.id.gallery_image); 
					        ImageGalleryAdapter imageAdapter = new ImageGalleryAdapter(mContext);
					       imageChange.setAdapter(imageAdapter);

							if(mPreviews.size()>=2)
							{						
								imageChange.setSelection(mPreviews.size()*1000000);
							}
							LinearLayout pointLinear2 = (LinearLayout) findViewById(R.id.gallery_point_linear);
					        for (int i = 0; i < mPreviews.size(); i++) {
					        	ImageView pointView2 = new ImageView(mHeaderView.getContext());
					        	if(i==0){
					        		pointView2.setBackgroundResource(R.drawable.feature_point_cur);
					        	}else
					        		pointView2.setBackgroundResource(R.drawable.feature_point);
						        pointLinear2.addView(pointView2);
							}
					        autoGallery.schedule(new TimerTask() {
					            int gallerypisition = 0;
					            @Override
					            public void run() {
					            	if(mTouch){
					            		gallerypisition = imageChange.getSelectedItemPosition() + 1;
						                Message msg = new Message();
						                Bundle date = new Bundle();// 存放数据
						                date.putInt("pos", gallerypisition);
						                msg.setData(date);
						                msg.what = IMAGE_CHANGE;//消息标识
						                autoGalleryHandler.sendMessage(msg);
					            	}
					            }
					            }, 5000, 5000);
					        
					        imageChange.setOnItemSelectedListener(new OnItemSelectedListener(){

								@Override
								public void onItemSelected(AdapterView<?> parent, View view,
										int position, long id) {
									// TODO Auto-generated method stub
									changePointView(position);
								}

								@Override
								public void onNothingSelected(AdapterView<?> arg0) {
									// TODO Auto-generated method stub
									}
						    	  }
						       );
					        imageChange.setOnItemClickListener(new OnItemClickListener(){

								@Override
								public void onItemClick(AdapterView<?> arg0,
										View view, int position, long id) {
									// TODO Auto-generated method stub
									int nAppIndex = ((Integer)view.getTag()).intValue();
									
									if (nAppIndex >= 0) {
										try {
											Log.i("RecommandAppListActivity", "广告位nAppIndex="+nAppIndex);
											postPV("广告位"+nAppIndex);
										} catch (NameNotFoundException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										if(mTopApp.get(nAppIndex).getAdType() == 0){
											Intent intent = new Intent(mContext, AppInfoPreloadActivity.class);
											intent.putExtra("appId", mTopApp.get(nAppIndex).getAppId());
											intent.putExtra("type",0);
											intent.putExtra("download", mAppListAdapter.getItem(nAppIndex).getTotalDownloads());
											startActivity(intent);
										}
										if(mTopApp.get(nAppIndex).getAdType() == 2){
											Intent intent = new Intent(mContext, TabWebActivity.class);
											intent.putExtra("bUrl", mTopApp.get(nAppIndex).getwebUrl());
											intent.putExtra("type",2);
											intent.putExtra("appIndex",nAppIndex);
											startActivity(intent);
										}
									}
								}
					        });
					        imageChange.setOnTouchListener(new OnTouchListener(){
								@Override
								public boolean onTouch(View v, MotionEvent event) {
									// TODO Auto-generated method stub
									Log.v("asd", "getAction="+event.getAction());
								    switch (event.getAction()) {
								    case MotionEvent.ACTION_MOVE: 
								    	SlideViewPager.mainPager.requestDisallowInterceptTouchEvent(true);
								        break;
								    case MotionEvent.ACTION_UP:
								    	SlideViewPager.mainPager.requestDisallowInterceptTouchEvent(false);
								    	mTouch = true;
								        break;
								    case MotionEvent.ACTION_DOWN:
								    	SlideViewPager.mainPager.requestDisallowInterceptTouchEvent(true);
								    	mTouch = false;
								        break;
								    case MotionEvent.ACTION_CANCEL:
								    	SlideViewPager.mainPager.requestDisallowInterceptTouchEvent(false);
								        break;
								    }
									return false;
								}
					        });
		//				mHeaderView.setBackgroundDrawable(drawable[0]);
						int width = mWindowManager.getDefaultDisplay().getWidth();
						int height = drawable[0].getMinimumHeight() * width / drawable[0].getMinimumWidth();
						AbsListView.LayoutParams lp = 
							new AbsListView.LayoutParams(width, height);
				        mHeaderView.setLayoutParams(lp);
				       
//				        mListView.addHeaderView(mHeaderView, null, false);
//				        bTopAppFlag = true;
		//Marked-s by JimmyJin		        
//						mTopLayoutRequest = new Request(0, Constant.TYPE_TOP_LAYOUT_DETIAL);	
//						mTopLayoutRequest.addObserver(new Observer() {
//
//							@Override
//							public void update(Observable observable, Object data) {
//								// TODO Auto-generated method stub
//								if (data != null) {
//									Message msg = Message.obtain(mHandler, ACTION_TOP_LAYOUT_DETIAL, data);
//									mHandler.sendMessage(msg);
//								}
//							}
//						});
//						mAppCacheService.getTopLayout(mTopLayoutRequest);
		//Marked-e by JimmyJin						
						
				        
				        Message msg1 = Message.obtain(mHandler, ACTION_TOP_LAYOUT_DETIAL, null);
						mHandler.sendMessage(msg1);
				        
//				        if (bAppListFlag&&bNewAppList) {
//				        	mLoadingIndicator.setVisibility(View.GONE);
//				        	mListView.setVisibility(View.VISIBLE);
//				        	bTopAppFlag = true;
//				        }
					}
					
//					mLoadingIndicator.setVisibility(View.GONE);
//					mListView.setVisibility(View.VISIBLE);
					
					break;
				case PROGRESSBAR_UPDATEING:
					Object[] data = (Object[])msg.obj;
					Integer mAppId = (Integer)data[0];
//					Log.i(TAG, "APPID=" + mAppId);
					int progress = ((Integer)data[1]).intValue();
//					Log.v("PROGRESSBAR_UPDATEING", "progress =" + progress);
//					mProgressBar = (ProgressBar) mListView.findViewWithTag(mAppId);		
					mProgressBar = (ProgressBar) mListView.findViewWithTag(getProcessbarViewTag(mAppId));	
					mStatus = (TextView) mListView.findViewWithTag(getStatusViewTag(mAppId));
//					Log.v("PROGRESSBAR_UPDATEING", "mProgressBar =" + mProgressBar);
//					mStatus = (TextView) mListView.findViewWithTag(mProgressBar);
					Log.v("case PROGRESSBAR_UPDATEING", "mStatus ="+mStatus);
					if(mProgressBar != null) {
						mProgressBar.setVisibility(View.VISIBLE);
						mStatus.setVisibility(View.VISIBLE);
						mProgressBar.setProgress(progress);
//						mStatus.setText(mContext.getText(R.string.app_pause));
					}
//					if(mAppListAdapter != null){
//						mAppListAdapter.notifyDataSetChanged();
//					}
					break;
				case ACTION_NETWORK_ERROR:
					mLoadingIndicator.setVisibility(View.GONE);
					mListView.setVisibility(View.VISIBLE);
					bInflatingAppList = false;
					Toast.makeText(mContext, mContext.getString(R.string.error_network_low_speed), Toast.LENGTH_LONG).show();					
//					showDialog(DIALOG_NETWORK_ERROR);
					break;
				default:
					break;
				}
				return;
			}
		};
	}
	private static String getProcessbarViewTag(int appId) {
		return "processbar" + appId;
	}
	private static String getStatusViewTag(int appId) {
		return "status" + appId;
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
    public void changePointView(int cur){
    	LinearLayout pointLinear = (LinearLayout) findViewById(R.id.gallery_point_linear);
    	View view = pointLinear.getChildAt(positon);
    	View curView = pointLinear.getChildAt(cur%mPreviews.size());
    	if(view!=null&& curView!=null){
    		ImageView pointView = (ImageView)view;
    		ImageView curPointView = (ImageView)curView;
    		pointView.setBackgroundResource(R.drawable.feature_point);
    		curPointView.setBackgroundResource(R.drawable.feature_point_cur);
    		positon = cur%mPreviews.size();
    	}
    }
	private void addThumbnailRequest(int position, int id) {
		
		Request request = new Request(0L, Constant.TYPE_APP_ICON);
		Object[] params = new Object[2];
		String imgUrl = mAppListAdapter.getItem(position).getIconUrl();

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
		mIconRequest = request;
		mMarketService.getAppIcon(mIconRequest);
	}

	private void clearPendingThumbRequest() {
		// TODO Auto-generated method stub
		Iterator<Integer> iterator = mIconStatusMap.keySet().iterator();
		
		while (iterator.hasNext()) {
			Integer value = iterator.next();
			if (!mIconStatusMap.get(value).booleanValue()) {
				iterator.remove();
			}
		}
		mMarketService.clearThumbRequest(MarketService.THREAD_THUMB);
	}
	
	public Drawable getThumbnail(int position, int id) {
		// TODO Auto-generated method stub
		boolean bThumbExists = mIconStatusMap.containsKey(Integer.valueOf(position));
		if (bBusy && !bThumbExists) {
			return CachedThumbnails.getDefaultIcon(this);
		}
		
		Drawable drawable = CachedThumbnails.getThumbnail(this, id);
		if (drawable == null) {
			boolean bThumbCached = false;
			if (bThumbExists) {
				bThumbCached = mIconStatusMap.get(Integer.valueOf(position)).booleanValue();
			}
			if (bThumbExists && !bThumbCached) {
				// cause thumb record existed
				// do not sent thumb request again, just return default icon
				return CachedThumbnails.getDefaultIcon(this);
			} else {
				// cause thumb record not existed 
				// or thumb not cached yet,
				// set cached flag as false, and send thumb request
				mIconStatusMap.put(Integer.valueOf(position), Boolean.valueOf(false));
				addThumbnailRequest(position, id);
				return CachedThumbnails.getDefaultIcon(this);
			}
		} else {
			// cause thumb has been cached
			// set cached flag as true
			mIconStatusMap.put(Integer.valueOf(position), Boolean.valueOf(true));
			return drawable;
		}
	}
	
    final Handler autoGalleryHandler = new Handler() {
        public void handleMessage(Message message) {
            super.handleMessage(message);
            switch (message.what) {
                case IMAGE_CHANGE:
//                	Log.v("asd", "IMAGE_CHANGE");
//            		DisplayMetrics dm = new DisplayMetrics();
//            		getWindowManager().getDefaultDisplay().getMetrics(dm);
//            		int screenW = dm.widthPixels;// 获取分辨率宽度
//            		int screenH = dm.heightPixels;// 获取分辨率高度
//            		int screenD = dm.densityDpi;
//            		Log.v("asd", "screenW = "+screenW);
//            		Log.v("asd", "screenH = "+screenH);
//            		Log.v("asd", "screenD = "+screenD);
//            		int velocityX = -1150*screenW/480;
//            		int velocityY = -150*screenH/800;
//            		if(screenW == 540){
//            			velocityX = -1550;
//            		}
//            		if(screenW == 720){
//            			velocityX = -2400;
//            		}
//                	float mEventX1 = 59.333336f*screenW/480;
//                	float mEventY1 = 85.33334f*screenH/800;
//                	MotionEvent e1 = MotionEvent.obtain(SystemClock.uptimeMillis(),  
//                	SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN,mEventX1, mEventY1, 0);  
//                	float mEventX2 = 450.0f*screenW/480;
//                	float mEventY2 = 101.00003f*screenH/800;
//                	MotionEvent e2 = MotionEvent.obtain(SystemClock.uptimeMillis(),  
//                	SystemClock.uptimeMillis(), MotionEvent.ACTION_UP,mEventX2, mEventY2, 0);  
//                	imageChange.onFling(e1, e2, velocityX, velocityY);  
                	imageChange.onKeyDown(KeyEvent.KEYCODE_DPAD_RIGHT,null);
        //        	imageChange.setSelection(message.getData().getInt("pos"));
                    break;
            }
            }
    }; 
    
    final Handler loadDateHandler = new Handler() {
        public void handleMessage(Message message) {
            super.handleMessage(message);
            switch (message.what) {
                case DATE_LOAD:
                	if(bLoadFlag)
                	{
                		initHeaderView();
                		initAd();//Added by JimmyJin
                		initTopApp();
                		inflateAppList();
                		bLoadFlag = false;
            		}
            		break;
            }
            }
    }; 
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		if (mScreenOrietation != newConfig.orientation) {
			resetTopApp();
		}
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mContext = this;
		mWindowManager = getWindowManager();
		mMarketService = MarketService.getServiceInstance(this);
		mAppCacheService = AppCacheService.getServiceInstance(this);
		
		Intent intent = getIntent();
		if (savedInstanceState != null && savedInstanceState.containsKey("new")) {
			bNewAppList = savedInstanceState.getBoolean("new");
		} else {
			bNewAppList = intent.getBooleanExtra("new", false);
		}
		bContainsHeader = intent.getBooleanExtra("header", false);
		intent.getBooleanExtra("topAppFlag", false);
		
		setContentView(R.layout.app_list);
		mHeaderView = 
			LayoutInflater.from(mContext).inflate(R.layout.app_list_header, null);
		initHandlers();
		initListView();
		mObserver = new DownloadingContentObserver();
        getContentResolver().registerContentObserver(Downloads.CONTENT_URI,
                true, mObserver);
		registerIntentReceivers();
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		if (id == DIALOG_NETWORK_ERROR) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this.getParent());
			builder.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.dlg_network_error_title)
				.setMessage(R.string.dlg_network_error_msg)
				.setPositiveButton(R.string.btn_retry, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if (mCurrentRequest != null) {
							mMarketService.getAppList(mCurrentRequest);
						}
					}
				})
				.setNegativeButton(R.string.btn_cancel, null);
			return builder.create();
		}
		return null;
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
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_MENU) {
    		startActivity(new Intent(RecommandAppListActivity.this, OptionsMenu.class));
    		overridePendingTransition(R.anim.fade, R.anim.hold);
    	}
    	return super.onKeyUp(keyCode, event);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event){
//		Log.v("asd", "onTouchEvent="+event.getActionIndex());
//		MarketPage.setTouchIntercept(true);
    	return super.onTouchEvent(event);  
    }
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if (mHeaderView != null) {
			Drawable drawable = mHeaderView.getBackground();
			if (drawable != null && drawable instanceof BitmapDrawable) {
				((BitmapDrawable)drawable).getBitmap().recycle();
			}
		}
		super.onDestroy();
		unregisterIntentReceivers();
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view,
			int position, long id) {
		int newPosition = position; 
		if (bContainsHeader) {
			newPosition--;
		}
		Log.v("asd", "mAppListAdapter="+mAppListAdapter);
		if(newPosition < mAppListAdapter.getCount()) {
			Intent intent = new Intent(this, AppInfoActivity.class);
			intent.putExtra("appInfo",new Application2(mAppListAdapter.getItem(newPosition)));
			intent.putExtra("appId", mAppListAdapter.getItem(newPosition).getAppId());
			/*************Added-s by JimmyJin for Pudding Project**************/
			intent.putExtra("type",0);
			/*************Added-s by JimmyJin for Pudding Project**************/
			intent.putExtra("download", mAppListAdapter.getItem(newPosition).getTotalDownloads());
			startActivity(intent);
		}
	}
	
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		// TODO Auto-generated method stub
//		return OptionsMenu.onOptionsItemSelected(this, item);
//	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (mHeaderView != null) {
			Drawable drawable = mHeaderView.getBackground();
			if (drawable != null) {
				drawable.setCallback(null);
			}
		}
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		Log.v("asd", "onResume");
		mScreenOrietation = getRequestedOrientation();
		if (mHeaderView != null && mHeaderView.getBackground() != null) {
			resetTopApp();
		}
		if (mAppListAdapter != null) {
			mAppListAdapter.notifyDataSetChanged();
			String where = Downloads.WHERE_RUNNING;
			mCursor = mContext.getContentResolver()
					.query(Downloads.CONTENT_URI,
							mCols, where, null, null);
		
		mAppListAdapter.setCursor(mCursor);
		if(mCursor!=null&&!mCursor.isClosed()){
			mCursor.close();
		}
		}
		super.onResume();
	}
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		Log.v("asd", "onStart");
		mScreenOrietation = getRequestedOrientation();
		if (mHeaderView != null && mHeaderView.getBackground() != null) {
			resetTopApp();
		}
		
		if (mAppListAdapter != null) {
			mAppListAdapter.notifyDataSetChanged();
		}
		super.onStart();
	}
	class ImageGalleryAdapter extends BaseAdapter {
		private LayoutInflater mInflator;
		public ImageGalleryAdapter(Context context) {
			mInflator = 
				(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			if (mPreviews != null) 
			{
				//return mPreviews.size();
				return Integer.MAX_VALUE;
			
			}
			return 0;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ImageView imageView;
			if (convertView == null) 
			{
//				convertView = mInflator.inflate(R.layout.top_app_button, null);
//				a.imageView = (ImageView) convertView.findViewById(R.id.imagetext);
//		//		imageView.setLayoutParams(new Gallery.LayoutParams(screenWidth,screenHeight/4));
//			//	imageView.setPadding(2, 2, 2, 2);
				convertView = mInflator.inflate(R.layout.appinfo_detail_preview_item, null);
				
				imageView = (ImageView) convertView;
				imageView.setLayoutParams(new Gallery.LayoutParams(screenWidth,screenHeight));
				
			} 			
			else 
			{
				imageView = (ImageView) convertView;		
			}
			if(0 != mPreviews.size()){
				imageView.setImageDrawable(mPreviews.get(position%mPreviews.size()));
			}
			imageView.setTag(Integer.valueOf(position%mPreviews.size()));
			return convertView;
		}
	}
	class DrawableAnim{
		private Drawable drawable;
		private boolean animFlag;
	}
}