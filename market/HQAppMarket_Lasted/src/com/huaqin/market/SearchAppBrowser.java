package com.huaqin.market;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TableLayout;
import android.widget.TextView;

import com.huaqin.android.market.sdk.bean.appNameList;
import com.huaqin.market.list.SearchAppListActivity;
import com.huaqin.market.ui.SearchHotActivity;
import com.huaqin.market.ui.TabWebActivity;
import com.huaqin.market.utils.Constant;
import com.huaqin.market.utils.OptionsMenu;
import com.huaqin.market.utils.SearchAdapter;
import com.huaqin.market.webservice.IMarketService;
import com.huaqin.market.webservice.MarketService;
import com.huaqin.market.webservice.Request;

public class SearchAppBrowser extends TabActivity
	implements TabHost.OnTabChangeListener {

	public static final String TAG = "SearchAppBrowser";
	public static final String SEARCH_REQUEST = "broadcast_search_request";
	public static final String SEARCH_RESULT = "show_search_result";
	public static final String SEARCH_MAIN = "show_search_main";
	
	private static final int TAB_ID_SEARCH_HOT = 0;
	private static final int TAB_ID_SEARCH_RESULT = 1;
	private static final String TAB_SEARCH_HOT = "hot";
	private static final String TAB_SEARCH_RESULT = "result";
	private static final int ACTION_APPNAMES_LIST = 11;
	private static final int ACTION_NETWORK_ERROR = 12;
	private View mSearchBarView;
	private TabHost mTabHost;
	private ArrayList<View> mTabViews;
	private AutoCompleteTextView mSearchText;
	private Handler mHandler;
	private String mKeywords;
	private BroadcastReceiver mSearchReciever;
	private BroadcastReceiver mSearchBack;
	public static appNameList searchappNameList;
	private IMarketService mMarketService;
	private Context mContext ;
	public static Button btnSearch = null;
	private int newTabId = 0;
	public SearchAppBrowser() {

		mKeywords = "";
		mSearchReciever = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action = intent.getAction();
				
				if (action.equals(SEARCH_RESULT)) {
					mTabHost.setCurrentTab(TAB_ID_SEARCH_RESULT);
					SearchAppListActivity.sPageFlag = false;
				} else if (action.equals(SEARCH_REQUEST)) {
					mKeywords = intent.getStringExtra("keyword");
					if (intent.getBooleanExtra("updateTextInput", false)) {
		//				mSearchText.setText(mKeywords);
						SearchAppListActivity.sPageFlag = false;
					}
				}
			}
		};
		
		mSearchBack = new BroadcastReceiver(){

			@Override
			public void onReceive(Context arg0, Intent intent) {
				// TODO Auto-generated method stub
				
				String action = intent.getAction();
				
				if (action.equals(SEARCH_MAIN)) {
					mTabHost.setCurrentTab(TAB_ID_SEARCH_HOT);
					SearchAppListActivity.sPageFlag = true;
				}
				else{
					mTabHost.setCurrentTab(TAB_ID_SEARCH_RESULT);
				}
			}
			
		};
	}

	private void registerIntentReceivers() {
		// TODO Auto-generated method stub
		IntentFilter intentFilter = new IntentFilter(SEARCH_REQUEST);
		intentFilter.addAction(SEARCH_RESULT);
	    registerReceiver(mSearchReciever, intentFilter);
	    
	    intentFilter = new IntentFilter(SEARCH_MAIN);
	    registerReceiver(mSearchBack, intentFilter);
	    
	}

	private void unregisterIntentReceivers() {
		// TODO Auto-generated method stub
		unregisterReceiver(mSearchReciever);
		unregisterReceiver(mSearchBack);
	}

	private void initViews() {
		// TODO Auto-generated method stub
		initSearchBar();
		initTabs();
		addSearchDatabaseRequest();
	}
	private void initHandler() {
		// TODO Auto-generated method stub
		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch (msg.what) {
				case ACTION_APPNAMES_LIST:
					Log.v("asd", "ACTION_APPNAMES_LIST");
//					Log.v("asd", "msg.obj = "+msg.obj);
					searchappNameList = (appNameList) msg.obj;
					Log.v("asd", "appNameList = "+searchappNameList.getAppNames().length);
					initSearchBar();
					break;
				default:
					break;
				}
			}
		};
	}
	private void initSearchBar() {
		// TODO Auto-generated method stub
		mSearchBarView = findViewById(R.id.search_bar_layout);
		
		mSearchText = (AutoCompleteTextView)findViewById(R.id.search_bar_text);
		if(searchappNameList != null){
			String[] database = new String[]{};
//			database = searchappNameList.getAppNames();
			List<String> tmp = new ArrayList<String>();  
			        for(String str:searchappNameList.getAppNames()){  
			            if(str!=null && str.length()!=0){  
			                tmp.add(str);  
			           }  
			       }  
			database = tmp.toArray(new String[0]); 
			SearchAdapter<String> searchAdapter = new SearchAdapter<String>(
					mContext,
					R.layout.search_droplist,
					database);

			mSearchText.setAdapter(searchAdapter);
		}

		Button btnClear = (Button) mSearchBarView.findViewById(R.id.btn_search_clear);
		btnClear.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mSearchText.setText("");
			}
		});
		
		btnSearch = (Button) mSearchBarView.findViewById(R.id.btn_search);
		btnSearch.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String keywords = mSearchText.getText().toString().trim();
				SearchAppListActivity.sPageFlag = false;
				if (keywords.length() > 0) {
					startSearch(keywords.replace("\'", "").replaceAll("\"", ""));
				}
			}
		});
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
		
		tabSpec = mTabHost.newTabSpec(TAB_SEARCH_HOT);
		textView = (TextView) layoutInflater.inflate(R.layout.tab_header_indicator, null);
		textView.setText(R.string.tab_search_hot);
		textView.setTextColor(Color.BLACK);
		textView.setLayoutParams(layoutParams);
		tabSpec.setIndicator(textView);
		mTabViews.add(textView);
		intent = new Intent();
		intent.setClass(this, SearchHotActivity.class);
		tabSpec.setContent(intent);
		mTabHost.addTab(tabSpec);
		
		tabSpec = mTabHost.newTabSpec(TAB_SEARCH_RESULT);
		textView = (TextView) layoutInflater.inflate(R.layout.tab_header_indicator, null);
		textView.setText(R.string.tab_search_result);
		textView.setTextColor(Color.BLACK);
		textView.setLayoutParams(layoutParams);
		tabSpec.setIndicator(textView);
		mTabViews.add(textView);
		intent = new Intent();
		intent.setClass(this, SearchAppListActivity.class);
		tabSpec.setContent(intent);
		mTabHost.addTab(tabSpec);
		
		mTabHost.setOnTabChangedListener(this);
		mTabHost.setCurrentTab(TAB_ID_SEARCH_RESULT);
		mTabHost.setCurrentTab(TAB_ID_SEARCH_HOT);
	}
	private void addSearchDatabaseRequest() {
		// TODO Auto-generated method stub
		Request request = new Request(0, Constant.TYPE_SEARCH_DATABASE);
		
		request.addObserver(new Observer() {

			@Override
			public void update(Observable observable, Object data) {
				// TODO Auto-generated method stub
				if (data != null) {
					Message msg = Message.obtain(mHandler, ACTION_APPNAMES_LIST, data);
					mHandler.sendMessage(msg);
				} else {
					Request request = (Request) observable;
					if (request.getStatus() == Constant.STATUS_ERROR) {
						mHandler.sendEmptyMessage(ACTION_NETWORK_ERROR);
					}
				}
			}
		});
		mMarketService.getSearchHotwords(request);
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

	private void startSearch(String keywords) {
		// TODO Auto-generated method stub
		// close soft keyboard
    	InputMethodManager inputManager = 
    		(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    	inputManager.hideSoftInputFromWindow(mSearchText.getWindowToken(),
    			InputMethodManager.RESULT_UNCHANGED_SHOWN);
    	
		mKeywords = keywords;
		Intent intent = new Intent();
		intent.setAction(SEARCH_REQUEST);
		intent.putExtra("keyword", mKeywords);
		sendBroadcast(intent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.tab_search_browser);

		mContext = this;
		mTabHost = getTabHost();
		mMarketService = MarketService.getServiceInstance(this);
		initHandler();
		initViews();
		
		registerIntentReceivers();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		InputMethodManager imm = ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE));
		imm.hideSoftInputFromWindow(mSearchText.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		unregisterIntentReceivers();
		super.onDestroy();
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		SlideViewPager.svCurrIndex = newTabId;
	}
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// TODO Auto-generated method stub
		if (event.getAction() == KeyEvent.ACTION_UP) {
			switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_BACK:
				Log.v("SearchAppListActivity", "sPageFlag"+SearchAppListActivity.sPageFlag);
				if(SearchAppListActivity.sPageFlag){
					finish();
				}else{
					Intent intent = new Intent();
					intent.setAction(SearchAppBrowser.SEARCH_MAIN);
					sendBroadcast(intent);
					SearchAppListActivity.sPageFlag = true;
				}
				return true;
			case KeyEvent.KEYCODE_MENU:
		    		startActivity(new Intent(SearchAppBrowser.this, OptionsMenu.class));
		    		overridePendingTransition(R.anim.fade, R.anim.hold);
		    		return true;
			default:
				break;
			}
		}
		return super.dispatchKeyEvent(event);
	}
	@Override
	public void onTabChanged(String tabId) {
		// TODO Auto-generated method stub
		if (tabId.equals(TAB_SEARCH_HOT)) {
			newTabId = TAB_ID_SEARCH_HOT;
		} else {
			newTabId = TAB_ID_SEARCH_RESULT;
		}
//		setCurrentTab(newTabId);
	}
}