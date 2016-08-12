package com.huaqin.market.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;

public class LoadingAnimation extends AnimationDrawable {
	
	// Animation rectangle size
	public static final int SIZE_SMALL = 0;
	public static final int SIZE_MEDIUM = 1;
	public static final int SIZE_LARGE = 2;
	
	// Animation step duration
	public static final int DEFAULT_DURATION = 500;
	
	// Animation step count
	private static final int ANI_NUMBER = 4;
	
	// Animation width
	private static final int WIDTH_SMALL = 54;
	private static final int WIDTH_MEDIUM = 90;
	private static final int WIDTH_LARGE = 180;
	
	// Animation height
	private static final int HEIGHT_SMALL = 12;
	private static final int HEIGHT_MEDIUM = 20;
	private static final int HEIGHT_LARGE = 40;
	
	// Animation margin
	private static final int SIDE_SMALL = 6;
	private static final int SIDE_SMALL_F = 8;
	private static final int SIDE_MEDIUM = 10;
	private static final int SIDE_MEDIUM_F = 14;
	private static final int SIDE_LARGE = 20;
	private static final int SIDE_LARGE_F = 28;
	
	private int nColorFocus;
	private int nColorNormal;
	private int nDuration;
	private int nStyle;
	private float nWidth;
	private float nHeight;
	private float nSpace;
	private float nSpaceFocus;

	// default constructor
	public LoadingAnimation(Context context) {

		super();
		
		this.nStyle = SIZE_MEDIUM;
		this.nColorFocus = Color.parseColor("#80ff9600");
		this.nColorNormal = Color.parseColor("#30000000");
		this.nDuration = DEFAULT_DURATION;
		
		initAnimation();
	}

	/*
	 * style - Rectangle size
	 * color1 - Focus Rectangle color id
	 * color2 - Normal Rectangle color id
	 * duration - time to display each frame in AnimationDrawable
	 */
	public LoadingAnimation(Context context, int style, 
			int color1, int color2, int duration) {

		super();
		
		nStyle = style;
		if (color1 == 0) {
			// Default color
			nColorFocus = Color.parseColor("#80ff9600");
			nColorNormal = Color.parseColor("#30000000");
		} else {
			Resources res = context.getResources();
			
			nColorFocus = res.getColor(color1);
			nColorNormal = res.getColor(color2);
		}
		nDuration = duration;
		
		initAnimation();
	}

	private void initAnimation() {
		// TODO Auto-generated method stub		
		switch (nStyle) {
		case SIZE_SMALL:
			nWidth = WIDTH_SMALL;
			nHeight = HEIGHT_SMALL;
			nSpace = SIDE_SMALL / 2.0f;
			nSpaceFocus = SIDE_SMALL_F / 2.0f;
			break;
			
		case SIZE_MEDIUM:
			nWidth = WIDTH_MEDIUM;
			nHeight = HEIGHT_MEDIUM;
			nSpace = SIDE_MEDIUM / 2.0f;
			nSpaceFocus = SIDE_MEDIUM_F / 2.0f;
			break;
			
		case SIZE_LARGE:
			nWidth = WIDTH_LARGE;
			nHeight = HEIGHT_LARGE;
			nSpace = SIDE_LARGE / 2.0f;
			nSpaceFocus = SIDE_LARGE_F / 2.0f;
			break;
			
		default:
			break;
		}
		
		// set to show the animation infinitely
		this.setOneShot(false);
		
		// initialize canvas and paint
		Canvas canvas = new Canvas();
		
		Paint paint1 = new Paint();
		paint1.setColor(nColorFocus);
		paint1.setStyle(Paint.Style.FILL);
		paint1.setAntiAlias(true);
		
		Paint paint2 = new Paint();
		paint2.setColor(nColorNormal);
		paint2.setStyle(Paint.Style.FILL);
		paint2.setAntiAlias(true);	
		
		float x = 0.0f;
		float y = 0.0f;
		// get the Rectangle center y-position
		y = nHeight / 2.0f;
		
		for (int i = 0; i < ANI_NUMBER; i++) {
			Bitmap bitmap =
				Bitmap.createBitmap((int)nWidth, (int)nHeight, Bitmap.Config.ARGB_4444);
			
			if (bitmap != null) {
				canvas.setBitmap(bitmap);
				canvas.drawColor(Color.TRANSPARENT);
				
				for (int j = 0; j < ANI_NUMBER; j++) {
					// get the Rectangle center x-position
					x = ((4 * j + 3) * nSpace);
					if (j == i) {
						// Focus Rectangle
						canvas.drawRect(
								(x - nSpaceFocus),
								(y - nSpaceFocus),
								(x + nSpaceFocus),
								(y + nSpaceFocus),
								paint1);
					} else {
						// Normal Rectangle
						canvas.drawRect(
								(x - nSpace),
								(y - nSpace),
								(x + nSpace),
								(y + nSpace),
								paint2);
					}
				}
				canvas.save();
				this.addFrame(new BitmapDrawable(bitmap), nDuration);
			}
		}
	}

	public int getMinWidth() {
		
		return (int) nWidth;
	}

	public int getMinHeight() {
		
		return (int) nHeight;
	}
}