package com.fineos.theme.fragment;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fineos.theme.R;
import com.fineos.theme.activity.ThemeRingtoneActivity;
import com.fineos.theme.download.Downloads;
import com.fineos.theme.model.RingtoneData;
import com.fineos.theme.model.ThemeData;
import com.fineos.theme.utils.Constant;
import com.fineos.theme.utils.ThemeLog;
import com.fineos.theme.utils.ThemeUtils;
import com.fineos.theme.webservice.Request;
import com.fineos.theme.webservice.ThemeService;

public class RingtoneOnlineFragment extends Fragment {
	private static final String TAG = "DownLoadRingtoneFragment";

	private static final int ACTION_ERROR = 0;
	private static final int ACTION_RINGTONE_LIST = 1;

	private static final String RINGTONE_NAME = "ringtone.mp3";
	private static final String NOTIFICATION_NAME = "notification.mp3";

	private boolean bBusy;
	private View mFooterView;
	private View mLoadingIndicator;
	private ImageView mLoadingAnimation;
	private AnimationDrawable loadingAnimation;
	private Request mCurrentRequest;
	private ListView mListView;
	private AbsListView.OnScrollListener mScrollListener;
	private Handler mHandler;
	private RingtoneAdapter mAdapter;
	private ThemeService mThemeService;
	private Context mContext;
	private int mThemeSizePause = 0;
	private int mThemeSizeResume = 0;
	private Boolean mbFirstLoad = false;
	private LoadThemesInfoTask mTask = null;

	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
		if (mAdapter != null && ThemeRingtoneActivity.mMediaPlayer != null) {
			ThemeRingtoneActivity.mMediaPlayer.stop();
		}

	}

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
	}

	private List<ThemeData> mThemesList;

	public RingtoneOnlineFragment() {

	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
//		MediaPlayer mediaPlayer = mAdapter != null ? ThemeRingtoneActivity.mMediaPlayer : null;
//		if (mediaPlayer != null && mediaPlayer.isPlaying()) {
//			mediaPlayer.pause();
//		}
		String[] availableThemes = ThemeUtils.getAvailableThemes(Constant.DEFAULT_THEME_PATH);
		mThemeSizePause = availableThemes == null ? 0 : availableThemes.length;
		ThemeLog.i(TAG, "onPause,mThemeSizeResume=" + mThemeSizeResume + ",mThemeSizePause=" + mThemeSizePause);

	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
//		if (mAdapter != null && ThemeRingtoneActivity.mMediaPlayer != null) {
//			ThemeRingtoneActivity.mMediaPlayer.start();
//		}
		String[] availableThemes = ThemeUtils.getAvailableThemes(Constant.DEFAULT_THEME_PATH);
		mThemeSizeResume = availableThemes == null ? 0 : availableThemes.length;
		ThemeLog.d(TAG, "onResume,mThemeSizeResume=" + mThemeSizeResume + ",mThemeSizePause=" + mThemeSizePause);
		ThemeLog.w(TAG, "onResume,mbFirstLoad=" + mbFirstLoad);
		if ((mThemeSizeResume > mThemeSizePause) && !mbFirstLoad) { // usb?????a3y
			mTask = new LoadThemesInfoTask();
			mTask.execute();
		} else if (!ThemeUtils.isSDCardExist()) {

			ThemeUtils.removeAllThemes(getActivity());
			inflateThemeList();
		}
		mbFirstLoad = false;
		super.onResume();
	}

	private BroadcastReceiver mThemeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			if (mAdapter != null) {
				mAdapter.notifyDataSetChanged();
			}
		}
	};

	private void registerIntentReceivers() {
		IntentFilter intentFilter = new IntentFilter(Downloads.ACTION_DOWNLOAD_COMPLETED_NOTIFICATION);

		mContext.registerReceiver(mThemeReceiver, intentFilter);
	}

	private void unregisterIntentReceivers() {

		mContext.unregisterReceiver(mThemeReceiver);
	}

	private void initListener() {
		// TODO Auto-generated method stub
		mScrollListener = new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				switch (scrollState) {
				case SCROLL_STATE_IDLE:
					break;
				case SCROLL_STATE_TOUCH_SCROLL:
				case SCROLL_STATE_FLING:

					break;
				default:
					break;
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				return;
			}
		};
	}

	private void initHandler() {
		// TODO Auto-generated method stub
		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch (msg.what) {
				case ACTION_RINGTONE_LIST:
					@SuppressWarnings("unchecked")
					List<ThemeData> themeList = (List<ThemeData>) msg.obj;
					mThemesList = themeList;
					// mThemesList =
					// ThemeUtils.getThemeListByType(ThemeData.THEME_ELEMENT_TYPE_RINGTONES,
					// getActivity());
					ThemeLog.i(TAG, "DownLoadRingtoneFragment,mThemesList size=" + mThemesList.size());
					ArrayList<RingtoneData> tmpList = new ArrayList<RingtoneData>();
					for (int i = 0; i < themeList.size(); i++) {
						RingtoneData tmp = new RingtoneData();
						ThemeLog.i(TAG, "getThemePath=" + mThemesList.get(i).getThemePath());
						// tmp.setThemePath(Uri.parse(Environment
						// .getExternalStorageDirectory()
						// + FileManager.RINGTONE_DIR_PATH
						// + "/"
						// + themeList.get(i).getFileName() + ".mp3"));
						tmp.setThemePath(Uri.parse(mThemesList.get(i).getThemePath()));
						tmp.setTitle(themeList.get(i).getTitle());
						tmp.setIsUsing_ringtone(themeList.get(i).getIsUsing_ringtone());

						tmp.setRingtoneDuration(themeList.get(i).getRingtoneDuration());

						tmpList.add(tmp);
					}
					try {
						mAdapter = new RingtoneAdapter(getActivity(), tmpList);

					} catch (NullPointerException e) {

						e.printStackTrace();
					}

					mListView.setAdapter(mAdapter);
					mLoadingIndicator.setVisibility(View.GONE);
					mListView.setVisibility(View.VISIBLE);
					break;

				case ACTION_ERROR:
					mLoadingIndicator.setVisibility(View.GONE);
					// mListView.setVisibility(View.VISIBLE);
					mListView.setVisibility(View.VISIBLE);
					// showDialog(DIALOG_NETWORK_ERROR);
//					Toast.makeText(getActivity(), getActivity().getString(R.string.error_network_low_speed), Toast.LENGTH_LONG).show();
					break;

				default:
					break;
				}
			}
		};
	}

	private void inflateThemeList() {
		// TODO Auto-generated method stub
		Request request = new Request(0, Constant.TYPE_RINGTONE_SDCARD_LIST);
		Object params[] = new Object[1];
		params[0] = mContext;
		ThemeLog.i(TAG, "mContext=" + mContext);
		request.setData(params);
		request.addObserver(new Observer() {

			@Override
			public void update(Observable observable, Object data) {
				// TODO Auto-generated method stub
				ThemeLog.i(TAG, "updata,data=" + data + ", mHandler=" + mHandler);
				if (data != null) {
					Message msg = Message.obtain(mHandler, ACTION_RINGTONE_LIST, data);
					if (mHandler != null) {
						mHandler.sendMessage(msg);
					}

				} else {
					Request request = (Request) observable;
					if (request.getStatus() == Constant.STATUS_ERROR) {
						mHandler.sendEmptyMessage(ACTION_ERROR);
					}
				}
			}
		});
		mCurrentRequest = request;
		mThemeService.getRingtoneList(request);
	}

	private void initView(View view) {
		mLoadingIndicator = view.findViewById(R.id.fullscreen_loading_indicator);
		mLoadingAnimation = (ImageView) mLoadingIndicator.findViewById(R.id.fullscreen_loading);
		mLoadingAnimation.setBackgroundResource(R.anim.loading_anim);
		loadingAnimation = (AnimationDrawable) mLoadingAnimation.getBackground();

		mLoadingAnimation.post(new Runnable() {
			@Override
			public void run() {
				loadingAnimation.start();
			}
		});
		mLoadingIndicator.setVisibility(View.GONE);
		mListView = (ListView) view.findViewById(android.R.id.list);
		mListView.setScrollbarFadingEnabled(false); // disable
		mListView.setVisibility(View.GONE);

		mListView.setOnScrollListener(mScrollListener);
		inflateThemeList();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		initListener();
		mContext = getActivity();
		registerIntentReceivers();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.local_theme_ringtone, container, false);
		mThemeService = ThemeService.getServiceInstance(mContext);
		initHandler();
		// mTask = new LoadThemesInfoTask();
		// mTask.execute();
		mbFirstLoad = true;
		initView(view);
		return view;

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterIntentReceivers();
	}

	class RingtoneAdapter extends ArrayAdapter<RingtoneData> {

		private LayoutInflater mInflater;
		private View.OnClickListener mOnClickListener;
		private ViewHolder viewHolder = null;
		private Hashtable<Integer, ViewHolder> mItemStatusMap;
		private int currentPosition;
		private int phonePosition;
		private int smsPosition;
		private int alarmPosition;

		public RingtoneAdapter(Context context, ArrayList<RingtoneData> objects) {

			super(context, 0, objects);
			// TODO Auto-generated constructor stub
			mInflater = LayoutInflater.from(context);
			mItemStatusMap = new Hashtable<Integer, ViewHolder>();
			currentPosition = -1;
			phonePosition = -1;
			smsPosition = -1;
			alarmPosition = -1;

			mOnClickListener = new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					onOperateClick(v);
				}
			};
		}

		void onOperateClick(View view) {
			Drawable mThumb;
			ViewHolder viewHolder = (ViewHolder) view.getTag();
			int mColor = mContext.getResources().getColor(R.color.ringtone_text_color);
			int mHighlightColor = mContext.getResources().getColor(R.color.ringtone_highlight_color);

			if (viewHolder != null) {

				switch (view.getId()) {

				case R.id.ringtone_list_item_area:

					ThemeLog.i(TAG, "ringtone_list_item_area clicked !!");
					ThemeLog.i(TAG, "play button state:" + viewHolder.mBtnPlay.getVisibility());
					ThemeLog.i(TAG, "ex state:" + viewHolder.mListItemEX.getVisibility());
					ThemeLog.d(TAG, "getFirstVisiblePosition =" + mListView.getFirstVisiblePosition());
					if (viewHolder.mBtnPlay.getVisibility() == View.GONE || viewHolder.mListItemEX.getVisibility() == View.GONE) {
						// show current select
						viewHolder.mListItemEX.setVisibility(View.VISIBLE);
						viewHolder.mBtnPlay.setVisibility(View.VISIBLE);
						ThemeLog.i(TAG, "viewHolder.mListItem.getY() =" + viewHolder.mListItem.getY());
						if (mListView.getLastVisiblePosition() == viewHolder.position) {
							int res = mContext.getResources().getDimensionPixelSize(R.dimen.ringtone_list_item_area_ex_height);
							int height = mListView.getHeight() - viewHolder.mListItem.getHeight() - res;
							mListView.setSelectionFromTop(viewHolder.position, height);
						}
						currentPosition = viewHolder.position;
					} else {
						viewHolder.mBtnPlay.setVisibility(View.GONE); // show
																		// current
																		// select
						viewHolder.mListItemEX.setVisibility(View.GONE);
						currentPosition = -1;
					}

					if (mItemStatusMap.get(R.id.ringtone_list_item_area) != null) {

						ViewHolder lastViewHolder = mItemStatusMap.get(R.id.ringtone_list_item_area);
						if (lastViewHolder.position != viewHolder.position) {
							lastViewHolder.mBtnPlay.setVisibility(View.GONE); // hide
																				// last
																				// selected
							lastViewHolder.mListItemEX.setVisibility(View.GONE);
						}

					}

					ThemeRingtoneActivity.stopMusic();

					mItemStatusMap.put(R.id.ringtone_list_item_area, viewHolder);
					break;

				case R.id.ringtone_play:

					ThemeLog.i(TAG, "play button clicked !!");
					if (!ThemeRingtoneActivity.isPlaying()) {
						ThemeRingtoneActivity.playMusic((RingtoneData) viewHolder.mListItemEX.getTag());
					} else {
						ThemeRingtoneActivity.stopMusic();
					}
					break;
				case R.id.ringtone_phone_flag_area:
					if (mItemStatusMap.get(R.id.ringtone_phone_flag_area) != null) {
						ViewHolder lastViewHolder = mItemStatusMap.get(R.id.ringtone_phone_flag_area);
						lastViewHolder.mPhoneFlag.setVisibility(View.GONE);
						mThumb = (Drawable) mContext.getResources().getDrawable(R.drawable.ic_rtset_phone);
						lastViewHolder.mListItemPhoneImg.setImageDrawable(mThumb);
						lastViewHolder.mListItemPhoneTextView.setTextColor(mColor);
					}
					viewHolder.mPhoneFlag.setVisibility(View.VISIBLE);
					phonePosition = viewHolder.phonePosition;
					mThumb = (Drawable) mContext.getResources().getDrawable(R.drawable.ic_rtset_phone_focused);
					viewHolder.mListItemPhoneImg.setImageDrawable(mThumb);

					viewHolder.mListItemPhoneTextView.setTextColor(mHighlightColor);
					mItemStatusMap.put(R.id.ringtone_phone_flag_area, viewHolder);
					setRingtone((RingtoneData) viewHolder.mListItemEX.getTag(), 1);
					break;
				case R.id.ringtone_sms_flag_area:
					if (mItemStatusMap.get(R.id.ringtone_sms_flag_area) != null) {
						ViewHolder lastViewHolder = mItemStatusMap.get(R.id.ringtone_sms_flag_area);
						lastViewHolder.mSMSFlag.setVisibility(View.GONE);
						mThumb = (Drawable) mContext.getResources().getDrawable(R.drawable.ic_rtset_notice);
						lastViewHolder.mListItemSmsImg.setImageDrawable(mThumb);
						lastViewHolder.mListItemSmsTextView.setTextColor(mColor);
					}
					viewHolder.mSMSFlag.setVisibility(View.VISIBLE);
					smsPosition = viewHolder.smsPosition;
					viewHolder.mListItemSmsTextView.setTextColor(mHighlightColor);
					mThumb = (Drawable) mContext.getResources().getDrawable(R.drawable.ic_rtset_notice_focused);
					viewHolder.mListItemSmsImg.setImageDrawable(mThumb);
					mItemStatusMap.put(R.id.ringtone_sms_flag_area, viewHolder);
					setRingtone((RingtoneData) viewHolder.mListItemEX.getTag(), 2);
					break;
				case R.id.ringtone_alarm_flag_area:
					if (mItemStatusMap.get(R.id.ringtone_alarm_flag_area) != null) {
						ViewHolder lastViewHolder = mItemStatusMap.get(R.id.ringtone_alarm_flag_area);
						lastViewHolder.mAlarmFlag.setVisibility(View.GONE);
						mThumb = (Drawable) mContext.getResources().getDrawable(R.drawable.ic_rtset_alarm);
						lastViewHolder.mListItemAlarmImg.setImageDrawable(mThumb);
						lastViewHolder.mListItemAlarmTextView.setTextColor(mColor);
					}
					viewHolder.mAlarmFlag.setVisibility(View.VISIBLE);
					alarmPosition = viewHolder.alarmPosition;
					viewHolder.mListItemAlarmTextView.setTextColor(mHighlightColor);
					mThumb = (Drawable) mContext.getResources().getDrawable(R.drawable.ic_rtset_alarm_focused);
					viewHolder.mListItemAlarmImg.setImageDrawable(mThumb);
					mItemStatusMap.put(R.id.ringtone_alarm_flag_area, viewHolder);
					setRingtone((RingtoneData) viewHolder.mListItemEX.getTag(), 3);
					break;
				case R.id.ringtone_delete_flag_area:
					break;
				default:
					break;
				}
			}
		}

		private String getPlayDuration(int ringtoneDuration) {

			int duration = ringtoneDuration / 1000; // ms to s

			int min = duration / 60;
			int sec = duration % 60;
			String sMin = String.valueOf(min);
			String sSec = String.valueOf(sec);
			if (min < 10) {
				sMin = "0" + sMin;
			}
			if (sec < 10) {
				sSec = "0" + sSec;
			}
			if (min <= 0 && sec <= 0) { // at last 1 second
				sMin = "00";
				sSec = "01";
			}
			return sMin + ":" + sSec;

		}

		private void setRingtone(RingtoneData ringtoneinfo, int type) {
			ContentValues values = new ContentValues();
			values.put(MediaStore.MediaColumns.DATA, ringtoneinfo.getThemePath().toString());
			values.put(MediaStore.MediaColumns.TITLE, ringtoneinfo.getTitle());
			values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");
			values.put(MediaStore.Audio.Media.ARTIST, "fineos ");
			if (type == 1) {
				values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
			} else if (type == 2) {
				values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
			} else if (type == 3) {
				values.put(MediaStore.Audio.Media.IS_ALARM, true);
			}
			Uri uri = MediaStore.Audio.Media.getContentUriForPath(ringtoneinfo.getThemePath().toString());
			Log.e(TAG, "uri =" + uri);
			Cursor c = mContext.getContentResolver().query(uri, new String[] { MediaStore.MediaColumns._ID }, MediaStore.MediaColumns.DATA + "=\"" + ringtoneinfo.getThemePath() + "\"", null, null);
			Log.e(TAG, "c =" + c);
			String tm = null;
			Uri newUri = null;
			if (c.moveToFirst()) {
				tm = c.getString(0);
				if (tm != null) {
					newUri = Uri.parse(uri + "/" + tm);
				} else {
					newUri = mContext.getContentResolver().insert(uri, values);
				}
			}

			ThemeLog.i(TAG, "tm =" + tm);
			ThemeLog.i(TAG, "newUri =" + newUri);
			// mContext.getContentResolver().delete(
			// uri,
			// MediaStore.MediaColumns.DATA + "=\""
			// + ringtoneinfo.getThemePath() + "\"", null);
			// Uri newUri = mContext.getContentResolver().insert(uri, values);
			mContext.getContentResolver().update(uri, values, MediaStore.MediaColumns.DATA + "=\"" + ringtoneinfo.getThemePath() + "\"", null);
			if (tm != null) {
				if (type == 1) {
					RingtoneManager.setActualDefaultRingtoneUri(mContext, RingtoneManager.TYPE_RINGTONE, newUri);
				}
				if (type == 2) {
					RingtoneManager.setActualDefaultRingtoneUri(mContext, RingtoneManager.TYPE_NOTIFICATION, newUri);
				}
				if (type == 3) {
					RingtoneManager.setActualDefaultRingtoneUri(mContext, RingtoneManager.TYPE_ALARM, newUri);
				}

			}
			Toast.makeText(mContext, mContext.getString(R.string.ringtone_set_success), Toast.LENGTH_LONG).show();
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return ((long) getItem(position).getId());
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			RingtoneData ringtoneinfo = getItem(position);
			long themeId = 0;
			if (ringtoneinfo.getId() != 0) {
				themeId = ringtoneinfo.getId();
			}
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.local_ringtone_list_item, parent, false);
				viewHolder = new ViewHolder();

				viewHolder.mListItem = (RelativeLayout) convertView.findViewById(R.id.ringtone_list_item_area);
				viewHolder.mListItem.setOnClickListener(mOnClickListener);
				viewHolder.mRingtoneTitle = (TextView) convertView.findViewById(R.id.ringtone_title);
				viewHolder.mBtnPlay = (ImageView) convertView.findViewById(R.id.ringtone_play);

				viewHolder.mRingtoneDuration = (TextView) convertView // ringtone
																		// duration
						.findViewById(R.id.ringtone_duration);

				viewHolder.mBtnPlay.setOnClickListener(mOnClickListener);

				viewHolder.mPhoneFlag = (ImageView) convertView.findViewById(R.id.ringtone_phone_flag);
				viewHolder.mSMSFlag = (ImageView) convertView.findViewById(R.id.ringtone_sms_flag);
				viewHolder.mAlarmFlag = (ImageView) convertView.findViewById(R.id.ringtone_alarm_flag);

				viewHolder.mListItemEX = (LinearLayout) convertView // second
																	// line
						.findViewById(R.id.ringtone_list_item_area_ex);
				viewHolder.mListItemEX.setVisibility(View.GONE);

				viewHolder.mListItemPhone = (RelativeLayout) convertView.findViewById(R.id.ringtone_phone_flag_area);
				viewHolder.mListItemSms = (RelativeLayout) convertView.findViewById(R.id.ringtone_sms_flag_area);
				viewHolder.mListItemAlarm = (RelativeLayout) convertView.findViewById(R.id.ringtone_alarm_flag_area);
				viewHolder.mListItemDelete = (RelativeLayout) convertView // delete
																			// area
						.findViewById(R.id.ringtone_delete_flag_area);

				viewHolder.mListItemPhoneImg = (ImageView) convertView.findViewById(R.id.ringtone_phone_flag_img);
				viewHolder.mListItemSmsImg = (ImageView) convertView.findViewById(R.id.ringtone_sms_flag_img);
				viewHolder.mListItemAlarmImg = (ImageView) convertView.findViewById(R.id.ringtone_alarm_flag_img);

				viewHolder.mListItemPhoneTextView = (TextView) convertView.findViewById(R.id.ringtone_phone_flag_text);
				viewHolder.mListItemSmsTextView = (TextView) convertView.findViewById(R.id.ringtone_sms_flag_text);
				viewHolder.mListItemAlarmTextView = (TextView) convertView.findViewById(R.id.ringtone_alarm_flag_text);

				viewHolder.mListItemPhone.setOnClickListener(mOnClickListener);
				viewHolder.mListItemSms.setOnClickListener(mOnClickListener);
				viewHolder.mListItemAlarm.setOnClickListener(mOnClickListener);
				viewHolder.mListItemDelete.setOnClickListener(mOnClickListener);

				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			viewHolder.mListItem.setTag(viewHolder);
			viewHolder.mBtnPlay.setTag(viewHolder);
			viewHolder.mListItemPhone.setTag(viewHolder);
			viewHolder.mListItemSms.setTag(viewHolder);
			viewHolder.mListItemAlarm.setTag(viewHolder);
			viewHolder.mListItemDelete.setTag(viewHolder);
			viewHolder.mListItemEX.setTag(ringtoneinfo);
			viewHolder.position = viewHolder.phonePosition = viewHolder.smsPosition = viewHolder.alarmPosition = position;

			viewHolder.mBtnPlay.setVisibility(View.GONE);
			viewHolder.mListItemEX.setVisibility(View.GONE);
			viewHolder.mPhoneFlag.setVisibility(View.GONE);
			viewHolder.mSMSFlag.setVisibility(View.GONE);
			viewHolder.mAlarmFlag.setVisibility(View.GONE);

			Drawable mPhoneThumb = (Drawable) mContext.getResources().getDrawable(R.drawable.ic_rtset_phone);
			Drawable mSmsThumb = (Drawable) mContext.getResources().getDrawable(R.drawable.ic_rtset_notice);
			Drawable mAlarmThumb = (Drawable) mContext.getResources().getDrawable(R.drawable.ic_rtset_alarm);

			int mPhoneColor = mContext.getResources().getColor(R.color.ringtone_text_color);
			int mSmsColor = mContext.getResources().getColor(R.color.ringtone_text_color);
			int mAlarmColor = mContext.getResources().getColor(R.color.ringtone_text_color);

			if (currentPosition == position) { // enable visible when select
				viewHolder.mListItemEX.setVisibility(View.VISIBLE);
				viewHolder.mBtnPlay.setVisibility(View.VISIBLE);
			}
			if (phonePosition == position) {
				mPhoneThumb = (Drawable) mContext.getResources().getDrawable(R.drawable.ic_rtset_phone_focused);
				viewHolder.mPhoneFlag.setVisibility(View.VISIBLE);
				mPhoneColor = mContext.getResources().getColor(R.color.ringtone_highlight_color);
				mItemStatusMap.put(R.id.ringtone_phone_flag_area, viewHolder);
			}

			if (smsPosition == position) {
				mSmsThumb = (Drawable) mContext.getResources().getDrawable(R.drawable.ic_rtset_sms_focused);
				viewHolder.mSMSFlag.setVisibility(View.VISIBLE);
				mSmsColor = mContext.getResources().getColor(R.color.ringtone_highlight_color);
				mItemStatusMap.put(R.id.ringtone_sms_flag_area, viewHolder);
			}

			if (alarmPosition == position) {
				mAlarmThumb = (Drawable) mContext.getResources().getDrawable(R.drawable.ic_rtset_alarm_focused);
				viewHolder.mAlarmFlag.setVisibility(View.VISIBLE);
				mAlarmColor = mContext.getResources().getColor(R.color.ringtone_highlight_color);
				mItemStatusMap.put(R.id.ringtone_alarm_flag_area, viewHolder);
			}
			viewHolder.mListItemPhoneImg.setImageDrawable(mPhoneThumb);
			viewHolder.mListItemSmsImg.setImageDrawable(mSmsThumb);
			viewHolder.mListItemAlarmImg.setImageDrawable(mAlarmThumb);

			viewHolder.mListItemPhoneTextView.setTextColor(mPhoneColor);
			viewHolder.mListItemSmsTextView.setTextColor(mSmsColor);
			viewHolder.mListItemAlarmTextView.setTextColor(mAlarmColor);
			viewHolder.mRingtoneTitle.setText(ringtoneinfo.getTitle());
			viewHolder.mRingtoneDuration.setText(getPlayDuration(ringtoneinfo.getRingtoneDuration()));
			return convertView;

		}

		class ViewHolder {
			int position;
			int phonePosition;
			int smsPosition;
			int alarmPosition;
			RelativeLayout mListItem;
			TextView mRingtoneTitle;
			TextView mRingtoneDuration;

			ImageView mBtnPlay;
			ImageView mPhoneFlag;
			ImageView mSMSFlag;
			ImageView mAlarmFlag;

			LinearLayout mListItemEX;
			RelativeLayout mListItemPhone;
			RelativeLayout mListItemSms;
			RelativeLayout mListItemAlarm;
			RelativeLayout mListItemDelete;

			ImageView mListItemPhoneImg;
			TextView mListItemPhoneTextView;
			ImageView mListItemSmsImg;
			TextView mListItemSmsTextView;
			ImageView mListItemAlarmImg;
			TextView mListItemAlarmTextView;
		}
	}/**/

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
			inflateThemeList();// refresh ui when back
		}
	}

}