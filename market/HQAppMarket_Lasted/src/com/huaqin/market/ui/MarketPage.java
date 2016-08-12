package com.huaqin.market.ui;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;



public class MarketPage extends ViewPager {

        private static boolean willIntercept = true;
        public MarketPage(Context context) {
                super(context);
        }
        
        public MarketPage(Context context, AttributeSet attrs) {
                super(context, attrs);
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent arg0) {
                if(willIntercept){
                        //这个地方直接返回true会很卡
                       return super.onInterceptTouchEvent(arg0);
                }else{
                        return false;
                }
        }

        public static void setTouchIntercept(boolean value){
        	Log.v("asd", "setTouchIntercept="+value);
                willIntercept = value;
        }
//        @Override
//        public boolean dispatchTouchEvent(MotionEvent ev) {
//            int action = ev.getAction();
//
//            switch (action) {
//
//            case MotionEvent.ACTION_DOWN:
//
//                Log.d("asd", "dispatchTouchEvent action:ACTION_DOWN");
//
//                break;
//
//            case MotionEvent.ACTION_MOVE:
//
//                Log.d("asd", "dispatchTouchEvent action:ACTION_MOVE");
//
//                break;
//
//            case MotionEvent.ACTION_UP:
//
//                Log.d("asd", "dispatchTouchEvent action:ACTION_UP");
//
//                break;
//
//            case MotionEvent.ACTION_CANCEL:
//
//                Log.d("asd", "dispatchTouchEvent action:ACTION_CANCEL");
//
//                break;
//
//            }
//            return super.dispatchTouchEvent(ev);   
//        }

}
