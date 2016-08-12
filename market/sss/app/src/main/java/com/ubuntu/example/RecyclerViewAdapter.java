package com.ubuntu.example;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by ubuntu on 16-7-6.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder>{

    private List<WindowData> mList;
    private LayoutInflater mInflater;
    private View.OnClickListener mOnClickListener;
    private boolean animFlag = false;
    private int actionBarHeight;
    private Context mContext;
    public RecyclerViewAdapter(Context context,List<WindowData> mList){
        this.mList = mList;
//        actionBarHeight = size;
        Log.v("","sss RecyclerViewAdapter mList ="+mList.size());
        mInflater = LayoutInflater.from(context);
        mContext = context;
        mOnClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                int tmp[] = new int[2];
                v.getLocationInWindow(tmp);
                ((MainActivity)mContext).imgAnim(v.getMeasuredWidth(),v.getMeasuredHeight(),tmp[0],tmp[1],(String)v.getTag(),v.getBackground());
            }
        };
    }

    @Override
    public int getItemCount() {
        // TODO Auto-generated method stub
        return mList.size();
    }

    @Override
    public void onBindViewHolder(MyViewHolder viewHolder, int postion) {
        // TODO Auto-generated method stub
        WindowData tmp = mList.get(postion);
        if(tmp.getWinType() == 0){
            viewHolder.nameView.setVisibility(View.VISIBLE);
            viewHolder.title1View.setVisibility(View.GONE);
            viewHolder.title2View.setVisibility(View.GONE);
            viewHolder.nameView.setText(tmp.getWinName());
            viewHolder.nameView.setTag(tmp.getWinName());
        }else{
            viewHolder.nameView.setVisibility(View.GONE);
            viewHolder.title1View.setVisibility(View.VISIBLE);
            viewHolder.title2View.setVisibility(View.VISIBLE);
            viewHolder.title1View.setText(tmp.getWinTitle1());
            viewHolder.title2View.setText(tmp.getWinTitle2());
            viewHolder.title1View.setTag(tmp.getWinTitle1());
            viewHolder.title2View.setTag(tmp.getWinTitle2());
        }
        if(tmp.getWinColor() == 0){
            viewHolder.nameView.setBackgroundResource(R.drawable.color1);
            viewHolder.title1View.setBackgroundResource(R.drawable.color1);
            viewHolder.title2View.setBackgroundResource(R.drawable.color1);
        }else if(tmp.getWinColor() == 1){
            viewHolder.nameView.setBackgroundResource(R.drawable.color2);
            viewHolder.title1View.setBackgroundResource(R.drawable.color2);
            viewHolder.title2View.setBackgroundResource(R.drawable.color2);
        }else{
            viewHolder.nameView.setBackgroundResource(R.drawable.color3);
            viewHolder.title1View.setBackgroundResource(R.drawable.color3);
            viewHolder.title2View.setBackgroundResource(R.drawable.color3);
        }
        viewHolder.nameView.setOnClickListener(mOnClickListener);
        viewHolder.title1View.setOnClickListener(mOnClickListener);
        viewHolder.title2View.setOnClickListener(mOnClickListener);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // TODO Auto-generated method stub
        MyViewHolder holder = null;
        //	if(viewType == 0){
        holder = new MyViewHolder(mInflater.inflate(
                R.layout.window_item1, parent, false));
        //	}

        return holder;
    }

    @Override
    public int getItemViewType(int position) {
        // TODO Auto-generated method stub
        return mList.get(position).getWinType();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView nameView;
        public TextView title1View;
        public TextView title2View;

        public MyViewHolder(View view) {
            super(view);
            // TODO Auto-generated constructor stub
            nameView = (TextView)(view.findViewById(R.id.name));
            title1View = (TextView)(view.findViewById(R.id.title1));
            title2View = (TextView)(view.findViewById(R.id.title2));

        }
    }

}
