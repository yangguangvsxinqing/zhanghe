package com.fineos.fileexplorer.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.Button;

import com.fineos.fileexplorer.R;

public class PathButton extends Button {

	private String directoryName;
	private boolean isCurrentDirectory;
	
	public PathButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initButton();
	}
	
	/**
	 * Set init params for the path button, including add an arrow to right of the button text.
	 * */
	private void initButton() {
		setBackground(getResources().getDrawable(R.drawable.selector_path_button));
		setTextColor(getResources().getColor(R.color.fineos_text_color));
		setAllCaps(false);
		setTextSize(17);

		Drawable arrow = getResources().getDrawable(R.drawable.layer_small);
		setCompoundDrawablesWithIntrinsicBounds(null, null, 
				arrow, null);
		setCompoundDrawablePadding(5);	
	}

	public PathButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		initButton();
	}
	
	public PathButton(Context context) {
		super(context);
		initButton();
	}

	public String getDirectoryName() {
		return directoryName;
	}

	public void setDirectoryName(String directoryName) {
		this.directoryName = directoryName;
		this.setText(directoryName);
//		if(directoryName.length() > 5){
//			this.setText("...");
//		}else{
//			this.setText(directoryName);
//		}
	}

	public boolean isCurrentDirectory() {
		return isCurrentDirectory;
	}

	public void setCurrentDirectory(boolean isCurrentDirectory) {
		this.isCurrentDirectory = isCurrentDirectory;
		if(isCurrentDirectory){
			setTextColor(getResources().getColor(R.color.focused_pathBar_color));
		}else{
			setTextColor(getResources().getColor(R.color.unfocused_pathBar_color));
		}
	}
	
	

	

}
