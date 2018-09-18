package com.spauldhaliwal.homestead.repositories.impl;

import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.spauldhaliwal.homestead.HomesteadsContract;
import com.spauldhaliwal.homestead.JobModel;
import com.spauldhaliwal.homestead.JobsContract;
import com.spauldhaliwal.homestead.repositories.HomeboardTasksRepository;

import java.util.ArrayList;
import java.util.List;

public class DatabaseTasksRepository implements HomeboardTasksRepository {
    private static final String TAG = "DatabaseTasksRepository";

    String homesteadId;
    String homesteadName;
    Query query;
    List<JobModel> taskList;
    ChildEventListener childEventListener;

    public DatabaseTasksRepository(String homesteadId, String homesteadName) {
        this.homesteadId = homesteadId;
        this.homesteadName = homesteadName;
        taskList = new ArrayList<JobModel>();
    }

    @Override
    public List<JobModel> getTasks() {

        query = FirebaseDatabase.getInstance()
                .getReference()
                .child(HomesteadsContract.ROOT_NODE)
                .child(homesteadId)
                .child(HomesteadsContract.JOBS_NODE)
                .orderByChild(JobsContract.SORT_ORDER);

        childEventListener = query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                JobModel job = dataSnapshot.getValue(JobModel.class);

                taskList.add(job);
                Log.d(TAG, "onChildAdded: " + taskList.toString());

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

//        String creatorImage = "https://lh3.googleusercontent.com/-nP6D8dTSSIg/AAAAAAAAAAI/AAAAAAAAMxk/kGAPCYDXu_4/s96-c/photo.jpg";
//        JobModel job1 = new JobModel("1", "first", "description 1", 0,creatorImage, false);
//        JobModel job2 = new JobModel("2", "second", "description 2", 0,creatorImage, false);
//        JobModel job3 = new JobModel("3", "third", "description 3", 0,creatorImage, false);
//
//        taskList.add(job1);
//        taskList.add(job2);
//        taskList.add(job3);

        return taskList;
    }
}
