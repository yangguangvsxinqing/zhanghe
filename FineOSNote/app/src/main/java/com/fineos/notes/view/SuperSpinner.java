package com.fineos.notes.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListAdapter;
import android.widget.TextView;


public class SuperSpinner extends TextView implements View.OnClickListener {

    /** popupWin 在控件上方弹出 */
    public static final int POPUP_ABOVE = SuperPopUpWindow.POPUP_ABOVE;
    /** popupWin 在控件下方弹出 */
    public static final int POPUP_BELOW = SuperPopUpWindow.POPUP_BELOW;

    /** 用列表来展示数据 */
    public static final int STYLE_LIST = SuperPopUpWindow.SHOW_DATA_USE_LIST;
    /** 用表格来展示数据 */
    public static final int STYLE_GRID = SuperPopUpWindow.SHOW_DATA_USE_GRID;

    /** popupWin 在显示中 */
    public static final int STATE_SHOW = SuperPopUpWindow.STATE_SHOW;
    /** popupWin 处于消失的动画中，消失动画做完，那么popupWin就消失 */
    public static final int STATE_DISMISSING = SuperPopUpWindow.STATE_DISMISSING;
    /** popupWin 在已消失 */
    public static final int STATE_DISMISS = SuperPopUpWindow.STATE_DISMISS;

    private int width,height;

    //private SpinnerPopup spinner;
    private SuperPopUpWindow superWindow;

    public SuperSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initSpinnerPopup();
    }

    public SuperSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        initSpinnerPopup();
    }

    public SuperSpinner(Context context) {
        super(context);
        initSpinnerPopup();
    }

    private void initSpinnerPopup() {
        super.setOnClickListener(this);
        setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);

        DisplayMetrics metric = new DisplayMetrics();
        WindowManager wm = (WindowManager) getContext()
                .getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metric);
        width = metric.widthPixels; // 屏幕宽度（像素）
        height = metric.heightPixels; // 屏幕高度（像素）

        superWindow = new SuperPopUpWindow(getContext(),width,height);
        superWindow.setAnchorView(this);
    }

    @Override
    public void onClick(View v) {
        if (null != superWindow && superWindow.tryShowPopupWin() ) {
            return;
        }
    }

    public void showPopupWin() {
        if (null != superWindow && superWindow.tryShowPopupWin() ) {
            return;
        }
    }

    public void tryDissMissTouchOutSide() {
        superWindow.tryDismissPopupWin();
    }

    public void dismissPopupWin(){
        superWindow.tryDismissPopupWin();
    }


    /**
     * 设置popupWin顶部停靠view
     * @param topView
     */
    public void setTopAnchor(View topView) {
        superWindow.setTopAnchor(topView);
    }

    /**
     * 设置popupWin底部停靠view
     * @param bottomView
     */
    public void setBottomAnchor(View bottomView) {
        superWindow.setBottomAnchor(bottomView);
    }

    /**
     * 设置数据适配器
     * @param adapter
     */
    public void setAdapter(ListAdapter adapter) {
        if (null == superWindow) {
            initSpinnerPopup();
        }
        superWindow.setAdapter(adapter);
    }

    /**
     * 设置pop的弹出方式
     * {@POPUP_ABOVE} 在控件上方弹出
     * {@POPUP_BELOW} 在控件下方弹出
     * 如果设置了 TopAnchor 或者 BottomAnchor , 此设置无效
     * @param showMode
     */
    public void setShowMode(int showMode) {
        superWindow.setShowMode(showMode);
    }

    /**
     * 设置popupWin的数据展示方式
     * {@STYLE_LIST} 以列表来展示数据
     * {@STYLE_GRID} 以表格来展示数据
     * @param viewStyle
     */
    public void setPopupViewStyle(int viewStyle) {
        superWindow.setPopupViewStyle(viewStyle);
    }

    /**
     * 当acitivity为全屏时需要设置
     * @param fullScreen
     */
    public void setFullScreen( boolean fullScreen ) {
        superWindow.setFullScreen(fullScreen);
    }

    /**
     * 设置自定义布局
     * @param customView
     */
    public void setCustomView( View customView ) {
        superWindow.setCustomView(customView);
    }

    /**
     * 绑定列表点击事件
     * @param itemClickListener
     */
    public void setOnPopItemClickListener(OnPopItemClickListener itemClickListener) {
        superWindow.setOnPopItemClickListener(itemClickListener);
    }

    /**
     * 点击外部，popupWin消失
     * @param outsideTouchable
     */
    public void setOutsideTouchable( boolean outsideTouchable ) {
        superWindow.setOutsideTouchable(outsideTouchable);
    }

    public void setDismissListener( SuperSpinner.PopupWinDismissListener dismissListener ) {
        superWindow.setDismissListener(dismissListener);
    }


//    /**
//     * 是否需要拦截返回事件，需要的话，通知Activity拦截，
//     * 并尝试隐藏popupwin
//     * @return
//     */
//    public boolean tryInterceptorBack() {
//        return superWindow.tryInterceptorBack();
//    }

    public interface OnPopItemClickListener {
        public void onPopItemClick(SuperSpinner superSpinner, int position, long id);
    }

    public interface PopupWinDismissListener {
        public void onPopupWinDismiss(SuperSpinner superSpinner);
    }
}
