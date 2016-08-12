package com.fineos.theme.fragment;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import fineos.app.AlertDialog;
import fineos.widget.listview.swinginadapters.prepared.SwingBottomInAnimationAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.baidu.mobstat.StatService;
import com.fineos.android.rom.sdk.bean.ThemeList;
import com.fineos.theme.R;
import com.fineos.theme.ThemeDataCache;
import com.fineos.theme.activity.ThemeBaseFragmentActivity;
import com.fineos.theme.activity.ThemeDetailActivity;
import com.fineos.theme.activity.ThemeDetailBaseNewActivity;
import com.fineos.theme.activity.ThemeDetailDynamicWallpaperActivity;
import com.fineos.theme.activity.ThemeDetailFontsActivity;
import com.fineos.theme.activity.ThemeDetailLockscreenActivity;
import com.fineos.theme.activity.ThemeDetailNewDynamicWallpaperActivity;
import com.fineos.theme.activity.ThemeDetailNewLockscreenActivity;
import com.fineos.theme.activity.ThemeDetailNewWallpaperActivity;
import com.fineos.theme.activity.ThemeDetailWallpaperActivity;
import com.fineos.theme.activity.ThemeListAdapter;
import com.fineos.theme.activity.ThemeListAdapter.ViewHolder;
import com.fineos.theme.baidusdk.ThemeApplication;
import com.fineos.theme.download.Constants;
import com.fineos.theme.download.Downloads;
import com.fineos.theme.download.ReportProvider;
import com.fineos.theme.model.Image2;
import com.fineos.theme.model.ThemeData;
import com.fineos.theme.ui.HalfGridView;
import com.fineos.theme.ui.HeaderGridView;
import com.fineos.theme.utils.CachedThumbnails;
import com.fineos.theme.utils.Constant;
import com.fineos.theme.utils.ImageCache;
import com.fineos.theme.utils.ThemeLog;
import com.fineos.theme.utils.ThemeUtils;
import com.fineos.theme.utils.Util;
import com.fineos.theme.webservice.IThemeService;
import com.fineos.theme.webservice.Request;
import com.fineos.theme.webservice.RequestHandler;
import com.fineos.theme.webservice.ThemeService;
import com.fineos.volley.GsonGetRequest;
import com.fineos.volley.VolleyURLBuilder;

public class ThemeMixerFragment extends Fragment {

	public static ThemeMixerFragment newInstance(int themeData, int onlineFlag) {
		ThemeMixerFragment themeMixerBaseFragment = new ThemeMixerFragment();
		Bundle args = new Bundle();
		args.putInt("isOnline", onlineFlag);
		args.putInt("elementtype", themeData);
		ThemeDataCache.flushThemeDataCache(themeData);
		themeMixerBaseFragment.setArguments(args);

		return themeMixerBaseFragment;
	}

	private static final String TAG = "ThemeMixerFragment";
	private static final int ACTION_ICON_LIST = 1;
	private static final int ACTION_ICON_IMG = 2;
	private static final int ACTION_ONLINE_ICON_LIST = 3;
	private static final int ACTION_ONLINE_ICON_IMG = 4;
	private static final int ACTION_NETWORK_ERROR = 0;
	public static final String ACTION_IMG_NOTIFY = "action_img_notify";
	private Context mContext;
	private HeaderGridView mGridView;
	private TextView mEmptyView;
	private int mNumColumns = 2;
	private Handler mHandler;
	private Request mCurrentRequest;
	private IThemeService mThemeService;
	private boolean bBusy;
	private Hashtable<Integer, Boolean> mIconStatusMap;
//	private Hashtable<Integer, BitmapDrawable> mIconMap;
	private int mThemeSizePause = 0;
	private int mThemeSizeResume = 0;
	private Boolean mbFirstLoad = false;
	private LoadThemesInfoTask mTask = null;
	private int nStartIndex;
	private boolean bReachEnd;
	private SwingBottomInAnimationAdapter mSwingBottomInAnimationAdapter;
	private boolean mbSetStartPostion = false;
	private Drawable drawable1;
	private int coutPerPage;
	private boolean bInflatingAppList;
	/***************************************************************************************/
	protected int mOnlineType;
	private ThemeListAdapter mAdapter;
	protected int mElementType;
	/***************************************************************************************/
	private ImageView mFirstLoadImageView;
	
	public ThemeMixerFragment() {
		bBusy = false;
		bReachEnd = false;
		bInflatingAppList = false;
		nStartIndex = 0;
		mIconStatusMap = new Hashtable<Integer, Boolean>();
	}

	public void setContext(Context context) {
		ThemeLog.i(TAG, "setContext,context=" + context);
		mContext = context;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mThemeService = ThemeService.getServiceInstance(getActivity());
		mContext = getActivity();
		mOnlineType = getArguments().getInt("isOnline");
		mElementType = getArguments().getInt("elementtype");
		ThemeDataCache.flushThemeDataCache(mElementType);
		ThemeLog.i(TAG, "inflateLocalIconList onlineFlag=" + mOnlineType);
		ThemeLog.i(TAG, "LocalThemeMixerBaseFragment,onCreate...");
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		ThemeLog.i(TAG, "ThemeMixerFragment onSaveInstanceState mElementType = "+mElementType);
		
		outState.putInt("mElementType", mElementType);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ThemeLog.i(TAG, "LocalThemeMixerBaseFragment,onCreateView...");
		ThemeDataCache.initCacheThumbnail();
		View view = inflater.inflate(R.layout.local_theme_mixer_fragment, container, false);
		mGridView = (HeaderGridView) view.findViewById(R.id.custom_gridview);
		View spaceView = inflater.inflate(R.layout.space_view, container, false);
		ViewGroup.LayoutParams lp = spaceView.getLayoutParams();
		int spaceViewHeight = (int)getActivity().getResources().getDimension(R.dimen.space_view_height) - mGridView.getVerticalSpacing();
		lp.height = Math.max(0, spaceViewHeight);
		mGridView.addHeaderView(spaceView);
		mEmptyView = (TextView) view.findViewById(R.id.empty_hint);
		mEmptyView.setVisibility(View.GONE);
		ThemeApplication.getInstance().getRequestQueue();
		if (savedInstanceState!=null) {
			mElementType = savedInstanceState.getInt("mElementType");
			
			ThemeLog.i("ThemeMixerFragment", "ThemeMixerFragment onCreate mElementType = "+mElementType);
		}
		
		initHandler();
		coutPerPage = Constant.WALLPAPER_LIST_COUNT_PER_TIME;
		if (mElementType == ThemeData.THEME_ELEMENT_TYPE_ICONS) {

		} else if (mElementType == ThemeData.THEME_ELEMENT_TYPE_LOCK_WALLPAPER || mElementType == ThemeData.THEME_ELEMENT_TYPE_LOCKSCREEN
				|| mElementType == ThemeData.THEME_ELEMENT_TYPE_DYNAMIC_WALLPAPER) {
			coutPerPage = Constant.THEME_LIST_COUNT_PER_TIME;
			mNumColumns = getResources().getInteger(R.integer.NumColumnsThree);
			mGridView.setNumColumns(mNumColumns);
		}

		mTask = new LoadThemesInfoTask();
		registerIntentReceivers();
		mTask.execute();
		mbFirstLoad = true;
//		if(mOnlineType == Constant.THEME_ONLINE_LIST_TYPE){
			initView();
//		}
		showLoadingView(view);
		return view;

	}
	
	private void showLoadingView(View view) {
		mFirstLoadImageView = (ImageView)view.findViewById(R.id.theme_loading);
		mFirstLoadImageView.setVisibility(View.VISIBLE);
		AnimationDrawable ad = (AnimationDrawable) mFirstLoadImageView.getBackground();
		ad.start();
	}
	
	private void hideLoadingView() {
		mFirstLoadImageView.setVisibility(View.INVISIBLE);
		AnimationDrawable ad = (AnimationDrawable) mFirstLoadImageView.getBackground();
		ad.stop();
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();

		String[] availableThemes = ThemeUtils.getAvailableThemes(Constant.DEFAULT_THEME_PATH);
		mThemeSizePause = availableThemes == null ? 0 : availableThemes.length;
		ThemeLog.i(TAG, "onPause,mThemeSizeResume=" + mThemeSizeResume + ",mThemeSizePause=" + mThemeSizePause);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		ThemeApplication.getInstance().cancelPendingRequests(Constant.TAG_ONLINE_THEME_LIST);
		ThemeApplication.getInstance().cancelPendingRequests(Constant.TAG_ONLINE_THEME_ICON);
		if(mAdapter != null){
//			cleanDrawable();
			mAdapter.clear();
			mAdapter.notifyDataSetChanged();
		}
		mHandler.removeCallbacksAndMessages(null); //remove all callbacks and Messages
		mHandler.removeMessages(ACTION_ICON_LIST);
		mHandler = null;
		mIconStatusMap.clear();
		ThemeDataCache.flushThemeDataCache(mElementType);
		unregisterIntentReceivers();
		mTask.cancel(true);
		mGridView = null;
	}
	private AbsListView.OnScrollListener mScrollListener = new AbsListView.OnScrollListener() {
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {

			switch (scrollState) {
			case SCROLL_STATE_IDLE:
				bBusy = false;
				int start = view.getFirstVisiblePosition();
				int counts = view.getChildCount();
				int position = 0;

				ThemeLog.i(TAG, "start = " + start + " count = " + counts + ""+"getCount="+mAdapter.getCount());
				if (mAdapter != null) {
					mAdapter.notifyDataSetChanged();
				}
				if(mElementType == ThemeData.THEME_ELEMENT_TYPE_WALLPAPER) {
					if (mOnlineType == Constant.THEME_ONLINE_LIST_TYPE && (start + counts) >= (mAdapter.getCount() - Constant.WALLPAPER_LIST_COUNT_PER_TIME/2)) {
						inflateLocalIconList();
					}
				} else {
					if (mOnlineType == Constant.THEME_ONLINE_LIST_TYPE && (start + counts) >= (mAdapter.getCount() - Constant.THEME_LIST_COUNT_PER_TIME/2)) {
						inflateLocalIconList();
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

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			if (mSwingBottomInAnimationAdapter != null && visibleItemCount != 0 && !mbSetStartPostion) {
				mSwingBottomInAnimationAdapter.setShouldAnimateFromPosition(visibleItemCount);
				mbSetStartPostion = true;
			}
			return;
		}
	};
	
	private void clearPendingThumbRequest() {
		Iterator<Integer> iterator = mIconStatusMap.keySet().iterator();

		while (iterator.hasNext()) {
			Integer value = iterator.next();
			if (!mIconStatusMap.get(value).booleanValue()) {
				iterator.remove();
			}
		}
		mThemeService.clearThumbRequest(ThemeService.THREAD_THUMB);
	}
	
	// 显示移动数据流量警告
	private void showNetWarnDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		View view = LayoutInflater.from(mContext).inflate(R.layout.hint_view, null);
		builder.setView(view);
		final CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox);
		builder.setCancelable(false);
		builder.setNegativeButton(getResources().getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();

				getActivity().finish();
			}
		});
		builder.setPositiveButton(getResources().getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				Util.setNetworkHint(mContext, !checkBox.isChecked());
				inflateLocalIconList();
				
			}
		});
		final AlertDialog dialog = builder.create();
		dialog.show();
	}
		
	@Override
	public void onResume() {
		super.onResume();

		String[] availableThemes = ThemeUtils.getAvailableThemes(Constant.DEFAULT_THEME_PATH);
		mThemeSizeResume = availableThemes == null ? 0 : availableThemes.length;
		ThemeUtils.removeNonExistingThemes(getActivity(), availableThemes);
		ThemeLog.i(TAG, "onResume,mThemeSizeResume=" + mThemeSizeResume + ",mThemeSizePause=" + mThemeSizePause);
		ThemeLog.i(TAG, "onResume,mbFirstLoad=" + mbFirstLoad);

		if (mOnlineType == Constant.THEME_LOCAL_LIST_TYPE) {
			if ((mThemeSizeResume >= mThemeSizePause) && !mbFirstLoad) {
				mTask = new LoadThemesInfoTask();
				mTask.execute();
			} else if (mThemeSizeResume < mThemeSizePause) {
				initView();
			}
//			if (mElementType == ThemeData.THEME_ELEMENT_TYPE_WALLPAPER || mElementType == ThemeData.THEME_ELEMENT_TYPE_LOCK_WALLPAPER || mElementType == ThemeData.THEME_ELEMENT_TYPE_DYNAMIC_WALLPAPER) { // wallpaper
//				if (!mbFirstLoad) {
//					inflateLocalIconList();
//				}
//			}
//
			mbFirstLoad = false;
			if (mAdapter != null) {
				mAdapter.notifyDataSetChanged();
			}
		}

	}

	private BroadcastReceiver mThemeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String actionString = intent.getAction();
			String packageNameString = intent.getStringExtra("packageName");
			if (actionString != null && actionString.equals(Constant.ACTION_INFLATE_LIST)) {
				inflateOnlineThemeList();
			} else if (actionString.equals(Constants.ACTION_DELTEITEMS)) {
				deleteorDownLoadItems(packageNameString, false);
			} else if (actionString.equals(Constants.ACTION_DOWNLOADITMES)) {
				deleteorDownLoadItems(packageNameString, true);
			} else if (actionString.equals(Downloads.ACTION_DOWNLOAD_COMPLETED_NOTIFICATION)) {
				String pn = intent.getStringExtra("downloadfilename");
				if (pn != null) {
					String pacString = pn.substring(0, pn.indexOf("."));
					deleteorDownLoadItems(pacString, true);
				}
			} else {
				if (mAdapter != null) {
					mAdapter.notifyDataSetChanged();
				}
			}
		}
	};
	
	private void deleteorDownLoadItems(String packageNameString, boolean download) {
		if (mAdapter == null || packageNameString == null) {
			return;
		}
		int count = mAdapter.getCount();
		for (int i = 0 ; i < count; i++) {
			if (mAdapter.getItem(i).getPackageName().equals(packageNameString)) {
				mAdapter.getItem(i).setIsDownLoaded(download);
			}
		}
		mAdapter.notifyDataSetChanged();
	}

	private void registerIntentReceivers() {
		IntentFilter intentFilter = new IntentFilter(Downloads.ACTION_DOWNLOAD_COMPLETED_NOTIFICATION);
		intentFilter.addAction(Constant.ACTION_INFLATE_LIST);
		mContext.registerReceiver(mThemeReceiver, intentFilter);
	}

	private void unregisterIntentReceivers() {
		mContext.unregisterReceiver(mThemeReceiver);
	}

	private void initView() {
		int spacingTotal = mGridView.getHorizontalSpacing() * (mNumColumns - 1);
		DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
		float aspectRatio = 1;
		if (dm.heightPixels > dm.widthPixels)
			aspectRatio = (float) dm.heightPixels / dm.widthPixels;
		else
			aspectRatio = (float) dm.widthPixels / dm.heightPixels;

		spacingTotal = (int) getResources().getDimension(R.dimen.local_gridview_horizontalSpacing_allthemes);

		int horizontalSpacing = (int) getResources().getDimension(R.dimen.local_gridview_horizontalSpacing_allthemes);
		int verticalSpacing = (int) getResources().getDimension(R.dimen.local_gridview_verticalSpacing_allthemes);
		int left = (int) getResources().getDimension(R.dimen.local_gridview_paddingLeft_allthemes);
		int right = (int) getResources().getDimension(R.dimen.local_gridview_paddingRight_allthemes);
		int top = (int) getResources().getDimension(R.dimen.local_gridview_paddingtop_allthemes);
		if (mElementType == ThemeData.THEME_ELEMENT_TYPE_DYNAMIC_WALLPAPER) { // lockscreen

		} else if (mElementType == ThemeData.THEME_ELEMENT_TYPE_LOCK_WALLPAPER) { // lock
																					// wallpaper
			verticalSpacing = (int) getResources().getDimension(R.dimen.local_gridview_verticalSpacing_lockwallpaper);
		} else if (mElementType == ThemeData.THEME_ELEMENT_TYPE_FONT) { // fonts
			horizontalSpacing = (int) getResources().getDimension(R.dimen.local_gridview_horizontalSpacing_fonts);
			verticalSpacing = (int) getResources().getDimension(R.dimen.local_gridview_verticalSpacing_fonts);
			left = (int) getResources().getDimension(R.dimen.local_gridview_paddingLeft_fonts);
			right = (int) getResources().getDimension(R.dimen.local_gridview_paddingRight_fonts);
		}
		int maxSpacing = (int)getActivity().getResources().getDimension(R.dimen.space_view_height);
		verticalSpacing = Math.min(verticalSpacing, maxSpacing);
		mGridView.setOnScrollListener(mScrollListener);
		mGridView.setHorizontalSpacing(horizontalSpacing);
		mGridView.setVerticalSpacing(verticalSpacing);
		mGridView.setPadding(left, top, right, 0);
		mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() { // 点击进入主题详细
					@Override
					public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
						Log.v(TAG, "ssss mGridView onItemClick");
						if(ThemeDataCache.getThemeDatas(mElementType)!=null){
						i = i - mNumColumns;
						if (i < 0) {
							ThemeLog.e(TAG, "return err onItemClick for i < 0 i :" + i);
							return;
						}
						if(!Util.getNetworkHint(mContext)){
							ReportProvider.postUserTheme(ThemeUtils.getpackageName(ThemeDataCache.getThemeDatas(mElementType).get(i), mOnlineType == 1), ThemeData.THEME_REPORT_SORT_CLICK);
						}
						Intent intent = null;

						if (mElementType == ThemeData.THEME_ELEMENT_TYPE_ICONS) { // icons

							intent = new Intent(getActivity(), ThemeDetailActivity.class);

						} else if (mElementType == ThemeData.THEME_ELEMENT_TYPE_LOCKSCREEN) { // lockscreen

							intent = new Intent(getActivity(), ThemeDetailNewLockscreenActivity.class);

						} else if (mElementType == ThemeData.THEME_ELEMENT_TYPE_WALLPAPER) { // wallpaper

							intent = new Intent(getActivity(), ThemeDetailNewWallpaperActivity.class);
						} else if (mElementType == ThemeData.THEME_ELEMENT_TYPE_FONT) { // fonts

							intent = new Intent(getActivity(), ThemeDetailFontsActivity.class);

						} else if (mElementType == ThemeData.THEME_ELEMENT_TYPE_DYNAMIC_WALLPAPER) { // dynamicwallpaper

							intent = new Intent(getActivity(), ThemeDetailNewDynamicWallpaperActivity.class);

						} else {
							intent = new Intent(getActivity(), ThemeDetailActivity.class);
						}

						Bundle bunble = new Bundle();
						bunble.putSerializable("themeInfo", ThemeDataCache.getThemeDatas(mElementType).get(i));
						bunble.putInt("mixType", mElementType);
						bunble.putInt("isOnline", mOnlineType);
						bunble.putInt("currentthemeposition", i);
						intent.putExtras(bunble);
						startActivity(intent);
						StatService.onEvent(mContext, "OnThemeclick", ThemeDataCache.getThemeDatas(mElementType).get(i).getTitle());
						//add activity switch animation
						getActivity().overridePendingTransition(com.fineos.R.anim.slide_in_right, com.fineos.R.anim.slide_out_left);
					}
					}
				});

		inflateLocalIconList();
	}
	
	private void initHandler() {
		// TODO Auto-generated method stub
		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch (msg.what) {
				case ACTION_ICON_LIST:
					hideLoadingView();
					List<ThemeData> themeList = (List<ThemeData>) msg.obj;
					ThemeLog.i(TAG, "ACTION_ICON_LIST");
					if (mContext == null) {
						mContext = getActivity();
					}
					if (mContext == null) {
						return;
					}
					int themeListSize = themeList.size();
					for (int i = 0; i < themeListSize; i++) {
						if (themeList.get(i).getTitle().equals(getResources().getString(R.string.default_theme_title))) {
							ThemeDataCache.cacheTheme(themeList.get(i), mElementType);
							themeList.remove(i);
							break;
						}
					}
					themeListSize = themeList.size();
					for (int i = 0; i < themeListSize; i++) {
						ThemeDataCache.cacheTheme(themeList.get(i), mElementType);
					}
					if (mElementType == ThemeData.THEME_ELEMENT_TYPE_FONT) {

//						if (mContext != null) {
//							Configuration curConfig = mContext.getResources().getConfiguration();
//							String fontPath = null;
//							ThemeLog.e(TAG, "curConfig fontPath=" + curConfig.fontPath);
//							ThemeLog.e(TAG, "curConfig fontPackageName=" + curConfig.fontPackageName);
//							if (curConfig.fontPath != null && curConfig.fontPath.length() > 0) {
//								fontPath = curConfig.fontPath.substring(curConfig.fontPath.lastIndexOf("/") + 1, curConfig.fontPath.lastIndexOf("."));
//								ThemeLog.e(TAG, "fontPath =" + fontPath);
//								for (int i = 0; i < mThemeList.size(); i++) {
//									if (mThemeList.get(i).getIsUsing_fonts()) {
//										mThemeList.get(i).setIsUsing_fonts(false);
//									}
//								}
//								for (int i = 0; i < mThemeList.size(); i++) {
//									String themePath = mThemeList.get(i).getThemePath()
//											.substring(mThemeList.get(i).getThemePath().lastIndexOf("_") + 1, mThemeList.get(i).getThemePath().lastIndexOf("."));
//									if (themePath.equals(fontPath)) {
//										mThemeList.get(i).setIsUsing_fonts(true);
//										break;
//									}
//								}
//							}
//						}
					}
					if (mAdapter != null) {
						mAdapter = null;
					}
					ThemeLog.i(TAG, "ACTION_ICON_LIST mContext =" + mContext);
					if (mContext != null) {
						mAdapter = new ThemeListAdapter(mContext, ThemeDataCache.getThemeDatas(mElementType), mElementType);
//						mGridView.setAdapter(mAdapter);
						mSwingBottomInAnimationAdapter = ThemeUtils.setAnimationAdapter(mGridView, ThemeUtils.ANIMATION_TIME, mAdapter);
						mGridView.setVisibility(View.VISIBLE);
					}

					break;

				case ACTION_ICON_IMG:
					Image2 icInfo = (Image2) msg.obj;
					ThemeLog.i(TAG, "ACTION_ICON_IMG icInfo=" + icInfo);
					ThemeLog.i(TAG, "ACTION_ICON_IMG mAdapter2=" + mAdapter);
					if (icInfo != null&&icInfo.mAppIcon != null) {
//						if (!mIconMap.containsKey(icInfo._id)) {
//							mIconMap.put(icInfo._id, (BitmapDrawable)icInfo.mAppIcon);
//						}
						ThemeDataCache.cacheLocalThumbs(icInfo._id, new BitmapDrawable(null, icInfo.mAppIcon),
								mGridView.getFirstVisiblePosition(), mGridView.getLastVisiblePosition(), mIconStatusMap, mElementType);
						if (mAdapter != null) {
							mAdapter.notifyDataSetChanged();
						}
					}
					break;

				case ACTION_ONLINE_ICON_LIST:
					hideLoadingView();	
					if (mContext != null) {
						((ThemeBaseFragmentActivity) mContext).hideLoadingDialog();
					}
					ThemeList onlineThemeList = (ThemeList) msg.obj;
					ThemeLog.i(TAG, "ACTION_ONLINE_ICON_LIST onlineThemeList=" + onlineThemeList.getThemeInfos().size());
					int appListSize = 0;
					int appListIndex = 0;
					if (onlineThemeList != null) {
						if (onlineThemeList.getThemeInfos() != null && onlineThemeList.getThemeInfos().size() > 0) {
							if(nStartIndex == 0){
								ThemeDataCache.getThemeDatas(mElementType).clear();
							}
							mEmptyView.setVisibility(View.GONE);
							appListSize = onlineThemeList.getThemeInfos().size();
							nStartIndex += appListSize;
							int onlineThemeListSize = onlineThemeList.getThemeInfos().size();
							for (int i = 0; i < onlineThemeListSize; i++) {
								ThemeData themeData = new ThemeData(onlineThemeList.getThemeInfos().get(i), getActivity());
//								if (Util.checkDownload(mContext, themeData.getPackageName())) {
//									themeData = ThemeUtils.getThemeByFildId(mContext, themeData.getPackageName());
//								}
								ThemeDataCache.cacheTheme(themeData, mElementType);
							}
							Intent intent = new Intent(Constant.ACTION_DATACHANGE);
							mContext.sendBroadcast(intent);
							
							ThemeLog.i(TAG, "ACTION_ONLINE_ICON_LIST nStartIndex=" + nStartIndex);
							if (mAdapter == null) {
								mAdapter = new ThemeListAdapter(mContext, ThemeDataCache.getThemeDatas(mElementType), mElementType);
//								mGridView.setAdapter(mAdapter);
								mSwingBottomInAnimationAdapter = ThemeUtils.setAnimationAdapter(mGridView, ThemeUtils.ANIMATION_TIME, mAdapter);
								
							} else {
								for (; appListIndex < appListSize; appListIndex++) {
//									mAdapter.add(ThemeDataCache.getThemeDatas().get(appListIndex));
								}
								if (appListIndex >= appListSize && appListIndex != 0) {
									mAdapter.notifyDataSetChanged();
								}
							}
							
							if (appListSize == 0 || appListSize < coutPerPage) {
								bReachEnd = true;
								// if (gridview.getFooterViewsCount() > 0) {
								// gridview.removeFooterView(mFooterView);
								// // mListView.addFooterView(mFooterViewSpace);
								// }
							}
//							mLoadingIndicator.setVisibility(View.GONE);
							mGridView.setVisibility(View.VISIBLE);
							bInflatingAppList = false;
						}
					}

					break;

				case ACTION_ONLINE_ICON_IMG:
					if (mAdapter != null) {
						mAdapter.notifyDataSetChanged();
					}
					Intent intent = new Intent(ACTION_IMG_NOTIFY);
					mContext.sendBroadcast(intent);
					break;

				case ACTION_NETWORK_ERROR:
					if (mContext != null) {
						((ThemeBaseFragmentActivity) mContext).hideLoadingDialog();
					}
					hideLoadingView();
//					mLoadingIndicator.setVisibility(View.GONE);
					// mListView.setVisibility(View.VISIBLE);
					mGridView.setVisibility(View.VISIBLE);
					mEmptyView.setVisibility(View.VISIBLE);
					// showDialog(DIALOG_NETWORK_ERROR);
					Toast.makeText(mContext, mContext.getString(R.string.error_network_low_speed), Toast.LENGTH_LONG).show();
					bInflatingAppList = false;
					break;

				default:
					break;
				}
			}
		};
	}

	private void inflateLocalIconList() {
		ThemeLog.i(TAG, "inflateLocalIconList mOnlineType=" + mOnlineType);
		ThemeLog.i(TAG, "inflateLocalIconList flag=" + Util.getNetworkHint(mContext));
		
		if(!SystemProperties.getBoolean("ro.fineos.net.hide_confirm", ThemeUtils.ISHIDENETDIALOG) && Util.getNetworkHint(mContext)){
			showNetWarnDialog();
		}else{
			if (mOnlineType == Constant.THEME_LOCAL_LIST_TYPE) {
				inflateLocalThemeList();
			} else {
				if(!SystemProperties.getBoolean("ro.fineos.net.hide_confirm", ThemeUtils.ISHIDENETDIALOG) && Util.getNetworkHint(mContext)){
					showNetWarnDialog();
				}else{
					inflateOnlineThemeList();
				}
			}
		}
	}

	private void inflateLocalThemeList() {
		final Request request = new Request(0, Constant.TYPE_ICON_LIST);
		final Object[] params = new Object[2];

		params[0] = mContext == null ? getActivity() : mContext;
		params[1] = mElementType;
		request.setData(params);
		request.addObserver(new Observer() {

			@Override
			public void update(Observable observable, Object data) {

				if (data != null && mHandler != null) {
					ThemeLog.i(TAG, "ACTION_CATEGORY_LIST data= " + data);
					Message msg = Message.obtain(mHandler, ACTION_ICON_LIST, data);
					mHandler.sendMessage(msg);
				}

			}
		});
		Thread t = new Thread() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				List<ThemeData> templist = ThemeUtils.getThemeListByType(mElementType, (Context)params[0]);
				request.setStatus(Constant.STATUS_SUCCESS);
				request.notifyObservers(templist);
				super.run();
			}
			
		};
		t.start();
	}

	private void inflateOnlineThemeList() {
		ThemeLog.w(TAG, "inflateOnlineThemeList");
		ThemeLog.i(TAG, "inflateOnlineThemeList bReachEnd ="+bReachEnd);
		if (bReachEnd || bInflatingAppList) {
			return;
		}
		bInflatingAppList = true;
		int themeType = 0;
		((ThemeBaseFragmentActivity) getActivity()).showLoadingDialog();
		
		if (mElementType == ThemeData.THEME_ELEMENT_TYPE_WALLPAPER) {
			themeType = Constant.ONLINE_THEME_WALLPAPER_TYPE;
		} else if (mElementType == ThemeData.THEME_ELEMENT_TYPE_LOCK_WALLPAPER) {
			themeType = Constant.THEME_LOCKSCREEN_WALLPAPER_TYPE;
		} else if (mElementType == ThemeData.THEME_ELEMENT_TYPE_FONT) {
			themeType = Constant.THEME_FONT_TYPE;
		} else if (mElementType == ThemeData.THEME_ELEMENT_TYPE_LOCKSCREEN) {
			themeType = Constant.THEME_LOCKSCREEN_TYPE;
		} else if (mElementType == ThemeData.THEME_ELEMENT_TYPE_ICONS) {
			themeType = Constant.THEME_FONT_ICON;
		} else if (mElementType == ThemeData.THEME_ELEMENT_TYPE_DYNAMIC_WALLPAPER) {
			themeType = Constant.THEME_DYNAMIC_WALLPAPER_TYPE;
		}
		
		GsonGetRequest<ThemeList> mGsonGetRequest = new GsonGetRequest<ThemeList>(
				VolleyURLBuilder.getThemeList(themeType, nStartIndex),
				ThemeList.class, 
				new Listener<ThemeList>(){
					@Override
					public void onResponse(ThemeList response) {
						// TODO Auto-generated method stub
						if (response != null) {
							Message msg = Message.obtain(mHandler, ACTION_ONLINE_ICON_LIST, response);
							mHandler.sendMessage(msg);
						}else{
							mHandler.sendEmptyMessage(ACTION_NETWORK_ERROR);
						}
					}}, 
				new ErrorListener(){
					@Override
					public void onErrorResponse(VolleyError error) {
						// TODO Auto-generated method stub
						mHandler.sendEmptyMessage(ACTION_NETWORK_ERROR);
					}
				});
		ThemeApplication.getInstance().addToRequestQueue(mGsonGetRequest,Constant.TAG_ONLINE_THEME_LIST);
	}

	private void addLocalThumbnailRequest(int position, final ThemeData theme) {

		final Request request = new Request(0L, Constant.TYPE_LOCAL_ICON_IMG);
		Object[] params = new Object[2];

		params[0] = theme;
		params[1] = mElementType;
		request.setData(params);
		request.addObserver(new Observer() {

			@Override
			public void update(Observable observable, Object data) {
				if (data != null) {
					if (mHandler != null) {
						Message msg = Message.obtain(mHandler, ACTION_ICON_IMG, data);
						mHandler.sendMessage(msg);
					}
				}
			}
		});
		Thread thread = new Thread() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				int elementType = mElementType;
				String themeId = theme.getFileName();
				Bitmap bmp = null;
				try {
					InputStream is = RequestHandler.fetch(theme, elementType);

					if (is != null) {
						BitmapFactory.Options opts = new BitmapFactory.Options();
						opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
						opts.inSampleSize = 1;
						bmp = BitmapFactory.decodeStream(is, null, opts);
						is.close();
					}
				} catch (IOException e) {

				}
				if (bmp != null) {
					Image2 img = new Image2();
					img._id = theme.getId();
					img.mAppIcon = bmp;
					request.setStatus(Constant.STATUS_SUCCESS);
					request.notifyObservers(img);
				} else {
					request.setStatus(Constant.STATUS_ERROR);
					request.notifyObservers(null);
				}
				super.run();
			}
			
		};
		
		thread.start();
		
//		mCurrentRequest = request;
//		mThemeService.getIconImg(mCurrentRequest);
	}

	public Drawable getThumbnail(int position, ThemeData theme) {
		boolean bThumbExists = mIconStatusMap.containsKey(Integer.valueOf(position));
		int id = theme.getId();
		String url = theme.getIconUrl();
		if (bBusy && mOnlineType == Constant.THEME_ONLINE_LIST_TYPE) {
	//		return ThemeDataCache.getDefaultIcon(mContext);
			return ThemeDataCache.getBusyOnlineThumbnail(mContext, url, mElementType);
		}
		Drawable drawable = null;
		if (mOnlineType == Constant.THEME_LOCAL_LIST_TYPE) {
			drawable = ThemeDataCache.getLocalThumbsIcon(theme, mElementType);
		} else {
			drawable = ThemeDataCache.getThumbnail(mContext, url, mElementType);
		}

		if (drawable == null) {
			boolean bThumbCached = false;
			if (bThumbExists) {
				bThumbCached = mIconStatusMap.get(Integer.valueOf(position)).booleanValue();
			}
			if (bThumbExists && !bThumbCached) {
				return null;
			//	return ThemeDataCache.getDefaultIcon(mContext);
			} else {

				mIconStatusMap.put(Integer.valueOf(position), Boolean.valueOf(false));
				ThemeLog.i(TAG, "getThumbnail theme=" + theme.getTitle());
				addThumbnailRequest(position, theme);
				return null;
	//			return ThemeDataCache.getDefaultIcon(mContext);
			}
		}
		mIconStatusMap.put(Integer.valueOf(position), Boolean.valueOf(true));
		return drawable;
	}

	private void addThumbnailRequest(int position, ThemeData theme) {
		if (mOnlineType == Constant.THEME_LOCAL_LIST_TYPE) {
			addLocalThumbnailRequest(position, theme);
		} else {
			addOnlineThumbnailRequest(position, theme);
		}
	}

	private void addOnlineThumbnailRequest(int position,final ThemeData theme) {
		Log.v(TAG, "sss addOnlineThumbnailRequest theme.getIconUrl()="+theme.getIconUrl());
		ImageRequest imageRequest = new ImageRequest(theme.getIconUrl(), 
				new Listener<Bitmap>(){
					@Override
					public void onResponse(Bitmap response) {
						// TODO Auto-generated method stub
						Log.v(TAG, "sss addOnlineThumbnailRequest onResponse="+response);
						if (response != null) {
							ImageCache.cacheThumbnail(mContext, theme.getId(), theme.getIconUrl(), response);
							mIconStatusMap.put(theme.getId(), true);				
						}
						if(mHandler!=null){
							mHandler.sendEmptyMessage(ACTION_ONLINE_ICON_IMG);
						}
					}}, 
				0, 0, Config.ARGB_8888, 
				new ErrorListener(){
					@Override
					public void onErrorResponse(VolleyError error) {
						// TODO Auto-generated method stub
						
					}});
		ThemeApplication.getInstance().addToRequestQueue(imageRequest,Constant.TAG_ONLINE_THEME_ICON);
	}

	private class LoadThemesInfoTask extends AsyncTask<String, Integer, Boolean> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(String... strings) {
			try {

				ThemeUtils.addThemesToDb(getActivity(), false); // create db

			} catch (NullPointerException e) {
				return Boolean.FALSE;
			} catch (UnsupportedEncodingException e) {

				e.printStackTrace();
			}
			return Boolean.TRUE;
		}

		@Override
		protected void onPostExecute(Boolean aBoolean) {
			if(mOnlineType == Constant.THEME_LOCAL_LIST_TYPE){
//				initView();
			}
		}
	}

}




