package com.fineos.theme.fragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.fineos.theme.model.RingtoneData;
import com.fineos.theme.utils.Constant;
import com.fineos.theme.utils.ThemeLog;
import com.fineos.theme.webservice.Request;
import com.fineos.theme.webservice.ThemeService;

public class RingtoneLocalFragment extends Fragment {
	private static final String TAG = "LocalRingtoneFragment";

	private static final int ACTION_ERROR = 0;
	private static final int ACTION_RINGTONE_LIST = 1;

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

	public RingtoneLocalFragment() {

	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
//		MediaPlayer mediaPlayer = mAdapter != null ? ThemeRingtoneActivity.mMediaPlayer : null;
//		if (mediaPlayer != null && mediaPlayer.isPlaying()) {
//			mediaPlayer.pause();
//		}
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
//		if (mAdapter != null && ThemeRingtoneActivity.mMediaPlayer != null) {
//			ThemeRingtoneActivity.mMediaPlayer.start();
//		}

		super.onResume();
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
					ArrayList<Ringtone> themeList = (ArrayList<Ringtone>) msg.obj;
					ArrayList<RingtoneData> tmpList = new ArrayList<RingtoneData>();
					for (int i = 0; i < themeList.size(); i++) {
						boolean illegalFlag = false;
						RingtoneData tmp = new RingtoneData();
						if (ThemeRingtoneActivity.mMediaPlayer != null) {
							try {

								ThemeRingtoneActivity.mMediaPlayer.reset();
								ThemeRingtoneActivity.mMediaPlayer.setDataSource(themeList.get(i).getUri().toString());
								ThemeRingtoneActivity.mMediaPlayer.prepare();

							} catch (IllegalArgumentException e) {
								// TODO Auto-generated catch block
								illegalFlag = true;
								e.printStackTrace();
							} catch (SecurityException e) {
								// TODO Auto-generated catch block
								illegalFlag = true;
								e.printStackTrace();
							} catch (IllegalStateException e) {
								// TODO Auto-generated catch block
								illegalFlag = true;
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								illegalFlag = true;
								e.printStackTrace();
							}

							if (!illegalFlag) {
								int duration = ThemeRingtoneActivity.mMediaPlayer.getDuration();
								tmp.setRingtoneDuration(duration);
								tmp.setTitle(themeList.get(i).getTitle(mContext));
								tmp.setThemePath(themeList.get(i).getUri());
								tmpList.add(tmp);
							}
						}
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
		Request request = new Request(0, Constant.TYPE_RINGTONE_LOCAL_LIST);
		Object params[] = new Object[1];
		params[0] = mContext;
		ThemeLog.e(TAG, "mContext=" + mContext);
		request.setData(params);
		request.addObserver(new Observer() {

			@Override
			public void update(Observable observable, Object data) {
				// TODO Auto-generated method stub
				if (data != null) {
					Message msg = Message.obtain(mHandler, ACTION_RINGTONE_LIST, data);
					mHandler.sendMessage(msg);
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
		mListView.setScrollbarFadingEnabled(true);
		mListView.setVisibility(View.GONE);

		// View emptyView = view.findViewById(R.id.list_empty);
		// mListView.setEmptyView(emptyView);

		mListView.setOnScrollListener(mScrollListener);
		inflateThemeList();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		initListener();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mContext = getActivity();
		View view = inflater.inflate(R.layout.local_theme_ringtone, container, false);
		mThemeService = ThemeService.getServiceInstance(mContext);
		initHandler();
		initView(view);
		return view;

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
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
			currentPosition = -1;
			phonePosition = -1;
			smsPosition = -1;
			alarmPosition = -1;
			mItemStatusMap = new Hashtable<Integer, ViewHolder>();
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

					ThemeLog.e(TAG, "play button state:" + viewHolder.mBtnPlay.getVisibility());
					ThemeLog.e(TAG, "ex state:" + viewHolder.mListItemEX.getVisibility());
					if (viewHolder.mBtnPlay.getVisibility() == View.GONE || viewHolder.mListItemEX.getVisibility() == View.GONE) {
						viewHolder.mBtnPlay.setVisibility(View.VISIBLE); // show
																			// current
																			// select
						viewHolder.mListItemEX.setVisibility(View.VISIBLE);
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
					// playRingtone((RingtoneData)
					// viewHolder.mListItemEX.getTag());
					if (!ThemeRingtoneActivity.isPlaying()) {
						ThemeRingtoneActivity.playMusic((RingtoneData) viewHolder.mListItemEX.getTag());
					} else {
						ThemeRingtoneActivity.stopMusic();
					}
					ThemeLog.i(TAG, "ringtone_play =" + ((RingtoneData) viewHolder.mListItemEX.getTag()).getThemePath());
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
					setRingtone((RingtoneData) viewHolder.mListItemEX.getTag(), R.id.ringtone_phone_flag_area);
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
					setRingtone((RingtoneData) viewHolder.mListItemEX.getTag(), R.id.ringtone_sms_flag_area);
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
					setRingtone((RingtoneData) viewHolder.mListItemEX.getTag(), R.id.ringtone_alarm_flag_area);
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

		private void stopMusic() {

			if (ThemeRingtoneActivity.mMediaPlayer != null && ThemeRingtoneActivity.mMediaPlayer.isPlaying()) {
				ThemeRingtoneActivity.mMediaPlayer.stop();
			}
		}

		private Boolean isPlaying() {
			return ThemeRingtoneActivity.mMediaPlayer != null ? ThemeRingtoneActivity.mMediaPlayer.isPlaying() : false;
		}

		private void setRingtone(RingtoneData ringtoneinfo, int ringtoneFlag) {
			ThemeLog.i(TAG, "ringtoneinfo.getThemePath() =" + ringtoneinfo.getThemePath());
			switch (ringtoneFlag) {
			case R.id.ringtone_phone_flag_area:
				RingtoneManager.setActualDefaultRingtoneUri(mContext, RingtoneManager.TYPE_RINGTONE, ringtoneinfo.getThemePath());
				break;
			case R.id.ringtone_sms_flag_area:
				RingtoneManager.setActualDefaultRingtoneUri(mContext, RingtoneManager.TYPE_NOTIFICATION, ringtoneinfo.getThemePath());
				break;
			case R.id.ringtone_alarm_flag_area:
				RingtoneManager.setActualDefaultRingtoneUri(mContext, RingtoneManager.TYPE_ALARM, ringtoneinfo.getThemePath());
				break;
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
				// convertView = mLayoutInflater.inflate(R.layout.app_list_item,
				// null);
				convertView = mInflater.inflate(R.layout.local_ringtone_list_item, parent, false);
				viewHolder = new ViewHolder();

				// viewHolder.mParent = convertView;
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

				viewHolder.mListItemEX = (LinearLayout) convertView.findViewById(R.id.ringtone_list_item_area_ex);
				viewHolder.mListItemEX.setVisibility(View.GONE);
				viewHolder.mListItemPhone = (RelativeLayout) convertView.findViewById(R.id.ringtone_phone_flag_area);
				viewHolder.mListItemSms = (RelativeLayout) convertView.findViewById(R.id.ringtone_sms_flag_area);
				viewHolder.mListItemAlarm = (RelativeLayout) convertView.findViewById(R.id.ringtone_alarm_flag_area);
				viewHolder.mListItemDelete = (RelativeLayout) convertView.findViewById(R.id.ringtone_delete_flag_area);

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
			if (currentPosition == position) {
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
				mSmsThumb = (Drawable) mContext.getResources().getDrawable(R.drawable.ic_rtset_notice_focused);
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
}