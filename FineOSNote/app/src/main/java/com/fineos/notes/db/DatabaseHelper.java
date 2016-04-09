package com.fineos.notes.db;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Picture;
import android.util.Log;

import com.fineos.notes.bean.Folder;
import com.fineos.notes.bean.Note;
import com.fineos.notes.bean.NotePicture;
import com.fineos.notes.constant.Constant;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper{
	//数据库名称
	private static final String DATABASE_NAME = "sqlite-note.db";
	// 数据库version
    private static final int DATABASE_VERSION = 1;
    private Map<String, Dao> daos = new HashMap<String, Dao>();  
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

	@Override
	public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
		try {
            TableUtils.createTable(connectionSource, Folder.class);
            TableUtils.createTable(connectionSource, Note.class);
            TableUtils.createTable(connectionSource, NotePicture.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int oldVersion,
			int newVersion) {
		try {
			TableUtils.dropTable(connectionSource, Note.class, true);
			TableUtils.dropTable(connectionSource, Folder.class, true);
			TableUtils.dropTable(connectionSource, NotePicture.class, true);
			onCreate(sqLiteDatabase, connectionSource);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	    
	    private static DatabaseHelper instance;  
	    
	    /** 
	     * 单例获取该Helper 
	     *  
	     * @param context 
	     * @return 
	     */  
	    public static synchronized DatabaseHelper getHelper(Context context)  
	    {
            context = context.getApplicationContext();
            if (instance == null)
	        {
                synchronized (DatabaseHelper.class)
	            {
                    if (instance == null) {
                        instance = new DatabaseHelper(context);
                    }
	            }  
	        }  
	  
	        return instance;  
	    }  
	    
	    public synchronized Dao getDao(Class clazz) throws SQLException  
	    {  
	        Dao dao = null;  
	        String className = clazz.getSimpleName();  
	  
	        if (daos.containsKey(className))  
	        {  
	            dao = daos.get(className);  
	        }  
	        if (dao == null)  
	        {  
	            dao = super.getDao(clazz);  
	            daos.put(className, dao);  
	        }  
	        return dao;  
	    }  
	    
	 
	    /** 
	     * 释放资源 
	     */  
	    @Override  
	    public void close()  
	    {  
	        super.close();  
	  
	        for (String key : daos.keySet())  
	        {  
	            Dao dao = daos.get(key);  
	            dao = null;  
	        }  
	    }  
}
