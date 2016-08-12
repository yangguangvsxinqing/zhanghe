package com.fineos.theme.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.fineos.theme.R;
import com.fineos.theme.utils.ThemeLog;
import com.fineos.theme.utils.Util;
import com.fineos.theme.webservice.ThemeService;

import fineos.app.AlertDialog;

public class ThirdFontsFragment extends Fragment {

	public static final String TAG = "ThirdFontsFragment";
	private static ThirdFontsFragment mThirdFontsFragment;
	public static ThirdFontsFragment newInstance(int themeData, int onlineFlag) {
		if (mThirdFontsFragment == null) {
			mThirdFontsFragment = new ThirdFontsFragment();
			mElementType = themeData;

			Bundle args = new Bundle();
			args.putInt("isOnline", onlineFlag);
			mThirdFontsFragment.setArguments(args);
		}
		

		return mThirdFontsFragment;
	}


	private Context mContext;
	private int mOnlineType;
	private ThemeService mThemeService;
	/***************************************************************************************/
	protected static int mElementType;

	/***************************************************************************************/

	public ThirdFontsFragment() {
	}

	public void setContext(Context context) {
		ThemeLog.i(TAG, "setContext,context=" + context);
		mContext = context;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mThemeService = ThemeService.getServiceInstance(getActivity());
		mContext = getActivity();
		mOnlineType = getArguments().getInt("isOnline");
		this.getActivity().getWindow().getDecorView().setBackgroundColor(Color.WHITE);
		ThemeLog.i(TAG, "inflateLocalIconList onlineFlag=" + mOnlineType);
		ThemeLog.i(TAG, "LocalThemeMixerBaseFragment,onCreate...");
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		ThemeLog.i("ThemeMixerFragment", "ThemeMixerFragment onSaveInstanceState mElementType = "+mElementType);
		
		outState.putInt("mElementType", mElementType);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ThemeLog.i(TAG, "LocalThemeMixerBaseFragment,onCreateView...");
		View view = getThridFontsView();
		if (savedInstanceState != null) {
			mElementType = savedInstanceState.getInt("mElementType");
			
			ThemeLog.i("ThemeMixerFragment", "ThemeMixerFragment onCreate mElementType = "+mElementType);
		}
		return view;

	}
	
	public View getThridFontsView(){
//    	if (piflow != null) {
//    		return piflow;
//    	}
    	View fontsview = null;
    	try {
			Context c = mContext.createPackageContext("com.ekesoo.font.huaqin", Context.CONTEXT_INCLUDE_CODE|Context.CONTEXT_IGNORE_SECURITY);
			int id = c.getResources().getIdentifier("fonts_style", "layout", "com.ekesoo.font.huaqin");
			LayoutInflater inflater = LayoutInflater.from(c);
			fontsview = inflater.inflate(id, null);
		} catch (Exception e) {
			// TODO: handle exception
			fontsview = View.inflate(mContext, R.layout.third_font_err, null);
			e.printStackTrace();
		}
    	return fontsview;
    }

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();	

	}
	
	// 显示移动数据流量警告
	private void showNetWarnDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		View view = LayoutInflater.from(mContext).inflate(R.layout.hint_view, null);
		builder.setView(view);
		final CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox);
		builder.setCancelable(false);
		builder.setNegativeButton(getResources().getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();

				getActivity().finish();
			}
		});
		builder.setPositiveButton(getResources().getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				Util.setNetworkHint(mContext, !checkBox.isChecked());
				
			}
		});
		final AlertDialog dialog = builder.create();
		dialog.show();
	}
		
	@Override
	public void onResume() {
		super.onResume();

		
	}

	
	
}




