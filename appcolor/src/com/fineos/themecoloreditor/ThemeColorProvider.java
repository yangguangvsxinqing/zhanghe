package com.fineos.themecoloreditor;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

public class ThemeColorProvider extends ContentProvider {

	public static final String TAG = "ThemeContentProvider";

	public static final String CONTENT = "content://com.android.themecoloreditor/";
	public static final Uri CONTENT_URI = Uri.parse(CONTENT);


	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
		Log.e(TAG, "openFile,mode=" + mode.toString());
		Log.e(TAG, "openFile,uri=" + uri.toString());

		Log.e("", "");
		File f = new File(getContext().getDir("themes", 0), uri.getPath());
		Log.e("", "uri.getPath(): 22222222222: " + f.getPath());

		Log.e(TAG, "openFile,f=" + f.exists());
		if (f.exists()) {
			try {
				return (ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY));
			} catch (Exception e) {
				Log.e(TAG, "openFile,Exception= " + e.getMessage());
			} finally {
				Log.e(TAG, "openFile,88888888888");
			}

		}
		Log.e(TAG, "openFile,f=00000000000000000000000000");
		throw new FileNotFoundException("ThemeContentProvider---" + uri.getPath());
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		Log.e(TAG, "provider create");
		return true;
	}

	protected static void copyFile(File sourceFile, File targetFile) throws IOException {
		Log.e("", "copy file");

		BufferedInputStream inBuff = null;
		BufferedOutputStream outBuff = null;
		try {
			// 新建文件输入流并对它进行缓冲
			inBuff = new BufferedInputStream(new FileInputStream(sourceFile));
			// 新建文件输出流并对它进行缓冲
			outBuff = new BufferedOutputStream(new FileOutputStream(targetFile));

			// 缓冲数组
			byte[] b = new byte[1024 * 5];
			int len;
			while ((len = inBuff.read(b)) != -1) {
				outBuff.write(b, 0, len);
			}
			// 刷新此缓冲的输出流
			outBuff.flush();
		} finally {
			// 关闭流
			if (inBuff != null)
				inBuff.close();
			if (outBuff != null)
				outBuff.close();
		}
	}

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Uri insert(Uri arg0, ContentValues arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Cursor query(Uri arg0, String[] arg1, String arg2, String[] arg3, String arg4) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}