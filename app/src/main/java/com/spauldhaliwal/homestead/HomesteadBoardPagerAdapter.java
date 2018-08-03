package com.spauldhaliwal.homestead;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

/**
 * Created by pauldhaliwal on 2018-03-12.
 */

public class HomesteadBoardPagerAdapter extends FragmentPagerAdapter {
    private static final String TAG = "HomesteadBoardPagerAdapter";

    public HomesteadBoardPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Log.d(TAG, "getItem: starts");
        switch (position) {
            case 0:
                return new HomeBoardFragment();
            case 1:
                return new PersonalBoardFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}
