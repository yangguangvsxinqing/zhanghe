package com.example.volley.customview;

import com.example.volley.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Paint.FontMetrics;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.MeasureSpec;

public class CustomImageView extends View{
	private String mTitleText;
	private int mTitleTextColor;
	private int mTitleTextSize;
	private Bitmap mImage;
	private int mImgType;
	private Paint mPaint;
	private Rect mViewShape;
	private Rect mTextRect;
	private int mTextHeight;
//	private int mWidth;
//	private int 
	public CustomImageView(Context context) {
		this(context,null);
		// TODO Auto-generated constructor stub
	}
	
	public CustomImageView(Context context, AttributeSet attrs) {
		this(context, attrs,0);
		// TODO Auto-generated constructor stub
	}

	public CustomImageView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CustomImageView, defStyleAttr, 0);  
		int index = a.getIndexCount();
		for(int i=0;i<index;i++){
			int attr = a.getIndex(i);
			switch (attr){
				case R.styleable.CustomImageView_titleText:
					mTitleText = a.getString(attr);
					break;
				case R.styleable.CustomImageView_titleTextColor:
					mTitleTextColor = a.getColor(attr, 0xffffffff);
					break;
				case R.styleable.CustomImageView_titleTextSize:
					mTitleTextSize = a.getDimensionPixelSize(attr, (int)TypedValue.applyDimension(  
                        TypedValue.COMPLEX_UNIT_SP, 16, getResources().getDisplayMetrics()));
					break;
				case R.styleable.CustomImageView_Image:
					mImage = BitmapFactory.decodeResource(a.getResources(), a.getResourceId(attr, 0));
					break;
				case R.styleable.CustomImageView_ImageScaleType:
					mImgType = a.getInt(attr, 0);
					break;
			}
		}
		a.recycle();
		mPaint = new Paint();
		mViewShape = new Rect();
		mPaint.setTextSize(mTitleTextSize);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
	//	super.onDraw(canvas);
		int width = getMeasuredWidth();
		int height = getMeasuredHeight();
		
		mPaint.setStrokeWidth(3);
		mPaint.setStyle(Style.STROKE);
		mPaint.setColor(Color.CYAN);
		canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), mPaint);
		mViewShape.left = getPaddingLeft();
		mViewShape.right = getMeasuredWidth() - getPaddingRight();
		mViewShape.top = getPaddingTop();
		mViewShape.bottom = getMeasuredHeight() - getPaddingBottom();
		
		mPaint.setColor(mTitleTextColor);
		mPaint.setStyle(Style.FILL);
		FontMetrics fontMetrics = mPaint.getFontMetrics();
		Log.v("", "sss mPaint.measureText(mTitleText)="+mPaint.measureText(mTitleText));
		Log.v("", "sss width="+width);
		
		if (mPaint.measureText(mTitleText) > width)
		{
			TextPaint paint = new TextPaint(mPaint);
			String msg = TextUtils.ellipsize(mTitleText, paint, (float) width - getPaddingLeft() - getPaddingRight(),
					TextUtils.TruncateAt.END).toString();
			canvas.drawText(msg, getPaddingLeft(), height-getPaddingBottom()-fontMetrics.descent, paint);

		} else
		{
			canvas.drawText(mTitleText, (width-mPaint.measureText(mTitleText))/2, height-getPaddingBottom()-fontMetrics.descent, mPaint);
		}
		Log.v("", "sss getMeasuredHeight="+getMeasuredHeight());
		mViewShape.bottom = (int) (getMeasuredHeight()-fontMetrics.descent+fontMetrics.ascent);
		if (mImgType == 0)
		{
			canvas.drawBitmap(mImage, null, mViewShape, mPaint);
		} else
		{
			//计算居中的矩形范围
			mViewShape.left = width / 2 - mImage.getWidth() / 2;
			mViewShape.right = width / 2 + mImage.getWidth() / 2;
			mViewShape.top = (mViewShape.height() - mImage.getHeight()) / 2;
			mViewShape.bottom = (mViewShape.height() + mImage.getHeight()) / 2;
			canvas.drawBitmap(mImage, null, mViewShape, mPaint);
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		// TODO Auto-generated method stub
		super.onLayout(changed, left, top, right, bottom);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
	//	super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int widthModel = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightModel = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		
		int width = 0;
		int height = 0;
		
		if(widthModel == MeasureSpec.EXACTLY){
			width = widthSize;
		}else{
			if(widthModel == MeasureSpec.AT_MOST){
				int textWidth = (int) (getPaddingLeft() + mPaint.measureText(mTitleText) + getPaddingRight());
				int imgWidth = (int) (getPaddingLeft() + mImage.getWidth() + getPaddingRight());
				width = Math.max(textWidth, imgWidth);
			}
		}
		
		if(heightModel == MeasureSpec.EXACTLY){
			height = heightSize;
		}else{
			FontMetrics fontMetrics = mPaint.getFontMetrics();
			if(heightModel == MeasureSpec.AT_MOST){
				int viewHeight = (int)(getPaddingTop() + getPaddingBottom() + fontMetrics.descent - fontMetrics.ascent + mImage.getHeight());
				height = Math.min(heightSize, viewHeight);
			}
		}
		setMeasuredDimension(width, height);
	}

}