package com.huaqin.market;

import java.util.ArrayList;

import com.huaqin.android.market.sdk.Constants;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.TabHost;
import android.widget.TableLayout;
import android.widget.TextView;

public class RankAppBrowser extends TabActivity
	implements TabHost.OnTabChangeListener {

	private static final int TAB_ID_WEEK = 0;
	private static final int TAB_ID_MONTH = 1;
	private static final int TAB_ID_SUMMARY = 2;
	private static final String TAB_WEEK = "weekly";
	private static final String TAB_MONTH = "monthly";
	private static final String TAB_TOTAL = "totally";
	
	private TabHost mTabHost;
	private ArrayList<View> mTabViews;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.tab_rankapp_browser);
		mTabHost = getTabHost();
		
		initTabs();
	}

	@Override
	public void onTabChanged(String tabId) {
		// TODO Auto-generated method stub
		int newTabId = -1;
		
		if (tabId.equals(TAB_WEEK)) {
			newTabId = TAB_ID_WEEK;
		} else if (tabId.equals(TAB_MONTH)) {
			newTabId = TAB_ID_MONTH;
		} else {
			newTabId = TAB_ID_SUMMARY;
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
		
		mTabViews = new ArrayList<View>();
		
		// initialize week ranking tab
		tabSpec = mTabHost.newTabSpec(TAB_WEEK);
		textView = (TextView) layoutInflater.inflate(R.layout.tab_header_indicator, null);
		textView.setText(R.string.tab_by_week);
		textView.setLayoutParams(layoutParams);
		tabSpec.setIndicator(textView);
		mTabViews.add(textView);
		intent = new Intent();
		intent.setClass(this, SplashActivity.class);
		intent.putExtra("ranking_type", Constants.RANK_TYPE_WEEK);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		tabSpec.setContent(intent);
		mTabHost.addTab(tabSpec);
		
		// initialize month ranking tab
		tabSpec = mTabHost.newTabSpec(TAB_MONTH);
		textView = (TextView) layoutInflater.inflate(R.layout.tab_header_indicator, null);
		textView.setText(R.string.tab_by_month);
		textView.setLayoutParams(layoutParams);
		tabSpec.setIndicator(textView);
		mTabViews.add(textView);
		intent = new Intent();
		intent.setClass(this, SplashActivity.class);
		intent.putExtra("ranking_type", Constants.RANK_TYPE_MONTH);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		tabSpec.setContent(intent);
		mTabHost.addTab(tabSpec);
		
		// initialize total ranking tab
		tabSpec = mTabHost.newTabSpec(TAB_TOTAL);
		textView = (TextView) layoutInflater.inflate(R.layout.tab_header_indicator, null);
		textView.setText(R.string.tab_by_total);
		textView.setLayoutParams(layoutParams);
		tabSpec.setIndicator(textView);
		mTabViews.add(textView);
		intent = new Intent();
		intent.setClass(this, SplashActivity.class);
		intent.putExtra("ranking_type", Constants.RANK_TYPE_TOTAL);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		tabSpec.setContent(intent);
		mTabHost.addTab(tabSpec);
		
		mTabHost.setOnTabChangedListener(this);
		setCurrentTab(TAB_ID_WEEK);
	}

	private void setCurrentTab(int tabIdNew) {
		// TODO Auto-generated method stub
		TextView textView = null;
		
		for (int i = 0; i < mTabViews.size(); i++) {
			textView = (TextView) mTabViews.get(i);
			if (i == tabIdNew) {
				textView.setBackgroundResource(R.drawable.bg_tab_header_focus);
				textView.setTextColor(Color.BLACK);
			} else {
				textView.setBackgroundDrawable(null);
				textView.setTextColor(Color.WHITE);
			}
		}
	}
}