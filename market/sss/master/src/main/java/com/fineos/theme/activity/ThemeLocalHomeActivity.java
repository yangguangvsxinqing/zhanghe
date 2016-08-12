package com.fineos.theme.activity;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.TextView;

import com.baidu.mobstat.StatService;
import com.fineos.android.rom.sdk.ClientInfo;
import com.fineos.theme.R;
import com.fineos.theme.ThemeConfig;
import com.fineos.theme.ThemeDataCache;
import com.fineos.theme.adapter.CustomAdapter;
import com.fineos.theme.download.ReportProvider;
import com.fineos.theme.model.CustomData;
import com.fineos.theme.model.Image2;
import com.fineos.theme.model.ThemeData;
import com.fineos.theme.preview.PreviewHelper;
import com.fineos.theme.provider.IconContentProvider;
import com.fineos.theme.provider.ThemeSQLiteHelper;
import com.fineos.theme.ui.HalfGridView;
import com.fineos.theme.ui.HeaderGridView;
import com.fineos.theme.ui.ThemeHeaderViewGroup;
import com.fineos.theme.utils.CachedThumbnails;
import com.fineos.theme.utils.Constant;
import com.fineos.theme.utils.ThemeLog;
import com.fineos.theme.utils.ThemeUtils;

public class ThemeLocalHomeActivity extends Activity {
	private HeaderGridView mLocalGridView;
	private TextView textTitle;
	private TextView line;

	private TextView layoutIcon;
	private TextView layoutLockScreen;

	private TextView layoutWallPaper;
	private TextView layoutDynamicwallpaperWallPaper;


	private Context mContext;
	private List<ThemeData> mThemesList;
	private ThemeListAdapter mAdapter;
	private LoadThemesInfoTask mTask;
	private Boolean mbFirstLoad;
	private boolean bBusy;
	private Boolean enable_online;

	private int mThemeSizePause;
	private int mThemeSizeResume;
	
	private ThemeHeaderViewGroup mThemeHeaderViewGroup;
	private CustomAdapter mCustomAdapter;
	private ArrayList<CustomData> mList;
	private static final String TAG = "ThemeLocalHomeActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		
		ThemeLog.i(TAG, "currentapiVersion: " + currentapiVersion);
		if (currentapiVersion <= Build.VERSION_CODES.KITKAT) {
			setTheme(com.fineos.internal.R.style.Theme_Holo_Light_FineOS);
		} else {
			setTheme(com.fineos.internal.R.style.FineOSTheme_Material_Light_WhiteActionBar);
		}
		
		setContentView(R.layout.layout_home_local);
		mContext = this;

		mLocalGridView = (HeaderGridView) findViewById(R.id.localGridView);
		textTitle = (TextView) findViewById(R.id.textTitle);
		line = (TextView) findViewById(R.id.line);

		View view = LayoutInflater.from(this).inflate(R.layout.layout_home_local_header, null);
		mThemeHeaderViewGroup = (ThemeHeaderViewGroup) view.findViewById(R.id.theme_header_viewgroup);
//		mHalfGridView.setVerticalSpacing((int) getResources().getDimension(R.dimen.local_gridview_verticalSpacing_fonts));
		
		mList = CustomData.loadMixerLocalIcon(this);
		mThemeHeaderViewGroup.setLayoutLp(2, mList.size()); // Each row shows two Items
		mThemeHeaderViewGroup.setgap((int)getApplicationContext().getResources().getDimension(R.dimen.local_theme_headerView_horizontal_gap), 
				(int)getApplicationContext().getResources().getDimension(R.dimen.local_theme_headerView_vertical_gap));
		mThemeHeaderViewGroup.addLocalThemeHeaderChildView(mList, mItemClickListener, R.layout.custom_gridview_item_ol);
		mLocalGridView.addHeaderView(view);
		enable_online = true;
		try {
			ActivityInfo activityInfo = getPackageManager().getActivityInfo(getComponentName(), PackageManager.GET_META_DATA);
			enable_online = activityInfo.metaData.getBoolean("enable_online");
		} catch (NameNotFoundException e) {

			e.printStackTrace();
		}

		if (enable_online) {
			textTitle.setVisibility(View.GONE);
			line.setVisibility(View.GONE);

			getActionBar().setTitle(getResources().getString(R.string.home_btn_local));
			getActionBar().setDisplayUseLogoEnabled(false);
			getActionBar().setDisplayHomeAsUpEnabled(true);
			getActionBar().setDisplayShowHomeEnabled(false);
		} else {
			line.setVisibility(View.VISIBLE);
			textTitle.setVisibility(View.VISIBLE);

			StatService.setAppChannel(this, ThemeConfig.getInstance().getROMCenterAndOtherChannel(), true);
			StatService.setOn(this, StatService.EXCEPTION_LOG);
			StatService.setDebugOn(true);
		}

		ThemeUtils.createThemeDir();
		ThemeLog.i(TAG, "qwe  tmpCursor");
	//	ThemeUtils.insertIconEntryToDb(mContext);
		Cursor tmpCursor = null;
		try {
			tmpCursor = mContext.getContentResolver().query(IconContentProvider.CONTENT_URI, null, null, null, ThemeSQLiteHelper.COLUMN_ID + " DESC");
			ThemeLog.v(TAG, "qwe  tmpCursor ="+tmpCursor);
			ThemeLog.v(TAG, "qwe  tmpCursor ="+tmpCursor.moveToFirst());
			ThemeLog.v(TAG, "qwe  tmpCursor ="+tmpCursor.getCount());
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if(tmpCursor!=null){
				tmpCursor.close();
			}
		}
		mThemesList = new ArrayList<ThemeData>();
		setProgressBarIndeterminateVisibility(true);

		mLocalGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		mLocalGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() { // 点击进入主题详细
					@Override
					public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
						if (i >= 3) {
							ReportProvider.postUserTheme(ThemeUtils.getpackageName(mAdapter.getItem(i - 3), false), ThemeData.THEME_REPORT_SORT_CLICK);

							Intent intent = new Intent(ThemeLocalHomeActivity.this, ThemeDetailActivity.class);
							Bundle bunble = new Bundle();
							bunble.putSerializable("themeInfo", mAdapter.getItem(i - 3));
							intent.putExtras(bunble);
							startActivity(intent);
						}
					}
				});

		mTask = new LoadThemesInfoTask();
		mTask.execute();
		markAsDone();
		mbFirstLoad = true;
	}
	
	OnClickListener mItemClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			onOperateClick((Integer) v.getTag());
		}
	};


	@Override
	protected void onResume() {
		String[] availableThemes = ThemeUtils.getAvailableThemes(Constant.DEFAULT_THEME_PATH);
		mThemeSizeResume = availableThemes == null ? 0 : availableThemes.length;
		ThemeUtils.removeNonExistingThemes(this, availableThemes);
		ThemeLog.i(TAG, "onResume,mThemeSizeResume=" + mThemeSizeResume + ",mThemeSizePause=" + mThemeSizePause);
		ThemeLog.i(TAG, "onResume,mbFirstLoad=" + mbFirstLoad);

		if ((mThemeSizeResume >= mThemeSizePause) && !mbFirstLoad) {
			mTask = new LoadThemesInfoTask();
			mTask.execute();
		} else if (mThemeSizeResume < mThemeSizePause) { // 处于usb 模式
//			ThemeUtils.removeNonExistingThemes(this, availableThemes);
			markAsDone();
		}

		mbFirstLoad = false;

		super.onResume();
		StatService.onResume(this);
	}

	public void onPause() {
		super.onPause();

		String[] availableThemes = ThemeUtils.getAvailableThemes(Constant.DEFAULT_THEME_PATH);
		mThemeSizePause = availableThemes == null ? 0 : availableThemes.length;
		ThemeLog.i(TAG, "onPause,mThemeSizeResume=" + mThemeSizeResume + ",mThemeSizePause=" + mThemeSizePause);

		StatService.onPause(this);
	}

	OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {

			onOperateClick((Integer) v.getTag());
		}
	};

	@Override
	public void onBackPressed() {
		// do something what you want
		super.onBackPressed();
		overridePendingTransition(com.fineos.R.anim.slide_in_left, 
        		com.fineos.R.anim.slide_out_right);
	}
	
	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(com.fineos.R.anim.slide_in_left, com.fineos.R.anim.slide_out_right);
	}

	@Override
	public void startActivity(Intent intent) {
		super.startActivity(intent);
		
		//add activity switch animation
		overridePendingTransition(com.fineos.R.anim.slide_in_right, com.fineos.R.anim.slide_out_left);
	}	
	private void onOperateClick(Integer id) {
		Intent intent = null;
		switch (id) {
		case CustomData.CUSTOM_ITEM_ICON:
			intent = new Intent(ThemeLocalHomeActivity.this, ThemeIconActivity.class);
			intent.putExtra("onlineFlag", Constant.THEME_LOCAL_LIST_TYPE);
			startActivity(intent);
			ThemeDataCache.flushThemeDataCache(ThemeData.THEME_ELEMENT_TYPE_ICONS);
			StatService.onEvent(this, "LocalIconClick", "click");
			break;
		case CustomData.CUSTOM_ITEM_LOCKSCREEN:
			intent = new Intent(ThemeLocalHomeActivity.this, ThemeLockScreenActivity.class);
			intent.putExtra("onlineFlag", Constant.THEME_LOCAL_LIST_TYPE);
			ThemeDataCache.flushThemeDataCache(ThemeData.THEME_ELEMENT_TYPE_LOCKSCREEN);
			startActivity(intent);

			StatService.onEvent(this, "LocalLocksreenClick", "click");
			break;
		case CustomData.CUSTOM_ITEM_FONT:
			intent = new Intent(ThemeLocalHomeActivity.this, ThemeFontActivity.class);
			intent.putExtra("onlineFlag", Constant.THEME_LOCAL_LIST_TYPE);
			ThemeDataCache.flushThemeDataCache(ThemeData.THEME_ELEMENT_TYPE_FONT);
			startActivity(intent);

			StatService.onEvent(this, "LocalFontClick", "click");
			break;
		case CustomData.CUSTOM_ITEM_WALLPAPER:
			intent = new Intent(ThemeLocalHomeActivity.this, ThemeOLWallpaperActivity.class);
			intent.putExtra("onlineFlag", Constant.THEME_LOCAL_LIST_TYPE);
			ThemeDataCache.flushThemeDataCache(ThemeData.THEME_ELEMENT_TYPE_WALLPAPER);
			startActivity(intent);

			StatService.onEvent(this, "LocalWallpaperClick", "click");
			break;
		case CustomData.CUSTOM_ITEM_DYNAMIC_WALLPAPER:
			intent = new Intent(ThemeLocalHomeActivity.this, ThemeDynamicWallpaper.class);
			intent.putExtra("onlineFlag", Constant.THEME_LOCAL_LIST_TYPE);
			ThemeDataCache.flushThemeDataCache(ThemeData.THEME_ELEMENT_TYPE_DYNAMIC_WALLPAPER);
			startActivity(intent);

			StatService.onEvent(this, "LocalDynamicClick", "click");
			break;
		case CustomData.CUSTOM_ITEM_REMINDER_WALLPAPER:
			try {
				intent = new Intent("com.android.intent.action.TIME_WALLPAPER_SET");
				intent.addCategory("android.intent.category.DEFAULT");
				startActivity(intent);
			} catch (Exception e) {
				
			}
			
			StatService.onEvent(this, "LocalReminderClick", "click");
			break;
			
		default:
			break;
		}
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onDestroy() {
		if(mAdapter != null){
			mAdapter.clear();
			cleanDrawable();
			mAdapter.notifyDataSetChanged();
		}
		mIconMap.clear();
		mThemesList.clear();
		mLocalGridView = null;
		super.onDestroy();
	}
	private void cleanDrawable(){
		if (mThemesList!=null && mIconMap!=null) {
			int mThemesListSize = mThemesList.size();
			for(int i=0;i<mThemesListSize;i++){
				BitmapDrawable bitmap = mIconMap.get(mThemesList.get(i).getId());
				if (bitmap!=null) {
					bitmap.getBitmap().recycle();
				}  
			}
		}
	}
	private class LoadThemesInfoTask extends AsyncTask<String, Integer, Boolean> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(String... strings) {
			try {
				ThemeUtils.addThemesToDb(ThemeLocalHomeActivity.this, false);
			} catch (NullPointerException e) {
				return Boolean.FALSE;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			return Boolean.TRUE;
		}

		@Override
		protected void onPostExecute(Boolean aBoolean) {

			setProgressBarIndeterminateVisibility(false);
			markAsDone(); // 数据库创建完毕后，绑定数据(预览图)到view

		}
	}

	void markAsDone() {
		mViewUpdateHandler.sendEmptyMessage(0);
	}

	private Handler mViewUpdateHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			List<ThemeData> tmp = ThemeUtils.getThemeListByType(ThemeData.THEME_ELEMENT_TYPE_COMPLETE_THEME, ThemeLocalHomeActivity.this);
			if (tmp == null || tmp.isEmpty()) {
				ThemeLog.w(TAG, "ThemeUtils.getThemeListByType ThemeData.THEME_ELEMENT_TYPE_COMPLETE_THEME returns null");
				return;
			}

			if (mThemesList != null && mThemesList.size() > 0) {
				mThemesList.clear();
			}
			ThemeLog.v(TAG, "mViewUpdateHandler1 tmp.size="+tmp.size());
			String path;
			int tmpListSize = tmp.size();
			for (int i = 0; i < tmpListSize; i++) {
				path = tmp.get(i).getThemePath().substring(tmp.get(i).getThemePath().lastIndexOf("/")+1,tmp.get(i).getThemePath().lastIndexOf("."));
				if (path.equals("default_fineos")){
					mThemesList.add(tmp.get(i));
					tmp.remove(i);
					break;
				}
			}
			tmpListSize = tmp.size();
			for (int i = 0; i < tmpListSize; i++) {
				ThemeLog.v(TAG, "mViewUpdateHandler i="+i);
				ThemeLog.v(TAG, "mViewUpdateHandler getThemePath="+tmp.get(i).getThemePath());
			}
			ThemeLog.v(TAG, "mViewUpdateHandler2 tmp.size="+tmp.size());
			for (int i = 0; i < tmp.size(); i++) {
				if(tmp.get(i).getThemePath().lastIndexOf(".")-tmp.get(i).getThemePath().indexOf(".") == 2){
					int index = Integer.valueOf(tmp.get(i).getThemePath().substring(tmp.get(i).getThemePath().indexOf(".")
							+1,tmp.get(i).getThemePath().lastIndexOf("."))).intValue();
					ThemeLog.v(TAG, "mViewUpdateHandler ="+index);
					if (index < 10&&index<=tmp.size()) {
						tmp.add(index,tmp.get(i));
						if(index > i){
							tmp.remove(i);
							if(index - i != 1){
								i--;
							}
						}else{
							tmp.remove(i+1);
							
						}
						
					}
				}
				
			}
			mThemesList.addAll(tmp);
			if (mAdapter == null) {
				mAdapter = new ThemeListAdapter(ThemeLocalHomeActivity.this, mThemesList);
				ThemeUtils.setAnimationAdapter(mLocalGridView, ThemeUtils.ANIMATION_TIME, mAdapter);
			} else {
				mAdapter.notifyDataSetChanged();
			}
			
//			mLocalGridView.setAdapter(mAdapter);
		}

	};

	private Hashtable<Integer, Boolean> mIconStatusMap = new Hashtable<Integer, Boolean>();
	private Hashtable<Integer, BitmapDrawable> mIconMap = new Hashtable<Integer, BitmapDrawable>();
	public Drawable getThumbnail(int position, ThemeData theme) {
		ThemeLog.i(TAG, "position: " + position);

		boolean bThumbExists = mIconStatusMap.containsKey(Integer.valueOf(position));
		if (bBusy && !bThumbExists) {
			return CachedThumbnails.getDefaultIcon(mContext);
		}

		ThemeLog.i(TAG, "theme.getId(): " + theme.getId());

		int id = (Integer) theme.getId();
		Drawable drawable = mIconMap.get(id);
		if (drawable == null) {
			boolean bThumbCached = false;
			if (bThumbExists) {
				bThumbCached = mIconStatusMap.get(Integer.valueOf(position)).booleanValue();
			}
			if (bThumbExists && !bThumbCached) {

				return CachedThumbnails.getDefaultIcon(mContext);
			} else {

				mIconStatusMap.put(Integer.valueOf(position), Boolean.valueOf(false));
				addThumbnailRequest(position, theme);
				return CachedThumbnails.getDefaultIcon(mContext);
			}
		} else {
			// cause thumb has been cached
			// set cached flag as true
			mIconStatusMap.put(Integer.valueOf(position), Boolean.valueOf(true));
			return drawable;
		}
	}

	private void addThumbnailRequest(final int position, ThemeData theme) {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				Image2 icInfo = (Image2) msg.obj;

				ThemeLog.i(TAG, "ACTION_THEME_ICON Image2 =" + icInfo.mAppIcon);

				if (icInfo.mAppIcon != null) {
					mIconMap.put(icInfo._id, (new BitmapDrawable(null,icInfo.mAppIcon)));
					mIconStatusMap.put(icInfo._id, true);

					if (mAdapter != null) {
						mAdapter.notifyDataSetChanged();
					}
				}
			}
		};

		Thread thread = new Thread() {
			@Override
			public void run() {
				Image2 img2 = new Image2();
				img2._id = mAdapter.getItem(position).getId();
				try {
					img2.mAppIcon = fetch(mAdapter.getItem(position));
				} catch (IOException e) {
					img2.mAppIcon = null;
				}

				ThemeLog.i(TAG, "img2._id: " + img2._id + " img2.mappIcon==null: " + (img2.mAppIcon == null));

				Message message = handler.obtainMessage(1, img2);
				handler.sendMessage(message);

			}
		};
		thread.start();
	}

	private Bitmap fetch(ThemeData theme) throws IOException {
		ZipFile zip = new ZipFile(theme.getThemePath());
		ZipEntry ze = zip.getEntry("preview/preview_launcher_0.jpg"); // 优先选择这张图作为
		if (ze == null) {

			ze = zip.getEntry("preview/preview_launcher_0.png");
			if (ze == null) {
				String[] previewList = PreviewHelper.getAllPreviews(theme);
				if (previewList.length > 0) { // 否则选择所有预览图的第一张
					ze = zip.getEntry(previewList[0]);
				}

			}

		}
		InputStream is = ze != null ? zip.getInputStream(ze) : null;

		Bitmap bmp = null;
		if (is != null) {
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
			opts.inSampleSize = 1;
			bmp = BitmapFactory.decodeStream(is, null, opts); // 从输入流解析出bitmap
//			drawable = new BitmapDrawable(bmp);
			is.close();
		}
		zip.close();

		return bmp;
	}

}

