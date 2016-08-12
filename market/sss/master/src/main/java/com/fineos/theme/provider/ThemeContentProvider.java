package com.fineos.theme.provider;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.fineos.theme.R.string;
import com.fineos.theme.utils.Constant;
import com.fineos.theme.utils.ThemeLog;
import com.fineos.theme.utils.ThemeUtils;

public class ThemeContentProvider extends ContentProvider {

	public static final String TAG = "ThemeContentProvider";
	private static final String THEMES_PATH = Constant.DEFAULT_THEME_PATH;
	private ThemeSQLiteOpenHelper mSQHelper;
	private IconSQLiteOpenHelper mIconSQHelper;

	public static final String CONTENT = "content://com.fineos.theme/";
	public static final Uri CONTENT_URI = Uri.parse(CONTENT);
	public static final String CONTENT_BACKUP = "content://com.fineos.theme.backup/";
	public static final Uri CONTENT_BACKUP_URI = Uri.parse(CONTENT_BACKUP);
	private static final HashMap<String, String> MIME_TYPES = new HashMap<String, String>();

	
	private static final UriMatcher mUriMatcher;
	private static final int RESET = 101;
	private static final int SET = 102;
	private static final int REVERT = 103;
	private static final int WALLPAPER_RESET = 104;
	private static final int WALLPAPER_SET = 105;

	static {
		mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		mUriMatcher.addURI("com.fineos.thememgr.Settings", "themes/resetAllUsingFlags", RESET);
		mUriMatcher.addURI("com.fineos.thememgr.Settings", "themes/setUsingFlags", SET);
		mUriMatcher.addURI("com.fineos.thememgr.Settings", "themes/revertAll", REVERT);
		mUriMatcher.addURI("com.fineos.thememgr.Settings", "themes/resetWallpaperUsingFlags", WALLPAPER_RESET);
		mUriMatcher.addURI("com.fineos.thememgr.Settings", "themes/setWallpaperUsingFlags", WALLPAPER_SET);

		MIME_TYPES.put(".ctz", "application/zip");
		MIME_TYPES.put(".CTZ", "application/zip");
		MIME_TYPES.put(".mtz", "application/zip");
		MIME_TYPES.put(".MTZ", "application/zip");
		MIME_TYPES.put(".ftz", "application/zip");
		MIME_TYPES.put(".FTZ", "application/zip");
		MIME_TYPES.put(".mp3", "audio/mp3");
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		String path = uri.toString();

		for (String extension : MIME_TYPES.keySet()) {
			if (path.endsWith(extension)) {
				return (MIME_TYPES.get(extension));
			}
		}

		return null;
	}

	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
		File f = null;
		ThemeLog.i(TAG, "openFile,mode=" + mode.toString());
		ThemeLog.i(TAG, "openFile,uri=" + uri.toString());
		if (getType(uri).contains("mp3")) {
			f = new File(Constant.CACHE_DIR, uri.getPath());
		} else if (uri.toString().contains("default_fineos")) {
			f = new File(Constant.SYSTEM_THEME_PATH, uri.getPath());
		} else {
			if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
				f = new File(Constant.DEFAULT_THEME_PATH, uri.getPath());
				ThemeLog.i(TAG, "uri.getPath(): " + f.getPath());
			} else {
				f = new File(getContext().getDir("themes", 0), uri.getPath());
				ThemeLog.i("", "uri.getPath(): 22222222222: " + f.getPath());
			}
		}

		if (!f.exists()) {
			f = new File(Constant.SYSTEM_THEME_PATH, uri.getPath());
		}
		ThemeLog.i(TAG, "openFile,f=" + f.toString());
		ThemeLog.i(TAG, "openFile,f=" + f.exists());
		if (f.exists()) {
			try {
				return (ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY));
			} catch (Exception e) {
				ThemeLog.e(TAG, "openFile,Exception= " + e.getMessage());
			} finally {
			}

		}
		throw new FileNotFoundException("ThemeContentProvider---" + uri.getPath());
	}

	static private void copy(InputStream in, File dst) throws IOException {
		FileOutputStream out = new FileOutputStream(dst);
		byte[] buf = new byte[1024];
		int len;

		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);

		}
		in.close();
		out.close();
	}

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		// TODO Auto-generated method stub
		SQLiteDatabase db = mSQHelper.getWritableDatabase();
		SQLiteDatabase db2 = mIconSQHelper.getWritableDatabase();
		ThemeLog.w(TAG, "delete,arg0.toString()=" + arg0.toString());
		ThemeLog.w(TAG, "delete,arg1=" + arg1);
		db.delete(ThemeSQLiteHelper.TABLE_THEMES, arg1, arg2);
		return 0;
	}

	@Override
	public Uri insert(Uri uri, ContentValues arg) {
		ThemeLog.d(TAG, "qwe insert, uri:" + uri.toString());
		SQLiteDatabase sqlDB = mSQHelper.getWritableDatabase();
		SQLiteDatabase db2 = mIconSQHelper.getWritableDatabase();
		ContentValues values = new ContentValues(arg);
		String table = uri.getPathSegments().get(0);
		ThemeLog.d(TAG, "qwe insert,table=" + table);
		long rowId;
		if(table.equals(ThemeSQLiteHelper.TABLE_THEMES)){
			rowId = sqlDB.insert(table, null, values);
		}else{
			ThemeLog.v(TAG, "qwe query,mIconSQHelper");
			rowId = db2.insert(table, null, values);
		}
		ThemeLog.d(TAG, "qwe rowId ="+rowId);
		if (rowId > 0) {
			Uri rowUri = ContentUris.appendId(uri.buildUpon(), rowId).build();

			ThemeLog.i(TAG, "qwe Leave insert()");
			return rowUri;
		} else {
			ThemeLog.i(TAG, "qwe Leave insert() null");

			return null;
		}

		// throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		mSQHelper = new ThemeSQLiteOpenHelper(getContext());
		mIconSQHelper = new IconSQLiteOpenHelper(getContext());
		ThemeLog.i(TAG, "qwe provider create");
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		sortOrder = ThemeSQLiteHelper.COLUMN_ID + " DESC";
		
//		String[] availableThemes = ThemeUtils.getAvailableThemes(Constant.DEFAULT_THEME_PATH);
//		ThemeUtils.removeNonExistingThemes(getContext(), availableThemes);
		
		// TODO Auto-generated method stub
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		String table = uri.getPathSegments().get(0);
		ThemeLog.v(TAG, "qwe query,table=" + table);
		SQLiteDatabase db;
		if(table.equals(ThemeSQLiteHelper.TABLE_THEMES)){
			db = mSQHelper.getWritableDatabase();
		}else{
			ThemeLog.v(TAG, "qwe query,mIconSQHelper");
			db = mIconSQHelper.getWritableDatabase();
		}
		ThemeLog.v(TAG, "qwe query,db="+db);
		qb.setTables(table);
//		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
		Cursor c = db.query(table, null, selection, selectionArgs, null, null, null);
		ThemeLog.v(TAG, "qwe Cursor="+c.getCount());
		if (c != null) {
			c.setNotificationUri(getContext().getContentResolver(), uri);
		}
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues value, String whereClause, String[] whereArgs) {
		ThemeLog.i(TAG, "ThemeContentProvider update");
		
		final int match = mUriMatcher.match(uri);
		SQLiteDatabase db = mSQHelper.getWritableDatabase();
		SQLiteDatabase db2 = mIconSQHelper.getWritableDatabase();
		String table = uri.getPathSegments().get(0);

		ThemeLog.i(TAG, "table: " + table);

		if (match == RESET) {
			ThemeLog.i(TAG, "ThemeContentProvider update RESET");

			value = new ContentValues();
			value.put(ThemeSQLiteHelper.COLUMN_THEME_ISUSING, "0");
			value.put(ThemeSQLiteHelper.COLUMN_ICONS_ISUSING, "0");
			value.put(ThemeSQLiteHelper.COLUMN_LOCKSCREEN_ISUSING, "0");
			value.put(ThemeSQLiteHelper.COLUMN_LOCK_WALLPAPER_ISUSING, "0");
			value.put(ThemeSQLiteHelper.COLUMN_WALLPAPER_ISUSING, "0");
			value.put(ThemeSQLiteHelper.COLUMN__FONT_ISUSING, "0");
			value.put(ThemeSQLiteHelper.COLUMN_RINGTONE_ISUSING, "0");
			db.update(table, value, whereClause, whereArgs);

			int ret = db.update(table, value, whereClause, whereArgs);
			return ret;
		}
		if (match == SET) {
			ThemeLog.i(TAG, "ThemeContentProvider update SET");

			SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
			qb.setTables(table);
			Cursor c = qb.query(db, new String[]{ThemeSQLiteHelper.COLUMN_THEME_PATH}, whereClause, null, null, null, null);
			if (c.moveToFirst()) {
				String themePath = c.getString(c.getColumnIndex(ThemeSQLiteHelper.COLUMN_THEME_PATH));
				ThemeLog.i(TAG, "themePath = " + themePath);
				copyFileToSystem(themePath);
			}
			c.close();

			value = new ContentValues();
			value.put(ThemeSQLiteHelper.COLUMN_ICONS_ISUSING, "1");
			value.put(ThemeSQLiteHelper.COLUMN_LOCKSCREEN_ISUSING, "1");
			value.put(ThemeSQLiteHelper.COLUMN_LOCK_WALLPAPER_ISUSING, "1");
			value.put(ThemeSQLiteHelper.COLUMN_WALLPAPER_ISUSING, "1");
			value.put(ThemeSQLiteHelper.COLUMN__FONT_ISUSING, "1");
			value.put(ThemeSQLiteHelper.COLUMN_RINGTONE_ISUSING, "1");
			value.put(ThemeSQLiteHelper.COLUMN_THEME_ISUSING, "1");
			db.update(table, value, whereClause, whereArgs);

			int ret = db.update(table, value, whereClause, whereArgs);
			return ret;
		}
		if (match == REVERT) {
			ThemeLog.i(TAG, "ThemeContentProvider update REVERT");

			int ret = db.update(table, value, whereClause, whereArgs);
			return ret;
		}
		if (match == WALLPAPER_RESET) {
			ThemeLog.d(TAG, "ThemeContentProvider update RESET");

			value = new ContentValues();
			value.put(ThemeSQLiteHelper.COLUMN_WALLPAPER_ISUSING, "0");
			db.update(table, value, whereClause, whereArgs);

			int ret = db.update(table, value, whereClause, whereArgs);
			return ret;
		}
		if (match == WALLPAPER_SET) {
			ThemeLog.d(TAG, "ThemeContentProvider update SET");

			SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
			qb.setTables(table);
			Cursor c = qb.query(db, new String[]{ThemeSQLiteHelper.COLUMN_THEME_PATH}, whereClause, null, null, null, null);
			if (c.moveToFirst()) {
				String themePath = c.getString(c.getColumnIndex(ThemeSQLiteHelper.COLUMN_THEME_PATH));
				ThemeLog.d(TAG, "themePath = " + themePath);
				copyFileToSystem(themePath);
			}
			c.close();

			value = new ContentValues();
			value.put(ThemeSQLiteHelper.COLUMN_WALLPAPER_ISUSING, "1");
			db.update(table, value, whereClause, whereArgs);

			int ret = db.update(table, value, whereClause, whereArgs);
			return ret;
		}
		int ret = db.update(table, value, whereClause, whereArgs);
		ThemeLog.w(TAG, "ThemeContentProvider update: ret=" + ret);
		return ret;
	}


	protected String copyFileToSystem(String theme) {

		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT && !theme.startsWith("/system")) {
			File file = new File(theme);

			ThemeLog.i(TAG, "theme: " + theme);

			File newFile =  getContext().getDir("themes", 0);
			File[] listFiles = newFile.listFiles();
			for (int i = 0; i < listFiles.length; i++) {
				if (!listFiles[i].getName().equals(theme.substring(theme.lastIndexOf(File.separator)+1))) {
					listFiles[i].delete();
				}
			}

			newFile = new File(newFile.getPath(), theme.substring(theme.lastIndexOf(File.separator)+1));
			ThemeLog.i(TAG, "newFile = " + newFile.getPath());

			if (!newFile.exists()) {
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

	protected static void copyFile(File sourceFile, File targetFile) throws IOException {
		
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
	
	
	private static void initDataBase(final Context context) throws UnsupportedEncodingException {

		ThemeUtils.addThemesToDb(context, true);
		ThemeLog.i(TAG, "initDataBase succe");
	}
	private static void initIconDataBase(final Context context){

		ThemeUtils.addIconsToDb(context);
	}
	
	public static class IconSQLiteOpenHelper extends SQLiteOpenHelper {
		public static final String DATABASE_NAME = "icons.db";
		public static final int DATABASE_VERSION = 1;
		public static final String AUTHORITY = "com.fineos.thememgr.Settings";
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/icons");
		public static final String TABLE_ICONS = "icons";
		public static final String COLUMN_ICON_FILE_NAME = "icon_file_name";
		public static final String COLUMN_ICON_PACKAGE_NAME = "package_name";
		public static final String COLUMN_ICON_CLASS_NAME = "class_name";
		public static final String COLUMN_ICON_FILE_PATH = "icon_path";
		public static final String COLUMN_ICON_NAME = "icons_name";
		// Database creation SQL statement
		private static final String DATABASE_CREATE = "create table " + TABLE_ICONS + "(" + ThemeSQLiteHelper.COLUMN_ID + " integer primary key autoincrement, "
				+ COLUMN_ICON_FILE_NAME + " text not null, " 
				+ COLUMN_ICON_PACKAGE_NAME + " text, "
				+ COLUMN_ICON_FILE_PATH+ " text, "
				+ COLUMN_ICON_NAME + " text, " 
				+ COLUMN_ICON_CLASS_NAME+ " text);";

//		private static final String DATABASE_CREATE = "create table " + ThemeSQLiteHelper.TABLE_THEMES + "(" + ThemeSQLiteHelper.COLUMN_ID + " integer primary key autoincrement, "
//				+ ThemeSQLiteHelper.COLUMN_THEME_FILE_NAME + " text not null);";

		private Context mContext;
		private SQLiteDatabase mDefaultWritableDatabase = null;

		public IconSQLiteOpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			ThemeLog.v(TAG, "qwe IconSQLiteOpenHelper IconSQLiteOpenHelper");
			mContext = context;
		}

		@Override
		public void onCreate(SQLiteDatabase database) {
			ThemeLog.v(TAG, "qwe IconSQLiteOpenHelper onCreate");
			mDefaultWritableDatabase = database;
			try {
				database.execSQL(DATABASE_CREATE);
				initDataBase(mContext);
			} catch (Exception e) {
				ThemeLog.e(TAG, "onCreate(SQLiteDatabase database) Exception: " + e);
				e.printStackTrace();
			}
		}
		private static void initDataBase(final Context context) throws UnsupportedEncodingException {

			ThemeUtils.addIconsToDb(context);
		}
		@Override
		public SQLiteDatabase getWritableDatabase() {
			final SQLiteDatabase db;
			if (mDefaultWritableDatabase != null) {
				db = mDefaultWritableDatabase;
			} else {
				db = super.getWritableDatabase();
			}
			return db;
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			int version = oldVersion;
			// if (version != DATABASE_VERSION) {
			mDefaultWritableDatabase = db;
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_ICONS);
			onCreate(db);

			// }
		}
	}
	
	
	private static class ThemeSQLiteOpenHelper extends SQLiteOpenHelper {
		// Database creation SQL statement
		private static final String DATABASE_CREATE = "create table " + ThemeSQLiteHelper.TABLE_THEMES + "(" + ThemeSQLiteHelper.COLUMN_ID + " integer primary key autoincrement, "
				+ ThemeSQLiteHelper.COLUMN_THEME_FILE_NAME + " text not null, " + ThemeSQLiteHelper.COLUMN_THEME_LASTUPDATE + " text, " + ThemeSQLiteHelper.COLUMN_THEME_TITLE + " text, "
				+ ThemeSQLiteHelper.COLUMN_THEME_AUTHOR + " text, " + ThemeSQLiteHelper.COLUMN_THEME_DESIGNER + " text, " + ThemeSQLiteHelper.COLUMN_THEME_PRICE + " float, "+ ThemeSQLiteHelper.COLUMN_THEME_VERSION + " text, "
				+ ThemeSQLiteHelper.COLUMN_THEME_UI_VERSION + " text, " + ThemeSQLiteHelper.COLUMN_THEME_PATH + " text not null, " + ThemeSQLiteHelper.COLUMN_RINGTONE_PATH + " text, "
				+ ThemeSQLiteHelper.COLUMN_WALLPAPER_PATH + " text, " + ThemeSQLiteHelper.COLUMN_LOCKSCREEN_WALLPAPER_PATH + " text, " + ThemeSQLiteHelper.COLUMN_PREVIEWS_LIST + " text, "
				+ ThemeSQLiteHelper.COLUMN_THEME_ICON + " BLOB, " + ThemeSQLiteHelper.COLUMN_THEME_DISCRITION + " text," + ThemeSQLiteHelper.COLUMN_IS_VERIFY + " integer, "
				+ ThemeSQLiteHelper.COLUMN_IS_DEFAULT_THEME + " integer, " + ThemeSQLiteHelper.COLUMN_HAS_WALLPAPER + " integer, "
				+ ThemeSQLiteHelper.COLUMN_WALLPAPER_ISUSING
				+ " integer, " // wallpaper
				+ ThemeSQLiteHelper.COLUMN_HAS_LOCK_WALLPAPER + " integer, "
				+ ThemeSQLiteHelper.COLUMN_LOCK_WALLPAPER_ISUSING
				+ " integer, " // lock wallpaper
				+ ThemeSQLiteHelper.COLUMN_HAS_ICONS + " integer, "
				+ ThemeSQLiteHelper.COLUMN_ICONS_ISUSING
				+ " integer, " // icons
				+ ThemeSQLiteHelper.COLUMN_HAS_LOCKSCREEN + " integer, "
				+ ThemeSQLiteHelper.COLUMN_LOCKSCREEN_ISUSING
				+ " integer, " // lockscreen
				+ ThemeSQLiteHelper.COLUMN_HAS_CONTACTS + " integer, " + ThemeSQLiteHelper.COLUMN_HAS_DIALER + " integer, " + ThemeSQLiteHelper.COLUMN_HAS_SYSTEMUI + " integer, "
				+ ThemeSQLiteHelper.COLUMN_HAS_FRAMEWORK + " integer, " + ThemeSQLiteHelper.COLUMN_HAS_RINGTONE + " integer, " + ThemeSQLiteHelper.COLUMN_RINGTONE_ISUSING
				+ " integer, " // ringtone
				+ ThemeSQLiteHelper.COLUMN_HAS_NOTIFICATION + " integer, " + ThemeSQLiteHelper.COLUMN_HAS_BOOTANIMATION + " integer, " + ThemeSQLiteHelper.COLUMN_HAS_MMS + " integer, "
				+ ThemeSQLiteHelper.COLUMN_HAS_FONT + " integer, " + ThemeSQLiteHelper.COLUMN__FONT_ISUSING + " integer, " // font
				+ ThemeSQLiteHelper.COLUMN_THEME_ISUSING + " integer, " // isusing
				+ ThemeSQLiteHelper.COLUMN_IS_COMPLETE + " integer);";

//		private static final String DATABASE_CREATE = "create table " + ThemeSQLiteHelper.TABLE_THEMES + "(" + ThemeSQLiteHelper.COLUMN_ID + " integer primary key autoincrement, "
//				+ ThemeSQLiteHelper.COLUMN_THEME_FILE_NAME + " text not null);";

		private Context mContext;
		private SQLiteDatabase mDefaultWritableDatabase = null;

		public ThemeSQLiteOpenHelper(Context context) {
			super(context, ThemeSQLiteHelper.DATABASE_NAME, null, ThemeSQLiteHelper.DATABASE_VERSION);
			mContext = context;
		}

		@Override
		public void onCreate(SQLiteDatabase database) {
			ThemeLog.i(TAG, "qwe SQLiteDatabase create");
			mDefaultWritableDatabase = database;
			try {
				database.execSQL(DATABASE_CREATE);
				initDataBase(mContext);
			} catch (Exception e) {
				ThemeLog.e(TAG, "onCreate(SQLiteDatabase database) Exception: " + e);
				e.printStackTrace();
			}
		}

		@Override
		public SQLiteDatabase getWritableDatabase() {
			final SQLiteDatabase db;
			if (mDefaultWritableDatabase != null) {
				db = mDefaultWritableDatabase;
			} else {
				db = super.getWritableDatabase();
			}
			return db;
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			int version = oldVersion;
			// if (version != DATABASE_VERSION) {
			mDefaultWritableDatabase = db;
			ThemeLog.i(TAG, "onUpgrade");
			db.execSQL("DROP TABLE IF EXISTS " + ThemeSQLiteHelper.TABLE_THEMES);
			onCreate(db);

			// }
		}
	}

}
