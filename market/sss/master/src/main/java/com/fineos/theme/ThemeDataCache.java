

package com.fineos.theme;

//import android.app.ThemeHelper;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.fineos.theme.model.ThemeData;
import com.fineos.theme.utils.FileManager;
import com.fineos.theme.utils.Md5;
import com.fineos.theme.utils.ThemeLog;
import com.fineos.theme.utils.ThemeUtils;



public class ThemeDataCache {
    @SuppressWarnings("unused")
    public static boolean bAllowBufferIcon;
//	private static Hashtable<String, Drawable> cachedThumbsDrawables = new Hashtable<String, Drawable>();
	private static LruCache<String, Bitmap> mMemoryCache;
//	private static ArrayList<String> keys = new ArrayList<String>();
	private static final HashMap<Integer, ArrayList<String>> mAllKeys = new HashMap<Integer,  ArrayList<String>>();
	private static ArrayList<String> mPreviewkeys = new ArrayList<String>();
	private static Drawable defaultIcon;
	
	private static final int PREVIEW_MAX_SIZE = 4;
	private static final int PREVIEW_FREE_COUNT = 2;
	private static Hashtable<String, Drawable> cachedPreviewDrawables = new Hashtable<String, Drawable>();
	
    private static final String TAG = "ThemeDataCache";
    private static ThemeDataCache INSTANCE;
    private static Object mLock = new Object();
//    private final HashMap<ThemeData, ThemeCacheEntry> mThemeHashMap = new HashMap<ThemeData, ThemeCacheEntry>();
//    private static final ArrayList<ThemeData> mThemeDatas = new ArrayList<ThemeData>();
    private ArrayList<CallBacks> mCallBacks = new ArrayList<CallBacks>();
    private static final HashMap<Integer, ArrayList<ThemeData>> mAllThemeDatas = new HashMap<Integer,  ArrayList<ThemeData>>();
	private static final int MAX_LOCAL_THUMBS_SIZE = 40;
	private static final int RELEALSE_LOCAL_THUMBS_SIZE = 10;
	
	private static final Hashtable<Integer, Hashtable<Integer, BitmapDrawable>> mLocalThumbsIconMap = new Hashtable<Integer, Hashtable<Integer, BitmapDrawable>>();
	private static final Hashtable<Integer, ArrayList<Integer>> mLocalThumbskeys = new Hashtable<Integer, ArrayList<Integer>>();
    
    public interface CallBacks {
    	public void onThemeDataChange();
    }
	
    private ThemeDataCache() {
    	
    }
    
    public static void initCacheThumbnail(){
		int maxMemory = (int) Runtime.getRuntime().maxMemory();  
        int cacheSize = maxMemory / 8;  
        // 设置图片缓存大小为程序最大可用内存的1/8  
        if(mMemoryCache == null){
        	mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {  
                @Override  
                protected int sizeOf(String key, Bitmap bitmap) {  
                    return bitmap.getByteCount();  
                }  
            }; 
        }
	}
    
	public static void flushThemeDataCache(int type) {
		synchronized (mLock) {
			
			cachedPreviewDrawables.clear();
			mPreviewkeys.clear();
			mLocalThumbsIconMap.clear();
			mLocalThumbskeys.clear();
			
			mAllThemeDatas.remove(type);
			mAllKeys.remove(type);
		}
	}
	
	public static void flushPreviewDrawable() {
		synchronized (mLock) {
			
			Iterator<Drawable> listdrawables = cachedPreviewDrawables.values().iterator();
			while (listdrawables.hasNext()) {
				Drawable drawable = listdrawables.next();
				drawable.setCallback(null);
			}
			cachedPreviewDrawables.clear();
			mPreviewkeys.clear();
		}
	}
	
	public static void flushCacheDrawable() {
		synchronized (mLock) {
			if (mMemoryCache != null) {
	            if (mMemoryCache.size() > 0) {
	                mMemoryCache.evictAll();
	            }
	            mMemoryCache = null;
	        }
		}
	}
	
	public static void forcedflushThemeDataCache () {
		synchronized (mLock) {
			mMemoryCache.evictAll();
			cachedPreviewDrawables.clear();
			
			mPreviewkeys.clear();
			mLocalThumbsIconMap.clear();
			mLocalThumbskeys.clear();
			mAllThemeDatas.clear();
			mAllKeys.clear();
		}
	}
	
	public void addCallbackListener(CallBacks callBacks) {
		if (!mCallBacks.contains(callBacks)) {
			mCallBacks.add(callBacks);
		}
	}
	
	public void removeCallBack (CallBacks callBacks) {
		mCallBacks.remove(callBacks);
	}
	
	public void removeCallbacks() {
		mCallBacks.clear();
	}
	
	public static void clearTypeTheme (int type) {
		mAllThemeDatas.remove(type);
		mAllKeys.remove(type);
	}
	
	public static void removeTheme(ThemeData themeData, int type) {
		if (themeData == null) {
			return;
		}
		
		synchronized (mLock) {
			ArrayList<ThemeData> deleteThemeDatas = new ArrayList<ThemeData>();
			ArrayList<ThemeData> mThemeDatas = mAllThemeDatas.get(type);
			ArrayList<String> keys = mAllKeys.get(type);
			if (mThemeDatas == null || keys == null) {
				ThemeLog.w(TAG, "removeTheme mThemeDatas is null return" + " type :" + type + "mAllThemeDatas.size():" + mAllThemeDatas.size());
				return;
			}
			deleteThemeDatas.add(themeData);
			for (ThemeData tempThemeData : mThemeDatas) {
				if (tempThemeData.getId() == themeData.getId()) {
					deleteThemeDatas.add(tempThemeData);
				}
			}
			mThemeDatas.removeAll(deleteThemeDatas);
			keys.remove(Integer.toString(themeData.getId()));
		}
	}
	
	public static void cacheTheme(ThemeData themeData, int type) {
		synchronized (mLock) {
			if (themeData == null) {
				return;
			}
			if (!mAllThemeDatas.containsKey(type)) {
				ArrayList<ThemeData> themeDatas = new ArrayList<ThemeData>();
				mAllThemeDatas.put(type, themeDatas);
			}
			
			if (!mAllKeys.containsKey(type)) {
				ArrayList<String> themeKeys = new ArrayList<String>();
				mAllKeys.put(type, themeKeys);
			}
			ArrayList<String> key = mAllKeys.get(type);
			ArrayList<ThemeData> themedata = mAllThemeDatas.get(type);
			
			if (!key.contains(Integer.toString(themeData.getId()))) {
				themedata.add(themeData);
				key.add(Integer.toString(themeData.getId()));
			}
		}
	}
	
	public static ArrayList<ThemeData> getThemeDatas(int type) {
		if (mAllThemeDatas.containsKey(type)) {
			return mAllThemeDatas.get(type);
		} else {
			return new ArrayList<ThemeData>();
		}
		
	}
	
	/*
	 * push to local map collection, then save to file system
	 */
	public static void cacheThumbnail(final Context context, final String id, String url, final Bitmap bitmap, final int type) {
		// TODO Auto-generated method stub
		if(url!=null){
			final String urlMD5 = Md5.encode(url);
			pushToThumbsCache(url, bitmap, type);
			
			// if (bAllowBufferIcon) {
			Thread t = new Thread() {
				public void run() {
					FileManager.writeAppIconToFile(context, urlMD5, bitmap);
				};
			};
			t.start();
		}
		// }
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
	 * Find in local map collection If not found, then try to read from file
	 * system with appId
	 */
	@SuppressWarnings("unused")
	public static Drawable getThumbnail(Context context, String url, int type) {
		// TODO Auto-generated method stub
		if(url!=null){
			final String urlMD5 = Md5.encode(url);
			Bitmap bmp = mMemoryCache.get(urlMD5);
		if (bmp == null){
			Drawable drawable = null;

			if (context != null) {
				// if(drawable != null)
				// {
				// drawable.setCallback(null);
				// }
				bmp = FileManager.readAppIconFromFile(context, urlMD5);
			}
			if (bmp != null) {
				pushToThumbsCache(urlMD5, bmp, type);
			}
		}
		if(bmp!=null){
			return new BitmapDrawable(null, bmp);
		}
	}
		return null;
	}
	@SuppressWarnings("unused")
	public static Drawable getBusyOnlineThumbnail(Context context, String url, int type) {
		// TODO Auto-generated method stub
		if(url!=null){
			final String urlMD5 = Md5.encode(url);
		
			Bitmap bmp = mMemoryCache.get(urlMD5);
			if (bmp != null){
				return new BitmapDrawable(null, bmp);
			}
		}
		return null;
	}

	@SuppressWarnings("unused")
	public static Drawable getWallPaperThumbnail(Context context, String url, int type) {
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
					pushToThumbsCache(urlMD5, bitmap, type);
				}
			}
			if(bitmap!=null){
				return new BitmapDrawable(null, bitmap);
			}
		}
			return null;
	}
	}
	/*
	 * Push drawable resource to local cache map with appId
	 */
	public static void pushToThumbsCache(String id, Bitmap bitmap, int type) {
		ArrayList<String> keys = mAllKeys.get(type);
		mMemoryCache.put(id, bitmap);
		// add id to keys collection
		if (keys!=null&&!keys.contains(id)) {
			keys.add(id);
		}
	}
	
	/*
	 * push to local map collection, then save to file system
	 */
	public static void cachePreviewDrawable(final Context context, final String url, final Bitmap bitmap) {
		// TODO Auto-generated method stub
		if(url!=null){
			final String urlMD5 = Md5.encode(url);
		Log.v("", "ssss cachePreviewDrawable urlMD5="+urlMD5);
		pushToPreviewCache(urlMD5, bitmap);
		Thread t = new Thread() {
			public void run() {
				FileManager.writePreviewDrawableToFile(context, urlMD5, bitmap);
			}
		};
		t.start();
		}
	}
	
	/*
	 * push to local map collection, then save to file system
	 */
	public static void cacheLocalPreviewDrawable(Context context, String title, Bitmap bitmap) {
		// TODO Auto-generated method stub
		pushToPreviewCache(title, bitmap);
//		FileManager.writePreviewDrawableToFile(context, id, drawable);
	}
	
	/*
	 * Find in local map collection If not found, then try to read from file
	 * system with appId
	 */
	@SuppressWarnings("unused")
	public static Bitmap getLocalPreviewDrawable(Context context, String title, ThemeData themeData, int mixType) {
		// TODO Auto-generated method stub
		Bitmap bitmap = null;
		bitmap = mMemoryCache.get(title);
		if (bitmap == null) {
			bitmap = loadLocalThemePreview(context, themeData, mixType);
			if (bitmap != null) {
				pushToPreviewCache(title, bitmap);
			}
		}
		return bitmap;
	}
	
	private static Bitmap loadLocalThemePreview(Context context,ThemeData themeData, int mixType) {
		String[] previewDrawblesize = ThemeUtils.getPreviewListByType(mixType, themeData);
		InputStream is = null;
		ZipFile zip = null;
		Bitmap bmp = null;
		if (previewDrawblesize == null || previewDrawblesize.length == 0) {
			return null;
		}
		try {
			zip = new ZipFile(themeData.getThemePath());
			int priviewDrawableId = 0;
			if (mixType == ThemeData.THEME_ELEMENT_TYPE_LOCKSCREEN) {
				priviewDrawableId = 1;
			}
			ZipEntry ze = zip.getEntry(previewDrawblesize[priviewDrawableId]);
			is = ze != null ? zip.getInputStream(ze) : null;
			if (is != null) {
				BitmapFactory.Options opts = new BitmapFactory.Options();
				opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
				opts.inSampleSize = 1;
				bmp = BitmapFactory.decodeStream(is, null, opts);
			}
		} catch (IOException e) {
			bmp = null;
		} catch (Exception e) {
			// TODO: handle exception
			bmp = null;
		} finally {
			try {
				if (zip != null) {
					zip.close();
				}
				is.close();				
			} catch (Exception e2) {
				// TODO: handle exception
			}
		}
		return bmp;
	}

	/*
	 * Find in local map collection If not found, then try to read from file
	 * system with appId
	 */
	@SuppressWarnings("unused")
	public static Bitmap getPreviewDrawable(Context context, String url, boolean srollBusy) {
		// TODO Auto-generated method stub
		if(url!=null){
			final String urlMD5 = Md5.encode(url);
			Bitmap bitmap;
			bitmap = mMemoryCache.get(urlMD5);
		if (bitmap == null){
			if (context != null) {
				if (!srollBusy) {
					bitmap = FileManager.readPreviewIconFromFile(context, urlMD5);
				}
			}
			if (bitmap != null) {
				pushToPreviewCache(urlMD5, bitmap);
			}
		}
		return bitmap;
		}
		return null;
	}

	/*
	 * Push drawable resource to local cache map with appId
	 */
	public static void pushToPreviewCache(String id, Bitmap bitmap) {
		
		mMemoryCache.put(id, bitmap);
		// add id to keys collection
		if (!mPreviewkeys.contains(id)) {
			mPreviewkeys.add(id);
		}
	}
	
	public static void cacheLocalThumbs(int id, BitmapDrawable bitmapDrawable, int firstVisiblePosition, int lastVisiblePosition, Hashtable<Integer, Boolean> iconStatusMap, int mixtype) {
		int count = 0;
		Iterator<Integer> itegers = mLocalThumbsIconMap.keySet().iterator();
		while (itegers.hasNext()) {
			Integer keyinInteger = itegers.next();
			count += mLocalThumbsIconMap.get(keyinInteger).size();
		}
		ArrayList<Integer> listkeys = new ArrayList<Integer>();
		if (count > MAX_LOCAL_THUMBS_SIZE) {
			int removesize = 0;
			Iterator<Integer> removeitegers = mLocalThumbskeys.keySet().iterator();
			while (removeitegers.hasNext()) {
				Integer keyinInteger = removeitegers.next();
				if (keyinInteger != mixtype) {
					removesize += mLocalThumbsIconMap.get(keyinInteger).size();
					listkeys.add(keyinInteger);
				}
			}
			for (Integer removeInteger : listkeys) {
				mLocalThumbsIconMap.remove(removeInteger);
				mLocalThumbskeys.remove(removeInteger);
			}
			
			int i = removesize;
			ArrayList<Integer> typekeys = mLocalThumbskeys.get(mixtype);
			while (i < RELEALSE_LOCAL_THUMBS_SIZE) {
				int j = typekeys.size();
				for (; j > 0; j--) {
					Integer integer = typekeys.get(j - 1);					
					int nowpostion = getPostionByThemeId(integer.intValue(), mixtype);
					if (nowpostion < firstVisiblePosition || nowpostion > lastVisiblePosition) {
						mLocalThumbsIconMap.get(mixtype).remove(integer);
						iconStatusMap.remove(integer);
						typekeys.remove(integer);
						break;
					}
				}		
				i++;
			}			
		}
		pushToLocalIconCache(id, bitmapDrawable, mixtype);
	}
	
	public static BitmapDrawable getLocalThumbsIcon(ThemeData themeData, int mixtype) {
		Hashtable<Integer, BitmapDrawable> listdrawables = mLocalThumbsIconMap.get(mixtype);
		if (listdrawables == null) {
			return null;
		}
		return listdrawables.get(themeData.getId());
	}
	
	private static void pushToLocalIconCache(int id, BitmapDrawable bitmapDrawable, int mixtype) {
		Hashtable<Integer, BitmapDrawable> listdrawables = mLocalThumbsIconMap.get(mixtype);
		if (listdrawables == null) {
			listdrawables = new Hashtable<Integer, BitmapDrawable>();
			mLocalThumbsIconMap.put(mixtype, listdrawables);
		}
		listdrawables.put(id, bitmapDrawable);
		ArrayList<Integer> keyArrayList = mLocalThumbskeys.get(mixtype);
		if (keyArrayList == null) {
			keyArrayList = new ArrayList<Integer>();
			mLocalThumbskeys.put(mixtype, keyArrayList);
		}
		if (!keyArrayList.contains(id)) {
			keyArrayList.add(id);
		}
	}
	
	private static int getPostionByThemeId(int nowId, int type) {
		ArrayList<ThemeData> themeDatas = mAllThemeDatas.get(type);
		int themeDataSize = themeDatas.size();
		for (int i = 0; i < themeDataSize; i++) {
			ThemeData themeData = themeDatas.get(i);
			if (themeData.getId() == nowId) {
				return i;
			}
		}
		return -1;		
	}
	
	public static float getIconCacheFile() {
		return FileManager.getIconCacheFilesSize();
	}
	
	public static void deleteFileCaches() {
		FileManager.deleteIconCacheFiles(false);
	}
   
}
