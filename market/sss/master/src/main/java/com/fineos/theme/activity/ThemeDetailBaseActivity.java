package com.fineos.theme.activity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import android.R.integer;
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
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.Toast;

import com.baidu.mobstat.StatService;
import com.fineos.billing.BillingWrap;
import com.fineos.billing.util.IabHelper;
import com.fineos.billing.util.IabResult;
import com.fineos.billing.util.Inventory;
import com.fineos.billing.util.Purchase;
import com.fineos.theme.R;
import com.fineos.theme.ThemeDataCache;
import com.fineos.theme.download.Downloads;
import com.fineos.theme.download.ReportProvider;
import com.fineos.theme.fragment.DeleteDialogFragment;
import com.fineos.theme.fragment.ProgressDialogFragment;
import com.fineos.theme.model.ThemeData;
import com.fineos.theme.utils.Constant;
import com.fineos.theme.utils.FileManager;
import com.fineos.theme.utils.ThemeLog;
import com.fineos.theme.utils.ThemeUtils;
import com.fineos.theme.utils.Util;
import com.fineos.theme.utils.ThemeBillingHelper;

import fineos.app.ProgressDialog;

public abstract class ThemeDetailBaseActivity extends Activity {
	protected Context mContext;
	protected ThemeData mThemeInfo;

	private BroadcastReceiver mThemeReceiver;
	protected static final String TAG = "FineOSTheme.ThemeDetailBaseActivity";

	protected static final int DIALOG_LOADING = 999;
	protected ProgressDialog loadingDialog;
	public static int mixType = -1;
	///xuqian add begin (for inapp billing)
	protected ArrayList<String> mSkuList = new ArrayList<String>() ;
	protected ThemeBillingHelper mBillingHelper = null ;
	protected String mProductID = "" ;
	protected String mPrice = "" ;
	protected HashMap<String, String> mPriceMap = new HashMap<String, String>();
	protected boolean mbSetUpSuccessed = false;
	protected boolean mbBuySuccessed = false;
	private ImageView mFirstLoadImageView;
	protected boolean mbGoogleBillingSupport = false ;
	protected String mFreeString = "" ;
	///xuqian add end 
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		mixType = (int) getIntent().getIntExtra("mixType", -1);
		if (currentapiVersion <= Build.VERSION_CODES.KITKAT) {
			setTheme(com.fineos.internal.R.style.Theme_Holo_Light_FineOS);
		} else {
			setTheme(com.fineos.internal.R.style.FineOSTheme_Material_Light_WhiteActionBar);
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
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		ThemeLog.v(TAG,"onDestroy()");
		if(mbGoogleBillingSupport&&mBillingHelper!=null){
			mBillingHelper.onDestroy();
			mBillingHelper = null ;
		}		
	}	
	///xuqian add for billing end	

	protected void showLoadingDialog() {
//		showDialog(DIALOG_LOADING);
		mFirstLoadImageView = (ImageView)findViewById(R.id.theme_loading);
		if (mFirstLoadImageView != null) {
			mFirstLoadImageView.setVisibility(View.VISIBLE);
			AnimationDrawable ad = (AnimationDrawable) mFirstLoadImageView.getBackground();
			ad.start();
		}
	}

	protected void hideLoadingDialog() {
		mFirstLoadImageView = (ImageView)findViewById(R.id.theme_loading);
		if (mFirstLoadImageView != null) {
			mFirstLoadImageView.setVisibility(View.INVISIBLE);
			AnimationDrawable ad = (AnimationDrawable) mFirstLoadImageView.getBackground();
			ad.stop();
		}
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

	public void setThemeReceiver(BroadcastReceiver mThemeReceiver) {
		this.mThemeReceiver = mThemeReceiver;
	}

	protected void registerIntentReceivers() {
		IntentFilter intentFilter = new IntentFilter(Downloads.ACTION_DOWNLOAD_COMPLETED_NOTIFICATION);

		mContext.registerReceiver(mThemeReceiver, intentFilter);
	}

	protected void unregisterIntentReceivers() {

		mContext.unregisterReceiver(mThemeReceiver);
	}

	protected void startDownload() {
		DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);

		String url = mThemeInfo.getDownloadUrl();
		String fileName = url.substring(url.lastIndexOf("/") + 1);

		ThemeLog.i(TAG, "fileName =" + fileName);
		ThemeLog.d(TAG, "context =" + mContext.getPackageName());
		ThemeLog.v(TAG, "APP_DIR_NAME =" + FileManager.APP_DIR_NAME);

		String path = Environment.getExternalStoragePublicDirectory(FileManager.THEME_DIR_PATH) + File.separator + fileName;
		if (new File(path).exists()) {
			Toast.makeText(this, getResources().getString(R.string.file_downloaded), Toast.LENGTH_SHORT).show();
			return;
		}
		if (getDownloadingCount() >= 5) {
			Toast.makeText(this, getResources().getString(R.string.file_download_more), Toast.LENGTH_SHORT).show();
			return;
		}

		// 开始下载
		Uri resource = Uri.parse(url);
		DownloadManager.Request request = new DownloadManager.Request(resource);
		// 设置文件类型
		MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
		String mimeString = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(url));
		request.setMimeType(mimeString);
		// 在通知栏中显示
		request.setShowRunningNotification(true);
		request.setVisibleInDownloadsUi(true);
		// sdcard的目录下的download文件夹
		request.setDestinationInExternalPublicDir(FileManager.THEME_DIR_PATH, fileName);
		mThemeInfo.setFileName(fileName);
		mThemeInfo.setThemePath(Environment.getExternalStorageDirectory() + FileManager.THEME_DIR_PATH + "/" + fileName);
		request.setTitle(fileName);

		long id = downloadManager.enqueue(request);
		Toast.makeText(this, getResources().getString(R.string.start_download), Toast.LENGTH_SHORT).show();

		ReportProvider.postUserTheme(mThemeInfo.getPackageName(), ThemeData.THEME_REPORT_SORT_DOWNLOAD_S);
		switch (mThemeInfo.getType()){
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
		ThemeLog.w(TAG, "themePath themeId =" + mThemeInfo.getId());

		SharedPreferences.Editor editor = mContext.getSharedPreferences("DOWNLOAD_FILE", Context.MODE_PRIVATE).edit();
		editor.putString("" + id, fileName);
		editor.commit();
	}

	protected String copyFileToSystem(String theme) {
		ThemeLog.i(TAG, "copyFileToSystem go ");

		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT && !theme.startsWith("/system")) {
			File file = new File(theme);
//			theme = "/data/data/com.fineos.theme/themes/AI01203256.ftz";
			ThemeLog.i(TAG, "theme: " + theme);

			File newFile = getDir("themes", 0);
			ThemeLog.d(TAG, "newFile = " + newFile.getPath());

			ThemeLog.v(TAG, "newFile.listFiles().length = " + newFile.listFiles().length);
			File[] listFiles = newFile.listFiles();

			ThemeLog.w(TAG, "listFiles.length........ ");
			for (int i = 0; i < listFiles.length; i++) {
				if (!listFiles[i].getName().equals(theme.substring(theme.lastIndexOf(File.separator)+1))) {
					ThemeLog.e(TAG, "delete file");
					listFiles[i].delete();
				}
			}

			newFile = new File(newFile.getPath(), theme.substring(theme.lastIndexOf(File.separator)+1));
			ThemeLog.i(TAG, "newFile = " + newFile.getPath());

			if (!newFile.exists()) {
				try {
					copyFile(file, newFile);
				} catch (Exception e) {
					ThemeLog.e(TAG, "copyFileToSystem copyFile Exception: ", e);
				}
			}
			return newFile.getPath();
		}
		return theme;
	}

	protected static void copyFile(File sourceFile, File targetFile) throws IOException {
		BufferedInputStream inBuff = null;
		BufferedOutputStream outBuff = null;
		try {
			inBuff = new BufferedInputStream(new FileInputStream(sourceFile));
			outBuff = new BufferedOutputStream(new FileOutputStream(targetFile));
			byte[] b = new byte[1024 * 5];
			int len;
			while ((len = inBuff.read(b)) != -1) {
				outBuff.write(b, 0, len);
			}
			outBuff.flush();
		} finally {
			if (inBuff != null)
				inBuff.close();
			if (outBuff != null)
				outBuff.close();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.theme_detail, menu);
		if (isThemeCanDelete()) {
			return true;
		}
		return false;
	}
	
	protected boolean isThemeCanDelete() {
		if (Util.checkDownload(mContext, mThemeInfo.getPackageName())) {
			if (mThemeInfo.getThemePath() != null && mThemeInfo.getThemePath().startsWith(Constant.SYSTEM_THEME_PATH)) {
				return false;
			}
			return true;
		}
		return false;
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

		Cursor cursor = downloadManager.query(myDownloadQuery);
		ThemeLog.i(TAG, "Util.getDownloadStatus -> getCount(): " + cursor.getCount());

		int fileNameIdx = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
		int urlIdx = cursor.getColumnIndex(DownloadManager.COLUMN_URI);
		int pathUrlIdx = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
		int statusIdx = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);

		if (cursor != null) {
			ThemeLog.i(TAG, "cursor.getCount(): " + cursor.getCount());

			while (cursor.moveToNext()) {
				String fileName = cursor.getString(fileNameIdx);
				String downloadUrl = cursor.getString(urlIdx);
				String localdUrl = cursor.getString(pathUrlIdx);
				int status = cursor.getInt(statusIdx);

				ThemeLog.i(TAG, "cursor.getPosition(): " + cursor.getPosition());
				ThemeLog.i(TAG, "fileName: " + fileName);
				ThemeLog.w(TAG, "downloadUrl: " + downloadUrl);
				ThemeLog.d(TAG, "localdUrl: " + localdUrl);
				ThemeLog.i(TAG, "status: " + status);

				if (downloadUrl.equals(themeUrl)) {
					result = status;
					break;
				}
			}
			cursor.close();
		}
		return result;
	}

	public int getDownloadingCount() {
		int result = 0;
		DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
		DownloadManager.Query myDownloadQuery = new DownloadManager.Query();
		myDownloadQuery.setFilterByStatus(DownloadManager.STATUS_PAUSED | DownloadManager.STATUS_RUNNING | DownloadManager.STATUS_PENDING);
		Cursor cursor = downloadManager.query(myDownloadQuery);
		ThemeLog.w(TAG, "Util.getDownloadStatus -> getCount(): " + cursor.getCount());
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
		FileTask fileTask = new FileTask();
		fileTask.execute();
		deleteTheme();
		ThemeDataCache.removeTheme(mThemeInfo, mixType);
	}

	public class FileTask extends AsyncTask<Void, Object, Boolean> {
		/**
		 * A callback method to be invoked before the background thread starts
		 * running
		 */
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
			File file = new File(mThemeInfo.getThemePath());
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
			ThemeUtils.deleteTheme(mThemeInfo, mContext);
//			mContext.sendBroadcast(new Intent(Downloads.ACTION_DOWNLOAD_COMPLETED_NOTIFICATION));
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
	
	public abstract void deleteTheme();

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
