package com.huaqin.market;

import java.util.ArrayList;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.TabHost;
import android.widget.TableLayout;
import android.widget.TextView;

import com.huaqin.market.list.DownloadAppBrowser;
import com.huaqin.market.list.InstalledAppListActivity;
import com.huaqin.market.list.UpgradeAppListActivity;
import com.huaqin.market.utils.OptionsMenu;

public class ManageAppBrowser extends TabActivity
	implements TabHost.OnTabChangeListener {

	private static final int TAB_ID_INSTALLED = 0;
	private static final int TAB_ID_UPGRADE = 1;
	private static final int TAB_ID_DOWNLOAD = 2;
	private static final String TAB_INSTALLED = "installed";
	private static final String TAB_UPGRADE = "upgrade";
	private static final String TAB_DOWNLOAD = "download";
	
	private TabHost mTabHost;
	private ArrayList<View> mTabViews;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);		
		
		setContentView(R.layout.tab_manageapp_browser);
		
		mTabHost = getTabHost();
		
		initTabs();
		
	}

	@Override
	public void onTabChanged(String tabId) {
		// TODO Auto-generated method stub
		int newTabId = -1;
		
		if (tabId.equals(TAB_INSTALLED)) {
			newTabId = TAB_ID_INSTALLED;
		} else if (tabId.equals(TAB_UPGRADE)) {
			newTabId = TAB_ID_UPGRADE;
		} else {
			newTabId = TAB_ID_DOWNLOAD;
		}
		setCurrentTab(newTabId);
	}

	private void initTabs() {
		// TODO Auto-generated method stub
		LayoutInflater layoutInflater = 
			(LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		TableLayout.LayoutParams layoutParams = 
			new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT, 1.0f);
		TabHost.TabSpec tabSpec;
		TextView textView;
		Intent intent;
		
		Intent getIntent = getIntent();
		boolean bDownload = getIntent.getBooleanExtra("bDownload", false);
		boolean bUpdated = getIntent.getBooleanExtra("bUpdate", false);
//		boolean bUpdate = getIntent.getBooleanExtra("bUpdate", false);
		Log.v("asd", "bDownload="+bDownload);
		mTabViews = new ArrayList<View>();
		
		// initialize installed packages tab
		tabSpec = mTabHost.newTabSpec(TAB_INSTALLED);
		textView = (TextView) layoutInflater.inflate(R.layout.tab_header_indicator, null);
		textView.setText(R.string.tab_installed);
		textView.setTextSize(18);
		textView.setLayoutParams(layoutParams);
		tabSpec.setIndicator(textView);
		mTabViews.add(textView);
		intent = new Intent();
		intent.setClass(this, InstalledAppListActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		tabSpec.setContent(intent);
		mTabHost.addTab(tabSpec);
		
		// initialize upgradable packages tab
		tabSpec = mTabHost.newTabSpec(TAB_UPGRADE);
		textView = (TextView) layoutInflater.inflate(R.layout.tab_header_indicator, null);
		textView.setText(R.string.tab_upgrade);
		textView.setTextSize(18);
		textView.setLayoutParams(layoutParams);
		tabSpec.setIndicator(textView);
		mTabViews.add(textView);
		intent = new Intent();
		intent.setClass(this, UpgradeAppListActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		tabSpec.setContent(intent);
		mTabHost.addTab(tabSpec);
		
		// initialize upgradable packages tab
		tabSpec = mTabHost.newTabSpec(TAB_DOWNLOAD);
		textView = (TextView) layoutInflater.inflate(R.layout.tab_header_indicator, null);
		textView.setText(R.string.tab_download);
		textView.setTextSize(18);
		textView.setLayoutParams(layoutParams);
		tabSpec.setIndicator(textView);
		mTabViews.add(textView);
		intent = new Intent();
		intent.setClass(this, DownloadAppBrowser.class);
		intent.putExtra("bDownload", bDownload);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		tabSpec.setContent(intent);
		mTabHost.addTab(tabSpec);
		
		mTabHost.setOnTabChangedListener(this);
		
		if(bDownload) {
			setCurrentTab(TAB_ID_DOWNLOAD);
			mTabHost.setCurrentTab(TAB_ID_DOWNLOAD);
		} else if(bUpdated) { 
			setCurrentTab(TAB_ID_UPGRADE);
			mTabHost.setCurrentTab(TAB_ID_UPGRADE);
		} else {
			setCurrentTab(TAB_ID_INSTALLED);
		}
		
	}
//    @Override
//    public boolean onKeyUp(int keyCode, KeyEvent event) {
//    	if (keyCode == KeyEvent.KEYCODE_MENU) {
//    		startActivity(new Intent(this, OptionsMenu.class));
//    		overridePendingTransition(R.anim.fade, R.anim.hold);
//    	}
//    	return super.onKeyUp(keyCode, event);
//    }
	private void setCurrentTab(int tabIdNew) {
		// TODO Auto-generated method stub
		TextView textView = null;
		
		for (int i = 0; i < mTabViews.size(); i++) {
			textView = (TextView) mTabViews.get(i);
			if (i == tabIdNew) {
				textView.setTextColor(Color.BLACK);
				textView.setBackgroundResource(R.drawable.bg_tab_header_focus);
			} else {
				textView.setTextColor(Color.WHITE);
				textView.setBackgroundDrawable(null);
			}
		}
	}
}