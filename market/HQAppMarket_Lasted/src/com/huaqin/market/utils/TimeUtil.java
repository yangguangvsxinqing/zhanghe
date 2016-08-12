package com.huaqin.market.utils;

import android.text.format.Time;

public class TimeUtil {
	
	public static long getRemainTime(long setTime){
		Time t=new Time(); 
        t.setToNow();
        int day = t.yearDay;
        int hour = t.hour; // 0-23
        int minute = t.minute;
        int second = t.second;
        return (setTime - ((hour * 60 + minute) * 60 + second) + 1) * 1000; 
	}
}
