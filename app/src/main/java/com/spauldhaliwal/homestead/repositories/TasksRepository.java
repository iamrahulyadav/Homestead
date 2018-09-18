package com.spauldhaliwal.homestead.repositories;

import android.support.v7.widget.RecyclerView;

import com.spauldhaliwal.homestead.JobModel;
import com.spauldhaliwal.homestead.TaskAdapter;

import java.util.List;

public interface TasksRepository {

    List<JobModel> getTasks();
}
