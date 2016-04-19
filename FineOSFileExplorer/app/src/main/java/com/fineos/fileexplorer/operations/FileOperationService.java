package com.fineos.fileexplorer.operations;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Process;
import android.util.Log;

import de.greenrobot.event.EventBus;

/**
 * Created by acmllaugh on 14-12-10.
 */
public class FileOperationService extends Service implements FileOperationListener {

    private static final String TAG = "FileOperationService";
    public static final String COPY_SOURCE_LIST = "copy_source_list";
    public static final String COPY_DESTATION_DIR = "copy_destation_dir";
    public static final String RENAME_SOURCE_FILE = "rename_source_file";
    public static final String RENAME_TO_FILE = "rename_to_file";
    public static final String MOVE_SOURCE_LIST = "move_source_list";
    public static final String MOVE_DESTATION_DIR = "move_destation_dir";
    public static final String COPY_ACTION = "com.fineos.fileexplorer.copy";
    public static final String RENAME_FILE = "com.fineos.fileexplorer.rename";
    public static final String MOVE_ACTION = "com.fineos.fileexplorer.move";
    public static final String DELETE_ACTION = "com.fineos.fileexplorer.delete";
    public static final String INVALID_ACTION = "invalid_action";
    public static final String DELETE_PATH_LIST = "delete_path_list";


    private EventBus mEventBus = EventBus.getDefault();
    private IFileOperationHelper mHelper;



    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        registerEventBus();
        String action = getActionFromIntent(intent);
        mHelper = FileOperationHelper.getInstance(this, this.getApplicationContext());
        if (action.equals(COPY_ACTION)) {
            CopyFileExecutor copyExecutor = new CopyFileExecutor(getApplicationContext(), this);
            copyExecutor.execute(intent.getStringArrayListExtra(COPY_SOURCE_LIST), intent.getStringExtra(COPY_DESTATION_DIR));
        }

        if (action.equals(RENAME_FILE)) {
            RenameFileExecutor renameFileExecutor = new RenameFileExecutor(this, getApplicationContext());
            renameFileExecutor.execute(intent.getStringExtra(RENAME_SOURCE_FILE), intent.getStringExtra(RENAME_TO_FILE));
        }

        if (action.equals(MOVE_ACTION)) {
            MoveFileExecutor moveFileExecutor = new MoveFileExecutor(getApplicationContext(), this);
            moveFileExecutor.execute(intent.getStringArrayListExtra(MOVE_SOURCE_LIST), intent.getStringExtra(MOVE_DESTATION_DIR));
        }

        if (action.equals(DELETE_ACTION)) {
            DeleteFileExecutor deleteExecutor = new DeleteFileExecutor(getApplicationContext(), this);
            deleteExecutor.execute(intent.getStringArrayListExtra(DELETE_PATH_LIST));
        }

        if (action.equals(INVALID_ACTION)) {
            stopSelf();
        }
        return START_STICKY;
    }

    private String getActionFromIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return INVALID_ACTION;
        }
        return intent.getAction();
    }


    private void registerEventBus() {
        try {
            if (!mEventBus.isRegistered(this)) {
                mEventBus.register(this);
            }
        } catch (Exception e) {
            Log.d(TAG, "registerEventBus (line 44): Event bus is not registered correctly : " + e.getMessage());
            e.printStackTrace();
        }
    }


    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy (line 290): Service on destroy is called.");
        unregisterEventBus();
        super.onDestroy();
    }

    private void unregisterEventBus() {
        try {
            if(mEventBus.isRegistered(this)) {
                mEventBus.unregister(this);
            }
        } catch (Exception e) {
            Log.d(TAG, "unregisterEventBus (line 61): Event bus is not unregistered correctly : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void onEvent(CancelMessage msg) {
        if (mHelper != null) {
            mHelper.cancel();
        }
    }

    public void onOperationFinised() {
          }

    @Override
    public void onOperationStart() {

    }

    @Override
    public void onProgress(int finished, int total, String description) {

    }

    @Override
    public void onFinish(String action, boolean isSuccess) {
        Log.d(TAG, "onFinish: on finished operation : " + action + " result : " + isSuccess);
        if (isSuccess) {
            mEventBus.post(new OperationResult(true, OperationResult.OperationResultType.FINISHED, "Operation finished successfully."));
        } else {
            mEventBus.post(new OperationResult(false, OperationResult.OperationResultType.FILE_IO_ERROR, " Operation finished unsuccessfully."));
        }
        stopSelf();
    }
}
