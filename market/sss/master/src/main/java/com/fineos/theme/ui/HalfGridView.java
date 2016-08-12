package com.fineos.theme.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class HalfGridView extends GridView {
	public HalfGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public HalfGridView(Context context) {
		super(context);
	}

	public HalfGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	// @Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		// int expandSpec = MeasureSpec.makeMeasureSpec(
		// Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
		// super.onMeasure(widthMeasureSpec, expandSpec);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
}