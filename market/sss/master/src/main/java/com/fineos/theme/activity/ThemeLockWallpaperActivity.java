package com.fineos.theme.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Window;
import android.widget.TextView;

import com.baidu.mobstat.StatService;
import com.fineos.theme.R;
import com.fineos.theme.fragment.ThemeMixerFragment;
import com.fineos.theme.model.ThemeData;

public class ThemeLockWallpaperActivity extends ThemeBaseFragmentActivity {

	@Override
	protected Fragment createFragment(int onlineFlag) {
		return ThemeMixerFragment.newInstance(ThemeData.THEME_ELEMENT_TYPE_LOCK_WALLPAPER, onlineFlag);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
	protected void switchonLineOrLocal(int onlineFlag) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void setCustomActionBarText(TextView tv) {
		// TODO Auto-generated method stub
		if (isonline()) {
			tv.setText(R.string.custom_lock_wallpaper);
		} else {
			tv.setText(R.string.custom_lock_wallpaper);
		}
		
	}

}