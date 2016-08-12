package com.fineos.theme.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.DownloadManager;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.TextView;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.baidu.mobstat.StatService;
import com.fineos.billing.util.IabHelper;
import com.fineos.billing.util.IabResult;
import com.fineos.theme.R;
import com.fineos.theme.ThemeDataCache;
import com.fineos.theme.adapter.WallpaperPagerAdapter;
import com.fineos.theme.baidusdk.ThemeApplication;
import com.fineos.theme.download.DownloadReceiver;
import com.fineos.theme.download.Downloads;
import com.fineos.theme.download.ReportProvider;
import com.fineos.theme.fragment.DeleteDialogFragment;
import com.fineos.theme.fragment.ProgressDialogFragment;
import com.fineos.theme.fragment.ThemeMixerFragment;
import com.fineos.theme.jazzyviewpager.JazzyViewPager;
import com.fineos.theme.jazzyviewpager.JazzyViewPager.TransitionEffect;
import com.fineos.theme.model.ThemeData;
import com.fineos.theme.utils.Constant;
import com.fineos.theme.utils.FileManager;
import com.fineos.theme.utils.ThemeBillingHelper;
import com.fineos.theme.utils.ThemeLog;
import com.fineos.theme.utils.ThemeUtils;
import com.fineos.theme.utils.Util;
import com.fineos.theme.webservice.IThemeService;
import com.fineos.theme.webservice.Request;
import com.fineos.theme.webservice.ThemeService;
import fineos.app.ProgressDialog;

public abstract class ThemeDetailBaseNewActivity extends Activity implements View.OnClickListener{
	private static final int ACTION_THEME_PREVIEW = 1;
	private static final int ACTION_NETWORK_ERROR = 0;
	private static final int ACTION_DOWN_LOADING_WALLPAPER_PREVIEW = 2;
	private static final int ACTION_HIDE_DIALOG = 3;
	private static final int ACTION_PREVIEW_REQUEST = 4;
	private static final int ACTION_START_GOOGLE_BILLING = 5;
	private static final int ACTION_GET_PRICE = 6;
	private static final int ACTION_SET_PRICE = 7;
	private static final int REQUEST_MESSAGE_DELAY = 200;
	private static final int THEMEMINLEAVE = 5;
	
	private static final int MAX_LOCAL_LOAD_COUNT = 9;
	private static final String ONLINE_THEME_ID = "onlinle_themeid";
	protected static final int DIALOG_LOADING = 999;
	protected ProgressDialog loadingDialog;
	private int wallpaperType = 2;
	private static final String TAG = "ThemeDetailBaseNewActivity";
	private static final Boolean DEBUG = Boolean.TRUE;
	
	protected final int DIALOG_PROGRESS = 0;
	protected ProgressDialog mProgressDialog;
	protected ThemeData mThemeInfo;
	protected Context mContext;
	private ImageView mFontPre;
	private ImageView mDeleteImageView;
	public static int mixType = -1;
	private int mCurrentId = 0;
	protected String[] mPreviewList = null;
	private TextView mThemeApply;
	private Display mDisplay;
	private Handler mHandler;
	private IThemeService mMarketService;
	// private ImageGalleryAdapter mAdapter;
	public static int screenWidth = 0;
	public static int screenHeight = 0;
	private final double RATO_PREVIEW = 0.7;
	private int mVirtualKeyHeight = 0;
	private IThemeService mThemeService;
	List<String> mExcludedItemsList;
	public boolean isOnline;
	public JazzyViewPager mViewPager;
	public PagerAdapter mPagerAdapter;
	private ViewStub mClingViewStub;
	///xuqian add begin (for inapp billing)
    protected ArrayList<String> mSkuList = new ArrayList<String>() ;
	protected ThemeBillingHelper mBillingHelper = null ;
	protected String mProductID = "" ;
	protected String mPrice = "" ;
	protected HashMap<String, String> mPriceMap = new HashMap<String, String>();
	protected Boolean mbFirstEnterBilling = true;
	private boolean mbBuySuccessed = false;
	private static final int CLINGANIMATION = 1000;
	private static final int CLINGANIMATION_DELAY = 500;
	private boolean mbRegister = false;
	private boolean mbSetUpSuccessed = false;
	protected boolean mbGoogleBillingSupport = false ;
	///xuqian add end 
	private boolean mbSrollBusy = false;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mContext = this;
		setContentView(R.layout.themedetail_base_new_activity);

		// mAdapter.notifyDataSetChanged();
		isOnline = getIntent().getIntExtra("isOnline", Constant.THEME_LOCAL_LIST_TYPE) == Constant.THEME_ONLINE_LIST_TYPE;
		mixType = (int) getIntent().getIntExtra("mixType", -1);
		mCurrentId = (int) getIntent().getIntExtra("currentthemeposition", 0);
		if (ThemeDataCache.getThemeDatas(mixType).size() < mCurrentId + 1) {
			ThemeLog.e(TAG, "ThemeDataCache may be cleared finish this Actvity " + "ThemeDataCache.getThemeDatas().size() :" + ThemeDataCache.getThemeDatas(mixType).size() + "mCurrentId :" + mCurrentId);
			super.onCreate(savedInstanceState);
			return;
		}
		ThemeDataCache.initCacheThumbnail();
		mThemeInfo = ThemeDataCache.getThemeDatas(mixType).get(mCurrentId);
		mProgressDialog = new ProgressDialog(this);
		
		int GoogleBillingSupportId = mContext.getResources().getIdentifier("google_billing_support", "bool", "com.fineos");
		   if(GoogleBillingSupportId <= 0){
			   GoogleBillingSupportId = com.fineos.internal.R.bool.google_billing_support;
		}
		mbGoogleBillingSupport = mContext.getResources().getBoolean(GoogleBillingSupportId);
		
		if (DEBUG)
			dump();
		Point smallestSize = new Point();
		Point largestSize = new Point();
		Point realSize = new Point();
		Display display = getWindowManager().getDefaultDisplay();
		display.getCurrentSizeRange(smallestSize, largestSize);
		display.getRealSize(realSize);
		DisplayMetrics dm = new DisplayMetrics();
		display.getMetrics(dm);
		mVirtualKeyHeight = realSize.y - dm.heightPixels;
		
		initHandlers();
		initView();
//		initAcitonBar();
//		showSystemUi(false);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constant.ACTION_THEME_APPLIED);
		filter.addAction(Constant.ACTION_THEME_NOT_APPLIED);
		filter.addAction(Constant.ACTION_DATACHANGE);
		registerReceiver(mBroadcastReceiver, filter);
		registerIntentReceivers();
		mbRegister = true;
	}
	
	///xuqian add for billing begin
		@Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
			// TODO Auto-generated method stub
			ThemeLog.v(TAG,"onActivityResult");
			if(mBillingHelper==null){
				return ;
			}
			if(mbGoogleBillingSupport&&!mBillingHelper.onActivityResult(requestCode, resultCode, data)){
				ThemeLog.v(TAG,"onActivityResult fail handle to super");
				super.onActivityResult(requestCode, resultCode, data);
			}
			
		}
		
		private void setPrice(){
			
			ThemeLog.v(TAG,"setPrice(),mPrice:"+mPrice+"mProductID:"+mProductID+",mCurrentId:"+mCurrentId); //10495
			String price = mPrice ;
			if(mPrice==null||"".equals(mPrice)){
				
				Float fPrice = mThemeInfo.getPrice();   // get from server
				//fPrice = 2.0f ;
				if(fPrice!=0){
					price = "($"+String.valueOf(fPrice)+")";
				}else{
					price = "" ;
				}
				
			}else{
				price = "("+price+")" ;
			}
			mThemeInfo.setsPrice(price);
			
			///xuqian add 
			if(price!=null&&!"".equals(price)){
				
				
				if(mBillingHelper!=null&&!mbBuySuccessed){
					
					Boolean downloaded = Util.checkDownload(mContext, mThemeInfo.getPackageName());
					ThemeLog.v(TAG, "downloaded:"+downloaded+",package:"+mThemeInfo.getPackageName());
					
					if(!downloaded){
						mThemeApply.setText(getResources().getString(R.string.btn_text_buy)+price);
					}
					
				}
				
			}
			mbFirstEnterBilling = false ;
		}
		
    	private void addGoogleBillingRequest() {
			if (mHandler == null) {
				return;
			}
    		mProductID = String.valueOf(mThemeInfo.getId());
			mSkuList.add(mProductID);
			mBillingHelper = new ThemeBillingHelper(this, mSkuList);
			Request request = new Request(0L, Constant.TYPE_START_GOOGLE_BILLING);
    		Object[] params = new Object[2];
    		params[0] = mBillingHelper;
    		params[1] = mSetupFinishedListener;
    		request.setData(params);
    		request.addObserver(new Observer() {

    			@Override
    			public void update(Observable observable, Object data) {
    				// TODO Auto-generated method stub
    				Request request = (Request) observable;
    				switch (request.getStatus()) {
    				case Constant.STATUS_SUCCESS:
    					
    					break;
    				case Constant.STATUS_ERROR:
    					
    					break;
    				default:
    					break;
    				}
    			}
    		});
    		if (mHandler != null) {
    			mHandler.removeMessages(ACTION_START_GOOGLE_BILLING);
        		Message msg = Message.obtain(mHandler, ACTION_START_GOOGLE_BILLING, request);
        		mHandler.sendMessageDelayed(msg, 0);
    		}
    	}
        
       private void addGetPriceRequest() {
    		
    	   mProductID = String.valueOf(mThemeInfo.getId());
			if(!mSkuList.contains(mProductID)){
				mSkuList.add(mProductID);
			}
    		Request request = new Request(0, Constant.TYPE_GET_PRICE);
    		request.setData(mSkuList);
    		request.addObserver(new Observer() {

    			@Override
    			public void update(Observable observable, Object data) {
    				// TODO Auto-generated method stub
    				Request request = (Request) observable;
    				ThemeLog.v(TAG,"request:"+request+",STATUS:"+request.getStatus());
    				switch (request.getStatus()) {
    				case Constant.STATUS_SUCCESS:
    				
    					mPriceMap = (HashMap<String, String>)data;
    					mPrice = mPriceMap.get(mProductID);
    					if (mHandler != null) {
    						Message msg = Message.obtain(mHandler, ACTION_SET_PRICE, null);
        		    		mHandler.sendMessageDelayed(msg, 0);
    					}
    					break;
    				case Constant.STATUS_ERROR:
    					
    					break;
    				default:
    					break;
    				}
    			}
    		});
    		if (mHandler != null) {
    			mHandler.removeMessages(ACTION_GET_PRICE);
        		Message msg = Message.obtain(mHandler, ACTION_GET_PRICE, request);
        		
        		ThemeLog.v(TAG,"mbFirstEnterBilling:"+mbFirstEnterBilling);
        		mHandler.sendMessageDelayed(msg, 0);
    		}
    	}
       
       private IabHelper.OnIabSetupFinishedListener mSetupFinishedListener = new IabHelper.OnIabSetupFinishedListener() {

			@Override
			public void onIabSetupFinished(IabResult result) {
				// TODO Auto-generated method stub
				ThemeLog.d(TAG, "Setup finished.");
				if (!result.isSuccess()) {

					ThemeLog.d(TAG, "Problem  setting up in-app billing,result:"
							+ result);
					return;
				}
				mbSetUpSuccessed = true;
				ThemeLog.d(TAG, "Setup successful. Querying inventory.");
                if(mBillingHelper!=null){
                    mBillingHelper.queryInventoryAsync();
                }
				addGetPriceRequest();
			}
		};
      
       
       
       
        
		///xuqian add for billing end
	
	private void hideStatusBar() {
		WindowManager.LayoutParams attrs = ((Activity)this.mContext).getWindow().getAttributes();
		attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
		attrs.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
		attrs.flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
		attrs.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
		
		((Activity)this.mContext).getWindow().setAttributes(attrs);
	}
	
	@Override
	public void onClick(View v) {

		int viewid = v.getId();
		switch (viewid) {
		case R.id.home_back:
			finish();
			break;
		case R.id.delete_button:
			showDeleteDialog();
			break;
		case R.id.ignore_button:
			ignoreCling();
			break;
		default:
			break;
		}
	}
	
	private void initAcitonBar() {
		getActionBar().setDisplayUseLogoEnabled(false);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setDisplayShowHomeEnabled(false);
		getActionBar().setTitle(mThemeInfo.getTitle());
	}
	
	@SuppressWarnings("deprecation")
    private void showSystemUi(boolean visible) {
		
        int flag = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        if (!visible) {
            // We used the deprecated "STATUS_BAR_HIDDEN" for unbundling
            flag |= View.STATUS_BAR_HIDDEN | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }
        mViewPager.setSystemUiVisibility(flag);
    }

	protected void registerIntentReceivers() {
		IntentFilter intentFilter = new IntentFilter(Downloads.ACTION_DOWNLOAD_COMPLETED_NOTIFICATION);
		mContext.registerReceiver(mThemeReceiver, intentFilter);
		intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
		intentFilter.addDataScheme("package");
		mContext.registerReceiver(mThemeReceiver, intentFilter);
		
		intentFilter = new IntentFilter(DownloadReceiver.ACTION_INSTALL_FAIL);
		mContext.registerReceiver(mThemeReceiver, intentFilter);
		
		intentFilter = new IntentFilter(ThemeMixerFragment.ACTION_IMG_NOTIFY);
		mContext.registerReceiver(mThemeReceiver, intentFilter);
	}

	protected void unregisterIntentReceivers() {

		mContext.unregisterReceiver(mThemeReceiver);
	}

	private BroadcastReceiver mThemeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			String pn = intent.getStringExtra("downloadfilename");
			ThemeLog.d(TAG, "mThemeReceiver action ="+action);
			if (action != null && action.equals(Downloads.ACTION_DOWNLOAD_COMPLETED_NOTIFICATION)) {
				if (isOnline) {
					onDownLoadComplete(pn);
				}
			} else if (action != null && action.equals(Intent.ACTION_PACKAGE_ADDED)) {
				final String extraPackageName = intent.getData().getSchemeSpecificPart();
				if (extraPackageName == null || extraPackageName.length() == 0) {
	                // they sent us a bad intent
	                return;
	            }
				String packageNames[] = new String[] { extraPackageName };
				if (packageNames == null || packageNames.length == 0) {
	                // they sent us a bad intent
	                return;
	            }
				for (int i = 0; i < packageNames.length; i++) {
					String packageName = packageNames[i];
					if (packageName.equals(mThemeInfo.getPackageName())) {
//						return true;
						onApkInstallComplete();
						break;
					}
				}
			}else if(action != null && action.equals(DownloadReceiver.ACTION_INSTALL_FAIL)){
				Toast.makeText(mContext, mContext.getResources().getString(R.string.install_fail), Toast.LENGTH_SHORT).show();
				onApkInstallComplete();
			}
			if(action != null && action.equals(ThemeMixerFragment.ACTION_IMG_NOTIFY)){
				if(mPagerAdapter!=null){
					mPagerAdapter.notifyDataSetChanged();
				}
			}
		}
	};
	
//	private boolean check
	
	private void deleteorDownLoadItems(String packageNameString, boolean download) {
		if (packageNameString == null) {
			return;
		}
		ThemeLog.w(TAG, "packageNameString :" + packageNameString + "download :" + download);
		ArrayList<ThemeData> list = ThemeDataCache.getThemeDatas(mixType);
		int listSize = list.size();
		for (int i = 0 ; i < listSize; i++) {
			ThemeLog.w(TAG, "list.get(i).getPackageName():" + list.get(i).getPackageName());
			if (list.get(i).getPackageName().equals(packageNameString)) {
				list.get(i).setIsDownLoaded(download);
			}
		}
		mPagerAdapter.notifyDataSetChanged();
	}

	
	protected void onDownLoadComplete(String pn) {
		ThemeLog.d(TAG, "mThemeReceiver onDownLoadComplete pn="+pn);
		if (pn != null) {
			String pacString = pn.substring(0, pn.indexOf("."));
			deleteorDownLoadItems(pacString, true);
			if(Util.checkThemeFileSratus(pn)){
				ThemeLog.d(TAG, "mThemeReceiver mThemeInfo.setThemePath ");
				mThemeInfo.setThemePath(Environment.getExternalStorageDirectory() + FileManager.THEME_DIR_PATH + "/" + pn);
			}
		}		
		pageSelected();
	}
	
	protected void onApkInstallComplete() {
		mPagerAdapter.notifyDataSetChanged();
		pageSelected();
	}
	
	private void pageSelected() {
		if (isOnline) {
			String url = mThemeInfo.getDownloadUrl();
			String fileName = url.substring(url.lastIndexOf("/") + 1,url.lastIndexOf("."));
			mThemeInfo.setFileName(fileName);
		}
		if(mThemeInfo.getThemePath() != null||Util.checkThemeDownloadSratus(mThemeInfo.getDownloadUrl(), mThemeInfo)){
			showDeleteImageView(isThemeCanDelete());
		}
		changeViewLp();
		if (isOnline) {
			startInflateOlineList();
			if(mbGoogleBillingSupport){
				addGetPriceRequest();
			}
			if (ThemeDataCache.getPreviewDrawable(getApplicationContext(), Integer.toString(mThemeInfo.getId()), mbSrollBusy) == null && !mbSrollBusy) {
				addPreviewRequest(ThemeDataCache.getThemeDatas(mixType).get(mViewPager.getCurrentItem()));
			}
		}
	}
	
	private void startInflateOlineList() {
		if (mPagerAdapter == null) {
			return;
		}
		if (ThemeDataCache.getThemeDatas(mixType).size() - mViewPager.getCurrentItem() <= THEMEMINLEAVE) {
			Intent intent = new Intent(Constant.ACTION_INFLATE_LIST);
			this.sendBroadcast(intent);
		}
	}
	
	protected void showDeleteImageView(boolean show) {
		if (show) {
			mDeleteImageView.setVisibility(View.VISIBLE);
		} else {
			mDeleteImageView.setVisibility(View.GONE);
		}
	}
	
	private boolean checkApkInstalling(String packageName) {
		String path = mContext.getSharedPreferences("DOWNLOAD_FILE", Context.MODE_PRIVATE).getString(packageName, null);
		ThemeLog.w(TAG, "packageName :" + packageName + "path :" + path);
		if (path != null) {
			return true;
		}
		return false;
	}
	
	private void changeViewLp() {
		if(mThemeApply == null){
			mThemeApply = (TextView) findViewById(R.id.btn_theme_apply);
		}
		if (isOnline&&mThemeInfo!=null) {
			int downloadStatus = getDownloadStatus(mThemeInfo.getDownloadUrl());
			if (downloadStatus == DownloadManager.STATUS_PENDING||downloadStatus == DownloadManager.STATUS_RUNNING
					||downloadStatus == DownloadManager.STATUS_PAUSED) {
				mThemeApply.setEnabled(false);
				mThemeApply.setPadding(0, 0, 0, 0);
				mThemeApply.setText(R.string.btn_downloading);
			}else if (isThemeCanDelete()&&Util.checkThemeDownloadSratus(mThemeInfo.getDownloadUrl(), mThemeInfo)) {
				if (checkInstall(mThemeInfo.getPackageName())) {
					mThemeApply.setEnabled(true);
					mThemeApply.setPadding(0, 0, 0, 0);
					if (mixType == ThemeData.THEME_ELEMENT_TYPE_WALLPAPER) {
						mThemeApply.setText(getResources().getString(R.string.btn_use));
					} else {
						mThemeApply.setText(getResources().getString(R.string.btn_apply));
					}
					
					mThemeApply.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {

							applyThemeButtonClick(mThemeApply);
						}
					});
				} else if (checkApkInstalling(mThemeInfo.getPackageName())) {
					mThemeApply.setEnabled(false);
					mThemeApply.setPadding(0, 0, 0, 0);
					mThemeApply.setText(getResources().getString(R.string.installing));
					mThemeApply.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							applyThemeButtonClick(mThemeApply);
						}
					});
				} else {
					mThemeApply.setEnabled(true);
					mThemeApply.setPadding(0, 0, 0, 0);
					mThemeApply.setText(getResources().getString(R.string.btn_install));
					mThemeApply.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							mThemeApply.setText(getResources().getString(R.string.installing));
							if(Settings.Global.getInt(mContext.getContentResolver(),
					                Settings.Global.INSTALL_NON_MARKET_APPS, 0) > 0){
							mThemeApply.setText(getResources().getString(R.string.installing));}
							applyThemeButtonClick(mThemeApply);
							mThemeApply.setEnabled(false);
						}
					});
				}
				
			}else if (mixType == ThemeData.THEME_ELEMENT_TYPE_DYNAMIC_WALLPAPER && (checkInstall(mThemeInfo.getPackageName()) || checkApkInstalling(mThemeInfo.getPackageName())||Util.checkApkDownloadSratus(mThemeInfo.getDownloadUrl(), mThemeInfo))) {
				if (checkInstall(mThemeInfo.getPackageName())) {
					mThemeApply.setEnabled(true);
					mThemeApply.setPadding(0, 0, 0, 0);
					mThemeApply.setText(getResources().getString(R.string.btn_apply));
					mThemeApply.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {

							applyThemeButtonClick(mThemeApply);
						}
					});
				}else if (checkApkInstalling(mThemeInfo.getPackageName())) {
					mThemeApply.setEnabled(false);
					mThemeApply.setPadding(0, 0, 0, 0);
					mThemeApply.setText(getResources().getString(R.string.installing));
					mThemeApply.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							applyThemeButtonClick(mThemeApply);
						}
					});
				}else if(Util.checkApkDownloadSratus(mThemeInfo.getDownloadUrl(), mThemeInfo)){
					mThemeApply.setEnabled(true);
					mThemeApply.setPadding(0, 0, 0, 0);
					mThemeApply.setText(getResources().getString(R.string.btn_install));
					mThemeApply.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							mThemeApply.setText(getResources().getString(R.string.installing));
							if(Settings.Global.getInt(mContext.getContentResolver(),
					                Settings.Global.INSTALL_NON_MARKET_APPS, 0) > 0){
							mThemeApply.setText(getResources().getString(R.string.installing));}
							applyThemeButtonClick(mThemeApply);
							mThemeApply.setEnabled(false);
						}
					});
				} 
				
			}  else {
				if (mThemeApply == null) {
					ThemeLog.e(TAG, "mThemeApply is null return");
					return;
				}
				mThemeApply.setEnabled(true);
				mThemeApply.setPadding(0, 0, 0, 0);
				mThemeApply.setText(getResources().getString(R.string.btn_download));
				
				mThemeApply.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
							Toast.makeText(mContext, getResources().getString(R.string.sd_unmounted), Toast.LENGTH_SHORT).show();
							return;
						}
						ThemeLog.v(TAG, "Price(google):"+mPrice+",price :"+mThemeInfo.getsPrice());
						if(mbGoogleBillingSupport&&mThemeInfo.getsPrice()!=null&&!"".equals(mThemeInfo.getsPrice())&&mBillingHelper!=null&&!mbBuySuccessed){
							
							if(!mbSetUpSuccessed){
								//Toast.makeText(mContext, getResources().getString(R.string.google_billing_unsupport_tip), Toast.LENGTH_SHORT).show();
								  Util.showAddaccountDialog(mContext);
							}else{
								mBillingHelper.doPurchase(mProductID, 	new IabHelper.OnIabPurchaseFinishedListener() {
									
									@Override
									public void onIabPurchaseFinished(IabResult result,
											com.fineos.billing.util.Purchase purchase) {
				
										ThemeLog.d(TAG, "Purchase finished, result:" + result
												+ ", purchase: " + purchase);
										if (result.isFailure()) {
				
											ThemeLog.d(TAG, "Error purchasing");
											return;
										}
				
										ThemeLog.d(TAG, "Purchase successful.");
										if (purchase.getSku().equals(mProductID)) {
				
											mbBuySuccessed = true;
											ThemeLog.d(TAG, "success purchase :" + mProductID);
											mBillingHelper.consumeAsync(purchase);
											
											startDownload();
											if (mThemeApply != null) {
											    mThemeApply.setEnabled(false);
												mThemeApply.setBackgroundResource(R.drawable.ic_btn_downloading);
												mThemeApply.setPadding(0, 0, 0, 0);
												mThemeApply.setText(R.string.btn_downloading);
											}
											
										}
										
										
				
									}
								});
								
							}// end of else
							
						}else{
							startDownload();
							if (mThemeApply != null) {
								mThemeApply.setEnabled(false);
								mThemeApply.setText(R.string.btn_downloading);
							}
						}
						
//						applyThemeButtonClick(mThemeApply);
					}
				});
				
			}
			
			///xuqian add begin 

			
		} else {
			
			mThemeApply.setEnabled(true);
			mThemeApply.setPadding(0, 0, 0, 0);
			if (mixType == ThemeData.THEME_ELEMENT_TYPE_WALLPAPER) {
				mThemeApply.setText(getResources().getString(R.string.btn_use));
			} else {
				mThemeApply.setText(getResources().getString(R.string.btn_apply));
			}
			mThemeApply.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {

					applyThemeButtonClick(mThemeApply);
				}
			});
		}
	}
	
	public boolean checkInstall(String packageName) {
		return true;
	}
	
	private int getVirtualKeyHeight() {
		return mVirtualKeyHeight;
	}

	private void initView() {
//		View view = findViewById(R.id.bottom_area);
//		FrameLayout.LayoutParams lp = (android.widget.FrameLayout.LayoutParams) view.getLayoutParams();
//		lp.bottomMargin = mVirtualKeyHeight;
		TextView backView = (TextView)findViewById(R.id.home_back);
		mDeleteImageView = (ImageView)findViewById(R.id.delete_button);
		mDeleteImageView.setImageResource(com.fineos.R.drawable.ic_menu_delete_holo_dark);
		backView.setCompoundDrawablesWithIntrinsicBounds(getApplicationContext().getResources().getDrawable(com.fineos.R.drawable.ic_ab_back_holo_dark),
				null, null, null);
		mDeleteImageView.setOnClickListener(this);
		backView.setOnClickListener(this);
		switch(mixType){
		case ThemeData.THEME_ELEMENT_TYPE_WALLPAPER:
		case ThemeData.THEME_ELEMENT_TYPE_DYNAMIC_WALLPAPER:
			backView.setText(R.string.wallpaper_detail);
			break;
		case ThemeData.THEME_ELEMENT_TYPE_LOCKSCREEN:
			backView.setText(R.string.lockscreen_detail);
			break;
		}
		if (isOnline) {
			mThemeService = ThemeService.getServiceInstance(this);
			
		} else {
			mExcludedItemsList = new ArrayList<String>();
		}
		mDisplay = getWindowManager().getDefaultDisplay();
		screenWidth = mDisplay.getWidth();
		screenHeight = mDisplay.getHeight();
		mThemeApply = (TextView) findViewById(R.id.btn_theme_apply);
		
		/// xuqian add begin for google billing
		
		if(mbGoogleBillingSupport&&isOnline){
			addGoogleBillingRequest();
		}
		///	xuqian add begin
		mViewPager = (JazzyViewPager) findViewById(R.id.viewpager);
		pageSelected();
		if (isOnline) {
			if(mThemeInfo.getPreviewUrl()!=null&&mThemeInfo.getPreviewUrl().length>0){
			if (ThemeDataCache.getPreviewDrawable(getApplicationContext(), mThemeInfo.getPreviewUrl()[0], mbSrollBusy) == null) {
				addPreviewRequest(mThemeInfo);
			}
			}
		} else {			
//			preloadImages();
		}
		mPagerAdapter = new WallpaperPagerAdapter(ThemeDataCache.getThemeDatas(mixType), ThemeDetailBaseNewActivity.this, mixType, mViewPager);
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setTransitionEffect(TransitionEffect.Stack);
//		mViewPager.setFadeEnabled(true);
		mViewPager.setCurrentItem(mCurrentId, false);
		mViewPager.setOffscreenPageLimit(2);
		mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int postion) {
				// TODO Auto-generated method stub
				mbSrollBusy = false;
				if (ThemeDataCache.getThemeDatas(mixType).size() < mViewPager.getCurrentItem() + 1) {
					ThemeLog.e(TAG, "onPageSelected err :" + "ThemeDataCache.getThemeDatas().size() :" + ThemeDataCache.getThemeDatas(mixType).size() + "mViewPager.getCurrentItem() :" + mViewPager.getCurrentItem());
					return;
				}
				changeCurrentThemeInfo(ThemeDataCache.getThemeDatas(mixType).get(mViewPager.getCurrentItem()));
//				getActionBar().setTitle(mThemeInfo.getTitle());
				pageSelected();			
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
//				mbSrollBusy = true;
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
				// TODO Auto-generated method stub
				switch (state) {
				case 0:  // idle
					mbSrollBusy = false;
					break;
				case 1: //scrolling
					mbSrollBusy = true;
					break;
				case 2: // stop scroll
					mbSrollBusy = false;
					break;
				default:
					break;
				}
			}
		});
		initClingView();
	}
	
	private void initClingView() {
		SharedPreferences sharedPreferences = getSharedPreferences("theme", Context.MODE_PRIVATE);
		boolean showcling = sharedPreferences.getBoolean(Constant.THEME_CLING, true);
		if (showcling) {
			mClingViewStub = (ViewStub) findViewById(R.id.themedetail_cling);
			mClingViewStub.inflate();
			mThemeApply.setVisibility(View.INVISIBLE);
			
			findViewById(R.id.ignore_button).setOnClickListener(this);
			findViewById(R.id.container).setOnClickListener(this);
			startClingAnim();
		}
	}
	
	private ObjectAnimator m0bjectAnimatora;
	private ObjectAnimator m0bjectAnimatorb;
	private void startClingAnim() {
		final ImageView iv = (ImageView)findViewById(R.id.cling_finger);
		AnimatorSet set = new AnimatorSet();
		m0bjectAnimatora = new ObjectAnimator();
		m0bjectAnimatora.setTarget(iv);
		final float animationdistance = getResources().getDimension(R.dimen.cling_finger_move_distance) * 2;
		m0bjectAnimatora.setPropertyName("translationX");
		m0bjectAnimatora.setFloatValues(animationdistance);
		m0bjectAnimatora.setDuration(CLINGANIMATION);
		m0bjectAnimatorb = new ObjectAnimator();
		m0bjectAnimatorb.setPropertyName("translationX");
		m0bjectAnimatorb.setTarget(iv);
		m0bjectAnimatorb.setFloatValues(0);
		m0bjectAnimatorb.setDuration(CLINGANIMATION);
		m0bjectAnimatora.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(Animator arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animator arg0) {
				// TODO Auto-generated method stub
				if (mHandler == null) {
					return;
				}
				mHandler.postDelayed(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (m0bjectAnimatorb != null) {
							m0bjectAnimatorb.start();
						}

					}
				}, CLINGANIMATION_DELAY);
			}
			
			@Override
			public void onAnimationCancel(Animator arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		m0bjectAnimatorb.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(Animator arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animator arg0) {
				// TODO Auto-generated method stub
				if (mHandler == null) {
					return;
				}
				mHandler.postDelayed(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (m0bjectAnimatora != null) {
							m0bjectAnimatora.start();
						}
					}
				}, CLINGANIMATION_DELAY);

			}
			
			@Override
			public void onAnimationCancel(Animator arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		m0bjectAnimatora.start();
	}
	
	private void stopClingAnimation() {
		if (m0bjectAnimatorb != null) {
			m0bjectAnimatorb.cancel();
			m0bjectAnimatorb = null;
		}
		
		if (m0bjectAnimatora != null) {
			m0bjectAnimatora.cancel();
			m0bjectAnimatora = null;
		}
	}
	
	private void ignoreCling() {
		SharedPreferences sharedPreferences = getSharedPreferences("theme", Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		editor.putBoolean(Constant.THEME_CLING, false);
		editor.commit();
		mThemeApply.setVisibility(View.VISIBLE);
		mClingViewStub.setVisibility(View.GONE);
		stopClingAnimation();
	}

	DialogInterface.OnClickListener onselect = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			wallpaperType = which + 1;
			ThemeData localThemeInfo = mThemeInfo;
			if (isOnline && isThemeCanDelete()) {
				localThemeInfo = ThemeUtils.getThemeByFildId(mContext, mThemeInfo.getPackageName());
			}
			ReportProvider.postUserTheme(ThemeUtils.getpackageName(localThemeInfo, false), ThemeData.THEME_REPORT_SORT_USE);
			applyTheme(localThemeInfo.getThemePath(), false, false, true , wallpaperType);
			if (mThemeApply != null) {
				mThemeApply.setEnabled(false);
			}
		}

	};
	
	public Drawable getPreviewDrawable(ThemeData themeData) {
		if(themeData.getPreviewUrl()!=null&&themeData.getPreviewUrl().length>0){
			Drawable drawable = null;
			Bitmap bmp = ThemeDataCache.getPreviewDrawable(mContext, themeData.getPreviewUrl()[0], mbSrollBusy);
			if(bmp != null){
				drawable = new BitmapDrawable(null, bmp);
			}
			ThemeLog.w(TAG, "getPreviewDrawable :" + "mbSrollBusy :" + mbSrollBusy + "isOnline :" + isOnline + "drawable :" + drawable);
			return drawable;
		}
		return null;
	}

	private void preloadImages() {
		(new PreviewLoaderAsyncTask()).execute();
	}

	private class PreviewLoaderAsyncTask extends AsyncTask {

		@Override
		protected Object doInBackground(Object[] params) {
			int count = Math.min(MAX_LOCAL_LOAD_COUNT, ThemeDataCache.getThemeDatas(mixType).size());
			for (int i = 0; i < count; i++) {
				if (ThemeDataCache.getThemeDatas(mixType).size() - 1 < i) {
					break;
				}
				ThemeData themeData = ThemeDataCache.getThemeDatas(mixType).get(i);
				Bitmap bmp = ThemeDataCache.getLocalPreviewDrawable(getApplicationContext(), themeData.getTitle()
						, themeData, mixType);
				Drawable bd = null;
				if(bmp!=null){
					bd = new BitmapDrawable(null, bmp);
				}

				if (bd != null && mHandler != null) {
					Message msg = Message.obtain(mHandler, ACTION_THEME_PREVIEW);
					mHandler.sendMessage(msg);
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Object o) {
			super.onPostExecute(o);
			
		}
	} // end of PreviewLoaderAsyncTask

	private void addPreviewRequest(ThemeData themeData) {
//		showLoadingDialog();
		if (mHandler == null) {
			return;
		}
		
		if (themeData.getPreviewUrl() == null || themeData.getPreviewUrl().length == 0) {
			Message msg = Message.obtain(mHandler, ACTION_THEME_PREVIEW, null);
			mHandler.sendMessage(msg);
			return;
		}
		final String previewUrl = themeData.getPreviewUrl()[0];
		ImageRequest imageRequest = new ImageRequest(previewUrl, 
				new Listener<Bitmap>(){
					@Override
					public void onResponse(Bitmap response) {
						// TODO Auto-generated method stub
						if (response != null) {
							ThemeDataCache.cachePreviewDrawable(getApplicationContext(), previewUrl, response);
							Log.v(TAG, "sss addPreviewRequest mHandler="+mHandler);
							if(mHandler!=null){
								mHandler.sendEmptyMessage(ACTION_THEME_PREVIEW);
							}
							
						}
					}}, 
				0, 0, Config.ARGB_8888, 
				new ErrorListener(){
					@Override
					public void onErrorResponse(VolleyError error) {
						// TODO Auto-generated method stub
						
					}});
		ThemeApplication.getInstance().addToRequestQueue(imageRequest,Constant.TAG_ONLINE_THEME_PREVIEW);
	}

	private void initHandlers() {
		// TODO Auto-generated method stub
		mHandler = new Handler() {

			@SuppressWarnings("unchecked")
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch (msg.what) {
				case ACTION_THEME_PREVIEW:
					
					hideLoadingDialog();
					if (mPagerAdapter == null) {
						mPagerAdapter = new WallpaperPagerAdapter(ThemeDataCache.getThemeDatas(mixType), ThemeDetailBaseNewActivity.this, mixType, mViewPager);
						mViewPager.setAdapter(mPagerAdapter);
						mViewPager.setCurrentItem(mCurrentId, false);
					}
					mPagerAdapter.notifyDataSetChanged();
					break;					
				case ACTION_NETWORK_ERROR:
					hideLoadingDialog();											
					Toast.makeText(mContext, mContext.getString(R.string.error_network_low_speed), Toast.LENGTH_LONG).show();
					break;
				case ACTION_HIDE_DIALOG:
					hideLoadingDialog();
					break;
				case ACTION_PREVIEW_REQUEST:
					if (mbSrollBusy) {
						if (mHandler != null && msg.obj != null) {
							mHandler.removeMessages(ACTION_PREVIEW_REQUEST);
							Message msg2 = Message.obtain(mHandler, ACTION_PREVIEW_REQUEST, msg.obj);
							mHandler.sendMessageDelayed(msg2, REQUEST_MESSAGE_DELAY);
						}
					} else {
						if (msg.obj != null) {
							mThemeService.getThemePreviews((Request)msg.obj);
						}
					}
										
					break;
				case ACTION_START_GOOGLE_BILLING:
					
					if (msg.obj != null) {
						
						ThemeLog.v(TAG, "mThemeService :"+mThemeService);
						if(mThemeService!=null){
							mThemeService.getStartGoogleBilling((Request)msg.obj);
						}
						
					}	
					break;		
				case ACTION_GET_PRICE:
					
					if (msg.obj != null) {
						
						if(mThemeService!=null){
							mThemeService.getPrice((Request)msg.obj);
						}
						
					}	
					break;	
				
				case ACTION_SET_PRICE:
					setPrice();
					break;		
				default:
					break;
				}
			}
		};
	}

	public String getThemeName(String uri){
		String themeName = null;
		if(uri!=null){
			String tmp[] = uri.split("##_##");
			themeName = tmp[0] == null?tmp[0]:tmp[0].substring(tmp[0].lastIndexOf("/")+1, tmp[0].lastIndexOf("."));
		}
		
		return themeName;
	}
	private String getWallpaperName(String path){
		String themeName;
		String uri = path.split("##_##")[0];
		ThemeLog.i(TAG, "sss getWallpaperName uri = " + uri.split("##_##")[0]);
		themeName = uri == null?uri:uri.substring(uri.lastIndexOf("/")+1, uri.lastIndexOf("."));
		return themeName;
	}
	private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			mProgressDialog.dismiss();
			if (Constant.ACTION_THEME_APPLIED.equals(action)) {
				String uri = intent.getStringExtra("ThemeUri");
				int type = intent.getIntExtra("Type", -1);
				applyThemeSucess(type,uri);
				finish();
			} else if (Constant.ACTION_THEME_NOT_APPLIED.equals(action)) {
				applyThemeFailed();
			} else if (Constant.ACTION_DATACHANGE.equals(action)) {
				((WallpaperPagerAdapter) mPagerAdapter).replaceThemeData(ThemeDataCache.getThemeDatas(mixType));
			} else {
				finish();
			}
		}
	};
	@Override
	protected void onResume() {
		super.onResume();
		StatService.onResume(this);
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
		ThemeLog.v(TAG, "mbGoogleBillingSupport:"+mbGoogleBillingSupport+",mbSetUpSuccessed:"+mbSetUpSuccessed);
		if(mbGoogleBillingSupport&&!mbSetUpSuccessed){
			addGoogleBillingRequest();
		}
		changeViewLp();
	}

	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPause(this);		
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		// ThemeLog.e(TAG, "onDestroy,UnRegister receiver...");
		if (mFontPre != null) {
			mFontPre.setImageDrawable(null);
		}
		if (mbRegister) {
			unregisterIntentReceivers();
			unregisterReceiver(mBroadcastReceiver);
			mbRegister = false;
		}
		stopClingAnimation();
		if(mbGoogleBillingSupport&&mBillingHelper != null){
			mBillingHelper.onDestroy();
			mBillingHelper = null ;
		}
		ThemeDataCache.flushPreviewDrawable();
		if(mHandler != null){
			mHandler.removeCallbacksAndMessages(null); //remove all callbacks and Messages
			mHandler = null;
		}
		
		mContext = null;
		if(mViewPager!=null){
			mViewPager.removeAllPagerViews();
			mViewPager = null;
		}
		
		System.gc();
		try {
			finalize();
		} catch (Throwable e) {
			// TODO: handle exception
		}
		
	}	
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		case R.id.action_delete:
			showDeleteDialog();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public int getDownloadStatus(String themeUrl) {
		int result = 0;

		DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
		DownloadManager.Query myDownloadQuery = new DownloadManager.Query();
//		myDownloadQuery.setFilterByStatus(DownloadManager.STATUS_PAUSED|DownloadManager.STATUS_SUCCESSFUL);
		myDownloadQuery.setFilterByStatus(DownloadManager.STATUS_PENDING | DownloadManager.STATUS_PAUSED | DownloadManager.STATUS_RUNNING);
		Cursor cursor = downloadManager.query(myDownloadQuery);
		int fileNameIdx = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
		int urlIdx = cursor.getColumnIndex(DownloadManager.COLUMN_URI);
		int pathUrlIdx = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
		int statusIdx = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);

		if (cursor != null) {
			ThemeLog.i(TAG, "cursor.getCount(): " + cursor.getCount());
			try {
				while (cursor.moveToNext()) {
					String fileName = cursor.getString(fileNameIdx);
					String downloadUrl = cursor.getString(urlIdx);
					String localdUrl = cursor.getString(pathUrlIdx);
					int status = cursor.getInt(statusIdx);

					ThemeLog.w(TAG, "cursor.getPosition(): " + cursor.getPosition());
					ThemeLog.i(TAG, "fileName: " + fileName);
					ThemeLog.d(TAG, "downloadUrl: " + downloadUrl);
					ThemeLog.v(TAG, "localdUrl: " + localdUrl);
					ThemeLog.i(TAG, "status: " + status);
					if (downloadUrl.equals(themeUrl)) {
						result = status;
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		}
		return result;
	}

	public int getDownloadingCount() {
		int result = 0;
		DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
		DownloadManager.Query myDownloadQuery = new DownloadManager.Query();
		myDownloadQuery.setFilterByStatus(DownloadManager.STATUS_PAUSED | DownloadManager.STATUS_RUNNING | DownloadManager.STATUS_PENDING);
		Cursor cursor = downloadManager.query(myDownloadQuery);
		ThemeLog.i(TAG, "Util.getDownloadStatus -> getCount(): " + cursor.getCount());
		if (cursor != null) {
			result = cursor.getCount();
			cursor.close();
		}
		return result;
	}

	private void showDeleteDialog() {
		removeOldFragmentByTag(Util.DIALOG_TAG_DELETE);
		FragmentManager fragmentManager = getFragmentManager();
		DialogFragment newFragment = DeleteDialogFragment.newInstance();
		((DeleteDialogFragment) newFragment).setOnClickListener(mDeleteDialogListener);
		newFragment.show(fragmentManager, Util.DIALOG_TAG_DELETE);
		fragmentManager.executePendingTransactions();
	}

	private void removeOldFragmentByTag(String tag) {
		FragmentManager fragmentManager = getFragmentManager();
		DialogFragment oldFragment = (DialogFragment) fragmentManager.findFragmentByTag(tag);
		if (null != oldFragment) {
			oldFragment.dismissAllowingStateLoss();
		}
	}

	private final DialogInterface.OnClickListener mDeleteDialogListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int arg1) {
			deleteItems();
			dialog.dismiss();
		}
	};

	public void deleteItems() {
		FileTask fileTask = new FileTask(mThemeInfo);
		fileTask.execute();
		if (!isOnline) {
			ThemeDataCache.removeTheme(mThemeInfo, mixType);
		}
		deleteTheme();
	}

	public class FileTask extends AsyncTask<Void, Object, Boolean> {
		/**
		 * A callback method to be invoked before the background thread starts
		 * running
		 */
		private final ThemeData mDeltedInfo;
		
		public FileTask(ThemeData deletedInfo) {
			if (isOnline && isThemeCanDelete()) {
				if (mixType == ThemeData.THEME_ELEMENT_TYPE_DYNAMIC_WALLPAPER) {

				} else {
					deletedInfo = ThemeUtils.getThemeByFildId(mContext, deletedInfo.getPackageName());
				}
			}
			mDeltedInfo = deletedInfo;
		}
		
		@Override
		protected void onPreExecute() {
			FragmentManager fragmentManager = getFragmentManager();
			DialogFragment newFragment = ProgressDialogFragment.newInstance();
			newFragment.show(fragmentManager, Util.DIALOG_TAG_PROGRESS);
			fragmentManager.executePendingTransactions();
		}

		/**
		 * A callback method to be invoked when the background thread starts
		 * running
		 * 
		 * @param params
		 *            the method need not parameters here
		 * @return true/false, success or fail
		 */
		@Override
		protected Boolean doInBackground(Void... params) {
			File file = null;
			if (mixType == ThemeData.THEME_ELEMENT_TYPE_DYNAMIC_WALLPAPER) {
				String fileName = mThemeInfo.getDownloadUrl().substring(mThemeInfo.getDownloadUrl().lastIndexOf("/") + 1);
				file = new File(Environment.getExternalStorageDirectory()
						+ FileManager.APP_DIR_NAME + "/" + fileName);
			} else {
				file = new File(mDeltedInfo.getThemePath());
			}
			
			if (file.exists()) {
				file.delete();
			}
			return true;
		}

		/**
		 * A callback method to be invoked after the background thread performs
		 * the task
		 * 
		 * @param result
		 *            the value returned by doInBackground()
		 */
		@Override
		protected void onPostExecute(Boolean result) {
			removeOldFragmentByTag(Util.DIALOG_TAG_PROGRESS);

			if (!result) {
				Toast.makeText(mContext, getResources().getString(R.string.delete_faild), Toast.LENGTH_SHORT).show();
				return;
			}
			if (mixType != ThemeData.THEME_ELEMENT_TYPE_DYNAMIC_WALLPAPER) {
				ThemeUtils.deleteTheme(mDeltedInfo, mContext);
			}
//			mContext.sendBroadcast(new Intent(Downloads.ACTION_DOWNLOAD_COMPLETED_NOTIFICATION));
			deleteorDownLoadItems(mDeltedInfo.getPackageName(), false);
			finish();
		}

		/**
		 * A callback method to be invoked when the background thread's task is
		 * cancelled
		 */
		@Override
		protected void onCancelled() {
			FragmentManager fragmentManager = getFragmentManager();
			DialogFragment oldFragment = (DialogFragment) fragmentManager.findFragmentByTag(Util.DIALOG_TAG_PROGRESS);
			if (null != oldFragment) {
				oldFragment.dismissAllowingStateLoss();
			}
		}
	}
	
	public void changeCurrentThemeInfo(ThemeData themeinfo) {
		mThemeInfo = themeinfo;
	}
	
	protected void startDownload() {
		DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);

		String url = mThemeInfo.getDownloadUrl();
		String fileName = url.substring(url.lastIndexOf("/") + 1);

		ThemeLog.i(TAG, "fileName =" + fileName);
		ThemeLog.d(TAG, "context =" + mContext.getPackageName());
		ThemeLog.w(TAG, "APP_DIR_NAME =" + FileManager.APP_DIR_NAME);

		String path = Environment.getExternalStoragePublicDirectory(FileManager.THEME_DIR_PATH) + File.separator + fileName;
		if (new File(path).exists()) {
			Toast.makeText(this, getResources().getString(R.string.file_downloaded), Toast.LENGTH_SHORT).show();
			return;
		}
		if (getDownloadingCount() >= 5) {
			Toast.makeText(this, getResources().getString(R.string.file_download_more), Toast.LENGTH_SHORT).show();
			return;
		}
		Uri resource = Uri.parse(url);
		DownloadManager.Request request = new DownloadManager.Request(resource);
		MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
		String mimeString = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(url));
		request.setMimeType(mimeString);
		request.setShowRunningNotification(true);
		request.setVisibleInDownloadsUi(true);
		request.setDestinationInExternalPublicDir(FileManager.THEME_DIR_PATH, fileName);
		mThemeInfo.setFileName(fileName);
//		mThemeInfo.setThemePath(Environment.getExternalStorageDirectory() + FileManager.THEME_DIR_PATH + "/" + fileName);
		request.setTitle(fileName);

		long id = downloadManager.enqueue(request);
		Toast.makeText(this, getResources().getString(R.string.start_download), Toast.LENGTH_SHORT).show();

		ReportProvider.postUserTheme(mThemeInfo.getPackageName(), ThemeData.THEME_REPORT_SORT_DOWNLOAD_S);
		switch (mixType){
		case ThemeData.THEME_ELEMENT_TYPE_DYNAMIC_WALLPAPER:
			StatService.onEvent(mContext, "DownloadDynamicWallpaper", mThemeInfo.getTitle(),1);
			break;
		case ThemeData.THEME_ELEMENT_TYPE_LOCKSCREEN:
			StatService.onEvent(mContext, "DownloadLockscreen", mThemeInfo.getTitle(),1);
			break;
		case ThemeData.THEME_ELEMENT_TYPE_WALLPAPER:
			StatService.onEvent(mContext, "DownloadWallpaper", mThemeInfo.getTitle(),1);
			break;
		default :
			StatService.onEvent(mContext, "DownloadTheme", mThemeInfo.getTitle(),1);
			break;
		}
	//	StatService.onEvent(mContext, "Download", mThemeInfo.getTitle());

		ThemeLog.i(TAG, "themePath themeId =" + mThemeInfo.getId());

		SharedPreferences.Editor editor = mContext.getSharedPreferences("DOWNLOAD_FILE", Context.MODE_PRIVATE).edit();
		editor.putString("" + id, fileName);
		editor.commit();
	}
	
	protected void showLoadingDialog() {
//		showDialog(DIALOG_LOADING);
	}

	protected void hideLoadingDialog() {
		new Handler().postDelayed(new Runnable() {
			public void run() {
				if (loadingDialog != null && loadingDialog.isShowing()) {
					loadingDialog.dismiss();
				}
			}
		}, 600);
	}

	@Override
	public void onBackPressed() {
		// do something what you want
		super.onBackPressed();
		overridePendingTransition(com.fineos.R.anim.slide_in_left, 
        		com.fineos.R.anim.slide_out_right);
	}
	
	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(com.fineos.R.anim.slide_in_left, com.fineos.R.anim.slide_out_right);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_LOADING:
			if (loadingDialog != null && loadingDialog.isShowing()) {
				loadingDialog.dismiss();
			}
			if (loadingDialog == null) {
				loadingDialog = new ProgressDialog(this);
			}
			loadingDialog.setTitle(getString(R.string.load));
			loadingDialog.setMessage(getString(R.string.loading));
			loadingDialog.setCancelable(false);
		}

		return loadingDialog;
	}
	
	protected boolean isThemeCanDelete() {
		
		if (Util.checkDownload(mContext, mThemeInfo.getFileName())) {
			mThemeInfo.setThemePath(Util.checkDownload(mContext, mThemeInfo.getFileName(), true));
			Log.v(TAG, "ssss isThemeCanDelete mThemeInfo.getThemePath="+mThemeInfo.getThemePath());
			if (mThemeInfo.getThemePath() != null && mThemeInfo.getThemePath().startsWith(Constant.SYSTEM_THEME_PATH)) {
				return false;
			}
			return true;
		}
		return false;
//		if (mThemeInfo.getIsDownLoaded()) {
//			return true;
//		} else {
//			return false;
//		}
	}
	
	public abstract void deleteTheme();
	public abstract void applyThemeButtonClick(TextView themeapplybutton);
	public abstract void applyThemeFailed();
	public abstract void applyThemeSucess(int type,String uri);
	protected abstract void applyTheme(String theme, boolean applyFont, boolean scaleBoot, boolean removeExistingTheme,int wallpaperType);

	protected void dump() {
		if (mThemeInfo == null)
			return;
		ThemeLog.i(TAG, "id=" + mThemeInfo.getId());
		ThemeLog.i(TAG, "FileName=" + mThemeInfo.getFileName());
		ThemeLog.i(TAG, "author=" + mThemeInfo.getauthor());
		ThemeLog.i(TAG, "designer=" + mThemeInfo.getdesigner());
		ThemeLog.i(TAG, "DownLoadCounts=" + mThemeInfo.getDownLoadCounts());
		ThemeLog.i(TAG, "DownloadingFlag=" + mThemeInfo.getDownloadingFlag());
		ThemeLog.i(TAG, "DownloadUrl=" + mThemeInfo.getDownloadUrl());
		ThemeLog.i(TAG, "IconUrl=" + mThemeInfo.getIconUrl());
		ThemeLog.i(TAG, "PreviewsList=" + mThemeInfo.getPreviewsList());
		// ThemeLog.e(TAG,"Price="+mThemeInfo.getPrice());
		ThemeLog.i(TAG, "Rating=" + mThemeInfo.getRating());
		ThemeLog.i(TAG, "Size=" + mThemeInfo.getSize());
		ThemeLog.i(TAG, "description=" + mThemeInfo.getDescription());
		ThemeLog.i(TAG, "ThemePath=" + mThemeInfo.getThemePath());
		ThemeLog.i(TAG, "Title=" + mThemeInfo.getTitle());
		ThemeLog.i(TAG, "Type=" + mThemeInfo.getType());
		ThemeLog.i(TAG, "UiVersion=" + mThemeInfo.getUiVersion());
		ThemeLog.i(TAG, "Version=" + mThemeInfo.getVersion());
		ThemeLog.i(TAG, "lastUpdate=" + mThemeInfo.getlastUpdate());
		ThemeLog.i(TAG, "is Locale ? :" + mThemeInfo.getLocale());

	}
	

}
