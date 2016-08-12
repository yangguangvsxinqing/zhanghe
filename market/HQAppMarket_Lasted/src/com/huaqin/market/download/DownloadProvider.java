package com.huaqin.market.download;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.CrossProcessCursor;
import android.database.Cursor;
import android.database.CursorWindow;
import android.database.CursorWrapper;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Binder;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.provider.BaseColumns;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;

/**
 * Allows application to interact with the download manager.
 */
public final class DownloadProvider extends ContentProvider {
	
	private static final String TAG = "DownloadProvider";

    /** Database filename */
    private static final String DB_NAME = "downloads.db";
    /** Current database version */
    //数据库版本号升级
    private static final int DB_VERSION = 106;
    /** Database version from which upgrading is a nop */
    private static final int DB_VERSION_NOP_UPGRADE_FROM = 31;
    /** Database version to which upgrading is a nop */
    private static final int DB_VERSION_NOP_UPGRADE_TO = 100;
    /** Name of table in the database */
    private static final String DB_TABLE = "downloads";

    /** MIME type for the entire download list */
    private static final String DOWNLOAD_LIST_TYPE = "vnd.android.cursor.dir/download";
    /** MIME type for an individual download */
    private static final String DOWNLOAD_TYPE = "vnd.android.cursor.item/download";

    /** URI matcher used to recognize URIs sent by applications */
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    /** URI matcher constant for the URI of the entire download list */
    private static final int DOWNLOADS = 1;
    /** URI matcher constant for the URI of an individual download */
    private static final int DOWNLOADS_ID = 2;
    static {
        sURIMatcher.addURI("hqdownloads", "download", DOWNLOADS);
        sURIMatcher.addURI("hqdownloads", "download/#", DOWNLOADS_ID);
    }

    private static final String[] sAppReadableColumnsArray = new String[] {
        Downloads._ID,
        Downloads.COLUMN_APP_DATA,
        Downloads._DATA,
        Downloads.COLUMN_MIME_TYPE,
        Downloads.COLUMN_VISIBILITY,
        Downloads.COLUMN_DESTINATION,
        Downloads.COLUMN_CONTROL,
        Downloads.COLUMN_STATUS,
        Downloads.COLUMN_LAST_MODIFICATION,
        Downloads.COLUMN_NOTIFICATION_PACKAGE,
        Downloads.COLUMN_NOTIFICATION_CLASS,
        Downloads.COLUMN_TOTAL_BYTES,
        Downloads.COLUMN_CURRENT_BYTES,
        Downloads.COLUMN_TITLE,
        Downloads.COLUMN_DESCRIPTION,
        Downloads.COLUMN_PACKAGE_NAME,
        Downloads.COLUMN_APP_ID,
		/*************Added-s by JimmyJin for Pudding Project**************/
        Downloads.COLUMN_APP_TYPE,
        Downloads.COLUMN_FROM_WHERE
		/*************Added-e by JimmyJin for Pudding Project**************/
    };

    private static HashSet<String> sAppReadableColumnsSet;
    static {
        sAppReadableColumnsSet = new HashSet<String>();
        for (int i = 0; i < sAppReadableColumnsArray.length; ++i) {
            sAppReadableColumnsSet.add(sAppReadableColumnsArray[i]);
        }
    }

    /** The database that lies underneath this content provider */
    private SQLiteOpenHelper mOpenHelper = null;

    /** List of uids that can access the downloads */
    private int mSystemUid = -1;
    private int mDefContainerUid = -1;

    /**
     * Creates and updated database on demand when opening it.
     * Helper class to create database the first time the provider is
     * initialized and upgrade it when a new version of the provider needs
     * an updated version of the database.
     */
    private final class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(final Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        /**
         * Creates database the first time we try to open it.
         */
        @Override
        public void onCreate(final SQLiteDatabase db) {
            createTable(db);
        }

        /**
         * Updates the database format when a content provider is used
         * with a database that was created with a different format.
         */
        @Override
        public void onUpgrade(final SQLiteDatabase db, int oldV, final int newV) {
            if (oldV == DB_VERSION_NOP_UPGRADE_FROM) {
                if (newV == DB_VERSION_NOP_UPGRADE_TO) { // that's a no-op upgrade.
                    return;
                }
                // NOP_FROM and NOP_TO are identical, just in different codelines. Upgrading
                //     from NOP_FROM is the same as upgrading from NOP_TO.
                oldV = DB_VERSION_NOP_UPGRADE_TO;
            }
            Log.i(Constants.TAG, "Upgrading downloads database from version " + oldV + " to " + newV
                    + ", which will destroy all old data");
            dropTable(db);
            createTable(db);
        }
    }

    /**
     * Initializes the content provider when it is created.
     */
    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        // Initialize the system uid
        mSystemUid = Process.SYSTEM_UID;
        // Initialize the default container uid. Package name hardcoded
        // for now.
        ApplicationInfo appInfo = null;
        try {
            appInfo = getContext().getPackageManager().
                    getApplicationInfo("com.android.defcontainer", 0);
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (appInfo != null) {
            mDefContainerUid = appInfo.uid;
        }
        return true;
    }

    /**
     * Returns the content-provider-style MIME types of the various
     * types accessible through this content provider.
     */
    @Override
    public String getType(final Uri uri) {
        int match = sURIMatcher.match(uri);
        switch (match) {
            case DOWNLOADS: {
                return DOWNLOAD_LIST_TYPE;
            }
            case DOWNLOADS_ID: {
                return DOWNLOAD_TYPE;
            }
            default: {
                throw new IllegalArgumentException("Unknown URI: " + uri);
            }
        }
    }

    /**
     * Creates the table that'll hold the download information.
     */
    private void createTable(SQLiteDatabase db) {
        try {
            db.execSQL("CREATE TABLE " + DB_TABLE + "(" +
                    BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Downloads.COLUMN_URI + " TEXT, " +
                    Constants.RETRY_AFTER_X_REDIRECT_COUNT + " INTEGER, " +
                    Downloads.COLUMN_APP_DATA + " TEXT, " +
                    Downloads.COLUMN_NO_INTEGRITY + " BOOLEAN, " +
                    Downloads.COLUMN_FILE_NAME_HINT + " TEXT, " +
                    Downloads._DATA + " TEXT, " +
                    Downloads.COLUMN_MIME_TYPE + " TEXT, " +
                    Downloads.COLUMN_DESTINATION + " INTEGER, " +
                    Constants.NO_SYSTEM_FILES + " BOOLEAN, " +
                    Downloads.COLUMN_VISIBILITY + " INTEGER, " +
                    Downloads.COLUMN_CONTROL + " INTEGER, " +
                    Downloads.COLUMN_STATUS + " INTEGER, " +
                    Constants.FAILED_CONNECTIONS + " INTEGER, " +
                    Downloads.COLUMN_LAST_MODIFICATION + " BIGINT, " +
                    Downloads.COLUMN_NOTIFICATION_PACKAGE + " TEXT, " +
                    Downloads.COLUMN_NOTIFICATION_CLASS + " TEXT, " +
                    Downloads.COLUMN_NOTIFICATION_EXTRAS + " TEXT, " +
                    Downloads.COLUMN_COOKIE_DATA + " TEXT, " +
                    Downloads.COLUMN_USER_AGENT + " TEXT, " +
                    Downloads.COLUMN_REFERER + " TEXT, " +
                    Downloads.COLUMN_TOTAL_BYTES + " INTEGER, " +
                    Downloads.COLUMN_CURRENT_BYTES + " INTEGER, " +
                    Constants.ETAG + " TEXT, " +
                    Constants.LAST_MOFIFY_AT_SERVER + " TEXT, " +                    
                    Constants.UID + " INTEGER, " +
                    Downloads.COLUMN_OTHER_UID + " INTEGER, " +
                    Downloads.COLUMN_TITLE + " TEXT, " +
                    Downloads.COLUMN_DESCRIPTION + " TEXT, " +
                    Downloads.COLUMN_PACKAGE_NAME + " TEXT, " +
    	           	/*************Added-s by JimmyJin for Pudding Project**************/
    	            Downloads.COLUMN_APP_TYPE  + " TEXT, " +
    	            Downloads.COLUMN_FROM_WHERE + " TEXT, " +          
    	           	/*************Added-s by JimmyJin for Pudding Project**************/

                    Downloads.COLUMN_APP_ID + " INTEGER);");
        } catch (SQLException ex) {
            Log.e(Constants.TAG, "couldn't create table in downloads database");
            throw ex;
        }
    }

    /**
     * Deletes the table that holds the download information.
     */
    private void dropTable(SQLiteDatabase db) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE);
        } catch (SQLException ex) {
            Log.e(Constants.TAG, "couldn't drop table in downloads database");
            throw ex;
        }
    }

    /**
     * Inserts a row in the database
     */
    @Override
    public Uri insert(final Uri uri, final ContentValues values) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        if (sURIMatcher.match(uri) != DOWNLOADS) {
            throw new IllegalArgumentException("Unknown/Invalid URI " + uri);
        }

        ContentValues filteredValues = new ContentValues();

        copyString(Downloads.COLUMN_URI, values, filteredValues);
        copyString(Downloads.COLUMN_APP_DATA, values, filteredValues);
        copyBoolean(Downloads.COLUMN_NO_INTEGRITY, values, filteredValues);
        copyString(Downloads.COLUMN_FILE_NAME_HINT, values, filteredValues);
        copyString(Downloads.COLUMN_MIME_TYPE, values, filteredValues);
        copyInteger(Downloads.COLUMN_DESTINATION, values, filteredValues);
        copyInteger(Downloads.COLUMN_VISIBILITY, values, filteredValues);
        copyInteger(Downloads.COLUMN_CONTROL, values, filteredValues);
        copyInteger(Downloads.COLUMN_TOTAL_BYTES, values, filteredValues);
        
        filteredValues.put(Downloads.COLUMN_STATUS, Downloads.STATUS_PENDING);
        filteredValues.put(Downloads.COLUMN_LAST_MODIFICATION, System.currentTimeMillis());
        String pckg = values.getAsString(Downloads.COLUMN_NOTIFICATION_PACKAGE);
        String clazz = values.getAsString(Downloads.COLUMN_NOTIFICATION_CLASS);
        if (pckg != null && clazz != null) {
            int uid = Binder.getCallingUid();
            try {
                if (uid == 0 ||
                        getContext().getPackageManager().getApplicationInfo(pckg, 0).uid == uid) {
                    filteredValues.put(Downloads.COLUMN_NOTIFICATION_PACKAGE, pckg);
                    filteredValues.put(Downloads.COLUMN_NOTIFICATION_CLASS, clazz);
                }
            } catch (PackageManager.NameNotFoundException ex) {
                /* ignored for now */
            }
        }
        copyString(Downloads.COLUMN_NOTIFICATION_EXTRAS, values, filteredValues);
        copyString(Downloads.COLUMN_COOKIE_DATA, values, filteredValues);
        copyString(Downloads.COLUMN_USER_AGENT, values, filteredValues);
        copyString(Downloads.COLUMN_REFERER, values, filteredValues);
        copyInteger(Downloads.COLUMN_OTHER_UID, values, filteredValues);
        filteredValues.put(Constants.UID, Binder.getCallingUid());
        if (Binder.getCallingUid() == 0) {
            copyInteger(Constants.UID, values, filteredValues);
        }
        copyString(Downloads._DATA, values, filteredValues);
        copyString(Downloads.COLUMN_TITLE, values, filteredValues);
        copyString(Downloads.COLUMN_DESCRIPTION, values, filteredValues);
        copyInteger(Downloads.COLUMN_APP_ID, values, filteredValues);
        copyString(Downloads.COLUMN_PACKAGE_NAME, values, filteredValues);
		/*************Added-s by JimmyJin for Pudding Project**************/
        copyString(Downloads.COLUMN_APP_TYPE, values, filteredValues);
        copyString(Downloads.COLUMN_FROM_WHERE, values, filteredValues);     
		/*************Added-e by JimmyJin for Pudding Project**************/
        Context context = getContext();
        
        long rowID = db.insert(DB_TABLE, null, filteredValues);

        Uri ret = null;

        if (rowID != -1) {
            context.startService(new Intent(context, DownloadService.class));
            ret = Uri.parse(Downloads.CONTENT_URI + "/" + rowID);
            context.getContentResolver().notifyChange(uri, null);
        }
        
        return ret;
    }

    /**
     * Starts a database query
     */
    @Override
    public Cursor query(final Uri uri, String[] projection,
             final String selection, final String[] selectionArgs,
             final String sort) {

        //Helpers.validateSelection(selection, sAppReadableColumnsSet);

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        int match = sURIMatcher.match(uri);
        switch (match) {
            case DOWNLOADS: {
                qb.setTables(DB_TABLE);
                break;
            }
            case DOWNLOADS_ID: {
                qb.setTables(DB_TABLE);
                qb.appendWhere(BaseColumns._ID + "=");
                qb.appendWhere(uri.getPathSegments().get(1));
                break;
            }
            default: {
                throw new IllegalArgumentException("Unknown URI: " + uri);
            }
        }
        
        Cursor ret = qb.query(db, projection, selection, selectionArgs,
                              null, null, sort);

        if (ret != null) {
           ret = new ReadOnlyCursorWrapper(ret);
        }
        
        if (ret != null) {
            ret.setNotificationUri(getContext().getContentResolver(), uri);
        }
        
        return ret;
    }

    /**
     * Updates a row in the database
     */
    @Override
    public int update(final Uri uri, final ContentValues values,
            final String where, final String[] whereArgs) {

        //Helpers.validateSelection(where, sAppReadableColumnsSet);
    	Log.i(TAG, "update========");

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        int count;
        long rowId = 0;
        boolean startService = false;

        ContentValues filteredValues;
        if (Binder.getCallingPid() != Process.myPid()) {
            filteredValues = new ContentValues();
            copyString(Downloads.COLUMN_APP_DATA, values, filteredValues);
            copyInteger(Downloads.COLUMN_VISIBILITY, values, filteredValues);
            Integer i = values.getAsInteger(Downloads.COLUMN_CONTROL);
            if (i != null) {
                filteredValues.put(Downloads.COLUMN_CONTROL, i);
                startService = true;
            }
            copyInteger(Downloads.COLUMN_CONTROL, values, filteredValues);
            copyString(Downloads.COLUMN_TITLE, values, filteredValues);
            copyString(Downloads.COLUMN_DESCRIPTION, values, filteredValues);
        } else {
            filteredValues = values;
            String filename = values.getAsString(Downloads._DATA);
            if (filename != null) {
                Cursor c = query(uri, new String[]
                        { Downloads.COLUMN_TITLE }, null, null, null);
                if (!c.moveToFirst() || c.getString(0) == null) {
                    values.put(Downloads.COLUMN_TITLE,
                            new File(filename).getName());
                }
                c.close();
            }
            if(filteredValues.containsKey(Downloads.COLUMN_CONTROL)) {
                int control = (Integer) filteredValues.get(Downloads.COLUMN_CONTROL);
                Log.d(TAG, "control =" + control);
                if(control == Downloads.CONTROL_RUN) {
                    startService = true;
                }
            }
        }
        int match = sURIMatcher.match(uri);
        switch (match) {
            case DOWNLOADS:
            case DOWNLOADS_ID: {
                String myWhere;
                if (where != null) {
                    if (match == DOWNLOADS) {
                        myWhere = "( " + where + " )";
                    } else {
                        myWhere = "( " + where + " ) AND ";
                    }
                } else {
                    myWhere = "";
                }
                if (match == DOWNLOADS_ID) {
                    String segment = uri.getPathSegments().get(1);
                    rowId = Long.parseLong(segment);
                    myWhere += " ( " + BaseColumns._ID + " = " + rowId + " ) ";
                }
                int callingUid = Binder.getCallingUid();
                if (Binder.getCallingPid() != Process.myPid() &&
                        callingUid != mSystemUid &&
                        callingUid != mDefContainerUid) {
                    myWhere += " AND ( " + Constants.UID + "=" +  Binder.getCallingUid() + " OR "
                            + Downloads.COLUMN_OTHER_UID + "=" +  Binder.getCallingUid() + " )";
                }
                if (filteredValues.size() > 0) {
//                	Log.i(TAG, "filteredValues=" + filteredValues + ",myWhere" + myWhere + ",whereArgs=" + whereArgs);
                    count = db.update(DB_TABLE, filteredValues, myWhere, whereArgs);
                } else {
                    count = 0;
                }
                break;
            }
            default: {
                throw new UnsupportedOperationException("Cannot update URI: " + uri);
            }
        }
        getContext().getContentResolver().notifyChange(uri, null);
        if (startService) {
            Context context = getContext();
            context.startService(new Intent(context, DownloadService.class));
        }
        return count;
    }

    /**
     * Deletes a row in the database
     */
    @Override
    public int delete(final Uri uri, final String where,
            final String[] whereArgs) {

        //Helpers.validateSelection(where, sAppReadableColumnsSet);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        int match = sURIMatcher.match(uri);
        switch (match) {
            case DOWNLOADS:
            case DOWNLOADS_ID: {
                String myWhere;
                if (where != null) {
                    if (match == DOWNLOADS) {
                        myWhere = "( " + where + " )";
                    } else {
                        myWhere = "( " + where + " ) AND ";
                    }
                } else {
                    myWhere = "";
                }
                if (match == DOWNLOADS_ID) {
                    String segment = uri.getPathSegments().get(1);
                    long rowId = Long.parseLong(segment);
                    myWhere += " ( " + BaseColumns._ID + " = " + rowId + " ) ";
                }
                int callingUid = Binder.getCallingUid();
                if (Binder.getCallingPid() != Process.myPid() &&
                        callingUid != mSystemUid &&
                        callingUid != mDefContainerUid) {
                    myWhere += " AND ( " + Constants.UID + "=" +  Binder.getCallingUid() + " OR "
                            + Downloads.COLUMN_OTHER_UID + "="
                            +  Binder.getCallingUid() + " )";
                }
                count = db.delete(DB_TABLE, myWhere, whereArgs);
                break;
            }
            default: {
                throw new UnsupportedOperationException("Cannot delete URI: " + uri);
            }
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    /**
     * Remotely opens a file
     */
    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode)
            throws FileNotFoundException {
        // This logic is mostly copied form openFileHelper. If openFileHelper eventually
        //     gets split into small bits (to extract the filename and the modebits),
        //     this code could use the separate bits and be deeply simplified.
        Cursor c = query(uri, new String[]{"_data"}, null, null, null);
        int count = (c != null) ? c.getCount() : 0;
        if (count != 1) {
            // If there is not exactly one result, throw an appropriate exception.
            if (c != null) {
                c.close();
            }
            if (count == 0) {
                throw new FileNotFoundException("No entry for " + uri);
            }
            throw new FileNotFoundException("Multiple items at " + uri);
        }

        c.moveToFirst();
        String path = c.getString(0);
        c.close();
        if (path == null) {
            throw new FileNotFoundException("No filename found.");
        }

        if (!"r".equals(mode)) {
            throw new FileNotFoundException("Bad mode for " + uri + ": " + mode);
        }
        ParcelFileDescriptor ret = ParcelFileDescriptor.open(new File(path),
                ParcelFileDescriptor.MODE_READ_ONLY);

        if (ret == null) {
            throw new FileNotFoundException("couldn't open file");
        } else {
            ContentValues values = new ContentValues();
            values.put(Downloads.COLUMN_LAST_MODIFICATION, System.currentTimeMillis());
            update(uri, values, null, null);
        }
        return ret;
    }

    private static final void copyInteger(String key, ContentValues from, ContentValues to) {
        Integer i = from.getAsInteger(key);
        if (i != null) {
            to.put(key, i);
        }
    }

    private static final void copyBoolean(String key, ContentValues from, ContentValues to) {
        Boolean b = from.getAsBoolean(key);
        if (b != null) {
            to.put(key, b);
        }
    }

    private static final void copyString(String key, ContentValues from, ContentValues to) {
    	Log.v("", "ssss key="+key);
        String s = from.getAsString(key);
        Log.v("", "ssss s="+s);
        if (s != null) {
            to.put(key, s);
        }
    }

    private class ReadOnlyCursorWrapper extends CursorWrapper implements CrossProcessCursor {
        public ReadOnlyCursorWrapper(Cursor cursor) {
            super(cursor);
            mCursor = (CrossProcessCursor) cursor;
        }

        @Override
		public void fillWindow(int pos, CursorWindow window) {
            mCursor.fillWindow(pos, window);
        }

        @Override
		public CursorWindow getWindow() {
            return mCursor.getWindow();
        }

        @Override
		public boolean onMove(int oldPosition, int newPosition) {
            return mCursor.onMove(oldPosition, newPosition);
        }

        private CrossProcessCursor mCursor;
    }

}
