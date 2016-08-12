package com.fineos.theme.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.LocalActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.mobstat.StatService;
import com.fineos.theme.R;
import com.fineos.theme.adapter.ThemeFragmentAdapter;
import com.fineos.theme.fragment.RingtoneLocalFragment;
import com.fineos.theme.fragment.RingtoneOnlineFragment;
import com.fineos.theme.fragment.RingtoneLocalFragment;
import com.fineos.theme.model.RingtoneData;
import com.fineos.theme.model.ThemeData;
import com.fineos.theme.ui.ThemePage;
import com.fineos.theme.utils.ThemeLog;

public class ThemeRingtoneActivity extends FragmentActivity {
	private ThemePage mPager;// 页卡内容
	private TextView mTab1, mTab2;// 页卡头标
	private int offset = 0;// 动画图片偏移量
	private int currIndex = 0;// 当前页卡编号
	private int bmpW;// 动画图片宽度
	private ImageView cursor;// 动画图片
	private ImageView cursor_static;
	private ArrayList<Fragment> fragmentsList;
	private LocalActivityManager manager = null;

	private LinearLayout mExceptTitle;
	public static WindowManager mWindowManager = null;
	private Context mContext;
	public static MediaPlayer mMediaPlayer = null;
	protected int mElementType = ThemeData.THEME_ELEMENT_TYPE_RINGTONES;
	private static final String TAG = "ThemeRingtoneActivity";
	

	/**
	 * 初始化动画
	 */

	public ThemeRingtoneActivity() {
		mContext = this;
	}

	private void InitImageView() {
		cursor = (ImageView) findViewById(R.id.cursor);
		cursor_static = (ImageView) findViewById(R.id.cursor_static);
		bmpW = BitmapFactory.decodeResource(getResources(), R.drawable.slide_view_arrow_two).getWidth();// 获取图片宽度
		ThemeLog.i(TAG, "bmpW =" + bmpW);
		mWindowManager = getWindowManager();
		DisplayMetrics dm = new DisplayMetrics();
		mWindowManager.getDefaultDisplay().getMetrics(dm);
		int screenW = dm.widthPixels;// 获取分辨率宽度
		int screenH = dm.heightPixels;// 获取分辨率宽度
		ThemeLog.i(TAG, "screenW =" + screenW);
		ThemeLog.i(TAG, "screenH =" + screenH);
		offset = (screenW / 2 - bmpW) / 2;// 计算偏移量
		Matrix matrix = new Matrix();
		matrix.postTranslate(offset + 34, 0);
		cursor.setImageMatrix(matrix);// 设置动画初始位置

	}

	private void InitViewPager() {
		mPager = (ThemePage) findViewById(R.id.vPager);
		fragmentsList = new ArrayList<Fragment>();

		Fragment tab1 = null;
		Fragment tab2 = null;

		if (mElementType == ThemeData.THEME_ELEMENT_TYPE_RINGTONES) {
			tab1 = new RingtoneLocalFragment();
			tab2 = new RingtoneOnlineFragment();
			fragmentsList.add(tab1);
			fragmentsList.add(tab2);

		}

		mPager.setAdapter(new ThemeFragmentAdapter(getSupportFragmentManager(), fragmentsList));
		// mPager.setCurrentItem(0);
		mPager.setCurrentItem(0);
		mPager.setOnPageChangeListener(new MyOnPageChangeListener());
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		ThemeLog.i(TAG, "currentapiVersion: " + currentapiVersion);
		if (currentapiVersion <= Build.VERSION_CODES.KITKAT) {
			setTheme(com.fineos.internal.R.style.Theme_Holo_Light_FineOS);
		} else {
			setTheme(com.fineos.internal.R.style.FineOSTheme_Material_Light_WhiteActionBar);
		}
		
		setContentView(R.layout.local_theme_mixer_fragment_activity);

		getActionBar().setTitle(getResources().getString(R.string.custom_ringtone));
		getActionBar().setDisplayUseLogoEnabled(false);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setDisplayShowHomeEnabled(false);

		mTab1 = (TextView) findViewById(R.id.tab1);
		mTab2 = (TextView) findViewById(R.id.tab2);

		mExceptTitle = (LinearLayout) findViewById(R.id.except_title);

		ThemeLog.i(TAG, "onCreate");
		manager = new LocalActivityManager(this, true);
		manager.dispatchCreate(savedInstanceState);
		displayByMixType();
		InitImageView();
		InitViewPager();

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		StatService.onResume(this);

		if (mMediaPlayer == null) {
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		}

	}

	public void onPause() {
		super.onPause();

		StatService.onPause(this);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mMediaPlayer != null) {
			mMediaPlayer = null;
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

	/**
	 * 初始化头标,tab显示
	 * 
	 */
	private void hideTabs() {
		mTab1.setVisibility(View.GONE);
		mTab2.setVisibility(View.GONE);
		cursor.setVisibility(View.GONE);
		cursor_static.setVisibility(View.GONE);
	}

	private void displayByMixType() {

		mTab1.setText(R.string.tab_title_local_ringtone);
		mTab2.setText(R.string.tab_title_download_ringtone);

		mTab1.setTextColor(0xffeb624b);
		mTab2.setTextColor(0xff646464);
		mTab1.setOnClickListener(new MyOnClickListener(0));
		mTab2.setOnClickListener(new MyOnClickListener(1));
	}

	public static void playMusic(RingtoneData ringtoneInfo) {
		if (mMediaPlayer != null) {
			try {
				mMediaPlayer.reset();
				if (ringtoneInfo.getThemePath() != null) {
					mMediaPlayer.setDataSource(ringtoneInfo.getThemePath().toString());
				}

				mMediaPlayer.prepare();
				mMediaPlayer.start();
				mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
					public void onCompletion(MediaPlayer arg0) {
						if (mMediaPlayer != null) {
							mMediaPlayer.stop();
						}

					}
				});
			} catch (IOException e) {
			}
		}
	}

	public static void stopMusic() {

		if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			mMediaPlayer.stop();
		}
	}

	public static Boolean isPlaying() {
		return mMediaPlayer != null ? mMediaPlayer.isPlaying() : false;
	}

	public Fragment getFragment() {
		return fragmentsList.get(0);
	}

	/**
	 * ViewPager适配器
	 */
	public class MyPagerAdapter extends PagerAdapter {
		public List<View> mListViews;

		public MyPagerAdapter(List<View> mListViews) {
			this.mListViews = mListViews;
		}

		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
			((ViewPager) arg0).removeView(mListViews.get(arg1));
		}

		@Override
		public void finishUpdate(View arg0) {
		}

		@Override
		public int getCount() {
			return mListViews.size();
		}

		@Override
		public Object instantiateItem(View arg0, int arg1) {
			((ViewPager) arg0).addView(mListViews.get(arg1), 0);
			return mListViews.get(arg1);
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == (arg1);
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {
		}
	}

	/**
	 * 头标点击监听
	 */
	public class MyOnClickListener implements View.OnClickListener {
		private int index = 0;

		public MyOnClickListener(int i) {
			index = i;
		}

		@Override
		public void onClick(View v) {
			mPager.setCurrentItem(index);
		}
	};

	/**
	 * 页卡切换监听
	 */
	public class MyOnPageChangeListener implements OnPageChangeListener {

		int one = offset * 2 + bmpW;// 页卡1 -> 页卡2 偏移量

		@Override
		public void onPageSelected(int arg0) {
			Animation animation = null;
			switch (arg0) {
			case 0:
				if (currIndex == 1) {
					animation = new TranslateAnimation(one, 0, 0, 0);
				}
				mTab1.setTextColor(0xffeb624b);
				mTab2.setTextColor(0xff646464);
				break;
			case 1:
				if (currIndex == 0) {
					animation = new TranslateAnimation(offset, one, 0, 0);
				}
				mTab1.setTextColor(0xff646464);
				mTab2.setTextColor(0xffeb624b);
				break;
			}

			currIndex = arg0;
			ThemeLog.i(TAG, "currIndex = " + currIndex);
			animation.setFillAfter(true);// True:图片停在动画结束位置
			animation.setDuration(300);
			// cursor.setAlpha(100);
			cursor.startAnimation(animation);
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	}

	private View getView(String id, Intent intent) {
		return manager.startActivity(id, intent).getDecorView();
	}
}