package com.huaqin.market.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Gallery;

public class MarketGallery extends Gallery {
	public MarketGallery(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public MarketGallery(Context context,AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public MarketGallery(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
//		int kEvent;
//		Log.v("asd", "e1 = "+e1);
//		Log.v("asd", "e2 = "+e2);
////		MarketPage.setTouchIntercept(false);
//        if(isScrollingLeft(e1, e2)){ //Check if scrolling left
//          kEvent = KeyEvent.KEYCODE_DPAD_LEFT;
//        }
//        else{ //Otherwise scrolling right
//          kEvent = KeyEvent.KEYCODE_DPAD_RIGHT;
//        }
//        onKeyDown(kEvent, null);
        super.onFling(e1, e2, velocityX, velocityY);
        return true;  
	}
	
//	@Override
//	public boolean onTouchEvent(MotionEvent event) {
//	    switch (event.getAction()) {
//	    case MotionEvent.ACTION_MOVE: 
//	    	AppInfoActivity.mPager.requestDisallowInterceptTouchEvent(true);
//	        break;
//	    case MotionEvent.ACTION_UP:
//	    case MotionEvent.ACTION_CANCEL:
//	    	AppInfoActivity.mPager.requestDisallowInterceptTouchEvent(false);
//	        break;
//	    }
//	    return true;
//	}
//	@Override
//	public boolean dispatchTouchEvent(MotionEvent ev) {
//		AppInfoActivity.mPager.requestDisallowInterceptTouchEvent(true);
//	return onTouchEvent(ev);
//	}
//
//	@Override
//	public boolean onInterceptTouchEvent(MotionEvent ev) {
//		AppInfoActivity.mPager.requestDisallowInterceptTouchEvent(true);
//	return onTouchEvent(ev);
//	}

//	private boolean isScrollingLeft(MotionEvent e1, MotionEvent e2){
//        return e2.getX() > e1.getX();
//    }

}
