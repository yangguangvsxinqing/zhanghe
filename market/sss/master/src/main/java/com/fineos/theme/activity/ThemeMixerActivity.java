package com.fineos.theme.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;

import com.baidu.mobstat.StatService;
import com.fineos.theme.R;
import com.fineos.theme.adapter.CustomAdapter;
import com.fineos.theme.model.CustomData;
import com.fineos.theme.ui.HalfGridView;
import com.fineos.theme.utils.Constant;

public class ThemeMixerActivity extends Activity {
	private HalfGridView mGridView;

	private ArrayList<CustomData> mList;
	private CustomAdapter mAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentapiVersion <= Build.VERSION_CODES.KITKAT) {
			setTheme(com.fineos.internal.R.style.Theme_Holo_Light_FineOS);
		} else {
			setTheme(com.fineos.internal.R.style.FineOSTheme_Material_Light_WhiteActionBar);
		}
		
		setContentView(R.layout.local_theme_mixer_main);

		initView();
	}

	private void initView() {
		getActionBar().setTitle(getResources().getString(R.string.home_btn_mix));
		getActionBar().setDisplayUseLogoEnabled(false);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setDisplayShowHomeEnabled(false);

		mGridView = (HalfGridView) findViewById(R.id.custom_gridview);
		mGridView.setVerticalSpacing((int) getResources().getDimension(R.dimen.local_gridview_verticalSpacing_fonts));

		mList = CustomData.loadMixerOnlineIcon(this);
		mAdapter = new CustomAdapter(this, mList, mOnClickListener, R.layout.custom_gridview_item_ol);
		mGridView.setAdapter(mAdapter);
	}

	OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			onOperateClick((Integer) v.getTag());
		}
	};

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void onOperateClick(Integer id) {
		Intent intent = null;
		switch (id) {
		case CustomData.CUSTOM_ITEM_ICON:
			intent = new Intent(this, ThemeIconActivity.class);
			intent.putExtra("onlineFlag", Constant.THEME_ONLINE_LIST_TYPE);
			startActivity(intent);

			StatService.onEvent(this, "OnlineIconClick", "click");
			break;
		case CustomData.CUSTOM_ITEM_LOCKSCREEN:
			intent = new Intent(this, ThemeLockScreenActivity.class);
			intent.putExtra("onlineFlag", Constant.THEME_ONLINE_LIST_TYPE);
			startActivity(intent);

			StatService.onEvent(this, "OnlineLockScreenClick", "click");
			break;
		case CustomData.CUSTOM_WALLPAPER:
			intent = new Intent(this, ThemeOLWallpaperActivity.class);
			intent.putExtra("onlineFlag", Constant.THEME_ONLINE_LIST_TYPE);
			startActivity(intent);

			StatService.onEvent(this, "OnlineWallpaperClick", "click");
			break;
		case CustomData.CUSTOM_ITEM_FONT:
			intent = new Intent(this, ThemeFontActivity.class);
			intent.putExtra("onlineFlag", Constant.THEME_ONLINE_LIST_TYPE);
			startActivity(intent);

			StatService.onEvent(this, "OnlineFontClick", "click");
			break;
		case CustomData.CUSTOM_ITEM_DYNAMIC_WALLPAPER:
			intent = new Intent(this, ThemeDynamicWallpaper.class);
			intent.putExtra("onlineFlag", Constant.THEME_ONLINE_LIST_TYPE);
			startActivity(intent);

			StatService.onEvent(this, "OnlineFontClick", "click");
			break;
		default:
			break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		StatService.onResume(this);
	}

	public void onPause() {
		super.onPause();

		StatService.onPause(this);
	}

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
}