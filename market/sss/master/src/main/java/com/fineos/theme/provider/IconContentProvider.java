package com.fineos.theme.provider;

import java.io.UnsupportedEncodingException;

import com.fineos.theme.utils.ThemeLog;
import com.fineos.theme.utils.ThemeUtils;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;


public class IconContentProvider extends ContentProvider {
	public static final String DATABASE_NAME = "icons.db";
	public static final int DATABASE_VERSION = 1;
	public static final String AUTHORITY = "com.fineos.thememgr.Settings";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/icons");
	public static final String TABLE_ICONS = "icons";
	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		ThemeLog.v("IconContentProvider", "qwe  IconContentProvider onCreate");
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		return null;

	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}
	
}