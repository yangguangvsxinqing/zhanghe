package com.fineos.theme.utils;

import java.util.ArrayList;
import java.util.Hashtable;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.fineos.theme.R;

public class CachedThumbnails {

	public static boolean bAllowBufferIcon;

	private static final int MAX_SIZE = 50;
	private static final int FREE_COUNT = 10;

	private static Hashtable<String, Drawable> cachedDrawables = new Hashtable<String, Drawable>();
	private static ArrayList<String> keys = new ArrayList<String>();
	private static Drawable defaultIcon;
	private static Object mLock = new Object();

	/*
	 * get default icon displayed for applications when application icon not
	 * cached or under loading procedure
	 */
	public static Drawable getDefaultIcon(Context context) {
		// TODO Auto-generated method stub
		if (defaultIcon == null) {
			defaultIcon = context.getResources().getDrawable(R.drawable.ic_pic_loading);
		}
		return defaultIcon;
	}

	/*
	 * push to local map collection, then save to file system
	 */
	public static void cacheThumbnail(final Context context, final int id,final String url, final Drawable drawable) {
		// TODO Auto-generated method stub
		synchronized (mLock) {
			if(url!=null){
				final String urlMD5 = Md5.encode(url);
				pushToCache(urlMD5, drawable);

				// if (bAllowBufferIcon) {
				Thread t = new Thread() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						FileManager.writeAppIconToFile(context, urlMD5, ((BitmapDrawable)drawable).getBitmap());
						super.run();
					}
				};
				t.start();
			}
		}
		
		// }
	}

	/*
	 * Find in local map collection If not found, then try to read from file
	 * system with appId
	 */
	@SuppressWarnings("unused")
	public static Drawable getThumbnail(Context context, String url) {
		synchronized (mLock) {
			// TODO Auto-generated method stub
			if(url!=null){
				final String urlMD5 = Md5.encode(url);
			
			if (cachedDrawables.containsKey(urlMD5)) {
				return cachedDrawables.get(urlMD5);
			} else {
				Drawable drawable = null;
				
				if (context != null) {
					// if(drawable != null)
					// {
					// drawable.setCallback(null);
					// }
					drawable = new BitmapDrawable(FileManager.readAppIconFromFile(context, urlMD5));
				}
				if (drawable != null) {
					pushToCache(urlMD5, drawable);
				}
				return drawable;
			}
		}
			return null;
	}
	}
	public static Drawable getCacheThumbDrawable (Context context, String url) {
		synchronized (mLock) {
			// TODO Auto-generated method stub
			if(url!=null){
				final String urlMD5 = Md5.encode(url);
			if (cachedDrawables.containsKey(urlMD5)) {
				return cachedDrawables.get(urlMD5);
			}
			}
			return getDefaultIcon(context);
			
		}
	}

	@SuppressWarnings("unused")
	public static Drawable getWallPaperThumbnail(Context context, String url) {
		// TODO Auto-generated method stub
		synchronized (mLock) {
			if(url!=null){
				final String urlMD5 = Md5.encode(url);
			if (cachedDrawables.containsKey(urlMD5)) {
				return cachedDrawables.get(urlMD5);
			} else {
				Drawable drawable = null;

				if (context != null) {
					// if(drawable != null)
					// {
					// drawable.setCallback(null);
					// }
					drawable = new BitmapDrawable(FileManager.readWallPaperIconFromFile(context, urlMD5));
				}
				if (drawable != null) {
					pushToCache(urlMD5, drawable);
				}
				return drawable;
			}
		}
			return null;
	}
	}
	/*
	 * Push drawable resource to local cache map with appId
	 */
	public static void pushToCache(String urlMD5, Drawable drawable) {
		synchronized (mLock) {
			if (cachedDrawables.size() >= MAX_SIZE) {
				for (int i = 0; i < FREE_COUNT; i++) {
					cachedDrawables.remove(keys.remove(i));
				}
			}
			cachedDrawables.put(urlMD5, drawable);
			// add id to keys collection
			keys.add(urlMD5);
		}
	}
	
	/*
	 * Push drawable resource to local cache map with appId
	 */
	public static void flushCache() {
		synchronized (mLock) {
			cachedDrawables.clear();
			keys.clear();
		}
	}
	
}