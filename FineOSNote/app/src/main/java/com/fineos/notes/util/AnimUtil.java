package com.fineos.notes.util;

import android.app.Activity;
import android.content.Context;

import com.fineos.notes.R;

/**
 * Created by ubuntu on 15-8-12.
 */
public class AnimUtil {

    public static void onBackPressedAnim(Activity activity){
        activity.overridePendingTransition(com.fineos.internal.R.anim.slide_in_left,
                com.fineos.internal.R.anim.slide_out_right); 
    }
    public static void startNewActivity(Activity activity){
        activity.overridePendingTransition(com.fineos.internal.R.anim.slide_in_right, 
	        		com.fineos.internal.R.anim.slide_out_left);

    }
}
