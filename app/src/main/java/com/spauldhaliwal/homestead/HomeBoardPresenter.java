package com.spauldhaliwal.homestead;

import android.arch.lifecycle.ViewModel;

import com.spauldhaliwal.homestead.repositories.HomeboardTasksRepository;

import java.util.List;

class HomeBoardPresenter extends ViewModel{

    private HomeBoardView view;
    private HomeboardTasksRepository tasksRepository;

    public HomeBoardPresenter(HomeBoardView view, HomeboardTasksRepository tasksRepository) {
        this.view = view;
        this.tasksRepository = tasksRepository;
    }

    public void loadTasks() {
        List taskList = tasksRepository.getTasks();

        if (taskList.isEmpty()) {
            view.displayNoTasks();
        } else {
            view.displayTasks(taskList);
        }
    }
}
