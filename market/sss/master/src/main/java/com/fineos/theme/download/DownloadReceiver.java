package com.fineos.theme.download;

import java.io.File;
import java.io.FilenameFilter;
import java.io.UnsupportedEncodingException;
import java.util.Timer;
import java.util.TimerTask;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.baidu.mobstat.StatService;
import com.fineos.theme.R;
import com.fineos.theme.ThemeBackgroundService;
import com.fineos.theme.activity.ThemeLocalHomeActivity;
import android.content.pm.PackageInstallObserver;
import com.fineos.theme.model.ThemeData;
import com.fineos.theme.provider.ThemeSQLiteHelper;
import com.fineos.theme.utils.Constant;
import com.fineos.theme.utils.FileManager;
import com.fineos.theme.utils.FileUtils;
import com.fineos.theme.utils.ThemeLog;
import com.fineos.theme.utils.ThemeUtils;
import com.fineos.theme.utils.Util;

/**
 * Receives system broadcasts (boot, network connectivity)
 */
public class DownloadReceiver extends BroadcastReceiver {
	private static final String TAG = "DownloadReceiver";
	public static final String ACTION_INSTALL_FAIL = "action_install_fail";
	Timer initTimer = null;
	Timer nextTimer = null;
	Timer timechangeTimer = null;
	boolean initTimerIsRunning = false;
	boolean nextTimerIsRunning = false;
	boolean timechangeTimerIsRunning = false;

	private String[] getAvailableThemes(String path) {
		ThemeLog.d(TAG, "Returning theme list for " + path);
		FilenameFilter themeFilter = new FilenameFilter() {
			@Override
			public boolean accept(File file, String s) {
				if (s.toLowerCase().endsWith(".ctz") || s.toLowerCase().endsWith(".mtz") || s.toLowerCase().endsWith(".ftz"))
					return true;
				else
					return false;
			}
		};

		File dir = new File(path);
		String[] dirList = null;
		if (dir.exists() && dir.isDirectory()) {
			dirList = dir.list(themeFilter); // 滤出所有后缀为 .ctz .mtz .ftz 的压缩包的路径
			ThemeLog.d(TAG, "dir List（end with .ctz,.mtz,.ftz）" + dirList);
		} else {
			ThemeLog.i(TAG, path + " does not exist or is not a directory!");
		}
		ThemeLog.i(TAG, "dirList=" + dirList);
		return dirList;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		ThemeLog.i(TAG, "DownloadReceiver onReceive");
		final String action = intent.getAction();
		ThemeLog.i(TAG, "action =" + action);
		if (action.equals(Downloads.ACTION_DOWNLOAD_COMPLETED)) {
			final long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
			String fileName = context.getSharedPreferences("DOWNLOAD_FILE", Context.MODE_PRIVATE).getString(id + "", "");
			if(fileName!=null&&fileName.length()>0){
				ReportProvider.postUserTheme(fileName.substring(0, fileName.indexOf(".")), ThemeData.THEME_REPORT_SORT_DOWNLOAD_E);
				Intent intent1 = new Intent(Downloads.ACTION_DOWNLOAD_COMPLETED_NOTIFICATION);
				intent1.putExtra("downloadfilename", fileName);
				context.sendBroadcast(intent1);
			}
			
			SharedPreferences sharedPreferences = context.getSharedPreferences("DOWNLOAD_PATH", 0);
			String themePath = sharedPreferences.getString(id + "", null);
			if(Util.checkAppFileSratus(themePath)){
				
			if (themePath != null) {
				ThemeLog.i(TAG, "themePath =" + themePath);
				installAPK(context, themePath);
			} 
			}else {
//				Cursor c = context.getContentResolver().query(ThemeSQLiteHelper.CONTENT_URI, null, ThemeSQLiteHelper.COLUMN_ID + " = " + themeId, null, null);
//				ThemeLog.e(TAG, "Cursor =" + c);
//				ThemeLog.e(TAG, "moveToFirst =" + c.moveToFirst());
//				if (c != null) {
//					ContentValues values = new ContentValues();
//					values.put(ThemeSQLiteHelper.COLUMN_IS_VERIFY, 1);
//					Uri updateIdUri = ContentUris.withAppendedId(ThemeSQLiteHelper.CONTENT_URI, themeId);
//
//					int SQLid = context.getContentResolver().update(updateIdUri, values, null, null);
//					// int SQLid =
//					// context.getContentResolver().update(ThemeSQLiteHelper.CONTENT_URI,
//					// values,
//					// ThemeSQLiteHelper.COLUMN_ID+ " = "+themeId, null);
//					ThemeLog.e(TAG, "SQLid =" + SQLid);
//				}
//				c.close();
				try {
					ThemeUtils.addThemesToDb(context, false);
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
//				ThemeUtils.addThemeEntryToDb("default", Constant.DEFAULT_SYSTEM_THEME, // 创建默认主题数据库
//						context.getApplicationContext(), true, false);
//				String[] availableThemes = getAvailableThemes(Constant.DEFAULT_THEME_PATH); // 从默认路径过滤主题（/FineOS/theme）
//				ThemeLog.e(TAG, "availableThemes=" + availableThemes);
//				for (String themeIndex : availableThemes) {
//					ThemeLog.e(TAG, "themeId =" + themeId);
//					try {                                  
//						ThemeUtils.addThemeEntryToDb(FileUtils.stripExtension(themeIndex), // 为所有主题创建数据库
//								Constant.DEFAULT_THEME_PATH + "/" + themeId, context, false, false);
//					} catch (UnsupportedEncodingException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
			}
			
			try {
				
				Boolean downed = Util.checkDownload(context, FileUtils.stripExtension(fileName));
				ThemeLog.i(TAG, "fileName =" + fileName+",downed :"+downed);
				if(downed ){   // delete theme packages while downloading,do not show toast 
					
					Toast.makeText(context, context.getResources().getString(R.string.file_download_complete), Toast.LENGTH_SHORT).show();
				    
				}
				
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			
		} else if (action.equals("android.intent.action.TIME_SET") || action.equals(Intent.ACTION_USER_PRESENT)) {
			MyRemindTask myRemindTask = new MyRemindTask(context);
			myRemindTask.run();
		}
	}

	private void installAPK(Context context, String filepath) {
		
		File file = new File(filepath);
		PackageManager pm = context.getPackageManager();
		PackageInfo info = pm.getPackageArchiveInfo(filepath, PackageManager.GET_ACTIVITIES);
		ApplicationInfo appInfo = null;
		String packageName = null;
		if (info != null) {
			appInfo = info.applicationInfo;
			packageName = appInfo.packageName;
		}
		int installFlags = 0;
		try {

			PackageInfo pi = pm.getPackageInfo(packageName,	PackageManager.GET_UNINSTALLED_PACKAGES);
			if (pi != null) {
				installFlags |= PackageManager.INSTALL_REPLACE_EXISTING;
			}
		} catch (NameNotFoundException e) {
		}
		PackageInstallObserver observer = new PackageInstallObserver(context , filepath);
		// Intent installingIntent = new Intent(ACTION_APP_INSTALLING);
		//
		// mContext.sendBroadcast(installingIntent);
		
		if(Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.INSTALL_NON_MARKET_APPS, 0) > 0&&isSystemApp(context)){
			SharedPreferences.Editor editor = context.getSharedPreferences("DOWNLOAD_FILE", Context.MODE_PRIVATE).edit();
			editor.putString(packageName, filepath);
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
			context.startActivity(install);
		}
		if (appInfo != null) {
			StatService.onEvent(context, "InstallDynamicWallpaper", appInfo.name);
		} else {
			ThemeLog.w(TAG, "appInfo is null , The Apk may be deleted by User");
		}
	}

	public boolean isSystemApp(Context context) {
		PackageManager pm = context.getPackageManager();
		try {
			PackageInfo info = pm.getPackageInfo(context.getPackageName(), 0);
			return (info != null) && (info.applicationInfo != null) &&
					((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
		} catch (NameNotFoundException e) {
			return false;
		}
	}

	private class MyRemindTask extends TimerTask {
		private Context MyRemindTaskContext;

		MyRemindTask(Context context) {
			MyRemindTaskContext = context;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			ThemeLog.i(TAG, "MyRemindTask run");
			MyRemindTaskContext.startService(new Intent(MyRemindTaskContext, ThemeBackgroundService.class));
		}

	}
}