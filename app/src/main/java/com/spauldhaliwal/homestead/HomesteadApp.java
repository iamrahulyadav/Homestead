package com.spauldhaliwal.homestead;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by pauldhaliwal on 2018-03-25.
 */

public class HomesteadApp extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
