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

package com.fineos.theme.fragment;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fineos.theme.R;
import com.fineos.theme.activity.ThemeDetailActivity;
import com.fineos.theme.model.ThemeData;
import com.fineos.theme.preview.PreviewHolder;
import com.fineos.theme.preview.PreviewManager;
import com.fineos.theme.utils.Constant;
import com.fineos.theme.utils.ThemeLog;
import com.fineos.theme.utils.ThemeUtils;

@SuppressLint("ValidFragment")
public class LocalFragment extends Fragment {
	private static final String TAG = "LocalFragment";
	private static final String THEMES_PATH = Constant.DEFAULT_THEME_PATH;
	private static final String THEMES_PATH_DEFAULT = Constant.DEFAULT_SYSTEM_THEME;
	private GridView mGridView = null;
	private PreviewAdapter mAdapter = null;
	private LoadThemesInfoTask mTask = null;
	private List<ThemeData> mThemesList;
	private Context mContext;
	private boolean mReady = true;
	private List<Runnable> mPendingCallbacks = new LinkedList<Runnable>();
	private int mThemeSizePause = 0;
	private int mThemeSizeResume = 0;
	private Boolean mbFirstLoad = false;
	public static String defaultTheme = "雅致生活";

	public LocalFragment() {
	}

	public LocalFragment(Context c) {
		mContext = c;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ThemeLog.v(TAG, "LocalFragment,onCreate..........");
		mThemesList = new ArrayList<ThemeData>();
		setRetainInstance(true);
		setHasOptionsMenu(true);
		getActivity().setProgressBarIndeterminateVisibility(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		ThemeLog.v(TAG, "LocalFragment,onCreateView..........");
		View v = inflater.inflate(R.layout.fragment_local_theme, container, false);
		ThemeUtils.createThemeDir();
		mGridView = (GridView) v.findViewById(R.id.coverflow);
		mGridView.setSelector(new ColorDrawable(Color.TRANSPARENT)); // remove
																		// select
																		// color
		mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() { // 点击进入主题详细
					@Override
					public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
						Intent intent = new Intent(getActivity(), ThemeDetailActivity.class);
						Bundle bunble = new Bundle();
						bunble.putSerializable("themeInfo", mThemesList.get(i));
						intent.putExtras(bunble);
						startActivity(intent);
					}
				});

		mGridView.setVisibility(View.GONE);
		mTask = new LoadThemesInfoTask();
		mTask.execute();
		mbFirstLoad = true;
		return v;
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mReady = false;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mReady = true;

		int pendingCallbakcs = mPendingCallbacks.size();

		while (pendingCallbakcs-- > 0)
			getActivity().runOnUiThread(mPendingCallbacks.remove(0));
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		return true; // xuqian add
	}

	private Handler mViewUpdateHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (mContext != null) {
				List<ThemeData> tmp;
				tmp = ThemeUtils.getThemeListByType(ThemeData.THEME_ELEMENT_TYPE_COMPLETE_THEME, mContext);
				if (mThemesList != null && mThemesList.size() > 0) {
					mThemesList.clear();
				}
				for (int i = 0; i < tmp.size(); i++) {
					if (tmp.get(i).getTitle().equals(defaultTheme)) {
						mThemesList.add(tmp.get(i));
						tmp.remove(i);
					}
				}
				for (int i = 0; i < tmp.size(); i++) {
					mThemesList.add(tmp.get(i));
				}
				mAdapter = new PreviewAdapter(mContext); // 从文件读thumbnail并绑定到数据
				mGridView.setAdapter(mAdapter);
				mGridView.setVisibility(View.VISIBLE);
			}
		}
	};

	public void runWhenReady(Runnable runnable) {
		if (mReady)
			getActivity().runOnUiThread(runnable);
		else
			mPendingCallbacks.add(runnable);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// inflater.inflate(R.menu.activity_theme_chooser, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		return true; //
	}

	private void cleanDrawable() {
		if (mAdapter != null) {
			if (mAdapter.mPreviewManager != null && mAdapter.mPreviewManager.drawableMap != null) {
				Iterator<String> iter = mAdapter.mPreviewManager.drawableMap.keySet().iterator();

				while (iter.hasNext()) {
					synchronized (mAdapter.mPreviewManager.drawableMap) {
						String key = iter.next();

						BitmapDrawable value = mAdapter.mPreviewManager.drawableMap.get(key);

						value.getBitmap().recycle();
					}
				}
				mAdapter.mPreviewManager.drawableMap.clear();
				mAdapter.destroy();
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		cleanDrawable();
		if (mGridView != null) {
			mGridView = null;
		}
		System.gc();
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		String[] availableThemes = ThemeUtils.getAvailableThemes(Constant.DEFAULT_THEME_PATH);
		mThemeSizePause = availableThemes == null ? 0 : availableThemes.length;
		ThemeLog.v(TAG, "onPause,mThemeSizeResume=" + mThemeSizeResume + ",mThemeSizePause=" + mThemeSizePause);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mContext == null) {
			mContext = getActivity();
		}
		String[] availableThemes = ThemeUtils.getAvailableThemes(Constant.DEFAULT_THEME_PATH);
		mThemeSizeResume = availableThemes == null ? 0 : availableThemes.length;
		ThemeLog.v(TAG, "onResume,mThemeSizeResume=" + mThemeSizeResume + ",mThemeSizePause=" + mThemeSizePause);
		ThemeLog.v(TAG, "onResume,mbFirstLoad=" + mbFirstLoad);
		if ((mThemeSizeResume > mThemeSizePause) && !mbFirstLoad) {
			mTask = new LoadThemesInfoTask(); 
			mTask.execute();
		} else if (mThemeSizeResume < mThemeSizePause) { // 处于usb 模式
			ThemeUtils.removeNonExistingThemes(getActivity(), availableThemes);
			markAsDone();
		}
		mbFirstLoad = false;
	}

	void markAsDone() {
		mViewUpdateHandler.sendEmptyMessage(0);
	}

	public class PreviewAdapter extends BaseAdapter {
		private Context mContext;

		private PreviewManager mPreviewManager = new PreviewManager();

		private View[] mPreviews;
		private int mPreviewWidth;
		private int mPreviewHeight;

		public PreviewAdapter(Context c) {
			mContext = c;
			int numColumns = getResources().getInteger(R.integer.gridviewNumColumns);
			int spacingTotal = mGridView.getHorizontalSpacing() * (numColumns - 1);
			DisplayMetrics dm = c.getResources().getDisplayMetrics();
			float aspectRatio = 1;
			if (dm.heightPixels > dm.widthPixels)
				aspectRatio = (float) dm.heightPixels / dm.widthPixels;
			else
				aspectRatio = (float) dm.widthPixels / dm.heightPixels;
			spacingTotal = (int) getResources().getDimension(R.dimen.local_gridview_horizontalSpacing_allthemes);
			// mPreviewWidth = dm.widthPixels / numColumns -
			// spacingTotal*(numColumns); //216
			mPreviewWidth = (int) getResources().getDimension(R.dimen.preview_width_all_themes);// 216;
			mPreviewHeight = (int) getResources().getDimension(R.dimen.preview_height_all_themes);// 304;
			ThemeLog.v(TAG, "dm.widthPixels=" + dm.widthPixels + ",dm.heightPixels=" + dm.heightPixels);
			ThemeLog.v(TAG, "spacingTotal=" + spacingTotal);
			ThemeLog.v(TAG, "mPreviewWidth=" + mPreviewWidth + ",mPreviewHeight=" + mPreviewHeight);

			preloadPreviews();
		}

		private void preloadPreviews() {
			mPreviews = new View[mThemesList.size()];
			for (int i = 0; i < mPreviews.length; i++) {
				LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				mPreviews[i] = inflater.inflate(R.layout.all_theme_previews, null);
				mPreviews[i].setId(i);

				RelativeLayout fl = (RelativeLayout) mPreviews[i].findViewById(R.id.preview_layout);
				LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) fl.getLayoutParams();
				params.width = mPreviewWidth;
				params.height = mPreviewHeight;
				// params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
				fl.setLayoutParams(params);

				PreviewHolder holder = new PreviewHolder();
				// holder.preview = (FlipImageView)
				// mPreviews[i].findViewById(R.id.preview_image);
				holder.preview = (ImageView) mPreviews[i].findViewById(R.id.preview_image);
				holder.name = (TextView) mPreviews[i].findViewById(R.id.theme_name);
				holder.usingTag = (ImageView) mPreviews[i].findViewById(R.id.using_indicator);
				// RelativeLayout lp= (RelativeLayout)
				// holder.usingTag.getLayoutParams();
				// if(lp!=null){
				// lp.leftMargin = (int)
				// getActivity().getResources().getDimension(R.dimen.using_tag_marginleft_lock_wallpaper);
				// lp.topMargin = (int)
				// getActivity().getResources().getDimension(R.dimen.using_tag_margintop_lock_wallpaper);
				// }
				// holder.usingTag.setLayoutParams(lp);
				//
				holder.progress = mPreviews[i].findViewById(R.id.loading_indicator);
				holder.index = i;
				mPreviews[i].setTag(holder);
				mPreviewManager.fetchDrawableOnThread(mThemesList.get(i), holder); // 读预览图到
																					// holder
				holder.name.setText(mThemesList.get(i).getTitle());
				holder.preview.setImageResource(R.drawable.empty_preview); // set
																			// default
																			// display
				ThemeLog.v(TAG, "is using ? " + mThemesList.get(i).getIsUsing());
				if (mThemesList.get(i).getIsUsing()) {
					holder.usingTag.setImageResource(R.drawable.ic_theme_focused);
				}
			}

		}

		public int getCount() {
			return mThemesList.size();
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			return mPreviews[position];
		}

		public void destroy() {
			mPreviewManager = null;
			mContext = null;
		}
	}

	private class LoadThemesInfoTask extends AsyncTask<String, Integer, Boolean> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(String... strings) {
			try {

				ThemeUtils.addThemesToDb(getActivity(), false); // create db

			} catch (NullPointerException e) {
				return Boolean.FALSE;
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return Boolean.TRUE;
		}

		@Override
		protected void onPostExecute(Boolean aBoolean) {
			runWhenReady(new Runnable() {
				@Override
				public void run() {
					getActivity().setProgressBarIndeterminateVisibility(false);
					markAsDone(); // 数据库创建完毕后，绑定数据(预览图)到view
				}
			});
		}
	}
}
