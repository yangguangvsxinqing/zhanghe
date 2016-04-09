package com.fineos.notes;

import android.app.ActionBar;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fineos.notes.adapter.NotesListAdapter;
import com.fineos.notes.adapter.SearchListAdapter;
import com.fineos.notes.bean.Note;
import com.fineos.notes.constant.Constant;
import com.fineos.notes.db.NoteDao;
import com.fineos.notes.util.AnimUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fineos.app.AlertDialog;


public class SearchActivity extends Activity implements OnClickListener {
    private ImageView cancle;
    private EditText search;
    private GridView gridView;
    private TextView nofind;
    private ImageView clear;
    private List<Note> notes;
    private SearchListAdapter mSearchListAdapter;
    private NotesListAdapter mNotesListAdapter;
    private GridView gridNotes;
    private GridChoiceModeListener mChoiceModeListener;
    private int noteSize;
    private LinearLayout bottom;
    private ImageView move,delete;
    private  String selectedForlderName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_note);
        initView();
        setActionBar();
    }

    private void initView() {
        gridNotes = (GridView) findViewById(R.id.gridNotes);
        nofind = (TextView) findViewById(R.id.tv_note_nofind);
        bottom = (LinearLayout) findViewById(R.id.bottom);
        move = (ImageView) findViewById(R.id.iv_move);
        delete = (ImageView) findViewById(R.id.iv_delete);


//        gridNotes.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
//        mChoiceModeListener = new GridChoiceModeListener();
//        gridNotes.setMultiChoiceModeListener(mChoiceModeListener);
        gridNotes.setOnItemClickListener(new GridOnItemListener());
        move.setOnClickListener(this);
        delete.setOnClickListener(this);
    }

    private void setActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.search_note_action, null);
        cancle = (ImageView) view.findViewById(R.id.tv_cancle);
        search = (EditText) view.findViewById(R.id.ed_search);
        clear = (ImageView) view.findViewById(R.id.iv_clear_search);

        cancle.setOnClickListener(this);
        clear.setOnClickListener(this);
        search.setHint("  "+getString(R.string.search_hint));
        search.addTextChangedListener(new TextChangedListener());
        actionBar.setCustomView(view);
        actionBar.show();
    }

    class TextChangedListener implements TextWatcher {

        @Override
        public void afterTextChanged(Editable s) {

            Log.w(Constant.TAG,"---afterTextChanged--s:"+s);
            if(s.toString().isEmpty()){
                nofind.setVisibility(View.VISIBLE);
                gridNotes.setVisibility(View.GONE);
                clear.setVisibility(View.GONE);
            }else {
                clear.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            Log.w(Constant.TAG, "--beforeTextChanged--");
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            Log.w(Constant.TAG, "---onTextChanged-s:" + s);
            List<HashMap<String, Object>> listData = new ArrayList<HashMap<String, Object>>();
            HashMap<String, Object> map = null;
            NoteDao dao = new NoteDao(SearchActivity.this);
            notes = dao.selectByKeyWorld(s.toString());
            noteSize = notes.size();
            Log.w(Constant.TAG,"notes:"+notes);
            int size = notes.size();
            if (size > 0) {
                for (int i = 0; i < size; i++) {
                    if (notes.get(i).isValid()) {
                        map = new HashMap<String, Object>();
                        map.put(Constant.TABLE_NOTE_ID, notes.get(i).getId());
                        map.put(Constant.TABLE_NOTE_FOLDER, notes.get(i)
                                .getFolder());
                        map.put(Constant.TABLE_NOTE_TITLE, notes.get(i)
                                .getTitle());
                        map.put(Constant.TABLE_NOTE_DETAIL, notes.get(i)
                                .getDetail());
                        map.put(Constant.TABLE_NOTE_BG, notes.get(i)
                                .getBackground());
                        map.put(Constant.TABLE_NOTE_PICTURE, notes.get(i)
                                .getPicture());
                        map.put(Constant.TABLE_NOTE_DATA, notes.get(i)
                                .getData());
                        listData.add(map);
                    } else {
                        Log.w(Constant.TAG, "输入数据无效");
                    }
                }
                if (listData.size() > 0) {
                    nofind.setVisibility(View.GONE);
                    gridNotes.setVisibility(View.VISIBLE);
                    mNotesListAdapter = new NotesListAdapter(SearchActivity.this, listData);
//                    mSearchListAdapter = new SearchListAdapter(SearchActivity.this, listData);
                    gridNotes.setAdapter(mNotesListAdapter);
                    mNotesListAdapter.notifyDataSetChanged();
                    gridNotes.refreshDrawableState();
                } else {
                    mSearchListAdapter.notifyDataSetChanged();
                    gridNotes.refreshDrawableState();
                    gridNotes.setVisibility(View.GONE);
                    nofind.setVisibility(View.VISIBLE);
                }
            }else{
                nofind.setVisibility(View.VISIBLE);
                gridNotes.setVisibility(View.GONE);
            }

        }
    }

    class GridOnItemListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> arg0, View view, int position,
                                long id) {
            @SuppressWarnings("unchecked")
            HashMap<String, Object> map = (HashMap<String, Object>) arg0
                    .getItemAtPosition(position);
            selectedForlderName =map.get(Constant.TABLE_NOTE_FOLDER).toString();
            int selectedPosition = 0;
            Log.w(Constant.TAG, "GridOnItemListener map:" + map);
            Log.w(Constant.TAG, "MainActivity GridOnItemListener selectedForlderName:" + selectedForlderName);
            int idStr = Integer.valueOf(map.get(Constant.TABLE_NOTE_ID).toString());
//            String folder = map.get(Constant.TABLE_NOTE_FOLDER).toString();
            Intent intent = new Intent(SearchActivity.this, AddNoteActivity.class);
            intent.putExtra(Constant.TABLE_NOTE_ID, idStr);
            intent.putExtra(Constant.TABLE_NOTE_FOLDER, selectedForlderName);
            intent.putExtra("selectedPosition",selectedPosition);
            intent.putExtra("intentFlag", 1);//0 编辑 ，1 预览
            startActivity(intent);
//            startActivityForResult(intent, Constant.MAIN_REQUESTCODE);
        }

    }

    @Override
    public void onActionModeStarted(ActionMode mode) {
        super.onActionModeStarted(mode);
    }

    @Override
    public void onActionModeFinished(ActionMode mode) {
        super.onActionModeFinished(mode);
    }

    class GridChoiceModeListener implements AbsListView.MultiChoiceModeListener {
        private View mMultiSelectActionBarView;
        public TextView mSelectedConvCount, mAllSelected, mCancle;
        private boolean allCheckMode;
        private NotesListAdapter modeAdapter;

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            Log.w(Constant.TAG, "onActionItemClicked");
            Log.w(Constant.TAG, "onActionItemClicked item:" + item);

            return false;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            Log.w(Constant.TAG, "onCreateActionMode");
            allCheckMode = false;

            if (mMultiSelectActionBarView == null) {
                mMultiSelectActionBarView = LayoutInflater.from(SearchActivity.this)
                        .inflate(R.layout.multi_select_actionbar, null);
                mSelectedConvCount =
                        (TextView) mMultiSelectActionBarView.findViewById(R.id.tv_selected);
                mAllSelected = (TextView) mMultiSelectActionBarView.findViewById(R.id.tv_selectall);
                mCancle = (TextView) mMultiSelectActionBarView.findViewById(R.id.tv_cancle);
            }
            mAllSelected.setText(getString(R.string.select_all));
            modeAdapter = mNotesListAdapter;
            mAllSelected.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (allCheckMode) {
                        modeAdapter.unChooseAll();
                        mAllSelected.setText(getString(R.string.select_all));
                        allCheckMode = false;
                    } else {
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
//            CommonUtil.showCancelTextView(MainActivity.this,R.drawable.actionbar_text_color);
            return true;

        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            Log.w(Constant.TAG, "onPrepareActionMode");
            if (mMultiSelectActionBarView == null) {
                ViewGroup v = (ViewGroup) LayoutInflater.from(SearchActivity.this)
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
            int selectId = Integer.valueOf(modeAdapter.getListItems().get(position).get(Constant.TABLE_NOTE_ID).toString());
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
            allSize = noteSize;
            Log.w(Constant.TAG, "MainActivity allSize setSeletedCountShow():" + allSize + " " + setSeletedCountShow());
            if (setSeletedCountShow() == allSize && allSize != 0) {
                allCheckMode = true;
                mAllSelected.setText(getString(R.string.unselect_all));
            } else {
                allCheckMode = false;
                mAllSelected.setText(getString(R.string.select_all));
            }
//
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            Log.w(Constant.TAG, "onDestroyActionMode");
            modeAdapter.unChooseAll();
            bottom.setVisibility(View.GONE);
            modeAdapter.notifyDataSetChanged();
            allCheckMode = false;

        }

        public int setSeletedCountShow() {
            int selectedCount = modeAdapter.getChooseItemCount();
            mSelectedConvCount.setText(Integer.toString(selectedCount));
            return selectedCount;
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_cancle:
                finish();
                AnimUtil.onBackPressedAnim(SearchActivity.this);
                break;
            case R.id.iv_clear_search:
                search.setText("");
                break;
            case  R.id.iv_move:
                ArrayList<String> idList = new ArrayList<String>();
                NotesListAdapter idSizeAdapter = mNotesListAdapter;
                for (int i = 0; i < idSizeAdapter.getSelectItems().size(); i++) {
                    String selectId = idSizeAdapter.getSelectItems().get(i).toString();
                    idList.add(selectId);
                }
                Intent intent1 = new Intent(this, MoveNoteActivity.class);
                Log.w(Constant.TAG, "MainActivity idList:" + idList);
                intent1.putStringArrayListExtra("id", idList);
                intent1.putExtra("folderName", selectedForlderName);
                startActivityForResult(intent1, Constant.MOVENOTE_REQUESTCODE);
                break;
            case R.id.iv_delete:
                operaDialog(selectedForlderName, getString(R.string.delete_note), getString(R.string.delete_note_message));
                break;
            default:
                break;
        }
    }
    private void operaDialog(final String name, final String dialog_title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
            builder.setMessage(message);
        builder.setTitle(dialog_title);
        builder.setPositiveButton(getResources().getString(R.string.done),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        NoteDao dao = new NoteDao(SearchActivity.this);

//                        dao.deleteById(selectId);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(
                        getResources().getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mNotesListAdapter.getSelectItems().clear();
                                mNotesListAdapter.unChooseAll();
                                mNotesListAdapter.setActionModeState(false);
                                gridNotes.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
                                gridNotes.setMultiChoiceModeListener(mChoiceModeListener);
//								gridViewNotesBind();
                                mNotesListAdapter.notifyDataSetChanged();
                                dialog.dismiss();

                            }
                        }).show();
//        swim.setSwimVisibility(true);
    }

}

