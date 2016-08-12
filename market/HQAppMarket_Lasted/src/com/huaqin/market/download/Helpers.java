package com.huaqin.market.download;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemClock;
import android.provider.BaseColumns;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File; 
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.huaqin.market.utils.FileManager;

/**
 * Some helper functions for the download manager
 */
public class Helpers {

    public static Random sRandom = new Random(SystemClock.uptimeMillis());

    /** Regex used to parse content-disposition headers */
    private static final Pattern CONTENT_DISPOSITION_PATTERN =
            Pattern.compile("attachment;\\s*filename\\s*=\\s*\"([^\"]*)\"");

    private Helpers() {
    }

    /*
     * Parse the Content-Disposition HTTP Header. The format of the header
     * is defined here: http://www.w3.org/Protocols/rfc2616/rfc2616-sec19.html
     * This header provides a filename for content that is going to be
     * downloaded to the file system. We only support the attachment type.
     */
    private static String parseContentDisposition(String contentDisposition) {
        try {
            Matcher m = CONTENT_DISPOSITION_PATTERN.matcher(contentDisposition);
            if (m.find()) {
                return m.group(1);
            }
        } catch (IllegalStateException ex) {
             // This function is defined as returning null when it can't parse the header
        }
        return null;
    }
    
    public static boolean isDeviceExist() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * Creates a filename (where the file should be saved) from a uri.
     */
    public static DownloadFileInfo generateSaveFile(
            Context context,
            String url,
            String hint,
            String contentDisposition,
            String contentLocation,
            String mimeType,
            int destination,
            int contentLength) throws FileNotFoundException {

        /*
         * Don't download files that we won't be able to handle
         */
        if (destination == Downloads.DESTINATION_EXTERNAL
                || destination == Downloads.DESTINATION_CACHE_PARTITION_PURGEABLE) {
            if (mimeType == null) {
                return new DownloadFileInfo(null, null, Downloads.STATUS_NOT_ACCEPTABLE);
            }
        }
        String filename = chooseFilename(
                url, hint, contentDisposition, contentLocation, destination);

        // Split filename between base and extension
        // Add an extension if filename does not have one
        String extension = null;
        int dotIndex = filename.indexOf('.');
        if (dotIndex < 0) {
            extension = chooseExtensionFromMimeType(mimeType, true);
        } else {
            extension = chooseExtensionFromFilename(
                    mimeType, destination, filename, dotIndex);
            filename = filename.substring(0, dotIndex);
        }

        /*
         *  Locate the directory where the file will be saved
         */
        File base = null;
        StatFs stat = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // Saving to external storage (SD card).
            String root = Environment.getExternalStorageDirectory().getPath();
            stat = new StatFs(root);

            /*
             * Check whether there's enough space on the target filesystem to save the file.
             * Put a bit of margin (in case creating the file grows the system by a few blocks).
             */
            if (stat.getBlockSize() * ((long) stat.getAvailableBlocks() - 4) < contentLength) {
                // Insufficient space.
                Log.e(Constants.TAG, "download aborted - not enough free space");
                return new DownloadFileInfo(null, null,
                        Downloads.STATUS_INSUFFICIENT_SPACE_ERROR);
            }

            base = new File(FileManager.APK_DIR_PATH);
            if (!base.isDirectory() && !base.mkdir()) {
                // Can't create download directory, e.g. because a file called "download"
                // already exists at the root level, or the SD card filesystem is read-only.
                return new DownloadFileInfo(null, null, Downloads.STATUS_FILE_ERROR);
            }
        } else {
            // No SD card found.
            return new DownloadFileInfo(null, null,
                    Downloads.STATUS_DEVICE_NOT_FOUND_ERROR);
        }

        boolean recoveryDir = Constants.RECOVERY_DIRECTORY.equalsIgnoreCase(filename + extension);

        filename = base.getPath() + File.separator + filename;

        /*
         * Generate a unique filename, create the file, return it.
         */
        String fullFilename = chooseUniqueFilename(
                destination, filename, extension, recoveryDir);
        if (fullFilename != null) {
            return new DownloadFileInfo(fullFilename, new FileOutputStream(fullFilename), 0);
        } else {
            return new DownloadFileInfo(null, null, Downloads.STATUS_FILE_ERROR);
        }
    }

    private static String chooseFilename(String url, String hint, String contentDisposition,
            String contentLocation, int destination) {
        String filename = null;

        // First, try to use the hint from the application, if there's one
        if (filename == null && hint != null && !hint.endsWith("/")) {
            int index = hint.lastIndexOf('/') + 1;
            if (index > 0) {
                filename = hint.substring(index);
            } else {
                filename = hint;
            }
        }

        // If we couldn't do anything with the hint, move toward the content disposition
        if (filename == null && contentDisposition != null) {
            filename = parseContentDisposition(contentDisposition);
            if (filename != null) {
                int index = filename.lastIndexOf('/') + 1;
                if (index > 0) {
                    filename = filename.substring(index);
                }
            }
        }

        // If we still have nothing at this point, try the content location
        if (filename == null && contentLocation != null) {
            String decodedContentLocation = Uri.decode(contentLocation);
            if (decodedContentLocation != null
                    && !decodedContentLocation.endsWith("/")
                    && decodedContentLocation.indexOf('?') < 0) {
                int index = decodedContentLocation.lastIndexOf('/') + 1;
                if (index > 0) {
                    filename = decodedContentLocation.substring(index);
                } else {
                    filename = decodedContentLocation;
                }
            }
        }

        // If all the other http-related approaches failed, use the plain uri
        if (filename == null) {
            String decodedUrl = Uri.decode(url);
            if (decodedUrl != null
                    && !decodedUrl.endsWith("/") && decodedUrl.indexOf('?') < 0) {
                int index = decodedUrl.lastIndexOf('/') + 1;
                if (index > 0) {
                    filename = decodedUrl.substring(index);
                }
            }
        }

        // Finally, if couldn't get filename from URI, get a generic filename
        if (filename == null) {
            filename = Constants.DEFAULT_DL_FILENAME;
        }

        filename = filename.replaceAll("[^a-zA-Z0-9\\.\\-_]+", "_");

        return filename;
    }

    private static String chooseExtensionFromMimeType(String mimeType, boolean useDefaults) {
        String extension = null;
        if (mimeType != null) {
            extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
            if (extension != null) {
                extension = "." + extension;
            }
        }
        if (extension == null) {
            if (mimeType != null && mimeType.toLowerCase().startsWith("text/")) {
                if (mimeType.equalsIgnoreCase("text/html")) {
                    extension = Constants.DEFAULT_DL_HTML_EXTENSION;
                } else if (useDefaults) {
                    extension = Constants.DEFAULT_DL_TEXT_EXTENSION;
                }
            } else if (useDefaults) {
//                extension = Constants.DEFAULT_DL_BINARY_EXTENSION;
                extension = Constants.DEFAULT_DL_APK_EXTENSION;
            }
        }
        return extension;
    }

    private static String chooseExtensionFromFilename(String mimeType, int destination,
            String filename, int dotIndex) {
        String extension = null;
        if (mimeType != null) {
            // Compare the last segment of the extension against the mime type.
            // If there's a mismatch, discard the entire extension.
            int lastDotIndex = filename.lastIndexOf('.');
            String typeFromExt = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    filename.substring(lastDotIndex + 1));
            if (typeFromExt == null || !typeFromExt.equalsIgnoreCase(mimeType)) {
                extension = chooseExtensionFromMimeType(mimeType, false);
            }
        }
        if (extension == null) {
            extension = filename.substring(dotIndex);
        }
        return extension;
    }

    private static String chooseUniqueFilename(int destination, String filename,
            String extension, boolean recoveryDir, long filesize) {
        
        String fullFilename = filename + extension;
        File file = new File(fullFilename);
        if (!file.exists()
                && (!recoveryDir ||
                (destination != Downloads.DESTINATION_CACHE_PARTITION &&
                        destination != Downloads.DESTINATION_CACHE_PARTITION_PURGEABLE &&
                        destination != Downloads.DESTINATION_CACHE_PARTITION_NOROAMING))) {
            return fullFilename;
        }
        
        if(file.exists() && file.length() == filesize) {
            return fullFilename;
        }
        
        filename = filename + Constants.FILENAME_SEQUENCE_SEPARATOR;
        /*
        * This number is used to generate partially randomized filenames to avoid
        * collisions.
        * It starts at 1.
        * The next 9 iterations increment it by 1 at a time (up to 10).
        * The next 9 iterations increment it by 1 to 10 (random) at a time.
        * The next 9 iterations increment it by 1 to 100 (random) at a time.
        * ... Up to the point where it increases by 100000000 at a time.
        * (the maximum value that can be reached is 1000000000)
        * As soon as a number is reached that generates a filename that doesn't exist,
        *     that filename is used.
        * If the filename coming in is [base].[ext], the generated filenames are
        *     [base]-[sequence].[ext].
        */
        int sequence = 1;
        for (int magnitude = 1; magnitude < 1000000000; magnitude *= 10) {
            for (int iteration = 0; iteration < 9; ++iteration) {
                fullFilename = filename + sequence + extension;
                if (!new File(fullFilename).exists()) {
                    return fullFilename;
                }
                sequence += sRandom.nextInt(magnitude) + 1;
            }
        }
        return null;
    }
    
    private static String chooseUniqueFilename(int destination, String filename,
            String extension, boolean recoveryDir) {
        String fullFilename = filename + extension;
        if (!new File(fullFilename).exists()
                && (!recoveryDir ||
                (destination != Downloads.DESTINATION_CACHE_PARTITION &&
                        destination != Downloads.DESTINATION_CACHE_PARTITION_PURGEABLE &&
                        destination != Downloads.DESTINATION_CACHE_PARTITION_NOROAMING))) {
            return fullFilename;
        }
        filename = filename + Constants.FILENAME_SEQUENCE_SEPARATOR;
        /*
        * This number is used to generate partially randomized filenames to avoid
        * collisions.
        * It starts at 1.
        * The next 9 iterations increment it by 1 at a time (up to 10).
        * The next 9 iterations increment it by 1 to 10 (random) at a time.
        * The next 9 iterations increment it by 1 to 100 (random) at a time.
        * ... Up to the point where it increases by 100000000 at a time.
        * (the maximum value that can be reached is 1000000000)
        * As soon as a number is reached that generates a filename that doesn't exist,
        *     that filename is used.
        * If the filename coming in is [base].[ext], the generated filenames are
        *     [base]-[sequence].[ext].
        */
        int sequence = 1;
        for (int magnitude = 1; magnitude < 1000000000; magnitude *= 10) {
            for (int iteration = 0; iteration < 9; ++iteration) {
                fullFilename = filename + sequence + extension;
                if (!new File(fullFilename).exists()) {
                    return fullFilename;
                }
                sequence += sRandom.nextInt(magnitude) + 1;
            }
        }
        return null;
    }

    /**
     * Deletes purgeable files from the cache partition. This also deletes
     * the matching database entries. Files are deleted in LRU order until
     * the total byte size is greater than targetBytes.
     */
    public static final boolean discardPurgeableFiles(Context context, long targetBytes) {
        Cursor cursor = context.getContentResolver().query(
                Downloads.CONTENT_URI,
                null,
                "( " +
                Downloads.COLUMN_STATUS + " = '" + Downloads.STATUS_SUCCESS + "' AND " +
                Downloads.COLUMN_DESTINATION +
                        " = '" + Downloads.DESTINATION_CACHE_PARTITION_PURGEABLE + "' )",
                null,
                Downloads.COLUMN_LAST_MODIFICATION);
        if (cursor == null) {
            return false;
        }
        long totalFreed = 0;
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast() && totalFreed < targetBytes) {
                File file = new File(cursor.getString(cursor.getColumnIndex(Downloads._DATA)));

                totalFreed += file.length();
                file.delete();
                long id = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
                context.getContentResolver().delete(
                        ContentUris.withAppendedId(Downloads.CONTENT_URI, id), null, null);
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        return totalFreed > 0;
    }

    /**
     * Returns whether the network is available
     */
//    public static boolean isNetworkAvailable(Context context) {
//        ConnectivityManager connectivity =
//                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//        if (connectivity == null) {
//            Log.w(Constants.TAG, "couldn't get connectivity manager");
//        } else {
//            NetworkInfo[] info = connectivity.getAllNetworkInfo();
//            if (info != null) {
//                for (int i = 0; i < info.length; i++) {
//                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
//                        return true;
//                    }
//                }
//            }
//        }
//        return false;
//    }
    /***********Added-s by JimmyJin**********/
    public static boolean isNetworkAvailable(Context context) {  	          
    	try {  
    		ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);  
    		if (connectivity == null) {
//    			Log.v(Constants.TAG, "JimmyJin couldn't get connectivity manager");
    			return false;     
    		} else{     	              
    			NetworkInfo info = connectivity.getActiveNetworkInfo(); 
//    			Log.v(Constants.TAG, "JimmyJin info="+info);
    			if (info != null && info.isConnected()) {     	                  
    				if (info.getState() == NetworkInfo.State.CONNECTED) {  
    					return true;  
    				}  
    			}  
    		}  
    	} catch (Exception e) {  
    		return false;  
    	}  
    	return false;  
    }  
    /***********Added-e by JimmyJin**********/    

    /**
     * Returns whether the network is roaming
     */
    public static boolean isNetworkRoaming(Context context) {
        ConnectivityManager connectivity =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            Log.w(Constants.TAG, "couldn't get connectivity manager");
        } else {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.getType() == ConnectivityManager.TYPE_MOBILE) {
                if (((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE))
                		.isNetworkRoaming()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks whether the filename looks legitimate
     */
    public static boolean isFilenameValid(String filename) {
        File dir = new File(filename).getParentFile();
        return dir.equals(Environment.getDownloadCacheDirectory())
                || dir.equals(new File(Environment.getExternalStorageDirectory()
                        + Constants.DEFAULT_DL_SUBDIR));
    }

    /**
     * Checks whether this looks like a legitimate selection parameter
     */
    public static void validateSelection(String selection, Set<String> allowedColumns) {
        try {
            if (selection == null) {
                return;
            }
            Lexer lexer = new Lexer(selection, allowedColumns);
            parseExpression(lexer);
            if (lexer.currentToken() != Lexer.TOKEN_END) {
                throw new IllegalArgumentException("syntax error");
            }
        } catch (RuntimeException ex) {
            throw ex;
        }

    }

    // expression <- ( expression ) | statement [AND_OR ( expression ) | statement] *
    //             | statement [AND_OR expression]*
    private static void parseExpression(Lexer lexer) {
        for (;;) {
            // ( expression )
            if (lexer.currentToken() == Lexer.TOKEN_OPEN_PAREN) {
                lexer.advance();
                parseExpression(lexer);
                if (lexer.currentToken() != Lexer.TOKEN_CLOSE_PAREN) {
                    throw new IllegalArgumentException("syntax error, unmatched parenthese");
                }
                lexer.advance();
            } else {
                // statement
                parseStatement(lexer);
            }
            if (lexer.currentToken() != Lexer.TOKEN_AND_OR) {
                break;
            }
            lexer.advance();
        }
    }

    // statement <- COLUMN COMPARE VALUE
    //            | COLUMN IS NULL
    private static void parseStatement(Lexer lexer) {
        // both possibilities start with COLUMN
        if (lexer.currentToken() != Lexer.TOKEN_COLUMN) {
            throw new IllegalArgumentException("syntax error, expected column name");
        }
        lexer.advance();

        // statement <- COLUMN COMPARE VALUE
        if (lexer.currentToken() == Lexer.TOKEN_COMPARE) {
            lexer.advance();
            if (lexer.currentToken() != Lexer.TOKEN_VALUE) {
                throw new IllegalArgumentException("syntax error, expected quoted string");
            }
            lexer.advance();
            return;
        }

        // statement <- COLUMN IS NULL
        if (lexer.currentToken() == Lexer.TOKEN_IS) {
            lexer.advance();
            if (lexer.currentToken() != Lexer.TOKEN_NULL) {
                throw new IllegalArgumentException("syntax error, expected NULL");
            }
            lexer.advance();
            return;
        }

        // didn't get anything good after COLUMN
        throw new IllegalArgumentException("syntax error after column name");
    }

    /**
     * A simple lexer that recognizes the words of our restricted subset of SQL where clauses
     */
    private static class Lexer {
        public static final int TOKEN_START = 0;
        public static final int TOKEN_OPEN_PAREN = 1;
        public static final int TOKEN_CLOSE_PAREN = 2;
        public static final int TOKEN_AND_OR = 3;
        public static final int TOKEN_COLUMN = 4;
        public static final int TOKEN_COMPARE = 5;
        public static final int TOKEN_VALUE = 6;
        public static final int TOKEN_IS = 7;
        public static final int TOKEN_NULL = 8;
        public static final int TOKEN_END = 9;

        private final String mSelection;
        private final Set<String> mAllowedColumns;
        private int mOffset = 0;
        private int mCurrentToken = TOKEN_START;
        private final char[] mChars;

        public Lexer(String selection, Set<String> allowedColumns) {
            mSelection = selection;
            mAllowedColumns = allowedColumns;
            mChars = new char[mSelection.length()];
            mSelection.getChars(0, mChars.length, mChars, 0);
            advance();
        }

        public int currentToken() {
            return mCurrentToken;
        }

        public void advance() {
            char[] chars = mChars;

            // consume whitespace
            while (mOffset < chars.length && chars[mOffset] == ' ') {
                ++mOffset;
            }

            // end of input
            if (mOffset == chars.length) {
                mCurrentToken = TOKEN_END;
                return;
            }

            // "("
            if (chars[mOffset] == '(') {
                ++mOffset;
                mCurrentToken = TOKEN_OPEN_PAREN;
                return;
            }

            // ")"
            if (chars[mOffset] == ')') {
                ++mOffset;
                mCurrentToken = TOKEN_CLOSE_PAREN;
                return;
            }

            // "?"
            if (chars[mOffset] == '?') {
                ++mOffset;
                mCurrentToken = TOKEN_VALUE;
                return;
            }

            // "=" and "=="
            if (chars[mOffset] == '=') {
                ++mOffset;
                mCurrentToken = TOKEN_COMPARE;
                if (mOffset < chars.length && chars[mOffset] == '=') {
                    ++mOffset;
                }
                return;
            }

            // ">" and ">="
            if (chars[mOffset] == '>') {
                ++mOffset;
                mCurrentToken = TOKEN_COMPARE;
                if (mOffset < chars.length && chars[mOffset] == '=') {
                    ++mOffset;
                }
                return;
            }

            // "<", "<=" and "<>"
            if (chars[mOffset] == '<') {
                ++mOffset;
                mCurrentToken = TOKEN_COMPARE;
                if (mOffset < chars.length && (chars[mOffset] == '=' || chars[mOffset] == '>')) {
                    ++mOffset;
                }
                return;
            }

            // "!="
            if (chars[mOffset] == '!') {
                ++mOffset;
                mCurrentToken = TOKEN_COMPARE;
                if (mOffset < chars.length && chars[mOffset] == '=') {
                    ++mOffset;
                    return;
                }
                throw new IllegalArgumentException("Unexpected character after !");
            }

            // columns and keywords
            // first look for anything that looks like an identifier or a keyword
            //     and then recognize the individual words.
            // no attempt is made at discarding sequences of underscores with no alphanumeric
            //     characters, even though it's not clear that they'd be legal column names.
            if (isIdentifierStart(chars[mOffset])) {
                int startOffset = mOffset;
                ++mOffset;
                while (mOffset < chars.length && isIdentifierChar(chars[mOffset])) {
                    ++mOffset;
                }
                String word = mSelection.substring(startOffset, mOffset);
                if (mOffset - startOffset <= 4) {
                    if (word.equals("IS")) {
                        mCurrentToken = TOKEN_IS;
                        return;
                    }
                    if (word.equals("OR") || word.equals("AND")) {
                        mCurrentToken = TOKEN_AND_OR;
                        return;
                    }
                    if (word.equals("NULL")) {
                        mCurrentToken = TOKEN_NULL;
                        return;
                    }
                }
                if (mAllowedColumns.contains(word)) {
                    mCurrentToken = TOKEN_COLUMN;
                    return;
                }
                throw new IllegalArgumentException("unrecognized column or keyword");
            }

            // quoted strings
            if (chars[mOffset] == '\'') {
                ++mOffset;
                while (mOffset < chars.length) {
                    if (chars[mOffset] == '\'') {
                        if (mOffset + 1 < chars.length && chars[mOffset + 1] == '\'') {
                            ++mOffset;
                        } else {
                            break;
                        }
                    }
                    ++mOffset;
                }
                if (mOffset == chars.length) {
                    throw new IllegalArgumentException("unterminated string");
                }
                ++mOffset;
                mCurrentToken = TOKEN_VALUE;
                return;
            }

            // anything we don't recognize
            throw new IllegalArgumentException("illegal character");
        }

        private static final boolean isIdentifierStart(char c) {
            return c == '_' ||
                    (c >= 'A' && c <= 'Z') ||
                    (c >= 'a' && c <= 'z');
        }

        private static final boolean isIdentifierChar(char c) {
            return c == '_' ||
                    (c >= 'A' && c <= 'Z') ||
                    (c >= 'a' && c <= 'z') ||
                    (c >= '0' && c <= '9');
        }
    }

    public static long getAvailaleSize(){

    	if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
		    File path = Environment.getExternalStorageDirectory(); 
		
		    StatFs stat = new StatFs(path.getPath()); 		
		    long blockSize = stat.getBlockSize(); 		
		    long availableBlocks = stat.getAvailableBlocks();			
		    return availableBlocks * blockSize; 

    	}
    	return -1;
    }

    /*
     * Get App ids from string with ',' delimiter
     *   @param strAppIds : string with app ids
     * 
     * by Jacob, 2012.02.02
     */
	public static long[] getAppIdsFromString(String strAppIds) {
		String[] arrAppIds = strAppIds.split("\\,");
		
		if (arrAppIds.length != 0) {
			long[] appIds = new long[arrAppIds.length];
			for (int i = 0; i < arrAppIds.length; i++) {
				appIds[i] = Long.parseLong(arrAppIds[i]);
			}
			return appIds;
		}
		
		return null;
	}
	
    /*
     * Get String from app ids with ',' delimiter
     *   @param appIds : app ids
     * 
     * by Jacob, 2012.02.02
     */
	public static String getStringForAppIds(long[] appIds) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < appIds.length; i++) {
			sb.append(appIds[i]);
			if(i < (appIds.length - 1)) {
				sb.append(",");
			}
		}

		return sb.toString();
	}
}
