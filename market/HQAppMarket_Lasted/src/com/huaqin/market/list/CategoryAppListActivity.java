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
import com.huaqin.market.SlideViewPager;
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

public class CategoryAppListActivity extends ListActivity
	implements AdapterView.OnItemClickListener {
	
	private static final int ACTION_APP_LIST = 0;
	private static final int ACTION_APP_ICON = 1;
	private static final int ACTION_NETWORK_ERROR = 2;
	private static final int PROGRESSBAR_UPDATEING = 13;
	private static final int DIALOG_NETWORK_ERROR = 100;
	
	private View mLoadingIndicator;
	private ImageView mLoadingAnimation;
	private AnimationDrawable loadingAnimation;
	private ListView mListView;
	private AppListAdapter mAppListAdapter;
	private View mFooterView;
//	private View mFooterViewSpace;
	private ProgressBar mProgressBar;
	private Hashtable<Integer, Boolean> mIconStatusMap;
	public static Hashtable<Integer, Boolean> mIconAnimStatusMap;
	private int nCategoryId;
	private int nSortType;
	private int nStartIndex;
	/*************Added-s by JimmyJin for Pudding Project**************/	
	private int type;
	/*************Added-e by JimmyJin for Pudding Project**************/
	private boolean bInflatingAppList;
	private boolean bReachEnd;
	private boolean bBusy;
	
	private IMarketService mMarketService;
	private Handler mHandler;
	private Request mCurrentRequest;
	private Context mContext;
	private final BroadcastReceiver mApplicationsReceiver;
	private final BroadcastReceiver mApplicationsFreeReceiver;
	private AbsListView.OnScrollListener mScrollListener;
	private Drawable drawable = null;
	private TextView mStatus;
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
	private Animation icon_push_in;
	public CategoryAppListActivity() {
		
		nStartIndex = 0;
		bReachEnd = false;
		bInflatingAppList = false;
		mIconStatusMap = new Hashtable<Integer, Boolean>();
		mIconAnimStatusMap = new Hashtable<Integer, Boolean>();
		mContext = this;
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
						
						if (!mIconStatusMap.containsKey(Integer.valueOf(position))) {
							ViewHolder viewHolder = 
								(ViewHolder) view.getChildAt(i).getTag();
							if (viewHolder != null) {
//								mAppListAdapter.initBtnStatus(viewHolder,(Application2)viewHolder.mButton.getTag());
								int id = (int) mAppListAdapter.getItemId(position);
								Drawable drawable = getThumbnail(position, id);
								viewHolder.mThumbnail.setImageDrawable(drawable);
							}
						}
					}
					
					// as list scrolled to end, send request to get above data
					if ((start + counts) >= (mAppListAdapter.getCount() - 2)) {
						inflateCategoryAppList();
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
				Log.v("CategoryAppListActivity", "onReceive ="+action);
				if(action.equals(SlideViewPager.CATE_LIST_REFRESH)){	
					onResume();
				}
			}
		};
		
		mApplicationsFreeReceiver = new BroadcastReceiver(){

			@Override
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();
				Log.v("CategoryAppListActivity", "onReceive ="+action);
				if(action.equals(SlideViewPager.LIST_REFRESH_FREE)){	
					onDestroy();
				}
			}
		};
	}
	
	private void initHandler() {
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
					
					if (appList != null) {
						appListSize = appList.size();
						nStartIndex += appListSize;
					}
					
					if (mAppListAdapter == null) {
						int itemType = Constant.LIST_ITEMTYPE_NORMAL;
						if (nSortType == Constant.LIST_SORT_BY_DOWNLOAD) {
							itemType = Constant.LIST_ITEMTYPE_DOWNLOAD;
						} else if (nSortType == Constant.LIST_SORT_BY_DATE) {
							itemType = Constant.LIST_ITEMTYPE_DATE;
						}
						mAppListAdapter = new AppListAdapter(
								CategoryAppListActivity.this,
								appList,
								itemType,
								String.valueOf(type));
						mListView.setAdapter(mAppListAdapter);
					} else {
						for (; appListIndex < appListSize; appListIndex++) {
							mAppListAdapter.add(appList.get(appListIndex));
						}
						if (appListIndex >= appListSize && appListIndex != 0) {
							mAppListAdapter.notifyDataSetChanged();
						}
					}
					
					if (appListSize == 0 || appListSize < Constant.LIST_COUNT_PER_TIME) {
						bReachEnd = true;
						if (mListView.getFooterViewsCount() > 0) {
							mListView.removeFooterView(mFooterView);
//							mListView.addFooterView(mFooterViewSpace);
						}
					}
					String where = Downloads.WHERE_RUNNING;
					
					mCursor = mContext.getContentResolver()
								.query(Downloads.CONTENT_URI,
										mCols, where, null, null);
					
					mAppListAdapter.setCursor(mCursor);
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
						if (mAppListAdapter != null) {
//							mAppListAdapter.iconShow();
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
//					showDialog(DIALOG_NETWORK_ERROR);
					Toast.makeText(mContext, mContext.getString(R.string.error_network_low_speed), Toast.LENGTH_LONG).show();
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
		
		mListView = (ListView) findViewById(android.R.id.list);
		mListView.setScrollbarFadingEnabled(true);
		
		mFooterView = LayoutInflater.from(this).inflate(R.layout.app_list_footer, null);
//		mFooterViewSpace = LayoutInflater.from(mContext).inflate(R.layout.app_list_foot_space, null);
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
//					mMarketService.getCategoryAppList(mCurrentRequest);
					/*************Added-s by JimmyJin for Pudding Project**************/
					mMarketService.getTypeList(mCurrentRequest);
					/*************Added-e by JimmyJin for Pudding Project**************/
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
				if (mCurrentRequest != null && mListView != null) {
					mLoadingIndicator.setVisibility(View.VISIBLE);
					mListView.setVisibility(View.GONE);
					bReachEnd = false;
					nStartIndex = 0;
					if (mListView.getFooterViewsCount() == 0 && mFooterView != null) {
						mListView.addFooterView(mFooterView);
					}
//					mMarketService.getCategoryAppList(mCurrentRequest);
					/*************Added-s by JimmyJin for Pudding Project**************/
					mMarketService.getTypeList(mCurrentRequest);
					/*************Added-e by JimmyJin for Pudding Project**************/
				}
			}
		});
		mListView.setEmptyView(emptyView);
		
		mListView.setOnItemClickListener(this);
		mListView.setOnScrollListener(mScrollListener);
		
		inflateCategoryAppList();
	}
	
	private void inflateCategoryAppList() {
		// TODO Auto-generated method stub
		if (bReachEnd || bInflatingAppList) {
			return;
		}
		bInflatingAppList = true;
		
		Request request = new Request(0, Constant.TYPE_CATE_APP_LIST);
		Object[] params = new Object[4];
		
		params[0] = Integer.valueOf(nCategoryId);
		params[1] = Integer.valueOf(nSortType);
		params[2] = Integer.valueOf(nStartIndex);
		/*************Added-s by JimmyJin for Pudding Project**************/
		params[3] = Integer.valueOf(type);
		/*************Added-s by JimmyJin for Pudding Project**************/
		request.setData(params);
		request.addObserver(new Observer() {

			@Override
			public void update(Observable observable, Object data) {
				// TODO Auto-generated method stub
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
//		mMarketService.getCategoryAppList(request);
		/*************Added-s by JimmyJin for Pudding Project**************/
		mMarketService.getTypeList(request);
		/*************Added-e by JimmyJin for Pudding Project**************/
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
					Message msg = Message.obtain(mHandler, ACTION_APP_ICON, data);
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
		Log.v("getAppIcon", "request ="+request);
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
		Log.v("cate", "id ="+id);
		try {
			drawable = CachedThumbnails.getThumbnail(this, id);
		       // load resources
		  }  catch (OutOfMemoryError E) { 
		       // do something to use less memory and try again
			  Log.v("CategoryAppListActivity","JimmyJin OutOfMemoryError E="+E);
		  }catch (Exception e) {
			  Log.v("CategoryAppListActivity","JimmyJin Exception e="+e); 
		  }
		


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
		nCategoryId = getIntent().getIntExtra("cateId", 0);
		nSortType = getIntent().getIntExtra("sortType", 1);
		
		/*************Added-s by JimmyJin for Pudding Project**************/
		type = getIntent().getIntExtra("type", 1);
		/*************Added-e by JimmyJin for Pudding Project**************/
		Log.v("CategoryAppListActivity","JimmyJin type666="+type);
		
		setContentView(R.layout.app_list);
		
		initHandler();
		initListView();
		mObserver = new DownloadingContentObserver();
        getContentResolver().registerContentObserver(Downloads.CONTENT_URI,
                true, mObserver);
		registerIntentReceivers();
	}
	private void registerIntentReceivers() {
		// TODO Auto-generated method stub
		IntentFilter intentFilter = new IntentFilter(SlideViewPager.CATE_LIST_REFRESH);
//		intentFilter.addDataScheme("package");
		registerReceiver(mApplicationsReceiver, intentFilter);
		
		IntentFilter intentFilterFress = new IntentFilter(SlideViewPager.LIST_REFRESH_FREE);
		registerReceiver(mApplicationsFreeReceiver, intentFilterFress);
		
	}
	
	private void unregisterReceivers(){
		unregisterReceiver(mApplicationsReceiver);
		unregisterReceiver(mApplicationsFreeReceiver);
	}
	@Override
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		if (id == DIALOG_NETWORK_ERROR) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.dlg_network_error_title)
				.setMessage(R.string.dlg_network_error_msg)
				.setPositiveButton(R.string.btn_retry, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						switch (mCurrentRequest.getType()) {
						case Constant.TYPE_CATE_APP_LIST:
//							mMarketService.getCategoryAppList(mCurrentRequest);
							/*************Added-s by JimmyJin for Pudding Project**************/
							mMarketService.getTypeList(mCurrentRequest);
							/*************Added-e by JimmyJin for Pudding Project**************/
							break;
						case Constant.TYPE_APP_ICON:
							Log.v("getAppIcon", "mCurrentRequest ="+mCurrentRequest);
							mMarketService.getAppIcon(mCurrentRequest);
							break;
						default:
							break;
						}
					}
				})
				.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						finish();
					}
				});
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
//    @Override
//    public boolean onKeyUp(int keyCode, KeyEvent event) {
//    	if (keyCode == KeyEvent.KEYCODE_MENU) {
//    		startActivity(new Intent(CategoryAppListActivity.this, OptionsMenu.class));
//    		overridePendingTransition(R.anim.fade, R.anim.hold);
//    	}
//    	if (keyCode == KeyEvent.KEYCODE_BACK) {
//    		finish();
//    	}
//    	return super.onKeyUp(keyCode, event);
//    }
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.v("CategoryAppListActivity", "onDestroy()");
		finish();
//		if (drawable != null && drawable instanceof BitmapDrawable) {
//			((BitmapDrawable)drawable).getBitmap().recycle();
//		}
		mLoadingIndicator.setVisibility(View.VISIBLE);
		System.gc();
		if(drawable != null){
			drawable.setCallback(null); 
			drawable = null;
		}
		if (mIconStatusMap != null) {
			mIconStatusMap.clear();
		}
		if (mAppListAdapter != null) {
			mAppListAdapter.clear();
		}
		if(mCursor != null){
			mCursor.close();
		}
		if (mListView != null) {
			mListView.setAdapter(null);
		}
		unregisterReceivers();
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if(position < mAppListAdapter.getCount()) {
			Intent intent = new Intent(this, AppInfoActivity.class);
			intent.putExtra("appInfo",new Application2(mAppListAdapter.getItem(position)));
			intent.putExtra("appId", mAppListAdapter.getItem(position).getAppId());
			/*************Added-s by JimmyJin for Pudding Project**************/
			intent.putExtra("type",type);
			/*************Added-s by JimmyJin for Pudding Project**************/
			intent.putExtra("download", mAppListAdapter.getItem(position).getTotalDownloads());
			startActivity(intent);
		}
	}
	
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		// TODO Auto-generated method stub
//		return OptionsMenu.onOptionsItemSelected(this, item);
//	}
}