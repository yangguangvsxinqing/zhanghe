package com.fineos.fileexplorer.adapters;

import com.fineos.fileexplorer.bussiness.FileViewActivityBussiness;

/**
 * Created by xiaoyue on 14-10-9.
 */
public interface IFileViewBussiness {
    FileViewActivityBussiness.SelectionState getCurrentSelectionState();
    void showCountNum(int num);

    void showSelectAll();

    void showDeSelectAll();
}
