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

package com.fineos.theme.provider;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.util.Log;

import com.fineos.theme.model.ThemeData;
import com.fineos.theme.utils.ThemeLog;
import com.fineos.theme.utils.ThemeUtils;

public class ThemesDataSource {

	private static final String TAG = "ThemesDataSource";
	// Database fields
	private Context mContext;
	private ContentResolver mResolver;
	private static ThemesDataSource sInstance;
	private String[] allColumns = { ThemeSQLiteHelper.COLUMN_ID, ThemeSQLiteHelper.COLUMN_THEME_FILE_NAME, ThemeSQLiteHelper.COLUMN_THEME_LASTUPDATE, ThemeSQLiteHelper.COLUMN_THEME_TITLE,
			ThemeSQLiteHelper.COLUMN_THEME_AUTHOR, ThemeSQLiteHelper.COLUMN_THEME_DESIGNER, ThemeSQLiteHelper.COLUMN_THEME_PRICE, ThemeSQLiteHelper.COLUMN_THEME_VERSION, ThemeSQLiteHelper.COLUMN_THEME_UI_VERSION,
			ThemeSQLiteHelper.COLUMN_THEME_PATH, ThemeSQLiteHelper.COLUMN_RINGTONE_PATH, ThemeSQLiteHelper.COLUMN_WALLPAPER_PATH, ThemeSQLiteHelper.COLUMN_LOCKSCREEN_WALLPAPER_PATH,
			ThemeSQLiteHelper.COLUMN_PREVIEWS_LIST, ThemeSQLiteHelper.COLUMN_THEME_DISCRITION, ThemeSQLiteHelper.COLUMN_IS_VERIFY, ThemeSQLiteHelper.COLUMN_IS_DEFAULT_THEME,
			ThemeSQLiteHelper.COLUMN_HAS_WALLPAPER, ThemeSQLiteHelper.COLUMN_HAS_LOCK_WALLPAPER, ThemeSQLiteHelper.COLUMN_HAS_ICONS, ThemeSQLiteHelper.COLUMN_HAS_LOCKSCREEN,
			ThemeSQLiteHelper.COLUMN_HAS_CONTACTS, ThemeSQLiteHelper.COLUMN_HAS_DIALER, ThemeSQLiteHelper.COLUMN_HAS_SYSTEMUI, ThemeSQLiteHelper.COLUMN_HAS_FRAMEWORK,
			ThemeSQLiteHelper.COLUMN_HAS_RINGTONE, ThemeSQLiteHelper.COLUMN_HAS_NOTIFICATION, ThemeSQLiteHelper.COLUMN_HAS_BOOTANIMATION, ThemeSQLiteHelper.COLUMN_HAS_MMS,
			ThemeSQLiteHelper.COLUMN_HAS_FONT, ThemeSQLiteHelper.COLUMN_IS_COMPLETE, ThemeSQLiteHelper.COLUMN_THEME_ISUSING, ThemeSQLiteHelper.COLUMN_PREVIEWS_LIST

	};

	public ThemesDataSource(Context context) {
		// dbHelper = new ThemeSQLiteHelper(context);
		mContext = context;
		mResolver = mContext.getContentResolver();
	}

	public static ThemesDataSource getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new ThemesDataSource(context);
		}
		return sInstance;
	}

	public void open() throws SQLException {
		// database = dbHelper.getWritableDatabase();
	}

	public void close() {
		// dbHelper.close();
	}

	public void createThemeEntry(ThemeData theme) {

		ThemeLog.i(TAG, "Has wallpaper ?  :" + theme.getHasWallpaper());

		ContentValues values = new ContentValues();
		values.put(ThemeSQLiteHelper.COLUMN_THEME_FILE_NAME, theme.getFileName());
		values.put(ThemeSQLiteHelper.COLUMN_THEME_LASTUPDATE, "" + theme.getlastUpdate());
		values.put(ThemeSQLiteHelper.COLUMN_THEME_TITLE, theme.getTitle());
		values.put(ThemeSQLiteHelper.COLUMN_THEME_AUTHOR, theme.getauthor());
		values.put(ThemeSQLiteHelper.COLUMN_THEME_DESIGNER, theme.getdesigner());
		values.put(ThemeSQLiteHelper.COLUMN_THEME_PRICE, theme.getPrice());       // add theme price
		values.put(ThemeSQLiteHelper.COLUMN_THEME_VERSION, theme.getVersion());
		values.put(ThemeSQLiteHelper.COLUMN_THEME_UI_VERSION, theme.getUiVersion());
		values.put(ThemeSQLiteHelper.COLUMN_THEME_PATH, theme.getThemePath());
		values.put(ThemeSQLiteHelper.COLUMN_RINGTONE_PATH, theme.getRingtonePath());
		values.put(ThemeSQLiteHelper.COLUMN_WALLPAPER_PATH, theme.getWallpaperPath());
		values.put(ThemeSQLiteHelper.COLUMN_LOCKSCREEN_WALLPAPER_PATH, theme.getScreenWallpaperPath());
		values.put(ThemeSQLiteHelper.COLUMN_PREVIEWS_LIST, theme.getPreviewsList());
		values.put(ThemeSQLiteHelper.COLUMN_THEME_DISCRITION, theme.getDescription()); 
		values.put(ThemeSQLiteHelper.COLUMN_THEME_ICON, ThemeUtils.input2byte2(theme));     //homeicon ,from wallpaper
		values.put(ThemeSQLiteHelper.COLUMN_IS_DEFAULT_THEME, theme.getIsDefaultTheme());
		values.put(ThemeSQLiteHelper.COLUMN_HAS_WALLPAPER, theme.getHasWallpaper());
		values.put(ThemeSQLiteHelper.COLUMN_HAS_LOCK_WALLPAPER, theme.getHasLockscreenWallpaper());
		values.put(ThemeSQLiteHelper.COLUMN_HAS_ICONS, theme.getHasIcons());
		values.put(ThemeSQLiteHelper.COLUMN_HAS_LOCKSCREEN, theme.getHasLock()); // lockscreen
		values.put(ThemeSQLiteHelper.COLUMN_HAS_CONTACTS, theme.getHasContacts());
		values.put(ThemeSQLiteHelper.COLUMN_HAS_DIALER, theme.getHasDialer());
		values.put(ThemeSQLiteHelper.COLUMN_HAS_SYSTEMUI, theme.getHasSystemUI());
		values.put(ThemeSQLiteHelper.COLUMN_HAS_FRAMEWORK, theme.getHasFramework());
		values.put(ThemeSQLiteHelper.COLUMN_HAS_RINGTONE, theme.getHasRingtone());
		values.put(ThemeSQLiteHelper.COLUMN_HAS_NOTIFICATION, theme.getHasNotification());
		values.put(ThemeSQLiteHelper.COLUMN_HAS_BOOTANIMATION, theme.getHasBootanimation());
		values.put(ThemeSQLiteHelper.COLUMN_HAS_MMS, theme.getHasMms());
		values.put(ThemeSQLiteHelper.COLUMN_HAS_FONT, theme.getHasFont());
		values.put(ThemeSQLiteHelper.COLUMN_IS_COMPLETE, theme.getIsComplete());
		values.put(ThemeSQLiteHelper.COLUMN_PREVIEWS_LIST, theme.getPreviewsList());
		values.put(ThemeSQLiteHelper.COLUMN_IS_VERIFY, theme.getVerifyFlag());

		if (entryExists(theme.getFileName()) /* || themeExists(theme) */) {

			ThemeLog.d(TAG, "theme:" + theme.getFileName() + "  exist,just update");

			int count = mResolver.update(ThemeSQLiteHelper.CONTENT_URI, values, ThemeSQLiteHelper.COLUMN_THEME_PATH + "='" + theme.getThemePath() + "'", null);

			ThemeLog.d(TAG, "update count:" + count);

		} else {
			ThemeLog.d(TAG, "theme:" + theme.getFileName() + "  not exist,insert !!!");
			mResolver.insert(ThemeSQLiteHelper.CONTENT_URI, values);

		}

	}

	public void insertIconEntry() {

		ContentValues values = new ContentValues();
		values.put(ThemeContentProvider.IconSQLiteOpenHelper.COLUMN_ICON_FILE_NAME, "zxc");
		values.put(ThemeContentProvider.IconSQLiteOpenHelper.COLUMN_ICON_CLASS_NAME, "zxczxc");
		values.put(ThemeContentProvider.IconSQLiteOpenHelper.COLUMN_ICON_FILE_PATH, "zxczxc");
		values.put(ThemeContentProvider.IconSQLiteOpenHelper.COLUMN_ICON_NAME, "zxczxc");
		values.put(ThemeContentProvider.IconSQLiteOpenHelper.COLUMN_ICON_PACKAGE_NAME, "zxczxc");

		mResolver.insert(ThemeContentProvider.IconSQLiteOpenHelper.CONTENT_URI, values);

	}
	
	public void insertIconEntry2() {

		ContentValues values = new ContentValues();
		values.put(ThemeContentProvider.IconSQLiteOpenHelper.COLUMN_ICON_FILE_NAME, "zxc222");
		values.put(ThemeContentProvider.IconSQLiteOpenHelper.COLUMN_ICON_CLASS_NAME, "zxczxc222");
		values.put(ThemeContentProvider.IconSQLiteOpenHelper.COLUMN_ICON_FILE_PATH, "zxczxc222");
		values.put(ThemeContentProvider.IconSQLiteOpenHelper.COLUMN_ICON_NAME, "zxczxc222");
		values.put(ThemeContentProvider.IconSQLiteOpenHelper.COLUMN_ICON_PACKAGE_NAME, "zxczxc222");

		mResolver.insert(ThemeContentProvider.IconSQLiteOpenHelper.CONTENT_URI, values);

	}
	
	public void updateThemePrice(ThemeData theme) {

		ContentValues values = new ContentValues();
		values.put(ThemeSQLiteHelper.COLUMN_THEME_PRICE, theme.getPrice());
		ThemeLog.v(TAG,"Price :"+theme.getPrice());
		ThemeLog.d(TAG, "insertThemePrice,theme:" + theme.getFileName() + "  exist,just update");
		int count = mResolver.update(ThemeSQLiteHelper.CONTENT_URI, values, ThemeSQLiteHelper.COLUMN_THEME_PATH+ "='" + theme.getThemePath() + "'", null);
		ThemeLog.d(TAG, "insertThemePrice,update count:" + count);
		
	}
	
	public void deleteTheme(ThemeData theme) {
		long id = theme.getId();
		ThemeLog.d(TAG, "Theme deleted with id: " + id);
		mResolver.delete(ThemeSQLiteHelper.CONTENT_URI, ThemeSQLiteHelper.COLUMN_ID + " = " + id, null);
		// SQLiteDatabase database;
		// database.delete(ThemeSQLiteHelper.TABLE_THEMES,
		// ThemeSQLiteHelper.COLUMN_ID
		// + " = " + id, null);
	}

	public int update(Uri arg0, ContentValues value, String whereClause, String[] whereArgs) {

		return mResolver.update(arg0, value, whereClause, whereArgs);
	}

	public boolean entryExists(String themeId) {
		Cursor c = null;
		boolean exists = false;
		try {
			c = mResolver.query(ThemeSQLiteHelper.CONTENT_URI, null, "file_name=?", new String[] { themeId }, null);
			if (c != null) {
				exists = c.getCount() > 0;
			}
		} catch (Exception e) {

		} finally {
			if (c != null) {
				c.close();
			}
		}
		ThemeLog.i(TAG, "theme themeId :" + themeId + ",exists=" + exists);
		return exists;
	}

	public boolean entryExists(long themeId) {
		// SQLiteDatabase database = null;
		// Cursor c = database.query(ThemeSQLiteHelperUtils.TABLE_THEMES,
		// allColumns,
		// ThemeSQLiteHelperUtils.COLUMN_THEME_FILE_NAME + "='" + themeId + "'",
		// null, null, null, null);
		ContentResolver cr = mContext.getContentResolver();
		Cursor c = null;
		boolean exists = false;
		String selection = new StringBuilder().append(" AND _id like '").append(themeId).append("%'").toString();
		try {
			c = cr.query(ThemeSQLiteHelper.CONTENT_URI, new String[] { ThemeSQLiteHelper.CONTENT_URI.toString() }, selection, null, null);
			if (c != null) {
				return true;
			}
		} catch (Exception e) {

		} finally {
			if (c != null) {
				c.close();
			}
		}
		return exists;
	}

	public boolean themeExists(ThemeData theme) {
		Cursor c = null;
		String title = theme.getTitle();
		boolean exists = false;
		try {
			c = mResolver.query(ThemeSQLiteHelper.CONTENT_URI, null, "title=?", new String[] { title }, null);
			if (c != null) {
				exists = c.getCount() > 0;
			}
		} catch (Exception e) {

		} finally {
			if (c != null) {
				c.close();
			}
		}
		ThemeLog.i(TAG, "theme title :" + title + ",exists=" + exists);
		return exists;
	}

	public boolean entryIsOlder(String themeId, long lastModified) {
		Cursor c = null;
		boolean isOlder = false;
		try {
			c = mResolver.query(ThemeSQLiteHelper.CONTENT_URI, null, "file_name=?", new String[] { themeId }, null);
			if (c != null) {
				if (c.getCount() > 0) {
					c.moveToFirst();
					String modified = c.getString(c.getColumnIndex(ThemeSQLiteHelper.COLUMN_THEME_LASTUPDATE));
					if (modified == null/*
										 * || lastModified >
										 * Long.parseLong(modified)
										 */)
						isOlder = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return isOlder;
	}

	public boolean entryThemeIsOlder(ThemeData themeData) {
		// Cursor c = database.query(ThemeSQLiteHelperUtils.TABLE_THEMES,
		// allColumns,
		// ThemeSQLiteHelperUtils.COLUMN_THEME_FILE_NAME + "='" + themeId + "'",
		// null, null, null, null);
		ContentResolver cr = mContext.getContentResolver();
		Cursor c = null;
		boolean isOlder = false;
		String selection = new StringBuilder().append(" AND _id like '").append(themeData.getId()).append("%'").toString();
		try {
			c = cr.query(ThemeSQLiteHelper.CONTENT_URI, null, selection, null, null);
			if (c != null) {
				isOlder = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return isOlder;
	}

	public ThemeData getThemeById(long id) {
		ThemeData theme = null;

		// Cursor c = database.query(ThemeSQLiteHelper.TABLE_THEMES, allColumns,
		// ThemeSQLiteHelper.COLUMN_ID + "='" + id + "'",
		// null, null, null, null);
		Cursor c = null;
		try {
			c = mResolver.query(ThemeSQLiteHelper.CONTENT_URI, null, "_id=?", new String[] { String.valueOf(id) }, null);
			if (c != null) {
				if (c.getCount() > 0) {
					c.moveToFirst();
					theme = cursorToTheme(c);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return theme;
	}

	public ThemeData getThemeByFileId(String packageName) {
		ThemeData theme = null;

		Cursor c = null;
		try {
			c = mResolver.query(ThemeSQLiteHelper.CONTENT_URI, null, "file_name=?", new String[] { packageName }, null);
			if (c != null) {
				if (c.getCount() > 0) {
					c.moveToFirst();
					theme = cursorToTheme(c);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (c != null) {
				c.close();
			}
		}
		if (theme != null) {
			ThemeLog.v(TAG, "check theme=" + theme.getTitle());
			ThemeLog.v(TAG, "check theme=" + theme.getPreviewsList());
		}
		return theme;
	}

	public List<ThemeData> getAllThemes() {
		List<ThemeData> themes = new ArrayList<ThemeData>();
		Cursor cursor = null;
		try {
			cursor = mResolver.query(ThemeSQLiteHelper.CONTENT_URI, null, null, null, null);
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				ThemeData theme = cursorToTheme(cursor);
				themes.add(theme);
				cursor.moveToNext();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return themes;
	}

	public List<ThemeData> getCompleteThemes() {
		List<ThemeData> themes = new ArrayList<ThemeData>();
		Cursor cursor = null;
		try {
			cursor = mResolver.query(ThemeSQLiteHelper.CONTENT_URI, null, "is_complete=1", null, null);
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				ThemeData theme = cursorToTheme(cursor);
				themes.add(theme);
				cursor.moveToNext();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return themes;
	}

	public List<ThemeData> getIconThemes() {
		List<ThemeData> themes = new ArrayList<ThemeData>();

		Cursor cursor = null;
		try {
			cursor = mResolver.query(ThemeSQLiteHelper.CONTENT_URI, null, ThemeSQLiteHelper.COLUMN_HAS_ICONS + "='1'", null, null);
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				ThemeData theme = cursorToTheme(cursor);
				themes.add(theme);
				cursor.moveToNext();
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		// Make sure to close the cursor
		return themes;
	}

	public List<ThemeData> getLockThemes() {
		List<ThemeData> themes = new ArrayList<ThemeData>();

		Cursor cursor = null;
		try {
			cursor = mResolver.query(ThemeSQLiteHelper.CONTENT_URI, null, ThemeSQLiteHelper.COLUMN_HAS_LOCKSCREEN + "='1'", null, null);
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				ThemeData theme = cursorToTheme(cursor);
				themes.add(theme);
				cursor.moveToNext();
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		// Make sure to close the cursor
		return themes;
	}

	public List<ThemeData> getWallpaperThemes() {
		List<ThemeData> themes = new ArrayList<ThemeData>();

		Cursor cursor = null;
		try {
			cursor = mResolver.query(ThemeSQLiteHelper.CONTENT_URI, null, ThemeSQLiteHelper.COLUMN_HAS_WALLPAPER + "='1'", null, null);

			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				ThemeData theme = cursorToTheme(cursor);
				themes.add(theme);
				cursor.moveToNext();
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return themes;
	}

	public List<ThemeData> getLockscreenWallpaperThemes() {
		List<ThemeData> themes = new ArrayList<ThemeData>();

		Cursor cursor = null;
		try {
			cursor = mResolver.query(ThemeSQLiteHelper.CONTENT_URI, null, ThemeSQLiteHelper.COLUMN_HAS_LOCK_WALLPAPER + "='1'", null, null);
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				ThemeData theme = cursorToTheme(cursor);
				themes.add(theme);
				cursor.moveToNext();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return themes;
	}

	public List<ThemeData> getSystemUIThemes() {
		List<ThemeData> themes = new ArrayList<ThemeData>();

		Cursor cursor = null;
		try {
			cursor = mResolver.query(ThemeSQLiteHelper.CONTENT_URI, null, ThemeSQLiteHelper.COLUMN_HAS_SYSTEMUI + "='1'", null, null);

			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				ThemeData theme = cursorToTheme(cursor);
				themes.add(theme);
				cursor.moveToNext();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return themes;
	}

	public List<ThemeData> getFrameworkThemes() {
		List<ThemeData> themes = new ArrayList<ThemeData>();

		Cursor cursor = null;
		try {

			cursor = mResolver.query(ThemeSQLiteHelper.CONTENT_URI, null, ThemeSQLiteHelper.COLUMN_HAS_FRAMEWORK + "='1'", null, null);

			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				ThemeData theme = cursorToTheme(cursor);
				themes.add(theme);
				cursor.moveToNext();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return themes;
	}

	public List<ThemeData> getRingtoneThemes() {
		List<ThemeData> themes = new ArrayList<ThemeData>();

		Cursor cursor = null;
		try {

			cursor = mResolver.query(ThemeSQLiteHelper.CONTENT_URI, null, ThemeSQLiteHelper.COLUMN_HAS_RINGTONE + "='1' OR " + ThemeSQLiteHelper.COLUMN_HAS_NOTIFICATION + "='1'", null, null);

			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				ThemeData theme = cursorToTheme(cursor);
				themes.add(theme);
				cursor.moveToNext();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return themes;
	}

	public List<ThemeData> getBootanimationThemes() {
		List<ThemeData> themes = new ArrayList<ThemeData>();

		Cursor cursor = null;
		try {

			cursor = mResolver.query(ThemeSQLiteHelper.CONTENT_URI, null, ThemeSQLiteHelper.COLUMN_HAS_BOOTANIMATION + "='1'", null, null);
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				ThemeData theme = cursorToTheme(cursor);
				themes.add(theme);
				cursor.moveToNext();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return themes;
	}

	public List<ThemeData> getMmsThemes() {
		List<ThemeData> themes = new ArrayList<ThemeData>();

		Cursor cursor = null;
		try {

			cursor = mResolver.query(ThemeSQLiteHelper.CONTENT_URI, null, ThemeSQLiteHelper.COLUMN_HAS_MMS + "='1'", null, null);
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				ThemeData theme = cursorToTheme(cursor);
				themes.add(theme);
				cursor.moveToNext();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return themes;
	}

	public List<ThemeData> getContactsThemes() {
		List<ThemeData> themes = new ArrayList<ThemeData>();

		Cursor cursor = null;
		try {

			cursor = mResolver.query(ThemeSQLiteHelper.CONTENT_URI, null, ThemeSQLiteHelper.COLUMN_HAS_CONTACTS + "='1'", null, null);
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				ThemeData theme = cursorToTheme(cursor);
				themes.add(theme);
				cursor.moveToNext();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return themes;
	}

	public List<ThemeData> getDialerThemes() {
		List<ThemeData> themes = new ArrayList<ThemeData>();

		Cursor cursor = null;
		try {

			cursor = mResolver.query(ThemeSQLiteHelper.CONTENT_URI, null, ThemeSQLiteHelper.COLUMN_HAS_DIALER + "='1'", null, null);
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				ThemeData theme = cursorToTheme(cursor);
				themes.add(theme);
				cursor.moveToNext();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return themes;
	}

	public List<ThemeData> getFontThemes() {
		List<ThemeData> themes = new ArrayList<ThemeData>();

		Cursor cursor = null;
		try {

			cursor = mResolver.query(ThemeSQLiteHelper.CONTENT_URI, null, ThemeSQLiteHelper.COLUMN_HAS_FONT + "='1'", null, null);
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				ThemeData theme = cursorToTheme(cursor);
				themes.add(theme);
				cursor.moveToNext();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return themes;
	}

	private ThemeData cursorToTheme(Cursor cursor) {
		ThemeData theme = new ThemeData();
		theme.setId(cursor.getInt(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_ID)));
		theme.setFileName(cursor.getString(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_THEME_FILE_NAME)));
		theme.setlastUpdate(Long.getLong(cursor.getString(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_THEME_LASTUPDATE)), 0));
		theme.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_THEME_TITLE)));
		theme.setAuthor(cursor.getString(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_THEME_AUTHOR)));
		theme.setDesigner(cursor.getString(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_THEME_DESIGNER)));
		ThemeLog.v(TAG,"price from cursor :"+cursor.getString(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_THEME_PRICE)));
		theme.setPrice(Float.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_THEME_PRICE))));
		theme.setVersion(cursor.getString(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_THEME_VERSION)));
		theme.setUiVersion(cursor.getString(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_THEME_UI_VERSION)));
		theme.setThemePath(cursor.getString(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_THEME_PATH)));
		theme.setRingtonePath(cursor.getString(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_RINGTONE_PATH)));
		theme.setWallpaperPath(cursor.getString(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_WALLPAPER_PATH)));
		theme.setScreenWallpaperPath(cursor.getString(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_LOCKSCREEN_WALLPAPER_PATH)));
		theme.setPreviewsList(cursor.getString(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_PREVIEWS_LIST)));
		theme.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_THEME_DISCRITION)));

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
		theme.setPackageName(cursor.getString(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_THEME_FILE_NAME)));
		return theme;
	}
}
