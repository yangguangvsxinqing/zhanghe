package com.fineos.notes;

import android.app.ActionBar;
import android.app.Activity;
//import android.app.AlertDialog;
import fineos.app.AlertDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.fineos.notes.adapter.MoveListAdapter;
import com.fineos.notes.bean.Folder;
import com.fineos.notes.constant.Constant;
import com.fineos.notes.db.FolderDao;
import com.fineos.notes.db.NoteDao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ubuntu on 15-6-30.
 */
public class MoveNoteActivity extends Activity {
    private ListView listFolder;
    private List<HashMap<String, Object>> listData;
    private List<Folder> folders;
    private String folderName;
    private int length = 0;
    private String addStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movelayout);
        initView();
        setActionBar();
    }

    private void initView() {
        final ArrayList<Integer> idList = getIntent().getIntegerArrayListExtra("id");
        listFolder = (ListView) findViewById(R.id.list_notefolder);

        listData = initFolder();
        MoveListAdapter moveListAdapter = new MoveListAdapter(MoveNoteActivity.this, listData);
        Log.w(Constant.TAG, "MoveNoteActivity listData:" + listData);
        listFolder.setAdapter(moveListAdapter);
        listFolder.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.w(Constant.TAG, "MoveActivity position:" + position);
                Log.w(Constant.TAG, "MoveActivity listData.get(position).get(\"folderName\"):" + listData.get(position).get("folderName"));
                folderName = (String) listData.get(position).get("folderName");
                if (folderName.equals(getString(R.string.addfolder))) {
                    addFolder(listData, idList);
                } else {
                    showMoveDialog(idList, folderName, position);
                }
            }
        });
    }

    private void setActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.move_actionbar, null);
        actionBar.setCustomView(view);

        view.findViewById(R.id.tv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void showMoveDialog(final ArrayList<Integer> idList, final String newName,final int position) {
        Log.w(Constant.TAG, "MoveNoteActivity idList:" + idList);
        TextView text = new TextView(MoveNoteActivity.this);
        text.setHighlightColor(Color.RED);
        text.setTextSize(60);
        text.setText(newName);
        new AlertDialog.Builder(MoveNoteActivity.this)
                .setTitle(getString(R.string.move_note_title))
                .setMessage(getString(R.string.move_note_message) + " " + text.getText() + "?")
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NoteDao dao = new NoteDao(MoveNoteActivity.this);
                        Log.w(Constant.TAG, "MoveNoteActivity--oldName--newName:" + idList.toString() + "--" + newName);
                        for (int i = 0; i < idList.size(); i++) {
                            int id = idList.get(i);
                            Log.w(Constant.TAG, "MoveNoteActivity id:" + id);
                            dao.updateByFolder(id, newName);
                        }

                        Log.w(Constant.TAG, "MoveNoteActivity folderName,newName,position:" + folderName + "," + newName + "," + position);
//                        Intent intent = new Intent(MoveNoteActivity.this, MainActivity.class);
                        Intent intent = new Intent();
                        intent.putExtra(Constant.TABLE_NOTE_FOLDER, newName);
                        intent.putExtra("selectedPosition", position);
//                startActivity(intent);
                        setResult(Constant.MOVENOTE_RESUTLCODE, intent);
                        finish();

                    }
                })
                .setNegativeButton(getString(R.string.cancel), null).show();

    }

    private List<HashMap<String, Object>> initFolder() {
        listData = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> map = null;
        FolderDao dao = new FolderDao(this);
        folders = dao.selectAll();
        if (!(folders.size() == 0)) {
            for (int i = 0; i < folders.size(); i++) {
                map = new HashMap<String, Object>();
                Log.w(Constant.TAG, "folders.get(i).getFolder():" + folders.get(i).getFolder());
                if (folders.get(i).getFolder()
                        .equals(Constant.All_NOTE)) {
                    map.put("folderName", getString(R.string.allnote));
                    listData.add(map);
                } else if (folders.get(i).getFolder()
                        .equals(Constant.CALL_RECORDING)) {
//                    map.put("icon", R.drawable.ic_callrecords);
                    map.put("folderName", getString(R.string.allrecode));
                    listData.add(map);
                } else if (folders.get(i).getFolder()
                        .equals(Constant.ADD_FOLDER)) {
                    map.put("folderName", getString(R.string.addfolder));
                    listData.add(map);
                } else {
                    map.put("folderName", folders.get(i).getFolder());
                    listData.add(listData.size() - 1, map);
                }
            }
        }
        return listData;
    }

    /**
     * 新建文件夹
     *
     * @param list
     * @param
     */
    private void addFolder(final List<HashMap<String, Object>> list, final ArrayList<Integer> idList) {
        final HashMap<String, Object> map = new HashMap<String, Object>();
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.addfolder_layout, null);
        EditText editText = (EditText) view.findViewById(R.id.et_addfolder);
        final TextView textView = (TextView) view.findViewById(R.id.tv_addfolder);
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
                addStr = s.toString();
                length = addStr.length();
                Log.w(Constant.TAG, "MainActivity addStr,length:" + addStr + "," + length);
                if (length > 16) {
                    textView.setVisibility(View.VISIBLE);
                    textView.setText(getString(R.string.addfolder_outmax_warn));
                } else {
                    textView.setVisibility(View.GONE);
                }
            }
        });
       final AlertDialog alertDialog = new AlertDialog.Builder(MoveNoteActivity.this)
                .setTitle(getString(R.string.addfolder))
                .setView(view)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.ok), null)
                .setNegativeButton(getString(R.string.cancel),null)
                .show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isAddfolder = true;
                if (length == 0) {
                    Log.w(Constant.TAG, "MainActivity length1:" + length);
                    textView.setVisibility(View.VISIBLE);
                    textView.setText(getString(R.string.addfolder_empt_warn));
                } else if (length > 16) {
                    Log.w(Constant.TAG, "MainActivity length2:" + length);
                    textView.setVisibility(View.VISIBLE);
                    textView.setText(getString(R.string.addfolder_outmax_warn));
                } else {
                    for (HashMap<String, Object> m : list) {
                        for (Map.Entry<String, Object> enty : m.entrySet()) {
                            String value = enty.getValue().toString();
                            Log.w(Constant.TAG, "MainActivity value:" + value);
                            Log.w(Constant.TAG, "MainActivity addStr:" + addStr);
                            if (value.equals(addStr)) {
                                isAddfolder = false;
                            }
                        }
                    }
                    if (isAddfolder) {
                        NoteDao noteDao = new NoteDao(MoveNoteActivity.this);
                        for (int i = 0; i < idList.size(); i++) {
                            int id = idList.get(i);
                            noteDao.updateByFolder(id, addStr);
                        }
                        //add new folderName to db
                        map.put("folderName", addStr);
                        list.add(list.size() - 1, map);
                        FolderDao folderDao = new FolderDao(
                                MoveNoteActivity.this);
                        Folder folder = new Folder();
                        folder.setFolder(addStr);
                        folderDao.add(folder);

                        Intent intent = new Intent();
                        intent.putExtra(Constant.TABLE_NOTE_FOLDER, addStr);
                        intent.putExtra("selectedPosition", list.size() - 2);
                        setResult(Constant.MOVENOTE_RESUTLCODE,intent);
//                        startActivity(intent);
                        finish();
                        alertDialog.dismiss();
                    } else {
                        textView.setVisibility(View.VISIBLE);
                        textView.setText(getString(R.string.addfolder_samename_warn));
                    }

                }
            }
        });
    }

}
