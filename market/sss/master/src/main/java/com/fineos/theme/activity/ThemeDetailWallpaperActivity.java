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
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
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
import com.fineos.theme.adapter.WallpaperPagerAdapter;
import com.fineos.theme.download.Downloads;
import com.fineos.theme.download.ReportProvider;
import com.fineos.theme.fragment.DeleteDialogFragment;
import com.fineos.theme.fragment.ProgressDialogFragment;
import com.fineos.theme.fragment.ThemeMixerFragment;
import com.fineos.theme.model.ThemeData;
import com.fineos.theme.provider.ThemeContentProvider;
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

import fineos.app.AlertDialog;
import fineos.app.ProgressDialog;
import fineos.content.res.IThemeManagerService;

public class ThemeDetailWallpaperActivity extends ThemeDetailBaseActivity {
	private static final int ACTION_THEME_PREVIEW = 1;
	private static final int ACTION_NETWORK_ERROR = 0;
	private static final int ACTION_DOWN_LOADING_WALLPAPER_PREVIEW = 2;
	private static final int ACTION_HIDE_DIALOG = 3;
	private final String lockWallpaperPath = "wallpaper/default_lock_wallpaper.png";
	private final String wallpaperPath = "wallpaper/default_wallpaper.png";
	private int wallpaperType = 2;
	private String[] choices;

	private static final String TAG = "ThemeDetailWallpaperActivity";
	private static final Boolean DEBUG = Boolean.TRUE;
	private final int DIALOG_PROGRESS = 0;
	protected ProgressDialog mProgressDialog;
	// private ThemeGallery mGallery;
	// private TextView mThemeDescription;
	private ImageView mFontPre;
	private int mCurrentId = 0;
	private ArrayList<BitmapDrawable> mPreviews;
	protected String[] mPreviewList = null;
	private TextView themeApply;
	private Display mDisplay;
	private Handler mHandler;
	private IThemeService mMarketService;
	// private ImageGalleryAdapter mAdapter;
	public static int screenWidth = 0;
	public static int screenHeight = 0;
	private final double RATO_PREVIEW = 0.7;

	private IThemeService mThemeService;
	List<String> mExcludedItemsList;
	private boolean isOnline;
	private ViewPager mViewPager;
	private PagerAdapter mPagerAdapter;
	private ArrayList<BitmapDrawable> mWallperPreviews = new ArrayList<BitmapDrawable>();
	
//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		
//		mContext = this;
//		setContentView(R.layout.local_theme_detail_lockscreen);
//
//		// mAdapter.notifyDataSetChanged();
//		isOnline = getIntent().getIntExtra("isOnline", 0) == 1;
//		mixType = (int) getIntent().getIntExtra("mixType", -1);
//		mCurrentId = getIntent().getIntExtra("currentpostion", 0);
//		mThemeInfo = ThemeMixerFragment.mThemeListSave.get(mCurrentId);
//		choices = new String[] { getResources().getString(R.string.wallpaper_apply_lockscreen_wallpaper), getResources().getString(R.string.wallpaper_apply_desktop_wallpaper),
//				getResources().getString(R.string.wallpaper_apply_all_wallpaper) };
//
//		getActionBar().setDisplayUseLogoEnabled(false);
//		getActionBar().setDisplayHomeAsUpEnabled(true);
//		getActionBar().setDisplayShowHomeEnabled(false);
//		getActionBar().setTitle(mThemeInfo.getTitle());
//
//		mProgressDialog = new ProgressDialog(this);
//		if (DEBUG)
//			dump();
//
//		initHandlers();
//		initView();
//		
//		setThemeReceiver(mThemeReceiver);
//		IntentFilter filter = new IntentFilter();
//		filter.addAction(Constant.ACTION_THEME_APPLIED);
//		filter.addAction(Constant.ACTION_THEME_NOT_APPLIED);
//		registerReceiver(mBroadcastReceiver, filter);
//		registerIntentReceivers();
//	}
//
//	private BroadcastReceiver mThemeReceiver = new BroadcastReceiver() {
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			if (intent != null && intent.getAction().equals(Downloads.ACTION_DOWNLOAD_COMPLETED_NOTIFICATION)) {
//				if (isOnline) {
//					onDownLoadComplete();
//					pageSelected();
//				}
//			}
//		}
//	};
//	
//	private void onDownLoadComplete() {
//		ThemeData tempinfoData = mThemeInfo;
//		int position = ThemeMixerFragment.mThemeListSave.indexOf(mThemeInfo);
//		mThemeInfo = ThemeUtils.getThemeByFildId(mContext, mThemeInfo.getPackageName());		
//		ThemeMixerFragment.mThemeListSave.set(position, mThemeInfo);
//		mPagerAdapter.notifyDataSetChanged();
//		pageSelected();
//	}
//	
//	private void pageSelected() {
//		invalidateOptionsMenu();
//		changeViewLp();
//	}
//	
//	private void changeViewLp() {
//		if (isOnline) {
//			int downloadStatus = getDownloadStatus(mThemeInfo.getDownloadUrl());
//			if (downloadStatus == DownloadManager.STATUS_PENDING||downloadStatus == DownloadManager.STATUS_RUNNING
//					||downloadStatus == DownloadManager.STATUS_PAUSED) {
//				themeApply.setEnabled(false);
//				themeApply.setBackgroundResource(R.drawable.ic_btn_downloading);
//				themeApply.setPadding(0, 0, 0, 0);
//				themeApply.setText(R.string.btn_downloading);
//				themeApply.setTextColor(getResources().getColor(R.color.text_color));
//			} else if (isThemeCanDelete()) {
//				themeApply.setEnabled(true);
//				themeApply.setBackgroundResource(R.drawable.ic_btn_apply);
//				themeApply.setPadding(0, 0, 0, 0);
//				themeApply.setText(getResources().getString(R.string.btn_use));
//				themeApply.setTextColor(getResources().getColor(R.color.white));
//				themeApply.setOnClickListener(new OnClickListener() {
//					@Override
//					public void onClick(View v) {
//
//						AlertDialog dialog = new AlertDialog.Builder(ThemeDetailWallpaperActivity.this).setTitle(R.string.btn_use).setItems(choices, onselect).create();
//						dialog.show();
//					}
//				});
//			} else {
//				themeApply.setEnabled(true);
//				themeApply.setBackgroundResource(R.drawable.ic_btn_download);
//				themeApply.setPadding(0, 0, 0, 0);
//				themeApply.setText(getResources().getString(R.string.btn_download));
//				themeApply.setTextColor(getResources().getColor(R.color.text_color));
//				themeApply.setOnClickListener(new OnClickListener() {
//					@Override
//					public void onClick(View v) {
//						if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
//							Toast.makeText(mContext, getResources().getString(R.string.sd_unmounted), Toast.LENGTH_SHORT).show();
//							return;
//						}
//						changeCurrentThemeInfo(ThemeMixerFragment.mThemeListSave.get(mViewPager.getCurrentItem()));
//						startDownload();
//						if (themeApply != null) {
//							themeApply.setEnabled(false);
//							themeApply.setText(R.string.btn_downloading);
//						}
//					}
//				});
//			}
//			
//		} else {
//			
//			themeApply.setEnabled(true);
//			themeApply.setBackgroundResource(R.drawable.ic_btn_apply);
//			themeApply.setPadding(0, 0, 0, 0);
//			themeApply.setText(getResources().getString(R.string.btn_use));
//			themeApply.setTextColor(getResources().getColor(R.color.white));
//			themeApply.setOnClickListener(new OnClickListener() {
//				@Override
//				public void onClick(View v) {
//
//					AlertDialog dialog = new AlertDialog.Builder(ThemeDetailWallpaperActivity.this).setTitle(R.string.btn_use).setItems(choices, onselect).create();
//					dialog.show();
//				}
//			});
//		}
//	}
//
//	public static void updateSysteFontConfiguration(FontResource fontRes) {
////		Configuration curConfig = new Configuration();
////		try {
////			curConfig.fontPackageName = fontRes.getPackageName();
////			curConfig.fontPath = fontRes.getFontFilePath();
////			ActivityManagerNative.getDefault().updatePersistentConfiguration(curConfig);
////		} catch (RemoteException e) {
////
////		}
//	}
//
//	private void initView() {
////		if (isOnline && Util.checkDownload(mContext, mThemeInfo.getPackageName())) {
////			Log.v(TAG, "checkDownload ");
////			mThemeInfo = ThemeUtils.getThemeByFildId(mContext, mThemeInfo.getPackageName());
////			Log.v(TAG, "checkDownload =" + mThemeInfo);
////			Log.v(TAG, "checkDownload =" + mThemeInfo.getTitle());
////			Log.v(TAG, "checkDownload =" + mThemeInfo.getPreviewsList());
//////			isOnline = false;
////		}
//
//		if (isOnline) {
//			mThemeService = ThemeService.getServiceInstance(this);
//		} else {
//			mExcludedItemsList = new ArrayList<String>();
//		}
//		mDisplay = getWindowManager().getDefaultDisplay();
//		screenWidth = mDisplay.getWidth();
//		screenHeight = mDisplay.getHeight();
//		themeApply = (TextView) findViewById(R.id.btn_theme_apply);
//		if (isOnline) {
//			int downloadStatus = getDownloadStatus(mThemeInfo.getDownloadUrl());
//			if (downloadStatus == DownloadManager.STATUS_PENDING||downloadStatus == DownloadManager.STATUS_RUNNING
//					||downloadStatus == DownloadManager.STATUS_PAUSED) {
//				themeApply.setEnabled(false);
//				themeApply.setBackgroundResource(R.drawable.ic_btn_downloading);
//				themeApply.setPadding(0, 0, 0, 0);
//				themeApply.setText(R.string.btn_downloading);
//				themeApply.setTextColor(getResources().getColor(R.color.text_color));
//			}else {
//				themeApply.setEnabled(true);
//				themeApply.setBackgroundResource(R.drawable.ic_btn_download);
//				themeApply.setPadding(0, 0, 0, 0);
//				themeApply.setText(getResources().getString(R.string.btn_download));
//				themeApply.setTextColor(getResources().getColor(R.color.text_color));
//				themeApply.setOnClickListener(new OnClickListener() {
//					@Override
//					public void onClick(View v) {
//						if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
//							Toast.makeText(mContext, getResources().getString(R.string.sd_unmounted), Toast.LENGTH_SHORT).show();
//							return;
//						}
//						changeCurrentThemeInfo(ThemeMixerFragment.mThemeListSave.get(mViewPager.getCurrentItem()));
//						startDownload();
//						if (themeApply != null) {
//							themeApply.setEnabled(false);
//							themeApply.setText(R.string.btn_downloading);
//						}
//					}
//				});
//			}
//			
//		} else {
//			
//			themeApply.setEnabled(true);
//			themeApply.setBackgroundResource(R.drawable.ic_btn_apply);
//			themeApply.setPadding(0, 0, 0, 0);
//			themeApply.setText(getResources().getString(R.string.btn_use));
//			themeApply.setTextColor(getResources().getColor(R.color.white));
//			themeApply.setOnClickListener(new OnClickListener() {
//				@Override
//				public void onClick(View v) {
//
//					AlertDialog dialog = new AlertDialog.Builder(ThemeDetailWallpaperActivity.this).setTitle(R.string.btn_use).setItems(choices, onselect).create();
//					dialog.show();
//				}
//			});
//		}
//
//		if (isOnline) {
//			for (int i = 0 ; i < ThemeMixerFragment.mThemeListSaveOnLineTemp.size(); i++) {
//				showLoadingDialog();
//				addOnlinePreviewRequest(ThemeMixerFragment.mThemeListSaveOnLineTemp.get(i), i);
//			}
//			
//		} else {
//			
//			preloadImages();
//		}
//		
//		mViewPager = (ViewPager) findViewById(R.id.wallpaper_viewpager);
//		mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//			
//			@Override
//			public void onPageSelected(int postion) {
//				// TODO Auto-generated method stub
//				changeCurrentThemeInfo(ThemeMixerFragment.mThemeListSave.get(mViewPager.getCurrentItem()));
//				getActionBar().setTitle(mThemeInfo.getTitle());
//				pageSelected();
////				mCurrentId = postion;
//			}
//			
//			@Override
//			public void onPageScrolled(int arg0, float arg1, int arg2) {
//				// TODO Auto-generated method stub
//				
//			}
//			
//			@Override
//			public void onPageScrollStateChanged(int arg0) {
//				// TODO Auto-generated method stub
//				
//			}
//		});
//		mPagerAdapter = new WallpaperPagerAdapter(ThemeMixerFragment.mThemeListSave, getApplicationContext(), mWallperPreviews);
//		mViewPager.setAdapter(mPagerAdapter);
//		mViewPager.setCurrentItem(mCurrentId, false);
//	}
//
//	DialogInterface.OnClickListener onselect = new DialogInterface.OnClickListener() {
//		@Override
//		public void onClick(DialogInterface dialog, int which) {
//			wallpaperType = which + 1;
//			ReportProvider.postUserTheme(ThemeUtils.getpackageName(mThemeInfo, false), ThemeData.THEME_REPORT_SORT_USE);
//			changeCurrentThemeInfo(ThemeMixerFragment.mThemeListSave.get(mViewPager.getCurrentItem()));
//			applyTheme(mThemeInfo.getThemePath(), false, false, true);
//			if (themeApply != null) {
//				themeApply.setEnabled(false);
//			}
//		}
//
//	};
//
//	private void preloadImages() {
//		(new PreviewLoaderAsyncTask()).execute();
//	}
//
//	private class PreviewLoaderAsyncTask extends AsyncTask {
//
//		@Override
//		protected Object doInBackground(Object[] params) {
//			for (int i = 0; i < ThemeMixerFragment.mThemeListSave.size(); i++) {
//				BitmapDrawable bd = loadLocalWallperPreview(ThemeMixerFragment.mThemeListSave.get(i));
//				if (bd != null) {
//					Message msg = Message.obtain(mHandler, ACTION_THEME_PREVIEW, bd);
//					mHandler.sendMessage(msg);
//				}
//			}
//			return null;
//		}
//
//		@Override
//		protected void onPostExecute(Object o) {
//			super.onPostExecute(o);
//			
//		}
//	} // end of PreviewLoaderAsyncTask
//	
//	private BitmapDrawable loadLocalWallperPreview(ThemeData themeData) {
//		BitmapDrawable bitmapDrawable = null;
//		String[] previewDrawblesize = ThemeUtils.getPreviewListByType(mixType, themeData);
//		if (previewDrawblesize == null || previewDrawblesize.length == 0) {
//			return null;
//		}
//		mPreviews = new ArrayList<BitmapDrawable>(previewDrawblesize.length);
//		try {
//			ZipFile zip = new ZipFile(themeData.getThemePath());
//			ZipEntry ze;
//
//			final Resources res = getResources();			
//			for (int j = 0; j < previewDrawblesize.length; j++) {
//				ze = zip.getEntry(previewDrawblesize[j]);
//				InputStream is = ze != null ? zip.getInputStream(ze) : null;
//				if (is != null) {
//					BitmapFactory.Options opts = new BitmapFactory.Options();
//					opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
//					opts.inSampleSize = 1;
//					Bitmap bmp = BitmapFactory.decodeStream(is, null, opts);
//					final BitmapDrawable drawable = new BitmapDrawable(res, bmp);
//					mPreviews.add(drawable);
//				}
//			}
//			bitmapDrawable = mPreviews.get(0);
//		} catch (IOException e) {
//			bitmapDrawable = null;
//		}
//		return bitmapDrawable;
//	}
//
//	private void addPreviewRequest() {
//		showLoadingDialog();
//		
//		Request request = new Request(0, Constant.TYPE_THEME_PREVIEW);
//		String[] previewUrls = mThemeInfo.getPreviewUrl();
//
//		if (previewUrls == null || previewUrls.length == 0) {
//			Message msg = Message.obtain(mHandler, ACTION_THEME_PREVIEW, null);
//			mHandler.sendMessage(msg);
//			return;
//		}
//
//		request.setData(previewUrls);
//		request.addObserver(new Observer() {
//
//			@Override
//			public void update(Observable observable, Object data) {
//				// TODO Auto-generated method stub
//				Request request = (Request) observable;
//				switch (request.getStatus()) {
//				case Constant.STATUS_SUCCESS:
//					Message msg = Message.obtain(mHandler, ACTION_THEME_PREVIEW, data);
//					mHandler.sendMessage(msg);
//					break;
//				case Constant.STATUS_ERROR:
//					mHandler.sendEmptyMessage(ACTION_NETWORK_ERROR);
//					break;
//				default:
//					break;
//				}
//			}
//		});
//
//		mThemeService.getThemePreviews(request);
//	}
//	
//	private void addOnlinePreviewRequest(ThemeData themeData, final int postion) {		
//		Request request = new Request(0, Constant.TYPE_THEME_PREVIEW);
//		String[] previewUrls = themeData.getPreviewUrl();
//
//		if (previewUrls == null || previewUrls.length == 0) {
//			Message msg = Message.obtain(mHandler, ACTION_THEME_PREVIEW, null);
//			mHandler.sendMessage(msg);
//			return;
//		}
//
//		request.setData(previewUrls);
//		request.addObserver(new Observer() {
//
//			@Override
//			public void update(Observable observable, Object data) {
//				// TODO Auto-generated method stub
//				Request request = (Request) observable;
//				switch (request.getStatus()) {
//				case Constant.STATUS_SUCCESS:
//					if ((postion + 1) >= ThemeMixerFragment.mThemeListSave.size()) {
//						Message msg = Message.obtain(mHandler, ACTION_THEME_PREVIEW, data);
//						mHandler.sendMessage(msg);
//					} else {
//						Message msg = Message.obtain(mHandler, ACTION_DOWN_LOADING_WALLPAPER_PREVIEW, data);
//						mHandler.sendMessage(msg);
//					}
//					
//					break;
//				case Constant.STATUS_ERROR:
//					mHandler.sendEmptyMessage(ACTION_NETWORK_ERROR);
//					break;
//				default:
//					break;
//				}
//			}
//		});
//
//		mThemeService.getThemePreviews(request);
//	}
//
//	private void initHandlers() {
//		// TODO Auto-generated method stub
//		mHandler = new Handler() {
//
//			@SuppressWarnings("unchecked")
//			@Override
//			public void handleMessage(Message msg) {
//				// TODO Auto-generated method stub
//				switch (msg.what) {
//				case ACTION_THEME_PREVIEW:
//					
//					hideLoadingDialog();
//					if (isOnline) {
//						mPreviews = (ArrayList<BitmapDrawable>) msg.obj;
//						if (msg.obj != null) {
//							mWallperPreviews.add(((ArrayList<BitmapDrawable>) msg.obj).get(0));
//							mPagerAdapter.notifyDataSetChanged();
//						}
//					} else {
//						mWallperPreviews.add((BitmapDrawable)msg.obj);
//						mPagerAdapter.notifyDataSetChanged();
//					}
//					break;					
//				case ACTION_NETWORK_ERROR:
//					hideLoadingDialog();											
//					Toast.makeText(mContext, mContext.getString(R.string.error_network_low_speed), Toast.LENGTH_LONG).show();
//					break;
//				case ACTION_DOWN_LOADING_WALLPAPER_PREVIEW:
//						if (msg.obj != null) {
//							mWallperPreviews.add(((ArrayList<BitmapDrawable>) msg.obj).get(0));
//							mPagerAdapter.notifyDataSetChanged();	
//						} 			
//					break;
//				case ACTION_HIDE_DIALOG:
//					hideLoadingDialog();
//					break;
//				default:
//					break;
//				}
//			}
//		};
//	}
//
//	@Override
//	protected Dialog onCreateDialog(int id) {
//		super.onCreateDialog(id);
//		switch (id) {
//		case DIALOG_PROGRESS:
//			mProgressDialog = new ProgressDialog(this);
//			mProgressDialog.setMessage(getResources().getText(R.string.applying_font));
//			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//			mProgressDialog.setCancelable(false);
//			mProgressDialog.setProgress(0);
//			mProgressDialog.show();
//			return mProgressDialog;
//		default:
//			return super.onCreateDialog(id);
//		}
//	}
//	private String getThemeName(String uri){
//		String themeName;
//		themeName = uri == null?uri:uri.substring(uri.lastIndexOf("/")+1, uri.lastIndexOf("."));
//		return themeName;
//	}
//	private String getWallpaperName(String path){
//		String themeName;
//		String uri = path.split("##_##")[0];
//		Log.e(TAG, "sss getWallpaperName uri = " + uri.split("##_##")[0]);
//		themeName = uri == null?uri:uri.substring(uri.lastIndexOf("/")+1, uri.lastIndexOf("."));
//		return themeName;
//	}
//	private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			String action = intent.getAction();
//			mProgressDialog.dismiss();
//			ThemeLog.e(TAG, "action = " + action);
//			String uri = intent.getStringExtra("ThemeUri");
//			int type = intent.getIntExtra("Type", -1);
//			ThemeLog.e(TAG, "action = " + action);
//			Log.e(TAG, "sss uri = " + uri);
//			Log.e(TAG, "sss uri = " + getWallpaperName(uri));
//			Log.e(TAG, "sss mThemeInfo.getThemePath() = " + getThemeName(mThemeInfo.getThemePath()));
//			Log.e(TAG, "sss Type = " + type);
//			if(wallpaperType == 1){
//				if(type != ThemeData.LOCKSCREEN_WALLPAPER_APPLY){
//					return;
//				}
//			}
//			if(wallpaperType == 2){
//				if(type != ThemeData.DESKTOP_WALLPAPER_APPLY){
//					return;
//				}
//			}
//			if(wallpaperType == 3){
//				if(type != ThemeData.WALLPAPER_APPLY){
//					return;
//				}
//			}
//			if (Constant.ACTION_THEME_APPLIED.equals(action)) {
//				if(uri==null||!getWallpaperName(uri).equals(getThemeName(mThemeInfo.getThemePath()))){
//					return;
//				}
//				if (wallpaperType == 1) {
//					ThemeUtils.resetUsingFlagByType(ThemeDetailWallpaperActivity.this, ThemeData.THEME_ELEMENT_TYPE_LOCK_WALLPAPER);
//					ThemeUtils.setUsingFlagByType(ThemeDetailWallpaperActivity.this, mThemeInfo, ThemeData.THEME_ELEMENT_TYPE_LOCK_WALLPAPER);
//				}
//				if (wallpaperType == 2) {
//					ThemeUtils.resetUsingFlagByType(ThemeDetailWallpaperActivity.this, ThemeData.THEME_ELEMENT_TYPE_WALLPAPER);
//					ThemeUtils.setUsingFlagByType(ThemeDetailWallpaperActivity.this, mThemeInfo, ThemeData.THEME_ELEMENT_TYPE_WALLPAPER);
//				}
//				if (wallpaperType == 3) {
//					ThemeUtils.resetUsingFlagByType(ThemeDetailWallpaperActivity.this, ThemeData.THEME_ELEMENT_TYPE_LOCK_WALLPAPER);
//					ThemeUtils.setUsingFlagByType(ThemeDetailWallpaperActivity.this, mThemeInfo, ThemeData.THEME_ELEMENT_TYPE_LOCK_WALLPAPER);
//					ThemeUtils.resetUsingFlagByType(ThemeDetailWallpaperActivity.this, ThemeData.THEME_ELEMENT_TYPE_WALLPAPER);
//					ThemeUtils.setUsingFlagByType(ThemeDetailWallpaperActivity.this, mThemeInfo, ThemeData.THEME_ELEMENT_TYPE_WALLPAPER);
//				}
//
//				Toast.makeText(ThemeDetailWallpaperActivity.this, getText(R.string.wallpaper_apply_success_tip), Toast.LENGTH_SHORT).show();
//			} else if (Constant.ACTION_THEME_NOT_APPLIED.equals(action)) {
//				SimpleDialogs.displayOkDialog(R.string.dlg_theme_failed_title, R.string.dlg_theme_failed_body, ThemeDetailWallpaperActivity.this);
//			}
//			finish();
//		}
//	};
//
//	@Override
//	protected void onResume() {
//		super.onResume();
//		StatService.onResume(this);
//
//		if (mProgressDialog != null && mProgressDialog.isShowing()) {
//			mProgressDialog.dismiss();
//		}
//		
//	}
//
//	@Override
//	protected void onPause() {
//		super.onPause();
//		StatService.onPause(this);
//		
//	}
//
//	@Override
//	protected void onDestroy() {
//		// TODO Auto-generated method stub
//		super.onDestroy();
//		// ThemeLog.e(TAG, "onDestroy,UnRegister receiver...");
//		if (mFontPre != null) {
//			mFontPre.setImageDrawable(null);
//		}
//		unregisterIntentReceivers();
//		if (mPreviews != null) {
//			if (mPreviews != null && mPreviews.size() > 0) {
//				for (int i = 0; i < mPreviews.size(); i++) {
//					mPreviews.get(i).getBitmap().recycle();
//				}
//			}
//			mPreviews.clear();
//		}
//		unregisterReceiver(mBroadcastReceiver);
//	}
//
//	@SuppressWarnings("deprecation")
//	private void applyTheme(String theme, boolean applyFont, boolean scaleBoot, boolean removeExistingTheme) {
//		Log.i("","ThemeDetailWallpaperActivity applyTheme theme1: " + theme);
//		theme = copyFileToSystem(theme);
//		Log.i("","ThemeDetailWallpaperActivity applyTheme theme2: " + theme);
//		
//		IThemeManagerService ts = IThemeManagerService.Stub.asInterface(ServiceManager.getService("ThemeService"));
//		try {
//			Log.i(TAG, "applyTheme stripPath =" + ThemeContentProvider.CONTENT + FileUtils.stripPath(theme));
//			Log.i(TAG, "applyTheme stripPath =" + wallpaperType);
//			Log.i(TAG, "applyTheme mixType =" + mixType);
//			if (mixType == ThemeData.THEME_ELEMENT_TYPE_LOCK_WALLPAPER) {
//
//				if (wallpaperType == 1) {
//					ts.applyThemeLockscreenWallpaperByName(ThemeContentProvider.CONTENT + FileUtils.stripPath(theme), lockWallpaperPath);
//				} else if (wallpaperType == 2) {
//					ts.applyThemeDesktopWallpaperByName(ThemeContentProvider.CONTENT + FileUtils.stripPath(theme), lockWallpaperPath);
//				} else if (wallpaperType == 3) {
//					ts.applyThemeWallpaperByName(ThemeContentProvider.CONTENT + FileUtils.stripPath(theme), lockWallpaperPath);
//				}
//			}
//			if (mixType == ThemeData.THEME_ELEMENT_TYPE_WALLPAPER) {
//
//				if (wallpaperType == 1) {
//					ts.applyThemeLockscreenWallpaperByName(ThemeContentProvider.CONTENT + FileUtils.stripPath(theme), wallpaperPath);
//				} else if (wallpaperType == 2) {
//					ts.applyThemeDesktopWallpaperByName(ThemeContentProvider.CONTENT + FileUtils.stripPath(theme), wallpaperPath);
//				} else if (wallpaperType == 3) {
//					ts.applyThemeWallpaperByName(ThemeContentProvider.CONTENT + FileUtils.stripPath(theme), wallpaperPath);
//				}
//			}
//			Log.i(TAG, "applyTheme showDialog");
//			mProgressDialog.setMessage(getResources().getText(R.string.applying_wallpaper));
//			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//			mProgressDialog.setCancelable(false);
//			mProgressDialog.setProgress(0);
//			mProgressDialog.show();
//		} catch (Exception e) {
//			Log.i(TAG, "exception happened !");
//			e.printStackTrace();
//			SimpleDialogs.displayOkDialog(R.string.dlg_theme_failed_title, R.string.dlg_theme_failed_body, ThemeDetailWallpaperActivity.this);
//		}
//	}
//
//	@Override
	public void deleteTheme() {
		// TODO Auto-generated method stub
//		ThemeMixerFragment.mThemeListSave.remove(mViewPager.getCurrentItem());
//		ThemeMixerFragment.mThemeListSaveOnLineTemp.remove(mViewPager.getCurrentItem());
//		mWallperPreviews.remove(mViewPager.getCurrentItem());
//		mPagerAdapter.notifyDataSetChanged();
	}

}
