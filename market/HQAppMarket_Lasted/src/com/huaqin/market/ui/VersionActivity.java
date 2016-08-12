package com.huaqin.market.ui;


import com.huaqin.market.R;
import com.huaqin.market.utils.OptionsMenu;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.TextView;

public class VersionActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.version_info);
		TextView versionName_textView = (TextView)findViewById(R.id.version_textview);
		String appVersionName;
		PackageManager manager = this.getPackageManager();
		try
		{
			PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
			appVersionName = info.versionName;
			String versionName = getString(R.string.version_name);
			versionName_textView.setText(versionName + appVersionName);
			
		}catch(NameNotFoundException e)
		{
			e.printStackTrace();
			
		}
		
		
//		Button ok_button = (Button)findViewById(R.id.confirm_buttonView);		
//		ok_button.setOnClickListener(new View.OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				finish();
//			}
//		});
	}
	
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_MENU) {
    		startActivity(new Intent(this, OptionsMenu.class));
    		overridePendingTransition(R.anim.fade, R.anim.hold);
    	}
        return super.onKeyUp(keyCode, event);
    } 
     
}
