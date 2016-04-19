package com.fineos.fileexplorer.operations;

import android.content.Context;
import android.os.Process;
import android.util.Log;

import com.fineos.fileexplorer.util.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/**
 * Created by xiaoyue on 15-7-27.
 */
public class CopyFileExecutor {

    private static final String TAG = "CopyFileExecutor";
    public static final String COPY_FILE_INTO_SLEF_MESSAGE = "cannot copy directory into itsself";
    private EventBus mEventBus = EventBus.getDefault();
    private FileOperationListener mOperationListener;
    private ArrayList<String> mCopyedFilePaths = new ArrayList<>();
    private Context mContext;

    public CopyFileExecutor(Context context, FileOperationListener listener) {
        mOperationListener = listener;
        mContext = context;
    }

    public void execute(ArrayList<String> sourceList, String destDir) {
        Log.d(TAG, "copy: source : " + sourceList.toString() + " dest : " + destDir);
        CopyTask task = new CopyTask(sourceList, destDir);
        task.start();
    }

    private void copyFiles(ArrayList<String> sourceList, String destDir) {
        int totalCount = sourceList.size();
        int finishedCount = 0;
        File destDirFile = new File(destDir);
        for (String sourcePath : sourceList) {
            File sourceFile = new File(sourcePath);
            if (sourceFile.isDirectory()) {
                copyDirectory(sourceFile, destDirFile);
            }else{
                copySingleFile(sourceFile, destDirFile);
            }
            mOperationListener.onProgress(finishedCount++, totalCount, sourceFile.getAbsolutePath());
        }
    }

    private void copySingleFile(File sourceFile, File destDir) {
        FileUtils.checkFileExistAndCanRead(sourceFile);
        File newFile = null;
        try {
            Log.d("FileOperationService", "copy from sourcePath : " +
                    sourceFile.getPath() + " to destDirectory : " + destDir);
            FileInputStream source = new FileInputStream(sourceFile.getAbsolutePath());
            File uniqueNewFile = FileUtils.getUniqueNewFile(sourceFile, destDir);
            newFile = uniqueNewFile;
            FileOutputStream dest = new FileOutputStream(uniqueNewFile);
            byte[] buffer = new byte[10240];
            int bytesReadedIn = source.read(buffer);
            while (bytesReadedIn != -1) {
                dest.write(buffer, 0, bytesReadedIn);
                bytesReadedIn = source.read(buffer);
            }
            source.close();
            dest.close();
            mCopyedFilePaths.add(uniqueNewFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            if (e.getMessage() != null && e.getMessage().contains("ENOSPC")) {
                if (newFile != null && newFile.exists()) {
                    newFile.delete();
                }
                throw new RuntimeException("copy failed, there is no space for file : " + sourceFile.getAbsolutePath());
            }
            throw new RuntimeException("copy io error, source : " + sourceFile + " dest dir : " + destDir);
        }
    }

    private void copyDirectory(File sourceDir, File destDir) {
        if (sourceDir.getAbsolutePath().equals(destDir.getAbsolutePath())) {
            throw new RuntimeException(COPY_FILE_INTO_SLEF_MESSAGE);
        }
        File subDirectory = getSubDirectory(sourceDir, destDir);
        if (!subDirectory.exists()) {
            subDirectory.mkdirs();
            mCopyedFilePaths.add(subDirectory.getAbsolutePath());
        }
        File[] files = sourceDir.listFiles();
        for (File sourceFile : files) {
            if (sourceFile.isDirectory()) {
                copyDirectory(sourceFile, subDirectory);
            } else {
                copySingleFile(sourceFile, subDirectory);
            }
        }
    }

    private File getSubDirectory(File sourceFile, File destDir) {
        return FileUtils.getUniqueNewFile(sourceFile, destDir);
    }


    private class CopyTask extends Thread {
        private final ArrayList<String> sourceList;
        private final String destDir;

        public CopyTask(ArrayList<String> sourceList, String destDir) {
            this.sourceList = sourceList;
            this.destDir = destDir;
        }

        @Override
        public void run() {
            try {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                File destFile = new File(destDir);
                FileUtils.checkFileExistAndCanWrite(destFile);
                FileUtils.checkSpaceNotZero(destFile);
                mOperationListener.onOperationStart();
                copyFiles(sourceList, destDir);
                notifyMediaServerFilesChanged();
                mOperationListener.onFinish(FileOperationService.COPY_ACTION, true);
            } catch (RuntimeException e) {
                e.printStackTrace();
                if (e.getMessage().contains(COPY_FILE_INTO_SLEF_MESSAGE)) {
                    mEventBus.post(new OperationResult(false, OperationResult.OperationResultType.CANNOT_COPY_FILE_INTO_SELF, "Copy file failed."));
                } else if (e.getMessage().contains("there is no space for file")) {
                    mEventBus.post(new OperationResult(false, OperationResult.OperationResultType.TARGET_STORAGE_IS_FULL, "Copy file failed."));
                } else {
                    mEventBus.post(new OperationResult(false, OperationResult.OperationResultType.FILE_IO_ERROR, "Copy file failed."));
                }
                mOperationListener.onFinish(FileOperationService.COPY_ACTION, false);
            }
        }
    }

    private void notifyMediaServerFilesChanged() {
        String[] filePathArray = new String[mCopyedFilePaths.size()];
        MediaScanUtils.scanNewFiles(mContext, mCopyedFilePaths.toArray(filePathArray), null);
    }

}
