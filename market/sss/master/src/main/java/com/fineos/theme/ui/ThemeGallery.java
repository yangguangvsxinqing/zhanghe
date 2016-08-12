package com.fineos.theme.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Gallery;

public class ThemeGallery extends Gallery {
	public ThemeGallery(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public ThemeGallery(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public ThemeGallery(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		// TODO Auto-generated method stub
		return super.onFling(e1, e2, velocityX, velocityY);
		// return true;
	}
}