package com.fineos.fileexplorer.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class FileUtils {

	private static final String ANDROID_SECURE = "/mnt/sdcard/.android_secure";
	private static String destination;
    public static final String ROOT_PATH = "/";


	public static void copyFile(File file, String dest, Context context) {
		if (file == null || dest == null) {
			StringUtils.printLog("FileUtils", "CopyFile: null parameter");
			return;
		}
		if (file.isDirectory()) {
			// directory exists in destination, rename it
			String destPath = StringUtils.makePath(dest, file.getName());
			File destFile = new File(destPath);
			int i = 1;
			while (destFile != null && destFile.exists()) {
				destPath = StringUtils.makePath(dest, file.getName() + " "
						+ i++);
				destFile = new File(destPath);
			}
			File[] fileList = file.listFiles();		
			if(fileList!=null && fileList.length != 0){
				for (File child : fileList) {
    				if (isNormalFile(child.getAbsolutePath())) {
    					copyFile(child, destPath, context);
    				}
    			}
            }
        	destFile.mkdirs();
		} else {
			try {
				String destFile = doCopyFile(file.getPath(), dest, context);
			} catch (Exception e) {
				Toast.makeText(context, "some paste failed. ", Toast.LENGTH_SHORT).show();
			}
		}
//		StringUtils.printLog("FileUtils", "CopyFile >>> " + file.getPath()
//				+ "," + dest);
	}

	  private boolean isFileStillAvailableForCopy(File file, String destDirectory) {
			File currentPath = new File(destDirectory);
			if(file.canRead() && currentPath.canWrite()){
				return true;
			}
			return false;
	}
	  
	public static boolean isNormalFile(String fullName) {
		return !fullName.equals(ANDROID_SECURE);
	}

	private static String doCopyFile(String src, String dest, Context context){
		File file = new File(src);
		if (file!= null && !file.exists() || file.isDirectory()) {
			StringUtils.printLog("FileUtils",
					"copyFile: file not exist or is directory, " + src);
			return null;
		}
		FileInputStream fi = null;
		FileOutputStream fo = null;
		try {
			fi = new FileInputStream(file);
			File destPlace = new File(dest);
			if (destPlace!= null && !destPlace.exists()) {
				if (!destPlace.mkdirs())
					return null;
			}
			String destPath = getUnOverlappedFilePath(file, dest);
			File destFile = new File(destPath);

			if (!destFile.createNewFile())
				return null;

			fo = new FileOutputStream(destFile);
			int count = 102400;
			byte[] buffer = new byte[count];
			int read = 0;
			while ((read = fi.read(buffer, 0, count)) != -1) {
				fo.write(buffer, 0, read);
			}
			// TODO: set access privilege
			//let the MediaStore know there is a new file added to dest directory.			
			//MediaScannerConnection.scanFile(context, new String[]{destFile.getPath()}, null, null);
			return destFile.getPath();
		} catch (FileNotFoundException e) {
			StringUtils.printLog("FileUtils", "copyFile: file not found! "
					+ src);
			e.printStackTrace();
		} catch (IOException e) {
			StringUtils.printLog("FileUtils", "copyFile: " + e.toString());
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
		} finally {
			try {
				if (fi != null)
					fi.close();
				if (fo != null)
					fo.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
    public static String getUnOverlappedFilePath(File file, String dest) {
		String destPath = StringUtils.makePath(dest, file.getName());
    	File destFile = new File(destPath);
    	String destName = "";
		int i = 1;
		while (destFile.exists()) {
			if(destFile.isDirectory()){
				destName = file.getName() + " " + i++;
			}else{
				destName = StringUtils.getNameFromFilename(file
						.getName())
						+ " "
						+ i++
						+ "."
						+ StringUtils.getExtFromFilename(file.getName());
			}
			destPath = StringUtils.makePath(dest, destName);
			destFile = new File(destPath);
		}
		return destPath;
	}

	public static boolean deleteFile(File file,Context context) {
        if (file == null || !file.canWrite()) {
            StringUtils.printLog("FileUtils", "DeleteFile: null parameter or no permission.");
            return false;
        } 
        boolean directory = file.isDirectory();
        if (directory) {
            for (File child : file.listFiles()) {
                if (isNormalFile(child.getAbsolutePath())) {
                	deleteFile(child, context);
                }
            }
        }
        notifyMediaStoreDataDeleted(file, context);
        //context.getContentResolver().delete(Uri.fromFile(file), null, null);
        if(file.delete()){
        	Log.d("FileUtils", "successfully deleted");
        	return true;
        }else{
        	Log.d("FileUtils", "delete fail");
        	return false;
        }
        //StringUtils.printLog("FileUtils", "DeleteFile >>> " + file.getPath());
    }

	private static void notifyMediaStoreDataDeleted(File file, Context context) {
		Uri deleteURI = getFileURI(file, context);
		if(deleteURI == null){
			StringUtils.printLog("FileUtils", "file uri is null!");
			return;
		}
		context.getContentResolver().delete(deleteURI, 
				null, null);	
	}

	private static Uri getFileURI(File file, Context context) {
		if(file == null || context == null) return null;
		Uri uri = MediaStore.Files.getContentUri("external");
		String[] projection = {
				MediaStore.Files.FileColumns._ID
		};
		String selection = MediaStore.Files.FileColumns.DATA + "='"
				+ StringUtils.getSqlName(file.getAbsolutePath())+"'";
		ContentResolver contentResolver = context.getContentResolver();
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
	
	public static boolean moveFile(File file, String dest, Context context) {
        StringUtils.printLog("FileUtils", "MoveFile >>> " + file.getPath() + "," + dest);

        if (file == null || dest == null) {
            StringUtils.printLog("FileUtils", "CopyFile: null parameter");
            return false;
        }
    	//If destination is the source directory, then we do not need to move.
        if(file.getPath().equals(dest + File.separator + file.getName())){
        	return true;
        }
        
        String newPath = getUnOverlappedFilePath(file, dest); // check if there is overlapped file.
        try {
        	File newFile = new File(newPath);
        	ArrayList<File> deleteFileList = new ArrayList<File>();
        	buildDeleteFileList(file, deleteFileList);
        	boolean result = file.renameTo(newFile);
        	notifyMediaStoreListDeleted(deleteFileList, context);
        	notifyMediaStoreDataMoved(newFile, context, null);
        	StringUtils.printLog("FileUtils", "file path : " + file.getPath());
            return result;
        } catch (SecurityException e) {
           StringUtils.printLog("FileUtils", "Fail to move file," + e.toString());
        }
        return false;
    }

	private static ArrayList<File> buildDeleteFileList(File file, ArrayList<File> deleteList) {
		if(file.isDirectory()){
			for(File child : file.listFiles()){
				buildDeleteFileList(child, deleteList);
			}
		}
		deleteList.add(file);
		return deleteList;
	}

	private static void notifyMediaStoreListDeleted(ArrayList<File> deleteList,
			Context context) {
		for(File file : deleteList){
			notifyMediaStoreDataDeleted(file, context);
		}
	}
	

	private static void notifyMediaStoreDataMoved(File newFile,
                                                  Context context, String mimeType) {
		if(newFile.isDirectory()){
            File[] fileList = newFile.listFiles();
            ArrayList<String> pathList = new ArrayList<String>();
            if (fileList != null && fileList.length > 0) {
                int length = fileList.length;
                for (int i = 0; i < length; i++) {
                    File file = fileList[i];
                    if (file.exists() && file.isDirectory()) {
                        notifyMediaStoreDataMoved(file, context, MimeUtils.getMimeType(file));
                    }else{
                        pathList.add(file.getAbsolutePath());
                    }
                }
                String[] pathArray = new String[pathList.size()];
                pathArray = (String[]) pathList.toArray(pathArray);
                MediaScannerConnection.scanFile(context, pathArray, null, null);
            }
		}
        //TODO: Remove dupulate scan directories.
        MediaScannerConnection.scanFile(context,new String[]{newFile.getAbsolutePath()}, new String[]{mimeType}, null);
	}

	// if path1 contains path2
    public static boolean containsPath(String path1, String path2) {
        String path = path2;
        while (path != null) {
            if (path.equalsIgnoreCase(path1))
                return true;

            if (path.equals(ROOT_PATH))
                break;
            path = new File(path).getParent();
        }

        return false;
    }

    public static boolean CreateFolder(String path, String name, Context context) {
        StringUtils.printLog("FileUtils", "CreateFolder >>> " + path + "," + name);

        File f = new File(StringUtils.makePath(path, name));
        if (f.exists())
            return false;
        boolean result = f.mkdir();
        MediaScannerConnection.scanFile(context, new String[]{f.getPath()}, null, null);
        return result;
    }

	public static File renameFile(File file, String text, Context context) {
		File newFile = new File(file.getParent() + File.separator + text);
        String mimeType = MimeUtils.getMimeType(file);
//		StringUtils.printLog("FileUtils", "new path : " + newFile.getPath());
		boolean result = file.renameTo(newFile);
		//notifyMediaStoreDataDeleted(file, context);
		//notifyMediaStoreDataMoved(newFile, context, mimeType);
        return  newFile;
	}

	public static File getUniqueNewFile(File sourceFile, File destDir) {
		File newFile = new File(StringUtils.makePath(destDir.getAbsolutePath(), sourceFile.getName()));
		int suffixIndex = 1;
		while (newFile.exists()) {
			if (newFile.getName().contains(".")) {
				String newFileName = StringUtils.getNameFromFilename(sourceFile.getName()) + suffixIndex + StringUtils.getExtFromFilename(sourceFile.getName());
				newFile = new File(StringUtils.makePath(destDir.getAbsolutePath(), newFileName));
			}else {
				newFile = new File(StringUtils.makePath(destDir.getAbsolutePath(), sourceFile.getName() + suffixIndex));
			}
			suffixIndex++;
		}
		return newFile;
	}

	public static File checkFileExistAndCanWrite(File file) {
		if (!file.exists() || !file.canWrite()) {
			throw new RuntimeException("file : " + file.getAbsolutePath() + " is not valid. exist : " + file.exists() + " can write : " + file.canWrite());
		}
		return file;
	}

	public static File checkFileExistAndCanRead(File file) {
		if (!file.exists() || !file.canRead()) {
			throw new RuntimeException("file : " + file.getAbsolutePath() + " is not valid. exist : " + file.exists() + " can read : " + file.canRead());
		}
		return file;
	}

	public static void checkDestDirHasEnoughSpace(File sourceFile, File destDir) {
		long needSpace = sourceFile.length();
		long freeSpace = destDir.getFreeSpace();
		if (freeSpace < needSpace) {

		}
	}

	public static void checkSpaceNotZero(File file) {
		if (file.getFreeSpace() <= 0) {
			throw new RuntimeException("file space under zero : " + file.getAbsolutePath());
		}
	}
}
