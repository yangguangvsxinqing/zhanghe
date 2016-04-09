package com.fineos.fileexplorer.operations;

import java.util.ArrayList;

/**
 * Created by acmllaugh on 14-12-11.
 */
public interface IFileOperationHelper {

    void copy(ArrayList<String> sourcePathList, String destDir);

    void deleteFinishedCallback(boolean deleteResult);

    void cancel();

    void rename(String sourceFilePath, String renameToFilePath);

    void move(ArrayList<String> sourceList, String destDir);
}
