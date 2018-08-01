package com.spauldhaliwal.homestead;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by pauldhaliwal on 2018-03-28.
 */

public class CurrentUser {

    private static String uid;
    private static String tokenId;
    private static String name;
    private static String email;
    private static String profileImage;
    private static String homesteadUid;
    private static String jobs;

    private static UserModel userModel;

    private static FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private static DatabaseReference databaseReference;

    public CurrentUser() {
    }

    public static String getUid() {
        return uid;
    }

    public static String getTokenId() {
        return tokenId;
    }

    public static String getName() {
        return name;
    }

    public static String getEmail() {
        return email;
    }

    public static String getProfileImage() {
        return profileImage;
    }

    public static String getHomesteadUid() {
        return homesteadUid;
    }

    public static String isJobs() {
        return jobs;
    }

    public static void buildUser(final OnGetDataListener onGetDataListener) {
        databaseReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child(UsersContract.ROOT_NODE)
                .child(user.getUid());

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            private static final String TAG = "CurrentUser";

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                CurrentUser.uid = (String) dataSnapshot.child(UsersContract.UID).getValue();
                CurrentUser.tokenId = (String) dataSnapshot.child(UsersContract.TOKEN_ID).getValue();
                CurrentUser.name = (String) dataSnapshot.child(UsersContract.NAME).getValue();
                CurrentUser.email = (String) dataSnapshot.child(UsersContract.EMAIL).getValue();
                CurrentUser.profileImage = (String) dataSnapshot.child(UsersContract.PROFILE_IMAGE).getValue();
                CurrentUser.homesteadUid = (String) dataSnapshot.child(UsersContract.HOMESTEAD_ID).getValue();

                onGetDataListener.onSuccess();


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: read error: " + databaseError);
            }
        });
    }



    public interface OnGetDataListener {
        //this is for callbacks
        void onSuccess();
    }





}
