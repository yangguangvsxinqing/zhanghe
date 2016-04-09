package com.fineos.fileexplorer.operations;

import android.content.Context;
import android.provider.MediaStore;
import android.util.Log;

import com.fineos.fileexplorer.util.FileUtils;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by xiaoyue on 15-8-3.
 */
public class DeleteFileExecutor {

    private static final String TAG = "DeleteFileExecutor";
    Context context;
    FileOperationListener listener;

    public DeleteFileExecutor(Context context, FileOperationListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void execute(ArrayList<String> deletePathList) {
        DeleteFileTask task = new DeleteFileTask(deletePathList);
        task.start();
    }

    private void deleteFiles(ArrayList<String> deletePathList) {
        int filesCount = deletePathList.size();
        int finishedCount = 0;
        for (String path : deletePathList) {
            File file = new File(path);
            FileUtils.checkFileExistAndCanWrite(file);
            if (file.isDirectory()) {
                Log.d(TAG, "deleteFiles: delete dir : " + file.getAbsolutePath());
                deleteDirectory(file);
            }else {
                Log.d(TAG, "deleteFiles: delete file : " + file.getAbsolutePath());
                deleteSingleFile(file);
            }
            listener.onProgress(finishedCount++, filesCount, file.getAbsolutePath());
        }
    }

    private void deleteSingleFile(File file) {
        context.getContentResolver().delete(
                MediaStore.Files.getContentUri("external"), " _data = ?", new String[]{file.getAbsolutePath()});
        if (file.exists()) file.delete();
    }

    private void deleteDirectory(File file) {
        if(file.listFiles() != null && file.listFiles().length != 0) {
            context.getContentResolver().delete(
                    MediaStore.Files.getContentUri("external"), " _data like ?", new String[]{file.getAbsolutePath() + File.separator + "%"});
        }
        context.getContentResolver().delete(
                MediaStore.Files.getContentUri("external"), " _data = ?", new String[]{file.getAbsolutePath()});
        if (file.exists()) {
            File[] childFiles = file.listFiles();
            if (childFiles != null && childFiles.length > 0) {
                for (File childFile : childFiles) {
                    if (childFile.isDirectory()) {
                        deleteDirectory(childFile);
                    }else{
                        deleteSingleFile(childFile);
                    }
                }
            }
            file.delete();
        }
    }

    private class DeleteFileTask extends Thread {
        ArrayList<String> deletePathList;

        public DeleteFileTask(ArrayList<String> deletePathList) {
            this.deletePathList = deletePathList;
        }

        @Override
        public void run() {
            listener.onOperationStart();
            try {
                deleteFiles(deletePathList);
            } catch (Exception e) {
                e.printStackTrace();
                listener.onFinish(FileOperationService.DELETE_ACTION, false);
            }
            listener.onFinish(FileOperationService.DELETE_ACTION, true);
        }
    }
}
