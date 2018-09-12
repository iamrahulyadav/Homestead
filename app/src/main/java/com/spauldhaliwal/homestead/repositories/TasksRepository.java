package com.spauldhaliwal.homestead.repositories;

import com.spauldhaliwal.homestead.JobModel;

import java.util.List;

public interface TasksRepository {

    List<JobModel> getTasks();
}
