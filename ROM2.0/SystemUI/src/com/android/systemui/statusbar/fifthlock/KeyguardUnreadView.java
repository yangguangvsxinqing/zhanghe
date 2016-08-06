package com.android.systemui.statusbar.fifthlock;

import android.content.Context;
import android.database.ContentObserver;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.systemui.R;

/**
 * @ClassName:KeyguardUnreadView
 * @Description:锁屏未读事件视图{modify for [HQ_BOWAY_KEYGUARD]}
 * @author:BingWu.Lee
 * @date:2015-6-10
 */
public class KeyguardUnreadView extends FrameLayout {

    private static final String TAG = "KeyguardUnreadView";

    public static final String KEY_PHONE = "com_android_contacts_mtk_unread";
    public static final String KEY_MMS = "com_android_mms_mtk_unread";

    private static final int MAX_COUNT = 99;
    private static final String MAX_COUNT_STRING = "99+";

    private int mCount = 0;
    boolean mAttachedToWindow = false;
    private String mNumberText;
    private String mKey;
    private ImageView mUnReadImageView;
    private TextView mUnReadTextView;

    Runnable mSetNumberRunnable = new Runnable() {
        @Override
        public void run() {
            setNumberImp(mCount);
        }
    };

    public KeyguardUnreadView(Context context) {
        this(context, null);
    }

    public KeyguardUnreadView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KeyguardUnreadView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater layouterInfalter = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layouterInfalter.inflate(R.layout.keyguard_unread_view,
                null);
        mUnReadImageView = (ImageView) view
                .findViewById(R.id.keyguard_unread_iv_icon);
        mUnReadTextView = (TextView) view
                .findViewById(R.id.keyguard_unread_tv_num);
        // / [ALPS01425873] Background is dirty
        setLayerType(LAYER_TYPE_HARDWARE, null);
        // setDrawingCacheEnabled(true);
        addView(view);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        mAttachedToWindow = true;
        registerUnReadEventObserver();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(mSetNumberRunnable);
        mAttachedToWindow = false;
        unRegisterUnReadEventObserver();
    }

    public void init(int drawableId, String key) {
        Drawable drawable = getContext().getResources().getDrawable(drawableId);
        mUnReadImageView.setImageDrawable(drawable);
        setPivotY(getMeasuredHeight() * 0.5f);
        setPivotX(getMeasuredWidth() * 0.5f);
        // 默认初始化一次
        this.mKey = key;
        int unReadCount = Settings.System.getInt(getContext()
                .getContentResolver(), mKey, 0);
        setNumberImp(unReadCount);
    }

    public void setNumber(int count) {
        if (mAttachedToWindow == true) {
            mCount = count;
            this.post(mSetNumberRunnable);
        }
    }

    private final void setNumberImp(int count) {
        if (count > MAX_COUNT) {
            mNumberText = MAX_COUNT_STRING;
        } else if (count > 0) {
            mNumberText = Integer.toString(count);
        } else {
            mUnReadTextView.setVisibility(View.GONE);
            return;
        }
        mUnReadTextView.setVisibility(View.VISIBLE);
        mUnReadTextView.setText(mNumberText);
    }

    private void registerUnReadEventObserver() {
        if (!StringUtil.isEmpty(mKey)) {
            getContext().getContentResolver().registerContentObserver(
                    Settings.System.getUriFor(mKey), true, mObserver);
        }
    }

    private void unRegisterUnReadEventObserver() {
        if (mObserver != null) {
            getContext().getContentResolver().unregisterContentObserver(
                    mObserver);
        }
    }

    /**
     * Observer监听
     */
    private ContentObserver mObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            int unReadCount = Settings.System.getInt(getContext()
                    .getContentResolver(), mKey, 0);
            setNumberImp(unReadCount);
        }
    };
}
