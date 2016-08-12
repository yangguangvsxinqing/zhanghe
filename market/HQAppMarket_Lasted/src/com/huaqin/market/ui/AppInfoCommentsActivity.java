package com.huaqin.market.ui;

import java.util.ArrayList;
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
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.huaqin.android.market.sdk.bean.Comment;
import com.huaqin.market.R;
import com.huaqin.market.SlideViewPager;
import com.huaqin.market.model.Application2;
import com.huaqin.market.model.Comment2;
import com.huaqin.market.utils.Constant;
import com.huaqin.market.utils.DeviceUtil;
import com.huaqin.market.utils.GlobalUtil;
import com.huaqin.market.utils.PackageUtil;
import com.huaqin.market.webservice.IMarketService;
import com.huaqin.market.webservice.MarketService;
import com.huaqin.market.webservice.Request;

public class AppInfoCommentsActivity extends Activity {

	private static final int ACTION_LIST_COMMENTS = 0;
	private static final int ACTION_NETWORK_ERROR = 1;
	private static final int ACTION_REFRESH_COMMENTS = 2;
	
	private static final int DIALOG_NETWORK_ERROR = 100;
	private static final int DIALOG_APP_NOT_INSTALL = 101;
	
	public static final String ADD_COMMENT = "broadcast_search_request";
	public static final String ADD_COMMENT_REFLASH = "broadcast_search_reflash";
	
	private boolean bBusy;
	private boolean bReachEnd;
	private boolean bInflatingCommentsList;
	private int nStartIndex;
	private String clientId;
	
	private View mLoadingIndicator;
	private ImageView mLoadingAnimation;
	private AnimationDrawable loadingAnimation;
	private ListView mListView;
	private CommentsAdapter mListAdapter;
	private View mFooterView;
	private View mLoadingIndicatorSmall;
	private View emptyView; 

	private Context mContext;
	private View mFooterErrorView;
	private Button mBtnAddComment;
	private Button mBtnRefresh;
	private TextView textAddComment;
	
	private Application2 mAppInfo;
	
	private IMarketService mMarketService;
	private Request mCurrentRequest;
	private Handler mHandler;
	private AbsListView.OnScrollListener mScrollListener;
	private View.OnClickListener addOnClickListener;
	private View.OnClickListener refreshOnClickListener;
	
	private final BroadcastReceiver mApplicationsAddCommentReceiver;
	private final BroadcastReceiver mApplicationsCommentInstalledReceiver;
	private final BroadcastReceiver mApplicationsFreeReceiver;

	public AppInfoCommentsActivity() {

		nStartIndex = 0;
		bBusy = false;
		bReachEnd = false;
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
					
					// as list scrolled to end, send request to get above data
					if ((start + counts) >= (mListAdapter.getCount() - 2)) {
						inflateCommentsList();
					}
					break;
				case SCROLL_STATE_TOUCH_SCROLL:
				case SCROLL_STATE_FLING:
					if (!bBusy) {
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
		
		// add comment, if app is not installed, show dialog notify user.
		addOnClickListener = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int appStatus =
					PackageUtil.getApplicationStatus(getPackageManager(), mAppInfo.getAppPackage());
				mBtnAddComment.setEnabled(false);
				mBtnAddComment.setClickable(false);
				switch (appStatus) {
				case PackageUtil.PACKAGE_NOT_INSTALLED:
					showDialog(DIALOG_APP_NOT_INSTALL);
					break;
				case PackageUtil.PACKAGE_INSTALLED:
				case PackageUtil.PACKAGE_UPDATE_AVAILABLE:
					Intent addCommentIntent = new Intent(AppInfoCommentsActivity.this, AddCommentDetailActivity.class);
					addCommentIntent.putExtra("appId", mAppInfo.getAppId());
					startActivity(addCommentIntent);
					break;
					
				default:
					break;
				}
			}
		};
		
		mApplicationsAddCommentReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
					refreshCommentsList();
			}
		};					
		mApplicationsCommentInstalledReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				final String action = intent.getAction();
				if(action.equals(ADD_COMMENT_REFLASH)){
					Log.v("asd", "mApplicationsInstalledReceiver ACTION_PACKAGE_ADDED");
					mBtnAddComment = (Button)findViewById(R.id.btn_post_comment);
					mBtnRefresh = (Button)findViewById(R.id.btn_refresh_comment);
					textAddComment = (TextView)findViewById(R.id.add_comment_expression);
					textAddComment.setVisibility(View.GONE);
					mBtnAddComment.setVisibility(View.VISIBLE);
					mBtnRefresh.setVisibility(View.VISIBLE);
				}
			}
		};
		mApplicationsFreeReceiver = new BroadcastReceiver(){

			@Override
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();
				if(action.equals(AppInfoActivity.ACTION_DETAIL_FREE)){	
					onDestroy();
				}
			}
		};

		refreshOnClickListener = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				refreshCommentsList();
			}
		};
		
		// report fault
//		faultOnClickListener = new View.OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				
//			}
//		};
	}

	private void initHandler() {
		// TODO Auto-generated method stub
		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				Log.v("AppInfoCommentsActivity", "handleMessage ="+msg.what);
				switch (msg.what) {
				case ACTION_LIST_COMMENTS:
					@SuppressWarnings("unchecked")
					ArrayList<Comment> commentList = (ArrayList<Comment>) msg.obj;
					int listSize = 0;
					int listIndex = 0;
					
					if (commentList != null) {
						listSize = commentList.size();
						nStartIndex += listSize;
					}
					if (mListAdapter == null) {
						mListAdapter = new CommentsAdapter(
								AppInfoCommentsActivity.this,
								commentList);
						mListView.setAdapter(mListAdapter);
					} else {
						for (; listIndex < listSize; listIndex++) {
							mListAdapter.add(commentList.get(listIndex));
						}
						if (listIndex >= listSize && listIndex != 0) {
							mListAdapter.notifyDataSetChanged();
						}
					}
					
					if (listSize == 0 || listSize < Constant.LIST_COUNT_PER_TIME) {
						bReachEnd = true;
						if (mListView.getFooterViewsCount() > 0) {
							mListView.removeFooterView(mFooterView);
						}
					}
					
					mLoadingIndicator.setVisibility(View.GONE);
					mListView.setVisibility(View.VISIBLE);
					bInflatingCommentsList = false;
					
					break;
				case ACTION_REFRESH_COMMENTS:
					if(mListAdapter != null){
						mListAdapter.clear();
					}
					@SuppressWarnings("unchecked")
					ArrayList<Comment> refreshComments = (ArrayList<Comment>) msg.obj;
					int commentsSize = 0;
					Log.v("AppInfoCommentsActivity", "refreshComments ="+refreshComments.size());
					if (refreshComments != null) {
						commentsSize = refreshComments.size();
						nStartIndex = commentsSize;
					}
					
					for(Comment comm : refreshComments) {
						mListAdapter.add(comm);
					}
					if (commentsSize == 0 || commentsSize < Constant.LIST_COUNT_PER_TIME) {
						bReachEnd = true;
						if (mListView.getFooterViewsCount() > 0) {
							mListView.removeFooterView(mFooterView);
						}
					}
					
					mLoadingIndicator.setVisibility(View.GONE);
					mListView.setVisibility(View.VISIBLE);
					bInflatingCommentsList = false;
					break;
					
				case ACTION_NETWORK_ERROR:
					Log.v("AppInfoCommentsActivity", "ACTION_REFRESH_COMMENTS nStartIndex ="+nStartIndex);
					if (nStartIndex == 0) {
						// Show network error dialog
						Log.v("AppInfoCommentsActivity", "ACTION_REFRESH_COMMENTS mLoadingIndicator ="+mLoadingIndicator);
						Log.v("AppInfoCommentsActivity", "ACTION_REFRESH_COMMENTS mListView ="+mListView);
						Log.v("AppInfoCommentsActivity", "ACTION_REFRESH_COMMENTS mContext ="+mContext);
						mLoadingIndicator.setVisibility(View.GONE);
						mListView.setVisibility(View.VISIBLE);
						Toast.makeText(mContext, mContext.getString(R.string.error_network_low_speed), Toast.LENGTH_LONG).show();
//						showDialog(DIALOG_NETWORK_ERROR);
					} else {
						// Show retry button in footer view
						Log.v("AppInfoCommentsActivity", "ACTION_REFRESH_COMMENTS mLoadingIndicatorSmall ="+mLoadingIndicatorSmall);
						Log.v("AppInfoCommentsActivity", "ACTION_REFRESH_COMMENTS mFooterErrorView ="+mFooterErrorView);
						if(mLoadingIndicatorSmall != null){
							mLoadingIndicatorSmall.setVisibility(View.GONE);
						}
						
						mFooterErrorView.setVisibility(View.VISIBLE);
					}
					break;
					
				default:
					break;
				}
			}
		};
	}
	private void registerIntentReceivers() {
		// TODO Auto-generated method stub
		IntentFilter intentFilter = new IntentFilter(ADD_COMMENT);
		Log.v("asd", "asd intentFilter="+intentFilter);
		registerReceiver(mApplicationsAddCommentReceiver, intentFilter);
		

		IntentFilter intentInstalledFilter = new IntentFilter(ADD_COMMENT_REFLASH);
		Log.v("asd", "asd intentInstalledFilter="+intentInstalledFilter);
		registerReceiver(mApplicationsCommentInstalledReceiver, intentInstalledFilter);
		
		IntentFilter intentFilterFree = new IntentFilter(AppInfoActivity.ACTION_DETAIL_FREE);
		registerReceiver(mApplicationsFreeReceiver, intentFilterFree);
	}
	
	private void unregisterIntentReceivers() {
		// TODO Auto-generated method stub
		unregisterReceiver(mApplicationsAddCommentReceiver); 
		unregisterReceiver(mApplicationsCommentInstalledReceiver); 
		unregisterReceiver(mApplicationsFreeReceiver);
		
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
		
		mListView = (ListView) findViewById(R.id.list_app_comments);
		mListView.setScrollbarFadingEnabled(true);
		
		mFooterView = LayoutInflater.from(this).inflate(R.layout.app_list_footer, null);
		ProgressBar loadingSmall =
			(ProgressBar) mFooterView.findViewById(R.id.small_loading);
		LoadingAnimation aniSmall = new LoadingAnimation(
				this,
				LoadingAnimation.SIZE_SMALL,
				0, 0, LoadingAnimation.DEFAULT_DURATION);
		loadingSmall.setIndeterminateDrawable(aniSmall);
		mFooterErrorView = mFooterView.findViewById(R.id.error_footer);
		Button btn_Retry = (Button) mFooterView.findViewById(R.id.btn_retry);
		btn_Retry.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mCurrentRequest != null) {
					mLoadingIndicatorSmall.setVisibility(View.VISIBLE);
					mFooterErrorView.setVisibility(View.GONE);
					mMarketService.getAppComments(mCurrentRequest);
				}
			}
		});
		mListView.addFooterView(mFooterView);
		
		// if app package not install, show text, else show add comment button 
		int appStatus =
			PackageUtil.getApplicationStatus(getPackageManager(), mAppInfo.getAppPackage());
		mBtnAddComment = (Button)findViewById(R.id.btn_post_comment);
		mBtnRefresh = (Button)findViewById(R.id.btn_refresh_comment);
		textAddComment = (TextView)findViewById(R.id.add_comment_expression);
		if(appStatus == PackageUtil.PACKAGE_NOT_INSTALLED) {
			mBtnAddComment.setVisibility(View.GONE);
			mBtnRefresh.setVisibility(View.GONE);
			textAddComment.setVisibility(View.VISIBLE);
		}
		
		emptyView = findViewById(R.id.list_app_comments_empty);
		mListView.setEmptyView(emptyView);
		
		mListView.setOnScrollListener(mScrollListener);
		
		inflateCommentsList();
		
		mBtnAddComment.setOnClickListener(addOnClickListener);
		mBtnRefresh.setOnClickListener(refreshOnClickListener);
		
	}

	private void inflateCommentsList() {
		// TODO Auto-generated method stub
		if (bReachEnd || bInflatingCommentsList) {
			return;
		}
		
		Request request = new Request(0, Constant.TYPE_APP_COMMENT_LIST);
		Object[] params = new Object[2];
		
		params[0] = Integer.valueOf(mAppInfo.getAppId());
		params[1] = Integer.valueOf(nStartIndex);
		request.setData(params);
		request.addObserver(new Observer() {

			@Override
			public void update(Observable observable, Object data) {
				// TODO Auto-generated method stub
				if (data != null) {
					Message msg = Message.obtain(mHandler, ACTION_LIST_COMMENTS, data);
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
		mMarketService.getAppComments(request);
	}
	
	// refresh comments
	private void refreshCommentsList() {
		mListView.setVisibility(View.GONE);
		mLoadingIndicator.setVisibility(View.VISIBLE);
		Request request = new Request(0, Constant.TYPE_APP_COMMENT_LIST);
		Object[] params = new Object[2];
		
		params[0] = Integer.valueOf(mAppInfo.getAppId());
		params[1] = Integer.valueOf(0);
		request.setData(params);
		request.addObserver(new Observer() {

			@Override
			public void update(Observable observable, Object data) {
				// TODO Auto-generated method stub
				if (data != null) {
					Message msg = Message.obtain(mHandler, ACTION_REFRESH_COMMENTS, data);
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
		mMarketService.getAppComments(request);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		mAppInfo = ((AppInfoActivity) getParent()).getAppInfo();
		mMarketService = MarketService.getServiceInstance(this);
		String ranClientId = DeviceUtil.getClientId();
		SharedPreferences sharedPreferences = this.getPreferences(MODE_WORLD_WRITEABLE);
		clientId = sharedPreferences.getString("clientId", ranClientId);
		if(clientId.equals(ranClientId)) {
			sharedPreferences.edit().putString("clientId", clientId).commit();
		}
		
		setContentView(R.layout.appinfo_comments);
		initHandler();
		initListView();
		registerIntentReceivers();
	}
	
	

	@Override
	protected void onRestart() {
		super.onRestart();
		refreshCommentsList();
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.v("AppInfoCommentsActivity", "onDestroy");
		unregisterIntentReceivers();
		if (mListAdapter != null) {
			mListAdapter.clear();
		}
		if(mListView != null){
			mListView.setAdapter(null);
		}
	}
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_NETWORK_ERROR:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.dlg_network_error_title)
				.setMessage(R.string.dlg_network_error_msg)
				.setPositiveButton(R.string.btn_retry, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if (mCurrentRequest != null) {
							mMarketService.getAppPreviews(mCurrentRequest);
						}
					}
				})
				.setNegativeButton(R.string.btn_cancel, null);
			return builder.create();
		case DIALOG_APP_NOT_INSTALL:
			builder = new AlertDialog.Builder(this);
			builder.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.dlg_network_error_title)
				.setMessage(R.string.dlg_network_error_msg)
				.setPositiveButton(R.string.btn_retry, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if (mCurrentRequest != null) {
							mMarketService.getAppPreviews(mCurrentRequest);
						}
					}
				})
				.setNegativeButton(R.string.btn_cancel, null);
			return builder.create();
		}
		return null;
	}

	private class CommentsAdapter extends ArrayAdapter<Comment> {

		private LayoutInflater mInflater;
		private String mDeviceId;
		private String mSubsId;
		private String mClientId;
		
		private Context mContext;
		
		private View.OnClickListener updateOnClickListener;

		public CommentsAdapter(Context context, ArrayList<Comment> objects) {
			super(context, 0, objects);
			// TODO Auto-generated constructor stub
			mInflater = 
				(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mContext = getApplicationContext();
			mDeviceId = DeviceUtil.getIMEI(mContext);
			mSubsId = DeviceUtil.getIMSI(mContext);
			mClientId = clientId;
			updateOnClickListener = new UpdateOnClickListener();
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return super.getItemId(position);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			Comment comment = getItem(position);
			String nickName = comment.getNickname();
			ViewHolder viewHolder = null; 
			
			if (convertView == null || 
					!(convertView.getTag() instanceof ViewHolder)) {
				convertView = mInflater.inflate(R.layout.app_list_comment_item, null);
				viewHolder = new ViewHolder();
				
				viewHolder.mCommentAuthor =
					(TextView) convertView.findViewById(R.id.app_listitem_comment_author);
				viewHolder.mCommentDate =
					(TextView) convertView.findViewById(R.id.app_listitem_comment_date);
				viewHolder.mCommentContent =
					(TextView) convertView.findViewById(R.id.app_listitem_comment_content);
				viewHolder.mRatingBar = 
					(RatingBar) convertView.findViewById(R.id.app_listitem_comment_stars);
				viewHolder.mButtonEdit = 
					(Button) convertView.findViewById(R.id.app_listitem_comment_btn_edit);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			
			if((mDeviceId != null && mDeviceId.equals(comment.getDeviceId())) || 
					(mSubsId != null && mSubsId.equals(comment.getSubscriberId())) || 
						(mClientId != null && mClientId.equals(comment.getClientId()))) {
//				viewHolder.mButtonEdit.setVisibility(View.VISIBLE);
//				viewHolder.mButtonEdit.setTag(new Integer(position));
//				viewHolder.mButtonEdit.setOnClickListener(updateOnClickListener);
			} else {
//				viewHolder.mButtonEdit.setVisibility(View.INVISIBLE);
			}
			
			if(nickName != null && !"".equals(nickName.trim())) {
				viewHolder.mCommentAuthor.setText(nickName + getString(R.string.text_comment));
			} else { 
				viewHolder.mCommentAuthor.setText(getString(R.string.text_anonymity_user) + getString(R.string.text_comment));
			}
			if (comment.getCreateTime() != null) {
				viewHolder.mCommentDate.setText(
						GlobalUtil.getDateByFormat(comment.getCreateTime(), "yyyy-MM-dd HH:mm"));
			}
			viewHolder.mCommentContent.setText(comment.getContent());
			viewHolder.mRatingBar.setRating(comment.getStars() * 1.0f);
			return convertView;
		}
		
		class UpdateOnClickListener implements View.OnClickListener {
			@Override
			public void onClick(View v) {
				Comment mEditComment = getItem(((Integer) v.getTag()).intValue());
				Comment2 mComment2 = new Comment2(mEditComment);
				Intent updateIntent = new Intent(getApplicationContext(), UpdateCommentDetailActivity.class);
				updateIntent.putExtra("comment2", mComment2);
				startActivity(updateIntent);
			}
		}

		class ViewHolder {

			TextView mCommentAuthor;
			TextView mCommentDate;
			TextView mCommentContent;
			RatingBar mRatingBar;
			Button mButtonEdit;
		}
	}
}