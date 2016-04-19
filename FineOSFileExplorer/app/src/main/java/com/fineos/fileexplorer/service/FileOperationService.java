package com.fineos.fileexplorer.service;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.fineos.fileexplorer.service.FileOperationService.OperationType;
import com.fineos.fileexplorer.util.StringUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import de.greenrobot.event.EventBus;

public class FileOperationService extends IntentService implements MediaScannerConnectionClient{

	public static final String NEW_FOLDER_PATH = "new_folder_path";
	public static final String PASTE_DEST_PATH = "paste_dest_path";
	public static final String PASTE_SOURCE_LIST = "paste_source_list";
	public static final String DELETE_LIST = "delete_list";
	public static final String INTENT_TYPE = "intent_type";
	public static final String COPY_DEST_PATH = "copy_dest_path";
	public static final String COPY_SOURCE_LIST = "copy_source_list";
	public static final String ANDROID_SECURE = "/mnt/sdcard/.android_secure";
	public static final String TASK_COMPLETED = "task_completed";
	public static final String TASK_NOT_COMPLETED = "task_not_completed";
	public static final String TAST_NAME_DUPLICATION = "tast_name_duplication" ;
	private static final int BUFFER = 4096;
	
	public enum OperationType{
		COPY, PASTE, DELETE, CREATE_FOLDER
	}
	private OperationType mIntentType;
	private ArrayList<String> mCopyPathList;
	private ArrayList<String> mDeletePathList;
	private ArrayList<String> mPasteSourcePathList;
	private String mCopyDestPath;
	private String mPasteDestPath;
	private String mNewFolderPath;
	private String mTaskCompleteState = TASK_COMPLETED;
	private MediaScannerConnection mMediaScanConnection;
	private EventBus mEventBus = EventBus.getDefault();
	
	public FileOperationService() {
		super("fileoperationservice");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mMediaScanConnection = new MediaScannerConnection(this, this);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if(mMediaScanConnection != null && !mMediaScanConnection.isConnected()){
			mMediaScanConnection.connect();
		}
		if(!mEventBus.isRegistered(this)){
			mEventBus.register(this);
		}
		mIntentType = getIntentType(intent);
		//Log.d("FileOperationService", "mIntentType : " + mIntentType.name());
		if (mIntentType != null) {
			switch (mIntentType) {
			case COPY:
				getCopyData(intent);
				doCopyOperation();
				break;
			case DELETE:
				getDeleteData(intent);
				doDeleteOperation();
				break;
			case PASTE:
				getPasteData(intent);
				doPasteOperation();
				break;
			case CREATE_FOLDER:
				getNewFolderData(intent);
				doCreateFolder();
				break;
			default:
				break;
			}
		}
		notifyServiceJobDown();
		mEventBus.unregister(this);
	}


	private void doCreateFolder() {
		boolean result = createFolder(mNewFolderPath);
		if(!result){
			mTaskCompleteState = TAST_NAME_DUPLICATION;
		}
	}


	private void getNewFolderData(Intent intent) {
		mNewFolderPath = intent.getStringExtra(NEW_FOLDER_PATH);
	}

	public boolean createFolder(String newFolderPath) {
        File f = new File(newFolderPath);
        if (f.exists())
            return false;
        boolean result = f.mkdir();
        MediaScannerConnection.scanFile(this, new String[]{f.getPath()}, null, null);
        return result;
    }

	private void getPasteData(Intent intent) {
		mPasteSourcePathList = intent.getStringArrayListExtra(PASTE_SOURCE_LIST);	
		mPasteDestPath = intent.getStringExtra(PASTE_DEST_PATH);
	}


	private void doPasteOperation() {
		for(String sourcePath : mPasteSourcePathList){
			File sourceFile = new File(sourcePath);
			moveFile(sourceFile, mPasteDestPath);
		}
	}

	public boolean moveFile(File file, String dest) {
       // StringUtils.printLog("FileUtils", "MoveFile >>> " + file.getPath() + "," + dest);
        if (file == null || dest == null) {
            StringUtils.printLog("FileUtils", "CopyFile: null parameter");
            return false;
        }
    	//If destination is the source directory, then we do not need to move.
        if(file.getPath().equals(dest + File.separator + file.getName())){
        	return true;
        }
        File destFile = new File(dest + File.separator + file.getName()) ;
        destFile = getUniqueFile(destFile);// check if there is overlapped file.
        try {
        	ArrayList<File> deleteFileList = new ArrayList<File>();
        	buildDeleteFileList(file, deleteFileList);
        	boolean result = file.renameTo(destFile);
        	if(!result){// Try copy through disks.
        		//Log.d("FileOperationService", "rename failed, try copy and delete.");
        		try{
            		copyToDirectory(file.getPath(), dest);
            		deleteFile(file);
            		return true;
        		}catch(Exception e){
        			return false;
        		}
        	}else{
            	notifyMediaStoreListDeleted(deleteFileList);
            	notifyMediaStoreDataMoved(destFile);
        	}
        	//StringUtils.printLog("FileUtils", "file path : " + file.getPath());  	
            return result;
        } catch (SecurityException e) {
           StringUtils.printLog("FileUtils", "Fail to move file," + e.toString());
        }
        return false;
    }
	
	private ArrayList<File> buildDeleteFileList(File file, ArrayList<File> deleteList) {
		if(file.isDirectory()){
			for(File child : file.listFiles()){
				buildDeleteFileList(child, deleteList);
			}
		}
		deleteList.add(file);
		return deleteList;
	}
	
	private void notifyMediaStoreListDeleted(ArrayList<File> deleteList) {
		for(File file : deleteList){
			notifyMediaStoreDataDeleted(file);
		}
	}
	
	private void notifyMediaStoreDataMoved(File newFile) {
		if(newFile.isDirectory()){			
			 for(File child : newFile.listFiles()){
				 notifyMediaStoreDataMoved(child);
			 }
		}
		StringUtils.printLog("FileUtils", "new file path : " + newFile.getPath());
        if(mMediaScanConnection!= null && mMediaScanConnection.isConnected()){
		    mMediaScanConnection.scanFile(newFile.getPath(), null);
        }else{
            if (mMediaScanConnection == null) {
                mMediaScanConnection = new MediaScannerConnection(this, this);
            }
                mMediaScanConnection.connect();
        }
	}
	

	private void doDeleteOperation() {
		for(String deletePath : mDeletePathList){
			File file = new File(deletePath);
			deleteFile(file);
		}
	}
	
	public  boolean deleteFile(File file) {
        if (file == null || !file.canWrite()) {
            StringUtils.printLog("FileUtils", "DeleteFile: null parameter or no permission.");
            if (file != null) {
                Log.d("com.fineos.fileexplorer.service.FileOperationService", "deleteFile (line 217): file can write :" + file.canWrite());
            }
            return false;
        }
        boolean directory = file.isDirectory() || (file.listFiles() != null);
        if (directory) {
            for (File child : file.listFiles()) {
                if (isNormalFile(child.getAbsolutePath())) {
                	deleteFile(child);
                }
            }
        }
        NotifyFile notifyFile = new NotifyFile();
        notifyFile.file = file;
        notifyFile.operationType = OperationType.DELETE;
        mEventBus.post(notifyFile);
        if(file.exists() && file.canWrite() && file.delete()){
        	Log.d("FileUtils", "successfully deleted");
        	return true;
        }else{
        	//No need to delete(deleted by content resolver) or delete fail.
        	Log.d("FileUtils", "delete fail");
        	return false;
        }
    }

	private  void notifyMediaStoreDataDeleted(File file) {
        Uri uri = MediaStore.Files.getContentUri("external");
        getContentResolver().delete(uri, MediaStore.Files.FileColumns.DATA + "=?", new String[]{file.getAbsolutePath()});
	}

    /**
     *  This method get file uri from file through database.
     *  Currently not used, but this is an useful method.
     * */
	private  Uri getFileURI(File file) {
		if(file == null) return null;
		Uri uri = MediaStore.Files.getContentUri("external");
		String[] projection = {
				MediaStore.Files.FileColumns._ID
		};
		String selection = MediaStore.Files.FileColumns.DATA + "='"
				+ StringUtils.getSqlName(file.getAbsolutePath())+"'";
		ContentResolver contentResolver = getContentResolver();
		Cursor c = contentResolver.query(uri, projection, selection, null, null);
		if(c.moveToNext()){
			Long fileID = c.getLong(c.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID));
			Uri deleteUri = ContentUris.withAppendedId(uri, fileID);
			c.close();
			return deleteUri;
		}else{
			StringUtils.printLog("FileUtils", "no result found in database.");
		}		
		c.close();
		return null;
	}
	


	private void getDeleteData(Intent intent) {
		mDeletePathList = intent.getStringArrayListExtra(DELETE_LIST);
	}


	private void notifyServiceJobDown() {
		Intent intent = new Intent(mTaskCompleteState);
		sendBroadcast(intent);	
	}


	private void doCopyOperation() {
		for(String sourcePath : mCopyPathList){
			File sourceFile = new File(sourcePath);
	//		Log.d("FileOperationService", "source file : " + sourceFile.getPath());
			//File destFile = copyFile(sourceFile, mCopyDestPath);
			int result = copyToDirectory(sourcePath, mCopyDestPath);
			Log.d("FileOperationService", "copy operation result : " + result);
		}
	}


	private void checkFileCopyComplete(File sourceFile, File destFile) {
		boolean taskIsCompleted = true;
		if(sourceFile == null || destFile == null){
			mTaskCompleteState = TASK_NOT_COMPLETED;
			return;
		}
		if(sourceFile.isDirectory()){
			if(sourceFile.listFiles() != null && destFile.listFiles() != null &&
					sourceFile.listFiles().length != destFile.listFiles().length
					&& !destFile.getParent().equals(sourceFile.getPath())){
				taskIsCompleted = false;
			}
		}else{
			Log.d("FileOperationService", "check single file source : " + sourceFile.getPath() + " length : " + sourceFile.length());
			Log.d("FileOperationService", "check single file dest : " + destFile.getPath() + " length : " + destFile.length());
			if(Math.abs(sourceFile.length() - destFile.length()) > 102400){
				taskIsCompleted = false;
			}
		}
		if(taskIsCompleted){
			mTaskCompleteState = TASK_COMPLETED;
		}else{
			mTaskCompleteState = TASK_NOT_COMPLETED;
		}
	}


	private void getCopyData(Intent intent) {
		mCopyPathList = intent.getStringArrayListExtra(COPY_SOURCE_LIST);
		mCopyDestPath = intent.getStringExtra(COPY_DEST_PATH);
	}


	private OperationType getIntentType(Intent intent) {
		String intentType = intent.getStringExtra(INTENT_TYPE);
		for(OperationType type : OperationType.values()){
			if(intentType.equals(type.name())){
				return type;
			}
		}
		return null;
	}
	
	public int copyToDirectory(String sourceFilePath, String destDirPath) {
		Log.d("FileOperationService", "copy from " + sourceFilePath + " to " + destDirPath);
		int copyResult = 0;
		File sourceFile = new File(sourceFilePath);
		File destDir = new File(destDirPath);
		if(!destDirSpaceEnough(sourceFile, destDir)){
			Log.d("FileOperationService", "dest dir space is not enough.");
			copyResult = -1;
			return copyResult;
		}
		byte[] data = new byte[BUFFER];
		int read = 0;
		if(!destDir.isDirectory()){
			Log.d("FileOperationService", "dest dir is not a directory.");
			copyResult = -1;
			return copyResult;
		}
		if(!destDir.canWrite()){
			Log.d("FileOperationService", "dest dir can not write. Currently not support root copy.");
			copyResult = -1;
			return copyResult;
		}
		if (sourceFile.isFile()) {
			String sourceFileName = sourceFile.getName();
			File newFile = new File(destDirPath + File.separator + sourceFileName);
			newFile = getUniqueFile(newFile);
			BufferedOutputStream outputStream = null;
			BufferedInputStream inputStream = null;
			try {
				outputStream = new BufferedOutputStream(
						new FileOutputStream(newFile));
				inputStream = new BufferedInputStream(
						new FileInputStream(sourceFile));
				while ((read = inputStream.read(data, 0, BUFFER)) != -1){
					outputStream.write(data, 0, read);
				}
				outputStream.flush();
				NotifyFile notifyFile = new NotifyFile();
				notifyFile.file = newFile;
				notifyFile.operationType = OperationType.COPY;
				mEventBus.post(notifyFile);
			} catch (FileNotFoundException e) {
				Log.e("FileNotFoundException", e.getMessage());
				copyResult = -1;
				return copyResult;
			} catch (IOException e) {
				//Log.e("IOException", e.getMessage());
				if(destDir.getUsableSpace() <= 0 || !destDirSpaceEnough(sourceFile, destDir)){
					Log.d("FileOperationService", "dest dir available space is not enough");
				}
				copyResult = -1;
				return copyResult;
			}finally{
				try {
					inputStream.close();
					outputStream.close();
				} catch (Exception e) {
					Log.d("FileOperationService", "close stream failed.");
					e.printStackTrace();
				}
			}
		} else if (sourceFile.isDirectory()) {
			String files[] = sourceFile.list();
			String dir =  destDirPath
					+ File.separator + sourceFile.getName();
			int len = files.length;
			File dirFile = new File(dir);
			dirFile = getUniqueFile(dirFile);
			dir = dirFile.getPath();
			if(!dirFile.mkdirs()){
				Log.d("FileOperationService", "make dir failed : " + dir);
				copyResult = -1;
				return copyResult;
			}
			for (int i = 0; i < len; i++){
				if(copyToDirectory(sourceFilePath + "/" + files[i], dir) == -1){
					copyResult = -1;
				}
			}
		}
		return 0;
	}
	
	
	private boolean destDirSpaceEnough(File sourceFile, File destDir) {
		long currentUsableSpace = destDir.getUsableSpace();
		long sourceFileLength = getFileLength(sourceFile);
//		Log.d("FileOperationService", "usable space : " + StringUtils.getProperStorageSizeString(currentUsableSpace));
//		Log.d("FileOperationService", "source file length : " + StringUtils.getProperStorageSizeString(sourceFileLength));
		if(currentUsableSpace > sourceFileLength){
			return true;
		}
		return false;
	}


	private long getFileLength(File sourceFile) {
		long fileLength = 0;
		if(sourceFile.isDirectory()){
			File[] childrenFiles = sourceFile.listFiles();
			if(childrenFiles != null){
				for(File file : childrenFiles){
					fileLength += getFileLength(file);
				}
			}
		}else{
			return fileLength += sourceFile.length();
		}
		return fileLength;
	}


	public File copyFile(File file, String dest) {
		File sourceFile ;
		File destFile;
		//Log.d("FileOperationService", "start copy file : " + file.getPath() + " to " + dest);
		if(file == null || !file.canRead()){
			Log.d("FileOperationService", "file is null or cannot read.");
			return null;
		}
		if(dest == null || dest.isEmpty()){
			Log.d("FileOperationService", "dest is null or empty.");
			return null;
		}
		sourceFile = file;
		String destPath = StringUtils.makePath(dest, sourceFile.getName());
		destFile= new File(destPath);
		destFile = getUniqueFile(destFile);// If directory exists in destination, rename it
		//Log.d("FileOperationService", "dest file is : " + destFile.getPath());
		if (sourceFile.isDirectory()) {
			File[] fileList = sourceFile.listFiles();		
			destFile.mkdirs();
			if(fileList!=null && fileList.length != 0){
				for (File child : fileList) {
    				if (isNormalFile(child.getAbsolutePath())) {
    					copyFile(child, destFile.getPath());
    				}
    			}
            }
		} else {
//			File[] fileArray = new File[]{sourceFile, destFile};
//			mEventBus.post(fileArray);
			copySingleFileUsingIOStream(sourceFile, destFile);
		}
		return destFile;
	}

	@Deprecated
	private String doCopySingleFile(File file, String destPath) {
		try {
			Log.d("FileOperationService", "copy from sourcePath : " + 
						file.getPath() + " to destPath : " + destPath);
			RandomAccessFile sourceFile  = new RandomAccessFile(file.getAbsolutePath(), "r");
			RandomAccessFile destFile = new RandomAccessFile(destPath, "rw");
			byte[] buffer = new byte[1024];
			while (sourceFile.read(buffer) != -1) {
					destFile.write(buffer);
			}
			sourceFile.close();
			destFile.close();
			NotifyFile notifyFile = new NotifyFile();
			notifyFile.file = new File(destPath);
			notifyFile.operationType = OperationType.COPY;
			mEventBus.post(notifyFile);
		} catch (IOException e) {			
			Toast.makeText(this, "some problem accured and paste didn't finish its job.", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		} 
		return null;
	}
	
	private void copySingleFileUsingIOStream(File file, File destFile){
		FileInputStream fi = null;
		FileOutputStream fo = null;
		try {
//			Log.d("FileOperationService", "copy from sourcePath : " + 
//						file.getPath() + " to destPath : " + destPath);
			fi = new FileInputStream(file);
			fo = new FileOutputStream(destFile);
//			FileChannel inChannel = fi.getChannel();
//			FileChannel outChannel = fo.getChannel();
//			outChannel.transferFrom(inChannel, 0, inChannel.size());
			int count = 102400;
			byte[] buffer = new byte[count];
			int read = 0;
			while ((read = fi.read(buffer, 0, count)) != -1) {
				fo.write(buffer, 0, read);
			}
			NotifyFile notifyFile = new NotifyFile();
			notifyFile.file = destFile;
			notifyFile.operationType = OperationType.COPY;
			mEventBus.post(notifyFile);
		} catch (IOException e) {			
			Log.d("FileOperationService", "An IO exception accurred during file copy.");
		} finally{
			try {
                if (fi != null) {
                    fi.close();
                }
                if (fo != null) {
                    fo.close();
                }
            }catch(Exception e){
				e.printStackTrace();
			}
		}
	}


	private File getUniqueFile(File destFile) {
		int i = 1;
		boolean suffixAdded = false;
		while (destFile != null && destFile.exists()) {
			String nextDestPath = destFile.getPath();
			if(!suffixAdded){
				nextDestPath += "_";
				suffixAdded = true;
			}else{
				nextDestPath = nextDestPath.substring(0, nextDestPath.lastIndexOf("_")+1);
			}
			if(destFile.isDirectory()){
				nextDestPath += i++;
			}else{
				String destPath = destFile.getPath();
				if(destPath.contains(".")){
					nextDestPath = destPath.substring(0, destPath.lastIndexOf(".")) + i++
							+ destPath.substring(destPath.lastIndexOf("."));
				}else{
					nextDestPath += i++;
				}
			}
			destFile = new File(nextDestPath);
		}
		return destFile;
	}

	public static boolean isNormalFile(String fullName) {
		return !fullName.equals(ANDROID_SECURE);
	}
	
	@Override
	public void onDestroy() {
		mMediaScanConnection.disconnect();
        try {
            this.unbindService(mMediaScanConnection);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
	}


	@Override
	public void onMediaScannerConnected() {
	}


	@Override
	public void onScanCompleted(String path, Uri uri) {
        mMediaScanConnection.disconnect();
	}
	
	public void onEventAsync(File[] files){
		copySingleFileUsingIOStream(files[0], files[1]);
	}
	
	public void onEventAsync(NotifyFile notifyFile) {
        if (mMediaScanConnection != null && !mMediaScanConnection.isConnected()) {
            mMediaScanConnection.connect();
        }
        switch (notifyFile.operationType) {
            case COPY:
                mMediaScanConnection.scanFile(notifyFile.file.getPath(), null);
                break;
            case DELETE:
                //mMediaScanConnection.scanFile(notifyFile.file.getPath(), null);
                notifyMediaStoreDataDeleted(notifyFile.file);
                break;
            default:
                break;
        }
    }
}

class NotifyFile{
	public File file;
	public OperationType operationType;
}
