package com.fineos.fileexplorer.operations;

import android.app.Service;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.fineos.fileexplorer.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.greenrobot.event.EventBus;

/**
 * Created by acmllaugh on 14-12-11.
 */
public class FileOperationHelper implements IFileOperationHelper, MediaScannerConnection.OnScanCompletedListener {
    private static final int SEND_TASK_INFO = 231;
    private static final int SEND_OPERATION_RESULT = 233;
    private static final int SEND_PROGRESS = 234;
    private static FileOperationHelper mInstance;
    private final Context mContext;
    private ExecutorService mOperationExecutor = Executors.newFixedThreadPool(10);
    private ExecutorService mScanExecutor = Executors.newFixedThreadPool(10);
    private EventBus mEventBus = EventBus.getDefault();
    private boolean mIsCancelled = false;
    private boolean mIsOperating = false;
    private boolean mIsScanning = false;
    private final MyLock mMyLock = new MyLock();
    private boolean isLocked = true;
    private boolean hasErrorAccuired = false;
    private Service mService;
    private int mScanCount;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SEND_TASK_INFO:
                    if (msg.obj instanceof PreparedTaskInfo) {
                        PreparedTaskInfo taskInfo = (PreparedTaskInfo) msg.obj;
                        mEventBus.post(taskInfo);
                    }
                    break;
                case SEND_OPERATION_RESULT:
                    if (msg.obj instanceof OperationResult) {
                        OperationResult result = (OperationResult) msg.obj;
                        mEventBus.post(result);
                        if (mService != null && !finishedSuccessfully(result)) {
                            mService.stopSelf();
                        }
                    }
                    break;
                case SEND_PROGRESS:
                    if (msg.obj instanceof ProgressMessage) {
                        ProgressMessage process = (ProgressMessage) msg.obj;
                        mEventBus.post(process);
                    }
                    break;
            }
//            mEventBus.post(new OperationResult("job finished."));
//            timeCount = System.currentTimeMillis() - timeCount;
//            Log.d("acmllaugh1", "handleMessage (line 41): time : " + (timeCount / 1000));
        }
    };

    private boolean finishedSuccessfully(OperationResult result) {
        return result.getResult() == OperationResult.OperationResultType.FINISHED;
    }

    private long timeCount = 0;

    private FileOperationHelper(Service mService, Context context) {
        this.mService = mService;
        this.mContext = context;
    }

    public static synchronized FileOperationHelper getInstance(Service service, Context context) {
        if (mInstance == null) {
            mInstance = new FileOperationHelper(service, context);
        }
        return mInstance;
    }

    public void copy(final ArrayList<String> sourceList, final String destDir) {
        synchronized (mMyLock) {
            mIsCancelled = false;
            mIsOperating = true;
        }
        timeCount = System.currentTimeMillis();
        final PreparedTaskInfo taskInfo = new PreparedTaskInfo();
        if (sourceList == null || sourceList.size() == 0 || destDir == null) {
            OperationResult result = new OperationResult(false,
                    OperationResult.OperationResultType.FILE_NOT_EXIST, "source file or destation folder is not exist.");
            sendResult(result);
        }
        mOperationExecutor.execute(new Runnable() {
            @Override
            public void run() {
                prepareCopy(sourceList, destDir, taskInfo);
            }
        });
        mOperationExecutor.execute(new Runnable() {
            @Override
            public void run() {
                while (!taskInfo.isPrepared) {
                    synchronized (mMyLock) {
                        try {
                            mMyLock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                startCopy(taskInfo);
            }
        });
    }

    @Override
    public void deleteFinishedCallback(boolean deleteResult) {
        if (!deleteResult) {
            Log.d("acmllaugh1", "deleteFinishedCallback (line 105): error accurred when delete file from database.");
        }
        if (mService != null) {
            mService.stopSelf();
        }
    }

    @Override
    public void cancel() {
        if (!mIsCancelled) {
            synchronized (this) {
                mIsCancelled = true;
            }
            OperationResult result = new OperationResult(false, OperationResult.OperationResultType.CANCEL
                    , "User cancelled the operation.");
            sendResult(result);
        }
    }

    @Override
    public void rename(String sourceFilePath, String renameToFilePath) {
        // When rename file, we directly rename the file, and update database after we renamed one file.
        File sourceFile = new File(sourceFilePath);
        OperationResult result;
        if (!sourceFile.exists()) {
            result = new OperationResult(false, OperationResult.OperationResultType.FILE_NOT_EXIST,
                    "rename source file does not exist.");
            sendResult(result);
        }else if (sourceFile.getFreeSpace() <= 0) {
            result = new OperationResult(false, OperationResult.OperationResultType.TARGET_STORAGE_IS_FULL,
                    "rename file's storage has been too full.");
            sendResult(result);
        }else if (!sourceFile.canWrite()) {
            result = new OperationResult(false, OperationResult.OperationResultType.TARGET_FILE_CANNOT_WRITE,
                    "rename file cannot write.");
            sendResult(result);
        }else {
            File renameToFile = new File(renameToFilePath);
            if (sourceFile.renameTo(renameToFile)) {
                result = new OperationResult(true, OperationResult.OperationResultType.FINISHED, "rename file success!");
                sendResult(result);
                scanRenamedFile(sourceFilePath, renameToFilePath);
                return;
            }
        }
        result = new OperationResult(false, OperationResult.OperationResultType.OTHER_REASONS,
                "rename file failed due to other reasons.");
        sendResult(result);
    }

    @Override
    public void move(ArrayList<String> sourceList, String destDir) {
        if (isInSameDir(sourceList.get(0), destDir)) {
            OperationResult result = new OperationResult(false,
                    OperationResult.OperationResultType.FINISHED, "need not to move, the same dir.");
            sendResult(result);
        }else {

        }
    }

    //Is move source and dest dir are the same dir.
    private boolean isInSameDir(String s, String destDir) {
        File sourceFile = new File(s);
        if (sourceFile.getAbsolutePath().equals(destDir + File.separator + sourceFile.getName())) {
            return true;
        }
        return false;
    }

    private void scanRenamedFile(final String sourceFilePath, final String renameToFilePath) {
        final File file = new File(renameToFilePath);
        Log.d("acmllaugh1", "scanRenamedFile (line 184): scan rename file : " + sourceFilePath + " to " + renameToFilePath);
        mScanExecutor.execute(new Runnable() {
            @Override
            public void run() {
                MediaScanUtils.updateFile(mContext, sourceFilePath, renameToFilePath);
            }
        });
    }

    private boolean prepareCopy(ArrayList<String> sourceDirList, String destDir, PreparedTaskInfo taskInfo) {
        HashMap<String, String> fileCopyRecordMap = taskInfo.fileCopyRecordMap;
        ArrayList<String> dirList = taskInfo.dirList;
        taskInfo.totalFileSize = 0l;
        for (String sourcePath : sourceDirList) {
            File file = new File(sourcePath);
            if (!file.exists()) {
                OperationResult result = new OperationResult(false,
                        OperationResult.OperationResultType.FILE_NOT_EXIST, "source file is not exist");
                sendResult(result);
                continue;
            }
            taskInfo.totalFileSize += file.length();
            if (file.isDirectory()) {
                String nextDestDir = getUniquePath(destDir + File.separator + file.getName());
                dirList.add(nextDestDir);
                processDir(file, nextDestDir, taskInfo);
            } else {
                String destFile = destDir + File.separator + file.getName();
                fileCopyRecordMap.put(sourcePath, getUniquePath(destFile));
            }
        }
        synchronized (mMyLock) {
            taskInfo.isPrepared = true;
            mMyLock.notify();
        }
        if (checkDestDir(taskInfo, destDir)) {
            sendPreparedMessage(taskInfo);
        }

//        for (String s : fileCopyRecordMap.keySet()) {
//            Log.d("acmllaugh1", "prepareCopy (line 84): source : " + s + " destation : " + fileCopyRecordMap.get(s));
//        }
//        for (String s : dirList) {
//            Log.d("acmllaugh1", "prepareCopy (line 87): dir : " + s);
//        }
//        Log.d("acmllaugh1", "prepareCopy (line 88): total size : " + (taskInfo.totalFileSize / 1024) / 1024);
        return true;
    }

    private boolean checkDestDir(PreparedTaskInfo taskInfo, String destDir) {
        File destDirFile = new File(destDir);
        if (!destDirFile.exists()) {
            OperationResult result = new OperationResult(false, OperationResult.OperationResultType.DESTATION_DIR_NOT_AVAILABLE
                    , "Destation directory is not available.");
            sendResult(result);
            return false;
        }
        if (destDirFile.getFreeSpace() < taskInfo.getTotalFileSize()) {
            OperationResult result = new OperationResult(false, OperationResult.OperationResultType.TARGET_STORAGE_IS_FULL
                    , "Target storage space is not enough.");
            sendResult(result);
            return false;
        }
        return true;
    }

    private void sendResult(OperationResult result) {
        Message msg = new Message();
        msg.what = SEND_OPERATION_RESULT;
        msg.obj = result;
        mHandler.sendMessage(msg);
    }

    private void sendPreparedMessage(PreparedTaskInfo taskInfo) {
        Message msg = new Message();
        msg.what = SEND_TASK_INFO;
        msg.obj = taskInfo;
        mHandler.sendMessage(msg);
    }

    private void processDir(File file, String destDir,
                            PreparedTaskInfo taskInfo) {
        if (file == null || !file.isDirectory()) {
            return;
        }
        taskInfo.totalFileSize += file.length();
        for (File child : file.listFiles()) {
            if (!child.exists()) {
                mEventBus.post(new OperationResult(false, OperationResult.OperationResultType.OTHER_REASONS, "file not exists."));
                continue;
            }
            taskInfo.totalFileSize += child.length();
            if (child.isDirectory()) {
                String nextDestDir = getUniquePath(destDir + File.separator + child.getName());
                taskInfo.getDirList().add(nextDestDir);
                processDir(child, nextDestDir, taskInfo);
            } else {
                String destFile = destDir + File.separator + child.getName();
                taskInfo.getFileMap().put(child.getAbsolutePath(), getUniquePath(destFile));
            }
        }
    }

    private void startCopy(final PreparedTaskInfo taskInfo) {
        Log.d("acmllaugh1", "startCopy (line 226): start copy.");
        boolean createDirResult = createDirs(taskInfo);
        if (!createDirResult || mIsCancelled) {
            Log.d("acmllaugh1", "startCopy (line 229): createDirResult : " + createDirResult);
            Log.d("acmllaugh1", "startCopy (line 230): cancelled : " + mIsCancelled);
            return;
        }
        synchronized (mMyLock) {
            taskInfo.fileCount += taskInfo.fileCopyRecordMap.size();
        }
        if (taskInfo.fileCount == 0) {
            // Maybe there are just directories that need to copy.
            synchronized (mMyLock) {
                updateFileCount(taskInfo);
            }
        }
        for (final String sourcePath : taskInfo.fileCopyRecordMap.keySet()) {
            final String destPath = taskInfo.fileCopyRecordMap.get(sourcePath);
//            mOperationExecutor.execute(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        FileUtils.copyFile(sourcePath, destPath);
//                        synchronized (mMyLock) {
//                            Log.d("acmllaugh1", "run (line 70): current mFileCount is : " + mFileCount);
//                            mFileCount--;
//                            if (mFileCount <= 0) {
//                                Log.d("acmllaugh1", "run (line 72): notice main thread.");
//                                isLocked = false;
//                                mMyLock.notify();
//                                mHandler.sendMessage(new Message());
//                            }
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            });
            mOperationExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    Process process = null;
                    try {
                        if (!hasErrorAccuired && !mIsCancelled) {
                            Log.d("acmllaugh1", "run (line 287): use cmd cp to copy " + sourcePath + " to " + destPath);
                            process = Runtime.getRuntime().exec(new String[]{"cp", sourcePath, destPath});
                            sendProgressMessage(sourcePath, taskInfo);
                            File sourceFile = new File(sourcePath);
                            File destFile = new File(destPath);
                            long sourceLength = sourceFile.length();
                            long lastCopiedBytes = 0;
                            int  copyTimeOutCount = 5;
                            while (sourceLength != destFile.length()) {
                                if (hasErrorAccuired || mIsCancelled) {
                                    return;
                                }
                                if (lastCopiedBytes >= destFile.length()) {
                                    copyTimeOutCount--;
                                    if (copyTimeOutCount <= 0) {
                                        Log.d("acmllaugh1", "run: copy time out : " + destPath);
                                        throw new IOException();
                                    }
                                }
                                lastCopiedBytes = destFile.length();
                                Thread.sleep(1000);
                            }
                            Thread.sleep(100);
                            process.getInputStream().close();
                            process.getOutputStream().close();
                            process.getErrorStream().close();
                            process.destroy();
                            synchronized (mMyLock) {
                                updateFileCount(taskInfo);
                            }
                        }
                    } catch (IOException e) {
                        Log.d("acmllaugh1", "run (line 283): io error accurried : " + e.toString());
                        synchronized (mMyLock) {
                            hasErrorAccuired = true;
                        }
                        OperationResult result = new OperationResult(false, OperationResult.OperationResultType.FILE_IO_ERROR
                                , mContext.getString(R.string.copy_ioe_detail));
                        sendResult(result);
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        Log.d("acmllaugh1", "run (line 283): interrupted error accurried.");
                        synchronized (mMyLock) {
                            hasErrorAccuired = true;
                        }
                        OperationResult result = new OperationResult(false, OperationResult.OperationResultType.OTHER_REASONS
                                , "InterruptedException has been throwed.");
                        sendResult(result);
                        e.printStackTrace();
                    } finally {
                        try {
                            if (process != null) {
                                process.getInputStream().close();
                                process.getOutputStream().close();
                                process.getErrorStream().close();
                                process.destroy();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

        }
    }

    private void updateFileCount(PreparedTaskInfo taskInfo) {
//        Log.d("acmllaugh1", "run (line 70): current mFileCount is : " + mFileCount);
        synchronized (mMyLock) {
            taskInfo.fileCount -= 1;
        }
        if (taskInfo.fileCount <= 0) {
            synchronized (mMyLock) {
                mIsOperating = false;
            }
//            Log.d("acmllaugh1", "run (line 72): notice main thread.");
            mMyLock.notify();
            OperationResult result = new OperationResult(true, OperationResult.OperationResultType.FINISHED,
                    "task successfully finished.");
            sendResult(result);
            scanFilesIfSuccess(taskInfo, result);
        }
    }

    private void scanFilesIfSuccess(PreparedTaskInfo taskInfo, OperationResult result) {
        if (result.getResult() == OperationResult.OperationResultType.FINISHED) {
            int dirCount = taskInfo.dirList.size();
            int fileCount = taskInfo.fileCopyRecordMap.size();
            synchronized (mMyLock) {
                mIsScanning = true;
                mScanCount += dirCount + fileCount;
            }
            final String[] pathArray = new String[mScanCount];
            for (int i = 0; i < dirCount; ++i) {
                pathArray[i] = taskInfo.dirList.get(i);
            }
            int index = dirCount;
            for (String s : taskInfo.fileCopyRecordMap.keySet()) {
                pathArray[index] = taskInfo.fileCopyRecordMap.get(s);
                index++;
            }
            mScanExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    MediaScanUtils.scanNewFiles(mContext, pathArray, FileOperationHelper.this);
                }
            });

        }
    }

    private boolean createDirs(PreparedTaskInfo taskInfo) {
        if (mIsCancelled) {
            return false;
        }
        for (String dir : taskInfo.dirList) {
            File file = new File(dir);
            boolean result = file.mkdirs();
            if (!result) {
                OperationResult operationResult = new OperationResult(false, OperationResult.OperationResultType.CREATE_DIR_FAILED,
                        "Create directory failed.");
                sendResult(operationResult);
                return false;
            }
        }
        return true;
    }

    private void sendProgressMessage(String sourcePath, PreparedTaskInfo taskInfo) {
        ProgressMessage process = new ProgressMessage(sourcePath, taskInfo.fileCount);
        Message msg = new Message();
        msg.what = SEND_PROGRESS;
        msg.obj = process;
        mHandler.sendMessage(msg);
    }

    private static String getUniquePath(String destFilePath) {
        File destFile = new File(destFilePath);
        int i = 1;
        boolean suffixAdded = false;
        while (destFile.exists()) {
            String nextDestPath = destFile.getPath();
            if (!suffixAdded) {
                nextDestPath += "_";
                suffixAdded = true;
            } else {
                nextDestPath = nextDestPath.substring(0, nextDestPath.lastIndexOf("_") + 1);
            }
            if (destFile.isDirectory()) {
                nextDestPath += i++;
            } else {
                String destPath = destFile.getPath();
                if (destPath.contains(".")) {
                    nextDestPath = destPath.substring(0, destPath.lastIndexOf(".")) + i++
                            + destPath.substring(destPath.lastIndexOf("."));
                } else {
                    nextDestPath += i++;
                }
            }
            destFile = new File(nextDestPath);
        }
        return destFile.getAbsolutePath();
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
//        Log.d("acmllaugh1", "onScanCompleted (line 354): scan " + path + " successful.");
        synchronized (this) {
            mScanCount--;
//            Log.d("acmllaugh1", "onScanCompleted (line 410): current scan count : " + mScanCount);
            if (mScanCount <= 0) {
                Log.d("acmllaugh1", "onScanCompleted (line 361): scan finished.");
                mIsScanning = false;
                if (!mIsOperating) {
                    mService.stopSelf();
                } else {
                    Log.d("acmllaugh1", "onScanCompleted (line 421): operation is executing, cannot stop service.");
                }
            }
        }
    }


    private final class MyLock {

    }


    public final class PreparedTaskInfo {
        private HashMap<String, String> fileCopyRecordMap;
        private ArrayList<String> dirList;
        private long totalFileSize;
        private int fileCount;
        private boolean isPrepared;

        private PreparedTaskInfo() {
            fileCopyRecordMap = new HashMap<String, String>();
            dirList = new ArrayList<String>();
            totalFileSize = 0;
            fileCount = 0;
            isPrepared = false;
        }

        public HashMap<String, String> getFileMap() {
            return fileCopyRecordMap;
        }

        public ArrayList<String> getDirList() {
            return dirList;
        }

        public long getTotalFileSize() {
            return totalFileSize;
        }

    }
}
