package com.huaqin.market.ui;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

import com.huaqin.market.utils.KeywordsFlow;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.Spanned;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.huaqin.android.market.sdk.bean.HotWordList;
import com.huaqin.android.market.sdk.bean.appNameList;
import com.huaqin.market.R;
import com.huaqin.market.SearchAppBrowser;
import com.huaqin.market.utils.Constant;
import com.huaqin.market.utils.OptionsMenu;
import com.huaqin.market.webservice.IMarketService;
import com.huaqin.market.webservice.MarketService;
import com.huaqin.market.webservice.Request;

public class SearchHotActivity extends Activity implements OnClickListener, OnGestureListener{

	private static final int ACTION_HOTWORDS_LIST = 0;
	private static final int ACTION_APPNAMES_LIST = 1;
	private static final int ACTION_NETWORK_ERROR = 2;
	
	private static final int DIALOG_NETWORK_ERROR = 100;
	
	private int nScreenWidth;
	private float nScreenDensity;
	private ArrayList<String> mHotwords;
	
	private View mLoadingIndicator;
	private ImageView mLoadingAnimation;
	private AnimationDrawable loadingAnimation;
	private View mEmptyView;
	private View mScrollView;
	private LinearLayout mHotwordContent;
	
	private IMarketService mMarketService;
	private Request mCurrentRequest;
	private Handler mHandler;

	private KeywordsFlow keywordsFlow;
	private  GestureDetector gDetector;
	
	private int tmpCount;

	private Context mContext;
	public SearchHotActivity() {
		mContext = this;
		tmpCount = 0;
		mHotwords = new ArrayList<String>();
	}

	private void initHandler() {
		// TODO Auto-generated method stub
		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch (msg.what) {
				case ACTION_HOTWORDS_LIST:
					HotWordList list = (HotWordList) msg.obj;
					String[] hotwords = list.getHotwords();
					
					if (hotwords != null && hotwords.length > 0) {
						mHotwords.clear();
						for (int i = 0; i < hotwords.length; i++) {
							mHotwords.add(hotwords[i]);
						}
						if(mHotwords != null){
							initHotwordsView();
							mEmptyView.setVisibility(View.GONE);
							mLoadingIndicator.setVisibility(View.GONE);
							mScrollView.setVisibility(View.VISIBLE);
							
							feedKeywordsFlow(keywordsFlow, mHotwords);//jm
							keywordsFlow.go2Show(KeywordsFlow.ANIMATION_IN);//jm
						}
						
					} else {
						mEmptyView.setVisibility(View.VISIBLE);
						mLoadingIndicator.setVisibility(View.GONE);
						mScrollView.setVisibility(View.GONE);
					}
					
					break;
				case ACTION_APPNAMES_LIST:
					Log.v("asd", "ACTION_APPNAMES_LIST");
//					Log.v("asd", "msg.obj = "+msg.obj);
					appNameList searchappNameList = (appNameList) msg.obj;
					Log.v("asd", "appNameList = "+searchappNameList.getAppNames().length);
					
					break;
				case ACTION_NETWORK_ERROR:
					mEmptyView.setVisibility(View.VISIBLE);
					mLoadingIndicator.setVisibility(View.GONE);
					mScrollView.setVisibility(View.GONE);
					Toast.makeText(mContext, mContext.getString(R.string.error_network_low_speed), Toast.LENGTH_LONG).show();
//					showDialog(DIALOG_NETWORK_ERROR);
					break;
					
				default:
					break;
				}
			}
		};
	}

	private void initViews() {
		// TODO Auto-generated method stub
		mLoadingIndicator = findViewById(R.id.fullscreen_loading_indicator);
		mLoadingAnimation = 
			(ImageView) mLoadingIndicator.findViewById(R.id.fullscreen_loading);
		mLoadingAnimation.setBackgroundResource(R.anim.loading_anim);
		loadingAnimation = (AnimationDrawable) mLoadingAnimation.getBackground();
		mLoadingAnimation.post(new Runnable(){
			@Override     
			public void run() {
				loadingAnimation.start();     
			}                     
		});
		
		mEmptyView = findViewById(R.id.low_speed);
		TextView tvRefresh = (TextView) mEmptyView.findViewById(R.id.lowspeed_refresh);
		tvRefresh.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mCurrentRequest != null) {
					mEmptyView.setVisibility(View.GONE);
					mScrollView.setVisibility(View.GONE);
					mLoadingIndicator.setVisibility(View.VISIBLE);
					mMarketService.getSearchHotwords(mCurrentRequest);
				}
			}
		});
		mEmptyView.setVisibility(View.GONE);
		
		mHotwordContent = (LinearLayout) findViewById(R.id.search_content_layout);
		mScrollView = findViewById(R.id.search_content_layout);
//		mScrollView.setScrollbarFadingEnabled(true);
		mScrollView.setVisibility(View.GONE);		
		keywordsFlow = (KeywordsFlow) findViewById(R.id.keywordsFlow);
		keywordsFlow.setDuration(500);
		keywordsFlow.setClickListener(this/*new OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.v("asd", "View = "+v);
				if (v instanceof TextView) {
					String keyword = ((TextView) v).getText().toString();
					Log.v("asd", "keyword = "+keyword);
					Intent intent = new Intent();
					intent.setAction(SearchAppBrowser.SEARCH_REQUEST);
					intent.putExtra("keyword", keyword);
					intent.putExtra("updateTextInput", true);
					sendBroadcast(intent);
				}else{
					keywordsFlow.rubKeywords();
					// keywordsFlow.rubAllViews();
					feedKeywordsFlow(keywordsFlow, mHotwords);
					keywordsFlow.go2Show(KeywordsFlow.ANIMATION_IN);
				}
			}
		}*/);
		Log.v("asd", "mHotwords = "+mHotwords);
		addSearchHotwordsRequest();
	}
	private void feedKeywordsFlow(KeywordsFlow keywordsFlow, ArrayList<String> words) {
//		Random rd = new Random();
//		
//		
//	      int n = words.size();
//          Random rand = new Random();
//          boolean[]  bool = new boolean[n];
//          int randInt = 0;
//          for(int i = 0; i < KeywordsFlow.MAX ; i++) {
//               do {
//                   randInt  = rand.nextInt(n);
//               }while(bool[randInt]);
//              bool[randInt] = true;
//
//  			String tmp = words.get(randInt);
//  			keywordsFlow.feedKeyword(tmp);
//         }

		for (int i = 0; i < KeywordsFlow.MAX; i++) {	
			if(tmpCount >= words.size()){
				tmpCount = 0;
			}
			String tmp = words.get(tmpCount);
			keywordsFlow.feedKeyword(tmp);
			tmpCount+=1;
		}
		
	}

	private void initHotwordsView() {
		// TODO Auto-generated method stub		
		// remove all exist hot word views
		int count = mHotwordContent.getChildCount() - 1;
		mHotwordContent.removeViews(1, count);

		LinearLayout.LayoutParams layoutParams = 
			new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		int margin = (int) (5.0f * nScreenDensity);
		layoutParams.leftMargin = margin;
		layoutParams.rightMargin = margin;
		layoutParams.topMargin = margin;
		layoutParams.bottomMargin = margin;
		
		LinearLayout linearLayout = new LinearLayout(this);
		linearLayout.setOrientation(LinearLayout.HORIZONTAL);
		linearLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		
		int width = nScreenWidth;
		float textSize = 20.0f;
		int textColor = getResources().getColor(R.color.search_hotword);
		View.OnClickListener textClickListener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.v("asd", "initHotwordsView View = "+v);
				
				String keyword = ((TextView) v).getText().toString();
				
				Intent intent = new Intent();
				intent.setAction(SearchAppBrowser.SEARCH_REQUEST);
				intent.putExtra("keyword", keyword);
				intent.putExtra("updateTextInput", true);
				sendBroadcast(intent);
			}
		};

		for (int i = 0; i < mHotwords.size(); i++) {
			String hotword = mHotwords.get(i);
			TextView textView = new TextView(this);
			textView.setTextSize(textSize);
			textView.setTextColor(textColor);
			textView.setLayoutParams(layoutParams);
			textView.setOnClickListener(textClickListener);
			
			Spanned spanned = Html.fromHtml("<u>" + hotword + "</u>");
			textView.setText(spanned);
			
			TextPaint textPaint = textView.getPaint();
			int textViewWidth = (int) ((textPaint.measureText(hotword) + 20.0f) * nScreenDensity);
			textViewWidth += layoutParams.leftMargin;
			textViewWidth += layoutParams.rightMargin;
			
			if (textViewWidth < width) {
				width -= textViewWidth;
				linearLayout.addView(textView);
			} else {
				mHotwordContent.addView(linearLayout);
				
				linearLayout = null;
				linearLayout = new LinearLayout(this);
				linearLayout.setOrientation(LinearLayout.HORIZONTAL);
				linearLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
				
				linearLayout.addView(textView);
				width = nScreenWidth;
			}
		}
		mHotwordContent.addView(linearLayout);
	}

	private void addSearchHotwordsRequest() {
		// TODO Auto-generated method stub
		Request request = new Request(0, Constant.TYPE_SEARCH_HOTWORDS);
		
		request.addObserver(new Observer() {

			@Override
			public void update(Observable observable, Object data) {
				// TODO Auto-generated method stub
				if (data != null) {
					Message msg = Message.obtain(mHandler, ACTION_HOTWORDS_LIST, data);
					mHandler.sendMessage(msg);
				} else {
					Request request = (Request) observable;
					if (request.getStatus() == Constant.STATUS_ERROR) {
						mHandler.sendEmptyMessage(ACTION_NETWORK_ERROR);
					}
				}
			}
		});
		
		mCurrentRequest = request;
		mMarketService.getSearchHotwords(request);
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
					Log.v("asd", "data = "+((appNameList)data).getAppNames().length);
					Log.v("asd", "msg = "+msg);
					mHandler.sendMessage(msg);
				} else {
					Request request = (Request) observable;
					if (request.getStatus() == Constant.STATUS_ERROR) {
						mHandler.sendEmptyMessage(ACTION_NETWORK_ERROR);
					}
				}
			}
		});
		
		mCurrentRequest = request;
		mMarketService.getSearchHotwords(request);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
		nScreenWidth = displayMetrics.widthPixels;
		nScreenDensity = displayMetrics.density;
		
		mMarketService = MarketService.getServiceInstance(this);
		
		setContentView(R.layout.search_hot_main);
		initHandler();
		initViews();
		
		gDetector = new GestureDetector(this);
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		if (id == DIALOG_NETWORK_ERROR) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this.getParent());
			builder.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.dlg_network_error_title)
				.setMessage(R.string.dlg_network_error_msg)
				.setPositiveButton(R.string.btn_retry, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if (mCurrentRequest != null) {
							mMarketService.getSearchHotwords(mCurrentRequest);
						}
					}
				})
				.setNegativeButton(R.string.btn_cancel, null);
			return builder.create();
		}
		return null;
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// TODO Auto-generated method stub
//		boolean bRet = super.onCreateOptionsMenu(menu);
//		if (bRet) {
//			bRet = OptionsMenu.onCreateOptionsMenu(menu);
//		}
//		return bRet;
//	}
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_MENU) {
    		startActivity(new Intent(SearchHotActivity.this, OptionsMenu.class));
    		overridePendingTransition(R.anim.fade, R.anim.hold);
    	}
    	return super.onKeyUp(keyCode, event);
    }
//    @Override
//	public boolean onTouchEvent(MotionEvent event) {
//		//return gDetector.onTouchEvent(event);
//		Log.v("onTouchEvent", "data");
//	
//		return gDetector.onTouchEvent(event);
//	}

@Override
public void onClick(View v) {
	// TODO Auto-generated method stub
	Log.v("asd", "View = "+v);
	if (v instanceof TextView) {
		String keyword = ((TextView) v).getText().toString();
		Log.v("asd", "keyword = "+keyword);
		Intent intent = new Intent();
		intent.setAction(SearchAppBrowser.SEARCH_REQUEST);
		intent.putExtra("keyword", keyword);
		intent.putExtra("updateTextInput", true);
		sendBroadcast(intent);
	}
}
@Override
public boolean onTouchEvent(MotionEvent event) {
	//return gDetector.onTouchEvent(event);
	Log.v("onTouchEvent", "data");
	if(mHotwords.size()!=0){
		keywordsFlow.rubKeywords();
		// keywordsFlow.rubAllViews();
		feedKeywordsFlow(keywordsFlow, mHotwords);
		keywordsFlow.go2Show(KeywordsFlow.ANIMATION_IN);
		return gDetector.onTouchEvent(event);
	}
	return false;
}
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		// TODO Auto-generated method stub
//		return OptionsMenu.onOptionsItemSelected(this, item);
//	}

@Override
public boolean onDown(MotionEvent arg0) {
	// TODO Auto-generated method stub
	return false;
}

@Override
public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2,
		float arg3) {
	// TODO Auto-generated method stub
	Log.v("asd", "onFling");
	if(mHotwords!=null){
		keywordsFlow.rubKeywords();
		// keywordsFlow.rubAllViews();
		feedKeywordsFlow(keywordsFlow, mHotwords);
		keywordsFlow.go2Show(KeywordsFlow.ANIMATION_IN);
	}
	return false;
}

@Override
public void onLongPress(MotionEvent arg0) {
	// TODO Auto-generated method stub
	
}

@Override
public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
		float arg3) {
	// TODO Auto-generated method stub
	return false;
}

@Override
public void onShowPress(MotionEvent arg0) {
	// TODO Auto-generated method stub
	
}

@Override
public boolean onSingleTapUp(MotionEvent arg0) {
	// TODO Auto-generated method stub
	return false;
}

}