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
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.fineos.fileexplorer.R;

public class ProcessingDialog extends AlertDialog {
	private static final int MAX_FILE_CHECK_COUNT = 108;
	public static final int MAX_NAME_LENGTH = 120;
    private String mInputText;
    private String mTitle;
    private String mMsg;
    private Context mContext;
    private View mView;
    private EditText mFolderName;
    private TextView mTextHint;
    private int mLastNameLength;


    public interface OnFinishListener {
        // return true to accept and dismiss, false reject
        boolean onFinish(String text);
    }

    public ProcessingDialog(Context context, String title, String msg) {
        super(context);
        mTitle = title;
        mMsg = msg;
        mContext = context;
    }

    public String getInputText() {
        return mInputText;
    }

    protected void onCreate(Bundle savedInstanceState) {
    	// Inflate textinput dialog and set dialog title and dialog message.
        mView = getLayoutInflater().inflate(R.layout.process_dialog, null);
        setView(mView);
        setTitle(mTitle);
        setMessage(mMsg);
        setCancelable(false);
        super.onCreate(savedInstanceState);
    }
}
