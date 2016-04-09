package com.fineos.fileexplorer.operations;

/**
 * Created by xiaoyue on 15-8-3.
 */
public interface FileOperationListener {

    void onOperationStart();

    void onProgress(int finished, int total, String description);

    void onFinish(String action, boolean isSuccess);

}
