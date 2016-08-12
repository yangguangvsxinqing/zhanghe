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

public class ThemeDetailNewLockscreenActivity extends ThemeDetailBaseNewActivity {
	private static final int ACTION_THEME_PREVIEW = 1;
	private static final int ACTION_NETWORK_ERROR = 0;

	private static final String TAG = "ThemeDetailNewLockscreenActivity";
	private static final Boolean DEBUG = Boolean.TRUE;
	@Override
	public void deleteTheme() {
		// TODO Auto-generated method stub
		invalidateOptionsMenu();
	}
	
	@Override
	public void applyThemeFailed() {
		// TODO Auto-generated method stub
		SimpleDialogs.displayOkDialog(R.string.dlg_theme_failed_title, R.string.dlg_theme_failed_body, ThemeDetailNewLockscreenActivity.this);
	}
	@Override
	public void applyThemeSucess(int type,String uri) {
		// TODO Auto-generated method stub
		if(type == mThemeInfo.LOCKSCREEN_APPLY){
			if(uri==null||!getThemeName(uri).equals(getThemeName(mThemeInfo.getThemePath()))){
				return;
			}
			ThemeUtils.resetUsingFlagByType(ThemeDetailNewLockscreenActivity.this, ThemeData.THEME_ELEMENT_TYPE_LOCK_WALLPAPER);
			ThemeUtils.setUsingFlagByType(ThemeDetailNewLockscreenActivity.this, mThemeInfo, ThemeData.THEME_ELEMENT_TYPE_LOCK_WALLPAPER);
			ThemeUtils.resetUsingFlagByType(ThemeDetailNewLockscreenActivity.this, mixType);
			ThemeUtils.setUsingFlagByType(ThemeDetailNewLockscreenActivity.this, mThemeInfo, mixType);
			Toast.makeText(ThemeDetailNewLockscreenActivity.this, getText(R.string.lockwallpaper_apply_success_tip), Toast.LENGTH_SHORT).show();
		
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
	@Override
	public void applyThemeButtonClick(TextView themeapplybutton) {
		// TODO Auto-generated method stub
		if (isOnline) {
			if (isThemeCanDelete()) {
				mThemeInfo = ThemeUtils.getThemeByFildId(mContext, mThemeInfo.getPackageName());
				ReportProvider.postUserTheme(ThemeUtils.getpackageName(mThemeInfo, false), ThemeData.THEME_REPORT_SORT_USE);				
				StatService.onEvent(mContext, "ApplyLockscreen", mThemeInfo.getTitle(), 1);
				applyTheme(mThemeInfo.getThemePath(), false, false, true , 0);
				if (themeapplybutton != null) {
					themeapplybutton.setEnabled(false);
				}
			}
		} else {
			ReportProvider.postUserTheme(ThemeUtils.getpackageName(mThemeInfo, false), ThemeData.THEME_REPORT_SORT_USE);

			applyTheme(mThemeInfo.getThemePath(), false, false, true, 0);
			StatService.onEvent(mContext, "ApplyLockscreen", mThemeInfo.getTitle(), 1);
			if (themeapplybutton != null) {
				themeapplybutton.setEnabled(false);
			}
		}
	}
	
	protected void applyTheme(String theme, boolean applyFont, boolean scaleBoot, boolean removeExistingTheme ,int wallpaperType) {
		theme = ThemeUtils.copyFileToSystem(theme, getApplicationContext());
		
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
			mPagerAdapter.notifyDataSetChanged();
			mProgressDialog.setMessage(getResources().getText(R.string.applying_lockscreen));
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
