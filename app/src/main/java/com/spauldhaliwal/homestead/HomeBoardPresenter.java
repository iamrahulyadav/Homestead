package com.spauldhaliwal.homestead;

import android.support.v7.widget.RecyclerView;

import com.spauldhaliwal.homestead.repositories.TasksRepository;

import java.util.List;

class HomeBoardPresenter {

    private HomeBoardView view;
    private TasksRepository tasksRepository;
    private  TaskAdapter adapter;

    public HomeBoardPresenter(HomeBoardView view, TasksRepository tasksRepository) {
        this.view = view;
        this.tasksRepository = tasksRepository;
        this.adapter = adapter;
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
