/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fineos.theme.activity;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import com.android.dex.ClassData.Method;
import com.baidu.mobstat.StatService;
import com.fineos.theme.R;
import com.fineos.theme.fragment.DeleteDialogFragment;
import com.fineos.theme.model.ThemeData;
import com.fineos.theme.utils.Constant;
import com.fineos.theme.utils.ThemeLog;
import com.fineos.theme.utils.ThemeUtils;
import com.fineos.theme.utils.Util;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.WallpaperManager;
import android.app.WallpaperInfo;
import android.app.Dialog;
import android.service.wallpaper.IWallpaperConnection;
import android.service.wallpaper.IWallpaperService;
import android.service.wallpaper.IWallpaperEngine;
import android.service.wallpaper.WallpaperSettingsActivity;
import android.content.DialogInterface;
import android.content.ServiceConnection;
import android.content.Intent;
import android.content.Context;
import android.content.ComponentName;
import android.graphics.Rect;
import android.os.Build;
import android.os.RemoteException;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.ViewGroup;
import android.view.Window;
import android.view.LayoutInflater;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class LiveWallpaperPreview extends Activity {
	static final String EXTRA_LIVE_WALLPAPER_INTENT = "android.live_wallpaper.intent";
	static final String EXTRA_LIVE_WALLPAPER_SETTINGS = "android.live_wallpaper.settings";
	static final String EXTRA_LIVE_WALLPAPER_PACKAGE = "android.live_wallpaper.package";

	private static final String LOG_TAG = "LiveWallpaperPreview";

	private WallpaperManager mWallpaperManager;
	private WallpaperConnection mWallpaperConnection;

	private String mSettings;
	private String mPackageName;
	private String mPath;
	private Intent mWallpaperIntent;
	private View mView;
	private ImageView mDeleteButton;
	private Dialog mDialog;
	private static WallpaperInfo mWallpaperInfo = null;
	private static final String TAG = "LiveWallpaperPreview";
	private HashMap<String, String> mFineOSLiveWallper = new HashMap<String, String>();

	public static void showPreview(Context context, int code, Intent intent, WallpaperInfo info) {
		if (info == null) {
			ThemeLog.w(LOG_TAG, "Failure showing preview", new Throwable());
			return;
		}
		mWallpaperInfo = info;
		Intent preview = new Intent(context, LiveWallpaperPreview.class);
		preview.putExtra(EXTRA_LIVE_WALLPAPER_INTENT, intent);
		preview.putExtra(EXTRA_LIVE_WALLPAPER_SETTINGS, info.getSettingsActivity());
		preview.putExtra(EXTRA_LIVE_WALLPAPER_PACKAGE, info.getPackageName());
		((Activity) context).startActivityForResult(preview, code);
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(com.fineos.R.anim.slide_in_left, com.fineos.R.anim.slide_out_right);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
/*		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		ThemeLog.e("", "currentapiVersion: " + currentapiVersion);
		if (currentapiVersion <= Build.VERSION_CODES.KITKAT) {
			setTheme(com.fineos.internal.R.style.Theme_Holo_Light_FineOS);
		};*/

		Bundle extras = getIntent().getExtras();
		mWallpaperIntent = (Intent) extras.get(EXTRA_LIVE_WALLPAPER_INTENT);
		if (mWallpaperIntent == null) {
			setResult(RESULT_CANCELED);
			finish();
		}
		
		setContentView(R.layout.live_wallpaper_preview);
		mView = findViewById(R.id.btn_theme_apply);
		mDeleteButton = (ImageView) findViewById(R.id.delete_button);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		/*
		getActionBar().setDisplayUseLogoEnabled(false);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setDisplayShowHomeEnabled(false);
		getActionBar().setTitle(mWallpaperInfo.loadLabel(getPackageManager()));
*/
		mSettings = extras.getString(EXTRA_LIVE_WALLPAPER_SETTINGS);
		ThemeLog.v(TAG, "mSettings=" + mSettings + ",mPackageName=" + mPackageName);
//		if (mSettings == null) {
//			mView.setVisibility(View.GONE);
//		}
		if(mWallpaperInfo!=null){
			mPackageName = mWallpaperInfo.getPackageName();
		}

		mFineOSLiveWallper = ThemeUtils.getAvailableApkPackageNames(Constant.DEFAULT_LIVEWALLPAPER_THEME_PATH, getApplicationContext());
		mDeleteButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				showDeleteDialog();
			}
		});
		mDeleteButton.setImageResource(com.fineos.R.drawable.ic_menu_delete_holo_dark);
		if (isApkFileExist()) {
			mDeleteButton.setVisibility(View.VISIBLE);
		} else {
			mDeleteButton.setVisibility(View.INVISIBLE);
		}
		TextView backView = (TextView)findViewById(R.id.home_back);
		backView.setCompoundDrawablesWithIntrinsicBounds(getApplicationContext().getResources().getDrawable(com.fineos.R.drawable.ic_ab_back_holo_dark),
				null, null, null);
		backView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
				
			}
		});
		mWallpaperManager = WallpaperManager.getInstance(this);
		mWallpaperConnection = new WallpaperConnection(mWallpaperIntent);
	}
	
	private boolean isApkFileExist() {
		
		if (mFineOSLiveWallper.containsKey(mPackageName)) {			
			mPath = mFineOSLiveWallper.get(mPackageName);
			return true;
		}
		return false;
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
			deleteApk();
			dialog.dismiss();
		}
	};
	
	private void deleteApk() {
		if (mPath != null) {
			File f = new File(mPath);
			if (f == null || !f.exists()) {
				return;
			}
			f.delete();
			mDeleteButton.setVisibility(View.INVISIBLE);
			Toast.makeText(getApplicationContext(), R.string.apk_deleted, 1000).show();
		}
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	public void setLiveWallpaper(View v) {
		try {
			mWallpaperManager.getIWallpaperManager().setWallpaperComponent(mWallpaperIntent.getComponent());
			mWallpaperManager.setWallpaperOffsetSteps(0.5f, 0.0f);
			mWallpaperManager.setWallpaperOffsets(v.getRootView().getWindowToken(), 0.5f, 0.0f);
			ThemeUtils.resetUsingFlagByType(LiveWallpaperPreview.this, ThemeData.THEME_ELEMENT_TYPE_WALLPAPER);
			setResult(RESULT_OK);
			
			Toast.makeText(this, getText(R.string.live_apply_success_tip), Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			ThemeLog.w(LOG_TAG, "Failure setting wallpaper", e);
			
			Toast.makeText(this, getText(R.string.live_apply_fail_tip), Toast.LENGTH_SHORT).show();
		}

		finish();
	}

	@SuppressWarnings({ "UnusedDeclaration" })
	public void configureLiveWallpaper(View v) {
		Intent intent = new Intent();
		ThemeLog.v(TAG, "configureLiveWallpaper,mPackageName=" + mPackageName + ",mSettings=" + mSettings);
		intent.setComponent(new ComponentName(mPackageName, mSettings));
		intent.putExtra(WallpaperSettingsActivity.EXTRA_PREVIEW_MODE, true);
		startActivity(intent);
	}

	@Override
	public void onResume() {
		super.onResume();
		StatService.onResume(this);
		if (mWallpaperConnection != null && mWallpaperConnection.mEngine != null) {
			try {
				mWallpaperConnection.mEngine.setVisibility(true);
			} catch (RemoteException e) {
				// Ignore
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		StatService.onPause(this);
		if (mWallpaperConnection != null && mWallpaperConnection.mEngine != null) {
			try {
				mWallpaperConnection.mEngine.setVisibility(false);
			} catch (RemoteException e) {
				// Ignore
			}
		}
	}

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();

		showLoading();

		mView.post(new Runnable() {
			public void run() {
				/// M: fix a nullpoint exception.
				if (mWallpaperConnection != null && !mWallpaperConnection.connect()) {
					mWallpaperConnection = null;
				}
			}
		});
	}

	private void showLoading() {
		LayoutInflater inflater = LayoutInflater.from(this);
		TextView content = (TextView) inflater.inflate(R.layout.live_wallpaper_loading, null);

		mDialog = new Dialog(this, android.R.style.Theme_Black);

		Window window = mDialog.getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();

		lp.width = WindowManager.LayoutParams.MATCH_PARENT;
		lp.height = WindowManager.LayoutParams.MATCH_PARENT;
		window.setType(WindowManager.LayoutParams.TYPE_APPLICATION_MEDIA);

		mDialog.setContentView(content, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		mDialog.show();
	}

	@Override
	public void onDetachedFromWindow() {
		super.onDetachedFromWindow();

		if (mDialog != null)
			mDialog.dismiss();

		/**
		 * M: fix a seldom JE by making sure
		 * disconnect the WallpaperConnection that has been connected.
		 */
		if (mWallpaperConnection != null && mWallpaperConnection.mConnected) {
			mWallpaperConnection.disconnect();
		}
		mWallpaperConnection = null;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (mWallpaperConnection != null && mWallpaperConnection.mEngine != null) {
			MotionEvent dup = MotionEvent.obtainNoHistory(ev);
			try {
				mWallpaperConnection.mEngine.dispatchPointer(dup);
			} catch (RemoteException e) {
			}
		}

		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			onUserInteraction();
		}
		boolean handled = getWindow().superDispatchTouchEvent(ev);
		if (!handled) {
			handled = onTouchEvent(ev);
		}

		if (!handled && mWallpaperConnection != null && mWallpaperConnection.mEngine != null) {
			int action = ev.getActionMasked();
			try {
				if (action == MotionEvent.ACTION_UP) {
					mWallpaperConnection.mEngine.dispatchWallpaperCommand(WallpaperManager.COMMAND_TAP, (int) ev.getX(), (int) ev.getY(), 0, null);
				} else if (action == MotionEvent.ACTION_POINTER_UP) {
					int pointerIndex = ev.getActionIndex();
					mWallpaperConnection.mEngine.dispatchWallpaperCommand(WallpaperManager.COMMAND_SECONDARY_TAP, (int) ev.getX(pointerIndex), (int) ev.getY(pointerIndex), 0, null);
				}
			} catch (RemoteException e) {
			}
		}
		return handled;
	}

	class WallpaperConnection extends IWallpaperConnection.Stub implements ServiceConnection {
		final Intent mIntent;
		IWallpaperService mService;
		IWallpaperEngine mEngine;
		boolean mConnected;

		WallpaperConnection(Intent intent) {
			mIntent = intent;
		}

		public boolean connect() {
			synchronized (this) {
				try {
					if (!bindService(mIntent, this, Context.BIND_AUTO_CREATE)) { 
						return false;
					}
				} catch (Exception e) {
					ThemeLog.v(TAG, "connect(),exception happened !");
					e.printStackTrace();
				}

				mConnected = true;
				return true;
			}
		}

		public void disconnect() {
			synchronized (this) {
				mConnected = false;
				if (mEngine != null) {
					try {
						mEngine.destroy();
					} catch (RemoteException e) {
						// Ignore
					}
					mEngine = null;
				}
				unbindService(this);
				mService = null;
			}
		}

		public void onServiceConnected(ComponentName name, IBinder service) {
			if (mWallpaperConnection == this) {
				mService = IWallpaperService.Stub.asInterface(service);
				try {
					final View view = mView;
					final View root = view.getRootView();
//					mService.attach(this, view.getWindowToken(), WindowManager.LayoutParams.TYPE_APPLICATION_MEDIA_OVERLAY, true, root.getWidth(), root.getHeight());

//                    Android5.0
					ThemeUtils.resetUsingFlagByType(LiveWallpaperPreview.this, ThemeData.THEME_ELEMENT_TYPE_WALLPAPER);
					if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.L) {
						mService.attach(this, view.getWindowToken(),
	                            WindowManager.LayoutParams.TYPE_APPLICATION_MEDIA_OVERLAY,
	                            true, root.getWidth(), root.getHeight(), new Rect());
					} else {
						try {
							java.lang.reflect.Method md = mService.getClass().getMethod("attach", new Class[]{IWallpaperConnection.class, 
									IBinder.class, int.class, boolean.class, int.class, int.class});
							md.setAccessible(true);
							md.invoke(mService, this, view.getWindowToken(),
		                            WindowManager.LayoutParams.TYPE_APPLICATION_MEDIA_OVERLAY,
		                            true, root.getWidth(), root.getHeight());
						} catch (NoSuchMethodException e) {
							e.printStackTrace();
							ThemeLog.w(TAG, "IWallpaperService NoSuchMethodException");
						}catch (Exception e) {
							// TODO: handle exception
							e.printStackTrace();
							ThemeLog.w(TAG, "IWallpaperService e" + e);
						}
					}
                    StatService.onEvent(LiveWallpaperPreview.this, "ApplyDynamicWallpaper", "apply",1);
				} catch (RemoteException e) {
					ThemeLog.w(LOG_TAG, "Failed attaching wallpaper; clearing", e);
					ThemeLog.v(TAG, "onServiceConnected,Failed attaching wallpaper; clearing");
				}
			}
		}

		public void onServiceDisconnected(ComponentName name) {
			mService = null;
			mEngine = null;
			if (mWallpaperConnection == this) {
				ThemeLog.w(LOG_TAG, "Wallpaper service gone: " + name);
			}
		}

		public void attachEngine(IWallpaperEngine engine) {
			synchronized (this) {
				if (mConnected) {
					mEngine = engine;
					try {
						engine.setVisibility(true);
					} catch (RemoteException e) {
						// Ignore
					}
				} else {
					try {
						engine.destroy();
					} catch (RemoteException e) {
						// Ignore
					}
				}
			}
		}

		public ParcelFileDescriptor setWallpaper(String name) {
			return null;
		}

		@Override
		public void engineShown(IWallpaperEngine engine) throws RemoteException {
		}
	}
}

