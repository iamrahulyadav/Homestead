package com.spauldhaliwal.homestead;

import android.app.Activity;
import android.util.Log;

public class ActivityState {
    private static final String TAG = "ActivityState";

    public static Activity mCurrentActivity = null;

    public static void setActivity (Activity activity) {
        Log.d(TAG, "ActivityState setActivity: " + activity);
        mCurrentActivity = activity;
    }

    public static void clearActivity (Activity activity) {
        Log.d(TAG, "ActivityState clearActivity: " + activity);
        if (mCurrentActivity.equals(activity)) {
            mCurrentActivity = null;
            Log.d(TAG, "ActivityState clearActivity result: " + mCurrentActivity);
        }
    }

    public static Activity getCurrentActivity() { return mCurrentActivity; }
}
