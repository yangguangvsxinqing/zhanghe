/*
 * Copyright (C) 2013 The ChameleonOS Project
 *
 * Licensed under the GNU GPLv2 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fineos.theme.provider;

import android.net.Uri;

public class ThemeSQLiteHelper {

	public static final String TABLE_THEMES = "themes";
	public static final String COLUMN_IS_COS_THEME = "cos_theme";
	public static final String COLUMN_IS_DEFAULT_THEME = "default_theme";
	public static final String COLUMN_HAS_WALLPAPER = "has_wallpaper";
	public static final String COLUMN_WALLPAPER_ISUSING = "wallpaper_isusing"; // wallpaper_isusing
	public static final String COLUMN_HAS_LOCK_WALLPAPER = "has_lock_wallpaper";
	public static final String COLUMN_LOCK_WALLPAPER_ISUSING = "lock_wallpaper_isusing"; // lock_wallpaper_isusing
	public static final String COLUMN_HAS_ICONS = "has_icons";
	public static final String COLUMN_ICONS_ISUSING = "icons_isusing"; // icons_isusing
	public static final String COLUMN_HAS_LOCKSCREEN = "has_lock";
	public static final String COLUMN_LOCKSCREEN_ISUSING = "lockscreen_isusing"; // lockscreen_isusing
	public static final String COLUMN_HAS_CONTACTS = "has_contacts";
	public static final String COLUMN_HAS_DIALER = "has_dialer";
	public static final String COLUMN_HAS_SYSTEMUI = "has_systemui";
	public static final String COLUMN_HAS_FRAMEWORK = "has_framework";
	public static final String COLUMN_HAS_RINGTONE = "has_ringtone";
	public static final String COLUMN_RINGTONE_ISUSING = "ringtone_isusing"; // ringtone_isusing
	public static final String COLUMN_HAS_NOTIFICATION = "has_notification";
	public static final String COLUMN_HAS_BOOTANIMATION = "has_bootanimation";
	public static final String COLUMN_HAS_MMS = "has_mms";
	public static final String COLUMN_HAS_FONT = "has_font";
	public static final String COLUMN__FONT_ISUSING = "font_isusing"; // font_isusing
	public static final String COLUMN_LAST_MODIFIED = "last_modified";

	/****************** NEW ******************************/
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_THEME_TITLE = "title";
	public static final String COLUMN_THEME_PRICE = "price";
	public static final String COLUMN_THEME_DISCOUNT = "discount";
	public static final String COLUMN_THEME_ICONURL = "iconUrl";
	public static final String COLUMN_THEME_PREVIEWURL = "previewUrl";
	public static final String COLUMN_THEME_DOWNLOADURL = "downloadUrl";
	public static final String COLUMN_THEME_LASTUPDATE = "lastUpdate";
	public static final String COLUMN_THEME_SIZE = "size";
	public static final String COLUMN_THEME_RATING = "rating";
	public static final String COLUMN_THEME_DOWNLOADCOUNTS = "downLoadCounts";
	public static final String COLUMN_THEME_TYPE = "type";
	public static final String COLUMN_THEME_DISCRITION = "discrition";
	public static final String COLUMN_THEME_VERSION = "version";
	public static final String COLUMN_THEME_UI_VERSION = "ui_version";
	public static final String COLUMN_THEME_DESIGNER = "designer";
	public static final String COLUMN_THEME_AUTHOR = "author";
	public static final String COLUMN_THEME_ISUPDATEAVAILABLE = "isUpdateAvailable";
	public static final String COLUMN_THEME_ISUSING = "isUsing"; // isusing
	public static final String COLUMN_THEME_DOWNLOADINGFLAG = "downloadingFlag";
	public static final String COLUMN_THEME_ISLOCALE = "isLocale";
	public static final String COLUMN_THEME_ICON = "icon";
	/****************** NEW ******************************/

	public static final String COLUMN_THEME_FILE_NAME = "file_name";
	public static final String COLUMN_THEME_PATH = "theme_path";
	public static final String COLUMN_RINGTONE_PATH = "ringtone_path";
	public static final String COLUMN_WALLPAPER_PATH = "wallpaper_path";
	public static final String COLUMN_LOCKSCREEN_WALLPAPER_PATH = "lockscreen_wallpaper_path";
	public static final String COLUMN_PREVIEWS_LIST = "previews_list";
	public static final String COLUMN_IS_COMPLETE = "is_complete";
	public static final String COLUMN_IS_VERIFY = "is_verify";// 0.未认证 1.已认证
																// 2.不显示

	public static final String DATABASE_NAME = "themes.db";

	public static final int DATABASE_VERSION = 13;
	public static final String AUTHORITY = "com.fineos.thememgr.Settings";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/themes");
}
