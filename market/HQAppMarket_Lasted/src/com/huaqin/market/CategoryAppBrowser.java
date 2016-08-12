package com.huaqin.market;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.huaqin.android.market.sdk.Constants;
import com.huaqin.market.list.CategoryListActivity;
import com.huaqin.market.list.NewAppListActivity;
import com.huaqin.market.list.RankAppListActivity;
import com.huaqin.market.list.SortAppListActivity;
import com.huaqin.market.list.SortGameListActivity;
import com.huaqin.market.ui.MarketPage;
import com.huaqin.market.utils.Constant;
import com.huaqin.market.utils.DeviceUtil;
import com.huaqin.market.utils.OptionsMenu;
import com.huaqin.market.webservice.IMarketService;
import com.huaqin.market.webservice.MarketService;
import com.huaqin.market.webservice.Request;
public class CategoryAppBrowser extends Activity{
	// ViewPager是google SDk中自带的一个附加包的一个类，可以用来实现屏幕间的切换。
	// android-support-v4.jar
	private MarketPage cmPager;//页卡内容
	private List<View> listViews; // Tab页面列表
	private ImageView cursor;// 动画图片
	private TextView t1, t2;// 页卡头标
	private int offset = 0;// 动画图片偏移量
	private int currIndex = 0;// 当前页卡编号
	private int bmpW;// 动画图片宽度
	private TextView searchBotton;// 搜索按钮
	private TextView manageBotton;// 管理按钮
	private ImageView manageFlag;// 管理标识
	private String page = null;
	private final BroadcastReceiver mManageFlagReceiver;
	private LocalActivityManager manager = null;
	public CategoryAppBrowser(){
		mManageFlagReceiver = new BroadcastReceiver(){

			@Override
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();
				Log.v("CategoryAppBrowser", "mManageFlagReceiver action="+action);
				if(action.equals(SlideViewPager.MANAGE_FLAG_REFRESH)){
					SlideViewPager.manageFlagS = false;
					manageFlag.setVisibility(View.VISIBLE);
				}
			}
		};
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v("CategoryAppBrowser", "onCreate");
		setContentView(R.layout.tab_category_browser);

		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		page = bundle.getString("intentpage");
		bundle.getInt("cateId");
		Log.v("asd", "page="+page);
	    manager = new LocalActivityManager(this, true);
        manager.dispatchCreate(savedInstanceState);
        
		InitImageView();
		InitTextView();
		InitViewPager();
		
		registerIntentReceivers();
	}
	/**
	 * 初始化头标
	 */
	private void InitTextView() {
		t1 = (TextView) findViewById(R.id.text1);
		t2 = (TextView) findViewById(R.id.text2);

		t1.setOnClickListener(new MyOnClickListener(0));
		t2.setOnClickListener(new MyOnClickListener(1));
	}

	/**
	 * 初始化ViewPager
	 */
	private void InitViewPager() {
		cmPager = (MarketPage) findViewById(R.id.vPager);
		TextView text1 = (TextView)findViewById(R.id.text1);
		TextView text2 = (TextView)findViewById(R.id.text2);
		listViews = new ArrayList<View>();
		searchBotton = (TextView)findViewById(R.id.search);
		Drawable drawable = this.getResources().getDrawable(R.drawable.title_seach);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		searchBotton.setCompoundDrawables(drawable, null, null, null);
		manageBotton = (TextView)findViewById(R.id.manage);
		manageFlag = (ImageView)findViewById(R.id.manage_flag);
        if(SlideViewPager.manageFlagS){
        	manageFlag.setVisibility(View.GONE);
        }else{
        	manageFlag.setVisibility(View.VISIBLE);
        }
		searchBotton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(CategoryAppBrowser.this, SearchAppBrowser.class);
				startActivity(intent);
			}
			
		});
		manageBotton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(CategoryAppBrowser.this, SlideViewPager.class);
				intent.putExtra("intentpage", MarketBrowser.TAB_MANAGE);
				manageFlag.setVisibility(View.GONE);
				SlideViewPager.manageFlagS = true;
				startActivity(intent);
			}
			
		});
		if(page.equals(MarketBrowser.NEW_TAB_APP)){
			Intent intent1 = new Intent();
			intent1.setClass(this, SortAppListActivity.class);
			text1.setText(R.string.tab_title_new);
//			intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			listViews.add(getView("1", intent1));

			Intent intent2 = new Intent();
			intent2.setClass(this, CategoryListActivity.class);
			intent2.putExtra("cateId", 100);
			text2.setText(R.string.tab_title_category);
			intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			listViews.add(getView("2", intent2));
		}else if(page.equals(MarketBrowser.NEW_TAB_GAME)){
			Intent intent1 = new Intent();
			intent1.setClass(this, SortGameListActivity.class);
			text1.setText(R.string.tab_title_new);
//			intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			listViews.add(getView("1", intent1));
			
			Intent intent2 = new Intent();
			intent2.setClass(this, CategoryListActivity.class);
			intent2.putExtra("cateId", 200);
			text2.setText(R.string.tab_title_category);
			intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			listViews.add(getView("2", intent2));
		}
		
		cmPager.setAdapter(new MyPagerAdapter(listViews));
		cmPager.setOnPageChangeListener(new MyOnPageChangeListener());

	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		InitViewPager();
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		unregisterReceivers();
		super.onDestroy();
	}
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_MENU) {
    		startActivity(new Intent(CategoryAppBrowser.this, OptionsMenu.class));
    		overridePendingTransition(R.anim.fade, R.anim.hold);
    	}
    	return super.onKeyUp(keyCode, event);
    }
	/**
	 * 初始化动画
	 */
	private void InitImageView() {
		cursor = (ImageView) findViewById(R.id.cursor);
		bmpW = BitmapFactory.decodeResource(getResources(), R.drawable.arrow_two)
				.getWidth();// 获取图片宽度
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenW = dm.widthPixels;// 获取分辨率宽度
		offset = (screenW / 2 - bmpW)/2;// 计算偏移量
		Matrix matrix = new Matrix();
		matrix.postTranslate(offset, 0);
		cursor.setImageMatrix(matrix);// 设置动画初始位置
//			try {
//				postPV("应用");
//			} catch (NameNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
	}
	private void registerIntentReceivers() {
		// TODO Auto-generated method stub
		Log.v("asd", "registerIntentReceivers");	
		IntentFilter manageIntentFilter = new IntentFilter(SlideViewPager.MANAGE_FLAG_REFRESH);
		registerReceiver(mManageFlagReceiver, manageIntentFilter);
		
	}
	
	private void unregisterReceivers(){
		unregisterReceiver(mManageFlagReceiver);
	}
	/**
	 * ViewPager适配器
	 */
	public class MyPagerAdapter extends PagerAdapter {
		public List<View> mListViews;

		public MyPagerAdapter(List<View> mListViews) {
			Log.v("asd", "mListViews = "+mListViews);
			this.mListViews = mListViews;
			Log.v("asd", "this.mListViews = "+this.mListViews);
		}

		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
			((ViewPager) arg0).removeView(mListViews.get(arg1));
		}

		@Override
		public void finishUpdate(View arg0) {
		}

		@Override
		public int getCount() {
			return mListViews.size();
		}

		@Override
		public Object instantiateItem(View arg0, int arg1) {
			((ViewPager) arg0).addView(mListViews.get(arg1), 0);
			return mListViews.get(arg1);
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == (arg1);
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {
		}
	}

	/**
	 * 头标点击监听
	 */
	public class MyOnClickListener implements View.OnClickListener {
		private int index = 0;

		public MyOnClickListener(int i) {
			index = i;
		}

		@Override
		public void onClick(View v) {
			cmPager.setCurrentItem(index);
		}
	};


	/**
	 * 页卡切换监听
	 */
	public class MyOnPageChangeListener implements OnPageChangeListener {

		int one;// 页卡1 -> 页卡2 偏移量

		@Override
		public void onPageSelected(int arg0) {
			Animation animation = null;
			one = offset*2 + bmpW;
			switch (arg0) {
			case 0:
					animation = new TranslateAnimation(one, 0, 0, 0);
					t1.setTextColor(0xff1c79cc);
//					t1.setShadowLayer(1, 1, 1, 0xff1e7490);
					t2.setTextColor(0xff585859);
//					t2.setShadowLayer(1, 1, 1, 0xfffcfcee);
				break;
			case 1:
					animation = new TranslateAnimation(offset, one, 0, 0);
					t1.setTextColor(0xff585859);
//					t1.setShadowLayer(1, 1, 1, 0xfffcfcee);
					t2.setTextColor(0xff1c79cc);
//					t2.setShadowLayer(1, 1, 1, 0xff1e7490);
				break;
			}
			currIndex = arg0;
			Log.v("asd", "currIndex = "+currIndex);
			animation.setFillAfter(true);// True:图片停在动画结束位置
			animation.setDuration(300);
//			cursor.setAlpha(100);
			cursor.startAnimation(animation);
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	}
	
	private View getView(String id,Intent intent)
	{
		return manager.startActivity(id, intent).getDecorView();
	}
}
/*
public class CategoryAppBrowser extends TabActivity 
	implements TabHost.OnTabChangeListener {  

//	private static final int TAB_ID_PREINSTALL = 0;
	private static final int TAB_ID_APP = 0;
	private static final int TAB_ID_GAME = 1; 
	
	private static final String TAB_APP = "app";
	private static final String TAB_GAME = "game";
	
	private TabHost mTabHost;
	private ArrayList<View> mTabViews;

	public static MarketPage mPager;//页卡内容
	private List<View> listViews; // Tab页面列表
	private ImageView cursor;// 动画图片
	private int offset = 0;// 动画图片偏移量
	private int currIndex = 0;// 当前页卡编号
	private int bmpW;// 动画图片宽度
	private LocalActivityManager manager = null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.tab_category_browser);

		mTabHost = getTabHost();
		manager = new LocalActivityManager(this, true);
        manager.dispatchCreate(savedInstanceState);
        
        InitImageView();
		initTabs();
		
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
		intentFilter = new IntentFilter();
		intentFilter.addAction(OptionsMenu.ACTION_MENU_EXIT);
	}

	@Override
	public void onTabChanged(String tabId) {
		// TODO Auto-generated method stub
		int newTabId = -1;

		if (tabId.equals(TAB_APP)) {
//		if (tabId.equals(TAB_PREINSTALL)) {
//			newTabId = TAB_ID_PREINSTALL;
//		} else if (tabId.equals(TAB_APP)) {
			newTabId = TAB_ID_APP;
		} else {
			newTabId = TAB_ID_GAME;
		}
//		setCurrentTab(newTabId);
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
		
//		tabSpec = mTabHost.newTabSpec(TAB_PREINSTALL);
//		textView = (TextView) layoutInflater.inflate(R.layout.tab_header_indicator, null);
//		textView.setText(R.string.tab_category_preinstall);
//		textView.setLayoutParams(layoutParams);
//		tabSpec.setIndicator(textView);
//		mTabViews.add(textView);
//		intent = new Intent();
//		intent.setClass(this, CategoryListActivity.class);
//		intent.putExtra("cateId", 0);
//		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		tabSpec.setContent(intent);
//		mTabHost.addTab(tabSpec);
		
		// initialize application tab
		tabSpec = mTabHost.newTabSpec(TAB_APP);
		textView = (TextView) layoutInflater.inflate(R.layout.tab_header_indicator, null);
		textView.setText(R.string.tab_category_app);
		textView.setLayoutParams(layoutParams);
		tabSpec.setIndicator(textView);
		mTabViews.add(textView);
		intent = new Intent();
		intent.setClass(this, CategoryListActivity.class);
		intent.putExtra("cateId", 0);
//		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		tabSpec.setContent(intent);
		mTabHost.addTab(tabSpec);
		
		// initialize game tab
		tabSpec = mTabHost.newTabSpec(TAB_GAME);
		textView = (TextView) layoutInflater.inflate(R.layout.tab_header_indicator, null);
		textView.setText(R.string.tab_category_game);
		textView.setLayoutParams(layoutParams);
		tabSpec.setIndicator(textView);
		mTabViews.add(textView);
		intent = new Intent();
		intent.setClass(this, CategoryListActivity.class);
		intent.putExtra("cateId", 0);
//		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		tabSpec.setContent(intent);
		mTabHost.addTab(tabSpec);
		
		mTabHost.setOnTabChangedListener(this);
//		setCurrentTab(TAB_ID_PREINSTALL);
		setCurrentTab(TAB_ID_APP);
		
		mPager = (MarketPage) findViewById(R.id.viewpage);
		Log.v("asd", "mPager="+mPager);
		listViews = new ArrayList<View>();
		Intent intent1 = new Intent();
		intent1.setClass(this, CategoryListActivity.class);
		intent1.putExtra("cateId", 1);
		intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		listViews.add(getView("1", intent1));
		
		Intent intent2 = new Intent();
		intent2.setClass(this, CategoryListActivity.class);
		intent2.putExtra("cateId", 2);
		intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		listViews.add(getView("2", intent2));
		Log.v("asd", "listViews = "+listViews.size());
		Log.v("asd", "listViews = "+listViews);
		mPager.setAdapter(new MyPagerAdapter(listViews));
		mPager.setCurrentItem(0);
		mPager.setOnPageChangeListener(new MyOnPageChangeListener());
	}
	
    private View getView(String id, Intent intent) {
        return manager.startActivity(id, intent).getDecorView();
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_MENU) {
    		startActivity(new Intent(this, OptionsMenu.class));
    		overridePendingTransition(R.anim.fade, R.anim.hold);
    	}
    	return super.onKeyUp(keyCode, event);
    }
    
	private void setCurrentTab(int newTabId) {
		// TODO Auto-generated method stub
		TextView textView = null;
		for (int i = 0; i < mTabViews.size(); i++) {
			textView = (TextView) mTabViews.get(i);
			if (i == newTabId) {
				textView.setTextColor(Color.BLACK);
				textView.setBackgroundResource(R.drawable.bg_tab_header_focus);
			} else {
				textView.setTextColor(Color.WHITE);
				textView.setBackgroundDrawable(null);
			}
		}
	}
	
	private void InitImageView() {
		cursor = (ImageView) findViewById(R.id.cursor);
		bmpW = BitmapFactory.decodeResource(getResources(), R.drawable.a)
				.getWidth();// 获取图片宽度
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenW = dm.widthPixels;// 获取分辨率宽度
		offset = (screenW / 2 - bmpW) / 2;// 计算偏移量
		Matrix matrix = new Matrix();
		matrix.postTranslate(offset, 0);
		cursor.setImageMatrix(matrix);// 设置动画初始位置
	}

	public class MyPagerAdapter extends PagerAdapter {
		public List<View> mListViews;

		public MyPagerAdapter(List<View> mListViews) {
			this.mListViews = mListViews;
		}

		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
			((ViewPager) arg0).removeView(mListViews.get(arg1));
		}

		@Override
		public void finishUpdate(View arg0) {
		}

		@Override
		public int getCount() {
			return mListViews.size();
		}

		@Override
		public Object instantiateItem(View arg0, int arg1) {
			((ViewPager) arg0).addView(mListViews.get(arg1), 0);
			return mListViews.get(arg1);
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == (arg1);
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {
		}
	}

	public class MyOnClickListener implements View.OnClickListener {
		private int index = 0;

		public MyOnClickListener(int i) {
			index = i;
		}

		@Override
		public void onClick(View v) {
			mPager.setCurrentItem(index);
		}
	};

	public class MyOnPageChangeListener implements OnPageChangeListener {
		int one = offset + bmpW;// 页卡1 -> 页卡2 偏移量
		@Override
		public void onPageSelected(int arg0) {
			Animation animation = null;
			TextView textView = null;
//			MarketPage.setTouchIntercept(true);
			for (int i = 0; i < mTabViews.size(); i++) {
				textView = (TextView) mTabViews.get(i);
				Log.v("asd", "textView="+textView);
				if (i == arg0) {
					textView.setBackgroundResource(R.drawable.bg_tab_header_focus);
					textView.setTextColor(Color.BLACK);
				} else {
					textView.setBackgroundDrawable(null);
					textView.setTextColor(Color.RED);
				}
			}	
			switch (arg0) {
			case 0:
					animation = new TranslateAnimation(one, 0, 0, 0);
				break;
			case 1:
					animation = new TranslateAnimation(offset, one, 0, 0);
				break;
			}
			currIndex = arg0;
			Log.v("asd", "currIndex = "+currIndex);
			animation.setFillAfter(true);// True:图片停在动画结束位置
			animation.setDuration(300);
//			cursor.setAlpha(100);
			cursor.startAnimation(animation);
		
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	}
}*/

