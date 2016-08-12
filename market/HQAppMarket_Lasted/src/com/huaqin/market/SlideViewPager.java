package com.huaqin.market;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import com.huaqin.android.market.sdk.Constants;
import com.huaqin.market.list.CategoryAppListActivity;
import com.huaqin.market.list.CategoryListActivity;
import com.huaqin.market.list.DownloadAppBrowser;
import com.huaqin.market.list.DownloadedAppListActivity;
import com.huaqin.market.list.InstalledAppListActivity;
import com.huaqin.market.list.NewAppListActivity;
import com.huaqin.market.list.RankAppListActivity;
import com.huaqin.market.list.RecommandAppListActivity;
import com.huaqin.market.list.SubjectListActivity;
import com.huaqin.market.list.UpgradeAppListActivity;
import com.huaqin.market.ui.AddContactActivity;
import com.huaqin.market.ui.AppInfoActivity;
import com.huaqin.market.ui.MarketPage;
import com.huaqin.market.utils.Constant;
import com.huaqin.market.utils.DeviceUtil;
import com.huaqin.market.utils.OptionsMenu;
import com.huaqin.market.webservice.IMarketService;
import com.huaqin.market.webservice.MarketService;
import com.huaqin.market.webservice.Request;

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
import android.widget.Toast;

/**
 * Tab页面手势滑动切换以及动画效果
 * 
 *
 * 
 */

public class SlideViewPager extends Activity{
	// ViewPager是google SDk中自带的一个附加包的一个类，可以用来实现屏幕间的切换。
	// android-support-v4.jar
	public static MarketPage mainPager;//页卡内容
	private MarketPage mPager;//页卡内容
	private List<View> listViews; // Tab页面列表
	private ImageView cursor;// 动画图片
	private TextView searchBotton;// 搜索按钮
	private TextView manageBotton;// 管理按钮
	private ImageView manageFlag;// 管理标识
	private TextView t1, t2, t3;// 页卡头标
	private int offset = 0;// 动画图片偏移量
	private int currIndex = 0;// 当前页卡编号
	private int bmpW;// 动画图片宽度
	private String page = null;
	private Integer cateID = 0;
	public static boolean manageFlagS = true;
	private LocalActivityManager manager = null;
	public static String newFlag;
	public static String CATE_LIST_REFRESH = "catelist_item_refresh";
	public static String LIST_REFRESH_FREE = "list_item_free";
	public static String MANAGE_FLAG_REFRESH = "manage_flag_refresh";
	public static int svCurrIndex = 0;
//	private Request mCurrentRequest;
	private final BroadcastReceiver mApplicationsReceiver;
	private final BroadcastReceiver mManageFlagReceiver;
	public static View img ;
	public SlideViewPager(){
//		mApplicationsReceiver = new BroadcastReceiver() {
//			@Override
//			public void onReceive(Context context, Intent intent) {
//				Log.v("asd", "SlideViewPager ACTION_PACKAGE_ADDED onReceive");
//				final String action = intent.getAction();
//				Log.v("asd", "SlideViewPager ACTION_PACKAGE_ADDED ="+action);
//				String pkgName = intent.getDataString().replace("package:", "");
//				if(action.equals(PuddingBroadcastReceiver.INSTALLED_APP_MSG)){
//					Log.v("asd", "SlideViewPager ACTION_PACKAGE_ADDED");
////					pkgName += getText(R.string.app_has_installed);
//					
//					Toast.makeText(getApplicationContext(), getText(R.string.app_has_installed), Toast.LENGTH_LONG).show();
//					Log.v("PACKAGE_APPSPRITE_INSTALL", "SlideViewPager pkgName = "+pkgName);
//				}
//				onResume();
//			}
//		};
		
		mApplicationsReceiver = new BroadcastReceiver(){

			@Override
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();
				Log.v("SlideViewPager", "mApplicationsReceiver action="+action);
				if(action.equals(PuddingBroadcastReceiver.INSTALLED_APP_MSG)){
				Toast.makeText(getApplicationContext(), getText(R.string.app_has_installed), Toast.LENGTH_LONG).show();	
				}
				onResume();
			}
		};
		
		mManageFlagReceiver = new BroadcastReceiver(){

			@Override
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();
				Log.v("SlideViewPager", "mManageFlagReceiver action="+action);
				if(action.equals(MANAGE_FLAG_REFRESH)){
					manageFlagS = false;
					manageFlag.setVisibility(View.VISIBLE);
				}
			}
		};
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v("SlideViewPager", "onCreate");
		setContentView(R.layout.slide_view_pager);
		img = findViewById(R.id.img);		
		img.setVisibility(View.INVISIBLE);
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		page = bundle.getString("intentpage");
		cateID = bundle.getInt("cateId");
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
		t3 = (TextView) findViewById(R.id.text3);
		t1.setTextColor(0xff1c79cc);
//		t1.setShadowLayer(1, 1, 1, 0xff1e7490);
		t2.setTextColor(0xff585859);
//		t2.setShadowLayer(1, 1, 1, 0xfffcfcee);
		t3.setTextColor(0xff585859);
//		t3.setShadowLayer(1, 1, 1, 0xfffcfcee);
		t1.setOnClickListener(new MyOnClickListener(0));
		t2.setOnClickListener(new MyOnClickListener(1));
		t3.setOnClickListener(new MyOnClickListener(2));
	}

	/**
	 * 初始化ViewPager
	 */
	private void InitViewPager() {
		mPager = (MarketPage) findViewById(R.id.vPager);
		mainPager = (MarketPage) findViewById(R.id.vPager);
		listViews = new ArrayList<View>();
		searchBotton = (TextView)findViewById(R.id.search);
		
		Drawable drawable = this.getResources().getDrawable(R.drawable.title_seach);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		searchBotton.setCompoundDrawables(drawable, null, null, null);
		manageBotton = (TextView)findViewById(R.id.manage);
		manageFlag = (ImageView)findViewById(R.id.manage_flag);
        if(manageFlagS){
        	manageFlag.setVisibility(View.GONE);
        }else{
        	manageFlag.setVisibility(View.VISIBLE);
        }
		searchBotton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(SlideViewPager.this, SearchAppBrowser.class);
				startActivity(intent);
			}
			
		});
		manageBotton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(SlideViewPager.this, SlideViewPager.class);
				intent.putExtra("intentpage", MarketBrowser.TAB_MANAGE);
				manageFlagS = true;
				manageFlag.setVisibility(View.GONE);
				startActivity(intent);
			}
			
		});
//		LayoutInflater mInflater = getLayoutInflater();
		
		TextView text1 = (TextView)findViewById(R.id.text1);
		TextView text2 = (TextView)findViewById(R.id.text2);
		TextView text3 = (TextView)findViewById(R.id.text3);
		
		if(page.equals(MarketBrowser.TAB_NEW))
		{

			Intent intent1 = new Intent(this, RecommandAppListActivity.class);
			intent1.putExtra("new", true);
			intent1.putExtra("header", true);
			intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			text1.setText(R.string.tab_newapp_recommend);
			listViews.add(getView("1", intent1));

			if(newFlag!=null&&newFlag.equals("1")){
				ImageView flagnew = (ImageView)findViewById(R.id.new_flag);
				flagnew.setVisibility(View.VISIBLE);
			}
			Intent intent2 = new Intent(this, SubjectListActivity.class);
			intent2.putExtra("_id", 11);
			intent2.putExtra("ranking_type", 2);
			intent2.putExtra("header", false);
			text2.setText(R.string.tab_newapp_like);
			listViews.add(getView("2", intent2));

			Intent intent3 = new Intent(this, NewAppListActivity.class);	
			intent3.putExtra("_id", 3);
			intent3.putExtra("ranking_type", 2);
			intent3.putExtra("list_item", 901);
			text3.setText(R.string.tab_newapp_new);
			listViews.add(getView("3", intent3));
		}
		if(page.equals(MarketBrowser.TAB_RANK)){
			Intent intent1 = new Intent();
			intent1.setClass(this, RankAppListActivity.class);
			intent1.putExtra("ranking_type", Constants.RANK_TYPE_WEEK);
	//		intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			text1.setText(R.string.tab_by_week);
			listViews.add(getView("1", intent1));
			
			Intent intent2 = new Intent();
			intent2.setClass(this, RankAppListActivity.class);
			intent2.putExtra("ranking_type", Constants.RANK_TYPE_MONTH);
//			intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			text2.setText(R.string.tab_by_month);
			listViews.add(getView("2", intent2));
			
			Intent intent3 = new Intent();
			intent3.setClass(this, RankAppListActivity.class);
			intent3.putExtra("ranking_type", Constants.RANK_TYPE_TOTAL);
	//		intent3.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			text3.setText(R.string.tab_by_total);
			listViews.add(getView("3", intent3));
		}
//		if(page.equals(MarketBrowser.TAB_CATEGORY)){
//			Intent intent1 = new Intent();
//			intent1.setClass(this, CategoryListActivity.class);
//			intent1.putExtra("cateId", 1);
//			intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			listViews.add(getView("1", intent1));
//		
//			Intent intent2 = new Intent();
//			intent2.setClass(this, CategoryListActivity.class);
//			intent2.putExtra("cateId", 2);
//			intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			listViews.add(getView("2", intent2));
//			
//			Log.v("asd", "TAB_CATEGORY");
//		}
		if(page.equals(MarketBrowser.TAB_MANAGE)){
			Intent getIntent = getIntent();
			boolean bDownload = getIntent.getBooleanExtra("bDownload", false);
			boolean bUpdated = getIntent.getBooleanExtra("bUpdate", false);
			
			Intent intent1 = new Intent();
			intent1.setClass(this, DownloadAppBrowser.class);
			intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent1.putExtra("bDownload", bDownload);
			text1.setText(R.string.tab_download);
			listViews.add(getView("1", intent1));
			
			Intent intent2 = new Intent();
			intent2.setClass(this, UpgradeAppListActivity.class);
			intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			text2.setText(R.string.tab_upgrade);
			listViews.add(getView("2", intent2));
			
			
			Intent intent3 = new Intent();
			intent3.setClass(this, InstalledAppListActivity.class);
			intent3.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			text3.setText(R.string.tab_installed);
			listViews.add(getView("3", intent3));
			
		}
		if(page.equals(MarketBrowser.TAB_CATEAPP)){

			Intent intent1 = new Intent();
			intent1.setClass(this, CategoryAppListActivity.class);
			intent1.putExtra("cateId", cateID);
			intent1.putExtra("type", 1);
			intent1.putExtra("sortType", Constant.LIST_SORT_BY_DOWNLOAD);
			intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			text1.setText(R.string.tab_by_download);
			listViews.add(getView("1", intent1));
			
			Intent intent2 = new Intent();
			intent2.setClass(this, CategoryAppListActivity.class);
			intent2.putExtra("cateId", cateID);
			intent1.putExtra("type", 1);
			intent2.putExtra("sortType", Constant.LIST_SORT_BY_RATING);
			intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			text2.setText(R.string.tab_by_rating);
			listViews.add(getView("2", intent2));
			
			Intent intent3 = new Intent();
			intent3.setClass(this, CategoryAppListActivity.class);
			intent3.putExtra("cateId", cateID);
			intent1.putExtra("type", 1);
			intent3.putExtra("sortType", Constant.LIST_SORT_BY_DATE);
			intent3.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			text3.setText(R.string.tab_by_date);
			listViews.add(getView("3", intent3));
		}
//		listViews.add(mInflater.inflate(R.layout.app_list, null));
//		listViews.add(mInflater.inflate(R.layout.app_list, null));
//		listViews.add(mInflater.inflate(R.layout.app_list, null));
		if(page.equals(MarketBrowser.TAB_NEW)){
			mainPager.setAdapter(new MyPagerAdapter(listViews));
			mainPager.setOnPageChangeListener(new MyOnPageChangeListener());
		}else{
			mPager.setAdapter(new MyPagerAdapter(listViews));
			mPager.setOnPageChangeListener(new MyOnPageChangeListener());
		}
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		img = findViewById(R.id.img);		
		img.setVisibility(View.INVISIBLE);
		
		svCurrIndex = currIndex;
		if(page.equals(MarketBrowser.TAB_MANAGE)){
			manageBotton.setEnabled(false);
		}else{
			manageBotton.setEnabled(true);
		}
		if(page.equals(MarketBrowser.TAB_SEARCH)){
			searchBotton.setEnabled(false);
		}else{
			searchBotton.setEnabled(true);
		}
		if (RecommandAppListActivity.mAppListAdapter != null) {
			RecommandAppListActivity.mAppListAdapter.notifyDataSetChanged();
		}
		if (NewAppListActivity.mAppListAdapter != null) {
			NewAppListActivity.mAppListAdapter.notifyDataSetChanged();
		}
		if (RankAppListActivity.mAppListAdapterWeek != null) {
			RankAppListActivity.mAppListAdapterWeek.notifyDataSetChanged();
		}
		if (RankAppListActivity.mAppListAdapterMonth != null) {
			RankAppListActivity.mAppListAdapterMonth.notifyDataSetChanged();
		}
		if (RankAppListActivity.mAppListAdapterTotal != null) {
			RankAppListActivity.mAppListAdapterTotal.notifyDataSetChanged();
		}
		if(DownloadedAppListActivity.mAppListAdapter != null){
			DownloadedAppListActivity.mAppListAdapter.notifyDataSetChanged();
		}
		if(UpgradeAppListActivity.mAppListAdapter != null){
			UpgradeAppListActivity.mAppListAdapter.notifyDataSetChanged();
		}
//		if(SearchAppListActivity.mAppListAdapter != null){
//			SearchAppListActivity.mAppListAdapter.notifyDataSetChanged();
//		}
        Intent intentCate = new Intent(CATE_LIST_REFRESH);    
        this.sendBroadcast(intentCate);
		InitViewPager();
	}
	 @Override    
	    public boolean dispatchKeyEvent(KeyEvent event) {   
	    	if (event.getKeyCode() == KeyEvent.KEYCODE_BACK                 
	    			&& event.getAction() == KeyEvent.ACTION_UP&&!page.equals(MarketBrowser.TAB_MANAGE)) {      
	    		Intent intentCateFree = new Intent(LIST_REFRESH_FREE);    
	            this.sendBroadcast(intentCateFree);
	    		} 
	    	if (event.getKeyCode()  == KeyEvent.KEYCODE_MENU&& event.getAction() == KeyEvent.ACTION_UP) {
	    		startActivity(new Intent(SlideViewPager.this, OptionsMenu.class));
	    		overridePendingTransition(R.anim.fade, R.anim.hold);
	    	}
	    	if(event.getKeyCode() == KeyEvent.KEYCODE_BACK                 
	    			&& event.getAction() == KeyEvent.ACTION_UP&&page.equals(MarketBrowser.TAB_MANAGE)){
	    		finish();
		    	return true;
	    	}
	    	return false;
	 }
//    @Override
//    public boolean onKeyUp(int keyCode, KeyEvent event) {
//    	Log.v("asd", "onKeyUp page = "+page);
//    	if (keyCode == KeyEvent.KEYCODE_MENU) {
//    		startActivity(new Intent(SlideViewPager.this, OptionsMenu.class));
//    		overridePendingTransition(R.anim.fade, R.anim.hold);
//    	}
////    	if (keyCode == KeyEvent.KEYCODE_BACK) {
////    		Log.v("keyCode", "KeyEvent.KEYCODE_BACK");
////    		if(page.equals(MarketBrowser.TAB_MANAGE)){	
////    			finish();
////    		}
////    	}
//    	return super.onKeyUp(keyCode, event);
//    }
    @Override
	protected void onStart() {
    	// TODO Auto-generated method stub
    	super.onStart();
    	Log.v("asd", "onStart");
    	
		img = findViewById(R.id.img);		
		img.setVisibility(View.INVISIBLE);
    }
    @Override
	protected void onRestart() {
    	// TODO Auto-generated method stub
    	super.onRestart();
    	Log.v("asd", "onRestart");
    	
		img = findViewById(R.id.img);		
		img.setVisibility(View.INVISIBLE);
    }
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		unregisterReceivers();
		super.onDestroy();
	}
	private void registerIntentReceivers() {
		// TODO Auto-generated method stub
		Log.v("registerIntentReceivers", "INSTALLED_APP_MSG");
		IntentFilter intentFilter = new IntentFilter(PuddingBroadcastReceiver.INSTALLED_APP_MSG);
//		intentFilter.addDataScheme("package");
		registerReceiver(mApplicationsReceiver, intentFilter);
		
		IntentFilter manageIntentFilter = new IntentFilter(MANAGE_FLAG_REFRESH);
		registerReceiver(mManageFlagReceiver, manageIntentFilter);
		
	}
	
	private void unregisterReceivers(){
		unregisterReceiver(mApplicationsReceiver);
		unregisterReceiver(mManageFlagReceiver);
	}
	
	/**
	 * 初始化动画
	 */
	private void InitImageView() {
		cursor = (ImageView) findViewById(R.id.cursor);
		bmpW = BitmapFactory.decodeResource(getResources(), R.drawable.a)
				.getWidth();// 获取图片宽度
		Log.v("InitImageView", "bmpW ="+bmpW);
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenW = dm.widthPixels;// 获取分辨率宽度
		int screenH = dm.heightPixels;// 获取分辨率宽度
		Log.v("InitImageView", "screenW ="+screenW);
		Log.v("InitImageView", "screenH ="+screenH);
		offset = (screenW / 3 - bmpW) / 2;// 计算偏移量
		Matrix matrix = new Matrix();
		matrix.postTranslate(offset, 0);
		cursor.setImageMatrix(matrix);// 设置动画初始位置
		
	}

	/**
	 * ViewPager适配器
	 */
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
			mPager.setCurrentItem(index);
		}
	};


	/**
	 * 页卡切换监听
	 */
	public class MyOnPageChangeListener implements OnPageChangeListener {

		int one = offset * 2 + bmpW;// 页卡1 -> 页卡2 偏移量
		int two = one * 2;// 页卡1 -> 页卡3 偏移量

		@Override
		public void onPageSelected(int arg0) {
			Animation animation = null;
			RankAppListActivity.nRankingTypeFlag = arg0+1;
			switch (arg0) {
			case 0:
				if (currIndex == 1) {
					animation = new TranslateAnimation(one, 0, 0, 0);
				} else if (currIndex == 2) {
					animation = new TranslateAnimation(two, 0, 0, 0);
				}
				t1.setTextColor(0xff1c79cc);
//				t1.setShadowLayer(1, 1, 1, 0xff1e7490);
				t2.setTextColor(0xff585859);
//				t2.setShadowLayer(1, 1, 1, 0xfffcfcee);
				t3.setTextColor(0xff585859);
//				t3.setShadowLayer(1, 1, 1, 0xfffcfcee);
				break;
			case 1:
				if (currIndex == 0) {
					animation = new TranslateAnimation(offset, one, 0, 0);
				} else if (currIndex == 2) {
					animation = new TranslateAnimation(two, one, 0, 0);
				}
				t1.setTextColor(0xff585859);
//				t1.setShadowLayer(1, 1, 1, 0xfffcfcee);
				t2.setTextColor(0xff1c79cc);
//				t2.setShadowLayer(1, 1, 1, 0xff1e7490);
				t3.setTextColor(0xff585859);
//				t3.setShadowLayer(1, 1, 1, 0xfffcfcee);
				if(page.equals(MarketBrowser.TAB_NEW)){
		    		Intent intent = new Intent();
					intent.setAction(SubjectListActivity.SUB_LOAD);
					sendBroadcast(intent);
				}
				break;
			case 2:
				if (currIndex == 0) {
					animation = new TranslateAnimation(offset, two, 0, 0);
				} else if (currIndex == 1) {
					animation = new TranslateAnimation(one, two, 0, 0);
				}
				t1.setTextColor(0xff585859);
//				t1.setShadowLayer(1, 1, 1, 0xfffcfcee);
				t2.setTextColor(0xff585859);
//				t2.setShadowLayer(1, 1, 1, 0xfffcfcee);
				t3.setTextColor(0xff1c79cc);
//				t3.setShadowLayer(1, 1, 1, 0xff1e7490);
				if(page.equals(MarketBrowser.TAB_NEW)){
		    		Intent intent = new Intent();
					intent.setAction(NewAppListActivity.NEW_LOAD);
					sendBroadcast(intent);
				}

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
