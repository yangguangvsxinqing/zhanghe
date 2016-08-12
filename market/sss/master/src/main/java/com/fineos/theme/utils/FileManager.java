package com.fineos.theme.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;

public class FileManager {

	public static final int BUFFER_SIZE = 8192;
	public static final String APP_ICON = "appicon";
	public static final String PREVIEW_ICON = "previewicon";
	public static final String WALLPAPER_ICON = "wallpapericon";
	public static final String CATE_ICON = "cateicon";
	public static final String APP_DIR_NAME = "/FineOS/.theme";
	public static final String THEME_DIR_PATH = APP_DIR_NAME + "/.themes";
	public static final String APP_DIR_PATH = Environment.getExternalStorageDirectory() + APP_DIR_NAME;
	// public static final String RINGTONE_DIR_PATH = APP_DIR_NAME +
	// "/ringtones";
	public static final String RINGTONE_DIR_PATH = APP_DIR_PATH + "/.ringtones";
	public static final String WALLPAPER_DIR_PATH = APP_DIR_PATH + "/.wallpapers";
	public static final String ICON_DIR_PATH = APP_DIR_PATH + "/.icons";
	public static final String THEME_DATA_FILE = APP_DIR_PATH + "/.pro.dat";
	private static final String TAG = "FileManager";

	/*
	 * Save application icon resource with id
	 */
	public static void writeAppIconToFile(Context context, int id, Bitmap bitmap) {
		writeIconToFile(context, APP_ICON + id, bitmap);
	}
	
	/*
	 * Save application icon resource with id
	 */
	public static void writeAppIconToFile(Context context, String id, Bitmap bitmap) {
		writeIconToFile(context, APP_ICON + id, bitmap);
	}
	
	/*
	 * Save Theme Preview icon resource with id
	 */
	public static void writePreviewDrawableToFile(Context context, int id, Bitmap bitmap) {
		writeIconToFile(context, PREVIEW_ICON + id, bitmap);
	}
	
	/*
	 * Save Theme Preview icon resource with id
	 */
	public static void writePreviewDrawableToFile(Context context, String id, Bitmap bitmap) {
		writeIconToFile(context, PREVIEW_ICON + id, bitmap);
	}

	/*
	 * Save wallpaper icon resource with id
	 */
	public static void writeWallPaperIconToFile(Context context, int id, Bitmap bitmap) {
		writeIconToFile(context, WALLPAPER_ICON + id, bitmap);
	}
	
	/*
	 * Save wallpaper icon resource with id
	 */
	public static void writeWallPaperIconToFile(Context context, String id, Bitmap bitmap) {
		writeIconToFile(context, WALLPAPER_ICON + id, bitmap);
	}

	/*
	 * Save category icon resource with id
	 */
	public static void writeCateIconToFile(Context context, String id, Bitmap bitmap) {
		writeIconToFile(context, CATE_ICON + id, bitmap);
	}

	/*
	 * Save icon resource with filename
	 */
	public static void writeIconToFile(Context context, String fileName, Bitmap bitmap) {

		FileOutputStream fOut = null;
		StringBuilder sb = new StringBuilder(ICON_DIR_PATH);

		File dir = new File(sb.toString());
		if (!dir.exists()) {
			dir.mkdirs();
		}
		ThemeLog.i("writeIconToFile", "fileName =" + fileName);
		ThemeLog.i("writeIconToFile", "drawable =" + bitmap);
		sb.append("/" + fileName);
		File file = new File(sb.toString());

		try {
			file.createNewFile();
			fOut = new FileOutputStream(file);
			if (null != bitmap) {
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
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
	public static Bitmap readAppIconFromFile(Context context, int id) {
		return readIconFromFile(context, APP_ICON + id);
	}
	
	/*
	 * Read application icon resource with id
	 */
	public static Bitmap readAppIconFromFile(Context context, String id) {
		return readIconFromFile(context, APP_ICON + id);
	}
//	
//	/*
//	 * Read application icon resource with id
//	 */
	public static Bitmap readPreviewIconFromFile(Context context, int id) {
		return readIconFromFile(context, PREVIEW_ICON + id);
	}
	
	/*
	 * Read application icon resource with id
	 */
	public static Bitmap readPreviewIconFromFile(Context context, String id) {
		return readIconFromFile(context, PREVIEW_ICON + id);
	}

	/*
	 * Read WallPaper icon resource with id
	 */
	public static Bitmap readWallPaperIconFromFile(Context context, int id) {
		return readIconFromFile(context, WALLPAPER_ICON + id);
	}
	
	/*
	 * Read WallPaper icon resource with id
	 */
	public static Bitmap readWallPaperIconFromFile(Context context, String id) {
		return readIconFromFile(context, WALLPAPER_ICON + id);
	}


//	/*
//	 * Read category icon resource with id
//	 */
//	public static Drawable readCateIconFromFile(Context context, int id) {
//		return readIconFromFile(context, CATE_ICON + id);
//	}
//	
//	/*
//	 * Read category icon resource with id
//	 */
//	public static Drawable readCateIconFromFile(Context context, String id) {
//		return readIconFromFile(context, CATE_ICON + id);
//	}


	/*
	 * Read icon resource with filename
	 */
	public static Bitmap readIconFromFile(Context context, String fileName) {

		StringBuilder sb = new StringBuilder(ICON_DIR_PATH);

		sb.append("/" + fileName);
		File file = new File(sb.toString());
		if (file != null && file.exists()) {
			Bitmap bitmap = null;
			try {
				FileInputStream fis = new FileInputStream(file);
				bitmap  = BitmapFactory.decodeStream(fis);
				fis.close();
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
				return null;
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return bitmap;
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
	 * Delete all cached files including icon resources file, apk files
	 */
	public static void deleteCacheFiles(Context context) {
		// TODO Auto-generated method stub
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			// File sdDir = Environment.getExternalStorageDirectory();
			File dir = new File(THEME_DIR_PATH);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			File[] files = dir.listFiles();
			if (null != files) {
				for (int i = 0; i < files.length; i++) {
					files[i].delete();
				}
			}

			dir = null;
			files = null;

			dir = new File(ICON_DIR_PATH);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			files = dir.listFiles();
			if (null != files) {
				for (int i = 0; i < files.length; i++) {
					files[i].delete();
				}
			}
		}

		// delete all completed tasks from DB
		// DownloadManager.deleteCompletedTasks(context);
	}
	
	public static void deleteIconCacheFiles(boolean ForcedDeleteAllFiles) {
		// TODO Auto-generated method stub
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			// File sdDir = Environment.getExternalStorageDirectory();
			File dir = null;
			dir = new File(ICON_DIR_PATH);
			if (!dir.exists()) {
				return;
			}
			File[] files = dir.listFiles();
			if (null != files) {
				for (int i = 0; i < files.length; i++) {
					String name = files[i].getName();
					if (!ForcedDeleteAllFiles) {
						if (name != null && name.contains(PREVIEW_ICON)) {
							files[i].delete();
						}
					} else {
						files[i].delete();
					}
				}
			}
			if (!ForcedDeleteAllFiles) {
				if (getIconCacheFilesSize() > Constant.THEME_CACHE_MAX_SIZE) { // check the cache size
					deleteIconCacheFiles(true);
				}
			}
		}
	}
	
	public static float getIconCacheFilesSize() {
		long length = 0;
		InputStream in = null;
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			// File sdDir = Environment.getExternalStorageDirectory();
			File dir = null;
			dir = new File(ICON_DIR_PATH);
			if (!dir.exists()) {
				return length;
			}
			File[] files = dir.listFiles();
			if (null != files) {
				for (int i = 0; i < files.length; i++) {
					length += files[i].length();
				}
			}
		}
		float fileSizeMB = length / (1024.0f*1024.0f);
		return fileSizeMB;
	}
	
}