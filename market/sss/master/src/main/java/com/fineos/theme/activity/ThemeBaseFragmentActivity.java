package com.fineos.theme.activity;


import android.app.ActionBar;
import android.app.Dialog;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.fineos.theme.R;
import com.fineos.theme.SystemBarTintManager;
import com.fineos.theme.utils.Constant;
import com.fineos.theme.utils.ThemeLog;

import fineos.app.AlertDialog;
import fineos.app.ProgressDialog;

public abstract class ThemeBaseFragmentActivity extends FragmentActivity {
	protected static final int DIALOG_LOADING = 999;
	public int minDialogShowTime = 600;
	public static int ONLINE = Constant.THEME_ONLINE_LIST_TYPE;
	public static int LOCAL = Constant.THEME_LOCAL_LIST_TYPE;
	protected ProgressDialog loadingDialog;
	protected ActionBar mActionBar;
	private boolean mbShowCustomActionBar;
	private int online_flag;
	public long mShowDialogTime;
	
	private boolean mbFirstEnter;
	private View mLoadingView;
	private SystemBarTintManager mSystemBarTintManager;
	private FrameLayout mDectorView;
	private Handler mHandler = new Handler();
	private ImageView mFirstLoadImageView;
	private static final String SAVE_ONLINE = "save_online";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentapiVersion <= Build.VERSION_CODES.KITKAT) {
			setTheme(com.fineos.internal.R.style.Theme_Holo_Light_FineOS);
		} else {
			setTheme(com.fineos.internal.R.style.FineOSTheme_Material_Light_WhiteActionBar);
		}
		super.onCreate(savedInstanceState);
		online_flag = getIntent().getIntExtra("onlineFlag", Constant.THEME_LOCAL_LIST_TYPE);
		if (savedInstanceState != null) {
			online_flag = savedInstanceState.getInt(SAVE_ONLINE, Constant.THEME_LOCAL_LIST_TYPE);
		}
		mbShowCustomActionBar = getResources().getBoolean(R.bool.show_custom_action_bar);
		setContentView(R.layout.activity_fragment);
		FragmentManager fm = getSupportFragmentManager();
		Fragment fragment = fm.findFragmentById(R.id.container);
		if (fragment == null) {
			fragment = createFragment(online_flag);
			fm.beginTransaction().add(R.id.container, fragment).commit();
		}
		mbFirstEnter = true;
		initActionBar();
		initLoadingView();
	}
	
	private void initLoadingView() {
		mDectorView = (FrameLayout) getWindow().getDecorView();
		mLoadingView = View.inflate(getApplicationContext(), R.layout.grid_foot_view, null);
		mDectorView.addView(mLoadingView, 0);
		mLoadingView.setVisibility(View.INVISIBLE);
		mSystemBarTintManager = new SystemBarTintManager(this); 
	}
	
	private void initActionBar() {
		mActionBar = getActionBar();
		mActionBar.setDisplayUseLogoEnabled(false);
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayShowHomeEnabled(false);
		mActionBar.setDisplayShowCustomEnabled(mbShowCustomActionBar);
		if (mbShowCustomActionBar) {
			int layoutid = getActionBarLayoutId();
			if (layoutid > 0) {
				mActionBar.setCustomView(layoutid);
				initCustomActionBar();
			}
		}
	}
	
	private void initCustomActionBar() {
		final TextView tv = (TextView) mActionBar.getCustomView().findViewById(R.id.switchonline);
		final TextView actionbarTitle = (TextView) mActionBar.getCustomView().findViewById(R.id.actionbar_title);
		if (online_flag == ONLINE) {
//			tv.setText(R.string.aciton_bar_local);
			tv.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.button_local), null, null, null);
		} else {
//			tv.setText(R.string.aciton_bar_online);
			tv.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.button_online), null, null, null);
		}
		actionbarTitle.setClickable(true);
		setCustomActionBarText(actionbarTitle);
		tv.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				if (online_flag == ONLINE) {
					online_flag = LOCAL;
//					tv.setText(R.string.aciton_bar_online);
					tv.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.button_online), null, null, null);
				} else {
					online_flag = ONLINE;
//					tv.setText(R.string.aciton_bar_local);
					tv.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.button_local), null, null, null);
				}
				switchFragment();
				switchonLineOrLocal(online_flag);
				setCustomActionBarText(actionbarTitle);
			}
		});
	}
	
	private void switchFragment() {
		FragmentManager fm = getSupportFragmentManager();
		Fragment fragment = fm.findFragmentById(R.id.container);
		fragment = createFragment(online_flag);
		fm.beginTransaction().replace(R.id.container, fragment).commit();
		mbFirstEnter = true;
	}

	public void showLoadingDialog() {
		mShowDialogTime = SystemClock.currentThreadTimeMillis();
		if (mbFirstEnter) {
//			showDialog(DIALOG_LOADING);
			mbFirstEnter = false;
		} else {
			showLoadingView();
		}
	}
	
	@Override
    protected void onSaveInstanceState(Bundle outState) {
    	// TODO Auto-generated method stub
    	outState.putInt(SAVE_ONLINE, online_flag);
    	super.onSaveInstanceState(outState);
    }
	
	public void showLoadingView() {
		android.widget.FrameLayout.LayoutParams lParams = (android.widget.FrameLayout.LayoutParams)mLoadingView.getLayoutParams();
		lParams.bottomMargin = mSystemBarTintManager.getConfig().getNavigationBarHeight();
		lParams.height = mSystemBarTintManager.getConfig().getActionBarHeight();
		lParams.gravity= Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
		mDectorView.bringChildToFront(mLoadingView);
		mLoadingView.setVisibility(View.VISIBLE);
		ImageView imageView = (ImageView)mLoadingView.findViewById(R.id.progressimage);
		AnimationDrawable ad = (AnimationDrawable)imageView.getBackground();
		ad.start();
	}
	
	private void hideLoadingView() {
		mLoadingView.setVisibility(View.INVISIBLE);
		ImageView imageView = (ImageView)mLoadingView.findViewById(R.id.progressimage);
		AnimationDrawable ad = (AnimationDrawable)imageView.getBackground();
		ad.stop();
	}

	public void hideLoadingDialog() {
		hideLoadingView();
	}
	
	public boolean isonline() {
		return online_flag == ONLINE;
	}
	
	public int getOnLineFlag() {
		return online_flag;
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
			return loadingDialog;
		}
		return null;
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
	
	protected int getActionBarLayoutId() {
		return R.layout.theme_actionbar;
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
	
	protected abstract Fragment createFragment(int onlineFlag);
	protected abstract void switchonLineOrLocal(int onlineFlag);
	protected abstract void setCustomActionBarText(TextView tv);

}
