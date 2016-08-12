package com.example.ui;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ubuntu.example.R;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by ubuntu on 16-7-11.
 */
public class TabFragment extends Fragment {


    public static final String TITLE = "title";
    private String mTitle = "Defaut Value";
    private HashMap<String,String> map;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            mTitle = getArguments().getString(TITLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        TextView tv = new TextView(getActivity());
        tv.setTextSize(60);
        Random r = new Random();
        tv.setBackgroundColor(Color.argb(r.nextInt(120), r.nextInt(255),
                r.nextInt(255), r.nextInt(255)));
        tv.setText(mTitle);
        tv.setGravity(Gravity.CENTER);
        return tv;

    }

    public static TabFragment newInstance(String title)
    {
        TabFragment tabFragment = new TabFragment();
        Bundle bundle = new Bundle();
        bundle.putString(TITLE, title);
        tabFragment.setArguments(bundle);
        return tabFragment;
    }
}
