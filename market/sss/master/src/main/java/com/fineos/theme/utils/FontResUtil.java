package com.fineos.theme.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.RemoteException;
import android.util.Log;

public class FontResUtil {
	private static final String TAG = "FontResAssembler";
	private static final String XML_FOLDER_NAME = "xml";
	private static final String PATH_SEPERATOR = "/";
	private static final String FONT_FILE_FOLDER_NAME = "fonts";

	private static final String SYSTEM_FONT_RES_STORAGE = "system_font_res";
	private static final String FONT_RES_PACKAGE_NAME_KEY = "font_res_package_name";
	private static final String FONT_RES_DISPLAY_NAME_KEY = "font_res_display_name";
	private static final String FONT_RES_FONT_FILE_PATH_KEY = "font_res_font_file_path";

	private static final String FONT_PACKAGE_PATTERN = "android.font.";

	public List<FontResource> assembleFontResourceFromPackage(PackageManager packageManager, String pacakgeName) {
		Resources res = null;
		try {
			res = packageManager.getResourcesForApplication(pacakgeName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		AssetManager assetManager = res.getAssets();
		List<FontResource> fontResList = new ArrayList<FontResource>();
		try {
			String[] resourceFontFileList = assetManager.list(FONT_FILE_FOLDER_NAME);
			if (resourceFontFileList == null || resourceFontFileList.length <= 0) {
				return fontResList;
			}
			Typeface typeface = null;
			typeface = Typeface.createFromAsset(assetManager, FONT_FILE_FOLDER_NAME + PATH_SEPERATOR + resourceFontFileList[0]);

			String[] resourceXMLList = assetManager.list(XML_FOLDER_NAME);
			if (resourceXMLList == null || resourceXMLList.length <= 0) {
				return fontResList;
			}
			InputStream inputStream = assetManager.open(XML_FOLDER_NAME + PATH_SEPERATOR + resourceXMLList[0]);

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(inputStream);

			Element root = (Element) document.getDocumentElement();
			fontResList.add(new FontResource(pacakgeName, root.getAttribute("displayname"), FONT_FILE_FOLDER_NAME + PATH_SEPERATOR + resourceFontFileList[0], typeface));
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
		return fontResList;
	}

	public static List<PackageInfo> getFontPackegeInfoList(PackageManager packageManager) {
		Pattern p = Pattern.compile(FONT_PACKAGE_PATTERN);
		List<PackageInfo> packegeInfoList = null;
		packegeInfoList = packageManager.getInstalledPackages(0);
		int packegeInfoListSize = packegeInfoList.size();
		for (int i = packegeInfoListSize - 1; i >= 0; i--) {
			// for (int i = 0; i < packegeInfoList.size(); i++) {
			PackageInfo packageInfo = packegeInfoList.get(i);
			Matcher m = p.matcher(packageInfo.packageName);
			if (!m.find()) {
				packegeInfoList.remove(i);
			}
		}
		return packegeInfoList;
	}

	public static List<FontResource> selectFontRes(List<FontResource> fontResList, int position) {
		int fontResListSize = fontResList.size();
		for (int i = 0; i < fontResListSize; i++) {
			FontResource fontRes = fontResList.get(i);
			fontRes.setSelected(false);
		}
		FontResource fontRes = fontResList.get(position);
		fontRes.setSelected(true);
		return fontResList;
	}

	public static List<FontResource> selectFontRes(List<FontResource> fontResList, FontResource targetFontRes) {
		String packageName = targetFontRes.getPackageName();
		String dispalyName = targetFontRes.getDisplayName();
		String fontFilePath = targetFontRes.getFontFilePath();
		int fontResListSize = fontResList.size();
		for (int i = 0; i < fontResListSize; i++) {
			FontResource fontRes = fontResList.get(i);
			if (fontRes.getPackageName().equals(packageName) && fontRes.getDisplayName().equals(dispalyName) && fontRes.getFontFilePath().equals(fontFilePath)) {
				fontRes.setSelected(true);
			} else {
				fontRes.setSelected(false);
			}
		}
		return fontResList;
	}

	public static FontResource getSystemFontRes(Context context) {
		SharedPreferences fontResInfo = context.getSharedPreferences(SYSTEM_FONT_RES_STORAGE, 0);
		String packageName = fontResInfo.getString(FONT_RES_PACKAGE_NAME_KEY, "");
		String dispalyName = fontResInfo.getString(FONT_RES_DISPLAY_NAME_KEY, "");
		String fontFilePath = fontResInfo.getString(FONT_RES_FONT_FILE_PATH_KEY, "");
		return new FontResource(packageName, dispalyName, fontFilePath, null);
	}

	public static FontResource getSelectedFontRes(List<FontResource> fontResList) {
		if (fontResList == null || fontResList.isEmpty()) {
			return null;
		}
		int fontResListSize = fontResList.size();
		for (int i = 0; i < fontResListSize; i++) {
			FontResource fontRes = fontResList.get(i);
			if (fontRes.isSelected()) {
				return fontRes;
			}
		}
		return null;
	}

	public static void saveSystemFontRes(Context context, FontResource fontRes) {
		String packageName = fontRes.getPackageName();
		String dispalyName = fontRes.getDisplayName();
		String fontFilePath = fontRes.getFontFilePath();
		SharedPreferences fontResInfo = context.getSharedPreferences(SYSTEM_FONT_RES_STORAGE, 0);
		fontResInfo.edit().putString(FONT_RES_PACKAGE_NAME_KEY, packageName).commit();
		fontResInfo.edit().putString(FONT_RES_DISPLAY_NAME_KEY, dispalyName).commit();
		fontResInfo.edit().putString(FONT_RES_FONT_FILE_PATH_KEY, fontFilePath).commit();
	}

	public static void updateSysteFontConfiguration(FontResource fontRes) {
//		Configuration curConfig = new Configuration();
//		try {
//			curConfig.fontPackageName = fontRes.getPackageName();
//			curConfig.fontPath = fontRes.getFontFilePath();
//			Log.e(TAG, "curConfig.fontPackageName =" + curConfig.fontPackageName);
//			Log.e(TAG, "curConfig.fontPath =" + curConfig.fontPath);
//			ActivityManagerNative.getDefault().updatePersistentConfiguration(curConfig);
//		} catch (RemoteException e) {
//			Log.w(TAG, "Unable to save font size");
//		}
	}
}
