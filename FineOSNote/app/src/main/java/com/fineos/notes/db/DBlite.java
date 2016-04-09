package com.fineos.notes.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.fineos.notes.bean.Folder;

public class DBlite extends SQLiteOpenHelper{
    //数据库名称
    private static final String DATABASE_NAME = "sqlite-note.db";
    // 数据库version
    private static final int DATABASE_VERSION = 1;
    Folder mFolder;
    public DBlite(Context context) { 
    	super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mFolder = new Folder();
} 

public void onCreate(SQLiteDatabase db) { 
        // TODO Auto-generated method stub 	
                db.execSQL("CREATE TABLE " + "tb_folder" + " ("
                      + mFolder.getId() + " INTEGER PRIMARY KEY,"
                      + mFolder.getFolder()+ " TEXT,"
                      + mFolder.getData() + " Long,"
                      + ");");
}

public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { 
        // TODO Auto-generated method stub 
        db.execSQL("DROP TABLE IF EXISTS " + "tb_folder");
        onCreate(db);
} 
}
