/*
 * Copyright (C) 2013 The ChameleonOS Project
 *
 * Licensed under the GNU GPLv2 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fineos.theme.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.util.Xml;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

import com.fineos.theme.R;
import com.fineos.theme.model.ThemeData;
import com.fineos.theme.preview.PreviewHelper;
import com.fineos.theme.preview.PreviewHolder;
import com.fineos.theme.provider.IconContentProvider;
import com.fineos.theme.provider.ThemeContentProvider;
import com.fineos.theme.provider.ThemeSQLiteHelper;
import com.fineos.theme.provider.ThemesDataSource;

import fineos.content.res.IThemeManagerService;
import fineos.widget.listview.swinginadapters.prepared.SwingBottomInAnimationAdapter;

public class ThemeUtils {
	private static final String TAG = "ThemeUtils";
	private static final boolean DEBUG = true;
	private static final String FLAG_USING = "1";
	private static final String FLAG_NOT_USING = "0";
	public static final int ANIMATION_TIME = 400;
	public static final boolean ISHIDENETDIALOG = false;
	public static final String APPLY_THEME_ACTION = "com.fineos.theme.action.apply_theme";
	public static final String APPLY_WALLPAPER_ACTION = "com.fineos.theme.action.apply_wallpaper";
	/**
	 * Checks if CACHE_DIR exists and returns true if it does
	 */
	public static boolean cacheRingtoneDirExists(String themeId) {
		return (new File(FileManager.RINGTONE_DIR_PATH)).exists();
	}

	public static boolean defaultCacheDirExists(String themeId) {
		return (new File(Constant.DATA_THEME_PATH + "/" + themeId + "/ringtones")).exists();
	}

	/**
	 * Creates CACHE_DIR if it does not already exist
	 */
	public static void createDefaultCacheDir(String themeId) {
		if (!defaultCacheDirExists(themeId)) {
			ThemeLog.i(TAG, "Creating createDefaultCacheDir begin");
			File dir = new File(Constant.DATA_THEME_PATH + "/" + themeId + "/ringtones");
			dir.mkdirs();
			ThemeLog.i(TAG, "Creating createDefaultCacheDir end");
		}
	}

	/**
	 * Creates CACHE_DIR if it does not already exist
	 */
	public static void createRingtoneDir(String themeId) {
		if (!cacheRingtoneDirExists(themeId)) {
			ThemeLog.i(TAG, "createCacheDir,Creating cache directory,themeId=" + themeId);
			File dir = new File(FileManager.RINGTONE_DIR_PATH);
			dir.mkdirs();
		}
	}

	public static boolean themeDirExists() {
		return (new File(Constant.DEFAULT_THEME_PATH)).exists();
	}

	/**
	 * Creates CACHE_DIR if it does not already exist
	 */
	public static void createThemeDir() {
		if (!themeDirExists()) {
			ThemeLog.i(TAG, "Creating cache directory");
			File dir = new File(Constant.DEFAULT_THEME_PATH);
			dir.mkdirs();
		}
	}

	public static void deleteFile(File file) {
		if (file.isDirectory())
			for (File f : file.listFiles())
				deleteFile(f);
		else
			file.delete();
	}

	/**
	 * Deletes a theme directory inside CACHE_DIR for the given theme
	 * 
	 * @param themeName
	 *            theme to delete cache directory for
	 */
	public static void deleteThemeCacheDir(String themeName) {
		File f = new File(Constant.CACHE_DIR + "/" + themeName);
		if (f.exists())
			deleteFile(f);
		f.delete();
	}

	public static boolean installedThemeHasFonts() {
		File fontsDir = new File("/data/fonts");
		return fontsDir.exists() && fontsDir.list().length > 0;
	}

	/**
	 * Simple copy routine given an input stream and an output stream
	 */
	public static void copyInputStream(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int len;

		while ((len = in.read(buffer)) >= 0) {
			out.write(buffer, 0, len);
		}

		out.close();
	}

	public static boolean isSymbolicLink(File f) throws IOException {
		return !f.getAbsolutePath().equals(f.getCanonicalPath());
	}

	public static void setDirPerms(File f) {
		try {
			if (isSymbolicLink(f))
				return;
		} catch (IOException e) {
			return;
		}

		if (!f.isDirectory())
			return;
		f.setReadable(true, false);
		f.setWritable(true, false);
		f.setExecutable(true, false);
	}

	public static void setFilePerms(File f) {
		try {
			if (isSymbolicLink(f))
				return;
		} catch (IOException e) {
			return;
		}
		f.setReadable(true, false);
		f.setWritable(true, false);
	}

	public static String extractThemeRingtones(String themeId, String themePath) {
		String fileName = null;

		if (!cacheRingtoneDirExists(themeId)) {
			createRingtoneDir(themeId);
		}

		try {
			ZipFile zip = new ZipFile(themePath);
			ZipEntry ze = null;

			InputStream is = null;
			FileOutputStream out = null;
			ze = zip.getEntry("ringtones/ringtone.mp3");
			if (ze != null) {
				is = zip.getInputStream(ze);
				fileName = FileManager.RINGTONE_DIR_PATH + "/" + themeId + ".mp3";
				out = new FileOutputStream(fileName);
				copyInputStream(is, out);
			}
		} catch (Exception e) {
			return null;
		}

		return fileName;
	}

	public static ThemeData getThemeEntryById(long id, Context context) {
		ThemeData theme = null;

		ThemesDataSource dataSource = ThemesDataSource.getInstance(context);
		dataSource.open();
		theme = dataSource.getThemeById(id);
		dataSource.close();

		return theme;
	}

	public static void deleteTheme(ThemeData theme, Context context) {
		ThemesDataSource dataSource = ThemesDataSource.getInstance(context);
		dataSource.open();
		dataSource.deleteTheme(theme);
		dataSource.close();
		(new File(theme.getThemePath())).delete();
	}

	public static List<ThemeData> getAllThemes(Context context) {
		ThemesDataSource dataSource = ThemesDataSource.getInstance(context);
		dataSource.open();
		List<ThemeData> themes = dataSource.getAllThemes();
		dataSource.close();
		return themes;
	}

	public static ThemeData getThemeByFildId(Context context, String packageName) {
		ThemesDataSource dataSource = ThemesDataSource.getInstance(context);
		dataSource.open();
		ThemeData theme = dataSource.getThemeByFileId(packageName);
		dataSource.close();
		return theme;
	}

	public static List<ThemeData> getThemeListByType(int elementType, Context context) {
		ThemesDataSource dataSource = new ThemesDataSource(context);
		dataSource.open();
		List<ThemeData> list = null;
		switch (elementType) {

		case ThemeData.THEME_ELEMENT_TYPE_ALL_THEME:
			list = dataSource.getAllThemes();
			break;
		case ThemeData.THEME_ELEMENT_TYPE_COMPLETE_THEME:
			list = dataSource.getCompleteThemes();
			break;
		case ThemeData.THEME_ELEMENT_TYPE_ICONS:
			list = dataSource.getIconThemes();
			break;
		case ThemeData.THEME_ELEMENT_TYPE_WALLPAPER:
			list = dataSource.getWallpaperThemes();
			break;
		case ThemeData.THEME_ELEMENT_TYPE_LOCK_WALLPAPER:
			list = dataSource.getLockscreenWallpaperThemes();
			break;
		case ThemeData.THEME_ELEMENT_TYPE_SYSTEMUI:
			list = dataSource.getSystemUIThemes();
			break;
		case ThemeData.THEME_ELEMENT_TYPE_FRAMEWORK:
			list = dataSource.getFrameworkThemes();
			break;
		case ThemeData.THEME_ELEMENT_TYPE_CONTACTS:
			list = dataSource.getContactsThemes();
			break;
		case ThemeData.THEME_ELEMENT_TYPE_DIALER:
			list = dataSource.getDialerThemes();
			break;
		case ThemeData.THEME_ELEMENT_TYPE_RINGTONES:
			list = dataSource.getRingtoneThemes();
			break;
		case ThemeData.THEME_ELEMENT_TYPE_BOOTANIMATION:
			list = dataSource.getBootanimationThemes();
			break;
		case ThemeData.THEME_ELEMENT_TYPE_MMS:
			list = dataSource.getMmsThemes();
			break;
		case ThemeData.THEME_ELEMENT_TYPE_FONT:
			try {
//				ThemeUtils.addThemesToDb(context, false);
				ThemeUtils.addAvailableThemesToDb(context);				
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			list = dataSource.getFontThemes();
			break;
		case ThemeData.THEME_ELEMENT_TYPE_LOCKSCREEN:
			list = dataSource.getLockThemes();
			break;
		default:
			list = dataSource.getAllThemes();

		}

		dataSource.close();
		return list;
	}

	public static String[] getPreviewListByType(int type, ThemeData theme) {
		if (theme == null) {
			return null;
		}
		String[] previewList = null;
		switch (type) {

		case ThemeData.THEME_ELEMENT_TYPE_ICONS:
			previewList = PreviewHelper.getIconPreviews(theme);
			break;
		case ThemeData.THEME_ELEMENT_TYPE_LOCK_WALLPAPER:
			previewList = PreviewHelper.getLockWallpaperPreviews(theme);
			break;
		case ThemeData.THEME_ELEMENT_TYPE_WALLPAPER:
			previewList = PreviewHelper.getWallpaperPreviews(theme);
			break;
		case ThemeData.THEME_ELEMENT_TYPE_FONT:
			previewList = PreviewHelper.getFontsPreviews(theme);
			break;
		case ThemeData.THEME_ELEMENT_TYPE_LOCKSCREEN:
			previewList = PreviewHelper.getLockScreenPreviews(theme);
			break;
		default:
			previewList = PreviewHelper.getThemePreviews(theme);
		}

		return previewList;

	}

	/***
	 * used for all elements themes :reset all themes' flag
	 * 
	 * @param context
	 */
	public static void resetAllUsingFlags(Context context) { // reset flags of
																// all themes
																// ,include mix
																// packages

		ContentValues value = new ContentValues();
		value.put(ThemeSQLiteHelper.COLUMN_THEME_ISUSING, FLAG_NOT_USING);
		value.put(ThemeSQLiteHelper.COLUMN_ICONS_ISUSING, FLAG_NOT_USING);
		value.put(ThemeSQLiteHelper.COLUMN_LOCKSCREEN_ISUSING, FLAG_NOT_USING);
		value.put(ThemeSQLiteHelper.COLUMN_LOCK_WALLPAPER_ISUSING, FLAG_NOT_USING);
		value.put(ThemeSQLiteHelper.COLUMN_WALLPAPER_ISUSING, FLAG_NOT_USING);
		value.put(ThemeSQLiteHelper.COLUMN__FONT_ISUSING, FLAG_NOT_USING);
		value.put(ThemeSQLiteHelper.COLUMN_RINGTONE_ISUSING, FLAG_NOT_USING);
		update(context, ThemeSQLiteHelper.CONTENT_URI, value, null, null);

	}

	/****
	 * used for mixed themes : reset all other themes' flag (indicated by
	 * mixtype,for example icons)
	 * 
	 * @param context
	 * @param type
	 */
	public static void resetUsingFlagByType(Context context, int type) {

		ContentValues value = new ContentValues();
		if (ThemeData.THEME_ELEMENT_TYPE_ICONS == type) {

			value.put(ThemeSQLiteHelper.COLUMN_ICONS_ISUSING, FLAG_NOT_USING);
		} else if (ThemeData.THEME_ELEMENT_TYPE_LOCKSCREEN == type) {

			value.put(ThemeSQLiteHelper.COLUMN_LOCKSCREEN_ISUSING, FLAG_NOT_USING);
		} else if (ThemeData.THEME_ELEMENT_TYPE_LOCK_WALLPAPER == type) {

			value.put(ThemeSQLiteHelper.COLUMN_LOCK_WALLPAPER_ISUSING, FLAG_NOT_USING);
		} else if (ThemeData.THEME_ELEMENT_TYPE_WALLPAPER == type) {

			value.put(ThemeSQLiteHelper.COLUMN_WALLPAPER_ISUSING, FLAG_NOT_USING);
		} else if (ThemeData.THEME_ELEMENT_TYPE_FONT == type) {

			value.put(ThemeSQLiteHelper.COLUMN__FONT_ISUSING, FLAG_NOT_USING);
		} else if (ThemeData.THEME_ELEMENT_TYPE_RINGTONES == type) {

			value.put(ThemeSQLiteHelper.COLUMN_RINGTONE_ISUSING, FLAG_NOT_USING);
		} else {

			resetAllUsingFlags(context);
			return;
		}

		update(context, ThemeSQLiteHelper.CONTENT_URI, value, null, null);

	}

	public static void setUsingFlagByType(Context context, ThemeData theme, int type) {

		setUsingFlagByType(context, theme.getThemePath(), type);
	}
	
	public static void setUsingFlagByType(Context context, String themepath, int type) {
		
		ContentValues value = new ContentValues();
		String whereClause = ThemeSQLiteHelper.COLUMN_THEME_PATH + "=?";
		String[] whereArgs = new String[] { themepath };
		if (ThemeData.THEME_ELEMENT_TYPE_ICONS == type) { // icons
			value.put(ThemeSQLiteHelper.COLUMN_ICONS_ISUSING, FLAG_USING);

		} else if (ThemeData.THEME_ELEMENT_TYPE_LOCKSCREEN == type) { // lockscreen

			value.put(ThemeSQLiteHelper.COLUMN_LOCKSCREEN_ISUSING, FLAG_USING);

		} else if (ThemeData.THEME_ELEMENT_TYPE_LOCK_WALLPAPER == type) { // lock
																			// wallpaper

			value.put(ThemeSQLiteHelper.COLUMN_LOCK_WALLPAPER_ISUSING, FLAG_USING);

		} else if (ThemeData.THEME_ELEMENT_TYPE_WALLPAPER == type) { // wallpaper

			value.put(ThemeSQLiteHelper.COLUMN_WALLPAPER_ISUSING, FLAG_USING);

		} else if (ThemeData.THEME_ELEMENT_TYPE_FONT == type) { // font

			value.put(ThemeSQLiteHelper.COLUMN__FONT_ISUSING, FLAG_USING);

		} else if (ThemeData.THEME_ELEMENT_TYPE_RINGTONES == type) { // ringtone

			value.put(ThemeSQLiteHelper.COLUMN_RINGTONE_ISUSING, FLAG_USING);

		} else { // complete theme
			value.put(ThemeSQLiteHelper.COLUMN_ICONS_ISUSING, FLAG_USING);
			value.put(ThemeSQLiteHelper.COLUMN_LOCKSCREEN_ISUSING, FLAG_USING);
			value.put(ThemeSQLiteHelper.COLUMN_LOCK_WALLPAPER_ISUSING, FLAG_USING);
			value.put(ThemeSQLiteHelper.COLUMN_WALLPAPER_ISUSING, FLAG_USING);
			value.put(ThemeSQLiteHelper.COLUMN__FONT_ISUSING, FLAG_USING);
			value.put(ThemeSQLiteHelper.COLUMN_RINGTONE_ISUSING, FLAG_USING);

			value.put(ThemeSQLiteHelper.COLUMN_THEME_ISUSING, FLAG_USING);
		}

		ThemeUtils.update(context, ThemeSQLiteHelper.CONTENT_URI, value, whereClause, whereArgs);
	}

	public static void setUsingTagByType(Context context, ThemeData theme, int type, PreviewHolder holder) {

		ThemeLog.i(TAG, "type=" + type + ",theme:" + theme.getFileName());
		if (type == ThemeData.THEME_ELEMENT_TYPE_ICONS) { // icons

			if (theme.getIsUsing_icons()) {
				holder.usingTag.setImageResource(R.drawable.ic_theme_focused);
			}

		} else if (type == ThemeData.THEME_ELEMENT_TYPE_FONT) { // fonts
			if (theme.getIsUsing_fonts()) {
				holder.usingTag.setImageResource(R.drawable.ic_theme_focused);
			}

		} else if (type == ThemeData.THEME_ELEMENT_TYPE_LOCKSCREEN) { // lockscreen

			if (theme.getIsUsing_lockscreen()) {
				holder.usingTag.setImageResource(R.drawable.ic_theme_focused);
			}
		} else if (type == ThemeData.THEME_ELEMENT_TYPE_LOCK_WALLPAPER) { // lockwallpaper
			ThemeLog.i(TAG, "theme.getIsUsing_lockwallpaper()=" + theme.getIsUsing_lockwallpaper());
			if (theme.getIsUsing_lockwallpaper()) {
				holder.usingTag.setImageResource(R.drawable.ic_theme_focused);
			}
		} else if (type == ThemeData.THEME_ELEMENT_TYPE_WALLPAPER) { // wallpaper

			if (theme.getIsUsing_wallpaper()) {
				holder.usingTag.setImageResource(R.drawable.ic_theme_focused);
			}
			WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
			WallpaperInfo curWallpaperUsing = wallpaperManager.getWallpaperInfo();
			if (curWallpaperUsing != null) { // live wallpaper is using ,rm
												// select tag(wallpaper)
				holder.usingTag.setImageDrawable(null);
			}

		} else if (type == ThemeData.THEME_ELEMENT_TYPE_RINGTONES) {
			if (theme.getIsUsing_ringtone()) {
				holder.usingTag.setImageResource(R.drawable.ic_theme_focused);
			}
		}

	}
	
	public static void updateThemePrice(Context context,ThemeData theme){
		
		ContentValues values = new ContentValues();
		values.put(ThemeSQLiteHelper.COLUMN_THEME_PRICE, theme.getPrice());
		ThemeLog.v(TAG,"Price :"+theme.getPrice()+",theme path :"+theme.getThemePath()+",file name :"+theme.getFileName()+"Package name :"+theme.getPackageName());
		update(context, ThemeSQLiteHelper.CONTENT_URI, values, ThemeSQLiteHelper.COLUMN_THEME_FILE_NAME+ "='" + theme.getPackageName() + "'", null);
	}

	public static boolean addThemeEntryToDb(String themeId, String themePath, Context context, boolean isDefaultTheme, boolean force) {
		ThemeLog.i(TAG, "addThemeEntryToDb");
		try {
			ThemesDataSource dataSource = ThemesDataSource.getInstance(context);
			File file = new File(themePath);
			long lastModified = file.lastModified();
			dataSource.open();
			
			if (!force && dataSource.entryExists(themeId)) {
				if (!dataSource.entryIsOlder(themeId, lastModified)) {
					dataSource.close();
					return true;
				}
			}
			ZipFile zip = new ZipFile(themePath);
			ZipEntry entry = zip.getEntry("description.xml"); // 读 description
																// 主题描述文件
			ThemeDetails details = null;
			try {
				InputStream stream = zip.getInputStream(entry);
				details = getThemeDetails(stream); // zip.getInputStream(entry)
				stream.close();
			} catch (Exception e) {

				return false;
			}
			ThemeData theme = new ThemeData();
			theme.setFileName(themeId);
			theme.setThemePath(themePath);
			theme.setTitle(details.title);
			theme.setAuthor(details.author);
			theme.setDesigner(details.designer);
			theme.setVersion(details.version);
			theme.setUiVersion(details.uiVersion);

			theme.setDescription(details.description); // set description

			theme.setIsDefaultTheme(isDefaultTheme);
			theme.setHasWallpaper(zip.getEntry("wallpaper/default_wallpaper.jpg") != null || zip.getEntry("wallpaper/default_wallpaper.png") != null);
			theme.setHasLockscreenWallpaper(zip.getEntry("wallpaper/default_lock_wallpaper.jpg") != null || zip.getEntry("wallpaper/default_lock_wallpaper.png") != null);

			theme.setHasJpgWallpaper(zip.getEntry("wallpaper/default_wallpaper.jpg") != null);
			theme.setHasJpgLockscreenWallpaper(zip.getEntry("wallpaper/default_lock_wallpaper.jpg") != null);

			theme.setHasIcons(zip.getEntry("icons") != null);
			theme.setHasLock(zip.getEntry("lockscreen") != null);
			theme.setHasContacts(zip.getEntry("com.android.contacts") != null);
			theme.setHasDialer(zip.getEntry("com.android.dialer") != null);
			theme.setHasSystemUI(zip.getEntry("com.android.systemui") != null);
			theme.setHasFramework(zip.getEntry("framework-res") != null);
			theme.setHasRingtone(zip.getEntry("ringtones/ringtone.mp3") != null);

			if (theme.getHasRingtone() && theme.getRingtonePath() == null) {
				ThemeUtils.extractThemeRingtones(FileUtils.stripExtension(theme.getFileName()), theme.getThemePath());
			}

			theme.setHasNotification(zip.getEntry("ringtones/notification.mp3") != null);
			theme.setHasBootanimation(zip.getEntry("boots") != null);
			theme.setHasMms(zip.getEntry("com.android.mms") != null);
			theme.setHasFont(zip.getEntry("fonts") != null);
			// theme.setIsComplete(theme.getHasSystemUI() &&
			// theme.getHasFramework() &&
			// theme.getHasMms() && theme.getHasContacts());
			ThemeLog.i(TAG, "theme name =" + theme.getFileName() + "  has icons? : " + theme.getHasIcons() + ", has lock ? :" + theme.getHasLock());
			theme.setIsComplete(theme.getHasIcons() && theme.getHasLock());
			// // xuqian
			// ,consider
			// as
			// a
			// complete
			// theme
			// package
			// theme.setIsComplete(isComplete(theme)); // own more than one
			// elements, consider a
			// complete package

			theme.setIsUsing(false); // default false
			theme.setPreviewsList( // 读预览图
			createPreviewList(zip, theme.getHasWallpaper(), theme.getHasJpgWallpaper(), theme.getHasLockscreenWallpaper(), theme.getHasJpgLockscreenWallpaper()));

			theme.setLastModified(lastModified);
			zip.close();
			try {
				dataSource.createThemeEntry(theme);
			} catch (Exception e) {
				e.printStackTrace();
			}
			dataSource.close();
		} catch (IOException e) {
			return false;
		}

		return true;
	}

	public static boolean addWallPaperEntryToDb(String themeId, String themePath, Context context, boolean isDefaultTheme, boolean force) throws UnsupportedEncodingException {
		ThemeLog.i(TAG, "addWallPaperEntryToDb");
		ThemeLog.i(TAG, "themeId =" + themeId);
		ThemeLog.i(TAG, "themePath =" + themePath);
		ThemesDataSource dataSource = ThemesDataSource.getInstance(context);
		File file = new File(themePath);
		long lastModified = file.lastModified();
		dataSource.open();
		if (!force && dataSource.entryExists(themeId)) {
			if (!dataSource.entryIsOlder(themeId, lastModified)) {
				dataSource.close();
				return true;
			}
		}

		ThemeData theme = new ThemeData();
		theme.setFileName(themeId);
		theme.setThemePath(themePath);
		theme.setTitle(themeId);
		theme.setAuthor(null);
		theme.setDesigner(null);
		theme.setVersion(null);
		theme.setUiVersion(null);

		theme.setDescription(null); // set description

		theme.setIsDefaultTheme(isDefaultTheme);
		theme.setHasWallpaper(true);
		theme.setHasLockscreenWallpaper(false);

		theme.setHasJpgWallpaper(true);
		theme.setHasJpgLockscreenWallpaper(false);

		theme.setHasIcons(false);
		theme.setHasLock(false);
		theme.setHasContacts(false);
		theme.setHasDialer(false);
		theme.setHasSystemUI(false);
		theme.setHasFramework(false);
		theme.setHasRingtone(false);
		if (theme.getHasRingtone() && theme.getRingtonePath() == null) {
			ThemeUtils.extractThemeRingtones(FileUtils.stripExtension(theme.getFileName()), theme.getThemePath());
		}

		theme.setHasNotification(false);
		theme.setHasBootanimation(false);
		theme.setHasMms(false);
		theme.setHasFont(false);
		// theme.setIsComplete(theme.getHasSystemUI() &&
		// theme.getHasFramework() &&
		// theme.getHasMms() && theme.getHasContacts());
		ThemeLog.i(TAG, "theme name =" + theme.getFileName() + "  has icons? : " + theme.getHasIcons() + ", has lock ? :" + theme.getHasLock());
		// theme.setIsComplete(theme.getHasIcons() && theme.getHasLock());
		// // xuqian
		// ,consider
		// as
		// a
		// complete
		// theme
		// package
		theme.setIsComplete(false); // own more than one
									// elements, consider a
									// complete package

		theme.setIsUsing(false); // default false
		theme.setPreviewsList(themePath);

		theme.setLastModified(lastModified);

		try {
			dataSource.createThemeEntry(theme);
		} catch (Exception e) {
			e.printStackTrace();
		}
		dataSource.close();

		return true;
	}

	public static boolean insertThemeEntryToDb(ThemeData theme, Context context) {
		ThemeLog.i(TAG, "addThemeEntryToDb");
		ThemesDataSource dataSource = ThemesDataSource.getInstance(context);
		dataSource.open();
		if (dataSource.entryExists(theme.getId())) {
			if (!dataSource.entryThemeIsOlder(theme)) {
				dataSource.close();
				return true;
			}
		}
		try {
			dataSource.createThemeEntry(theme);
		} catch (Exception e) {
			e.printStackTrace();
		}
		dataSource.close();

		return true;
	}
	
	public static boolean insertIconEntryToDb(Context context) {
		ThemeLog.i(TAG, "qwe insertIconEntryToDb");
		ThemesDataSource dataSource = ThemesDataSource.getInstance(context);
		dataSource.open();
		try {
			dataSource.insertIconEntry();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			dataSource.insertIconEntry2();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		dataSource.close();

		return true;
	}

	public static Boolean isComplete(ThemeData theme) {

		Boolean isComplete = false;
		Boolean hasIcons = theme.getHasIcons();
		Boolean hasLock = theme.getHasLock();
		Boolean hasLockWallpaper = theme.getHasLockscreenWallpaper();
		Boolean hasWallpaper = theme.getHasWallpaper();
		Boolean hasFonts = theme.getHasFont();
		Boolean hasRingTone = theme.getHasRingtone();
		if (hasIcons && hasLock) {
			isComplete = true;
		} else if (hasIcons && hasLockWallpaper) {
			isComplete = true;
		} else if (hasIcons && hasWallpaper) {
			isComplete = true;
		} else if (hasIcons && hasFonts) {
			isComplete = true;
		} else if (hasIcons && hasRingTone) {
			isComplete = true;
		} else if (hasLock && hasLockWallpaper) {
			isComplete = true;
		} else if (hasLock && hasWallpaper) {
			isComplete = true;
		} else if (hasLock && hasFonts) {
			isComplete = true;
		} else if (hasLock && hasRingTone) {
			isComplete = true;
		} else if (hasLockWallpaper && hasWallpaper) {
			isComplete = true;
		} else if (hasLockWallpaper && hasFonts) {
			isComplete = true;
		} else if (hasLockWallpaper && hasRingTone) {
			isComplete = true;
		} else if (hasWallpaper && hasFonts) {
			isComplete = true;
		} else if (hasWallpaper && hasRingTone) {
			isComplete = true;
		} else if (hasFonts && hasRingTone) {
			isComplete = true;
		}

		return isComplete;
	}

	public static String[] getAvailableThemes(String path) {
		ThemeLog.i(TAG, "Returning theme list for " + path);

		FilenameFilter themeFilter = new FilenameFilter() {
			@Override
			public boolean accept(File file, String s) {
				if (s.toLowerCase().endsWith(".ctz") || s.toLowerCase().endsWith(".mtz") || s.toLowerCase().endsWith(".ftz"))
					return true;
				else
					return false;
			}
		};

		File dir = new File(path);
		String[] dirList = null;
		ThemeLog.i(TAG, "dir.exists()" + dir.exists());
		if (dir != null && dir.exists() && dir.isDirectory()) {
			dirList = dir.list(themeFilter); // 滤出所有后缀为 .ctz .mtz .ftz 的压缩包的路径
			ThemeLog.i(TAG, "dir List（end with .ctz,.mtz,.ftz）" + dirList);
		} else {
			ThemeLog.i(TAG, path + " does not exist or is not a directory!");
		}
		return dirList;
	}
	
	public static HashMap<String, String> getAvailableApkPackageNames(String path, Context context) {
		ThemeLog.i(TAG, "Returning theme list for " + path);
		ArrayList<String> packageNames = new ArrayList<String>();
		FilenameFilter themeFilter = new FilenameFilter() {
			@Override
			public boolean accept(File file, String s) {
				if (s.toLowerCase().endsWith(".apk")) {
					return true;
				} else {
					return false;
				}					
			}
		};
		HashMap<String, String> hashMap = new HashMap<String, String>();
		File dir = new File(path);
		String[] dirList = null;
		ThemeLog.i(TAG, "dir.exists()" + dir.exists());
		if (dir != null && dir.exists() && dir.isDirectory()) {
			dirList = dir.list(themeFilter);
			ThemeLog.i(TAG, "dir List（end with .ctz,.mtz,.ftz）" + dirList);
		} else {
			ThemeLog.i(TAG, path + " does not exist or is not a directory!");
		}
		if (dirList != null && dirList.length > 0) {
			int length = dirList.length;
			for (int i = 0; i < length; i++) {
				String packageName = getPackageName(path + dirList[i], context);
				if (packageName != null) {
					hashMap.put(packageName, path + dirList[i]);
				}
			}
		}
		return hashMap;
	}
	
	public static String getPackageName(String apkfilePath, Context context) {
		File file = new File(apkfilePath);
		if (file.length() <= 0) {
			return null;
		}
		PackageManager pm = context.getPackageManager();
		PackageInfo info = pm.getPackageArchiveInfo(apkfilePath, PackageManager.GET_ACTIVITIES);
		ApplicationInfo appInfo = null;
		String packageName = null;
		if (info != null) {
			appInfo = info.applicationInfo;
			packageName = appInfo.packageName;
		}
		return packageName;
	}

	public static String[] getWallpapers(String path) {
		FilenameFilter themeFilter = new FilenameFilter() {
			@Override
			public boolean accept(File file, String s) {
				if (s.toLowerCase().endsWith(".png"))
					return true;
				else
					return false;
			}
		};
		ThemeLog.i(TAG, "themeFilter =" + themeFilter.toString());
		File dir = new File(path);
		String[] dirList = null;
		if (dir != null && dir.exists() && dir.isDirectory()) {
			dirList = dir.list(themeFilter); // 滤出所有后缀为 .png 的压缩包的路径
			if (dirList != null) {
				for (int i = 0 ; i < dirList.length; i++) {
					ThemeLog.i(TAG, "dirList =" + dirList[i]);					
				}	
			}
			
		} else {
			ThemeLog.i(TAG, path + " does not exist or is not a directory!");
		}
		return dirList;
	}

	public static void removeNonExistingThemes(Context context, String[] availableThemes) {
		List<ThemeData> themes = getAllThemes(context);
		for (ThemeData theme : themes) {
			ThemeLog.i(TAG, "removeNonExistingThemes,theme(in datebase)=" + theme.getThemePath());
			boolean exists = false;
			if (theme.getThemePath().contains(Constant.SYSTEM_THEME_PATH)) {
				exists = true;
				continue;
			}

			ThemeLog.i(TAG, "removeNonExistingThemes,availableThemes" + availableThemes);
			if (availableThemes != null) {
				for (String s : availableThemes) {
					ThemeLog.i(TAG, "removeNonExistingThemes,theme(in sd card)=" + s);
					if (theme.getThemePath().contains(s)) {
						exists = true;
						break;
					}
				}
			} else { // availableThemes ==null ,no usb mode

				exists = false;
			}

			ThemeLog.i(TAG, "removeNonExistingThemes,exists=" + exists);
			if (!exists) {
				deleteTheme(theme, context);
				deleteThemeCacheDir(theme.getFileName());
			}
		}
	}

	public static Boolean isSDCardExist() {
		Boolean exist = true;
		exist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
		ThemeLog.i(TAG, "SD Card exist ? " + exist);
		return exist;

	}

	public static void removeAllThemes(Context context) {
		List<ThemeData> themes = getAllThemes(context);
		for (ThemeData theme : themes) {
			deleteTheme(theme, context);
			deleteThemeCacheDir(theme.getFileName());
		}
	}
	public static void addIconsToDb(Context context){
		String path = "/data"+ Environment.getDataDirectory().getAbsolutePath()	+ File.separator 
				+ "com.fineos.theme" + File.separator + IconContentProvider.DATABASE_NAME;
		InputStream is = null;
		FileOutputStream fos = null;
		try {
			is = context.getAssets().open(IconContentProvider.DATABASE_NAME);
			ThemeLog.v(TAG, "qwe is=" + is);

			File f = new File(path);
			ThemeLog.v(TAG, "qwe path=" + path);
			if (f.exists()) {
				f.delete();
			}
			fos = new FileOutputStream(f);
			int len = -1;
			byte[] buffer = new byte[1024];
			while ((len = is.read(buffer)) != -1) {
				fos.write(buffer, 0, len);
				fos.flush();
			}			
		} catch (IOException e) {
			System.exit(0);
		} finally {
			try {
				if (is != null) {
					is.close();
				}
				
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {
				System.exit(0);
			}
		}
		
		SQLiteDatabase db = context.openOrCreateDatabase(path, Context.MODE_PRIVATE, null);
		ContentResolver mResolver = context.getContentResolver();
		Cursor c = null;
		try {
			c = db.rawQuery("select * from icons", null);

			ThemeLog.v(TAG, "qwe icons Cursor rawQuery="+c.getCount());
			
			while (c.moveToNext()) {
				
				ContentValues values = new ContentValues();
				values.put(ThemeContentProvider.IconSQLiteOpenHelper.COLUMN_ICON_FILE_NAME, c.getString(c.getColumnIndex(ThemeContentProvider.IconSQLiteOpenHelper.COLUMN_ICON_FILE_NAME)));
				values.put(ThemeContentProvider.IconSQLiteOpenHelper.COLUMN_ICON_CLASS_NAME, c.getString(c.getColumnIndex(ThemeContentProvider.IconSQLiteOpenHelper.COLUMN_ICON_CLASS_NAME)));
				values.put(ThemeContentProvider.IconSQLiteOpenHelper.COLUMN_ICON_FILE_PATH, c.getString(c.getColumnIndex(ThemeContentProvider.IconSQLiteOpenHelper.COLUMN_ICON_FILE_PATH)));
				values.put(ThemeContentProvider.IconSQLiteOpenHelper.COLUMN_ICON_NAME, c.getString(c.getColumnIndex(ThemeContentProvider.IconSQLiteOpenHelper.COLUMN_ICON_NAME)));
				values.put(ThemeContentProvider.IconSQLiteOpenHelper.COLUMN_ICON_PACKAGE_NAME, c.getString(c.getColumnIndex(ThemeContentProvider.IconSQLiteOpenHelper.COLUMN_ICON_PACKAGE_NAME)));

				mResolver.insert(ThemeContentProvider.IconSQLiteOpenHelper.CONTENT_URI, values);
			}

		} catch (Exception e) {
			ThemeLog.v(TAG, "qwe addIconsToDb Exception " + e);
		} finally {
			if (c != null) {
				c.close();
			}
		}
	}
	
	public static void addThemesToDb(Context context, boolean initDataBase) throws UnsupportedEncodingException {
		ThemeLog.e(TAG, "qwe addThemesToDb,begin to add themes to db");
		String[] availableThemes = addAvailableThemesToDb(context);
		String[] defaultThemes = addDefaultThemesToDb(context);
		ThemeLog.e(TAG, "qwe addThemesToDb,initDataBase="+initDataBase);
		int themeLength;
		themeLength = defaultThemes.length + (availableThemes!=null?availableThemes.length:0);
		if (themeLength>0) {
			int initFlag = 0;
			ThemeData theme = null;			
			if (initDataBase) {
				IThemeManagerService ts = IThemeManagerService.Stub.asInterface(ServiceManager.getService("ThemeService"));
				String defaultTheme = null;
				Cursor cursor = null;
				ThemeLog.e(TAG, "addThemesToDb,ts="+ts);
				try {
					defaultTheme = ts.getDefaultThemePath();
					ThemeLog.i(TAG, "addThemesToDb theme defaultTheme: " + defaultTheme);

					try {
						cursor = context.getContentResolver().query(ThemeSQLiteHelper.CONTENT_URI, null, null, null, ThemeSQLiteHelper.COLUMN_ID + " DESC");
						cursor.moveToFirst();
						while (!cursor.isAfterLast()) {
							theme = cursorToTheme(cursor);
							if (theme.getThemePath().equals(defaultTheme)) {
								ThemeLog.i(TAG, "addThemesToDb theme get defaultTheme");
								break;
							}
							cursor.moveToNext();
						}
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						if (cursor != null) {
							cursor.close();
						}
					}
				} catch (RemoteException e1) {
					
					e1.printStackTrace();
				}
				resetAllUsingFlags(context);

				setUsingFlagByType(context, theme, -1);
				ThemeLog.i(TAG, "addThemesToDb theme =" + theme.getTitle());
			}

			removeNonExistingThemes(context, availableThemes);
		}

	}
	
	public static String[] addAvailableThemesToDb (Context context) throws UnsupportedEncodingException {
		String[] availableThemes = getAvailableThemes(Constant.DEFAULT_THEME_PATH); // 从默认路径过滤主题（/FineOS/.theme/.themes）

		if (availableThemes != null && availableThemes.length > 0) {
			for (String themeId : availableThemes) {
				ThemeLog.i(TAG, "addThemesToDb availableThemes themeId=" + themeId);
				addThemeEntryToDb(FileUtils.stripExtension(themeId), // 为所有主题创建数据库
						Constant.DEFAULT_THEME_PATH + "/" + themeId, context, false, true);
			}
		}
		return availableThemes;
	}
	
	public static String[] addDefaultThemesToDb (Context context) throws UnsupportedEncodingException {
		String[] defaultThemes = getAvailableThemes(Constant.SYSTEM_THEME_PATH); // 从默认路径过滤主题（system/media/theme）
		if (defaultThemes != null && defaultThemes.length > 0) {
			for (String themeId : defaultThemes) {
				addThemeEntryToDb(FileUtils.stripExtension(themeId), // 为所有主题创建数据库
						Constant.SYSTEM_THEME_PATH + "/" + themeId, context, true, true);
			}
		}
		return defaultThemes;
	}

	private static ThemeData cursorToTheme(Cursor cursor) {
		ThemeData theme = new ThemeData();
		theme.setId(cursor.getInt(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_ID)));
		theme.setFileName(cursor.getString(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_THEME_FILE_NAME)));
		theme.setlastUpdate(Long.getLong(cursor.getString(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_THEME_LASTUPDATE)), 0));
		theme.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_THEME_TITLE)));
		theme.setAuthor(cursor.getString(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_THEME_AUTHOR)));
		theme.setDesigner(cursor.getString(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_THEME_DESIGNER)));
		theme.setPrice(Float.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_THEME_PRICE))));
		theme.setVersion(cursor.getString(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_THEME_VERSION)));
		theme.setUiVersion(cursor.getString(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_THEME_UI_VERSION)));
		theme.setThemePath(cursor.getString(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_THEME_PATH)));
		theme.setRingtonePath(cursor.getString(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_RINGTONE_PATH)));
		theme.setWallpaperPath(cursor.getString(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_WALLPAPER_PATH)));
		theme.setScreenWallpaperPath(cursor.getString(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_LOCKSCREEN_WALLPAPER_PATH)));
		theme.setPreviewsList(cursor.getString(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_PREVIEWS_LIST)));
		theme.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_THEME_DISCRITION))); // xuqian

		theme.setVerifyFlag(cursor.getInt(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_IS_VERIFY)));
		theme.setIsDefaultTheme(cursor.getInt(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_IS_DEFAULT_THEME)) == 1);
		theme.setHasWallpaper(cursor.getInt(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_HAS_WALLPAPER)) == 1);
		theme.setHasIcons(cursor.getInt(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_HAS_ICONS)) == 1);
		theme.setHasLock(cursor.getInt(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_HAS_LOCKSCREEN)) == 1);

		theme.setHasLockscreenWallpaper(cursor.getInt(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_HAS_LOCK_WALLPAPER)) == 1);
		theme.setHasContacts(cursor.getInt(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_HAS_CONTACTS)) == 1);
		theme.setHasDialer(cursor.getInt(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_HAS_DIALER)) == 1);
		theme.setHasSystemUI(cursor.getInt(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_HAS_SYSTEMUI)) == 1);
		theme.setHasFramework(cursor.getInt(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_HAS_FRAMEWORK)) == 1);
		theme.setHasRingtone(cursor.getInt(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_HAS_RINGTONE)) == 1);
		theme.setHasNotification(cursor.getInt(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_HAS_NOTIFICATION)) == 1);
		theme.setHasBootanimation(cursor.getInt(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_HAS_BOOTANIMATION)) == 1);
		theme.setHasMms(cursor.getInt(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_HAS_MMS)) == 1);
		theme.setHasFont(cursor.getInt(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_HAS_FONT)) == 1);
		theme.setIsComplete(cursor.getInt(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_IS_COMPLETE)) == 1);

		theme.setIsUsing(cursor.getInt(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_THEME_ISUSING)) == 1); // isusing
		theme.setIsUsing_icons(cursor.getInt(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_ICONS_ISUSING)) == 1); // icons
																															// isusing
		theme.setIsUsing_lockscreen(cursor.getInt(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_LOCKSCREEN_ISUSING)) == 1); // lockscreen
																																	// isusing
		theme.setIsUsing_lockwallpaper(cursor.getInt(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_LOCK_WALLPAPER_ISUSING)) == 1); // lock
																																			// wallpaper
																																			// isusing
		theme.setIsUsing_wallpaper(cursor.getInt(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_WALLPAPER_ISUSING)) == 1); // wallpaper
																																	// isusing
		theme.setIsUsing_fonts(cursor.getInt(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN__FONT_ISUSING)) == 1); // font
																															// isusing
		theme.setIsUsing_ringtone(cursor.getInt(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_RINGTONE_ISUSING)) == 1); // ringtone
																																// isusing

		theme.setPreviewsList(cursor.getString(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_PREVIEWS_LIST)));

		return theme;
	}

	public static int update(Context context, Uri arg0, ContentValues value, String whereClause, String[] whereArgs) {

		ThemesDataSource dataSource = ThemesDataSource.getInstance(context);
		return dataSource.update(arg0, value, whereClause, whereArgs);
	}

	private static String createPreviewList(ZipFile zip, boolean hasWallpaper, boolean hasJpgWallpaper, boolean hasLockWallpaper, boolean hasJpgLockWallpaper) {
		try {
			StringBuilder builder = new StringBuilder();
			String delimeter = ""; // 分割符
			ZipEntry ze;
			for (Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements();) {
				ze = e.nextElement();
				if (ze.getName().contains("preview_")) {
					builder.append(delimeter);
					delimeter = "|";
					builder.append(ze.getName());
				}
			}

			if (hasWallpaper) {
				if (hasJpgWallpaper) {

					builder.append("|wallpaper/default_wallpaper_small.jpg");
					builder.append("|wallpaper/default_wallpaper.jpg"); // jpg
																		// first
																		// ,then
																		// png

				} else {
					builder.append("|wallpaper/default_wallpaper_small.png");
					builder.append("|wallpaper/default_wallpaper.png");

				}
			}

			if (hasLockWallpaper) {
				if (hasJpgLockWallpaper) {
					builder.append("|wallpaper/default_lock_wallpaper_small.jpg");
					builder.append("|wallpaper/default_lock_wallpaper.jpg");

				} else {
					builder.append("|wallpaper/default_lock_wallpaper_small.png");
					builder.append("|wallpaper/default_lock_wallpaper.png");

				}

			}
			return builder.toString();
		} catch (Exception e) {
		}
		return null;
	}

	public static ThemeDetails getThemeDetails(InputStream descriptionEntry) throws XmlPullParserException, IOException {
		ThemeDetails details = new ThemeDetails();
		XmlPullParser parser = Xml.newPullParser();

		parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
		if (descriptionEntry == null)
			return details;

		parser.setInput(descriptionEntry, null); // xuqian

		int eventType = parser.next();
		while (eventType != XmlPullParser.START_TAG && eventType != XmlPullParser.END_DOCUMENT)
			eventType = parser.next();
		if (eventType != XmlPullParser.START_TAG)
			throw new XmlPullParserException("No start tag found!");
		String str = parser.getName();
		// if (parser.getName().equals("ChaOS-Theme"))
		// details.isCosTheme = true;
		while (parser.next() != XmlPullParser.END_DOCUMENT) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}

			String name = parser.getName();
			if ("title".equals(name)) {
				if (parser.next() == XmlPullParser.TEXT) {
					details.title = parser.getText();
					parser.nextTag();
				}
			} else if ("designer".equals(name)) {
				if (parser.next() == XmlPullParser.TEXT) {
					details.designer = parser.getText();
					parser.nextTag();
				}
			} else if ("author".equals(name)) {
				if (parser.next() == XmlPullParser.TEXT) {
					details.author = parser.getText();
					parser.nextTag();
				}
			} else if ("version".equals(name)) {
				if (parser.next() == XmlPullParser.TEXT) {
					details.version = parser.getText();
					parser.nextTag();
				}
			} else if ("uiVersion".equals(name)) {
				if (parser.next() == XmlPullParser.TEXT) {
					details.uiVersion = parser.getText();
					parser.nextTag();
				}
			} else if ("description".equals(name)) {
				if (parser.next() == XmlPullParser.TEXT) {
					details.description = parser.getText();
					parser.nextTag();
				}
			}
		}

		return details;
	}

	public static ThemeDetails getThemeDetails(String themePath) {
		ThemeDetails details = new ThemeDetails();
		try {
			ZipFile zip = new ZipFile(themePath);

			ZipEntry entry = zip.getEntry("description.xml");
			if (entry == null)
				return details;

			try {
				details = getThemeDetails(zip.getInputStream(entry));
			} catch (Exception e) {
				return null;
			}
			zip.close();
		} catch (FileNotFoundException e) {
			return null;
		} catch (IOException e) {
			return null;
		}

		return details;
	}

	public static InputStream fetchInputStream(ThemeData theme) throws IOException {
		ZipFile zip = new ZipFile(theme.getThemePath());
		ZipEntry ze = zip.getEntry("preview/preview_launcher_0.jpg");
		if (ze == null) {
			String[] previewList = PreviewHelper.getAllPreviews(theme);
			if (previewList.length > 0)
				ze = zip.getEntry(previewList[0]); // 否则选择所有预览图的第一张
		}
		return ze != null ? zip.getInputStream(ze) : null;
	}

	public static InputStream fetchFineHomeIconIs(ThemeData theme) throws IOException {
		ZipFile zip = new ZipFile(theme.getThemePath());
		ZipEntry ze = zip.getEntry("preview/finehome_icon.png");
		// if (ze == null) {
		// String[] previewList = PreviewHelper.getAllPreviews(theme);
		// // if (previewList.length > 0)
		// // ze = zip.getEntry(previewList[0]);
		// }
		InputStream stream = ze != null ? zip.getInputStream(ze) : null;
		ThemeLog.v(TAG, "stream="+stream);
		if (stream != null) {
//			stream.close();
		}
//		zip.close();

		return stream;

	}

	public BitmapDrawable fetchDrawable(ThemeData theme) {
		String themeId = theme.getFileName();
		if (DEBUG)
			ThemeLog.d(this.getClass().getSimpleName(), "theme ID:" + themeId);
		try {
			InputStream is = fetchFineHomeIconIs(theme);
			BitmapDrawable drawable = null;
			if (is != null) {
				BitmapFactory.Options opts = new BitmapFactory.Options();
				opts.inPreferredConfig = Bitmap.Config.RGB_565;
				opts.inSampleSize = 2;
				Bitmap bmp = BitmapFactory.decodeStream(is, null, opts);
				drawable = new BitmapDrawable(bmp);
				is.close();
			}
			return drawable;
		} catch (IOException e) {
			if (DEBUG)
				ThemeLog.e(this.getClass().getSimpleName(), "fetchDrawable failed", e);
			return null;
		}
	}

	static void writeBitmap(ContentValues values, Bitmap bitmap) {
		if (bitmap != null) {
			byte[] data = flattenBitmap(bitmap);
			values.put(ThemeSQLiteHelper.COLUMN_THEME_ICON, data);
		}
	}

	static byte[] flattenBitmap(Bitmap bitmap) {
		// Try go guesstimate how much space the icon will take when serialized
		// to avoid unnecessary allocations/copies during the write.
		int size = bitmap.getWidth() * bitmap.getHeight() * 4;
		ByteArrayOutputStream out = new ByteArrayOutputStream(size);
		try {
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.flush();
			out.close();
			InputStream in;
			ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
			return out.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static final byte[] input2byte(ThemeData theme) {
		InputStream in = null;
		ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
		byte[] buff = new byte[100];
		int rc = 0;
		byte[] in2b = null;
		try {
			in = fetchFineHomeIconIs(theme);
			while ((rc = in.read(buff, 0, buff.length-1)) != -1) {
				swapStream.write(buff, 0, rc);
			}
			in.close();
			in2b = swapStream.toByteArray();
			swapStream.close();
		} catch (Exception e) {
			ThemeLog.v(TAG, "e="+e);
		}
		
		ThemeLog.v(TAG,"in2b1 : "+in2b);
		if(in2b==null){
			
			in2b = input2byte2(theme);
		}
		ThemeLog.v(TAG,"in2b2 : "+in2b);
		
		return in2b;
	}
	
	///xuqian add begin 
	
	public static final byte[] input2byte2(ThemeData theme) {
		
		   BitmapDrawable drawable = fetchHomeIconDrawableFromWallpaper(theme);
		   Bitmap bitmap = null ;
		   if(drawable!=null){
			   bitmap = drawable.getBitmap();
		   }
		   if(bitmap==null){
			   return null ;
		   }
		   bitmap = zoomImage(bitmap,120,120);
		   bitmap = getRoundedCornerBitmap(bitmap);
		   ThemeLog.v(TAG,"input2byte2,bitmap w:"+bitmap.getWidth());
		   ByteArrayOutputStream out = new ByteArrayOutputStream();
			try {
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
				out.flush();
				out.close();
				return out.toByteArray();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		
	}
	
	public static BitmapDrawable fetchHomeIconDrawableFromWallpaper(ThemeData theme) {
		String themeId = theme.getFileName();
		try {
			InputStream is = fetchFineHomeIconIsFromWallpaper(theme);
			ThemeLog.v(TAG,"fetchHomeIconDrawableFromWallpaper,is:"+is);
			BitmapDrawable drawable = null;
			if (is != null) {
				BitmapFactory.Options opts = new BitmapFactory.Options();
				opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
				opts.inSampleSize = 2;
				Bitmap bmp = BitmapFactory.decodeStream(is, null, opts);
				drawable = new BitmapDrawable(bmp);
				is.close();
			}
			return drawable;
		} catch (IOException e) {
			return null;
		}
	}
	
	public static InputStream fetchFineHomeIconIsFromWallpaper(ThemeData theme) throws IOException {
		ZipFile zip = new ZipFile(theme.getThemePath());
		ZipEntry ze = zip.getEntry("wallpaper/default_wallpaper_small.png");
		if(ze ==null){
			ze = zip.getEntry("wallpaper/default_wallpaper_small.jpg");
		}
		InputStream stream = ze != null ? zip.getInputStream(ze) : null;
		ThemeLog.v(TAG, "fetchFineHomeIconIsFromWallpaper, stream="+stream);
		
		return stream;

	}
	
	public static Bitmap zoomImage(Bitmap bgimage, double newWidth,
			double newHeight) {
		float width = bgimage.getWidth();
		float height = bgimage.getHeight();
		
		Matrix matrix = new Matrix();
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) width,
				(int) height, matrix, true);
		
		if(bgimage!=null){         //release old bitmap
			bgimage.recycle();
			bgimage = null ;
		}
		
		return bitmap;
	}
	
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
	    Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
	    bitmap.getHeight(), Config.ARGB_8888);
	    
	    Canvas canvas = new Canvas(output);
	 
	    final int color = 0xff424242;
	    final Paint paint = new Paint();
	    final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
	    final RectF rectF = new RectF(rect);
	    final float roundPx = 12;
	 
	    paint.setAntiAlias(true);
	    canvas.drawARGB(0, 0, 0, 0);
	    paint.setColor(color);
	    canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
	 
	    paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
	    canvas.drawBitmap(bitmap, rect, rect, paint);
	 
	    if(bitmap!=null){         //release old bitmap
	    	bitmap.recycle();
	    	bitmap = null ;
		}
	    
	    return output;
	  }
	
	///xuqian add end 
	

	public static boolean checkNetworkState(Context context) {
		// TODO Auto-generated method stub
		ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (connectMgr == null) {
			return false;
		}

		NetworkInfo nwInfo = connectMgr.getActiveNetworkInfo();

		if (nwInfo == null || !nwInfo.isAvailable()) {
			return false;
		}
		return true;
	}

	public static String getpackageName(ThemeData themeData, boolean isOnline) {
		String pathString = isOnline ? themeData.getDownloadUrl() : themeData.getThemePath();

		pathString = pathString.substring(pathString.lastIndexOf(File.separator) + 1);

		return pathString.substring(0, pathString.indexOf("."));
	}
	
	public static void applyTheme(Context context, String theme, boolean applyFont, boolean scaleBoot, boolean removeExistingTheme) {
		ThemeLog.i(TAG,"ThemeDetailLockscreenActivity applyTheme theme1: " + theme);
		theme = copyFileToSystem(theme, context);
		ThemeLog.i(TAG,"ThemeDetailLockscreenActivity applyTheme theme2: " + theme);
		IThemeManagerService ts = IThemeManagerService.Stub.asInterface(ServiceManager.getService("ThemeService"));
		try {
			ts.applyThemeFont(ThemeContentProvider.CONTENT + FileUtils.stripPath(theme));

			ThemeLog.e(TAG, "DIALOG_PROGRESS");

		} catch (Exception e) {
			ThemeLog.e(TAG, "exception happened: "+e.getMessage());
			// SimpleDialogs.displayOkDialog(R.string.dlg_theme_failed_title,
			// R.string.dlg_theme_failed_body,
			// this);
		}
		if (theme != null) {
			String useThemePath = Constant.DEFAULT_THEME_PATH  + File.separator + theme.substring(theme.lastIndexOf(File.separator) + 1);
			ThemeUtils.resetUsingFlagByType(context, ThemeData.THEME_ELEMENT_TYPE_FONT);
			ThemeUtils.setUsingFlagByType(context, useThemePath, ThemeData.THEME_ELEMENT_TYPE_FONT);
		}
	}
	
	public static String copyFileToSystem(String theme, Context context) {
		ThemeLog.i(TAG, "copyFileToSystem go ");

		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT
				&& !theme.startsWith("/system")) {
			File file = new File(theme);
			File newFile = context.getDir("themes", 0);
			File[] listFiles = newFile.listFiles();
			for (int i = 0; i < listFiles.length; i++) {
				if (!listFiles[i].getName().equals(theme.substring(theme.lastIndexOf(File.separator) + 1))) {
					listFiles[i].delete();
				}
			}

			newFile = new File(newFile.getPath(), theme.substring(theme.lastIndexOf(File.separator) + 1));			if (!newFile.exists()) {
				try {
					copyFile(file, newFile);
				} catch (Exception e) {
					ThemeLog.e(TAG, "copyFileToSystem copyFile Exception: ", e);
				}
			}
			return newFile.getPath();
		}
		return theme;
	}
	
	private static void copyFile(File sourceFile, File targetFile) throws IOException {
		BufferedInputStream inBuff = null;
		BufferedOutputStream outBuff = null;
		try {
			// 新建文件输入流并对它进行缓冲
			inBuff = new BufferedInputStream(new FileInputStream(sourceFile));
			// 新建文件输出流并对它进行缓冲
			outBuff = new BufferedOutputStream(new FileOutputStream(targetFile));

			// 缓冲数组
			byte[] b = new byte[1024 * 5];
			int len;
			while ((len = inBuff.read(b)) != -1) {
				outBuff.write(b, 0, len);
			}

			// 刷新此缓冲的输出流
			outBuff.flush();
		} finally {
			// 关闭流
			if (inBuff != null)
				inBuff.close();
			if (outBuff != null)
				outBuff.close();
		}
	}
	
	public static SwingBottomInAnimationAdapter setAnimationAdapter(AbsListView abslistView, int delayMillis, BaseAdapter baseAdapter) {
		SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(baseAdapter);
		swingBottomInAnimationAdapter.setAbsListView(abslistView);
		swingBottomInAnimationAdapter.setShouldAnimate(false);
		abslistView.setAdapter(swingBottomInAnimationAdapter);
		return swingBottomInAnimationAdapter;
	}

	public static class ThemeDetails {
		public String title;
		public String designer;
		public String author;
		public String version;
		public String uiVersion;
		// public boolean isCosTheme;
		public String description;
	}
}
