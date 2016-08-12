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

package com.fineos.theme.preview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.util.Log;

import com.fineos.theme.model.ThemeData;
import com.fineos.theme.utils.ThemeLog;

public class PreviewHelper {
	public static final String PREVIEW_PREFIX = "preview_";
	public static final String PREVIEW_LAUNCHER_PREFIX = "preview_launcher_"; // for
																				// all
																				// theme
																				// previews
	public static final String PREVIEW_ICONS_PREFIX = "preview_icons_";
	public static final String PREVIEW_ICONS_PREFIX_SMALL = "preview_icons_small"; // for
																					// mixer
																					// icon
	public static final String PREVIEW_STATUSBAR_PREFIX = "preview_statusbar_";
	public static final String PREVIEW_MMS_PREFIX = "preview_mms_";
	public static final String PREVIEW_CONTACTS_PREFIX = "preview_contact_";
	public static final String PREVIEW_DIALER_PREFIX = "preview_dialer_";
	public static final String PREVIEW_BOOTANIMATION_PREFIX = "preview_animation_";
	public static final String PREVIEW_FONTS_PREFIX = "preview_fonts_";
	public static final String PREVIEW_FONTS_PREFIX_SMALL = "preview_fonts_small";
	public static final String PREVIEW_WALLPAPER_PREFIX = "default_wallpaper";
	public static final String PREVIEW_LOCK_WALLPAPER_PREFIX = "default_lock_wallpaper";
	// public static final String PREVIEW_WALLPAPER_PREFIX =
	// "preview_launcher_1";

	public static final String PREVIEW_LOCK_SCREEN_PREFIX = "preview_lockscreen";

	public static String[] getAllPreviews(ThemeData theme) {
		if (theme != null && theme.getPreviewsList() != null) {
			return theme.getPreviewsList().split("\\|");
		}
		return null;
	}

	public static String[] getThemePreviews(ThemeData theme) {
		if (theme != null) {
			int allPreviewLength;
			int lockScreenPreviewLength;
			int launcherPreviewLength;
			int statusbarPreviewLength;
			int contactsPreviewLength;
			int mmsPreviewLength;

			String[] lockScreenPreviews = getLockScreenPreviews(theme);
			lockScreenPreviewLength = lockScreenPreviews.length - 1;
			String[] launcherPreviews = getLauncherPreviews(theme);
			launcherPreviewLength = launcherPreviews.length - 1;
			String[] statusbarPreviews = getStatusbarPreviews(theme);
			statusbarPreviewLength = statusbarPreviews.length;
			String[] contactsPreviews = getContactsPreviews(theme);
			contactsPreviewLength = contactsPreviews.length;
			String[] mmsPreviews = getMmsPreviews(theme);
			mmsPreviewLength = mmsPreviews.length;
			allPreviewLength = lockScreenPreviewLength + launcherPreviewLength + statusbarPreviewLength + contactsPreviewLength + mmsPreviewLength;

			ThemeLog.i("PreviewHelper", "PreviewHelper allPreviewLength: " + allPreviewLength);

			String[] previews = new String[allPreviewLength];
			int currentPosition = 0;
			if (lockScreenPreviews != null) {
				for (int i = 0; i < lockScreenPreviewLength; i++) {
					previews[currentPosition] = lockScreenPreviews[i + 1];
					currentPosition++;
				}
			}
			if (launcherPreviews != null) {
				for (int i = 0; i < launcherPreviewLength; i++) {
					previews[currentPosition] = launcherPreviews[i + 1];
					currentPosition++;
				}
			}
			if (statusbarPreviews != null) {
				for (int i = 0; i < statusbarPreviewLength; i++) {
					previews[currentPosition] = statusbarPreviews[i];
					currentPosition++;
				}
			}
			if (contactsPreviews != null) {
				for (int i = 0; i < contactsPreviewLength; i++) {
					previews[currentPosition] = contactsPreviews[i];
					currentPosition++;
				}
			}
			if (contactsPreviews != null) {
				for (int i = 0; i < mmsPreviewLength; i++) {
					previews[currentPosition] = mmsPreviews[i];
					currentPosition++;
				}
			}
			return previews;
		}
		return null;
	}

	public static String[] getLauncherPreviews(ThemeData theme) {
		return getPreviews(theme, PREVIEW_LAUNCHER_PREFIX);
	}

	public static String[] getIconPreviews(ThemeData theme) {
		return getBigPreviews(theme, PREVIEW_ICONS_PREFIX);
	}

	public static String[] getIconPreviewsSmall(ThemeData theme) {
		return getSmallIconPreviews(theme, PREVIEW_ICONS_PREFIX_SMALL);
	}

	public static String[] getStatusbarPreviews(ThemeData theme) {
		return getPreviews(theme, PREVIEW_STATUSBAR_PREFIX);
	}

	public static String[] getMmsPreviews(ThemeData theme) {
		return getPreviews(theme, PREVIEW_MMS_PREFIX);
	}

	public static String[] getContactsPreviews(ThemeData theme) {
		return getPreviews(theme, PREVIEW_CONTACTS_PREFIX);
	}

	public static String[] getDialerPreviews(ThemeData theme) {
		return getPreviews(theme, PREVIEW_DIALER_PREFIX);
	}

	public static String[] getBootanimationPreviews(ThemeData theme) {
		return getPreviews(theme, PREVIEW_BOOTANIMATION_PREFIX);
	}

	public static String[] getFontsPreviews(ThemeData theme) {
		return getBigPreviews(theme, PREVIEW_FONTS_PREFIX);
	}

	public static String[] getFontsPreviewsSmall(ThemeData theme) {
		return getPreviews(theme, PREVIEW_FONTS_PREFIX_SMALL);
	}

	public static String[] getWallpaperPreviews(ThemeData theme) {
		return getPreviews(theme, PREVIEW_WALLPAPER_PREFIX);
	}

	public static String[] getLockWallpaperPreviews(ThemeData theme) {
		return getPreviews(theme, PREVIEW_LOCK_WALLPAPER_PREFIX);
	}

	public static String[] getLockScreenPreviews(ThemeData theme) {
		return getPreviews(theme, PREVIEW_LOCK_SCREEN_PREFIX);
	}

	private static String[] getPreviews(ThemeData theme, String prefix) {
		String[] completeList = getAllPreviews(theme); // 获取特定前缀主题的所有priviews
		List<String> list = new ArrayList<String>();
		for (String item : completeList) {
			if (item.contains(prefix))
				list.add(item);
		}
		Collections.sort(list);
		return list.toArray(new String[0]);
	}

	private static String[] getBigPreviews(ThemeData theme, String prefix) {
		String[] completeList = getAllPreviews(theme); // 获取特定前缀主题的所有priviews
		List<String> list = new ArrayList<String>();
		for (String item : completeList) {
			if (item.contains(prefix) && (!item.endsWith("_small.jpg") && !item.endsWith("_small.png"))) {
				list.add(item);
			}

		}
		Collections.sort(list);
		return list.toArray(new String[0]);
	}

	private static String[] getSmallIconPreviews(ThemeData theme, String prefix) {
		String[] completeList = getAllPreviews(theme);
		List<String> list = new ArrayList<String>();
		for (String item : completeList) {
			if (item.endsWith("icons_small.jpg") || item.endsWith("icons_small.png")) {
				list.add(item);
			}

		}
		Collections.sort(list);
		return list.toArray(new String[0]);
	}

	private static String[] getSmallFontPreviews(ThemeData theme, String prefix) {
		String[] completeList = getAllPreviews(theme);
		List<String> list = new ArrayList<String>();
		for (String item : completeList) {
			if (item.endsWith("fonts_small.jpg") || item.endsWith("fonts_small.png")) {
				list.add(item);
			}

		}
		Collections.sort(list);
		return list.toArray(new String[0]);
	}

}
