package com.huaqin.market.list;

import java.io.File;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.huaqin.market.R;
import com.huaqin.market.download.Constants;
import com.huaqin.market.download.DownloadManager;
import com.huaqin.market.download.Downloads;
import com.huaqin.market.ui.UninstallHintActivity;
import com.huaqin.market.utils.CachedThumbnails;
import com.huaqin.market.utils.OptionsMenu;
import com.huaqin.market.utils.PackageUtil;

public class DownloadedAppListActivity extends Activity {

	public static final String ACTION_APP_DELETED = "com.hauqin.intent.action.APP_DELETED";
	
	private static final int CONTEXT_MENU_DELETE_FILE = 0;
	private static final int CONTEXT_MENU_CANCLE = 1;
	
	private Context mContext;
	
	private View mLoadingIndicator;
	private ImageView mLoadingAnimation;
	private AnimationDrawable loadingAnimation;
	private ListView mListView;
	public static DownloadAppListAdapter mAppListAdapter;
	private Cursor mCursor;
	private int nContextMenuPosition;
	
	private final BroadcastReceiver mApplicationsReceiver;
	private final BroadcastReceiver mApplicationOperationReciever;

	public DownloadedAppListActivity() {

		nContextMenuPosition = 0;
		
		mApplicationsReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				refreshDownloadListView();
			}
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
	}

	private void initHandler() {
		// TODO Auto-generated method stub
		
	}

	private void initListView() {
		// TODO Auto-generated method stub
		Log.v("asd", "initListView="+mLoadingAnimation);
		mLoadingIndicator = findViewById(R.id.fullscreen_loading_indicator);
		mLoadingAnimation = 
			(ImageView) mLoadingIndicator.findViewById(R.id.fullscreen_loading);
		mLoadingAnimation.setBackgroundResource(R.anim.loading_anim);
		loadingAnimation = (AnimationDrawable) mLoadingAnimation.getBackground();
		Log.v("asd", "loadingAnimation="+mLoadingAnimation);
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
		
		mAppListAdapter = new DownloadAppListAdapter(mContext);
		mListView.setAdapter(mAppListAdapter);
		
		registerForContextMenu(mListView);
		
//		refreshDownloadListView();
	}

	private void refreshDownloadListView() {
		// TODO Auto-generated method stub

		String where = Downloads.COLUMN_STATUS + "=" + Downloads.STATUS_SUCCESS;
		mCursor = getApplicationContext().getContentResolver()
					.query(Downloads.CONTENT_URI,
							null, where, null, null);
		if(mAppListAdapter != null){
			mAppListAdapter.setCursor(mCursor);
			mLoadingIndicator.setVisibility(View.GONE);
			mListView.setVisibility(View.VISIBLE);
		}
	}

	private void registerIntentReceivers() {
		// TODO Auto-generated method stub
		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		intentFilter.addDataScheme("package");
		Log.v("asd", "registerIntentReceivers");
		registerReceiver(mApplicationsReceiver, intentFilter);
		
		IntentFilter intentFilter2 = new IntentFilter(ACTION_APP_DELETED);
		intentFilter2.addCategory(Downloads.ACTION_DOWNLOAD_COMPLETED);
		mContext.registerReceiver(mApplicationOperationReciever, intentFilter2);
	}

	private void unregisterIntentReceivers() {
		// TODO Auto-generated method stub
		unregisterReceiver(mApplicationsReceiver);
		unregisterReceiver(mApplicationOperationReciever);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Log.v("asd", "DownloadedAppListActivity");
		mContext = this;
		
		setContentView(R.layout.app_list);
		
		initHandler();
		initListView();
		registerIntentReceivers();
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
//    		startActivity(new Intent(DownloadedAppListActivity.this, OptionsMenu.class));
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
		Log.v("asd", "refreshDownloadListView onResume");
		refreshDownloadListView();
		
	}
	@Override
	protected void onStart(){
		// TODO Auto-generated method stub
		super.onStart();
		Log.v("asd", "refreshDownloadListView onStart");
		refreshDownloadListView();
		
	}
	
	@Override
	protected void onRestart(){
		// TODO Auto-generated method stub
		super.onRestart();
		Log.v("asd", "refreshDownloadListView onRestart");
		refreshDownloadListView();
		
	}
	public class DownloadAppListAdapter extends BaseAdapter {
		
		private Context mContext;
		
		private Cursor mCursor;
		private int idIdx;
		private int dataIdx;
		private int titleIdx;
		private int appIdx;
		private int pkgnameIdx;
		
		private LayoutInflater mLayoutInflater;
		private View.OnClickListener mOnClickListener;
		private View.OnLongClickListener mOnLongClickListener;
		
		public DownloadAppListAdapter(Context context) {

			mContext = context;
			Log.v("asd", "adapter oncreate");
			mLayoutInflater = 
				(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			mOnClickListener = new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					switch (v.getId()) {
					case R.id.app_listitem_operate:
						onOperateClick(v);
						break;
						
					case R.id.app_listitem_group:
						onListItemClick(v);
						break;
						
					default:
						break;
					}
				}
			};
			
			mOnLongClickListener = new View.OnLongClickListener() {
				
				@Override
				public boolean onLongClick(View v) {
					// TODO Auto-generated method stub
					switch (v.getId()) {					
					case R.id.app_listitem_group:
						return onListItemLongClick(v);
					}
					return false;
				}
			};
			

		}

		private boolean onListItemLongClick(View v) {
			// TODO Auto-generated method stub		
			return false;
		}

		private void onOperateClick(View v) {
			// TODO Auto-generated method stub
			
		}

		private void onListItemClick(View v) {
			// TODO Auto-generated method stub
			int position = ((Integer) v.getTag()).intValue();
			
			mCursor.moveToPosition(position);
			
			String filePath = mCursor.getString(dataIdx);
			String packagename = mCursor.getString(pkgnameIdx);
			String filetitle = mCursor.getString(titleIdx);
			int appStatus = PackageUtil.getApplicationStatus(mContext.getPackageManager(),packagename);
			boolean checkSystemVersion = PackageUtil.checkingSystemVersion();
			if(appStatus == PackageUtil.PACKAGE_UPDATE_AVAILABLE && checkSystemVersion){
				boolean signatureConflict = PackageUtil.isCertificatesConfilctedWithInstalledApk(mContext,filePath);
				if(true == signatureConflict){
					Intent uninstallActivity = new Intent(mContext,UninstallHintActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString("package", packagename);
					bundle.putString("filepath", filePath);
					bundle.putString("filetitle", filetitle);
					uninstallActivity.putExtras(bundle);
					uninstallActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					mContext.startActivity(uninstallActivity);
				}else{
					installapk(mContext,filePath);
				}
			}else{
				installapk(mContext,filePath);
			}
			
		}
		private void installapk(Context context,String filePath){
			File file = new File(filePath);
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(file), Constants.MIMETYPE_APK);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		}

		public void onDeleteAppClick(int position) {
			// TODO Auto-generated method stub
			mCursor.moveToPosition(position);
			DownloadManager.deleteCompletedTask(mContext, mCursor.getLong(idIdx));
			
			File file = new File(mCursor.getString(dataIdx));
			file.delete();		
			
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
				
				idIdx = mCursor.getColumnIndexOrThrow(BaseColumns._ID);
				dataIdx = mCursor.getColumnIndexOrThrow(Downloads._DATA);
				titleIdx = mCursor.getColumnIndexOrThrow(Downloads.COLUMN_TITLE);
				appIdx = mCursor.getColumnIndex(Downloads.COLUMN_APP_ID);
				pkgnameIdx = mCursor.getColumnIndex(Downloads.COLUMN_PACKAGE_NAME);
				notifyDataSetChanged();
			}
		}
		private void getCursor(){
			String where = Downloads.COLUMN_STATUS + "=" + Downloads.STATUS_SUCCESS;
			if (mCursor != null) {
				mCursor.close();
			}
			mCursor = getApplicationContext().getContentResolver()
						.query(Downloads.CONTENT_URI,
								null, where, null, null);

			Log.v("asd", "newCursor = "+mCursor);
			setCursor(mCursor);
		}
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			
			if (mCursor != null) {
				Log.v("asd", "getCount = "+mCursor.getCount());
				getCursor();
				return mCursor.getCount();
			}
			return 0;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			if (mCursor != null) {
				mCursor.moveToPosition(position);
				return mCursor.getLong(idIdx);
			}
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder viewHolder = null;
			mCursor.moveToPosition(position);
			
			
			if (convertView == null || 
					!(convertView.getTag() instanceof ViewHolder)) {
				convertView = mLayoutInflater.inflate(R.layout.app_list_downloaded_item, null);
				viewHolder = new ViewHolder();
				
				viewHolder.mListItem =
					(RelativeLayout) convertView.findViewById(R.id.app_listitem_group);
				
				viewHolder.mListItem.setOnClickListener(mOnClickListener);
				viewHolder.mListItem.setOnLongClickListener(mOnLongClickListener);
				
				viewHolder.mIcon =
					(ImageView) convertView.findViewById(R.id.app_listitem_icon);
				viewHolder.mName =
					(TextView) convertView.findViewById(R.id.app_listitem_name);
				viewHolder.mInfo =
					(TextView) convertView.findViewById(R.id.app_listitem_info_2);
				
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			
//			if ((position % 2) == 0) {
//				viewHolder.mListItem.setBackgroundResource(R.drawable.bg_list_item_view);
//			} else {
//				viewHolder.mListItem.setBackgroundResource(R.drawable.bg_list_item_grey);
//			}
			
			viewHolder.mListItem.setTag(Integer.valueOf(position));
			Log.i("downloadedlist", "mIcon =" + getThumbnail(position, mCursor.getInt(appIdx)));
			viewHolder.mIcon.setImageDrawable(getThumbnail(position, mCursor.getInt(appIdx)));
			viewHolder.mName.setText(mCursor.getString(titleIdx));
			viewHolder.mInfo.setText(R.string.app_download_over);
			viewHolder.mInfo.setVisibility(View.VISIBLE);

			return convertView;
		}
		
		public Drawable getThumbnail(int position, int id) {
			
			Drawable drawable = CachedThumbnails.getThumbnail(mContext, mCursor.getInt(appIdx));
			if(drawable == null) {
				return CachedThumbnails.getDefaultIcon(mContext);
			}
			return drawable;
		}

		class ViewHolder {

			RelativeLayout mListItem;
			ImageView mIcon;
			TextView mName;
			TextView mInfo;
		}
	}
}