package com.fineos.theme;

import java.io.UnsupportedEncodingException;

import com.fineos.theme.utils.ThemeLog;
import com.fineos.theme.utils.ThemeUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ThridFontsReceiver extends BroadcastReceiver {
	
	public static final String TAG = "ThridFontsReceiver";
	public static final String THRID_ACTION = "com.fineos.thridfonts.apply";
	public static final String THRID_FONTS_PATH = "thridfontspath";

	@Override
	public void onReceive(final Context context, Intent intent) {
		// TODO Auto-generated method stub
		if (intent != null) {
			String actionString = intent.getAction();
			if (actionString != null && actionString.equals(THRID_ACTION)) {
				final String theme = intent.getStringExtra(THRID_FONTS_PATH);
				ThemeLog.w(TAG, "theme :" + theme);
				Thread t = new Thread() {
					public void run() {
						try {
							ThemeUtils.addThemesToDb(context, false);
						} catch (UnsupportedEncodingException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						ThemeUtils.applyTheme(context, theme, true, false, true);
					};
				};
				t.start();
			}
		}
	}
	
}
