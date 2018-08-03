package com.spauldhaliwal.homestead;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;


/**
 * Created by pauldhaliwal on 2018-03-12.
 */

public class HomeBoardFragment extends Fragment {
    private static final String TAG = "HomeBoardFragment";

    DatabaseRecyclerAdapter firebaseAdapter;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.home_board_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //TODO Display Homestead name. Implement in CurrentUser utiliy class.
//        TextView homesteadName = getActivity().findViewById(R.id.homestead_task_list_header_text);
//        homesteadName.setText(CurrentUser.getHomesteadUid());

        final RecyclerView recyclerView = getActivity().findViewById(R.id.recycler_list_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child(HomesteadsContract.ROOT_NODE)
                .child(CurrentUser.getHomesteadUid())
                .child(HomesteadsContract.JOBS_NODE)
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

                Intent intent = new Intent(getActivity(), AddEditActivity.class);
                JobModel job = firebaseAdapter.getItem(position);
                intent.putExtra(JobsContract.ROOT_NODE, job);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onStart() {
        firebaseAdapter.startListening();
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        firebaseAdapter.stopListening();
    }

}