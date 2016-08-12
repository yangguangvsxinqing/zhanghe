package com.fineos.theme.ui;

import com.fineos.theme.activity.ThemeOnlineHomeActivity;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class ADViewpager extends ViewPager {
    private Context mContext;
	public ADViewpager(Context context) {
		
		super(context);
		// TODO Auto-generated constructor stub
		mContext = context;
	}
	public ADViewpager(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
	}
	private static boolean willIntercept = true;
	
	@Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
		if(arg0.getAction() == MotionEvent.ACTION_DOWN){
			((ThemeOnlineHomeActivity)mContext).stopAutoScroll();
		}
            if(willIntercept){
                    //这个地方直接返回true会很卡
                   return super.onInterceptTouchEvent(arg0);
            }else{
                    return false;
            }
    }
	
    public static void setTouchIntercept(boolean value){
            willIntercept = value;
    }
    
    
}