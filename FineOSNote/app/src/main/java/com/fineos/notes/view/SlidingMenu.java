package com.fineos.notes.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.fineos.notes.R;
import com.fineos.notes.constant.Constant;
import com.fineos.notes.util.ScreenUtils;
import com.nineoldandroids.view.ViewHelper;

public class SlidingMenu extends HorizontalScrollView
{
	/**
	 * 屏幕宽度
	 */
	private int mScreenWidth;
	/**
	 * dp
	 */
	private int mMenuRightPadding;
	/**
	 * 菜单的宽度
	 */
	private int mMenuWidth;
	private int mHalfMenuWidth;

	private boolean isOpen;

	private boolean once;

	private ViewGroup mMenu;
	private ViewGroup mContent;
    private boolean isScroll = true;


	public SlidingMenu(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);

	}

	public SlidingMenu(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		mScreenWidth = ScreenUtils.getScreenWidth(context);

		TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.SlidingMenu, defStyle, 0);
		int n = a.getIndexCount();
		for (int i = 0; i < n; i++)
		{
			int attr = a.getIndex(i);
			switch (attr)
			{
			case R.styleable.SlidingMenu_rightPadding:
				// 默认50
				mMenuRightPadding = a.getDimensionPixelSize(attr,
						(int) TypedValue.applyDimension(
								TypedValue.COMPLEX_UNIT_DIP, 50f,
								getResources().getDisplayMetrics()));// 默认为10DP
				break;
			}
		}
		a.recycle();
	}

	public SlidingMenu(Context context)
	{
		this(context, null, 0);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
        Log.w(Constant.TAG,"SlidingMenu onMeasure");
        Log.w(Constant.TAG,"SlidingMenu onMeasure widthMeasureSpec heightMeasureSpec:"+widthMeasureSpec+" "+heightMeasureSpec);
		/**
		 * 显示的设置一个宽度
		 */
        if (!once)
		{
            LinearLayout wrapper = (LinearLayout) getChildAt(0);
            mMenu = (ViewGroup) wrapper.getChildAt(0);
            mContent = (ViewGroup) wrapper.getChildAt(1);

            mMenuWidth = mScreenWidth - mMenuRightPadding;
            mHalfMenuWidth = mMenuWidth / 2;
            mMenu.getLayoutParams().width = mMenuWidth;
            mContent.getLayoutParams().width = mScreenWidth;

        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b)
	{
        Log.w(Constant.TAG,"SlidingMenu onLayout");
        Log.w(Constant.TAG,"SlidingMenu onLayout changed:"+changed);
        super.onLayout(changed, l, t, r, b);	
        if (changed){
            // 将菜单隐藏
            this.scrollTo(mMenuWidth, 0);
            once = true;
        }
    }

	@Override
	public boolean onTouchEvent(MotionEvent ev)
	{
        Log.w(Constant.TAG,"SlidingMenu onTouchEvent");
        Log.w(Constant.TAG,"SlidingMenu onTouchEvent isScroll:"+isScroll);

		//ACTION_MOVE --> ACTION_UP
		int action = ev.getAction();
		switch (action)
		{
		// Up时，进行判断，如果显示区域大于菜单宽度一半则完全显示，否则隐藏
		case MotionEvent.ACTION_UP:
			int scrollX = getScrollX();
			if (scrollX > mHalfMenuWidth)
			{
				this.smoothScrollTo(mMenuWidth, 0);
				isOpen = false;
			} else
			{
				this.smoothScrollTo(0, 0);
				isOpen = true;
			}
			return true;
		}
        Log.w(Constant.TAG,"onTouchEvent isScroll:"+isScroll);
        if (isScroll) {
            return super.onTouchEvent(ev);
        } else {
            return true;
        }

	}

    public void setScroll (boolean b) {
        Log.w(Constant.TAG,"SlidingMenu setScroll");

        isScroll = b;
    }

	/**
	 * 打开菜单
	 */
	public void openMenu()
	{
        Log.w(Constant.TAG,"SlidingMenu openMenu:"+isOpen);

		if (isOpen)
			return;
		this.smoothScrollTo(0, 0);
		isOpen = true;
	}

	/**
	 * 关闭菜单
	 */
	public void closeMenu()
	{
        Log.w(Constant.TAG,"SlidingMenu closeMenu");

		if (isOpen)
		{
			this.smoothScrollTo(mMenuWidth, 0);
			isOpen = false;
		}
	}

	/**
	 * 切换菜单状态
	 */
	public void toggle()
	{
        Log.w(Constant.TAG,"SlidingMenu toggle isOpen:"+isOpen);

        if (isOpen)
		{
			closeMenu();
		} else
		{
			openMenu();
		}
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt)
	{
        Log.w(Constant.TAG,"SlidingMenu onScrollChanged");
            super.onScrollChanged(l, t, oldl, oldt);
            float scale = l * 1.0f / mMenuWidth;
            float leftScale = 1 - 0.3f * scale;
            float rightScale = 0.8f + scale * 0.2f;

            ViewHelper.setScaleX(mMenu, leftScale);
            ViewHelper.setScaleY(mMenu, leftScale);
            ViewHelper.setAlpha(mMenu, 0.6f + 0.4f * (1 - scale));
            ViewHelper.setTranslationX(mMenu, mMenuWidth * scale * 0.7f);

            ViewHelper.setPivotX(mContent, 0);
            ViewHelper.setPivotY(mContent, mContent.getHeight() / 2);
            ViewHelper.setScaleX(mContent, rightScale);
            ViewHelper.setScaleY(mContent, rightScale);
	}

}
