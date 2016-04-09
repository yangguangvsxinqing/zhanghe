package com.fineos.notes.view;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.fineos.notes.R;

import java.lang.reflect.Field;

/**
 * Created by wangxiaoyang on 15-7-17.
 */
public class SuperPopUpWindow extends PopupWindow implements PopupWindow.OnDismissListener, View.OnClickListener,
        AdapterView.OnItemClickListener, Animation.AnimationListener {
    /**
     * 上下文
     */
    private Context mContext;

    private SuperPopUpWindow sPopUpWin;
    /**
     * 弹出框布局
     */
    private RelativeLayout popLayout;
    /**
     * 弹出框布局背景
     */
    private View popBg;

    /**
     * 弹出框布局内容
     */
    private View popContentView;
    /**
     * 列表显示内容
     */
    private ListView popListView;
    /**
     * 表格显示内容
     */
    private GridView popGridView;
    /**
     * 用户自定义内容
     */
    private FrameLayout popCustom;

    /**
     * 用来弹出popupWin的view
     */
    private SuperSpinner anchorView;

    /**
     * popupWin的顶部或者底部位置，不设置按照默认情况显示
     */
    private View topView, bottomView;

    /**
     * 列表或表格显示的时候需要用的"空白"
     */
    private int margen = 0;

    private int stateBarHeight = 0;
    private boolean isFullScreen = false;
    /**
     * 用来显示的adapter
     */
    private ListAdapter listAdapter;

    private SuperSpinner.OnPopItemClickListener itemClickListener;

    private Animation viewEnterAnim, viewExitAnim, bgEnter, bgExit;


    /**
     * popupWin 在控件上方弹出
     */
    public static final int POPUP_ABOVE = 0;
    /**
     * popupWin 在控件下方弹出
     */
    public static final int POPUP_BELOW = 1;

    /**
     * 默认向下弹出弹窗
     */
    private int popMode = POPUP_BELOW;

    /**
     * 用列表来展示数据
     */
    public static final int SHOW_DATA_USE_LIST = 0;
    /**
     * 用表格来展示数据
     */
    public static final int SHOW_DATA_USE_GRID = 1;
    /**
     * 用自定义布局来展示数据
     */
    private static final int SHOW_DATA_USE_CUSTOM = 2;
    private int dataMode = SHOW_DATA_USE_LIST;


    /**
     * popupWin 在显示中
     */
    public static final int STATE_SHOW = 0;
    /**
     * popupWin 处于消失的动画中，消失动画做完，那么popupWin就消失
     */
    public static final int STATE_DISMISSING = 1;
    /**
     * popupWin 在已消失
     */
    public static final int STATE_DISMISS = 2;
    private int state = STATE_DISMISS;

    /**
     * popupWin的停靠方式，主要方便内部运算
     */
    private static final int ANCHOR_MODE_TOP = 0;
    private static final int ANCHOR_MODE_BOTTOM = 1;
    private static final int ANCHOR_MODE_DEFAULT = 2;
    private int anchorMode = ANCHOR_MODE_DEFAULT;

    /**
     * 动画的弹出方式
     */
    private static final int ANIM_TOP_BOTTOM = 0;
    private static final int ANIM_BOTTOM_TOP = 1;
    private int animMode = ANIM_TOP_BOTTOM;

    /**
     * popupWin的高度
     */
    private int popHeight = 0;

    private int screenWidth = 0;
    private int screenHeight = 0;

    private SuperSpinner.PopupWinDismissListener dismissListener;


    public SuperPopUpWindow(Context context, int sWidth, int sHeight) {
        super(context);
        mContext = context;
        screenWidth = sWidth;
        screenHeight = sHeight;
        initPopupWindow();
    }

    private SuperPopUpWindow(){
        super();
    }

    private void initPopupWindow() {

        sPopUpWin = new SuperPopUpWindow();

        margen = mContext.getResources().getDimensionPixelOffset(R.dimen.fineos_widget_spinner_margen);
        stateBarHeight = getStateBarHeight(mContext);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        popLayout = (RelativeLayout) inflater.inflate(R.layout.fineos_spinner_popup, null);

        popBg = popLayout.findViewById(R.id.spinnerBg);
        popBg.setOnClickListener(this);

        popContentView = popLayout.findViewById(R.id.popContentView);

        popListView = (ListView) popLayout.findViewById(R.id.popListView);
        popListView.setOnItemClickListener(this);

        popGridView = (GridView) popLayout.findViewById(R.id.popGridView);
        popGridView.setOnItemClickListener(this);

        popCustom = (FrameLayout) popLayout.findViewById(R.id.popCustomView);

        setContentView(popLayout);
        setFocusable(true);
        setBackgroundDrawable(new BitmapDrawable());

        //setTouchable(true);
//        setTouchInterceptor(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                Log.d("wxy", "onTouch event.getAction()= " + event.getAction());
//                if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                    tryDismissPopupWin();
//                    return true;
//                }
//                return false;
//            }
//        });

        setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("wxy", "onTouch event.getAction()= " + event.getAction());
                int a = MotionEvent.ACTION_OUTSIDE;
                return false;
            }
        });

        setOutsideTouchable(true);

        setOnDismissListener(this);
        setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);
        setAnimationStyle(R.style.SuperSpinner);

        loadAnim();

        bgEnter = AnimationUtils.loadAnimation(mContext, R.anim.spinner_bg_enter);
        bgExit = AnimationUtils.loadAnimation(mContext, R.anim.spinner_bg_exit);

        setPopupViewStyle(dataMode);

    }

    /**
     * activity为全屏的时候设置，用于修正计算参数
     *
     * @param isFullScreenMode
     */
    public void setFullScreen(boolean isFullScreenMode) {
        isFullScreen = isFullScreenMode;
    }

    /**
     * 点击外部，popupWin消失
     *
     * @param outsideTouchable
     */
//    public void setOutsideTouchable(boolean outsideTouchable) {
//        setOutsideTouchable(outsideTouchable);
//    }

    /**
     * 设置数据适配器
     *
     * @param adapter
     */
    public void setAdapter(ListAdapter adapter) {
        listAdapter = adapter;
        popListView.setAdapter(listAdapter);
        popGridView.setAdapter(listAdapter);
//        if( listAdapter.getItem(0) instanceof String ) {
//            anchorView.setText((String) listAdapter.getItem(0));
//        }
    }

    /**
     * 设置点击事件的监听
     *
     * @param listener
     */
    public void setOnPopItemClickListener(SuperSpinner.OnPopItemClickListener listener) {
        itemClickListener = listener;
    }

    /**
     * 设置popupWin的停靠view
     *
     * @param view
     */
    public void setAnchorView(SuperSpinner view) {
        anchorView = view;
    }

    /**
     * 设置popupWin的停靠布局的顶部view
     *
     * @param view
     */
    public void setTopAnchor(View view) {
        anchorMode = ANCHOR_MODE_TOP;
        animMode = ANIM_BOTTOM_TOP;
        loadAnim();
        topView = view;
    }

    /**
     * 设置popupWin的停靠布局的底部view
     *
     * @param view
     */
    public void setBottomAnchor(View view) {
        anchorMode = ANCHOR_MODE_BOTTOM;
        animMode = ANIM_TOP_BOTTOM;
        loadAnim();
        bottomView = view;
    }

    /**
     * 设置当前的动画方式
     */
    private void loadAnim() {
        int enterAnim = animMode == ANIM_TOP_BOTTOM ?
                R.anim.spinner_content_top2bottom_enter : R.anim.spinner_content_bottom2top_enter;
        int exitAnim = animMode == ANIM_TOP_BOTTOM ?
                R.anim.spinner_content_top2bottom_exit : R.anim.spinner_content_bottom2top_exit;
        viewEnterAnim = AnimationUtils.loadAnimation(mContext, enterAnim);
        viewExitAnim = AnimationUtils.loadAnimation(mContext, exitAnim);
        viewExitAnim.setAnimationListener(this);
        updateContentParams();
    }

    private void updateContentParams() {
        int addAlign = animMode == ANIM_TOP_BOTTOM ?
                RelativeLayout.ALIGN_PARENT_TOP : RelativeLayout.ALIGN_PARENT_BOTTOM;
        int removeAlign = animMode == ANIM_TOP_BOTTOM ?
                RelativeLayout.ALIGN_PARENT_BOTTOM : RelativeLayout.ALIGN_PARENT_TOP;
        RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) popContentView.getLayoutParams();
        params.addRule(addAlign);
        params.removeRule(removeAlign);
        int topMargen = animMode == ANIM_TOP_BOTTOM ? 0 : margen;
        int bottomMargen = animMode == ANIM_TOP_BOTTOM ? margen : 0;
//        params.setMargins(0, topMargen, 0, bottomMargen);
        params.setMargins(0, topMargen, 0, 0);
        popContentView.setLayoutParams(params);
    }

    /**
     * 设置pop的弹出方式，
     * 如果设置了 TopAnchor 或者 BottomAnchor , 此设置无效
     *
     * @param mode
     */
    public void setShowMode(int mode) {
        popMode = mode;
        if (POPUP_ABOVE == popMode) {
            setAnimMode(ANIM_BOTTOM_TOP);
        } else if (POPUP_BELOW == popMode) {
            setAnimMode(ANIM_TOP_BOTTOM);
        }
    }

    /**
     * 设置自定义布局，来展示自定义的数据
     *
     * @param custom 自定义布局
     */
    public void setCustomView(View custom) {
        popCustom.removeAllViews();
        popCustom.addView(custom);
        setPopupViewStyle(SHOW_DATA_USE_CUSTOM);
    }

    public void setDismissListener( SuperSpinner.PopupWinDismissListener dismissListener ) {
        this.dismissListener = dismissListener;
    }


    /**
     * 设置动画的显示方式
     *
     * @param mode
     */
    private void setAnimMode(int mode) {
        animMode = mode;
        loadAnim();
    }


    /**
     * 设置s数据显示的模式
     *
     * @param styleMode
     */
    public void setPopupViewStyle(int styleMode) {
        dataMode = styleMode;
        popCustom.setVisibility(SHOW_DATA_USE_CUSTOM == dataMode ? View.VISIBLE : View.GONE);
        popGridView.setVisibility(SHOW_DATA_USE_GRID == dataMode ? View.VISIBLE : View.GONE);
        popListView.setVisibility(SHOW_DATA_USE_LIST == dataMode ? View.VISIBLE : View.GONE);
    }

    /**
     * 计算popupWin的高度和宽度
     */
    private void computeWidthAndHeight() {
        Log.d("wxy", "width = " + screenWidth + " ,height = " + screenHeight);
        int tvBottom = 0, bvTop = screenHeight;
        int[] tvBottoms = {0, 0}, bvTops = {0, 0};
        if (ANCHOR_MODE_TOP == anchorMode) {
            topView.getLocationOnScreen(tvBottoms);
            anchorView.getLocationOnScreen(bvTops);
            tvBottom = tvBottoms[1] + topView.getHeight();
            bvTop = bvTops[1];
        } else if (ANCHOR_MODE_BOTTOM == anchorMode) {
            anchorView.getLocationOnScreen(tvBottoms);
            bottomView.getLocationOnScreen(bvTops);
            tvBottom = tvBottoms[1] + anchorView.getHeight();
            bvTop = bvTops[1];
        } else if (ANCHOR_MODE_DEFAULT == anchorMode) {
            if (POPUP_ABOVE == popMode) {
                anchorView.getLocationOnScreen(bvTops);
                tvBottom = stateBarHeight;
                if (isFullScreen) {
                    tvBottom = 0;
                }
                bvTop = bvTops[1];
            } else if (POPUP_BELOW == popMode) {
                anchorView.getLocationOnScreen(tvBottoms);
                tvBottom = tvBottoms[1] + anchorView.getHeight();
                bvTop = screenHeight;
            }
        }
        Log.d("wxy", "bvTop = " + bvTop + " ,tvBottom = " + tvBottom);
        popHeight = bvTop - tvBottom;
        Log.d("wxy", "popHeight = " + popHeight);
        setWidth(screenWidth);
        setHeight(popHeight);
    }

    /**
     * 弹出spinner
     */
    private void showSpinner() {
        // 移动到第一列  这里可能会有bug
        popGridView.setSelection(0);
        popListView.setSelection(0);
//            if (!popListView.isStackFromBottom()) {
//                popListView.setStackFromBottom(true);
//            }
//            popListView.setStackFromBottom(false);
        computeWidthAndHeight();
        int locations[] = {0, 0};
        anchorView.getLocationInWindow(locations);
        int offY = 0;
        int aHeight = anchorView.getHeight();
        if (ANCHOR_MODE_BOTTOM == anchorMode) {
                /*
                |achorView
				|-----------|
				| ---       |
				| ---       |
				| ---       |
				|-----------|
				|bottomView
				 */
            offY = locations[1] + aHeight;
        } else if (ANCHOR_MODE_TOP == anchorMode) {
				/*
				|topView
				|-----------|
				| ---       |
				| ---       |
				| ---       |
				|-----------|
				|achorView
				 */
            offY = locations[1] - popHeight;
        } else if (ANCHOR_MODE_DEFAULT == anchorMode) {
            if (ANIM_TOP_BOTTOM == animMode) {
                    /*
				    |achorView
				    |-----------|
				    | ---       |
				    | ---       |
				    | ---       |
				    |-----------|
				    */
                offY = locations[1] + aHeight;
            } else if (ANIM_BOTTOM_TOP == animMode) {
					/*
				    |-----------|
				    | ---       |
				    | ---       |
				    | ---       |
				    |-----------|
				    |achorView
				    */
                offY = locations[1] - popHeight;
            }
        }
        showAtLocation(anchorView, Gravity.TOP, 0, offY);
        popContentView.startAnimation(viewEnterAnim);
        popBg.startAnimation(bgEnter);
        state = STATE_SHOW;
    }

    public boolean tryShowPopupWin() {
        boolean wasShowing = state == STATE_SHOW;
        boolean isDismissing = state == STATE_DISMISSING;
        if (wasShowing || isDismissing) {
            return false;
        } else {
            showSpinner();
            return true;
        }
    }

    public void tryDismissPopupWin() {
        if (STATE_SHOW == state) {
            popContentView.startAnimation(viewExitAnim);
            popBg.startAnimation(bgExit);
        }
    }

    @Override
    public void onDismiss() {
        state = STATE_DISMISS;

        if( null != dismissListener ) {
            dismissListener.onPopupWinDismiss(anchorView);
        }

    }

    @Override
    public void dismiss() {
        tryDismissPopupWin();
    }

    @Override
    public void onClick(View v) {
        if (v.equals(popBg)) {
            tryDismissPopupWin();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //anchorView.setText((String) listAdapter.getItem(position));
        if( listAdapter.getItem(position) instanceof String ) {

        }

        if (null != itemClickListener) {
            itemClickListener.onPopItemClick(anchorView,position,id);
        }
        tryDismissPopupWin();
    }

    @Override
    public void onAnimationStart(Animation animation) {
        state = STATE_DISMISSING;
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        super.dismiss();
        state = STATE_DISMISS;
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    private int getStateBarHeight(Context context) {
        Class<?> androidDimen = null;
        Object obj = null;
        Field field = null;
        int x = 0, barHeight = 20;
        try {
            androidDimen = Class.forName("com.android.internal.R$dimen");
            obj = androidDimen.newInstance();
            field = androidDimen.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            barHeight = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return barHeight;
    }
}
