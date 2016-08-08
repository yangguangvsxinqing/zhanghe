/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2010. All rights reserved.
 * 
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

package com.fineos.themecoloreditor;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import android.content.res.AssetManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ServiceManager;
import android.util.Log;
import fineos.content.res.IThemeManagerService;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;

import android.content.Context;

import com.fineos.themecoloreditor.ThemeXmlHelper;


public class ThemeColorApply{
	
	private static final String TAG = null;
	private Context mContext;
	InputStream mInputStream;
	OutputStream mOutputStream;
	List<ThemeColor> mThemeColors;
	private static String assetXmlName =  "theme_values.xml";
	private static String saveXmlName = android.os.Environment.getExternalStorageDirectory() + "/theme_values.xml";
	private static String saveZipName = android.os.Environment.getExternalStorageDirectory() + "/colors";
	private static String copyZipName = "/data/system/theme/colors";
	private ColorItemAdapter coloradapter;

	public ThemeColorApply(Context context){
		mContext = context;
	}
	
	private boolean fileIsExists(String fileName){
		try{
			File f=new File(fileName);
			if(!f.exists()){
				return false;
			}
		}catch (Exception e) {
			return false;
		}
		return true;
	}
	
	private void readXmlFromAssets(){
		AssetManager mngr=mContext.getAssets();
		try {
			mInputStream=mngr.open(assetXmlName);
			mThemeColors = ThemeXmlHelper.getColors(mInputStream);
		} catch (IOException e) {
		} catch (Exception e) {
		}		
	}
	
	private void readXmlFromFile(){
		try {
			mInputStream=new FileInputStream(saveXmlName);
			mThemeColors = ThemeXmlHelper.getColors(mInputStream);
		} catch (IOException e) {
		} catch (Exception e) {
		}		
	}

	public void readXml(){
		if(fileIsExists(saveXmlName)){
			readXmlFromFile();
		}else{
			readXmlFromAssets();
		}
	}
	
	public void writeXml(){
		try {
			mOutputStream = new FileOutputStream(saveXmlName);
			ThemeXmlHelper.save(mThemeColors, mOutputStream);
		} catch (IOException e) {
		} catch (Exception e) {
		}		
	}
	
	public void zipXml(){
		File xmlFile = new File(saveXmlName);
		File zipFile = new File(mContext.getDir("themes", 0), "colors");
		try {
			ZipUtils.zipFile(xmlFile, zipFile, "");
		} catch (IOException e) {
			e.printStackTrace();
		} 		
	}

	public void apply(){
		writeXml();
		zipXml();
	
		IThemeManagerService ts = IThemeManagerService.Stub.asInterface(ServiceManager.getService("ThemeService"));
		try {
			Log.e(TAG, " zhanghe applyTheme start for theme");
			//add by zhanghe0000
//			ts.applyThemeColorURI(ThemeColorProvider.CONTENT + "colors");	
			
			List<String> mColorsListName = new ArrayList<String>();
			List<String> mColorsListValue = new ArrayList<String>();
			String[] arrayColorsName = this.mContext.getResources().getStringArray(R.array.ColorsListName);
//			String[] arrayColorsValue = this.mContext.getResources().getStringArray(R.array.ColorsListValue);
			
			for (int i = 0; i < arrayColorsName.length; i++) {
				mColorsListName.add(arrayColorsName[i]);
//				mColorsListValue.add(arrayColorsValue[i]);
			}
			
			Log.e(TAG, " zhanghe applyTheme start for theme2");
			Log.i(TAG, "zhanghe mColorsListName"+mColorsListName);
			Log.i(TAG, "zhanghe mColorsListValue"+mColorsListValue);
			ThemeColorEditorActivity tea = new ThemeColorEditorActivity();
			
			Log.e(TAG, " zhanghe applyTheme start for theme2");
			Log.i(TAG, "zhanghe1111 mColorsListName"+mColorsListName);
			Log.i(TAG, "zhanghe3333 mColorsListValue"+tea.getColorsListValue());
			ts.applyThemeColorsList(mColorsListName,tea.getColorsListValue());
//			void applyThemeColorsList(in List<String> name, in List<String> colors);
									
			
		} catch (Exception e) {
			Log.e(TAG, "applyTheme exception happened !");
			e.printStackTrace();
		}
	}
	
	
	
	public List<ThemeColor> getColorNameList(){
		readXml();
		return mThemeColors;
	}
	
	public void setColorNameList(List<ThemeColor> themeColors){
		mThemeColors = themeColors;
	}

	
}
