package com.example.volley;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class HscrollRecyclerView extends RecyclerView{
	
	public OnItemScrollChangeListener mItemScrollChangeListener;
	public View mCurrentView;
	
	public HscrollRecyclerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	//	this.setOnScrollListener(this);
	}
	
	public void setOnItemScrollChangeListener(
			OnItemScrollChangeListener mItemScrollChangeListener)
	{
		this.mItemScrollChangeListener = mItemScrollChangeListener;
	}
	
	public interface OnItemScrollChangeListener
	{
		void onChange(View view, int position);
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b)
	{
		super.onLayout(changed, l, t, r, b);
		mCurrentView = getChildAt(0);
		if (mItemScrollChangeListener != null)
		{
			Log.v("", "sss mCurrentView="+getChildPosition(mCurrentView));
			mItemScrollChangeListener.onChange(mCurrentView,
					getChildPosition(mCurrentView));
		}
	}
}