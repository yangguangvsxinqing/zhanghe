package com.fineos.theme.utils;

import android.graphics.Typeface;

public class FontResource {
	private String mFontFilePath = null;
	private String mDisplayName = null;
	private String mPackageName = null;
	private Typeface mTypeface = null;
	private boolean isSelected = false;

	public FontResource(String packageName, String displayName, String fontFilePath, Typeface typeface) {
		mPackageName = packageName;
		mFontFilePath = fontFilePath;
		mDisplayName = displayName;
		mTypeface = typeface;
	}

	public Typeface getTypeface() {
		return mTypeface;
	}

	public String getPackageName() {
		return mPackageName;
	}

	public String getDisplayName() {
		return mDisplayName;
	}

	public String getFontFilePath() {
		return mFontFilePath;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean selected) {
		isSelected = selected;
	}
}
