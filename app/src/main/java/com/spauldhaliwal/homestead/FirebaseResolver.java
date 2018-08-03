package com.spauldhaliwal.homestead;

import android.util.Log;

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
                String sortOrder = JobsContract.STATUS_CLAIMED + "_" + id;

                job = new JobModel(id, name, description, creatorId, creatorImage, JobsContract.STATUS_CLAIMED, creatorId, isPrivate, sortOrder);

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

                        DatabaseReference homesteadNotifications = FirebaseDatabase
                                .getInstance()
                                .getReference(HomesteadsContract.ROOT_NODE)
                                .child(CurrentUser.getHomesteadUid())
                                .child(HomesteadsContract.NOTIFICATIONS);

                        String id = homesteadsJob.push().getKey();
                        String sortOrder = JobsContract.STATUS_OPEN + "_" + id;
                        job = new JobModel(id, name, description, creatorId, creatorImage, JobsContract.STATUS_OPEN, null, isPrivate, sortOrder);
                        homesteadsJob.child(id).setValue(job);

                        String notificationId = homesteadNotifications.push().getKey();
                        NotificationModel notificationModel = new NotificationModel(CurrentUser.getName(),
                                name,
                                description,
                                notificationId,
                                creatorId,
                                JobsContract.TYPE);

                        homesteadNotifications.child(notificationId).setValue(notificationModel);
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

    static boolean updateJob(final String uid, final String name, final String description, int status, final String owner, final boolean isPrivate) {

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

                job = new JobModel(uid, name, description, status, owner, true);

                userRef.child(uid).child(JobsContract.NAME).setValue(job.getName());
                userRef.child(uid).child(JobsContract.DESCRIPTION).setValue(job.getDescription());
                Log.d(TAG, "updateJob: job.getStatus: " + job.getStatus());
                userRef.child(uid).child(JobsContract.STATUS).setValue(job.getStatus());
                userRef.child(uid).child(JobsContract.OWNER).setValue(job.getOwner());

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

                homesteadsJob.child(uid).child(JobsContract.NAME).setValue(name);
                homesteadsJob.child(uid).child(JobsContract.DESCRIPTION).setValue(description);
                homesteadsJob.child(uid).child(JobsContract.STATUS).setValue(status);
                homesteadsJob.child(uid).child(JobsContract.OWNER).setValue(owner);

                if (status == JobsContract.STATUS_CLOSED) {
                    String sortOrder = JobsContract.STATUS_CLOSED + "_" + uid;
                    homesteadsJob.child(uid).child(JobsContract.SORT_ORDER).setValue(sortOrder);
                }

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
            NotificationModel notificationModel = new NotificationModel(senderName,
                    null,
                    message,
                    notificationId,
                    senderId,
                    MessagesContract.TYPE);

            homesteadNotifications.child(notificationId).setValue(notificationModel);


            Log.d(TAG, "sendMessage: sending" + messageModel.toString());
            return true;
        } else {
            return false;
        }
    }

    static boolean insertJobNote(final String jobId, final String creatorId, final String creatorImage, final String content, final boolean isPrivate) {

        final DatabaseReference databaseJobs = FirebaseDatabase.getInstance().
                getReference(JobsContract.ROOT_NODE);
        if (isPrivate) {
            if (content.length() > 0) {
                DatabaseReference noteRef = FirebaseDatabase
                        .getInstance()
                        .getReference()
                        .child(UsersContract.ROOT_NODE)
                        .child(userUid)
                        .child(UsersContract.JOBS_NODE)
                        .child(jobId)
                        .child(JobsContract.NOTES);

                String id = databaseJobs.push().getKey();

                JobNote jobNote = new JobNote(id, creatorId, creatorImage, content);

                noteRef.child(id).setValue(jobNote);
                Log.d(TAG, "insertJobNote: inserting " + jobNote.toString());

                return true;
            }
            return false;

        } else {
            if (content.length() > 0) {
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
                        DatabaseReference homesteadsJobNote = FirebaseDatabase
                                .getInstance()
                                .getReference(HomesteadsContract.ROOT_NODE)
                                .child(userHomesteadId)
                                .child(HomesteadsContract.JOBS_NODE)
                                .child(jobId)
                                .child(JobsContract.NOTES);

//                        DatabaseReference homesteadNotifications = FirebaseDatabase
//                                .getInstance()
//                                .getReference(HomesteadsContract.ROOT_NODE)
//                                .child(CurrentUser.getHomesteadUid())
//                                .child(HomesteadsContract.NOTIFICATIONS);

                        String id = homesteadsJobNote.push().getKey();
                        JobNote jobNote = new JobNote(id, creatorId, creatorImage, content);
                        homesteadsJobNote.child(id).setValue(jobNote);

//                        String notificationId = homesteadNotifications.push().getKey();
//                        NotificationModel notificationModel = new NotificationModel(CurrentUser.getName(),
//                                name,
//                                description,
//                                notificationId,
//                                creatorId,
//                                JobsContract.TYPE);
//
//                        homesteadNotifications.child(notificationId).setValue(notificationModel);
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

    static boolean createHomestead(String name) {
        if (name.length() > 0) {
            DatabaseReference homesteadsRef = FirebaseDatabase
                    .getInstance()
                    .getReference(HomesteadsContract.ROOT_NODE);

            String homesteadId = homesteadsRef.push().getKey();
            String homesteadName = name;

            HomesteadModel newHomestead = new HomesteadModel(homesteadId, name);
            homesteadsRef.child(homesteadId).setValue(newHomestead);

            DatabaseReference userRef = FirebaseDatabase
                    .getInstance()
                    .getReference()
                    .child(UsersContract.ROOT_NODE)
                    .child(userUid);

            userRef.child(UsersContract.HOMESTEAD_ID).setValue(homesteadId);
            userRef.child(UsersContract.HOMESTEAD_NAME).setValue(homesteadName);
            return true;

        } else {
            return false;
        }
    }

    static void leaveHomestead() {

        DatabaseReference userRef = FirebaseDatabase
                .getInstance()
                .getReference()
                .child(UsersContract.ROOT_NODE)
                .child(userUid);

       userRef.child(UsersContract.HOMESTEAD_ID).removeValue();

    }

}

