package com.example.myprojectdemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class SwitchButtonActivity extends Activity {
	
    ShimmerTextView tv;
    Shimmer shimmer;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.switchbutton_main);
		 tv = (ShimmerTextView) findViewById(R.id.shimmer_tv);
	}

	public void toggleAnimation(View target) {
        if (shimmer != null && shimmer.isAnimating()) {
            shimmer.cancel();
        } else {
            shimmer = new Shimmer();
            shimmer.start(tv);
        }
    }
}
