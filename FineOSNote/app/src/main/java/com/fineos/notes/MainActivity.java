package com.fineos.notes;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fineos.notes.adapter.MenuListAdapter;
import com.fineos.notes.adapter.NotesListAdapter;
import com.fineos.notes.adapter.TopMenuListAdapter;
import com.fineos.notes.app.MyApplication;
import com.fineos.notes.bean.Folder;
import com.fineos.notes.bean.Note;
import com.fineos.notes.bean.NotePicture;
import com.fineos.notes.constant.Constant;
import com.fineos.notes.db.FolderDao;
import com.fineos.notes.db.NoteDao;
import com.fineos.notes.db.PictureDao;
import com.fineos.notes.util.CommonUtil;
import com.fineos.notes.view.BottomMenuDialog;
import com.fineos.notes.view.CircleImageView;
import com.fineos.notes.view.SlidingMenu;
import com.fineos.notes.view.SuperSpinner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fineos.app.AlertDialog;
import fineos.app.ColorHelper;
import fineos.widget.SwimViewLayout;

//import fineos.widget.SearchView;

//import android.app.AlertDialog;

public class MainActivity extends Activity implements OnClickListener, CompoundButton.OnCheckedChangeListener, SuperSpinner.OnPopItemClickListener {
    private SlidingMenu mMenu;
    private ImageView newNoteBtn, menu;
    private ListView menuList;
    private Button mangeBtn;
    private List<Note> notes;
    private List<Folder> folders;
    private RelativeLayout emptTip;
    private ListView gridNotes;
    private SwimViewLayout swim;
    private RelativeLayout titleLayout;
    private MenuListAdapter menuAdapter;
    private List<HashMap<String, Object>> listData;
    private List<HashMap<String, Object>> listMenu;
    private String folderName = Constant.All_NOTE;//default name
    private NotesListAdapter mNotesListAdapter;
    private MultiChoiceModeListener mChoiceModeListener;
    private LinearLayout bottom;
    private ImageView move, delete;
    //    private SearchView searchView;
    private com.fineos.notes.view.CircleImageView photo;//touxiang
    private TextView name;//xingming
    private MyApplication app;
    private int selectedPosition = 0;
    private String selectedForlderName = Constant.All_NOTE;//default name
    private String newFolderName;//rename folder
    private int noteSize;//someone folder note'size
    private int notesSize;//all folder notes'size
    private ActionMode actionMode;
    private CheckBox title_check;
    private TextView nofind;
    private int length = 0;
    private String addStr;
    private TopMenuListAdapter topAdapter;

    /**
     * declear searchView view
     */
    private RelativeLayout searchView;
    private EditText searchInput;
    private TextView searchHint;
    private ImageView searchClear;
    private View view_searchLayout;

    private SuperSpinner superSpinner;

    //dpc @{
    private ImageView searchbg;
    private  boolean isActionMode = false;
    //@}
//    private EditText addfolderText;
//    private TextVie//
//HQ01809083 HQ01809123
     private int countsSelected;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initActionBar();
        initAllData();
//        doIntent();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (searchInput != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
        }
    }

    private void doIntent() {
        initActionBar();
        Intent intent = getIntent();
        folderName = intent.getStringExtra(Constant.TABLE_NOTE_FOLDER);
        selectedForlderName = folderName;
        selectedPosition = intent.getIntExtra("selectedPosition", 0);
        if (selectedForlderName == null || selectedForlderName.equals(getString(R.string.allnote)) || selectedForlderName.equals(Constant.All_NOTE)) {
            Log.w(Constant.TAG, "MainActivity Constant.All_NOTE:" + Constant.All_NOTE);
            selectedForlderName = Constant.All_NOTE;
            initAllData();
        } else {
            Log.w(Constant.TAG, "MainActivity folder.equals(Constant.All_NOTE:" + folderName.equals(Constant.All_NOTE));
            setFolderData(selectedForlderName);
        }
        try {

        }catch (Exception e){
        }
        title_check.setText(selectedForlderName);
    }


    private void initView() {
        app = (MyApplication) getApplication();

        gridNotes = (ListView) findViewById(R.id.gridNotes);
        bottom = (LinearLayout) findViewById(R.id.bottom);
        mMenu = (SlidingMenu) findViewById(R.id.id_menu);//sliding menu
        newNoteBtn = (ImageView) findViewById(R.id.btn_new_note);
        menuList = (ListView) findViewById(R.id.menu_list);
        mangeBtn = (Button) findViewById(R.id.manage);
        emptTip = (RelativeLayout) findViewById(R.id.empt_tip);
        move = (ImageView) findViewById(R.id.iv_move);
        delete = (ImageView) findViewById(R.id.iv_delete);
        photo = (CircleImageView) findViewById(R.id.iv_photo);
        name = (TextView) findViewById(R.id.tv_name);
//      searchView = (SearchView) findViewById(R.id.search_view);
        nofind = (TextView) findViewById(R.id.tv_note_nofind);


        addSearchView(gridNotes);
        gridNotes.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
        mChoiceModeListener = new GridChoiceModeListener();
        gridNotes.setMultiChoiceModeListener(mChoiceModeListener);
        gridNotes.setOnItemClickListener(new GridOnItemListener());
        newNoteBtn.setOnClickListener(this);
        delete.setOnClickListener(this);
        move.setOnClickListener(this);

        //theme color
        Drawable bg = newNoteBtn.getDrawable();
        int color =ColorHelper.getColor(getResources(), R.color.notes_add_note);
        bg.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

//        // Swim控件
        swim = (SwimViewLayout) findViewById(R.id.swimLayout);
        swim.setSwimAndContentView(newNoteBtn, gridNotes);
//      swim.setSwimVisibility(true);
        mMenu.setScroll(false);

        superSpinner = (SuperSpinner) findViewById(R.id.spinner_menu);
        superSpinner.setPopupViewStyle(SuperSpinner.STYLE_LIST);
        superSpinner.setShowMode(SuperSpinner.POPUP_BELOW);
        superSpinner.setOnPopItemClickListener(this);
        superSpinner.setDismissListener(new SuperSpinner.PopupWinDismissListener() {
            @Override
            public void onPopupWinDismiss(SuperSpinner superSpinner) {
                title_check.setChecked(false);
            }
        });



//        //  HQ01690326 dpc @{
//        gridNotes.setOnScrollListener(new AbsListView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(AbsListView view, int scrollState) {
//                InputMethodManager inputManager = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//                if (getCurrentFocus() != null) {
//                    inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
//                    getCurrentFocus().clearFocus();
//                }
//            }
//
//            @Override
//            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//
//            }
//        });
        //HQ01690326 @}
    }

    private void addSearchView(ListView listView) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view_searchLayout = inflater.inflate(R.layout.searchview_layout, null);

        //HQ01690323 dpc @{
        searchbg = (ImageView) view_searchLayout.findViewById(R.id.searchbg);
        //@}
        searchView = (RelativeLayout) view_searchLayout.findViewById(R.id.relayout_searchView);
        searchInput = (EditText) view_searchLayout.findViewById(R.id.ed_search_input);
        searchHint = (TextView) view_searchLayout.findViewById(R.id.tv_search_hint);
        searchClear = (ImageView) view_searchLayout.findViewById(R.id.iv_search_clear);
//        searchInput.setInputType(InputType.TYPE_CLASS_TEXT
//                | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
        searchInput.addTextChangedListener(new TextWatchListener());

        searchInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
//                    newNoteBtn.setVisibility(View.GONE);
                    searchHint.setVisibility(View.GONE);
                }
                // //HQ01690323 dpc
                //else
                else if(TextUtils.isEmpty(searchInput.getText().toString())){
                //@}
//                    newNoteBtn.setVisibility(View.VISIBLE);
                    searchHint.setVisibility(View.VISIBLE);
                }
            }
        });
        searchClear.setOnClickListener(this);

////        searchView = (fineos.widget.SearchView) view.findViewById(R.id.search_view);
//        searchView.setOnQueryTextListener(this);
//        searchView.setOnCloseListener(this);
//        searchView.setFocusable(true);
        listView.addHeaderView(view_searchLayout);
    }

    private void initActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = layoutInflater.inflate(R.layout.main_actionbar, null);
        actionBar.setCustomView(v, new ActionBar.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        titleLayout = (RelativeLayout) v.findViewById(R.id.relayout_titlebar);
        title_check = (CheckBox) v.findViewById(R.id.check_titlte);
        menu = (ImageView) findViewById(R.id.iv_menu);// menu

        title_check.setOnCheckedChangeListener(this);
        menu.setOnClickListener(this);
    }

    /**
     * all folder data
     */
    private void initAllData() {
        menu.setVisibility(View.GONE);

        listData = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> map = null;
        NoteDao dao = new NoteDao(this);
        notes = dao.selectAll();
        notesSize = notes.size();
        if (notes.size() > 0) {
            for (int i = 0; i < notes.size(); i++) {
                if (notes.get(i).isValid()) {
                    map = new HashMap<String, Object>();
                    map.put(Constant.TABLE_NOTE_ID, notes.get(i).getId());
                    map.put(Constant.TABLE_NOTE_FOLDER, notes.get(i)
                            .getFolder());
                    map.put(Constant.TABLE_NOTE_TITLE, notes.get(i).getTitle());
                    map.put(Constant.TABLE_NOTE_DETAIL, notes.get(i)
                            .getDetail());
                    map.put(Constant.TABLE_NOTE_BG, notes.get(i)
                            .getBackground());
                    map.put(Constant.TABLE_NOTE_PICTURE, notes.get(i)
                            .getPicture());
                    map.put(Constant.TABLE_NOTE_DATA, notes.get(i).getData());
                    listData.add(map);
                } else {
                    Log.w(Constant.TAG, "输入数据无效");
                }
            }
            if (listData.size() > 0) {
                gridNotes.setVisibility(View.VISIBLE);
                emptTip.setVisibility(View.GONE);
//                gridNotes.addHeaderView(view_searchLayout);
                searchView.setVisibility(View.VISIBLE);
                mNotesListAdapter = new NotesListAdapter(this, listData);
                gridNotes.setAdapter(mNotesListAdapter);
            } else {
//                searchView.setVisibility(View.GONE);
//                gridNotes.removeHeaderView(view_searchLayout);
                gridNotes.setVisibility(View.GONE);
                emptTip.setVisibility(View.VISIBLE);
            }

        } else {
            gridNotes.setVisibility(View.GONE);
            emptTip.setVisibility(View.VISIBLE);
//            searchView.setVisibility(View.GONE);
        }
    }


    private void setSelectedPosition(int position) {
        selectedPosition = position;
    }

    /**
     * 文件夹的便签内容
     *
     * @param folder
     * @return
     */
    private void setFolderData(String folder) {
        menu.setVisibility(View.VISIBLE);
//        gridNotes.removeHeaderView(view_searchLayout);
        //M: dupengcan ,other folder note marginTop is too large @{
        searchbg.setVisibility(View.GONE);
        //@}
        searchView.setVisibility(View.GONE);

        List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> map = null;
        NoteDao dao = new NoteDao(this);
        notes = dao.selectByFolder(folder);
        noteSize = notes.size();
        if (notes.size() > 0) {
            for (int i = 0; i < notes.size(); i++) {
                if (notes.get(i).isValid()) {
                    map = new HashMap<String, Object>();
                    map.put(Constant.TABLE_NOTE_ID, notes.get(i).getId());
                    map.put(Constant.TABLE_NOTE_FOLDER, notes.get(i)
                            .getFolder());
                    map.put(Constant.TABLE_NOTE_TITLE, notes.get(i).getTitle());
                    map.put(Constant.TABLE_NOTE_DETAIL, notes.get(i)
                            .getDetail());
                    map.put(Constant.TABLE_NOTE_BG, notes.get(i)
                            .getBackground());
                    map.put(Constant.TABLE_NOTE_PICTURE, notes.get(i)
                            .getPicture());
                    map.put(Constant.TABLE_NOTE_DATA, notes.get(i).getData());
                    list.add(map);
                }
            }
            if (list.size() > 0) {
                gridNotes.setVisibility(View.VISIBLE);
                emptTip.setVisibility(View.GONE);
                mNotesListAdapter = new NotesListAdapter(this, list);
                gridNotes.setAdapter(mNotesListAdapter);

            } else {
                gridNotes.setVisibility(View.GONE);
                emptTip.setVisibility(View.VISIBLE);
            }
        } else {
            map = new HashMap<String, Object>();
            map.put(Constant.TABLE_NOTE_ID, 8);
            map.put(Constant.TABLE_NOTE_TITLE, "title");
            map.put(Constant.TABLE_NOTE_DETAIL, "detail");
            map.put(Constant.TABLE_NOTE_BG, 2);
            long xx = -2;
            map.put(Constant.TABLE_NOTE_DATA, xx);
            list.add(map);
            mNotesListAdapter = new NotesListAdapter(this, list);
            gridNotes.setAdapter(mNotesListAdapter);
            newNoteBtn.setVisibility(View.VISIBLE);
            emptTip.setVisibility(View.VISIBLE);
        }
    }


    /**
     * 新建文件夹
     *
     * @param list
     * @param
     */
    private void addFolder(final List<HashMap<String, Object>> list, final int position) {
        length = 0;
        final HashMap<String, Object> map = new HashMap<String, Object>();
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.addfolder_layout, null);
        final EditText editText = (EditText) view.findViewById(R.id.et_addfolder);
        final TextView textView = (TextView) view.findViewById(R.id.tv_addfolder);
        textView.setVisibility(View.GONE);
        editText.setHint(getString(R.string.addfolder_hint));
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                addStr = s.toString().trim();
                length = addStr.length();
                if (length > 16) {
                    textView.setVisibility(View.VISIBLE);
                    textView.setText(getString(R.string.addfolder_outmax_warn));
                } else {
                    textView.setVisibility(View.GONE);
                }
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(getString(R.string.addfolder));
        builder.setCancelable(false);
        builder.setView(view);
        builder.create();
        builder.setPositiveButton(getString(R.string.ok), null);
        builder.setNegativeButton(getString(R.string.cancel), null);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        //HQ01713377
        CommonUtil.openKeybord(editText, MainActivity.this);
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isAddfolder = true;
                if (length == 0) {
                    textView.setVisibility(View.VISIBLE);
                    textView.setText(getString(R.string.addfolder_empt_warn));
                } else if (length > 16) {
                    textView.setVisibility(View.VISIBLE);
                    textView.setText(getString(R.string.addfolder_outmax_warn));
                } else {
                    for (HashMap<String, Object> m : list) {
                        for (Map.Entry<String, Object> enty : m.entrySet()) {
                            String value = enty.getValue().toString();
                            if (value.equals(addStr)) {
                                isAddfolder = false;
                            }
                        }
                    }
                    if (addStr.equals("All Notes") || addStr.equals("Add Folder")) {
                        isAddfolder = false;
                    }
                    if (isAddfolder) {//folder can be add
                        map.put("folderName", addStr);
                        list.add(list.size() - 1, map);
                        FolderDao folderDao = new FolderDao(
                                MainActivity.this);
                        Folder folder = new Folder();
                        folder.setFolder(addStr);
                        folderDao.add(folder);
                        //HQ01713377
                        CommonUtil.closeKeybord(editText, MainActivity.this);
                        alertDialog.dismiss();
                        title_check.setText(addStr);
                        title_check.setChecked(false);
                        setSelectedPosition(position);
                        selectedForlderName = addStr;
                        setFolderData(addStr);
                    } else {
                        textView.setVisibility(View.VISIBLE);
                        textView.setText(getString(R.string.addfolder_samename_warn));
                    }
                }
            }

        });
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //HQ01713377
                CommonUtil.closeKeybord(editText, MainActivity.this);
                alertDialog.dismiss();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constant.MAIN_ADD_REQUESTCODE://add note requestcode
                searchInput.setText("");
                searchInput.clearFocus();
                Log.w("dpc", "MainActivity onActivityResult() selectedForlderName:"+selectedForlderName);
                if (selectedForlderName.equals(getString(R.string.allnote)) || selectedForlderName.equals(Constant.All_NOTE)) {
		    selectedForlderName = getString(R.string.allnote);
                    initAllData();
                } else {
                    setFolderData(selectedForlderName);
                }
                break;
            case Constant.MAIN_EDT_REQUESTCODE://edit note requestcode
                searchInput.setText("");
                searchInput.clearFocus();
                if (selectedForlderName.equals(getString(R.string.allnote)) || selectedForlderName.equals(Constant.All_NOTE)) {
		    selectedForlderName = getString(R.string.allnote);
                    initAllData();
                } else {
                    setFolderData(selectedForlderName);
                }
                break;
            case Constant.MOVENOTE_REQUESTCODE:// move requestcode
                if (data != null) {
                    String name = data.getStringExtra(Constant.TABLE_NOTE_FOLDER);
                    selectedPosition = data.getIntExtra("selectedPosition", 0);

                    selectedForlderName = name;
                    title_check.setText(selectedForlderName);
                    if (name.equals(getString(R.string.allnote)) || name.equals(Constant.All_NOTE)) {
                        initAllData();
                    } else {
                        setFolderData(name);
                    }
                    actionMode.finish();
                    mNotesListAdapter.getSelectItems().clear();
                    mNotesListAdapter.setActionModeState(false);
                    mNotesListAdapter.notifyDataSetChanged();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_new_note:
                Intent intent = new Intent(this, AddNoteActivity.class);
                intent.putExtra(Constant.TABLE_NOTE_FOLDER, selectedForlderName);
                intent.putExtra("intentFlag", 0);//0 新建 1 预览 编辑
                intent.putExtra("selectedPosition", selectedPosition);//0 新建 1 预览 编辑
//                startActivity(intent);
//                finish();
                startActivityForResult(intent, Constant.MAIN_ADD_REQUESTCODE);
                break;
            case R.id.iv_search_clear:
                CommonUtil.closeKeybord(searchInput, MainActivity.this);
                searchInput.setText("");
                searchInput.requestFocus();
                break;
            case R.id.iv_move:
                //HQ01809123 @{
                if (countsSelected == 0) {
                    cancleDelete();
                    break;
                }
                //@}
                ArrayList<Integer> idList = new ArrayList<Integer>();
                NotesListAdapter idSizeAdapter = mNotesListAdapter;
                for (int i = 0; i < idSizeAdapter.getSelectItems().size(); i++) {
                    Integer selectId = idSizeAdapter.getSelectItems().get(i);
                    idList.add(selectId);
                }
                Intent intent1 = new Intent(this, MoveNoteActivity.class);
                intent1.putIntegerArrayListExtra("id", idList);
                intent1.putExtra("folderName", selectedForlderName);
                startActivityForResult(intent1, Constant.MOVENOTE_REQUESTCODE);
                break;
            case R.id.iv_delete:
                //HQ01809083 @{
                if (countsSelected == 0) {
                    cancleDelete();
                    break;
                }
                //@}
//                deleteNote(folderName);
                operaDialog(selectedForlderName, getString(R.string.delete_note), getString(R.string.delete_note_message));
                break;
            case R.id.iv_menu:
                Log.w("dpc","MainActivity onClick() selectedForlderName:"+selectedForlderName);
                showBottomMenuDialog(selectedForlderName);
                break;
            default:
                break;
        }

    }


    PopupWindow popupwindow;
    ListView topMenu;
    /**
     * show top folder
     * @param menuItems
     */
    private void tryShowMenuDialog(List<HashMap<String, Object>> menuItems) {
        topAdapter = new TopMenuListAdapter(MainActivity.this, menuItems);
        if (null != superSpinner) {
            superSpinner.setAdapter(topAdapter);
            topAdapter.setSelectedPosition(selectedPosition);
            superSpinner.showPopupWin();
        }
    }


    private void showTopMenuDialog(final List<HashMap<String, Object>> menuItems) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.menu_dialog_layout, null);
        popupwindow = new PopupWindow(customView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupwindow.setOutsideTouchable(true);
        popupwindow.setAnimationStyle(R.style.PopupWindowAnimation);
        // 自定义view添加触摸事件
        customView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (popupwindow != null && popupwindow.isShowing()) {
                    title_check.setChecked(false);
                    popupwindow.dismiss();
                    popupwindow = null;
                }
                return false;
            }
        });
        topMenu = (ListView) customView.findViewById(R.id.listview_menu_dialog);

        final TopMenuListAdapter adapter = new TopMenuListAdapter(MainActivity.this, menuItems);
        adapter.setSelectedPosition(selectedPosition);
        topMenu.setAdapter(adapter);

        topMenu.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                folderName = (String) menuItems.get(position).get("folderName");
                selectedForlderName = folderName;
                if (selectedForlderName.equals(getString(R.string.addfolder))) {
//                    addFolder(listMenu);
                    title_check.setChecked(true);
                    popupwindow.dismiss();
                    initAllData();

                } else if (selectedForlderName.equals(getString(R.string.allnote))) {
                    menu.setVisibility(View.GONE);
                    searchView.setVisibility(View.VISIBLE);
                    initAllData();
                    title_check.setText(selectedForlderName);
                    title_check.setChecked(false);
                    popupwindow.dismiss();
                    setSelectedPosition(position);
                } else {
                    setSelectedPosition(position);
                    menu.setVisibility(View.VISIBLE);
                    searchView.setVisibility(View.GONE);
                    setFolderData(selectedForlderName);
                    title_check.setText(selectedForlderName);
                    title_check.setChecked(false);
                    popupwindow.dismiss();
                }
            }
        });
    }

    /**
     * rename or delete other folder
     * @param folder
     */
    private void showBottomMenuDialog(final String folder) {
        String[] items = {getResources().getString(R.string.delete_folder), getResources().getString(R.string.rename_folder)};
        BottomMenuDialog bottomMenuDialog = new BottomMenuDialog(MainActivity.this, items);
        bottomMenuDialog.setCancelable(true);
        bottomMenuDialog.setItemClickedListener(new BottomMenuDialog.MenuDialogListener() {
            @Override
            public void onDialogMenuItemSelected(String item) {
                if (item.equals(getResources().getString(R.string.rename_folder))) {
                    renameFolderDialog(folder, getResources().getString(R.string.rename_folder));
                } else {
                    operaDialog(folder, getResources().getString(R.string.delete_folder), getResources().getString(R.string.delete_folder_message));
                }

            }

        });
        bottomMenuDialog.show();

    }

    /**
     * delete folder
     * @param folderName
     */
    private void deleteFolder(String folderName) {
        Log.w("dpc", "MainActivity deleteFolder() folderName:" + folderName);
        FolderDao folderDao = new FolderDao(MainActivity.this);
        folderDao.deleteFolder(folderName);
        NoteDao noteDao = new NoteDao(MainActivity.this);
        PictureDao pictureDao = new PictureDao(MainActivity.this);
        List<Note> listNotes = noteDao.selectByFolder(folderName);
        if (listNotes.size() > 0) {//note is not empty
            for (int i = 0; i < listNotes.size(); i++) {
                int noteid = listNotes.get(i).getId();
                pictureDao.deletePictureByNoteId(noteid);
            }
            noteDao.deleteNoteByFolderName(folderName);
        }

    }

    /**
     * rename folder
     * @param oldName
     * @param newName
     */
    private void renameFolder(String oldName, String newName) {
        FolderDao folderDao = new FolderDao(MainActivity.this);
        folderDao.updateFolder(oldName, newName);

        NoteDao noteDao = new NoteDao(MainActivity.this);
        noteDao.updateNoteFolderByFolder(oldName, newName);


    }

    /**
     * delete note
     * @param name
     */
    private void deleteNote(String name) {
        NoteDao dao = new NoteDao(MainActivity.this);
        for (int i = 0; i < mNotesListAdapter.getSelectItems().size(); i++) {
            String selectId = mNotesListAdapter.getSelectItems().get(i).toString();
            dao.deleteById(selectId);
        }
        if (name.equals(getString(R.string.allnote))) {
            initAllData();
        } else {
            setFolderData(name);
        }

        mNotesListAdapter.getSelectItems().clear();
        mNotesListAdapter.setActionModeState(false);
        gridNotes.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        gridNotes.setMultiChoiceModeListener(mChoiceModeListener);
        mNotesListAdapter.notifyDataSetChanged();
        gridNotes.refreshDrawableState();
        gridNotes.invalidate();
    }


    private void renameFolderDialog(final String folder, String title) {
        length = 0;

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.addfolder_layout, null);
        final EditText editText = (EditText) view.findViewById(R.id.et_addfolder);
        final TextView textView = (TextView) view.findViewById(R.id.tv_addfolder);
//        editText.setHint(folder);
        editText.setText(folder);
//        editText.setHint(getString(R.string.addfolder_hint));
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                addStr = s.toString().trim();
                length = addStr.length();
                if (length > 16) {
                    textView.setVisibility(View.VISIBLE);
                    textView.setText(getString(R.string.addfolder_outmax_warn));
                } else {
                    textView.setVisibility(View.GONE);
                }
            }
        });
        final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle(title)
                .setView(view)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.ok), null)
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
        //HQ01713377
        CommonUtil.openKeybord(editText, MainActivity.this);
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (length == 0) {
                    textView.setVisibility(View.VISIBLE);
                    textView.setText(getString(R.string.addfolder_empt_warn));
                } else if (length > 16) {
                    textView.setVisibility(View.VISIBLE);
                    textView.setText(getString(R.string.addfolder_outmax_warn));
                } else {
                    if (!addStr.equals(folder)) {
                        renameFolder(folder, addStr);
                        //HQ01713377
                        CommonUtil.closeKeybord(editText, MainActivity.this);
                        alertDialog.dismiss();
                        selectedForlderName = addStr;
                        title_check.setText(addStr);
                    } else {
                        textView.setVisibility(View.VISIBLE);
                        textView.setText(getString(R.string.addfolder_samename_warn));
                    }
                }
            }
        });
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //HQ01713377
                CommonUtil.closeKeybord(editText, MainActivity.this);
                alertDialog.dismiss();
            }
        });

    }

    /**
     * delete  folder or delete note
     * @param name
     * @param dialog_title
     * @param message
     */
    private void operaDialog(final String name, final String dialog_title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(message);
        builder.setTitle(dialog_title);
        builder.setPositiveButton(getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        if (dialog_title.equals(getResources().getString(R.string.delete_folder))) {
                            deleteFolder(name);
//                          menu.setVisibility(View.GONE);
                            initAllData();
                            setSelectedPosition(0);
                            title_check.setText(getString(R.string.allnote));
                        } else if (dialog_title.equals(getResources().getString(R.string.delete_note))) {
                            deleteNote(name);
                        }
                    }
                })
                .setNegativeButton(
                        getResources().getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                cancleDelete();
                            }
                        }).show();
    }
//HQ01809083 @{
    private void cancleDelete() {
        mNotesListAdapter.getSelectItems().clear();
        mNotesListAdapter.unChooseAll();
        mNotesListAdapter.setActionModeState(false);
        gridNotes.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
        gridNotes.setMultiChoiceModeListener(mChoiceModeListener);
        mNotesListAdapter.notifyDataSetChanged();
    }
//@}

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.check_titlte:
                if (isChecked) {
                    listMenu = initTopMenu();
                    tryShowMenuDialog(listMenu);
                }
                break;
        }
    }

    /**
     * folder list
     * @return
     */
    private List<HashMap<String, Object>> initTopMenu() {
        listMenu = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> map = null;
        FolderDao dao = new FolderDao(this);
        folders = dao.selectAll();
        if (!(folders.size() == 0)) {
            for (int i = 0; i < folders.size(); i++) {
                map = new HashMap<String, Object>();
                if (folders.get(i).getFolder()
                        .equals(Constant.All_NOTE)) {
                    map.put("folderName", getString(R.string.allnote));
                    listMenu.add(map);
                } else if (folders.get(i).getFolder()
                        .equals(Constant.CALL_RECORDING)) {
                    map.put("folderName", getString(R.string.allrecode));
                    listMenu.add(map);
                } else if (folders.get(i).getFolder()
                        .equals(Constant.ADD_FOLDER)) {
                    map.put("folderName", getString(R.string.addfolder));
                    listMenu.add(map);
                } else {
                    map.put("folderName", folders.get(i).getFolder());
                    listMenu.add(listMenu.size() - 1, map);
                }
            }
        }
        return listMenu;
    }


    class  TextWatchListener implements  TextWatcher{

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String newString = s.toString();
            if (newString.length() == 0) {
                searchClear.setVisibility(View.GONE);
            } else {
                searchClear.setVisibility(View.VISIBLE);
            }
            showSearchResult(newString);
            searchInput.requestFocus();
        }
    }

    //dpc HQ01687841 @{
    private void showSearchResult(String searchWords) {
//        newNoteBtn.setVisibility(View.GONE);
        List<HashMap<String, Object>> listData = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> map = null;
        if (TextUtils.isEmpty(searchWords)) {
            initAllData();
            nofind.setVisibility(View.GONE);
        } else {
            List<Integer> p_ids = null;
            PictureDao pictureDao = new PictureDao(MainActivity.this);
            List<NotePicture> pictureList = pictureDao.selectByKeyWorld(searchWords);
            int pictureSize = pictureList.size();
            Log.w("dpc", "pictureSize=" + pictureSize);
            if (pictureSize > 0) {
                p_ids = new ArrayList<Integer>();
                for (int i = 0; i< pictureSize;i++) {
                    p_ids.add(pictureList.get(i).getNoteId());
                }
            }
            List<Integer> n_ids = null;
            NoteDao noteDao = new NoteDao(MainActivity.this);
            notes = noteDao.selectByKeyWorld(searchWords);
            noteSize = notes.size();
            Log.w("dpc", "noteSize=" + noteSize);
            if (noteSize > 0) {
                n_ids = new ArrayList<Integer>();
                for (int i = 0; i < noteSize; i++) {
                    n_ids.add(notes.get(i).getId());
                }
            }

            // at least one case is not null
            List<Note> selectNotes = new ArrayList<>();
            if (p_ids == null & n_ids != null) {//n_ids is not null
                for (int i = 0;i< n_ids.size(); i++){
                    // one id is a Note
                    selectNotes.add(noteDao.selectById(n_ids.get(i)).get(0));
                }
            }else if (n_ids == null & p_ids != null){//p_ids is not null
                for (int i = 0;i< p_ids.size(); i++){
                    // one id is a Note
                    selectNotes.add(noteDao.selectById(p_ids.get(i)).get(0));
                }
            }else if(n_ids != null & p_ids != null){// n_ids is not null and p_ids is not null
                for(int i = 0; i< p_ids.size(); i++){
                    if (!n_ids.contains(p_ids.get(i))) {
                        n_ids.add(p_ids.get(i));
                    }
                }
                for (int i = 0;i< n_ids.size(); i++){
                    // one id is a Note
                    selectNotes.add(noteDao.selectById(n_ids.get(i)).get(0));
                }
            }
            int size = selectNotes.size();
            if (size > 0) {
                Collections.sort(selectNotes, new Comparator<Note>() {
                    @Override
                    public int compare(Note note1, Note note2) {
                        int comparetime = note1.getData().compareTo(note2.getData());
                        if (comparetime == 0) {
                            return (note1.getId() == note2.getId() ? 0 : (note1.getId() > note2.getId() ? 1 : -1));
                        }
                        return -comparetime;
                    }
                });
                for (int i = 0; i < size; i++) {
                    if (selectNotes.get(i).isValid()) {
                        map = new HashMap<String, Object>();
                        map.put(Constant.TABLE_NOTE_ID, selectNotes.get(i).getId());
                        map.put(Constant.TABLE_NOTE_FOLDER, selectNotes.get(i)
                                .getFolder());
                        map.put(Constant.TABLE_NOTE_TITLE, selectNotes.get(i)
                                .getTitle());
                        map.put(Constant.TABLE_NOTE_DETAIL, selectNotes.get(i)
                                .getDetail());
                        map.put(Constant.TABLE_NOTE_BG, selectNotes.get(i)
                                .getBackground());
                        map.put(Constant.TABLE_NOTE_PICTURE, selectNotes.get(i)
                                .getPicture());
                        map.put(Constant.TABLE_NOTE_DATA, selectNotes.get(i)
                                .getData());
                        listData.add(map);
                    } else {
                        Log.w(Constant.TAG, "输入数据无效");
                    }
                }
                if (listData.size() > 0) {
                    nofind.setVisibility(View.GONE);
//                    gridNotes.setVisibility(View.VISIBLE);
                    mNotesListAdapter = new NotesListAdapter(MainActivity.this, listData);
                    gridNotes.setAdapter(mNotesListAdapter);
                    mNotesListAdapter.notifyDataSetChanged();
                    gridNotes.refreshDrawableState();
                } else {
                    mNotesListAdapter.notifyDataSetChanged();
                    gridNotes.refreshDrawableState();
//                    gridNotes.setVisibility(View.GONE);
                    nofind.setVisibility(View.VISIBLE);
                }
            } else {
                nofind.setVisibility(View.VISIBLE);
//                gridNotes.setVisibility(View.GONE);
            }
        }
    }

// private void showSearchResult(String searchWords) {
////        newNoteBtn.setVisibility(View.GONE);
//        List<HashMap<String, Object>> listData = new ArrayList<HashMap<String, Object>>();
//        HashMap<String, Object> map = null;
//        if (TextUtils.isEmpty(searchWords)) {
//            initAllData();
//            nofind.setVisibility(View.GONE);
//        } else {
//            NoteDao dao = new NoteDao(MainActivity.this);
//            notes = dao.selectByKeyWorld(searchWords);
//            noteSize = notes.size();
//            int size = notes.size();
//            if (size > 0) {
//                for (int i = 0; i < size; i++) {
//                    if (notes.get(i).isValid()) {
//                        map = new HashMap<String, Object>();
//                        map.put(Constant.TABLE_NOTE_ID, notes.get(i).getId());
//                        map.put(Constant.TABLE_NOTE_FOLDER, notes.get(i)
//                                .getFolder());
//                        map.put(Constant.TABLE_NOTE_TITLE, notes.get(i)
//                                .getTitle());
//                        map.put(Constant.TABLE_NOTE_DETAIL, notes.get(i)
//                                .getDetail());
//                        map.put(Constant.TABLE_NOTE_BG, notes.get(i)
//                                .getBackground());
//                        map.put(Constant.TABLE_NOTE_PICTURE, notes.get(i)
//                                .getPicture());
//                        map.put(Constant.TABLE_NOTE_DATA, notes.get(i)
//                                .getData());
//                        listData.add(map);
//                    } else {
//                        Log.w(Constant.TAG, "输入数据无效");
//                    }
//                }
//                if (listData.size() > 0) {
//                    nofind.setVisibility(View.GONE);
////                    gridNotes.setVisibility(View.VISIBLE);
//                    mNotesListAdapter = new NotesListAdapter(MainActivity.this, listData);
//                    gridNotes.setAdapter(mNotesListAdapter);
//                    mNotesListAdapter.notifyDataSetChanged();
//                    gridNotes.refreshDrawableState();
//                } else {
//                    mNotesListAdapter.notifyDataSetChanged();
//                    gridNotes.refreshDrawableState();
////                    gridNotes.setVisibility(View.GONE);
//                    nofind.setVisibility(View.VISIBLE);
//                }
//            } else {
//                nofind.setVisibility(View.VISIBLE);
////                gridNotes.setVisibility(View.GONE);
//            }
//        }
//    }
      // HQ01687841 @}
    @Override
    public void onPopItemClick(SuperSpinner superSpinner, int position, long id) {
//        folderName = (String) superSpinner.get(position).get("folderName");
        folderName = (String) listMenu.get(position).get("folderName");
        if (folderName.equals(getString(R.string.addfolder))) {
            addFolder(listMenu, position);
        } else if (folderName.equals(getString(R.string.allnote))) {
            selectedForlderName = folderName;
            title_check.setText(selectedForlderName);
            title_check.setChecked(false);
            setSelectedPosition(position);
            initAllData();
        } else {
            selectedForlderName = folderName;
            title_check.setText(selectedForlderName);
            title_check.setChecked(false);
            setSelectedPosition(position);
            setFolderData(selectedForlderName);
        }
        Log.w("dpc", "MainActivity onPopItemClick() selectedForlderName:" + selectedForlderName);
    }


    /**
     * @author ubuntu
     */
    class GridOnItemListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> arg0, View view, int position,
                                long id) {
            @SuppressWarnings("unchecked")
            HashMap<String, Object> map = (HashMap<String, Object>) arg0
                    .getItemAtPosition(position);
            int idStr = Integer.valueOf(map.get(Constant.TABLE_NOTE_ID).toString());
//            String folder = map.get(Constant.TABLE_NOTE_FOLDER).toString();
            Intent intent = new Intent(MainActivity.this, AddNoteActivity.class);
            intent.putExtra(Constant.TABLE_NOTE_ID, idStr);
            intent.putExtra(Constant.TABLE_NOTE_FOLDER, selectedForlderName);
            intent.putExtra("selectedPosition", selectedPosition);
            intent.putExtra("intentFlag", 1);//0 编辑 ，1 预览
//           startActivity(intent);
//            finish();
            startActivityForResult(intent, Constant.MAIN_EDT_REQUESTCODE);
        }

    }

    @Override
    public void onBackPressed() {
        if (searchInput.isFocused()) {
            searchInput.setText("");
            searchInput.clearFocus();
        } else {
            super.onBackPressed();
        }

    }


    @Override
    public void onActionModeStarted(ActionMode mode) {
        actionMode = mode;
        mNotesListAdapter.setActionModeState(true);
        mMenu.setScroll(false);
        super.onActionModeStarted(mode);
    }

    @Override
    public void onActionModeFinished(ActionMode mode) {
        swim.setSwimVisibility(true);
        newNoteBtn.setVisibility(View.VISIBLE);
        mNotesListAdapter.setActionModeState(false);
        gridNotes.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
        gridNotes.setMultiChoiceModeListener(mChoiceModeListener);
        gridNotes.invalidateViews();
        searchInput.clearFocus();
        super.onActionModeFinished(mode);
    }


    class GridChoiceModeListener implements MultiChoiceModeListener {
        private View mMultiSelectActionBarView;
        public TextView mSelectedConvCount, mAllSelected, mCancle;
        private boolean allCheckMode;
        private NotesListAdapter modeAdapter;
        private int gridCounts ;
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            return false;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            allCheckMode = false;
            //HQ01690323 dpc @{
            searchbg.setClickable(true);
            searchbg.setFocusable(true);
            //@}
            if (mMultiSelectActionBarView == null) {
                mMultiSelectActionBarView = LayoutInflater.from(MainActivity.this)
                        .inflate(R.layout.multi_select_actionbar, null);
                mSelectedConvCount =
                        (TextView) mMultiSelectActionBarView.findViewById(R.id.tv_selected);
                mAllSelected = (TextView) mMultiSelectActionBarView.findViewById(R.id.tv_selectall);
                mCancle = (TextView) mMultiSelectActionBarView.findViewById(R.id.tv_cancle);
            }
            mAllSelected.setText(getString(R.string.select_all));
            modeAdapter = mNotesListAdapter;
            //HQ01808578 @{
            gridCounts = modeAdapter.getCount();
            //@}
            mAllSelected.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (allCheckMode) {
                //M:HQ01808578 unChooseAll exit ActionMode @
                        for (int position = 1; position <= gridCounts; position++) {
                            gridNotes.setItemChecked(position,false);
                        }
                //@}
                        modeAdapter.unChooseAll();
                        mAllSelected.setText(getString(R.string.select_all));
                        allCheckMode = false;
                    } else {
                        //M:HQ01808578 @{
                        for (int position = 1; position <= gridCounts; position++) {
                            Log.w("checked", "allCheckMode position=" +  position);
                            gridNotes.setItemChecked(position,true);
                        }
                        //@}
                        modeAdapter.chooseAll();
                        mAllSelected.setText(getString(R.string.unselect_all));
                        allCheckMode = true;
                    }
                    setSeletedCountShow();
                    modeAdapter.notifyDataSetChanged();
                }
            });
            mode.setCustomView(mMultiSelectActionBarView);
            bottom.setVisibility(View.VISIBLE);
            swim.setSwimVisibility(false);
            newNoteBtn.setVisibility(View.GONE);
            titleLayout.setVisibility(View.GONE);
            if (searchInput != null) {
                CommonUtil.closeKeybord(searchInput,MainActivity.this);
            }
//            CommonUtil.showCancelTextView(MainActivity.this,R.drawable.actionbar_text_color);
            return true;

        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            if (mMultiSelectActionBarView == null) {
                ViewGroup v = (ViewGroup) LayoutInflater.from(MainActivity.this)
                        .inflate(R.layout.multi_select_actionbar, null);
                mSelectedConvCount =
                        (TextView) mMultiSelectActionBarView.findViewById(R.id.tv_selected);
                mAllSelected = (TextView) mMultiSelectActionBarView.findViewById(R.id.tv_selectall);
                mCancle = (TextView) mMultiSelectActionBarView.findViewById(R.id.tv_cancle);
                mode.setCustomView(v);
            }
            return true;
        }

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position,
                                              long id, boolean checked) {

            int selectId = Integer.valueOf(modeAdapter.getListItems().get(position-1).get(Constant.TABLE_NOTE_ID).toString());
            int size = modeAdapter.getSelectItems().size();
            if (checked) {
                //add note id
                if (size == 0) {
                    modeAdapter.getSelectItems().add(selectId);
                } else {
                    for (int i = 0; i < size; i++) {
                        if (!modeAdapter.getSelectItems().contains(selectId)) {
                            modeAdapter.getSelectItems().add(selectId);
                        }
                    }
                }

            } else {
                //remove note index
                for (int i = 0; i < size; i++) {
                    if (selectId == modeAdapter.getSelectItems().get(i)) {
                        modeAdapter.getSelectItems().remove(i);
                        size = size - 1;
                    }
                }
            }
            modeAdapter.notifyDataSetChanged();
            int allSize = 0;
//HQ01599548
            if (selectedForlderName.equals(getString(R.string.allnote)) | selectedForlderName.equals("All Notes")) {//all folder notesSize
                allSize = notesSize;
            } else {// someone folder noteSize
                allSize = noteSize;
            }
            if (setSeletedCountShow() == allSize && allSize != 0) {
                allCheckMode = true;
mAllSelected.requestFocus();
                mAllSelected.setText(getString(R.string.unselect_all));
            } else {
                allCheckMode = false;
mAllSelected.requestFocus();
                mAllSelected.setText(getString(R.string.select_all));
            }
//		
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            modeAdapter.unChooseAll();
            swim.setSwimVisibility(true);
            titleLayout.setVisibility(View.VISIBLE);
            bottom.setVisibility(View.GONE);
            modeAdapter.notifyDataSetChanged();
            allCheckMode = false;
            //HQ01690323 dpc @{
            searchbg.setClickable(false);
            searchbg.setFocusable(false);
            Log.w("test", "searchHint.viisib=" + (searchHint.getVisibility() == View.VISIBLE));
            Log.w("test", "hasFocus=" + (searchInput.hasFocus()));
            if (searchHint.getVisibility() == View.VISIBLE) {
                searchInput.clearFocus();
                Log.w("test", "searchInput.hasFocus=" + (searchInput.hasFocus()));
                searchView.setFocusable(true);
                searchView.setFocusableInTouchMode(true);
                searchView.requestFocus();
                Log.w("test", "searchView.hasFocus=" + (searchView.hasFocus()));
            }
            //@}
        }

        public int setSeletedCountShow() {
            int selectedCount = modeAdapter.getChooseItemCount();
            countsSelected =selectedCount;
            mSelectedConvCount.setText(Integer.toString(selectedCount));
            return selectedCount;
        }

    }

}
