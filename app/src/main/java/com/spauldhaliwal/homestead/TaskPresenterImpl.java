package com.spauldhaliwal.homestead;

class TaskPresenterImpl implements TaskPresenter {

    private final TaskAdapterView adapterView;
    private final TasksRepository repository;

    public TaskPresenterImpl(TaskAdapterView view) {
        this.adapterView = view;
        this.repository = new TasksRepository(this);
    }

    @Override
    public void sendTaskToAdapter(JobModel task) {
        adapterView.addItem(task);
    }

    @Override
    public void requestTasks(String homesteadId) {
        repository.request(homesteadId);

    }
}
