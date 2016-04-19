package com.fineos.fileexplorer.views;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.fineos.fileexplorer.R;
import com.fineos.fileexplorer.entity.FileInfo.FileCategory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class CustomSearchView extends SearchView implements OnClickListener{

    public static final int MAX_TEXT_LENGTH = 100;

    public interface CategorySelectedListener{
		void onCategorySelected(FileCategory fileCategory);
	}
	
	
	private final static String TAG = CustomSearchView.class.getSimpleName();
	Context context;
	Button categorySelectButton;
	Dialog dialog;
	CategorySelectedListener categorySelectedListener;


	public CustomSearchView(Context context) {
		super(context);
        this.context = context;
        setupCustomLooks();
		
	}
	
	public CustomSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        setupCustomLooks();

    }
	
	public void setCategorySelectedListener(CategorySelectedListener categorySelectedListener) {
		this.categorySelectedListener = categorySelectedListener;
	}

	private void setupCustomLooks(){
        try{		
    		int searchBarId = getResources().getIdentifier("android:id/search_bar", null, null); 
    		LinearLayout searchBarLinearLayout = (LinearLayout)this.findViewById(searchBarId);

			initCategorySelectButton();
    		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    		layoutParams.setMargins(0, 0, 0, 0);
    		searchBarLinearLayout.addView(categorySelectButton, 0, layoutParams);
    		//set search plate(underline of search text).
    		int searchSrcTextId = getResources().getIdentifier("android:id/search_plate", null, null);  
    		View searchPlate  = (View) this.findViewById(searchSrcTextId);
    		searchPlate.setBackgroundColor(0xffffff);
    			
    		int searchEditFrameId = getResources().getIdentifier("android:id/search_edit_frame", null, null);  
    		LinearLayout searchEditFrameLinearLayout = (LinearLayout)this.findViewById(searchEditFrameId);
    		
    		searchEditFrameLinearLayout.removeViewAt(0);//remove the default search icon.
			int queryTextViewId = getResources().getIdentifier("android:id/search_src_text", null, null);
            AutoCompleteTextView autoComplete = (AutoCompleteTextView) this.findViewById(queryTextViewId);
            autoComplete.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_TEXT_LENGTH)});
            autoComplete.setPadding(0, getResources().getDimensionPixelSize(R.dimen.search_input_padding_top), 0, 0);
            autoComplete.setTextSize(17);
			autoComplete.setHintTextColor(getResources().getColor(R.color.fineos_edittext_hint_color));
			setCursorColor(autoComplete);
			Class<?> clazz = Class.forName("android.widget.SearchView$SearchAutoComplete");

			SpannableStringBuilder stopHint = new SpannableStringBuilder();
			stopHint.append("  ");
			stopHint.append(getResources().getString(R.string.search_input_hint));

//			// Add the icon as an spannable
//			Drawable searchIcon = getResources().getDrawable(R.drawable.ic_search);  
//			Method textSizeMethod = clazz.getMethod("getTextSize");  
//			Float rawTextSize = (Float)textSizeMethod.invoke(autoComplete);  
//			int textSize = (int) (rawTextSize * 1.25);  
//			searchIcon.setBounds(0, 0, textSize, textSize);  
//			stopHint.setSpan(new ImageSpan(searchIcon), 1, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//
			// Set the new hint text
			Method setHintMethod = clazz.getMethod("setHint", CharSequence.class);
			setHintMethod.invoke(autoComplete, stopHint);
			categorySelectButton.setOnClickListener(new CategoryButtonClickListener());
    		}catch(Exception e){
    			e.printStackTrace();
    		}
	}

	private void initCategorySelectButton() {
		categorySelectButton = new Button(context);
		categorySelectButton.setText(context.getString(R.string.search_category_all));
		categorySelectButton.setBackgroundColor(0xffffff);
		categorySelectButton.setPadding(0,-5,0,0);
		categorySelectButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, getSearchBarTextSize());
		categorySelectButton.setTextColor(getResources().getColor(R.color.fineos_text_color));
		categorySelectButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.triangle, 0);
//		categorySelectButton.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
	}

	private float getSearchBarTextSize() {
		float density = getResources().getDisplayMetrics().density;
		return getContext().getResources().getDimension(R.dimen.search_bar_font_size)/ density;
	}

	private void setCursorColor(AutoCompleteTextView autoCompleteTextView) {
		try {
			Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
			mCursorDrawableRes.setAccessible(true);
			mCursorDrawableRes.set(autoCompleteTextView, R.drawable.fineos_edittext_cursor); //This sets the cursor resource ID to 0 or @null which will make it visible on white background
		} catch (Exception e) {}
	}


	@Override
	public void onClick(View v) {
		if (v instanceof Button) {
			Button button = (Button) v;
			if (!sameButtonPressed(button)) {
				categorySelectButton.setText(button.getText().toString());
				categorySelectButton.invalidate();
				categorySelectedListener.onCategorySelected(getCategoryFromButtonID(button));
			}
			dialog.dismiss();
		}
	}

	private boolean sameButtonPressed(Button button) {
		return categorySelectButton.getText().equals(button.getText());
	}

	private FileCategory getCategoryFromButtonID(Button button) {
		switch (button.getId()) {
			case R.id.button_select_cetegory_apk:
				return FileCategory.APK;
			case R.id.button_select_cetegory_doc:
				return FileCategory.DOC;
			case R.id.button_select_cetegory_music:
				return FileCategory.MUSIC;
			case R.id.button_select_cetegory_picture:
				return FileCategory.PIC;
			case R.id.button_select_cetegory_video:
				return FileCategory.VIDEO;
			case R.id.button_select_cetegory_all:
			default:
				return null;
		}
	}


	private class CategoryButtonClickListener implements View.OnClickListener{
		@Override
		public void onClick(View v) {
			displayCategorySelectDialog(v);
		}

		private void displayCategorySelectDialog(View v) {
			dialog = new Dialog(context);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setContentView(buildCategorySelectionTable());
			dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
			WindowManager.LayoutParams wlp = calculateDialogDisplayLocation(v);
			dialog.getWindow().setAttributes(wlp);
			dialog.show();
		}

		private WindowManager.LayoutParams calculateDialogDisplayLocation(View v) {
			WindowManager.LayoutParams wlp = dialog.getWindow().getAttributes();
			wlp.gravity = Gravity.TOP | Gravity.LEFT;
			wlp.x = Math.round(CustomSearchView.this.getX()) + v.getWidth()/4;
			wlp.y = Math.round(v.getY())+ v.getHeight() + getResources().getDimensionPixelOffset(R.dimen.search_dialog_y_offset);
			wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
			return wlp;
		}

		private View buildCategorySelectionTable() {
			LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			TableLayout categorySelectionTableLayout = (TableLayout)layoutInflater.inflate(R.layout.tablelayout_file_category_dialog, null);
			setButtonsListenerInCategorySelectDialog(categorySelectionTableLayout);
			return categorySelectionTableLayout;
		}

		private void setButtonsListenerInCategorySelectDialog(TableLayout categorySelectionTableLayout) {
			int childCountInTableLayout = categorySelectionTableLayout.getChildCount();
			for (int i = 0; i < childCountInTableLayout; i++) {
				setButtonListenerForOneRow(categorySelectionTableLayout.getChildAt(i));
			}
		}

		private void setButtonListenerForOneRow(View childView) {
			if (childView instanceof TableRow) {
                TableRow row = (TableRow) childView;
                int childCountInOneRow = row.getChildCount();
				for (int i = 0; i < childCountInOneRow; i++) {
					View view = row.getChildAt(i);
					if (view instanceof Button) { // ChildView is a button, not a divide line.
						view.setOnClickListener(CustomSearchView.this);
					}
				}
			}
		}
	}
	

}
