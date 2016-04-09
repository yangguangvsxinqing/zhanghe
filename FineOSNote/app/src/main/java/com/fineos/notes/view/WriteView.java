package com.fineos.notes.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


import com.fineos.notes.constant.Constant;
import com.fineos.notes.unti.defaults;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


public class WriteView extends View{

	
	private Bitmap mBitmap;
	private Canvas mCanvas;
	private Path mPath;
	private Paint mBitmapPaint;// 画布的画笔
	private Paint mPaint;// 真实的画笔
	private float mX, mY;// 临时点坐标
	private static final float TOUCH_TOLERANCE = 0;
	// 保存Path路径的集合,用List集合来模拟栈
	private List<DrawPath> savePath;
	// 记录Path路径的对象
	private DrawPath dp;
	private int screenWidth, screenHeight;// 屏幕長寬
	private int handWroteMode;
	private int drawpatnsize;
	private String mpath;
	private int paint_color = defaults.STCKET_PAINTS_COLOR;
    private float paint_size = defaults.STCKET_PAINTS_SIZE;
	private Context mContext;
	public OnDrawChange drawChange;
	
	private class DrawPath {
		public Path path;// 路径
		public Paint paint;// 画笔
		public int paintcolor;
		public float paintsize;
		public Bitmap mBitmap;//iamge
		public boolean isPath; 
		public int x;
		public int y;
	 }
	
	
	public interface OnDrawChange{
		
		public void onDrawChange();
	}
	public WriteView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}	
	
	
	public WriteView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		this.mContext = context;
		mBitmapPaint = new Paint(Paint.DITHER_FLAG);
		
		initPaint();
		
		savePath = new ArrayList<DrawPath>();
	}
	
	public WriteView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	

	/*implements this interface to listener*/
	public void setDrawListen(OnDrawChange change){
		drawChange =change;
	}

	
	@Override
	protected void onDraw(Canvas canvas) {
		Log.w("dpc", "WriteView onDraw");
		// TODO Auto-generated method stub
		super.onDraw(canvas);
//		  canvas.drawColor(0xFFEBEBEB);
		canvas.drawColor(0xFFFFFFFF);
		// 将前面已经画过得显示出来
		canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
		if (mPath != null) {
			// 实时的显示
			canvas.drawPath(mPath, mPaint);
		}

		if (drawChange != null) {
			drawChange.onDrawChange();
		}
	}

    public void setBitmaps(Bitmap mbitmap,int x, int y){
    	dp = new DrawPath();
    	dp.mBitmap = mbitmap;
    	dp.isPath = false;
    	dp.paint = mPaint;
    	dp.x = x;
    	dp.y = y;
    	savePath.add(dp);
        Log.w(Constant.TAG, "WriteView mCanvas mPaint:" + mCanvas + "" + mPaint);
        Log.w(Constant.TAG, "WriteView mbitmap:" + mbitmap.getWidth() + "" + mbitmap.getHeight());
    	mCanvas.drawBitmap(mbitmap, x, y, mPaint);
    	invalidate();
    }

    //color
    public void setPaintColor(int color){
    	paint_color = color;
    	initPaint();
    }

    //paint size
    public void setPaintSize(int size){
    	paint_size = size;
    	initPaint();
    }
    
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		screenWidth = w;
		screenHeight =h;
		Log.d("dpc", "onSizeChanged screenWidth:" + screenWidth+","+"screenHeight:"+screenHeight);
		//
		try {
			mBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
		}catch (OutOfMemoryError e) {
			Log.d("dpc", "OutOfMemoryError:" + e);
		}
		// 保存一次一次绘制出来的图形
		mCanvas = new Canvas(mBitmap);
//		mCanvas.drawColor(0xFFEBEBEB);
		mCanvas.drawColor(0xFFFFFFFF);//write
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		float x = event.getX();
		float y = event.getY();
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
		// 每次down下去重新new一个Path
		mPath = new Path();
		//每一次记录的路径对象是不一样的
		dp = new DrawPath();
		dp.path = mPath;
		dp.paint = mPaint;
		dp.isPath = true;
		touch_start(x, y);
		invalidate();
		break;
		case MotionEvent.ACTION_MOVE:
		touch_move(x, y);
		invalidate();
		break;
		case MotionEvent.ACTION_UP:
		touch_up();
		invalidate();
		break;
		}
		return true;
		
	}
	
	
	public void initPaint(){
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setColor(paint_color);		
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);// 设置外边缘
		mPaint.setStrokeCap(Paint.Cap.SQUARE);// 形状
		mPaint.setStrokeWidth(paint_size);// 画笔宽度
		mPaint.setDither(true);
	}
	
	private void touch_start(float x, float y) {
		mPath.moveTo(x, y);
		mX = x;
		mY = y;
	}
	
	
	private void touch_move(float x, float y) {
		float dx = Math.abs(x - mX);
		float dy = Math.abs(mY - y);
		if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
		// 从x1,y1到x2,y2画一条贝塞尔曲线，更平滑(直接用mPath.lineTo也是可以的)
		mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
			//mPath.lineTo(mX, mY);
			
		mX = x;
		mY = y;
		}
	}
			
	private void touch_up() {
		mPath.lineTo(mX, mY);
		mCanvas.drawPath(mPath, mPaint);
		//将一条完整的路径保存下来(相当于入栈操作)
		savePath.add(dp);
		 
		 mPath = null;// 重新置空
		}
		/**
		* 撤销的核心思想就是将画布清空，
		* 将保存下来的Path路径最后一个移除掉，
		* 重新将路径画在画布上面。
		*/
	public void undo() {
		
		mBitmap = Bitmap.createBitmap(screenWidth, screenHeight,
		Bitmap.Config.ARGB_8888);
		mCanvas.setBitmap(mBitmap);// 重新设置画布，相当于清空画布
//		mCanvas.drawColor(0xFFEBEBEB);
		mCanvas.drawColor(0xFFFFFFFF);
		// 清空画布，但是如果图片有背景的话，则使用上面的重新初始化的方法，用该方法会将背景清空掉...
		if (savePath != null && savePath.size() > 0) {
		// 移除最后一个path,相当于出栈操作
		savePath.remove(savePath.size() - 1);
		Iterator<DrawPath> iter = savePath.iterator();
		
		while (iter.hasNext()) {			
		 DrawPath drawPath = iter.next();
		 if(drawPath.isPath){
		    mCanvas.drawPath(drawPath.path, drawPath.paint);
		  }else{
		    mCanvas.drawBitmap(drawPath.mBitmap, drawPath.x, drawPath.y, drawPath.paint);
		  }
		}
		invalidate();// 刷新

		}
	 }
	
	public String getPath(){
		return mpath;
	}

    /**
     *
     * @param Dir
     * @param fileName
     */
    public  void savaBitMap(String Dir,String fileName){
        if(mBitmap == null) return;
        File file = new File(Dir);
        if (!file.exists()){
            Log.w(Constant.TAG,"file.mkdirs()");
            file.mkdirs();
        }
        try {
            Log.w(Constant.TAG,"file.mkdirs()"+Dir+fileName+".PNG");
            FileOutputStream out = new FileOutputStream(Dir+fileName+".PNG");
            mBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
            addGallery(fileName, System.currentTimeMillis(), Dir+ fileName, Dir+fileName+".PNG");
            Log.w(Constant.TAG, "已经保存");
        } catch (FileNotFoundException e) {
            Log.w(Constant.TAG, "saveBitMap --FileNotFoundException");
            e.printStackTrace();
        } catch (IOException e) {
            Log.w(Constant.TAG, "saveBitMap --IOException");
            e.printStackTrace();
        }
    }

	public boolean save(String name){
		
//		SimpleDateFormat sDateFormat =new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
//		Date curDate = new Date(System.currentTimeMillis());
//		String  name =  "IMG_"+sDateFormat.format(curDate);
		
		File dirs = new File(Constant.Dir);
        if (!dirs.exists()){
            dirs.mkdirs();          
        }
        
        mpath =dirs.getAbsolutePath()+"/"+ name + ".PNG";
        File f = new File(mpath);
		try {
		  FileOutputStream fos = new FileOutputStream(f);
		  mBitmap.compress(CompressFormat.PNG, 100, fos);
		  fos.flush();
		  fos.close();
//		  addGallery(name, System.currentTimeMillis(), dirs.getAbsolutePath()+"/"+ name, mpath);
		  return true;
		} catch (FileNotFoundException e) {
		   e.printStackTrace();
		   return false;
		} catch (IOException e) {
		   e.printStackTrace();
		   return false;
		}
		
		
	}
		/**
		* 重做的核心思想就是将撤销的路径保存到另外一个集合里面(栈)，
		* 然后从redo的集合里面取出最顶端对象，
		* 画在画布上面即可。
		*/
	public void redo(){
		
		mBitmap = Bitmap.createBitmap(screenWidth, screenHeight,
		Bitmap.Config.ARGB_8888);
		mCanvas.setBitmap(mBitmap);// 重新设置画布，相当于清空画布
//		mCanvas.drawColor(0xFFEBEBEB);
		mCanvas.drawColor(0xFFFFFFFF);
		// 清空画布，但是如果图片有背景的话，则使用上面的重新初始化的方法，用该方法会将背景清空掉...
		if (savePath != null && savePath.size() > 0) {
			Log.w(Constant.TAG, "WriteView savePath1:" + savePath);
			savePath.clear();
			Log.w(Constant.TAG, "WriteView savePath2:" + savePath);
		}
		 invalidate();// 刷新

	  }

	 /*todatabase*/
	 public void addGallery(String title, long dateTaken,String filename, String filePath){
		 
		 defaults.addImage(mContext.getContentResolver(), title, dateTaken, filename, filePath);
	 }
	 
	public boolean isPathEmpty(){
		
		if(savePath.size() >0){
			return false;
		}
		return true;
	}
	
}
