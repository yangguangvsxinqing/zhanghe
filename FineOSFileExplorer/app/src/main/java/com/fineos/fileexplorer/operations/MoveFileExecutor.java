package com.fineos.fileexplorer.operations;

import android.content.Context;

import com.fineos.fileexplorer.util.FileUtils;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by xiaoyue on 15-8-4.
 */
public class MoveFileExecutor implements FileOperationListener{

    private final Context context;
    private FileOperationListener listener;
    private ArrayList<String> sameStoragePathList = new ArrayList<String>();
    private ArrayList<String> differentStoragePathList = new ArrayList<String>();
    private int taskCount = 3;


    public MoveFileExecutor(Context context, FileOperationListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void execute(ArrayList<String> sourcePathList, String destinationDirPath) {
        MoveTask task = new MoveTask(sourcePathList, destinationDirPath);
        task.start();
    }

    private void moveFiles(ArrayList<String> sourcePathList, String destinationDirPath) {
        prepareMoveList(sourcePathList, destinationDirPath);
        moveFileByRename(sameStoragePathList, destinationDirPath);
        moveFileWithCopy(differentStoragePathList, destinationDirPath);
    }

    private void prepareMoveList(ArrayList<String> sourcePathList, String destinationDirPath) {
        for (String sourcePath : sourcePathList) {
            if (destinationIsSameAsSourcePath(sourcePath, destinationDirPath)) {
                continue;
            }
            if (inSameStorage(sourcePath, destinationDirPath)) {
                sameStoragePathList.add(sourcePath);
            }else {
                differentStoragePathList.add(sourcePath);
            }
        }
    }

    private boolean inSameStorage(String sourcePath, String destinationDirPath) {
        File sourceFile = new File(sourcePath);
        File destDir = new File(destinationDirPath);
        if (sourceFile.getFreeSpace() == destDir.getFreeSpace()) {
            return true;
        }
        return false;
    }

    private boolean destinationIsSameAsSourcePath(String sourcePath, String destinationDirPath) {
        File sourceFile = new File(sourcePath);
        if (sourceFile.getParent().equals(destinationDirPath)) {
            return true;
        }
        return false;
    }

    private void moveFileWithCopy(ArrayList<String> sourcePathList, String destinationDirPath) {
        CopyFileExecutor executor = new CopyFileExecutor(context, this);
        executor.execute(sourcePathList, destinationDirPath);
    }

    private void moveFileByRename(ArrayList<String> sourcePathList, String destinationDirPath) {
        if (sourcePathList == null || sourcePathList.size() == 0) {
            decraseTaskCount();
        }
        for (String sourcePath : sourcePathList) {
            String renamePath = getUniqueRenamePath(destinationDirPath, sourcePath);
            RenameFileExecutor renameFileExecutor = new RenameFileExecutor(this, context);
            renameFileExecutor.execute(sourcePath, renamePath);
        }
    }

    private String getUniqueRenamePath(String destinationDirPath, String sourcePath) {
        File destDir = new File(destinationDirPath);
        return FileUtils.getUniqueNewFile(new File(sourcePath), destDir).getAbsolutePath();
    }

    private String getFileNameFromPathString(String sourcePath) {
        return new File(sourcePath).getName();
    }

    @Override
    public void onOperationStart() {

    }

    @Override
    public void onProgress(int finished, int total, String description) {

    }

    @Override
    public void onFinish(String action, boolean isSuccess) {
        decraseTaskCount();
        if (action.equals(FileOperationService.COPY_ACTION) && isSuccess) {
            DeleteFileExecutor executor = new DeleteFileExecutor(context, this);
            executor.execute(differentStoragePathList);
            return;
        }
        if (isSuccess && taskCount == 0) {
            listener.onFinish(FileOperationService.MOVE_ACTION, true);
        }
    }

    private synchronized void decraseTaskCount() {
        taskCount--;
    }

    private class MoveTask extends Thread {
        private final ArrayList<String> sourceList;
        private final String destDir;

        public MoveTask(ArrayList<String> sourceList, String destDir) {
            this.sourceList = sourceList;
            this.destDir = destDir;
        }

        @Override
        public void run() {
            moveFiles(sourceList, destDir);
        }
    }
}
