package com.fineos.fileexplorer.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.fineos.fileexplorer.R;

import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.Locale;

public class StringUtils {
    private final static String TAG = StringUtils.class.getSimpleName();
	private static final int LIMIT_FILE_NAME_LENGTH = 50;
	private static final int LIMIT_FILE_PATH_LENGTH = 50;
    public static final int ENGLISH = 0;
    public static final int CHINESE = 1;
    public static final String STORAGE_SDCARD0 = "/storage/sdcard0";
    public static final String STORAGE_SDCARD1 = "/storage/sdcard1";
    public static final String STORAGE_USBOTG = "/storage/usbotg";
    public static final String STORAGE_EMULATED_0 = "/storage/emulated/0";


    public static String getProperStorageSizeString(Long size){
		double countInKB = Math.ceil(size / 1024);
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
		DecimalFormat format = 	new DecimalFormat("#.#", symbols);
		if(countInKB > 999){
			double countInMB = countInKB / 1024;
			if(countInMB > 999){
				double countInGb = countInMB / 1024;
				return format.format(countInGb) + "GB";
			}else{
				return format.format(countInMB) + "MB";
			}
		}else{
			return format.format(countInKB) + "KB";
		}
	}
	
	public static String getFileShortName(String longFileName){
		//when file name is too long, it will be regroup as a shorter string:
		int length = longFileName.length();
		if(length < LIMIT_FILE_NAME_LENGTH) return longFileName;
		String shortName = longFileName.substring(0, LIMIT_FILE_NAME_LENGTH-15);
		shortName = shortName + "...";
		shortName = shortName + longFileName.substring(length-15, length);
		return shortName;
	}
	
	public static String getFileShortPath(String longFilePath){
		//when file name is too long, it will be regroup as a shorter string:
		int length = longFilePath.length();
		if(length < LIMIT_FILE_PATH_LENGTH) return longFilePath;
		String shortName = longFilePath.substring(0, LIMIT_FILE_PATH_LENGTH-5);
		shortName = shortName + "...";
		return shortName;
	}
	
	
	/**
	 * A quick method to print log on logcat.
	 * We can setup this encapsulation and config eclipse to automatically write the "classname" part.
	 * 
	 * */
	public static void printLog(String classname, String logContent){
		Log.d(classname, logContent);
	}
	
	/**
	 * This method converts dp unit to equivalent pixels, depending on device density. 
	 * 
	 * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
	 * @param context Context to get resources and device specific display metrics
	 * @return A float value to represent px equivalent to dp depending on device density
	 */
	public static float convertDpToPixel(float dp, Context context){
	    Resources resources = context.getResources();
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    return dp * (metrics.densityDpi / 160f);
	}

	public static CharSequence getDateStringFromLong(long lastModified, Context mContext) {
        if(get24HourMode(mContext)){
           return DateFormat.format("yyyy-MM-dd HH:mm", lastModified);
        }
		return DateFormat.format("yyyy-MM-dd hh:mm a", lastModified);
	}

    /**
     * @return true if clock is set to 24-hour mode
     */
    public static boolean get24HourMode(final Context context) {
        return android.text.format.DateFormat.is24HourFormat(context);
    }
	
	public static String makePath(String path1, String path2) {
        if (path1.endsWith(File.separator))
            return path1 + path2;

        return path1 + File.separator + path2;
    }
	
	public static String getNameFromFilename(String filename) {
        int dotPosition = filename.lastIndexOf('.');
        return filename.substring(0, dotPosition);
    }
	
	public static String getPathFromFilepath(String filepath) {
        int pos = filepath.lastIndexOf('/');
        if (pos != -1) {
            return filepath.substring(0, pos);
        }
        return "";
    }

    public static String getNameFromFilepath(String filepath) {
        int pos = filepath.lastIndexOf('/');
        if (pos != -1) {
            return filepath.substring(pos + 1);
        }
        return "";
    }
    
    public static String getExtFromFilename(String filename) {
        int dotPosition = filename.lastIndexOf('.');
        return filename.substring(dotPosition, filename.length());
    }

	public static String getParentPath(String currentDirectoryPath, String rootPath) {
		String parentPath = "";
		if(currentDirectoryPath.equals(rootPath) || 
				currentDirectoryPath.length() < rootPath.length()){
			return parentPath;
		}else{
			parentPath = currentDirectoryPath.substring(0,
					currentDirectoryPath.lastIndexOf("/"));
		}
		return parentPath;
	}
	
    public static String formatDateString(Context context, long time) {
        java.text.DateFormat dateFormat = DateFormat .getDateFormat(context);
        java.text.DateFormat timeFormat = android.text.format.DateFormat
                .getTimeFormat(context);
        Date date = new Date(time);
        return dateFormat.format(date) + " " + timeFormat.format(date);
    }

	public static boolean isVaildFileName(String fileName) {
        if (fileName == null || fileName.length() > 255){
            return false;
        }else if (containsIllegalCharacters(fileName)) {
            return false;
        } else if (fileName.length() == 1 && !fileName.equals("/") && !fileName.equals(".")) {
            return true;
        }
		return fileName.matches("[^\\s\\\\/:\\*\\?\\\"<>\\|](\\x20|[^\\s\\\\/:\\*\\?\\\"<>\\|])*[^\\\\/:\\*\\?\\\"<>\\|\\.]$");
	}

	public static String getSqlName(String filename) {
		int length = filename.length();
		if(filename.contains("'")){
			for(int i = filename.indexOf("'"); i < length; ++i){
				if(filename.charAt(i) == '\''){
					filename = filename.substring(0, i) + "'" + filename.substring(i);
					length++;
					i++;
				}
			}
		}
		return filename;		
	}


    public static boolean containsIllegalCharacters(String displayName)
    {// See if displayName contains some illegal chars, e.g. emotion characters.
        final int nameLength = displayName.length();

        for (int i = 0; i < nameLength; i++)
        {
            final char hs = displayName.charAt(i);

            if (0xd800 <= hs && hs <= 0xdbff)
            {
                final char ls = displayName.charAt(i + 1);
                final int uc = ((hs - 0xd800) * 0x400) + (ls - 0xdc00) + 0x10000;

                if (0x1d000 <= uc && uc <= 0x1f77f)
                {
                    return true;
                }
            }
            else if (Character.isHighSurrogate(hs))
            {
                final char ls = displayName.charAt(i + 1);

                if (ls == 0x20e3)
                {
                    return true;
                }
            }
            else
            {
                // non surrogate
                if (0x2100 <= hs && hs <= 0x27ff)
                {
                    return true;
                }
                else if (0x2B05 <= hs && hs <= 0x2b07)
                {
                    return true;
                }
                else if (0x2934 <= hs && hs <= 0x2935)
                {
                    return true;
                }
                else if (0x3297 <= hs && hs <= 0x3299)
                {
                    return true;
                }
                else if (hs == 0xa9 || hs == 0xae || hs == 0x303d || hs == 0x3030 || hs == 0x2b55 || hs == 0x2b1c || hs == 0x2b1b || hs == 0x2b50)
                {
                    return true;
                }
            }
        }

        return false;
    }

    public static void makeToast(Context mContext, String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

    public static int getLanguage() {
        String language = Locale.getDefault().getLanguage();
        if(language == null) return -1;
        if (language.contains("en")) {
            return ENGLISH;
        }
        if (language.contains("ch")) {
            return CHINESE;
        }
        return -1;
    }

    public static String getProperPathString(String filePath, Context context) {
        if(filePath == null || filePath.isEmpty())return filePath;
        String phoneStorageName = context.getString(R.string.inner_storage_name);
        String extraSDCardName = context.getString(R.string.sd_card_name);
        String resultString = "";
        boolean sdCardIsMainStorage = Environment.isExternalStorageRemovable();
        if (filePath.toLowerCase().startsWith("/storage/usbotg/")) {
            resultString = filePath.replace(STORAGE_USBOTG, "USB");
        }else {
            if (sdCardIsMainStorage) {
                if(filePath.startsWith(STORAGE_SDCARD0))resultString = filePath.replace(STORAGE_SDCARD0, extraSDCardName);
                if(filePath.startsWith(STORAGE_SDCARD1))resultString = filePath.replace(STORAGE_SDCARD1, phoneStorageName);
            }else{
                if(filePath.startsWith(STORAGE_SDCARD0))resultString = filePath.replace(STORAGE_SDCARD0, phoneStorageName);
                if(filePath.startsWith(STORAGE_SDCARD1))resultString = filePath.replace(STORAGE_SDCARD1, extraSDCardName);
            }
            if(filePath.startsWith(STORAGE_EMULATED_0))resultString = filePath.replace(STORAGE_EMULATED_0, phoneStorageName);
        }
        if (resultString.isEmpty()) {
            return filePath;
        }
        return resultString;
    }

}
