package com.spauldhaliwal.homestead;

public interface TaskPresenter {

    void sendTaskToAdapter(JobModel task);
    void requestTasks(String homesteadId);
}
