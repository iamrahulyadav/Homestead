package com.spauldhaliwal.homestead;

import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class TasksRepository {
    private static final String TAG = "TasksRepository";

    private final TaskPresenter presenter;
    private Query query;

    public TasksRepository(TaskPresenter presenter) {
        this.presenter = presenter;
    }

    public void request(String homesteadId) {

        query = FirebaseDatabase.getInstance()
                .getReference()
                .child(HomesteadsContract.ROOT_NODE)
                .child(homesteadId)
                .child(HomesteadsContract.JOBS_NODE)
                .orderByChild(JobsContract.SORT_ORDER);

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                presenter.sendTaskToAdapter(dataSnapshot.getValue(JobModel.class));
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


    }
}
