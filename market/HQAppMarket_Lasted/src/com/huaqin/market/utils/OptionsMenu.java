package com.huaqin.market.utils;


import com.huaqin.market.MarketBrowser;
import com.huaqin.market.R;
import com.huaqin.market.SlideViewPager;
import com.huaqin.market.ui.AddContactActivity;
import com.huaqin.market.ui.SettingsActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class OptionsMenu extends Activity {
	private int m_nSreenHeight = 0;
	private Button m_menu_setting;
	private Button m_menu_contact;
	private Button m_menu_about;
	private Button m_menu_exit;
	public static final String ACTION_MENU_EXIT = "com.hauqin.intent.action.MENU_EXIT";
	public static final int EXIT_APPLICATION = 10;
    final int myPid = MarketBrowser.processMyPid;
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.v("asd", "onCreate0");
        super.onCreate(savedInstanceState);
        Log.v("asd", "onCreate1");
        setContentView(R.layout.option_menu);
Log.v("asd", "onCreate2");
        m_menu_setting = (Button)findViewById(R.id.menu1);
        m_menu_contact = (Button)findViewById(R.id.menu2);
        m_menu_about = (Button)findViewById(R.id.menu3);
        m_menu_exit = (Button)findViewById(R.id.menu4);
        m_menu_setting.setOnClickListener(menuSettingClickListener);
        m_menu_contact.setOnClickListener(menuContactClickListener);
        m_menu_about.setOnClickListener(menuAboutClickListener);
        m_menu_exit.setOnClickListener(menuExitClickListener);
        Log.v("asd", "onCreate");
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        m_nSreenHeight = dm.heightPixels;
             
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_MENU) {
    		this.finish();
    	}
    	return super.onKeyUp(keyCode, event);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	if (event.getX() < m_nSreenHeight - m_menu_setting.getHeight()) {
    		finish();
    	}
        return false;
    }
    private OnClickListener menuSettingClickListener = new OnClickListener() {
    	public void onClick(View v) {
    		startActivity(new Intent(OptionsMenu.this, SettingsActivity.class));
    		finish();
    	}
    };
    private OnClickListener menuContactClickListener = new OnClickListener() {
    	public void onClick(View v) {
    		startActivity(new Intent(OptionsMenu.this, AddContactActivity.class));
    		finish();
    	}
    };
    private OnClickListener menuAboutClickListener = new OnClickListener() {
    	public void onClick(View v) {
    		Intent manageIntent = new Intent(OptionsMenu.this, SlideViewPager.class);
    		manageIntent.putExtra("bDownload", false);
    		manageIntent.putExtra("bUpdate", false);
//    		manageIntent = new Intent(this, SlideViewPager.class);
    		manageIntent.putExtra("intentpage", MarketBrowser.TAB_MANAGE);
    		startActivity(manageIntent);
//    		startActivity(new Intent(OptionsMenu.this, VersionActivity.class));
    		finish();
    	}
    };
    private OnClickListener menuExitClickListener = new OnClickListener() {
		public void onClick(View v) {
    				
//    		Intent intent = new Intent();   
//    		intent.setAction(ACTION_MENU_EXIT);
//    		sendBroadcast(intent);// 该函数用于发送广播   
    		
    		Intent mIntent = new Intent();  
    		mIntent.setClass(OptionsMenu.this, MarketBrowser.class);  
    		mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);  
    		mIntent.putExtra("flag", EXIT_APPLICATION);  
    		Log.v("asd", "putExtra flag");
    		startActivity(mIntent);  
    		finish();   
    	}
    };
}
/*
public class OptionsMenu {

	private static final int ID_SETTING = 1;
	private static final int ID_ACCOUNT = 2;
	private static final int ID_CONTACT = 3;
	private static final int ID_ABOUT = 4;
	private static final int ID_EXIT = 5;

	public static boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, ID_SETTING, 0, R.string.menu_setting)
			.setIcon(R.drawable.ic_menu_setting);
		menu.add(0, ID_CONTACT, 0, R.string.menu_contact)
			.setIcon(R.drawable.ic_menu_account);
//			.setEnabled(false);
		menu.add(0, ID_ABOUT, 0, R.string.menu_about)
			.setIcon(R.drawable.ic_menu_about);
		menu.add(0, ID_EXIT, 0, R.string.menu_exit)
		.setIcon(R.drawable.ic_menu_account);
		return true;
	}

	public static boolean onOptionsItemSelected(Activity activity, MenuItem item) {
		Intent intent = new Intent();
		
		switch (item.getItemId()) {
		case ID_SETTING:
			intent.setClass(activity, SettingsActivity.class);
			activity.startActivity(intent);
			break;
			
		case ID_ACCOUNT:
			break;
			
		case ID_CONTACT:
			break;
			
		case ID_ABOUT:
			intent.setClass(activity, VersionActivity.class);
			activity.startActivity(intent);
			break;
		
		case ID_EXIT:
			Process.killProcess(android.os.Process.myPid());
			break;
		default:
			break;
		}
		return true;
	}
}*/