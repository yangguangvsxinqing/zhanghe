package com.example.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ubuntu.example.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ubuntu on 16-7-12.
 */
public class ListFragment extends Fragment{
    List<String> mList;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initListData();
    }

    private void initListData() {
        mList = new ArrayList<String>();
        for(int i=0;i<15;i++){
            mList.add("item"+i);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list,null);
        RecyclerView mListView = (RecyclerView) view.findViewById(R.id.list);
        MyAdapter adapter = new MyAdapter(getActivity(),mList);
        mListView.setAdapter(adapter);
        mListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return view;
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder>{
        private LayoutInflater mLayoutInflater;
        public MyAdapter(Context context,List<String> list){
            mLayoutInflater = LayoutInflater.from(context);
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            MyViewHolder myViewHolder = new MyViewHolder(mLayoutInflater.inflate(R.layout.item_lsit,parent,false));
            return myViewHolder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            Log.v("ListFragment","onBindViewHolder position="+position);
            holder.text.setText(mList.get(position));
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }

        @Override
        public int getItemViewType(int position) {
            return super.getItemViewType(position);
        }

        public class MyViewHolder extends RecyclerView.ViewHolder{
            public TextView text;
            public MyViewHolder(View itemView) {
                super(itemView);
                text = (TextView) itemView.findViewById(R.id.list_item);
            }
        }
    }

    public static ListFragment newInstance()
    {
        ListFragment listFragment = new ListFragment();
        return listFragment;
    }
}
