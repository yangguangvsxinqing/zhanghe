package com.fineos.theme.fragment;

import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.zip.Inflater;

import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.WallpaperManager;
import android.support.v4.app.Fragment;
import android.app.WallpaperInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.service.wallpaper.WallpaperService;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.FrameLayout.LayoutParams;

import com.fineos.theme.R;
import com.fineos.theme.activity.LiveWallpaperPreview;
import com.fineos.theme.model.ThemeData;
import com.fineos.theme.ui.FlipImageView;
import com.fineos.theme.ui.HalfGridView;
import com.fineos.theme.ui.HeaderGridView;
import com.fineos.theme.utils.ThemeLog;
import com.fineos.theme.utils.ThemeUtils;
import com.fineos.theme.webservice.ThemeService;

public class DynamicWallpaperFragment extends Fragment {

	private static final String TAG = "LocalThemeDynamicWallpaperActivity";
	private static final int REQUEST_PREVIEW = 100;
	private HeaderGridView mGridView;
	private TextView mTitle;
	private ImageView mImg;
	private int mNumColumns = 3;
	private LiveWallpaperAdapter mAdapter = null;
	private Context mContext;
	private WallpaperManager mWallpaperManager;
	private ImageView mFirstLoadImageView;

	public static DynamicWallpaperFragment newInstance(int themeData, int onlineFlag) {
		DynamicWallpaperFragment themeMixerBaseFragment = new DynamicWallpaperFragment();
		Bundle args = new Bundle();
		args.putInt("isOnline", onlineFlag);
		themeMixerBaseFragment.setArguments(args);

		return themeMixerBaseFragment;
	}
	
	public DynamicWallpaperFragment() {
		mContext = getActivity();
	}

	public DynamicWallpaperFragment(Context context) {
		mContext = context;
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		ThemeLog.v(TAG, "onResume......");
		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}
//		mGridView.setAdapter(mAdapter);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (mContext == null) {
			mContext = getActivity();
		}
		if (mContext == null) {
			return null;
		}
		View view = inflater.inflate(R.layout.local_theme_mixer_fragment,
				container, false);
		showLoadingView(view);
		mWallpaperManager = WallpaperManager.getInstance(mContext);
		mGridView = (HeaderGridView) view.findViewById(R.id.custom_gridview);
		mGridView.setNumColumns(mNumColumns);
		View spaceView = inflater.inflate(R.layout.space_view, container, false);
		ViewGroup.LayoutParams lp = spaceView.getLayoutParams();
		lp.height = (int)getActivity().getResources().getDimension(R.dimen.space_dyview_height);
		mGridView.addHeaderView(spaceView);
		mAdapter = new LiveWallpaperAdapter(mContext);
		ThemeUtils.setAnimationAdapter(mGridView, ThemeUtils.ANIMATION_TIME, mAdapter);
		mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					int i, long l) {
				i = i - mNumColumns;
				if (i < 0) {
					ThemeLog.e(TAG, "return err onItemClick for i < 0 i :" + i);
					return;
				}
				LiveWallpaperAdapter.LiveWallpaperInfo wallpaperInfo = (LiveWallpaperAdapter.LiveWallpaperInfo) mAdapter
						.getItem(i);
				final Intent intent = wallpaperInfo.intent;
				final WallpaperInfo info = wallpaperInfo.info;
				LiveWallpaperPreview.showPreview(mContext, REQUEST_PREVIEW,
						intent, info);
				getActivity().overridePendingTransition(com.fineos.R.anim.slide_in_right, com.fineos.R.anim.slide_out_left);
			}
		});
		return view;

	}

	/***************************************************************************************/
	public class LiveWallpaperAdapter extends BaseAdapter {
		private static final String LOG_TAG = "LiveWallpaperListAdapter";
		private static final String VIDEO_LIVE_WALLPAPER_PACKAGE = "com.mediatek.vlw";
		private static final String DOOV_TIMER_PACKAGE = "com.idddx.lwp.duowei.dynamictime";

		private final LayoutInflater mInflater;
		private final PackageManager mPackageManager;

		private List<LiveWallpaperInfo> mWallpapers;
		private int mPreviewWidth;
		private int mPreviewHeight;

		@SuppressWarnings("unchecked")
		public LiveWallpaperAdapter(Context context) {
			mInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mPackageManager = context.getPackageManager();

			List<ResolveInfo> list = mPackageManager.queryIntentServices(
					new Intent(WallpaperService.SERVICE_INTERFACE),
					PackageManager.GET_META_DATA);
			mWallpapers = generatePlaceholderViews(list.size());

			mPreviewWidth = (int) getResources().getDimension(
					R.dimen.live_wallpaper_thumbnail_width);// 216
			mPreviewHeight = (int) getResources().getDimension(
					R.dimen.live_wallpaper_thumbnail_height);// 304
			mGridView.setHorizontalSpacing((int) getResources().getDimension(
					R.dimen.local_gridview_horizontalSpacing_allthemes));
			mGridView.setVerticalSpacing((int) getResources().getDimension(
					R.dimen.local_gridview_verticalSpacing_dynamicWallpaper));
			int left = (int) getResources().getDimension(
					R.dimen.local_gridview_paddingLeft_allthemes);
			int right = (int) getResources().getDimension(
					R.dimen.local_gridview_paddingRight_allthemes);
			int top = (int) getResources().getDimension(
					R.dimen.local_gridview_paddingtop_allthemes);
			mGridView.setPadding(left, top, right, 0);
			ThemeLog.v(TAG, "mPreviewWidth(mix_dynamic)=" + mPreviewWidth);
			ThemeLog.v(TAG, "mPreviewHeight(mix_dynamic)=" + mPreviewHeight);
			new LiveWallpaperEnumerator(context).execute(list);
		}

		private List<LiveWallpaperInfo> generatePlaceholderViews(int amount) {
			// / M: just new an empty ArrayList and dynamicly add items to fix
			// the
			// issue caused by VLW. @{
			ArrayList<LiveWallpaperInfo> list = new ArrayList<LiveWallpaperInfo>();
			// / @}
			return list;
		}

		public int getCount() {
			if (mWallpapers == null) {
				return 0;
			}
			return mWallpapers.size();
		}

		public Object getItem(int position) {
			return mWallpapers.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.live_wallpaper_preview_item, parent, false);

				holder = new ViewHolder();
				holder.title = (TextView) convertView.findViewById(R.id.theme_name);
				holder.thumbnail = (ImageView) convertView.findViewById(R.id.preview_image);
				holder.usingTag = (ImageView) convertView.findViewById(R.id.using_tag);
				LayoutParams lp = (LayoutParams) holder.usingTag.getLayoutParams();
				if (lp != null) {
					lp.leftMargin = (int) getResources().getDimension(R.dimen.using_tag_marginleft_live_wallpaper);
					lp.topMargin = (int) getResources().getDimension(R.dimen.using_tag_margintop_live_wallpaper);
				}
				holder.usingTag.setLayoutParams(lp);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			LiveWallpaperInfo wallpaperInfo = mWallpapers.get(position);
			String lable = (String) wallpaperInfo.info.loadLabel(mPackageManager);
			String packageName = wallpaperInfo.info.getPackageName();
			String serviceName = wallpaperInfo.info.getServiceName();
			ThemeLog.v(TAG,	"wallpaperInfo  lable =" + wallpaperInfo.info.loadLabel(mPackageManager));
			ThemeLog.v(TAG,	"wallpaperInfo  package name=" + wallpaperInfo.info.getPackageName());
			ThemeLog.v(TAG,	"wallpaperInfo  ServiceName =" + wallpaperInfo.info.getServiceName());
			ThemeLog.v(TAG, "wallpaperInfo  intent=" + wallpaperInfo.intent);
			if (holder.thumbnail != null) {
				holder.thumbnail.setImageDrawable(wallpaperInfo.thumbnail);
				WallpaperInfo curWallpaperUsing = mWallpaperManager.getWallpaperInfo();
				String packageName2 = null;
				String serviceName2 = null;
				if (curWallpaperUsing != null) {
					packageName2 = curWallpaperUsing.getPackageName();
					serviceName2 = curWallpaperUsing.getServiceName();
				}
				ThemeLog.v(TAG, "wallpaperInfo  curWallpaperUsing="	+ curWallpaperUsing);
				ThemeLog.v(TAG, "wallpaperInfo  package name2=" + packageName2);
				ThemeLog.v(TAG, "wallpaperInfo  ServiceName2 =" + serviceName2);
				if (curWallpaperUsing == null) { // if null,means static
													// wallpaper using
					holder.usingTag.setImageDrawable(null);
				} else if (packageName2.equals(packageName)
						&& serviceName2.equals(serviceName)) {
					ThemeLog.v(TAG, "wallpaperInfo equals,title=" + wallpaperInfo.info.loadLabel(mPackageManager));
					holder.usingTag.setImageResource(R.drawable.ic_theme_focused);
				} else {
					holder.usingTag.setImageDrawable(null);
				}
			}

			if (holder.title != null && wallpaperInfo.info != null) {
				holder.title.setText(wallpaperInfo.info.loadLabel(mPackageManager));
				if (holder.thumbnail == null) {
					holder.title.setCompoundDrawablesWithIntrinsicBounds(null,	wallpaperInfo.thumbnail, null, null);
				}
			}

			return convertView;
		}

		public class LiveWallpaperInfo {
			public Drawable thumbnail;
			public WallpaperInfo info;
			public Intent intent;
		}

		private class ViewHolder {
			TextView title;
			// FlipImageView thumbnail;
			ImageView thumbnail;
			ImageView usingTag;
		}

		private class LiveWallpaperEnumerator extends
				AsyncTask<List<ResolveInfo>, LiveWallpaperInfo, Void> {
			private Context mContext;
			private int mWallpaperPosition;

			public LiveWallpaperEnumerator(Context context) {
				super();
				mContext = context;
				mWallpaperPosition = 0;
			}

			@Override
			protected Void doInBackground(List<ResolveInfo>... params) {
				final PackageManager packageManager = mContext
						.getPackageManager();

				List<ResolveInfo> list = params[0];

				final Resources res = mContext.getResources();
				BitmapDrawable galleryIcon = (BitmapDrawable) res
						.getDrawable(R.drawable.livewallpaper_placeholder);
				Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG
						| Paint.DITHER_FLAG);
				paint.setTextAlign(Paint.Align.CENTER);
				Canvas canvas = new Canvas();

				Collections.sort(list, new Comparator<ResolveInfo>() {
					final Collator mCollator;

					{
						mCollator = Collator.getInstance();
					}

					public int compare(ResolveInfo info1, ResolveInfo info2) {
						return mCollator.compare(
								info1.loadLabel(packageManager),
								info2.loadLabel(packageManager));
					}
				});

				// / M: moved the size computing out of the for loop. @{
				int thumbWidth = res
						.getDimensionPixelSize(R.dimen.live_wallpaper_thumbnail_width);
				int thumbHeight = res
						.getDimensionPixelSize(R.dimen.live_wallpaper_thumbnail_height);
				// / @}

				for (ResolveInfo resolveInfo : list) {
					WallpaperInfo info = null;
					try {
						info = new WallpaperInfo(mContext, resolveInfo);
					} catch (XmlPullParserException e) {
						ThemeLog.w(LOG_TAG, "Skipping wallpaper "
								+ resolveInfo.serviceInfo, e);
						continue;
					} catch (IOException e) {
						ThemeLog.w(LOG_TAG, "Skipping wallpaper "
								+ resolveInfo.serviceInfo, e);
						continue;
					}

					if (VIDEO_LIVE_WALLPAPER_PACKAGE.equals(info.getPackageName()) 
							|| DOOV_TIMER_PACKAGE.equals(info.getPackageName())) {
						ThemeLog.w(LOG_TAG,
								"Skipping wallpaper " + info.getPackageName());
						continue;
					}

					LiveWallpaperInfo wallpaper = new LiveWallpaperInfo();
					wallpaper.intent = new Intent(
							WallpaperService.SERVICE_INTERFACE);
					wallpaper.intent.setClassName(info.getPackageName(),
							info.getServiceName());
					wallpaper.info = info;

					// / M: fix the thumbnail image null bug. @{
					BitmapDrawable thumb = (BitmapDrawable) info
							.loadThumbnail(mPackageManager);
					BitmapDrawable thumbNew;
					if (thumb != null) {
						Bitmap bitmap = Bitmap.createScaledBitmap(
								thumb.getBitmap(), thumbWidth, thumbHeight,
								false);
						thumbNew = new BitmapDrawable(res, bitmap);
					} else {
						Bitmap thumbnail = Bitmap.createBitmap(thumbWidth,
								thumbHeight, Bitmap.Config.ARGB_8888);

						paint.setColor(res
								.getColor(R.color.live_wallpaper_thumbnail_background));
						canvas.setBitmap(thumbnail);
						canvas.drawPaint(paint);

						galleryIcon.setBounds(0, 0, thumbWidth, thumbHeight);
						galleryIcon.setGravity(Gravity.CENTER);
						galleryIcon.draw(canvas);

						String title = info.loadLabel(packageManager)
								.toString();

						paint.setColor(res
								.getColor(R.color.live_wallpaper_thumbnail_text_color));
						paint.setTextSize(res
								.getDimensionPixelSize(R.dimen.live_wallpaper_thumbnail_text_size));

						canvas.drawText(
								title,
								(int) (thumbWidth * 0.5),
								thumbHeight
										- res.getDimensionPixelSize(R.dimen.live_wallpaper_thumbnail_text_offset),
								paint);

						thumbNew = new BitmapDrawable(res, thumbnail);
					}
					wallpaper.thumbnail = thumbNew;
					// / @}
					publishProgress(wallpaper);
				}

				return null;
			}

			@Override
			protected void onProgressUpdate(LiveWallpaperInfo... infos) {
				for (LiveWallpaperInfo info : infos) {
					info.thumbnail.setDither(true);
					if (mWallpaperPosition < mWallpapers.size()) {
						mWallpapers.set(mWallpaperPosition, info);
					} else {
						mWallpapers.add(info);
					}
					mWallpaperPosition++;
					LiveWallpaperAdapter.this.notifyDataSetChanged();
					hideLoadingView();
				}
			}
		}
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

}
