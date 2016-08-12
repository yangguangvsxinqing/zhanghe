package com.fineos.theme.activity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.SystemProperties;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Interpolator;
import android.webkit.MimeTypeMap;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.baidu.frontia.FrontiaApplication;
import com.baidu.mobstat.StatService;
import com.cmcm.adsdk.NativeAdListener;
import com.cmcm.adsdk.NativeAdManager;
import com.cmcm.adsdk.nativead.CMNativeAd;
import com.fineos.android.rom.sdk.ClientInfo;
import com.fineos.android.rom.sdk.bean.SubjectAdList;
import com.fineos.android.rom.sdk.bean.ThemeList;
import com.fineos.theme.R;
import com.fineos.theme.SystemBarTintManager;
import com.fineos.theme.ThemeConfig;
import com.fineos.theme.ThemeDataCache;
import com.fineos.theme.baidusdk.ThemeApplication;
import com.fineos.theme.download.Constants;
import com.fineos.theme.download.Downloads;
import com.fineos.theme.download.ReportProvider;
import com.fineos.theme.model.CustomData;
import com.fineos.theme.model.Image2;
import com.fineos.theme.model.SubjectAdData;
import com.fineos.theme.model.ThemeData;
import com.fineos.theme.ui.ADViewpager;
import com.fineos.theme.ui.HeaderGridView;
import com.fineos.theme.ui.ThemeHeaderViewGroup;
import com.fineos.theme.utils.CachedThumbnails;
import com.fineos.theme.utils.Constant;
import com.fineos.theme.utils.DeviceUtil;
import com.fineos.theme.utils.FileManager;
import com.fineos.theme.utils.ImageCache;
import com.android.volley.Response.ErrorListener;
import com.android.volley.toolbox.ImageRequest;
import com.fineos.theme.utils.ThemeLog;
import com.fineos.theme.utils.ThemeUtils;
import com.fineos.theme.utils.Util;
import com.fineos.theme.webservice.Request;
import com.fineos.theme.webservice.ThemeService;
import com.huaqin.android.rom.sdk.bean.SplashAd;
import com.huaqin.android.rom.sdk.bean.VersionUpgrade;
import com.huaqin.romcenter.ROMCenter;
import com.huaqin.romcenter.utils.ApkFlagRequestAgent;
import com.huaqin.romcenter.utils.ROMApplication;
import com.huaqin.romcenter.utils.SplashADRequestAgent;
import com.huaqin.romcenter.utils.UpdateRequestAgent;
import com.umeng.analytics.AnalyticsConfig;
import com.umeng.analytics.MobclickAgent;
/*广点通
import com.qq.e.ads.nativ.NativeAD.NativeAdListener;
import com.qq.e.ads.nativ.NativeAD;
import com.qq.e.ads.nativ.NativeADDataRef;*/
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengRegistrar;
import com.umeng.update.UmengUpdateAgent;

import fineos.app.AlertDialog;
import fineos.app.ProgressDialog;
import fineos.widget.listview.swinginadapters.prepared.SwingBottomInAnimationAdapter;

import com.fineos.billing.util.IabHelper;
import com.fineos.billing.util.IabResult;
import com.fineos.theme.utils.ThemeBillingHelper;

import com.fineos.volley.GsonGetRequest;
import com.fineos.volley.GsonPostRequest;
import com.fineos.volley.VolleyURLBuilder;
public class ThemeOnlineHomeActivity extends Activity{
	private static final String TAG = "FineOSTheme.ThemeOnlineHomeActivity";
	private HeaderGridView mGridView;
	private ThemeHeaderViewGroup mThemeHeaderViewGroup;
	private LinearLayout emptyView;
	private LinearLayout sadView;
	private ThemeListAdapter mAdapter;
	private ImageView grayIV;
	private ImageGalleryAdapter imageAdapter;
	private static final int ACTION_NETWORK_ERROR = 0;
	private static final int ACTION_THEME_LIST = 1;
	private static final int ACTION_THEME_ICON = 2;
	private static final int ACTION_THEME_AD = 3;
	private static final int ACTION_THEME_AD_IMG = 4;
	private static final int ACTION_SPLASH_AD_ID = 5;
	private static final int ACTION_START_GOOGLE_BILLING = 6;
	private static final int ACTION_GET_PRICE = 7;
	private static final int ACTION_SET_PRICE = 8 ;
	private static final int ACTION_WRITE_DATA = 300 ;
	
	private static final int DIALOG_UPDATE_AVALIABLE = 100;
	private static final int DIALOG_UPDATE_NONE = 101;
	public static final int IMAGE_CHANGE = 10;
	private static final int DIALOG_LOADING = 999;
	private static final int SCROLL_ANIMATION_TIME = 400;
	private static final int AUTO_SCROLL_TIME = 5000;
	private static final int MAX_THEME_ADD_ERR = 2;
	private ProgressDialog loadingDialog;
	private Display mDisplay;
	public int screenWidth = 0;
	public int screenHeight = 0;
	private boolean aDFlag;
	private DownloadManager downloadManager;
	private int nStartIndex;
	private ThemeService mThemeService;
	private boolean bReachEnd;
	private boolean bInflatingAppList;
	private Context mContext;
	private boolean bBusy;
	private ImageView updateFlag;
	private String mSelfUpdateUrl;
	private String mSelfUpdateMsg;
	
//	private NativeADDataRef adItem;
//	private NativeAD nativeAD;
	private List<ThemeData> mThemeList;
	private ArrayList<ThemeData> mTmpThemeList = new ArrayList<ThemeData>();
	private List<SubjectAdData> mADList;
	private boolean mTouch = true;
	private int positon = 0;
	private Hashtable<Integer, Boolean> mIconStatusMap = new Hashtable<Integer, Boolean>();
	private Hashtable<Integer, Bitmap> mADIconMap = new Hashtable<Integer, Bitmap>();
	private ADViewpager mIvHelp;
	private static final int INITPOSITON = 100;
	private String adPosId;
	public Handler handler = new Handler();
	private int[] drawbleids = {
			   R.drawable.ic_fonts_big,	   
			   R.drawable.ic_lockwallpaper_big,
			   R.drawable.ic_dynamicwallpaper_big,
			   R.drawable.ic_lockscreen_big,
			   R.drawable.ic_local_big
	};
	List<CMNativeAd> adList;
	private boolean mbFirstEnter;
	private boolean mbLoadAd = false;
	private boolean mbLoadData;
	private View mLoadingView;
	private ImageView mFirstLoadImageView;
	private SystemBarTintManager mSystemBarTintManager;
	private FrameLayout mDectorView;
	
	///聚合广告 add 
	private NativeAdManager nativeAdManager;
	///xuqian add begin (for inapp billing)
	private ThemeBillingHelper mBillingHelper = null ;
	private ArrayList<String> mSkuList = new ArrayList<String>() ;
	private boolean mbFirstEnterBilling = true;
	protected HashMap<String, String> mPriceMap = new HashMap<String, String>();
	private boolean mbSetUpSuccessed = false;
	private boolean mbGoogleBillingSupport = false ;
	///xuqian add end 
	
	private SwingBottomInAnimationAdapter mSwingBottomInAnimationAdapter;
	private boolean mbSetStartPostion = false;
	private boolean mbInflateAdSuccess = false;
	private LinearLayout pointLinear2;
	private BroadcastReceiver mThemeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String actionString = intent.getAction();
			String packageNameString = intent.getStringExtra("packageName");
			if (actionString.equals(Downloads.ACTION_DOWNLOAD_COMPLETED_NOTIFICATION)) {
				String pn = intent.getStringExtra("downloadfilename");
				if (pn != null) {
					String pacString = pn.substring(0, pn.indexOf("."));
					deleteorDownLoadItems(pacString, true);
				}
			} else if (actionString.equals(Constants.ACTION_DELTEITEMS)) {
				deleteorDownLoadItems(packageNameString, false);
			} else if (actionString.equals(Constants.ACTION_DOWNLOADITMES)) {
				deleteorDownLoadItems(packageNameString, true);
			}
		}
	};
	
	private void deleteorDownLoadItems(String packageNameString, boolean download) {
		if (mAdapter == null || packageNameString == null) {
			return;
		}
		int count = mAdapter.getCount();
		for (int i = 0 ; i < count; i++) {
			if (mAdapter.getItem(i).getPackageName().equals(packageNameString)) {
				mAdapter.getItem(i).setIsDownLoaded(download);
			}
		}
		mAdapter.notifyDataSetChanged();
	}

	private void registerIntentReceivers() {
		IntentFilter intentFilter = new IntentFilter(Downloads.ACTION_DOWNLOAD_COMPLETED_NOTIFICATION);
		intentFilter.addAction(Constants.ACTION_DELTEITEMS);
		intentFilter.addAction(Constants.ACTION_DOWNLOADITMES);
		mContext.registerReceiver(mThemeReceiver, intentFilter);
	}

	private void unregisterIntentReceivers() {

		mContext.unregisterReceiver(mThemeReceiver);
	}
	
	public void stopAutoScroll() {
		mHandler.removeMessages(IMAGE_CHANGE);
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case ACTION_THEME_LIST:

				hideLoadingDialog();
				ThemeList themeList = (ThemeList) msg.obj;
				int appListSize = 0;
				int appListIndex = 0;
				if (themeList != null && themeList.getThemeInfos() != null) {
					if (themeList.getThemeInfos() != null && themeList.getThemeInfos().size() > 0) {
						emptyView.setVisibility(View.GONE);
						sadView.setVisibility(View.GONE);
						if (nStartIndex == 0||mThemeList != null) {
							mThemeList.clear();
						}
						if (nStartIndex == 0||mTmpThemeList != null) {
							mTmpThemeList.clear();
						}
						Log.v(TAG, "ssss ACTION_THEME_LIST mTmpThemeList ="+mTmpThemeList.size());
						appListSize = themeList.getThemeInfos().size();
						nStartIndex += appListSize;
						//mTmpThemeList = new ArrayList<ThemeData>();
						int themeListSize = themeList.getThemeInfos().size();
						for (int i = 0; i < themeListSize; i++) {
							mTmpThemeList.add(new ThemeData(themeList.getThemeInfos().get(i), ThemeOnlineHomeActivity.this));
							mThemeList.add(new ThemeData(themeList.getThemeInfos().get(i), ThemeOnlineHomeActivity.this));
							int themeid = themeList.getThemeInfos().get(i).getThemeId();
							ThemeLog.v("ThemeOnlineHomeActivity","themeid:"+themeid+",mbFirstEnterBilling:"+mbFirstEnterBilling);
						    
							///xuqian add for google biling begin
							if(mbGoogleBillingSupport){
								if(!mSkuList.contains(String.valueOf(themeid))){
									mSkuList.add(String.valueOf(themeid));
							    }
							}
							///xuqian add for google biling end 
						}
						
						///xuqian add for google billing begin
						if(mbGoogleBillingSupport){
							
							ThemeLog.v(TAG,"Setuped ? "+mbSetUpSuccessed+",mbFirstEnterBilling:"+mbFirstEnterBilling);
							if(mbFirstEnterBilling||!mbSetUpSuccessed){   //newwork not ok ,start googleBilling
								addGoogleBillingRequest();
							}
							
							if(mbSetUpSuccessed){
								
								addGetPriceRequest();
								
							}
						}
						///xuqian add for google billing end
						Log.v(TAG, "ssss ACTION_THEME_LIST mTmpThemeList ="+mTmpThemeList.size());
						if (mAdapter == null) {
							mAdapter = new ThemeListAdapter(mContext, mTmpThemeList);
							mSwingBottomInAnimationAdapter = ThemeUtils.setAnimationAdapter(mGridView, ThemeUtils.ANIMATION_TIME, mAdapter);
					   } else {
							int listSize = mThemeList.size();
							for (; appListIndex < appListSize; appListIndex++) {
								mAdapter.add(mTmpThemeList.get(appListIndex));
							}
							if (appListIndex >= appListSize && appListIndex != 0) {
								mAdapter.notifyDataSetChanged();
							}
						}
						if (appListSize == 0 || appListSize < Constant.THEME_LIST_COUNT_PER_TIME) {
							bReachEnd = true;
						}
						
						Log.v(TAG, "ssss ACTION_THEME_LIST mAdapter ="+mAdapter.getCount());

						bInflatingAppList = false;
					}
				} else {
					emptyView.setVisibility(View.VISIBLE);
					sadView.setVisibility(View.VISIBLE);
				}

				break;

			case ACTION_THEME_ICON:
				if (mAdapter != null) {
					mAdapter.notifyDataSetChanged();
				}
				break;
			case ACTION_THEME_AD:
				SubjectAdList subjectAdList = (SubjectAdList)msg.obj;
				int index = 0;
				String posADID = null;
				if (subjectAdList != null && subjectAdList.getSubjectAds() != null && subjectAdList.getSubjectAds().size() > 0) {
					mbInflateAdSuccess = true;
				} else {
					mbInflateAdSuccess = false;
				}
				mADList.clear();
				int subjectAdListSize = subjectAdList.getSubjectAds().size();
				for (int i = 0; i < subjectAdListSize; i++) {
					if(subjectAdList.getSubjectAds().get(i).getType()==1&&(subjectAdList.getSubjectAds().get(i).getAdId()==null
							||subjectAdList.getSubjectAds().get(i).getAdId().length()==0)){
						continue;
					}
					if(subjectAdList.getSubjectAds().get(i).getType() <= 2){
						mADList.add(new SubjectAdData(subjectAdList.getSubjectAds().get(i)));
						if(mADList.get(i).getType() == 1){
							index++;
							adPosId = mADList.get(i).getAdId();
						}
					}else{
						subjectAdList.getSubjectAds().remove(i);
						i--;
						subjectAdListSize--;
					}
				//		getADThumbnail(i,new SubjectAdData(subjectAdList.getSubjectAds().get(i)));
				}
				ThemeLog.v(TAG, "mADList =" + mADList.size());
				ThemeLog.v(TAG, "adPosId =" + adPosId);
				if(adPosId!=null&&adPosId.length()>0){
					SharedPreferences sharedPreferences = getSharedPreferences("theme", Context.MODE_PRIVATE); //私有数据
					Editor editor = sharedPreferences.edit();
					editor.putString("adPosId", adPosId);
					editor.commit();
				}
				loadAD(adPosId,index);
				break;
			case ACTION_THEME_AD_IMG:	
				Image2 icInfo = (Image2) msg.obj;
				if (mADIconMap == null) {
					icInfo.mAppIcon.recycle();
					break;
				}
				if (icInfo.mAppIcon != null) {
					mADIconMap.put(icInfo._id, icInfo.mAppIcon);
				}
				ThemeLog.i(TAG, "ACTION_THEME_AD_IMG Image2 =" + icInfo._id);
				ThemeLog.i(TAG, "ACTION_THEME_AD_IMG Image2 =" + icInfo.mAppIcon);
				ThemeLog.i(TAG, "mADIconMap.size() =" + mADIconMap.size());
				ThemeLog.i(TAG, "mADIconMap.size() =" + mADList.size());
				if(mADIconMap.size() == mADList.size()&&!aDFlag){
					List<View> adImgList = new ArrayList<View>();
					aDFlag = true;
					pointLinear2 = (LinearLayout) findViewById(R.id.gallery_point_linear);
			        int mADListSize = mADList.size();
					for (int i = 0; i < mADListSize; i++) {
			        	ImageView pointView2 = new ImageView(getApplicationContext());
			        	android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			    		lp.leftMargin = (int)getResources().getDimension(R.dimen.point_margin_left);
//			    		lp.bottomMargin = (int)getResources().getDimension(R.dimen.point_margin_left);
			    		pointView2.setLayoutParams(lp);
			    		if(i==0) {
			        		pointView2.setBackgroundResource(R.drawable.feature_point_cur);
			        	} else {
			        		pointView2.setBackgroundResource(R.drawable.feature_point);
			        	}
			        	if (pointLinear2 != null) {
			        		pointLinear2.addView(pointView2);
			        	}
			        	View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.sub_ad_item, null);
			        	ImageView adImg = (ImageView)view.findViewById(R.id.sub_img);
			        	adImg.setTag(i);
						adImg.setBackground(new BitmapDrawable(null, mADIconMap.get(i)));
						adImgList.add(view);
						adImg.setOnClickListener(mAdclickListener);
					}
			        imageAdapter = new ImageGalleryAdapter(adImgList);
			        
					mIvHelp.setAdapter(imageAdapter);
					
					if (adImgList.size() > 0) {
//						mIvHelp.setCurrentItem(adImgList.size() * INITPOSITON);
					}					
					mIvHelp.setOnTouchListener(new OnTouchListener(){
						@Override
						public boolean onTouch(View v, MotionEvent event) {
							// TODO Auto-generated method stub
						    switch (event.getAction()) {
						    case MotionEvent.ACTION_MOVE: 
						        break;
						    case MotionEvent.ACTION_UP:
						    	beginAutoScroll();
						        break;
						    case MotionEvent.ACTION_DOWN:
						    	stopAutoScroll();
						        break;
						    case MotionEvent.ACTION_CANCEL:
						        break;
						    }
							return false;
						}
			        });
					mIvHelp.setOnPageChangeListener(new OnPageChangeListener(){

						@Override
						public void onPageScrollStateChanged(int arg0) {
							// TODO Auto-generated method stub
							changePointView(mIvHelp.getCurrentItem());
						}

						@Override
						public void onPageScrolled(int arg0, float arg1, int arg2) {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void onPageSelected(int arg0) {
							// TODO Auto-generated method stub
							changePointView(arg0);
						}});
						beginAutoScroll();
				}
				break;
			case ACTION_SPLASH_AD_ID:
				String splashAd = (String)msg.obj;
				ThemeLog.v(TAG, "SplashActivity ACTION_SPLASH_AD_ID splashAD ="+splashAd);
				SharedPreferences sharedPreferences = getSharedPreferences("theme", Context.MODE_PRIVATE); //私有数据
				Editor editor = sharedPreferences.edit();
				editor.putString("splashAd", splashAd);
				editor.commit();
				break;	
			case ACTION_NETWORK_ERROR:
				
				hideLoadingDialog();
				if (!mbLoadData) {
					emptyView.setVisibility(View.VISIBLE);
					sadView.setVisibility(View.VISIBLE);
				} else {
					Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.error_network_low_speed), Toast.LENGTH_LONG).show();
				}				
				bInflatingAppList = false;
				break;
			case IMAGE_CHANGE:
				ThemeLog.v(TAG, "IMAGE_CHANGE");
				if (mIvHelp.getAdapter() != null && mIvHelp.getAdapter().getCount() > 1) {
					startScrollAnimation();
					changePointView(mIvHelp.getCurrentItem());
		        }
				break;
				
			case ACTION_START_GOOGLE_BILLING:
				if (msg.obj != null) {
					
					if(mThemeService!=null){
						mThemeService.getStartGoogleBilling((Request)msg.obj);
					}
				}
				ThemeLog.v(TAG,"XUQIAN,ACTION_START_GOOGLE_BILLING...");
				
				break;		
			case ACTION_GET_PRICE:
				if (msg.obj != null) {
					
					if(mThemeService!=null){
						mThemeService.getPrice((Request)msg.obj);
					}
				}
				ThemeLog.v(TAG,"XUQIAN,ACTION_GET_PRICE...");
				break;	
				
			case ACTION_SET_PRICE:
				setPrice();
				if(mAdapter!=null){
					mAdapter.notifyDataSetChanged();
				}
				
				break;		
			default:
				break;
			}
		}
	};
	
	private ValueAnimator mScrollAnim;
	private float mAnimatorPercent = 0;
	
	@SuppressLint("NewApi")
	public void startScrollAnimation() {
		stopScrollAnimation();
		mScrollAnim = new ValueAnimator();
		mScrollAnim.setDuration(SCROLL_ANIMATION_TIME);
		mScrollAnim.setFloatValues(0.0f, 1.0f);
		mScrollAnim.removeAllUpdateListeners();
		mAnimatorPercent = 0;
		DisplayMetrics metric = new DisplayMetrics();  
		getWindowManager().getDefaultDisplay().getMetrics(metric);  
		final int width = metric.widthPixels;
		mScrollAnim.addUpdateListener(new AnimatorUpdateListener() {
			public void onAnimationUpdate(ValueAnimator animation) {
				final float percent = (Float) animation.getAnimatedValue();
				mIvHelp.scrollBy((int)((percent - mAnimatorPercent) * width), 0);
				mAnimatorPercent = percent;
			}
		});
		mScrollAnim.addListener(new AnimatorListenerAdapter() {
			public void onAnimationEnd(Animator animation) {
				mIvHelp.setCurrentItem(mIvHelp.getCurrentItem() + 1);
				beginAutoScroll();
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub
				mAnimatorPercent = 0;
				super.onAnimationCancel(animation);
			}
			
		});
		mScrollAnim.start();
	}
	
	private void stopScrollAnimation() {
		if (mScrollAnim != null) {
			mScrollAnim.cancel();
		}
	}
	
	private void beginAutoScroll(){
		mHandler.removeMessages(IMAGE_CHANGE);
		mHandler.sendEmptyMessageDelayed(IMAGE_CHANGE, AUTO_SCROLL_TIME);
	}
	public void showLoadingView() {
		android.widget.FrameLayout.LayoutParams lParams = (android.widget.FrameLayout.LayoutParams)mLoadingView.getLayoutParams();
		lParams.bottomMargin = mSystemBarTintManager.getConfig().getNavigationBarHeight();
		lParams.height = mSystemBarTintManager.getConfig().getActionBarHeight();
		lParams.gravity= Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
		mDectorView.bringChildToFront(mLoadingView);
		mLoadingView.setVisibility(View.VISIBLE);
		ImageView imageView = (ImageView)mLoadingView.findViewById(R.id.progressimage);
		AnimationDrawable ad = (AnimationDrawable)imageView.getBackground();
		ad.start();
	}
	
	private void hideLoadingView() {
		ImageView imageView = (ImageView)mLoadingView.findViewById(R.id.progressimage);
		if(imageView!=null){
			AnimationDrawable ad = (AnimationDrawable)imageView.getBackground();
			ad.stop();
		}
		mLoadingView.setVisibility(View.INVISIBLE);
	}
	
	private void initLoadingView() {
		mDectorView = (FrameLayout) getWindow().getDecorView();
		mLoadingView = View.inflate(getApplicationContext(), R.layout.grid_foot_view, null);
		mDectorView.addView(mLoadingView, 0);
		mLoadingView.setVisibility(View.INVISIBLE);
		mSystemBarTintManager = new SystemBarTintManager(this); 
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		TextView msgText;
		switch (id) {
		case DIALOG_UPDATE_AVALIABLE:
			if (mSelfUpdateUrl != null) {
				View view = LayoutInflater.from(this).inflate(R.layout.update_content_view, null);
				msgText = (TextView) view.findViewById(R.id.update_msg);
				msgText.setVisibility(View.VISIBLE);
				builder.setTitle(R.string.dlg_update_or_not_title).setMessage(mSelfUpdateMsg).setView(view).setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						startDownload(mContext, mSelfUpdateUrl);
					}

				}).setNegativeButton(R.string.btn_no, null);

				return builder.create();
			}
			break;
		case DIALOG_UPDATE_NONE:
			View view = LayoutInflater.from(this).inflate(R.layout.update_content_view, null);
			msgText = (TextView) view.findViewById(R.id.update_msg);
			msgText.setVisibility(View.GONE);
			try {
				builder.setTitle(R.string.dlg_update_not_title)
						.setMessage(mContext.getResources().getText(R.string.dlg_update_not_msg) + "v" + (getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName)).setView(view)
						.setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {

							}
						});
			} catch (NotFoundException e) {

				e.printStackTrace();
			} catch (NameNotFoundException e) {

				e.printStackTrace();
			}
			return builder.create();
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
			return loadingDialog;
		}
		return null;
	}
	
	public void changePointView(int cur){
    	LinearLayout pointLinear = (LinearLayout) findViewById(R.id.gallery_point_linear);
    	if (pointLinear == null) {
    		return;
    	}
    	View view = pointLinear.getChildAt(positon);
    	View curView = pointLinear.getChildAt(cur % mADList.size());
    	if(view != null && curView!=null){
    		ImageView pointView = (ImageView)view;
    		ImageView curPointView = (ImageView)curView;
    		pointView.setBackgroundResource(R.drawable.feature_point);
    		curPointView.setBackgroundResource(R.drawable.feature_point_cur);
    		positon = cur % mADList.size();
    	}
    }
	
	protected void showLoadingDialog() {
//		showDialog(DIALOG_LOADING);
		
		mFirstLoadImageView = (ImageView)findViewById(R.id.theme_loading);
		mFirstLoadImageView.setVisibility(View.VISIBLE);
		AnimationDrawable ad = (AnimationDrawable) mFirstLoadImageView.getBackground();
		ad.start();
	}

	protected void hideLoadingDialog() {
		hideLoadingView();
		mFirstLoadImageView.setVisibility(View.INVISIBLE);
		AnimationDrawable ad = (AnimationDrawable) mFirstLoadImageView.getBackground();
		ad.stop();
	}

	private void startDownload(Context context, String downloadUrl) {
		if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			Toast.makeText(getApplicationContext(), getResources().getString(R.string.sd_unmounted), Toast.LENGTH_SHORT);
			return;
		}
		
		String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1);

		// 开始下载
		Uri resource = Uri.parse(downloadUrl);
		DownloadManager.Request request = new DownloadManager.Request(resource);
//            request.setAllowedNetworkTypes(Request.NETWORK_MOBILE | Request.NETWORK_WIFI);   
//            request.setAllowedOverRoaming(false);   
		// 设置文件类型
		MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
		String mimeString = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(downloadUrl));
		request.setMimeType(mimeString);
		// 在通知栏中显示
		request.setShowRunningNotification(true);
		request.setVisibleInDownloadsUi(true);
		// sdcard的目录下的download文件夹
		request.setDestinationInExternalPublicDir(FileManager.APP_DIR_NAME, fileName);
		request.setTitle(fileName);

		long id = downloadManager.enqueue(request);
		SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences("DOWNLOAD_PATH", MODE_PRIVATE).edit();
		editor.putString(id + "", FileManager.APP_DIR_PATH + "/" + fileName);
		editor.commit();
	}

	private AbsListView.OnScrollListener mScrollListener = new AbsListView.OnScrollListener() {
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {

			switch (scrollState) {
			case SCROLL_STATE_IDLE:
				bBusy = false;
				int start = view.getFirstVisiblePosition();
				int counts = view.getChildCount();
				int position = 0;

				ThemeLog.i(TAG, "start = " + start + " count = " + counts + "");
				if ((start + counts) >= (mAdapter.getCount() - Constant.THEME_LIST_COUNT_PER_TIME/2 - 2)) {
					inflateThemeList();
				}
				mAdapter.notifyDataSetChanged();
				break;
			case SCROLL_STATE_TOUCH_SCROLL:
			case SCROLL_STATE_FLING:
				if (!bBusy) {
					clearPendingThumbRequest();
					bBusy = true;
				}
				break;
			default:
				break;
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			if (mSwingBottomInAnimationAdapter != null && visibleItemCount != 0 && !mbSetStartPostion) {
				mSwingBottomInAnimationAdapter.setShouldAnimateFromPosition(visibleItemCount);
				mbSetStartPostion = true;
			}
			return;
		}
	};

	// 显示移动数据流量警告
	private void showNetWarnDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		View view = LayoutInflater.from(this).inflate(R.layout.hint_view, null);
		builder.setView(view);
		final CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox);
		builder.setCancelable(false);
		builder.setNegativeButton(getResources().getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();

				finish();
			}
		});
		builder.setPositiveButton(getResources().getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				onInit();
				grayIV.setVisibility(View.GONE);
				Util.setNetworkHint(ThemeOnlineHomeActivity.this, !checkBox.isChecked());
			}
		});
		final AlertDialog dialog = builder.create();
		dialog.show();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ThemeLog.i(TAG, "++++++++++++++++2015-05-05++++++++++++++++");
		setTheme(com.fineos.internal.R.style.FineOSTheme_Material_Light_WhiteActionBar_NoActionBar_ThemePark);
		setContentView(R.layout.layout_home_online);
		grayIV = (ImageView) findViewById(R.id.grayIV);
		mThemeList = new ArrayList<ThemeData>();
		mADList = new ArrayList<SubjectAdData>();
		aDFlag = false;
		mbFirstEnter = true;
		mContext = this;
		int GoogleBillingSupportId = mContext.getResources().getIdentifier("google_billing_support", "bool", "com.fineos");
		   if(GoogleBillingSupportId <= 0){
			   GoogleBillingSupportId = com.fineos.internal.R.bool.google_billing_support;
		}
		mbGoogleBillingSupport = mContext.getResources().getBoolean(GoogleBillingSupportId);
		ThemeLog.v(TAG, "mbGoogleBillingSupport:"+mbGoogleBillingSupport);
		registerIntentReceivers();
		
		if (!SystemProperties.getBoolean("ro.fineos.net.hide_confirm", ThemeUtils.ISHIDENETDIALOG) && Util.getNetworkHint(this)) {
			grayIV.setVisibility(View.VISIBLE);
			showNetWarnDialog();

			return;
		} else {
			MobclickAgent.updateOnlineConfig(this);
			onInit();
			initBaiduFrontia();
		}
	}
	
	private class TestUpdateRequest implements ApkFlagRequestAgent {

		@Override
		public void updateApkFlags(String apkFlag, String key) {
			// TODO Auto-generated method stub
			if (apkFlag == null || key == null) {
				return;
			}
			SharedPreferences sharedPreferences = getSharedPreferences("theme", Context.MODE_PRIVATE);
			String onlineflag = sharedPreferences.getString(key, Constant.ThemeSwitchKeyDefaultValue.DEFAULT_VALUE);
			if (!onlineflag.equals(apkFlag)) {
				sharedPreferences.edit().putString(key, apkFlag).commit();
			}
			ThemeLog.w(TAG, "updateApkFlags , " + "key :" + key + "apkFlag :" + apkFlag);
		}

		@Override
		public void updateRequest(String arg0) {
			// TODO Auto-generated method stub
			
		}
		
	}

	private void onInit() {
		initLoadingView();
		showLoadingDialog();
		FrontiaApplication.initFrontiaApplication(getApplicationContext());

		mGridView = (HeaderGridView) findViewById(R.id.onlineGridView);
		downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
		bReachEnd = false;
		bInflatingAppList = false;
		nStartIndex = 0;
		ROMApplication.initROMApplication(getApplicationContext());
		ROMCenter.setDebugOn(true);
		ROMCenter.initROMCenter(getApplicationContext(), "theme", ThemeConfig.getInstance().getROMCenterAndOtherChannel(), 
				ThemeConfig.getInstance().getRomCenterType());
		ROMCenter.postApkFlagInfo(new TestUpdateRequest(), Constant.ThemeSwitchKey.ONLINE_THIRDFONTS);
		ROMCenter.postApkFlagInfo(new TestUpdateRequest(), Constant.ThemeSwitchKey.ONLINE_THIRDLOCKSCREEN);
		
		ClientInfo.setUserId(DeviceUtil.getUserId(getApplicationContext()));
		
		String device_token = UmengRegistrar.getRegistrationId(getApplicationContext());
		ThemeLog.e(TAG, "device_token :" + device_token);
		initUmeng();
		mThemeService = ThemeService.getServiceInstance(getApplicationContext());		
		inflateSplashADId();
		initView();
		initEmptyView();
		writeThemeData();
	}
	
	private void initUmeng() {
		
		String device_token = UmengRegistrar.getRegistrationId(getApplicationContext());
		
		AnalyticsConfig.setAppkey(ThemeConfig.getInstance().getUmengKey());
        AnalyticsConfig.setChannel(ThemeConfig.getInstance().getUmengChannel());
        MobclickAgent.updateOnlineConfig(this);
		
		// init umeng_update
        UmengUpdateAgent.setUpdateCheckConfig(false);
		UmengUpdateAgent.setAppkey(ThemeConfig.getInstance().getUmengKey());
		UmengUpdateAgent.setChannel(ThemeConfig.getInstance().getUmengChannel());

		UmengUpdateAgent.setDefault();
		UmengUpdateAgent.update(getApplicationContext());
		UmengUpdateAgent.setAppkey(ThemeConfig.getInstance().getUmengKey());
		UmengUpdateAgent.setUpdateOnlyWifi(false);
        
		// init umeng_push
		PushAgent mPushAgent = PushAgent.getInstance(getApplicationContext());	
		mPushAgent.setAppkeyAndSecret(ThemeConfig.getInstance().getUmengKey(), ThemeConfig.getInstance().getUmengMessageSecret());
		mPushAgent.setMessageChannel(ThemeConfig.getInstance().getUmengChannel());
		mPushAgent.enable();
		PushAgent.getInstance(getApplicationContext()).onAppStart();
		ThemeLog.v(TAG, "onInit mPushAgent="+mPushAgent.isEnabled() + "deviceToken:" + mPushAgent.getRegistrationId());		
	}

	private void initView() {
		mGridView.setOnScrollListener(mScrollListener);
		mGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		int mNumColumns = getResources().getInteger(R.integer.gridviewNumColumns);
		mGridView.setNumColumns(mNumColumns);
		mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() { // 点击进入主题详细
					@Override
					public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
						if (i >= 3) {
							ReportProvider.postUserTheme(ThemeUtils.getpackageName(mAdapter.getItem(i - 3), true), ThemeData.THEME_REPORT_SORT_CLICK);

							Intent intent = new Intent(mContext, ThemeDetailActivity.class);
							Bundle bunble = new Bundle();
							bunble.putSerializable("themeInfo", mAdapter.getItem(i - 3));
							bunble.putInt("mixType", ThemeData.THEME_ELEMENT_TYPE_COMPLETE_THEME);
							bunble.putInt("isOnline", 1);
							intent.putExtras(bunble);
							startActivity(intent);
						}
					}
				});

		View view = LayoutInflater.from(this).inflate(R.layout.layout_home_online_header, null);
		TextView mBtnMixer = (TextView) view.findViewById(R.id.btnMixer);
		TextView mBtnLocal = (TextView) view.findViewById(R.id.btnLocal);
		mDisplay = getWindowManager().getDefaultDisplay();
		screenWidth = mDisplay.getWidth();
		screenHeight = 344;
		mIvHelp = (ADViewpager) view.findViewById(R.id.ivHelp);
		
		List<View> adImgList = new ArrayList<View>();
		View inImg = new ImageView(getApplicationContext());
		inImg.setBackgroundResource(R.drawable.pic);
		adImgList.add(inImg);
		ImageGalleryAdapter imageAdapter = new ImageGalleryAdapter(adImgList);
		mIvHelp.setAdapter(imageAdapter);
//		imageAdapter = new ImageGalleryAdapter(mContext);
//		mIvHelp.setAdapter(imageAdapter);
		mThemeHeaderViewGroup = (ThemeHeaderViewGroup)view.findViewById(R.id.themeheaderviewgroup);
		mThemeHeaderViewGroup.addThemeDrawableChildView(CustomData.loadMixerOnlineIcon(getApplicationContext()),
				mOnClickListener);
		
		mGridView.addHeaderView(view);
		updateFlag = (ImageView) findViewById(R.id.update_flag);
		updateFlag.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				ThemeLog.i(TAG, "updateFlag" + "mSelfUpdateUrl =" + mSelfUpdateUrl);
				
				if (mSelfUpdateUrl != null && mSelfUpdateUrl.length() > 0) {
					showDialog(DIALOG_UPDATE_AVALIABLE);
				} else {
					showDialog(DIALOG_UPDATE_NONE);
				}
			}
		});
		String[] availableThemes = ThemeUtils.getAvailableThemes(Constant.DEFAULT_THEME_PATH);
		ThemeUtils.removeNonExistingThemes(this, availableThemes);
		try {
			inflateThemeADList();
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		ROMCenter.postUpdateInfo(new ThemeUpdateRequest());
	}
	public IUmengRegisterCallback mRegisterCallback = new IUmengRegisterCallback() {
		
		@Override
		public void onRegistered(String registrationId) {
			// TODO Auto-generated method stub
			handler.post(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
				//	updateStatus();
				}
			});
		}
	};
	private void initEmptyView() {
		emptyView = (LinearLayout) findViewById(R.id.emptyGrid);
		sadView = (LinearLayout) findViewById(R.id.sadView);
		sadView.setVisibility(View.GONE);
		ArrayList<ThemeData> tmpThemeList = new ArrayList<ThemeData>();
		mAdapter = new ThemeListAdapter(mContext, tmpThemeList);
		mSwingBottomInAnimationAdapter = ThemeUtils.setAnimationAdapter(mGridView, ThemeUtils.ANIMATION_TIME, mAdapter);
		TextView btnNet = (TextView) emptyView.findViewById(R.id.btnNet);
		TextView btnRetry = (TextView) emptyView.findViewById(R.id.btnRetry);
		btnNet.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent("android.settings.SETTINGS");
				intent.addCategory(Intent.CATEGORY_DEFAULT);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
		});
		btnRetry.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				inflateThemeList();
				if (!mbInflateAdSuccess && !mbFirstEnter) {
					try {
						inflateThemeADList();
					} catch (NameNotFoundException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
	
	OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			onOperateClick((Integer) v.getTag());
		}
	};
	
	private void onOperateClick(Integer id) {
		Intent intent = null;
		switch (id) {
		case CustomData.CUSTOM_ITEM_ICON:
			intent = new Intent(this, ThemeIconActivity.class);
			intent.putExtra("onlineFlag", Constant.THEME_ONLINE_LIST_TYPE);
			startActivitySafely(intent);

			StatService.onEvent(this, "OnlineIconClick", "click");
			break;
		case CustomData.CUSTOM_ITEM_LOCKSCREEN:
			intent = new Intent(this, ThemeLockScreenActivity.class);
			intent.putExtra("onlineFlag", Constant.THEME_ONLINE_LIST_TYPE);
			startActivitySafely(intent);

			StatService.onEvent(this, "OnlineLockScreenClick", "click");
			break;
		case CustomData.CUSTOM_WALLPAPER:
			intent = new Intent(this, ThemeOLWallpaperActivity.class);
			intent.putExtra("onlineFlag", Constant.THEME_ONLINE_LIST_TYPE);
			startActivitySafely(intent);

			StatService.onEvent(this, "OnlineWallpaperClick", "click");
			break;
		case CustomData.CUSTOM_ITEM_LOCAL_THEME:
			intent = new Intent(this, ThemeLocalHomeActivity.class);
			startActivitySafely(intent);
			break;
		case CustomData.CUSTOM_ITEM_FONT:
			intent = new Intent(this, ThemeFontActivity.class);
			intent.putExtra("onlineFlag", Constant.THEME_ONLINE_LIST_TYPE);
			startActivitySafely(intent);

			StatService.onEvent(this, "OnlineFontClick", "click");
			break;
		case CustomData.CUSTOM_ITEM_DYNAMIC_WALLPAPER:
			intent = new Intent(this, ThemeDynamicWallpaper.class);
			intent.putExtra("onlineFlag", Constant.THEME_ONLINE_LIST_TYPE);
			startActivitySafely(intent);

			StatService.onEvent(this, "OnlineDyanmicWallpaper", "click");
			break;
		default:
			break;
		}
	}
	
	private void startActivitySafely (Intent intent) {
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		try {
			this.startActivity(intent);
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}

	private void clearPendingThumbRequest() {
		Iterator<Integer> iterator = mIconStatusMap.keySet().iterator();

		while (iterator.hasNext()) {
			Integer value = iterator.next();
			if (!mIconStatusMap.get(value).booleanValue()) {
				iterator.remove();
			}
		}
		mThemeService.clearThumbRequest(ThemeService.THREAD_THUMB);
	}
	
	private void writeThemeData(){
		String data;
		data = ThemeConfig.getInstance().getRootUri()+"&"+ThemeConfig.getInstance().getROMCenterAndOtherChannel()+"&"
				+ThemeConfig.getInstance().getRomCenterType()+"&"+ThemeConfig.getInstance().getBaiduAppKey()+"&"
				+ThemeConfig.getInstance().getUmengKey()+"&"+ThemeConfig.getInstance().getUmengMessageSecret();
		
		Request request = new Request(0, Constant.TYPE_WRITE_THEME_DATA);
		request.setData(data);
		request.addObserver(new Observer() {

			@Override
			public void update(Observable observable, Object data) {
				
			}
		});
		mThemeService.writeData(request);
	}

	private void inflateThemeList() {
		if (bReachEnd || bInflatingAppList) {
			return;
		}
		bInflatingAppList = true;
		if (mbFirstEnter) {
			hideLoadingView();
			showLoadingDialog();
			mbFirstEnter = false;
		} else {
			showLoadingView();
		}		
		
		GsonGetRequest<ThemeList> mGsonGetRequest = new GsonGetRequest<ThemeList>(
				VolleyURLBuilder.getThemeList(Constant.THEME_LIST_TYPE, nStartIndex),
				ThemeList.class, 
				new Listener<ThemeList>(){
					@Override
					public void onResponse(ThemeList response) {
						// TODO Auto-generated method stub
						if (response != null) {
							Message msg = Message.obtain(mHandler, ACTION_THEME_LIST, response);
							mHandler.sendMessage(msg);
							mbLoadData = true;
						}else{
							mHandler.sendEmptyMessage(ACTION_NETWORK_ERROR);
						}
					}}, 
				new ErrorListener(){
					@Override
					public void onErrorResponse(VolleyError error) {
						// TODO Auto-generated method stub
						mHandler.sendEmptyMessage(ACTION_NETWORK_ERROR);
					}
					});
		ThemeApplication.getInstance().addToRequestQueue(mGsonGetRequest,Constant.TAG_ONLINE_THEME_LIST);
	}

	private void inflateThemeADList()  throws NameNotFoundException {
		ThemeLog.v(TAG, "inflateThemeADList");
		mbLoadAd = true;
		Request request = new Request(0, Constant.TYPE_ONLINE_THEME_AD);
		Object[] params = new Object[4];
		PackageManager manager = mContext.getPackageManager();
		PackageInfo pkgInfo = manager.getPackageInfo(mContext.getPackageName(),0);
		String appPackageName = pkgInfo.packageName;
		//取设置语言对应国家
		String countryCode = mContext.getResources().getConfiguration().locale.getCountry();
		
		Build bd = new Build();
		String phoneModel = bd.MODEL;
		GsonPostRequest<SubjectAdList> mGsonPostRequest = new GsonPostRequest<SubjectAdList>(
				VolleyURLBuilder.postSubjectAd(), 
				VolleyURLBuilder.subjectAdRequestData(appPackageName,phoneModel,countryCode,phoneModel), 
				SubjectAdList.class, 
				new Listener<SubjectAdList>(){
					@Override
					public void onResponse(SubjectAdList response) {
						// TODO Auto-generated method stub
						mbLoadAd = false;
						if (response != null) {
							Message msg = Message.obtain(mHandler, ACTION_THEME_AD, response);
							mHandler.sendMessage(msg);
						}else{
							mHandler.sendEmptyMessage(ACTION_NETWORK_ERROR);
						}
					}
				}, 
				new ErrorListener(){
					@Override
					public void onErrorResponse(VolleyError error) {
						// TODO Auto-generated method stub
						mbInflateAdSuccess = false;
						mHandler.sendEmptyMessage(ACTION_NETWORK_ERROR);
					}
				});
		ThemeApplication.getInstance().addToRequestQueue(mGsonPostRequest,Constant.TAG_ONLINE_THEME_AD);
	}
	
	private void inflateSplashADId() {
		ROMCenter.postSplashADInfo(new ThemeSplashADRequest());
//		Request request = new Request(0, Constant.TYPE_SPLASH_AD_ID);
//		request.addObserver(new Observer() {
//
//			@Override
//			public void update(Observable observable, Object data) {
//				if (data != null) {
//					Message msg = Message.obtain(mHandler, ACTION_SPLASH_AD_ID, data);
//					mHandler.sendMessage(msg);
//				} else {
//					Request request = (Request) observable;
//					if (request.getStatus() == Constant.STATUS_ERROR) {
//					}
//				}
//			}
//		});
//		mCurrentRequest = request;
//		mThemeService.getThemeADList(request);
	}
	
	public void addADThumbnailRequest(final int position, final String adPicUrl, final ThemeADRequesetErr err) {
		
		ImageRequest imageRequest = new ImageRequest(adPicUrl, 
				new Listener<Bitmap>(){
					@Override
					public void onResponse(Bitmap response) {
						// TODO Auto-generated method stub
						
						Image2 icInfo = new Image2();
						icInfo.mAppIcon = response;
						icInfo._id = position;
						Message msg = Message.obtain(mHandler, ACTION_THEME_AD_IMG, icInfo);
						mHandler.sendMessage(msg);
					}}, 
				0, 0, Config.ARGB_8888, 
				new ErrorListener(){
					@Override
					public void onErrorResponse(VolleyError error) {
						// TODO Auto-generated method stub
							if (err.errcount < MAX_THEME_ADD_ERR) {
								addADThumbnailRequest(position, adPicUrl, err);
								ThemeLog.w(TAG, "data is null, now reAddADThumbnailRequest" + "errcount :" + err.errcount );
							}
							err.errcount ++;
					}});
		ThemeApplication.getInstance().addToRequestQueue(imageRequest,Constant.TAG_ONLINE_THEME_ICON);
	}
	
	private class ThemeADRequesetErr {
		
		public int errcount = 0;
	}
	
	public void addThumbnailRequest(int position, final ThemeData theme) {
		
		ImageRequest imageRequest = new ImageRequest(theme.getIconUrl(), 
				new Listener<Bitmap>(){
					@Override
					public void onResponse(Bitmap response) {
						// TODO Auto-generated method stub
						if (response != null) {
							ImageCache.cacheThumbnail(mContext, theme.getId(), theme.getIconUrl(), response);
							mIconStatusMap.put(theme.getId(), true);				
						}
						mHandler.sendEmptyMessage(ACTION_THEME_ICON);
					}}, 
				0, 0, Config.ARGB_8888, 
				new ErrorListener(){
					@Override
					public void onErrorResponse(VolleyError error) {
						// TODO Auto-generated method stub
						
					}});
		ThemeApplication.getInstance().addToRequestQueue(imageRequest,Constant.TAG_ONLINE_THEME_ICON);
	}

	public Drawable getThumbnail(int position, ThemeData theme) {
		ThemeLog.i("ThemeLocalHomeActivity", "position: " + position);		
		boolean bThumbExists = mIconStatusMap.containsKey(Integer.valueOf(position));
		String url = theme.getIconUrl();
		if (bBusy) {
			return ImageCache.getCacheThumbDrawable(mContext, url);
		}
		Drawable drawable = ImageCache.getThumbnail(mContext, url);
		if (drawable == null) {
			boolean bThumbCached = false;
			if (bThumbExists) {
				bThumbCached = mIconStatusMap.get(Integer.valueOf(position)).booleanValue();
			}
			if (bThumbExists && !bThumbCached) {

				return ImageCache.getDefaultIcon(mContext);
			} else {
				mIconStatusMap.put(Integer.valueOf(position), Boolean.valueOf(false));
				addThumbnailRequest(position, theme);
				return ImageCache.getDefaultIcon(mContext);
			}
		} else {

			mIconStatusMap.put(Integer.valueOf(position), Boolean.valueOf(true));
			return drawable;
		}
	}
	
	public Drawable getADThumbnail(int position, SubjectAdData subjectAd) {
		Drawable drawable = null;
		if(mADIconMap.get(position)!=null){
			drawable = new BitmapDrawable(null, mADIconMap.get(position));
		}else{
			ThemeADRequesetErr err = new ThemeADRequesetErr();
			addADThumbnailRequest(position, subjectAd.getPicUrl(), err);
		}
		return drawable;
	}

	private void initBaiduFrontia() {
		ThemeLog.i(TAG, "ThemeOnlineHomeActivity initBaiduFrontia() go");

		// Push: 以apikey的方式登录
		// Push: 如果想基于地理位置推送，可以打开支持地理位置的推送的开关
//		PushManager.startWork(getApplicationContext(), PushConstants.LOGIN_TYPE_API_KEY, "soVfSGHoMDaASNcshSyZ9UbZ");
//		PushManager.enableLbs(getApplicationContext());
		StatService.setAppKey(ThemeConfig.getInstance().getBaiduAppKey());
		StatService.setAppChannel(this, ThemeConfig.getInstance().getROMCenterAndOtherChannel(), true);
		StatService.setOn(this, StatService.EXCEPTION_LOG);
		StatService.setDebugOn(true);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (SystemProperties.getBoolean("ro.fineos.net.hide_confirm", ThemeUtils.ISHIDENETDIALOG) || !Util.getNetworkHint(this)) {
			StatService.onResume(this);
			MobclickAgent.onResume(this);
			ThemeLog.v(TAG, "onResume(),mbLoadData:"+mbLoadData+",setuped ?"+mbSetUpSuccessed);
			if (!mbLoadData) {
				inflateThemeList();
			}
			if(mbGoogleBillingSupport&&!mbSetUpSuccessed){
				addGoogleBillingRequest();
			}
			ThemeLog.w(TAG, "mbLoadAd : " + mbLoadAd + "mbInflateAdSuccess :" + mbInflateAdSuccess);
			if (!mbInflateAdSuccess && !mbLoadAd) {
				try {
					inflateThemeADList();
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}
			}
			beginAutoScroll();
		}
		
		ClientInfo.setUserId(DeviceUtil.getUserId(getApplicationContext()));
	}

	public void onPause() {
		super.onPause();
		if (SystemProperties.getBoolean("ro.fineos.net.hide_confirm", ThemeUtils.ISHIDENETDIALOG) || !Util.getNetworkHint(this)) {
			StatService.onPause(this);
			MobclickAgent.onPause(this);
		}
		stopAutoScroll();
		ClientInfo.setUserId(DeviceUtil.getUserId(getApplicationContext()));
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mThemeService != null) {
			mThemeService.finalize();
		}
		if (mAdapter != null) {
			mAdapter.clear();
			mAdapter.notifyDataSetChanged();
		}
		//for google billing
		if(mbGoogleBillingSupport&&mBillingHelper!=null){
			mBillingHelper.onDestroy();
			mBillingHelper = null ;
		}
		if(mThemeList!=null){
			mThemeList.clear();
		}
		if(mTmpThemeList!=null){
			mTmpThemeList.clear();
		}
		
        if(mIconStatusMap!=null){
            mIconStatusMap.clear();
        }
              
        unbindDrawables(emptyView);
		unbindDrawables(mFirstLoadImageView);
		unbindDrawables(mThemeHeaderViewGroup);
		unbindDrawables(mLoadingView);
        mGridView = null;
		unregisterIntentReceivers();
		CachedThumbnails.flushCache();
//		int size = mListViews.size();
		
		mHandler.removeCallbacksAndMessages(null);
//		for (int i = 0; i < size; i++) {
//			unbindDrawables(mListViews.get(i));
//		}		
		if (mADIconMap != null) {
			Enumeration<Integer> set = mADIconMap.keys();
			while (set.hasMoreElements()) {
				Integer i = set.nextElement();
				mADIconMap.get(i).recycle();
			}
			mADIconMap.clear();
			mADIconMap = null;
		}
//		Process.killProcess(Process.myPid());
	}
	
	private void unbindDrawables(View view) { // remove all childViews when remove widget for gc
		if (view != null) {
			view.destroyDrawingCache();
		}
    	
    	if (view.getBackground() != null) {
			view.getBackground().setCallback(null);						
		}
		
		if(view instanceof ImageView){
			if(((ImageView) view).getDrawable() != null){
				((ImageView) view).getDrawable().setCallback(null);
			}
		}else if(view instanceof AdapterView){
			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
				unbindDrawables(((ViewGroup) view).getChildAt(i));
			}
		}else if (view instanceof ViewGroup) {
			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
				unbindDrawables(((ViewGroup) view).getChildAt(i));
			}
			
			((ViewGroup) view).removeAllViews();
		}
    }
	
	///xuqian add for billing begin
		@Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
			// TODO Auto-generated method stub
			ThemeLog.v(TAG,"onActivityResult");
			if(mbGoogleBillingSupport&&mBillingHelper!=null&&!mBillingHelper.onActivityResult(requestCode, resultCode, data)){
				ThemeLog.v(TAG,"onActivityResult fail handle to super");
				super.onActivityResult(requestCode, resultCode, data);
			}
			
		}
		
		private void setPriceAsy(){
			Message msg = Message.obtain(mHandler, ACTION_SET_PRICE, null);
			mHandler.sendMessageDelayed(msg, 0);
		}
		private void setPrice(){
			   HashMap<String, String> priceMaps = mPriceMap;
			   int mThemeListSize = mThemeList.size();
				for (int j=0;j<mThemeListSize;j++){
					ThemeData theme = mThemeList.get(j);
					ThemeData theme2 = mTmpThemeList.get(j);
					String sID = String.valueOf(theme.getId());
					String sPrice = priceMaps.get(sID);   // frome google
					
					boolean isNeedRestore = false ;
					boolean isBuyed = false ;
                    if(mBillingHelper!=null){
                       isNeedRestore = mBillingHelper.isNeedRestore(sID);
					   isBuyed = mBillingHelper.isBuyed(sID);
                    }

					
					ThemeLog.v(TAG,"Sku :"+sID+",isNeedRestore :"+isNeedRestore+",isBuyed:"+isBuyed);
					
					// if buyed ,download directly
					if(isNeedRestore||isBuyed){   //give product to user
						theme.setType(ThemeData.THEME_PACKAGE_TYPE_FREE);
						theme2.setType(ThemeData.THEME_PACKAGE_TYPE_FREE);
					}else if(sPrice==null||"".equals(sPrice)){
						theme.setType(ThemeData.THEME_PACKAGE_TYPE_FREE);
						theme2.setType(ThemeData.THEME_PACKAGE_TYPE_FREE);
					}else{
						theme.setType(ThemeData.THEME_PACKAGE_TYPE_FOR_SALE);
						theme2.setType(ThemeData.THEME_PACKAGE_TYPE_FOR_SALE);
					}
					
					ThemeLog.v(TAG, "sID:"+sID+",sPrice:"+sPrice);
					theme.setsPrice(sPrice);          //sPrice from google ;fPrice from local 
					theme2.setsPrice(sPrice);
				}
				mbFirstEnterBilling= false ;
		   }

         private void addGoogleBillingRequest() {
    		
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
    		
    		ThemeLog.v(TAG,"addGoogleBillingRequest(),mFirstEnter:"+mbFirstEnterBilling);
    		mHandler.removeMessages(ACTION_START_GOOGLE_BILLING);
    		Message msg = Message.obtain(mHandler, ACTION_START_GOOGLE_BILLING, request);
    		mHandler.sendMessageDelayed(msg, 0);
    		
    		
    	}
        
       private void addGetPriceRequest() {
    		
    		Request request = new Request(0, Constant.TYPE_GET_PRICE);
    		request.setData(mSkuList);
    		request.addObserver(new Observer() {

    			@Override
    			public void update(Observable observable, Object data) {
    				// TODO Auto-generated method stub
    				Request request = (Request) observable;
    				ThemeLog.v(TAG, "request:"+request+",STATUS:"+request.getStatus());
    				switch (request.getStatus()) {
    				case Constant.STATUS_SUCCESS:
    				
    					mPriceMap = (HashMap<String, String>)data;
    		    		setPriceAsy();
    					break;
    				case Constant.STATUS_ERROR:
    					
    					break;
    				default:
    					break;
    				}
    			}
    		});
    		
    		mHandler.removeMessages(ACTION_GET_PRICE);
    		Message msg = Message.obtain(mHandler, ACTION_GET_PRICE, request);
    	   ThemeLog.v(TAG,"addGetPriceRequest(),mFirstEnter:"+mbFirstEnterBilling);
    		mHandler.sendMessageDelayed(msg, 0);
    		
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
	
	@Override
	public void startActivity(Intent intent) {
		super.startActivity(intent);
		
		//add activity switch animation
		overridePendingTransition(com.fineos.R.anim.slide_in_right, com.fineos.R.anim.slide_out_left);
	}

	private class ThemeUpdateRequest implements UpdateRequestAgent {

		@Override
		public void updateRequest(VersionUpgrade vp) {
			ThemeLog.i(TAG, "ThemeUpdateRequest ");
			if (vp != null) {
				ThemeLog.i(TAG, "ThemeUpdateRequest vp != null: "+ vp.getDownUrl());
				mSelfUpdateUrl = vp.getDownUrl();
				mSelfUpdateMsg = vp.getUpgradeNote();
				updateFlag.setImageResource(R.drawable.ic_newupdate);
			}
		}

	}
	
	private class ThemeSplashADRequest implements SplashADRequestAgent {

		@Override
		public void SplashADRequest(SplashAd splashAd) {
			// TODO Auto-generated method stub
			if(splashAd!=null){
				ThemeLog.v(TAG, "SplashActivity ACTION_SPLASH_AD_ID splashAD ="+splashAd);
				SharedPreferences sharedPreferences = getSharedPreferences("theme", Context.MODE_PRIVATE); //私有数据
				Editor editor = sharedPreferences.edit();
				editor.putString("splashAd", splashAd.getAdId());
				editor.commit();
				
				SharedPreferences sharedPreferences1 = getSharedPreferences("theme", Context.MODE_PRIVATE); //私有数据
				Editor editor1 = sharedPreferences1.edit();
				editor1.putString("splashAdAdvertiser", splashAd.getAdvertiser());
				editor1.commit();
			}
		}

	}
	
	public class ImageGalleryAdapter extends PagerAdapter {
		public List<View> mListViews;

		public ImageGalleryAdapter(List<View> mListViews) {
			this.mListViews = mListViews;
			int j=0;
			int mADListSize = mADList.size();
			for (int i = 0; i < mADListSize; i++) {
				
				if (mADList.get(i).getType() == 1) {
					if(adList!=null&&adList.size()>0){
						while(j<adList.size()){
							adList.get(j).registerViewForInteraction(mListViews.get(i));
							j++;
							break;
						}
					}
				}
			}
			
		}
		
		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
//			((ViewPager) arg0).removeView(mListViews.get(arg1%mListViews.size()));
		}

		@Override
		public void finishUpdate(View arg0) {
		}

		@Override
		public int getCount() {
			if(mListViews.size()>1){
				return Integer.MAX_VALUE;
			}else{
				return mListViews.size();
			}
			
		}

		@Override
		public Object instantiateItem(View arg0, int arg1) {
			if(mListViews.get(arg1%mListViews.size()).getParent()==null){
				((ViewPager)arg0).addView(mListViews.get(arg1%mListViews.size()), 0);
            }
            else {
                ((ViewGroup)mListViews.get(arg1%mListViews.size()).getParent()).removeView(mListViews.get(arg1%mListViews.size()));
                ((ViewPager)arg0).addView(mListViews.get(arg1%mListViews.size()), 0);
            }
			
			return mListViews.get(arg1%mListViews.size());
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
	
	public class ViewPagerScroller extends Scroller {
	    private int mScrollDuration = 2000;             // 滑动速度
	    
	    /**
	     * 设置速度速度
	     * @param duration
	     */
	    public void setScrollDuration(int duration){
	        this.mScrollDuration = duration;
	    }
	     
	    public ViewPagerScroller(Context context) {
	        super(context);
	    }
	 
	    public ViewPagerScroller(Context context, Interpolator interpolator) {
	        super(context, interpolator);
	    }
	 
	    public ViewPagerScroller(Context context, Interpolator interpolator, boolean flywheel) {
	        super(context, interpolator, flywheel);
	    }
	 
	    @Override
	    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
	        super.startScroll(startX, startY, dx, dy, mScrollDuration);
	    }
	 
	    @Override
	    public void startScroll(int startX, int startY, int dx, int dy) {
	        super.startScroll(startX, startY, dx, dy, mScrollDuration);
	    }
	 
	     
	     
	    public void initViewPagerScroll(ViewPager viewPager) {
	        try {
	            Field mScroller = ViewPager.class.getDeclaredField("mScroller");
	            mScroller.setAccessible(true);
	            mScroller.set(viewPager, this);
	        } catch(Exception e) {
	            e.printStackTrace();
	        }
	    }
	}
	
	private void loadADImg(){
			int mADListSize = mADList.size();
			for (int i = 0; i < mADListSize; i++) {
				getADThumbnail(i, mADList.get(i));
			}
	}
	
	private OnClickListener mAdclickListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			// TODO Auto-generated method stub
			performAdclick(view);
		}
	};
	
	private void performAdclick(View view) {
		int nAppIndex = ((Integer)view.getTag()).intValue();
		StatService.onEvent(mContext, "ADonClick", mADList.get(nAppIndex).getADTitle(), 1);
		if (nAppIndex >= 0) {
			if(mADList.get(nAppIndex).getType() == 0) {
				switch (mADList.get(nAppIndex).getTheme().getThemeType()){
				case 0://主题
					Intent intent = new Intent(mContext, ThemeDetailActivity.class);
					Bundle bunble = new Bundle();
					bunble.putSerializable("themeInfo", new ThemeData(mADList.get(nAppIndex).getTheme(), ThemeOnlineHomeActivity.this));
					bunble.putInt("mixType", ThemeData.THEME_ELEMENT_TYPE_COMPLETE_THEME);
					bunble.putInt("isOnline", 1);
					intent.putExtras(bunble);
					startActivity(intent);
					break;
				case 1://锁屏
					intent = new Intent(mContext, ThemeDetailNewLockscreenActivity.class);
					bunble = new Bundle();
					ThemeDataCache.clearTypeTheme(ThemeData.THEME_ELEMENT_TYPE_LOCKSCREEN);
					ThemeDataCache.cacheTheme(new ThemeData(mADList.get(nAppIndex).getTheme(), ThemeOnlineHomeActivity.this), ThemeData.THEME_ELEMENT_TYPE_LOCKSCREEN);
					bunble.putSerializable("themeInfo", new ThemeData(mADList.get(nAppIndex).getTheme(),ThemeOnlineHomeActivity.this));
					bunble.putInt("mixType", ThemeData.THEME_ELEMENT_TYPE_LOCKSCREEN);
					bunble.putInt("isOnline", 1);
					bunble.putInt("currentthemeposition", 0);
					intent.putExtras(bunble);
					startActivity(intent);
					break;
				case 2://动态壁纸
					intent = new Intent(mContext, ThemeDetailNewDynamicWallpaperActivity.class);
					bunble = new Bundle();
					ThemeDataCache.clearTypeTheme(ThemeData.THEME_ELEMENT_TYPE_DYNAMIC_WALLPAPER);
					ThemeDataCache.cacheTheme(new ThemeData(mADList.get(nAppIndex).getTheme(), ThemeOnlineHomeActivity.this), ThemeData.THEME_ELEMENT_TYPE_DYNAMIC_WALLPAPER);
					bunble.putSerializable("themeInfo", new ThemeData(mADList.get(nAppIndex).getTheme(), ThemeOnlineHomeActivity.this));
					bunble.putInt("mixType", ThemeData.THEME_ELEMENT_TYPE_DYNAMIC_WALLPAPER);
					bunble.putInt("isOnline", 1);
					bunble.putInt("currentthemeposition", 0);
					intent.putExtras(bunble);
					startActivity(intent);
					break;
				case 3://字体
					intent = new Intent(mContext, ThemeDetailFontsActivity.class);
					bunble = new Bundle();
					bunble.putSerializable("themeInfo", new ThemeData(mADList.get(nAppIndex).getTheme(), ThemeOnlineHomeActivity.this));
					bunble.putInt("mixType", ThemeData.THEME_ELEMENT_TYPE_COMPLETE_THEME);
					bunble.putInt("isOnline", 1);
					bunble.putInt("currentthemeposition", 0);
					intent.putExtras(bunble);
					startActivity(intent);
					break;
				case 5://壁纸
					intent = new Intent(mContext, ThemeDetailNewWallpaperActivity.class);
					bunble = new Bundle();
					ThemeDataCache.clearTypeTheme(ThemeData.THEME_ELEMENT_TYPE_WALLPAPER);
					ThemeDataCache.cacheTheme(new ThemeData(mADList.get(nAppIndex).getTheme(), ThemeOnlineHomeActivity.this), ThemeData.THEME_ELEMENT_TYPE_WALLPAPER);
					bunble.putSerializable("themeInfo", new ThemeData(mADList.get(nAppIndex).getTheme(), ThemeOnlineHomeActivity.this));
					bunble.putInt("mixType", ThemeData.THEME_ELEMENT_TYPE_WALLPAPER);
					bunble.putInt("isOnline", 1);
					bunble.putInt("currentthemeposition", 0);
					intent.putExtras(bunble);
					startActivity(intent);
					break;
				}
				
			}
			if(mADList.get(nAppIndex).getType() == 2){
				Intent intent = new Intent(mContext, TabWebActivity.class);
				intent.putExtra("bUrl", mADList.get(nAppIndex).getWebUrl());
				intent.putExtra("title",mADList.get(nAppIndex).getADTitle());
				startActivity(intent);
			}
		}
	}
	
	public void loadAD(String nativePosID,final int count) {
//	    if (nativeAD == null) {
//	      this.nativeAD = new NativeAD(this, Constant.THEME_AD_APPID, nativePosID, this);
//	    }
//	    nativeAD.loadAD(count);
		 ThemeLog.v(TAG, "ssss onADLoaded count =" + count);
		 nativeAdManager = new NativeAdManager(mContext, new NativeAdListener(){

			@Override
			public void adClicked(CMNativeAd arg0) {
				// TODO Auto-generated method stub
			}

			@Override
			public void adFailedToLoad() {
				// TODO Auto-generated method stub
				ThemeLog.v(TAG, "adFailedToLoad");
				int mADListSize = mADList.size();
				for (int i = 0; i < mADListSize; i++) {
					if (mADList.get(i).getType() == 1) {
						
						mADList.remove(i);
						mADListSize--;
						i--;
					}else{
						getADThumbnail(i, mADList.get(i));
					}
				}
			}

			@Override
			public void adLoaded() {
				// TODO Auto-generated method stub
				ThemeLog.v(TAG, "adLoaded");
				int j = 0;
				adList = nativeAdManager.getAdList(adPosId,count);
				int mADListSize = mADList.size();
				for (int i = 0; i < mADListSize; i++) {
					
				if (mADList.get(i).getType() == 1) {
				
				if(adList!=null&&adList.size()>0){
					while(j<adList.size()){
						mADList.get(i).setADTitle(adList.get(j).getAdTitle());
						mADList.get(i).setPicUrl(adList.get(j).getAdCoverImageUrl());
						j++;
						break;
					}
					}
					}
				}
				for (int i = 0; i < mADListSize; i++) {
					getADThumbnail(i, mADList.get(i));
				}
			}});
		 nativeAdManager.requestAd(adPosId);
		 /*	///////////inmobi
		for (int i = 0; i < mADList.size(); i++) {
			
			if (mADList.get(i).getType() == 1) {
				
			final int position = i;
		InMobiNative nativeAd = new InMobiNative(Long.valueOf(nativePosID), new InMobiNative.NativeAdListener() {
			InMobiNative nativeAd = new InMobiNative(1443673329639L, new InMobiNative.NativeAdListener() {
	            @Override
	            public void onAdLoadSucceeded(final InMobiNative inMobiNative) {
	                try {
	                    JSONObject content = new JSONObject((String) inMobiNative.getAdContent());
	                    
	                    mADList.get(position).setPicUrl(content.getJSONObject("screenshots").getString("url"));
	                    ThemeLog.v(TAG, "onADLoaded setPicUrl =" + content.getJSONObject("screenshots").getString("url"));
	    				mADList.get(position).setADTitle(content.getString("title"));
	    				ThemeLog.v(TAG, "onADLoaded setADTitle =" + content.getString("title"));
	                } catch (JSONException e) {
	                }
	            }

	            @Override
	            public void onAdLoadFailed(InMobiNative inMobiNative, InMobiAdRequestStatus inMobiAdRequestStatus) {
	            	ThemeLog.v(TAG, "onADLoaded Failed to load ad. " + inMobiAdRequestStatus.getMessage());
	            }

	            @Override
	            public void onAdDismissed(InMobiNative inMobiNative) {

	            }

	            @Override
	            public void onAdDisplayed(InMobiNative inMobiNative) {

	            }

	            @Override
	            public void onUserLeftApplication(InMobiNative inMobiNative) {

	            }
	        });
	        nativeAd.load();
		}*/
	}
	/*  ////////广点通
	@Override
	public void onADLoaded(List<NativeADDataRef> arg0) {
		// TODO Auto-generated method stub
		ThemeLog.v(TAG, "onADLoaded ="+arg0.size());
		if (arg0.size() > 0) {
			for (int j = 0; j < arg0.size(); j++) {
				adItem = arg0.get(j);
				for (int i = 0; i < mADList.size(); i++) {
					if (mADList.get(i).getType() == 1) {
						ThemeLog.v(TAG, "onADLoaded =" + adItem.getTitle());
						mADList.get(i).setADTitle(adItem.getTitle());
						ThemeLog.v(TAG, "onADLoaded =" + adItem.getImgUrl());
						mADList.get(i).setPicUrl(adItem.getImgUrl());
					}
				}
			}
			for (int i = 0; i < mADList.size(); i++) {
				getADThumbnail(i, mADList.get(i));
			}
		} else {
			ThemeLog.i("AD_DEMO", "NOADReturn");
		}
	}

	@Override
	public void onADStatusChanged(NativeADDataRef arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNoAD(int arg0) {
		// TODO Auto-generated method stub
		
	}*/
}
