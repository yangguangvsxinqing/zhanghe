/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui;

import android.app.Service;
import android.content.Context;
import android.content.Intent;

import android.os.IBinder;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import android.util.Log;
import android.provider.Settings;
import android.os.SystemProperties;
import android.os.Environment;

public class LoadInitWallPaperService extends Service {
  private static final String PROP_2SDCARD_SWAP = "ro.mtk_2sdcard_swap";
  public static final String FROM_PATH = "/system/fatBackUp";
  private String TO_PATH_SDCARD0 = "/mnt/sdcard/Wallpaper";
  //public static final String TO_PATH_SDCARD1 = "/mnt/sdcard2/Wallpaper";

    @Override
    public void onCreate() {
        super.onCreate();
    }

	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	checkStoragePath();
    	startBackFroundTask();
        return Service.START_STICKY;
    }
	
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

	private void startBackFroundTask(){
		List<String> list = new ArrayList<String>();
		File mfile = new File(FROM_PATH);
  		final File[] files = mfile.listFiles(); 
		new Thread(new Runnable() { 
    		public void run() {
				String fromFilePath = "";
				String toFilePath = "";	
				if(files != null){
					for (int i = 0; i < files.length; i++) {
			  			File file = files[i];
						if(checkIsImageFile(file.getPath())){
							fromFilePath = FROM_PATH+"/"+file.getName();
							toFilePath = TO_PATH_SDCARD0+"/"+file.getName();
							//Log.d("shiguibiao","from = "+fromFilePath+"\n"+" to = "+toFilePath);
							File fromFile = new File(fromFilePath);
							File toFile = new File(toFilePath);
							FileOpreateUtils.copyfile(fromFile,toFile,false);
						}
					
					 }
				}
				Settings.Global.putInt(getContentResolver(), "COPY_RES_TO_WALLPAPER", 1);
				LoadInitWallPaperService.this.stopSelf();
			}
		}).start();
		 
	}

	private boolean checkIsImageFile(String fName) {
		 boolean isImageFile = false;

		 String FileEnd = fName.substring(fName.lastIndexOf(".") + 1,
		   fName.length()).toLowerCase();
		 if (FileEnd.equals("jpg") || FileEnd.equals("gif")
		   || FileEnd.equals("png") || FileEnd.equals("jpeg")
		   || FileEnd.equals("bmp") || FileEnd.equals("mp4") || FileEnd.equals("MP4")) {
		  	isImageFile = true;
		 } else {
		  	isImageFile = false;
		 }

		 return isImageFile;

	} 

	private void checkStoragePath(){
		if (SystemProperties.get(PROP_2SDCARD_SWAP).equals("1")){
			if (Environment.isExternalStorageRemovable()){
				//Log.d("shiguibiao","test 1shiguibiao first set 2");
				TO_PATH_SDCARD0 = "/mnt/sdcard2/Wallpaper";
			} else {
				//Log.d("shiguibiao","test 1shiguibiao first set 1");
				TO_PATH_SDCARD0 = "/mnt/sdcard/Wallpaper";
			}
		} else {
			Log.d("shiguibiao","test 11shiguibiao first set 1");
			TO_PATH_SDCARD0 = "/mnt/sdcard/Wallpaper";
		}

	}
}
