package com.spauldhaliwal.homestead;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";
    ViewPager mViewPager;
    HomesteadBoardPagerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "MainActivity onCreate: starts");
        super.onCreate(savedInstanceState);
        FloatingActionButton fab = findViewById(R.id.fab);
        Log.d(TAG, "Toolbar onCreate: MainActivity Toolbar: " + toolbar);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, newAddEditActivity.class);
                startActivity(intent);

            }
        });

        mAdapter = new HomesteadBoardPagerAdapter(getSupportFragmentManager());
        mViewPager = findViewById(R.id.content_pager);
        mViewPager.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }
}
