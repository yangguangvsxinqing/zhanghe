package com.fineos.theme.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.os.SystemProperties;

import com.fineos.theme.baidusdk.ThemeApplication;
import com.fineos.theme.utils.Constant;
import com.fineos.theme.utils.ThemeLog;
import com.fineos.theme.utils.ThemeUtils;
import com.fineos.theme.utils.Util;
import com.qq.e.ads.splash.SplashAD;
import com.qq.e.ads.splash.SplashADListener;
import com.umeng.analytics.MobclickAgent;
import com.fineos.theme.R;

import fineos.app.AlertDialog;

public class SplashActivity extends Activity implements SplashADListener {

	@SuppressWarnings("unused")
	private SplashAD splashAD;
	private ViewGroup container;
	private Handler mHandler;
	private static final int ACTION_NEW_ACTIVITY = 1;
	private static final int ACTION_NEW_ACTIVITY_DELAY = 2;
	private static final int DELAY_BEFORE_HOME_DISPLAY = 1000;
	private static final String TAG = "SplashActivity";
	private static final int READ_PHONE_STATE_REQUESTCODE = 100001;
	private static final int READ_EXTERNAL_STORAGE_REQUESTCODE = 100002;
	private static final int GET_ACCOUNTS_REQUESTCODE = 100003;
	private static final int ACCESS_COARSE_LOCATION_REQUESTCODE = 100004;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ThemeLog.v(TAG, "SplashActivity onCreate");
		setContentView(R.layout.activity_splash);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		checkThemePermission();

	}
	
	private void checkThemePermission(){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
			for(String permission : ThemeApplication.getInstance().permissions){
				if(!permissionGranted(permission)){
					if(permission == ThemeApplication.getInstance().permissions[0]){
						showPermissionDialog(READ_PHONE_STATE_REQUESTCODE);
					}else if(permission == ThemeApplication.getInstance().permissions[1]){
						showPermissionDialog(READ_EXTERNAL_STORAGE_REQUESTCODE);
					}else if(permission == ThemeApplication.getInstance().permissions[2]){
						showPermissionDialog(GET_ACCOUNTS_REQUESTCODE);
					}else if(permission == ThemeApplication.getInstance().permissions[3]){
						showPermissionDialog(ACCESS_COARSE_LOCATION_REQUESTCODE);
					}
					return;
				}
			}
		}
		initHandler();
	}
	
	private boolean permissionGranted(String permission){
		PackageManager pm = getPackageManager(); 
		return pm.checkPermission(permission, getPackageName()) == PackageManager.PERMISSION_GRANTED;
	}
	
	private void showPermissionDialog(final int requestCode){

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		TextView msgText;
		View view = LayoutInflater.from(this).inflate(R.layout.update_content_view, null);
		msgText = (TextView) view.findViewById(R.id.update_msg);
		msgText.setVisibility(View.GONE);
		builder.setTitle(R.string.dlg_request_permission).setMessage(getRequestMsg(requestCode)).setView(view).setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				requestPermissions(new String[]{getRequestPermission(requestCode)}, requestCode);
			}

		}).setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				finish();
			}

		});

		builder.create().show();
	}
	
	private String getRequestPermission(int requestCode){
		switch(requestCode){
			case READ_PHONE_STATE_REQUESTCODE:
				return ThemeApplication.getInstance().permissions[0];
			case READ_EXTERNAL_STORAGE_REQUESTCODE:
				return ThemeApplication.getInstance().permissions[1];
			case GET_ACCOUNTS_REQUESTCODE:
				return ThemeApplication.getInstance().permissions[2];
			case ACCESS_COARSE_LOCATION_REQUESTCODE:
				return ThemeApplication.getInstance().permissions[3];
		}
		return null;
	}
	
	private String getRequestMsg(int requestCode){
		switch(requestCode){
		case READ_PHONE_STATE_REQUESTCODE:
			return getResources().getString(R.string.request_permission_read_phone_state);
		case READ_EXTERNAL_STORAGE_REQUESTCODE:
			return getResources().getString(R.string.request_permission_external_storage);
		case GET_ACCOUNTS_REQUESTCODE:
			return getResources().getString(R.string.request_permission_get_accounts);
		case ACCESS_COARSE_LOCATION_REQUESTCODE:
			return getResources().getString(R.string.request_permission_coarse_location);
	}
	return null;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,
			String[] permissions, int[] grantResults) {
		// TODO Auto-generated method stub
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		
		if(grantResults[0]!=PackageManager.PERMISSION_GRANTED){
			finish();
		}
		checkThemePermission();
	}

	private void initHandler() {
		// TODO Auto-generated method stub
		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				ThemeLog.v(TAG, "msg.what =" + msg.what);
				switch (msg.what) {
				case ACTION_NEW_ACTIVITY:

					next();
					break;
				case ACTION_NEW_ACTIVITY_DELAY	:
					finish() ;
					break ;
				default:
					break;
				}
			}
		};
		SharedPreferences sharedPreferences = getSharedPreferences("theme", 0);
		String splashADId = sharedPreferences.getString("splashAd", null);
		String splashAdAdvertiser = sharedPreferences.getString("splashAdAdvertiser", null);
		if ((!SystemProperties.getBoolean("ro.fineos.net.hide_confirm", ThemeUtils.ISHIDENETDIALOG) && Util.getNetworkHint(this)) || splashADId == null) {
			ThemeLog.v(TAG, "SplashActivity ACTION_NEW_ACTIVITY");
			mHandler.sendEmptyMessageDelayed(ACTION_NEW_ACTIVITY, 2000);
		} else {
			container = (ViewGroup) this.findViewById(R.id.splash_container);
			MobclickAgent.updateOnlineConfig(this);
			if(splashAdAdvertiser!=null&&splashAdAdvertiser.equals("1")){//广点通
				splashAD = new SplashAD(this, container, Constant.THEME_AD_APPID, splashADId, this);
			}else{
				mHandler.sendEmptyMessageDelayed(ACTION_NEW_ACTIVITY, 2000);
			}
		}
	}

	@Override
	public void onADPresent() {
		ThemeLog.i(TAG, "SplashADPresent");
	}

	@Override
	public void onADDismissed() {
		ThemeLog.i(TAG, "SplashADDismissed");
		nextDelay();
	}

	private void next() {
		ThemeLog.i(TAG, "SplashActivity next");
		this.startActivity(new Intent(this, ThemeOnlineHomeActivity.class));
		this.finish();
	}
	
	private void nextDelay() {
		ThemeLog.i(TAG, "SplashActivity next");
		this.startActivity(new Intent(this, ThemeOnlineHomeActivity.class));
		mHandler.sendEmptyMessageDelayed(ACTION_NEW_ACTIVITY_DELAY, DELAY_BEFORE_HOME_DISPLAY);
	}
    

	@Override
	public void onNoAD(int arg0) {
		ThemeLog.v(TAG, "SplashActivity onNoAD,ecode =" + arg0);
		nextDelay();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				|| keyCode == KeyEvent.KEYCODE_HOME) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
