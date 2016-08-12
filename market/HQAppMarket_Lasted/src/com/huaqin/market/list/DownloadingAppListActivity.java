package com.huaqin.market.list;

import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.huaqin.market.R;
import com.huaqin.market.download.DownloadManager;
import com.huaqin.market.download.Downloads;
import com.huaqin.market.list.DownloadingAppListActivity.DownloadingAppListAdapter.ViewHolder;
import com.huaqin.market.ui.AppInfoActivity;
import com.huaqin.market.utils.CachedThumbnails;
import com.huaqin.market.utils.GlobalUtil;
import com.huaqin.market.utils.OptionsMenu;

public class DownloadingAppListActivity extends Activity {
	
	private static final String TAG = "DownloadingAppListActivity";

	public static final String ACTION_APP_DELETED = "com.hauqin.intent.action.APP_DELETED";
//	public static final String ACTION_APP_DOWNLOAD_START = "com.hauqin.intent.action.APP_DOWNLOAD_START";
	
	private static final int CONTEXT_MENU_DELETE_FILE = 0;
	private static final int CONTEXT_MENU_CANCLE = 1;
	
	private static final int PROGRESSBAR_UPDATEING = 100;
	
	private static final Boolean BFALSE = new Boolean(false);
	private static final Boolean BTRUE = new Boolean(true);
	private Context mContext;
	
	private DownloadContentObserver mObserver;
	
	private View mLoadingIndicator;
	private ImageView mLoadingAnimation;
	private AnimationDrawable loadingAnimation;
	private ListView mListView;
	private ProgressBar mProgressBar;
	private TextView mSize;
	private TextView mBytes;
	private Hashtable<Integer, Long> mAppBytesMap;
	private TextView mInfo;
	private DownloadingAppListAdapter mAppListAdapter;
	private Cursor mCursor;
	private int nContextMenuPosition;

	private final BroadcastReceiver mApplicationsReceiver;
	private final BroadcastReceiver mApplicationOperationReciever;
	private final BroadcastReceiver mUpdateReceiver;
	
	private Handler mHandler;
	private ViewHolder viewHolder = null;
	
	private LayoutInflater mLayoutInflater;
	private DownloadThread mDownloadThread;
	
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

	public DownloadingAppListActivity() {

		nContextMenuPosition = 0;
		mAppBytesMap = new Hashtable<Integer, Long>();
		mApplicationsReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				refreshDownloadListView();}
		};
		
		mApplicationOperationReciever = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				refreshDownloadListView();
				Toast.makeText(mContext, R.string.toast_downlaod_deleted, Toast.LENGTH_SHORT)
					.show();
			}
		};
		
		mUpdateReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				// TODO Auto-generated method stub
				final String action = arg1.getAction();
				if (action.equals(Downloads.ACTION_DOWNLOAD_UPDATEUILIST)) {
					refreshDownloadListView();
					Log.w(TAG, "DownloadingAppListActivity update receiver...");
				} else if (action.equals(Downloads.ACTION_DOWNLOAD_RESULTPROMT)) {
					String strResultPrompt = arg1.getStringExtra(Downloads.RESULTPROMPT);
					Log.w(TAG, "DownloadingAppListActivity download result prompt...");
					if (strResultPrompt != null) {
						Toast.makeText(getApplicationContext(), strResultPrompt, Toast.LENGTH_LONG).show();
					}
				}
			}
		};
		
	}

	private static String getItemViewTag(int appId) {
		return "item" + appId;
	}
	
	private static String getProcessbarViewTag(int appId) {
		return "processbar" + appId;
	}
	
	private static String getControlViewTag(int appId) {
		return "control" + appId;
	}
	
	private static String getSizeViewTag(int appId) {
		return "size" + appId;
	}
	
	private static String getByteViewTag(int appId) {
		return "byte" + appId;
	}
	
	private void initHandler() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case PROGRESSBAR_UPDATEING:
					Object[] data = (Object[])msg.obj;
					Integer mAppId = (Integer)data[0];
//					Log.i(TAG, "APPID=" + mAppId);
					int progress = ((Integer)data[1]).intValue();
					int max = ((Integer)data[2]).intValue();
					String currentBytes;
					String maxBytes;
//					mProgressBar = (ProgressBar) mListView.findViewWithTag(mAppId);		
					mProgressBar = (ProgressBar) mListView.findViewWithTag(getProcessbarViewTag(mAppId));	
					mSize = (TextView) mListView.findViewWithTag(getSizeViewTag(mAppId));	
					mBytes = (TextView) mListView.findViewWithTag(getByteViewTag(mAppId));	
					mInfo = (TextView) mListView.findViewWithTag(mProgressBar);
					if(mInfo == null && mProgressBar != null) {
						Log.d(TAG, "mInfo is null");
					}else if(mInfo != null && mProgressBar == null){
						Log.d(TAG, "mProgressBar is null");
					}else if(mInfo == null && mProgressBar == null){
						Log.d(TAG, "mInfo and mProgressBar is null");
					}else if(mInfo != null && mProgressBar != null) {
						if(progress < 1024){
							currentBytes = progress+"b";
						}else if(progress>1024*1024){
							currentBytes = (float)(Math.round((float)progress/(1024*1024)*100))/100+"m";
						}else{
							currentBytes = (progress/1024)+"k";
						}
						if(max < 1024){
							maxBytes = max+"b";
						}else if(max>1024*1024){
							maxBytes = (float)(Math.round((float)max/(1024*1024)*100))/100+"m";
						}else{
							maxBytes = (max/1024)+"k";
						}
						mSize.setText(currentBytes+"/"+maxBytes);
						mProgressBar.setProgress((progress*100)/max);
						mInfo.setText((progress*100)/max + "%");
						mInfo.setGravity(Gravity.CENTER);
					}else{
						Log.d(TAG, "mInfo and mProgressBar is unknown");
					}
					break;
				default:
					break;
				}
			}
		};
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
		
		View emptyView = findViewById(R.id.list_empty);
		TextView tvEmpty = (TextView) emptyView.findViewById(R.id.list_empty_text);
		tvEmpty.setText(R.string.error_no_download_task);
		mListView.setEmptyView(emptyView);
		
		mAppListAdapter = new DownloadingAppListAdapter(mContext);
		mListView.setAdapter(mAppListAdapter);
		
		registerForContextMenu(mListView);
		
		refreshDownloadListView();
	}

	private void refreshDownloadListView() {
		// TODO Auto-generated method stub
		String where = Downloads.WHERE_RUNNING;
		
		mCursor = mContext.getContentResolver()
					.query(Downloads.CONTENT_URI,
							mCols, where, null, null);
		
		mAppListAdapter.setCursor(mCursor);
		
		mLoadingIndicator.setVisibility(View.GONE);
		mListView.setVisibility(View.VISIBLE);
	}

	private void registerIntentReceivers() {
		// TODO Auto-generated method stub
		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		intentFilter.addDataScheme("package");
		registerReceiver(mApplicationsReceiver, intentFilter);
		
		IntentFilter intentFilter2 = new IntentFilter(ACTION_APP_DELETED);
		intentFilter2.addCategory(Downloads.ACTION_DOWNLOAD_COMPLETED);
		mContext.registerReceiver(mApplicationOperationReciever, intentFilter2);
		
		IntentFilter updateIntentFilter = new IntentFilter(Downloads.ACTION_DOWNLOAD_UPDATEUILIST);
		updateIntentFilter.addAction(Downloads.ACTION_DOWNLOAD_RESULTPROMT);
		registerReceiver(mUpdateReceiver, updateIntentFilter);
	}

	private void unregisterIntentReceivers() {
		// TODO Auto-generated method stub
		unregisterReceiver(mApplicationsReceiver);
		unregisterReceiver(mApplicationOperationReciever);
		unregisterReceiver(mUpdateReceiver);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Log.v("asd", "DownloadingAppListActivity");
		mContext = this;
		
		setContentView(R.layout.app_list);
		
		initHandler();
		initListView();
		registerIntentReceivers();
		mObserver = new DownloadContentObserver();
        getContentResolver().registerContentObserver(Downloads.CONTENT_URI,
                true, mObserver);
	}
	

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
		nContextMenuPosition =
			((AdapterView.AdapterContextMenuInfo) menuInfo).position;
		
		menu.add(0, CONTEXT_MENU_DELETE_FILE, 0, R.string.ctxmenu_delete);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case CONTEXT_MENU_DELETE_FILE:
			mAppListAdapter.onDeleteAppClick(nContextMenuPosition);
			return true;
			
		case CONTEXT_MENU_CANCLE:
			return true;
			
		default:
			return super.onContextItemSelected(item);
		}
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
//    		startActivity(new Intent(this, OptionsMenu.class));
//    		overridePendingTransition(R.anim.fade, R.anim.hold);
//    	}
//    	return super.onKeyUp(keyCode, event);
//    }
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		if (mAppListAdapter != null) {
			mAppListAdapter = null;
		}
		if (mListView != null) {
			mListView.setAdapter(null);
		}
		unregisterIntentReceivers();
	}

//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		// TODO Auto-generated method stub
//		return OptionsMenu.onOptionsItemSelected(this, item);
//	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.v("asd", "refreshDownloadListView");
		refreshDownloadListView();
	}

	private class DownloadContentObserver extends ContentObserver {

		public DownloadContentObserver() {
			super(mHandler);
		}
		
		@Override
		public void onChange(final boolean selfChange) {
			mDownloadThread = new DownloadThread();
            mDownloadThread.start();
        }
	}
	
	private class DownloadThread extends Thread {
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
		            	Object[] data = new Object[3];
		            	data[0] = new Integer(appId);
		            	data[1] = new Integer(progress);
		            	data[2] = new Integer(max);
		            	Message msg = Message.obtain(mHandler, PROGRESSBAR_UPDATEING, data);
						mHandler.sendMessage(msg);
		            }
		        }
	        }finally {
	        	cursor.close();
	        }
		}
	}
	
	public class DownloadingAppListAdapter extends BaseAdapter {
		private Context mContext;
		
		private Cursor mCursor;
		
		private View.OnClickListener mOnClickListener;
		private HashMap<Integer, Boolean> bDownloadingStatus;
		
		private View.OnLongClickListener mOnLongClickListener;
		
		public DownloadingAppListAdapter(Context context) {
			mContext = context;
			mLayoutInflater = 
				(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			bDownloadingStatus = new HashMap<Integer, Boolean>();
			mOnLongClickListener = new View.OnLongClickListener() {
				
				@Override
				public boolean onLongClick(View v) {
					Log.i(TAG, "onlongclick()");
					switch (v.getId()) {					
					case R.id.app_listitem_group:
						return onListItemLongClick(v);
					}
					return false;
				}
			};
			
			mOnClickListener = new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					switch (v.getId()) {
					case R.id.app_listitem_control:
						onOperateClick(v);
						break;
					default:
						break;
					}
				}
			};
			
		}
		private void onOperateClick(View v)
		{
			int appId = (Integer) v.getTag();
			ImageButton imageButton = (ImageButton) v;
					Integer appIdObj = new Integer(appId);
					boolean bDownloading = bDownloadingStatus.get(appId).booleanValue();
					Log.i(TAG, "onclick()");
					Log.v("asd", "bDownloading="+bDownloading);
					if(bDownloading) {
						bDownloadingStatus.remove(v.getTag());
						imageButton.setImageResource(R.drawable.btn_manage_downloading_start); 
						Log.i(TAG, "true");
						DownloadManager.pauseDownload(mContext, new long[]{appId});
						bDownloadingStatus.put(appIdObj, BFALSE);
						Intent intent = new Intent(AppInfoActivity.ACTION_DETAIL_DOWNLOAD);
						intent.setAction(AppInfoActivity.ACTION_DETAIL_DOWNLOAD_PAUSE);
						Log.v("asd", "pauseDownload appId"+appId);
		            	intent.putExtra("detailappId", appId);
						sendBroadcast(intent);
						
					} else {
						bDownloadingStatus.remove(v.getTag());
						imageButton.setImageResource(R.drawable.btn_manage_downloading_pause);
						Log.i(TAG, "false");
						DownloadManager.resumeDownload(mContext, new long[]{appId});
						bDownloadingStatus.put(appIdObj, BTRUE);
						Intent intent = new Intent(AppInfoActivity.ACTION_DETAIL_DOWNLOAD);
						intent.setAction(AppInfoActivity.ACTION_DETAIL_DOWNLOAD_REZUME);
						Log.v("asd", "resumeDownload appId"+appId);
		            	intent.putExtra("detailappId", appId);
						sendBroadcast(intent);
					}
		}
		private boolean onListItemLongClick(View v) {
			// TODO Auto-generated method stub		
			return false;
		}
		
		public void onDeleteAppClick(int position) {
			// TODO Auto-generated method stub
			mCursor.moveToPosition(position);
			DownloadManager.deleteCompletedTask(mContext, mCursor.getLong(idColumn));
			
			try {
				File file = new File(mCursor.getString(dataColumn));
//				file.delete();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Intent intent = new Intent(ACTION_APP_DELETED);
			mContext.sendBroadcast(intent);
		}

		public void setCursor(Cursor newCursor) {
			// TODO Auto-generated method stub
			if (mCursor != newCursor) {
				if (mCursor != null) {
					mCursor.close();
				}
				mCursor = newCursor;
				
				notifyDataSetChanged();
			}
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			if (mCursor != null) {
				return mCursor.getCount();
			}
			return 0;
		}

		@Override
		public Object getItem(int position) {
			mCursor.move(position);
			return mCursor;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			if (mCursor != null) {
				mCursor.moveToPosition(position);
				return mCursor.getLong(idColumn);
			}
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(mCursor != null) {
				mCursor.moveToPosition(position);
				
				if (convertView == null || 
						!(convertView.getTag() instanceof ViewHolder)) {
					convertView = mLayoutInflater.inflate(R.layout.app_list_download_item, null);
					viewHolder = new ViewHolder();
					
					viewHolder.mListItem =
						(RelativeLayout) convertView.findViewById(R.id.app_listitem_group);
					viewHolder.mListItemGroupAll = 
						(RelativeLayout) convertView.findViewById(R.id.app_listitem_download);
					viewHolder.mListItem.setOnLongClickListener(mOnLongClickListener);
					viewHolder.mIcon =
						(ImageView) convertView.findViewById(R.id.app_listitem_icon);
					viewHolder.mName =
						(TextView) convertView.findViewById(R.id.app_listitem_name);
					viewHolder.mInfo =
						(TextView) convertView.findViewById(R.id.app_listitem_info);
					viewHolder.mSize = 
						(TextView) convertView.findViewById(R.id.app_listitem_size);
					viewHolder.mBytes = 
							(TextView) convertView.findViewById(R.id.app_listitem_byte);
					
					viewHolder.mProgressBar = 
						(ProgressBar) convertView.findViewById(R.id.app_listitem_progressbar);
					viewHolder.mControl = 
						(ImageButton) convertView.findViewById(R.id.app_listitem_control);
					convertView.setTag(viewHolder);
				} else {
					viewHolder = (ViewHolder) convertView.getTag();
				}
				
//				if ((position % 2) == 0) {
//					viewHolder.mListItemGroupAll.setBackgroundResource(R.drawable.bg_list_item_view);
//				} else {
//					viewHolder.mListItemGroupAll.setBackgroundResource(R.drawable.bg_list_item_grey);
//				}
				
				int appId = mCursor.getInt(appIdColumn);
				int totalBytes = mCursor.getInt(totalBytesColumn);
				long currentBytes = mCursor.getInt(currentBytesColumn);
				int progress = (int)(currentBytes * 100 / totalBytes);
//				viewHolder.mListItem.setTag(Integer.valueOf(position));
				Log.i(TAG, "mIcon =" + getThumbnail(position, appId));
				viewHolder.mIcon.setImageDrawable(getThumbnail(position, appId));
				viewHolder.mName.setText(mCursor.getString(titleColumn));
				viewHolder.mSize.setText(getText(R.string.app_size) + GlobalUtil.getSize(totalBytes));
				viewHolder.mSize.setTag(getSizeViewTag(appId));
				viewHolder.mBytes.setTag(getByteViewTag(appId));
				
//				viewHolder.mProgressBar.setTag(new Integer(appId));
				viewHolder.mProgressBar.setTag(getProcessbarViewTag(appId));
				viewHolder.mProgressBar.setProgress(progress);
//				AppInfoActivity.mProgressBar.setTag(getProcessbarViewTag(appId));
				viewHolder.mInfo.setText(progress + "%");
				viewHolder.mInfo.setVisibility(View.VISIBLE);
				viewHolder.mInfo.setTag(viewHolder.mProgressBar);
				
				Log.i(TAG, "before setonclick");
				viewHolder.mControl.setOnClickListener(mOnClickListener);
//				viewHolder.mControl.setTag(new Integer(appId));
//				viewHolder.mControl.setTag(getControlViewTag(appId));
//				viewHolder.mControl.setTag(viewHolder.mInfo);
				
				viewHolder.mControl.setTag(appId);
				
				int controlValue = mCursor.getInt(controlColumn);
				if(controlValue == 0) {
					bDownloadingStatus.put(appId, BTRUE);
					viewHolder.mControl.setImageResource(R.drawable.btn_manage_downloading_pause);
				} else if(controlValue == 1) {
					bDownloadingStatus.put(appId, BFALSE);
					viewHolder.mControl.setImageResource(R.drawable.btn_manage_downloading_start);
				}
				return convertView;
			}
			return null;
		}
		

		
		public Drawable getThumbnail(int position, int id) {
			
			Drawable drawable = CachedThumbnails.getThumbnail(mContext, id);
			if(drawable == null) {
				return CachedThumbnails.getDefaultIcon(mContext);
			}
			return drawable;
		}

		class ViewHolder {

			RelativeLayout mListItem;
			RelativeLayout mListItemGroupAll;
			ImageView mIcon;
			TextView mName;
			TextView mInfo;
			TextView mSize;
			TextView mBytes;
			ProgressBar mProgressBar;
			ImageButton mControl;
		}
	}
}