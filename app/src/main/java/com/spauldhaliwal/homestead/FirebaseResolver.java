package com.spauldhaliwal.homestead;

import android.net.wifi.hotspot2.pps.Credential;
import android.util.Log;

import com.firebase.ui.auth.data.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by pauldhaliwal on 2018-03-24.
 */

public abstract class FirebaseResolver {
    private static final String TAG = "FirebaseResolver";

    private static JobModel job;
    private static final String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    static boolean insertJob(final String name, final String description, final String creatorId, final String creatorImage, final boolean isPrivate) {

        final DatabaseReference databaseJobs = FirebaseDatabase.getInstance().
                getReference(JobsContract.ROOT_NODE);
        if (isPrivate) {
            if (name.length() > 0) {
                DatabaseReference userRef = FirebaseDatabase
                        .getInstance()
                        .getReference()
                        .child(UsersContract.ROOT_NODE)
                        .child(userUid)
                        .child(UsersContract.JOBS_NODE);

                String id = databaseJobs.push().getKey();
                job = new JobModel(id, name, description, creatorId, creatorImage, isPrivate);

                userRef.child(id).setValue(job);
                Log.d(TAG, "insertJob: inserting " + job.toString());

                return true;
            }
            return false;

        } else {
            if (name.length() > 0) {
                // Retrieve current user's node in /Users
                //
                DatabaseReference userHomesteadRef = FirebaseDatabase
                        .getInstance()
                        .getReference()
                        .child(UsersContract.ROOT_NODE)
                        .child(userUid)
                        .child(UsersContract.HOMESTEAD_ID);

                // Event listener to read the user's data
                userHomesteadRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //Retrieve user's homesteadId
                        String userHomesteadId = (String) dataSnapshot.getValue();
                        Log.d(TAG, "onDataChange: userHomesteadid is " + userHomesteadId);

                        //Reference homestead's Job node for insertion
                        DatabaseReference homesteadsJob = FirebaseDatabase
                                .getInstance()
                                .getReference(HomesteadsContract.ROOT_NODE)
                                .child(userHomesteadId)
                                .child(HomesteadsContract.JOBS_NODE);

                        String id = homesteadsJob.push().getKey();
                        job = new JobModel(id, name, description, creatorId, creatorImage, isPrivate);
                        homesteadsJob.child(id).setValue(job);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                return true;
            }
            return false;
        }
    }

    static boolean updateJob(final String uid, final String name, final String description, final boolean isPrivate) {

        final DatabaseReference databaseJobs = FirebaseDatabase.getInstance().
                getReference(JobsContract.ROOT_NODE);
        if (isPrivate) {
            if (name.length() > 0) {
                DatabaseReference userRef = FirebaseDatabase
                        .getInstance()
                        .getReference()
                        .child(UsersContract.ROOT_NODE)
                        .child(userUid)
                        .child(UsersContract.JOBS_NODE);

                job = new JobModel(uid, name, description, true);

                userRef.child(uid).child(JobsContract.NAME).setValue(job.getName());
                userRef.child(uid).child(JobsContract.DESCRIPTION).setValue(job.getDescription());
                Log.d(TAG, "updateJob: updating " + job.toString());
                return true;
            }
            return false;

        } else {
            if (name.length() > 0) {
                DatabaseReference homesteadsJob = FirebaseDatabase
                        .getInstance()
                        .getReference(HomesteadsContract.ROOT_NODE)
                        .child(CurrentUser.getHomesteadUid())
                        .child(HomesteadsContract.JOBS_NODE);

                job = new JobModel(uid, name, description, false);
                homesteadsJob.child(uid).child(JobsContract.NAME).setValue(job.getName());
                homesteadsJob.child(uid).child(JobsContract.DESCRIPTION).setValue(job.getDescription());

                return true;
            }
            return false;
        }

    }

    static void delete(final String uid, boolean isPrivate) {
        if (isPrivate) {
            DatabaseReference userRef = FirebaseDatabase
                    .getInstance()
                    .getReference()
                    .child(UsersContract.ROOT_NODE)
                    .child(userUid)
                    .child(UsersContract.JOBS_NODE);

            userRef.child(uid).setValue(null);
        } else {
            DatabaseReference homesteadsJob = FirebaseDatabase
                    .getInstance()
                    .getReference(HomesteadsContract.ROOT_NODE)
                    .child(CurrentUser.getHomesteadUid())
                    .child(HomesteadsContract.JOBS_NODE);

            homesteadsJob.child(uid).setValue(null);
        }

    }

    static boolean sendMessage(final String message, final String senderId, final String senderName, final long timeSent, final String senderProfileImage) {

        final DatabaseReference databaseMessages = FirebaseDatabase.getInstance().
                getReference(MessagesContract.ROOT_NODE);

        final DatabaseReference senderNotifications = FirebaseDatabase
                .getInstance()
                .getReference(UsersContract.ROOT_NODE)
                .child(CurrentUser.getUid())
                .child(UsersContract.NOTIFICATIONS);

        final DatabaseReference homesteadNotifications = FirebaseDatabase
                .getInstance()
                .getReference(HomesteadsContract.ROOT_NODE)
                .child(CurrentUser.getHomesteadUid())
                .child(HomesteadsContract.NOTIFICATIONS);

        if (message.length() > 0) {
            DatabaseReference messageRef = FirebaseDatabase
                    .getInstance()
                    .getReference()
                    .child(MessagesContract.ROOT_NODE)
                    .child(CurrentUser.getHomesteadUid());

            String id = databaseMessages.push().getKey();
            MessageModel messageModel = new MessageModel(message, id, senderId, senderName, timeSent, senderProfileImage);
            messageRef.child(id).setValue(messageModel);

            String notificationId = homesteadNotifications.push().getKey();
            NotificationModel notificationModel = new NotificationModel(senderName, message, notificationId, senderId);
            homesteadNotifications.child(notificationId).setValue(notificationModel);


            Log.d(TAG, "sendMessage: sending" + messageModel.toString());
            return true;
        } else {
            return false;
        }
    }

}

