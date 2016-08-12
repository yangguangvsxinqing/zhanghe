package com.huaqin.market.ui;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.huaqin.android.market.sdk.bean.Application;
import com.huaqin.market.R;
import com.huaqin.market.SlideViewPager;
import com.huaqin.market.model.Application2;
import com.huaqin.market.model.Image3;
import com.huaqin.market.utils.CachedThumbnails;
import com.huaqin.market.utils.Constant;
import com.huaqin.market.utils.GlobalUtil;
import com.huaqin.market.webservice.IMarketService;
import com.huaqin.market.webservice.MarketService;
import com.huaqin.market.webservice.Request;

public class AppInfoDetailActivity extends Activity {

	private static final int ACTION_APP_PREVIEW = 0;
	private static final int ACTION_APP_RELATE = 1;
	
	private static final int ACTION_NETWORK_ERROR = 2;
	
	private static final int APP_INFO_DESCRIPTION_LINES = 3;
	
	private static final int ACTION_APP_ICON = 4;
	
	private View mPreviewLayout;
	private View mLoadingIndicator;
	private MarketGallery mPreviewGallery;
	private PreviewGalleryAdapter mAdapter;
	private TextView mPreviewIndicator;
	private ArrayList<Drawable> mPreviews;
	private ArrayList<ImageView> mPreRelateviews;
	private AnimationDrawable smallLoadingAnimation;
	private final BroadcastReceiver mApplicationsFreeReceiver;
	private TextView[] relateText;
	private boolean moreFlag;
	//private RatingBar[] relateRatingBar;
	
	private Application2 mAppInfo;
	
	private IMarketService mMarketService;
	private Handler mHandler;
	
	/*************Added-s by JimmyJin for Pudding Project**************/
	private Display mDisplay;
	public static int screenWidth = 0;
	public static int screenHeight = 0;
	/*************Added-e by JimmyJin for Pudding Project**************/
	
//	private int nAppDownload;
	private ArrayList<Application> appList;
//	private int type;
	
	public AppInfoDetailActivity() {
		relateText = new TextView[4];
		moreFlag = true;
//		relateRatingBar = new RatingBar[4];
		appList = new ArrayList<Application>();
		mPreviews = new ArrayList<Drawable>();
		mPreRelateviews = new ArrayList<ImageView>();
		mApplicationsFreeReceiver = new BroadcastReceiver(){

			@Override
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();
				if(action.equals(AppInfoActivity.ACTION_DETAIL_FREE)){	
					onDestroy();
				}
			}
		};
	}

	private void initHandlers() {
		// TODO Auto-generated method stub
		mHandler = new Handler() {

			@SuppressWarnings("unchecked")
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch (msg.what) {
				case ACTION_APP_PREVIEW:					
					ArrayList<Drawable> prevList = (ArrayList<Drawable>) msg.obj;
					// clear old previews
					Log.v("ACTION_APP_PREVIEW", "prevList ="+prevList);
					if(mPreviews != null){
						mPreviews.clear();
					}
					if(null != prevList){
						mPreviews.addAll(prevList);
						// release prevList
						prevList.clear();
					}
					
					prevList = null;
					
					// free old mAdapter
					if (mAdapter != null) {
						mAdapter = null;
					}
					mAdapter = new PreviewGalleryAdapter(AppInfoDetailActivity.this);
					if(mPreviewGallery != null){
						mPreviewGallery.setAdapter(mAdapter);
					}
					
					if(mPreviews.size()>=2)
					{						
						mPreviewGallery.setSelection((mPreviews.size()*10000000));
					}
					
					mLoadingIndicator.setVisibility(View.GONE);
					mPreviewLayout.setVisibility(View.VISIBLE);
					
					break;
				case ACTION_APP_ICON:
					
					Image3 icInfo = (Image3) msg.obj;
					if (icInfo.mAppIcon != null) {
						CachedThumbnails.cacheThumbnail(
								AppInfoDetailActivity.this,
								icInfo._id, icInfo.mAppIcon);
						
						Drawable drawable = icInfo.mAppIcon;
						if(mPreRelateviews != null){
							switch (icInfo.postion)
							{
							case 0:
								if(drawable != null)
								{
									mPreRelateviews.get(0).setImageDrawable(drawable);
								}
								break;
							case 1:
								if(drawable != null)
								{
									mPreRelateviews.get(1).setImageDrawable(drawable);
								}
								break;
							case 2:
								if(drawable != null)
								{
									mPreRelateviews.get(2).setImageDrawable(drawable);
								}
								break;
							case 3:
								if(drawable != null)
								{
									mPreRelateviews.get(3).setImageDrawable(drawable);
								}
								break;
							default:
								break;
							}
						}	
					}
					break;
				case ACTION_APP_RELATE:
					
					appList = (ArrayList<Application>) msg.obj;
					relateText[0] = (TextView)findViewById(R.id.menu_about_text1);
					relateText[1] = (TextView)findViewById(R.id.menu_about_text2);
					relateText[2] = (TextView)findViewById(R.id.menu_about_text3);
					relateText[3] = (TextView)findViewById(R.id.menu_about_text4);
//					relateRatingBar[0] = (RatingBar)findViewById(R.id.relate_appinfo_rating1);
//					relateRatingBar[1] = (RatingBar)findViewById(R.id.relate_appinfo_rating2);
//					relateRatingBar[2] = (RatingBar)findViewById(R.id.relate_appinfo_rating3);
//					relateRatingBar[3] = (RatingBar)findViewById(R.id.relate_appinfo_rating4);
		//			Log.v("asd", "relateRatingBar = "+relateRatingBar);
					for(int i = 0;i<appList.size();i++)
					{
						String iconUrl = appList.get(i).getIconUrl();
						if(iconUrl != null){
							addThumbnailRequest(appList.get(i).getAppId(),appList.get(i).getIconUrl(),i);
							relateText[i].setText(appList.get(i).getAppName());
//							Log.v("asd", "relateRatingBar = "+appList.get(i).getStars());
//							relateRatingBar[i].setRating(appList.get(i).getStars());
						}
					}
					LinearLayout convertView[] = new LinearLayout[4];
					convertView[0] = (LinearLayout)findViewById(R.id.relate_area1);
					convertView[0].setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View view) {
							// TODO Auto-generated method stub
//							Intent intent = new Intent(AppInfoDetailActivity.this, AppInfoPreloadActivity.class);
							Intent intent = new Intent(AppInfoDetailActivity.this, AppInfoActivity.class);
							intent.putExtra("appInfo",new Application2(appList.get(0)));
							intent.putExtra("appId", appList.get(0).getAppId());
							/*************Added-s by JimmyJin for Pudding Project**************/
							intent.putExtra("type",0);
							/*************Added-s by JimmyJin for Pudding Project**************/
							intent.putExtra("download", appList.get(0).getTotalDownloads());
							startActivity(intent);
						}
					});
					
					convertView[1] = (LinearLayout)findViewById(R.id.relate_area2);
					convertView[1].setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View view) {
							// TODO Auto-generated method stub
//							Intent intent = new Intent(AppInfoDetailActivity.this, AppInfoPreloadActivity.class);
							Intent intent = new Intent(AppInfoDetailActivity.this, AppInfoActivity.class);
							intent.putExtra("appInfo",new Application2(appList.get(1)));
							intent.putExtra("appId", appList.get(1).getAppId());
							/*************Added-s by JimmyJin for Pudding Project**************/
							intent.putExtra("type",0);
							/*************Added-s by JimmyJin for Pudding Project**************/
							intent.putExtra("download", appList.get(1).getTotalDownloads());
							startActivity(intent);
						}
					});
					
					convertView[2] = (LinearLayout)findViewById(R.id.relate_area3);
					convertView[2].setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View view) {
							// TODO Auto-generated method stub
//							Intent intent = new Intent(AppInfoDetailActivity.this, AppInfoPreloadActivity.class);
							Intent intent = new Intent(AppInfoDetailActivity.this, AppInfoActivity.class);
							intent.putExtra("appInfo",new Application2(appList.get(2)));
							intent.putExtra("appId", appList.get(2).getAppId());
							/*************Added-s by JimmyJin for Pudding Project**************/
							intent.putExtra("type",0);
							/*************Added-s by JimmyJin for Pudding Project**************/
							intent.putExtra("download", appList.get(2).getTotalDownloads());
							startActivity(intent);
						}
					});
					
					convertView[3] = (LinearLayout)findViewById(R.id.relate_area4);
					convertView[3].setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View view) {
							// TODO Auto-generated method stub
//							Intent intent = new Intent(AppInfoDetailActivity.this, AppInfoPreloadActivity.class);
							Intent intent = new Intent(AppInfoDetailActivity.this, AppInfoActivity.class);
							intent.putExtra("appInfo",new Application2(appList.get(3)));
							intent.putExtra("appId", appList.get(3).getAppId());
							/*************Added-s by JimmyJin for Pudding Project**************/
							intent.putExtra("type",0);
							/*************Added-s by JimmyJin for Pudding Project**************/
							intent.putExtra("download", appList.get(3).getTotalDownloads());
							startActivity(intent);
						}
					});
					for(int i = 0;i<appList.size();i++)
					{
						if(appList.get(i).getAppName()!=null){
							convertView[i].setVisibility(View.VISIBLE);
						}
					}
					break;
				case ACTION_NETWORK_ERROR:
					mLoadingIndicator.setVisibility(View.GONE);
					mPreviewLayout.setVisibility(View.VISIBLE);
					
					// When failed to get previews
					// do not display network error dialog
					break;
				default:
					break;
				}
			}
		};
	}

	private void initViews() {
		// TODO Auto-generated method stub	
		initAppDetails();
		
		mLoadingIndicator = findViewById(R.id.appinfo_loading_indicator);
//		Log.e("AppInfo", "mLoadingIndicator=" + mLoadingIndicator); 
		ImageView loadingSmall = 
			(ImageView) mLoadingIndicator.findViewById(R.id.appinfo_small_loading);
		loadingSmall.setBackgroundResource(R.anim.loading_anim);
		smallLoadingAnimation = (AnimationDrawable) loadingSmall.getBackground();
		loadingSmall.post(new Runnable(){
			@Override     
			public void run() {
				smallLoadingAnimation.start();     
			}                     
		});
		
		mPreviewLayout = findViewById(R.id.appinfo_preview_layout);
		mLoadingIndicator.setVisibility(View.VISIBLE);
		mPreviewLayout.setVisibility(View.GONE);
		
		mPreviewGallery = (MarketGallery) findViewById(R.id.appinfo_preview_gallery);
		
		mPreviewIndicator = (TextView) findViewById(R.id.appinfo_preview_indicator);
		
		mPreviewGallery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if(0 != mPreviews.size()){//Added by JimmyJin
					StringBuilder builder = new StringBuilder(String.valueOf((position%mPreviews.size() + 1)));
					builder.append("-");
					builder.append(mPreviews.size());
					mPreviewIndicator.setText(builder.toString());
				}
			}
			
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				return;
			}
		});
		mPreviewGallery.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
			    switch (event.getAction()) {
			    case MotionEvent.ACTION_MOVE: 
			    	AppInfoActivity.amPager.requestDisallowInterceptTouchEvent(true);
			        break;
			    case MotionEvent.ACTION_UP:
			    	AppInfoActivity.amPager.requestDisallowInterceptTouchEvent(false);
			    	break;
			    case MotionEvent.ACTION_DOWN:
			    	AppInfoActivity.amPager.requestDisallowInterceptTouchEvent(true);
			        break;
			    case MotionEvent.ACTION_CANCEL:
			    	AppInfoActivity.amPager.requestDisallowInterceptTouchEvent(false);
			        break;
			    }
				return false;
			}
        });
		View emptyView = findViewById(R.id.appinfo_preview_empty);
		TextView emptyTextView = (TextView)emptyView.findViewById(R.id.appinfo_preview_empty_text);
		emptyTextView.setVisibility(View.VISIBLE);
		mPreviewGallery.setEmptyView(emptyView);

/****************begin,modified by daniel_whj,2012-02-11,for bug HQ00080515******************/		
/*
	remove permission views including title and cotent .but keep permission info for installer
*/
		TextView tvPermTitle = (TextView)findViewById(R.id.appinfo_detail_permission_title);
		tvPermTitle.setVisibility(View.GONE);
		
		LinearLayout permList =
			(LinearLayout) findViewById(R.id.appinfo_permission_view);
		AppSecurityPermissions appPerm =
			new AppSecurityPermissions(this, mAppInfo.getPermissions());
		View permView = appPerm.getPermissionsView();
		
		//permList.addView(permView);
		
/****************end,modified by daniel_whj,2012-02-11,for bug HQ00080515******************/

		addPreviewRequest();
		addAppRelateRequest();
	}
	private void registerIntentReceivers() {
		// TODO Auto-generated method stub	
		IntentFilter intentFilterFree = new IntentFilter(AppInfoActivity.ACTION_DETAIL_FREE);
		registerReceiver(mApplicationsFreeReceiver, intentFilterFree);
		
	}
	
	private void unregisterReceivers(){
		unregisterReceiver(mApplicationsFreeReceiver);
	}
	private void initAppDetails() {
		// TODO Auto-generated method stub
		StringBuilder builder = new StringBuilder();

		mPreRelateviews.add((ImageView)findViewById(R.id.menu_about1));
		mPreRelateviews.add((ImageView)findViewById(R.id.menu_about2));
		mPreRelateviews.add((ImageView)findViewById(R.id.menu_about3));
		mPreRelateviews.add((ImageView)findViewById(R.id.menu_about4));
		// Version
		TextView tvVersion = (TextView) findViewById(R.id.tv_appinfo_version);
		builder.append(getString(R.string.app_version));
		builder.append(mAppInfo.getVersionName());
		tvVersion.setText(builder.toString());
		
		// Size
		builder.setLength(0);
		TextView tvSize = (TextView) findViewById(R.id.tv_appinfo_size);
		builder.append(getString(R.string.app_size));
		builder.append(GlobalUtil.getSize(mAppInfo.getSize()));
		tvSize.setText(builder.toString());
		
		// Update time
		builder.setLength(0);
		TextView tvUpdate = (TextView) findViewById(R.id.tv_appinfo_updatetime);
		builder.append(getString(R.string.app_lastupdate));
		builder.append(GlobalUtil.getDateByFormat(
				mAppInfo.getReleaseDate(), "yyyy-MM-dd")
		);
		tvUpdate.setText(builder.toString());
		
		// Download count
		builder.setLength(0);
		TextView tvDownloads = (TextView) findViewById(R.id.tv_appinfo_downloads);
		builder.append(getString(R.string.app_downlods));
		if(mAppInfo.getDownloads() < 10000){
			builder.append(mAppInfo.getDownloads());
		}else{
			builder.append(mAppInfo.getDownloads()/10000);
			builder.append(getString(R.string.app_downlods_more));
		}
		
		tvDownloads.setText(builder.toString());
		
		// Description
		TextView tvDesc = (TextView) findViewById(R.id.tv_appinfo_description);
		Button IvDescMore=(Button) findViewById(R.id.iv_appinfo_more);
		
		String strDesc=mAppInfo.getAppDesc();
		tvDesc.setText(strDesc);
		Rect StrDescBounds = new Rect();		
		tvDesc.getPaint().getTextBounds(strDesc, 0, strDesc.length(), StrDescBounds);
		
		DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);        
				
		int linecount=StrDescBounds.width()/(int)(dm.widthPixels*dm.density)+1;	
		
		if(linecount<APP_INFO_DESCRIPTION_LINES)
		{
			IvDescMore.setVisibility(View.GONE);
		}
		else
		{
			tvDesc.setMaxLines(3);
			
			if(IvDescMore!=null)
			{
				IvDescMore.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						// TODO Auto-generated method stub
						if(moreFlag){
							TextView tvAppInfoDesc = (TextView) findViewById(R.id.tv_appinfo_description);
							tvAppInfoDesc.setMaxLines(tvAppInfoDesc.getLineCount());
							Button IvDescMore=(Button) findViewById(R.id.iv_appinfo_more);
							IvDescMore.setText(R.string.appinfo_close);
							moreFlag = false;
//							IvDescMore.setVisibility(View.GONE);
						}else{
							TextView tvAppInfoDesc = (TextView) findViewById(R.id.tv_appinfo_description);
							tvAppInfoDesc.setMaxLines(3);
							Button IvDescMore=(Button) findViewById(R.id.iv_appinfo_more);
							IvDescMore.setText(R.string.appinfo_open);
							moreFlag = true;
						}
					}
				});
				
			}
		}
		//relate 
	}

	private void addPreviewRequest() {
		// TODO Auto-generated method stub
		Request request = new Request(0, Constant.TYPE_APP_PREVIEW);
		String[] previewUrls = mAppInfo.getPreviewUrl();
		
		if (previewUrls == null || previewUrls.length == 0) {
			Message msg = Message.obtain(mHandler, ACTION_APP_PREVIEW, null);
			mHandler.sendMessage(msg);
			return;
		}
		
		request.setData(previewUrls);
		request.addObserver(new Observer() {

			@Override
			public void update(Observable observable, Object data) {
				// TODO Auto-generated method stub
				Request request = (Request) observable;
				switch (request.getStatus()) {
				case Constant.STATUS_SUCCESS:
					Message msg = Message.obtain(mHandler, ACTION_APP_PREVIEW, data);
					mHandler.sendMessage(msg);
					break;
				case Constant.STATUS_ERROR:
					mHandler.sendEmptyMessage(ACTION_NETWORK_ERROR);
					break;
				default:
					break;
				}
			}
		});
		
		mMarketService.getAppPreviews(request);
	}
	private void addThumbnailRequest(int id,String imageIcon,int postion) {
		
		Request request = new Request(0L, Constant.TYPE_RELATE_APP_ICON);
		Object[] params = new Object[3];
		String imgUrl = imageIcon;
		
		params[0] = Integer.valueOf(id);
		params[1] = imgUrl;
		params[2] = postion;
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
	private void addAppRelateRequest() {
		// TODO Auto-generated method stub
		Request request = new Request(0, Constant.TYPE_APP_RELATE);
		
		request.setData(Integer.valueOf(mAppInfo.getDownloads()));
		request.addObserver(new Observer() {

			@Override
			public void update(Observable observable, Object data) {
				// TODO Auto-generated method stub
				Log.v("asd", "addAppRelateRequest data = "+data);
				if (data != null) {
					Log.v("asd", "addAppRelateRequest2 data = "+data);
					Message msg = Message.obtain(mHandler, ACTION_APP_RELATE, data);
					mHandler.sendMessage(msg);
				} else {
					mHandler.sendEmptyMessage(ACTION_NETWORK_ERROR);
				}
			}
		});
		
		mMarketService.getAppDetail(request);
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		mAppInfo = ((AppInfoActivity) getParent()).getAppInfo();
		mMarketService = MarketService.getServiceInstance(this);
		
		setContentView(R.layout.appinfo_detail);
		
		/*************Added-s by JimmyJin for Pudding Project**************/
		mDisplay = getWindowManager().getDefaultDisplay();
		screenWidth = mDisplay.getWidth();
		screenHeight = mDisplay.getHeight();
		/*************Added-e by JimmyJin for Pudding Project**************/
		
		initHandlers();
		initViews();
		registerIntentReceivers();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.v("AppInfoDetailActivity", "onDestroy()");
		finish();
//		if (drawable != null && drawable instanceof BitmapDrawable) {
//			((BitmapDrawable)drawable).getBitmap().recycle();
//		}
		unregisterReceivers();
		mLoadingIndicator.setVisibility(View.VISIBLE);
		System.gc();
		View emptyView = findViewById(R.id.appinfo_preview_empty);
		TextView emptyTextView = (TextView)emptyView.findViewById(R.id.appinfo_preview_empty_text);
		emptyTextView.setVisibility(View.GONE);
		mPreviewGallery.setEmptyView(emptyView);
		if (mPreviews != null) {
			for(int i=0;i<mPreviews.size();i++){
				if(mPreviews.get(i) != null){
					mPreviews.get(i).setCallback(null); 
				}
			}
			mPreviews.clear();
		}
		
		if (mAdapter != null) {
			mAdapter = null;
		}
		if (appList != null) {
			appList.clear();
		}
		if (mPreviewGallery != null) {
			mPreviewGallery.setAdapter(null);
		}
		
		if (mPreRelateviews != null) {
			for(int i=0;i<mPreRelateviews.size();i++){
				if(mPreRelateviews.get(i) != null){
					mPreRelateviews.get(i).getDrawable().setCallback(null); 
				}
			}
			mPreRelateviews.clear();
			mPreRelateviews = null;
		}

//		if (mPreviews != null) {
//			mPreviews.setAdapter(null);
//		}
	}
    
	class PreviewGalleryAdapter extends BaseAdapter {

		private LayoutInflater mInflator;
		//private Matrix mMatrix;

		public PreviewGalleryAdapter(Context context) {

			mInflator = 
				(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			//mMatrix = new Matrix();
			//mMatrix.postScale(MarketBrowser.screenWidth/2, MarketBrowser.screenHeight/2);
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
				convertView = mInflator.inflate(R.layout.appinfo_detail_preview_item, null);
				
				imageView = (ImageView) convertView;
				imageView.setAdjustViewBounds(true);
//				imageView.setLayoutParams(new Gallery.LayoutParams(MarketBrowser.screenWidth/2-5,
//						MarketBrowser.screenHeight/2));	
				imageView.setLayoutParams(new Gallery.LayoutParams(screenWidth/2-5,
						screenHeight/2));	
				imageView.setPadding(2, 2, 2, 2);
			} 			
			else 
			{
				imageView = (ImageView) convertView;				
			}
			if(0 != mPreviews.size())//Added by JimmyJin for FC
				imageView.setImageDrawable(mPreviews.get(position%mPreviews.size()));
			
			return imageView;
		}
	}
}