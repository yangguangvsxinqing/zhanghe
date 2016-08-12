package com.huaqin.market.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.huaqin.market.download.DownloadManager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

public class FileManager {

	public static final int BUFFER_SIZE = 8192;
	public static final String APP_ICON = "appicon";
	public static final String SUB_ICON = "subicon";
	public static final String CATE_ICON = "cateicon";
	public static final String APP_DIR_NAME = "/hqappmarket";
	
	public static final String APP_DIR_PATH = Environment.getExternalStorageDirectory() + APP_DIR_NAME;
	public static final String APK_DIR_PATH = APP_DIR_PATH + "/apks";
	public static final String ICON_DIR_PATH = APP_DIR_PATH + "/icons";

	/*
	 * Save application icon resource with id
	 */
	public static void writeAppIconToFile(Context context, int id, Drawable drawable) {
		writeIconToFile(context, APP_ICON + id, drawable);
	}

	/*
	 * Save subject icon resource with id
	 */
	public static void writeSubIconToFile(Context context, int id, Drawable drawable) {
		writeIconToFile(context, SUB_ICON + id, drawable);
	}

	/*
	 * Save category icon resource with id
	 */
	public static void writeCateIconToFile(Context context, int id, Drawable drawable) {
		writeIconToFile(context, CATE_ICON + id, drawable);
	}

	/*
	 * Save icon resource with filename
	 */
	public static void writeIconToFile(Context context, String fileName, Drawable drawable) {

		FileOutputStream fOut = null;
		StringBuilder sb = new StringBuilder(ICON_DIR_PATH);
		
		File dir = new File(sb.toString());
		if (!dir.exists()) {
			dir.mkdirs();
		}
		
		sb.append("/" + fileName);
		File file = new File(sb.toString());

		try {
			file.createNewFile();
			fOut = new FileOutputStream(file);
			if(null != drawable){
				BitmapDrawable bDrawable = (BitmapDrawable) drawable;
				Bitmap bmp = bDrawable.getBitmap();
				if(null != bmp){
					bmp.compress(Bitmap.CompressFormat.PNG, 100, fOut);
				}
			}
			fOut.flush();
			fOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Read application icon resource with id
	 */
	public static Drawable readAppIconFromFile(Context context, int id) {
		return readIconFromFile(context, APP_ICON + id);
	}

	/*
	 * Read subject icon resource with id
	 */
	public static Drawable readSubIconFromFile(Context context, int id) {
		return readIconFromFile(context, SUB_ICON + id);
	}

	/*
	 * Read category icon resource with id
	 */
	public static Drawable readCateIconFromFile(Context context, int id) {
		return readIconFromFile(context, CATE_ICON + id);
	}

	/*
	 * Read icon resource with filename
	 */
	public static Drawable readIconFromFile(Context context, String fileName) {

		StringBuilder sb = new StringBuilder(ICON_DIR_PATH);
		
		sb.append("/" + fileName);
		File file = new File(sb.toString());
		if (file != null&&file.exists()) {	
			Drawable drawable = null;
			try { 
				drawable = Drawable.createFromPath(file.toString());
			} catch (OutOfMemoryError e) { 
			    e.printStackTrace(); 
			    return null;
			}
			return drawable;
		} else {
			return null;
		}
	}

	/*
	 * Delete a file by filename
	 */
	public static void deleteFile(Context context, String fileName) {
		// TODO Auto-generated method stub
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			String fullPath = APP_DIR_PATH + "/" + fileName;
			
			File file = new File(fullPath);
			file.delete();
		}
	}

	/*
	 * Delete all cached files
	 * including icon resources file, apk files
	 */
	public static void deleteCacheFiles(Context context) {
		// TODO Auto-generated method stub
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//			File sdDir = Environment.getExternalStorageDirectory();
			File dir = new File(APK_DIR_PATH);
			if(!dir.exists()){
				dir.mkdirs();
			}
			File[] files = dir.listFiles();
			if(null != files){
				for (int i = 0; i < files.length; i++) {
					files[i].delete();
				}
			}
			
			dir = null;
			files = null;
			
			dir = new File(ICON_DIR_PATH);
			if(!dir.exists()){
				dir.mkdirs();
			}
			files = dir.listFiles();
			if(null != files){
				for (int i = 0; i < files.length; i++) {
					files[i].delete();
				}
			}
		}
		
		// delete all completed tasks from DB
		DownloadManager.deleteCompletedTasks(context);
	}
}