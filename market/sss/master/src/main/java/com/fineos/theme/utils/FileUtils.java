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

package com.fineos.theme.utils;

import java.io.UnsupportedEncodingException;

//import org.apache.http.util.EncodingUtils;

import android.util.Log;

public class FileUtils {

	/**
	 * cut off the file extension
	 * 
	 * @throws UnsupportedEncodingException
	 */
	public static String stripExtension(String filename) throws UnsupportedEncodingException {
		if (filename.lastIndexOf('.') > -1) {
			ThemeLog.e("stripExtension", "filename =" + filename);
	//		filename = EncodingUtils.getString((filename.substring(0, filename.lastIndexOf('.'))).getBytes(), "GBK");
			filename = filename.substring(0, filename.lastIndexOf('.'));
		}

		return filename;
	}

	/**
	 * Strips all path information and returns just the filename
	 * 
	 * @param filename
	 *            complete path and filename
	 * @return filename without any path information
	 */
	public static String stripPath(String filename) {
		int index = filename.lastIndexOf('/');
		if (index > -1) {
			filename = filename.substring(index + 1);
		}

		return filename;
	}

}
