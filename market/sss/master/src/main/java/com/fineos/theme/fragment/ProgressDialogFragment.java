package com.fineos.theme.fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import com.fineos.theme.R;

import fineos.app.ProgressDialog;

/** M: use DialogFragment to show Dialog */
public class ProgressDialogFragment extends DialogFragment {
	/**
	 * M: create a instance of ProgressDialogFragment
	 * 
	 * @return the instance of ProgressDialogFragment
	 */
	public static ProgressDialogFragment newInstance() {
		return new ProgressDialogFragment();
	}

	@Override
	/**
	 * M: create a progress dialog
	 */
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		ProgressDialog dialog = new ProgressDialog(getActivity());
		dialog.setTitle(getString(R.string.delete));
		dialog.setMessage(getString(R.string.deleting));
		dialog.setCancelable(false);
		return dialog;
	}
}