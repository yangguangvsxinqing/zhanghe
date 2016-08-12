package com.fineos.theme.activity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.fineos.theme.R;
import com.fineos.theme.model.ThemeData;
import com.fineos.theme.provider.ThemeContentProvider;
import com.fineos.theme.utils.Constant;
import com.fineos.theme.utils.ThemeLog;
import com.fineos.theme.utils.ThemeUtils;

public class OneKeyFaceLiftActivity extends Activity {
	private final static String TAG = "OneKeyFaceLiftActivity";
	private Context mContext;
	private ThemeData mThemeInfo;

	public static final String ACTION_THEME_APPLY = "android.fineos.theme.action.ACTION_THEME_APPLY";
	public OneKeyFaceLiftActivity() {
		mContext = this;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ThemeLog.e(TAG, "onCreate");

		String[] availableThemes = ThemeUtils.getAvailableThemes(Constant.DEFAULT_THEME_PATH);
		ThemeUtils.removeNonExistingThemes(this, availableThemes);
		List<ThemeData> allTheme = ThemeUtils.getThemeListByType(ThemeData.THEME_ELEMENT_TYPE_COMPLETE_THEME, mContext);
		if (allTheme != null & allTheme.size() > 0) {
			int i = 0;
			int j = allTheme.size();
			for (; i < j; i++) {
				if (allTheme.get(i).getIsUsing()) {
					break;
				}
			}
			mThemeInfo = allTheme.get((i + 1) % j);
			if (allTheme.size() == 1) {
				Toast.makeText(mContext, getText(R.string.apply_failed_tip_once), Toast.LENGTH_SHORT).show();
				finish();
			} else {
				applyTheme(mThemeInfo);
			}
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {

		switch (event.getKeyCode()) {
		case KeyEvent.KEYCODE_BACK:
			return true;
		default:
			break;
		}

		return super.dispatchKeyEvent(event);
	}

	@Override
	protected void onDestroy() {

		super.onDestroy();

	}

	private void applyTheme(ThemeData theme) {
		String themePath = copyFileToSystem(theme.getThemePath());
		
		ThemeLog.e(TAG, "ThemeContentProvider.CONTENT + stripPath(themePath): "+ThemeContentProvider.CONTENT + stripPath(themePath)+" theme.getId(): "+theme.getId());

		Intent intent = new Intent(ACTION_THEME_APPLY);
		intent.putExtra("themePath", ThemeContentProvider.CONTENT + stripPath(themePath));
		intent.putExtra("themeId", theme.getId());
		sendBroadcast(intent);
		finish();
	}

	protected String copyFileToSystem(String theme) {
		ThemeLog.i(TAG, "copyFileToSystem go ");

		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT && !theme.startsWith("/system")) {
			File file = new File(theme);
//			theme = "/data/data/com.fineos.theme/themes/AI01203256.ftz";
			ThemeLog.i(TAG, "theme: " + theme);

			File newFile = getDir("themes", 0);
			ThemeLog.i(TAG, "newFile = " + newFile.getPath());

			ThemeLog.i(TAG, "newFile.listFiles().length = " + newFile.listFiles().length);
			File[] listFiles = newFile.listFiles();

			ThemeLog.i("", "listFiles.length........ ");
			for (int i = 0; i < listFiles.length; i++) {
				if (!listFiles[i].getName().equals(theme.substring(theme.lastIndexOf(File.separator)+1))) {
					ThemeLog.i(TAG, "delete file");
					listFiles[i].delete();
				}
			}

			newFile = new File(newFile.getPath(), theme.substring(theme.lastIndexOf(File.separator)+1));
			ThemeLog.i(TAG, "newFile = " + newFile.getPath());

			if (!newFile.exists()) {
				try {
					copyFile(file, newFile);
				} catch (Exception e) {
					ThemeLog.e(TAG, "copyFileToSystem copyFile Exception: ", e);
				}
			}

			return newFile.getPath();

		}

		return theme;
	}

	protected static void copyFile(File sourceFile, File targetFile) throws IOException {
		ThemeLog.i(TAG, "copy file");
		
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
	
	private String stripPath(String filename) {
		int index = filename.lastIndexOf('/');
		if (index > -1) {
			filename = filename.substring(index + 1);
		}

		return filename;
	}
}
