package com.fineos.theme.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

@SuppressLint("ResourceAsColor")
public class ThemePage extends ViewPager {

	private static boolean willIntercept = true;

	public ThemePage(Context context) {
		super(context);
	}

	public ThemePage(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent arg0) {
		if (willIntercept) {
			// 这个地方直接返回true会很卡
			return super.onInterceptTouchEvent(arg0);
		} else {
			return false;
		}
	}

	public static void setTouchIntercept(boolean value) {
		willIntercept = value;
	}
}