package com.fineos.fileexplorer.views;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.fineos.fileexplorer.R;

/**
 * Created by xiaoyue on 15-7-22.
 */
public class MenuDialog extends Dialog implements DialogInterface.OnKeyListener, View.OnClickListener {

    private View contentView;
    private ListView menuItemListView;
    private MenuDialogListener menuDialogListener;
    private MenuListAdapter adapter;

    public interface MenuDialogListener {
        void onDialogMenuItemSelected(String item);
        void onDialogDismiss();
    }

    public MenuDialog(Context context, String[] menuStrings) {
        super(context, R.style.menu_dialog_style);
        initDialogWindow(context);
        loadMenuStringsToListView(menuStrings);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
            dismiss();
            return true;
        }
        return false;
    }


    @Override
    public void onClick(View v) {
        if (v instanceof TextView) {
            TextView textView = (TextView) v;
            menuDialogListener.onDialogMenuItemSelected(textView.getText().toString());
            dismiss();
        }
    }



    private void loadMenuStringsToListView(String[] menuStrings) {
        adapter = new MenuListAdapter(getContext(), R.layout.menu_item_layout, menuStrings);
        menuItemListView.setAdapter(adapter);
    }

    @Override
    public void show() {
        super.show();
    }

    private void initDialogWindow(Context context) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentView = layoutInflater.inflate(R.layout.menu_dialog_layout, null, false);
        menuItemListView = (ListView) contentView.findViewById(R.id.listview_menu_dialog);
        setContentView(contentView);
        getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        getWindow().setBackgroundDrawable(new ColorDrawable(0));
        WindowManager.LayoutParams wlp = getWindow().getAttributes();
        wlp.gravity = Gravity.BOTTOM | Gravity.START;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(wlp);
        setOnKeyListener(this);
    }

    public void setItemClickedListener(MenuDialogListener clickListener) {
        Log.d("acmllaugh1", "setItemClickedListener: set item click listener");
        this.menuDialogListener = clickListener;
    }

    @Override
    public void dismiss() {
        super.dismiss();
        menuDialogListener.onDialogDismiss();
    }

    private class MenuListAdapter extends ArrayAdapter<String> {
        public MenuListAdapter(Context context, int resource, String[] objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            view.setOnClickListener(MenuDialog.this);
            return view;
        }
    }
}
