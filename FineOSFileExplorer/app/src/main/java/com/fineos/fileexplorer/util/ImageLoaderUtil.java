package com.fineos.fileexplorer.util;

import android.content.Context;
import android.graphics.Bitmap;

import com.fineos.fileexplorer.R;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;

public class ImageLoaderUtil {
	
	private static DisplayImageOptions options = new DisplayImageOptions.Builder()
			.showImageOnLoading(R.drawable.ic_picture)
			.showImageForEmptyUri(R.drawable.ic_picture)
			.showImageOnFail(R.drawable.ic_picture)
			.cacheInMemory(true)
			.cacheOnDisk(true)
			.considerExifParams(true)
			//.displayer(new RoundedBitmapDisplayer(10))
            .displayer(new SimpleBitmapDisplayer())
			.build();
	
	public static DisplayImageOptions getOption(){
		return options;
	}
	
	public static void initImageLoader(Context context) {
		// This configuration tuning is custom. You can tune every option, you may tune some of them,
		// or you can create default configuration by
		//  ImageLoaderConfiguration.createDefault(this);
		// method.
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
				.threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
/*				.diskCacheFileNameGenerator(new Md5FileNameGenerator()) */
				.diskCacheSize(20 * 1024 * 1024) // 20 Mb
				.memoryCacheSize(20 * 1024 * 1024)
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.writeDebugLogs() // Remove for release app
				.build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);
	}
}
