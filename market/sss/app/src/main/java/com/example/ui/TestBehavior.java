package com.example.ui;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * Created by ubuntu on 16-7-27.
 */
public class TestBehavior extends CoordinatorLayout.Behavior<View>{
    private String TAG = "TestBehavior";
    public TestBehavior(Context context, AttributeSet attrs){
        super(context,attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        boolean flag = dependency instanceof TextView;
        Log.v("TAG","sss layoutDependsOn = "+flag);
        return flag;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        int offset = dependency.getTop() - child.getTop();

        Log.v("TAG","sss onDependentViewChanged offset="+offset);
        ViewCompat.offsetTopAndBottom(child, offset);
        return true;
    }

//    @Override
//    public void onDependentViewRemoved(CoordinatorLayout parent, View child, View dependency) {
//        super.onDependentViewRemoved(parent, child, dependency);
//    }
}
