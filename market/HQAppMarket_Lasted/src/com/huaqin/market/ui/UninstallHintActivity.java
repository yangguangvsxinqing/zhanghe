package com.huaqin.market.ui;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.huaqin.market.R;
import com.huaqin.market.download.Constants;
import com.huaqin.market.utils.PackageUtil;

public class UninstallHintActivity extends Activity{
	Button btn_ok = null;
    Button btn_cancel = null;
    TextView APK_title_text = null;
    TextView activity_hint_text = null;
    ImageView imageView = null;
    private String pkgName = null;
    private String filepath = null;
    private String fileTitle = null;
    private Context mContext;
    private Handler mHandler;
    
    private int BTN_OK_FLAG = 1;
    private static final int UNINSTALL_FLAG = 1;
    private static final int INSTALL_FLAG = 2;
    private static final int INSTALL_COMPLETE_FLAG = 3;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.uninstall_hint_activity);
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		pkgName = bundle.getString("package");
		fileTitle = bundle.getString("filetitle");
		filepath = bundle.getString("filepath");
		viewInit();
		mContext = this;
		initHandler();
		
		Drawable icon = PackageUtil.getApplicationIcon(mContext.getPackageManager(), filepath);
		imageView.setImageDrawable(icon);
		APK_title_text.setText(fileTitle);
		
		btn_ok.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(BTN_OK_FLAG == UNINSTALL_FLAG){
					Uri uri = Uri.parse("package:" + pkgName);
					Intent intent = new Intent(Intent.ACTION_DELETE, uri);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					mContext.startActivity(intent);
				}else if(BTN_OK_FLAG == INSTALL_FLAG){
        			File file = new File(filepath);
        			Intent install = new Intent(Intent.ACTION_VIEW);
        			install.setDataAndType(Uri.fromFile(file), Constants.MIMETYPE_APK);
        			install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        			mContext.startActivity(install);
        			finish();
				}
			}
			
		});
		
		btn_cancel.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
			
		});
	}
    
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		int appstatus = PackageUtil.getApplicationStatus(mContext.getPackageManager(), pkgName);
		if(appstatus == PackageUtil.PACKAGE_UPDATE_AVAILABLE){
			Message msg = Message.obtain(mHandler, UNINSTALL_FLAG);
			mHandler.sendMessage(msg);
		}else if(appstatus == PackageUtil.PACKAGE_NOT_INSTALLED){
			Message msg = Message.obtain(mHandler, INSTALL_FLAG);
			mHandler.sendMessage(msg);
		}else{
			Message msg = Message.obtain(mHandler, INSTALL_COMPLETE_FLAG);
			mHandler.sendMessage(msg);
		}
	}


	private void initHandler(){
		mHandler = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				switch (msg.what){
				case INSTALL_FLAG:
					activity_hint_text.setText(R.string.dlg_uninstallactivity_install);
					btn_ok.setText(R.string.app_install);
					BTN_OK_FLAG = INSTALL_FLAG;
					break;
				case UNINSTALL_FLAG:
					activity_hint_text.setText(R.string.dlg_apk_signature_hint);
					btn_ok.setText(R.string.uninstallactivity_uninstall);
					BTN_OK_FLAG = UNINSTALL_FLAG;
					break;
				case INSTALL_COMPLETE_FLAG:
					activity_hint_text.setText(R.string.dlg_uninstallactivity_install_complete);
					btn_ok.setText(R.string.dlg_uninstallactivity_back);
					BTN_OK_FLAG = INSTALL_COMPLETE_FLAG;
					break;
				}
			}
		};
		
	}
	private void viewInit(){
    	btn_ok = (Button) findViewById(R.id.btn_ok_uninstall_hint);
    	btn_cancel = (Button) findViewById(R.id.btn_cancel_uninstall_hint);
    	APK_title_text = (TextView)findViewById(R.id.app_title);
    	activity_hint_text = (TextView)findViewById(R.id.signature_hint);
    	imageView = (ImageView) findViewById(R.id.uninstall_app_icon);
    }
}
