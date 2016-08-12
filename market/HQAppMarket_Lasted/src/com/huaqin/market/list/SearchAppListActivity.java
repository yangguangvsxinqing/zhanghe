package com.huaqin.market.list;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.huaqin.android.market.sdk.bean.Application;
import com.huaqin.market.R;
import com.huaqin.market.SearchAppBrowser;
import com.huaqin.market.SlideViewPager;
import com.huaqin.market.download.Downloads;
import com.huaqin.market.list.AppListAdapter.ViewHolder;
import com.huaqin.market.model.Application2;
import com.huaqin.market.model.Image2;
import com.huaqin.market.ui.AppInfoActivity;
import com.huaqin.market.ui.AppInfoPreloadActivity;
import com.huaqin.market.ui.LoadingAnimation;
import com.huaqin.market.ui.SearchHotActivity;
import com.huaqin.market.utils.CachedThumbnails;
import com.huaqin.market.utils.Constant;
import com.huaqin.market.utils.OptionsMenu;
import com.huaqin.market.webservice.IMarketService;
import com.huaqin.market.webservice.MarketService;
import com.huaqin.market.webservice.Request;

public class SearchAppListActivity extends ListActivity
	implements AdapterView.OnItemClickListener {
	
	private static final int ACTION_SEARCH_APP_LIST_HEAD = 0;
	private static final int ACTION_SEARCH_APP_LIST = 1;
	private static final int ACTION_SEARCH_APP_ICON = 2;
	private static final int ACTION_NETWORK_ERROR = 3;
	private static final int PROGRESSBAR_UPDATEING = 13;
	private static final int DIALOG_NETWORK_ERROR = 100;
	
	private ListView mListView;
	private AppListAdapter mAppListAdapter;
	private View mFooterView;
	private static View mFooterViewSpace;
	private View mLoadingIndicator;
	private ImageView mLoadingAnimation;
	private AnimationDrawable loadingAnimation;
	private TextView mStatus;
	private Hashtable<Integer, Boolean> mIconStatusMap;
	public static Hashtable<Integer, Boolean> mIconAnimStatusMap;
	private IMarketService mMarketService;
	private Request mCurrentRequest;
	private Handler mHandler;
	private String mKeywords;
	private boolean bInflatingAppList;
	private boolean bBusy;
	private boolean bReachEnd;
	private int nStartIndex;
	private Animation icon_push_in; 
	private int nSearchIndex = 0;
	
	private Context mContext;
	
	public static boolean sPageFlag = true;
	
	private BroadcastReceiver mSearchReciever;
	private final BroadcastReceiver mApplicationsReceiver;
	private AbsListView.OnScrollListener mScrollListener;

	private DownloadingContentObserver mObserver;
	private DownloadingThread mDownloadThread;
	private Cursor mCursor;
	private String[] mCols = new String [] {
            Downloads._ID,
            Downloads.COLUMN_TITLE,
            Downloads.COLUMN_CURRENT_BYTES,
            Downloads.COLUMN_TOTAL_BYTES,
            Downloads._DATA,
            Downloads.COLUMN_APP_ID,
            Downloads.COLUMN_CONTROL
    };
	
	private final int idColumn = 0;
	private final int titleColumn = 1;
	private final int currentBytesColumn = 2;
	private final int totalBytesColumn = 3;
	private final int dataColumn = 4;
	private final int appIdColumn = 5;
	private final int controlColumn = 6;
	private ProgressBar mProgressBar;
	public SearchAppListActivity() {

		mIconStatusMap = new Hashtable<Integer, Boolean>();
		mIconAnimStatusMap = new Hashtable<Integer, Boolean>();
		mKeywords = "";
		bInflatingAppList = false;
		bBusy = false;
		bReachEnd = false;
		nStartIndex = 0;
		mContext = this;
		
		mSearchReciever = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action = intent.getAction();
				Log.v("SearchAppListActivity", "mSearchReciever = "+action);
				if (action.equals(SearchAppBrowser.SEARCH_REQUEST)) {
					mKeywords = intent.getStringExtra("keyword");
					if(mKeywords != null){
					Log.v("SearchAppListActivity", "SEARCH_REQUEST = "+mKeywords);
					bReachEnd = false;
					nStartIndex = 0;
//					sPageFlag = true;
					// clear last search results first
//					Log.v("SearchAppListActivity","JimmyJin mAppListAdapter8989="+mAppListAdapter);
//					SearchAppBrowser.btnSearch.setEnabled(false);
					if (mAppListAdapter != null) {
						mAppListAdapter.clear();
						mListView.setAdapter(mAppListAdapter);
						nStartIndex = 0;
					}
					mListView.setVisibility(View.GONE);
					mLoadingIndicator.setVisibility(View.VISIBLE);
					nSearchIndex+=1;
					inflateSearchAppListHead();
					
					Intent intent2 = new Intent(SearchAppBrowser.SEARCH_RESULT);
					SearchAppListActivity.this.sendBroadcast(intent2);
				}
				}
			}
		};
		
		mScrollListener = new AbsListView.OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				switch (scrollState) {
				case SCROLL_STATE_IDLE:
					bBusy = false;
					int start = view.getFirstVisiblePosition();
					int counts = view.getChildCount();
					int position = 0;
					
					for (int i = 0; i < counts; i++) {
						position = start + i;
						
//						if (!mIconStatusMap.containsKey(Integer.valueOf(position))) {
							ViewHolder viewHolder = 
								(ViewHolder) view.getChildAt(i).getTag();
							if (viewHolder != null) {
//								mAppListAdapter.initBtnStatus(viewHolder,(Application2)viewHolder.mButton.getTag());
								int id = (int) mAppListAdapter.getItemId(position);
								Drawable drawable = getThumbnail(position, id);
								viewHolder.mThumbnail.setImageDrawable(drawable);
							}
						}
//					}
					
					// as list scrolled to end, send request to get above data
					if ((start + counts) >= (mAppListAdapter.getCount() - 2)) {
//						SearchAppBrowser.btnSearch.setEnabled(false);
						inflateSearchAppList();
					}
					break;
				case SCROLL_STATE_TOUCH_SCROLL:
				case SCROLL_STATE_FLING:
					if (!bBusy) {
						clearPendingThumbRequest();
						bBusy = true;
					}
					break;
				default:
					break;
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				
			}
		};
		mApplicationsReceiver = new BroadcastReceiver(){

			@Override
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();
				if(action.equals(SlideViewPager.CATE_LIST_REFRESH)){	
					onResume();
				}
			}
		};
	}

	private void registerIntentReceivers() {
		// TODO Auto-generated method stub
		IntentFilter intentFilter = new IntentFilter(SearchAppBrowser.SEARCH_REQUEST);
	    registerReceiver(mSearchReciever, intentFilter);
		IntentFilter cateIntentFilter = new IntentFilter(SlideViewPager.CATE_LIST_REFRESH);
//		intentFilter.addDataScheme("package");
		registerReceiver(mApplicationsReceiver, cateIntentFilter);
	}

	private void unregisterIntentReceivers() {
		// TODO Auto-generated method stub
		unregisterReceiver(mSearchReciever);
		unregisterReceiver(mApplicationsReceiver);
	}

	private void initHandler() {
		// TODO Auto-generated method stub
		mHandler = new Handler() {

			@SuppressWarnings("null")
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch (msg.what) {
				case ACTION_SEARCH_APP_LIST_HEAD:
					@SuppressWarnings("unchecked")
					ArrayList<Application> appListHead =
					(ArrayList<Application>) msg.obj;
					Log.v("handleMessage", "ACTION_SEARCH_APP_LIST_HEAD");
					int appListHeadSize = 0;
//					int appListHeadIndex = 0;
					AppListAdapter newAppListAdapter = null;
					ListView mListHeadView = getListView();
					if (appListHead != null) {
						appListHeadSize = appListHead.size();
						Log.v("ACTION_SEARCH_APP_LIST_HEAD", "appListHead.size() ="+appListHead.size());
						nStartIndex += appListHeadSize;
						Log.v("ACTION_SEARCH_APP_LIST_HEAD", "nStartIndex1 ="+nStartIndex);
					}	
					if (newAppListAdapter == null) {
						newAppListAdapter = new AppListAdapter(
								SearchAppListActivity.this,
								appListHead,
								Constant.LIST_ITEMTYPE_NORMAL,
								"0");
						
						mListHeadView.setAdapter(newAppListAdapter);
					}
					
					if (appListHeadSize == 0 || appListHeadSize < Constant.LIST_COUNT_PER_TIME) {
						bReachEnd = true;
						if (mListView.getFooterViewsCount() > 0) {
							mListView.removeFooterView(mFooterView);
//							mListView.addFooterView(mFooterViewSpace);
						}
					}
					mListView.setVisibility(View.GONE);
					Log.v("ACTION_SEARCH_APP_LIST_HEAD", "nSearchIndex ="+nSearchIndex);
					if(nSearchIndex<1){
						mLoadingIndicator.setVisibility(View.GONE);
						mListView.setVisibility(View.VISIBLE);
						bInflatingAppList = false;
					}
					mAppListAdapter = newAppListAdapter;
					mListView = mListHeadView;
					newAppListAdapter = null;
					break;
				case ACTION_SEARCH_APP_LIST:
					@SuppressWarnings("unchecked")
					ArrayList<Application> appList =
						(ArrayList<Application>) msg.obj;
					Log.v("handleMessage", "ACTION_SEARCH_APP_LIST_HEAD");
					int appListSize = 0;
					int appListIndex = 0;
					mListView.setVisibility(View.GONE);
					if (appList != null) {
						appListSize = appList.size();
						nStartIndex += appListSize;
					}	
					
//					SearchAppBrowser.btnSearch.setEnabled(true);
					
					if (mAppListAdapter == null) {
						mAppListAdapter = new AppListAdapter(
								SearchAppListActivity.this,
								appList,
								Constant.LIST_ITEMTYPE_NORMAL,
								"0");
						mListView.setAdapter(mAppListAdapter);
//						SearchAppBrowser.btnSearch.setEnabled(true);
					} else{
						if(!mAppListAdapter.isEmpty()){
							if(appList!=null&&appList.size()>0){
								if(mAppListAdapter.getItemId(appListIndex) == appList.get(appListIndex).getAppId()){
									mAppListAdapter.clear();
								}
							}
						}
						for (; appListIndex < appListSize; appListIndex++) {
							mAppListAdapter.add(appList.get(appListIndex));
						}
						if (appListIndex >= appListSize && appListIndex != 0) {
							mAppListAdapter.notifyDataSetChanged();
//							SearchAppBrowser.btnSearch.setEnabled(true);
						}
					}

					if (appListSize == 0 || appListSize < Constant.LIST_COUNT_PER_TIME) {
						bReachEnd = true;
						if (mListView.getFooterViewsCount() > 0) {
							mListView.removeFooterView(mFooterView);
//							mListView.addFooterView(mFooterViewSpace);
//							SearchAppBrowser.btnSearch.setEnabled(true);
						}
					}
					Log.v("ACTION_SEARCH_APP_LIST", "nSearchIndex ="+nSearchIndex);
					if(nSearchIndex<1){
						mLoadingIndicator.setVisibility(View.GONE);
						mListView.setVisibility(View.VISIBLE);
						bInflatingAppList = false;
					}
					String where = Downloads.WHERE_RUNNING;
					
					mCursor = mContext.getContentResolver()
								.query(Downloads.CONTENT_URI,
										mCols, where, null, null);
					
					mAppListAdapter.setCursor(mCursor);
					if(mCursor!=null&&!mCursor.isClosed()){
						mCursor.close();
					}
					break;
					
				case ACTION_SEARCH_APP_ICON:
					Image2 icInfo = (Image2) msg.obj;
					
					if (icInfo.mAppIcon != null) {
						CachedThumbnails.cacheThumbnail(
								SearchAppListActivity.this,
								icInfo._id, icInfo.mAppIcon);
						
						ImageView imageView = 
							(ImageView) mListView.findViewWithTag(String.valueOf(icInfo._id));
						if(imageView != null) 
						{	imageView.setImageDrawable(icInfo.mAppIcon);
						}
						if (mAppListAdapter != null) {
							mAppListAdapter.notifyDataSetChanged();
						}
					}
					break;
				case PROGRESSBAR_UPDATEING:
					Object[] data = (Object[])msg.obj;
					Integer mAppId = (Integer)data[0];
					int progress = ((Integer)data[1]).intValue();
					mProgressBar = (ProgressBar) mListView.findViewWithTag(getProcessbarViewTag(mAppId));	
					mStatus = (TextView) mListView.findViewWithTag(getStatusViewTag(mAppId));
					Log.v("case PROGRESSBAR_UPDATEING", "mStatus ="+mStatus);
					if(mProgressBar != null) {
						mProgressBar.setVisibility(View.VISIBLE);
						mStatus.setVisibility(View.VISIBLE);
						mProgressBar.setProgress(progress);
					}
					break;					
				case ACTION_NETWORK_ERROR:
					mLoadingIndicator.setVisibility(View.GONE);
					mListView.setVisibility(View.VISIBLE);
					Toast.makeText(mContext, mContext.getString(R.string.error_network_low_speed), Toast.LENGTH_LONG).show();
//					showDialog(DIALOG_NETWORK_ERROR);
					break;
					
				default:
					break;
				}
			}
		};
	}
	private static String getProcessbarViewTag(int appId) {
		return "processbar" + appId;
	}
	private static String getStatusViewTag(int appId) {
		return "status" + appId;
	}
	private void initListView() {
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
		mLoadingIndicator.setVisibility(View.GONE);
		
		mListView = getListView();
		mListView.setScrollbarFadingEnabled(true);
		mListView.setVisibility(View.GONE);
		mFooterView = LayoutInflater.from(this).inflate(R.layout.app_list_footer, null);
		mFooterViewSpace = LayoutInflater.from(mContext).inflate(R.layout.app_list_foot_space, null);
		ProgressBar loadingSmall =
			(ProgressBar) mFooterView.findViewById(R.id.small_loading);
		LoadingAnimation aniSmall = new LoadingAnimation(
				this,
				LoadingAnimation.SIZE_SMALL,
				0, 0, LoadingAnimation.DEFAULT_DURATION);
		loadingSmall.setIndeterminateDrawable(aniSmall);
		Button btn_retry = (Button)mFooterView.findViewById(R.id.btn_retry);
		btn_retry.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mCurrentRequest != null) {
					mMarketService.getAppListByKeyword(mCurrentRequest);
				}
			}
		});
		mListView.addFooterView(mFooterView);
		
		View emptyView = findViewById(R.id.list_empty);
		mListView.setEmptyView(emptyView);
		
		mListView.setOnItemClickListener(this);
		mListView.setOnScrollListener(mScrollListener);
		
		//Added-s by JimmyJin
		if(mFooterViewSpace != null){
			mListView.addFooterView(mFooterViewSpace);
		}else
			mListView.removeFooterView(mFooterViewSpace);
		//Added-e by JimmyJin
	}
	private void inflateSearchAppListHead(){
		if (bReachEnd || bInflatingAppList) {
			return;
		}
		Request request = new Request(0, Constant.TYPE_SEARCH_APP_LIST);
		Object[] params = new Object[2];
	
		params[0] = mKeywords;
		params[1] = Integer.valueOf(nStartIndex);
		request.setData(params);
		nSearchIndex-=1;
		request.addObserver(new Observer() {

			@Override
			public void update(Observable observable, Object data) {
				// TODO Auto-generated method stub
				if (data != null) {
					Message msg = Message.obtain(mHandler, ACTION_SEARCH_APP_LIST_HEAD, data);
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
		mMarketService.getAppListByKeyword(request);
	
	}
	private void inflateSearchAppList() {
		// TODO Auto-generated method stub
		if (bReachEnd || bInflatingAppList) {
			return;
		}
		Request request = new Request(0, Constant.TYPE_SEARCH_APP_LIST);
		Object[] params = new Object[2];
		
		params[0] = mKeywords;
		params[1] = Integer.valueOf(nStartIndex);
		request.setData(params);
		request.addObserver(new Observer() {

			@Override
			public void update(Observable observable, Object data) {
				// TODO Auto-generated method stub
				if (data != null) {
					Message msg = Message.obtain(mHandler, ACTION_SEARCH_APP_LIST, data);
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
		mMarketService.getAppListByKeyword(request);
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(mAppListAdapter != null){
			mAppListAdapter.notifyDataSetChanged();
			String where = Downloads.WHERE_RUNNING;
			mCursor = mContext.getContentResolver()
					.query(Downloads.CONTENT_URI,
							mCols, where, null, null);
		
		mAppListAdapter.setCursor(mCursor);
		if(mCursor!=null&&!mCursor.isClosed()){
			mCursor.close();
		}
		}
	}
	private void addThumbnailRequest(int position, int id) {
		
		Request request = new Request(0L, Constant.TYPE_APP_ICON);
		Object[] params = new Object[2];
		String imgUrl = mAppListAdapter.getItem(position).getIconUrl();
		
		params[0] = Integer.valueOf(id);
		params[1] = imgUrl;
		request.setData(params);
		request.addObserver(new Observer() {

			@Override
			public void update(Observable observable, Object data) {
				// TODO Auto-generated method stub
				if (data != null) {
					Message msg = Message.obtain(mHandler, ACTION_SEARCH_APP_ICON, data);
					mHandler.sendMessage(msg);
				}
			}
		});
		mCurrentRequest = request;
		mMarketService.getAppIcon(request);
	}

	private void clearPendingThumbRequest() {
		// TODO Auto-generated method stub
		Iterator<Integer> iterator = mIconStatusMap.keySet().iterator();
		
		while (iterator.hasNext()) {
			Integer value = iterator.next();
			if (!mIconStatusMap.get(value).booleanValue()) {
				iterator.remove();
			}
		}
		mMarketService.clearThumbRequest(MarketService.THREAD_THUMB);
	}

	public Drawable getThumbnail(int position, int id) {
		// TODO Auto-generated method stub
		boolean bThumbExists = mIconStatusMap.containsKey(Integer.valueOf(position));
		if (bBusy && !bThumbExists) {
			return CachedThumbnails.getDefaultIcon(this);
		}
		
		Drawable drawable = CachedThumbnails.getThumbnail(this, id);
		if (drawable == null) {
			boolean bThumbCached = false;
			if (bThumbExists) {
				bThumbCached = mIconStatusMap.get(Integer.valueOf(position)).booleanValue();
			}
			if (bThumbExists && !bThumbCached) {
				// cause thumb record existed
				// do not sent thumb request again, just return default icon
				return CachedThumbnails.getDefaultIcon(this);
			} else {
				// cause thumb record not existed 
				// or thumb not cached yet,
				// set cached flag as false, and send thumb request
				mIconStatusMap.put(Integer.valueOf(position), Boolean.valueOf(false));
				addThumbnailRequest(position, id);
				return CachedThumbnails.getDefaultIcon(this);
			}
		} else {
			// cause thumb has been cached
			// set cached flag as true
			mIconStatusMap.put(Integer.valueOf(position), Boolean.valueOf(true));
			return drawable;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mMarketService = MarketService.getServiceInstance(this);
		
		setContentView(R.layout.app_list);
		
		initHandler();
		initListView();
		
		registerIntentReceivers();
		mObserver = new DownloadingContentObserver();
        getContentResolver().registerContentObserver(Downloads.CONTENT_URI,
                true, mObserver);
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
						switch (mCurrentRequest.getType()) {
						case Constant.TYPE_SEARCH_APP_LIST:
							mMarketService.getAppListByKeyword(mCurrentRequest);
							break;
						case Constant.TYPE_APP_ICON:
							mMarketService.getAppIcon(mCurrentRequest);
							break;
						default:
							break;
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
    	if (keyCode == KeyEvent.KEYCODE_BACK) {
    		Intent intent = new Intent();
			intent.setAction(SearchAppBrowser.SEARCH_MAIN);
			sendBroadcast(intent);
    	}
    	return super.onKeyUp(keyCode, event);
    }

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mAppListAdapter != null) {
			mAppListAdapter.clear();
		}
		if (mListView != null) {
			mListView.setAdapter(null);
		}
		unregisterIntentReceivers();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if(position < mAppListAdapter.getCount()) {
			Intent intent = new Intent(this, AppInfoPreloadActivity.class);
//			Intent intent = new Intent(this, AppInfoActivity.class);
//			intent.putExtra("appInfo",new Application2(mAppListAdapter.getItem(position)));
			intent.putExtra("appId", mAppListAdapter.getItem(position).getAppId());
			/*************Added-s by JimmyJin for Pudding Project**************/
			intent.putExtra("type",0);
			/*************Added-s by JimmyJin for Pudding Project**************/
			intent.putExtra("download", mAppListAdapter.getItem(position).getTotalDownloads());
			startActivity(intent);
		}
	}
	private class DownloadingContentObserver extends ContentObserver {

		public DownloadingContentObserver() {
			super(mHandler);
		}
		
		@Override
		public void onChange(final boolean selfChange) {
			mDownloadThread = new DownloadingThread();
            mDownloadThread.start();
        }
	}
	
	private class DownloadingThread extends Thread {
		@Override
		public void run() {
			updateProgressBar();
		}

		private void updateProgressBar() {
	        
			Cursor cursor = mContext.getContentResolver().query(
	                Downloads.CONTENT_URI, mCols,
	                Downloads.WHERE_RUNNING, null, BaseColumns._ID);
	        if (cursor == null) {
	            return;
	        }
	        
	        try{
		        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
		            int max = cursor.getInt(totalBytesColumn);
		            int progress = cursor.getInt(currentBytesColumn);
		            int appId = cursor.getInt(appIdColumn);
		            String title = cursor.getString(titleColumn);
		            if (title == null || title.length() == 0) {
		                title = mContext.getResources().getString(
		                        R.string.download_unknown_title);
		            }
		            
		            if(progress < max && max > 0 && (progress * 100) > max) {
		            	Object[] data = new Object[2];
		            	data[0] = new Integer(appId);
		            	data[1] = new Integer(progress * 100 / max);
		            	Message msg = Message.obtain(mHandler, PROGRESSBAR_UPDATEING, data);
						mHandler.sendMessage(msg);
		            }
		        }
	        }finally {
	        	cursor.close();
	        }
		}
	}
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		// TODO Auto-generated method stub
//		return OptionsMenu.onOptionsItemSelected(this, item);
//	}
}