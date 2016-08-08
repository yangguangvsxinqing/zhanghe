/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2010. All rights reserved.
 * 
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

package com.fineos.themecoloreditor;

import android.R.integer;
import android.app.Activity;
import fineos.app.AlertDialog;
import fineos.content.res.IThemeManagerService;
import fineos.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.AvoidXfermode;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.media.AudioManager;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ServiceManager;
//import android.os.ServiceManager;
import android.os.storage.StorageManager;
//import android.os.storage.StorageVolume;
import android.provider.BaseColumns;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Video.Media;
import android.provider.MediaStore.Video.VideoColumns;
//import android.support.annotation.StringRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.app.ActionBar;
import android.view.LayoutInflater;
import android.widget.LinearLayout.LayoutParams;
import android.content.pm.PackageManager;
import android.content.pm.ApplicationInfo;
import android.content.ComponentName;
import java.io.IOException;
import android.widget.EditText;
import android.text.TextWatcher;
import android.text.Editable;
import android.graphics.Color;
import android.widget.Button;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import android.content.res.AssetManager;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;

import com.fineos.themecoloreditor.ColorItemAdapter;
import com.fineos.themecoloreditor.R;
import com.fineos.themecoloreditor.RecycleViewAdapter;

import android.widget.BaseAdapter;

import org.json.JSONArray;
import org.json.JSONException;

public class ThemeColorEditorActivity extends Activity {
	private ExecutorService service;
	private static   ProgressDialog progressDialog;
	private ColorStausChangedReceiver mColorReceiver=new ColorStausChangedReceiver();
	private RecyclerView appRecyclerView;
	private RecyclerView colorRecycleView;
	private RecycleViewAdapter mAdapter;
	private ColorItemAdapter coloradapter;
	private List<int[]> mDatas = new ArrayList<int[]>();
	private List<Integer>mdetails;
	private TextView finish;
	private TextView check;
	private JSONArray array=new JSONArray();
	private int temposition;
	private String ColorChangeApp[]={"dialer","calendar","deskclock","notes","calculator","system"};
	private ArrayList<ImageView>imageViews;
	private static final String TAG = "ThemeColorEditorActivity";
	Button mApplyButton;
	private  GradientDrawable myGrad ;
	List<ThemeColor> mThemeColors;
	ThemeColorApply mThemeColorApply;
	private SharedPreferences sharedPreferences;
	private int oriimage[]={R.drawable.call,
			R.drawable.calendar, R.drawable.clock,R.drawable.note, R.drawable.calculator,
			R.drawable.notify};
	private int dialDrawble[] = {R.drawable.dial1,R.drawable.dial2};
	private int calendarDrawable[] = {R.drawable.calendar1, R.drawable.calendar2};
	private int mmsDrawable[] = {R.drawable.mms1, R.drawable.mms2, R.drawable.mms3};
	private int settingsDrawable[] = {R.drawable.setting};
	private int SystemUiDrawable[] = {R.drawable.systemui};
	ArrayList<ThemeColors> mColorsList = new ArrayList<ThemeColors>();
	
	public List<String> mColorsListName = new ArrayList<String>();
	public List<String> mColorsListValue = new ArrayList<String>();
	
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.firstpage);
		service= Executors.newFixedThreadPool(2);
		getApplicationContext().registerReceiver(mColorReceiver,new IntentFilter("com.android.server.ThemeManager.action.THEME_APPLIED"));
		getApplicationContext().registerReceiver(mColorReceiver,new IntentFilter("com.android.server.ThemeManager.action.THEME_NOT_APPLIED"));
		 sharedPreferences = getSharedPreferences("zhj", Context.MODE_PRIVATE);
		try {
			ArrayHelper.getHelper().readPreference(new JSONArray(sharedPreferences.getString("positiongroup","[]")));
		} catch (JSONException e) {
			e.printStackTrace();
		}
//		int color[]={this.getResources().getColor(R.color.first),this.getResources().getColor(R.color.second),this.getResources().getColor(R.color.third),this.getResources().getColor(R.color.fourth),this.getResources().getColor(R.color.fifth),this.getResources().getColor(R.color.sixth),this.getResources().getColor(R.color.seventh),this.getResources().getColor(R.color.eigth),this.getResources().getColor(R.color.ningth),this.getResources().getColor(R.color.tenth),this.getResources().getColor(R.color.eleventh),this.getResources().getColor(R.color.twelveth),this.getResources().getColor(R.color.thirteenth)};
		mThemeColorApply = new ThemeColorApply(this);
		initDatas(getThemeColors());
		
		try {
			init();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		String[] arrayColorsName = getResources().getStringArray(R.array.ColorsListName);
		String[] arrayColorsValue = getResources().getStringArray(R.array.ColorsListValue);
		
		for (int i = 0; i < arrayColorsValue.length; i++) {
			mColorsListName.add(arrayColorsName[i]);
			mColorsListValue.add(arrayColorsValue[i]);
		}
	 
	}
	
	private ArrayList<ThemeColors> getThemeColors() {
		addColorList("fine1", R.array.fine1, mColorsList);
		addColorList("fine2", R.array.fine2, mColorsList);
		addColorList("fine3", R.array.fine3, mColorsList);
		addColorList("fine4", R.array.fine4, mColorsList);
		addColorList("fine5", R.array.fine5, mColorsList);
		addColorList("fine6", R.array.fine6, mColorsList);
		addColorList("fine7", R.array.fine7, mColorsList);
		addColorList("fineos_softly", R.array.fineos_softly, mColorsList);
		addColorList("fineos_beautiful_day", R.array.fineos_beautiful_day, mColorsList);
		addColorList("fineos_elegant_pink", R.array.fineos_elegant_pink, mColorsList);
		addColorList("fineos_girl", R.array.fineos_girl, mColorsList);
		addColorList("fineos_night", R.array.fineos_night, mColorsList);
		addColorList("fineos_glory_gold", R.array.fineos_glory_gold, mColorsList);
		addColorList("default_fineos", R.array.default_fineos, mColorsList);
		return mColorsList;
	}
	
	private void addColorList(String name, int arrayId, ArrayList<ThemeColors> colorsList) {
		int[] themedetailcolor = getResources().getIntArray(arrayId);
		if (themedetailcolor.length < 3) {
			return;
		}		
		ThemeColors colors2 = new ThemeColors();
		colors2.setColorName(name);
		colors2.setTitleColor(themedetailcolor[0]);
		colors2.setContext2Color(themedetailcolor[1]);
		colors2.setContext1Color(themedetailcolor[2]);
		colors2.setBottomButtonColor(themedetailcolor[3]);
		colorsList.add(colors2);
	} 
	
	public interface sharePreferencesHelper
	{
		void readPreference(JSONArray array) throws JSONException;
		void writePreference(JSONArray array);
	}
	private void initDatas(ArrayList<ThemeColors> colorsList)
	{
		mDatas.add(dialDrawble);
		mDatas.add(calendarDrawable);
		mDatas.add(mmsDrawable);
		mDatas.add(settingsDrawable);
		mDatas.add(SystemUiDrawable);
//		mDatas.add(dialDrawble);
		mdetails=new ArrayList<Integer>();
		for (ThemeColors aColor : colorsList) {
			mdetails.add(aColor.getTitleColor());
		}

		imageViews=new ArrayList<ImageView>();
	}
	private void init() throws JSONException {
		finish=(TextView)findViewById(R.id.goback);
//		finish.setBackgroundResource(com.fineos.internal.R.drawable.ic_ab_back_holo_light);
		finish.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(com.fineos.internal.R.drawable.ic_ab_back_holo_light), null, null, null);
		finish.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//finish.setBackground(getResources().getDrawable(R.drawable.ic_ab_back_holo_dark_am_pressed));
				finishAffinity();
			}
		});
		mApplyButton = (Button) findViewById(R.id.button_apply);
		mApplyButton.setOnClickListener(mButtonHandler);
		mApplyButton.setTextColor(Color.WHITE);
		mApplyButton.setAllCaps(false);
        check=(TextView)findViewById(R.id.selectall);
		mAdapter=new RecycleViewAdapter(getApplicationContext(),mDatas);
		coloradapter=new ColorItemAdapter(this,mColorsList);
		if(ArrayHelper.ifnotchecked())
		{
			mAdapter.selectall();
			clickagain();
		}
		else
		{
			check.setText(R.string.checkall);
			firstcheck();
		}


        coloradapter.setbeginposition(sharedPreferences.getInt("position", 0));
		coloradapter.setOnItemClickLitener(new ColorItemAdapter.OnItemClickLitener() {
			@Override
			public void onItemselected(ThemeColors colors, int position)  {
				temposition = position;
				Log.i(TAG, " getTitleColor="+Integer.toHexString(colors.getTitleColor()));
				Log.i(TAG, " getContext1Color="+Integer.toHexString(colors.getContext1Color()));
				Log.i(TAG, " getContext2Color="+Integer.toHexString(colors.getContext2Color()));
				Log.i(TAG, " getBottomButtonColor="+Integer.toHexString(colors.getBottomButtonColor()));
				String[] arrayColors = {Integer.toHexString(colors.getTitleColor()),Integer.toHexString(colors.getContext1Color())
						,Integer.toHexString(colors.getContext2Color()),Integer.toHexString(colors.getBottomButtonColor())};
				for (int j = 0; j < arrayColors.length; j++) {
					mColorsListValue.remove(j);
					mColorsListValue.add(j,arrayColors[j]);
				}
				Log.i(TAG, "zhanghe22221  mColorsListValue="+ mColorsListValue);
				
				ArrayHelper.setColor(colors);
				mAdapter.notifyDataSetChanged();

			}
		});

		appRecyclerView=(RecyclerView)findViewById(R.id.recyclerview_applicimage);
		colorRecycleView=(RecyclerView)findViewById(R.id.recyclerview_color);
		//设置布局管理器
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
		linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
		LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(this);
		linearLayoutManager1.setOrientation(LinearLayoutManager.HORIZONTAL);
		appRecyclerView.setLayoutManager(linearLayoutManager);
		colorRecycleView.setLayoutManager(linearLayoutManager1);
		//int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.space);
		//int spacingInPixels1 = getResources().getDimensionPixelSize(R.dimen.imagespace);
		//appRecyclerView.addItemDecoration(new SpaceItemDecoration(spacingInPixels1));
		//colorRecycleView.addItemDecoration(new SpaceItemDecoration(spacingInPixels));
		//设置适配器
		appRecyclerView.setAdapter(mAdapter);
		colorRecycleView.setAdapter(coloradapter);
		int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.color_item_space);
		colorRecycleView.addItemDecoration(new SpaceItemDecoration(spacingInPixels));
		
		int recyclerview_applicimage_space = getResources().getDimensionPixelSize(R.dimen.recyclerview_applicimage_space);
		appRecyclerView.addItemDecoration(new SpaceItemDecoration(recyclerview_applicimage_space));
	}

	//button 2次点击
	private void firstcheck() {

		check.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mAdapter.selectall();
				check.setText(R.string.checknone);
				mAdapter.notifyDataSetChanged();
				clickagain();
			}
		});
	}
	private void clickagain()
	{
		check.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mAdapter.selectnone();
				check.setText(R.string.checkall);
				mAdapter.notifyDataSetChanged();
				firstcheck();
			}
		});
	}


	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		getApplication().unregisterReceiver(mColorReceiver);
		SharedPreferences sharedPreferences = getSharedPreferences("zhj", Context.MODE_PRIVATE);

		SharedPreferences.Editor editor = sharedPreferences.edit();//获取编辑器
		ArrayHelper.getHelper().writePreference(array);
		editor.putString("positiongroup",array.toString());
        editor.putInt("colorvalue", ArrayHelper.getColor().getTitleColor());
		editor.putInt("position", temposition);
		editor.apply();
	}

	public class ColorStausChangedReceiver extends BroadcastReceiver
	{

		@Override
		public void onReceive(Context context, Intent intent) {
			//progressDialog.dismiss();
		
			
			if(intent.getAction().equals("com.android.server.ThemeManager.action.THEME_APPLIED"))
			{
				if(progressDialog.isShowing())
				{
					progressDialog.dismiss();
				}
				Toast.makeText(ThemeColorEditorActivity.this,R.string.finished,Toast.LENGTH_SHORT).show();
			}
			else if (intent.getAction().equals("com.android.server.ThemeManager.action.THEME_NOT_APPLIED"))
			{
				Toast.makeText(ThemeColorEditorActivity.this,R.string.failed,Toast.LENGTH_SHORT).show();
			}

		}
	}
	private final View.OnClickListener mButtonHandler = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			final Message m;
			if (v == mApplyButton) {
				progressDialog=ProgressDialog.show(ThemeColorEditorActivity.this,"",getString(R.string.applying));
				//mApplyButton.setBackground(getResources().getDrawable(R.drawable.ic_btn_apply_pressed));
				service= Executors.newFixedThreadPool(1);
				service.submit(new Mythread());
				
			
			} 
		}
	};
	public class Mythread extends Thread
{
	@Override
	public void run() {
		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		onApplyButtonClicked();
	}
}
	private synchronized void onApplyButtonClicked(){
//		getColorNameList();
		Log.i(TAG, "zhanghe onApplyButtonClicked()");

//		mThemeColorApply.apply();
		mThemeColorApply.writeXml();
		mThemeColorApply.zipXml();
	
		IThemeManagerService ts = IThemeManagerService.Stub.asInterface(ServiceManager.getService("ThemeService"));
		try {

			Log.i(TAG, "zhanghe mColorsListName"+mColorsListName);
			Log.i(TAG, "zhanghe mColorsListValue"+mColorsListValue);

			
			Log.e(TAG, " zhanghe applyTheme start for theme2");
			Log.i(TAG, "zhanghe1111 mColorsListName"+getColorsListName());
			Log.i(TAG, "zhanghe3333 mColorsListValue"+getColorsListValue());
			ts.applyThemeColorsList(getColorsListName(),getColorsListValue());
//			void applyThemeColorsList(in List<String> name, in List<String> colors);
									
			
		} catch (Exception e) {
			Log.e(TAG, "applyTheme exception happened !");
			e.printStackTrace();
		}
		
	}

	private int getColorInt(int color){
		int colorInt ;

		int alpha=(color >> 24) &0xFF;
		int red = (color >> 16) & 0xFF;
		int green=(color >> 8) & 0XFF;
		int blue=color & 0XFF;
		colorInt =  Color.argb(alpha, red, green, blue);
		return colorInt;
	}
	
	public void getColorNameList(){
		mThemeColors = mThemeColorApply.getColorNameList();
	}
	public List<String> getColorsListValue(){
		return mColorsListValue;
	}
	
	public List<String> getColorsListName(){
		return mColorsListName;
	}
	public void setColorNameList(){
		mThemeColorApply.setColorNameList(mThemeColors);
	}

	public class SpaceItemDecoration extends RecyclerView.ItemDecoration {

		private int space;

		public SpaceItemDecoration(int space) {
			this.space = space;
		}

		@Override
		public void getItemOffsets(Rect outRect, View view,
				RecyclerView parent, RecyclerView.State state) {
			if (parent.getChildPosition(view) != 0) {
				outRect.left = space;
			}	
		}
	}
	
}
