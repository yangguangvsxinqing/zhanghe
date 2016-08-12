package com.huaqin.market;

import java.util.ArrayList;

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

import com.huaqin.market.list.NewAppListActivity;
import com.huaqin.market.list.RecommandAppListActivity;
import com.huaqin.market.list.SubjectListActivity;

public class NewAppBrowser extends TabActivity
	implements TabHost.OnTabChangeListener {

	private static final int TAB_ID_RECOMMEND = 0; 
	private static final int TAB_ID_LIKE = 1;
	private static final int TAB_ID_NEW = 2;
	private static final String TAB_RECOMMEND = "recommend";
	private static final String TAB_LIKE = "like";
	private static final String TAB_NEW = "new";
	public static final int FLEEP_DISTANCE = 100;
	private int currentTabID = -1; 
	
	private TabHost mTabHost;
	private ArrayList<View> mTabViews;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.tab_newapp_browser);
		mTabHost = getTabHost();
		
		initTabs();
	}

	@Override
	public void onTabChanged(String tabId) {
		// TODO Auto-generated method stub
		if (tabId.equals(TAB_RECOMMEND)) {
			currentTabID = TAB_ID_RECOMMEND;
		} else if (tabId.equals(TAB_LIKE)) {
			currentTabID = TAB_ID_LIKE;
		} else {
			currentTabID = TAB_ID_NEW;
		}
		setCurrentTab(currentTabID);
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
		
		// initialize recommend tab
		tabSpec = mTabHost.newTabSpec(TAB_RECOMMEND);
		textView = (TextView) layoutInflater.inflate(R.layout.tab_header_indicator, null);
		textView.setText(R.string.tab_newapp_recommend);
		textView.setLayoutParams(layoutParams);
		tabSpec.setIndicator(textView);
		mTabViews.add(textView);
		intent = new Intent();
		intent.setClass(this, RecommandAppListActivity.class);
		intent.putExtra("new", true);
		intent.putExtra("header", true);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		tabSpec.setContent(intent);
		mTabHost.addTab(tabSpec);
		
		// initialize column recommend tab
		tabSpec = mTabHost.newTabSpec(TAB_LIKE);
		textView = (TextView) layoutInflater.inflate(R.layout.tab_header_indicator, null);
		textView.setText(R.string.tab_newapp_like);
		textView.setLayoutParams(layoutParams);
		tabSpec.setIndicator(textView);
		mTabViews.add(textView);
		intent = new Intent();
		intent.setClass(this, SubjectListActivity.class);
		intent.putExtra("_id", 11);
		intent.putExtra("ranking_type", 2);
		intent.putExtra("header", false);
		tabSpec.setContent(intent);
		mTabHost.addTab(tabSpec);
		
		// initialize newest tab
		tabSpec = mTabHost.newTabSpec(TAB_NEW);
		textView = (TextView) layoutInflater.inflate(R.layout.tab_header_indicator, null);
		textView.setText(R.string.tab_newapp_new);
		textView.setLayoutParams(layoutParams);
		tabSpec.setIndicator(textView);
		mTabViews.add(textView);
		intent = new Intent();
		intent.setClass(this, NewAppListActivity.class);
		intent.putExtra("_id", 3);
		intent.putExtra("ranking_type", 2);
		intent.putExtra("list_item", 901);
		tabSpec.setContent(intent);
		mTabHost.addTab(tabSpec);
		
		mTabHost.setOnTabChangedListener(this);
		currentTabID = TAB_ID_RECOMMEND;
		setCurrentTab(currentTabID);
	}

	private void setCurrentTab(int tabIdNew) {
		// TODO Auto-generated method stub
		TextView textView = null;
		for (int i = 0; i < 3; i++) {
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