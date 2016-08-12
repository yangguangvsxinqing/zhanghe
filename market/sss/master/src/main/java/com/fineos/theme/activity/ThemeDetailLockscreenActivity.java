package com.fineos.theme.activity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
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
import com.fineos.theme.download.Downloads;
import com.fineos.theme.download.ReportProvider;
import com.fineos.theme.fragment.DeleteDialogFragment;
import com.fineos.theme.fragment.ProgressDialogFragment;
import com.fineos.theme.model.ThemeData;
import com.fineos.theme.provider.ThemeContentProvider;
import com.fineos.theme.utils.Constant;
import com.fineos.theme.utils.FileUtils;
import com.fineos.theme.utils.SimpleDialogs;
import com.fineos.theme.utils.ThemeLog;
import com.fineos.theme.utils.ThemeUtils;
import com.fineos.theme.utils.Util;
import com.fineos.theme.webservice.IThemeService;
import com.fineos.theme.webservice.Request;
import com.fineos.theme.webservice.ThemeService;

import fineos.app.ProgressDialog;
import fineos.content.res.IThemeManagerService;

public class ThemeDetailLockscreenActivity extends ThemeDetailBaseActivity {
	private static final int ACTION_THEME_PREVIEW = 1;
	private static final int ACTION_NETWORK_ERROR = 0;

	private static final String TAG = "FineOS_Theme.LocalThemeInfo";
	private static final Boolean DEBUG = Boolean.TRUE;
	protected static final int DIALOG_PROGRESS = 0;
	protected ProgressDialog mProgressDialog;
	// private ThemeGallery mGallery;
	// private TextView mThemeDescription;
	private ImageView mLockPre;
	// private ThemeData mThemeInfo;
	private ArrayList<BitmapDrawable> mPreviews;
	protected String[] mPreviewList = null;
	private TextView themeApply;
	private Display mDisplay;
	private Handler mHandler;
	private IThemeService mMarketService;
	// private ImageGalleryAdapter mAdapter;
	public static int screenWidth = 0;
	public static int screenHeight = 0;
	public final double RATO_PREVIEW = 0.7;

	private IThemeService mThemeService;
	private List<String> mExcludedItemsList;
	private boolean isOnline;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.local_theme_detail_lockscreen);

		mContext = this;
		mThemeInfo = (ThemeData) getIntent().getSerializableExtra("themeInfo");
		isOnline = getIntent().getIntExtra("isOnline", 0) == 1;

		getActionBar().setDisplayUseLogoEnabled(false);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setDisplayShowHomeEnabled(false);
		getActionBar().setTitle(mThemeInfo.getTitle());

		if (DEBUG)
			dump();

		initHandlers();
		initView();
		
		setThemeReceiver(mThemeReceiver);
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constant.ACTION_THEME_APPLIED);
		filter.addAction(Constant.ACTION_THEME_NOT_APPLIED);
		registerReceiver(mBroadcastReceiver, filter);
		registerIntentReceivers();
	}

	private BroadcastReceiver mThemeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
//			int themeId = intent.getIntExtra("themeId", 0);
//			if (themeId == mThemeInfo.getId()) {
//				isOnline = false;
			initView();
			invalidateOptionsMenu();
//			}
		}
	};
	private String getThemeName(String uri){
		String themeName;
		themeName = uri == null?uri:uri.substring(uri.lastIndexOf("/")+1, uri.lastIndexOf("."));
		return themeName;
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
		screenWidth = mDisplay.getWidth(); // 720
		screenHeight = mDisplay.getHeight(); // 1280

		mLockPre = (ImageView) findViewById(R.id.img_lock);

		RelativeLayout.LayoutParams lp = null;
		lp = (RelativeLayout.LayoutParams) mLockPre.getLayoutParams();
		lp.width = (int) (screenWidth * RATO_PREVIEW);
		lp.height = (int) (screenHeight * RATO_PREVIEW);
		ThemeLog.i(TAG, "screenWidth=" + screenWidth + " ,screenHeight =" + screenHeight);
		ThemeLog.i(TAG, "lp.width=" + lp.width + " ,lp.height =" + lp.height);

		mLockPre.setLayoutParams(lp);
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
					StatService.onEvent(mContext, "ApplyLockscreen", mThemeInfo.getTitle(), 1);
					applyTheme(mThemeInfo.getThemePath(), false, false, true);
					if (themeApply != null) {
						themeApply.setEnabled(false);
					}
				}
			});
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
			Bitmap bmp = null;
			try {
				ZipFile zip = new ZipFile(mThemeInfo.getThemePath());
				ZipEntry ze;

				final Resources res = getResources();
				ThemeLog.i(TAG, "PreviewLoaderAsyncTask mPreviews.size=" + mPreviews.size());
				ThemeLog.i(TAG, "PreviewLoaderAsyncTask mPreviewList.length=" + mPreviewList.length);
				for (int j = 0; j < mPreviewList.length - 1; j++) {

					ze = zip.getEntry(mPreviewList[j + 1]);
					InputStream is = ze != null ? zip.getInputStream(ze) : null;
					if (is != null) {
						BitmapFactory.Options opts = new BitmapFactory.Options();
						opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
						opts.inSampleSize = 2;
						bmp = BitmapFactory.decodeStream(is, null, opts);
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

					if (mPreviews != null && mPreviews.size() > 0&&mLockPre!=null) {
						mLockPre.setImageDrawable(mPreviews.get(0));
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
			mProgressDialog.setMessage(getResources().getText(R.string.applying_lockscreen));
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
			String uri = intent.getStringExtra("ThemeUri");
			int type = intent.getIntExtra("Type", -1);
			ThemeLog.i(TAG, "action = " + action);
			ThemeLog.d(TAG, "uri = " + getThemeName(uri));
			ThemeLog.v(TAG, "mThemeInfo.getThemePath() = " + getThemeName(mThemeInfo.getThemePath()));
			if(type != ThemeData.LOCKSCREEN_APPLY){
				return;
			}
			if (Constant.ACTION_THEME_APPLIED.equals(action)) {
				if(uri==null||!getThemeName(uri).equals(getThemeName(mThemeInfo.getThemePath()))){
					return;
				}
				ThemeUtils.resetUsingFlagByType(ThemeDetailLockscreenActivity.this, ThemeData.THEME_ELEMENT_TYPE_LOCK_WALLPAPER);
				ThemeUtils.setUsingFlagByType(ThemeDetailLockscreenActivity.this, mThemeInfo, ThemeData.THEME_ELEMENT_TYPE_LOCK_WALLPAPER);
				ThemeUtils.resetUsingFlagByType(ThemeDetailLockscreenActivity.this, mixType);
				ThemeUtils.setUsingFlagByType(ThemeDetailLockscreenActivity.this, mThemeInfo, mixType);
				Toast.makeText(ThemeDetailLockscreenActivity.this, getText(R.string.lockwallpaper_apply_success_tip), Toast.LENGTH_SHORT).show();
			} else if (Constant.ACTION_THEME_NOT_APPLIED.equals(action)) {
				SimpleDialogs.displayOkDialog(R.string.dlg_theme_failed_title, R.string.dlg_theme_failed_body, ThemeDetailLockscreenActivity.this);
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

		// mAdapter.notifyDataSetChanged();

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
		if (mLockPre != null) {
			mLockPre.setImageDrawable(null);
		}
		unregisterIntentReceivers();
		if (mPreviews != null) {
			if (mPreviews != null && mPreviews.size() > 0) {
				for (int i = 0; i < mPreviews.size(); i++) {
					mPreviews.get(i).getBitmap().recycle();
				}
			}
			mPreviews.clear();
		}
		unregisterReceiver(mBroadcastReceiver);
		// unregisterReceiver(mBroadcastReceiver);
	}

	private void applyTheme(String theme, boolean applyFont, boolean scaleBoot, boolean removeExistingTheme) {
		ThemeLog.i(TAG,"ThemeDetailLockscreenActivity applyTheme theme1: " + theme);
		theme = copyFileToSystem(theme);
		ThemeLog.i(TAG,"ThemeDetailLockscreenActivity applyTheme theme2: " + theme);
		
		IThemeManagerService ts = IThemeManagerService.Stub.asInterface(ServiceManager.getService("ThemeService"));
		try {
			ThemeLog.i(TAG, "theme=" + theme + " ,mixType(11==lockscreen)=" + mixType);

			if (mixType == ThemeData.THEME_ELEMENT_TYPE_ICONS) {
				ts.applyThemeIcons(ThemeContentProvider.CONTENT + FileUtils.stripPath(theme));
			} else if (mixType == ThemeData.THEME_ELEMENT_TYPE_LOCK_WALLPAPER) {
				ts.applyThemeLockscreenWallpaper(ThemeContentProvider.CONTENT + FileUtils.stripPath(theme));
			} else {
				// ts.resetThemeLockScreen();
				ts.applyThemeLockScreen(ThemeContentProvider.CONTENT + FileUtils.stripPath(theme));
			}
			showDialog(DIALOG_PROGRESS);

		} catch (Exception e) {
			ThemeLog.e(TAG, "exception happened !");
			e.printStackTrace();
			SimpleDialogs.displayOkDialog(R.string.dlg_theme_failed_title, R.string.dlg_theme_failed_body, this);
		}
	}
	@Override
	public void deleteTheme() {
		// TODO Auto-generated method stub
		initView();
		invalidateOptionsMenu();
	}

}
