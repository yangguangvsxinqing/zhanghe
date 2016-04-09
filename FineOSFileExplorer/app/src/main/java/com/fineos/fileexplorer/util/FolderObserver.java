package com.fineos.fileexplorer.util;

import android.os.FileObserver;
import android.util.Log;

/**
 * Created by xiaoyue on 15-5-21.
 */
public class FolderObserver extends FileObserver {

    public interface FolderObserverListener{
        void onContentChange();
    }

    private FolderObserverListener mListener;

    public FolderObserver(String path) {
        super(path, FileObserver.CLOSE_WRITE | FileObserver.CREATE | FileObserver.DELETE
                | FileObserver.DELETE_SELF | FileObserver.MODIFY);
    }

    public void setmListener(FolderObserverListener listener) {
        this.mListener = listener;
    }


    @Override
    public void onEvent(int event, String path) {
        Log.d("FileViewActivity", "onEvent: folder obsrever : " + event);
        switch (event) {
            case FileObserver.CREATE:
            case FileObserver.DELETE:
            case FileObserver.MODIFY:
                mListener.onContentChange();
                break;
        }
    }

}
