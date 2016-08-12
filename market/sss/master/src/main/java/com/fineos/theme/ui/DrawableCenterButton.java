package com.fineos.theme.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.TextView;

public class DrawableCenterButton extends Button {

	public DrawableCenterButton(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	public DrawableCenterButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DrawableCenterButton(Context context) {
		super(context);
	}

//	@Override
//	protected void onDraw(Canvas canvas) {
//		Drawable[] drawables = getCompoundDrawables();
//		if (drawables != null) {
//			Drawable drawableLeft = drawables[0];
//			if (drawableLeft != null) {
//				float textWidth = getPaint().measureText(getText().toString());
//				int drawablePadding = getCompoundDrawablePadding();
//				int drawableWidth = 0;
//				drawableWidth = drawableLeft.getIntrinsicWidth();
//
//				float bodyWidth = textWidth + drawableWidth + drawablePadding;
//				setPadding(0, 0, (int) (getWidth() - bodyWidth), 0);
//				canvas.translate((getWidth() - bodyWidth) / 2, 0);
//			}
//		}
//		super.onDraw(canvas);
//	}
	
	@Override
	public void setCompoundDrawablesWithIntrinsicBounds(int left, int top,
			int right, int bottom) {
		// TODO Auto-generated method stub
		super.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom);
	}
	
	@Override
	public void setCompoundDrawablesWithIntrinsicBounds(Drawable left,
			Drawable top, Drawable right, Drawable bottom) {
		// TODO Auto-generated method stub
		super.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom);
	}
}
