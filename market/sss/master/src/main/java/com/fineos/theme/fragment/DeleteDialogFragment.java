package com.fineos.theme.fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.fineos.theme.R;

import fineos.app.AlertDialog;

public class DeleteDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

	private DialogInterface.OnClickListener mClickListener = null;

	/**
	 * M: create a instance of DeleteDialogFragment
	 * 
	 * @param single
	 *            if the number of files to be deleted is only one ?
	 * @return the instance of DeleteDialogFragment
	 */
	public static DeleteDialogFragment newInstance() {
		return new DeleteDialogFragment();
	}

	@Override
	/**
	 * M: create a dialog
	 */
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		String alertMsg = getString(R.string.alert_delete);
		builder.setTitle(R.string.delete).setMessage(alertMsg).setPositiveButton(getString(R.string.dialog_ok), this).setNegativeButton(getString(R.string.dialog_cancel), null);
		Dialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(false);
		return dialog;
	}

	/**
	 * M: the process of click OK button on dialog
	 */
	@Override
	public void onClick(DialogInterface arg0, int arg1) {
		if (null != mClickListener) {
			mClickListener.onClick(arg0, arg1);
		}
	}

	/**
	 * M: set listener of OK button
	 * 
	 * @param listener
	 *            the listener to be set
	 */
	public void setOnClickListener(DialogInterface.OnClickListener listener) {
		mClickListener = listener;
	}

}