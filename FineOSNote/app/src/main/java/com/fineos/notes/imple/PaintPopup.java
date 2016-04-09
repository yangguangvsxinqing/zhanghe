package com.fineos.notes.imple;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioGroup;

import com.fineos.notes.R;


public class PaintPopup implements RadioGroup.OnCheckedChangeListener{

	
	 public Activity mActivity;
	 private PopupWindow popupWindow;
	 private ImageView size_one;
	 private ImageView size_tow;
	 private ImageView size_three;
	 private ImageView size_four;
	 private ImageView size_five;
	 private ImageView size_six;
	 private ImageView size_seven;

	 private PaintPopupChooseListener listener;

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId)
        {
            case R.id.paint_choose_one:
                listener.setPaintSizeListener(mActivity.getResources().getDimensionPixelSize(R.dimen.size1_paintPopup));
                dismiss();

                break;
            case R.id.paint_choose_tow:
                listener.setPaintSizeListener(mActivity.getResources().getDimensionPixelSize(R.dimen.size2_paintPopup));
                dismiss();
                break;
            case R.id.paint_choose_three:
                listener.setPaintSizeListener(mActivity.getResources().getDimensionPixelSize(R.dimen.size3_paintPopup));
                dismiss();
                break;
            case R.id.paint_choose_four:
                listener.setPaintSizeListener(mActivity.getResources().getDimensionPixelSize(R.dimen.size4_paintPopup));
                dismiss();
                break;
            case R.id.paint_choose_five:
                listener.setPaintSizeListener(mActivity.getResources().getDimensionPixelSize(R.dimen.size5_paintPopup));
                dismiss();
                break;
            case R.id.paint_choose_six:
                listener.setPaintSizeListener(mActivity.getResources().getDimensionPixelSize(R.dimen.size6_paintPopup));
                dismiss();
                break;
            case R.id.paint_choose_seven:
                listener.setPaintSizeListener(mActivity.getResources().getDimensionPixelSize(R.dimen.size7_paintPopup));
                dismiss();
                break;
        }
    }

    public interface PaintPopupChooseListener{
		 
		 public void setPaintSizeListener(int size);
	 }
	 
	 
	 public PaintPopup(Activity mActivity,int winWidth){
		 this.mActivity = mActivity;

		 initPaintLayout(winWidth);
	 }
	 
	 
	 public void initPaintLayout(int winWidth){
		 
	       //获取自定义布局文件popupwindow.xml 
	       View popupWindow_view = mActivity.getLayoutInflater() 
	                .inflate(R.layout.paint_layout, null, false);
		 
	       //创建popupWindow实例 
	        popupWindow = new PopupWindow(popupWindow_view, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true); 
		    popupWindow.setBackgroundDrawable(new BitmapDrawable());
	        popupWindow.setFocusable(true);
			// 设置允许在外点击消失
	        popupWindow.setOutsideTouchable(true);
//            popupWindow.showAsDropDown(mActivity.getWindow().getDecorView().findViewById(R.id.control_view),60,0);
         if (winWidth == 720){
             popupWindow.showAsDropDown(mActivity.getWindow().getDecorView().findViewById(R.id.control_view),60,0);
         }else {
             popupWindow.showAsDropDown(mActivity.getWindow().getDecorView().findViewById(R.id.control_view),95,0);
         }

            RadioGroup radioGroup = (RadioGroup) popupWindow_view.findViewById(R.id.rg_paintlayout);
            radioGroup.setOnCheckedChangeListener(this);

//			size_one =(ImageView)popupWindow_view.findViewById(R.id.paint_choose_one);
//			size_tow =(ImageView)popupWindow_view.findViewById(R.id.paint_choose_tow);
//			size_three =(ImageView)popupWindow_view.findViewById(R.id.paint_choose_three);
//			size_four =(ImageView)popupWindow_view.findViewById(R.id.paint_choose_four);
//			size_five =(ImageView)popupWindow_view.findViewById(R.id.paint_choose_five);
//			size_six =(ImageView)popupWindow_view.findViewById(R.id.paint_choose_six);
//			size_seven =(ImageView)popupWindow_view.findViewById(R.id.paint_choose_seven);
//
//			size_one.setOnClickListener(this);
//			size_one.setTag(2);
//			size_tow.setOnClickListener(this);
//			size_tow.setTag(3);
//			size_three.setOnClickListener(this);
//			size_three.setTag(4);
//			size_four.setOnClickListener(this);
//			size_four.setTag(11);
//			size_five.setOnClickListener(this);
//			size_five.setTag(20);
//            size_six.setOnClickListener(this);
//			size_six.setTag(26);
//            size_seven.setOnClickListener(this);
//			size_seven.setTag(30);
	 }
	 
	 
	 
	 public void show(int winWidth){
		 
		 if(popupWindow.isShowing()){
			 dismiss();
		 }else{
//		        popupWindow.showAsDropDown(v);
//             popupWindow.showAsDropDown(mActivity.getWindow().getDecorView().findViewById(R.id.control_view),60,0);
             if (winWidth == 720){
                 popupWindow.showAsDropDown(mActivity.getWindow().getDecorView().findViewById(R.id.control_view),60,0);
             }else {
                 popupWindow.showAsDropDown(mActivity.getWindow().getDecorView().findViewById(R.id.control_view),95,0);
             }

         }
	 }


	public void dismiss() {
			popupWindow.dismiss();
		}
		
	
	public void setListener(PaintPopupChooseListener listener){
		this.listener = listener;
	}
	
	
//	@Override
//	public void onClick(View v) {
//		// TODO Auto-generated method stub
//		Integer size = (Integer) v.getTag();
//		if(listener !=null){
//		  listener.onClickPaintListener(size);
//		}
//		dismiss();
//	}
}
