package com.fineos.notes.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fineos.notes.R;
import com.fineos.notes.bean.NotePicture;
import com.fineos.notes.constant.Constant;
import com.fineos.notes.db.PictureDao;
import com.fineos.notes.util.ImageTools;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class NotesListAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater inflater;
    public ArrayList<Integer> selectItems = new ArrayList<Integer>();// 是否选择
    private List<HashMap<String, Object>> listItems;
    private String type;
    private boolean actionModeStarted;
    private int[] itemState;
    private ActionMode actionMode;

    private List<Integer> selecedtId = new ArrayList<Integer>();
    private Integer[] imageIDs = {R.drawable.bg_letter_preview1,
            R.drawable.bg_letter_preview2, R.drawable.bg_letter_preview3,
            R.drawable.bg_letter_preview4, R.drawable.bg_letter_preview5};
    private int bg = -1;
    private View view;

    public NotesListAdapter(Context context,
                            List<HashMap<String, Object>> listItems) {
        Log.w(Constant.TAG, "NotesListAdapter listItems:" + listItems);
        this.context = context;
        this.listItems = listItems;
        inflater = LayoutInflater.from(context);

    }

    public List<Integer> getSelectItems() {
        return selectItems;
    }

    public void clearListItems() {
        listItems.clear();
    }

    public List<HashMap<String, Object>> getListItems() {
        return listItems;
    }

    @Override
    public int getCount() {
        return listItems.size();
    }

    @Override
    public Object getItem(int position) {
        return listItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.main_grid_item, null);
            // 获取控件
            holder.itemLayout = (RelativeLayout) convertView.findViewById(R.id.ly_layout);
//			holder.bg = (ImageView) (convertView).findViewById(R.id.iv_notebg);
            holder.choose = (ImageView) (convertView)
                    .findViewById(R.id.note_choose);
            holder.data = (TextView) (convertView).findViewById(R.id.tv_data);
            holder.detail = (TextView) (convertView)
                    .findViewById(R.id.et_detail);
            holder.picture_icon = (ImageView) convertView.findViewById(R.id.iv_picture_icon);
            // 将设置好的布局保存到缓存中，并将其设置在Tag里，以便后面方便取出Tag
            convertView.setTag(holder);
            holder.itemLayout.setTag(1);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        bg = (Integer) listItems.get(position).get(Constant.TABLE_NOTE_BG);
        String title = String.valueOf(listItems.get(position).get(
                Constant.TABLE_NOTE_TITLE));
        String detail = String.valueOf(listItems.get(position).get(
                Constant.TABLE_NOTE_DETAIL));
        Long date = (Long) listItems.get(position)
                .get(Constant.TABLE_NOTE_DATA);
        if (date == -2) {
            holder.itemLayout.setVisibility(View.GONE);
        } else {
            holder.itemLayout.setBackgroundResource(imageIDs[bg]);
            setTextDetail(holder, detail);
            if (date != null) {
                SimpleDateFormat formatter = new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm");
                Date dates = new Date(date);
                holder.data.setText(formatter.format(dates));
            }
            if (selectItems.size() > 0) {
                int noteItenm = Integer.valueOf(listItems.get(position)
                        .get(Constant.TABLE_NOTE_ID).toString());
                if (selectItems.contains(noteItenm)) {
                    holder.choose.setVisibility(View.VISIBLE);
                } else {
                    holder.choose.setVisibility(View.GONE);
                }
            } else {
                holder.choose.setVisibility(View.GONE);
            }

            String idStr = listItems.get(position).get(Constant.TABLE_NOTE_ID).toString();
            if (idStr != null) {
                int noteId = Integer.valueOf(idStr);
//tplink 反馈，有图片时候列表文字显示不是最上面的，而死图片下面的文字 @{
//                showPictureIcon(noteId,holder);
                showPictureIcon(noteId, holder,detail);
            //@}
//                setDynamicPicture(noteId, holder.dynamic_picture, holder.detail);
            }
        }

        return convertView;
    }

    private void setTextDetail(ViewHolder holder, String detail) {
        switch (bg) {
            case 0:
                if (!TextUtils.isEmpty(detail)) {
                    holder.detail.setText(detail);
                    holder.detail.setTextColor(context.getResources().getColor(R.color.bg_letter_0_detail));
                } else {
                    holder.detail.setText(R.string.tip_nodetail);
                    holder.detail.setTextColor(context.getResources().getColor(R.color.hintColor));
                }
                break;
            case 1:
                if (!TextUtils.isEmpty(detail)) {
                    holder.detail.setText(detail);
                    holder.detail.setTextColor(context.getResources().getColor(R.color.bg_letter_1_detail));
                } else {
                    holder.detail.setText(R.string.tip_nodetail);
                    holder.detail.setTextColor(context.getResources().getColor(R.color.hintColor));

                }
                break;
            case 2:
                if (!TextUtils.isEmpty(detail)) {
                    holder.detail.setText(detail);
                    holder.detail.setTextColor(context.getResources().getColor(R.color.bg_letter_2_detail));
                } else {
                    holder.detail.setText(R.string.tip_nodetail);
                    holder.detail.setTextColor(context.getResources().getColor(R.color.hintColor));

                }
                break;
            case 3:
                if (!TextUtils.isEmpty(detail)) {
                    holder.detail.setText(detail);
                    holder.detail.setTextColor(context.getResources().getColor(R.color.bg_letter_3_detail));
                } else {
                    holder.detail.setText(R.string.tip_nodetail);
                    holder.detail.setTextColor(context.getResources().getColor(R.color.hintColor));

                }
                break;
            case 4:
                if (!TextUtils.isEmpty(detail)) {
                    holder.detail.setText(detail);
                    holder.detail.setTextColor(context.getResources().getColor(R.color.bg_letter_4_detail));
                } else {
                    holder.detail.setText(R.string.tip_nodetail);
                    holder.detail.setTextColor(context.getResources().getColor(R.color.hintColor));
                }
                break;
        }
    }

    private void showPictureIcon(int noteId,ViewHolder holder,String detail){
        PictureDao pictureDao = new PictureDao(context);
        List<NotePicture> notePictures;
        notePictures = pictureDao.selectPictureByNoteId(noteId);
        Log.w(Constant.TAG, "NotesListAdapter notePictures:" + notePictures);
        boolean isHavePicture = false;
        //HQ01784660
        boolean isHaveContent = false;
        int size = notePictures.size();
        Log.w(Constant.TAG, "NotesListAdapter size:" + size);
        for (int i = 0;i <size;i++){
            String path = notePictures.get(i).getImagePath();
            if (!TextUtils.isEmpty(path) && isFileExist(path)) {
                isHavePicture = true;
            }

        }
//tplink
        if (TextUtils.isEmpty(detail)) {
            for (int i = 0; i < size; i++) {
                String content = notePictures.get(i).getContent();
                if (!TextUtils.isEmpty(content)) {
                    setTextDetail(holder, content);
                    isHaveContent = true;
                    break;
                }
            }
        }
        if (isHavePicture) {
            holder.picture_icon.setVisibility(View.VISIBLE);
        } else {
            //HQ01784660 @{
            Log.d("isFileExist", "isHaveContent="+isHaveContent);
            Log.d("isFileExist", "holder.detail.getText()="+holder.detail.getText());
            Log.d("isFileExist", "holder.detail.getText()="+holder.detail.getText());
            if (!isHaveContent && holder.detail.getText().equals(context.getString(R.string.tip_nodetail))){
                holder.itemLayout.setVisibility(View.GONE);
            }else {
                holder.itemLayout.setVisibility(View.VISIBLE);
            }
            //@}
//          holder.picture_icon.setVisibility(View.GONE);
        }
    }
 //HQ01784660 @{
    private boolean isFileExist(String path){
        File file = new File(path);
        Log.d("isFileExist", "path="+path);
        Log.d("isFileExist", "isFileExist="+file.exists());
        return file.exists();
    }
//@}

    private void setDynamicPicture(int noteId, final ImageView imageView,TextView detail) {
        PictureDao pictureDao = new PictureDao(context);
        List<NotePicture> notePictures;
        notePictures = pictureDao.selectPictureByNoteId(noteId);
        int size = notePictures.size();
        if (size > 0) {
            imageView.setVisibility(View.VISIBLE);
            detail.setVisibility(View.GONE);
            String path = notePictures.get(0).getImagePath();
            if (!TextUtils.isEmpty(path)) {//if picture exsist don't show content
                String uri = "file://"+path;
                ImageLoader imageLoader = ImageLoader.getInstance();
                imageLoader.displayImage(uri, imageView, null, new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String s, View view) {

                    }

                    @Override
                    public void onLoadingFailed(String s, View view, FailReason failReason) {

                    }

                    @Override
                    public void onLoadingComplete(String s, View view, Bitmap bitmap) {

                        int width = bitmap.getWidth();
                 int height = bitmap.getHeight();
                if (width > 600) {
                    width = 600;
                }
                if (height > 300) {
                    height = 300;
                }
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
                        imageView.setLayoutParams(params);
                        params.setMargins(32,16,32,16);
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    }

                    @Override
                    public void onLoadingCancelled(String s, View view) {

                    }
                });


//               Bitmap smalBitmap = ImageTools.getZoomImage(path, Constant.PICTURE_WIDTH, Constant.PICTURE_HEIGHT);
//
//                int width = smalBitmap.getWidth();
//                int height = smalBitmap.getHeight();
//                if (width > 600) {
//                    width = 600;
//                }
//                if (height > 300) {
//                    height = 300;
//                }
//                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
//                params.setMargins(32,16,32,16);
//                imageView.setLayoutParams(params);
//                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//                BitmapDrawable tmp = new BitmapDrawable(smalBitmap);
//                imageView.setImageDrawable(tmp);
            }
        } else {
            detail.setVisibility(View.VISIBLE);
        }


    }


    private void setPicture(int noteId, LinearLayout noteLayout, TextView detail) {

        PictureDao pictureDao = new PictureDao(context);
        List<NotePicture> notePictures;
        notePictures = pictureDao.selectPictureByNoteId(noteId);
        Log.w(Constant.TAG, "NotesListAdapter noteId,notePictures:" + noteId + "," + notePictures);

        int size = notePictures.size();
        if (size > 0) {//if picture is t exist  don't show content
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(R.layout.dynamic_note_view, null);
            noteLayout.clearChildFocus(view);
            ImageView imageView = (ImageView) view.findViewById(R.id.iv_show_insertImage);
            EditText dynamic_editText = (EditText) view.findViewById(R.id.et_show_inertContext);

            detail.setVisibility(View.GONE);
            dynamic_editText.setVisibility(View.GONE);

            String path = notePictures.get(0).getImagePath();
            if (!TextUtils.isEmpty(path)) {//if picture exsist don't show content

                Bitmap smalBitmap = ImageTools.getZoomImage(path, 636, 1120);
                int width = smalBitmap.getWidth();
                int height = smalBitmap.getHeight();
                if (width > 636) {
                    width = 636;
                }
                if (height > 300) {
                    height = 300;
                }
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
                imageView.setLayoutParams(params);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                noteLayout.addView(view);

                BitmapDrawable tmp = new BitmapDrawable(smalBitmap);
                imageView.setImageDrawable(tmp);
            } else {//if picture is not exsist show content
                String content = notePictures.get(0).getContent();
                if (content != null) {
                    dynamic_editText.setText(content);
                } else {
                    dynamic_editText.setVisibility(View.GONE);
                }
            }
        }
    }

    public void setActionModeState(boolean flag) {
        actionModeStarted = flag;
    }


    public boolean isActionModeStart() {
        return actionModeStarted;
    }

    public int getChooseItemCount() {
        Log.w(Constant.TAG, "NotesListAdapter getChooseItemCount:" + selectItems.size());
        Log.w(Constant.TAG, "NotesListAdapter selectItems:" + selectItems);
        return selectItems.size();
    }

    public void chooseAll() {
        int size = listItems.size();
        Log.w(Constant.TAG, "NotesListAdapter chooseAll() listItems:" + listItems);
        selectItems.clear();
        for (int i = 0; i < size; i++) {
            Integer str = Integer.valueOf(listItems.get(i).get(Constant.TABLE_NOTE_ID).toString());
            selectItems.add(str);
        }
    }

    public void unChooseAll() {
        int size = listItems.size();
        Log.w(Constant.TAG, "unChooseAll size:" + size);
        Log.w(Constant.TAG, "unChooseAll  clear selectItems:" + selectItems);
        selectItems.clear();
        Log.w(Constant.TAG, "unChooseAll clear  selectItems:" + selectItems);
    }

    class ViewHolder {
        RelativeLayout itemLayout;
        ImageView choose,picture_icon;
        TextView detail;
        TextView data;
    }
}
