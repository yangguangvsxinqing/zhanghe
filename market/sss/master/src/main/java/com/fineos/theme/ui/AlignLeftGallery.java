package com.fineos.theme.ui;

import android.R.attr;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Transformation;
import android.widget.Gallery;

public class AlignLeftGallery extends Gallery {

	private static final String TAG = "AlignLeftGallery";

	private Camera mCamera;

	private int mWidth;

	private int mPaddingLeft;

	private boolean flag;

	private static int firstChildWidth;

	private static int firstChildPaddingLeft;

	private static final int OFFSET = 115;

	public AlignLeftGallery(Context context) {
		super(context);

		mCamera = new Camera();
		this.setStaticTransformationsEnabled(true);
	}

	public AlignLeftGallery(Context context, AttributeSet attrs) {

		super(context, attrs);
		mCamera = new Camera();
		this.setStaticTransformationsEnabled(true);
	}

	public AlignLeftGallery(Context context, AttributeSet attrs, int defStyle) {

		super(context, attrs, defStyle);
		mCamera = new Camera();
		this.setStaticTransformationsEnabled(true);
	}

	private void setAttributesValue(Context context, AttributeSet attrs) {

		TypedArray typedArray = context.obtainStyledAttributes(attrs, new int[] { attr.paddingLeft });

		mPaddingLeft = typedArray.getDimensionPixelSize(0, 0);

		typedArray.recycle();

	}

	// 控制gallery中每个图片的旋转 ,继承自 gallery
	protected boolean getChildStaticTransformation(View child, Transformation t) {

		t.clear();

		t.setTransformationType(Transformation.TYPE_MATRIX);

		mCamera.save();

		final Matrix imageMatrix = t.getMatrix();

		final int imageHeight = child.getLayoutParams().height;

		final int imageWidth = child.getLayoutParams().width;

		if (flag) {

			firstChildWidth = getChildAt(0).getWidth();

			firstChildPaddingLeft = getChildAt(0).getPaddingLeft();

			flag = false;

		}
		// offset = firstChildWidth / 2 + firstChildPaddingLeft
		// + mPaddingLeft - mWidth / 2 ;
		// mCamera.translate(firstChildWidth / 2 + firstChildPaddingLeft
		// + mPaddingLeft - mWidth / 2, 0f, 0f);
		// mCamera.getMatrix(imageMatrix);

		mCamera.restore();

		return true;

	}

	protected void onSizeChanged(int w, int h, int oldw, int oldh) {

		if (!flag) {

			mWidth = w;

			getLayoutParams().width = mWidth;

			flag = true;

		}

		super.onSizeChanged(w, h, oldw, oldh);

	}

	public void alignGalleryToLeft(Float density) {
		MarginLayoutParams mlp = (MarginLayoutParams) this.getLayoutParams();
		mlp.setMargins(Math.round(-OFFSET * density), mlp.topMargin, mlp.rightMargin, mlp.bottomMargin);
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

		// TODO Auto-generated method stub

		// return super.onFling(e1, e2, velocityX, velocityY);

		return false;

	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

		// TODO Auto-generated method stub

		return super.onScroll(e1, e2, distanceX, distanceY);

	}

}
