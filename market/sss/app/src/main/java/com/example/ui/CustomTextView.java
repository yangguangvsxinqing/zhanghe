package com.example.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;

import com.ubuntu.example.R;

/**
 * Created by ubuntu on 16-7-11.
 */
public class CustomTextView extends TextView{


    private int mTextStartX;
    private static String TAG = "CustomTextView";
    public enum Direction
    {
        LEFT , RIGHT ;
    }
    private int mDirection = DIRECTION_LEFT;

    private static final int  DIRECTION_LEFT = 0 ;
    private static final int  DIRECTION_RIGHT= 1 ;

    public void setDirection(int direction)
    {
        mDirection = direction;
    }

    private String mText = "testtesttest";
    private Paint mPaint;
    private int mTextSize = 30;

    private int mTextOriginColor = 0xff000000;
    private int mTextChangeColor = 0xffff0000;

    private Rect mTextBound = new Rect();
    private int mTextWidth;

    private int mRealWidth;

    private float mProgress;

    public CustomTextView(Context context) {
        super(context,null);
        // TODO Auto-generated constructor stub
    }

    public CustomTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // TODO Auto-generated constructor stub
    }

    public CustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        TypedArray ta = context.obtainStyledAttributes(attrs,
                R.styleable.CustomTextView);
        mText = ta.getString(R.styleable.CustomTextView_CustomTitleText);

        mTextSize = ta.getDimensionPixelSize(
                R.styleable.CustomTextView_CustomTitleTextSize, mTextSize);
        mTextOriginColor = ta.getColor(
                R.styleable.CustomTextView_text_origin_color,
                mTextOriginColor);
        mTextChangeColor = ta.getColor(
                R.styleable.CustomTextView_text_change_color,
                mTextChangeColor);
        mProgress = ta.getFloat(R.styleable.CustomTextView_progress, 0);

        mDirection = ta.getInt(R.styleable.CustomTextView_direction, mDirection);

        ta.recycle();

        mPaint.setTextSize(mTextSize);
        measureText();
    }
    @Override
    public void setTextColor(int color) {
        // TODO Auto-generated method stub
        super.setTextColor(color);
        mProgress = 1;
        mTextChangeColor = color;
        requestLayout();
        invalidate();
    }

    private void measureText()
    {
        mTextWidth = (int) mPaint.measureText(mText);
        mPaint.getTextBounds(mText, 0, mText.length(), mTextBound);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        Log.v(TAG,"sss onDraw");
        Log.v(TAG, "sss getMeasuredWidth()="+getMeasuredWidth());
        Log.v(TAG, "sss mTextWidth="+mTextWidth);
        mTextStartX = (int)(getMeasuredWidth() - mTextWidth) / 2;
        int r = (int) (mProgress* mTextWidth +mTextStartX );
        if(mDirection == DIRECTION_LEFT)
        {
            drawChangeLeft(canvas, r);
            drawOriginLeft(canvas, r);
        }else
        {
            drawOriginRight(canvas, r);
            drawChangeRight(canvas, r);
        }
    }

    private void drawChangeRight(Canvas canvas, int r)
    {
        drawText(canvas, mTextChangeColor, (int) (mTextStartX +(1-mProgress)*mTextWidth), mTextStartX+mTextWidth );
    }
    private void drawOriginRight(Canvas canvas, int r)
    {
        drawText(canvas, mTextOriginColor, mTextStartX, (int) (mTextStartX +(1-mProgress)*mTextWidth) );
    }

    private void drawChangeLeft(Canvas canvas, int r)
    {
        drawText(canvas, mTextChangeColor, mTextStartX, (int) (mTextStartX + mProgress * mTextWidth) );
    }

    private void drawOriginLeft(Canvas canvas, int r)
    {
        drawText(canvas, mTextOriginColor, (int) (mTextStartX + mProgress * mTextWidth), mTextStartX +mTextWidth );
    }

    private void drawText(Canvas canvas , int color , int startX , int endX)
    {
        mPaint.setColor(color);
        canvas.save(Canvas.CLIP_SAVE_FLAG);
        canvas.clipRect(startX, 0, endX, getMeasuredHeight());
        canvas.drawText(mText, mTextStartX, getMeasuredHeight() / 2
                + mTextBound.height() / 2, mPaint);
        canvas.restore();
    }

    private int dp2px(float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, getResources().getDisplayMetrics());
    }

    private int sp2px(float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                dpVal, getResources().getDisplayMetrics());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO Auto-generated method stub
        //
        Log.v(TAG,"sss onMeasure");
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public float getProgress() {
        return mProgress;
    }

    public void setProgress(float progress) {
        this.mProgress = progress;
        invalidate();
    }

    private static final String KEY_STATE_PROGRESS = "key_progress";
    private static final String KEY_DEFAULT_STATE = "key_default_state";

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putFloat(KEY_STATE_PROGRESS, mProgress);
        bundle.putParcelable(KEY_DEFAULT_STATE, super.onSaveInstanceState());
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {

        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            mProgress = bundle.getFloat(KEY_STATE_PROGRESS);
            super.onRestoreInstanceState(bundle
                    .getParcelable(KEY_DEFAULT_STATE));
            return;
        }
        super.onRestoreInstanceState(state);
    }


}
