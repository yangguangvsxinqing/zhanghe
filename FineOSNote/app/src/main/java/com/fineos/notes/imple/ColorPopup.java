package com.fineos.notes.imple;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.fineos.notes.R;
import com.fineos.notes.constant.Constant;
import com.j256.ormlite.stmt.QueryBuilder;


public class ColorPopup implements RadioGroup.OnCheckedChangeListener{

	
	 public Activity mActivity;
	 private PopupWindow popupWindow;
	 private Button whitebutton;
	 private RadioButton blackbutton,redbutton,orangebutton,yellowbutton,greenbutton,bluebutton,purplebutton;
	 private Button light_greenbutton;
	 private ColorPopupChooseListener listener;
 	//dpc at 1224 
	private int popuColor ;
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

        switch (checkedId){

            case R.id.color_black:
                popuColor = mActivity.getResources().getColor(R.color.black_ColorPopup);
                listener.setColor(popuColor);
                dismiss();
                break;
            case R.id.color_red:
                popuColor = mActivity.getResources().getColor(R.color.red_ColorPopup);
                listener.setColor(popuColor);
                dismiss();
                break;
            case R.id.color_orange:
                popuColor = mActivity.getResources().getColor(R.color.orange_ColorPopup);
                listener.setColor(popuColor);
                dismiss();
                break;
            case R.id.color_yellow:
                popuColor = mActivity.getResources().getColor(R.color.yellow_ColorPopup);
                listener.setColor(popuColor);
                dismiss();
                break;
            case R.id.color_green:
                popuColor = mActivity.getResources().getColor(R.color.green_ColorPopup);
                listener.setColor(popuColor);
                dismiss();
                break;
            case  R.id.color_blue:
                popuColor = mActivity.getResources().getColor(R.color.blue_ColorPopup);
                listener.setColor(popuColor);
                dismiss();
                break;
            case R.id.color_purple:
                popuColor = mActivity.getResources().getColor(R.color.purple_ColorPopup);
                listener.setColor(popuColor);
                dismiss();
                break;
        }
    }

    public interface ColorPopupChooseListener{
		 
		 public void setColor(int color);
	 }
	 
//dpc 
	 public int getColorPopupChoose(){
         	return  popuColor;
     	} 
	 public ColorPopup(Activity mActivity,int winWidth){
		 this.mActivity = mActivity;

		 initColorLayout(winWidth);
//dpc
		popuColor = mActivity.getResources().getColor(R.color.black_ColorPopup);
	 }

	 
	 
	 public void initColorLayout(int winWidth){
         Log.w(Constant.TAG,"ColorPopup--");
		 
	       //得到屏幕的宽度和高度 
//	       int screenWidth = mActivity.getWindowManager().getDefaultDisplay().getWidth();
//	      int screenHeight = mActivity.getWindowManager().getDefaultDisplay().getHeight();
	       //获取自定义布局文件popupwindow.xml 
	       View popupWindow_view = mActivity.getLayoutInflater() 
	                .inflate(R.layout.color_layout, null, false);
		 
	       //创建popupWindow实例 
	        popupWindow = new PopupWindow(popupWindow_view, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
		    popupWindow.setBackgroundDrawable(new BitmapDrawable());
	        popupWindow.setFocusable(true);
			// 设置允许在外点击消失
	        popupWindow.setOutsideTouchable(true);
            if (winWidth == 720){
                popupWindow.showAsDropDown(mActivity.getWindow().getDecorView().findViewById(R.id.control_view), 60, 0);
            }else {
                popupWindow.showAsDropDown(mActivity.getWindow().getDecorView().findViewById(R.id.control_view), 95, 0);
            }
//            popupWindow.showAsDropDown(mActivity.getWindow().getDecorView().findViewById(R.id.control_view),60,0);
//            popupWindow.showAtLocation(mActivity.getWindow().getDecorView().findViewById(R.id.control_view), Gravity.CENTER_HORIZONTAL,0,50);


         RadioGroup radioGroup = (RadioGroup) popupWindow_view.findViewById(R.id.coloy_layout);
         radioGroup.setOnCheckedChangeListener(this);


//			blackbutton = (RadioButton) popupWindow_view.findViewById(R.id.color_black);
////			whitebutton =(Button)popupWindow_view.findViewById(R.id.color_white);
//			redbutton = (RadioButton) popupWindow_view.findViewById(R.id.color_red);
//			orangebutton = (RadioButton) popupWindow_view.findViewById(R.id.color_orange);
//			yellowbutton = (RadioButton) popupWindow_view.findViewById(R.id.color_yellow);
//			greenbutton = (RadioButton) popupWindow_view.findViewById(R.id.color_green);
////			light_greenbutton =(Button)popupWindow_view.findViewById(R.id.color_light_green);
//			bluebutton = (RadioButton) popupWindow_view.findViewById(R.id.color_blue);
//			purplebutton = (RadioButton) popupWindow_view.findViewById(R.id.color_purple);



	 }
	 
	 
	 
	 public void show(int winWidth){
		 
		 if(popupWindow.isShowing()){
			 dismiss();
		 }else{
//             popupWindow.showAsDropDown(mActivity.getWindow().getDecorView().findViewById(R.id.control_view),60,0);
             if (winWidth == 720){
                 popupWindow.showAsDropDown(mActivity.getWindow().getDecorView().findViewById(R.id.control_view), 60, 0);
             }else {
                 popupWindow.showAsDropDown(mActivity.getWindow().getDecorView().findViewById(R.id.control_view), 95, 0);
             }
//		        popupWindow.showAsDropDown(v);
		 }
	 }


	public void dismiss() {
			popupWindow.dismiss();
		}
	
	
	public void setListener(ColorPopupChooseListener listener){
		this.listener = listener;
	}
	
}
