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
import android.provider.Settings;
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

public class ThemeDetailNewDynamicWallpaperActivity extends ThemeDetailBaseNewActivity {
	
	private WallpaperInfo mInfo;
	private TextView mApplyButton;
	private static final String TAG = "ThemeDetailNewDynamicWallpaperActivity";
	
	@Override
	public void deleteTheme() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void applyThemeButtonClick(TextView themeapplybutton) {
		// TODO Auto-generated method stub
		mApplyButton = themeapplybutton;
		if (isOnline) {
			if (checkInstall(mThemeInfo.getPackageName())) {
				applyTheme(null, false, false, false , 0);
			} else {
				installApk();
			}
		}
	}
	
	private void installApk() {
		String fileName = mThemeInfo.getDownloadUrl().substring(
				mThemeInfo.getDownloadUrl().lastIndexOf("/") + 1);
		File file = new File(Environment.getExternalStorageDirectory()
				+ FileManager.APP_DIR_NAME + "/" + fileName);
		PackageManager pm = mContext.getPackageManager();
		PackageInfo info = pm.getPackageArchiveInfo(Environment.getExternalStorageDirectory()
						+ FileManager.APP_DIR_NAME + "/" + fileName,PackageManager.GET_ACTIVITIES);
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
			if (pi != null) {
				installFlags |= PackageManager.INSTALL_REPLACE_EXISTING;
			}
		} catch (NameNotFoundException e) {
		}
		PackageInstallObserver observer = new PackageInstallObserver(mContext,Environment.getExternalStorageDirectory()
				+ FileManager.APP_DIR_NAME + "/" + fileName);
		// Intent installingIntent = new Intent(ACTION_APP_INSTALLING);
		//
		// mContext.sendBroadcast(installingIntent);
		ThemeLog.w(TAG, "installApk fileName =" + fileName);
		ThemeLog.w(TAG, "installApk packageName =" + packageName);
		
		if(Settings.Global.getInt(mContext.getContentResolver(),
                Settings.Global.INSTALL_NON_MARKET_APPS, 0) > 0){
			SharedPreferences.Editor editor = mContext.getSharedPreferences("DOWNLOAD_FILE", Context.MODE_PRIVATE).edit();
			editor.putString(packageName, Environment.getExternalStorageDirectory()
					+ FileManager.APP_DIR_NAME + "/" + fileName);
			editor.commit();
			try {
				pm.installPackage(Uri.fromFile(file), observer, installFlags, packageName);
			} catch (Exception e) {
				ThemeLog.w(TAG, "sss installPackage  Exception="+e.getMessage());
			}
		}else{
			Intent install = new Intent(Intent.ACTION_VIEW);
			install.setDataAndType(Uri.fromFile(file), Constants.MIMETYPE_APK);
			install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(install);
		}
		StatService.onEvent(mContext, "InstallDynamicWallpaper",
				mThemeInfo.getTitle(), 1);
	}
	
	@Override
	protected void onApkInstallComplete() {
		// TODO Auto-generated method stub
		super.onApkInstallComplete();
	}
	
	@Override
	protected boolean isThemeCanDelete() {
		// TODO Auto-generated method stub
		boolean b = Util.checkDownload(mThemeInfo.getDownloadUrl()) && Util.checkApkDownloadSratus(mThemeInfo.getDownloadUrl(), mThemeInfo);
		return b;
	}
	
	private boolean checkApkFileExist() {
		String fileName = mThemeInfo.getDownloadUrl().substring(mThemeInfo.getDownloadUrl().lastIndexOf("/") + 1);
		ThemeLog.w(TAG, "fileName :" + fileName);
		File file = new File(Environment.getExternalStorageDirectory()
				+ FileManager.APP_DIR_NAME + "/" + fileName);
		if (file.exists()) {
			return true;
		}
		return false;
	}
	
	@Override
	protected void showDeleteImageView(boolean show) {
		// TODO Auto-generated method stub
		super.showDeleteImageView(show);
	}

	@Override
	public void applyThemeFailed() {
		// TODO Auto-generated method stub
		SimpleDialogs.displayOkDialog(R.string.dlg_theme_failed_title, R.string.dlg_theme_failed_body, 
				ThemeDetailNewDynamicWallpaperActivity.this);
	}
	
	@Override
	protected void startDownload() {
		String downloadUrl = mThemeInfo.getDownloadUrl();
		String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1);

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
		DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
		long id = downloadManager.enqueue(request);
		Toast.makeText(mContext, getResources().getString(R.string.start_download), Toast.LENGTH_SHORT).show();

		ReportProvider.postUserTheme(mThemeInfo.getPackageName(), ThemeData.THEME_REPORT_SORT_DOWNLOAD_S);
		StatService.onEvent(this, "Download", mThemeInfo.getTitle(), 1);

		SharedPreferences.Editor editor = mContext.getSharedPreferences("DOWNLOAD_PATH", MODE_PRIVATE).edit();
		editor.putString(id + "", Environment.getExternalStorageDirectory() + FileManager.APP_DIR_NAME + "/" + fileName);
		editor.commit();

		editor = mContext.getSharedPreferences("DOWNLOAD_FILE", MODE_PRIVATE).edit();
		editor.putString(id + "", mThemeInfo.getPackageName());
		editor.commit();
		StatService.onEvent(mContext, "DownloadDynamicWallpaper", mThemeInfo.getTitle(),1);
	}

	@Override
	public void applyThemeSucess(int type,String uri) {
		// TODO Auto-generated method stub
		ThemeUtils.resetUsingFlagByType(ThemeDetailNewDynamicWallpaperActivity.this, mixType);
		ThemeUtils.setUsingFlagByType(ThemeDetailNewDynamicWallpaperActivity.this, mThemeInfo, mixType);
		ThemeUtils.resetUsingFlagByType(ThemeDetailNewDynamicWallpaperActivity.this, ThemeData.THEME_ELEMENT_TYPE_WALLPAPER);
		Toast.makeText(ThemeDetailNewDynamicWallpaperActivity.this, getText(R.string.apply_success_tip), Toast.LENGTH_SHORT).show();
		this.finish();
	}
	
	@Override
	protected void onDownLoadComplete(String pn) {
		// TODO Auto-generated method stub
		super.onDownLoadComplete(pn);
		if (mApplyButton != null) {
			mApplyButton.setText(R.string.installing);
		}
	}

	@Override
	protected void applyTheme(String theme, boolean applyFont,
			boolean scaleBoot, boolean removeExistingTheme,int wallpaperType) {
		// TODO Auto-generated method stub
		
		ReportProvider.postUserTheme(mThemeInfo.getPackageName(), ThemeData.THEME_REPORT_SORT_USE);
		StatService.onEvent(mContext, "ApplyDynamicWallpaper", mThemeInfo.getTitle(), 1);
		try {
			ThemeUtils.resetUsingFlagByType(ThemeDetailNewDynamicWallpaperActivity.this, ThemeData.THEME_ELEMENT_TYPE_WALLPAPER);
			WallpaperManager mWallpaperManager = WallpaperManager.getInstance(mContext);
			mWallpaperManager.getIWallpaperManager().setWallpaperComponent(mInfo.getComponent());
			mWallpaperManager.setWallpaperOffsetSteps(0.5f, 0.0f);
			mWallpaperManager.setWallpaperOffsets(mApplyButton.getRootView().getWindowToken(), 0.5f, 0.0f);
			setResult(RESULT_OK);
			Toast.makeText(ThemeDetailNewDynamicWallpaperActivity.this, 
					getText(R.string.live_apply_success_tip), Toast.LENGTH_SHORT).show();
			this.finish();
		} catch (Exception e) {
			Toast.makeText(this, getText(R.string.live_apply_fail_tip), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}
	
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
					SharedPreferences.Editor editor = mContext.getSharedPreferences("DOWNLOAD_FILE", Context.MODE_PRIVATE).edit();
					editor.remove(packageName);
					editor.commit();
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

}
