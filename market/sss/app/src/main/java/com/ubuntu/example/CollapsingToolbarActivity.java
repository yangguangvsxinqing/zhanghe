package com.ubuntu.example;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;

/**
 * Created by ubuntu on 16-7-11.
 */
public class CollapsingToolbarActivity extends AppCompatActivity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acticity_collapsing);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("CollapsingToolbar");
        mToolbar.setTitleTextColor(0xffffffff);
        mToolbar.setNavigationIcon(R.drawable.ic_ab_back_holo_dark_am_normal);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        FloatingActionButton fab;
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CollapsingToolbarActivity.this,CustomTextActivity.class);
                startActivity(intent);
            }
        });

        CardView test1 = (CardView) findViewById(R.id.test1);
        test1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CollapsingToolbarActivity.this,NestedTestActivity.class);
                startActivity(intent);
            }
        });
    }


}
