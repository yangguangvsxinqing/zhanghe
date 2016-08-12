package com.huaqin.market.list;

import java.util.ArrayList;

import com.huaqin.market.R;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.TabHost;
import android.widget.TableLayout;
import android.widget.TextView;

public class DownloadAppBrowser extends TabActivity
	implements TabHost.OnTabChangeListener {

	private static final int TAB_ID_DOWNLOADED = 1;
	private static final int TAB_ID_DOWNLOADING = 0;
	private static final String TAB_DOWNLOADED = "downloaded";
	private static final String TAB_DOWNLOADING = "downloading";
	
	private TabHost mTabHost;
	private ArrayList<View> mTabViews;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);		
		
		setContentView(R.layout.tab_downloaded_app_browser);
		mTabHost = getTabHost();
		initTabs();
	}

	@Override
	public void onTabChanged(String tabId) {
		// TODO Auto-generated method stub
		int newTabId = -1;
		
		if (tabId.equals(TAB_DOWNLOADED)) {
			newTabId = TAB_ID_DOWNLOADED;
		} else if (tabId.equals(TAB_DOWNLOADING)) {
			newTabId = TAB_ID_DOWNLOADING;
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
		Log.v("asd", "bDownload="+bDownload);
		mTabViews = new ArrayList<View>();
		// initialize upgradable packages tab
				tabSpec = mTabHost.newTabSpec(TAB_DOWNLOADING);
				textView = (TextView) layoutInflater.inflate(R.layout.tab_header_indicator, null);
				textView.setText(R.string.app_downloading);
				textView.setLayoutParams(layoutParams);
				tabSpec.setIndicator(textView);
				mTabViews.add(textView);
				intent = new Intent();
				intent.setClass(this, DownloadingAppListActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				tabSpec.setContent(intent);
				mTabHost.addTab(tabSpec);
		// initialize installed packages tab
		tabSpec = mTabHost.newTabSpec(TAB_DOWNLOADED);
		textView = (TextView) layoutInflater.inflate(R.layout.tab_header_indicator, null);
		textView.setText(R.string.app_downloaded);
		textView.setLayoutParams(layoutParams);
		tabSpec.setIndicator(textView);
		mTabViews.add(textView);
		intent = new Intent();
		intent.setClass(this, DownloadedAppListActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		tabSpec.setContent(intent);
		mTabHost.addTab(tabSpec);
		
		mTabHost.setOnTabChangedListener(this);
		if(bDownload) { 
			setCurrentTab(TAB_ID_DOWNLOADED);
			mTabHost.setCurrentTab(TAB_ID_DOWNLOADED);
		} else {
			setCurrentTab(TAB_ID_DOWNLOADING);
			mTabHost.setCurrentTab(TAB_ID_DOWNLOADING);
		}
	}

	private void setCurrentTab(int tabIdNew) {
		// TODO Auto-generated method stub
		TextView textView = null;
		Log.v("asd", "setCurrentTab="+tabIdNew);
		if(DownloadedAppListActivity.mAppListAdapter != null){
			DownloadedAppListActivity.mAppListAdapter.notifyDataSetChanged();
		}
		for (int i = 0; i < mTabViews.size(); i++) {
			textView = (TextView) mTabViews.get(i);
//			Drawable drawable = mContext.getResources().getDrawable(R.drawable.bg_manage_download_tab_focus);
			if (i == tabIdNew) {
				textView.setTextColor(0xff70aadb);
				textView.setBackgroundResource(R.drawable.bg_manage_download_tab_focus);
			} else {
				textView.setTextColor(0xffb1b1b1);
				textView.setBackgroundResource(R.drawable.bg_manage_download_tab_normal);
			}
//			textView.setTextColor(Color.WHITE);
		}
	}
}