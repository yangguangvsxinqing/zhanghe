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

import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.DownloadManager;
import android.app.FragmentManager;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mobstat.StatService;
import com.fineos.theme.R;
import com.fineos.theme.download.Constants;
import com.fineos.theme.download.Downloads;
import com.fineos.theme.download.ReportProvider;
import com.fineos.theme.fragment.DeleteDialogFragment;
import com.fineos.theme.fragment.ProgressDialogFragment;
import com.fineos.theme.model.ThemeData;
import com.fineos.theme.utils.Constant;
import com.fineos.theme.utils.FileManager;
import com.fineos.theme.utils.SimpleDialogs;
import com.fineos.theme.utils.ThemeLog;
import com.fineos.theme.utils.ThemeUtils;
import com.fineos.theme.utils.Util;
import com.fineos.theme.webservice.IThemeService;
import com.fineos.theme.webservice.Request;
import com.fineos.theme.webservice.ThemeService;
import android.content.pm.PackageInstallObserver;
import android.content.pm.IPackageInstallObserver;
import fineos.app.ProgressDialog;

public class ThemeDetailDynamicWallpaperActivity extends ThemeDetailBaseActivity {
	private static final int ACTION_THEME_PREVIEW = 1;
	private static final int ACTION_NETWORK_ERROR = 0;

	private static final String TAG = "FineOSTheme.ThemeDetailActivity";
	private static final Boolean DEBUG = Boolean.TRUE;

	protected static final int DIALOG_PROGRESS = 0;
	protected static final int DIALOG_DELAY = 1;

	protected ProgressDialog mProgressDialog;
	protected AlertDialog mDialog;
//	private Gallery mGallery;
	private ImageView mImage;
//	private TextView mThemeDescription;
	private ArrayList<BitmapDrawable> mPreviews;
	protected String[] mPreviewList = null;
	private TextView themeApply;
	private Display mDisplay;
	private Handler mHandler;
	private IThemeService mMarketService;
	private ImageGalleryAdapter mAdapter;
	public static int screenWidth = 0;
	public static int screenHeight = 0;

	private IThemeService mThemeService;
	private DownloadManager downloadManager;
	private WallpaperInfo mInfo;
	private List<String> mExcludedItemsList;
	private ThemeData mThemeInfo;
	private boolean isOnline;
	private int themeId;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.local_theme_detail_dynamic_wallpaper);
		ThemeLog.v(TAG, "onResume...............................");
		mContext = this;
		themeId = (int) getIntent().getLongExtra("themeid", 0);
		if (themeId != 0) {
			mThemeInfo = ThemeUtils.getThemeEntryById(themeId, mContext);
		} else {
			mThemeInfo = (ThemeData) getIntent().getSerializableExtra("themeInfo");
		}
		isOnline = getIntent().getIntExtra("isOnline", 0) == 1;
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		
		
		initHandlers();
		initView();
	
		getActionBar().setDisplayUseLogoEnabled(false);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setDisplayShowHomeEnabled(false);
		getActionBar().setTitle(mThemeInfo.getTitle());	
		showSystemUi(false);
		setThemeReceiver(mThemeReceiver);
		registerIntentReceivers();
		IntentFilter intentFilter = new IntentFilter(Downloads.ACTION_INSTALL_COMPLETED_NOTIFICATION);
		intentFilter.addDataScheme("package");
		mContext.registerReceiver(mThemeInstallReceiver, intentFilter);
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
        mImage.setSystemUiVisibility(flag);
    }

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		return super.onTouchEvent(event);
	}
	
	private BroadcastReceiver mThemeInstallReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			ThemeLog.v(TAG, "mThemeInstallReceiver intent.getAction() ="+intent.getAction());
			if(intent.getAction().equals(Downloads.ACTION_INSTALL_COMPLETED_NOTIFICATION)){
				String packageName = intent.getData().getSchemeSpecificPart();
				if(packageName.equals(mThemeInfo.getPackageName())){
					checkThemeApplyState();
				}
			}
		}
	};
	
	private BroadcastReceiver mThemeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			ThemeLog.v(TAG, "mThemeReceiver intent.getAction() ="+intent.getAction());
			
			if(intent.getAction().equals(Downloads.ACTION_DOWNLOAD_COMPLETED_NOTIFICATION)){
				checkThemeApplyState();
				invalidateOptionsMenu();
			}
		}
	};

	public boolean checkInstall(String packageName) {
		PackageManager mPackageManager = mContext.getPackageManager();

		List<ResolveInfo> list = mPackageManager.queryIntentServices(new Intent(WallpaperService.SERVICE_INTERFACE), PackageManager.GET_META_DATA);
		for (ResolveInfo resolveInfo : list) {
			WallpaperInfo info = null;
			try {
				info = new WallpaperInfo(mContext, resolveInfo);
				ThemeLog.v(TAG, "WallpaperInfo packageName=" + packageName);
				ThemeLog.v(TAG, "WallpaperInfo info=" + info.getPackageName());
				if (info.getPackageName().equals(packageName)) {
					mInfo = info;
					return true;
				}
			} catch (XmlPullParserException e) {
				ThemeLog.w(TAG, "Skipping wallpaper " + resolveInfo.serviceInfo, e);
				continue;
			} catch (IOException e) {
				ThemeLog.w(TAG, "Skipping wallpaper " + resolveInfo.serviceInfo, e);
				continue;
			}
		}

		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		ThemeLog.i(TAG, "onCreateOptionsMenu...............................");

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.theme_detail, menu);

		if (Util.checkDownload(mThemeInfo.getDownloadUrl())) {

			return true;
		}

		return false;
	}

	private void initView() {

		mThemeService = ThemeService.getServiceInstance(this);
		downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

		mDisplay = getWindowManager().getDefaultDisplay();
		screenWidth = mDisplay.getWidth();
		screenHeight = mDisplay.getHeight();
//		mGallery = (Gallery) findViewById(R.id.gallery_image);
//		((AlignLeftGallery) mGallery).alignGalleryToLeft();
		mImage = (ImageView) findViewById(R.id.image);
//		mThemeDescription = (TextView) findViewById(R.id.theme_info_description);
		themeApply = (TextView) findViewById(R.id.btn_theme_apply);

		checkThemeApplyState();

//		mThemeDescription.setText(mThemeInfo.getDescription());
		mPreviews = new ArrayList<BitmapDrawable>();
		if (isOnline) {
			addPreviewRequest();
		} else {
			preloadImages();
		}
	}
	
	@Override
	public void deleteItems(){
		String fileName = mThemeInfo.getDownloadUrl().substring(mThemeInfo.getDownloadUrl().lastIndexOf("/") + 1);
		File file = new File(Environment.getExternalStorageDirectory() + FileManager.APP_DIR_NAME + "/" + fileName);
		if (file.exists()) {
			file.delete();
		}
		sendBroadcast(new Intent(Downloads.ACTION_DOWNLOAD_COMPLETED_NOTIFICATION));
		finish();
	}
	
	private void checkThemeApplyState(){
		if (checkInstall(mThemeInfo.getPackageName())) {
			themeApply.setEnabled(true);
			themeApply.setBackgroundResource(R.drawable.ic_btn_apply);
			themeApply.setPadding(0, 0, 0, 0);
			themeApply.setText(getResources().getString(R.string.btn_apply));
			themeApply.setTextColor(getResources().getColor(R.color.white));
			themeApply.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						ReportProvider.postUserTheme(mThemeInfo.getPackageName(), ThemeData.THEME_REPORT_SORT_USE);

						WallpaperManager mWallpaperManager = WallpaperManager.getInstance(mContext);
						mWallpaperManager.getIWallpaperManager().setWallpaperComponent(mInfo.getComponent());
						mWallpaperManager.setWallpaperOffsetSteps(0.5f, 0.0f);
						mWallpaperManager.setWallpaperOffsets(v.getRootView().getWindowToken(), 0.5f, 0.0f);
						setResult(RESULT_OK);
						
						Toast.makeText(ThemeDetailDynamicWallpaperActivity.this, 
								getText(R.string.live_apply_success_tip), Toast.LENGTH_SHORT).show();
						
						if (themeApply != null) {
							themeApply.setEnabled(false);
						}
					} catch (RemoteException e) {
						// do nothing
					} catch (RuntimeException e) {
						ThemeLog.w(TAG, "Failure setting wallpaper", e);
					}
				}
			});
		} else if (!Util.checkDownload(mThemeInfo.getDownloadUrl())) {

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
							themeApply.setEnabled(false);
							themeApply.setBackgroundResource(R.drawable.ic_btn_downloading);
							themeApply.setPadding(0, 0, 0, 0);
							themeApply.setText(R.string.btn_downloading);
							themeApply.setTextColor(getResources().getColor(R.color.text_color));
						}
					}
				});
			
		} else if (Util.checkDownloadSratus(mThemeInfo.getDownloadUrl(),mThemeInfo)){
			int downloadStatus = getDownloadStatus(mThemeInfo.getDownloadUrl());
			if (downloadStatus == DownloadManager.STATUS_PENDING||downloadStatus == DownloadManager.STATUS_RUNNING
					||downloadStatus == DownloadManager.STATUS_PAUSED) {
				themeApply.setEnabled(false);
				themeApply.setBackgroundResource(R.drawable.ic_btn_downloading);
				themeApply.setPadding(0, 0, 0, 0);
				themeApply.setText(R.string.btn_downloading);
				themeApply.setTextColor(getResources().getColor(R.color.text_color));
			} else {
				themeApply.setEnabled(true);
				themeApply.setBackgroundResource(R.drawable.ic_btn_download);
				themeApply.setPadding(0, 0, 0, 0);
				themeApply.setText(getResources().getString(R.string.btn_install));
				themeApply.setTextColor(getResources().getColor(R.color.text_color));
				themeApply.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						String fileName = mThemeInfo.getDownloadUrl().substring(mThemeInfo.getDownloadUrl().lastIndexOf("/") + 1);
						File file = new File(Environment.getExternalStorageDirectory() + FileManager.APP_DIR_NAME + "/" + fileName);
						 PackageManager pm = mContext.getPackageManager();
					        PackageInfo info = pm.getPackageArchiveInfo(Environment.getExternalStorageDirectory() + FileManager.APP_DIR_NAME + "/" + fileName, PackageManager.GET_ACTIVITIES);
					        ApplicationInfo appInfo = null;
					        String packageName = null;
					        if (info != null) {
					            appInfo = info.applicationInfo;
					            packageName = appInfo.packageName;
					        }
     	       			   int installFlags = 0;
     	       			   try {
     	       
						PackageInfo pi = pm.getPackageInfo(packageName,
	       	                        PackageManager.GET_UNINSTALLED_PACKAGES);
	       	                if(pi != null) {
	       	                    installFlags |= PackageManager.INSTALL_REPLACE_EXISTING;
	       	                }
	       	            } catch (NameNotFoundException e) {
	       	            }
	       			   PackageInstallObserver observer = new PackageInstallObserver(mContext,Environment.getExternalStorageDirectory()
	       					+ FileManager.APP_DIR_NAME + "/" + fileName);
//	       			   Intent installingIntent = new Intent(ACTION_APP_INSTALLING);
//
//	       			   mContext.sendBroadcast(installingIntent);
	       	           pm.installPackage(Uri.fromFile(file), observer, installFlags, packageName);
	       	           StatService.onEvent(mContext, "InstallDynamicWallpaper", mThemeInfo.getTitle(),1);
		        }
						
						
//				try {
//							Intent install = new Intent(Intent.ACTION_VIEW);
//							install.setDataAndType(Uri.fromFile(file), Constants.MIMETYPE_APK);
//							install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//							startActivity(install);
//
//						} catch (Exception e) {
//							e.printStackTrace();
//						}
				});
			}
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
			String[] asyncPreviewList = mPreviewList;
			try {
				ZipFile zip = new ZipFile(mThemeInfo.getThemePath());
				ZipEntry ze;

				final Resources res = getResources();
				for (int j = 0; j < asyncPreviewList.length; j++) {
					ThemeLog.i(TAG, "mPreviewList[j]=" + asyncPreviewList[j]);
					ThemeLog.i(TAG, "mixType=" + mixType);
					if (mixType < 0) { // -1 ,from all theme previews

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
						BitmapDrawable drawable = new BitmapDrawable(res, bmp);

						mPreviews.add(drawable);
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

					// free old mAdapter
					if (mAdapter != null) {
						mAdapter = null;
					}
					mAdapter = new ImageGalleryAdapter(ThemeDetailDynamicWallpaperActivity.this);

//					if (mGallery != null) {
//						mGallery.setAdapter(mAdapter);
//					}
					if (mPreviews != null && mPreviews.size() > 0) {
						mImage.setImageDrawable(mPreviews.get(0));
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
	protected void startDownload() {
		String downloadUrl = mThemeInfo.getDownloadUrl();
		String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1);

		if (Util.checkDownload(mThemeInfo.getDownloadUrl())) {
			Toast.makeText(this, getResources().getString(R.string.file_downloaded), Toast.LENGTH_SHORT).show();
			return;
		}

		if (getDownloadingCount()>=5) {
			Toast.makeText(this, getResources().getString(R.string.file_download_more), Toast.LENGTH_SHORT).show();
			return;
		}
		
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
		request.setDestinationInExternalPublicDir(FileManager.APP_DIR_NAME, fileName);
		request.setTitle(fileName);

		long id = downloadManager.enqueue(request);
		Toast.makeText(mContext, getResources().getString(R.string.start_download), Toast.LENGTH_SHORT).show();

		ReportProvider.postUserTheme(mThemeInfo.getPackageName(), ThemeData.THEME_REPORT_SORT_DOWNLOAD_S);
		StatService.onEvent(this, "DownloadDynamicWallpaper", mThemeInfo.getTitle(),1);

		SharedPreferences.Editor editor = mContext.getSharedPreferences("DOWNLOAD_PATH", MODE_PRIVATE).edit();
		editor.putString(id + "", Environment.getExternalStorageDirectory() + "/" + FileManager.APP_DIR_NAME + "/" + fileName);
		editor.commit();

		editor = mContext.getSharedPreferences("DOWNLOAD_FILE", MODE_PRIVATE).edit();
		editor.putString(id + "", mThemeInfo.getPackageName());
		editor.commit();
	}

	class ImageGalleryAdapter extends BaseAdapter {
		private Context mContext;
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
			lp = new Gallery.LayoutParams((int) (screenWidth * 0.54), (int) (screenHeight * 0.54)); // 388
																									// x
																									// 688
			holder.galleryItem.setLayoutParams(lp);
			if (0 != mPreviews.size()) {
				holder.galleryItem.setImageDrawable(mPreviews.get(position % mPreviews.size()));
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

	private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			ThemeLog.i(TAG, "action = " + action);
			if (Constant.ACTION_THEME_APPLIED.equals(action)) {

				ThemeUtils.resetUsingFlagByType(ThemeDetailDynamicWallpaperActivity.this, mixType);
				ThemeUtils.setUsingFlagByType(ThemeDetailDynamicWallpaperActivity.this, mThemeInfo, mixType);
				if (mixType == ThemeData.THEME_ELEMENT_TYPE_ICONS) {
					StatService.onEvent(mContext, "ApplyIcon", mThemeInfo.getTitle(),1);
					Toast.makeText(ThemeDetailDynamicWallpaperActivity.this, getText(R.string.icon_apply_success_tip), Toast.LENGTH_SHORT).show();

				} else {
					StatService.onEvent(mContext, "ApplyDynamicWallpaper", mThemeInfo.getTitle(), 1);
					ThemeUtils.resetUsingFlagByType(ThemeDetailDynamicWallpaperActivity.this, ThemeData.THEME_ELEMENT_TYPE_WALLPAPER);
					Toast.makeText(ThemeDetailDynamicWallpaperActivity.this, getText(R.string.apply_success_tip), Toast.LENGTH_SHORT).show();
				}
				// do nothing
			} else if (Constant.ACTION_THEME_NOT_APPLIED.equals(action)) {
				SimpleDialogs.displayOkDialog(R.string.dlg_theme_failed_title, R.string.dlg_theme_failed_body, ThemeDetailDynamicWallpaperActivity.this);
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
		if(themeApply!=null){
			checkThemeApplyState();
		}
		showSystemUi(false);
		
		// mAdapter.notifyDataSetChanged();
		ThemeLog.v(TAG, "onResume...............................");
		RegistReceiver();

	}

	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPause(this);

		ThemeLog.d(TAG, "onPause.................................");
		UnRegistReceiver();
	}

	public void RegistReceiver() {

		ThemeLog.d(TAG, "Register receiver............");
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constant.ACTION_THEME_APPLIED);
		filter.addAction(Constant.ACTION_THEME_NOT_APPLIED);
		registerReceiver(mBroadcastReceiver, filter);

	}

	public void UnRegistReceiver() {

		ThemeLog.d(TAG, "UnRegistReceiver.............");
		unregisterReceiver(mBroadcastReceiver);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mPreviews != null) {
			int mPreviewSize = mPreviews.size();
			for (int i = 0; i < mPreviewSize; i++) {
				mPreviews.get(i).getBitmap().recycle();
			}
			mPreviews.clear();
		}
		unregisterIntentReceivers();
		unregisterReceiver(mThemeInstallReceiver);
	}

//	@Override
//	public void onConfigurationChanged(Configuration newConfig) {
//		// super.onConfigurationChanged(newConfig);
//		ThemeLog.d(TAG, "onConfigurationChanged,newConfig=" + newConfig);
//	}

	@Override
	public void deleteTheme() {
		// TODO Auto-generated method stub
		checkThemeApplyState();
		invalidateOptionsMenu();
	}

}
