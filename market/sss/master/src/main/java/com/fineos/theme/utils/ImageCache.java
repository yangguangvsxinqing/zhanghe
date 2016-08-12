package com.fineos.theme.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.util.LruCache;

import com.fineos.theme.R;

public class ImageCache {

	public static boolean bAllowBufferIcon;

	private static Drawable defaultIcon;
	private static Object mLock = new Object();
	private static LruCache<String, Bitmap> mMemoryCache;

	public static void initCacheThumbnail(){
		int maxMemory = (int) Runtime.getRuntime().maxMemory();  
        int cacheSize = maxMemory / 8;  
        // 设置图片缓存大小为程序最大可用内存的1/8  
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {  
            @Override  
            protected int sizeOf(String key, Bitmap bitmap) {  
                return bitmap.getByteCount();  
            }  
        }; 
	}
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
	public static void cacheThumbnail(final Context context, final int id,final String url, final Bitmap bitmap) {
		// TODO Auto-generated method stub
		synchronized (mLock) {
			if(url!=null){
				final String urlMD5 = Md5.encode(url);
				pushToCache(urlMD5, bitmap);

				// if (bAllowBufferIcon) {
				Thread t = new Thread() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						FileManager.writeAppIconToFile(context, urlMD5, bitmap);
						super.run();
					}
				};
				t.start();
			}
		}
	}
	
	public static void cacheThumbnail(final Context context, final String url, final Bitmap bitmap) {
		// TODO Auto-generated method stub
		synchronized (mLock) {
			if(url!=null){
				final String urlMD5 = Md5.encode(url);
				pushToCache(urlMD5, bitmap);

				// if (bAllowBufferIcon) {
				Thread t = new Thread() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						FileManager.writeAppIconToFile(context, urlMD5, bitmap);
						super.run();
					}
				};
				t.start();
			}
		}
	}

	/*
	 * Find in local map collection If not found, then try to read from file
	 * system with appId
	 */
	public static Drawable getThumbnail(Context context, String url) {
		synchronized (mLock) {
			// TODO Auto-generated method stub
			Bitmap bitmap;
			if(url!=null){
				final String urlMD5 = Md5.encode(url);
				bitmap = mMemoryCache.get(urlMD5);
			if (bitmap==null){
				if (context != null) {
					bitmap = FileManager.readAppIconFromFile(context, urlMD5);
				}
				if (bitmap != null) {
					pushToCache(urlMD5, bitmap);
				}
			}
			if(bitmap!=null){
				return new BitmapDrawable(null, bitmap);
			}
		}
			return null;
	}
	}
	
	public static Bitmap getThumbnailBitmap(Context context, String url) {
		synchronized (mLock) {
			// TODO Auto-generated method stub
			Bitmap bitmap;
			if(url!=null){
				final String urlMD5 = Md5.encode(url);
				bitmap = mMemoryCache.get(urlMD5);
			if (bitmap==null){
				if (context != null) {
					bitmap = FileManager.readAppIconFromFile(context, urlMD5);
				}
				if (bitmap != null) {
					pushToCache(urlMD5, bitmap);
				}
			}
			if(bitmap!=null){
				return bitmap;
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
				Bitmap bitmap = mMemoryCache.get(urlMD5);
			if (bitmap!=null) {
				return new BitmapDrawable(null, bitmap);
			}
			}
			return getDefaultIcon(context);
		}
	}

	@SuppressWarnings("unused")
	public static Drawable getWallPaperThumbnail(Context context, String url) {
		synchronized (mLock) {
			// TODO Auto-generated method stub
			Bitmap bitmap;
			if(url!=null){
				final String urlMD5 = Md5.encode(url);
				bitmap = mMemoryCache.get(urlMD5);
			if (bitmap==null){
				if (context != null) {
					bitmap = FileManager.readWallPaperIconFromFile(context, urlMD5);
				}
				if (bitmap != null) {
					pushToCache(urlMD5, bitmap);
				}
			}
			if(bitmap!=null){
				return new BitmapDrawable(null, bitmap);
			}
		}
			return null;
	}
	}
//	/*
//	 * Push drawable resource to local cache map with appId
//	 */
//	public static void pushToCache(String urlMD5, Drawable drawable) {
//        
//		synchronized (mLock) {
//			if (cachedDrawables.size() >= MAX_SIZE) {
//				for (int i = 0; i < FREE_COUNT; i++) {
//					cachedDrawables.remove(keys.remove(i));
//				}
//			}
//			cachedDrawables.put(urlMD5, drawable);
//			// add id to keys collection
//			keys.add(urlMD5);
//		}
//	}
	
	public static void pushToCache(String urlMD5, Bitmap bitmap) {
        
		synchronized (mLock) {
			
			mMemoryCache.put(urlMD5,bitmap);
		}
		
		
	}
	
	/*
	 * Push drawable resource to local cache map with appId
	 */
	public static void flushCache() {
		synchronized (mLock) {
			 if (mMemoryCache != null) {
		            if (mMemoryCache.size() > 0) {
		                mMemoryCache.evictAll();
		            }
		        mMemoryCache = null;
		     }
		}
	}
	
}