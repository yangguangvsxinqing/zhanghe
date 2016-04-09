/*
 * Copyright (c) 2010-2011, The MiCode Open Source Community (www.micode.net)
 *
 * This file is part of FileExplorer.
 *
 * FileExplorer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FileExplorer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SwiFTP.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.fineos.fileexplorer.views;

import fineos.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.fineos.fileexplorer.R;
import com.fineos.fileexplorer.util.StringUtils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Date;

public class TextInputDialog extends AlertDialog {
	private static final int MAX_FILE_CHECK_COUNT = 108;
	public static final int MAX_NAME_LENGTH = 120;
    private String mInputText;
    private String mTitle;
    private String mMsg;
    private OnFinishListener mListener;
    private Context mContext;
    private View mView;
    private EditText mFolderNameEditText;
    private TextView mTextHint;
    private int mLastNameLength;
    private String mCurrentDirectory;
    private String mSelectedFileName;
    private boolean mDialogCreateFinished = false;
    private boolean mHideAnimIsplaying = false;


    public interface OnFinishListener {
        // return true to accept and dismiss, false reject
        boolean onFinish(String text);
    }

    public TextInputDialog(Context context, String title, String msg, String text, OnFinishListener listener, String currentDirectory) {
        super(context);
        //mTitle = title;
        if(!title.equals(context.getString(R.string.create_folder))){
            mSelectedFileName = text;
        }
        this.setCancelable(false);
        mMsg = msg;
        mListener = listener;
        mInputText = text;
        mContext = context;
        mCurrentDirectory = currentDirectory;
    }

    public String getInputText() {
        return mInputText;
    }

    protected void onCreate(Bundle savedInstanceState) {
    	// Inflate textinput dialog and set dialog title and dialog message.
        mView = getLayoutInflater().inflate(R.layout.textinput_dialog, null);
        setView(mView);
        setTitle(mTitle);
        setMessage(mMsg);
        
        // Bind text hint and text input EditTextView to member variable and show text hint an screen.
        mFolderNameEditText = (EditText) mView.findViewById(R.id.edittext_dialog);
        setDefaultNewFileName();
             
        mTextHint = (TextView) mView.findViewById(R.id.text_hint);
        mTextHint.setAlpha(0);
        //showTextHint(mInputText);

        setUpNewFolderNameListener();
        setUpPositiveButtonListener();
        setUpNegativeButtonListener();
        
        super.onCreate(savedInstanceState);
        mDialogCreateFinished = true;
    }

    private void setDefaultNewFileName() {
//		String newFilePath = mCurrentDirectory + File.separator + mInputText;
//		File file = new File(newFilePath);
//		int i = 0;
//		while(file.exists()){
//			if(i > MAX_FILE_CHECK_COUNT){
//                if(!file.isDirectory()){
//                    file = new File(newFilePath.substring(0, newFilePath.lastIndexOf('.')) + new Date().getTime()
//                    + newFilePath.substring(newFilePath.lastIndexOf('.'), newFilePath.length()));
//                }else{
//                    file = new File(newFilePath + new Date().getTime());
//                }
//			}
//            if(!file.isDirectory() && newFilePath.lastIndexOf('.') != -1){
//                file = new File(newFilePath.substring(0, newFilePath.lastIndexOf('.')) + ++i
//                        + newFilePath.substring(newFilePath.lastIndexOf('.'), newFilePath.length()));
//            }else{
//                file = new File(newFilePath + ++i);
//            }
//		}
//		mInputText = file.getName();
		mFolderNameEditText.setText(mInputText);// Default input file name.
	}

	private void setUpNegativeButtonListener() {
		 setButton(BUTTON_NEGATIVE, mContext.getString(android.R.string.cancel),
	                (DialogInterface.OnClickListener) null);
	}

	private void setUpPositiveButtonListener() {
		 setButton(BUTTON_POSITIVE, mContext.getString(android.R.string.ok),
                 new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         if (which == BUTTON_POSITIVE) {
                             mInputText = mFolderNameEditText.getText().toString();
                             if (mListener.onFinish(mInputText)) {
                                 dismiss();
                             }
                         }
                     }
                 });
	}

	private void setUpNewFolderNameListener() {
		setUpTextChangeListener();
		setUpOnFocusChangeListener();	
	}
	
	private void setUpOnFocusChangeListener() {
		mFolderNameEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // Always show keyboard.
                    TextInputDialog.this
                            .getWindow()
                            .setSoftInputMode(
                                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    // Select text with out extension.
                    if (inputHasExtension()) {
                        mFolderNameEditText.setSelection(0, mInputText.indexOf("."));
                    }
                }
            }
        });
	}

    private boolean inputHasExtension() {
        File file = new File(mCurrentDirectory + File.separator + mInputText);
        if (file == null || file.isDirectory()) {
            // We do not care directory's "extension".
            return false;
        } else {
            if (mInputText.lastIndexOf(".") != -1) {
                return true;
            } else {
                return false;
            }
        }
    }

    private void setUpTextChangeListener() {
		mFolderNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                showTextHint(s.toString());
            }
        });
	}

	private void showTextHint(String inputText) {
        boolean hideHint = true;
		int nameLength = inputText.length();
		String hintString = "";
		if(getButton(BUTTON_POSITIVE) != null){
			getButton(BUTTON_POSITIVE).setEnabled(false);
		}
		
		if(mLastNameLength > 100){
			onDisplayLabel(mTextHint);
            hideHint = false;
        }
        if(!StringUtils.isVaildFileName(inputText) && nameLength > 0) {
            mTextHint.setText(R.string.hint_invalid_file_name);
            onDisplayLabel(mTextHint);
            hideHint = false;
        }
        else if(!inputText.isEmpty() && newFileExits(inputText)){
			hintString = mContext
					.getString(R.string.file_name_already_exists);
			mTextHint.setText(hintString);
            onDisplayLabel(mTextHint);
            hideHint = false;
		} else {
			if (getButton(BUTTON_POSITIVE) != null) {
				getButton(BUTTON_POSITIVE).setEnabled(true);
			}
			if (nameLength >= MAX_NAME_LENGTH) {
				mTextHint.setText(R.string.max_name_length_hint);
			}else if (nameLength > 100) {
                hintString = String.format(
                        mContext.getString(R.string.file_name_hint),
                        Integer.toString(MAX_NAME_LENGTH - nameLength));
                mTextHint.setText(hintString);
            }
        }
        if (hideHint) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    onHideLabel(mTextHint);
                }
            }, 100);
        }
        mLastNameLength = inputText.length();
	}

//    private boolean isStringUTF8Encoding(String fileName) {
//        try {
//            byte[] stringBytes = fileName.getBytes("UTF-8");
//            int count = fileName.length();
//            for (int i = 0; i < count; ++i) {
//                Log.d("acmllaugh1", "isStringUTF8Encoding (line 272): char : " + fileName.charAt(i));
//                if (Character.UnicodeBlock.of(fileName.charAt(i)) == Character.UnicodeBlock.EMOTICONS) {
//                    Log.d("acmllaugh1", "isStringUTF8Encoding (line 271): char " + fileName.charAt(i) + " is emotion.");
//                }
//            }
//
//            Log.d("acmllaugh1", "isStringUTF8Encoding (line 270): String is utf8 string.");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//            return false;
//        }
//        return true;
//    }

    private boolean newFileExits(String inputText) {
		File file = new File(mCurrentDirectory + File.separator + inputText);
        if(file.exists()){
            Log.d("TextInputDialog", "new file name : " + file.getName());
            if(mSelectedFileName != null){
                Log.d("TextInputDialog", "selected file name : " + mSelectedFileName);
            }
            if(mSelectedFileName != null && mSelectedFileName.equals(file.getName())){
                return false;
            }
            return true;
        }
		return false;
	}

	private boolean newFileExits(Editable s) {
		
		return false;
	}

	public void onDisplayLabel(View label) {
        if(label.getAlpha() == 0){
            final float offset = label.getHeight();
            final float currentY = label.getY();
            if (currentY != offset) {
                label.setY(offset);
            }
            label.animate().alpha(1).y(0);
        }
    }
	
	public void onHideLabel(View label) {
        if(label.getAlpha() > 0 && !mHideAnimIsplaying){
            label.animate().cancel();
            final float offset = label.getHeight();
            final float currentY = label.getY();
            if (currentY != 0) {
                label.setY(0);
            }
            label.animate().alpha(0).y(offset).withEndAction(new Runnable() {
                @Override
                public void run() {
                    mHideAnimIsplaying = false;
                }
            });
            mHideAnimIsplaying = true;
        }
    }


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            this.dismiss();
        }
        return super.onKeyUp(keyCode, event);
    }
}
