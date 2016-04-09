package com.fineos.notes.unti;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.provider.MediaStore.Images;

public class defaults {

	
	
	public static final int NOTES_PAINTS_SIZE = 4;
	public static final int NOTES_PAINTS_COLOR = 0xFF000000;
	
	public static final int STCKET_PAINTS_SIZE = 2;
	public static final int STCKET_PAINTS_COLOR = 0xFF000000;
	
	public static final boolean DEFAULT_FOR_BD2 = false;
	
	private static final Uri STORAGE_URI = Images.Media.EXTERNAL_CONTENT_URI;
	
	
    public static Uri addImage(ContentResolver cr, String title, long dateTaken,String filename, String filePath) {
    	
        ContentValues values = new ContentValues(6);
        values.put(Images.Media.TITLE, title);

        // That filename is what will be handed to Gmail when a user shares a
        // photo. Gmail gets the name of the picture attachment from the
        // "DISPLAY_NAME" field.
        values.put(Images.Media.DISPLAY_NAME, filename);
        values.put(Images.Media.DATE_TAKEN, dateTaken);
        values.put(Images.Media.MIME_TYPE, "image/png");
        values.put(Images.Media.ORIENTATION, 0);
        values.put(Images.Media.DATA, filePath);

        return cr.insert(STORAGE_URI, values);
    }
    
    public static void deleteImage(ContentResolver cr, String title) {
    	
         cr.delete(STORAGE_URI, Images.Media.DATA+ "=?", new String[]{title});
         cr.notifyChange(STORAGE_URI, null);
    }    
    
}
