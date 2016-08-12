package com.huaqin.market.utils;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.huaqin.market.R;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings.Secure;

public class GlobalUtil {

	public static boolean bAutoCheckUpdate;
	public static boolean bAutoCreateShortcut;

	/*
	 * Check system setting for whether allow to install 3rd party applications
	 */
	public static boolean allowInstallNonMarketApps(Context context) {
		// TODO Auto-generated method stub
		ContentResolver cr = context.getContentResolver();
		
		if (Secure.getInt(cr, Secure.INSTALL_NON_MARKET_APPS, 0) == 0) {
			return false;
		} else {
			return true;
		}
	}

	public static void showNonMarketAppDialog(final Context context) {
		// TODO Auto-generated method stub
		new AlertDialog.Builder(context).setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.dlg_notmarket_warning_title)
				.setMessage(R.string.dlg_notmarket_warning_msg)
				.setPositiveButton(R.string.btn_edit_settings, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						Intent intent = new Intent("android.settings.APPLICATION_SETTINGS");
						context.startActivity(intent);
					}
				})
				.setNegativeButton(R.string.btn_cancel, null)
				.show();
	}

	/*
	 * Get application size string with raw byte size
	 */
	public static String getSize(int size) {
		// TODO Auto-generated method stub
		NumberFormat numberFormat = NumberFormat.getInstance();
		StringBuilder builder = null;
		String strSize = null;
		
		if (size > 1048576) {
			numberFormat.setMaximumFractionDigits(2);
			strSize = String.valueOf(numberFormat.format((size / 1048576.0f)));
			builder = new StringBuilder(strSize);
			builder.append(" M");
		} else {
			numberFormat.setMaximumFractionDigits(0);
			strSize = String.valueOf(numberFormat.format((size / 1024.0f)));
			builder = new StringBuilder(strSize);
			builder.append(" K");
		}
		return builder.toString();
	}

	/*
	 * Get Date string with raw Date by specified format
	 */
	public static String getDateByFormat(Date date, String format) {
		// TODO Auto-generated method stub
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
		
		return simpleDateFormat.format(date);
	}
	
	public static boolean checkNetworkState(Context context) {
		// TODO Auto-generated method stub
		ConnectivityManager connectMgr =
			(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		if (connectMgr == null) {
			return false;
		}
		
		NetworkInfo nwInfo = connectMgr.getActiveNetworkInfo();
		
		if (nwInfo == null || !nwInfo.isAvailable()) {
			return false;
		}
		return true;
	}
}