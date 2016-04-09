package com.fineos.fileexplorer.operations;

import android.content.ContentValues;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by acmllaugh on 14-12-12.
 */
public class MediaScanUtils {

    public static void scanNewFiles(Context context, String[] filePathArray, MediaScannerConnection.OnScanCompletedListener listener){
        MediaScannerConnection.scanFile(context, filePathArray, null, listener);
    }

    public static void deleteFiles(Context context, String[] filePathArray, IFileOperationHelper helper) {
        if (helper == null || context == null) {
            return;
        }
        if (filePathArray == null) {
            helper.deleteFinishedCallback(false);
            return;
        }
        int size = filePathArray.length;
        if (size > 0) {
            String column = MediaStore.Files.FileColumns.DATA;
            StringBuilder selectionBuilder = new StringBuilder(column + "=?");
            for (int i = 1; i < size; i++) {
                selectionBuilder.append(" OR ");
                selectionBuilder.append(column + "=?");
            }
            Uri uri = MediaStore.Files.getContentUri("external");
            try {
                context.getContentResolver().delete(uri, selectionBuilder.toString(), filePathArray);
            } catch (Exception e) {
                helper.deleteFinishedCallback(false);
            }
        }
        helper.deleteFinishedCallback(true);
    }


    public static void updateFile(Context context, String sourceFilePath, String renameToFilePath) {
        ArrayList<String> filePathList = new ArrayList<String>();
        File file = new File(renameToFilePath);
        buildSubFilePathList(file, filePathList);
        Uri uri = MediaStore.Files.getContentUri("external");
        String deleteSelection = MediaStore.Files.FileColumns.DATA + " like ?";;
        int rowsDeleted = context.getContentResolver().delete(uri, deleteSelection, new String[]{sourceFilePath + "%"});
        Log.d("acmllaugh1", "updateFile (line 70): rows deleted : " + rowsDeleted);
        String[] pathList = new String[filePathList.size()];
        pathList = filePathList.toArray(pathList);
        scanNewFiles(context, pathList, null);
    }

    private static void buildSubFilePathList(File file, ArrayList<String> filePathList) {
        if (!file.exists()) {
            return;
        }
        filePathList.add(file.getAbsolutePath());
        if (!file.isDirectory()) {
            return;
        }
        File[] childFiles = file.listFiles();
        Log.d("acmllaugh1", "buildSubFilePathList: child files size : " + childFiles.length);
        if(childFiles == null) return;
        for (File child : childFiles) {
            buildSubFilePathList(child, filePathList);
        }
    }
}
