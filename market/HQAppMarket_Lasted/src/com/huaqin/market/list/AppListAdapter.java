package com.huaqin.market.list;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.LayoutParams;

import com.huaqin.android.market.sdk.bean.Application;
import com.huaqin.market.MarketBrowser;
import com.huaqin.market.R;
import com.huaqin.market.SlideViewPager;
import com.huaqin.market.download.Constants;
import com.huaqin.market.download.DownloadManager;
import com.huaqin.market.download.Downloads;
import com.huaqin.market.download.Helpers;
import com.huaqin.market.model.Application2;
import com.huaqin.market.ui.UninstallHintActivity;
import com.huaqin.market.utils.CachedThumbnails;
import com.huaqin.market.utils.GlobalUtil;
import com.huaqin.market.utils.PackageUtil;

public class AppListAdapter extends ArrayAdapter<Application> {
	private static final String TAG = "AppListAdapter";
	private Context mContext;
	private LayoutInflater mLayoutInflater;
	private Drawable mThumb = null;
	private int mProgress = 0;
	private static final int PROGRESSBAR_UPDATEING = 1;
	private static final int NON_HOTTYPE = 0;
	private static final int HOTTYPE_START = 1;
	private static final int HOTTYPE_HOT = 2;
	private static final int HOTTYPE_OFFICAL = 3;
	private View.OnClickListener mOnClickListener;
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
	private final int currentBytesColumn = 2;
	private final int totalBytesColumn = 3;
	private final int controlColumn = 6;
//	private Animation push_in; 
	private Animation icon_push_in; 
	private ViewHolder viewHolder = null;;
	private Drawable mAnimationThumb = null;
	private ViewHolder mAnimationViewHolder = null;
	private Bitmap mSelectedItemBitmap;
	private Bitmap mBitmap;
	/*************Added-s by JimmyJin for Pudding Project**************/
	private String mFromWhere ="0";
	public static View mListImg ;
	public AppListAdapter(Context context, ArrayList<Application> objects, int itemType,String fromWhere) {
		
		super(context, 0, objects);
		mFromWhere = fromWhere;
		mContext = context;
		
		//mDownloadArray = new ArrayList<Integer>();
		mLayoutInflater = 
			(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mOnClickListener = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.v("onClick", "View ="+v);
				switch (v.getId()) {
				case R.id.app_listmenu_progressbar_area:
					onProgressbarClick(v);
					break;
				case R.id.btnview_adapter_download:
					onOperateClick(v);
					break;
				default:
					break;
				}
			}
		};
		
//		push_in=AnimationUtils.loadAnimation(context, R.anim.item_rightrun); 
		icon_push_in=AnimationUtils.loadAnimation(context, R.anim.item_leftrun); 
	}
	private void onProgressbarClick(View v){
		Application2 mApp2Info = (Application2) v.getTag();
		initDownloadStatus(mApp2Info);
		ProgressBar mProgressBar = (ProgressBar)v.findViewById(R.id.app_listmenu_progressbar);
		Button mButton = (Button)v.findViewById(R.id.btnview_adapter_download);
		TextView mStatus = (TextView)v.findViewById(R.id.app_listmenu_progressbar_status);;
		mProgressBar.setVisibility(View.VISIBLE);
		mStatus.setVisibility(View.VISIBLE);
		
		switch(mApp2Info.bDownloadingFlag)
		{
			case 0:
//				DownloadManager.startDownloadAPK(mContext, mApp2Info, mFromWhere);
//				Toast.makeText(mContext.getApplicationContext(), mContext.getText(R.string.app_downloading_view_other_layout), Toast.LENGTH_LONG).show();
//				mApp2Info.bDownloadingFlag=1;
				break;
			case 1:
				DownloadManager.pauseDownload(mContext, new long[]{mApp2Info.getAppId()});
				mStatus.setText(mContext.getText(R.string.app_rezume));
				mApp2Info.bDownloadingFlag=2;
				break;
			case 2:
				DownloadManager.resumeDownload(mContext, new long[]{mApp2Info.getAppId()});
				mApp2Info.bDownloadingFlag=1;
				mStatus.setText(mContext.getText(R.string.app_pause));
				break;
		}
		
	}
    private void onOperateClick(View v){
//    	Application2 mApp2Info = (Application2) v.getTag();
    	Application2 mApp2Info = ((AppInfo) v.getTag()).app2;
    	Log.v("onOperateClick", "mApp2Info ="+mApp2Info);
    	initDownloadStatus(mApp2Info);
       	if (mApp2Info.bUpdateAvailable) {
			Application2 appInfo =
				new Application2(PackageUtil.getUpdateApplication(mApp2Info.getAppPackage()));
			long sdCardAvailSize = Helpers.getAvailaleSize();
			Log.v(TAG,"JimmyJin sdCardAvailSize678="+sdCardAvailSize);
			if (sdCardAvailSize == -1) {
				Toast.makeText(mContext, mContext.getString(R.string.sdcard_not_amount_info), Toast.LENGTH_LONG).show();
			} else if(sdCardAvailSize < appInfo.getSize()) {
				Toast.makeText(mContext, mContext.getString(R.string.dlg_sdcard_size_not_enough_msg), Toast.LENGTH_LONG).show();
			} else {
				
				if(!DownloadManager.queryDownloadingURL(mContext, appInfo)) {
					if(checkNetworkState()){
						Button button = (Button)v;
//						ProgressBar mProgressBar = (ProgressBar)v;
//						TextView mStatus = (TextView)v;
//						button.setCompoundDrawables(null,null,null,null);
						Log.v("onOperateClick","appInfo.bDownloading="+mApp2Info.bDownloadingFlag);
						switch(mApp2Info.bDownloadingFlag)
						{
						case 0:
							//Added-s by JimmyJin
							IconAnimation(v);
							//Added-e by JimmyJin
							
							DownloadManager.startDownloadAPK(mContext, appInfo, mFromWhere);
							Toast.makeText(mContext.getApplicationContext(), mContext.getText(R.string.app_downloading_view_other_layout), Toast.LENGTH_LONG).show();
							mApp2Info.bDownloadingFlag=1;
							button.setText(mContext.getText(R.string.app_pause));
							button.setVisibility(View.GONE);
//							mStatus.setText(mContext.getText(R.string.app_pause));
//							mProgressBar.setVisibility(View.VISIBLE);
//							mStatus.setVisibility(View.VISIBLE);
							break;
						case 1:
							DownloadManager.pauseDownload(mContext, new long[]{appInfo.getAppId()});
							mApp2Info.bDownloadingFlag=2;
							button.setText(mContext.getText(R.string.app_rezume));
							button.setVisibility(View.GONE);
//							mStatus.setText(mContext.getText(R.string.app_rezume));
//							mProgressBar.setVisibility(View.VISIBLE);
//							mStatus.setVisibility(View.VISIBLE);
							break;
						case 2:
							DownloadManager.resumeDownload(mContext, new long[]{appInfo.getAppId()});
							mApp2Info.bDownloadingFlag=1;
							button.setText(mContext.getText(R.string.app_pause));
//							mStatus.setText(mContext.getText(R.string.app_pause));
//							mProgressBar.setVisibility(View.VISIBLE);
							button.setVisibility(View.GONE);
//							mStatus.setVisibility(View.VISIBLE);
							break;
						}				
					}
					else{
						Toast.makeText(mContext.getApplicationContext(), mContext.getText(R.string.app_downloading_without_network), Toast.LENGTH_LONG).show();
					}
				} else {
					Toast.makeText(mContext.getApplicationContext(), mContext.getText(R.string.dlg_apk_downloading_msg), Toast.LENGTH_LONG).show();
				}
			}
		} else if(mApp2Info.bInstalled){
			
			Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(mApp2Info.getAppPackage());  
			if(intent != null){
				mContext.startActivity(intent);  
			}
		}else if(mApp2Info.bDownloadNotInstalled) {
			String packagename = mApp2Info.getAppPackage();
			String filetitle = mApp2Info.getAppName();
//			String filepath = Environment.getExternalStorageDirectory() + "/hqappmarket/apks/" + filetitle + ".apk";
			String filepath = DownloadManager.queryDownloadedURL(mContext, mApp2Info);

			int appStatus = PackageUtil.getApplicationStatus(mContext.getPackageManager(),packagename);
			boolean checkSystemVersion = PackageUtil.checkingSystemVersion();
			if(appStatus == PackageUtil.PACKAGE_UPDATE_AVAILABLE && checkSystemVersion){
				boolean signatureConflict = PackageUtil.isCertificatesConfilctedWithInstalledApk(mContext,filepath);
				if(true == signatureConflict){
					Intent uninstallactivity = new Intent(mContext,UninstallHintActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString("package", packagename);
					bundle.putString("filepath", filepath);
					bundle.putString("filetitle", filetitle);
					uninstallactivity.putExtras(bundle);
					uninstallactivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					mContext.startActivity(uninstallactivity);
				}else{
//					progressBar.setVisibility(View.GONE);
					installapk(mContext,filepath);
				}
			}else{
				Log.v(TAG,"JimmyJin installapk="+filepath);
				installapk(mContext,filepath);
			}	
//			}else{
//				Intent install = new Intent(Intent.ACTION_VIEW);
//				install.setDataAndType(Uri.fromFile(mApp2Info.downloadedAppFile), Constants.MIMETYPE_APK);
//				install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				mContext.startActivity(install);
//			}
			
//			Intent install = new Intent(Intent.ACTION_VIEW);
//			install.setDataAndType(Uri.fromFile(mApp2Info.downloadedAppFile), Constants.MIMETYPE_APK);
//			install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			mContext.startActivity(install);
		} else {
			long sdCardAvailSize = Helpers.getAvailaleSize();
			Log.v(TAG,"JimmyJin sdCardAvailSize678678="+sdCardAvailSize);
			if (sdCardAvailSize == -1) {
				Toast.makeText(mContext, mContext.getString(R.string.sdcard_not_amount_info), Toast.LENGTH_LONG).show();
			} else if(sdCardAvailSize < mApp2Info.getSize()) {
				Toast.makeText(mContext, mContext.getString(R.string.dlg_sdcard_size_not_enough_msg), Toast.LENGTH_LONG).show();
			} else {
				if(checkNetworkState()){
//					DownloadManager.startDownloadAPK(mContext, mApp2Info, mFromWhere);
//					Button button = (Button)v; 
//					button.setCompoundDrawables(null,null,null,null);
////					button.setText(mContext.getText(R.string.btn_appinfo_downloading));
////					button.setEnabled(false);		
//					button.setText(mContext.getText(R.string.app_pause));
//					mApp2Info.bDownloading = true;
//					Toast.makeText(mContext.getApplicationContext(), mContext.getText(R.string.app_downloading_view_other_layout), Toast.LENGTH_LONG).show();
					Button button = (Button)v;
//					ProgressBar mProgressBar = (ProgressBar)v;
//					TextView mStatus = (TextView)v;
//					
					Log.v("onOperateClick","button="+button);
//					button.setCompoundDrawables(null,null,null,null);			
					switch(mApp2Info.bDownloadingFlag)
					{
					case 0:
						//Added-s by JimmyJin
						IconAnimation(v);
						//Added-e by JimmyJin
						
						DownloadManager.startDownloadAPK(mContext, mApp2Info, mFromWhere);
						Toast.makeText(mContext.getApplicationContext(), mContext.getText(R.string.app_downloading_view_other_layout), Toast.LENGTH_LONG).show();
						mApp2Info.bDownloadingFlag=1;
						button.setText(mContext.getText(R.string.app_pause));
//						mStatus.setText(mContext.getText(R.string.app_pause));
//						mProgressBar.setVisibility(View.VISIBLE);
						button.setVisibility(View.GONE);
//						mStatus.setVisibility(View.VISIBLE);
						break;
					case 1:
						DownloadManager.pauseDownload(mContext, new long[]{mApp2Info.getAppId()});
						mApp2Info.bDownloadingFlag=2;
						button.setText(mContext.getText(R.string.app_rezume));
//						mStatus.setText(mContext.getText(R.string.app_rezume));
//						mProgressBar.setVisibility(View.VISIBLE);
						button.setVisibility(View.GONE);	
//						mStatus.setVisibility(View.VISIBLE);
						break;
					case 2:
						DownloadManager.resumeDownload(mContext, new long[]{mApp2Info.getAppId()});
						mApp2Info.bDownloadingFlag=1;
						button.setText(mContext.getText(R.string.app_pause));
//						mStatus.setText(mContext.getText(R.string.app_pause));
//						mProgressBar.setVisibility(View.VISIBLE);
						button.setVisibility(View.GONE);
//						mStatus.setVisibility(View.VISIBLE);
						break;
					}
				}
				else{
					Toast.makeText(mContext.getApplicationContext(), mContext.getText(R.string.app_downloading_without_network), Toast.LENGTH_LONG).show();
				}
				
			}
		}
	}
	private String getProcessbarViewTag(int appId) {
		return "processbar" + appId;
	} 
	private String getStatusViewTag(int appId) {
		return "status" + appId;
	} 
    private void installapk(Context context,String filepath){
    	Log.v(TAG,"JimmyJin filepath="+filepath);
    	File file = new File(filepath);
    	Log.v(TAG,"JimmyJin file678678="+file);
    	Intent install = new Intent(Intent.ACTION_VIEW);
		install.setDataAndType(Uri.fromFile(file), Constants.MIMETYPE_APK);
		install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(install);
    }
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		//return Long.parseLong(((Application)getItem(position)).getAppId());
		return (getItem(position)).getAppId();
	}
private void initDownloadStatus(Application2 appInfo){
	int appId = 0;
	if (appInfo.getAppId() != 0) {
		appId = appInfo.getAppId();
//		Log.d(TAG,"2getView() appId = " + app.getAppId()); 
	}
	String where = Downloads.WHERE_RUNNING +" and "+Downloads.WHERE_APP_ID;
	boolean downloading = DownloadManager.queryDownloadingURL(mContext, appInfo);
	int controlValue = 2;
	int max = 1;
	int progress = 0;
	mCursor = mContext.getContentResolver()
			.query(Downloads.CONTENT_URI,
					mCols, where, new String[]{"" + appId}, null);
	if(mCursor != null){
		if(mCursor.moveToFirst()){
			controlValue = mCursor.getInt(controlColumn);
			max = mCursor.getInt(totalBytesColumn);
	        progress = mCursor.getInt(currentBytesColumn);
		}
        if(progress < max && max > 0 && (progress * 100) > max) {
        	mProgress = progress * 100 / max;
        }else{
        	mProgress = 0;
        }
	}
	if(mCursor!=null&&!mCursor.isClosed()){
		mCursor.close();
	}
	if(downloading) {
//		mDownloadButton.setClickable(false);
//		mAppStatus.setText(R.string.app_downloading);
		
		if(controlValue == 0) {
			appInfo.bDownloadingFlag = 1;
		} else if(controlValue == 1) {
			appInfo.bDownloadingFlag = 2;
		}
	} 
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
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		Log.v("getView", "mCursor="+mCursor);
//		if(mCursor==null){
//			return null;		
//		}
//			mCursor.moveToPosition(position);
//			Log.v("asd", "mCursor.moveToPosition"+mCursor.moveToPosition(position));
		Application app = getItem(position);
		AppInfo appinfo = null;
		Log.v("asd", "app ="+app);
//		Log.d(TAG,"getView() appId = " + app.getAppId());

		Application2 appInfo = new Application2(app);
		Log.v("asd", "mContext.getPackageManager() ="+mContext.getPackageManager());
		Log.v("asd", "appInfo.getAppPackage() ="+appInfo.getAppPackage());
		int appStatus = PackageUtil.getApplicationStatus(mContext.getPackageManager(), appInfo.getAppPackage());
//		Log.d(TAG,"getView() mAppInfo.Id = " + appInfo.getAppId());
		
		int appId = 0;
		if (app.getAppId() != 0) {
			appId = app.getAppId();
//			Log.d(TAG,"2getView() appId = " + app.getAppId());
		}
		initDownloadStatus(appInfo);
		
		float rating = 0.0f;
		Integer intRating = app.getStars();
		if(intRating != null) {
			rating = (float)(intRating.intValue());
		} 
		mThumb = null;
		
		if (convertView == null){
//			convertView = mLayoutInflater.inflate(R.layout.app_list_item, null);
			convertView = mLayoutInflater.inflate(R.layout.app_list_item, parent, false);
			viewHolder = new ViewHolder();
			
//			viewHolder.mParent = convertView;
			viewHolder.mListItem = (RelativeLayout) convertView.findViewById(R.id.app_listitem_layout);
			viewHolder.mProgressBar = (ProgressBar) convertView.findViewById(R.id.app_listmenu_progressbar);
//			viewHolder.mProgressBar.setTag(getProcessbarViewTag(appId));
//			viewHolder.mProgressBar.setVisibility(View.GONE);		
			viewHolder.mProgressBarArea = (RelativeLayout) convertView.findViewById(R.id.app_listmenu_progressbar_area);
			viewHolder.mStatus = (TextView) convertView.findViewById(R.id.app_listmenu_progressbar_status);
//			viewHolder.mStatus.setVisibility(View.GONE);
			viewHolder.mThumbnail = (ImageView) convertView.findViewById(R.id.app_listitem_thumb);
//			viewHolder.mAuthor =  (TextView) convertView.findViewById(R.id.app_listitem_author);
			viewHolder.mTitle = (TextView) convertView.findViewById(R.id.app_listitem_title);
			viewHolder.mSize = (TextView) convertView.findViewById(R.id.app_listitem_size);
//			viewHolder.mFee = (TextView) convertView.findViewById(R.id.app_listitem_feetype);
			viewHolder.mDownloadLevel = (TextView) convertView.findViewById(R.id.app_listitem_downlevel);		
			viewHolder.mRating =  (RatingBar) convertView.findViewById(R.id.app_listitem_rating);
			viewHolder.mButton = (Button)convertView.findViewById(R.id.btnview_adapter_download);
			viewHolder.mButton.setVisibility(View.VISIBLE);
			viewHolder.mHotType = (ImageView)convertView.findViewById(R.id.app_listitem_hottype);
			viewHolder.mHotType.setVisibility(View.GONE);
			convertView.setTag(viewHolder);
		} else{
			viewHolder = (ViewHolder) convertView.getTag();
		}
		Log.v("getView", "appInfo.getHotType="+appInfo.getHotType());
		Drawable drawable = null;
		switch(appInfo.getHotType()){
			case NON_HOTTYPE:
				viewHolder.mHotType.setVisibility(View.GONE);
				break;
			case HOTTYPE_HOT:
				drawable = mContext.getResources().getDrawable(R.drawable.ic_hot);
				viewHolder.mHotType.setVisibility(View.VISIBLE);
				viewHolder.mHotType.setBackgroundDrawable(drawable);
				break;
			case HOTTYPE_START:
				drawable = mContext.getResources().getDrawable(R.drawable.ic_start);
				viewHolder.mHotType.setVisibility(View.VISIBLE);
				viewHolder.mHotType.setBackgroundDrawable(drawable);
				break;
			case HOTTYPE_OFFICAL:
				drawable = mContext.getResources().getDrawable(R.drawable.ic_offical);
				viewHolder.mHotType.setVisibility(View.VISIBLE);
				viewHolder.mHotType.setBackgroundDrawable(drawable);
				break;
			default:
				break;
		}
		
//		viewHolder.mButton.setTag(appInfo);
		viewHolder.mProgressBarArea.setTag(appInfo);
		viewHolder.mProgressBar.setTag(getProcessbarViewTag(appId));
		viewHolder.mProgressBar.setProgress(mProgress);
		viewHolder.mStatus.setText(mContext.getText(R.string.app_pause));
//		viewHolder.mListBottonArea.setTag(getProcessbarViewTag(appId));
		viewHolder.mStatus.setTag(getStatusViewTag(appId));
		viewHolder.mButton.setOnClickListener(mOnClickListener);
		viewHolder.mProgressBarArea.setOnClickListener(mOnClickListener);
			
		if(appStatus == PackageUtil.PACKAGE_NOT_INSTALLED )
			{
				Log.v("asd", "initBtnStatus");
//				initBtnStatus(viewHolder,appInfo,mCursor);	
			}
		else
			{
//				initBtnStatusEx(viewHolder,appInfo,appStatus,mCursor);	
			}
//		viewHolder.mButton.setTag(appInfo);
//		Log.d(TAG,"viewHolder.mButton.getTag() = " + viewHolder.mButton.getTag());
//		initBtnStatus(viewHolder,appInfo);	
//		if ((position % 2) == 0) {
//			viewHolder.mListItem.setBackgroundResource(R.drawable.bg_list_item_view);
//		} else {
//			viewHolder.mListItem.setBackgroundResource(R.drawable.bg_list_item_grey);
//		}
		
		viewHolder.mThumbnail.setTag(Integer.valueOf(appId));
		if (mContext instanceof NewAppListActivity) {
			mThumb = ((NewAppListActivity) mContext).getThumbnail(position, appId);
		} else if (mContext instanceof RecommandAppListActivity) {
			mThumb = ((RecommandAppListActivity) mContext).getThumbnail(position, appId);
		} else if (mContext instanceof RankAppListActivity) {
			mThumb = ((RankAppListActivity) mContext).getThumbnail(position, appId);
		} else if (mContext instanceof CategoryAppListActivity) {
			mThumb = ((CategoryAppListActivity) mContext).getThumbnail(position, appId);
		} else if (mContext instanceof SearchAppListActivity) {
			mThumb = ((SearchAppListActivity) mContext).getThumbnail(position, appId);
		} else if (mContext instanceof RelatedAppListActivity) {
			mThumb = ((RelatedAppListActivity) mContext).getThumbnail(position, appId);
		}else if (mContext instanceof SortGameListActivity) {
			mThumb = ((SortGameListActivity) mContext).getThumbnail(position, appId);
		}else if (mContext instanceof SortAppListActivity) {
			mThumb = ((SortAppListActivity) mContext).getThumbnail(position, appId);
		}else {
			mThumb = CachedThumbnails.getDefaultIcon(mContext);
		}
//		MarketPage.setTouchIntercept(true);
		Log.d(TAG,"getView() mThumb = " + mThumb);
		viewHolder.mThumbnail.setImageDrawable(mThumb);
		viewHolder.mTitle.setText(app.getAppName());
		Log.d(TAG,"getView() app.getAppName() = " + app.getAppName());
		Log.d(TAG,"getView() app.getAppId() = " + app.getAppId());
		viewHolder.mSize.setText(mContext.getResources().getString(R.string.app_size)+GlobalUtil.getSize(app.getSize()));
		if(appInfo.getDownloads() == 0){
			viewHolder.mDownloadLevel.setText("0"+mContext.getResources().getString(R.string.app_downlods_item));	
		}else if(appInfo.getDownloads() <10000){
			viewHolder.mDownloadLevel.setText(appInfo.getDownloads()+mContext.getResources().getString(R.string.app_downlods_item));
		}else{
			viewHolder.mDownloadLevel.setText(appInfo.getDownloads()/10000+mContext.getResources().getString(R.string.app_downlods_item_large));
		}
		
//		viewHolder.mAuthor.setText(app.getAuthor());
//		viewHolder.mFee.setText(R.string.app_fee_free);
		viewHolder.mRating.setRating(rating);
		viewHolder.bPriced = false;
		
		
		appinfo = new AppInfo();
		appinfo.icon = mThumb;
		appinfo.app2 = appInfo;
		appinfo.listItem = convertView; 		
		viewHolder.mButton.setTag(appinfo);
		
//		push_in.setDuration(duration);   
//		convertView.setAnimation(push_in);  
//		int width = RecommandAppListActivity.mWindowManager.getDefaultDisplay().getWidth();
//		int height = RecommandAppListActivity.mWindowManager.getDefaultDisplay().getHeight();
//		AbsListView.LayoutParams lp = new AbsListView.LayoutParams(width, 92*RecommandAppListActivity.dm.densityDpi/240);
//		convertView.setLayoutParams(lp);

		return convertView;
		
	}
	private boolean checkNetworkState() {
		// TODO Auto-generated method stub
		ConnectivityManager connectMgr =
			(ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		if (connectMgr == null) {
			return false;
		}
		
		NetworkInfo nwInfo = connectMgr.getActiveNetworkInfo();
		
		if (nwInfo == null || !nwInfo.isAvailable()) {
			return false;
		}
		return true;
	}

	private void initBtnStatus(ViewHolder viewHolder,Application2 appInfo,Cursor cursor){
		String downloadedORDownloading = DownloadManager.queryDownloadedORDownloadingAPKEx(mContext, appInfo);
		String downloadedforDownloading = DownloadManager.queryDownloadedURL(mContext, appInfo);
		Drawable drawable = null;
		Log.v("AppList","JimmyJin downloadedORDownloading="+downloadedORDownloading);
		Log.v("AppList","JimmyJin downloadedforDownloading="+downloadedforDownloading);
		Log.v("AppList"," appInfo.bDownloadingFlag="+appInfo.bDownloadingFlag);
		if(null == downloadedORDownloading||null == downloadedforDownloading){
			
//				Drawable drawable = mContext.getResources().getDrawable(R.drawable.btn_subinfo_download);
				viewHolder.mButton.setClickable(true);
//				drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
//				viewHolder.mButton.setCompoundDrawables(drawable,null,null,null);
				viewHolder.mButton.setText(mContext.getText(R.string.btn_appinfo_download));
//				viewHolder.mButton.setTextColor(0xff3b9305);
				viewHolder.mButton.setEnabled(true);
//				viewHolder.mProgressBar.setProgress(0);
//				viewHolder.mProgressBar.setVisibility(View.GONE);
				viewHolder.mButton.setVisibility(View.VISIBLE);
				drawable = mContext.getResources().getDrawable(R.drawable.ic_btn_applist);
				viewHolder.mButton.setBackgroundDrawable(drawable);
//				viewHolder.mStatus.setVisibility(View.GONE);
//				drawable = mContext.getResources().getDrawable(R.drawable.ic_btn_applist);
//				drawable.setBounds(0, 0,viewHolder.mButton.getWidth(), viewHolder.mButton.getHeight());
//				viewHolder.mButton.setBackgroundDrawable(drawable);
			if(downloadedORDownloading != null&&downloadedORDownloading.equals("Downloading")){
				viewHolder.mButton.setClickable(true);
//				viewHolder.mButton.setCompoundDrawables(null,null,null,null);
				if(cursor != null){
					int progress = DownloadManager.queryDownloadingProgressURL(mContext,appInfo);
					Log.v("initBtnStatus", "progress ="+progress);
					viewHolder.mProgressBar.setProgress(progress);
					viewHolder.mProgressBar.setVisibility(View.VISIBLE);
					viewHolder.mButton.setVisibility(View.GONE);
				}
//				viewHolder.mButton.setTextColor(0xff3b9305);
				if(appInfo.bDownloadingFlag == 2){
					viewHolder.mStatus.setVisibility(View.VISIBLE);
					viewHolder.mStatus.setText(mContext.getText(R.string.app_rezume));
					viewHolder.mButton.setText(null);
				}if(appInfo.bDownloadingFlag == 1){
					viewHolder.mStatus.setVisibility(View.VISIBLE);
					viewHolder.mStatus.setText(mContext.getText(R.string.app_pause));
					viewHolder.mButton.setText(mContext.getText(R.string.app_pause));
				}

//				viewHolder.mButton.setEnabled(false);
			}
		}else{
			if(!downloadedORDownloading.equals("Downloading")){
				appInfo.downloadedAppFile = new File(downloadedORDownloading);
				if(appInfo.downloadedAppFile != null && appInfo.downloadedAppFile.exists()) {
//					Drawable drawable = mContext.getResources().getDrawable(R.drawable.btn_subinfo_download);
					viewHolder.mButton.setClickable(true);
//					drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
					appInfo.bDownloadNotInstalled = true;
//					viewHolder.mProgressBar.setProgress(0);
//					viewHolder.mProgressBar.setVisibility(View.GONE);
					viewHolder.mButton.setVisibility(View.VISIBLE);
//					viewHolder.mStatus.setVisibility(View.GONE);
//					viewHolder.mButton.setCompoundDrawables(drawable,null,null,null);
					viewHolder.mButton.setText(mContext.getText(R.string.app_install));
					drawable = mContext.getResources().getDrawable(R.drawable.ic_btn_applist);
//					drawable.setBounds(0, 0,viewHolder.mButton.getWidth(), viewHolder.mButton.getHeight());
					viewHolder.mButton.setBackgroundDrawable(drawable);
					viewHolder.mButton.setEnabled(true);
				}
			}else{
					viewHolder.mButton.setClickable(false);
//					viewHolder.mButton.setCompoundDrawables(null,null,null,null);
					viewHolder.mButton.setText(mContext.getText(R.string.btn_appinfo_downloading));
					viewHolder.mButton.setEnabled(false);
			} 
		}
	}

	public void initBtnStatusEx(ViewHolder viewHolder,Application2 appInfo,int appStatus,Cursor cursor){
		Drawable drawable = null;
		Log.v("initBtnStatusEx", "appStatus ="+appStatus);
		Log.v("AppList"," appInfo.bDownloadingFlag="+appInfo.bDownloadingFlag);
		switch (appStatus) {			
		case PackageUtil.PACKAGE_UPDATE_AVAILABLE:	
			String downloadedORDownloading = DownloadManager.queryDownloadedORDownloadingAPKEx(mContext, appInfo);
			if(downloadedORDownloading == null){
//				drawable = mContext.getResources().getDrawable(R.drawable.btn_subinfo_update);
				appInfo.bUpdateAvailable = true;
				appInfo.bInstalled = false;
				viewHolder.mButton.setClickable(true);
//				drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
				viewHolder.mButton.setText(mContext.getText(R.string.app_update_available));
//				viewHolder.mButton.setTextColor(0xffa60a0a);
//				viewHolder.mProgressBar.setProgress(0);
//				viewHolder.mProgressBar.setVisibility(View.GONE);
				viewHolder.mButton.setVisibility(View.VISIBLE);
//				viewHolder.mStatus.setVisibility(View.GONE);
				drawable = mContext.getResources().getDrawable(R.drawable.ic_btn_update);
//				viewHolder.mButton.setTextColor(0xffa69305);
//				drawable.setBounds(0, 0,viewHolder.mButton.getWidth(), viewHolder.mButton.getHeight());
				viewHolder.mButton.setBackgroundDrawable(drawable);
//				viewHolder.mButton.setCompoundDrawables(drawable,null,null,null);
			}else if(downloadedORDownloading!=null && downloadedORDownloading.equals("Downloading")){
				viewHolder.mButton.setClickable(false);
//				viewHolder.mButton.setCompoundDrawables(null,null,null,null);
				if(downloadedORDownloading != null&&downloadedORDownloading.equals("Downloading")){
					viewHolder.mButton.setClickable(true);
					if(cursor != null){
						int progress = DownloadManager.queryDownloadingProgressURL(mContext,appInfo);
						viewHolder.mProgressBar.setProgress(progress);
						viewHolder.mProgressBar.setVisibility(View.VISIBLE);
						viewHolder.mButton.setVisibility(View.GONE);
//						drawable = mContext.getResources().getDrawable(R.drawable.ic_btn_applist);
//						drawable.setBounds(0, 0,viewHolder.mButton.getWidth(), viewHolder.mButton.getHeight());
//						viewHolder.mButton.setBackgroundDrawable(drawable);
					}
					if(appInfo.bDownloadingFlag == 2){
//						viewHolder.mButton.setTextColor(0xff3b9305);
						viewHolder.mStatus.setVisibility(View.VISIBLE);
						viewHolder.mStatus.setText(mContext.getText(R.string.app_rezume));
						viewHolder.mButton.setText(null);
					}if(appInfo.bDownloadingFlag == 1){
//						viewHolder.mButton.setTextColor(0xff3b9305);
						viewHolder.mStatus.setVisibility(View.VISIBLE);
						viewHolder.mStatus.setText(mContext.getText(R.string.app_pause));
						viewHolder.mButton.setText(null);
					}
				}
//				viewHolder.mButton.setText(mContext.getText(R.string.btn_appinfo_downloading));
//				viewHolder.mButton.setEnabled(false);
			}else if(downloadedORDownloading!=null && !downloadedORDownloading.equals("Downloading")){
				appInfo.downloadedAppFile = new File(downloadedORDownloading);
				if(appInfo.downloadedAppFile != null && appInfo.downloadedAppFile.exists()) {
//					drawable = mContext.getResources().getDrawable(R.drawable.btn_subinfo_download);
//					drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
					appInfo.bUpdateAvailable = false;
					appInfo.bDownloadNotInstalled = true;
					Log.v("asd", "mProgressBar = ");
					viewHolder.mButton.setClickable(true);
					viewHolder.mButton.setEnabled(true);
					viewHolder.mButton.setVisibility(View.VISIBLE);
//					viewHolder.mButton.setCompoundDrawables(drawable,null,null,null);
//					viewHolder.mProgressBar.setProgress(0);
//					viewHolder.mProgressBar.setVisibility(View.GONE);
//					viewHolder.mStatus.setVisibility(View.GONE);
					viewHolder.mButton.setText(R.string.app_install);
					
//					drawable = mContext.getResources().getDrawable(R.drawable.ic_btn_applist);
//					drawable.setBounds(0, 0,viewHolder.mButton.getWidth(), viewHolder.mButton.getHeight());
//					viewHolder.mButton.setBackgroundDrawable(drawable);
				}
			}
			break;
			
		case PackageUtil.PACKAGE_INSTALLED:
			appInfo.bUpdateAvailable = false;
			appInfo.bInstalled = true;
//			drawable = mContext.getResources().getDrawable(R.drawable.btn_subinfo_open);
			viewHolder.mButton.setClickable(true);
			viewHolder.mButton.setEnabled(true);
			viewHolder.mButton.setVisibility(View.VISIBLE);
//			drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
			viewHolder.mButton.setText(mContext.getText(R.string.app_open));
//			viewHolder.mButton.setTextColor(0xff515151);
//			viewHolder.mProgressBar.setProgress(0);
//			viewHolder.mProgressBar.setVisibility(View.GONE);
//			viewHolder.mStatus.setVisibility(View.GONE);
//			drawable = mContext.getResources().getDrawable(R.drawable.ic_btn_open);
//			drawable.setBounds(0, 0,viewHolder.mButton.getWidth(), viewHolder.mButton.getHeight());
//			viewHolder.mButton.setBackgroundDrawable(drawable);
//			viewHolder.mButton.setCompoundDrawables(drawable,null,null,null);
			break;
			
		default:
			break;
		}
	}

	class ViewHolder {
		int _id;
		View mParent;
		ImageView mThumbnail;
//		TextView mAuthor;
		TextView mTitle;
		TextView mSize;
		TextView mDownloadLevel;
//		TextView mFee;
		RatingBar mRating;

		ProgressBar mProgressBar;
		TextView mStatus;
		Button mButton;
		ImageView mHotType;
		boolean bPriced;
		RelativeLayout mProgressBarArea;
		RelativeLayout mListItem;
	}
	
	class AppInfo{
		Drawable icon;
		Application2 app2;
		View listItem;
	}
	
	@SuppressWarnings("deprecation")
	private void IconAnimation(View v){
		//Added-s by JimmyJin
		mAnimationThumb = ((AppInfo)v.getTag()).icon;
		mAnimationViewHolder = (ViewHolder) ((AppInfo)v.getTag()).listItem.getTag();	
		mAnimationViewHolder.mThumbnail.setDrawingCacheEnabled(true);
		mSelectedItemBitmap = Bitmap.createBitmap( mAnimationViewHolder.mThumbnail.getDrawingCache() );
		mAnimationViewHolder.mThumbnail.setDrawingCacheEnabled(false);

		mBitmap = toConformBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.animation_icon),mSelectedItemBitmap);
		Intent intent = new Intent(SlideViewPager.MANAGE_FLAG_REFRESH);
		mContext.sendBroadcast(intent);
		if (mContext instanceof SortAppListActivity ||mContext instanceof SortGameListActivity){
			mListImg = MarketBrowser.img;
			
		}else{
			mListImg = SlideViewPager.img;
		}
		mListImg.setPadding(0, 0, 0, 0);
		mListImg.setBackgroundDrawable(new BitmapDrawable(mBitmap));
		mListImg.bringToFront();

        int left = ((AppInfo)v.getTag()).listItem.getLeft();
        int bottom = ((AppInfo)v.getTag()).listItem.getBottom();
        int right = ((AppInfo)v.getTag()).listItem.getRight();

        AnimationSet set =new AnimationSet(false);
        ScaleAnimation scaleAnim = new ScaleAnimation (1.0f,0.2f,1.0f,0.2f,0.5f,0.5f);
        set.addAnimation(scaleAnim);
		TranslateAnimation translateAnimationX = new TranslateAnimation(left, right, 0, 0);
		translateAnimationX.setInterpolator(new LinearInterpolator());
		translateAnimationX.setRepeatCount(0);
		TranslateAnimation translateAnimationY = new TranslateAnimation(0, 0,  bottom, 0);
		translateAnimationY.setInterpolator(new AccelerateInterpolator());
		translateAnimationY.setRepeatCount(0);
		set.addAnimation(translateAnimationX);
		set.addAnimation(translateAnimationY);
		set.setDuration(500);

		mListImg.startAnimation(set);
		//Added-e by JimmyJin
	}
	
	private Bitmap toConformBitmap(Bitmap background, Bitmap foreground) {
        if( background == null ) {   
           return null;   
        }   
        int bgWidth = background.getWidth();   
        int bgHeight = background.getHeight();   
        //int fgWidth = foreground.getWidth();   
        //int fgHeight = foreground.getHeight();   
        Bitmap newbmp = Bitmap.createBitmap(bgWidth, bgHeight, Config.ARGB_8888);  
        Canvas cv = new Canvas(newbmp);   
        //draw bg into   
        cv.drawBitmap(background, 0, 0, null);
        //draw fg into   
        cv.drawBitmap(foreground, 0, 0, null);
        //save all clip   
        cv.save(Canvas.ALL_SAVE_FLAG);
        //store   
        cv.restore(); 
        return newbmp;   
   }
}