package com.android.systemui.statusbar.fifthlock;

import java.util.Calendar;
import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.systemui.R;

/**
 * @ClassName:KeyguardClockView
 * @Description:锁屏时间信息视图{modify for [HQ_BOWAY_KEYGUARD]}
 * @author:BingWu.Lee
 * @date:2015-6-10
 */
public class KeyguardClockView extends RelativeLayout {

    private static final int MSG_UPDATE = 0;
    private static final int LOW_BATTERY_LEVEL = 15;
    private static final String LANGUAGE_CHINA = "zh";

    /** 容器视图 */
    private View mContainerView;
    private TextView mTvTime;
    private TextView mTvDate;
    private BatteryTracker mTracker = new BatteryTracker();
    private boolean isShowBatteryInfo;
    private boolean isLastPlugin;

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (Intent.ACTION_TIME_TICK.equals(action)
                    || Intent.ACTION_TIME_CHANGED.equals(action)
                    || Intent.ACTION_TIMEZONE_CHANGED.equals(action)
                    || Intent.ACTION_LOCALE_CHANGED.equals(action)) {
                if (Intent.ACTION_LOCALE_CHANGED.equals(action)
                        || Intent.ACTION_TIMEZONE_CHANGED.equals(action)) {
                    // need to get a fresh date format
                }
                updateTime();
            }
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE: {
                    // 刷新时间信息
                    isShowBatteryInfo = false;
                    updateTime();
                    break;
                }
            }
        }
    };

    public KeyguardClockView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater inflater = LayoutInflater.from(context);
        mContainerView = inflater.inflate(R.layout.keyguard_dateview, this,
                true);
        mTvTime = (TextView) mContainerView.findViewById(R.id.dateview_tv_time);
        mTvDate = (TextView) mContainerView.findViewById(R.id.dateview_tv_date);
    }

    public KeyguardClockView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KeyguardClockView(Context context) {
        this(context, null);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        getContext().registerReceiver(mIntentReceiver, filter, null, null);
        // 刷新時間显示
        updateTime();
        // 注册电池电量变化广播
        IntentFilter filterBattery = new IntentFilter();
        filterBattery.addAction(Intent.ACTION_BATTERY_CHANGED);
        final Intent sticky = getContext().registerReceiver(mTracker,
                filterBattery);
        if (sticky != null) {
            // 初始化预加载电池信息
            mTracker.onReceive(getContext(), sticky);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getContext().unregisterReceiver(mIntentReceiver);
        getContext().unregisterReceiver(mTracker);
    }
    
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 发生语言变化重新更新时间
        updateTime();
    }
    
    private boolean isLocaleChinese() {
        Locale l = Locale.getDefault();
        if (LANGUAGE_CHINA.equals(l.getLanguage())) {
            return true;
        }
        return false;
    }

    private void updateTime() {
        mTvTime.setText(TimeUtil.getLocalTime(TimeUtil.STR_FORMAT_DATE_TIME));
        Calendar calendar = Calendar.getInstance();
        LunarCalendar lunarCalendar = new LunarCalendar(getContext());
        LunarCalendarConvertUtil.parseLunarCalendar(
                calendar.get(Calendar.YEAR),
                (calendar.get(Calendar.MONTH) + 1),
                calendar.get(Calendar.DAY_OF_MONTH), lunarCalendar);
        String[] str1 = lunarCalendar.getLunarCalendarInfo(false);
        // 当前没有显示电池信息
        if (!isShowBatteryInfo){
            if (isLocaleChinese()) {
                mTvDate.setText(getResources().getString(R.string.date_format,
                        TimeUtil.getLocalTime(TimeUtil.STR_FORMAT_DATE),
                        str1[1], str1[2]));
            } else {
                mTvDate.setText(TimeUtil.getLocalTime(TimeUtil.STR_FORMAT_DATE));
            }
        }
    }

    private class BatteryTracker extends BroadcastReceiver {
        private static final int UNKNOWN_LEVEL = -1;
        // current battery status
        int level = UNKNOWN_LEVEL;
        int plugType;
        boolean isPluggedIn;

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                level = (int) (100f * intent.getIntExtra(
                        BatteryManager.EXTRA_LEVEL, 0) / intent.getIntExtra(
                        BatteryManager.EXTRA_SCALE, 100));

                plugType = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
                isPluggedIn = plugType != 0;

                // 未插入充电器并且电池电量小于低电等级
                if (!isPluggedIn && level < LOW_BATTERY_LEVEL
                        && mTvDate != null) {
                    mTvDate.setText(getResources().getString(
                            R.string.keyguard_low_battery));
                    isShowBatteryInfo = true;
                    // 3S后还原时间显示
                    mHandler.sendEmptyMessageDelayed(MSG_UPDATE, 3000);
                }

                if (isLastPlugin == isPluggedIn) {
                    return;
                }
                isLastPlugin = isPluggedIn;

                if (isPluggedIn && mTvDate != null) {
                    mTvDate.setText(getResources().getString(
                            R.string.keyguard_battery_chargeing, level));
                    isShowBatteryInfo = true;
                    // 3S后还原时间显示
                    mHandler.sendEmptyMessageDelayed(MSG_UPDATE, 3000);
                }
            }
        }
    }

}
