package com.fineos.theme.activity;

import android.R.integer;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.baidu.mobstat.StatService;
import com.fineos.theme.R;
import com.fineos.theme.fragment.ThemeMixerFragment;
import com.fineos.theme.fragment.ThirdFontsFragment;
import com.fineos.theme.model.CustomData;
import com.fineos.theme.model.ThemeData;
import com.fineos.theme.utils.Constant;

public class ThemeFontActivity extends ThemeBaseFragmentActivity {

	@Override
	protected Fragment createFragment(int onlineFlag) {
		if (isonline() && getApplicationContext().getResources().getBoolean(R.bool.online_fonts_third)) {
			return ThirdFontsFragment.newInstance(ThemeData.THEME_ELEMENT_TYPE_FONT, onlineFlag);
		}
		return ThemeMixerFragment.newInstance(ThemeData.THEME_ELEMENT_TYPE_FONT, onlineFlag);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (mActionBar != null) {
			TextView tv = (TextView) mActionBar.getCustomView().findViewById(R.id.switchonline);
			if (tv != null && !CustomData.isThridFontsSwitchOn(getApplicationContext())) {
				tv.setVisibility(View.INVISIBLE);
			}
		}
	}
	
	@Override
	public View onCreateView(View parent, String name, Context context,
			AttributeSet attrs) {
		// TODO Auto-generated method stub
		return super.onCreateView(parent, name, context, attrs);
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
		if (isonline()) {
			tv.setText(R.string.custom_fonts);
		} else {
			tv.setText(R.string.custom_local_fonts);
		}
		// TODO Auto-generated method stub
		
	}

}