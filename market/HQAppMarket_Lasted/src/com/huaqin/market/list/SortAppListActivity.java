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

public class SortAppListActivity extends ListActivity
	implements AdapterView.OnItemClickListener {
	
	private static final int ACTION_APP_LIST = 0;
	private static final int ACTION_APP_ICON = 1;
	private static final int ACTION_NETWORK_ERROR = 2;
	
	private static final int DIALOG_NETWORK_ERROR = 100;
	private static final int PROGRESSBAR_UPDATEING = 13;
	
	private Context mContext;
	private Hashtable<Integer, Boolean> mIconStatusMap;
	private Request mCurrentRequest;
	private IMarketService mMarketService;
	private Handler mHandler;
	private static View mFooterViewSpace;
	private ProgressBar mProgressBar;
	private View mLoadingIndicator;
	private ImageView mLoadingAnimation;
	private ListView mListView;
	private AnimationDrawable loadingAnimation;
	private AnimationDrawable smallLoadingAnimation;
	public static AppListAdapter mAppListAdapter;
	private View mFooterView;
	private int nStartIndex;
	public static Hashtable<Integer, Boolean> mIconAnimStatusMap;
	private Animation icon_push_in;
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
	
	private boolean bInflatingNewAppList;
	private boolean bReachEnd;
	private boolean bBusy;
	private boolean bFlag;
	private TextView mStatus;
	private final BroadcastReceiver mApplicationsReceiver;
//	private final BroadcastReceiver mApplicationsShowReceiver;
	private AbsListView.OnScrollListener mScrollListener;
	
	private Drawable drawable1 = null;
	public SortAppListActivity() {
		
		nStartIndex = 0;
		bReachEnd = false;
		bInflatingNewAppList = false;
		bFlag = true;
		mIconStatusMap = new Hashtable<Integer, Boolean>();
		mIconAnimStatusMap = new Hashtable<Integer, Boolean>();
		mApplicationsReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
			}
		};
		
		initListener();
	}
	
	private void initListener() {
		// TODO Auto-generated method stub
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
								
								int id = (int) mAppListAdapter.getItemId(position);
//								if(drawable1 != null)
//									drawable1 = null;
								Drawable drawable = getThumbnail(position, id);
//								boolean bThumbExists = mIconStatusMap.containsKey(Integer.valueOf(position));
//								if(drawable != null){
									viewHolder.mThumbnail.setImageDrawable(drawable);
//						          if(mIconAnimStatusMap.get(Integer.valueOf(id)) == null){
//										mIconAnimStatusMap.put(Integer.valueOf(id), Boolean.valueOf(true));
//								        icon_push_in=AnimationUtils.loadAnimation(mContext, R.anim.item_leftrun); 
//								        viewHolder.mThumbnail.setAnimation(icon_push_in);
//									}
//								}
//								if(mIconAnimStatusMap.get(Integer.valueOf(id)) == null&&bThumbExists){
//						        	  mIconAnimStatusMap.put(Integer.valueOf(id), Boolean.valueOf(true));
//						        	  icon_push_in=AnimationUtils.loadAnimation(mContext, R.anim.item_leftrun); 
//							          viewHolder.mThumbnail.setAnimation(icon_push_in);  
//						        }
							}
//						}
					}
					
					// as list scrolled to end, send request to get above data
					if ((start + counts) >= (mAppListAdapter.getCount() - 2)) {
						inflateSortAppList();
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
				return;
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
						mAppListAdapter = new AppListAdapter(
								SortAppListActivity.this,
								appList,
								Constant.LIST_ITEMTYPE_NORMAL,
								"0");
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
					bInflatingNewAppList = false;
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
					bInflatingNewAppList = false;
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
					mMarketService.getNewAppList(mCurrentRequest);
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
					mMarketService.getNewAppList(mCurrentRequest);
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
		
		inflateSortAppList();
	}
	
	public void startAnimation() {   
	       if (smallLoadingAnimation.isRunning()) {   
	    	   smallLoadingAnimation.stop();   
	       } else {   
	    	   smallLoadingAnimation.stop();   
	    	   smallLoadingAnimation.start();   
	       }   
	    }   
	@Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_MENU) {
    		startActivity(new Intent(SortAppListActivity.this, OptionsMenu.class));
    		overridePendingTransition(R.anim.fade, R.anim.hold);
    	}
    	return super.onKeyUp(keyCode, event);
    }
	private void inflateSortAppList() {
		// TODO Auto-generated method stub
		if (bReachEnd || bInflatingNewAppList) {
			return;
		}
		bInflatingNewAppList = true;
		Request request = new Request(0, Constant.TYPE_SORT_APP_LIST);
		Object[] params = new Object[2];
		
		params[0] = Integer.valueOf(0);
		params[1] = Integer.valueOf(nStartIndex);
		request.setData(params);

		Log.v("SortAppListActivity", "inflateNewAppList");
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
		mMarketService.getSortAppList(request);
	}

	private void registerIntentReceivers() {
		// TODO Auto-generated method stub
		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		intentFilter.addDataScheme("package");
		registerReceiver(mApplicationsReceiver, intentFilter);
		
	}
	
	private void unregisterIntentReceivers() {
		// TODO Auto-generated method stub
		unregisterReceiver(mApplicationsReceiver);
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
				}
			}
		});
		mCurrentRequest = request;
		mMarketService.getAppIcon(mCurrentRequest);
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
		
		setContentView(R.layout.app_list);
		initHandler();
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
						switch (mCurrentRequest.getType()) {
						case Constant.TYPE_SORT_APP_LIST:
							mMarketService.getSortAppList(mCurrentRequest);
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
//    @Override
//    public boolean onKeyUp(int keyCode, KeyEvent event) {
//    	if (keyCode == KeyEvent.KEYCODE_MENU) {
//    		startActivity(new Intent(NewAppListActivity.this, OptionsMenu.class));
//    		overridePendingTransition(R.anim.fade, R.anim.hold);
//    	}
//    	return super.onKeyUp(keyCode, event);
//    }
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
	public void onItemClick(AdapterView<?> parent, View view,
			int position, long id) {
		if(position < mAppListAdapter.getCount()) {
//			Intent intent = new Intent(this, AppInfoPreloadActivity.class);
			Intent intent = new Intent(this, AppInfoActivity.class);
			intent.putExtra("appInfo",new Application2(mAppListAdapter.getItem(position)));
			intent.putExtra("appId", mAppListAdapter.getItem(position).getAppId());
			/*************Added-s by JimmyJin for Pudding Project**************/
			intent.putExtra("type",0);
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

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub		
		if (isFinishing() && mAppListAdapter != null) {
			for (int i = 0; i < mAppListAdapter.getCount(); i++) {
				Application app = mAppListAdapter.getItem(i);
				if (app != null) {
					//DownloadService downloadService = DownloadService.getInstance(this);
					//downloadService.removeHandler(asset._id);
				}
			}
		}
		
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		if (mAppListAdapter != null) {
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
}