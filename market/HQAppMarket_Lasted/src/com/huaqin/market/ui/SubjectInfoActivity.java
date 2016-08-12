package com.huaqin.market.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
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
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.huaqin.android.market.sdk.bean.Application;
import com.huaqin.android.market.sdk.bean.NewTopic;
import com.huaqin.market.R;
import com.huaqin.market.download.Constants;
import com.huaqin.market.download.DownloadManager;
import com.huaqin.market.download.Downloads;
import com.huaqin.market.download.Helpers;
import com.huaqin.market.list.SubjectListActivity;
import com.huaqin.market.model.Application2;
import com.huaqin.market.model.Image2;
import com.huaqin.market.model.Subject2;
import com.huaqin.market.utils.Constant;
import com.huaqin.market.utils.DeviceUtil;
import com.huaqin.market.utils.GlobalUtil;
import com.huaqin.market.utils.PackageUtil;
import com.huaqin.market.webservice.IMarketService;
import com.huaqin.market.webservice.MarketService;
import com.huaqin.market.webservice.Request;

public class SubjectInfoActivity extends Activity
	implements View.OnClickListener{

	private static final int ACTION_TOPIC_CONTENT = 0;
	private static final int ACTION_TOPIC_IMAGE = 1;
	private static final int ACTION_TOPIC_APP = 2;
	private static final int ACTION_TOPIC_APP_THUMB = 3;
	private static final int ACTION_UP_COMMENT_CONTENT = 4;
	private static final int ACTION_DOWN_COMMENT_CONTENT = 5;
	private static final int ACTION_NETWORK_ERROR = 6;
	private static final int ACTION_USER_PV = 7;
	private static final int PROGRESSBAR_UPDATEING = 13;
	private static final int DIALOG_NETWORK_ERROR = 100;
	
	private static final String TAG = "SubjectInfoActivity";
	
	private Cursor mCursor;
	
	private LayoutInflater mInflater; 
	private View mLoadingIndicator;
	private ImageView mLoadingAnimation;
	private AnimationDrawable loadingAnimation;
	private View mEmptyView;
	private View mSubInfoView;
	private HashMap<Integer, Application> mTopicApp;
	private HashMap<Integer, View> mTopicView;
	 private ArrayList<Application> appList;
	/*************Added-s by JimmyJin for Pudding Project**************/
	private String mFromWhere ="0";
	/*************Added-e by JimmyJin for Pudding Project**************/
	private Subject2 mSubInfo;
	private int nStartIndex;
	private int nTopicViewIndex;
	private Context mContext;
	private IMarketService mMarketService;
	private Request mCurrentRequest;
	private Handler mHandler;
	private boolean mIsReceiveMessage=false;
    private ArrayList<NewTopic> topicList;
    private final BroadcastReceiver mApplicationsInstalledReceiver;
    private DownloadingContentObserver mObserver;
    private DownloadingThread mDownloadThread;
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
	private PopupWindow popWin;
	// Constructor
	public SubjectInfoActivity() {

		mContext = this;
		nStartIndex = 0;
		nTopicViewIndex = 0;
		topicList = new ArrayList<NewTopic>();
		mTopicApp = new HashMap<Integer, Application>();
		mTopicView = new HashMap<Integer, View>();
		appList = new ArrayList<Application>();
		
		mApplicationsInstalledReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();
				Log.v("SubjectInfoActivity mApplicationsInstalledReceiver", "action ="+action);
				onResume();
			}
		};
	}

	private void initHandlers() {
		// TODO Auto-generated method stub
		
		mIsReceiveMessage=true;
		
		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch (msg.what) {
				case ACTION_TOPIC_CONTENT:
					topicList = (ArrayList<NewTopic>) msg.obj;
					int topicSize = 0;
					
					if (topicList != null) {
						topicSize = topicList.size();
						nStartIndex += topicSize;
					}
					
					initTopicViews(topicList);
					Log.v(TAG,"JimmyJin topicSize="+topicSize);
					if (topicSize == 0 || topicSize < Constant.LIST_COUNT_PER_TIME) {
					}
					
					mLoadingIndicator.setVisibility(View.GONE);
					mEmptyView.setVisibility(View.GONE);
					mSubInfoView.setVisibility(View.VISIBLE);
					
//					if (!bReachEnd) {
//						Log.v(TAG,"JimmyJin into !bReachEnd");
//						addTopicRequest();
//					}
					Log.v("asd", "ACTION_TOPIC_CONTENT");
					break;
					
				case ACTION_TOPIC_IMAGE:
					Image2 imgInfo = (Image2) msg.obj;
					if (imgInfo.mAppIcon != null) {
						View v = mTopicView.get(imgInfo._id);
						ImageView ivTopicImage = (ImageView) v.findViewById(R.id.subinfo_topic_app_thumb);
						
						ivTopicImage.setImageDrawable(imgInfo.mAppIcon);
					}
//					if (imgInfo.mAppIcon != null) {
//						View view = mTopicView.get(imgInfo._id);
////						ImageView ivTopicImage = (ImageView) view.findViewById(R.id.subinfo_topic_image);
////						
////						ivTopicImage.setImageDrawable(imgInfo.mAppIcon);
//					}
					break;
					
				case ACTION_TOPIC_APP:
					Object[] data = (Object[]) msg.obj;
					int viewIndex = ((Integer) data[0]).intValue();
					Log.v("asd", "viewIndex = "+viewIndex);
					if (data[1] != null) {
						Application app = (Application) data[1];
						appList.add(viewIndex-1, app);
						View view = mTopicView.get(viewIndex);
						if (app.getAppId() > 0) {
							mTopicApp.put(viewIndex, app);
							initTopicAppViews(viewIndex, view, app);
						}
					}
					break;
					
				case ACTION_TOPIC_APP_THUMB:
					Image2 icInfo = (Image2) msg.obj;
					
					if (icInfo.mAppIcon != null) {
//						View v = mTopicView.get(icInfo._id);
//						ImageView ivTopicImage = (ImageView) v.findViewById(R.id.subinfo_topic_app_thumb);
//						
//						ivTopicImage.setImageDrawable(icInfo.mAppIcon);
					}
					break;
				case ACTION_UP_COMMENT_CONTENT:
					Button btSupport = (Button) findViewById(R.id.sub_support);
					Button btOppose = (Button) findViewById(R.id.sub_oppose);
					btSupport.setEnabled(false);
					btOppose.setEnabled(false);
					String tmpDate;
					tmpDate = "subId"+ mSubInfo.getSubjId();
					SharedPreferences.Editor editor = 
					    	  mContext.getSharedPreferences("Recon", MODE_PRIVATE).edit();
					      editor.putInt(tmpDate, mSubInfo.getSubjId());
					      editor.commit();
					
					Toast.makeText(getApplicationContext(), R.string.add_sub_up_comment_success, Toast.LENGTH_LONG).show();
					break;
				case ACTION_DOWN_COMMENT_CONTENT:
					btSupport = (Button) findViewById(R.id.sub_support);
					btOppose = (Button) findViewById(R.id.sub_oppose);
					btSupport.setEnabled(false);
					btOppose.setEnabled(false);
					tmpDate = "subId"+ mSubInfo.getSubjId();
					editor = mContext.getSharedPreferences("Recon", MODE_PRIVATE).edit();
					      editor.putInt(tmpDate, mSubInfo.getSubjId());
					      editor.commit();
					Toast.makeText(getApplicationContext(), R.string.add_sub_down_comment_success, Toast.LENGTH_LONG).show();
					break;
				case PROGRESSBAR_UPDATEING:
					Object[] progressbarData = (Object[]) msg.obj;
//					for(int i=0;i<appList.size();i++){
//						View view = mTopicView.get(i+1);
//						Application2 appInfo = new Application2(appList.get(i));
//						Log.v("asd", "mTopicView = "+view);
//						Log.v("asd", "appInfo = "+appInfo);
//						Button rButton =  (Button)view.findViewById(R.id.subinfo_topic_app_button);
//						ProgressBar mProgressBar =  (ProgressBar)view.findViewById(R.id.subinfo_listmenu_progressbar);
//						TextView mStatus =  (TextView)view.findViewById(R.id.subinfo_listmenu_progressbar_status);
//					}
//					Log.i(TAG, "APPID=" + mAppId);

					int pViewIndex = ((Integer)progressbarData[0]).intValue();
					int progress = ((Integer)progressbarData[1]).intValue();
					Log.v("SubjectInfoActivity", "pViewIndex = "+pViewIndex);
					Log.v("SubjectInfoActivity", "progress = "+progress);	
//					mProgressBar = (ProgressBar) mListView.findViewWithTag(mAppId);	
					Log.v("SubjectInfoActivity", "mTopicView = "+mTopicView.get(pViewIndex+1));
					if(mTopicView.get(pViewIndex+1) != null){
					View view = mTopicView.get(pViewIndex+1);
					ProgressBar mProgressBar =  (ProgressBar)view.findViewById(R.id.subinfo_listmenu_progressbar);
					Log.v("SubjectInfoActivity", "mProgressBar1 = "+mProgressBar);
					if(mProgressBar != null) {
						mProgressBar.setVisibility(View.VISIBLE);
						mProgressBar.setProgress(progress);
						Log.v("SubjectInfoActivity", "mProgressBar2 = "+mProgressBar);
					}
					}
					break;
				case ACTION_NETWORK_ERROR:
					
					mEmptyView.setVisibility(View.VISIBLE);
					mLoadingIndicator.setVisibility(View.GONE);
					mSubInfoView.setVisibility(View.GONE);
					
					if(mIsReceiveMessage)
					{
						Toast.makeText(mContext, mContext.getString(R.string.error_network_low_speed), Toast.LENGTH_LONG).show();
//						showDialog(DIALOG_NETWORK_ERROR);
					}
					
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
	private void initDownloadStatus(Application2 appInfo){
		int appId = 0;
		if (appInfo.getAppId() != 0) {
			appId = appInfo.getAppId();
//			Log.d(TAG,"2getView() appId = " + app.getAppId());
		}
		String where = Downloads.WHERE_RUNNING +" and "+Downloads.WHERE_APP_ID;
		boolean downloading = DownloadManager.queryDownloadingURL(mContext, appInfo);
		int controlValue = 2;
		mCursor = mContext.getContentResolver()
				.query(Downloads.CONTENT_URI,
						mCols, where, new String[]{"" + appId}, null);
		if(mCursor != null){
			if(mCursor.moveToFirst()){
				controlValue = mCursor.getInt(controlColumn);
			}
		}
		if(mCursor!=null&&!mCursor.isClosed()){
			mCursor.close();
		}
		if(downloading) {
//			mDownloadButton.setClickable(false);
//			mAppStatus.setText(R.string.app_downloading);
			
			if(controlValue == 0) {
				appInfo.bDownloadingFlag = 1;
			} else if(controlValue == 1) {
				appInfo.bDownloadingFlag = 2;
			}
		} 
	}
	private void initTopicAppViews(int viewIndex, final View view, Application app) {
		// TODO Auto-generated method stub
		Log.v("asd", "initTopicAppViews = "+viewIndex);
		Log.v("asd", "initTopicAppViews = "+view);
		TextView tvAppTitle = (TextView) view.findViewById(R.id.subinfo_topic_app_title);
		tvAppTitle.setText(app.getAppName());
		
		TextView tvAppSize = (TextView) view.findViewById(R.id.subinfo_topic_app_size);
		tvAppSize.setText(/*getString(R.string.app_size) + */GlobalUtil.getSize(app.getSize()));
		
		TextView tvTopicContent = (TextView) view.findViewById(R.id.subinfo_topic_content);
		tvTopicContent.setText(topicList.get(viewIndex-1).getContent());
		
		RatingBar rbAppRate = (RatingBar) view.findViewById(R.id.subinfo_topic_app_rating);
		
		Button rButton =  (Button)view.findViewById(R.id.subinfo_topic_app_button);
		ProgressBar mProgressBar = (ProgressBar)view.findViewById(R.id.subinfo_listmenu_progressbar);
		mProgressBar.setTag(getProcessbarViewTag(app.getAppId()));
		mProgressBar.setVisibility(View.GONE);
		TextView mStatus = (TextView)view.findViewById(R.id.subinfo_listmenu_progressbar_status);
		mProgressBar.setVisibility(View.GONE);

		Application2 appInfo = new Application2(app);
		rButton.setTag(appInfo);
		mProgressBar.setTag(appInfo);
		initDownloadStatus(appInfo);
		if(mCursor != null){
			buttonState(rButton,mProgressBar,mStatus,appInfo,mCursor);
		}
		mProgressBar.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.v("mProgressBar", "v ="+v);
				Application2 mApp2Info = (Application2)v.getTag();
				Log.v("mProgressBar", "mApp2Info ="+mApp2Info);
				TextView mStatus = (TextView)view.findViewById(R.id.subinfo_listmenu_progressbar_status);
				Log.v("mProgressBar", "mStatus ="+mStatus);
				if(checkNetworkState()){
					Log.v("mProgressBar", "mApp2Info.bDownloadingFlag ="+mApp2Info.bDownloadingFlag);
					switch(mApp2Info.bDownloadingFlag)
					{
					case 1:
						DownloadManager.pauseDownload(mContext, new long[]{mApp2Info.getAppId()});
						mApp2Info.bDownloadingFlag=2;
						mStatus.setText(mContext.getText(R.string.app_rezume));
						break;
					case 2:
						DownloadManager.resumeDownload(mContext, new long[]{mApp2Info.getAppId()});
						mApp2Info.bDownloadingFlag=1;
						mStatus.setText(mContext.getText(R.string.app_pause));
						break;
					}				
				}
				else{
					Toast.makeText(mContext.getApplicationContext(), mContext.getText(R.string.app_downloading_without_network), Toast.LENGTH_LONG).show();
				}
			
			}});
		rButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
		    	Application2 mApp2Info = (Application2)v.getTag();
		    	Button button = (Button)v;
				ProgressBar mProgressBar = (ProgressBar)view.findViewById(R.id.subinfo_listmenu_progressbar);
				TextView mStatus = (TextView)view.findViewById(R.id.subinfo_listmenu_progressbar_status);
				
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
//								button.setCompoundDrawables(null,null,null,null);
								Log.v("onOperateClick","appInfo.bDownloading="+mApp2Info.bDownloadingFlag);
								switch(mApp2Info.bDownloadingFlag)
								{
								case 0:
									DownloadManager.startDownloadAPK(mContext, appInfo, mFromWhere);
									Toast.makeText(mContext.getApplicationContext(), mContext.getText(R.string.app_downloading_view_other_layout), Toast.LENGTH_LONG).show();
									mApp2Info.bDownloadingFlag=1;
									mProgressBar.setVisibility(View.VISIBLE);
									mStatus.setText(mContext.getText(R.string.app_pause));
									button.setText(null);
									break;
								case 1:
									DownloadManager.pauseDownload(mContext, new long[]{appInfo.getAppId()});
									mApp2Info.bDownloadingFlag=2;
									mStatus.setText(mContext.getText(R.string.app_rezume));
									button.setText(null);
									break;
								case 2:
									DownloadManager.resumeDownload(mContext, new long[]{appInfo.getAppId()});
									mApp2Info.bDownloadingFlag=1;
									mStatus.setText(mContext.getText(R.string.app_pause));
									button.setText(null);
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
					Intent intent = getPackageManager().getLaunchIntentForPackage(mApp2Info.getAppPackage());  
					if(intent != null){
						startActivity(intent); } 

				} else if(mApp2Info.bDownloadNotInstalled) {
					String packagename = mApp2Info.getAppPackage();
					String filetitle = mApp2Info.getAppName();
//					String filepath = Environment.getExternalStorageDirectory() + "/hqappmarket/apks/" + filetitle + ".apk";
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
							installapk(mContext,filepath);
						}
					}else{
						installapk(mContext,filepath);
					}	
				}else {
					long sdCardAvailSize = Helpers.getAvailaleSize();
					Log.v(TAG,"JimmyJin sdCardAvailSize678678="+sdCardAvailSize);
					if (sdCardAvailSize == -1) {
						Toast.makeText(mContext, mContext.getString(R.string.sdcard_not_amount_info), Toast.LENGTH_LONG).show();
					} else if(sdCardAvailSize < mApp2Info.getSize()) {
						Toast.makeText(mContext, mContext.getString(R.string.dlg_sdcard_size_not_enough_msg), Toast.LENGTH_LONG).show();
					} else {
						if(checkNetworkState()){
//							button.setCompoundDrawables(null,null,null,null);
							Log.v("onOperateClick","appInfo.bDownloading="+mApp2Info.bDownloadingFlag);
							switch(mApp2Info.bDownloadingFlag)
							{
							case 0:
								DownloadManager.startDownloadAPK(mContext, mApp2Info, mFromWhere);
								Toast.makeText(mContext.getApplicationContext(), mContext.getText(R.string.app_downloading_view_other_layout), Toast.LENGTH_LONG).show();
								mApp2Info.bDownloadingFlag=1;
								mProgressBar.setVisibility(View.VISIBLE);
								mStatus.setText(mContext.getText(R.string.app_pause));
								button.setText(null);
								break;
							case 1:
								DownloadManager.pauseDownload(mContext, new long[]{mApp2Info.getAppId()});
								mApp2Info.bDownloadingFlag=2;
								mStatus.setText(mContext.getText(R.string.app_rezume));
								button.setText(null);
								break;
							case 2:
								DownloadManager.resumeDownload(mContext, new long[]{mApp2Info.getAppId()});
								mApp2Info.bDownloadingFlag=1;
								mStatus.setText(mContext.getText(R.string.app_pause));
								button.setText(null);
								break;
							}				
						}
						else{
							Toast.makeText(mContext.getApplicationContext(), mContext.getText(R.string.app_downloading_without_network), Toast.LENGTH_LONG).show();
						}
					}
				}
		       	
		       	if(mCursor != null){
		       		Log.v(TAG, "buttonState  start");
		       		buttonState((Button)v,mProgressBar,mStatus,mApp2Info,mCursor);
		       		Log.v(TAG, "buttonState  end");
		       	}
			}
			
		});
		if(app.getStars() != null) {
			rbAppRate.setRating(app.getStars());
		} else {
			rbAppRate.setRating(0.0f);
		}
		
		addTopicAppThumbRequest(viewIndex, app.getAppId(), app.getIconUrl());
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
    private void installapk(Context context,String filepath){
    	File file = new File(filepath);
    	Log.v(TAG,"JimmyJin file678678="+file);
    	Intent install = new Intent(Intent.ACTION_VIEW);
		install.setDataAndType(Uri.fromFile(file), Constants.MIMETYPE_APK);
		install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(install);
    }
    private void buttonState(Button rButton,ProgressBar mProgressBar,TextView mStatus,Application2 appInfo,Cursor cursor){
    	int appStatus = PackageUtil.getApplicationStatus(mContext.getPackageManager(), appInfo.getAppPackage());
		boolean downloading = DownloadManager.queryDownloadingURL(mContext, appInfo);
		String downloadedPath = DownloadManager.queryDownloadedURL(mContext, appInfo);
		if(downloadedPath != null) {
			appInfo.downloadedAppFile = new File(downloadedPath);
		}
		Log.v("asd", "appStatus="+appStatus);
		switch (appStatus) {
		case PackageUtil.PACKAGE_NOT_INSTALLED:
			appInfo.bUpdateAvailable = false;
			appInfo.bInstalled = false;
			if(appInfo.downloadedAppFile != null){
				Log.v("asd", "downloadedAppFile = "+appInfo.downloadedAppFile.exists());
			}
			Drawable drawable = getResources().getDrawable(R.drawable.ic_btn_applist);
			drawable.setBounds(0, 0,rButton.getWidth(), rButton.getHeight());
			rButton.setBackgroundDrawable(drawable);
			if(downloading) {
				rButton.setClickable(true);
//				viewHolder.mButton.setCompoundDrawables(null,null,null,null);
				if(cursor != null){
					int progress = DownloadManager.queryDownloadingProgressURL(mContext,appInfo);
					Log.v("initBtnStatus", "progress ="+progress);
					if(mProgressBar != null){
						mProgressBar.setProgress(progress);
						mProgressBar.setVisibility(View.VISIBLE);
						rButton.setVisibility(View.GONE);
					}
					
				}
				
				if(appInfo.bDownloadingFlag == 2){
					if(mStatus != null){
						mStatus.setVisibility(View.VISIBLE);
						mStatus.setText(mContext.getText(R.string.app_rezume));
						rButton.setText(null);
					}
					
				}if(appInfo.bDownloadingFlag == 1){
					if(mStatus != null){
						mStatus.setVisibility(View.VISIBLE);
						mStatus.setText(mContext.getText(R.string.app_pause));
						rButton.setText(null);
					}
				}
				
			} else if(appInfo.downloadedAppFile != null && appInfo.downloadedAppFile.exists()) {
				rButton.setClickable(true);
				rButton.setEnabled(true);
				rButton.setVisibility(View.VISIBLE);
				mProgressBar.setVisibility(View.GONE);
				mStatus.setVisibility(View.GONE);
				appInfo.bDownloadNotInstalled = true;
				rButton.setText(mContext.getText(R.string.app_install));
			} else {
				rButton.setVisibility(View.VISIBLE);
				mProgressBar.setVisibility(View.GONE);
				mStatus.setVisibility(View.GONE);
				rButton.setClickable(true);
				rButton.setText(mContext.getText(R.string.btn_appinfo_download));
			}
			break;
			
		case PackageUtil.PACKAGE_UPDATE_AVAILABLE:
//			Drawable drawable = getResources().getDrawable(R.drawable.btn_subinfo_update);
			appInfo.bUpdateAvailable = true;
			appInfo.bInstalled = false;
			rButton.setClickable(true);

			mProgressBar.setVisibility(View.GONE);
			mStatus.setVisibility(View.GONE);
//			drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
			rButton.setText(mContext.getText(R.string.app_update_available));
			drawable = getResources().getDrawable(R.drawable.ic_btn_update);
			drawable.setBounds(0, 0,rButton.getWidth(), rButton.getHeight());
			rButton.setBackgroundDrawable(drawable);
//			rButton.setCompoundDrawables(drawable,null,null,null);
			if(appInfo.downloadedAppFile != null && appInfo.downloadedAppFile.exists()) {
//				mDownloadButton.setClickable(true);
				appInfo.bUpdateAvailable = false;
				appInfo.bDownloadNotInstalled = true;
				rButton.setVisibility(View.VISIBLE);
				rButton.setClickable(true);
				rButton.setEnabled(true);
				rButton.setText(R.string.app_install);
			}
			break;
			
		case PackageUtil.PACKAGE_INSTALLED:
			appInfo.bUpdateAvailable = false;
			appInfo.bInstalled = true;
			rButton.setVisibility(View.VISIBLE);
			mProgressBar.setVisibility(View.GONE);
			mStatus.setVisibility(View.GONE);
			rButton.setClickable(true);
			rButton.setEnabled(true);
//			rButton.setBackgroundResource(R.drawable.btn_subinfo_open);
			rButton.setText(mContext.getText(R.string.app_open));
			drawable = getResources().getDrawable(R.drawable.ic_btn_open);
			drawable.setBounds(0, 0,rButton.getWidth(), rButton.getHeight());
			rButton.setBackgroundDrawable(drawable);
			break;
			
		default:
			break;
		}
    }
	private void initTopicViews(ArrayList<NewTopic> topicList) {
		// TODO Auto-generated method stub
		View topicView = null;
		NewTopic topic = null;
		LinearLayout topicContent = (LinearLayout) mSubInfoView.findViewById(R.id.subinfo_content);
		Log.v("asd", "topicList = "+topicList);
		Log.v("asd", "topicList = "+topicList.size());
		for (int i = 0; i < topicList.size(); i++) {
			topicView = mInflater.inflate(R.layout.subinfo_topic, null);
			topic = topicList.get(i);
			
			if (topic == null) {
				continue;
			}
			nTopicViewIndex++;
			Log.v("asd", "topic = "+topic);
//			TextView tvTopicId = (TextView) topicView.findViewById(R.id.subinfo_topic_id);
//			tvTopicId.setText(String.valueOf(nTopicViewIndex));
//			TextView tvTopicTitle = (TextView) topicView.findViewById(R.id.subinfo_topic_title);
//			tvTopicTitle.setText(topic.getTitle());
//			TextView tvTopicContent = (TextView) appView.findViewById(R.id.subinfo_topic_content);
//			Log.v("asd", "tvTopicContent = "+tvTopicContent);
//			Log.v("asd", "topic.getContent() = "+topic.getContent());
//			tvTopicContent.setText(topic.getContent());
			if (topic.getIllustrations() != null) {
				String imgUrl = topic.getIllustrations();
				addTopicImageRequest(nTopicViewIndex, imgUrl);
			}
			// add on-click listener for app view 
			View appView = topicView.findViewById(R.id.subinfo_topic_app_layout);
			subItem subItem = new subItem();
			subItem.position = i;
			subItem.appId = topic.getAppId();
			appView.setTag(subItem);
			appView.setOnClickListener(this);
			addTopicAppRequest(nTopicViewIndex, topic.getAppId());
			Log.v("asd", "mTopicView = "+mTopicView);
			Log.v("asd", "topicContent = "+topicContent);
			mTopicView.put(nTopicViewIndex, topicView);
			topicContent.addView(topicView);
		}
	}

	private void addTopicAppThumbRequest(int viewIndex, int appId, String iconUrl) {
		// TODO Auto-generated method stub
		Request request = new Request(0, Constant.TYPE_SUBS_TOPIC_THUMB);
		Object[] params = new Object[3];
		
		params[0] = Integer.valueOf(viewIndex);
		params[1] = iconUrl;
		params[2] = Integer.valueOf(appId);
		request.setData(params);
		request.addObserver(new Observer() {

			@Override
			public void update(Observable observable, Object data) {
				// TODO Auto-generated method stub
				if (data != null) {
					Message msg = Message.obtain(mHandler, ACTION_TOPIC_APP_THUMB, data);
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
		mMarketService.getAppIcon(request);
	}
	private void registerIntentReceivers() {
    	// TODO Auto-generated method stub	
		IntentFilter intentInstalledFilter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
		intentInstalledFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		intentInstalledFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		intentInstalledFilter.addDataScheme("package");
		registerReceiver(mApplicationsInstalledReceiver, intentInstalledFilter);
	}
	private void unregisterIntentReceivers() {
		// TODO Auto-generated method stub
		unregisterReceiver(mApplicationsInstalledReceiver); 
	}
	private void addTopicImageRequest(int viewIndex, String imgUrl) {
		// TODO Auto-generated method stub
		Request request = new Request(0, Constant.TYPE_SUBS_TOPIC_IMAGE);
		Object[] params = new Object[2];
		
		params[0] = Integer.valueOf(viewIndex);
		params[1] = imgUrl;
		request.setData(params);
		request.addObserver(new Observer() {

			@Override
			public void update(Observable observable, Object data) {
				// TODO Auto-generated method stub
				if (data != null) {
					Message msg = Message.obtain(mHandler, ACTION_TOPIC_IMAGE, data);
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
		mMarketService.getTopicImage(request);
	}

	private void addTopicAppRequest(int viewIndex, int appId) {
		// TODO Auto-generated method stub
		Request request = new Request(0, Constant.TYPE_SUBS_TOPIC_APP);
		Object[] params = new Object[2];
		Log.v("asd", "params = "+params);
		params[0] = Integer.valueOf(viewIndex);
		params[1] = Integer.valueOf(appId);
		Log.v("asd", "params0 = "+params[0]);
		Log.v("asd", "params1 = "+params[1]);
		request.setData(params);
		request.addObserver(new Observer() {

			@Override
			public void update(Observable observable, Object data) {
				// TODO Auto-generated method stub
				if (data != null) {
					Message msg = Message.obtain(mHandler, ACTION_TOPIC_APP, data);
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
		Log.v(TAG,"JimmyJin into addTopicAppRequest_TYPE_SUBS_TOPIC_APP!");
		mMarketService.getTopicApp(request);
	}
	private void addTopicRequest() {
		Log.v(TAG,"JimmyJin into addTopicRequest!");
		// TODO Auto-generated method stub
		Request request = new Request(0, Constant.TYPE_SUBJECT_TOPIC_LIST);
		Object[] params = new Object[2];
		
		params[0] = Integer.valueOf(mSubInfo.getSubjId());
		params[1] = Integer.valueOf(nStartIndex);
		request.setData(params);
		request.addObserver(new Observer() {

			@Override
			public void update(Observable observable, Object data) {
				// TODO Auto-generated method stub
				if (data != null) {
					Message msg = Message.obtain(mHandler, ACTION_TOPIC_CONTENT, data);
					mHandler.sendMessage(msg);
					Log.v("asd", "ACTION_TOPIC_CONTENT2");
				} else {
					Request request = (Request) observable;
					if (request.getStatus() == Constant.STATUS_ERROR) {
						mHandler.sendEmptyMessage(ACTION_NETWORK_ERROR);
					}
				}
			}
		});
		
		mCurrentRequest = request;
		mMarketService.getTopicList(request);
	}

	private void initViews() {
		// TODO Auto-generated method stub
		StringBuilder builder = new StringBuilder();
		
		TextView tvSubTitle = (TextView) findViewById(R.id.subinfo_title);
		tvSubTitle.setText(mSubInfo.getTitle());
		
		TextView tvDate = (TextView) findViewById(R.id.subinfo_date);
		builder.append(getString(R.string.sub_date));
		builder.append(GlobalUtil.getDateByFormat(mSubInfo.getReleaseDate(), "yyyy-MM-dd"));
		tvDate.setText(builder.toString());
		
		Button btSupport = (Button) findViewById(R.id.sub_support);
		Button btOppose = (Button) findViewById(R.id.sub_oppose);
		String strSupport = getString(R.string.sub_support_tx)+"("+mSubInfo.getUp()+")";
		String strOppose = getString(R.string.sub_oppose_tx)+"("+mSubInfo.getDown()+")";
		
		btSupport.setText(strSupport);
		btOppose.setText(strOppose);
		
		String tmpDate = "subId"+ mSubInfo.getSubjId();
		Log.v("asd", "getSubjId = "+mSubInfo.getSubjId());
		Log.v("asd", "this = "+tmpDate);
		SharedPreferences sharedPreferences = getSharedPreferences("Recon", 0);
		int mSubId = sharedPreferences.getInt(tmpDate, -1);
		Log.v("asd", "mSubId = "+mSubId);
		if(mSubId != -1){
			btSupport.setEnabled(false);
			btOppose.setEnabled(false);
		}
		
		btSupport.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(true){
				Request request = new Request(0, Constant.TYPE_ADD_FAVORITE);
				Object[] params = new Object[3];
				params[0] = Integer.toString(mSubInfo.getSubjId());
				params[1] = "1";
				params[2] = "0";
				Button btSupport = (Button) findViewById(R.id.sub_support);
				Button btOppose = (Button) findViewById(R.id.sub_oppose);
				String strSupport = getString(R.string.sub_support_tx)+"("+(mSubInfo.getUp()+1)+")";
				btSupport.setText(strSupport);

				final int[] location = new int[2];  
				btSupport.getLocationOnScreen(location); 
				View popLayout = getLayoutInflater().inflate(R.layout.pop_layout, null);
//				if (popWin != null) {
//					popWin.dismiss();
//				}
				popWin = new PopupWindow(popLayout, 50, 50);
				popWin.showAtLocation(popLayout, Gravity.TOP | Gravity.CENTER_HORIZONTAL,
						location[0]-btSupport.getWidth(), location[1]-btSupport.getHeight());
				btSupport.setEnabled(false);
				btOppose.setEnabled(false);
				request.setData(params);
				request.addObserver(new Observer() {
	
					@Override
					public void update(Observable observable, Object data) {
						Log.v("SubjectInfoActivity","data="+data);
						// TODO Auto-generated method stub
						if (data != null) {
							Message msg = Message.obtain(mHandler, ACTION_UP_COMMENT_CONTENT, data);
						//	msg.obj = Integer.valueOf(appId);
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
				mMarketService.addAppComment(request);
		}		
	}
	});
		
		btOppose.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(true){
				Request request = new Request(0, Constant.TYPE_ADD_FAVORITE);
				Object[] params = new Object[3];
				params[0] = Integer.toString(mSubInfo.getSubjId());
				params[1] = "0";
				params[2] = "1";
				Button btSupport = (Button) findViewById(R.id.sub_support);
				Button btOppose = (Button) findViewById(R.id.sub_oppose);
				String strOppose = getString(R.string.sub_oppose_tx)+"("+(mSubInfo.getDown()+1)+")";
				btOppose.setText(strOppose);
				btSupport.setEnabled(false);
				btOppose.setEnabled(false);

				final int[] location1 = new int[2];  
				btOppose.getLocationOnScreen(location1); 
				View popLayout = getLayoutInflater().inflate(R.layout.pop_layout, null);
				if (popWin != null) {
					popWin.dismiss();
				}
				popWin = new PopupWindow(popLayout, 30, 50);
				popWin.showAtLocation(popLayout, Gravity.TOP | Gravity.CENTER_HORIZONTAL,
						location1[0]-btOppose.getWidth(), location1[1]-btOppose.getHeight());
				request.setData(params);
				request.addObserver(new Observer() {
	
					@Override
					public void update(Observable observable, Object data) {
						Log.v("SubjectInfoActivity1","data="+data);
						// TODO Auto-generated method stub
						if (data != null) {
							Message msg = Message.obtain(mHandler, ACTION_DOWN_COMMENT_CONTENT, data);
						//	msg.obj = Integer.valueOf(appId);
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
				mMarketService.addAppComment(request);
//			if(mCurrentRequest.getStatus() != Constant.STATUS_SUCCESS){
//				
//			}
		}		
	}
			
		});
		
		builder.setLength(0);
		TextView tvVisit = (TextView) findViewById(R.id.subinfo_visit);
		builder.append(getString(R.string.sub_visit));
		if (mSubInfo.getReads() != null) {
			builder.append(mSubInfo.getReads());
		} else {
			builder.append(0);
		}
		tvVisit.setText(builder.toString());
		
		if (mSubInfo.isHot()) {
			ImageView ivHot = (ImageView) findViewById(R.id.subinfo_hot);
			ivHot.setVisibility(View.VISIBLE);
		}
		
		TextView tvSubDesc = (TextView) findViewById(R.id.subinfo_desc);
		tvSubDesc.setText(mSubInfo.getDescription());
		
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
		mSubInfoView = findViewById(R.id.subinfo_scrollview_layout);
		mEmptyView.setVisibility(View.GONE);
		mSubInfoView.setVisibility(View.GONE);
		
		addTopicRequest();
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
		            int i=0;
		            if(progress < max && max > 0 && (progress * 100) > max) {
		            	Object[] data = new Object[2];
		            	if(appList != null){
		            	for(i=0;i<appList.size();i++){
		            		if(appList.get(i).getAppId() == appId){
		            			int index = 0;
		            			index = i;
		            			data[0] = new Integer(index);
				            	data[1] = new Integer(progress * 100 / max);
				            	Log.v("updateProgressBar", "data[0] ="+data[0]);
				            	Message msg = Message.obtain(mHandler, PROGRESSBAR_UPDATEING, data);
								mHandler.sendMessage(msg);
		            		}
		            	}
		            	}
		            }
		        }
	        }finally {
	        	cursor.close();
	        }
		}
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		mMarketService = MarketService.getServiceInstance(this);
		mSubInfo = (Subject2) getIntent().getSerializableExtra("subInfo");
		
		mInflater = LayoutInflater.from(this);
		
		setContentView(R.layout.subinfo_main);
		mObserver = new DownloadingContentObserver();
        getContentResolver().registerContentObserver(Downloads.CONTENT_URI,
                true, mObserver);
        registerIntentReceivers();
		initHandlers();
		initViews();
		
//		try {
//			postPV("专题详情");
//		} catch (NameNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		unregisterIntentReceivers();
		super.onDestroy();
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
						case Constant.TYPE_SUBJECT_TOPIC_LIST:
							mMarketService.getTopicList(mCurrentRequest);
							break;
						case Constant.TYPE_SUBS_TOPIC_IMAGE:
							mMarketService.getTopicImage(mCurrentRequest);
							break;
						case Constant.TYPE_SUBS_TOPIC_APP:
							Log.v(TAG,"JimmyJin into onCreateDialog_TYPE_SUBS_TOPIC_APP!");
							mMarketService.getTopicApp(mCurrentRequest);
							break;
						case Constant.TYPE_SUBS_TOPIC_THUMB:
							mMarketService.getAppIcon(mCurrentRequest);
							break;
						default:
							break;
						}
						if (mCurrentRequest != null) {
							mMarketService.getTopicList(mCurrentRequest);
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

	@Override
	public void onClick(View v) {
		Intent intent = new Intent(this, AppInfoActivity.class);
		subItem subItem = (subItem)v.getTag();
		intent.putExtra("appInfo",new Application2(appList.get(subItem.position)));
		intent.putExtra("appId", (Integer)subItem.appId);
		/*************Added-s by JimmyJin for Pudding Project**************/
		intent.putExtra("type",0);
		/*************Added-s by JimmyJin for Pudding Project**************/
//		intent.putExtra("download", mAppListAdapter.getItem((Integer)v.getTag()).getTotalDownloads());
		startActivity(intent);
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub		
		mIsReceiveMessage=false;
		super.onStop();
	}	
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		mIsReceiveMessage=false;
		super.onPause();
//		if (popWin != null) {
//			popWin.dismiss();
//		}
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		mIsReceiveMessage=true;
		for(int i=0;i<appList.size();i++){
			View view = mTopicView.get(i+1);
			Application2 appInfo = new Application2(appList.get(i));
			Log.v("asd", "mTopicView = "+view);
			Log.v("asd", "appInfo = "+appInfo);
			Button rButton =  (Button)view.findViewById(R.id.subinfo_topic_app_button);
			ProgressBar mProgressBar =  (ProgressBar)view.findViewById(R.id.subinfo_listmenu_progressbar);
			TextView mStatus =  (TextView)view.findViewById(R.id.subinfo_listmenu_progressbar_status);
			if(mCursor != null){
				buttonState(rButton,mProgressBar,mStatus,appInfo,mCursor);
			}
			initTopicAppViews(i+1, view, appList.get(i));
		}
		
		super.onResume();
	}
	class subItem{
		private int position;
		private int appId;
	}
}