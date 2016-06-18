package com.example.volley.customview;

import com.example.volley.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

public class CustomView1 extends View{
	private String mTitleText;
	private int mTitleTextColor;
	private int mTitleTextSize;
	private Rect mBound;  
    private Paint mPaint;  
	public CustomView1(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}
	public CustomView1(Context context, AttributeSet attrs) {
		this(context, attrs,0);
		// TODO Auto-generated constructor stub
	}
	public CustomView1(Context context, AttributeSet attrs, int defStyle)  
    {  
        super(context, attrs, defStyle);  
        /** 
         * 获得我们所定义的自定义样式属性 
         */  
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CustomTitleView, defStyle, 0);  
        int n = a.getIndexCount();  
        for (int i = 0; i < n; i++)  
        {  
            int attr = a.getIndex(i);  
            switch (attr)  
            {  
            case R.styleable.CustomTitleView_titleText:  
                mTitleText = a.getString(attr);  
                break;  
            case R.styleable.CustomTitleView_titleTextColor:  
                mTitleTextColor = 0xffffffff;  
                break;  
            case R.styleable.CustomTitleView_titleTextSize:  
                // 默认设置为16sp，TypeValue也可以把sp转化为px  
                mTitleTextSize = a.getDimensionPixelSize(attr, (int) TypedValue.applyDimension(  
                        TypedValue.COMPLEX_UNIT_SP, 16, getResources().getDisplayMetrics()));  
                break;  
  
            }  
  
        }  
        a.recycle();  
  
        /** 
         * 获得绘制文本的宽和高 
         */  
        mPaint = new Paint();  
        mPaint.setTextSize(mTitleTextSize);  
        // mPaint.setColor(mTitleTextColor);  
        mBound = new Rect();  
        mPaint.getTextBounds(mTitleText, 0, mTitleText.length(), mBound);  
  
    }
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		mPaint.setColor(0xff66ccff);
		canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), mPaint);
		mPaint.setColor(mTitleTextColor);
		FontMetrics fontMetrics = mPaint.getFontMetrics();
		//(getWidth()-mBound.width())/2
		canvas.drawText(mTitleText, (getWidth()-mPaint.measureText(mTitleText))/2, (getHeight()+fontMetrics.descent-fontMetrics.ascent)/2-fontMetrics.descent, mPaint);
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
	//	super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int widthModel = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightModel = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		
		int width;
		int height;
		FontMetrics fontMetrics = mPaint.getFontMetrics();
		Log.v("", "sss mTitleTextSize="+mTitleTextSize);
		if(widthModel == MeasureSpec.EXACTLY){
			width = widthSize;
		}else{
			mPaint.setTextSize(mTitleTextSize);
			mPaint.measureText(mTitleText);
			width = (int) (getPaddingLeft() + mPaint.measureText(mTitleText) + getPaddingRight());
		}
		
		if(heightModel == MeasureSpec.EXACTLY){
			height = heightSize;
		}else{
			mPaint.setTextSize(mTitleTextSize);
			height = (int) (getPaddingTop() + fontMetrics.descent - fontMetrics.ascent + getPaddingBottom());
		}
		setMeasuredDimension(width, height);
	}  
}