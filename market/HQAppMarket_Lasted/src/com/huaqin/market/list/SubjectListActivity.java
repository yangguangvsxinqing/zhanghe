package com.huaqin.market.list;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

import org.apache.http.HttpException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.huaqin.android.market.sdk.NewSubjectProvider;
import com.huaqin.android.market.sdk.bean.NewSubject;
import com.huaqin.market.R;
import com.huaqin.market.model.Image2;
import com.huaqin.market.model.Subject2;
import com.huaqin.market.ui.LoadingAnimation;
import com.huaqin.market.ui.SubjectInfoActivity;
import com.huaqin.market.utils.CachedThumbnails;
import com.huaqin.market.utils.Constant;
import com.huaqin.market.utils.DeviceUtil;
import com.huaqin.market.utils.FileManager;
import com.huaqin.market.utils.GlobalUtil;
import com.huaqin.market.utils.OptionsMenu;
import com.huaqin.market.webservice.IMarketService;
import com.huaqin.market.webservice.MarketService;
import com.huaqin.market.webservice.Request;

public class SubjectListActivity extends ListActivity
	implements AdapterView.OnItemClickListener {

	private static final int ACTION_SUBJECT_LIST = 0;
	private static final int ACTION_SUBJECT_ICON = 1;
	private static final int ACTION_NETWORK_ERROR = 2;
	
	private static final int ACTION_USER_PV = 3;
	
	private static final int DIALOG_NETWORK_ERROR = 100;
	
	public static final String SUB_LOAD = "show_sub_list";
	
	private ListView mListView;
	private SubjectsAdapter mAdapter;
	private View mFooterView;
	private View mLoadingIndicator;
	private ImageView mLoadingAnimation;
	private AnimationDrawable loadingAnimation;
	private View mFooterViewSpace;
	private Hashtable<Integer, Boolean> mIconStatusMap;
	private Hashtable<Integer, Drawable> mThumbMap;
	private IMarketService mMarketService;
	private Request mCurrentRequest;
	private Handler mHandler;
	private final BroadcastReceiver mApplicationsInstalledReceiver;
	private final BroadcastReceiver mApplicationsShowReceiver;
	private boolean bInflatingSubjectsList;
	private boolean bBusy;
	private boolean bReachEnd;
	private int nStartIndex;
	private Context mContext;
	private boolean bFlag;
	
	
	private AbsListView.OnScrollListener mScrollListener;
	
	public SubjectListActivity() {
		
		mIconStatusMap = new Hashtable<Integer, Boolean>();
		mThumbMap = new Hashtable<Integer, Drawable>();
		mContext = this;
		bInflatingSubjectsList = false;
		bFlag = true;
		bBusy = false;
		bReachEnd = false;
		nStartIndex = 0;
		mApplicationsInstalledReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				/**************Added-s by JimmyJin 20120816*****************/
				final String action = intent.getAction();
				Log.v("SubjectListActivity mApplicationsInstalledReceiver", "action ="+action);
				if(action.equals(Intent.ACTION_PACKAGE_ADDED)){
//					pkgName += getText(R.string.app_has_installed);
//					Toast.makeText(getApplicationContext(), getText(R.string.app_has_installed), Toast.LENGTH_LONG).show();	
				}
				onResume();
			}
		};
		mApplicationsShowReceiver = new BroadcastReceiver(){

			@Override
			public void onReceive(Context arg0, Intent intent) {
				// TODO Auto-generated method stub
				final String action = intent.getAction();
				Log.v("mApplicationsShowReceiver", "action ="+action);
				if(action.equals(SubjectListActivity.SUB_LOAD)){
					if(bFlag){
						inflateSubjectList();
						bFlag = false;
					}
				}
			}
		};
		
		mScrollListener = new AbsListView.OnScrollListener() {
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				
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
					
					for (int i = 0; i < counts; i++) {
						position = start + i;
						
						if (!mIconStatusMap.containsKey(Integer.valueOf(position))) {
							ViewHolder viewHolder = 
								(ViewHolder) view.getChildAt(i).getTag();
							if (viewHolder != null) {
								int id = (int) mAdapter.getItemId(position);
								Drawable drawable = getThumbnail(position, id);
								viewHolder.mSubjectThumb.setImageDrawable(drawable);
							}
						}
					}
					
					// as list scrolled to end, send request to get above data
					if ((start + counts) >= (mAdapter.getCount() - 2)) {
						inflateSubjectList();
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

	private void initHandler() {
		// TODO Auto-generated method stub
		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch (msg.what) {
				case ACTION_SUBJECT_LIST:
					@SuppressWarnings("unchecked")
					ArrayList<NewSubject> subList = (ArrayList<NewSubject>) msg.obj;
					int subListSize = 0;
					int subListIndex = 0;
					
					if (subList != null) {
						subListSize = subList.size();
						nStartIndex += subListSize;
					}
					
					if (mAdapter == null) {
						mAdapter = new SubjectsAdapter(
								SubjectListActivity.this,
								subList);
						mListView.setAdapter(mAdapter);
					} else {
						for (; subListIndex < subListSize; subListIndex++) {
							mAdapter.add(subList.get(subListIndex));
						}
						if (subListIndex >= subListSize && subListIndex != 0) {
							mAdapter.notifyDataSetChanged();
						}
					}
					
					if (subListSize == 0 || subListSize < Constant.LIST_COUNT_PER_TIME) {
						bReachEnd = true;
						if (mListView.getFooterViewsCount() > 0) {
							mListView.removeFooterView(mFooterView);
//							mListView.addFooterView(mFooterViewSpace);
						}
					}
					
					mLoadingIndicator.setVisibility(View.GONE);
					mListView.setVisibility(View.VISIBLE);
					bInflatingSubjectsList = false;
					
					break;
					
				case ACTION_SUBJECT_ICON:
					Image2 icInfo = (Image2) msg.obj;
					
					if (icInfo.mAppIcon != null) {
						mThumbMap.put(icInfo._id, icInfo.mAppIcon);
						if (CachedThumbnails.bAllowBufferIcon) {
							FileManager.writeSubIconToFile(SubjectListActivity.this,
									icInfo._id, icInfo.mAppIcon);
						}
						if (mAdapter != null) {
							mAdapter.notifyDataSetChanged();
						}
					}
					break;
					
				case ACTION_NETWORK_ERROR:
					mLoadingIndicator.setVisibility(View.GONE);
					mListView.setVisibility(View.VISIBLE);
					bInflatingSubjectsList = false;
					Toast.makeText(mContext, mContext.getString(R.string.error_network_low_speed), Toast.LENGTH_LONG).show();
//				showDialog(DIALOG_NETWORK_ERROR);
					break;
					
				default:
					break;
				}
				return;
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
		
		mListView = getListView();
		mListView.setScrollbarFadingEnabled(true);
		
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
					mMarketService.getSubjectList(mCurrentRequest);
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
					mMarketService.getSubjectList(mCurrentRequest);
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
		
//		try {
//			postPV("Ӧ������");
//		} catch (NameNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		inflateSubjectList();
	}
	
	private void inflateSubjectList() {
		// TODO Auto-generated method stub
		if (bReachEnd || bInflatingSubjectsList) {
			return;
		}
		Log.v("asd", "inflateSubjectList");
		bInflatingSubjectsList = true;
		Request request = new Request(0, Constant.TYPE_SUBJECT_LIST);
		Object[] params = new Object[2];
		
		params[0] = Integer.valueOf(0);
		params[1] = Integer.valueOf(nStartIndex);
		request.setData(params);
		request.addObserver(new Observer() {

			@Override
			public void update(Observable observable, Object data) {
				// TODO Auto-generated method stub
				if (data != null) {
					Message msg = Message.obtain(mHandler, ACTION_SUBJECT_LIST, data);
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
		mMarketService.getSubjectList(request);
	}
	
	private void addThumbnailRequest(int id, String thumbUrl) {
		
		Request request = new Request(0L, Constant.TYPE_SUBS_ICON);
		Object[] params = new Object[2];
		
		params[0] = Integer.valueOf(id);
		params[1] = thumbUrl;
		request.setData(params);
		request.addObserver(new Observer() {

			@Override
			public void update(Observable observable, Object data) {
				// TODO Auto-generated method stub
				if (data != null) {
					Message msg = Message.obtain(mHandler, ACTION_SUBJECT_ICON, data);
					mHandler.sendMessage(msg);
				}
			}
		});
		mCurrentRequest = request;
		mMarketService.getSubjectIcon(request);
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
	private void registerIntentReceivers() {
    	// TODO Auto-generated method stub	
		IntentFilter intentInstalledFilter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
		intentInstalledFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		intentInstalledFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		intentInstalledFilter.addDataScheme("package");
		registerReceiver(mApplicationsInstalledReceiver, intentInstalledFilter);
		
		IntentFilter intentShowFilter = new IntentFilter(SubjectListActivity.SUB_LOAD);
		registerReceiver(mApplicationsShowReceiver, intentShowFilter);

	}
	private void unregisterIntentReceivers() {
		// TODO Auto-generated method stub
		unregisterReceiver(mApplicationsShowReceiver);
		unregisterReceiver(mApplicationsInstalledReceiver); 
	}

	public Drawable getThumbnail(int position, int id) {
		// TODO Auto-generated method stub
		boolean bThumbExists = mIconStatusMap.containsKey(Integer.valueOf(position));
		boolean bThumbCached = false;
		
		if (bBusy && !bThumbExists) {
			return CachedThumbnails.getDefaultIcon(this);
		}
		
		if (bThumbExists) {
			bThumbCached = mIconStatusMap.get(Integer.valueOf(position)).booleanValue();
		}
		
		if (mThumbMap.containsKey(id)) {
			if (!bThumbExists || (bThumbExists && !bThumbCached)) {
				mIconStatusMap.put(Integer.valueOf(position), Boolean.valueOf(true));
			}
			return mThumbMap.get(id);
		} else {
			Drawable drawable = null;
			
			if (CachedThumbnails.bAllowBufferIcon) {
				drawable = FileManager.readSubIconFromFile(this, id);
			}
			if (drawable == null) {
				
				if (bThumbExists && !bThumbCached) {
					// cause thumb record existed but not cached
					// do not sent thumb request again, just return default icon
					return CachedThumbnails.getDefaultIcon(this);
				} else {
					// cause thumb record not existed
					// or thumb not cached yet,
					// set cached flag as false, and send thumb request
					mIconStatusMap.put(Integer.valueOf(position), Boolean.valueOf(false));
					
					// check image URL validity
					String url = mAdapter.getThumbURL(position);
					if (url.startsWith("http://")) {
						addThumbnailRequest(id, mAdapter.getThumbURL(position));
					} else {
						// push default icon to buffer
						mIconStatusMap.put(Integer.valueOf(position), Boolean.valueOf(true));
						mThumbMap.put(id, CachedThumbnails.getDefaultIcon(this));
					}
					return CachedThumbnails.getDefaultIcon(this);
				}
			} else {
				mThumbMap.put(id, drawable);
				mIconStatusMap.put(Integer.valueOf(position), Boolean.valueOf(true));
			}
			
			return drawable;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		mMarketService = MarketService.getServiceInstance(this);
		registerIntentReceivers();
		setContentView(R.layout.app_list);
		initHandler();
		initListView();
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
						case Constant.TYPE_SUBJECT_LIST:
							mMarketService.getSubjectList(mCurrentRequest);
							break;
						case Constant.TYPE_SUBS_ICON:
							mMarketService.getSubjectIcon(mCurrentRequest);
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
    	if (keyCode == KeyEvent.KEYCODE_MENU) {
    		startActivity(new Intent(SubjectListActivity.this, OptionsMenu.class));
    		overridePendingTransition(R.anim.fade, R.anim.hold);
    	}
    	return super.onKeyUp(keyCode, event);
    }

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if (mAdapter != null) {
			mAdapter.clear();
		}
		if (mListView != null) {
			mListView.setAdapter(null);
		}
		unregisterIntentReceivers();
		super.onDestroy();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view,
			int position, long id) {
		if(position < mAdapter.getCount()) {
			Intent intent = new Intent(this, SubjectInfoActivity.class);	
			Subject2 subject = new Subject2(mAdapter.getItem(position));
			NewSubject sub = null;
			try {
				sub=NewSubjectProvider.getNewSubjectDetail(subject.getSubjId());
				Log.v("asd", "sub = "+sub);
				if(sub != null)
				{subject = new Subject2(sub);}
			}catch (HttpException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			Log.v("asd", "Subject2 = "+subject);
			if(sub!=null){
				intent.putExtra("subInfo", subject);
				startActivity(intent);
			}
		}
	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		// TODO Auto-generated method stub
//		return OptionsMenu.onOptionsItemSelected(this, item);
//	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		Log.v("SubjectListActivity mApplicationsInstalledReceiver", "onResume");
		super.onResume();
	}

	class SubjectsAdapter extends ArrayAdapter<NewSubject> {
		
		private LayoutInflater mInflater;

		public SubjectsAdapter(Context context, ArrayList<NewSubject> objects) {
			
			super(context, 0, objects);
			
			mInflater = 
				(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return getItem(position).getSubjId();
		}
		
		public String getThumbURL(int position) {
			// TODO Auto-generated method stub
			return getItem(position).getIcon();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			NewSubject subject = getItem(position);
			int subId = subject.getSubjId();
			ViewHolder viewHolder = null;
			
			if (convertView == null || 
					!(convertView.getTag() instanceof ViewHolder)) {
				convertView = mInflater.inflate(R.layout.sub_list_item, null);
				viewHolder = new ViewHolder();
				viewHolder.mListItem = (LinearLayout) convertView.findViewById(R.id.sub_listitem_layout);
				viewHolder.mSubjectThumb = (ImageView) convertView.findViewById(R.id.sub_listitem_thumb);
				viewHolder.mSubjectTitle = (TextView) convertView.findViewById(R.id.sub_listitem_title);
				viewHolder.mSubjectVisit = (TextView) convertView.findViewById(R.id.sub_listitem_visit);
				viewHolder.mSubjectDate = (TextView) convertView.findViewById(R.id.sub_listitem_date);
//				viewHolder.mSubjectHot = (ImageView) convertView.findViewById(R.id.sub_listitem_hot);
				
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			
//			if ((position % 2) == 0) {
//				viewHolder.mListItem.setBackgroundResource(R.drawable.bg_list_item_view);
//			} else {
//				viewHolder.mListItem.setBackgroundResource(R.drawable.bg_list_item_grey);
//			}
			
			viewHolder.mSubjectThumb.setTag(Integer.valueOf(subId));
			viewHolder.mSubjectThumb.setImageDrawable(getThumbnail(position, subId));
			viewHolder.mSubjectTitle.setText(subject.getTitle());
			viewHolder.mSubjectVisit.setText(getVisitText(subject.getReads()));
			viewHolder.mSubjectDate.setText(
					GlobalUtil.getDateByFormat(subject.getCreateTime(), "yyyy-MM-dd"));			
			
			return convertView;
		}
	}
	
	private String getVisitText(String visit) {
		// TODO Auto-generated method stub
		if (visit == null) {
			return 0 + getString(R.string.sub_reads);
		}
		return visit + getString(R.string.sub_reads);
	}
	
	class ViewHolder {
		
		ImageView mSubjectThumb;
		TextView mSubjectTitle;
		TextView mSubjectVisit;
		TextView mSubjectDate;
//		ImageView mSubjectHot;
		LinearLayout mListItem;
	}
}