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
import com.fineos.theme.ThemeDataCache;
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

public class ThemeDetailNewWallpaperActivity extends ThemeDetailBaseNewActivity {

	private static final String TAG = "ThemeDetailNewWallpaperActivity";
	private static final String LOCK_WALLPAPER_PATH = "wallpaper/default_lock_wallpaper.png";
	private static final String WALLPAPERPATH = "wallpaper/default_wallpaper.png";
	private String[] choices;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		choices = new String[] { getResources().getString(R.string.wallpaper_apply_lockscreen_wallpaper), getResources().getString(R.string.wallpaper_apply_desktop_wallpaper),
				getResources().getString(R.string.wallpaper_apply_all_wallpaper) };
		super.onCreate(savedInstanceState);
	}
	
	private void showWallpaperDialog() {
		AlertDialog dialog = new AlertDialog.Builder(ThemeDetailNewWallpaperActivity.this).setTitle(R.string.btn_use).setItems(choices, onselect).create();
		dialog.show();
	}
	
	@Override
	public void deleteTheme() {
		// TODO Auto-generated method stub
		if (mPagerAdapter != null) {
			mPagerAdapter.notifyDataSetChanged();
		}
	}

	private String getWallpaperDetailPath() {
		// TODO Auto-generated method stub
		String path = null;
		if (mixType == ThemeData.THEME_ELEMENT_TYPE_LOCK_WALLPAPER) {
			path = LOCK_WALLPAPER_PATH;			
		} else {
			path = WALLPAPERPATH;
		}
		return path;
	}

	@Override
	public void applyThemeFailed() {
		// TODO Auto-generated method stub
		SimpleDialogs.displayOkDialog(R.string.dlg_theme_failed_title, R.string.dlg_theme_failed_body, ThemeDetailNewWallpaperActivity.this);
	}

	@Override
	public void applyThemeSucess(int type,String uri) {
		// TODO Auto-generated method stub
		ThemeData themeinfo = mThemeInfo;
		if (isOnline) {
			themeinfo = ThemeUtils.getThemeByFildId(mContext, mThemeInfo.getPackageName());
		}
		ThemeLog.i(TAG,"ThemeDetailWallpaperActivity applyTheme themeinfo: " + themeinfo);
		if(type == ThemeData.DESKTOP_WALLPAPER_APPLY){
			if(uri==null || themeinfo == null || themeinfo.getThemePath() == null || !getThemeName(uri).equals(getThemeName(themeinfo.getThemePath()))){
				return;
			}
			ThemeUtils.resetUsingFlagByType(ThemeDetailNewWallpaperActivity.this, ThemeData.THEME_ELEMENT_TYPE_WALLPAPER);
			ThemeUtils.setUsingFlagByType(ThemeDetailNewWallpaperActivity.this, themeinfo, ThemeData.THEME_ELEMENT_TYPE_WALLPAPER);
			if (!isOnline) {
				ArrayList<ThemeData> mThemeDataList = ThemeDataCache.getThemeDatas(mixType);
				for (ThemeData themeData : mThemeDataList) {
					if (!themeData.equals(mThemeInfo)) {
						themeData.setIsUsing_wallpaper(false);
					} else {
						themeData.setIsUsing_wallpaper(true);
					}
				}
			}
		}
		if(type == ThemeData.LOCKSCREEN_WALLPAPER_APPLY){
			if(uri==null||!getThemeName(uri).equals(getThemeName(themeinfo.getThemePath()))){
				return;
			}
			ThemeUtils.resetUsingFlagByType(ThemeDetailNewWallpaperActivity.this, ThemeData.THEME_ELEMENT_TYPE_LOCK_WALLPAPER);
			ThemeUtils.setUsingFlagByType(ThemeDetailNewWallpaperActivity.this, themeinfo, ThemeData.THEME_ELEMENT_TYPE_LOCK_WALLPAPER);
		}
		if(type == ThemeData.WALLPAPER_APPLY){
			if(uri==null||!getThemeName(uri).equals(getThemeName(themeinfo.getThemePath()))){
				return;
			}
			ThemeUtils.resetUsingFlagByType(ThemeDetailNewWallpaperActivity.this, ThemeData.THEME_ELEMENT_TYPE_LOCK_WALLPAPER);
			ThemeUtils.setUsingFlagByType(ThemeDetailNewWallpaperActivity.this, themeinfo, ThemeData.THEME_ELEMENT_TYPE_LOCK_WALLPAPER);
			ThemeUtils.resetUsingFlagByType(ThemeDetailNewWallpaperActivity.this, ThemeData.THEME_ELEMENT_TYPE_WALLPAPER);
			ThemeUtils.setUsingFlagByType(ThemeDetailNewWallpaperActivity.this, themeinfo, ThemeData.THEME_ELEMENT_TYPE_WALLPAPER);
			if (!isOnline) {
				ArrayList<ThemeData> mThemeDataList = ThemeDataCache.getThemeDatas(mixType);
				
				for (ThemeData themeData : mThemeDataList) {
					if (!themeData.equals(mThemeInfo)) {
						themeData.setIsUsing_wallpaper(false);
						themeData.setIsUsing_lockscreen(false);
					} else {
						themeData.setIsUsing_wallpaper(true);
						themeData.setIsUsing_lockscreen(true);
					}
				}
			}
		}
		
		Toast.makeText(ThemeDetailNewWallpaperActivity.this, getText(R.string.wallpaper_apply_success_tip), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void applyThemeButtonClick(TextView themeapplybutton) {
		// TODO Auto-generated method stub
		if (isOnline) {
			if (isThemeCanDelete()) {
				showWallpaperDialog();
			}
		} else {
			showWallpaperDialog();
		}		
	}
	@Override
	protected void onResume() {
		super.onResume();
		StatService.onResume(mContext);

	}

	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPause(mContext);
	}
	@SuppressWarnings("deprecation")
	protected void applyTheme(String theme, boolean applyFont, boolean scaleBoot, boolean removeExistingTheme, int wallpaperType) {
		ThemeLog.i(TAG,"ThemeDetailWallpaperActivity applyTheme theme1: " + theme);
		theme = ThemeUtils.copyFileToSystem(theme, getApplicationContext());
		ThemeLog.i(TAG,"ThemeDetailWallpaperActivity applyTheme theme2: " + theme);
		
		IThemeManagerService ts = IThemeManagerService.Stub.asInterface(ServiceManager.getService("ThemeService"));
		try {
			ThemeLog.i(TAG, "applyTheme stripPath =" + ThemeContentProvider.CONTENT + FileUtils.stripPath(theme));
			ThemeLog.i(TAG, "applyTheme stripPath =" + wallpaperType);
			ThemeLog.i(TAG, "applyTheme mixType =" + mixType);
			if (mixType == ThemeData.THEME_ELEMENT_TYPE_LOCK_WALLPAPER) {

				if (wallpaperType == 1) {
					StatService.onEvent(mContext, "ApplyLockscreenWallpaper", mThemeInfo.getTitle(), 1);
					ts.applyThemeLockscreenWallpaperByName(ThemeContentProvider.CONTENT + FileUtils.stripPath(theme), getWallpaperDetailPath());
				} else if (wallpaperType == 2) {
					StatService.onEvent(mContext, "ApplyDesktopWallpaper", mThemeInfo.getTitle(), 1);
					ts.applyThemeDesktopWallpaperByName(ThemeContentProvider.CONTENT + FileUtils.stripPath(theme), getWallpaperDetailPath());
				} else if (wallpaperType == 3) {
					StatService.onEvent(mContext, "ApplyBothWallpaper", mThemeInfo.getTitle(), 1);
					ts.applyThemeWallpaperByName(ThemeContentProvider.CONTENT + FileUtils.stripPath(theme), getWallpaperDetailPath());
				}
			}
			if (mixType == ThemeData.THEME_ELEMENT_TYPE_WALLPAPER) {

				if (wallpaperType == 1) {
					StatService.onEvent(mContext, "ApplyLockscreenWallpaper", mThemeInfo.getTitle(), 1);
					ts.applyThemeLockscreenWallpaperByName(ThemeContentProvider.CONTENT + FileUtils.stripPath(theme), getWallpaperDetailPath());
				} else if (wallpaperType == 2) {
					StatService.onEvent(mContext, "ApplyDesktopWallpaper", mThemeInfo.getTitle(), 1);
					ts.applyThemeDesktopWallpaperByName(ThemeContentProvider.CONTENT + FileUtils.stripPath(theme), getWallpaperDetailPath());
				} else if (wallpaperType == 3) {
					StatService.onEvent(mContext, "ApplyBothWallpaper", mThemeInfo.getTitle(), 1);
					ts.applyThemeWallpaperByName(ThemeContentProvider.CONTENT + FileUtils.stripPath(theme), getWallpaperDetailPath());
				}
			}
			ThemeLog.i(TAG, "applyTheme showDialog");
			mProgressDialog.setMessage(getResources().getText(R.string.applying_wallpaper));
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mProgressDialog.setCancelable(false);
			mProgressDialog.setProgress(0);
			mProgressDialog.show();
		} catch (Exception e) {
			ThemeLog.e(TAG, "exception happened !");
			e.printStackTrace();
			applyThemeFailed();
		}
	}

}
