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

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.fineos.theme.R;
import com.fineos.theme.model.ThemeData;
import com.fineos.theme.utils.ThemeLog;

public class PreviewManager {
	private static final boolean DEBUG = true;
	private static final String TAG = "PreviewManager";
	public final Map<String, BitmapDrawable> drawableMap;

	public PreviewManager() {
		drawableMap = new WeakHashMap<String, BitmapDrawable>();
	}

	@SuppressWarnings("deprecation")
	public BitmapDrawable fetchDrawable(ThemeData theme) {
		String themeId = theme.getFileName();
		if (drawableMap.containsKey(themeId)) {
			return drawableMap.get(themeId);
		}

		if (DEBUG)
			ThemeLog.d(this.getClass().getSimpleName(), "theme ID:" + themeId);
		try {
			InputStream is = fetch(theme);
			ThemeLog.e(TAG, "fetchDrawable, is =" + is);
			BitmapDrawable drawable = null;
			if (is != null) {
				BitmapFactory.Options opts = new BitmapFactory.Options();
				opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
				opts.inSampleSize = 1;
				Bitmap bmp = BitmapFactory.decodeStream(is, null, opts); // 从输入流解析出bitmap
				drawable = new BitmapDrawable(bmp); // 并转换成 drawable
				is.close();
			}

			if (drawable != null) {
				drawableMap.put(themeId, drawable); // 放入缓存
				if (DEBUG)
					ThemeLog.e(this.getClass().getSimpleName(), "got a thumbnail drawable: " + drawable.getBounds() + ", " + drawable.getIntrinsicHeight() + "," + drawable.getIntrinsicWidth() + ", "
							+ drawable.getMinimumHeight() + "," + drawable.getMinimumWidth());
			} else {
				if (DEBUG)
					ThemeLog.w(this.getClass().getSimpleName(), "could not get thumbnail");
			}

			return drawable;
		} catch (IOException e) {
			if (DEBUG)
				ThemeLog.e(this.getClass().getSimpleName(), "fetchDrawable failed", e);
			return null;
		}
	}

	public void fetchDrawableOnThread(final ThemeData theme, final PreviewHolder holder) {
		String themeId = theme.getFileName(); // 以文件名作为主题 id
		if (drawableMap.containsKey(themeId)) {
			holder.preview.setImageDrawable(drawableMap.get(themeId));
			holder.progress.setVisibility(View.GONE);
			return;
		}

		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message message) {
				long delay = 0;
				delay = holder.index * 50;
				ThemeLog.e(TAG, "message.obj=" + message.obj);
				if (message.obj != null) {
					// holder.preview.setImageDrawableAnimated((BitmapDrawable)
					// message.obj, delay);
					holder.preview.setImageDrawable((BitmapDrawable) message.obj);
				} else {
					// holder.preview.setImageResourceAnimated(R.drawable.no_preview,
					// delay); //预览图加载失败显示
					holder.preview.setImageResource(R.drawable.no_preview);
				}
				holder.progress.setVisibility(View.GONE);
			}
		};

		Thread thread = new Thread() {
			@Override
			public void run() {
				// TODO : set imageView to a "pending" image
				BitmapDrawable drawable = fetchDrawable(theme);
				Message message = handler.obtainMessage(1, drawable);
				handler.sendMessage(message);

			}
		};
		thread.start();
	}

	private InputStream fetch(ThemeData theme) throws IOException {
		ZipFile zip = new ZipFile(theme.getThemePath());
		ZipEntry ze = zip.getEntry("preview/preview_launcher_0.jpg"); // 优先选择这张图作为
		if (ze == null) {

			ze = zip.getEntry("preview/preview_launcher_0.png");
			if (ze == null) {
				String[] previewList = PreviewHelper.getAllPreviews(theme);
				if (previewList.length > 0) { // 否则选择所有预览图的第一张
					ze = zip.getEntry(previewList[0]);
				}

			}

		}
		return ze != null ? zip.getInputStream(ze) : null;
	}
}
