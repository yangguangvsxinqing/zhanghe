package com.fineos.fileexplorer.operations;

/**
 * Created by acmllaugh on 14-12-12.
 */
public final class ProgressMessage {
    private String fileName;
    private int fileLeftCount;

    public ProgressMessage(String currentFilePath, int fileLeftCount) {
        this.fileName = currentFilePath;
        this.fileLeftCount = fileLeftCount;
    }

    public String getFileName() {
        return fileName;
    }

    public int getFileLeftCount() {
        return fileLeftCount;
    }
}
