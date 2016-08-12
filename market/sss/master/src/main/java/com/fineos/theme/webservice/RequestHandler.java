package com.fineos.theme.webservice;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.provider.MediaStore;
import android.util.Log;

import com.fineos.android.rom.sdk.bean.SubjectAdList;
import com.fineos.android.rom.sdk.bean.ThemeList;
import com.fineos.billing.util.IabHelper;
import com.fineos.theme.model.Image2;
import com.fineos.theme.model.ThemeData;
import com.fineos.theme.preview.PreviewHelper;
import com.fineos.theme.utils.Constant;
import com.fineos.theme.utils.FileManager;
import com.fineos.theme.utils.ThemeBillingHelper;
import com.fineos.theme.utils.ThemeLog;
import com.fineos.theme.utils.ThemeUtils;

public class RequestHandler extends Thread {

	private static final String THREAD_NAME = "RequestHandler";

	public boolean bIsRunning;
	private int nThreadId;
	private ThemeService mService;
	private ThemeServiceAgent mAgent;
	
	protected ArrayList<String> mSkuList = new ArrayList<String>() ;
	protected ThemeBillingHelper mBillingHelper = null ;
	

	public RequestHandler(ThemeService service, int threadId) {
		// set thread name according to thread id
		super((THREAD_NAME + threadId));

		this.nThreadId = threadId;
		this.mService = service;
		this.bIsRunning = true;
		this.mAgent = mService.getAgentInstance();
	}

	public static InputStream fetch(ThemeData theme, int elementType) throws IOException {
		String previewName = null;
		try {
			switch (elementType) {
			case ThemeData.THEME_ELEMENT_TYPE_ICONS: // icons
				previewName = PreviewHelper.getIconPreviewsSmall(theme)[0];
				break;
			case ThemeData.THEME_ELEMENT_TYPE_WALLPAPER:
				previewName = PreviewHelper.getWallpaperPreviews(theme)[1]; // 0=
																			// wallpaper;1=
																			// small
																			// wallpaper
				break;
			case ThemeData.THEME_ELEMENT_TYPE_LOCK_WALLPAPER:
				previewName = PreviewHelper.getLockWallpaperPreviews(theme)[0];
				break;
			case ThemeData.THEME_ELEMENT_TYPE_SYSTEMUI:
				previewName = PreviewHelper.getStatusbarPreviews(theme)[0];
				break;
			case ThemeData.THEME_ELEMENT_TYPE_FRAMEWORK:
				previewName = PreviewHelper.getLauncherPreviews(theme)[0];
				break;
			case ThemeData.THEME_ELEMENT_TYPE_CONTACTS:
				previewName = PreviewHelper.getContactsPreviews(theme)[0];
				break;
			case ThemeData.THEME_ELEMENT_TYPE_DIALER:
				previewName = PreviewHelper.getDialerPreviews(theme)[0];
				break;
			case ThemeData.THEME_ELEMENT_TYPE_RINGTONES:
				previewName = PreviewHelper.getContactsPreviews(theme)[0];
				break;
			case ThemeData.THEME_ELEMENT_TYPE_BOOTANIMATION:
				previewName = PreviewHelper.getBootanimationPreviews(theme)[0];
				break;
			case ThemeData.THEME_ELEMENT_TYPE_MMS:
				previewName = PreviewHelper.getMmsPreviews(theme)[0];
				break;
			case ThemeData.THEME_ELEMENT_TYPE_FONT: // font
				previewName = PreviewHelper.getFontsPreviewsSmall(theme)[0];
				break;
			case ThemeData.THEME_ELEMENT_TYPE_LOCKSCREEN:
				previewName = PreviewHelper.getLockScreenPreviews(theme)[0];
				break;

			}
		} catch (Exception e) {
			previewName = null;
		}
		if (theme.getThemePath() == null) {
			ThemeLog.e("RequestHandler", "ThemePath is null err");
			return null;
		}
		ZipFile zip = new ZipFile(theme.getThemePath());
		ZipEntry ze = previewName != null ? zip.getEntry(previewName) : null;
		InputStream is = ze != null ? zip.getInputStream(ze) : null;
//		zip.close();
		return is;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true) {
			if (!bIsRunning) {
				continue;
			}
			Request request = null;
			Object[] params = null;
			Object param = null;
			try {
				request = (Request) mService.popRequest(nThreadId);
				if (request == null) {
					continue;
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			}
			int startIndex;
			int type;
			int download;
			int page;
			String userId;
			String loginId;
			String themeId;
			String appVersion;
			String pageName;
			String phoneModel;
			String osVersion;
			String stars;
			String content;
			String channelId;
			Image2 img;
			List<ThemeData> mThemeList = null;
			switch (request.getType()) {
			case Constant.TYPE_ICON_LIST:
				if (request.getData() != null) {
					params = (Object[]) request.getData();
					Context context = (Context) params[0];
					ThemeLog.i("TYPE_ICON_LIST", "context =" + context);
					if (context != null) {
						int mixtype = (Integer) params[1];
						// mThemeList = ThemeUtils.getAllThemes(context);

						mThemeList = ThemeUtils.getThemeListByType(mixtype, context);
						request.setStatus(Constant.STATUS_SUCCESS);
						request.notifyObservers(mThemeList);
					}
				}

				break;

			case Constant.TYPE_RINGTONE_LOCAL_LIST:

				if (request.getData() != null) {
					params = (Object[]) request.getData();
					Context context = (Context) params[0];
					List<Ringtone> resArr = new ArrayList<Ringtone>();
					RingtoneManager manager = new RingtoneManager(context);
					// manager.setType(RingtoneManager.TYPE_NOTIFICATION);
					manager.setType(RingtoneManager.TYPE_RINGTONE);
					Cursor cursor = manager.getCursor();
					int count = cursor.getCount();
					for (int i = 0; i < count; i++) {
						resArr.add(manager.getRingtone(i));
					}
					RingtoneManager manager2 = new RingtoneManager(context);
					manager2.setType(RingtoneManager.TYPE_NOTIFICATION);
					cursor = manager2.getCursor();
					count = cursor.getCount();
					for (int i = 0; i < count; i++) {
						resArr.add(manager2.getRingtone(i));
					}
					cursor.close();
					request.setStatus(Constant.STATUS_SUCCESS);
					request.notifyObservers(resArr);
				}
				break;

			case Constant.TYPE_RINGTONE_DOWNLOAD_LIST:

				if (request.getData() != null) {

					params = (Object[]) request.getData();
					Context context = (Context) params[0];
					// mThemeList = ThemeUtils.getAllThemes(context);
					mThemeList = ThemeUtils.getThemeListByType(ThemeData.THEME_ELEMENT_TYPE_RINGTONES, context);

					// List<Ringtone> resArr = new ArrayList<Ringtone>();
					// RingtoneManager manager = new RingtoneManager(context);
					// //manager.setType(RingtoneManager.TYPE_NOTIFICATION);
					// Cursor cursor = manager.getCursor();
					// int count = cursor.getCount();
					// for (int i = 0; i < count; i++) {
					// resArr.add(new manager.getRingtone(i));
					// }
					//
					ThemeLog.i("TYPE_RINGTONE_LIST", "mThemeList =" + mThemeList.size());
					request.setStatus(Constant.STATUS_SUCCESS);
					request.notifyObservers(mThemeList);

				}
				break;
			case Constant.TYPE_RINGTONE_SDCARD_LIST:

				if (request.getData() != null) {
					params = (Object[]) request.getData();
					Context context = (Context) params[0];
					Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
					if (cursor != null && cursor.getCount() > 0) {
						mThemeList = new ArrayList<ThemeData>();
						while (cursor.moveToNext()) {
							ThemeData tmpRingtone = new ThemeData();
							tmpRingtone.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)));
							tmpRingtone.setThemePath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));
							tmpRingtone.setRingtoneDuration(Integer.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))).intValue());
							mThemeList.add(tmpRingtone);
						}
					}
					if (mThemeList != null && mThemeList.size() > 0) {
						request.setStatus(Constant.STATUS_SUCCESS);
						request.notifyObservers(mThemeList);
					} else {
						request.setStatus(Constant.STATUS_ERROR);
						request.notifyObservers(null);
					}

				}
				break;
			case Constant.TYPE_LOCAL_ICON_IMG:
				if (request.getData() != null) {
					params = (Object[]) request.getData();
					ThemeData theme = (ThemeData) params[0];
					int elementType = (Integer) params[1];
					themeId = theme.getFileName();
					Bitmap bmp = null;
					try {
						InputStream is = fetch(theme, elementType);

						if (is != null) {
							BitmapFactory.Options opts = new BitmapFactory.Options();
							opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
							opts.inSampleSize = 1;
							bmp = BitmapFactory.decodeStream(is, null, opts);
							is.close();
						}
					} catch (IOException e) {

					}
					if (bmp != null) {
						img = new Image2();
						img._id = theme.getId();
						img.mAppIcon = bmp;
						request.setStatus(Constant.STATUS_SUCCESS);
						request.notifyObservers(img);
					} else {
						request.setStatus(Constant.STATUS_ERROR);
						request.notifyObservers(null);
					}
				}
				break;
			
			case Constant.TYPE_ONLINE_THEME_AD:
//					params = (Object[]) request.getData();
//					String imgUrl = (String) params[1];
//					img = new Image2();
				if (request.getData() != null) {
					params = (Object[]) request.getData();
					pageName = (String) params[0];
					phoneModel = (String) params[1];
					String countryCode = (String) params[2];
					channelId = (String) params[3];
					SubjectAdList ADlist = null;
					ThemeLog.v("TYPE_ONLINE_THEME_AD", "ssss TYPE_ONLINE_THEME_AD");
					try {
						ADlist = mAgent.postSubjectAd(pageName,phoneModel,countryCode,channelId);
						request.setStatus(Constant.STATUS_SUCCESS);
						request.notifyObservers(ADlist);
					} catch (SocketException e) {
						// TODO Auto-generated catch block
						request.setStatus(Constant.STATUS_ERROR);
						request.notifyObservers(null);
					}
					}
				break;
			case Constant.TYPE_START_GOOGLE_BILLING:
				if (request.getData() != null) {
					params = (Object[]) request.getData();
					mBillingHelper = (ThemeBillingHelper) params[0];
					IabHelper.OnIabSetupFinishedListener listener =(IabHelper.OnIabSetupFinishedListener)params[1];
					Log.v("TYPE_START_GOOGLE_BILLING","mBillingHelper:"+mBillingHelper+",listener :"+listener);
					try{
						mBillingHelper.startSetup(listener);
						request.setStatus(Constant.STATUS_SUCCESS);
						
					} catch(Exception e){
						request.setStatus(Constant.STATUS_ERROR);
					}
					
				}
				break;		
			case Constant.TYPE_GET_PRICE:

				if (request.getData() != null) {
					try{
						mSkuList = (ArrayList<String>)request.getData();
						HashMap<String, String> priceMap = mBillingHelper.getPrice(mSkuList);
						request.setStatus(Constant.STATUS_SUCCESS);
						request.notifyObservers(priceMap);
					} catch(Exception e){
						request.setStatus(Constant.STATUS_ERROR);
					}
				}
				
				break;	
			case Constant.TYPE_WRITE_THEME_DATA:
				if (request.getData() != null) {
					String data = (String)request.getData();  
					FileOutputStream fOut = null;
					StringBuilder sb = new StringBuilder(FileManager.APP_DIR_PATH);
					File dir = new File(sb.toString());
					if (!dir.exists()) {
						dir.mkdirs();
					}
					File file = new File(FileManager.THEME_DATA_FILE);
					if(!file.exists()){
						try {
							file.createNewFile();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					try {
						fOut = new FileOutputStream(file);
				        
				        byte[] buf = data.getBytes();  
				        fOut.write(buf);
						fOut.flush();
						fOut.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				break;
			default:
				break;
			}
		}
	} // end of run
}