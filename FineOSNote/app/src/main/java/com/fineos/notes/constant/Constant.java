package com.fineos.notes.constant;

import android.net.Uri;
import android.os.Environment;

import java.io.File;


public class Constant {


    public static String All_NOTE = "All Notes";
    public static String CALL_RECORDING = "Call Recording";
    public static String ADD_FOLDER = "Add Folder";


    public static String TABLE_NOTE_ID = "note_id";
	public static String TABLE_NOTE_FOLDER = "folder";
	public static String TABLE_NOTE_TITLE = "title";
	public static String TABLE_NOTE_DETAIL = "detail";
	public static String TABLE_NOTE_BG = "background";
	public static String TABLE_NOTE_PICTURE = "picture";
	public static String TABLE_NOTE_DATA = "data";

    public static String TABLE_FOLDER_ID = "folder_id";


    public static String TABLE_NOTE_PICTURE_ID = "image_id";
    public static String TABLE_NOTE_PICTURE_NOTEID = "note_id";
    public static String TABLE_NOTE_PICTURE_CONTENT = "content";
    public static String TABLE_NOTE_PICTURE_PATH = "imagePath";

	public static String TAG = "FineOSNote";
	public static final int MAIN_ADD_REQUESTCODE = 0;
	public static final int MAIN_EDT_REQUESTCODE = 1;
	public static final int REQUESTCODE2 = 1;
	public static final int ADDNOTE_ADD_RESULTCODE = 2;//add note result
	public static final int ADDNOTE_EDIT_RESULTCODE = 3;//edit note result
	public static final int RESULTCODE3 = 4;
	public static final int CAMERAR_REQUESTCODE= 5;
	public static final int CAMERAR_RESULTCODE = 6;
	public static final int PICTURE_REQUESTCODE = 7;
	public static final int PICTURE_RESULTCODE = 8;
    public static final int MOVENOTE_REQUESTCODE = 9;
    public static final int MOVENOTE_RESUTLCODE = 10;
    public static final int PICTURECROP_REQUESTCODE = 11;
    public static  final int GRAFFITI_REQUESTCODE = 12;
    public static  final int ADDNOTE_REQUESTCODE = 13;
    public static  final int ADDNOTE_RESULTCODE = 14;

    public static  final int CROP_RESULTCODE = 15;
    public static  final int CROP_REQUESTCODE = 16;

    public static  final int GRAFFITTI_RESULTCODE = 17;

	public static final String AUTHORITY="com.fineos.myinfo";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/myInfo");//数据库URI
    public static  final String Dir = "/mnt/sdcard/myPicture/";
    public static  final String GraffitiDir = "/mnt/sdcard/graffiti/";
    public static  final String EmailPicturePath = Environment.getExternalStorageDirectory()+ File.separator+"fineos/emailpicture";
    public static  final String SAVEPICTUREDIR =Environment.getExternalStorageDirectory()+File.separator+"Note"+File.separator;

    public static  final String PICTURETYPE = ".png";

    public static final int PICTURE_WIDTH = 636;
    public static final int PICTURE_HEIGHT = 1120;


}
