package com.fineos.notes.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaScannerConnection;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.fineos.notes.R;
import com.fineos.notes.bean.Note;
import com.fineos.notes.constant.Constant;
import com.fineos.notes.db.NoteDao;
import com.mediatek.storage.StorageManagerEx;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

//dpc
/**
 * 获得屏幕相关的辅助类
 * 
 * @author zhy
 * 
 */
public class ScreenUtils
{
	private ScreenUtils()
	{
		/* cannot be instantiated */
		throw new UnsupportedOperationException("cannot be instantiated");
	}

	/**
	 * 获得屏幕高度
	 * 
	 * @param context
	 * @return
	 */
	public static int getScreenWidth(Context context)
	{
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics outMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(outMetrics);
		return outMetrics.widthPixels;
	}

	/**
	 * 获得屏幕宽度
	 * 
	 * @param context
	 * @return
	 */
	public static int getScreenHeight(Context context)
	{
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics outMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(outMetrics);
		return outMetrics.heightPixels;
	}

	/**
	 * 获得状态栏的高度
	 * 
	 * @param context
	 * @return
	 */
	public static int getStatusHeight(Context context)
	{

		int statusHeight = -1;
		try
		{
			Class<?> clazz = Class.forName("com.android.internal.R$dimen");
			Object object = clazz.newInstance();
			int height = Integer.parseInt(clazz.getField("status_bar_height")
					.get(object).toString());
			statusHeight = context.getResources().getDimensionPixelSize(height);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return statusHeight;
	}

	/**
	 * 获取当前屏幕截图，包含状态栏
	 * 
	 * @param activity
	 * @return
	 */
	public static Bitmap snapShotWithStatusBar(Activity activity)
	{
		View view = activity.getWindow().getDecorView();
		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache();
		Bitmap bmp = view.getDrawingCache();
		int width = getScreenWidth(activity);
		int height = getScreenHeight(activity);
		Bitmap bp = null;
		bp = Bitmap.createBitmap(bmp, 0, 0, width, height);
		view.destroyDrawingCache();
		return bp;

	}

	/**
	 * 获取当前屏幕截图，不包含状态栏
	 * 
	 * @param activity
	 * @return
	 */
	public static Bitmap snapShotWithoutStatusBar(Activity activity)
	{
		View view = activity.getWindow().getDecorView();
		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache();
		Bitmap bmp = view.getDrawingCache();
		Rect frame = new Rect();
		activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
		int statusBarHeight = frame.top;

		int width = getScreenWidth(activity);
		int height = getScreenHeight(activity);
		Bitmap bp = null;
		bp = Bitmap.createBitmap(bmp, 0, statusBarHeight, width, height
				- statusBarHeight);
		view.destroyDrawingCache();
		return bp;

	}


	public static  Bitmap getBitMapByLayout(Context context,RelativeLayout layout,ScrollView scrollView ,int id){
		List<Note> notes = null;
		Integer[] imageIDshow = { R.drawable.bg_letter_preview1_normal,
				R.drawable.bg_letter_preview2_normal, R.drawable.bg_letter_preview3_normal,
				R.drawable.bg_letter_preview4_normal, R.drawable.bg_letter_preview5_normal};
		int h1 = layout.getHeight();
		int h =0;
		Log.w(Constant.TAG, "ScreenUtils layout.getHeight():" + h1);
		Bitmap bitmap = null;
//		Log.w(Constant.TAG,"getBitmapByView  scrollView.getChildCount():"+scrollView.getChildCount());
		// 获取scrollview实际高度
		for (int i = 0; i < scrollView.getChildCount(); i++) {
			h += scrollView.getChildAt(i).getHeight();
			Log.w(Constant.TAG, "ScreenUtils h:" + h);
			NoteDao dao = new NoteDao(context);
			notes = dao.selectById(id);
			if(notes.size()>0){
				int bg = notes.get(0).getBackground();
				scrollView.getChildAt(i).setBackgroundResource(imageIDshow[bg]);
			}else {
				scrollView.getChildAt(i).setBackgroundColor(
						Color.parseColor("#ffffff"));
			}
		}
		Log.w(Constant.TAG, "ScreenUtils scrollView.getHeight():" + h);
		// 创建对应大小的bitmap
		bitmap = Bitmap.createBitmap(layout.getWidth(), h,
				Bitmap.Config.RGB_565);
		final Canvas canvas = new Canvas(bitmap);
		layout.draw(canvas);
		return bitmap;
	}

	/**
	 * 截取scrollview的屏幕
	 * @param scrollView
	 * @return
	 */
	public static Bitmap getBitmapByView(Context context,ScrollView scrollView,int id,int temp_bg) {
		List<Note> notes = null;
		Integer[] imageIDshow = {R.drawable.bg_letter_share1,
				R.drawable.bg_letter_share2, R.drawable.bg_letter_share3,
				R.drawable.bg_letter_share4, R.drawable.bg_letter_share5};
		int h = 0;
		Bitmap bitmap = null;
		int count = scrollView.getChildCount();
		// 获取scrollview实际高度
		for (int i = 0; i < count; i++) {
			h += scrollView.getChildAt(i).getHeight();
		}
		/*HQ01597861 dpc ,if no color open this code 
		NoteDao dao = new NoteDao(context);
		notes = dao.selectById(id);
		if (notes.size() > 0) {
			int bg = notes.get(0).getBackground();
			scrollView.getChildAt(count - 1).setBackgroundResource(imageIDshow[temp_bg]);

		} else {
			scrollView.getChildAt(count - 1).setBackgroundResource(imageIDshow[temp_bg]);
		}*/

		// 创建对应大小的bitmap
		try {
			bitmap = Bitmap.createBitmap(scrollView.getWidth(), h, Bitmap.Config.RGB_565);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		if(bitmap != null){
			final Canvas canvas = new Canvas(bitmap);
			scrollView.draw(canvas);
		}
		//return bitmap;
		return drawTextToBitmap(context,bitmap);
	}
	
	//HQ01597861 dpc
	private static Bitmap drawTextToBitmap(Context context,Bitmap bitmap) {
		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(Color.parseColor("#303030"));
	        paint.setTextSize(26);
	        canvas.drawText(context.getString(R.string.watermark),32,bitmap.getHeight()-18,paint);
		return bitmap;
	}
	/**
     * 压缩图片
     * @param image
     * @return
     */
    public static Bitmap compressImage(Bitmap image) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int options = 80;
        // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
        while (baos.toByteArray().length / 1024 > 500) {
            // 重置baos
			baos.reset();
            // 这里压缩options%，把压缩后的数据存放到baos中
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);
            // 每次都减少10
			if (options >=10) {
				options -= 10;
			}
        }
        // 把压缩后的数据baos存放到ByteArrayInputStream中
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        // 把ByteArrayInputStream数据生成图片
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
		return bitmap;
    }

    /**
     * 保存到sdcard
     * @param b
     * @return
     */
    public static String savePic(Context context,Bitmap b,String type) {
		//HQ01573778 
		if(b == null)  return null;
		String  preName = context.getString(R.string.app_name);
		//HQ01751707 dpc @{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss",
				Locale.US);
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss",
//				Locale.US);
		//@}
//		String path = Constant.SAVEPICTUREDIR+preName+File.separator;
		String path = StorageManagerEx.getDefaultPath()+File.separator+preName+File.separator;
		Log.d("dpc", "path=" + path);
		File outfile = new File(path);
//		File outfile = context.getExternalFilesDir(parentDir);
		// 如果文件不存在，则创建一个新文件
		if(!outfile.exists()){
			outfile.mkdirs();
		}

		String picName =  preName+"_"+sdf.format(new Date()) + ".png";
		String fname = outfile +File.separator + picName;
		Log.d("dpc", "fname=" + fname);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(fname);
			if (null != fos) {
				b.compress(Bitmap.CompressFormat.PNG, 95, fos);
				fos.flush();
				fos.close();
				saveImageToGallery(context, outfile);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//HQ01682568 dpc @{
		if (type.equals("download")) {
			return getRealPath(context,picName);
		}else {
			return  path+picName;
		}
		//@}
	}

	public static  void saveImageToGallery(Context context,File file) {
		MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()+File.separator}, null, null);
	}

	public static String getRealPath(Context context,String filename){
		String path = StorageManagerEx.getDefaultPath();
		String dir = "";
		path = setRealPath(context,path);
		dir = path + File.separator + context.getString(R.string.app_name)+File.separator;
		showDir = dir+filename;
		return showDir;
	}

	private static final String STORAGE_PATH_SD1 = "/storage/sdcard0";
	private static final String STORAGE_PATH_SD2 = "/storage/sdcard1";
	private static final String STORAGE_PATH_EMULATED = "/storage/emulated/";
	private static final String STORAGE_PATH_SHARE_SD = "/storage/emulated/0";
	private static String showDir = null;
	private static  String setRealPath(Context context,String path){
		if (path.equals(STORAGE_PATH_SHARE_SD)){
			return  context.getString(R.string.inner_storage_name);
		}else if (path.equals(STORAGE_PATH_SD1)) {
			return  context.getString(R.string.inner_storage_name);
		}else if (path.equals(STORAGE_PATH_SD2)){
			return "sdcard1";
		}else if (path.equals(STORAGE_PATH_EMULATED)){
			return context.getString(R.string.inner_storage_name);
		}
		return path;
	}
	//HQ01574105 dpc at 20151216 end
	private void getinputStatus(Context context){
		InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.isFullscreenMode();

	}
}
