package com.huaqin.market.utils;

import java.util.ArrayList;
import java.util.Hashtable;

import com.huaqin.market.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class CachedThumbnails {

	public static boolean bAllowBufferIcon;
	
	private static final int MAX_SIZE = 50;
	private static final int FREE_COUNT = 10;
	
	private static Hashtable<Integer, Drawable> cachedDrawables = 
			new Hashtable<Integer, Drawable>();
	private static ArrayList<Integer> keys = new ArrayList<Integer>();
	private static Drawable defaultIcon;

	/*
	 * get default icon displayed for applications
	 * when application icon not cached or under loading procedure
	 */
	public static Drawable getDefaultIcon(Context context) {
		// TODO Auto-generated method stub
		if (defaultIcon == null) {
			defaultIcon = context.getResources().getDrawable(R.drawable.icon);
		}
		return defaultIcon;
	}

	/*
	 * push to local map collection, then save to file system
	 */
	public static void cacheThumbnail(Context context, int id, Drawable drawable) {
		// TODO Auto-generated method stub
		pushToCache(id, drawable);
		
		if (bAllowBufferIcon) {
			FileManager.writeAppIconToFile(context, id, drawable);
		}
	}

	/*
	 * Find in local map collection
	 * If not found, then try to read from file system with appId
	 */
	@SuppressWarnings("unused")
	public static Drawable getThumbnail(Context context, int id) {
		// TODO Auto-generated method stub
		if (cachedDrawables.containsKey(id)) {
			return cachedDrawables.get(id);
		} else {
			Drawable drawable = null;

			if (bAllowBufferIcon) {
				if(context != null)
				{
//					if(drawable != null)
//					{
//						drawable.setCallback(null);
//					}
					drawable = FileManager.readAppIconFromFile(context, id);
				}
				if (drawable != null) {
					pushToCache(id, drawable);
				}
			}
			return drawable;
		}
	}

	/*
	 * Push drawable resource to local cache map with appId
	 */
	public static void pushToCache(int id, Drawable drawable) {
		// TODO Auto-generated method stub
		if (cachedDrawables.size() >= MAX_SIZE) {
			// pop the oldest FREE_COUNT items in cachedDrawables
			for (int i = 0; i < FREE_COUNT; i++) {
				cachedDrawables.remove(keys.remove(i));
			}
		}
		cachedDrawables.put(id, drawable);
		// add id to keys collection
		keys.add(id);
	}
}