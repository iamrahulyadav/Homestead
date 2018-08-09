package com.spauldhaliwal.homestead;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

/**
 * Created by pauldhaliwal on 2018-03-12.
 */

public class PersonalBoardFragment extends Fragment {

    private static final String TAG = "HomeBoardFragment";

    DatabaseRecyclerAdapter firebaseAdapter;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private static final String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    Query query;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: starts");
        return inflater.inflate(R.layout.personal_board_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final RecyclerView recyclerView = getActivity().findViewById(R.id.personal_recycler_list_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        query = FirebaseDatabase.getInstance()
                .getReference()
                .child(UsersContract.ROOT_NODE)
                .child(userUid)
                .child(UsersContract.JOBS_NODE)
                .orderByChild(JobsContract.SORT_ORDER);

        FirebaseRecyclerOptions<JobModel> options =
                new FirebaseRecyclerOptions.Builder<JobModel>()
                        .setQuery(query, JobModel.class)
                        .build();

        firebaseAdapter = new DatabaseRecyclerAdapter(options);
        recyclerView.setAdapter(firebaseAdapter);


        ItemClickSupport.addTo(recyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                Log.d(TAG, "onItemClick: item clicked with position " + position + " and id " + position);

                Intent intent = new Intent(getActivity(), newAddEditActivity.class);

                JobModel job = firebaseAdapter.getItem(position);
                Log.d(TAG, "onItemClicked Private: jobId is " + job);
                intent.putExtra(JobsContract.ROOT_NODE, job);
                startActivity(intent);
            }
        });

        Log.d(TAG, "onActivityCreated: adapter set");


    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart: starts");
        firebaseAdapter.startListening();
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        firebaseAdapter.stopListening();
    }


}
