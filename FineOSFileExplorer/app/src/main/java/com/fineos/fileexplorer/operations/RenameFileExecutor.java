package com.fineos.fileexplorer.operations;

import android.content.Context;
import android.util.Log;

import com.fineos.fileexplorer.util.FileUtils;

import java.io.File;

/**
 * Created by xiaoyue on 15-8-3.
 */
public class RenameFileExecutor {

    private final Context context;
    FileOperationListener listener;

    public RenameFileExecutor(FileOperationListener listener, Context context) {
        this.listener = listener;
        this.context = context;
    }

    public void execute(String sourcePath, String renamePath) {
        RnameFileTask task = new RnameFileTask(sourcePath, renamePath);
        task.start();
    }

    private void rename(String sourcePath, String renamePath) {
        File sourceFile = new File(sourcePath);
        FileUtils.checkFileExistAndCanWrite(sourceFile);
        FileUtils.checkSpaceNotZero(sourceFile);
        File renameToFile = new File(renamePath);
        boolean renameResult = sourceFile.renameTo(renameToFile);
        if (renameResult) {
            scanRenamedFile(sourcePath, renamePath);
            listener.onFinish(FileOperationService.RENAME_FILE, true);
        }else {
            throw new RuntimeException("rename failed.");
        }
    }

    private void scanRenamedFile(final String sourceFilePath, final String renameToFilePath) {
        Log.d("acmllaugh1", "scanRenamedFile (line 184): scan rename file : " + sourceFilePath + " to " + renameToFilePath);
        MediaScanUtils.updateFile(context, sourceFilePath, renameToFilePath);
    }


    private class RnameFileTask extends Thread {
        String sourcePath;
        String renamePath;

        public RnameFileTask(String sourcePath, String renamePath) {
            this.sourcePath = sourcePath;
            this.renamePath = renamePath;
        }

        @Override
        public void run() {
            listener.onOperationStart();
            try {
                rename(sourcePath, renamePath);
            } catch (RuntimeException e) {
                listener.onFinish(FileOperationService.RENAME_FILE, false);
            }
        }
    }
}
