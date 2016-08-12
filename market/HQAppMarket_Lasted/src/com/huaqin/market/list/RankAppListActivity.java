package com.huaqin.market.list;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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
import com.huaqin.market.download.Downloads;
import com.huaqin.market.list.AppListAdapter.ViewHolder;
import com.huaqin.market.model.Application2;
import com.huaqin.market.model.Image2;
import com.huaqin.market.ui.AppInfoActivity;
import com.huaqin.market.ui.AppInfoPreloadActivity;
import com.huaqin.market.ui.LoadingAnimation;
import com.huaqin.market.utils.CachedThumbnails;
import com.huaqin.market.utils.Constant;
import com.huaqin.market.utils.OptionsMenu;
import com.huaqin.market.webservice.IMarketService;
import com.huaqin.market.webservice.MarketService;
import com.huaqin.market.webservice.Request;


public class RankAppListActivity extends Activity
	implements AdapterView.OnItemClickListener {
	
	private static final int ACTION_APP_LIST = 1;
	private static final int ACTION_APP_ICON = 2;
	private static final int ACTION_NETWORK_ERROR = 3;
	private static final int PROGRESSBAR_UPDATEING = 13;
	private static final int DIALOG_NETWORK_ERROR = 100;
	
	private Hashtable<Integer, Boolean> mIconStatusMap;
	public static Hashtable<Integer, Boolean> mWeekIconAnimStatusMap;
	public static Hashtable<Integer, Boolean> mMonIconAnimStatusMap;
	public static Hashtable<Integer, Boolean> mTotalIconAnimStatusMap;
	private Request mCurrentRequest;
	
	private final BroadcastReceiver mApplicationsReceiver;
	private AbsListView.OnScrollListener mScrollListener;
	private ProgressBar mProgressBar;
	public static AppListAdapter mAppListAdapterWeek;
	public static AppListAdapter mAppListAdapterMonth;
	public static AppListAdapter mAppListAdapterTotal;
	private IMarketService mMarketService;
	private Handler mHandler;
	
	private Context mContext;
	
	private int nRankingType;
	
	public static int nRankingTypeFlag = 1;
	
	private int nStartIndex;
	private boolean bBusy;
	private boolean bReachEnd;
	private boolean bInflatingAppList;
	
	private View mLoadingIndicator;
	private ImageView mLoadingAnimation;
	private AnimationDrawable loadingAnimation;
	private ListView mListView;
	private static View mFooterView;
	private static View mFooterViewSpace;
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
	private View mStatus;
	private Animation icon_push_in; 
	// Constructor
	public RankAppListActivity() {
		nStartIndex = 0;
		bBusy = false;
		bReachEnd = false;
		bInflatingAppList = false;
		mIconStatusMap = new Hashtable<Integer, Boolean>();
		mWeekIconAnimStatusMap = new Hashtable<Integer, Boolean>();
		mMonIconAnimStatusMap = new Hashtable<Integer, Boolean>();
		mTotalIconAnimStatusMap = new Hashtable<Integer, Boolean>();
		mApplicationsReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				if (mAppListAdapterWeek != null) {
					mAppListAdapterWeek.notifyDataSetChanged();
				}
				if (mAppListAdapterMonth != null) {
					mAppListAdapterMonth.notifyDataSetChanged();
				}
				if (mAppListAdapterTotal != null) {
					mAppListAdapterTotal.notifyDataSetChanged();
				}
			}
		};	
		initListener();
	}
	
	private void initHandlers() {
		// TODO Auto-generated method stub
		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch (msg.what) {
				case ACTION_APP_LIST:
					@SuppressWarnings("unchecked")
					ArrayList<Application> appList = 
							(ArrayList<Application>) msg.obj;
					int appListSize = 0;
					int appListIndex = 0;
					Log.v("asd", "appList = "+appList.size());
					if (appList != null) {
						appListSize = appList.size();
						nStartIndex += appListSize;
					}
					Log.v("asd", "appListIndex = "+appListIndex);
					if(nRankingType == 1){
						if (mAppListAdapterWeek == null) {
							mAppListAdapterWeek = new AppListAdapter(
									mContext,
									appList,
									Constant.LIST_ITEMTYPE_NORMAL,
									"0");
							
							mListView.setAdapter(mAppListAdapterWeek);
						} else {
							for (; appListIndex < appListSize; appListIndex++) {
								mAppListAdapterWeek.add(appList.get(appListIndex));
							}
							if (appListIndex >= appListSize && appListIndex != 0) {
								mAppListAdapterWeek.notifyDataSetChanged();
							}
						}
					}
					if(nRankingType == 2){
						if (mAppListAdapterMonth == null) {
							mAppListAdapterMonth = new AppListAdapter(
									mContext,
									appList,
									Constant.LIST_ITEMTYPE_NORMAL,
									"0");
							
							mListView.setAdapter(mAppListAdapterMonth);
						} else {
							for (; appListIndex < appListSize; appListIndex++) {
								mAppListAdapterMonth.add(appList.get(appListIndex));
							}
							if (appListIndex >= appListSize && appListIndex != 0) {
								mAppListAdapterMonth.notifyDataSetChanged();
							}
						}
					}
					if(nRankingType == 3){
						if (mAppListAdapterTotal == null) {
							mAppListAdapterTotal = new AppListAdapter(
									mContext,
									appList,
									Constant.LIST_ITEMTYPE_NORMAL,
									"0");
							
							mListView.setAdapter(mAppListAdapterTotal);
						} else {
							for (; appListIndex < appListSize; appListIndex++) {
								mAppListAdapterTotal.add(appList.get(appListIndex));
							}
							if (appListIndex >= appListSize && appListIndex != 0) {
								mAppListAdapterTotal.notifyDataSetChanged();
							}
						}
					}

					if (appListSize == 0 || appListSize < Constant.LIST_COUNT_PER_TIME) {
						bReachEnd = true;
						mListView.removeFooterView(mFooterView);
//						mListView.addFooterView(mFooterViewSpace);
					}
					String where = Downloads.WHERE_RUNNING;
					mCursor = mContext.getContentResolver()
							.query(Downloads.CONTENT_URI,
									mCols, where, null, null);
				
					if (mAppListAdapterWeek != null) {
						mAppListAdapterWeek.setCursor(mCursor);
					}
					if (mAppListAdapterMonth != null) {
						mAppListAdapterMonth.setCursor(mCursor);
					}
					if (mAppListAdapterTotal != null) {
						mAppListAdapterTotal.setCursor(mCursor);
					}
					if(mCursor!=null&&!mCursor.isClosed()){
						mCursor.close();
					}
					mLoadingIndicator.setVisibility(View.GONE);
					mListView.setVisibility(View.VISIBLE);
					bInflatingAppList = false;
					break;
					
				case ACTION_APP_ICON:
					Image2 icInfo = (Image2) msg.obj;
					
					if (icInfo.mAppIcon != null) {
						CachedThumbnails.cacheThumbnail(mContext,
								icInfo._id, icInfo.mAppIcon);
						if (mAppListAdapterWeek != null) {
							mAppListAdapterWeek.notifyDataSetChanged();
						}
						if (mAppListAdapterMonth != null) {
							mAppListAdapterMonth.notifyDataSetChanged();
						}
						if (mAppListAdapterTotal != null) {
							mAppListAdapterTotal.notifyDataSetChanged();
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
					Log.v("asd", "ACTION_NETWORK_ERROR  ");
					mLoadingIndicator.setVisibility(View.GONE);
					mListView.setVisibility(View.VISIBLE);
					bInflatingAppList = false;
					
//					showDialog(DIALOG_NETWORK_ERROR);
					Toast.makeText(mContext, mContext.getString(R.string.error_network_low_speed), Toast.LENGTH_LONG).show();
					break;
					
				default:
					break;
				}
				return;
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
		
		mListView = (ListView) findViewById(android.R.id.list);
		mListView.setScrollbarFadingEnabled(true);
		
		mFooterView = LayoutInflater.from(mContext).inflate(R.layout.app_list_footer, null);
		mFooterViewSpace = LayoutInflater.from(mContext).inflate(R.layout.app_list_foot_space, null);
		ProgressBar loadingSmall =
			(ProgressBar) mFooterView.findViewById(R.id.small_loading);
		LoadingAnimation aniSmall = new LoadingAnimation(
				this,
				LoadingAnimation.SIZE_SMALL,
				0, 0, LoadingAnimation.DEFAULT_DURATION);
		loadingSmall.setIndeterminateDrawable(aniSmall);
		Button btnRefresh = (Button) mFooterView.findViewById(R.id.btn_retry);
		btnRefresh.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mCurrentRequest != null) {
					mMarketService.getRankingAppList(mCurrentRequest);
				}
			}
		});
		mListView.addFooterView(mFooterView);
		
		View emptyView = findViewById(R.id.low_speed);
		TextView tvRefresh = (TextView) emptyView.findViewById(R.id.lowspeed_refresh);
		tvRefresh.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
                Log.v("asd", "tvRefresh.setOnClickListener mListView = "+mListView);
				if (mCurrentRequest != null && mListView != null) {
					mLoadingIndicator.setVisibility(View.VISIBLE);
					mListView.setVisibility(View.GONE);
					bReachEnd = false;
					nStartIndex = 0;
					if (mListView.getFooterViewsCount() == 0 && mFooterView != null) {
						mListView.addFooterView(mFooterView);
					}
					mMarketService.getRankingAppList(mCurrentRequest);
				}
			}
		});
		mListView.setEmptyView(emptyView);
		mListView.setOnItemClickListener(this);
		mListView.setOnScrollListener(mScrollListener);
		
		//Added-s by JimmyJin
		if(mFooterViewSpace != null){
			mListView.addFooterView(mFooterViewSpace);
		}else
			mListView.removeFooterView(mFooterViewSpace);
		//Added-e by JimmyJin
		
		inflateAppList();
	}
	
	private void registerIntentReceivers() {
		// TODO Auto-generated method stub
		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		intentFilter.addDataScheme("package");
		registerReceiver(mApplicationsReceiver, intentFilter);
	}
	
	private void inflateAppList() {
		
		if (bReachEnd || bInflatingAppList) {
			return;
		}
		
		bInflatingAppList = true;
		
		Request request = new Request(0, Constant.TYPE_APP_RANKING_LIST);
		Object[] params = new Object[2];
		
		params[0] = Integer.valueOf(nRankingType);
		params[1] = Integer.valueOf(nStartIndex);
		request.setData(params);
		request.addObserver(new Observer() {
			@Override
			public void update(Observable observable, Object data) {
				// TODO Auto-generated method stub
				Log.v("asd", "data = "+data);
				if (data != null) {
					Message msg = Message.obtain(mHandler, ACTION_APP_LIST, data);
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
		mMarketService.getRankingAppList(request);
	}

	private void initListener() {
		// TODO Auto-generated method stub
		mScrollListener = new AbsListView.OnScrollListener() {
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				return;
			}
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				switch (scrollState) {
				case SCROLL_STATE_IDLE:
					bBusy = false;
					int start = view.getFirstVisiblePosition();
					int counts = view.getChildCount();
					int position = 0;
					Log.v("asd", "counts = "+counts);
					for (int i = 0; i < counts; i++) {
						View localView = view.getChildAt(i);
						position = start + i;
						
						ViewHolder viewHolder = (ViewHolder) localView.getTag();
					    if (viewHolder != null) {
					    	Log.v("asd", "nRankingType = "+nRankingType);
					    	if(nRankingType == 1){
						    	if(mAppListAdapterWeek != null){
//							    	mAppListAdapterWeek.initBtnStatus(viewHolder,(Application2)viewHolder.mButton.getTag());
						    		int id = (int) mAppListAdapterWeek.getItemId(position);
							          Drawable drawable = getThumbnail(position, id);
							          drawable.setCallback(null);
							          viewHolder.mThumbnail.setImageDrawable(drawable);
						    	}
					    	}
					    	if(nRankingType == 2){
						    	if(mAppListAdapterMonth != null){
//						    		mAppListAdapterMonth.initBtnStatus(viewHolder,(Application2)viewHolder.mButton.getTag());
						    		int id = (int) mAppListAdapterMonth.getItemId(position);
							          Drawable drawable = getThumbnail(position, id);
							          drawable.setCallback(null);
							          viewHolder.mThumbnail.setImageDrawable(drawable);
						    	}
					    	}
					    	if(nRankingType == 3){
						    	if(mAppListAdapterTotal != null){
//						    		mAppListAdapterTotal.initBtnStatus(viewHolder,(Application2)viewHolder.mButton.getTag());
						    		int id = (int) mAppListAdapterTotal.getItemId(position);
							          Drawable drawable = getThumbnail(position, id);
							          drawable.setCallback(null);
							          viewHolder.mThumbnail.setImageDrawable(drawable);
						    	}
					    	}

					    }
					}
					
					// as list scrolled to end, send request to get above data
					if(nRankingType == 1){
						if ((start + counts) >= (mAppListAdapterWeek.getCount() - 2)) {
							inflateAppList();
						}
					}
					if(nRankingType == 2){
						if ((start + counts) >= (mAppListAdapterMonth.getCount() - 2)) {
							inflateAppList();
						}
					}
					if(nRankingType == 3){
						if ((start + counts) >= (mAppListAdapterTotal.getCount() - 2)) {
							inflateAppList();
						}
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
		};
	}
	
	private void addThumbnailRequest(int position, int id) {
		
		Request request = new Request(0L, Constant.TYPE_APP_ICON);
		Object[] params = new Object[2];
		String imgUrl;
		if(nRankingType == 1){
			imgUrl = mAppListAdapterWeek.getItem(position).getIconUrl();
		}
		else if(nRankingType == 2){
			imgUrl = mAppListAdapterMonth.getItem(position).getIconUrl();
		}else{
			imgUrl = mAppListAdapterTotal.getItem(position).getIconUrl();
		}
		
		
		params[0] = Integer.valueOf(id);
		params[1] = imgUrl;
		request.setData(params);
		request.addObserver(new Observer() {

			@Override
			public void update(Observable observable, Object data) {
				// TODO Auto-generated method stub
				if (data != null) {
					Message msg = Message.obtain(mHandler, ACTION_APP_ICON, data);
					nRankingTypeFlag = nRankingType;
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
		
		mContext = this;
		mMarketService = MarketService.getServiceInstance(this);
		
		Intent intent = getIntent();
		if (savedInstanceState != null) {
			nRankingType = savedInstanceState.getInt("ranking_type", 1);	
			
		} else {
			nRankingType = intent.getIntExtra("ranking_type", 1);
		}
		setContentView(R.layout.app_list);
		initHandlers();
		initListView();
		mObserver = new DownloadingContentObserver();
        getContentResolver().registerContentObserver(Downloads.CONTENT_URI,
                true, mObserver);
		registerIntentReceivers();
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
							switch (mCurrentRequest.getType()) {
							case Constant.TYPE_APP_RANKING_LIST:
								mMarketService.getRankingAppList(mCurrentRequest);
								break;
							case Constant.TYPE_APP_ICON:
								mMarketService.getAppIcon(mCurrentRequest);
								break;
							default:
								break;
							}
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
    		startActivity(new Intent(mContext, OptionsMenu.class));
    		overridePendingTransition(R.anim.fade, R.anim.hold);
    	}
    	return super.onKeyUp(keyCode, event);
    }
	@Override
	public void onItemClick(AdapterView<?> parent, View view,
			int position, long id) {
			if(nRankingType == 1){
				if(position < mAppListAdapterWeek.getCount()) {
//					Intent intent = new Intent(this, AppInfoPreloadActivity.class);
					Intent intent = new Intent(this, AppInfoActivity.class);
					intent.putExtra("appInfo",new Application2(mAppListAdapterWeek.getItem(position)));
					intent.putExtra("appId", mAppListAdapterWeek.getItem(position).getAppId());
					/*************Added-s by JimmyJin for Pudding Project**************/
					intent.putExtra("type",0);
					/*************Added-s by JimmyJin for Pudding Project**************/
					intent.putExtra("download", mAppListAdapterWeek.getItem(position).getTotalDownloads());
					startActivity(intent);
				}
			}
			if(nRankingType == 2){
				if(position < mAppListAdapterMonth.getCount()) {
//					Intent intent = new Intent(this, AppInfoPreloadActivity.class);
					Intent intent = new Intent(this, AppInfoActivity.class);
					intent.putExtra("appInfo",new Application2(mAppListAdapterMonth.getItem(position)));
					intent.putExtra("appId", mAppListAdapterMonth.getItem(position).getAppId());
					/*************Added-s by JimmyJin for Pudding Project**************/
					intent.putExtra("type",0);
					/*************Added-s by JimmyJin for Pudding Project**************/
					intent.putExtra("download", mAppListAdapterMonth.getItem(position).getTotalDownloads());
					startActivity(intent);
				}
			}
			if(nRankingType == 3){
				if(position < mAppListAdapterTotal.getCount()) {
//					Intent intent = new Intent(this, AppInfoPreloadActivity.class);
					Intent intent = new Intent(this, AppInfoActivity.class);
					intent.putExtra("appInfo",new Application2(mAppListAdapterTotal.getItem(position)));
					intent.putExtra("appId", mAppListAdapterTotal.getItem(position).getAppId());
					/*************Added-s by JimmyJin for Pudding Project**************/
					intent.putExtra("type",0);
					/*************Added-s by JimmyJin for Pudding Project**************/
					intent.putExtra("download", mAppListAdapterTotal.getItem(position).getTotalDownloads());
					startActivity(intent);
				}
			}

	}
	
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		// TODO Auto-generated method stub
//		return OptionsMenu.onOptionsItemSelected(this, item);
//	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if (mIconStatusMap != null) {
			mIconStatusMap.clear();
		}
		if (mAppListAdapterWeek != null) {
			mAppListAdapterWeek.clear();
		}
		if (mAppListAdapterMonth != null) {
			mAppListAdapterMonth.clear();
		}
		if (mAppListAdapterTotal != null) {
			mAppListAdapterTotal.clear();
		}
//		if (mListView != null) {
//			mListView.setAdapter(null);
//		}
		super.onDestroy();
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
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		String where = Downloads.WHERE_RUNNING;
		mCursor = mContext.getContentResolver()
				.query(Downloads.CONTENT_URI,
						mCols, where, null, null);
		if(mAppListAdapterWeek != null){
			mAppListAdapterWeek.notifyDataSetChanged();
			mAppListAdapterWeek.setCursor(mCursor);
		}
		if(mAppListAdapterMonth != null){
			mAppListAdapterWeek.notifyDataSetChanged();
			mAppListAdapterMonth.setCursor(mCursor);
		}
		if(mAppListAdapterTotal != null){
			mAppListAdapterWeek.notifyDataSetChanged();
			mAppListAdapterTotal.setCursor(mCursor);
		}
		if(mCursor!=null&&!mCursor.isClosed()){
			mCursor.close();
		}
//		if (mAppListAdapter[nRankingType-1] != null) {
//			mAppListAdapter[nRankingType-1].notifyDataSetChanged();
//		}
	}

}