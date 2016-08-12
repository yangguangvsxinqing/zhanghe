package com.fineos.theme.activity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.app.Activity;
import android.app.ActivityManagerNative;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.DownloadManager;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.TextView;
import com.baidu.mobstat.StatService;
import com.fineos.theme.R;
import com.fineos.theme.ThridFontsReceiver;
import com.fineos.theme.download.Downloads;
import com.fineos.theme.download.ReportProvider;
import com.fineos.theme.fragment.DeleteDialogFragment;
import com.fineos.theme.fragment.ProgressDialogFragment;
import com.fineos.theme.model.ThemeData;
import com.fineos.theme.provider.ThemeContentProvider;
import com.fineos.theme.provider.ThemesDataSource;
import com.fineos.theme.utils.Constant;
import com.fineos.theme.utils.FileUtils;
import com.fineos.theme.utils.FontResource;
import com.fineos.theme.utils.SimpleDialogs;
import com.fineos.theme.utils.ThemeLog;
import com.fineos.theme.utils.ThemeUtils;
import com.fineos.theme.utils.Util;
import com.fineos.theme.webservice.IThemeService;
import com.fineos.theme.webservice.Request;
import com.fineos.theme.webservice.ThemeService;
import fineos.app.ProgressDialog;
import fineos.content.res.IThemeManagerService;

public class ThemeDetailFontsActivity extends ThemeDetailBaseActivity {
	private static final int ACTION_THEME_PREVIEW = 1;
	private static final int ACTION_NETWORK_ERROR = 0;

	private static final String TAG = "ThemeDetailFontsActivity";
	private static final Boolean DEBUG = Boolean.TRUE;
	private final int DIALOG_PROGRESS = 0;
	protected ProgressDialog mProgressDialog;
	// private ThemeGallery mGallery;
	// private TextView mThemeDescription;
	private ImageView mFontPre;
	private ArrayList<BitmapDrawable> mPreviews;
	protected String[] mPreviewList = null;
	private TextView themeApply;
	private Display mDisplay;
	private Handler mHandler;
	private IThemeService mMarketService;
	// private ImageGalleryAdapter mAdapter;
	public static int screenWidth = 0;
	public static int screenHeight = 0;
	private final double RATO_PREVIEW = 0.65;

	private IThemeService mThemeService;
	private TextView mThemeAuthor;
	private TextView mThemeSize;
	private TextView mThemePrice;
	List<String> mExcludedItemsList;
	private boolean isOnline;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mContext = this;
		setContentView(R.layout.local_theme_detail_lockscreen);

		// mAdapter.notifyDataSetChanged();
		mThemeInfo = (ThemeData) getIntent().getSerializableExtra("themeInfo");
		isOnline = getIntent().getIntExtra("isOnline", 0) == 1;

		getActionBar().setDisplayUseLogoEnabled(false);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setDisplayShowHomeEnabled(false);
		getActionBar().setTitle(mThemeInfo.getTitle());

		IntentFilter filter = new IntentFilter();
		filter.addAction(Constant.ACTION_THEME_APPLIED);
		filter.addAction(Constant.ACTION_THEME_NOT_APPLIED);
		registerReceiver(mBroadcastReceiver, filter);

		if (DEBUG)
			dump();

		initHandlers();
		initView();
		
		setThemeReceiver(mThemeReceiver);
		registerIntentReceivers();
	}
	
	@Override
	protected void registerIntentReceivers() {
		// TODO Auto-generated method stub
		IntentFilter intentFilter = new IntentFilter(Downloads.ACTION_DOWNLOAD_COMPLETED_NOTIFICATION);
		intentFilter.addAction(ThridFontsReceiver.THRID_ACTION);
		mContext.registerReceiver(mThemeReceiver, intentFilter);
	}

	private BroadcastReceiver mThemeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String actionString = intent.getAction();
			if (actionString != null && actionString.equals(Downloads.ACTION_DOWNLOAD_COMPLETED_NOTIFICATION)) {
				initView();
				invalidateOptionsMenu();
			} else if (actionString != null && actionString.equals(ThridFontsReceiver.THRID_ACTION)) {
				String uriString = intent.getStringExtra(ThridFontsReceiver.THRID_FONTS_PATH);
				
			}
		}
	};
	
	public static void updateSysteFontConfiguration(FontResource fontRes) {
//		Configuration curConfig = new Configuration();
//		try {
//			curConfig.fontPackageName = fontRes.getPackageName();
//			curConfig.fontPath = fontRes.getFontFilePath();
//			ActivityManagerNative.getDefault().updatePersistentConfiguration(curConfig);
//		} catch (RemoteException e) {
//		}
	}
	
	private void initView() {
		if (isOnline && Util.checkDownload(mContext, mThemeInfo.getPackageName())) {
			ThemeLog.v(TAG, "checkDownload ");
			mThemeInfo = ThemeUtils.getThemeByFildId(mContext, mThemeInfo.getPackageName());
			ThemeLog.v(TAG, "checkDownload =" + mThemeInfo);
			ThemeLog.v(TAG, "checkDownload =" + mThemeInfo.getTitle());
			ThemeLog.v(TAG, "checkDownload =" + mThemeInfo.getPreviewsList());
			isOnline = false;
		}
		if (isOnline) {
			mThemeService = ThemeService.getServiceInstance(this);

		} else {
			mPreviewList = ThemeUtils.getPreviewListByType(mixType, mThemeInfo);
			ThemeLog.i(TAG, "mPreviewList=" + mPreviewList);
			mExcludedItemsList = new ArrayList<String>();
		}
		mDisplay = getWindowManager().getDefaultDisplay();
		screenWidth = mDisplay.getWidth();
		screenHeight = mDisplay.getHeight();

		mFontPre = (ImageView) findViewById(R.id.img_lock);

		RelativeLayout.LayoutParams lp = null;
		lp = (RelativeLayout.LayoutParams) mFontPre.getLayoutParams();
		lp.width = (int) (screenWidth * RATO_PREVIEW);
		lp.height = (int) (screenHeight * RATO_PREVIEW);
		ThemeLog.i(TAG, "screenWidth=" + screenWidth + " ,screenHeight =" + screenHeight);
		ThemeLog.i(TAG, "lp.width=" + lp.width + " ,lp.height =" + lp.height);

		mFontPre.setLayoutParams(lp);

		themeApply = (TextView) findViewById(R.id.btn_theme_apply);
		if (isOnline) {
			int downloadStatus = getDownloadStatus(mThemeInfo.getDownloadUrl());
			if (downloadStatus == DownloadManager.STATUS_PENDING||downloadStatus == DownloadManager.STATUS_RUNNING
					||downloadStatus == DownloadManager.STATUS_PAUSED) {
				themeApply.setEnabled(false);
				themeApply.setBackgroundResource(R.drawable.ic_btn_downloading);
				themeApply.setPadding(0, 0, 0, 0);
				themeApply.setText(R.string.btn_downloading);
				themeApply.setTextColor(getResources().getColor(R.color.text_color));
			}else {
				themeApply.setEnabled(true);
				themeApply.setBackgroundResource(R.drawable.ic_btn_download);
				themeApply.setPadding(0, 0, 0, 0);
				themeApply.setText(getResources().getString(R.string.btn_download));
				themeApply.setTextColor(getResources().getColor(R.color.text_color));
				themeApply.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
							Toast.makeText(mContext, getResources().getString(R.string.sd_unmounted), Toast.LENGTH_SHORT).show();
							return;
						}
						
						startDownload();
						if (themeApply != null) {
							themeApply.setEnabled(false);
							themeApply.setBackgroundResource(R.drawable.ic_btn_downloading);
							themeApply.setPadding(0, 0, 0, 0);
							themeApply.setText(R.string.btn_downloading);
							themeApply.setTextColor(getResources().getColor(R.color.text_color));
						}
					}
				});
			}

		} else {
			themeApply.setEnabled(true);
			themeApply.setBackgroundResource(R.drawable.ic_btn_apply);
			themeApply.setPadding(0, 0, 0, 0);
			themeApply.setText(getResources().getString(R.string.btn_apply));
			themeApply.setTextColor(getResources().getColor(R.color.white));
			themeApply.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ReportProvider.postUserTheme(ThemeUtils.getpackageName(mThemeInfo, isOnline), ThemeData.THEME_REPORT_SORT_USE);
					StatService.onEvent(mContext, "ApplyFonts", mThemeInfo.getTitle(), 1);
					applyTheme(mThemeInfo.getThemePath(), true, false, true);
					if (themeApply != null) {
						themeApply.setEnabled(false);
					}
				}
			});
		}
		
		mThemeAuthor = (TextView)findViewById(R.id.author);
		mThemeSize = (TextView)findViewById(R.id.size);
		mThemePrice= (TextView)findViewById(R.id.price);

		if (isOnline) {
			if(mThemeInfo.getSize()!=0){
				String iM = Util.ByteToM(mThemeInfo.getSize())+"M";
				mThemeSize.setText(iM);
			}

		} else {
			String iM = Util.FileSize(mThemeInfo.getThemePath());
			mThemeSize.setText(iM);
		}
		
		if(mThemeInfo.getauthor()!=null){
			if(mThemeAuthor!=null){
				mThemeAuthor.setText(mThemeInfo.getauthor());
			}
		}
		
		String sPrice = mThemeInfo.getsPrice()  ;
		String freeString = getResources().getString(R.string.default_price_text);
		if(sPrice==null||"".equals(sPrice)){
			if(Locale.getDefault().getLanguage().equals("en")){
				sPrice = "   " + freeString;
			}else{
				sPrice = freeString;
			}
			
		}else{
			ThemeLog.v(TAG,"laugush :"+Locale.getDefault().getLanguage());
			if(Locale.getDefault().getLanguage().equals("en")){
				sPrice = "   "+sPrice ;
			}else{
				sPrice = sPrice ;
			}
			
		}
		if(mThemePrice!=null){
			mThemePrice.setText(sPrice);
		}
		if (isOnline) {
			addPreviewRequest();
		} else {
			mPreviews = new ArrayList<BitmapDrawable>(mPreviewList.length);
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
			String[] asyncPreviewList = mPreviewList;
			try {
				ZipFile zip = new ZipFile(mThemeInfo.getThemePath());
				ZipEntry ze;

				final Resources res = getResources();
				ThemeLog.i(TAG, "mPreviews.size=" + mPreviews.size());

				for (int j = 0; j < asyncPreviewList.length; j++) {

					ze = zip.getEntry(asyncPreviewList[j]);
					InputStream is = ze != null ? zip.getInputStream(ze) : null;
					if (is != null) {
						BitmapFactory.Options opts = new BitmapFactory.Options();
						opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
						opts.inSampleSize = 1;
						Bitmap bmp = BitmapFactory.decodeStream(is, null, opts);
						final BitmapDrawable drawable = new BitmapDrawable(res, bmp);

						ThemeLog.i(TAG, "drawable=" + drawable);
						mPreviews.add(drawable);
					}
				}

			} catch (IOException e) {
			}
			return null;
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
		
		Request request = new Request(0, Constant.TYPE_THEME_PREVIEW);
		String[] previewUrls = mThemeInfo.getPreviewUrl();

		if (previewUrls == null || previewUrls.length == 0) {
			Message msg = Message.obtain(mHandler, ACTION_THEME_PREVIEW, null);
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
					Message msg = Message.obtain(mHandler, ACTION_THEME_PREVIEW, data);
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

		mThemeService.getThemePreviews(request);
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
					if (isOnline) {
						mPreviews = (ArrayList<BitmapDrawable>) msg.obj;
					}

					if (mPreviews != null && mPreviews.size() > 0&&mFontPre!=null) {
						mFontPre.setImageDrawable(mPreviews.get(0));
						mFontPre.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.bg_font));
					}

					break;					
				case ACTION_NETWORK_ERROR:
					hideLoadingDialog();
											
					Toast.makeText(mContext, mContext.getString(R.string.error_network_low_speed), Toast.LENGTH_LONG).show();
					break;
				default:
					break;
				}
			}
		};
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		super.onCreateDialog(id);
		switch (id) {
		case DIALOG_PROGRESS:
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setMessage(getResources().getText(R.string.applying_font));
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mProgressDialog.setCancelable(false);
			mProgressDialog.setProgress(0);
			mProgressDialog.show();
			return mProgressDialog;
		default:
			return super.onCreateDialog(id);
		}
	}

	private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			ThemeLog.i(TAG, "action = " + action);
			if (Constant.ACTION_THEME_APPLIED.equals(action)) {
				
			} else if (Constant.ACTION_THEME_NOT_APPLIED.equals(action)) {
				SimpleDialogs.displayOkDialog(R.string.dlg_theme_failed_title, R.string.dlg_theme_failed_body, ThemeDetailFontsActivity.this);
			}
			if (themeApply != null) {
				themeApply.setEnabled(true);
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
		unregisterIntentReceivers();
		if (mPreviews != null) {
			if (mPreviews != null && mPreviews.size() > 0) {
				int mPreviewSize = mPreviews.size();
				for (int i = 0; i < mPreviewSize; i++) {
					mPreviews.get(i).getBitmap().recycle();
				}
			}
			mPreviews.clear();
		}
		unregisterReceiver(mBroadcastReceiver);
	}

	private void applyTheme(String theme, boolean applyFont, boolean scaleBoot, boolean removeExistingTheme) {
		ThemeLog.i(TAG,"ThemeDetailLockscreenActivity applyTheme theme1: " + theme);
		theme = copyFileToSystem(theme);
		ThemeLog.e(TAG,"ThemeDetailLockscreenActivity applyTheme theme2: " + theme);
		StatService.onEvent(this, "ApplyFont", mThemeInfo.getTitle(), 1);
		IThemeManagerService ts = IThemeManagerService.Stub.asInterface(ServiceManager.getService("ThemeService"));
		
		try {
			ThemeLog.i(TAG, "theme=" + theme + " ,mixType(10==fonts)=" + mixType);
			ts.applyThemeFont(ThemeContentProvider.CONTENT + FileUtils.stripPath(theme));

			ThemeLog.i(TAG, "DIALOG_PROGRESS");
			showDialog(DIALOG_PROGRESS);

		} catch (Exception e) {
			ThemeLog.e(TAG, "exception happened: "+e.getMessage());
			// SimpleDialogs.displayOkDialog(R.string.dlg_theme_failed_title,
			// R.string.dlg_theme_failed_body,
			// this);
		}
		ThemeUtils.resetUsingFlagByType(ThemeDetailFontsActivity.this, mixType);
		ThemeUtils.setUsingFlagByType(ThemeDetailFontsActivity.this, mThemeInfo, mixType);
		Toast.makeText(mContext, getText(R.string.font_apply_success_tip), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void deleteTheme() {
		// TODO Auto-generated method stub
//		initView();
//		invalidateOptionsMenu();
	}

}
