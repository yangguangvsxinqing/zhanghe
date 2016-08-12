package com.fineos.theme.download;

import android.net.Uri;
import android.provider.BaseColumns;

public final class Downloads implements BaseColumns {

	public static final Uri CONTENT_URI = Uri.parse("content://downloads/download");
	public static final String ACTION_DOWNLOAD_COMPLETED = "android.intent.action.DOWNLOAD_COMPLETE";
	public static final String ACTION_DOWNLOAD_UPDATEUILIST = "android.intent.action.DOWNLOAD_UPDATEUILIST";
	public static final String ACTION_DOWNLOAD_RESULTPROMT = "android.intent.action.DOWNLOAD_RESULTPROMT";
	public static final String ACTION_NOTIFICATION_CLICKED = "android.intent.action.DOWNLOAD_NOTIFICATION_CLICKED";
	public static final String ACTION_DOWNLOAD_COMPLETED_NOTIFICATION = "android.intent.action.DOWNLOAD_COMPLETE_NOTIFICATION";
	public static final String ACTION_INSTALL_COMPLETED_NOTIFICATION = "android.intent.action.PACKAGE_ADDED";
	public static final String ACTION_DELETE_NOTIFICATION = "android.intent.action.DELETE_NOTIFICATION";
	public static final String COLUMN_URI = "uri";
	public static final String COLUMN_APP_DATA = "entity";
	public static final String COLUMN_NO_INTEGRITY = "no_integrity";
	public static final String COLUMN_FILE_NAME_HINT = "hint";
	public static final String _DATA = "_data";
	public static final String COLUMN_MIME_TYPE = "mimetype";
	public static final String COLUMN_DESTINATION = "destination";
	public static final String COLUMN_VISIBILITY = "visibility";
	public static final String COLUMN_CONTROL = "control";
	public static final String COLUMN_STATUS = "status";
	public static final String COLUMN_LAST_MODIFICATION = "lastmod";
	public static final String COLUMN_NOTIFICATION_PACKAGE = "notificationpackage";
	public static final String COLUMN_NOTIFICATION_CLASS = "notificationclass";
	public static final String COLUMN_NOTIFICATION_EXTRAS = "notificationextras";
	public static final String COLUMN_COOKIE_DATA = "cookiedata";
	public static final String COLUMN_USER_AGENT = "useragent";
	public static final String COLUMN_REFERER = "referer";
	public static final String COLUMN_TOTAL_BYTES = "total_bytes";
	public static final String COLUMN_CURRENT_BYTES = "current_bytes";
	public static final String COLUMN_CURRENT_BYTES_1 = "current_bytes_1";
	public static final String COLUMN_CURRENT_BYTES_2 = "current_bytes_2";
	public static final String COLUMN_CURRENT_BYTES_3 = "current_bytes_3";
	public static final String COLUMN_CURRENT_BYTES_4 = "current_bytes_4";
	public static final String COLUMN_CURRENT_BYTES_5 = "current_bytes_5";
	public static final String COLUMN_OTHER_UID = "otheruid";
	public static final String COLUMN_TITLE = "title";
	public static final String COLUMN_DESCRIPTION = "description";
	public static final String COLUMN_APP_ID = "appId";
	public static final String COLUMN_PACKAGE_NAME = "pkgName";
	/************* Added-s by JimmyJin for Pudding Project **************/
	public static final String COLUMN_APP_TYPE = "appType";
	public static final String COLUMN_FROM_WHERE = "fromWhere";
	/************* Added-e by JimmyJin for Pudding Project **************/
	public static final int VISIBILITY_VISIBLE = 0;
	public static final int VISIBILITY_VISIBLE_NOTIFY_COMPLETED = 1;
	public static final int VISIBILITY_HIDDEN = 2;

	public static final int DESTINATION_EXTERNAL = 0;
	public static final int DESTINATION_CACHE_PARTITION = 1;
	public static final int DESTINATION_CACHE_PARTITION_PURGEABLE = 2;
	public static final int DESTINATION_CACHE_PARTITION_NOROAMING = 3;

	public static final String PERMISSION_ACCESS = "huaqin.permission.ACCESS_DOWNLOAD_MANAGER";
	public static final String PERMISSION_ACCESS_ADVANCED = "huaqin.permission.ACCESS_DOWNLOAD_MANAGER_ADVANCED";
	public static final String PERMISSION_CACHE = "huaqin.permission.ACCESS_CACHE_FILESYSTEM";
	public static final String PERMISSION_SEND_INTENTS = "huaqin.permission.SEND_DOWNLOAD_COMPLETED_INTENTS";

	public static final int STATUS_PENDING = 190;
	public static final int STATUS_PENDING_PAUSED = 191;
	public static final int STATUS_RUNNING = 192;
	public static final int STATUS_RUNNING_PAUSED = 193;
	public static final int STATUS_DOWNLOAD_PAUSED = 194;
	public static final int STATUS_WAITDOWNLOAD_PAUSED = 195;
	public static final int STATUS_SUCCESS = 200;

	public static final int STATUS_BAD_REQUEST = 400;
	public static final int STATUS_NOT_ACCEPTABLE = 406;
	public static final int STATUS_LENGTH_REQUIRED = 411;
	public static final int STATUS_PRECONDITION_FAILED = 412;

	public static final int STATUS_CANCELED = 490;
	public static final int STATUS_UNKNOWN_ERROR = 491;
	public static final int STATUS_FILE_ERROR = 492;
	public static final int STATUS_UNHANDLED_REDIRECT = 493;
	public static final int STATUS_UNHANDLED_HTTP_CODE = 494;
	public static final int STATUS_HTTP_DATA_ERROR = 495;
	public static final int STATUS_HTTP_EXCEPTION = 496;
	public static final int STATUS_TOO_MANY_REDIRECTS = 497;
	public static final int STATUS_INSUFFICIENT_SPACE_ERROR = 498;
	public static final int STATUS_DEVICE_NOT_FOUND_ERROR = 499;

	public static final int CONTROL_RUN = 0;
	public static final int CONTROL_PAUSED = 1;

	public static final int STATUS_UNDOWNLOAD = 0;
	public static final int STATUS_DOWNLOADING = 1;
	public static final int STATUS_DOWNLOADED = 2;

	public static final String WHERE_RUNNING = "(status >= '100') AND (status <= '199') AND (visibility IS NULL OR visibility == '0' OR visibility == '1')";
	public static final String WHERE_COMPLETED = "status = '200'";
	public static final String WHERE_RUNNING_OR_COMPLETED = "((status >= '100') AND (status <= '199') AND (visibility IS NULL OR visibility == '0' OR visibility == '1') OR status = '200')";
	public static final String WHERE_APP_ID = "appId=?";

	public static final String RESULTPROMPT = "resultpromt";

	public static boolean isStatusInformational(int status) {
		return (status >= 100 && status < 200);
	}

	public static boolean isStatusSuspended(int status) {
		return (status == STATUS_PENDING_PAUSED || status == STATUS_RUNNING_PAUSED);
	}

	public static boolean isStatusSuccess(int status) {
		return (status >= 200 && status < 300);
	}

	public static boolean isStatusError(int status) {
		return (status >= 400 && status < 600);
	}

	public static boolean isStatusClientError(int status) {
		return (status >= 400 && status < 500);
	}

	public static boolean isStatusServerError(int status) {
		return (status >= 500 && status < 600);
	}

	public static boolean isStatusCompleted(int status) {
		// return (status >= 200 && status < 300) || (status >= 400 && status <
		// 600);
		return (status >= 200 && status < 300);
	}
}