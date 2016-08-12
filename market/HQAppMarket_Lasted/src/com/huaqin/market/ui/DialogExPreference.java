package com.huaqin.market.ui;

import com.huaqin.market.R;
import com.huaqin.market.utils.FileManager;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.widget.Toast;

public class DialogExPreference extends DialogPreference {

	private Context mContext;

	public DialogExPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mContext = context;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		super.onClick(dialog, which);
		
		if (which == DialogInterface.BUTTON_POSITIVE) {
			new CleanTask().execute("");
		}
	}

	class CleanTask extends AsyncTask<String, Integer, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {
			// TODO Auto-generated method stub
			FileManager.deleteCacheFiles(mContext);
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			if (result.booleanValue()) {
				Toast.makeText(mContext,
						R.string.settings_clean_data_finish,
						Toast.LENGTH_SHORT)
					.show();
			}
		}
	}
}