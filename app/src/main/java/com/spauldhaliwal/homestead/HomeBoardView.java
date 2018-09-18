package com.spauldhaliwal.homestead;

import java.util.List;

public interface HomeBoardView {
    void displayTasks(List<JobModel> taskList);

    void displayNoTasks();

}
