package com.spauldhaliwal.homestead;

import com.spauldhaliwal.homestead.repositories.TasksRepository;

import java.util.List;

class HomeBoardPresenter {

    private HomeBoardView view;
    private TasksRepository tasksRepository;

    public HomeBoardPresenter(HomeBoardView view, TasksRepository tasksRepository) {
        this.view = view;
        this.tasksRepository = tasksRepository;
    }


    public void loadTasks() {
        List taskList = tasksRepository.getTasks();
        view.displayTasks(taskList);
    }
}
