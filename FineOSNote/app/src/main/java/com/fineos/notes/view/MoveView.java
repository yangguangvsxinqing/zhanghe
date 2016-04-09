package com.fineos.notes.view;



import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class MoveView extends View{

	public Bitmap cBitmap;
	public Paint rPaint;
	public Paint iPaint;
	
	float x = 0;
	float y = 0;
	float pointx =0;
	float pointy =0;
	public int mwidth = 0;
	public int mheight = 0;
	
	private int imagex;
	private int imagey;
	
	
	
	public int getImagex() {
		Log.w("dpc", "MoveView getLeft,imagex:" + getLeft() + "," + imagex);
		return getLeft() + imagex;
	}

	public int getImagey() {
		Log.w("dpc", "MoveView getTop(),imagey:" + getTop() + "," + imagey);

		return getTop()+imagey;
	}


	public MoveView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public MoveView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		rPaint = new Paint();
		rPaint.setColor(Color.GREEN);
		rPaint.setAntiAlias(true);
		
		iPaint = new Paint();
		iPaint.setAntiAlias(true);
	}
	
	public MoveView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);

		if(cBitmap == null){
			return;
		}
		//Rect r = new Rect(getLeft(),getTop(),getRight(),getBottom());
		
		//canvas.drawRect(r, rPaint);
		int x1 = getLeft();
		int y1 = getTop();
		int x2 = x1 +cBitmap.getWidth();
		int y2 = y1 +cBitmap.getHeight();
		
		//canvas.drawLine(x1, y1, x2, y1, rPaint);
		//canvas.drawLine(x1, y1, x1, y2, rPaint);
		//canvas.drawLine(x2+2, y1, x2+2, y2, rPaint);
		//canvas.drawLine(x1, y2+2, x2, y2+2, rPaint);
		
		imagex = mwidth-cBitmap.getWidth();
		imagey = mheight -cBitmap.getHeight();;
		if(imagex>0){
			imagex = imagex/2;
		}else{
			imagex =0;
		}
		
		if(imagey>0){
			imagey = imagey/2;
		}else{
			imagey =0;
		}
		
		
		if(cBitmap != null){
			canvas.drawBitmap(cBitmap, imagex, imagey, iPaint);			
		}
		//canvas.drawLine(x1, y1, x2, y2, rPaint);
		//canvas.drawLine(x1, y2, x2, y1, rPaint);
		
	}

	/*setBitmap for show*/
	public void setBitmap(Bitmap mbitmap){
		
		cBitmap = mbitmap;
		layout(0, 0, 0 +getWidth(), 0+getHeight());
		this.invalidate();
	}
	

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		
		
	  switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			 x = event.getRawX();
			 y = event.getRawY();
			 
			 pointx = this.getLeft();
			 pointy = this.getTop();
		    break;
		case MotionEvent.ACTION_MOVE:
 
			 float currx = event.getRawX();
			 float curry = event.getRawY();
			 
			 float gap_x = currx - x;
			 float gap_y = curry - y;
			 
			 int x1 = (int)(pointx + gap_x);
			 int y1 = (int)(pointy + gap_y);
			 int x2 = (int)(x1+getWidth());
			 int y2 = (int)y1 +getHeight();
			 Log.d("chenqiwei", "x1>>" + x1 +"-- y1" + y1 +"--x2" +x2 + "--y2" + y2);
			// if(x1 >=0 && x2<=getWidth() && y1>=0 && y2<=getHeight()){			
              layout(x1, y1, x1+getWidth(), y1 +getHeight());
			// }
		    break;
		case MotionEvent.ACTION_UP:
	 
		    break;
		 }
		return true;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// TODO Auto-generated method stub
		super.onSizeChanged(w, h, oldw, oldh);
		mwidth = w;
		mheight =h;
		Log.d("chenqiwei", "w>>>" +w);
	}

	
	
	
	
}
