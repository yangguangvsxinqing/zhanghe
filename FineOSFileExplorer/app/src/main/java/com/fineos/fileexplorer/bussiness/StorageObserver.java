package com.fineos.fileexplorer.bussiness;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.fineos.fileexplorer.activity.DialogActivity;
import com.fineos.fileexplorer.activity.FileExplorerSearchActivity;

import java.util.ArrayList;

public class StorageObserver extends BroadcastReceiver {
	
	private static StorageObserver mInstance= null;
	private static ArrayList<StorageInfoLoader> mLoaderList = new ArrayList<StorageInfoLoader>();
	
	private StorageObserver(StorageInfoLoader loader) {
		mLoaderList.add(loader);				
		buildObserver(loader.getContext());
	}
	
	public static StorageObserver getInstance(StorageInfoLoader loader){
		if(mInstance == null){
			return new StorageObserver(loader);
		}
		mLoaderList.add(loader);
		return mInstance;
	}
	
	public void buildObserver(Context context) {
		IntentFilter filter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
			filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
			filter.addDataScheme("file");
			context.registerReceiver(this, filter);
	}
	
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if(!intent.getBooleanExtra(FileExplorerSearchActivity.NOT_SHOW_DIALOG, false)){
			Intent dialogIntent = new Intent(context, DialogActivity.class);
			dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(dialogIntent);
		}
		for(StorageInfoLoader loader : mLoaderList){
			loader.onContentChanged();
		}
	}
	
}
