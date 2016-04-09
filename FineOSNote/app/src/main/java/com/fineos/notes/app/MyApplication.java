package com.fineos.notes.app;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import com.fineos.notes.R;
import com.fineos.notes.bean.Folder;
import com.fineos.notes.bean.MyInfo;
import com.fineos.notes.bean.Note;
import com.fineos.notes.constant.Constant;
import com.fineos.notes.db.FolderDao;
import com.fineos.notes.db.NoteDao;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import android.app.Application;
import android.content.ContentProviderClient;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;

public class MyApplication extends Application {
	private MyApplication app;
	private FolderDao dao;
	private Cursor cursor;

	@Override
	public void onCreate() {
		super.onCreate();
		app = this;

		initImageLoader();
		initData();
//		initMyInfo();
	}

	private void initImageLoader() {
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
		ImageLoader.getInstance().init(config);
	}

	public MyApplication getApplication() {
		return app;
	}

	private void initData() {
        if (dao == null) {
            dao = new FolderDao(this);
        }
        Folder folder = null;
//        String[] folderName = {"All Notes",
//                "Call Recording", "Add Folder" };
        //Temporarily remove calls
        String[] folderName = {"All Notes", "Add Folder" };

        int[] folderId = {1,2};
        for (int i = 0; i < folderName.length; i++) {
            List<Folder> folderList = dao.selectByfolderId(folderId[i]);
            Log.w(Constant.TAG,"folderList folderList.size():"+folderList+"  "+folderList.size());
            if (folderList.size() <= 0) {
                folder = new Folder();
                folder.setFolder(folderName[i]);
                dao.add(folder);
            }
        }

	}

	private void initMyInfo() {
		if (isValidProvider(Constant.CONTENT_URI)) {
			cursor = getContentResolver().query(Constant.CONTENT_URI, null,
					null, null, null);
		}
	}

	// 判断所要使用的Provider是否有效
	private boolean isValidProvider(Uri uri) {
		ContentProviderClient client = getContentResolver()
				.acquireContentProviderClient(uri);
		if (client == null) {
			return false;
		} else {
			client.release();
			return true;
		}
	}

	// 查询用户名
	public String getNameFromCursor() {
		if (cursor == null)
			return null;
		String name = null;
		if(cursor.moveToFirst()){
			name = cursor.getString(cursor.getColumnIndex(MyInfo.NAME));
		}
        Log.w(Constant.TAG,"MyApplication name:"+name);
		return name;
	}

	// 获取头像
	public Bitmap getBmpFromCursor() {
        if(cursor ==null)
			return null;
		Bitmap bmpout = null;
		if(cursor.moveToFirst()){
			byte[] in = cursor.getBlob(cursor.getColumnIndex(MyInfo.ICON));
			bmpout = BitmapFactory.decodeByteArray(in, 0, in.length);
			ByteArrayInputStream ba = new ByteArrayInputStream(in);
			Drawable.createFromStream(ba, "photo");
			try {
				ba.close();
			} catch (IOException e) {
                e.printStackTrace();
            }
        }
		return bmpout;
	}

}
