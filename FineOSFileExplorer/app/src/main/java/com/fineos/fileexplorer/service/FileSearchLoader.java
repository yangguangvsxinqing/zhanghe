package com.fineos.fileexplorer.service;

import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Files.FileColumns;
import android.util.Log;

import com.fineos.fileexplorer.entity.FileInfo.FileCategory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class FileSearchLoader implements IFileSearchHelper {

    private String ZIP_MIME_TYPE = "application/zip";
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
    private Context mContext;

    public FileSearchLoader(Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    public List<String> getMediaFileInfoListByCategory(FileCategory fileCategory) {
        return null;
    }

    @Override
    public List<String> fuzzyQuery(String queryString, int startPoint,
                                   int searchStep) {
        return null;
    }

    @Override
    public int getResultCount() {
        return 0;
    }

    @Override
    public CursorLoader fuzzyQuery(String queryString,
                                   FileCategory mFileCategory) {
        Uri uri = MediaStore.Files.getContentUri("external");
        if (mFileCategory != null) {
            uri = FileCategory.getContentUriByCategory(mFileCategory);
        }
        String[] projection = new String[]{MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATA};
        String selection = buildSelection(queryString, mFileCategory);
        String sortOrder = MediaStore.Files.FileColumns.DATE_MODIFIED + " desc";
        if (queryString != null && queryString.length() != 0) {
            return new CursorLoader(mContext, uri, projection, selection, new String[]{"%" + queryString + "%"},
                    sortOrder);
        }
        return new CursorLoader(mContext, uri, projection, selection, null,
                sortOrder);
    }


    @Override
    public CursorLoader fuzzyQuery(String queryString) {
        return fuzzyQuery(queryString, null);
    }


    @Override
    public Loader<Cursor> queryCategory(FileCategory mFileCategory) {
        return fuzzyQuery(null, mFileCategory);
    }

    private String buildSelection(String queryString, FileCategory cat) {
        String selection = null;
        if (cat == null) {
            if (queryString != null && !queryString.isEmpty()) {
                selection = buildPuzzySelection(queryString);
            } else {
                selection = buildNoHiddenFileSelection();
            }
            Log.d("FileSearchLoader", "selection : " + selection);
            return selection;
        }
        // Category is not null, build category selection.
        switch (cat) {
            case DOC:
                selection = buildDocSelection();
                break;
            case ZIP:
                //selection = "(" + FileColumns.MIME_TYPE + " == '" + ZIP_MIME_TYPE
                //        + "')";
				//fineos add by zhanghe 
				selection = "(" + FileColumns.MIME_TYPE + " == '" + ZIP_MIME_TYPE
                        + "')"+" OR "+"(" + FileColumns.DATA + " LIKE '%.rar'" + ")"+" OR "+"(" + FileColumns.DATA + " LIKE '%.7z'" + ")";

                break;
            case APK:
                selection = "(" + FileColumns.DATA + " LIKE '%.apk'" + ")";
                break;
            default:
        }
        if (selection == null && queryString != null && !queryString.isEmpty()) {
            // Files other than DOC, ZIP and APK;
            selection = buildPuzzySelection(queryString);
        } else if (queryString != null && !queryString.isEmpty()) {
            selection = selection + " AND ("
                    + buildPuzzySelection(queryString) + ")";
        } else if (cat != null && (cat.name() == FileCategory.APK.name() || cat.name() == FileCategory.ZIP.name()
                || cat.name() == FileCategory.DOC.name())) {
            selection += " AND (" + buildNoHiddenFileSelection() + ")";
            Log.d("FileSearchLoader", "selection : " + selection);
        }
        return selection;
    }

    private String buildNoHiddenFileSelection() {
        StringBuilder where = new StringBuilder();
        where.append("(" + MediaStore.Files.FileColumns.TITLE + " not like '.%' ");
        where.append(" AND ");
        where.append(MediaStore.Files.FileColumns.TITLE + " like '%')");
        where.append(" OR ");
        where.append("(" + MediaStore.Files.FileColumns.DISPLAY_NAME + " not like '.%'");
        where.append(" AND ");
        where.append(MediaStore.Files.FileColumns.DISPLAY_NAME + " like '%')");
        return where.toString();
    }

    private String buildPuzzySelection(String queryString) {
        StringBuilder where = new StringBuilder();
//         where.append("(" + MediaStore.Files.FileColumns.TITLE + " not like '.%' ");
//         where.append(" AND ");
//         where.append(MediaStore.Files.FileColumns.TITLE + " like '%" + queryString + "%')");
//         where.append(" OR ");
//         where.append("(" + MediaStore.Files.FileColumns.DISPLAY_NAME + " not like '.%'");
//         where.append(" AND ");
//         where.append(MediaStore.Files.FileColumns.DISPLAY_NAME + " like '%" + queryString + "%')");
        where.append("(" + FileColumns.DATA + " like ?)");
        return where.toString();
    }

    private String buildDocSelection() {
        StringBuilder selection = new StringBuilder();
        selection.append("(");
        Iterator<String> iter = DOC_MIME_TYPES_SET.iterator();
        while (iter.hasNext()) {
            selection.append("(" + FileColumns.MIME_TYPE + "=='" + iter.next()
                    + "') OR ");
        }
        selection.delete(selection.lastIndexOf(")") + 1, selection.length());
        selection.append(" OR (").append(FileColumns.DATA).append(" LIKE '%.xlsx' ")
                .append(") OR (").append(FileColumns.DATA).append(" LIKE '%.docx' ")
                .append(") OR (").append(FileColumns.DATA).append(" LIKE '%.ppt' ")
                .append(") OR (").append(FileColumns.DATA).append(" LIKE '%.pptx')");


        selection.append(")");
        return selection.toString();
    }


}
