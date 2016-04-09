package com.fineos.notes.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fineos.notes.R;
import com.fineos.notes.constant.Constant;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class SearchListAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater inflater;
    public ArrayList<String> selectItems = new ArrayList<String>();// 是否选择
    private List<HashMap<String, Object>> listItems;
    private String type;
    private boolean actionModeStarted;
    private int[] itemState;
    private List<Integer> selecedtId = new ArrayList<Integer>();
    private Integer[] imageIDs = { R.drawable.bg_letter_preview1_normal,
            R.drawable.bg_letter_preview2_normal, R.drawable.bg_letter_preview3_normal,
            R.drawable.bg_letter_preview4_normal, R.drawable.bg_letter_preview5_normal};
    private int bg =-1;
    public SearchListAdapter(Context context,
                             List<HashMap<String, Object>> listItems) {
        this.context = context;
        this.listItems = listItems;
        inflater = LayoutInflater.from(context);

    }

    public List<String> getSelectItems() {
        return selectItems;
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

            holder.bg_ly = (LinearLayout) (convertView).findViewById(R.id.ly_layout);
            holder.choose = (ImageView) (convertView)
                    .findViewById(R.id.note_choose);
            holder.detail = (TextView) (convertView)
                    .findViewById(R.id.et_detail);
//            holder.day = (TextView) (convertView).findViewById(R.id.tv_day);
//            holder.month = (TextView) (convertView).findViewById(R.id.tv_month);
//            holder.year = (TextView) (convertView).findViewById(R.id.tv_year);
            // 将设置好的布局保存到缓存中，并将其设置在Tag里，以便后面方便取出Tag
            convertView.setTag(holder);
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
        if (bg != -1) {
            holder.bg_ly.setBackgroundResource(imageIDs[bg]);
        }else{
            holder.bg_ly.setBackgroundResource(imageIDs[0]);
            Log.w(Constant.TAG,"---setbackground--");
        }
        switch (bg) {
            case 0:
                if (title != "") {
                    holder.title.setText(title);
                    holder.title.setTextColor(context.getResources().getColor(R.color.bg_letter_0_title));
                }
                if (detail != "") {
                    holder.detail.setText(detail);
                    holder.detail.setTextColor(context.getResources().getColor(R.color.bg_letter_0_detail));
                }
//			holder.devide.setBackgroundColor(context.getResources().getColor(R.color.bg_letter_1_devide));
                break;
            case 1:
                if (title != "") {
                    holder.title.setText(title);
                    holder.title.setTextColor(context.getResources().getColor(R.color.bg_letter_1_title));
                }
                if (detail != "") {
                    holder.detail.setText(detail);
                    holder.detail.setTextColor(context.getResources().getColor(R.color.bg_letter_1_detail));
                }
//			holder.devide.setBackgroundColor(context.getResources().getColor(R.color.bg_letter_2_devide));
                break;
            case 2:
                if (title != "") {
                    holder.title.setText(title);
                    holder.title.setTextColor(context.getResources().getColor(R.color.bg_letter_2_title));
                }
                if (detail != "") {
                    holder.detail.setText(detail);
                    holder.detail.setTextColor(context.getResources().getColor(R.color.bg_letter_2_detail));
                }
//			holder.devide.setBackgroundColor(context.getResources().getColor(R.color.bg_letter_3_devide));
                break;
            case 3:
                if (title != "") {
                    holder.title.setText(title);
                    holder.title.setTextColor(context.getResources().getColor(R.color.bg_letter_3_title));
                }
                if (detail != "") {
                    holder.detail.setText(detail);
                    holder.detail.setTextColor(context.getResources().getColor(R.color.bg_letter_3_detail));
                }
//			holder.devide.setBackgroundColor(context.getResources().getColor(R.color.bg_letter_4_devide));
                break;
            case 4:
                if (title != "") {
                    holder.title.setText(title);
                    holder.title.setTextColor(context.getResources().getColor(R.color.bg_letter_4_title));
                }
                if (detail != "") {
                    holder.detail.setText(detail);
                    holder.detail.setTextColor(context.getResources().getColor(R.color.bg_letter_4_detail));
                }
//			holder.devide.setBackgroundColor(context.getResources().getColor(R.color.bg_letter_5_devide));
                break;
            default:
                if (title != "") {
                    holder.title.setText(title);
                }
                if (detail != "") {
                    holder.detail.setText(detail);
                }
                break;
        }
//		if (title != "") {
//			holder.title.setText(title);
//		}
//		if (detail != "") {
//			holder.detail.setText(detail);
//		}
        if (date != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(date);

            holder.day.setText(String.valueOf(calendar
                    .get(Calendar.DAY_OF_MONTH)));
            holder.month.setText(String.valueOf(calendar.get(Calendar.MONTH))
                    + "月");
            holder.year.setText(String.valueOf(calendar.get(Calendar.YEAR)));
        }

        if (selectItems.size() > 0) {
            if (selectItems.contains(listItems.get(position)
                    .get(Constant.TABLE_NOTE_ID).toString())) {
                holder.choose.setVisibility(View.VISIBLE);
            } else {
                holder.choose.setVisibility(View.GONE);
            }
        } else {
            holder.choose.setVisibility(View.GONE);
        }

        return convertView;
    }

    public void setActionModeState(boolean flag) {
        actionModeStarted = flag;
    }

    public boolean isActionModeStart() {
        return actionModeStarted;
    }

    public int getChooseItemCount() {

        return selectItems.size();
    }

    public void chooseAll() {
        unChooseAll();
        int size = listItems.size();
        for (int i = 0; i < size; i++) {
            String str = listItems.get(i).get(Constant.TABLE_NOTE_ID)
                    .toString();
            selectItems.add(str);
        }
    }

    public void unChooseAll() {
        int size = listItems.size();
        for (int i = 0; i < size; i++) {
            String str = listItems.get(i).get(Constant.TABLE_NOTE_ID)
                    .toString();
            selectItems.remove(str);
        }
    }

    class ViewHolder {
        LinearLayout bg_ly;
        ImageView  choose;
        TextView title, detail;
        TextView day, month, year;
        GridView picture;
        View devide;
    }
}
