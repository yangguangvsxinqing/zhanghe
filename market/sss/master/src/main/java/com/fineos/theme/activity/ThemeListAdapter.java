package com.fineos.theme.activity;

import java.util.List;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fineos.theme.R;
import com.fineos.theme.fragment.ThemeMixerFragment;
import com.fineos.theme.model.ThemeData;
import com.fineos.theme.provider.ThemeSQLiteHelper;
import com.fineos.theme.utils.ThemeLog;
import com.fineos.theme.utils.ThemeUtils;
import com.fineos.theme.utils.Util;

public class ThemeListAdapter extends ArrayAdapter<ThemeData> {
	private static final String TAG = "AppListAdapter";
	private Context mContext;
	private LayoutInflater mLayoutInflater;

	private ViewHolder viewHolder = null;

	public static View mListImg;
	private int mElementType = ThemeData.THEME_ELEMENT_TYPE_COMPLETE_THEME;
	private int mPreviewWidth, mPreviewHeight;
	private int layoutResource;
	private String mFreeString = "" ;
	public ThemeListAdapter(Context context, List<ThemeData> objects) {

		super(context, 0, objects);
		mContext = context;
		mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutResource = R.layout.all_theme_previews_home;
		// boutiqueFragment = new BoutiqueFragment(mContext);
		mFreeString = mContext.getResources().getString(R.string.default_price_text);
	}

	public ThemeListAdapter(Context context, List<ThemeData> objects, int mElementType) {

		super(context, 0, objects);
		mContext = context;
		this.mElementType = mElementType;
		mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutResource = R.layout.all_theme_previews;
		// boutiqueFragment = new BoutiqueFragment(mContext);
		mFreeString = mContext.getResources().getString(R.string.default_price_text);
	}

	@Override
	public long getItemId(int position) {

		return (getItem(position)).getId();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub

		return super.getCount();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ThemeData themeInfo = getItem(position);
		long themeId = 0;
		if (themeInfo.getId() != 0) {
			themeId = themeInfo.getId();
		}

		if (convertView == null) {
			convertView = mLayoutInflater.inflate(layoutResource, parent, false);
			viewHolder = new ViewHolder();
			viewHolder.mListArea = (LinearLayout) convertView.findViewById(R.id.list_area);
			viewHolder.mListAreaImg = (ImageView) convertView.findViewById(R.id.preview_image);
			viewHolder.mListAreaTitle = (TextView) convertView.findViewById(R.id.theme_name);
			viewHolder.mListAreaPrice = (TextView) convertView.findViewById(R.id.theme_price);
			viewHolder.mListAreaFlag = (ImageView) convertView.findViewById(R.id.using_indicator);
			viewHolder.mListDownloadFlag = (ImageView) convertView.findViewById(R.id.download_indicator);
			viewHolder.mListItem = (RelativeLayout) convertView.findViewById(R.id.preview_layout);
			mPreviewWidth = (int) mContext.getResources().getDimension(R.dimen.preview_width_all_themes_home);// 216;
			mPreviewHeight = (int) mContext.getResources().getDimension(R.dimen.preview_height_all_themes);// 304;
			viewHolder.mListAreaImg.setBackgroundResource(R.drawable.ic_pic_loading);
			if (mElementType == ThemeData.THEME_ELEMENT_TYPE_ICONS) { // icons
				mPreviewWidth = (int) mContext.getResources().getDimension(R.dimen.preview_width_icon);
				mPreviewHeight = (int) mContext.getResources().getDimension(R.dimen.preview_height_icon);
			} else if (mElementType == ThemeData.THEME_ELEMENT_TYPE_LOCKSCREEN) {
				mPreviewWidth = (int) mContext.getResources().getDimension(R.dimen.preview_width_all_themes);// 216;
				mPreviewHeight = (int) mContext.getResources().getDimension(R.dimen.preview_height_all_themes);// 304;
			} else if (mElementType == ThemeData.THEME_ELEMENT_TYPE_DYNAMIC_WALLPAPER) {
				mPreviewWidth = (int) mContext.getResources().getDimension(R.dimen.live_wallpaper_thumbnail_width);
				mPreviewHeight = (int) mContext.getResources().getDimension(R.dimen.live_wallpaper_thumbnail_height);
			} else if (mElementType == ThemeData.THEME_ELEMENT_TYPE_LOCK_WALLPAPER) { // lock
				mPreviewWidth = (int) mContext.getResources().getDimension(R.dimen.preview_width_all_themes);// 216;
				mPreviewHeight = (int) mContext.getResources().getDimension(R.dimen.preview_height_all_themes);// 304;
			} else if (mElementType == ThemeData.THEME_ELEMENT_TYPE_WALLPAPER) { // wallpaper
				mPreviewWidth = (int) mContext.getResources().getDimension(R.dimen.preview_width_static_wallpaper);
				mPreviewHeight = (int) mContext.getResources().getDimension(R.dimen.preview_height_static_wallpaper);
				viewHolder.mListAreaTitle.setVisibility(View.GONE);
			} else if (mElementType == ThemeData.THEME_ELEMENT_TYPE_FONT) { // fonts
				mPreviewWidth = (int) mContext.getResources().getDimension(R.dimen.preview_width_fonts);
				mPreviewHeight = (int) mContext.getResources().getDimension(R.dimen.preview_height_fonts);
				ImageView iv = (ImageView)viewHolder.mListItem.findViewById(R.id.preview_image);
				viewHolder.mListAreaImg.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.bg_font));
				iv.setScaleType(ScaleType.FIT_XY);
				viewHolder.mListAreaTitle.setVisibility(View.GONE);
			} 
			
			ThemeLog.v(TAG, "viewHolder.mListItem =" + viewHolder.mListItem);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mPreviewWidth, mPreviewHeight);
			viewHolder.mListItem.setLayoutParams(params);
			convertView.setTag(viewHolder);

		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		viewHolder.mListDownloadFlag.setVisibility(View.GONE);
		viewHolder.mListAreaFlag.setVisibility(View.GONE);
		if(viewHolder.mListAreaPrice!=null){               //xuqian
			viewHolder.mListAreaPrice.setVisibility(View.GONE);
		}
		if (mElementType == ThemeData.THEME_ELEMENT_TYPE_ICONS) {
			if (themeInfo.getIsUsing_icons()) {
				viewHolder.mListAreaFlag.setVisibility(View.VISIBLE);
			}
			if (themeInfo.getDownloadUrl() != null && Util.checkDownload(mContext, themeInfo.getPackageName())) {
				viewHolder.mListDownloadFlag.setVisibility(View.VISIBLE);
			}
		} else if (mElementType == ThemeData.THEME_ELEMENT_TYPE_LOCKSCREEN) {
			if (themeInfo.getIsUsing_lockscreen()) {
				viewHolder.mListAreaFlag.setVisibility(View.VISIBLE);
			}
			if (themeInfo.getDownloadUrl() != null && Util.checkDownload(mContext, themeInfo.getPackageName())) {
				viewHolder.mListDownloadFlag.setVisibility(View.VISIBLE);
			}
		} else if (mElementType == ThemeData.THEME_ELEMENT_TYPE_LOCK_WALLPAPER) {
			if (themeInfo.getIsUsing_lockwallpaper()) {
				viewHolder.mListAreaFlag.setVisibility(View.VISIBLE);
			}
			if (themeInfo.getDownloadUrl() != null && Util.checkDownload(mContext, themeInfo.getPackageName())) {
				viewHolder.mListDownloadFlag.setVisibility(View.VISIBLE);
			}
		} else if (mElementType == ThemeData.THEME_ELEMENT_TYPE_WALLPAPER) {
			if (themeInfo.getIsUsing_wallpaper()) {
				viewHolder.mListAreaFlag.setVisibility(View.VISIBLE);
				
			}
			if (themeInfo.getDownloadUrl() != null && themeInfo.getIsDownLoaded()) {
				viewHolder.mListDownloadFlag.setVisibility(View.VISIBLE);
			}
		} else if (mElementType == ThemeData.THEME_ELEMENT_TYPE_FONT) {
			if (themeInfo.getIsUsing_fonts()) {
				viewHolder.mListAreaFlag.setVisibility(View.VISIBLE);
			}
			if (themeInfo.getDownloadUrl() != null && Util.checkDownload(mContext, themeInfo.getPackageName())) {
				viewHolder.mListDownloadFlag.setVisibility(View.VISIBLE);
			}
		} else if (mElementType == ThemeData.THEME_ELEMENT_TYPE_DYNAMIC_WALLPAPER) {
			if (false) {
				viewHolder.mListAreaFlag.setVisibility(View.VISIBLE);
			}
			if (themeInfo.getDownloadUrl() != null && Util.checkDownload(themeInfo.getDownloadUrl())
					&& !getIsDownload(themeInfo.getDownloadUrl())) {
				viewHolder.mListDownloadFlag.setVisibility(View.VISIBLE);
			}
		} else {
			if (themeInfo.getIsUsing()) {
				viewHolder.mListAreaFlag.setVisibility(View.VISIBLE);
			}
			if (themeInfo.getDownloadUrl() != null && themeInfo.getIsDownLoaded()) {
				viewHolder.mListDownloadFlag.setVisibility(View.VISIBLE);
			}
		}
		viewHolder.mListAreaImg.setTag(themeId);
		if (mContext instanceof ThemeOnlineHomeActivity) {
			viewHolder.mListAreaImg.setImageDrawable(((ThemeOnlineHomeActivity) mContext).getThumbnail(position, themeInfo));
		} else if (mContext instanceof ThemeLocalHomeActivity) {
			viewHolder.mListAreaImg.setImageDrawable(((ThemeLocalHomeActivity) mContext).getThumbnail(position, themeInfo));
		} else {
			try {
				viewHolder.mListAreaImg.setImageDrawable(((ThemeMixerFragment) ((FragmentActivity) mContext).getSupportFragmentManager().findFragmentById(R.id.container))
						.getThumbnail(position, themeInfo));
			} catch (Exception e) {
				// TODO: handle exception
				ThemeLog.e(TAG, "getView crash " +  e);
				e.printStackTrace();
			}			
		}

		viewHolder.mListAreaTitle.setText(themeInfo.getTitle());
		
		
		String sPrice = themeInfo.getsPrice() ;   //get from google first
		if(sPrice==null||"".equals(sPrice)){     // if null
			
			Float fPrice = themeInfo.getPrice();   // get from server
			//fPrice = 1.0f ;
			if(fPrice!=0){
				sPrice = "$"+String.valueOf(fPrice);
			}else{
				sPrice = mFreeString;   // then default free
			}
		}
		ThemeLog.v(TAG,"themeInfo title:"+themeInfo.getTitle()+"themeInfo url :"+themeInfo.getDownloadUrl()+",sPrice:"+sPrice);
		if(viewHolder.mListAreaPrice!=null){
			
			if(themeInfo.getDownloadUrl()!=null){   //only online theme display price
				int GoogleBillingSupportId = mContext.getResources().getIdentifier("google_billing_support", "bool", "com.fineos");
				   if(GoogleBillingSupportId <= 0){
					   GoogleBillingSupportId = com.fineos.internal.R.bool.google_billing_support;
				}
				Boolean showPrice = mContext.getResources().getBoolean(GoogleBillingSupportId);
				if(showPrice){
					viewHolder.mListAreaPrice.setVisibility(View.VISIBLE);
					
					Boolean downloaded = Util.checkDownload(mContext, themeInfo.getPackageName());
					String buyed = mContext.getResources().getString(R.string.buyed_text);
					ThemeLog.v(TAG, "downloaded:"+downloaded+",package:"+themeInfo.getPackageName());
					if(!mFreeString.equals(sPrice)&&downloaded){
						sPrice = buyed ;
					}
					viewHolder.mListAreaPrice.setText(sPrice);
				}else{
					viewHolder.mListAreaPrice.setVisibility(View.GONE);
				}
				
			}
			
		}
		return convertView;

	}
	
	private boolean getIsDownload(String themeUrl) {
		boolean result = false;

		DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
		DownloadManager.Query myDownloadQuery = new DownloadManager.Query();
//		myDownloadQuery.setFilterByStatus(DownloadManager.STATUS_PAUSED|DownloadManager.STATUS_SUCCESSFUL);
		myDownloadQuery.setFilterByStatus(DownloadManager.STATUS_PENDING | DownloadManager.STATUS_PAUSED | DownloadManager.STATUS_RUNNING);
		Cursor cursor = downloadManager.query(myDownloadQuery);
		int fileNameIdx = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
		int urlIdx = cursor.getColumnIndex(DownloadManager.COLUMN_URI);
		int pathUrlIdx = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
		int statusIdx = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);

		if (cursor != null) {
			ThemeLog.i(TAG, "cursor.getCount(): " + cursor.getCount());
			try {
				while (cursor.moveToNext()) {
					String fileName = cursor.getString(fileNameIdx);
					String downloadUrl = cursor.getString(urlIdx);
					String localdUrl = cursor.getString(pathUrlIdx);
					int status = cursor.getInt(statusIdx);

					ThemeLog.w(TAG, "cursor.getPosition(): " + cursor.getPosition());
					ThemeLog.i(TAG, "fileName: " + fileName);
					ThemeLog.d(TAG, "downloadUrl: " + downloadUrl);
					ThemeLog.v(TAG, "localdUrl: " + localdUrl);
					ThemeLog.i(TAG, "status: " + status);
					if (downloadUrl.equals(themeUrl)) {
						result = true;
						break;
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		}
		return result;
	}

	public class ViewHolder {
		int _id;
		View mParent;
		LinearLayout mListArea;
		public ImageView mListAreaImg;
		TextView mListAreaTitle;
		TextView mListAreaPrice ;  //xuqian add for google billing
		ImageView mListAreaFlag;
		ImageView mListDownloadFlag;
		RelativeLayout mListItem;
	}

	private Bitmap toConformBitmap(Bitmap background, Bitmap foreground) {
		if (background == null) {
			return null;
		}
		int bgWidth = background.getWidth();
		int bgHeight = background.getHeight();
		// int fgWidth = foreground.getWidth();
		// int fgHeight = foreground.getHeight();
		Bitmap newbmp = Bitmap.createBitmap(bgWidth, bgHeight, Config.ARGB_8888);
		Canvas cv = new Canvas(newbmp);
		// draw bg into
		cv.drawBitmap(background, 0, 0, null);
		// draw fg into
		cv.drawBitmap(foreground, 0, 0, null);
		// save all clip
		cv.save(Canvas.ALL_SAVE_FLAG);
		// store
		cv.restore();
		return newbmp;
	}
}