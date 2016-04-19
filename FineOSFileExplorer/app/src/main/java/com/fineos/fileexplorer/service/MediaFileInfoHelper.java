package com.fineos.fileexplorer.service;

import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.MediaStore.Files.FileColumns;
import android.provider.Settings;
import android.util.Log;

import com.fineos.fileexplorer.entity.FileInfo;
import com.fineos.fileexplorer.entity.FileInfo.FileCategory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * @author Xiao Yue This class find media file information by accessing the
 *         system database(using MediaStore). It is a singleton for properly
 *         accessing database.
 * */
public class MediaFileInfoHelper implements IMediaFileInfoHelper,
		IFileSearchHelper {

	private static final String TAG = "MediaFileInfoHelper";

	private static MediaFileInfoHelper instance = null;
	// "external" means storages except internal (may include more than one
	// sdcard).
	private static final String VOLUME_NAME = "external";
	private HashSet<String> DOC_MIME_TYPES_SET = new HashSet<String>() {
		{
			add("text/plain");
			add("text/plain");
			add("application/pdf");
			add("application/msword");
			add("application/vnd.ms-excel");
			add("application/vnd.ms-excel");
		}
	};
	private String ZIP_MIME_TYPE = "application/zip";
	private Context context;
	private String path;
	
	//Four params that all search needs:
	private Uri uri;
	private List<String> projection = new ArrayList<String>();
	private String selection = "";
	private String suffix = "";
	
	private int resultCount;

	public static MediaFileInfoHelper getInstance(Context context) {
		if (instance == null) {
			instance = new MediaFileInfoHelper(context);
		}
		return instance;
	}

	private MediaFileInfoHelper(Context context) {
		/**
		 * use Application Context as the context to avoid accidentally memory
		 * leak, for more information:
		 * "http://android-developers.blogspot.nl/2009/01/avoiding-memory-leaks.html"
		 */
		this.context = context.getApplicationContext();
	}

	@Override
	public MediaCategoryInfo getMediaFileInfo(FileCategory fileCategory) {
		buildSearchParams(fileCategory);
		MediaCategoryInfo mediaCategoryInfo = queryCategoryInfo(fileCategory);
		clearParams();
		return mediaCategoryInfo;
	}

	private void clearParams() {
		uri = null;
		projection.clear();
		selection = "";		
	}

	private MediaCategoryInfo queryCategoryInfo(FileCategory fileCategory) {
		Log.d(TAG, "uri: " + uri + "selection : " + selection);
        Log.d("acmllaugh1", "queryCategoryInfo (line 89): uri : " + uri + " selection : " + selection);
		MediaCategoryInfo mediaCategoryInfo = new MediaCategoryInfo();
		mediaCategoryInfo.setMediaCategory(fileCategory);
		String[] projection = new String[2];
		this.projection.toArray(projection);
        if (selection.length() < 1) {
            selection = "_size > 0";
        }
        Cursor c = context.getContentResolver().query(uri, projection, selection, null, null);
		if (c == null) {
			Log.e(TAG, "fail to query uri:" + uri);
			return mediaCategoryInfo;
		}
		if (c.moveToNext()) {
			Log.v(TAG, "Retrieved " + fileCategory.name() + " info >>> count:"
					+ c.getLong(0) + " size:" + c.getLong(1));
			mediaCategoryInfo.setMediaCategory(fileCategory);
			mediaCategoryInfo.setMediaCount(c.getLong(0));// 0 refers to the count.
			mediaCategoryInfo.setMediaSize(c.getLong(1));// 1 refers to size.
			c.close();
			return mediaCategoryInfo;
		}
		return mediaCategoryInfo;

//        Uri queryUri = uri;
//        String[] projection = new String[]{FileColumns.DATA};
//        Cursor c = context.getContentResolver().query(queryUri, projection, selection, null, null);
//        if (c == null) {
//            Log.e(TAG, "fail to query uri:" + uri);
//			return null;
//        }
//        // Cursor is not null, read category files data.
//        MediaCategoryInfo mediaCategoryInfo = new MediaCategoryInfo();
//        mediaCategoryInfo.setMediaCategory(fileCategory);
//        long fileSize = 0;
//        long fileCount = 0;
//        while (c.moveToNext()) {
//            String filePath = c.getString(c.getColumnIndex(FileColumns.DATA));
//            if (filePath == null) {
//                //TODO : tell system to delete this line.
//                continue;
//            }
//            File file = new File(filePath);
//            if (file.exists()) {
//                if (!file.isHidden()) {
//                    fileSize += file.length();
//                    fileCount++;
//                }
//            }else{
//                try {
//                    // We need to tell system database this file does not exist any more.
//                    Uri uri = MediaStore.Files.getContentUri("external");
//                    String[] filePathArg = {file.getAbsolutePath()};
//                    context.getContentResolver().delete(uri, FileColumns.DATA + "=?", filePathArg);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        if (fileCount > 0 /*&& fileSize > 0*/) {
//            mediaCategoryInfo.setMediaCount(fileCount);
//            mediaCategoryInfo.setMediaSize(fileSize);
//        }
//        try {
//            c.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return mediaCategoryInfo;
	}

	private void buildSearchParams(FileCategory fileCategory) {
		uri = FileCategory.getContentUriByCategory(fileCategory);
		projection.add("COUNT(*)");
		projection.add("SUM(_size)");
		selection = buildSelection(fileCategory);	
	}

	
	@Override
	public List<String> getMediaFileInfoListByCategory(FileCategory fileCategory) {
		this.path = "";
		Uri uri = FileCategory.getContentUriByCategory(fileCategory);
		String selection = buildSelection(fileCategory);
		// String sortOrder = buildSortOrder(sort);
		ArrayList<String> resultFileList = new ArrayList<String>();

		if (uri == null) {
			Log.e(TAG, "invalid uri, category:" + fileCategory.name());
			return null;
		}
		String[] columns = new String[] { FileColumns.DATA };
		Log.d(TAG, "selection is :" + selection);
		// uri = MediaStore.Files.getContentUri("external");
		Cursor c = context.getContentResolver().query(uri, columns, selection,
				null, MediaStore.Files.FileColumns.DATE_MODIFIED + " desc");

		if (c == null) {
			Log.e(TAG, "fail to query uri:" + uri);
			return null;
		}
		int i = 0;
		while (c.moveToNext()) {
			i++;
			String mediaFilePath = c.getString(c
					.getColumnIndex(MediaStore.Files.FileColumns.DATA));
			resultFileList.add(mediaFilePath);
		}
		Log.d(TAG, "The final i is : " + i);
		this.resultCount = i;
		return resultFileList;
	}

	public List<String> fuzzyQuery(String queryString, int startPoint,
			int searchStep) {
		ArrayList<String> resultList = new ArrayList<String>();
		String selection = "";
		Uri uri = MediaStore.Files.getContentUri("external");
		Uri query;
		int selectColumnNum = searchStep + startPoint;
		//this.resultCount = countSearchResult(queryString);
		this.resultCount = 10000;
		if(resultCount > 1000){
			query = uri
					.buildUpon()
					.appendQueryParameter("limit",
							Integer.toString(selectColumnNum)).build();
		}else{
			query = uri;
		}		
		if (!(queryString == null || queryString.equals(""))) {
			selection = MediaStore.Files.FileColumns.DATA + " like '%"
					+ queryString + "%'";
		}
		Log.d(TAG, selection);
		// String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + " = " +
		// MediaStore.Files.FileColumns.MEDIA_TYPE_NONE;
		Cursor cursor = context.getContentResolver().query(query, null,
				selection, null,
				MediaStore.Files.FileColumns.DATE_MODIFIED + " desc");
		int count = 0;
		while (cursor.moveToNext()) {
			if (count < startPoint) {
				count++;
				continue;
			}
			
			// Log.d(TAG,
			// cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)));
			String resultPath = cursor.getString(cursor
					.getColumnIndex(MediaStore.Files.FileColumns.DATA));
			String fileName =resultPath.substring(resultPath.lastIndexOf("/"));
			if(fileName.toLowerCase(Locale.getDefault()).contains(queryString.toLowerCase(Locale.getDefault()))){
				resultList.add(resultPath);
				count++;
			}
		}
		Log.d("acmllaugh", "Final count is by MediaStore: " + count);
		try {
			cursor.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultList;
	}

	private int countSearchResult(String queryString) {
		Uri uri = MediaStore.Files.getContentUri("external");
		String[] projection = { "COUNT(*)" };
		String selection = "";
		if (!(queryString == null || queryString.equals(""))) {
			selection = MediaStore.Files.FileColumns.DATA + " like '%"
					+ queryString + "%'";
		}
		Cursor cursor = context.getContentResolver().query(uri, projection,
				selection, null, null);
		if (cursor.moveToNext()) {
			return cursor.getInt(0);
		}
		return 0;
	}

	private String buildSelection(FileCategory cat) {
		String selection = null;
		switch (cat) {
		case DOC:
			selection = buildDocSelection()/*+" OR "+"(" + FileColumns.DATA + " LIKE '%.xlsx'" + ")"+" OR "+"(" + FileColumns.DATA + " LIKE '%.docx'" + ")"
					+" OR "+"(" + FileColumns.DATA + " LIKE '%.ppt'" + ")"+" OR "+"(" + FileColumns.DATA + " LIKE '%.pptx'" + ")"+" OR "+"(" + FileColumns.DATA + " LIKE '%.txt'" + ")"*/;
			break;
		case ZIP:
		//fineos add by zhanghe
			selection = "(" + FileColumns.MIME_TYPE + " == '" + ZIP_MIME_TYPE
					+ "')"+" OR "+"(" + FileColumns.DATA + " LIKE '%.rar'" + ")"+" OR "+"(" + FileColumns.DATA + " LIKE '%.7z'" + ")";
			break;
		case APK:
			selection = FileColumns.DATA + " LIKE '%.apk'";
			break;
		default:
			selection = "";
		}
		// not count files which are under other storage path:
		if (path == null || path.equals("")) {
			return selection;
		}
		if (!selection.isEmpty()) {
			selection = selection + " AND " + FileColumns.DATA + " LIKE '%"
					+ path + "%'";
		} else {
			selection = FileColumns.DATA + " LIKE '%" + path + "%'";
		}
		// Log.d(TAG, "Final selection is : " + selection);
		return selection;
	}

	private String buildDocSelection() {
		StringBuilder selection = new StringBuilder();
		selection.append("(");
		Iterator<String> iter = DOC_MIME_TYPES_SET.iterator();
		while (iter.hasNext()) {
			selection.append("(" + FileColumns.MIME_TYPE + "=='" + iter.next()
					+ "') OR ");
		}
		selection.delete(selection.lastIndexOf(")")+1, selection.length());

		selection.append(" OR (").append(FileColumns.DATA).append(" LIKE '%.xlsx' ")
				.append(") OR (").append(FileColumns.DATA).append(" LIKE '%.docx' ")
				.append(") OR (").append(FileColumns.DATA).append(" LIKE '%.ppt' ")
				.append(") OR (").append(FileColumns.DATA).append(" LIKE '%.pptx')");

		selection.append(")");
		return selection.toString();
	}

	@Override
	public int getResultCount() {
		return this.resultCount;
	}

	@Override
	public CursorLoader fuzzyQuery(String queryString) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Loader<Cursor> queryCategory(FileCategory mFileCategory) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CursorLoader fuzzyQuery(String queryString,
			FileCategory mFileCategory) {
		// TODO Auto-generated method stub
		return null;
	}
}
