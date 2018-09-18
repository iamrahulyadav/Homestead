package com.spauldhaliwal.homestead;

import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.spauldhaliwal.homestead.repositories.impl.DatabaseTasksRepository;

import java.util.List;

public class HomeBoardFragment extends Fragment implements HomeBoardView {
    private static final String TAG = "HomeBoardFragment";

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String homesteadId;
    private String homesteadName;
    private HomeBoardPresenter presenter;
    private RecyclerView recyclerView;
    private TaskAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.home_board_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        //****************** M V P *********************
        SharedPreferences sharedPref = getActivity().getSharedPreferences("com.spauldhaliwal.homestead.SignInActivity.PREFERENCES_FILE_KEY",
                Context.MODE_PRIVATE);
        homesteadId = sharedPref.getString(UsersContract.HOMESTEAD_ID, null);
        homesteadName = sharedPref.getString(UsersContract.HOMESTEAD_NAME, null);
        Log.d(TAG, "HomeboardFragment onActivityCreated: homesteadId: " + homesteadId);
        recyclerView = getActivity().findViewById(R.id.recycler_list_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        presenter = new HomeBoardPresenter(this, new DatabaseTasksRepository(homesteadId, homesteadName));



        presenter.loadTasks();

        //****************** M V P *********************


        TextView homesteadNameTextView = getActivity().findViewById(R.id.homestead_task_list_header_text);
        homesteadNameTextView.setText(homesteadName);

    }

    @Override
    public void displayTasks(List<JobModel> taskList) {
        Log.d(TAG, "displayTasks: found some tasks");
        Log.d(TAG, "displayTasks: taskList: " + taskList.toString());

        adapter = new TaskAdapter(taskList);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void displayNoTasks() {
        Log.d(TAG, "displayNoTasks: found no tasks");

    }



}