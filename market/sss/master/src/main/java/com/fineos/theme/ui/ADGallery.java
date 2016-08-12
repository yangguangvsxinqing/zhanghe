package com.fineos.theme.ui;

import android.R.attr;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Transformation;
import android.widget.Gallery;

public class ADGallery extends Gallery {

	private static final String TAG = "AlignLeftGallery";

	private int mWidth;

	private int mPaddingLeft;

	private boolean flag;

	private static int firstChildWidth;

	private static int firstChildPaddingLeft;

	private static final int OFFSET = 133;

	public ADGallery(Context context) {
		super(context);

		this.setStaticTransformationsEnabled(true);
	}

	public ADGallery(Context context, AttributeSet attrs) {

		super(context, attrs);
		this.setStaticTransformationsEnabled(true);
	}

	public ADGallery(Context context, AttributeSet attrs, int defStyle) {

		super(context, attrs, defStyle);
		this.setStaticTransformationsEnabled(true);
	}

	private void setAttributesValue(Context context, AttributeSet attrs) {

		TypedArray typedArray = context.obtainStyledAttributes(attrs, new int[] { attr.paddingLeft });

		mPaddingLeft = typedArray.getDimensionPixelSize(0, 0);

		typedArray.recycle();

	}
	private boolean isScrollingLeft(MotionEvent e1, MotionEvent e2)    
    {       
     return e2.getX() > e1.getX();     
    } 
	@Override    
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,    
      float velocityY) {    
     // TODO Auto-generated method stub    
   //  return super.onFling(e1, e2, 0, velocityY);//方法一：只去除翻页惯性    
   //  return false;//方法二：只去除翻页惯性  注：没有被注释掉的代码实现了开始说的2种效果。    
     int kEvent;      
     if(isScrollingLeft(e1, e2)){     
      //Check if scrolling left         
      kEvent = KeyEvent.KEYCODE_DPAD_LEFT;      
      }  else{     
       //Otherwise scrolling right        
       kEvent = KeyEvent.KEYCODE_DPAD_RIGHT;       
       }      
     onKeyDown(kEvent, null);      
     return true;      
     }   

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

		// TODO Auto-generated method stub

		return super.onScroll(e1, e2, distanceX, distanceY);

	}

}
