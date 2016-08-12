package com.huaqin.market.ui;

import com.huaqin.market.R;
import com.huaqin.market.utils.CachedThumbnails;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity
	implements Preference.OnPreferenceChangeListener {

	private static final String KEY_BUFFER_ICON = "settings_buffer_icon";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.settings_preferences);
		
		// add listener for "buffer icon" settings
		((CheckBoxPreference) findPreference(KEY_BUFFER_ICON))
			.setOnPreferenceChangeListener(this);
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		// TODO Auto-generated method stub
		final String key = preference.getKey();
		
		if (key.equals(KEY_BUFFER_ICON)) {
			CachedThumbnails.bAllowBufferIcon = (Boolean) newValue;
		}
		return true;
	}
}