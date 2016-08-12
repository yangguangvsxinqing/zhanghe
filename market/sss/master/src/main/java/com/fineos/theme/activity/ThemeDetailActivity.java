package com.fineos.theme.activity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.ServiceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.baidu.mobstat.StatService;
import com.fineos.billing.util.IabHelper;
import com.fineos.billing.util.IabResult;
import com.fineos.theme.R;
import com.fineos.theme.baidusdk.ThemeApplication;
import com.fineos.theme.download.Constants;
import com.fineos.theme.download.ReportProvider;
import com.fineos.theme.model.ThemeData;
import com.fineos.theme.provider.ThemeContentProvider;
import com.fineos.theme.provider.ThemesDataSource;
import com.fineos.theme.ui.AlignLeftGallery;
import com.fineos.theme.utils.Constant;
import com.fineos.theme.utils.FileManager;
import com.fineos.theme.utils.FileUtils;
import com.fineos.theme.utils.ImageCache;
import com.fineos.theme.utils.SimpleDialogs;
import com.fineos.theme.utils.ThemeLog;
import com.fineos.theme.utils.ThemeUtils;
import com.fineos.theme.utils.Util;
import com.fineos.theme.webservice.IThemeService;
import com.fineos.theme.webservice.Request;
import com.fineos.theme.webservice.ThemeService;
import com.fineos.theme.utils.ThemeBillingHelper;
import com.fineos.billing.util.IabHelper.OnIabPurchaseFinishedListener;
import fineos.app.ProgressDialog;
import fineos.content.res.IThemeManagerService;

public class ThemeDetailActivity extends ThemeDetailBaseActivity {
	private static final int ACTION_THEME_PREVIEW = 1;
	private static final int ACTION_NETWORK_ERROR = 0;
	private static final int ACTION_START_GOOGLE_BILLING = 2;
	private static final int ACTION_GET_PRICE = 3;
	private static final int ACTION_SET_PRICE = 4;
	private static final Boolean DEBUG = Boolean.TRUE;

	protected static final int DIALOG_PROGRESS = 0;
	protected static final int DIALOG_DELAY = 1;

	protected ProgressDialog mProgressDialog;
	protected AlertDialog mDialog;
	private Gallery mGallery;
//	private TextView mThemeDescription;
	private ArrayList<Bitmap> mPreviews;
	protected String[] mPreviewList = null;
	private TextView mThemeApply;
	///for theme info display
	private TextView mThemeAuthor;
	private TextView mThemeSize;
	private TextView mThemePrice;
	
	private Display mDisplay;
	private Handler mHandler;

	private ImageGalleryAdapter mAdapter;
	public static int screenWidth = 0;
	public static int screenHeight = 0;

	private IThemeService mThemeService;
	private List<String> mExcludedItemsList;
	private boolean isOnline;
	private int themeId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(com.fineos.internal.R.style.FineOSTheme_Material_Light_WhiteActionBar);
		setContentView(R.layout.local_theme_detail);
		ThemeLog.i(TAG, "onCreate...............................");
		mContext = this;
		themeId = (int) getIntent().getLongExtra("themeid", 0);
		if (themeId != 0) {
			mThemeInfo = ThemeUtils.getThemeEntryById(themeId, mContext);
		} else {
			mThemeInfo = (ThemeData) getIntent().getSerializableExtra("themeInfo");
		}
		isOnline = getIntent().getIntExtra("isOnline", 0) == 1;
		getActionBar().setDisplayUseLogoEnabled(false);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setDisplayShowHomeEnabled(false);
		getActionBar().setTitle(mThemeInfo.getTitle());

		initHandlers();

		 //xuqian add sku begin
		int GoogleBillingSupportId = mContext.getResources().getIdentifier("google_billing_support", "bool", "com.fineos");
		   if(GoogleBillingSupportId <= 0){
			   GoogleBillingSupportId = com.fineos.internal.R.bool.google_billing_support;
		}
		mbGoogleBillingSupport = mContext.getResources().getBoolean(GoogleBillingSupportId);
		mProductID = Integer.toString(mThemeInfo.getId());
		mFreeString = getResources().getString(R.string.default_price_text);
		ThemeLog.v(TAG,"mProductID:"+mProductID+",mPrice:"+mThemeInfo.getPrice());
		initView();
		if (mbGoogleBillingSupport&&isOnline) {
			addGoogleBillingRequest();   // for google billing
		}
		setThemeReceiver(mThemeReceiver);
		registerIntentReceivers();
		RegistReceiver();
	}
	
    private void addGoogleBillingRequest() {
		
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
		
		mHandler.removeMessages(ACTION_START_GOOGLE_BILLING);
		Message msg = Message.obtain(mHandler, ACTION_START_GOOGLE_BILLING, request);
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
			mbSetUpSuccessed = true ;
			ThemeLog.d(TAG, "Setup successful. Querying inventory.");
            if(mBillingHelper!=null){
                mBillingHelper.queryInventoryAsync();
            }
			addGetPriceRequest();
		}
	};
	
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
  					Message msg = Message.obtain(mHandler, ACTION_SET_PRICE, null);
  		    		mHandler.sendMessageDelayed(msg, 0);
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
  		mHandler.sendMessageDelayed(msg, 0);
  		
  	}
	
	 private void setPrice(){
			
			ThemeLog.v(TAG,"setPrice(),mPrice:"+mPrice+"mProductID:"+mProductID); //
			if(mThemePrice!=null){
				if(mPrice!=null&&!"".equals(mPrice)){
					
					Boolean downloaded = Util.checkDownload(mContext, mThemeInfo.getPackageName());
					String buyed = mContext.getResources().getString(R.string.buyed_text);
					ThemeLog.v(TAG, "downloaded:"+downloaded+",package:"+mThemeInfo.getPackageName());
					if(!mFreeString.equals(mPrice)&&downloaded){
						mPrice = buyed ;
					}
					mThemePrice.setText(mPrice);
				}
			}
	 }
	
	//xuqian add end 
	
	
	

	private BroadcastReceiver mThemeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(mThemeInfo!=null){
				ThemeLog.v(TAG,"Downloaded ,theme price :"+mThemeInfo.getPrice()+"title:"+mThemeInfo.getTitle()+",themepath:"+mThemeInfo.getThemePath());
				ThemeUtils.updateThemePrice(mContext, mThemeInfo);   //write to db
			}
			
			initView();
			invalidateOptionsMenu();
			onDownLoadComplete();
		}
	};
	
	private void onDownLoadComplete() {
		Intent intent = new Intent(Constants.ACTION_DOWNLOADITMES);
		intent.putExtra("packageName", mThemeInfo.getPackageName());
		this.sendBroadcast(intent);
	}

	private void initView() {
		mThemeAuthor = (TextView)findViewById(R.id.author);
		mThemeSize = (TextView)findViewById(R.id.size);
		mThemePrice= (TextView)findViewById(R.id.price);
		if (isOnline && Util.checkDownload(mContext, mThemeInfo.getPackageName())) {
			ThemeLog.i(TAG, "checkDownload ");
			mThemeInfo = ThemeUtils.getThemeByFildId(mContext, mThemeInfo.getPackageName());
			ThemeLog.i(TAG, "checkDownload mThemeInfo =" + mThemeInfo);
			ThemeLog.i(TAG, "checkDownload title =" + mThemeInfo.getTitle());
			ThemeLog.i(TAG, "checkDownload price =" + mThemeInfo.getPrice());
			ThemeLog.i(TAG, "checkDownload previewsList =" + mThemeInfo.getPreviewsList());
			isOnline = false;
		}
		if (isOnline) {
			if(mThemeInfo.getSize()!=0){
				String iM = Util.FormetFileSize(mThemeInfo.getSize());
				mThemeSize.setText(iM);
			}
			mThemeService = ThemeService.getServiceInstance(this);

		} else {
			String iM = Util.FileSize(mThemeInfo.getThemePath());
			mThemeSize.setText(iM);
			if (mThemeInfo == null) {
				long id = getIntent().getLongExtra("themeid", -1);
				mThemeInfo = ThemesDataSource.getInstance(mContext).getThemeById(id);
				mPreviewList = ThemeUtils.getPreviewListByType(mixType, mThemeInfo);
			} else {
				if (DEBUG)
					dump();
				mPreviewList = ThemeUtils.getPreviewListByType(mixType, mThemeInfo);
				ThemeLog.i(TAG, "mPreviewList=" + mPreviewList.length);
			}
			mExcludedItemsList = new ArrayList<String>();
		}
		mDisplay = getWindowManager().getDefaultDisplay();
		screenWidth = mDisplay.getWidth();
		screenHeight = mDisplay.getHeight();
		mGallery = (Gallery) findViewById(R.id.gallery_image);
		DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
		((AlignLeftGallery) mGallery).alignGalleryToLeft(metric.density);
//		mThemeDescription = (TextView) findViewById(R.id.theme_info_description);
		mThemeApply = (TextView) findViewById(R.id.btn_theme_apply);
		mThemeApply.setEnabled(true);
		mThemeApply.setTextColor(getResources().getColor(R.color.white));
		///xuqian add begin 
		
		if(mThemeInfo.getauthor()!=null){
			if(mThemeAuthor!=null){
				mThemeAuthor.setText(mThemeInfo.getauthor());
			}
		}
		
		mPrice = mThemeInfo.getsPrice()  ;
		
		ThemeLog.v(TAG, "mPrice(FROM GOOGLE):"+mPrice);
		if(mPrice==null||"".equals(mPrice)){   // get from google first
			
			Float fPrice = mThemeInfo.getPrice();   // get from server
			if(fPrice!=0){
				mPrice = "$"+String.valueOf(fPrice);
			}else{
				mPrice = mFreeString;                // then default free
			}
		}
		if(mThemePrice!=null){
			
			Boolean downloaded = Util.checkDownload(mContext, mThemeInfo.getPackageName());
			String buyed = mContext.getResources().getString(R.string.buyed_text);
			ThemeLog.v(TAG, "downloaded:"+downloaded+",package:"+mThemeInfo.getPackageName()+",mPrice:"+mPrice);
			if(mbGoogleBillingSupport&&mPrice != null&&!"".equals(mPrice)&&!mFreeString.equals(mPrice)&&downloaded){
				mPrice = buyed ;
			}
			if(Locale.getDefault().getLanguage().equals("en")){
				mThemePrice.setText("   " + mPrice);  // for layout
			} else {
				mThemePrice.setText(mPrice);
			}
		}
		///xuqian add end 
		
		if (isOnline) {
			int downloadStatus = getDownloadStatus(mThemeInfo.getDownloadUrl());
			if (downloadStatus == DownloadManager.STATUS_PENDING||downloadStatus == DownloadManager.STATUS_RUNNING
					||downloadStatus == DownloadManager.STATUS_PAUSED) {
				mThemeApply.setEnabled(false);
				mThemeApply.setBackgroundResource(R.drawable.ic_btn_downloading);
				mThemeApply.setPadding(0, 0, 0, 0);
				mThemeApply.setText(R.string.btn_downloading);
			} else {
				mThemeApply.setEnabled(true);
				mThemeApply.setBackgroundResource(R.drawable.ic_btn_download);
				mThemeApply.setPadding(0, 0, 0, 0);
				mThemeApply.setText(getResources().getString(R.string.btn_download));
				ThemeLog.v(TAG, "mbGoogleBillingSupport:"+mbGoogleBillingSupport+",mPrice:"+mPrice+",mFreeString:"+mFreeString+"equals :"+mPrice.equals(mFreeString));
				if(mbGoogleBillingSupport&&(mPrice != null&&!"".equals(mPrice)&&!mPrice.equals(mFreeString)/*&&mThemeInfo.getType()!=ThemeData.THEME_PACKAGE_TYPE_FREE*/)){
					mThemeApply.setText(R.string.btn_text_buy);
				}
				mThemeApply.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
							Toast.makeText(mContext, getResources().getString(R.string.sd_unmounted), Toast.LENGTH_SHORT).show();
							return;
						}
						
						ThemeLog.v(TAG, "Package type :"+mThemeInfo.getType()+",mbSetUpSuccessed:"+mbSetUpSuccessed);
						
						if(mbGoogleBillingSupport&&(mPrice != null&&!"".equals(mPrice)&&!mPrice.equals(mFreeString)/*&&mThemeInfo.getType()!=ThemeData.THEME_PACKAGE_TYPE_FREE*/)){
							//mThemeApply.setText(R.string.btn_text_buy);
							
							if(!mbSetUpSuccessed){
								
							    Util.showAddaccountDialog(mContext);
							}else{
								if(mBillingHelper!=null){
									mBillingHelper.doPurchase(mProductID,new IabHelper.OnIabPurchaseFinishedListener() {
											
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
													ThemeLog.d(TAG, "success purchase :" + mProductID+"startDownload....");
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
								}
								
							}// end of else
							
							
						} else {                // consider free
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
			}
			
		} else {
			mThemeApply.setEnabled(true);
			mThemeApply.setBackgroundResource(R.drawable.ic_btn_apply);
			mThemeApply.setPadding(0, 0, 0, 0);
			mThemeApply.setText(getResources().getString(R.string.btn_apply));
			mThemeApply.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					try {
						ReportProvider.postUserTheme(mThemeInfo.getPackageName(), ThemeData.THEME_REPORT_SORT_USE);

						applyTheme(mThemeInfo.getThemePath(), false, false, true);
						if (mThemeApply != null) {
							mThemeApply.setEnabled(false);
						}

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
		if(mPreviews!=null){
			mPreviews = null;
		}
		mPreviews = new ArrayList<Bitmap>();
		if (isOnline) {
			addPreviewRequest();
		} else {
			preloadImages();
		}
	}

	private void preloadImages() {
		(new PreviewLoaderAsyncTask()).execute();

	}

	private class PreviewLoaderAsyncTask extends AsyncTask {

		@Override
		protected Object doInBackground(Object[] params) {
			int i = 0;
			Bitmap bmp = null;
			String[] asyncPreviewList;
			try {
				ZipFile zip = new ZipFile(mThemeInfo.getThemePath());
				ZipEntry ze;
				
				final Resources res = getResources();
				asyncPreviewList = mPreviewList;
				for (int j = 0; j < asyncPreviewList.length; j++) {
					ThemeLog.i(TAG, "mPreviewList[j]=" + asyncPreviewList[j]);
					ThemeLog.i(TAG, "mixType=" + mixType);
					if (mixType < 0) {
						if (asyncPreviewList[j].contains("_small") || asyncPreviewList[j].contains("default") || asyncPreviewList[j].contains("fonts")) {
							ThemeLog.i(TAG, "find one :mPreviewList[j]=" + asyncPreviewList[j]);
							continue;
						}
					}

					ze = zip.getEntry(asyncPreviewList[j]);
					InputStream is = ze != null ? zip.getInputStream(ze) : null;
					if (is != null) {
						BitmapFactory.Options opts = new BitmapFactory.Options();
						// opts.inPreferredConfig = Bitmap.Config.RGB_565;
						opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
						opts.inSampleSize = 2;
						bmp = BitmapFactory.decodeStream(is, null, opts);
//						BitmapDrawable drawable = new BitmapDrawable(res, bmp);

						mPreviews.add(bmp);
					}
				}
			} catch (IOException e) {
			}
			// if(mixType != ThemeData.THEME_ELEMENT_TYPE_ICONS&&bmp!=null){
			// bmp.recycle();
			// }

			return null;

		}

		public void onPostExecute() {
			// TODO Auto-generated method stub

		}

		@Override
		protected void onPostExecute(Object o) {
			super.onPostExecute(o);
			Message msg = Message.obtain(mHandler, ACTION_THEME_PREVIEW, null);
			mHandler.sendMessage(msg);
		}
	} // end of PreviewLoaderAsyncTask

	private void addPreviewRequest() {
		showLoadingDialog();
		final String[] previewUrls = mThemeInfo.getPreviewUrl();
		for(int i=0;i<previewUrls.length;i++){
			final String url = previewUrls[i];
			final int index = i;
			Bitmap btm = ImageCache.getThumbnailBitmap(mContext, url);
			if(btm == null){
				ImageRequest imageRequest = new ImageRequest(previewUrls[i], 
						new Listener<Bitmap>(){
							@Override
							public void onResponse(Bitmap response) {
								// TODO Auto-generated method stub
								if (response != null) {
									ImageCache.cacheThumbnail(mContext, url, response);
									mPreviews.add(ImageCache.getThumbnailBitmap(mContext, url));
								}
								Log.v(TAG, "ssss onResponse index"+index);
								if(mPreviews.size() == previewUrls.length){
									mHandler.sendEmptyMessage(ACTION_THEME_PREVIEW);
								}
							}}, 
						0, 0, Config.ARGB_8888, 
						new ErrorListener(){
							@Override
							public void onErrorResponse(VolleyError error) {
								// TODO Auto-generated method stub
								
							}});
				ThemeApplication.getInstance().addToRequestQueue(imageRequest,Constant.TAG_ONLINE_THEME_PREVIEW);
			}else{
				mPreviews.add(btm);
				if(mPreviews.size() == previewUrls.length){
					mHandler.sendEmptyMessage(ACTION_THEME_PREVIEW);
				}
			}
		}
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
					// free old mAdapter
					if (mAdapter != null) {
						mAdapter = null;
					}
					mAdapter = new ImageGalleryAdapter(ThemeDetailActivity.this);
					if (mGallery != null) {
						mGallery.setAdapter(mAdapter);
					}

					break;
					case ACTION_NETWORK_ERROR:
						hideLoadingDialog();
												
						Toast.makeText(mContext, mContext.getString(R.string.error_network_low_speed), Toast.LENGTH_LONG).show();
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

	class ImageGalleryAdapter extends BaseAdapter {
		private LayoutInflater mInflator;
		ImageView pre;

		public ImageGalleryAdapter(Context context) {
			mContext = context;
			mInflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			// return mPreviewList.length;
			return mPreviews.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			// return mGallery.getItemAtPosition(position);
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
			ThemeLog.i(TAG, "getView,convertView=" + convertView);
			ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflator.inflate(R.layout.theme_detail_preview_item, null);
				pre = (ImageView) convertView;
				holder.galleryItem = pre;
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();

			}
			Gallery.LayoutParams lp = null;
			lp = new Gallery.LayoutParams((int) (screenWidth * 0.65), (int) (screenHeight * 0.65)); // 468
																									// x
																									// 834
			holder.galleryItem.setLayoutParams(lp);
			if (0 != mPreviews.size()) {
				holder.galleryItem.setImageDrawable(new BitmapDrawable(null, mPreviews.get(position % mPreviews.size())));
			}
			return convertView;

		}

		class ViewHolder {

			ImageView galleryItem;

		}

	} // end of adapter

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_PROGRESS:
			mProgressDialog = new ProgressDialog(this);
			// ThemeLog.e(TAG, "DIALOG_PROGRESS mProgressDialog"+mProgressDialog);
			if (mixType == ThemeData.THEME_ELEMENT_TYPE_ICONS) {
				mProgressDialog.setMessage(getResources().getText(R.string.applying_icons));
			} else {
				mProgressDialog.setMessage(getResources().getText(R.string.applying_theme));
			}

			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mProgressDialog.setCancelable(false);
			mProgressDialog.setProgress(0);
			mProgressDialog.show();
			return mProgressDialog;
		case DIALOG_DELAY:
			mDialog = new AlertDialog.Builder(mContext).create();
			return mDialog;
		default:
			return super.onCreateDialog(id);
		}
	}
	private String getThemeName(String uri){
		String themeName;
		themeName = uri == null?uri:uri.substring(uri.lastIndexOf("/")+1, uri.lastIndexOf("."));
		return themeName;
	}
	private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			String uri = intent.getStringExtra("ThemeUri");
			int type = intent.getIntExtra("Type", -1);
			ThemeLog.i(TAG, "action = " + action);
			ThemeLog.d(TAG, "uri = " + getThemeName(uri));
			ThemeLog.w(TAG, "mThemeInfo.getThemePath() = " + getThemeName(mThemeInfo.getThemePath()));
			if (mixType == ThemeData.THEME_ELEMENT_TYPE_ICONS){
				if(type > 0 && type != ThemeData.ICON_APPLY){
					return;
				}
			}
			if (mixType == -1){
				if(type > 0 && type != ThemeData.THEME_APPLY){
					return;
				}
			}
			if ((Constant.ACTION_THEME_APPLIED.equals(action))) {
				if(uri==null||!getThemeName(uri).equals(getThemeName(mThemeInfo.getThemePath()))){
					return;
				}
				ThemeUtils.resetUsingFlagByType(ThemeDetailActivity.this, mixType);
				ThemeUtils.setUsingFlagByType(ThemeDetailActivity.this, mThemeInfo, mixType);
				if (mixType == ThemeData.THEME_ELEMENT_TYPE_ICONS) {
					Toast.makeText(ThemeDetailActivity.this, getText(R.string.icon_apply_success_tip), Toast.LENGTH_SHORT).show();

				} else {
					Toast.makeText(ThemeDetailActivity.this, getText(R.string.apply_success_tip), Toast.LENGTH_SHORT).show();
				}

			} else if (Constant.ACTION_THEME_NOT_APPLIED.equals(action)) {
				SimpleDialogs.displayOkDialog(R.string.dlg_theme_failed_title, R.string.dlg_theme_failed_body, ThemeDetailActivity.this);
			}
			if (mThemeApply != null) {
				mThemeApply.setEnabled(true);
			}
			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		StatService.onResume(this);

		// ugly hack to keep the dialog from reappearing when the app is
		// restarted
		// due to a theme change.
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}

		ThemeLog.v(TAG, "mbGoogleBillingSupport:"+mbGoogleBillingSupport+",mbSetUpSuccessed:"+mbSetUpSuccessed);
		if(mbGoogleBillingSupport&&!mbSetUpSuccessed){
			addGoogleBillingRequest();
		}
	
		ThemeLog.i(TAG, "onResume...............................");
		
       
	}

	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPause(this);

		ThemeLog.i(TAG, "onPause.................................");
		
	}

	public void RegistReceiver() {

		ThemeLog.i(TAG, "Register receiver............");
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constant.ACTION_THEME_APPLIED);
		filter.addAction(Constant.ACTION_THEME_NOT_APPLIED);
		registerReceiver(mBroadcastReceiver, filter);

	}

	public void UnRegistReceiver() {

		ThemeLog.i(TAG, "UnRegistReceiver.............");
		unregisterReceiver(mBroadcastReceiver);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		ThemeApplication.getInstance().cancelPendingRequests(Constant.TAG_ONLINE_THEME_PREVIEW);
		ThemeApplication.getInstance().cancelPendingRequests(Constant.TAG_ONLINE_THEME_LIKE);
		ThemeApplication.getInstance().getRequestQueue().cancelAll(Constant.TAG_ONLINE_THEME_PREVIEW);
		if (isOnline == false&&mPreviews != null) {
			int mPreviewSize = mPreviews.size();
			for (int i = 0; i < mPreviewSize; i++) {
				if(mPreviews.get(i) != null && mPreviews.get(i) != null){
					mPreviews.get(i).recycle();
				}
			}
			mPreviews.clear();
			mPreviews = null;
		}
		UnRegistReceiver();
		unregisterIntentReceivers();
	}

//	@Override
//	public void onConfigurationChanged(Configuration newConfig) {
//		// super.onConfigurationChanged(newConfig);
//		ThemeLog.i(TAG, "onConfigurationChanged,newConfig=" + newConfig);
//	}

	private void applyTheme(String theme, boolean applyFont, boolean scaleBoot, boolean removeExistingTheme) {
		ThemeLog.i(TAG,"ThemeDetailActivity applyTheme theme1: " + theme);
		theme = copyFileToSystem(theme);
		ThemeLog.i(TAG,"ThemeDetailActivity applyTheme theme2: " + theme);

//		IThemeManagerService ts = IThemeManagerService.Stub.asInterface(ServiceManager.getService("ThemeService"));
		try {

			ThemeLog.i(TAG, "theme=" + theme + " ,mixType(0==icons)=" + mixType);
			ThemeLog.i(TAG, "theme=" + ThemeContentProvider.CONTENT + FileUtils.stripPath(theme));
			if (mixType == ThemeData.THEME_ELEMENT_TYPE_ICONS) {
				StatService.onEvent(ThemeDetailActivity.this, "ApplyIcon", mThemeInfo.getTitle(),1);
//				Intent intent = new Intent(ThemeUtils.APPLY_THEME_ACTION);
//				intent.putExtra("theme", ThemeContentProvider.CONTENT + FileUtils.stripPath(theme));
//				this.sendBroadcast(intent);
//				ts.applyThemeIcons(ThemeContentProvider.CONTENT + FileUtils.stripPath(theme));

			} else {
				ThemeLog.i(TAG, "applyTheme start for theme");
				StatService.onEvent(ThemeDetailActivity.this, "ApplyTheme", mThemeInfo.getTitle(),1);
				Intent intent = new Intent(ThemeUtils.APPLY_THEME_ACTION);
				intent.putExtra("theme", ThemeContentProvider.CONTENT + FileUtils.stripPath(theme));
				this.sendBroadcast(intent);
//				ts.applyTheme(ThemeContentProvider.CONTENT + FileUtils.stripPath(theme), mExcludedItemsList, applyFont, scaleBoot, removeExistingTheme);
			}
			showDialog(DIALOG_PROGRESS);

		} catch (Exception e) {
			ThemeLog.e(TAG, "applyTheme exception happened !");
			e.printStackTrace();
			SimpleDialogs.displayOkDialog(R.string.dlg_theme_failed_title, R.string.dlg_theme_failed_body, ThemeDetailActivity.this);
		}
	}

	@Override
	public void deleteTheme() {
		// TODO Auto-generated method stub
//		initView();
//		invalidateOptionsMenu();
		Intent intent = new Intent(Constants.ACTION_DELTEITEMS);
		intent.putExtra("packageName", mThemeInfo.getPackageName());
		this.sendBroadcast(intent);
	}

}
